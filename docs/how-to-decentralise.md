# How to Decentralise an Event or Field: Developer Guide

This guide provides a complete, copy-paste-ready walkthrough on how to decentralise an event or case field in the Employment Tribunals Case Management Service (`et-ccd-callbacks`).

---

## 1. When Should an Event or Field Be Decentralised?

Decentralisation is recommended when:
- **High Concurrency / Frequent Conflicts**: The field is written concurrently by different actors (e.g. citizen actions on portal, external API callbacks, background jobs, document stitching) and frequently suffers `409 Conflict` errors due to optimistic locking on `case_data.data`.
- **Read-Receipts & Status Flags**: Fields that track state independently of the primary case workflow (e.g. read receipts, hub links, view logs).
- **Large Blobs**: Heavy metadata or document bundles that do not need to bloat the main case data JSON payload.

---

## 2. Architecture Recap

When an event/field is decentralised:
1. The field's data is stored in a dedicated **`public`** relational table in `et_cos` instead of the monolithic `ccd.case_data.data` JSON blob.
2. The event handler is registered via `DecentralisedConfigBuilder`, persisting directly to the dedicated table and never incrementing `ccd.case_data.version`.
3. Calls to fetch the case (by ExUI, Citizen API, or callbacks) use **`ETCaseView`** to dynamically merge the table data back into `CaseData` on read.

---

## 3. Step-by-Step Implementation

```
Step 1: Flyway Migration (public.<table_name> with ON DELETE CASCADE)
   │
Step 2: JPA Entity & Spring Data Repository
   │
Step 3: Decentralised Event Handler (CCDConfig & DecentralisedConfigBuilder)
   │
Step 4: Dynamic Projection in ETCaseView (Read overlay)
   │
Step 5: CCD JSON Definitions (CaseEvent.json with empty callback URLs)
   │
Step 6: Integration Test (@CcdSdkTest)
```

---

### Step 1: Create the Flyway Database Migration

Create a new migration script in `src/main/resources/db/migration/V0XX__<DescriptiveName>.sql` (e.g. `V024__CreateMyFeatureTable.sql`).

> [!IMPORTANT]
> Always add `REFERENCES ccd.case_data(reference) ON DELETE CASCADE`. This guarantees that when cases are deleted by retention policies, off-blob records are automatically purged.

#### Pattern A: Structured Case Data / Complex Type (JSONB)
*Use this when storing complex data types or status objects per case (mirrors `hub_link_status` in `V018__CreateHubLinkStatus.sql`). If a row can exist before the JSONB object is populated (e.g. `digital_case_file` in `V022__CreateDigitalCaseFileTables.sql`, which can hold only `pending_bundle_id`), make `data` nullable with `CHECK (data IS NULL OR jsonb_typeof(data) = 'object')`.*

```sql
CREATE TABLE public.my_feature_data (
    case_reference BIGINT PRIMARY KEY
        REFERENCES ccd.case_data(reference) ON DELETE CASCADE,
    data JSONB NOT NULL
        CHECK (jsonb_typeof(data) = 'object')
);
```

#### Pattern B: Discrete Item Records / Read Receipts / Logs
*Use this when recording discrete item views or audit records (mirrors `notification_view` in `V023__CreateNotificationView.sql`).*

```sql
CREATE TABLE public.my_feature_item (
    case_reference BIGINT NOT NULL
        REFERENCES ccd.case_data(reference) ON DELETE CASCADE,
    item_id TEXT NOT NULL,
    viewed_at TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'UTC'),
    PRIMARY KEY (case_reference, item_id)
);
```

---

### Step 2: Create JPA Entity & Spring Data Repository

#### 1. JPA Entity
Create the entity class in `src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/domain/ccd/`:

```java
package uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.gov.hmcts.et.common.model.ccd.types.MyFeatureType;

@Entity
@Table(name = "my_feature_data")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyFeatureData {

    @Id
    @Column(name = "case_reference")
    private Long caseReference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private MyFeatureType data;

    public static MyFeatureData create(long caseReference, MyFeatureType data) {
        return new MyFeatureData(caseReference, data);
    }
}
```

*For composite key entities (e.g. read receipts), use `@EmbeddedId` with an `@Embeddable` static class implementing `Serializable` (see `NotificationView.java`).*

#### 2. Spring Data Repository
Create the repository interface in `src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/domain/repository/ccd/`:

```java
package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.MyFeatureData;

@Repository
public interface MyFeatureDataRepository extends JpaRepository<MyFeatureData, Long> {
}
```

