package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT;

@ExtendWith(SpringExtension.class)
class NocRemoveRepresentationServiceTest {

    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private NocNotificationService nocNotificationService;
    @Mock
    private NocRemoveRepresentationEmailService nocRemoveRepresentationEmailService;
    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private NocRemoveRepresentationService nocRemoveRepresentationService;

    private static final String ADMIN_TOKEN = "adminToken";

    private static final String ORG_CLAIMANT_NAME = "Org C";
    private static final String ORG_CLAIMANT_EMAIL = "org.c@test.com";
    private static final String REP_CLAIMANT_NAME = "Legal Rep C";
    private static final String REP_CLAIMANT_EMAIL = "rep.c@test.com";
    private static final String CLAIMANT_NAME = "Chris Claimant";

    private CaseDetails caseDetails;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        caseDetails = generateCaseDetails();
    }

    private CaseDetails generateCaseDetails() throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(Thread.currentThread()
                .getContextClassLoader().getResource("nocRemoveRepTest.json")).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }

    @Test
    @SneakyThrows
    void shouldRevokeClaimantLegalRep_happyPath() {
        when(nocNotificationService.findClaimantRepOrgSuperUserEmail(any()))
                .thenReturn(ORG_CLAIMANT_EMAIL);
        when(adminUserService.getAdminUserToken())
                .thenReturn(ADMIN_TOKEN);

        nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails);

        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ONE))
                .revokeClaimantRepresentation(ADMIN_TOKEN, caseDetails);
        // send email to organisation admin
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE)).sendEmailToOrgAdmin(
                any(CaseDetails.class),
                eq(ORG_CLAIMANT_EMAIL),
                eq(REP_CLAIMANT_NAME),
                eq(EMAIL_TYPE_TO_ORG_ADMIN_NO_REP_LEFT)
        );
        // send email to removed legal rep
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmailToRemovedLegalRep(any(CaseDetails.class), eq(REP_CLAIMANT_EMAIL));
        // send email to unrepresented party
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmailToUnrepresentedClaimant(any(CaseDetails.class), eq(ORG_CLAIMANT_NAME));
        // send email to other party
        verify(nocRemoveRepresentationEmailService, times(LoggerTestUtils.INTEGER_ONE))
                .sendEmailToOtherRespondents(any(CaseDetails.class), eq(List.of()), eq(CLAIMANT_NAME));
    }

    @Test
    void shouldRevokeClaimantLegalRep_missingRepresentativeClaimantType() {
        caseDetails.getCaseData().setRepresentativeClaimantType(null);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails)
        );
        assertThat(exception.getMessage())
                .isEqualTo("Representative not found for case ID 1775651960650043.");
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ZERO))
                .revokeClaimantRepresentation(anyString(), any());
    }
}