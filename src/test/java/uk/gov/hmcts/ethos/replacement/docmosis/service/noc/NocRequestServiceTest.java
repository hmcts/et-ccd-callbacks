package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class NocRequestServiceTest {

    @InjectMocks
    private NocRequestService nocRequestService;
    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private NocNotificationService nocNotificationService;
    @Mock
    private EmailService emailService;
    @Mock
    private CaseAccessService caseAccessService;
    @Mock
    private EmailNotificationService emailNotificationService;

    private static final String USER_TOKEN = "userToken";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(nocRequestService,
            "nocOrgAdminNotRepresentingTemplateId", "nocOrgAdminNotRepresentingTemplateId");
        ReflectionTestUtils.setField(nocRequestService,
            "nocLegalRepNoLongerAssignedTemplateId", "nocLegalRepNoLongerAssignedTemplateId");
        ReflectionTestUtils.setField(nocRequestService,
            "nocCitizenNoLongerRepresentedTemplateId", "nocCitizenNoLongerRepresentedTemplateId");
        ReflectionTestUtils.setField(nocRequestService,
            "nocOtherPartyNotRepresentedTemplateId", "nocOtherPartyNotRepresentedTemplateId");
    }

    @Test
    void shouldRevokeClaimantLegalRep() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("123456789/1234")
            .withClaimant("Claimant Name")
            .withClaimantType("claimant@test.com")
            .withRespondent(RespondentSumType.builder()
                .respondentName("Respondent Name")
                .respondentEmail("respondent@test.com")
                .build())
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId("caseId");

        RepresentedTypeC rep = RepresentedTypeC.builder()
            .representativeId("repId")
            .nameOfRepresentative("Claimant Legal Rep Name")
            .nameOfOrganisation("Org Name")
            .representativeEmailAddress("rep@test.com")
            .myHmctsOrganisation(Organisation.builder()
                .organisationID("orgId")
                .build())
            .build();
        caseDetails.getCaseData().setRepresentativeClaimantType(rep);

        when(nocNotificationService.findClaimantRepOrgSuperUserEmail(rep)).thenReturn("org@test.com");
        when(emailService.getCitizenCaseLink(anyString())).thenReturn("claimantCitizenCaseLink");
        when(emailService.getExuiCaseLink(anyString())).thenReturn("respondentExUICaseLink");
        when(emailService.getSyrCaseLink(anyString(), anyString())).thenReturn("respondentCitizenCaseLink");
        when(caseAccessService.getCaseUserAssignmentsById(any()))
            .thenReturn(List.of(CaseUserAssignment.builder().build()));
        when(emailNotificationService.getRespondentsAndRepsEmailAddresses(any(), any()))
            .thenReturn(Map.of("respSolicitor@test.com", "respondentId"));

        nocRequestService.revokeClaimantLegalRep(caseDetails, USER_TOKEN);

        verify(nocCcdService, times(1)).revokeClaimantRepresentation(anyString(), any());
        verify(emailService, times(1)).sendEmail(
            eq("nocOrgAdminNotRepresentingTemplateId"),
            eq("org@test.com"),
            eq(Map.of(
                "case_number", "123456789/1234",
                "claimant", "Claimant Name",
                "list_of_respondents", "Respondent Name",
                "legalRepName", "Claimant Legal Rep Name"
            ))
        );
        verify(emailService, times(1)).sendEmail(
            eq("nocLegalRepNoLongerAssignedTemplateId"),
            eq("rep@test.com"),
            eq(Map.of(
                "case_number", "123456789/1234",
                "claimant", "Claimant Name",
                "list_of_respondents", "Respondent Name"
            ))
        );
        verify(emailService, times(1)).sendEmail(
            eq("nocCitizenNoLongerRepresentedTemplateId"),
            eq("claimant@test.com"),
            eq(Map.of(
                "case_number", "123456789/1234",
                "claimant", "Claimant Name",
                "list_of_respondents", "Respondent Name",
                "legalRepOrg", "Org Name",
                "linkToCitUI", "claimantCitizenCaseLink"
            ))
        );
        verify(emailService, times(1)).sendEmail(
            eq("nocOtherPartyNotRepresentedTemplateId"),
            eq("respSolicitor@test.com"),
            eq(Map.of(
                "case_number", "123456789/1234",
                "claimant", "Claimant Name",
                "list_of_respondents", "Respondent Name",
                "party_name", "Claimant Name",
                "linkToCitUI", "respondentCitizenCaseLink"
            ))
        );
    }
}