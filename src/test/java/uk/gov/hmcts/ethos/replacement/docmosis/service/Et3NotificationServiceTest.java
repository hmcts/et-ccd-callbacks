package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TestEmailService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class Et3NotificationServiceTest {
    private Et3NotificationService et3NotificationService;
    private EmailService emailService;
    private CaseData caseData;
    private CaseDetails caseDetails;

    @Captor
    ArgumentCaptor<Map<String, Object>> personalisation;

    @BeforeEach
    void setUp() {
        emailService = spy(new TestEmailService());
        et3NotificationService = new Et3NotificationService(emailService);

        caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("12345/6789")
            .withClaimantType("claimant@unrepresented.com")
            .withRepresentativeClaimantType("Claimant Rep", "claimant@represented.com")
            .withClaimantIndType("Claimant", "LastName", "Mr", "Mr")
            .withRespondentWithAddress("Respondent Unrepresented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null, "respondent@unrepresented.com")
            .withRespondentWithAddress("Respondent Represented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null)
            .withRespondentRepresentative("Respondent Represented", "Rep LastName", "res@rep.com")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.setCaseId("1234");
        caseData = caseDetails.getCaseData();
        caseData.setClaimant("Claimant LastName");
    }

    @Test
    void sendNotifications_shouldSendThreeNotifications() {
        et3NotificationService.sendNotifications(caseDetails);

        verify(emailService, times(1)).sendEmail(any(), eq("claimant@represented.com"), personalisation.capture());
        assertThat(personalisation.getValue()).containsEntry("linkToCitizenHub", "citizenUrl1234");

        verify(emailService, times(1)).sendEmail(any(), eq("respondent@unrepresented.com"), personalisation.capture());
        assertThat(personalisation.getValue()).containsEntry("linkToExUI", "exuiUrl1234");

        verify(emailService, times(1)).sendEmail(any(), eq("res@rep.com"), personalisation.capture());
        assertThat(personalisation.getValue()).containsEntry("linkToExUI", "exuiUrl1234");
    }

    @Test
    void sendNotifications_shouldHandleMissingEmails() {
        caseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(null);
        caseData.getRepCollection().get(0).getValue().setRepresentativeEmailAddress(null);
        caseData.getRespondentCollection().get(0).getValue().setRespondentEmail(null);
        et3NotificationService.sendNotifications(caseDetails);
        verify(emailService, times(0)).sendEmail(any(), any(), any());
    }
}