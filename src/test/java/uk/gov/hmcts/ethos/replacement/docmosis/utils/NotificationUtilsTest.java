package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class NotificationUtilsTest {

    private static final String CASE_ID = "1234567890123456";
    private static final String ETHOS_CASE_REFERENCE = "6000001/2026";
    private static final String CLAIMANT_NAME = "Claimant Name";
    private static final String RESPONDENT_ID = "Respondent ID";
    private static final String RESPONDENT_NAME = "Respondent Name";
    private static final String RESPONDENT_EMAIL = "respondent@hmcts.org";
    private static final String REPRESENTATIVE_ID = "Representative ID";
    private static final String ORGANISATION_ID = "Organisation ID";

    @BeforeEach
    void setUp() {
        LoggerTestUtils.initializeLogger(NotificationUtils.class);
    }

    @Test
    void theIsCaseDetailsValidForNotification() {
        // when case details is empty should return false
        assertThat(NotificationUtils.isCaseValidForNotification(null)).isFalse();
        // when case details not have case id should return false
        CaseDetails caseDetails = new CaseDetails();
        assertThat(NotificationUtils.isCaseValidForNotification(caseDetails)).isFalse();
        // when case details not have case data should return false
        caseDetails.setCaseId(CASE_ID);
        assertThat(NotificationUtils.isCaseValidForNotification(caseDetails)).isFalse();
        // when case data not has ethos case reference should return false
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        assertThat(NotificationUtils.isCaseValidForNotification(caseDetails)).isFalse();
        // when case data not has claimant should return false
        caseData.setEthosCaseReference(ETHOS_CASE_REFERENCE);
        assertThat(NotificationUtils.isCaseValidForNotification(caseDetails)).isFalse();
        // when case data not has respondent should return false
        caseData.setClaimant(CLAIMANT_NAME);
        assertThat(NotificationUtils.isCaseValidForNotification(caseDetails)).isFalse();
        // when case data has respondent should return true
        RespondentSumType respondentSumType = new RespondentSumType();
        respondentSumType.setRespondentName(RESPONDENT_NAME);
        respondentSumType.setRespondentEmail(RESPONDENT_EMAIL);
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setId(RESPONDENT_ID);
        respondent.setValue(respondentSumType);
        caseData.setRespondentCollection(List.of(respondent));
        assertThat(NotificationUtils.isCaseValidForNotification(caseDetails)).isTrue();
    }

    @Test
    void theCanNotifyRespondentRepresentativeOrganisation() {
        // when representative is empty should return false
        assertThat(NotificationUtils.canNotifyRespondentRepresentativeOrganisation(null)).isFalse();
        // when representative does not have id should return false
        RepresentedTypeRItem representative = new RepresentedTypeRItem();
        assertThat(NotificationUtils.canNotifyRespondentRepresentativeOrganisation(representative)).isFalse();
        // when representative does not have value should return false
        representative.setId(REPRESENTATIVE_ID);
        assertThat(NotificationUtils.canNotifyRespondentRepresentativeOrganisation(representative)).isFalse();
        // when representative does not have organisation should return false
        representative.setValue(RepresentedTypeR.builder().build());
        assertThat(NotificationUtils.canNotifyRespondentRepresentativeOrganisation(representative)).isFalse();
        // when representative does not have organisation id should return false
        representative.getValue().setRespondentOrganisation(Organisation.builder().build());
        assertThat(NotificationUtils.canNotifyRespondentRepresentativeOrganisation(representative)).isFalse();
        // when representative is valid should return true
        representative.getValue().getRespondentOrganisation().setOrganisationID(ORGANISATION_ID);
        assertThat(NotificationUtils.canNotifyRespondentRepresentativeOrganisation(representative)).isTrue();
    }

    @Test
    void theFindClaimantRepresentativeOrganisationId() {
        // when claimant representative is empty should return empty string
        assertThat(NotificationUtils.findClaimantRepresentativeOrganisationId(null)).isEmpty();
        // when claimant representative does not have organisation should return empty string
        RepresentedTypeC claimantRepresentative = RepresentedTypeC.builder().build();
        assertThat(NotificationUtils.findClaimantRepresentativeOrganisationId(claimantRepresentative)).isEmpty();
        // when claimant representative does not have organisation id should return empty string
        claimantRepresentative.setMyHmctsOrganisation(Organisation.builder().build());
        assertThat(NotificationUtils.findClaimantRepresentativeOrganisationId(claimantRepresentative)).isEmpty();
        // when claimant representative has organisation id should return organisation id
        claimantRepresentative.getMyHmctsOrganisation().setOrganisationID(ORGANISATION_ID);
        assertThat(NotificationUtils.findClaimantRepresentativeOrganisationId(claimantRepresentative))
                .isEqualTo(ORGANISATION_ID);
    }
}
