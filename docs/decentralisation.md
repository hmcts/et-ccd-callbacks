# CCD Decentralisation in Employment Tribunals (`et-cos`)

This document outlines the architecture, implementation patterns, data storage, and operational considerations of the **CCD Decentralised Runtime** within the Employment Tribunals Case Management Service (`et-ccd-callbacks` / `et-cos`).

---

## 1. Overview & Architecture

Historically, HMCTS Core Case Data (CCD) operated on a **centralised monolithic model**:
- All jurisdictions stored their case data in a single shared database (`datastore`) managed by `ccd-data-store-api`.
- Case states and fields were persisted inside large, monolithic JSON blobs (`case_data.data`) with an incrementing optimistic locking `version` counter.
- **The Problem**: Whenever high-concurrency interactions occurred—such as a Citizen opening the Citizen Hub while an asynchronous document stitching task completed in the background or a caseworker amended case details—the simultaneous updates triggered version mismatch errors (**`409 Conflict: Case could not be updated`**). In addition, any shared database degradation affected all jurisdictions.

### The Decentralised Model

Under the **CCD Decentralised Runtime** (`hmcts.ccd.sdk` and `com.github.hmcts:decentralised-runtime`):
- Case persistence and event execution are shifted from the centralised CCD data store into the service's own dedicated PostgreSQL database (**`et_cos`**).
- The central CCD Data Store operates as a router / control plane, delegating all operations for ET case types (`ET_EnglandWales`, `ET_Scotland`, `ET_EnglandWales_Listings`, `ET_Scotland_Listings`) directly to `et-cos`.
- High-concurrency attributes are extracted into dedicated relational tables that bypass the main case JSON blob entirely.

```
                           +------------------------+
                           |  Manage Cases (ExUI)   |
                           |   or Citizen/SYA API   |
                           +-----------+------------+
                                       |
                                       v
                           +------------------------+
                           |   CCD Data Store       |
                           |   (Control Plane)      |
                           +-----------+------------+
                                       |
          Routes ET Case Types         | (via case-type-service-urls)
                                       v
        +-------------------------------------------------------------+
        |                 et-ccd-callbacks (et-cos)                   |
        |                                                             |
        |  +------------------------+     +------------------------+  |
        |  |  JsonCallbackBridge    |     |  ETCaseView            |  |
        |  |  (In-JVM Callbacks)    |     |  (Dynamic Projection)  |  |
        |  +-----------+------------+     +-----------+------------+  |
        |              |                              |               |
        |              | Direct Writes / Events       | Reads & Merges|
        |              v                              v               |
        |  +-------------------------------------------------------+  |
        |  |                 et_cos Database                       |  |
        |  |                                                       |  |
        |  |  [ccd schema]                                         |  |
        |  |  ├── ccd.case_data (core metadata & blob)             |  |
        |  |  ├── ccd.case_event                                   |  |
        |  |  └── ccd.message_queue_candidates                     |  |
        |  |                                                       |  |
        |  |  [public schema - off-blob domain tables]             |  |
        |  |  ├── public.hub_link_status (citizen status)          |  |
        |  |  ├── public.digital_case_file (DCF & bundles)         |  |
        |  |  └── public.notification_view (view tracking)         |  |
        |  +-------------------------------------------------------+  |
        +-------------------------------------------------------------+
```

---

## 2. Database Architecture & Schema Separation

Inside the `et_cos` database, data is split between the standard CCD schema and domain-specific tables:

### Core CCD Tables (`ccd` Schema)
The decentralised runtime maintains standard CCD tables directly within `et_cos`:
- **`ccd.case_data`**: The authoritative record for the case reference, case type ID, case state, security classification, version, and the primary case JSON blob (`data`).
- **`ccd.case_event`**: Audit history of all events executed against cases.
- **`ccd.message_queue_candidates`**: Outbox for Work Allocation / Case Event publishing.

### Domain-Specific Relational Tables (`public` Schema)
To prevent locking contention on the monolithic `ccd.case_data.data` column, frequently updated subsystems use dedicated tables with foreign keys referencing `ccd.case_data(reference) ON DELETE CASCADE`:

| Table | Migration | Purpose | Foreign Key |
|---|---|---|---|
| **`hub_link_status`** | `V018__CreateHubLinkStatus.sql` | Citizen Portal (SYA) hub sections and completion status | `case_reference REFERENCES ccd.case_data(reference) ON DELETE CASCADE` |
| **`digital_case_file`** | `V022__CreateDigitalCaseFileTables.sql` | Digital Case File bundle definitions, status, and pending bundle UUID | `case_reference REFERENCES ccd.case_data(reference) ON DELETE CASCADE` |
| **`notification_view`** | `V023__CreateNotificationView.sql` | Read receipts / viewed status for tribunal notifications and TSE responses | `case_reference REFERENCES ccd.case_data(reference) ON DELETE CASCADE` |

