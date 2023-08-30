package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLink;
import uk.gov.hmcts.et.common.model.ccd.types.LinkReason;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class CaseLinksEmailServiceTest {
    private static final String RESPONDENT_NAME = "Respondent";
    private static final String REP_EMAIL = "rep1@test.com";
    private static final String LEGAL_REP = "legalRep";
    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";

    private CaseLinksEmailService caseLinksEmailService;
    @MockBean
    private EmailService emailService;
    @MockBean
    private CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;
    private CCDRequest ccdRequest;
    private SubmitEvent submitEvent;
    private CaseDetails caseDetails;
    private Map<String, Object> claimantPersonalisation;
    private Map<String, Object> respondentPersonalisation;
    private Map<String, Object> repPersonalisation;

    @BeforeEach
    void setUp() {
        caseLinksEmailService = new CaseLinksEmailService(
                caseRetrievalForCaseWorkerService,
                emailService);
        ReflectionTestUtils.setField(caseLinksEmailService, "caseLinkedTemplateId", "1");
        ReflectionTestUtils.setField(caseLinksEmailService, "caseUnlinkedTemplateId", "2");
        ReflectionTestUtils.setField(caseLinksEmailService, "caseLinkedLegalRepTemplateId", "3");
        ReflectionTestUtils.setField(caseLinksEmailService, "caseUnlinkedLegalRepTemplateId", "4");
        when(emailService.getCitizenCaseLink("1234")).thenReturn("citizenUrl/1234");
        when(emailService.getExuiCaseLink("1234")).thenReturn("manageCase/1234");

        claimantPersonalisation = Map.of(
                "caseNumber", "12345/6789",
                "linkToManageCase", "To manage your case, go to citizenUrl/1234");

        respondentPersonalisation = Map.of(
                "caseNumber", "12345/6789",
                "linkToManageCase", "");

        repPersonalisation = Map.of(
                "caseTitle", "Claimant LastName vs Respondent, Respondent Unrepresented, Respondent Represented",
                "caseNumber", "12345/6789",
                "linkToManageCase", "manageCase/1234");

        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(RESPONDENT_NAME);
        respondentSumType.setRespondentEmail("res@rep.com");

        CaseLink caseLink = getCaseLink("CLRC017");
        ListTypeItem<CaseLink> caseLinks = ListTypeItem.from(caseLink);

        caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("12345/6789")
                .withClaimantType("claimant@unrepresented.com")
                .withClaimantRepresentedQuestion("No")
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
                .withCaseLinks(caseLinks)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.setCaseId("1234");
        ccdRequest = new CCDRequest(caseDetails);

        submitEvent = new SubmitEvent();
        submitEvent.setCaseData(new CaseData());
        when(caseRetrievalForCaseWorkerService.caseRetrievalRequest(eq(AUTH_TOKEN), any(), any(), any()))
                .thenReturn(submitEvent);
    }

    @Test
    void shouldSendCaseLinkingEmailsToAllParties() {
        caseLinksEmailService.sendMailWhenCaseLinkForHearing(ccdRequest,
                AUTH_TOKEN,
                true);

        verify(emailService, times(4)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail("1", "claimant@unrepresented.com", claimantPersonalisation);
        verify(emailService).sendEmail("1", "respondent@unrepresented.com", respondentPersonalisation);
        verify(emailService).sendEmail("1", "res@rep.com", respondentPersonalisation);
        verify(emailService).sendEmail("3", "rep1@test.com", repPersonalisation);
    }

    @Test
    void shouldNotSendCaseLinkingEmailsWhenNonHearingCaseLink() {
        CaseLink caseLink1 = getCaseLink("CLRC016");
        ListTypeItem<CaseLink> caseLinks = ListTypeItem.from(caseLink1);
        ccdRequest.getCaseDetails().getCaseData().setCaseLinks(caseLinks);

        caseLinksEmailService.sendMailWhenCaseLinkForHearing(ccdRequest,
                AUTH_TOKEN,
                true);

        verify(emailService, times(0)).sendEmail(any(), any(), any());
    }

    @Test
    void shouldSendEmailsWhenExistingCaseLinksAndAddingCaseLink() {
        CaseLink caseLink = getCaseLink("CLRC017");
        CaseLink caseLink1 = getCaseLink("CLRC016");
        ListTypeItem<CaseLink> caseLinksAfterSubmit = ListTypeItem.from(caseLink, caseLink1);
        caseDetails.getCaseData().setCaseLinks(caseLinksAfterSubmit);
        var caseLinksBeforeSubmit = ListTypeItem.from(caseLinksAfterSubmit.get(1));
        submitEvent.getCaseData().setCaseLinks(caseLinksBeforeSubmit);

        caseLinksEmailService.sendMailWhenCaseLinkForHearing(ccdRequest,
                AUTH_TOKEN,
                true);

        verify(emailService, times(4)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail("1", "claimant@unrepresented.com", claimantPersonalisation);
        verify(emailService).sendEmail("1", "respondent@unrepresented.com", respondentPersonalisation);
        verify(emailService).sendEmail("1", "res@rep.com", respondentPersonalisation);
        verify(emailService).sendEmail("3", "rep1@test.com", repPersonalisation);
    }

    @Test
    void shouldSendCaseUnLinkingEmailsWhenExistingCaseLinks() {
        CaseLink caseLink = getCaseLink("CLRC017");
        CaseLink caseLink1 = getCaseLink("CLRC016");
        ListTypeItem<CaseLink> caseLinksBeforeSubmit = ListTypeItem.from(caseLink, caseLink1);
        var caseLinksAfterSubmit = ListTypeItem.from(caseLinksBeforeSubmit.get(1));
        submitEvent.getCaseData().setCaseLinks(caseLinksBeforeSubmit);
        caseDetails.getCaseData().setCaseLinks(caseLinksAfterSubmit);

        caseLinksEmailService.sendMailWhenCaseLinkForHearing(ccdRequest,
                AUTH_TOKEN,
                false);

        verify(emailService, times(4)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail("2", "claimant@unrepresented.com", claimantPersonalisation);
        verify(emailService).sendEmail("2", "respondent@unrepresented.com", respondentPersonalisation);
        verify(emailService).sendEmail("2", "res@rep.com", respondentPersonalisation);
        verify(emailService).sendEmail("4", "rep1@test.com", repPersonalisation);
    }

    @Test
    void shouldSendCaseUnLinkingEmailsWhenRemovingLastCaseLink() {
        CaseLink caseLink = getCaseLink("CLRC017");
        ListTypeItem<CaseLink> caseLinksBeforeSubmit = ListTypeItem.from(caseLink);
        submitEvent.getCaseData().setCaseLinks(caseLinksBeforeSubmit);

        caseDetails.getCaseData().setCaseLinks(null);

        caseLinksEmailService.sendMailWhenCaseLinkForHearing(ccdRequest,
                AUTH_TOKEN,
                false);

        verify(emailService, times(4)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail("2", "claimant@unrepresented.com", claimantPersonalisation);
        verify(emailService).sendEmail("2", "respondent@unrepresented.com", respondentPersonalisation);
        verify(emailService).sendEmail("2", "res@rep.com", respondentPersonalisation);
        verify(emailService).sendEmail("4", "rep1@test.com", repPersonalisation);
    }

    @Test
    void shouldSendCaseUnLinkingEmailsWhenRemovingLastCaseLinkFromEmptyList() {
        CaseLink caseLink = getCaseLink("CLRC017");
        ListTypeItem<CaseLink> caseLinksBeforeSubmit = ListTypeItem.from(caseLink);
        submitEvent.getCaseData().setCaseLinks(caseLinksBeforeSubmit);
        ListTypeItem<CaseLink> caseLinkListTypeItem = new ListTypeItem<>();
        caseDetails.getCaseData().setCaseLinks(caseLinkListTypeItem);

        caseLinksEmailService.sendMailWhenCaseLinkForHearing(ccdRequest,
                AUTH_TOKEN,
                false);

        verify(emailService, times(4)).sendEmail(any(), any(), any());
        verify(emailService).sendEmail("2", "claimant@unrepresented.com", claimantPersonalisation);
        verify(emailService).sendEmail("2", "respondent@unrepresented.com", respondentPersonalisation);
        verify(emailService).sendEmail("2", "res@rep.com", respondentPersonalisation);
        verify(emailService).sendEmail("4", "rep1@test.com", repPersonalisation);
    }

    @Test
    void shouldNotSendCaseUnLinkingEmailsWhenRemovingNonHearingLink() {
        ListTypeItem<CaseLink> caseLinkListTypeItem = new ListTypeItem<>();
        CaseLink caseLink1 = getCaseLink("CLRC016");
        ListTypeItem<CaseLink> caseLinksBeforeSubmit = ListTypeItem.from(caseLink1);
        submitEvent.getCaseData().setCaseLinks(caseLinksBeforeSubmit);
        caseDetails.getCaseData().setCaseLinks(caseLinkListTypeItem);

        caseLinksEmailService.sendMailWhenCaseLinkForHearing(ccdRequest,
                AUTH_TOKEN,
                false);

        verify(emailService, times(0)).sendEmail(any(), any(), any());
    }

    private CaseLink getCaseLink(String linkReasonCode) {
        LinkReason linkReason = new LinkReason();
        linkReason.setReason(linkReasonCode);
        ListTypeItem<LinkReason> linkReasons = ListTypeItem.from(linkReason, "1");

        return CaseLink.builder().caseReference("1").caseType(ENGLANDWALES_CASE_TYPE_ID)
                .reasonForLink(linkReasons).build();
    }

}
