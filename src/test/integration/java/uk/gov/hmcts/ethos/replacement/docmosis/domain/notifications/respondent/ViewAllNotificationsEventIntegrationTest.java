package uk.gov.hmcts.ethos.replacement.docmosis.domain.notifications.respondent;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.sdk.testing.CcdEventTestSupport;
import uk.gov.hmcts.ccd.sdk.testing.CcdSdkTest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig;
import uk.gov.hmcts.ethos.replacement.docmosis.config.JacksonConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.ETCaseView;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@CcdSdkTest(
    components = {EtJsonCcdConfig.class, ViewAllNotificationsEvent.class, ETCaseView.class,
        JacksonConfiguration.class},
    repositories = HubLinkStatusRepository.class,
    entities = HubLinkStatus.class
)
class ViewAllNotificationsEventIntegrationTest {

    @Autowired
    private CcdEventTestSupport<CaseData, CaseState> events;

    @Test
    void submittingViewDoesNotChangeStoredCaseData() {
        var englandWales = events.forCaseType(ENGLANDWALES_CASE_TYPE_ID);
        CaseData stored = new CaseData();
        stored.setEthosCaseReference("1234/2026");
        long reference = englandWales.seed(CaseState.Accepted, stored);
        final var before = englandWales.snapshot(reference);

        CaseData submitted = new CaseData();
        submitted.setEthosCaseReference("1234/2026");
        submitted.setPseViewNotifications("Rendered notifications");
        englandWales.event(reference, ViewAllNotificationsEvent.EVENT_ID, submitted).submitExpectingSuccess();
        var laterResult = englandWales.event(reference, ViewAllNotificationsEvent.EVENT_ID, submitted)
            .atRevision(0).submitExpectingSuccess();

        assertThat(laterResult.projectedCase().getPseViewNotifications()).isNull();
        assertThat(laterResult.rawData()).isEqualTo(before.rawData());
        assertThat(laterResult.blobVersion()).isEqualTo(before.blobVersion());
        assertThat(laterResult.caseRevision()).isEqualTo(2);
    }
}
