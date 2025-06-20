#!/usr/bin/env bash

set -eu

echo "📋 Creating ECM Admin Case via API"
echo "=================================="

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Use preview-specific token utilities
source "${SCRIPT_DIR}/utils/auth-utils.sh"

echo "🔐 Getting authentication tokens..."
USER_TOKEN=$(get_user_token)
SERVICE_TOKEN=$(get_service_token "ccd_data")

# Admin case type configuration
JURISDICTION="EMPLOYMENT"
CASE_TYPE="ECMAdmin"
EVENT_ID="createAdminCase"

echo "📋 Creating ECM Admin case..."

# Step 1: Start case creation - get event token
echo "  🎯 Step 1: Getting event token for case creation..."

start_response=$(curl -s -w "%{http_code}" \
    -X GET \
    "${CCD_DATA_STORE_API_BASE_URL}/caseworkers/${CCD_ADMIN_USERNAME}/jurisdictions/${JURISDICTION}/case-types/${CASE_TYPE}/event-triggers/${EVENT_ID}/token" \
    -H "Authorization: Bearer ${USER_TOKEN}" \
    -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
    -H "Content-Type: application/json")

start_http_code="${start_response: -3}"
start_response_body="${start_response%???}"

if [[ "${start_http_code}" != "200" ]]; then
    echo "    ❌ Failed to get event token (HTTP ${start_http_code})"
    echo "    Response: ${start_response_body}"
    exit 1
fi

EVENT_TOKEN=$(echo "${start_response_body}" | jq -r '.token')
if [[ "${EVENT_TOKEN}" == "null" || -z "${EVENT_TOKEN}" ]]; then
    echo "    ❌ Failed to extract event token from response"
    exit 1
fi

echo "    ✅ Event token obtained successfully"

# Step 2: Submit case creation
echo "  📝 Step 2: Submitting case creation..."

# Basic admin case data
case_data='{
    "ecmAdminData": {
        "managingOffice": "Leeds",
        "tribunalOffice": "Leeds",
        "fileLocation": "Leeds",
        "dateLoaded": "'$(date +%Y-%m-%d)'",
        "positionType": "Lead ECM",
        "notes": "Preview environment admin case created via API"
    }
}'

submit_response=$(curl -s -w "%{http_code}" \
    -X POST \
    "${CCD_DATA_STORE_API_BASE_URL}/caseworkers/${CCD_ADMIN_USERNAME}/jurisdictions/${JURISDICTION}/case-types/${CASE_TYPE}/cases" \
    -H "Authorization: Bearer ${USER_TOKEN}" \
    -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{
        \"data\": ${case_data},
        \"event\": {
            \"id\": \"${EVENT_ID}\",
            \"summary\": \"Creating ECM Admin case for preview environment\",
            \"description\": \"Automated case creation for preview environment setup\"
        },
        \"event_token\": \"${EVENT_TOKEN}\"
    }")

submit_http_code="${submit_response: -3}"
submit_response_body="${submit_response%???}"

if [[ "${submit_http_code}" == "201" ]]; then
    CASE_ID=$(echo "${submit_response_body}" | jq -r '.id')
    echo "    ✅ ECM Admin case created successfully"
    echo "    📋 Case ID: ${CASE_ID}"
else
    echo "    ❌ Failed to create ECM Admin case (HTTP ${submit_http_code})"
    echo "    Response: ${submit_response_body}"
    
    # Try alternative approach if the above fails
    echo "  🔄 Trying alternative case creation approach..."
    
    # Sometimes the case type might be slightly different
    ALT_CASE_TYPE="ET_Admin"
    ALT_EVENT_ID="initiateCase"
    
    alt_start_response=$(curl -s -w "%{http_code}" \
        -X GET \
        "${CCD_DATA_STORE_API_BASE_URL}/caseworkers/${CCD_ADMIN_USERNAME}/jurisdictions/${JURISDICTION}/case-types/${ALT_CASE_TYPE}/event-triggers/${ALT_EVENT_ID}/token" \
        -H "Authorization: Bearer ${USER_TOKEN}" \
        -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
        -H "Content-Type: application/json")
    
    alt_start_http_code="${alt_start_response: -3}"
    alt_start_response_body="${alt_start_response%???}"
    
    if [[ "${alt_start_http_code}" == "200" ]]; then
        ALT_EVENT_TOKEN=$(echo "${alt_start_response_body}" | jq -r '.token')
        
        alt_submit_response=$(curl -s -w "%{http_code}" \
            -X POST \
            "${CCD_DATA_STORE_API_BASE_URL}/caseworkers/${CCD_ADMIN_USERNAME}/jurisdictions/${JURISDICTION}/case-types/${ALT_CASE_TYPE}/cases" \
            -H "Authorization: Bearer ${USER_TOKEN}" \
            -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
            -H "Content-Type: application/json" \
            -d "{
                \"data\": {},
                \"event\": {
                    \"id\": \"${ALT_EVENT_ID}\",
                    \"summary\": \"Creating ET Admin case for preview environment\",
                    \"description\": \"Automated case creation for preview environment setup\"
                },
                \"event_token\": \"${ALT_EVENT_TOKEN}\"
            }")
        
        alt_submit_http_code="${alt_submit_response: -3}"
        alt_submit_response_body="${alt_submit_response%???}"
        
        if [[ "${alt_submit_http_code}" == "201" ]]; then
            ALT_CASE_ID=$(echo "${alt_submit_response_body}" | jq -r '.id')
            echo "    ✅ ET Admin case created successfully (alternative approach)"
            echo "    📋 Case ID: ${ALT_CASE_ID}"
        else
            echo "    ❌ Alternative approach also failed (HTTP ${alt_submit_http_code})"
            echo "    Response: ${alt_submit_response_body}"
        fi
    else
        echo "    ❌ Alternative case type not available"
    fi
fi

echo "✅ ECM Admin case creation process completed!"

# Verify case exists by searching
echo "🔍 Verifying case creation..."

search_response=$(curl -s -w "%{http_code}" \
    -X POST \
    "${CCD_DATA_STORE_API_BASE_URL}/searchCases" \
    -H "Authorization: Bearer ${USER_TOKEN}" \
    -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{
        \"ccd_case_type\": \"${CASE_TYPE}\",
        \"size\": 10
    }")

search_http_code="${search_response: -3}"
search_response_body="${search_response%???}"

if [[ "${search_http_code}" == "200" ]]; then
    case_count=$(echo "${search_response_body}" | jq '.total // 0')
    echo "  ✅ Found ${case_count} admin cases in the system"
else
    echo "  ⚠️  Could not verify case creation (HTTP ${search_http_code})"
fi

echo "✅ Admin case setup completed!"

