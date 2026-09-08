param([string]$Worldline='C:/Users/lucas/dev/Worldline',
      [string]$Java8='C:/Users/lucas/dev/mc-b173-h01-retromcp-world/tools/zulu8.96.0.19-ca-jdk8.0.502-win_x64',
      [switch]$Obfuscated,
      [ValidateRange(1,4)][int]$NetworkOrientations=4)
$ErrorActionPreference='Stop'
$projectRoot=Split-Path $PSScriptRoot
& python (Join-Path $projectRoot 'tests/test_refinery_ports.py')
if($LASTEXITCODE -ne 0) { throw 'Refinery connector asset proof failed' }
$runtimeRoot=Join-Path $Worldline '.worldline/runtime/legacy-testkit/workspaces/forge'
& (Join-Path $PSScriptRoot 'build.ps1') -Worldline $Worldline -Loader forge
& (Join-Path $PSScriptRoot 'test_unit.ps1') -Worldline $Worldline
& python (Join-Path $Worldline 'skills/b1-7-3-anti-slop/scripts/audit_side_safety.py') --project $projectRoot --common-root src/main/java --client-prefix refinery.client
if($LASTEXITCODE -ne 0) { throw 'Common-side audit failed' }
$gameBin=Join-Path $runtimeRoot 'minecraft/bin'
$gameClasspath=@((Join-Path $projectRoot 'build/classes'),$gameBin,(Join-Path $runtimeRoot 'minecraft/jars/deobfuscated.jar'))
$gameClasspath+=@(Get-ChildItem (Join-Path $runtimeRoot 'libraries') -Recurse -Filter *.jar | ForEach-Object FullName)
$fixtureBin=Join-Path $projectRoot 'build/runtime-tests'
New-Item -ItemType Directory -Force $fixtureBin | Out-Null
$fixtureSources=@(Get-ChildItem (Join-Path $projectRoot 'tests/runtime/java') -Recurse -Filter *.java | ForEach-Object FullName)
& javac --release 8 -encoding UTF-8 -cp ($gameClasspath -join ';') -d $fixtureBin @fixtureSources
if($LASTEXITCODE -ne 0) { throw 'Runtime fixture compilation failed' }
# Only the prepared ignored test runtime receives the product and opt-in test fixture.
Copy-Item -Path (Join-Path $projectRoot 'build/classes/*') -Destination $gameBin -Recurse -Force
Copy-Item -Path (Join-Path $fixtureBin '*') -Destination $gameBin -Recurse -Force
$bundle=Join-Path $projectRoot 'build/runtime-bundle'
New-Item -ItemType Directory -Force $bundle | Out-Null
Copy-Item -Path (Join-Path $projectRoot 'build/classes/*'),(Join-Path $fixtureBin '*') -Destination $bundle -Recurse -Force
$probe=Join-Path $runtimeRoot 'minecraft/game/mods/worldline/net/minecraft/src/mod_WorldlineTestKitProbe.class'
Copy-Item -LiteralPath $probe -Destination (Join-Path $bundle 'net/minecraft/src/mod_WorldlineTestKitProbe.class') -Force
# Legacy ModLoader discovers root mod_ entries, then loads their mapped packaged class.
Get-ChildItem (Join-Path $bundle 'net/minecraft/src') -Filter mod_*.class | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $bundle $_.Name) -Force
}
& jar cf (Join-Path $runtimeRoot 'refinery-proof.jar') -C $bundle .
if($LASTEXITCODE -ne 0) { throw 'Runtime bundle packaging failed' }
$manifestPath=Join-Path $runtimeRoot 'worldline-testkit.properties'
$manifest=(Get-Content -LiteralPath $manifestPath -Raw) -replace '(?m)^probe.source=.*','probe.source=refinery-proof.jar' -replace '(?m)^probe.target=.*','probe.target=mods/refinery-proof.jar'
[IO.File]::WriteAllText($manifestPath,$manifest)
if($Obfuscated) {
    & (Join-Path $PSScriptRoot 'package.ps1') -Worldline $Worldline -SkipBuild
    $runtimeRoot=Join-Path $projectRoot '.local/obfuscated-runtime'
}
$worldlineClasspath=@(Get-ChildItem (Join-Path $Worldline '.worldline/build/classes') -Directory | ForEach-Object FullName)
$worldlineClasspath+=(Join-Path $Worldline '.worldline/build/adapter-classes/modloader-forge-testkit')
$testBin=Join-Path $projectRoot 'build/worldline-tests'
New-Item -ItemType Directory -Force $testBin | Out-Null
$testSources=@(Get-ChildItem (Join-Path $projectRoot 'tests/worldline/java') -Recurse -Filter *.java | ForEach-Object FullName)
& javac --release 21 -encoding UTF-8 -cp ($worldlineClasspath -join ';') -d $testBin @testSources
if($LASTEXITCODE -ne 0) { throw 'Worldline extension compilation failed' }
$run=Join-Path $projectRoot ('build/proofs/'+[guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force $run | Out-Null
$env:REFINERY_PROOF_ROOT=$run
$env:REFINERY_NETWORK_ROTATIONS=[string]$NetworkOrientations
$worldlineClasspath+=$testBin
& java "-Dworldline.legacy.forge.workspace=$runtimeRoot" "-Dworldline.legacy.java8Home=$Java8" -cp ($worldlineClasspath -join ';') refinery.proof.RunProof
if($LASTEXITCODE -ne 0) { throw "Worldline runtime proof failed; evidence: $run" }
[IO.File]::WriteAllText((Join-Path $projectRoot 'build/latest-proof.txt'),$run)
