package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.LawOfDemeter"})
class NocNotificationServiceTest {
    private static final String NEW_ORG_ADMIN_EMAIL = "orgadmin1@test.com";
    private static final String OLD_ORG_ADMIN_EMAIL = "orgadmin2@test.com";
    private static final String NEW_ORG_ID = "1";
    private static final String OLD_ORG_ID = "2";
    @InjectMocks
    private NocNotificationService nocNotificationService;
    @Mock
    private EmailService emailService;
    @Mock
    private OrganisationClient organisationClient;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    private CaseDetails caseDetailsBefore;
    private CaseDetails caseDetailsNew;

    @Mock
    private NocRespondentHelper nocRespondentHelper;

    @BeforeEach
    void setUp() {
        Organisation organisationToAdd = Organisation.builder()
            .organisationID(NEW_ORG_ID)
            .organisationName("New Organisation").build();
        Organisation organisationToRemove = Organisation.builder()
            .organisationID(OLD_ORG_ID)
            .organisationName("Old Organisation").build();

        caseDetailsNew = CaseDataBuilder.builder()
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
            .withTwoRespondentRepresentative(NEW_ORG_ID, OLD_ORG_ID, NEW_ORG_ADMIN_EMAIL, OLD_ORG_ADMIN_EMAIL)
            .withRespondent("Respondent", YES, "2022-03-01", "res@rep.com", false)
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
            .withTwoRespondentRepresentative(NEW_ORG_ID, OLD_ORG_ID, NEW_ORG_ADMIN_EMAIL, OLD_ORG_ADMIN_EMAIL)
            .withRespondent("Respondent", YES, "2022-03-01",
                "res@rep.com", false)
            .withChangeOrganisationRequestField(
                organisationToAdd,
                organisationToRemove,
                null,
                null,
                null)
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetailsBefore.setCaseId("1682497607486678");
        caseDetailsNew.setCaseId("1682497607486678");
        caseDetailsBefore.getCaseData().setClaimant("Claimant LastName");
        caseDetailsNew.getCaseData().setClaimant("Claimant LastName");
        caseDetailsBefore.getCaseData().setTribunalCorrespondenceEmail("respondent@unrepresented.com");

        when(authTokenGenerator.generate()).thenReturn("authToken");
        when(adminUserService.getAdminUserToken()).thenReturn("adminUserToken");
    }

    @Test
    void sendNotificationsShouldSendFiveNotifications() {
        RetrieveOrgByIdResponse.SuperUser oldSuperUser = RetrieveOrgByIdResponse.SuperUser.builder()
                .email(OLD_ORG_ADMIN_EMAIL).build();
        RetrieveOrgByIdResponse retrieveOrgByIdResponse1 = RetrieveOrgByIdResponse.builder()
                .superUser(oldSuperUser).build();
        when(organisationClient.getOrganisationById(anyString(), anyString(), eq(OLD_ORG_ID)))
                .thenReturn(ResponseEntity.ok(retrieveOrgByIdResponse1));

        RetrieveOrgByIdResponse.SuperUser newSuperUser = RetrieveOrgByIdResponse.SuperUser.builder()
                .email(NEW_ORG_ADMIN_EMAIL).build();
        RetrieveOrgByIdResponse retrieveOrgByIdResponse2 = RetrieveOrgByIdResponse.builder()
                .superUser(newSuperUser).build();
        when(organisationClient.getOrganisationById(anyString(), anyString(), eq(NEW_ORG_ID)))
                .thenReturn(ResponseEntity.ok(retrieveOrgByIdResponse2));

        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Respondent");
        respondentSumType.setRespondentEmail("res@rep.com");
        when(nocRespondentHelper.getRespondent(any(), any())).thenReturn(respondentSumType);
        nocNotificationService.sendNotificationOfChangeEmails(
                caseDetailsBefore,
                caseDetailsNew,
                caseDetailsBefore.getCaseData().getChangeOrganisationRequestField());
        // Claimant
        verify(emailService, times(1)).sendEmail(any(), eq("claimant@represented.com"), any());
        //New Representative
        verify(emailService, times(1)).sendEmail(any(), eq(NEW_ORG_ADMIN_EMAIL), any());
        //Old Representative
        verify(emailService, times(1)).sendEmail(any(), eq(OLD_ORG_ADMIN_EMAIL), any());
        // Tribunal
        verify(emailService, times(1)).sendEmail(any(), eq("respondent@unrepresented.com"), any());
        // Respondent
        verify(emailService, times(1)).sendEmail(any(), eq("res@rep.com"), any());
    }

    @Test
    void handleMissingEmails() {
        reset(emailService);

        when(organisationClient.getOrganisationById(anyString(), anyString(), anyString()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        CaseData oldCaseData = caseDetailsBefore.getCaseData();

        oldCaseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(null);
        oldCaseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(null);
        oldCaseData.getRepCollection().get(0).getValue().setRepresentativeEmailAddress(null);
        oldCaseData.getRepCollection().get(1).getValue().setRepresentativeEmailAddress(null);
        oldCaseData.setTribunalCorrespondenceEmail(null);

        caseDetailsNew.getCaseData().getRepCollection().get(0).getValue().setRepresentativeEmailAddress(null);

        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName("Respondent");
        respondentSumType.setRespondentEmail(null);

        when(nocRespondentHelper.getRespondent(any(), any())).thenReturn(respondentSumType);

        nocNotificationService.sendNotificationOfChangeEmails(
                caseDetailsBefore,
                caseDetailsNew,
                caseDetailsBefore.getCaseData().getChangeOrganisationRequestField());
        verify(emailService, times(0)).sendEmail(any(), any(), any());
    }
}
