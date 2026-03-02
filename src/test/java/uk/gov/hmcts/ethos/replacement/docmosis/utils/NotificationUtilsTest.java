package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import ch.qos.logback.classic.Level;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NOC_TYPE_REMOVAL;

final class NotificationUtilsTest {

    private static final String CASE_ID = "1234567890123456";
    private static final String ETHOS_CASE_REFERENCE = "6000001/2026";
    private static final String CLAIMANT_NAME = "Claimant Name";
    private static final String RESPONDENT_ID = "Respondent ID";
    private static final String RESPONDENT_NAME = "Respondent Name";
    private static final String RESPONDENT_EMAIL = "respondent@hmcts.org";
    private static final String REPRESENTATIVE_ID = "Representative ID";
    private static final String ORGANISATION_ID = "Organisation ID";
    private static final String ORGANISATION_EMAIL = "organisation@hmcts.org";

    private static final String EXPECTED_WARNING_INVALID_PARAMETERS_TO_RESOLVE_ORGANISATION_EMAIL_WITHOUT_CASE_ID =
            "Invalid parameters(orgId, caseId, nocType). Unable to resolve organisation's superuser email Case id: ";
    private static final String EXPECTED_WARNING_INVALID_PARAMETERS_TO_RESOLVE_ORGANISATION_EMAIL_WITH_CASE_ID =
            "Invalid parameters(orgId, caseId, nocType). Unable to resolve organisation's superuser email "
                    + "Case id: " + CASE_ID;
    private static final String EXPECTED_WARNING_INVALID_ORGANISATION_RESPONSE_TO_RESOLVE_ORGANISATION_EMAIL_1 =
            "Cannot retrieve old organisation by id Organisation ID [null] null. Unable to resolve organisation's "
                    + "superuser email. Case id: " + CASE_ID;
    private static final String EXPECTED_WARNING_INVALID_ORGANISATION_RESPONSE_TO_RESOLVE_ORGANISATION_EMAIL_2 =
            "Cannot retrieve old organisation by id " + ORGANISATION_ID + " [400 BAD_REQUEST] RetrieveOrgByIdResponse"
                    + "(superUser=null). Unable to resolve organisation's superuser email. Case id: " + CASE_ID;
    private static final String EXPECTED_WARNING_INVALID_ORGANISATION_RESPONSE_TO_RESOLVE_ORGANISATION_EMAIL_3 =
            "Cannot retrieve old organisation by id " + ORGANISATION_ID + " [200 OK] null. Unable to resolve "
                    + "organisation's superuser email. Case id: " + CASE_ID;
    private static final String EXPECTED_WARNING_INVALID_ORGANISATION_RESPONSE_TO_RESOLVE_ORGANISATION_EMAIL_4 =
            "Cannot retrieve old organisation by id " + ORGANISATION_ID + " [200 OK] RetrieveOrgByIdResponse"
                    + "(superUser=null). Unable to resolve organisation's superuser email. Case id: " + CASE_ID;
    private static final String EXPECTED_WARNING_INVALID_ORGANISATION_RESPONSE_TO_RESOLVE_ORGANISATION_EMAIL_5 =
            "Cannot retrieve old organisation by id " + ORGANISATION_ID + " [200 OK] RetrieveOrgByIdResponse"
                    + "(superUser=RetrieveOrgByIdResponse.SuperUser(firstName=null, lastName=null, email=null)). "
                    + "Unable to resolve organisation's superuser email. Case id: " + CASE_ID;

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
    void theCanNotifyRepresentativeOrganisation() {
        // when representative is empty should return false
        assertThat(NotificationUtils.canNotifyRepresentativeOrganisation(null)).isFalse();
        // when representative does not have id should return false
        RepresentedTypeRItem representative = new RepresentedTypeRItem();
        assertThat(NotificationUtils.canNotifyRepresentativeOrganisation(representative)).isFalse();
        // when representative does not have value should return false
        representative.setId(REPRESENTATIVE_ID);
        assertThat(NotificationUtils.canNotifyRepresentativeOrganisation(representative)).isFalse();
        // when representative does not have organisation should return false
        representative.setValue(RepresentedTypeR.builder().build());
        assertThat(NotificationUtils.canNotifyRepresentativeOrganisation(representative)).isFalse();
        // when representative does not have organisation id should return false
        representative.getValue().setRespondentOrganisation(Organisation.builder().build());
        assertThat(NotificationUtils.canNotifyRepresentativeOrganisation(representative)).isFalse();
        // when representative is valid should return true
        representative.getValue().getRespondentOrganisation().setOrganisationID(ORGANISATION_ID);
        assertThat(NotificationUtils.canNotifyRepresentativeOrganisation(representative)).isTrue();
    }

