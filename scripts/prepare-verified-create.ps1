#requires -Version 5.1
<#
.SYNOPSIS
  Prepare local Maven coordinates from a checksum-verified official Create
  Modrinth release. Does not disable HTTPS verification or change build.gradle.
.EXAMPLE
  powershell.exe -NoProfile -ExecutionPolicy RemoteSigned -File .\scripts\prepare-verified-create.ps1
#>
[CmdletBinding()]
param()

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$projectRoot = Split-Path -Parent $PSScriptRoot
$base = Join-Path $projectRoot '.gradle\zeppelin-verified-create'
$repository = Join-Path $base 'repository'
$officialJar = Join-Path $base 'create-1.21.1-6.0.10.jar'
$version = '6.0.10+mc1.21.1'
$headers = @{ 'User-Agent' = 'ZeppelinMustHave-VerifiedReleaseBuilder/1.0' }

function Save-MavenArtifact(
    [string] $group,
    [string] $name,
    [string] $versionName,
    [string] $sourceJar,
    [string] $classifier = ''
) {
    $groupPath = $group.Replace('.', [System.IO.Path]::DirectorySeparatorChar)
    $directory = Join-Path (Join-Path (Join-Path $repository $groupPath) $name) $versionName
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $stem = "$name-$versionName"
    Copy-Item -LiteralPath $sourceJar -Destination (Join-Path $directory "$stem.jar") -Force
    if ($classifier) {
        Copy-Item -LiteralPath $sourceJar -Destination (Join-Path $directory "$stem-$classifier.jar") -Force
    }

    $pom = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>$group</groupId><artifactId>$name</artifactId>
  <version>$versionName</version><packaging>jar</packaging>
</project>
"@
    $pomFile = Join-Path $directory "$stem.pom"
    [IO.File]::WriteAllText($pomFile, $pom, [Text.UTF8Encoding]::new($false))
    $null = [xml] $pom
    Write-Host ("Prepared " + $group + ":" + $name + ":" + $versionName)
}

$versions = @(Invoke-RestMethod -Uri 'https://api.modrinth.com/v2/project/LNytGWDc/version' -Headers $headers -TimeoutSec 35)
$release = $versions | Where-Object { $_.version_number -eq $version } | Select-Object -First 1
if (-not $release) { throw "Official Create release $version is unavailable on Modrinth." }
$file = $release.files | Where-Object { $_.filename -eq 'create-1.21.1-6.0.10.jar' } | Select-Object -First 1
if (-not $file -or -not $file.hashes.sha512) {
    throw 'Official release JAR or published SHA-512 hash was not found.'
}
New-Item -ItemType Directory -Force -Path $base | Out-Null
if (-not (Test-Path $officialJar) -or
    (Get-FileHash -Path $officialJar -Algorithm SHA512).Hash.ToLowerInvariant() -ne $file.hashes.sha512.ToLowerInvariant()) {
    Invoke-WebRequest -UseBasicParsing -Uri $file.url -OutFile $officialJar -TimeoutSec 100
}
$sha512 = (Get-FileHash -Path $officialJar -Algorithm SHA512).Hash.ToLowerInvariant()
if ($sha512 -ne $file.hashes.sha512.ToLowerInvariant()) {
    throw 'Official release JAR SHA-512 mismatch. Refusing to build.'
}
Write-Host "Verified official Create release SHA-512: $sha512"

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($officialJar)
try {
    $bundled = @{
        'flywheel-neoforge-1.21.1-1.0.6.jar' = 'META-INF/jarjar/flywheel-neoforge-1.21.1-1.0.6.jar'
        'ponder-neoforge-1.0.82+mc1.21.1.jar' = 'META-INF/jarjar/ponder-neoforge-1.0.82+mc1.21.1.jar'
        'Registrate-MC1.21-1.3.0+67.jar' = 'META-INF/jarjar/Registrate-MC1.21-1.3.0+67.jar'
    }
    foreach ($name in $bundled.Keys) {
        $entry = $zip.GetEntry($bundled[$name])
        if (-not $entry) { throw "Official release does not embed $name." }
        [System.IO.Compression.ZipFileExtensions]::ExtractToFile(
            $entry, (Join-Path $base $name), $true
        )
    }
}
finally {
    $zip.Dispose()
}

Save-MavenArtifact 'com.simibubi.create' 'create-1.21.1' '6.0.10-280' $officialJar 'slim'
Save-MavenArtifact 'net.createmod.ponder' 'ponder-neoforge' '1.0.82+mc1.21.1' (Join-Path $base 'ponder-neoforge-1.0.82+mc1.21.1.jar')
Save-MavenArtifact 'dev.engine-room.flywheel' 'flywheel-neoforge-1.21.1' '1.0.6' (Join-Path $base 'flywheel-neoforge-1.21.1-1.0.6.jar')
# Full bundled Flywheel includes dev.engine_room.flywheel.api classes.
Save-MavenArtifact 'dev.engine-room.flywheel' 'flywheel-neoforge-api-1.21.1' '1.0.6' (Join-Path $base 'flywheel-neoforge-1.21.1-1.0.6.jar')
Save-MavenArtifact 'com.tterrag.registrate' 'Registrate' 'MC1.21-1.3.0+67' (Join-Path $base 'Registrate-MC1.21-1.3.0+67.jar')
Write-Host 'Verified local Maven cache is ready. Build with -I scripts/verified-create.init.gradle.'
