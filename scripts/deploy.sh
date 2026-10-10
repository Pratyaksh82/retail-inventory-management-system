#!/usr/bin/env bash
# ==============================================================================
# Retail & Library Inventory Management System - Linux / macOS Deployment Script
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$WORKSPACE_ROOT"

echo "================================================================================"
echo "  BUILDING & PACKAGING RETAIL & LIBRARY INVENTORY SYSTEM (WEEK 5)"
echo "================================================================================"

BIN_DIR="$WORKSPACE_ROOT/bin"
DIST_DIR="$WORKSPACE_ROOT/dist"

rm -rf "$BIN_DIR" "$DIST_DIR"
mkdir -p "$BIN_DIR" "$DIST_DIR"

echo ">>> Compiling Java sources..."
LIB_JAR="$WORKSPACE_ROOT/lib/junit-platform-console-standalone-1.10.2.jar"
find src -name "*.java" > "$DIST_DIR/sources.txt"
javac -cp "$LIB_JAR:$BIN_DIR" -d "$BIN_DIR" @"$DIST_DIR/sources.txt"
rm -f "$DIST_DIR/sources.txt"

echo ">>> Running automated test harness..."
java -cp "$BIN_DIR" com.inventory.library.LibraryTestRunner

echo ">>> Packaging standalone Executable JAR..."
cat << 'EOF' > "$DIST_DIR/MANIFEST.MF"
Manifest-Version: 1.0
Main-Class: com.inventory.UnifiedInventorySystem
Created-By: Antigravity Java Builder
Implementation-Title: Retail and Library Inventory Management System
Implementation-Version: 1.0.0-PROD
EOF

JAR_PATH="$DIST_DIR/retail-inventory-system-1.0.jar"
jar cvfm "$JAR_PATH" "$DIST_DIR/MANIFEST.MF" -C "$BIN_DIR" com

echo "================================================================================"
echo "  DEPLOYMENT PACKAGE GENERATED: $JAR_PATH"
echo "  Run via: java -jar dist/retail-inventory-system-1.0.jar"
echo "================================================================================"
