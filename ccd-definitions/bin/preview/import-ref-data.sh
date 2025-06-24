#!/usr/bin/env bash

set -eu

echo "📊 Importing Reference Data via API"
echo "==================================="

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Use preview-specific token utilities
source "${SCRIPT_DIR}/utils/auth-utils.sh"

echo "🔐 Getting authentication tokens..."
USER_TOKEN=$(get_user_token)
SERVICE_TOKEN=$(get_service_token "et_cos")

# Base URL for ET COS service
ET_COS_BASE_URL="${ET_COS_BASE_URL:-${CCD_DEF_CASE_SERVICE_BASE_URL}}"

echo "📍 Importing venue reference data..."

# Function to import venue data
import_venue_data() {
    echo "  Importing ET venue data"
    
    local response
    response=$(curl -s -w "%{http_code}" \
        -X POST \
        "${ET_COS_BASE_URL}/admin/venues/import" \
        -H "Authorization: Bearer ${USER_TOKEN}" \
        -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
        -H "Content-Type: application/json" \
        -d '{
            "venues": [
                {
                    "venueCode": "LEEDS",
                    "venueName": "Leeds Employment Tribunal",
                    "venueAddress": "Byron House, 2a Maid Marian Way, Nottingham, NG1 6HS"
                },
                {
                    "venueCode": "MANCHESTER", 
                    "venueName": "Manchester Employment Tribunal",
                    "venueAddress": "Alexandra House, 14-22 The Parsonage, Manchester, M3 2JA"
                },
                {
                    "venueCode": "GLASGOW",
                    "venueName": "Glasgow Employment Tribunal", 
                    "venueAddress": "Eagles Building, 215 Bothwell Street, Glasgow, G2 7EZ"
                }
            ]
        }')
    
    local http_code="${response: -3}"
    local response_body="${response%???}"
    
    if [[ "${http_code}" == "201" ]] || [[ "${http_code}" == "200" ]]; then
        echo "    ✅ Venue data imported successfully"
    else
        echo "    ❌ Failed to import venue data (HTTP ${http_code})"
        echo "    Response: ${response_body}"
    fi
}

# Function to import staff reference data
import_staff_data() {
    echo "  Importing ET staff data"
    
    local response
    response=$(curl -s -w "%{http_code}" \
        -X POST \
        "${ET_COS_BASE_URL}/admin/staff/import" \
        -H "Authorization: Bearer ${USER_TOKEN}" \
        -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
        -H "Content-Type: application/json" \
        -d '{
            "staff": [
                {
                    "staffId": "JUDGE001",
                    "staffName": "Judge A. Smith",
                    "staffRole": "Employment Judge",
                    "venue": "LEEDS"
                },
                {
                    "staffId": "JUDGE002", 
                    "staffName": "Judge B. Jones",
                    "staffRole": "Employment Judge",
                    "venue": "MANCHESTER"
                },
                {
                    "staffId": "JUDGE003",
                    "staffName": "Judge C. Wilson", 
                    "staffRole": "Employment Judge",
                    "venue": "GLASGOW"
                }
            ]
        }')
    
    local http_code="${response: -3}"
    local response_body="${response%???}"
    
    if [[ "${http_code}" == "201" ]] || [[ "${http_code}" == "200" ]]; then
        echo "    ✅ Staff data imported successfully"
    else
        echo "    ❌ Failed to import staff data (HTTP ${http_code})"
        echo "    Response: ${response_body}"
    fi
}

# Function to import file location data
import_file_location_data() {
    echo "  Importing file location data"
    
    local response
    response=$(curl -s -w "%{http_code}" \
        -X POST \
        "${ET_COS_BASE_URL}/admin/file-locations/import" \
        -H "Authorization: Bearer ${USER_TOKEN}" \
        -H "ServiceAuthorization: ${SERVICE_TOKEN}" \
        -H "Content-Type: application/json" \
        -d '{
            "fileLocations": [
                {
                    "locationCode": "LEEDS_FILES",
                    "locationName": "Leeds File Storage",
                    "venue": "LEEDS"
                },
                {
                    "locationCode": "MANCHESTER_FILES",
                    "locationName": "Manchester File Storage", 
                    "venue": "MANCHESTER"
                },
                {
                    "locationCode": "GLASGOW_FILES",
                    "locationName": "Glasgow File Storage",
                    "venue": "GLASGOW"
                }
            ]
        }')
    
    local http_code="${response: -3}"
    local response_body="${response%???}"
    
    if [[ "${http_code}" == "201" ]] || [[ "${http_code}" == "200" ]]; then
        echo "    ✅ File location data imported successfully"
    else
        echo "    ❌ Failed to import file location data (HTTP ${http_code})"
        echo "    Response: ${response_body}"
    fi
}

# Import all reference data
import_venue_data
import_staff_data  
import_file_location_data

echo "✅ Reference data import completed!"

