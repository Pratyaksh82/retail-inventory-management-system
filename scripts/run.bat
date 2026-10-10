@echo off
REM ============================================================================
REM Retail & Library Inventory Management System - Windows Execution Script
REM ============================================================================

set SCRIPT_DIR=%~dp0..
cd /d "%SCRIPT_DIR%"

echo ============================================================================
echo Starting Unified Retail & Library Inventory Management Platform...
echo ============================================================================

if exist "dist\retail-inventory-system-1.0.jar" (
    java -jar "dist\retail-inventory-system-1.0.jar"
) else (
    echo Building and running from compiled classes...
    javac -cp "lib/*;bin" -d bin src\main\java\com\inventory\**\*.java
    java -cp "bin;lib/*" com.inventory.UnifiedInventorySystem
)

pause
