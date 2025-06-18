#!/usr/bin/env bash

set -eu

echo "🚀 Initializing Preview Environment for ET CCD Definitions"
echo "=========================================================="

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
UTILS_DIR="${SCRIPT_DIR}/../utils"

# Set defaults if not provided
PR_NUMBER="${1:-}"
ET_PREVIEW_FLEXI_DB_PASSWORD="${2:-}"

if [[ -z "${PR_NUMBER}" ]]; then
    echo "❌ Error: PR number is required"
    echo "Usage: $0 PR_NUMBER [ET_PREVIEW_FLEXI_DB_PASSWORD]"
    exit 1
fi

echo "🔧 PR Number: ${PR_NUMBER}"

# Set preview environment URLs
export CCD_DEFINITION_STORE_API_BASE_URL="https://ccd-definition-store-api-et-ccd-definitions-admin-pr-${PR_NUMBER}.preview.platform.hmcts.net"
export CCD_DATA_STORE_API_BASE_URL="https://ccd-data-store-api-et-ccd-definitions-admin-pr-${PR_NUMBER}.preview.platform.hmcts.net"
export CCD_USER_PROFILE_API_BASE_URL="https://ccd-user-profile-api-et-ccd-definitions-admin-pr-${PR_NUMBER}.preview.platform.hmcts.net"
export IDAM_API_BASE_URL="https://idam-api.aat.platform.hmcts.net"
export SERVICE_AUTH_PROVIDER_API_BASE_URL="https://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
export ET_COS_URL="https://et-cos-et-ccd-definitions-admin-pr-${PR_NUMBER}.preview.platform.hmcts.net"

# Admin credentials from environment
export CCD_ADMIN_USERNAME="${CCD_ADMIN_USERNAME:-}"
export CCD_ADMIN_PASSWORD="${CCD_ADMIN_PASSWORD:-}"

if [[ -z "${CCD_ADMIN_USERNAME}" || -z "${CCD_ADMIN_PASSWORD}" ]]; then
    echo "❌ Error: CCD_ADMIN_USERNAME and CCD_ADMIN_PASSWORD must be set"
    exit 1
fi

echo "🔐 Setting up authentication..."

# Step 1: Import reference data (if password provided)
if [[ -n "${ET_PREVIEW_FLEXI_DB_PASSWORD}" ]]; then
    echo "📊 Importing reference data..."
    "${SCRIPT_DIR}/import-ref-data.sh" "${PR_NUMBER}" "${ET_PREVIEW_FLEXI_DB_PASSWORD}"
fi

# Step 2: Create CCD roles
echo "👥 Creating CCD roles..."
"${SCRIPT_DIR}/create-ccd-roles.sh"

# Step 3: Generate Excel configurations for preview
echo "📋 Generating Excel configurations..."
"${SCRIPT_DIR}/generate-preview-configs.sh" "${PR_NUMBER}"

# Step 4: Import CCD definitions
echo "📥 Importing CCD definitions..."
"${SCRIPT_DIR}/import-ccd-definitions.sh" "${PR_NUMBER}"

# Step 5: Create ECM Admin case
echo "📋 Creating ECM Admin case..."
"${SCRIPT_DIR}/create-admin-case.sh"

# Step 6: Add organizational roles to users
echo "🏢 Adding organizational roles to users..."
"${SCRIPT_DIR}/add-org-roles-to-users.sh"

echo "✅ Preview environment initialization complete!"
echo "🌐 Access URLs:"
echo "   - ExUI: https://xui-et-ccd-definitions-admin-pr-${PR_NUMBER}.preview.platform.hmcts.net"
echo "   - CCD Admin: https://admin-web-et-ccd-definitions-admin-pr-${PR_NUMBER}.preview.platform.hmcts.net"
echo "   - ET COS: https://et-cos-et-ccd-definitions-admin-pr-${PR_NUMBER}.preview.platform.hmcts.net"