    @Test
    void theCanResolveOrganisationSuperuserEmail() {
        // when organisation id is empty should return false
        RetrieveOrgByIdResponse retrieveOrgByIdResponse = RetrieveOrgByIdResponse.builder().build();
        ResponseEntity<RetrieveOrgByIdResponse> orgResponse = new ResponseEntity<>(retrieveOrgByIdResponse,
                HttpStatus.OK);
        assertThat(NotificationUtils.canResolveOrganisationSuperuserEmail(StringUtils.EMPTY, ORGANISATION_ID,
                NOC_TYPE_REMOVAL, orgResponse)).isFalse();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_ONE,
                EXPECTED_WARNING_INVALID_PARAMETERS_TO_RESOLVE_ORGANISATION_EMAIL_WITHOUT_CASE_ID);
        // when case id is empty should return false
        assertThat(NotificationUtils.canResolveOrganisationSuperuserEmail(CASE_ID, StringUtils.EMPTY,
                NOC_TYPE_REMOVAL, orgResponse)).isFalse();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_TWO,
                EXPECTED_WARNING_INVALID_PARAMETERS_TO_RESOLVE_ORGANISATION_EMAIL_WITH_CASE_ID);
        // when noc type is empty should return false
        assertThat(NotificationUtils.canResolveOrganisationSuperuserEmail(CASE_ID, ORGANISATION_ID,
                StringUtils.EMPTY, orgResponse)).isFalse();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_THREE,
                EXPECTED_WARNING_INVALID_PARAMETERS_TO_RESOLVE_ORGANISATION_EMAIL_WITH_CASE_ID);
        // when organisation response is empty should return false
        assertThat(NotificationUtils.canResolveOrganisationSuperuserEmail(CASE_ID, ORGANISATION_ID,
                NOC_TYPE_REMOVAL, null)).isFalse();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_FOUR,
                EXPECTED_WARNING_INVALID_ORGANISATION_RESPONSE_TO_RESOLVE_ORGANISATION_EMAIL_1);
        // when organisation response status code is different from successful should return false
        orgResponse = new ResponseEntity<>(retrieveOrgByIdResponse, HttpStatus.BAD_REQUEST);
        assertThat(NotificationUtils.canResolveOrganisationSuperuserEmail(CASE_ID, ORGANISATION_ID,
                NOC_TYPE_REMOVAL, orgResponse)).isFalse();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_FIVE,
                EXPECTED_WARNING_INVALID_ORGANISATION_RESPONSE_TO_RESOLVE_ORGANISATION_EMAIL_2);
        // when organisation response not has body should return false
        orgResponse = new ResponseEntity<>(null, HttpStatus.OK);
        assertThat(NotificationUtils.canResolveOrganisationSuperuserEmail(CASE_ID, ORGANISATION_ID,
                NOC_TYPE_REMOVAL, orgResponse)).isFalse();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_SIX,
                EXPECTED_WARNING_INVALID_ORGANISATION_RESPONSE_TO_RESOLVE_ORGANISATION_EMAIL_3);
        // when organisation response body not has superuser should return false
        orgResponse = new ResponseEntity<>(retrieveOrgByIdResponse, HttpStatus.OK);
        assertThat(NotificationUtils.canResolveOrganisationSuperuserEmail(CASE_ID, ORGANISATION_ID,
                NOC_TYPE_REMOVAL, orgResponse)).isFalse();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_SEVEN,
                EXPECTED_WARNING_INVALID_ORGANISATION_RESPONSE_TO_RESOLVE_ORGANISATION_EMAIL_4);
        // when superuser not has email should return false
        retrieveOrgByIdResponse.setSuperUser(RetrieveOrgByIdResponse.SuperUser.builder().build());
        orgResponse = new ResponseEntity<>(retrieveOrgByIdResponse, HttpStatus.OK);
        assertThat(NotificationUtils.canResolveOrganisationSuperuserEmail(CASE_ID, ORGANISATION_ID,
                NOC_TYPE_REMOVAL, orgResponse)).isFalse();
        LoggerTestUtils.checkLog(Level.WARN, LoggerTestUtils.INTEGER_EIGHT,
                EXPECTED_WARNING_INVALID_ORGANISATION_RESPONSE_TO_RESOLVE_ORGANISATION_EMAIL_5);
        // when superuser has email should return true
        retrieveOrgByIdResponse.getSuperUser().setEmail(ORGANISATION_EMAIL);
        assertThat(NotificationUtils.canResolveOrganisationSuperuserEmail(CASE_ID, ORGANISATION_ID,
                NOC_TYPE_REMOVAL, orgResponse)).isTrue();
    }
}
