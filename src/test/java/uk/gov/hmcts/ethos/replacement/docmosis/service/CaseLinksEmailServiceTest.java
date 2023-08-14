package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.config.NotificationProperties;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class CaseLinksEmailServiceTest {
    private static final String RESPONDENT_NAME = "Respondent";
    private static final String REP_EMAIL = "rep1@test.com";
    private static final String LEGAL_REP = "legalRep";

    private CaseLinksEmailService caseLinksEmailService;
    @MockBean
    private EmailService emailService;
    @SpyBean
    private NotificationProperties notificationProperties;
    private CaseDetails caseDetails;
    private RespondentSumType respondentSumType;
    private Map<String, Object> claimantPersonalisation;
    private Map<String, Object> respondentPersonalisation;

    @BeforeEach
    void setUp() {
        caseLinksEmailService = new CaseLinksEmailService(notificationProperties, emailService);
        ReflectionTestUtils.setField(notificationProperties, "exuiUrl", "exuiUrl/");
        ReflectionTestUtils.setField(notificationProperties, "citizenUrl", "citizenUrl/");
        ReflectionTestUtils.setField(caseLinksEmailService, "caseLinkedTemplateId", "1");
        ReflectionTestUtils.setField(caseLinksEmailService, "caseUnlinkedTemplateId", "2");

        claimantPersonalisation = Map.of(
                "caseNumber", "12345/6789",
                "linkToManageCase", "citizenUrl/1234");

        respondentPersonalisation = Map.of(
                "caseNumber", "12345/6789",
                "linkToManageCase", "exuiUrl/1234");

        respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(RESPONDENT_NAME);
        respondentSumType.setRespondentEmail("res@rep.com");

        caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("12345/6789")
                .withClaimantType("claimant@unrepresented.com")
                .withRepresentativeClaimantType("Claimant Rep", "claimant@represented.com")
                .withClaimantIndType("Claimant", "LastName", "Mr", "Mr")
                .withRespondent(respondentSumType)
                .withRespondentWithAddress("Respondent Unrepresented",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        null, "respondent@unrepresented.com")
                .withRespondentWithAddress("Respondent Represented",
                        "32 Sweet Street", "14 House", null,
                        "Manchester", "M11 4ED", "United Kingdom",
                        null)
                .withRespondentRepresentative("Respondent Represented", LEGAL_REP, REP_EMAIL)
                .withHearing("1", "test", "Judy", "Venue", List.of("Telephone", "Video"),
                        "length num", "type", "Yes")
                .withHearingSession(
                        0,
                        "1",
                        "2029-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_LISTED,
                        true)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.setCaseId("1234");
    }

    @Test
    void shouldSendCaseLinkingEmails() {
        caseLinksEmailService.sendCaseLinkingEmails(caseDetails, true);

        verify(emailService, times(4)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail("1", "claimant@unrepresented.com", claimantPersonalisation);
        verify(emailService).sendEmail("1", "respondent@unrepresented.com", respondentPersonalisation);
        verify(emailService).sendEmail("1", "rep1@test.com", respondentPersonalisation);
        verify(emailService).sendEmail("1", "res@rep.com", respondentPersonalisation);
    }

    @Test
    void shouldSendCaseUnLinkingEmails() {
        caseLinksEmailService.sendCaseLinkingEmails(caseDetails, false);

        verify(emailService, times(4)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail("2", "claimant@unrepresented.com", claimantPersonalisation);
        verify(emailService).sendEmail("2", "respondent@unrepresented.com", respondentPersonalisation);
        verify(emailService).sendEmail("2", "rep1@test.com", respondentPersonalisation);
        verify(emailService).sendEmail("2", "res@rep.com", respondentPersonalisation);
    }

}
