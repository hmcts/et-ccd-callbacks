#!/usr/bin/env bash

set -eu

echo "🚀 ET CCD Preview Environment Setup"
echo "===================================="
echo ""

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Check for required environment variables
required_vars=(
    "CCD_DEFINITION_STORE_API_BASE_URL"
    "CASE_USER_PROFILE_API_BASE_URL"
    "CCD_DEF_CASE_SERVICE_BASE_URL"
    "IDAM_API_BASE_URL"
    "S2S_BASE_URL"
    "IDAM_ADMIN_WEB_SERVICE_KEY"
    "IDAM_ADMIN_SECRET"
    "S2S_SECRET"
)

echo "🔍 Checking required environment variables..."
missing_vars=()
for var in "${required_vars[@]}"; do
    if [[ -z "${!var:-}" ]]; then
        missing_vars+=("$var")
    else
        echo "  ✅ $var is set"
    fi
done

if [[ ${#missing_vars[@]} -gt 0 ]]; then
    echo ""
    echo "❌ Missing required environment variables:"
    for var in "${missing_vars[@]}"; do
        echo "  - $var"
    done
    echo ""
    echo "Please set these variables and try again."
    echo "See docs/preview-environment.md for setup instructions."
    exit 1
fi

echo ""
echo "🎯 Starting preview environment setup..."
echo ""

# Step 1: Create CCD roles
echo "📝 Step 1: Creating CCD roles..."
if "${SCRIPT_DIR}/create-ccd-roles.sh"; then
    echo "  ✅ CCD roles created successfully"
else
    echo "  ❌ Failed to create CCD roles"
    exit 1
fi

echo ""

# Step 2: Import CCD definitions  
echo "📋 Step 2: Importing CCD definitions..."
if "${SCRIPT_DIR}/import-ccd-definitions.sh"; then
    echo "  ✅ CCD definitions imported successfully"
else
    echo "  ❌ Failed to import CCD definitions"
    exit 1
fi

echo ""

# Step 3: Add organizational roles to users
echo "👥 Step 3: Adding organizational roles to users..."
if "${SCRIPT_DIR}/add-org-roles-to-users.sh"; then
    echo "  ✅ Organizational roles added successfully"
else
    echo "  ❌ Failed to add organizational roles"
    exit 1
fi

echo ""

# Step 4: Create admin cases
echo "📁 Step 4: Creating admin cases..."
if "${SCRIPT_DIR}/create-admin-case.sh"; then
    echo "  ✅ Admin cases created successfully"
else
    echo "  ❌ Failed to create admin cases"
    exit 1
fi

echo ""

# Step 5: Import reference data
echo "📊 Step 5: Importing reference data..."
if "${SCRIPT_DIR}/import-ref-data.sh"; then
    echo "  ✅ Reference data imported successfully"
else
    echo "  ❌ Failed to import reference data"
    exit 1
fi

echo ""
echo "🎉 Preview environment setup completed successfully!"
echo ""
echo "Next steps:"
echo "1. Access ExUI at your preview environment URL"
echo "2. Log in with your IDAM AAT credentials"
echo "3. Test ET case creation and management"
echo ""
echo "See docs/preview-environment.md for login URLs and additional information."

