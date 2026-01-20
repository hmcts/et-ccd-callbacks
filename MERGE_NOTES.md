# Repository Merge: et-message-handler into et-ccd-callbacks

## Overview
This document describes the partial merge of the `et-message-handler` repository into `et-ccd-callbacks`, replacing Azure Service Bus with a PostgreSQL-based queue system.

## What Has Been Completed

### 1. Database Queue Infrastructure ✅
- **Created**: Flyway migrations for queue tables
  - `V014__CreateUpdatesQueue.sql` - creates `create_updates_queue` table
  - `V015__UpdateCaseQueue.sql` - creates `update_case_queue` table
- **Features**: Message locking, retry tracking, status management

### 2. Domain Layer ✅
- **Entities**:
  - `CreateUpdatesQueueMessage` - represents messages in create_updates_queue
  - `UpdateCaseQueueMessage` - represents messages in update_case_queue
  - `QueueMessageStatus` - enum for message states (PENDING, PROCESSING, COMPLETED, FAILED)
- **Repositories**:
  - `CreateUpdatesQueueRepository` - CRUD operations with locking for create-updates queue
  - `UpdateCaseQueueRepository` - CRUD operations with locking for update-case queue

### 3. Queue Processing Services ✅
- **CreateUpdatesQueueProcessor**: Scheduled task that polls and processes messages from create_updates_queue
  - Replaces `CreateUpdatesBusReceiverTask` from et-message-handler
  - Converts CreateUpdatesMsg to UpdateCaseMsg and enqueues them
  - Handles TransferToEcm cases (currently stubbed)
- **UpdateCaseQueueProcessor**: Scheduled task that polls and processes messages from update_case_queue
  - Replaces `UpdateCaseBusReceiverTask` from et-message-handler
  - **NOTE**: Now wired to UpdateManagementService, but dependencies still needed (see SERVICE_MIGRATION_TODO.md)
- **UpdateCaseQueueSender**: Service to send UpdateCaseMsg to database queue

### 4. Message Sending ✅
- **Updated**: `CreateUpdatesBusSender` now writes to database instead of Azure Service Bus
  - Uses `CreateUpdatesQueueRepository` instead of `ServiceBusSender`
  - Serializes messages to JSON and stores in database
  - Maintains same interface for compatibility

### 5. Configuration ✅
- **Updated**: `application.yaml` with queue processor settings:
  ```yaml
  queue:
    create-updates:
      poll-interval: ${CREATE_UPDATES_POLL_INTERVAL:1000}
      batch-size: ${CREATE_UPDATES_BATCH_SIZE:10}
      threads: ${CREATE_UPDATES_THREADS:15}
    update-case:
      poll-interval: ${UPDATE_CASE_POLL_INTERVAL:1000}
      batch-size: ${UPDATE_CASE_BATCH_SIZE:10}
      threads: ${UPDATE_CASE_THREADS:15}
  ```
- Removed Azure Service Bus connection strings from configuration

### 6. Scheduling ✅
- Scheduling already enabled in `DocmosisApplication` with `@EnableScheduling`

## What Still Needs To Be Done

### 1. Service Layer Migration ⚠️ IN PROGRESS
**UpdateManagementService** has been migrated but requires its dependencies.
See `SERVICE_MIGRATION_TODO.md` for detailed tracking.

The following services from et-message-handler still need to be migrated:

**Core Services**:
- `UpdateManagementService` - Main business logic for update-case processing
- `SingleReadingService` - Reads cases from CCD
- `SingleUpdateService` - Updates individual cases
- `MultipleUpdateService` - Updates multiple cases
- `TransferToEcmService` - Handles case transfers to ECM
- `LegalRepAccessService` - Manages legal rep access
- `EmailService` - Sends confirmation emails
- `UserService` - Gets access tokens
- `AccessTokenService` - Token management

**Supporting Services**:
- `SingleTransferService` - Handles single case transfers
- `SingleCreationService` - Creates new cases
- `CreateEcmSingleService` - Creates ECM cases
- `CaseTransfer` - Case transfer logic

**Package Migration**: All classes should be moved from `uk.gov.hmcts.reform.ethos.ecm.consumer` to `uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler`

### 2. Domain Entities ⚠️
Copy from et-message-handler (if not already in et-ccd-callbacks):
- `MultipleCounter` and `MultipleCounterRepository`
- `MultipleErrors` and `MultipleErrorsRepository`

### 3. Configuration Classes
May need to copy/adapt:
- `EmailClient` configuration
- OAuth2 configuration (if different)
- Jackson configuration (if needed)

### 4. Integration with Queue Processors
- Update `UpdateCaseQueueProcessor` to inject and call `UpdateManagementService.updateLogic()`
- Update `CreateUpdatesQueueProcessor` to call `TransferToEcmService` for transfer cases

### 5. Testing ⚠️
- Copy unit tests from et-message-handler
- Update tests for database queue instead of Service Bus
- Create integration tests for:
  - Database queue insertion
  - Message processing
  - Retry logic
  - Concurrent processing

### 6. Clean Up Service Bus Configuration
Files that reference Azure Service Bus and may need to be removed/updated:
- `ServiceBusSenderConfiguration.java` - Remove or refactor
- `QueueClientConfiguration.java` - Remove or refactor
- `DevQueueClient.java` - Remove or refactor for development
- `FakeServiceBus.java` (in cftlib) - Update for database queue
- `MockServiceBus.java` (in tests) - Update for database queue

