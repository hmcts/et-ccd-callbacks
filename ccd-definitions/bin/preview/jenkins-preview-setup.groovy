// Updated Jenkins pipeline configuration for robust API-based preview environment setup
// Replace the functional UI tests with reliable API calls

// In the existing Jenkinsfile_CNP, replace the functionalTest:preview section:

/*
OLD APPROACH (unreliable UI tests):
before('functionalTest:preview') {
  env.CCD_ADMIN_URL="https://admin-web-et-ccd-definitions-admin-pr-${CHANGE_ID}.preview.platform.hmcts.net"
  env.IMPORT_PREVIEW=true
}

afterAlways('functionalTest:preview') {
  env.IDAM_API_URL = "https://idam-api.aat.platform.hmcts.net"
  env.ROLE_ASSIGNMENT_URL = "https://am-role-assignment-et-ccd-definitions-admin-pr-${CHANGE_ID}.preview.platform.hmcts.net"
  env.XUI_URL = "https://xui-et-ccd-definitions-admin-pr-${CHANGE_ID}.preview.platform.hmcts.net"
  sh """
  ./bin/add-org-roles-to-users.sh
"""
}
*/

// NEW APPROACH (robust API-based setup):
before('functionalTest:preview') {
  // Set environment URLs for API calls
  env.CCD_DEFINITION_STORE_API_BASE_URL = "https://ccd-definition-store-api-et-ccd-definitions-admin-pr-${CHANGE_ID}.preview.platform.hmcts.net"
  env.CCD_DATA_STORE_API_BASE_URL = "https://ccd-data-store-api-et-ccd-definitions-admin-pr-${CHANGE_ID}.preview.platform.hmcts.net"
  env.CCD_USER_PROFILE_API_BASE_URL = "https://ccd-user-profile-api-et-ccd-definitions-admin-pr-${CHANGE_ID}.preview.platform.hmcts.net"
  env.IDAM_API_BASE_URL = "https://idam-api.aat.platform.hmcts.net"
  env.SERVICE_AUTH_PROVIDER_API_BASE_URL = "https://rpe-service-auth-provider-aat.service.core-compute-aat.internal"
  env.ET_COS_URL = "https://et-cos-et-ccd-definitions-admin-pr-${CHANGE_ID}.preview.platform.hmcts.net"
  
  // Run comprehensive API-based setup
  sh """
    echo "🚀 Starting API-based preview environment setup..."
    ./bin/preview/init-preview-env.sh ${CHANGE_ID} ${ET_PREVIEW_FLEXI_DB_PASSWORD}
  """
}

// Optional: Still run the org roles assignment after API setup
afterAlways('functionalTest:preview') {
  env.IDAM_API_URL = "https://idam-api.aat.platform.hmcts.net"
  env.ROLE_ASSIGNMENT_URL = "https://am-role-assignment-et-ccd-definitions-admin-pr-${CHANGE_ID}.preview.platform.hmcts.net"
  env.XUI_URL = "https://xui-et-ccd-definitions-admin-pr-${CHANGE_ID}.preview.platform.hmcts.net"
  
  sh """
    echo "🏢 Adding organizational roles to users..."
    ./bin/add-org-roles-to-users.sh
  """
}

// Benefits of this approach:
// 1. ✅ Faster execution - API calls are much faster than UI automation
// 2. ✅ More reliable - No dependency on UI element selectors or browser stability
// 3. ✅ Better error handling - HTTP status codes provide clear failure reasons
// 4. ✅ Easier debugging - API responses can be logged and analyzed
// 5. ✅ Environment independence - Not affected by AAT environment instability
// 6. ✅ Atomic operations - Each step can be verified independently
// 7. ✅ Idempotent - Can be run multiple times safely

/* 
MANUAL TESTING:
To test this setup manually:

1. Set environment variables:
   export CCD_ADMIN_USERNAME="your-admin-username"
   export CCD_ADMIN_PASSWORD="your-admin-password"
   export API_GATEWAY_S2S_KEY="your-s2s-key"
   export DATA_STORE_S2S_KEY="your-data-store-key"
   export DEFINITION_STORE_S2S_KEY="your-definition-store-key"

2. Run the setup:
   ./bin/preview/init-preview-env.sh YOUR_PR_NUMBER

3. Verify the setup:
   - Check ExUI: https://xui-et-ccd-definitions-admin-pr-YOUR_PR_NUMBER.preview.platform.hmcts.net
   - Check CCD Admin: https://admin-web-et-ccd-definitions-admin-pr-YOUR_PR_NUMBER.preview.platform.hmcts.net
*/

