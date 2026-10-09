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
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_REMOVE_OPTION_YOURSELF;

@ExtendWith(SpringExtension.class)
class NocRemoveRepresentationServiceTest {

    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private NocNotificationService nocNotificationService;
    private UserIdamService userIdamService;

    @InjectMocks
    private NocRemoveRepresentationService nocRemoveRepresentationService;

    private static final String CLAIMANT_REPRESENTATIVE_EMAIL_VALID = "rep.c@test.com";
    private static final String CLAIMANT_REPRESENTATIVE_EMAIL_INVALID = "rep.r@test.com";
    private static final String CLAIMANT_REPRESENTATIVE_ID = "claimantRepresentativeId";
    private static final String CASE_ID = "1775651960650043";
    private static final String DUMMY_USER_TOKEN = "dummyUserToken";
    private static final String ORGANISATION_ID = "organisationId";

    private static final String EXPECTED_EXCEPTION_INVALID_CASE_DETAILS = "Case details are required";
    private static final String EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND =
            "Representative not found for case ID " + CASE_ID + ".";

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
        UserDetails userDetails = new UserDetails();
        userDetails.setEmail(CLAIMANT_REPRESENTATIVE_EMAIL_VALID);
        userDetails.setUid(UUID.randomUUID().toString());
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> nocRemoveRepresentationService.setNocRemoveOption(userDetails, null));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_INVALID_CASE_DETAILS);
        // when representative is lead representative, should set nocRemoveOption to organisation
        nocRemoveRepresentationService.setNocRemoveOption(userDetails, caseDetails);
        assertThat(caseDetails.getCaseData().getNocRemoveOption()).isEqualTo(NOC_REMOVE_OPTION_ORGANISATION);
        // when representative is not lead representative, should set nocRemoveOption to yourself
        userDetails.setEmail(CLAIMANT_REPRESENTATIVE_EMAIL_INVALID);
        nocRemoveRepresentationService.setNocRemoveOption(userDetails, caseDetails);
        assertThat(caseDetails.getCaseData().getNocRemoveOption()).isEqualTo(NOC_REMOVE_OPTION_YOURSELF);
    }

    @Test
    @SneakyThrows
    void theRevokeClaimantLegalRep() {
        // when there is no claimant representative should throw representative not found exception
        CaseDetails tmpCaseDetails = new CaseDetails();
        tmpCaseDetails.setCaseData(new CaseData());
        tmpCaseDetails.setCaseId(CASE_ID);
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> nocRemoveRepresentationService.revokeClaimantLegalRep(DUMMY_USER_TOKEN, tmpCaseDetails));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND);
        // when claimant representative exists, but noc remove option is not organisation should revoke only
        // representation
        tmpCaseDetails.getCaseData().setRepresentativeClaimantType(RepresentedTypeC.builder()
                .representativeId(CLAIMANT_REPRESENTATIVE_ID).build());
        tmpCaseDetails.getCaseData().setClaimantRepresentativeRemoved(NO);
        tmpCaseDetails.getCaseData().setClaimantRepresentedQuestion(YES);
        tmpCaseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_YOURSELF);
        tmpCaseDetails.getCaseData().setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())
                .organisation(Organisation.builder().organisationID(ORGANISATION_ID).build()).build());
        nocRemoveRepresentationService.revokeClaimantLegalRep(DUMMY_USER_TOKEN, tmpCaseDetails);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_ONE))
                .revokeClaimantRepresentation(DUMMY_USER_TOKEN, tmpCaseDetails);
        assertThat(tmpCaseDetails.getCaseData().getRepresentativeClaimantType()).isEqualTo(RepresentedTypeC.builder()
                .representativeId(CLAIMANT_REPRESENTATIVE_ID).build());
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentativeRemoved()).isEqualTo(NO);
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentedQuestion()).isEqualTo(YES);
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentativeOrganisationPolicy()).isEqualTo(
                OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                        ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).organisation(
                                Organisation.builder().organisationID(ORGANISATION_ID).build()).build());
        // when claimant representative exists, and noc remove option is organisation should revoke representation and
        // mark claimant as unrepresented
        tmpCaseDetails.getCaseData().setNocRemoveOption(NOC_REMOVE_OPTION_ORGANISATION);
        nocRemoveRepresentationService.revokeClaimantLegalRep(DUMMY_USER_TOKEN, tmpCaseDetails);
        verify(nocCcdService, times(LoggerTestUtils.INTEGER_TWO))
                .revokeClaimantRepresentation(DUMMY_USER_TOKEN, tmpCaseDetails);
        assertThat(tmpCaseDetails.getCaseData().getRepresentativeClaimantType()).isNull();
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentativeRemoved()).isEqualTo(YES);
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentedQuestion()).isEqualTo(NO);
        assertThat(tmpCaseDetails.getCaseData().getClaimantRepresentativeOrganisationPolicy()).isEqualTo(
                OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                        ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).build());
    }
}