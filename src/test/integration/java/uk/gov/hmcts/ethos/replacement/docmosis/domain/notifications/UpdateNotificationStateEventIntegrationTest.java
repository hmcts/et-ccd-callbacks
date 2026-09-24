package uk.gov.hmcts.ethos.replacement.docmosis.domain.notifications;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ccd.data.casedetails.SecurityClassification;
import uk.gov.hmcts.ccd.decentralised.dto.DecentralisedCaseEvent;
import uk.gov.hmcts.ccd.decentralised.dto.DecentralisedEventDetails;
import uk.gov.hmcts.ccd.domain.model.definition.CaseDetails;
import uk.gov.hmcts.ccd.sdk.impl.CaseSubmissionService;
import uk.gov.hmcts.ccd.sdk.testing.CcdEventTestSupport;
import uk.gov.hmcts.ccd.sdk.testing.CcdSdkTest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig;
import uk.gov.hmcts.ethos.replacement.docmosis.config.JacksonConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.ETCaseView;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.DigitalCaseFile;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.NotificationView;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.DigitalCaseFileRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.NotificationViewRepository;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@CcdSdkTest(
    components = {EtJsonCcdConfig.class, JacksonConfiguration.class,
        UpdateNotificationStateEvent.class, ETCaseView.class},
    repositories = {HubLinkStatusRepository.class, DigitalCaseFileRepository.class,
        NotificationViewRepository.class},
    entities = {HubLinkStatus.class, DigitalCaseFile.class, NotificationView.class}
)
class UpdateNotificationStateEventIntegrationTest {

    // The actor ccd-sdk-test-support registers by default; its constant is package-private.
    private static final String SDK_TEST_TOKEN = "Bearer ccd-sdk-test";

    @Autowired
    private CcdEventTestSupport<CaseData, CaseState> events;

    @Autowired
    private NotificationViewRepository notificationViewRepository;

    @Autowired
    private CaseSubmissionService submissionService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private ObjectMapper mapper;

    @ParameterizedTest
    @ValueSource(strings = {ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID})
    void recordsViewsWithoutChangingTheCcdBlob(String caseTypeId) {
        var cases = events.forCaseType(caseTypeId);
        long reference = cases.seed(CaseState.Accepted, caseData(NOT_VIEWED_YET, null));
        final var before = cases.snapshot(reference);

        var result = cases.event(reference, UpdateNotificationStateEvent.EVENT_ID, caseData(VIEWED, YES))
            .submitExpectingSuccess();

        assertThat(result.rawData()).isEqualTo(before.rawData());
        assertThat(result.blobVersion()).isEqualTo(before.blobVersion());
        assertThat(notificationViewRepository.findItemIds(reference))
            .containsExactlyInAnyOrder("notification", "tribunalResponse", "tseResponse");

        CaseData projected = result.projectedCase();
        SendNotificationType notification = projected.getSendNotificationCollection().getFirst().getValue();
        assertThat(notification.getNotificationState()).isEqualTo(VIEWED);
        assertThat(notification.getRespondNotificationTypeCollection().getFirst().getValue().getState())
            .isEqualTo(VIEWED);
        assertThat(projected.getGenericTseApplicationCollection().getFirst().getValue()
            .getRespondCollection().getFirst().getValue().getViewedByClaimant()).isEqualTo(YES);
    }

    @ParameterizedTest
    @ValueSource(strings = {ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID})
    void acceptsViewsStartedBeforeAnotherEventWasCommitted(String caseTypeId) {
        var cases = events.forCaseType(caseTypeId);
        long reference = cases.seed(CaseState.Accepted, caseData(NOT_VIEWED_YET, null));

        cases.event(reference, UpdateNotificationStateEvent.EVENT_ID, caseData(VIEWED, null))
            .submitExpectingSuccess();
        var laterResult = cases.event(reference, UpdateNotificationStateEvent.EVENT_ID, caseData(VIEWED, YES))
            .atRevision(0).submitExpectingSuccess();

        assertThat(laterResult.caseRevision()).isEqualTo(2);
        assertThat(notificationViewRepository.findItemIds(reference))
            .containsExactlyInAnyOrder("notification", "tribunalResponse", "tseResponse");
    }

