package uk.gov.hmcts.reform.et.syaapi.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.ccd.sdk.testing.CcdEventTestSupport;
import uk.gov.hmcts.ccd.sdk.testing.CcdSdkTest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.et.syaapi.models.HubLinksStatusesRequest;
import uk.gov.hmcts.reform.et.syaapi.models.SendNotificationStateUpdateRequest;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;

@CcdSdkTest(
    components = {EtJsonCcdConfig.class, JacksonConfiguration.class, ETCaseView.class,
        CitizenSystemEventService.class, HubLinkService.class, NotificationViewService.class,
        SendNotificationService.class},
    repositories = {HubLinkStatusRepository.class, DigitalCaseFileRepository.class,
        NotificationViewRepository.class},
    entities = {HubLinkStatus.class, DigitalCaseFile.class, NotificationView.class}
)
@Import(CitizenSystemEventIntegrationTest.SystemEventExecutorConfiguration.class)
@TestPropertySource(properties = {
    "ccd.decentralised-runtime.system-user.id=" + CitizenSystemEventIntegrationTest.SYSTEM_USER_ID,
    "ccd.decentralised-runtime.system-user.username=et-system@example.com",
    "ccd.decentralised-runtime.system-user.first-name=Employment Tribunals",
    "ccd.decentralised-runtime.system-user.last-name=System"
})
class CitizenSystemEventIntegrationTest {

    static final String SYSTEM_USER_ID = "et-system-user-id";
    private static final String AUTHORIZATION = "Bearer citizen";
    private static final String CITIZEN_ID = "citizen-id";

    @Autowired
    private CcdEventTestSupport<CaseData, CaseState> events;
    @Autowired
    private HubLinkService hubLinkService;
    @Autowired
    private SendNotificationService sendNotificationService;
    @Autowired
    private HubLinkStatusRepository hubLinkStatusRepository;
    @Autowired
    private NotificationViewRepository notificationViewRepository;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private IdamClient idamClient;
    @MockitoBean
    private AuthTokenGenerator authTokenGenerator;
    @MockitoBean
    private CoreCaseDataApi ccdApiClient;
    // Collaborators of SendNotificationService that marking a notification viewed does not use.
    @MockitoBean
    private CaseService caseService;
    @MockitoBean
    private CaseDocumentService caseDocumentService;
    @MockitoBean
    private uk.gov.hmcts.reform.et.syaapi.helper.CaseDetailsConverter caseDetailsConverter;
    @MockitoBean
    private NotificationPseService notificationPseService;
    @MockitoBean
    private FeatureToggleService featureToggleService;

    @TestConfiguration
    @ComponentScan(basePackages = "uk.gov.hmcts.ccd.sdk.impl", useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\.SystemEventExecutorImpl"))
    static class SystemEventExecutorConfiguration {
    }

    @BeforeEach
    void setUp() {
        when(idamClient.getUserInfo(AUTHORIZATION)).thenReturn(UserInfo.builder()
            .uid(CITIZEN_ID).sub("jane@example.com").givenName("Jane").familyName("Doe")
            .roles(List.of("citizen")).build());
        when(authTokenGenerator.generate()).thenReturn("s2s");
    }

    @ParameterizedTest
    @ValueSource(strings = {ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID})
    void storesHubLinkStatusesAndRecordsHistoryWithoutChangingTheBlob(String caseTypeId) {
        var cases = events.forCaseType(caseTypeId);
        long reference = cases.seed(CaseState.Accepted, new CaseData());
        final var before = cases.snapshot(reference);
        readableByCitizen(caseTypeId, reference, new CaseData());
        HubLinksStatuses statuses = new HubLinksStatuses();

        hubLinkService.updateHubLinkStatuses(HubLinksStatusesRequest.builder()
            .caseId(String.valueOf(reference)).caseTypeId(caseTypeId).hubLinksStatuses(statuses).build(),
            AUTHORIZATION);

        assertThat(hubLinkStatusRepository.findById(reference)).isPresent();
        var after = cases.snapshot(reference);
        assertThat(after.rawData()).isEqualTo(before.rawData());
        assertThat(after.blobVersion()).isEqualTo(before.blobVersion());
        assertHistory(reference, HubLinkService.EVENT_ID, HubLinkService.EVENT_NAME);
    }

    @ParameterizedTest
    @ValueSource(strings = {ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID})
    void recordsNotificationViewsAndHistoryWithoutChangingTheBlob(String caseTypeId) {
        var cases = events.forCaseType(caseTypeId);
        CaseData unviewed = notification(NOT_VIEWED_YET);
        long reference = cases.seed(CaseState.Accepted, unviewed);
        final var before = cases.snapshot(reference);
        readableByCitizen(caseTypeId, reference, unviewed);
        SendNotificationStateUpdateRequest request = SendNotificationStateUpdateRequest.builder()
            .caseId(String.valueOf(reference)).caseTypeId(caseTypeId).sendNotificationId("notification").build();

        CaseDetails result = sendNotificationService.updateSendNotificationState(AUTHORIZATION, request);

        assertThat(notificationViewRepository.findItemIds(reference))
            .containsExactlyInAnyOrder("notification", "tribunalResponse");
        assertThat(mapper.convertValue(result.getData(), CaseData.class).getSendNotificationCollection()
            .getFirst().getValue().getNotificationState()).isEqualTo(VIEWED);
        var after = cases.snapshot(reference);
        assertThat(after.rawData()).isEqualTo(before.rawData());
        assertThat(after.blobVersion()).isEqualTo(before.blobVersion());
        assertHistory(reference, NotificationViewService.EVENT_ID, NotificationViewService.EVENT_NAME);
    }

    private void readableByCitizen(String caseTypeId, long reference, CaseData data) {
        when(ccdApiClient.getCase(eq(AUTHORIZATION), any(), eq(String.valueOf(reference))))
            .thenReturn(CaseDetails.builder()
                .id(reference)
                .caseTypeId(caseTypeId)
                .data(mapper.convertValue(data, new TypeReference<Map<String, Object>>() {}))
                .build());
    }

    private void assertHistory(long reference, String eventId, String eventName) {
        Map<String, Object> event = jdbc.queryForMap("""
            select ce.event_id, ce.event_name, ce.user_id, ce.user_first_name, ce.user_last_name,
                   ce.proxied_by, ce.proxied_by_first_name, ce.proxied_by_last_name
              from ccd.case_event ce
              join ccd.case_data cd on cd.id = ce.case_data_id
             where cd.reference = ?
             order by ce.id desc
             limit 1
            """, reference);
        // The history names the citizen, made on their behalf by the et-cos system user.
        assertThat(event)
            .containsEntry("event_id", eventId)
            .containsEntry("event_name", eventName)
            .containsEntry("user_id", CITIZEN_ID)
            .containsEntry("user_first_name", "Jane")
            .containsEntry("user_last_name", "Doe")
            .containsEntry("proxied_by", SYSTEM_USER_ID)
            .containsEntry("proxied_by_first_name", "Employment Tribunals")
            .containsEntry("proxied_by_last_name", "System");
    }

    private static CaseData notification(String state) {
        CaseData caseData = new CaseData();
        caseData.setSendNotificationCollection(List.of(SendNotificationTypeItem.builder()
            .id("notification")
            .value(SendNotificationType.builder()
                .notificationState(state)
                .respondNotificationTypeCollection(List.of(GenericTypeItem.<RespondNotificationType>builder()
                    .id("tribunalResponse")
                    .value(RespondNotificationType.builder().state(state).build())
                    .build()))
                .build())
            .build()));
        return caseData;
    }
}
