package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class ClaimantRepresentativeUtilsTest {

    private static final String CASE_ID = "1234567890123456";
    private static final String CLAIMANT_EMAIL_ADDRESS = "claimant@email.com";
    private static final String CLAIMANT_REPRESENTATIVE_EMAIL_ADDRESS = "claimantrep@email.com";
    private static final String ORGANISATION_ID_1 = "dummy12_organisation34_id56";
    private static final String ORGANISATION_ID_2 = "dummy65_organisation43_id21";

    private static final String EXPECTED_WARNING_CLAIMANT_EMAIL_NOT_FOUND = "Could not find claimant email address.";
    private static final String EXPECTED_WARNING_WITHOUT_CASE_ID = "Claimant email not found for case ";
    private static final String EXPECTED_WARNING_WITH_CASE_ID = "Claimant email not found for case 1234567890123456";

    @BeforeEach
    void setUp() {
        LoggerTestUtils.initializeLogger(ClaimantRepresentativeUtils.class);
    }

    @Test
    void testTheGetClaimantNocNotificationEmail() {
        // when case details is empty should return empty string
        assertThat(ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(null)).isEmpty();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE, EXPECTED_WARNING_WITHOUT_CASE_ID);
        // when case details does not have any case data should return empty string
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        assertThat(ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(caseDetails)).isEmpty();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO, EXPECTED_WARNING_WITH_CASE_ID);
        // when claimant representative is empty and not able to find claimant's email should log not found warning.
        caseDetails.setCaseData(new CaseData());
        assertThat(ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(caseDetails)).isEmpty();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_THREE, EXPECTED_WARNING_CLAIMANT_EMAIL_NOT_FOUND);
        // when claimant representative is not empty and finds claimant e-mail should return that e-mail address
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress(CLAIMANT_EMAIL_ADDRESS);
        caseDetails.getCaseData().setClaimantType(claimantType);
        assertThat(ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(caseDetails))
                .isEqualTo(CLAIMANT_EMAIL_ADDRESS);
        // when claimant representative's email address is empty should return claimant email address
        caseDetails.getCaseData().setRepresentativeClaimantType(RepresentedTypeC.builder().build());
        assertThat(ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(caseDetails))
                .isEqualTo(CLAIMANT_EMAIL_ADDRESS);
        // when claimant representative has email address should return that one
        caseDetails.getCaseData().getRepresentativeClaimantType().setRepresentativeEmailAddress(
                CLAIMANT_REPRESENTATIVE_EMAIL_ADDRESS);
        assertThat(ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(caseDetails))
                .isEqualTo(CLAIMANT_REPRESENTATIVE_EMAIL_ADDRESS);
    }

    @Test
    void theIsClaimantOrganisationLinkedToRespondents() {
        // when claimant representative is empty should return false
        List<String> respondentRepresentativeOrganisationIds = List.of(StringUtils.EMPTY);
        assertThat(ClaimantRepresentativeUtils.isClaimantOrganisationLinkedToRespondents(
                null, respondentRepresentativeOrganisationIds)).isFalse();
        // when respondent representative organisation ids is empty should return false
        RepresentedTypeC claimantRepresentative =  RepresentedTypeC.builder().build();
        assertThat(ClaimantRepresentativeUtils.isClaimantOrganisationLinkedToRespondents(
                claimantRepresentative, null)).isFalse();
        // when both claimant organisation id and my hmcts organisation Ids are empty should return false
        assertThat(ClaimantRepresentativeUtils.isClaimantOrganisationLinkedToRespondents(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isFalse();
        Organisation organisation = Organisation.builder().build();
        claimantRepresentative.setMyHmctsOrganisation(organisation);
        assertThat(ClaimantRepresentativeUtils.isClaimantOrganisationLinkedToRespondents(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isFalse();
        // when respondent representative organisation id is empty should return false
        organisation.setOrganisationID(ORGANISATION_ID_2);
        assertThat(ClaimantRepresentativeUtils.isClaimantOrganisationLinkedToRespondents(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isFalse();
        // when respondent representative's organisation id is not equal to representative's both my Hmcts Organisation
        // id and organisation id should return false
        respondentRepresentativeOrganisationIds = List.of(ORGANISATION_ID_1);
        assertThat(ClaimantRepresentativeUtils.isClaimantOrganisationLinkedToRespondents(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isFalse();
        // when respondent representative's organisation id is equal to claimant's representative's my hmcts
        // organisation id should return true
        claimantRepresentative.getMyHmctsOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(ClaimantRepresentativeUtils.isClaimantOrganisationLinkedToRespondents(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isTrue();
        // when respondent representative's organisation id is equal to claimant's representative's organisation id
        // should return true
        claimantRepresentative.getMyHmctsOrganisation().setOrganisationID(StringUtils.EMPTY);
        claimantRepresentative.setOrganisationId(ORGANISATION_ID_1);
        assertThat(ClaimantRepresentativeUtils.isClaimantOrganisationLinkedToRespondents(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isTrue();
    }

    @Test
    void theHasOrganisationIdentifier() {
        // when representative is empty should return false
        assertThat(ClaimantRepresentativeUtils.hasOrganisationIdentifier(null)).isFalse();
        // when both organisation id and my hmcts organisation id are empty should return false
        RepresentedTypeC claimantRepresentative =  RepresentedTypeC.builder().build();
        assertThat(ClaimantRepresentativeUtils.hasOrganisationIdentifier(claimantRepresentative)).isFalse();
        // when my hmcts organisation id is empty and organisation id is not empty should return true
        claimantRepresentative.setOrganisationId(ORGANISATION_ID_1);
        assertThat(ClaimantRepresentativeUtils.hasOrganisationIdentifier(claimantRepresentative)).isTrue();
        // when both my hmcts organisation id and organisation id are empty should return true
        claimantRepresentative.setOrganisationId(StringUtils.EMPTY);
        Organisation organisation = Organisation.builder().build();
        claimantRepresentative.setMyHmctsOrganisation(organisation);
        assertThat(ClaimantRepresentativeUtils.hasOrganisationIdentifier(claimantRepresentative)).isFalse();
        // when my hmcts organisation id is not empty and organisation id is empty should return true
        organisation.setOrganisationID(ORGANISATION_ID_1);
        assertThat(ClaimantRepresentativeUtils.hasOrganisationIdentifier(claimantRepresentative)).isTrue();
    }
}
