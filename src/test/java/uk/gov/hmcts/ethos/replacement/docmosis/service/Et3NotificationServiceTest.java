package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.MethodNamingConventions", "PMD.LawOfDemeter", "PMD.UnusedPrivateField", "PMD.TooManyMethods",
    "PMD.ExcessiveImports"})
class Et3NotificationServiceTest {
    @InjectMocks
    private Et3NotificationService et3NotificationService;
    @Mock
    private EmailService emailService;
    private CaseData caseData;

    @Value("${et3notification.template.id}")
    private String templateId;

    @BeforeEach
    void setUp() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
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

        caseData = caseDetails.getCaseData();
        caseData.setClaimant("Claimant LastName");
    }

    @Test
    void sendNotifications_shouldSendThreeNotifications() {
        et3NotificationService.sendNotifications(caseData);
        verify(emailService, times(1)).sendEmail(any(), eq("claimant@represented.com"), any());
        verify(emailService, times(1)).sendEmail(any(), eq("respondent@unrepresented.com"), any());
        verify(emailService, times(1)).sendEmail(any(), eq("res@rep.com"), any());
    }
}
