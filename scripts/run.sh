#!/usr/bin/env bash
# ==============================================================================
# Retail & Library Inventory Management System - Linux / macOS Run Script
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORKSPACE_ROOT="$(dirname "$SCRIPT_DIR")"
cd "$WORKSPACE_ROOT"

if [[ -f "dist/retail-inventory-system-1.0.jar" ]]; then
    exec java -jar "dist/retail-inventory-system-1.0.jar" "$@"
else
    echo "Executable JAR not found. Running deploy script first..."
    bash scripts/deploy.sh
    exec java -jar "dist/retail-inventory-system-1.0.jar" "$@"
fi
