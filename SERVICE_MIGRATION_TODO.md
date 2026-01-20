# Service Migration TODO

This file tracks which services from et-message-handler still need to be migrated.

## Status: PARTIAL - Core service migrated, dependencies needed

### ✅ Migrated
- `UpdateManagementService` - Main orchestration service

### ⚠️ REQUIRED (High Priority - UpdateManagementService depends on these)
These services are injected into UpdateManagementService and must be migrated for it to work:

1. **MultipleUpdateService** - Updates multiple cases
   - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/MultipleUpdateService.java`
   - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/MultipleUpdateService.java`
   - Dependencies: CcdClient, UserService

2. **SingleReadingService** - Reads and routes single case updates
   - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/SingleReadingService.java`
   - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/SingleReadingService.java`
   - Dependencies: CcdClient, UserService, SingleUpdateService, SingleTransferService

3. **EmailService** - Sends confirmation emails
   - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/EmailService.java`
   - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/EmailService.java`
   - Dependencies: EmailClient (config class)

4. **LegalRepAccessService** - Manages legal representative access
   - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/LegalRepAccessService.java`
   - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/LegalRepAccessService.java`

### 📦 REQUIRED (Dependencies of above services)

5. **UserService** - Gets access tokens
   - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/UserService.java`
   - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/UserService.java`
   - Dependencies: IdamApi, AccessTokenService

6. **AccessTokenService** - Token management
   - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/AccessTokenService.java`
   - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/AccessTokenService.java`
   - Dependencies: OAuth2Configuration, RestTemplate

7. **SingleUpdateService** - Updates individual cases
   - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/SingleUpdateService.java`
   - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/SingleUpdateService.java`
   - Dependencies: CcdClient

8. **SingleTransferService** - Handles single case transfers
   - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/SingleTransferService.java`
   - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/SingleTransferService.java`
   - Dependencies: CcdClient, SingleCreationService

9. **SingleCreationService** - Creates new cases
   - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/SingleCreationService.java`
   - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/SingleCreationService.java`

### 🔄 FOR TransferToEcm Feature

10. **TransferToEcmService** - Handles case transfers to ECM
    - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/transfertoecm/TransferToEcmService.java`
    - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/transfertoecm/TransferToEcmService.java`
    - Dependencies: CcdClient, UserService, CreateEcmSingleService

11. **CreateEcmSingleService** - Creates ECM cases
    - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/transfertoecm/CreateEcmSingleService.java`
    - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/transfertoecm/CreateEcmSingleService.java`

### 🔧 Configuration Classes Needed

12. **EmailClient** - Email configuration
    - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/config/EmailClient.java`
    - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/config/EmailClient.java`

13. **OAuth2Configuration** - OAuth configuration (check if already exists in et-ccd-callbacks)
    - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/config/OAuth2Configuration.java`

### 📋 IDAM/Model Classes

14. **IdamApi** - IDAM interface (check if already exists in ecm-common)
    - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/idam/IdamApi.java`

15. **TokenResponse** - Token response model
    - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/idam/TokenResponse.java`

16. **CaseTransfer** - Case transfer model
    - Source: `et-message-handler/src/main/java/uk/gov/hmcts/reform/ethos/ecm/consumer/service/CaseTransfer.java`
    - Target: `et-ccd-callbacks/src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/service/messagehandler/CaseTransfer.java`

## Migration Strategy

1. **Phase 1 (CRITICAL)**: Migrate core dependencies
   - Copy SingleReadingService, MultipleUpdateService, EmailService, LegalRepAccessService
   - Copy UserService, AccessTokenService
   - Copy EmailClient configuration
   - This will make UpdateManagementService functional

2. **Phase 2**: Migrate update/transfer services
   - Copy SingleUpdateService, SingleTransferService, SingleCreationService
   - Copy CaseTransfer model
   - This completes the update flow

3. **Phase 3**: TransferToEcm feature
   - Copy TransferToEcmService, CreateEcmSingleService
   - Update CreateUpdatesQueueProcessor to use TransferToEcmService

4. **Phase 4**: Integration and testing
   - Update UpdateCaseQueueProcessor to inject UpdateManagementService
   - Write/adapt unit tests
   - Integration testing

## Quick Copy Command Pattern

For each service, use this pattern:
```bash
# Read from et-message-handler
# Update package: uk.gov.hmcts.reform.ethos.ecm.consumer -> uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler
# Update imports as needed
# Save to et-ccd-callbacks
```

## Notes
- CcdClient already exists in ecm-common library, no need to copy
- Some configuration may already exist in et-ccd-callbacks (OAuth2Configuration, etc.)
- Check for existing implementations before copying to avoid duplicates
