package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;

@ExtendWith(SpringExtension.class)
class NocRemoveRepresentationServiceTest {

    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private NocNotificationService nocNotificationService;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private UserIdamService userIdamService;

    @InjectMocks
    private NocRemoveRepresentationService nocRemoveRepresentationService;

    private static final String ADMIN_TOKEN = "adminToken";

    private static final String ORG_CLAIMANT_EMAIL = "org.c@test.com";
    private static final String DUMMY_USER_TOKEN = "dummyUserToken";

    private static final String EXPECTED_EXCEPTION_INVALID_CASE_DETAILS = "Case details are required";
    private static final String CLAIMANT_REPRESENTATIVE_EMAIL_VALID = "rep.c@test.com";
    private static final String CLAIMANT_REPRESENTATIVE_EMAIL_INVALID = "rep.r@test.com";

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
    void theSetNocRemoveOption() {
        // when case details are not valid should throw generic service exception
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> nocRemoveRepresentationService.setNocRemoveOption(DUMMY_USER_TOKEN, null));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_INVALID_CASE_DETAILS);
        // when representative is lead representative, should set nocRemoveOption to organisation
        UserDetails userDetails = new UserDetails();
        userDetails.setEmail(CLAIMANT_REPRESENTATIVE_EMAIL_VALID);
        userDetails.setUid(UUID.randomUUID().toString());
        when(userIdamService.getUserDetails(DUMMY_USER_TOKEN)).thenReturn(userDetails);
        nocRemoveRepresentationService.setNocRemoveOption(DUMMY_USER_TOKEN, caseDetails);
        assertThat(caseDetails.getCaseData().getNocRemoveOption()).isEqualTo(NOC_REMOVE_OPTION_ORGANISATION);
        // when representative is not lead representative, should set nocRemoveOption to yourself
        userDetails.setEmail(CLAIMANT_REPRESENTATIVE_EMAIL_INVALID);
        when(userIdamService.getUserDetails(DUMMY_USER_TOKEN)).thenReturn(userDetails);
        nocRemoveRepresentationService.setNocRemoveOption(DUMMY_USER_TOKEN, caseDetails);
        assertThat(caseDetails.getCaseData().getNocRemoveOption()).isEqualTo(NOC_REMOVE_OPTION_YOURSELF);
    }

    @Test
    @SneakyThrows
    void shouldRevokeClaimantLegalRep_happyPath() {
        when(nocNotificationService.findClaimantRepOrgSuperUserEmail(any())).thenReturn(ORG_CLAIMANT_EMAIL);
        when(adminUserService.getAdminUserToken()).thenReturn(ADMIN_TOKEN);
        nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ONE))
                .revokeClaimantRepresentation(ADMIN_TOKEN, caseDetails);
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