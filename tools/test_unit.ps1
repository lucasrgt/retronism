param([string]$Worldline='C:/Users/lucas/dev/Worldline')
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot
$runtime=Join-Path $Worldline '.worldline/runtime/legacy-testkit/workspaces/forge'
$testOut=Join-Path $root 'build/unit-tests'
New-Item -ItemType Directory -Force $testOut | Out-Null
$classpath=@((Join-Path $root 'build/classes'),(Join-Path $runtime 'minecraft/bin'),(Join-Path $runtime 'minecraft/jars/deobfuscated.jar'))
$classpath+=@(Get-ChildItem (Join-Path $root 'tests/libs') -Filter *.jar | ForEach-Object FullName)
$classpath+=@(Get-ChildItem (Join-Path $runtime 'libraries') -Recurse -Filter *.jar | ForEach-Object FullName)
$sources=@(Get-ChildItem (Join-Path $root 'tests/src') -Recurse -Filter *Test.java)
& javac --release 8 -Xlint:-options -encoding UTF-8 -cp ($classpath -join ';') -d $testOut @($sources | ForEach-Object FullName)
if($LASTEXITCODE -ne 0) { throw 'Unit test compilation failed' }
$classpath=@($testOut)+$classpath
& java -cp ($classpath -join ';') org.junit.runner.JUnitCore @($sources | ForEach-Object { 'net.minecraft.src.'+$_.BaseName })
if($LASTEXITCODE -ne 0) { throw 'Unit tests failed' }
