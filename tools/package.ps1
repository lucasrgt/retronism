param([string]$Worldline='C:/Users/lucas/dev/Worldline', [switch]$SkipBuild)
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot
$runtime=Join-Path $Worldline '.worldline/runtime/legacy-testkit/workspaces/forge'
if (-not $SkipBuild) { & (Join-Path $PSScriptRoot 'build.ps1') -Worldline $Worldline }
Copy-Item -Path (Join-Path $root 'build/classes/*') -Destination (Join-Path $runtime 'minecraft/bin') -Recurse -Force
Push-Location $runtime
try {
    & java -jar RetroMCP-CLI.jar reobfuscate client *> (Join-Path $root 'build/reobfuscate.log')
    if($LASTEXITCODE -ne 0) { throw 'RetroMCP reobfuscation failed' }
} finally { Pop-Location }
$log=Get-Content (Join-Path $root 'build/reobfuscate.log') -Raw
if($log -notmatch 'Finished successfully!' -or $log -match '\[ERROR\]') { throw 'RetroMCP reported incomplete reobfuscation' }
& python (Join-Path $PSScriptRoot 'package_release.py') $runtime
if($LASTEXITCODE -ne 0) { throw 'Release packaging failed' }
