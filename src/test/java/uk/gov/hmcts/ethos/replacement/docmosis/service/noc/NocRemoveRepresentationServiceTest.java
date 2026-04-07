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
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class NocRemoveRepresentationServiceTest {

    @InjectMocks
    private NocRemoveRepresentationService nocRemoveRepresentationService;
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
    @Mock
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;

    private static final String USER_TOKEN = "userToken";
    private static final String TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING = "nocOrgAdminNotRepresentingTemplateId";
    private static final String TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED = "nocLegalRepNoLongerAssignedTemplateId";
    private static final String TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED = "nocCitizenNoLongerRepresentedTemplateId";
    private static final String TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED = "nocOtherPartyNotRepresentedTemplateId";
    private static final String EMAIL_ORG_ADMIN = "org@test.com";
    private static final String EMAIL_LEGAL_REP = "rep@test.com";
    private static final String EMAIL_CLAIMANT = "claimant@test.com";
    private static final String EMAIL_RESP_SOLICITOR = "respSolicitor@test.com";
    private static final String CASE_REFERENCE = "123456789/1234";
    private static final String CASE_ID = "caseId";
    private static final String CLAIMANT_NAME = "Claimant Name";
    private static final String RESPONDENT_A_NAME = "Respondent Name A";
    private static final String CLAIMANT_LEGAL_REP_NAME = "Claimant Legal Rep Name";
    private static final String RESPONDENT_A_LEGAL_REP_NAME = "Respondent A Legal Rep Name";
    private static final String RESPONDENT_B_LEGAL_REP_NAME = "Respondent A Legal Rep Name";
    private static final String ORGANISATION_ID = "orgId";
    private static final String ORGANISATION_NAME = "Org Name";
    private static final String LINK_CITIZEN_CASE = "claimantCitizenCaseLink";
    private static final String LINK_RESP_EXUI_CASE = "respondentExUICaseLink";
    private static final String LINK_RESP_CITIZEN_CASE = "respondentCitizenCaseLink";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(nocRemoveRepresentationService,
            TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING, TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING);
        ReflectionTestUtils.setField(nocRemoveRepresentationService,
            TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED, TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED);
        ReflectionTestUtils.setField(nocRemoveRepresentationService,
            TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED, TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED);
        ReflectionTestUtils.setField(nocRemoveRepresentationService,
            TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED, TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED);
    }

    @Test
    void shouldRevokeClaimantLegalRep() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference(CASE_REFERENCE)
            .withClaimant(CLAIMANT_NAME)
            .withClaimantType(EMAIL_CLAIMANT)
            .withRespondent(RespondentSumType.builder()
                .respondentName(RESPONDENT_A_NAME)
                .respondentEmail("respondent@test.com")
                .build())
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.setCaseId(CASE_ID);

        RepresentedTypeC rep = RepresentedTypeC.builder()
            .representativeId("repId")
            .nameOfRepresentative(CLAIMANT_LEGAL_REP_NAME)
            .nameOfOrganisation(ORGANISATION_NAME)
            .representativeEmailAddress(EMAIL_LEGAL_REP)
            .myHmctsOrganisation(Organisation.builder()
                .organisationID("orgId")
                .build())
            .build();
        caseDetails.getCaseData().setRepresentativeClaimantType(rep);

        when(nocNotificationService.findClaimantRepOrgSuperUserEmail(rep)).thenReturn(EMAIL_ORG_ADMIN);
        when(emailService.getCitizenCaseLink(anyString())).thenReturn(LINK_CITIZEN_CASE);
        when(emailService.getExuiCaseLink(anyString())).thenReturn(LINK_RESP_EXUI_CASE);
        when(emailService.getSyrCaseLink(anyString(), anyString())).thenReturn(LINK_RESP_CITIZEN_CASE);
        when(caseAccessService.getCaseUserAssignmentsById(any()))
            .thenReturn(List.of(CaseUserAssignment.builder().build()));
        when(emailNotificationService.getRespondentsAndRepsEmailAddresses(any(), any()))
            .thenReturn(Map.of(EMAIL_RESP_SOLICITOR, "respondentId"));

        nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails, USER_TOKEN);

        verify(nocCcdService, times(1)).revokeClaimantRepresentation(USER_TOKEN, caseDetails);
        verify(emailService, times(1)).sendEmail(
            eq(TEMPLATE_NOC_ORG_ADMIN_NOT_REPRESENTING),
            eq(EMAIL_ORG_ADMIN),
            eq(Map.of(
                "case_number", CASE_REFERENCE,
                "claimant", CLAIMANT_NAME,
                "list_of_respondents", RESPONDENT_A_NAME,
                "legalRepName", CLAIMANT_LEGAL_REP_NAME
            ))
        );
        verify(emailService, times(1)).sendEmail(
            eq(TEMPLATE_NOC_LEGAL_REP_NO_LONGER_ASSIGNED),
            eq(EMAIL_LEGAL_REP),
            eq(Map.of(
                "case_number", CASE_REFERENCE,
                "claimant", CLAIMANT_NAME,
                "list_of_respondents", RESPONDENT_A_NAME
            ))
        );
        verify(emailService, times(1)).sendEmail(
            eq(TEMPLATE_NOC_CITIZEN_NO_LONGER_REPRESENTED),
            eq(EMAIL_CLAIMANT),
            eq(Map.of(
                "case_number", CASE_REFERENCE,
                "claimant", CLAIMANT_NAME,
                "list_of_respondents", RESPONDENT_A_NAME,
                "legalRepOrg", ORGANISATION_NAME,
                "linkToCitUI", LINK_CITIZEN_CASE
            ))
        );
        verify(emailService, times(1)).sendEmail(
            eq(TEMPLATE_NOC_OTHER_PARTY_NOT_REPRESENTED),
            eq(EMAIL_RESP_SOLICITOR),
            eq(Map.of(
                "case_number", CASE_REFERENCE,
                "claimant", CLAIMANT_NAME,
                "list_of_respondents", RESPONDENT_A_NAME,
                "party_name", CLAIMANT_NAME,
                "linkToCitUI", LINK_RESP_CITIZEN_CASE
            ))
        );
    }

    @Test
    void shouldRevokeClaimantLegalRep_missingRepresentativeClaimantType() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.getCaseData().setRepresentativeClaimantType(null);

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails, USER_TOKEN)
        );
        assertThat(exception.getMessage()).isEqualTo("Missing RepresentativeClaimantType");
        verify(nocCcdService, times(0))
            .revokeClaimantRepresentation(USER_TOKEN, caseDetails);
    }

    @Test
    void isMoreThanOneRespondent_shouldReturnNo_whenNoRepresentatives() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of());

        String result = nocRemoveRepresentationService.isMoreThanOneRespondent(caseDetails, USER_TOKEN);

        assertThat(result).isEqualTo("No");
    }

    @Test
    void isMoreThanOneRespondent_shouldReturnNo_whenNoOrganisationId() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        RepresentedTypeRItem item = RepresentedTypeRItem.builder()
            .id("1")
            .value(RepresentedTypeR.builder().build())
            .build();
        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of(item));

        String result = nocRemoveRepresentationService.isMoreThanOneRespondent(caseDetails, USER_TOKEN);

        assertThat(result).isEqualTo("No");
    }

    @Test
    void isMoreThanOneRespondent_shouldReturnNo_whenOnlyOneRepresentative() {
        RepresentedTypeRItem item = RepresentedTypeRItem.builder()
            .id("1")
            .value(RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_A_LEGAL_REP_NAME)
                .respondentOrganisation(Organisation.builder()
                    .organisationID(ORGANISATION_ID)
                    .build())
                .build())
            .build();
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.getCaseData().setRepCollection(List.of(item));
        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of(item));

        String result = nocRemoveRepresentationService.isMoreThanOneRespondent(caseDetails, USER_TOKEN);

        assertThat(result).isEqualTo("No");
    }

    @Test
    void isMoreThanOneRespondent_shouldReturnYes_whenMoreThanOneRepresentative() {
        RepresentedTypeRItem item1 = RepresentedTypeRItem.builder()
            .id("1")
            .value(RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_A_LEGAL_REP_NAME)
                .respondentOrganisation(Organisation.builder()
                    .organisationID(ORGANISATION_ID)
                    .build())
                .build())
            .build();
        RepresentedTypeRItem item2 = RepresentedTypeRItem.builder()
            .id("2")
            .value(RepresentedTypeR.builder()
                .nameOfRepresentative(RESPONDENT_B_LEGAL_REP_NAME)
                .respondentOrganisation(Organisation.builder()
                    .organisationID(ORGANISATION_ID)
                    .build())
                .build())
            .build();
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.getCaseData().setRepCollection(List.of(item1, item2));
        when(nocRespondentRepresentativeService.findRepresentativesByToken(anyString(), any()))
            .thenReturn(List.of(item1));

        String result = nocRemoveRepresentationService.isMoreThanOneRespondent(caseDetails, USER_TOKEN);

        assertThat(result).isEqualTo("Yes");
    }
}