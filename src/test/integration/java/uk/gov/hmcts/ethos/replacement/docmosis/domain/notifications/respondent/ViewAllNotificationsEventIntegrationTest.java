package uk.gov.hmcts.ethos.replacement.docmosis.domain.notifications.respondent;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.sdk.testing.CcdEventTestSupport;
import uk.gov.hmcts.ccd.sdk.testing.CcdSdkTest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
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

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@CcdSdkTest(
    components = {EtJsonCcdConfig.class, ViewAllNotificationsEvent.class, ETCaseView.class,
        JacksonConfiguration.class},
    repositories = {HubLinkStatusRepository.class, DigitalCaseFileRepository.class,
        NotificationViewRepository.class},
    entities = {HubLinkStatus.class, DigitalCaseFile.class, NotificationView.class}
)
class ViewAllNotificationsEventIntegrationTest {

    @Autowired
    private CcdEventTestSupport<CaseData, CaseState> events;

    @ParameterizedTest
    @ValueSource(strings = {ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID})
    void submittingViewDoesNotChangeStoredCaseData(String caseTypeId) {
        var cases = events.forCaseType(caseTypeId);
        CaseData stored = new CaseData();
        stored.setEthosCaseReference("1234/2026");
        long reference = cases.seed(CaseState.Accepted, stored);
        final var before = cases.snapshot(reference);

        CaseData submitted = new CaseData();
        submitted.setEthosCaseReference("1234/2026");
        submitted.setPseViewNotifications("Rendered notifications");
        cases.event(reference, ViewAllNotificationsEvent.EVENT_ID, submitted).submitExpectingSuccess();
        var laterResult = cases.event(reference, ViewAllNotificationsEvent.EVENT_ID, submitted)
            .atRevision(0).submitExpectingSuccess();

        assertThat(laterResult.projectedCase().getPseViewNotifications()).isNull();
        assertThat(laterResult.rawData()).isEqualTo(before.rawData());
        assertThat(laterResult.blobVersion()).isEqualTo(before.blobVersion());
        assertThat(laterResult.caseRevision()).isEqualTo(2);
    }
}
