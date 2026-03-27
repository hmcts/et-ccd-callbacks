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
    private static final String CLAIMANT_REPRESENTATIVE_NAME = "claimantRepresentativeName";

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

    @Test
    void theHasRepresentative() {
        // when claimant representative is empty should return false
        assertThat(ClaimantRepresentativeUtils.hasRepresentative(null)).isFalse();
        // when claimant representative is not empty should return true
        assertThat(ClaimantRepresentativeUtils.hasRepresentative(RepresentedTypeC.builder().build())).isTrue();
    }

    @Test
    void theHasHmctsOrganisationId() {
        // when claimant representative's hmcts organisation is empty should return false
        assertThat(ClaimantRepresentativeUtils.hasHmctsOrganisationId(RepresentedTypeC.builder()
                .organisationId(StringUtils.EMPTY).build())).isFalse();
        // when claimant representative's hmcts organisation id is empty should return false
        assertThat(ClaimantRepresentativeUtils.hasHmctsOrganisationId(RepresentedTypeC.builder()
                .myHmctsOrganisation(Organisation.builder().organisationID(StringUtils.EMPTY).build()).build()))
                .isFalse();
        // when claimant representative's hmcts organisation id is not empty should return true
        assertThat(ClaimantRepresentativeUtils.hasHmctsOrganisationId(RepresentedTypeC.builder()
                .myHmctsOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_1).build()).build()))
                .isTrue();
    }

    @Test
    void theGetHmctsOrganisationIdOrEmpty() {
        // when claimant representative's hmcts organisation is empty should return empty string
        assertThat(ClaimantRepresentativeUtils.getHmctsOrganisationIdOrEmpty(RepresentedTypeC.builder()
                .organisationId(StringUtils.EMPTY).build())).isEmpty();
        // when claimant representative's hmcts organisation id is not empty should return organisation id
        assertThat(ClaimantRepresentativeUtils.getHmctsOrganisationIdOrEmpty(RepresentedTypeC.builder()
                .myHmctsOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_1).build()).build()))
                .isEqualTo(ORGANISATION_ID_1);
    }

    @Test
    void theHasRepresentativeEmail() {
        // when claimant representative's email address is empty should return false
        assertThat(ClaimantRepresentativeUtils.hasRepresentativeEmail(RepresentedTypeC.builder()
                .representativeEmailAddress(StringUtils.EMPTY).build())).isFalse();
        // when claimant representative's email address is not empty should return true
        assertThat(ClaimantRepresentativeUtils.hasRepresentativeEmail(RepresentedTypeC.builder()
                .representativeEmailAddress(CLAIMANT_REPRESENTATIVE_EMAIL_ADDRESS).build())).isTrue();
    }

    @Test
    void theAddAmendClaimantRepresentative() {
        // when claimant representative empty should not assign any claimant representative
        CaseData caseData = new CaseData();
        ClaimantRepresentativeUtils.addAmendClaimantRepresentative(caseData);
        assertThat(caseData.getRepresentativeClaimantType()).isNull();
        assertThat(caseData.getClaimantRepresentativeRemoved()).isNull();
        assertThat(caseData.getClaimantRepresentativeOrganisationPolicy().getOrganisation()).isNull();
        // when claimant represented question is no and there exists claimant representative should set claimant
        // representative removed Yes
        caseData.setClaimantRepresentedQuestion(NO);
        caseData.setRepresentativeClaimantType(RepresentedTypeC.builder().build());
        ClaimantRepresentativeUtils.addAmendClaimantRepresentative(caseData);
        assertThat(caseData.getRepresentativeClaimantType()).isNull();
        assertThat(caseData.getClaimantRepresentativeRemoved()).isEqualTo(YES);
        assertThat(caseData.getClaimantRepresentativeOrganisationPolicy().getOrganisation()).isNull();
        // when claimant representative exists and claimant represented question is Yes should assign representative
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder()
                .nameOfRepresentative(CLAIMANT_REPRESENTATIVE_NAME).build();
        caseData.setRepresentativeClaimantType(claimantRepresentative);
        caseData.setClaimantRepresentedQuestion(YES);
        ClaimantRepresentativeUtils.addAmendClaimantRepresentative(caseData);
        assertThat(caseData.getClaimantRepresentativeRemoved()).isEqualTo(NO);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeId()).isNotNull();
        assertThat(caseData.getRepresentativeClaimantType().getOrganisationId()).isNotNull();
        assertThat(caseData.getClaimantRepresentativeOrganisationPolicy().getOrganisation()).isNotNull();
        assertThat(caseData.getClaimantRepresentativeOrganisationPolicy().getOrganisation().getOrganisationID())
                .isEqualTo(caseData.getRepresentativeClaimantType().getOrganisationId());
        // when claimant representative exists and already has representative and organisation ids should not change
        // those ids
        claimantRepresentative.setRepresentativeId(CLAIMANT_REPRESENTATIVE_ID);
        claimantRepresentative.setOrganisationId(ORGANISATION_ID_1);
        ClaimantRepresentativeUtils.addAmendClaimantRepresentative(caseData);
        assertThat(caseData.getClaimantRepresentativeRemoved()).isEqualTo(NO);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeId()).isNotNull()
                .isEqualTo(CLAIMANT_REPRESENTATIVE_ID);
        assertThat(caseData.getRepresentativeClaimantType().getOrganisationId()).isNotNull()
                .isEqualTo(ORGANISATION_ID_1);
        assertThat(caseData.getClaimantRepresentativeOrganisationPolicy().getOrganisation()).isNotNull();
        assertThat(caseData.getClaimantRepresentativeOrganisationPolicy().getOrganisation().getOrganisationID())
                .isEqualTo(ORGANISATION_ID_1);
        // when claimant representative exists and has my hmcts organisation with id should set organisation id to
        // my hmcts organisation id
        claimantRepresentative.setMyHmctsOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_2).build());
        ClaimantRepresentativeUtils.addAmendClaimantRepresentative(caseData);
        assertThat(caseData.getClaimantRepresentativeRemoved()).isEqualTo(NO);
        assertThat(caseData.getRepresentativeClaimantType().getRepresentativeId()).isNotNull()
                .isEqualTo(CLAIMANT_REPRESENTATIVE_ID);
        assertThat(caseData.getRepresentativeClaimantType().getOrganisationId()).isNotNull()
                .isEqualTo(ORGANISATION_ID_2);
        assertThat(caseData.getClaimantRepresentativeOrganisationPolicy().getOrganisation()).isNotNull();
        assertThat(caseData.getClaimantRepresentativeOrganisationPolicy().getOrganisation().getOrganisationID())
                .isEqualTo(ORGANISATION_ID_2);
    }
}