### 7. Database Migration Scripts
Ensure Flyway migrations from et-message-handler (V001-V004) are incorporated if needed:
- `V001__MultipleCounter.sql`
- `V002__MultipleErrors.sql`
- `V003__MultipleCounterFunction.sql`
- `V004__MultipleErrorsFunction.sql`

### 8. Infrastructure Updates
- Update `bootWithCCD` configuration in build.gradle to remove `et_msg_handler` database
- Update Terraform/Helm charts (if applicable)
- Update environment variable documentation

### 9. Observability & Monitoring
- Add metrics for:
  - Queue depth
  - Processing time
  - Retry counts
  - Error rates
- Add health checks for queue processors
- Add dashboards/alerts

### 10. Documentation
- Update README with new architecture
- Document queue processor configuration
- Document migration from Service Bus to database queue
- Add troubleshooting guide

## Architecture Changes

### Before (Azure Service Bus)
```
et-ccd-callbacks                et-message-handler
     |                                   |
     | -> Azure Service Bus -> create-updates queue
     |                                   |
     |                              [Receiver polls]
     |                                   |
     |                         CreateUpdatesBusReceiverTask
     |                                   |
     |                    [Sends to] update-case queue
     |                                   |
     |                         UpdateCaseBusReceiverTask
     |                                   |
     |                          UpdateManagementService
```

### After (Database Queue)
```
et-ccd-callbacks
     |
CreateUpdatesBusSender -> create_updates_queue (PostgreSQL)
     |                             |
     |                    [CreateUpdatesQueueProcessor polls]
     |                             |
     |                    UpdateCaseQueueSender -> update_case_queue (PostgreSQL)
     |                                                      |
     |                                         [UpdateCaseQueueProcessor polls]
     |                                                      |
     |                                              UpdateManagementService
     |                                              (TODO: needs migration)
```

## Benefits of Database Queue

1. **Simplified Infrastructure**: No Azure Service Bus dependency
2. **Transactional Consistency**: Queue operations in same transaction as data changes
3. **Easier Local Development**: No external dependencies to configure
4. **Cost Reduction**: Eliminates Service Bus costs
5. **Consolidated Deployment**: One service instead of two

## Migration Checklist for Production

- [ ] Migrate all service classes
- [ ] Migrate all tests
- [ ] Run integration tests
- [ ] Performance testing
- [ ] Deploy to dev environment
- [ ] Monitor queue depth and processing times
- [ ] Deploy to test environment
- [ ] Run full regression tests
- [ ] Deploy to staging
- [ ] Run parallel with existing services
- [ ] Cutover to database queue
- [ ] Decommission et-message-handler service
- [ ] Remove Azure Service Bus resources

## Known Issues / TODOs

1. **TransferToEcm Logic**: Stubbed in `CreateUpdatesQueueProcessor` - needs full implementation
2. **UpdateManagementService**: Not migrated - `UpdateCaseQueueProcessor` just marks messages as complete
3. **Email Notifications**: EmailService not migrated
4. **Error Handling**: Need to verify error handling matches original behavior
5. **Concurrent Processing**: Test with multiple application instances
6. **Message Ordering**: Verify ordering requirements are met (if any)

## Testing the Changes

### Local Testing
1. Start the application with cftlib profile
2. Trigger a multiple update operation
3. Check `create_updates_queue` table for messages
4. Watch logs for queue processor activity
5. Verify messages move through states: PENDING -> PROCESSING -> COMPLETED

### Database Queries
```sql
-- Check queue status
SELECT status, COUNT(*) FROM create_updates_queue GROUP BY status;
SELECT status, COUNT(*) FROM update_case_queue GROUP BY status;

-- Check failed messages
SELECT * FROM create_updates_queue WHERE status = 'FAILED';
SELECT * FROM update_case_queue WHERE status = 'FAILED';

-- Check stuck messages (locked for > 10 minutes)
SELECT * FROM create_updates_queue 
WHERE status = 'PROCESSING' AND locked_until < NOW() - INTERVAL '10 minutes';
```

## Configuration Environment Variables

### Removed (no longer needed)
- `CREATE_UPDATES_QUEUE_SEND_CONNECTION_STRING`
- `CREATE_UPDATES_QUEUE_LISTEN_CONNECTION_STRING`
- `UPDATE_CASE_QUEUE_SEND_CONNECTION_STRING`
- `UPDATE_CASE_QUEUE_LISTEN_CONNECTION_STRING`

### Added (optional, with defaults)
- `CREATE_UPDATES_POLL_INTERVAL` (default: 1000ms)
- `CREATE_UPDATES_BATCH_SIZE` (default: 10)
- `CREATE_UPDATES_THREADS` (default: 15)
- `UPDATE_CASE_POLL_INTERVAL` (default: 1000ms)
- `UPDATE_CASE_BATCH_SIZE` (default: 10)
- `UPDATE_CASE_THREADS` (default: 15)

## Contact
For questions about this merge, contact the ET team.

## References
- Original et-message-handler repository
- [Database as Queue Pattern](https://github.com/hmcts/ethos-repl-docmosis-service) (similar pattern)
- Azure Service Bus to Database Queue migration guide
