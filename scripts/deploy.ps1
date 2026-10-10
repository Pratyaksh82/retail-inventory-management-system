# ==============================================================================
# Retail & Library Inventory Management System - PowerShell Deployment Script
# ==============================================================================
[CmdletBinding()]
param (
    [switch]$RunAfterBuild = $false,
    [switch]$SkipTests = $false
)

$ErrorActionPreference = "Stop"
$WorkspaceRoot = Split-Path -Parent $PSScriptRoot
Set-Location $WorkspaceRoot

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "  BUILDING & PACKAGING RETAIL & LIBRARY INVENTORY SYSTEM (WEEK 5)" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan

# 1. Clean Directories
$BinDir = Join-Path $WorkspaceRoot "bin"
$DistDir = Join-Path $WorkspaceRoot "dist"

if (Test-Path $BinDir) { Remove-Item -Recurse -Force $BinDir }
if (Test-Path $DistDir) { Remove-Item -Recurse -Force $DistDir }
New-Item -ItemType Directory -Path $BinDir | Out-Null
New-Item -ItemType Directory -Path $DistDir | Out-Null

# 2. Compile Java Source Files
Write-Host ">>> Compiling Java source files..." -ForegroundColor Yellow
$LibJar = Join-Path $WorkspaceRoot "lib\junit-platform-console-standalone-1.10.2.jar"
$SourceFiles = Get-ChildItem -Path (Join-Path $WorkspaceRoot "src\main\java") -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
$TestFiles = Get-ChildItem -Path (Join-Path $WorkspaceRoot "src\test\java") -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

& javac -cp "$LibJar;$BinDir" -d $BinDir ($SourceFiles + $TestFiles)
Write-Host "✅ Source files compiled successfully into $BinDir" -ForegroundColor Green

# 3. Execute Tests
if (-not $SkipTests) {
    Write-Host ">>> Executing Automated Test Suite..." -ForegroundColor Yellow
    & java -cp $BinDir com.inventory.library.LibraryTestRunner
    if ($LASTEXITCODE -ne 0) {
        throw "Unit tests failed! Aborting deployment packaging."
    }
    Write-Host "✅ All unit tests passed!" -ForegroundColor Green
}

# 4. Package Executable JAR
Write-Host ">>> Packaging Executable JAR..." -ForegroundColor Yellow
$ManifestFile = Join-Path $DistDir "MANIFEST.MF"
@"
Manifest-Version: 1.0
Main-Class: com.inventory.UnifiedInventorySystem
Created-By: Antigravity Java Builder
Implementation-Title: Retail and Library Inventory Management System
Implementation-Version: 1.0.0-PROD
"@ | Set-Content -Path $ManifestFile -Encoding Ascii

$JarPath = Join-Path $DistDir "retail-inventory-system-1.0.jar"
& jar cvfm $JarPath $ManifestFile -C $BinDir com

Write-Host "✅ Packaged Executable JAR: $JarPath" -ForegroundColor Green
Write-Host "   Size: $((Get-Item $JarPath).Length) bytes" -ForegroundColor Gray

# 5. Summary
Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "  DEPLOYMENT PACKAGE READY FOR PRODUCTION HANDOFF!" -ForegroundColor Cyan
Write-Host "  To run: java -jar dist\retail-inventory-system-1.0.jar" -ForegroundColor White
Write-Host "================================================================================" -ForegroundColor Cyan

if ($RunAfterBuild) {
    & java -jar $JarPath
}