When a case is deleted or purged (via retain-and-dispose), cascading foreign keys ensure all associated off-blob domain data is automatically removed.

---

## 3. Decentralised Events (`DecentralisedConfigBuilder`)

Standard CCD events update the case JSON blob and increment `ccd.case_data.version`. In contrast, **Decentralised Events** bypass the blob:
1. They are registered via `DecentralisedConfigBuilder<CaseData, CaseState, PlaceholderRole>`.
2. They persist state directly into relational tables without writing back to the `ccd.case_data.data` column.
3. They never trigger version conflicts with other concurrent events.

### Implementation Pattern

```java
@Component
@RequiredArgsConstructor
public class UpdateNotificationStateEvent implements CCDConfig<CaseData, CaseState, PlaceholderRole> {

    public static final String EVENT_ID = "UPDATE_NOTIFICATION_STATE";
    private final NotificationViewRepository notificationViewRepository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public void configureDecentralised(DecentralisedConfigBuilder<CaseData, CaseState, PlaceholderRole> builder) {
        // Record views in our table without touching the case data blob, avoiding conflicts.
        builder.decentralisedEvent(EVENT_ID, this::submit).forAllStates();
    }

    private SubmitResponse<CaseState> submit(EventPayload<CaseData, CaseState> payload) {
        long caseReference = payload.caseReference();
        Set<String> alreadyViewed = notificationViewRepository.findItemIds(caseReference);
        NotificationViews.viewedItemIds(payload.caseData()).stream()
            .filter(id -> !alreadyViewed.contains(id))
            .forEach(id -> notificationViewRepository.markViewed(caseReference, id));
        return SubmitResponse.defaultResponse();
    }
}
```

### Registered Decentralised Events in ET

- **`UPDATE_HUBLINK_STATUS`** (`UpdateHubLinkStatusEvent`):
  Records citizen portal hub link section statuses in `hub_link_status`.
- **`UPDATE_NOTIFICATION_STATE`** (`UpdateNotificationStateEvent`):
  Marks tribunal notifications as viewed by the claimant in `notification_view`.
- **`viewAllNotifications`** (`ViewAllNotificationsEvent`):
  Handles bulk viewing of notifications for respondents without optimistic locks.
- **`asyncStitchingComplete`** (`AsyncStitchingCompleteEvent`):
  Triggered when EM Document Stitching finishes assembling a Digital Case File (DCF). Saves the PDF document and status to `digital_case_file` asynchronously without race conditions against caseworkers editing the case.

---

## 4. Dynamic Projection via Case View (`CaseView`)

Because domain data is split across relational tables and not stored in the core JSON blob, callers (ExUI, Citizen API, and callbacks) need a unified representation of the case.

The service uses **`ETCaseView`** (implementing `uk.gov.hmcts.ccd.sdk.CaseView<CaseData, CaseState>`) to dynamically project relational data back into `CaseData` upon retrieval:

```java
@Component
@RequiredArgsConstructor
public class ETCaseView implements CaseView<CaseData, CaseState> {

    private final HubLinkStatusRepository hubLinkStatusRepository;
    private final DigitalCaseFileRepository digitalCaseFileRepository;
    private final NotificationViewRepository notificationViewRepository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public CaseData getCase(CaseViewRequest<CaseState> request, CaseData blobCase) {
        // 1. Hub Link Status
        hubLinkStatusRepository.findById(request.caseRef())
            .map(HubLinkStatus::getData)
            .ifPresent(blobCase::setHubLinksStatuses);

        // 2. Digital Case File & in-progress bundles
        digitalCaseFileRepository.findById(request.caseRef())
            .ifPresent(dcf -> {
                blobCase.setDigitalCaseFile(dcf.getData());
                blobCase.setCaseBundles(dcf.getPendingBundleId() == null
                    ? null : List.of(pendingBundle(dcf.getPendingBundleId())));
            });

        // 3. Notification views
        NotificationViews.apply(blobCase, notificationViewRepository.findItemIds(request.caseRef()));

        return blobCase;
    }
}
```

Other case types that do not have custom tables (e.g. `AdminCaseView`, `MultipleCaseView`, `ListingCaseView`) implement pass-through projections.

---

## 5. Decentralised Retain & Dispose Policy

In the decentralised model, retention and disposal runs locally against the `et_cos` database rather than through a central CCD cron job.

**`EtRetainAndDisposePolicy`** implements `uk.gov.hmcts.ccd.sdk.RetainAndDisposePolicy`:
- Directly queries `ccd.case_data` in `et_cos`.
- Evaluates disposal candidates based on ET policy:
  1. **Deleted Cases**: Cases explicitly moved to state `Delete` are eligible for disposal immediately.
  2. **Expired Draft Cases**: Unsubmitted draft cases in state `AWAITING_SUBMISSION_TO_HMCTS` created more than 365 days ago are eligible for disposal.

### Configuration (`application.yaml`)

