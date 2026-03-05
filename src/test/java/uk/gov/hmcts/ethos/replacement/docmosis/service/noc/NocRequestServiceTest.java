package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    @Test
    void shouldRevokeClaimantLegalRepAndSendNotifications() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("123456789/1234")
            .withClaimant("Claimant Name")
            .withRespondent(RespondentSumType.builder()
                .respondentName("Jane Doe")
                .build())
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        RepresentedTypeC rep = RepresentedTypeC.builder()
            .representativeId("repId")
            .nameOfRepresentative("John Doe")
            .nameOfOrganisation("Org Ltd")
            .representativeEmailAddress("rep@example.com")
            .myHmctsOrganisation(Organisation.builder()
                .organisationID("orgId")
                .build())
            .build();
        caseDetails.getCaseData().setRepresentativeClaimantType(rep);

        nocRequestService.revokeClaimantLegalRep(caseDetails, USER_TOKEN);

        verify(nocCcdService, times(1)).revokeClaimantRepresentation(anyString(), any());
    }
}