    @ParameterizedTest
    @ValueSource(strings = {ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID})
    void acceptsViewsSubmittedAtAVersionSupersededByAnotherUsersUpdate(String caseTypeId) {
        var cases = events.forCaseType(caseTypeId);
        long reference = cases.seed(CaseState.Accepted, caseData(NOT_VIEWED_YET, null));
        int startVersion = cases.snapshot(reference).blobVersion();
        simulateCaseworkerUpdate(reference);

        submitAtVersion(caseTypeId, reference, UpdateNotificationStateEvent.EVENT_ID,
            caseData(VIEWED, YES), startVersion);

        var after = cases.snapshot(reference);
        assertThat(after.blobVersion()).isEqualTo(startVersion + 1);
        assertThat(after.rawData().get("claimant").asText()).isEqualTo("Caseworker update");
        assertThat(notificationViewRepository.findItemIds(reference))
            .containsExactlyInAnyOrder("notification", "tribunalResponse", "tseResponse");
    }

    @ParameterizedTest
    @ValueSource(strings = {ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID})
    void blobWritingEventIsRejectedAtASupersededVersion(String caseTypeId) {
        // Control for the test above: an event that still writes the blob fails in the same situation.
        var cases = events.forCaseType(caseTypeId);
        long reference = cases.seed(CaseState.Accepted, caseData(NOT_VIEWED_YET, null));
        int startVersion = cases.snapshot(reference).blobVersion();
        simulateCaseworkerUpdate(reference);

        assertThatThrownBy(() -> submitAtVersion(caseTypeId, reference, "UPDATE_RESPONDENT_PSE_STATE",
            caseData(VIEWED, null), startVersion))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }

    private void simulateCaseworkerUpdate(long reference) {
        jdbc.update("""
            update ccd.case_data
               set data = jsonb_set(data, '{claimant}', '"Caseworker update"'), version = version + 1
             where reference = ?
            """, reference);
    }

    // Submits as CCD would for an event started when the case was at the given version.
    private void submitAtVersion(String caseTypeId, long reference, String eventId, CaseData data, int version) {
        Map<String, Object> stored = jdbc.queryForMap(
            "select id, state, case_revision from ccd.case_data where reference = ?", reference);
        DecentralisedCaseEvent event = DecentralisedCaseEvent.builder()
            .caseDetailsBefore(caseDetails(caseTypeId, reference, stored, new CaseData(), version))
            .caseDetails(caseDetails(caseTypeId, reference, stored, data, version))
            .internalCaseId(((Number) stored.get("id")).longValue())
            .startRevision(((Number) stored.get("case_revision")).longValue())
            .eventDetails(DecentralisedEventDetails.builder()
                .caseType(caseTypeId)
                .eventId(eventId)
                .eventName(eventId)
                .build())
            .build();
        submissionService.submit(event, SDK_TEST_TOKEN, UUID.randomUUID());
    }

    private CaseDetails caseDetails(String caseTypeId, long reference, Map<String, Object> stored,
                                    CaseData data, int version) {
        CaseDetails details = new CaseDetails();
        details.setId(String.valueOf(stored.get("id")));
        details.setReference(reference);
        details.setCaseTypeId(caseTypeId);
        details.setJurisdiction("EMPLOYMENT");
        details.setState((String) stored.get("state"));
        details.setVersion(version);
        details.setSecurityClassification(SecurityClassification.PUBLIC);
        details.setData(mapper.convertValue(data, new TypeReference<Map<String, JsonNode>>() {}));
        details.setSupplementaryData(Map.of());
        return details;
    }

    private static CaseData caseData(String viewState, String viewedByClaimant) {
        CaseData caseData = new CaseData();
        caseData.setSendNotificationCollection(List.of(SendNotificationTypeItem.builder()
            .id("notification")
            .value(SendNotificationType.builder()
                .notificationState(viewState)
                .respondNotificationTypeCollection(List.of(GenericTypeItem.<RespondNotificationType>builder()
                    .id("tribunalResponse")
                    .value(RespondNotificationType.builder().state(viewState).build())
                    .build()))
                .build())
            .build()));
        caseData.setGenericTseApplicationCollection(List.of(GenericTseApplicationTypeItem.builder()
            .id("application")
            .value(GenericTseApplicationType.builder()
                .respondCollection(List.of(TseRespondTypeItem.builder()
                    .id("tseResponse")
                    .value(TseRespondType.builder().viewedByClaimant(viewedByClaimant).build())
                    .build()))
                .build())
            .build()));
        return caseData;
    }
}