```yaml
ccd:
  decentralised-runtime:
    retain-and-dispose:
      mode: ${ET_RETAIN_AND_DISPOSE_MODE:off}            # 'off' | 'audit' | 'live'
      cron: "${ET_RETAIN_AND_DISPOSE_CRON:0 0 2 * * *}" # Nightly execution
      zone: ${ET_RETAIN_AND_DISPOSE_ZONE:UTC}
      maximum-candidate-percentage-by-state:
        Delete: 100
      system-user:
        username: ${ET_COS_SYSTEM_USER}
        password: ${ET_COS_SYSTEM_USER_PASSWORD}
```

---

## 6. Work Allocation Integration

In decentralised mode, case event messages destined for Work Allocation (WA) are written directly to `et_cos.ccd.message_queue_candidates`.

1. **In-JVM Bridge (`MessageQueueCandidateTask`)**:
   Polls `ccd.message_queue_candidates` and translates publish events into `et_cos.wa_case_event_messages`.
2. **`wa-case-event-handler`**:
   Reads `wa_case_event_messages` and initiates or configures tasks via Camunda BPMN/DMN.
3. **`client-context` Propagation**:
   Work Allocation user task completion headers (`client-context`) passed in HTTP requests are intercepted by `CaseActionsForCaseWorkerController` and returned in the servlet response header to automatically complete user tasks in Camunda.

---

## 7. Configuration Reference

### Application Properties (`application.yaml`)

```yaml
decentralisation:
  legacy-json-service: true
  local-callback-placeholder: ET_COS_URL
  local-callback-base-url: ${ET_COS_URL:http://localhost:8081}
  external-callback-base-urls:
    "[CCD_DEF_AAC_URL]": ${AAC_URL:http://localhost:4454}

ccd:
  decentralised-runtime:
    cdam-attach:
      enabled: ${CCD_DECENTRALISED_RUNTIME_CDAM_ATTACH_ENABLED:true}
```

### CFTLib / Preview Environment Routing

In CFTLib and Preview environments, CCD Data Store routes case type requests to the local decentralised instance:

```json
{
  "ccd": {
    "decentralised": {
      "case-type-service-urls": {
        "ET_EnglandWales": "http://localhost:8081",
        "ET_Scotland": "http://localhost:8081",
        "ET_EnglandWales_Listings": "http://localhost:8081",
        "ET_Scotland_Listings": "http://localhost:8081",
        "ET_Admin": "http://localhost:8081"
      }
    }
  }
}
```

### Elasticsearch Indexer

In production and preview environments, `ccd-runtime-indexing` publishes decentralised case data to the Elasticsearch cluster:
- **Preview**: `CCD_SDK_DECENTRALISED_ES_INDEXER_ENABLED: true`
- **Local/Default**: `CCD_SDK_DECENTRALISED_ES_INDEXER_ENABLED: false`

---

## 8. Testing Decentralised Events

Integration tests for decentralised events should use the `@CcdSdkTest` annotation and `CcdEventTestSupport`:

```java
@CcdSdkTest(
    components = {EtJsonCcdConfig.class, JacksonConfiguration.class,
        UpdateNotificationStateEvent.class, ETCaseView.class},
    repositories = {HubLinkStatusRepository.class, DigitalCaseFileRepository.class,
        NotificationViewRepository.class},
    entities = {HubLinkStatus.class, DigitalCaseFile.class, NotificationView.class}
)
class UpdateNotificationStateEventIntegrationTest {

    @Autowired
    private CcdEventTestSupport<CaseData, CaseState> events;
    @Autowired
    private NotificationViewRepository notificationViewRepository;

    @Test
    void recordsViewsWithoutChangingTheCcdBlob() {
        var cases = events.forCaseType(ENGLANDWALES_CASE_TYPE_ID);
        long reference = cases.seed(CaseState.Accepted, caseData(NOT_VIEWED_YET, null));
        var before = cases.snapshot(reference);

        var result = cases.event(reference, UpdateNotificationStateEvent.EVENT_ID, caseData(VIEWED, YES))
            .submitExpectingSuccess();

        // 1. Verify core blob is completely untouched
        assertThat(result.rawData()).isEqualTo(before.rawData());
        assertThat(result.blobVersion()).isEqualTo(before.blobVersion());

        // 2. Verify domain table received the update
        assertThat(notificationViewRepository.findItemIds(reference)).contains("notification");

        // 3. Verify CaseView projected the new state on read
        CaseData projected = result.projectedCase();
        assertThat(projected.getSendNotificationCollection().getFirst().getValue().getNotificationState())
            .isEqualTo(VIEWED);
    }
}
```

---

## 9. How to Decentralise an Event or Field

For the dedicated, copy-paste-ready developer guide with full code examples, database migrations, repository patterns, event handlers, `ETCaseView` projections, and testing snippets, see:

👉 **[How to Decentralise an Event or Field: Developer Guide](how-to-decentralise.md)**
