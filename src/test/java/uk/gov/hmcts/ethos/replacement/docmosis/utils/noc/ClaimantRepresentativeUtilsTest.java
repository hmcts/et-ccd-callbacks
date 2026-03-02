package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import ch.qos.logback.classic.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

final class ClaimantRepresentativeUtilsTest {

    private static final String CASE_ID = "1234567890123456";
    private static final String CLAIMANT_EMAIL_ADDRESS = "claimant@email.com";
    private static final String CLAIMANT_REPRESENTATIVE_EMAIL_ADDRESS = "claimantrep@email.com";
    private static final String ORGANISATION_ID_1 = "dummy12_organisation34_id56";
    private static final String ORGANISATION_ID_2 = "dummy65_organisation43_id21";
    private static final String RESPONDENT_REPRESENTATIVE_ID = "respondentRepresentativeId";
    private static final String CLAIMANT_REPRESENTATIVE_ID = "claimantRepresentativeId";

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
    void theIsClaimantRepresentativeEmailMatchedWithRespondents() {
        // when claimant representative is empty should return false
        CaseData caseData = new CaseData();
        caseData.setRepCollection(new ArrayList<>());
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeEmailMatchedWithRespondents(caseData)).isFalse();
        // when claimant representative's email address is empty should return false
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder().build());
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeEmailMatchedWithRespondents(caseData)).isFalse();
        // when claimant representative's email address is not equal to any of the respondent representative's email
        // address should return false
        caseData.getRepresentativeClaimantType().setRepresentativeEmailAddress(CLAIMANT_REPRESENTATIVE_EMAIL_ADDRESS);
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeEmailMatchedWithRespondents(caseData)).isFalse();
        // when respondent representative's email address is equal to claimant representative's email address
        // should return true
        RepresentedTypeRItem respondentRepresentative = RepresentedTypeRItem.builder().id(RESPONDENT_REPRESENTATIVE_ID)
                .value(RepresentedTypeR.builder().representativeEmailAddress(CLAIMANT_REPRESENTATIVE_EMAIL_ADDRESS)
                        .build()).build();
        caseData.getRepCollection().add(respondentRepresentative);
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeEmailMatchedWithRespondents(caseData)).isTrue();
    }

    @Test
    void theMarkClaimantAsUnrepresented() {
        // should remove all claimant representative details and not throw any exception
        CaseData caseData = new CaseData();
        caseData.setClaimantRepresentativeOrganisationPolicy(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).build());
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder().representativeId(CLAIMANT_REPRESENTATIVE_ID)
                .myHmctsOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_1).build()).build());
        caseData.setClaimantRepresentativeRemoved(NO);
        caseData.setClaimantRepresentedQuestion(YES);
        ClaimantRepresentativeUtils.markClaimantAsUnrepresented(caseData);
        assertThat(caseData.getRepresentativeClaimantType()).isNull();
        assertThat(caseData.getClaimantRepresentativeRemoved()).isEqualTo(YES);
        assertThat(caseData.getClaimantRepresentedQuestion()).isEqualTo(NO);
        assertThat(caseData.getClaimantRepresentativeOrganisationPolicy()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).build());

    }
}
