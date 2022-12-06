package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class NocNotificationServiceTest {
    @InjectMocks
    private NocNotificationService nocNotificationService;
    @Mock
    private EmailService emailService;
    private CaseData caseData;
    private CaseDetails caseDetails;
    private CaseDetails caseDetailsBefore;
    private CallbackRequest callbackRequest;

    @BeforeEach
    void setUp() {
        Organisation organisationToAdd = Organisation.builder()
            .organisationID("1")
            .organisationName("New Organisation").build();
        Organisation organisationToRemove = Organisation.builder()
            .organisationID("2")
            .organisationName("Old Organisation").build();

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
            .withChangeOrganisationRequestField(
                organisationToAdd,
                organisationToRemove,
                null,
                null,
                null)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetailsBefore = CaseDataBuilder.builder()
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

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build();

        caseDetails.setCaseId("1234");
        caseData = caseDetails.getCaseData();
        caseData.setClaimant("Claimant LastName");
    }

    @Test
    void sendNotifications_shouldSendThreeNotifications() {
        nocNotificationService.sendNotificationOfChangeEmails(callbackRequest, caseData);
        // Claimant
        verify(emailService, times(1)).sendEmail(any(), eq("claimant@represented.com"), any());
        // Previous respondent
        verify(emailService, times(1)).sendEmail(any(), eq("respondent@unrepresented.com"), any());
        //New respondent
        verify(emailService, times(1)).sendEmail(any(), eq("res@rep.com"), any());
        // Tribunal
        verify(emailService, times(1)).sendEmail(any(), eq("respondent@unrepresented.com"), any());
        // Respondent
        verify(emailService, times(1)).sendEmail(any(), eq("res@rep.com"), any());
    }

    @Test
    void handle_missing_emails() {
        reset(emailService);
        caseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(null);
        caseData.getRepCollection().get(0).getValue().setRepresentativeEmailAddress(null);
        caseData.getRespondentCollection().get(0).getValue().setRespondentEmail(null);
        nocNotificationService.sendNotificationOfChangeEmails(callbackRequest, caseData);
        verify(emailService, times(0)).sendEmail(any(), any(), any());
    }
}
