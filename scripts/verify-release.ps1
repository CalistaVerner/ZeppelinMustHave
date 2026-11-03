#requires -Version 5.1
<#
.SYNOPSIS
    Verifies Zeppelin Must Have without bypassing TLS validation.
.EXAMPLE
    .\scripts\verify-release.ps1 -Stage full
.EXAMPLE
    .\scripts\verify-release.ps1 -Stage server
#>
[CmdletBinding()]
param(
    [ValidateSet('full', 'build', 'gametest', 'server', 'client')]
    [string] $Stage = 'full',
    [switch] $UseVerifiedCreateBundle
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$gradleWrapper = Join-Path $projectRoot 'gradlew.bat'
$gradleProperties = Join-Path $projectRoot 'gradle.properties'
$createPom = 'https://maven.createmod.net/com/simibubi/create/create-1.21.1/6.0.10-280/create-1.21.1-6.0.10-280.pom'

function Find-Jdk21 {
    if ($env:JAVA_HOME) {
        $exe = Join-Path $env:JAVA_HOME 'bin\javac.exe'
        if (Test-Path $exe) { return $env:JAVA_HOME }
    }

    $java = Get-Command javac.exe -ErrorAction SilentlyContinue
    if ($java) {
        return Split-Path -Parent (Split-Path -Parent $java.Source)
    }

    $jdks = Join-Path $HOME '.gradle\jdks'
    if (Test-Path $jdks) {
        foreach ($directory in (Get-ChildItem $jdks -Directory -ErrorAction SilentlyContinue)) {
            if (Test-Path (Join-Path $directory.FullName 'bin\javac.exe')) {
                $detected = & (Join-Path $directory.FullName 'bin\javac.exe') -version 2>&1 | Out-String
                if ($detected -match '\b21(?:\.|\s)') { return $directory.FullName }
            }
        }
    }

    throw 'Java JDK 21 was not found. Install JDK 21 or configure JAVA_HOME.'
}

function Invoke-Gradle([string] $Task) {
    Write-Host "Running Gradle task: $Task"
    $arguments = @($Task, '--no-daemon', '--console=plain', '--stacktrace')
    if ($UseVerifiedCreateBundle) {
        $arguments = @('-I', (Join-Path $PSScriptRoot 'verified-create.init.gradle')) + $arguments
    }
    & $gradleWrapper @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle task '$Task' failed (exit code $LASTEXITCODE)."
    }
}

function Test-CreateRepository {
    Write-Host 'Checking official Create Maven HTTPS certificate...'
    try {
        Invoke-WebRequest -UseBasicParsing -Method Head -TimeoutSec 20 -Uri $createPom | Out-Null
    }
    catch {
        throw ("Create Maven HTTPS verification failed: " + $_.Exception.Message +
            ". An expired upstream certificate must be renewed by its operator. " +
            "Do not disable TLS validation or import an expired certificate.")
    }
}

function Assert-GameTestResult {
    $logFile = Join-Path $projectRoot 'run-gametest\logs\latest.log'
    if (-not (Test-Path $logFile)) {
        throw 'GameTest log not found: the test server did not start.'
    }
    $contents = Get-Content -LiteralPath $logFile -Raw
    if ($contents -match 'Failed to start the minecraft server|Mod loading failures have occurred|ModLoadingException|ModLoadingCrashException') {
        throw 'GameTest startup FAILED: see run-gametest/logs/latest.log. Gradle may report BUILD SUCCESSFUL despite a Minecraft startup crash.'
    }
    if ($contents -notmatch '(?i)(game.?tests? (?:complete|finished|passed)|all tests passed|[1-9][0-9]* (?:tests|gametests) passed)') {
        throw 'GameTest execution was not confirmed in the server log; release validation remains incomplete.'
    }
}

Push-Location $projectRoot
try {
    if (-not (Test-Path $gradleWrapper)) { throw 'Gradle wrapper is missing.' }
    $env:JAVA_HOME = Find-Jdk21
    $env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:Path
    $javacVersion = & (Join-Path $env:JAVA_HOME 'bin\javac.exe') -version 2>&1 | Out-String
    if ($javacVersion -notmatch '\b21(?:\.|\s)') {
        throw "Expected JDK 21; found: $javacVersion"
    }
    Write-Host "Using $($env:JAVA_HOME): $($javacVersion.Trim())"

    if ($UseVerifiedCreateBundle) {
        & (Join-Path $PSScriptRoot 'prepare-verified-create.ps1')
    }
    else {
        Test-CreateRepository
    }

    switch ($Stage) {
        'full' {
            Invoke-Gradle 'build'
            Invoke-Gradle 'runGameTestServer'
            Assert-GameTestResult
        }
        'build'    { Invoke-Gradle 'build' }
        'gametest' {
            Invoke-Gradle 'runGameTestServer'
            Assert-GameTestResult
        }
        'server'   {
            Write-Host 'Starting dedicated server. Minecraft EULA must be accepted separately.'
            Invoke-Gradle 'runServer'
        }
        'client'   {
            Write-Host 'Starting interactive Minecraft client; close the game to complete.'
            Invoke-Gradle 'runClient'
        }
    }

    if ($Stage -in @('full', 'build')) {
        $props = @{}
        Get-Content $gradleProperties | ForEach-Object {
            if ($_ -match '^\s*([^#\s=]+)\s*=\s*(.+)$') { $props[$matches[1]] = $matches[2].Trim() }
        }
        $expectedJar = Join-Path $projectRoot ("build\libs\{0}-{1}.jar" -f $props['mod_id'], $props['mod_version'])
        if (-not (Test-Path $expectedJar)) {
            throw "Expected release JAR is missing: $expectedJar"
        }
        $artifact = Get-Item $expectedJar
        if ($artifact.Length -le 0) { throw "Release JAR is empty: $expectedJar" }
        $hash = Get-FileHash -Algorithm SHA256 -Path $expectedJar
        Write-Host "Verified artifact: $($artifact.FullName)"
        Write-Host "Size: $($artifact.Length) bytes | SHA256: $($hash.Hash)"
    }
    Write-Host "Verification stage '$Stage' completed successfully."
}
finally {
    Pop-Location
}
