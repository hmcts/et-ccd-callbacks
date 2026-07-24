package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

class RepresentedClaimantEmailServiceTest {

    private static final String OLD_EMAIL = "old@example.com";
    private static final String NEW_EMAIL = "new@example.com";

    private RepresentedClaimantEmailService service;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        service = new RepresentedClaimantEmailService();
        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress(OLD_EMAIL);
        caseData = new CaseData();
        caseData.setClaimantType(claimantType);
        caseData.setClaimantRepresentedQuestion(YES);
        caseData.setCurrentClaimantEmail(OLD_EMAIL);
        caseData.setNewClaimantEmail(NEW_EMAIL);
        caseData.setClaimantId("existing-claimant-id");
    }

    @Test
    void initialisePrepopulatesCurrentEmailAndClearsNewEmail() {
        assertThat(service.initialise(caseData)).isEmpty();
        assertThat(caseData.getCurrentClaimantEmail()).isEqualTo(OLD_EMAIL);
        assertThat(caseData.getNewClaimantEmail()).isNull();
    }

    @Test
    void initialiseRejectsUnrepresentedClaimant() {
        caseData.setClaimantRepresentedQuestion(NO);
        caseData.setNewClaimantEmail(NEW_EMAIL);

        assertThat(service.initialise(caseData))
                .containsExactly(RepresentedClaimantEmailService.CLAIMANT_NOT_REPRESENTED_ERROR);
        assertThat(caseData.getNewClaimantEmail()).isEqualTo(NEW_EMAIL);
    }

    @Test
    void initialiseRejectsWhenRepresentationFlagUnset() {
        caseData.setClaimantRepresentedQuestion(null);

        assertThat(service.initialise(caseData))
                .containsExactly(RepresentedClaimantEmailService.CLAIMANT_NOT_REPRESENTED_ERROR);
    }

    @Test
    void validateRejectsInvalidEmail() {
        caseData.setNewClaimantEmail("not-an-email");

        assertThat(service.validateNewEmail(caseData))
                .containsExactly("The email address entered is invalid.");
    }

    @Test
    void validateRejectsUnchangedEmailIgnoringCase() {
        caseData.setNewClaimantEmail(OLD_EMAIL.toUpperCase(Locale.ROOT));

        assertThat(service.validateNewEmail(caseData))
                .containsExactly(RepresentedClaimantEmailService.EMAIL_UNCHANGED_ERROR);
    }

    @Test
    void validateRejectsUnrepresentedWithoutCheckingEmail() {
        caseData.setClaimantRepresentedQuestion(NO);
        caseData.setNewClaimantEmail("not-an-email");

        assertThat(service.validateNewEmail(caseData))
                .containsExactly(RepresentedClaimantEmailService.CLAIMANT_NOT_REPRESENTED_ERROR);
    }

    @Test
    void prepareUpdateAppliesContactEmailWithoutChangingClaimantId() {
        assertThat(service.prepareUpdate(caseData)).isEmpty();

        assertThat(caseData.getClaimantType().getClaimantEmailAddress()).isEqualTo(NEW_EMAIL);
        assertThat(caseData.getClaimantId()).isEqualTo("existing-claimant-id");
        assertThat(caseData.getCurrentClaimantEmail()).isNull();
        assertThat(caseData.getNewClaimantEmail()).isNull();
    }

    @Test
    void prepareUpdateCreatesMissingClaimantType() {
        caseData.setClaimantType(null);

        assertThat(service.prepareUpdate(caseData)).isEmpty();
        assertThat(caseData.getClaimantType()).isNotNull();
        assertThat(caseData.getClaimantType().getClaimantEmailAddress()).isEqualTo(NEW_EMAIL);
        assertThat(caseData.getClaimantId()).isEqualTo("existing-claimant-id");
    }

    @Test
    void prepareUpdateReturnsErrorsWithoutChangingCaseData() {
        caseData.setNewClaimantEmail(OLD_EMAIL);

        assertThat(service.prepareUpdate(caseData))
                .containsExactly(RepresentedClaimantEmailService.EMAIL_UNCHANGED_ERROR);
        assertThat(caseData.getClaimantType().getClaimantEmailAddress()).isEqualTo(OLD_EMAIL);
        assertThat(caseData.getClaimantId()).isEqualTo("existing-claimant-id");
    }
}
