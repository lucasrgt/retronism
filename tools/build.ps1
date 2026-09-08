param(
    [string]$Worldline = 'C:/Users/lucas/dev/Worldline',
    [ValidateSet('modloader','forge')][string]$Loader = 'forge',
    [string]$AeroModelLib = 'C:/Users/lucas/dev/aero-model-lib',
    [string]$MachineApi = 'C:/Users/lucas/dev/aero-machine-api'
)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot
$runtimeRoot = Join-Path $Worldline ".worldline/runtime/legacy-testkit/workspaces/$Loader"
foreach ($name in @('classes','aero-classes','generated-java')) {
    $owned = [IO.Path]::GetFullPath((Join-Path $projectRoot "build/$name"))
    $boundary = [IO.Path]::GetFullPath((Join-Path $projectRoot 'build')) + [IO.Path]::DirectorySeparatorChar
    if (-not $owned.StartsWith($boundary, [StringComparison]::OrdinalIgnoreCase)) { throw 'Build cleanup escaped its directory' }
    if (Test-Path -LiteralPath $owned) { Remove-Item -LiteralPath $owned -Recurse -Force }
}
$classes = Join-Path $projectRoot 'build/classes'
New-Item -ItemType Directory -Force $classes | Out-Null
$libraries = @(Get-ChildItem (Join-Path $runtimeRoot 'libraries') -Recurse -Filter *.jar | Where-Object Name -NotLike '*-sources.jar' | ForEach-Object FullName)
$classpath = (@((Join-Path $runtimeRoot 'minecraft/bin'),(Join-Path $runtimeRoot 'minecraft/jars/deobfuscated.jar')) + $libraries) -join ';'
$aeroRevision = (& git -C $AeroModelLib rev-parse HEAD).Trim()
$dependencyPins = ConvertFrom-StringData (Get-Content (Join-Path $projectRoot 'dependencies.properties') -Raw)
if ($aeroRevision -ne $dependencyPins['aeromodellib.revision']) { throw 'AeroModelLib revision differs from dependencies.properties' }
$apiRevision = (& git -C $MachineApi rev-parse HEAD).Trim()
if ($apiRevision -ne $dependencyPins['machineapi.revision']) { throw 'Machine API revision differs from dependencies.properties' }
& git -C $MachineApi diff --quiet HEAD -- '*.java'
if ($LASTEXITCODE -ne 0) { throw 'Machine API dependency has unpinned source edits' }
& git -C $AeroModelLib diff --quiet HEAD -- core modloader/aero
if ($LASTEXITCODE -ne 0) { throw 'AeroModelLib dependency has unpinned source edits' }
$aeroClasses = Join-Path $projectRoot 'build/aero-classes'
New-Item -ItemType Directory -Force $aeroClasses | Out-Null
$aeroSources = @(Get-ChildItem (Join-Path $AeroModelLib 'core'),(Join-Path $AeroModelLib 'modloader/aero') -Recurse -Filter *.java | ForEach-Object FullName)
$aeroArgs = @('--release','8','-Xlint:-options','-encoding','UTF-8','-cp',('"'+($classpath -replace '\\','/')+'"'),'-d',('"'+($aeroClasses -replace '\\','/')+'"'))
$aeroArgs += @($aeroSources | ForEach-Object { '"'+($_ -replace '\\','/')+'"' })
$aeroArgFile = Join-Path $projectRoot 'build/aero-javac.args'
[IO.File]::WriteAllLines($aeroArgFile,$aeroArgs)
& javac "@$aeroArgFile"
if ($LASTEXITCODE -ne 0) { throw 'AeroModelLib dependency compilation failed' }
$classpath += ';'+$aeroClasses
$apiSources = @(Get-ChildItem $MachineApi -Filter *.java | ForEach-Object FullName)
& javac --release 8 -encoding UTF-8 -cp $classpath -d $aeroClasses @apiSources
if ($LASTEXITCODE -ne 0) { throw 'Machine API compilation failed' }
& python (Join-Path $PSScriptRoot 'prepare_sources.py')
if ($LASTEXITCODE -ne 0) { throw 'Source preparation failed' }
$sources = @(Get-ChildItem (Join-Path $projectRoot 'build/generated-java'),(Join-Path $projectRoot 'src/main/java'),(Join-Path $projectRoot 'src/client/java') -Recurse -Filter *.java | ForEach-Object FullName)
& javac --release 8 -Xmaxerrs 30 -Xlint:-options -encoding UTF-8 -cp $classpath -d $classes @sources
if ($LASTEXITCODE -ne 0) { throw 'Mod compilation failed' }
Copy-Item -Path (Join-Path $projectRoot 'src/main/resources/*') -Destination $classes -Recurse -Force
Copy-Item -Path (Join-Path $projectRoot 'src/retronism/assets/*') -Destination $classes -Recurse -Force
Copy-Item -Path (Join-Path $aeroClasses '*') -Destination $classes -Recurse -Force
Copy-Item -LiteralPath (Join-Path $AeroModelLib 'LICENSE.md') -Destination (Join-Path $classes 'AEROMODELLIB-LICENSE.md') -Force
New-Item -ItemType Directory -Force (Join-Path $projectRoot 'dist') | Out-Null
& jar cf (Join-Path $projectRoot 'dist/retronism-0.2.0-dev.jar') -C $classes .
if ($LASTEXITCODE -ne 0) { throw 'Mod packaging failed' }
Write-Output 'Compiled development mod; release jar requires reobfuscation.'
