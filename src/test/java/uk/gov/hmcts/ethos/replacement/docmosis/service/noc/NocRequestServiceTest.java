package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseAccessService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class NocRequestServiceTest {

    @Mock
    private NocRequestService nocRequestService;
    @InjectMocks
    private NocCcdService nocCcdService;
    @InjectMocks
    private EmailService emailService;
    @InjectMocks
    private CaseAccessService caseAccessService;
    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private static final String USER_TOKEN = "userToken";

    @Test
    void shouldRevokeClaimantLegalRepAndSendNotifications() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
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

        verify(nocCcdService).revokeClaimantRepresentation(USER_TOKEN, caseDetails);
    }
}