> [!TIP]
> For high-concurrency inserts where multiple requests might attempt to mark the same item, write an idempotent query with `ON CONFLICT DO NOTHING` and `@Transactional(propagation = Propagation.MANDATORY)`:
> ```java
> @Modifying
> @Transactional(propagation = Propagation.MANDATORY)
> @Query(value = "INSERT INTO public.my_feature_item (case_reference, item_id) "
>     + "VALUES (:caseReference, :itemId) ON CONFLICT (case_reference, item_id) DO NOTHING",
>     nativeQuery = true)
> void recordItem(@Param("caseReference") long caseReference, @Param("itemId") String itemId);
> ```

---

### Step 3: Implement the Decentralised Event Handler

Create an event component implementing `CCDConfig<CaseData, CaseState, PlaceholderRole>` in `src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/domain/<feature>/`:

```java
package uk.gov.hmcts.ethos.replacement.docmosis.domain.myfeature;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.DecentralisedConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig.PlaceholderRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.MyFeatureData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.MyFeatureDataRepository;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
@RequiredArgsConstructor
public class UpdateMyFeatureEvent implements CCDConfig<CaseData, CaseState, PlaceholderRole> {

    public static final String EVENT_ID = "UPDATE_MY_FEATURE";
    private final MyFeatureDataRepository repository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public void configureDecentralised(DecentralisedConfigBuilder<CaseData, CaseState, PlaceholderRole> builder) {
        // Registering as decentralised event prevents touching ccd.case_data.data blob
        builder.decentralisedEvent(EVENT_ID, this::submit)
            .forAllStates();
    }

    private SubmitResponse<CaseState> submit(EventPayload<CaseData, CaseState> payload) {
        CaseData caseData = payload.caseData();
        if (caseData.getMyFeatureField() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "myFeatureField is required");
        }

        // Write directly to our table without touching ccd.case_data.data
        repository.save(MyFeatureData.create(payload.caseReference(), caseData.getMyFeatureField()));

        // SubmitResponse default response does not update ccd.case_data.data
        return SubmitResponse.defaultResponse();
    }
}
```

---

### Step 4: Project the Field via `ETCaseView` (The Reader)

Open `src/main/java/uk/gov/hmcts/ethos/replacement/docmosis/domain/caseview/ETCaseView.java`, inject the repository, and merge the table data back into `blobCase`:

```java
@Component
@RequiredArgsConstructor
public class ETCaseView implements CaseView<CaseData, CaseState> {

    private final HubLinkStatusRepository hubLinkStatusRepository;
    private final DigitalCaseFileRepository digitalCaseFileRepository;
    private final NotificationViewRepository notificationViewRepository;
    private final MyFeatureDataRepository myFeatureDataRepository; // <-- Inject new repository

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public CaseData getCase(CaseViewRequest<CaseState> request, CaseData blobCase) {
        // Project decentralised data onto the in-memory CaseData
        myFeatureDataRepository.findById(request.caseRef())
            .map(MyFeatureData::getData)
            .ifPresent(blobCase::setMyFeatureField);

        // Existing projections...
        hubLinkStatusRepository.findById(request.caseRef())
            .map(HubLinkStatus::getData)
            .ifPresent(blobCase::setHubLinksStatuses);

        digitalCaseFileRepository.findById(request.caseRef())
            .ifPresent(dcf -> {
                blobCase.setDigitalCaseFile(dcf.getData());
                blobCase.setCaseBundles(dcf.getPendingBundleId() == null
                    ? null : List.of(pendingBundle(dcf.getPendingBundleId())));
            });

        NotificationViews.apply(blobCase, notificationViewRepository.findItemIds(request.caseRef()));

        return blobCase;
    }
}
```

---

### Step 5: Update CCD Definitions (`ccd-definitions/`)

1. **`CaseEvent.json`**:
   Add the event under `ccd-definitions/jurisdictions/{england-wales,scotland}/json/CaseEvent/CaseEvent.json`:
   ```json
   {
     "CaseTypeID": "ET_EnglandWales",
     "ID": "UPDATE_MY_FEATURE",
     "Name": "Update My Feature",
     "Description": "Updates feature outside main blob",
     "DisplayOrder": 95,
     "PreConditionState(s)": "*",
     "PostConditionState": "*",
     "SecurityClassification": "Public",
     "ShowEventNotes": "N",
     "ShowSummary": "N",
     "EventEnablingCondition": "caseType=\"dummy\"",
     "CallBackURLAboutToStartEvent": "",
     "CallBackURLAboutToSubmitEvent": "",
     "CallBackURLSubmittedEvent": ""
   }
   ```
   > [!NOTE]
   > The callback URLs (`CallBackURLAboutToStartEvent`, `CallBackURLAboutToSubmitEvent`, `CallBackURLSubmittedEvent`) must be set to empty strings `""` because the event is handled in-process by the decentralised event handler without external HTTP roundtrips.

