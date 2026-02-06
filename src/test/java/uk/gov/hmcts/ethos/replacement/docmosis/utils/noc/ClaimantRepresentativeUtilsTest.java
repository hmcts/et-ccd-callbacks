package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClaimantRepresentativeUtilsTest {

    private ListAppender<ILoggingEvent> appender;
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
        Logger logger = (Logger) LoggerFactory.getLogger(ClaimantRepresentativeUtils.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @Test
    void testTheGetClaimantNocNotificationEmail() {
        // when case details is empty should return empty string
        assertThat(ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(null)).isEmpty();
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.WARN)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains(EXPECTED_WARNING_WITHOUT_CASE_ID);
        // when case details does not have any case data should return empty string
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(CASE_ID);
        assertThat(ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(caseDetails)).isEmpty();
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.WARN)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains(EXPECTED_WARNING_WITH_CASE_ID);
        // when claimant representative is empty and not able to find claimant's email should log not found warning.
        caseDetails.setCaseData(new CaseData());
        assertThat(ClaimantRepresentativeUtils.getClaimantNocNotificationEmail(caseDetails)).isEmpty();
        assertThat(appender.list)
                .filteredOn(e -> e.getLevel() == Level.WARN)
                .extracting(ILoggingEvent::getFormattedMessage)
                .contains(EXPECTED_WARNING_CLAIMANT_EMAIL_NOT_FOUND);
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
    void theIsClaimantRepresentativeOrganisationInRespondentOrganisations() {
        // when claimant representative is empty should return false
        List<String> respondentRepresentativeOrganisationIds = List.of(StringUtils.EMPTY);
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeOrganisationInRespondentOrganisations(
                null, respondentRepresentativeOrganisationIds)).isFalse();
        // when respondent representative organisation ids is empty should return false
        RepresentedTypeC claimantRepresentative =  RepresentedTypeC.builder().build();
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeOrganisationInRespondentOrganisations(
                claimantRepresentative, null)).isFalse();
        // when both claimant organisation id and myHmctsOrganisation Ids are empty should return false
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeOrganisationInRespondentOrganisations(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isFalse();
        Organisation organisation = Organisation.builder().build();
        claimantRepresentative.setMyHmctsOrganisation(organisation);
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeOrganisationInRespondentOrganisations(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isFalse();
        // when respondent representative organisation id is empty should return false
        organisation.setOrganisationID(ORGANISATION_ID_2);
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeOrganisationInRespondentOrganisations(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isFalse();
        // when respondent representative's organisation id is not equal to representative's both myHmctsOrganisation Id
        // and organisation id should return false
        respondentRepresentativeOrganisationIds = List.of(ORGANISATION_ID_1);
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeOrganisationInRespondentOrganisations(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isFalse();
        // when respondent representative's organisation id is equal to claimant's representative' s my hmcts
        // organisation id should return true
        claimantRepresentative.getMyHmctsOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeOrganisationInRespondentOrganisations(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isTrue();
        // when respondent representative's organisation id is equal to claimant's representative's organisation id
        // should return true
        claimantRepresentative.getMyHmctsOrganisation().setOrganisationID(StringUtils.EMPTY);
        claimantRepresentative.setOrganisationId(ORGANISATION_ID_1);
        assertThat(ClaimantRepresentativeUtils.isClaimantRepresentativeOrganisationInRespondentOrganisations(
                claimantRepresentative, respondentRepresentativeOrganisationIds)).isTrue();
    }
}
