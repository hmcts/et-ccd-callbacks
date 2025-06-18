#!/usr/bin/env bash

set -eu

echo "📋 Generating Preview Excel Configurations"
echo "=========================================="

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="${SCRIPT_DIR}/../.."
CCD_DEFINITIONS_DIR="${REPO_ROOT}/ccd-definitions"

# Get PR number
PR_NUMBER="${1:-}"

if [[ -z "${PR_NUMBER}" ]]; then
    echo "❌ Error: PR number is required"
    echo "Usage: $0 PR_NUMBER"
    exit 1
fi

echo "🔧 PR Number: ${PR_NUMBER}"

# Ensure we're in the right directory
cd "${CCD_DEFINITIONS_DIR}"

echo "📋 Generating Admin Excel configuration..."
if command -v yarn >/dev/null 2>&1; then
    yarn setup
    yarn generate-excel-preview "${PR_NUMBER}"
else
    echo "❌ Error: yarn not found. Please install yarn first."
    exit 1
fi

echo "📋 Generating England-Wales Excel configuration..."
cd "${CCD_DEFINITIONS_DIR}/jurisdictions/england-wales"
if [[ -f "package.json" ]]; then
    # Set up dependencies if needed
    if [[ ! -d "node_modules" ]]; then
        yarn install
    fi
    yarn generate-excel-preview "${PR_NUMBER}"
else
    echo "❌ Error: package.json not found in England-Wales jurisdiction"
    exit 1
fi

echo "📋 Generating Scotland Excel configuration..."
cd "${CCD_DEFINITIONS_DIR}/jurisdictions/scotland"
if [[ -f "package.json" ]]; then
    # Set up dependencies if needed
    if [[ ! -d "node_modules" ]]; then
        yarn install
    fi
    yarn generate-excel-preview "${PR_NUMBER}"
else
    echo "❌ Error: package.json not found in Scotland jurisdiction"
    exit 1
fi

# Return to CCD definitions directory
cd "${CCD_DEFINITIONS_DIR}"

echo "🔍 Verifying generated files..."

# Check if all expected files exist
declare -a EXPECTED_FILES=(
    "definitions/xlsx/et-admin-ccd-config-preview.xlsx"
    "jurisdictions/england-wales/definitions/xlsx/et-englandwales-ccd-config-preview.xlsx" 
    "jurisdictions/scotland/definitions/xlsx/et-scotland-ccd-config-preview.xlsx"
)

for file_path in "${EXPECTED_FILES[@]}"; do
    if [[ -f "${file_path}" ]]; then
        local file_size=$(du -h "${file_path}" | cut -f1)
        echo "  ✅ Generated: $(basename "${file_path}") (${file_size})"
    else
        echo "  ❌ Missing: ${file_path}"
        exit 1
    fi
done

echo "✅ Preview Excel configurations generated successfully!"

# Display summary
echo ""
echo "📊 Generated Files Summary:"
echo "   📁 Admin: definitions/xlsx/et-admin-ccd-config-preview.xlsx"
echo "   📁 England-Wales: jurisdictions/england-wales/definitions/xlsx/et-englandwales-ccd-config-preview.xlsx"
echo "   📁 Scotland: jurisdictions/scotland/definitions/xlsx/et-scotland-ccd-config-preview.xlsx"