2. **`AuthorisationCaseEvent.json`**:
   Grant access (`CRU` / `R`) to roles triggering this event:
   ```json
   {
     "CaseTypeID": "ET_EnglandWales",
     "CaseEventID": "UPDATE_MY_FEATURE",
     "UserRole": "caseworker-employment",
     "CRUD": "CRU"
   }
   ```

3. **`CaseField.json` / `AuthorisationCaseField.json`**:
   Keep the field declared in CCD definitions so UI schemas and ExUI understand the model when projected.

---

### Step 6: Historical Data Backfill (For Pre-Existing Fields)

If you are decentralising a field that already has data in historical cases:
1. **Backward Compatibility**: Ensure `ETCaseView` falls back to `blobCase.getMyFeatureField()` if no row exists in the new table.
2. **Backfill Task**: Create a Spring scheduled task (e.g. extending `ScheduledTaskRunner`) to batch-read `ccd.case_data.data->'myFeatureField'` and write missing rows into `my_feature_data`.

---

### Step 7: Write Integration Tests with `@CcdSdkTest`

Create an integration test in `src/test/integration/java/uk/gov/hmcts/ethos/replacement/docmosis/domain/<feature>/`:

```java
package uk.gov.hmcts.ethos.replacement.docmosis.domain.myfeature;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.sdk.testing.CcdEventTestSupport;
import uk.gov.hmcts.ccd.sdk.testing.CcdSdkTest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.MyFeatureType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig;
import uk.gov.hmcts.ethos.replacement.docmosis.config.JacksonConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.ETCaseView;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.MyFeatureData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.MyFeatureDataRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@CcdSdkTest(
    components = {EtJsonCcdConfig.class, JacksonConfiguration.class,
        UpdateMyFeatureEvent.class, ETCaseView.class},
    repositories = {MyFeatureDataRepository.class},
    entities = {MyFeatureData.class}
)
class UpdateMyFeatureEventIntegrationTest {

    @Autowired
    private CcdEventTestSupport<CaseData, CaseState> events;
    @Autowired
    private MyFeatureDataRepository repository;

    @ParameterizedTest
    @ValueSource(strings = {ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID})
    void executesWithoutMutatingBlobAndProjectsOnRead(String caseTypeId) {
        var cases = events.forCaseType(caseTypeId);
        long ref = cases.seed(CaseState.Accepted, CaseData.builder().build());
        var beforeSnapshot = cases.snapshot(ref);

        MyFeatureType payload = MyFeatureType.builder().status("COMPLETED").build();
        CaseData eventData = CaseData.builder().myFeatureField(payload).build();

        var result = cases.event(ref, UpdateMyFeatureEvent.EVENT_ID, eventData)
            .submitExpectingSuccess();

        // 1. Verify main case blob was not altered or version bumped
        assertThat(result.rawData()).isEqualTo(beforeSnapshot.rawData());
        assertThat(result.blobVersion()).isEqualTo(beforeSnapshot.blobVersion());

        // 2. Verify table persistence
        assertThat(repository.findById(ref)).isPresent();

        // 3. Verify projection via ETCaseView
        CaseData projected = result.projectedCase();
        assertThat(projected.getMyFeatureField().getStatus()).isEqualTo("COMPLETED");
    }
}
```

---

### Step 8: Safe Production Rollout Phases

To ensure zero downtime during rolling pod deployments:
1. **Phase 1 (Schema & Reader)**: Deploy the Flyway migration, JPA Entity/Repository, and `ETCaseView` projection with fallback. At this stage, nothing writes to the table yet, making it a safe rollback target.
2. **Phase 2 (Writer & Definitions)**: Deploy `UpdateMyFeatureEvent` and the CCD JSON definition changes. Incoming events start writing to the table.
3. **Phase 3 (Backfill)**: Run a scheduled batch task to backfill historical cases from `case_data.data->'myFeatureField'` into the new table if needed.
