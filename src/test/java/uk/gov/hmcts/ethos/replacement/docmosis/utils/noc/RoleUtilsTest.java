package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

final class RoleUtilsTest {

    private static final String RESPONDENT_NAME_ONE = "Respondent Name One";
    private static final String RESPONDENT_NAME_TWO = "Respondent Name Two";
    private static final String RESPONDENT_NAME_THREE = "Respondent Name Three";
    private static final String RESPONDENT_NAME_FOUR = "Respondent Name Four";
    private static final String RESPONDENT_NAME_FIVE = "Respondent Name Five";
    private static final String RESPONDENT_NAME_SIX = "Respondent Name Six";
    private static final String RESPONDENT_NAME_SEVEN = "Respondent Name Seven";
    private static final String RESPONDENT_NAME_EIGHT = "Respondent Name Eight";
    private static final String RESPONDENT_NAME_NINE = "Respondent Name Nine";
    private static final String RESPONDENT_NAME_TEN = "Respondent Name Ten";
    private static final String ORGANISATION_ID_ONE = "Organisation id 1";
    private static final String ORGANISATION_ID_TWO = "Organisation id 2";
    private static final String ORGANISATION_ID_THREE = "Organisation id 3";
    private static final String ORGANISATION_ID_FOUR = "Organisation id 4";
    private static final String ORGANISATION_ID_FIVE = "Organisation id 5";
    private static final String ORGANISATION_ID_SIX = "Organisation id 6";
    private static final String ORGANISATION_ID_SEVEN = "Organisation id 7";
    private static final String ORGANISATION_ID_EIGHT = "Organisation id 8";
    private static final String ORGANISATION_ID_NINE = "Organisation id 9";
    private static final String ORGANISATION_ID_TEN = "Organisation id 10";
    private static final String ROLE_SOLICITOR_A = "[SOLICITORA]";
    private static final String ROLE_SOLICITOR_B = "[SOLICITORB]";
    private static final String ROLE_SOLICITOR_C = "[SOLICITORC]";
    private static final String ROLE_SOLICITOR_D = "[SOLICITORD]";
    private static final String ROLE_SOLICITOR_E = "[SOLICITORE]";
    private static final String ROLE_SOLICITOR_F = "[SOLICITORF]";
    private static final String ROLE_SOLICITOR_G = "[SOLICITORG]";
    private static final String ROLE_SOLICITOR_H = "[SOLICITORH]";
    private static final String ROLE_SOLICITOR_I = "[SOLICITORI]";
    private static final String ROLE_SOLICITOR_J = "[SOLICITORJ]";
    private static final String ROLE_SOLICITOR_K = "[SOLICITORK]";
    private static final String ROLE_SOLICITOR_L = "[SOLICITORL]";
    private static final String ROLE_SOLICITOR_A_LOWERCASE = "[solicitora]";
    private static final String ROLE_SOLICITOR_B_LOWERCASE = "[solicitorb]";
    private static final String ROLE_CLAIMANT_SOLICITOR = "[CLAIMANTSOLICITOR]";
    private static final String ROLE_CLAIMANT_SOLICITOR_LOWERCASE = "[claimantsolicitor]";
    private static final String ROLE_CLAIMANT_SOLICITOR_INVALID = "[CLAIMANTSOLICITORINVALID]";
    private static final String ROLE_INVALID = "[INVALIDROLE]";

    private static final int INTEGER_THREE = 3;
    private static final int INTEGER_FOUR = 4;
    private static final int INTEGER_FIVE = 5;
    private static final int INTEGER_SIX = 6;
    private static final int INTEGER_SEVEN = 7;
    private static final int INTEGER_EIGHT = 8;
    private static final int INTEGER_NINE = 9;
    private static final int INTEGER_TEN = 10;
    private static final int INTEGER_ELEVEN = 11;

    @Test
    void theIsRespondentRepresentativeRole() {
        // null input should return false
        assertThat(RoleUtils.isRespondentRepresentativeRole(null)).isFalse();
        // valid respondent solicitor roles should return true
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_A)).isTrue();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_B)).isTrue();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_C)).isTrue();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_D)).isTrue();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_E)).isTrue();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_F)).isTrue();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_G)).isTrue();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_H)).isTrue();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_I)).isTrue();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_J)).isTrue();
        // invalid respondent solicitor roles should return false
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_K)).isFalse();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_L)).isFalse();
        // lowercase respondent solicitor roles should return false (case-sensitive)
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_A_LOWERCASE)).isFalse();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_B_LOWERCASE)).isFalse();
    }

    @Test
    void theIsClaimantRepresentativeRole() {
        // null input should return false
        assertThat(RoleUtils.isClaimantRepresentativeRole(null)).isFalse();
        // valid claimant solicitor role should return true
        assertThat(RoleUtils.isClaimantRepresentativeRole(ROLE_CLAIMANT_SOLICITOR)).isTrue();
        // invalid claimant solicitor role should return false
        assertThat(RoleUtils.isClaimantRepresentativeRole(ROLE_CLAIMANT_SOLICITOR_INVALID)).isFalse();
        // lowercase claimant solicitor role should return false (case-sensitive)
        assertThat(RoleUtils.isClaimantRepresentativeRole(ROLE_CLAIMANT_SOLICITOR_LOWERCASE)).isFalse();
    }

    @Test
    void theIsValidRole() {
        // null input should return false
        assertThat(RoleUtils.isValidRole(null)).isFalse();
        // valid claimant solicitor role should return true
        assertThat(RoleUtils.isValidRole(ROLE_CLAIMANT_SOLICITOR)).isTrue();
        // valid respondent solicitor role should return true
        assertThat(RoleUtils.isValidRole(ROLE_SOLICITOR_A)).isTrue();
        // invalid role should return false
        assertThat(RoleUtils.isValidRole(ROLE_INVALID)).isFalse();
        // lowercase claimant solicitor role should return false (case-sensitive)
        assertThat(RoleUtils.isClaimantRepresentativeRole(ROLE_CLAIMANT_SOLICITOR_LOWERCASE)).isFalse();
        // lowercase respondent solicitor roles should return false (case-sensitive)
        assertThat(RoleUtils.isClaimantRepresentativeRole(ROLE_SOLICITOR_A_LOWERCASE)).isFalse();
        assertThat(RoleUtils.isClaimantRepresentativeRole(ROLE_SOLICITOR_B_LOWERCASE)).isFalse();
    }

    @Test
    void theFindSolicitorRoleIndexByRespondentName() {
        // when case data is empty should return minus one
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(null, RESPONDENT_NAME_ONE))
                .isEqualTo(NumberUtils.INTEGER_MINUS_ONE);
        // when respondent name is empty should return minus one
        CaseData caseData = new CaseData();
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, StringUtils.EMPTY))
                .isEqualTo(NumberUtils.INTEGER_MINUS_ONE);
        // when respondent name not found in the notice of change answers
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_ONE))
                .isEqualTo(NumberUtils.INTEGER_MINUS_ONE);
        // when respondent names are found in the notice of change answers
        setAllNoticeOfChangeAnswers(caseData);
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_ONE))
                .isEqualTo(NumberUtils.INTEGER_ZERO);
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_TWO))
                .isEqualTo(NumberUtils.INTEGER_ONE);
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_THREE))
                .isEqualTo(NumberUtils.INTEGER_TWO);
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_FOUR))
                .isEqualTo(INTEGER_THREE);
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_FIVE))
                .isEqualTo(INTEGER_FOUR);
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_SIX))
                .isEqualTo(INTEGER_FIVE);
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_SEVEN))
                .isEqualTo(INTEGER_SIX);
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_EIGHT))
                .isEqualTo(INTEGER_SEVEN);
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_NINE))
                .isEqualTo(INTEGER_EIGHT);
        assertThat(RoleUtils.findSolicitorRoleIndexByRespondentName(caseData, RESPONDENT_NAME_TEN))
                .isEqualTo(INTEGER_NINE);
    }

    private static void setAllNoticeOfChangeAnswers(CaseData caseData) {
        caseData.setNoticeOfChangeAnswers0(NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_ONE).build());
        caseData.setNoticeOfChangeAnswers1(NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_TWO).build());
        caseData.setNoticeOfChangeAnswers2(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_THREE).build());
        caseData.setNoticeOfChangeAnswers3(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_FOUR).build());
        caseData.setNoticeOfChangeAnswers4(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_FIVE).build());
        caseData.setNoticeOfChangeAnswers5(NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_SIX).build());
        caseData.setNoticeOfChangeAnswers6(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_SEVEN).build());
        caseData.setNoticeOfChangeAnswers7(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_EIGHT).build());
        caseData.setNoticeOfChangeAnswers8(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_NINE).build());
        caseData.setNoticeOfChangeAnswers9(NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_TEN).build());
    }

    @Test
    void theSolicitorRoleLabelForIndex() {
        // when index is less than 0 should return empty string
        assertThat(RoleUtils.solicitorRoleLabelForIndex(NumberUtils.INTEGER_MINUS_ONE)).isEqualTo(StringUtils.EMPTY);
        // when index is greater than or equal to 10 should return empty string
        assertThat(RoleUtils.solicitorRoleLabelForIndex(INTEGER_TEN)).isEqualTo(StringUtils.EMPTY);
        // when index is equal to 0 should return role [SOLICITORA]
        assertThat(RoleUtils.solicitorRoleLabelForIndex(NumberUtils.INTEGER_ZERO)).isEqualTo(ROLE_SOLICITOR_A);
        // when index is equal to 1 should return role [SOLICITORB]
        assertThat(RoleUtils.solicitorRoleLabelForIndex(NumberUtils.INTEGER_ONE)).isEqualTo(ROLE_SOLICITOR_B);
        // when index is equal to 2 should return role [SOLICITORC]
        assertThat(RoleUtils.solicitorRoleLabelForIndex(NumberUtils.INTEGER_TWO)).isEqualTo(ROLE_SOLICITOR_C);
        // when index is equal to 3 should return role [SOLICITORD]
        assertThat(RoleUtils.solicitorRoleLabelForIndex(INTEGER_THREE)).isEqualTo(ROLE_SOLICITOR_D);
        // when index is equal to 4 should return role [SOLICITORE]
        assertThat(RoleUtils.solicitorRoleLabelForIndex(INTEGER_FOUR)).isEqualTo(ROLE_SOLICITOR_E);
        // when index is equal to 5 should return role [SOLICITORF]
        assertThat(RoleUtils.solicitorRoleLabelForIndex(INTEGER_FIVE)).isEqualTo(ROLE_SOLICITOR_F);
        // when index is equal to 6 should return role [SOLICITORG]
        assertThat(RoleUtils.solicitorRoleLabelForIndex(INTEGER_SIX)).isEqualTo(ROLE_SOLICITOR_G);
        // when index is equal to 7 should return role [SOLICITORH]
        assertThat(RoleUtils.solicitorRoleLabelForIndex(INTEGER_SEVEN)).isEqualTo(ROLE_SOLICITOR_H);
        // when index is equal to 8 should return role [SOLICITORI]
        assertThat(RoleUtils.solicitorRoleLabelForIndex(INTEGER_EIGHT)).isEqualTo(ROLE_SOLICITOR_I);
        // when index is equal to 9 should return role [SOLICITORJ]
        assertThat(RoleUtils.solicitorRoleLabelForIndex(INTEGER_NINE)).isEqualTo(ROLE_SOLICITOR_J);
    }

    @Test
    public void theFindRespondentRepresentativeRole() {
        // when representative is empty should return empty string
        CaseData caseData = new CaseData();
        assertThat(RoleUtils.findRespondentRepresentativeRole(null, caseData)).isEqualTo(StringUtils.EMPTY);

        // when representative value is empty should return empty string
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        assertThat(RoleUtils.findRespondentRepresentativeRole(representative, caseData)).isEqualTo(StringUtils.EMPTY);

        // when representative has role, should return that role
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_A).build());
        assertThat(RoleUtils.findRespondentRepresentativeRole(representative, caseData)).isEqualTo(ROLE_SOLICITOR_A);

        // when representative not has role and respondent name should return empty string
        representative.setValue(RepresentedTypeR.builder().build());
        assertThat(RoleUtils.findRespondentRepresentativeRole(representative, caseData)).isEqualTo(StringUtils.EMPTY);

        // when representative has respondent name but case data does not have any notice of change answer
        representative.getValue().setRespRepName(RESPONDENT_NAME_ONE);
        assertThat(RoleUtils.findRespondentRepresentativeRole(representative, caseData)).isEqualTo(StringUtils.EMPTY);

        // when representative has respondent name and case data has notice of change answer with the same
        // respondent name
        caseData.setNoticeOfChangeAnswers3(NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_ONE).build());
        assertThat(RoleUtils.findRespondentRepresentativeRole(representative, caseData)).isEqualTo(ROLE_SOLICITOR_D);
    }

    @Test
    void theFindRoleIndexByRoleLabel() {
        // when role is null should return minus one
        assertThat(RoleUtils.findRoleIndexByRoleLabel(StringUtils.EMPTY)).isEqualTo(NumberUtils.INTEGER_MINUS_ONE);
        // when role is empty should return minus one
        assertThat(RoleUtils.findRoleIndexByRoleLabel(StringUtils.EMPTY)).isEqualTo(NumberUtils.INTEGER_MINUS_ONE);
        // when claimant solicitor role should return minus one
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_CLAIMANT_SOLICITOR)).isEqualTo(
                NumberUtils.INTEGER_MINUS_ONE);
        // when not a valid respondent solicitor role should return minus one
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_K)).isEqualTo(NumberUtils.INTEGER_MINUS_ONE);
        //when valid solicitor role should return its index
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_A)).isEqualTo(NumberUtils.INTEGER_ZERO);
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_B)).isEqualTo(NumberUtils.INTEGER_ONE);
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_C)).isEqualTo(NumberUtils.INTEGER_TWO);
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_D)).isEqualTo(INTEGER_THREE);
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_E)).isEqualTo(INTEGER_FOUR);
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_F)).isEqualTo(INTEGER_FIVE);
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_G)).isEqualTo(INTEGER_SIX);
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_H)).isEqualTo(INTEGER_SEVEN);
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_I)).isEqualTo(INTEGER_EIGHT);
        assertThat(RoleUtils.findRoleIndexByRoleLabel(ROLE_SOLICITOR_J)).isEqualTo(INTEGER_NINE);
    }

    @Test
    void theFindRespondentNameByIndex() {
        CaseData caseData = new CaseData();
        // when index is less than 0 should return empty string
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, NumberUtils.INTEGER_MINUS_ONE))
                .isEqualTo(StringUtils.EMPTY);
        // when index is equal to 10 should return empty string
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, INTEGER_TEN)).isEqualTo(StringUtils.EMPTY);
        // when index is greater than 10 should return empty string
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, INTEGER_ELEVEN)).isEqualTo(StringUtils.EMPTY);
        // when index is greater than or equal to 0 and less than 10 and case data notice of change answers are empty
        // should return empty string
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, NumberUtils.INTEGER_ONE)).isEqualTo(StringUtils.EMPTY);
        // when notice of change answers exists should return that respondent name
        setAllNoticeOfChangeAnswers(caseData);
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, NumberUtils.INTEGER_ZERO))
                .isEqualTo(RESPONDENT_NAME_ONE);
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, NumberUtils.INTEGER_ONE))
                .isEqualTo(RESPONDENT_NAME_TWO);
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, NumberUtils.INTEGER_TWO))
                .isEqualTo(RESPONDENT_NAME_THREE);
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, INTEGER_THREE)).isEqualTo(RESPONDENT_NAME_FOUR);
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, INTEGER_FOUR)).isEqualTo(RESPONDENT_NAME_FIVE);
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, INTEGER_FIVE)).isEqualTo(RESPONDENT_NAME_SIX);
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, INTEGER_SIX)).isEqualTo(RESPONDENT_NAME_SEVEN);
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, INTEGER_SEVEN)).isEqualTo(RESPONDENT_NAME_EIGHT);
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, INTEGER_EIGHT)).isEqualTo(RESPONDENT_NAME_NINE);
        assertThat(RoleUtils.findRespondentNameByIndex(caseData, INTEGER_NINE)).isEqualTo(RESPONDENT_NAME_TEN);
    }

    @Test
    void theFindRespondentNameByRole() {
        // when case data is empty should return empty string
        assertThat(RoleUtils.findRespondentNameByRole(null, ROLE_SOLICITOR_A)).isEqualTo(StringUtils.EMPTY);
        // when role is empty should return empty string
        CaseData caseData = new CaseData();
        assertThat(RoleUtils.findRespondentNameByRole(caseData, StringUtils.EMPTY)).isEqualTo(StringUtils.EMPTY);
        // when case data doesn't have any notice of change answer should return empty string
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_A)).isEqualTo(StringUtils.EMPTY);
        // when role is invalid should return empty string
        setAllNoticeOfChangeAnswers(caseData);
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_K)).isEqualTo(StringUtils.EMPTY);
        // when role is valid should return respondent name
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_A)).isEqualTo(RESPONDENT_NAME_ONE);
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_B)).isEqualTo(RESPONDENT_NAME_TWO);
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_C)).isEqualTo(RESPONDENT_NAME_THREE);
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_D)).isEqualTo(RESPONDENT_NAME_FOUR);
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_E)).isEqualTo(RESPONDENT_NAME_FIVE);
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_F)).isEqualTo(RESPONDENT_NAME_SIX);
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_G)).isEqualTo(RESPONDENT_NAME_SEVEN);
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_H)).isEqualTo(RESPONDENT_NAME_EIGHT);
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_I)).isEqualTo(RESPONDENT_NAME_NINE);
        assertThat(RoleUtils.findRespondentNameByRole(caseData, ROLE_SOLICITOR_J)).isEqualTo(RESPONDENT_NAME_TEN);
    }

    private static void setAllRespondentOrganisationPolicy(CaseData caseData) {
        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_ONE).build()).build());
        caseData.setRespondentOrganisationPolicy1(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_TWO).build()).build());
        caseData.setRespondentOrganisationPolicy2(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_THREE).build()).build());
        caseData.setRespondentOrganisationPolicy3(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_FOUR).build()).build());
        caseData.setRespondentOrganisationPolicy4(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_FIVE).build()).build());
        caseData.setRespondentOrganisationPolicy5(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_SIX).build()).build());
        caseData.setRespondentOrganisationPolicy6(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_SEVEN).build()).build());
        caseData.setRespondentOrganisationPolicy7(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_EIGHT).build()).build());
        caseData.setRespondentOrganisationPolicy8(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_NINE).build()).build());
        caseData.setRespondentOrganisationPolicy9(OrganisationPolicy.builder().organisation(Organisation.builder()
                .organisationID(ORGANISATION_ID_TEN).build()).build());
    }

    @Test
    void theRemoveOrganisationPolicyByIndex() {
        // when case data is empty should not throw any exception
        assertDoesNotThrow(() -> RoleUtils.removeOrganisationPolicyByIndex(null, NumberUtils.INTEGER_ZERO));
        // when index is less than zero should not throw any exception
        CaseData caseData = new CaseData();
        setAllRespondentOrganisationPolicy(caseData);
        assertDoesNotThrow(() -> RoleUtils.removeOrganisationPolicyByIndex(caseData, NumberUtils.INTEGER_MINUS_ONE));
        // when index is equal to 10 should not throw any exception
        assertDoesNotThrow(() -> RoleUtils.removeOrganisationPolicyByIndex(caseData, INTEGER_TEN));
        // when index is greater than 10 should not throw any exception
        assertDoesNotThrow(() -> RoleUtils.removeOrganisationPolicyByIndex(caseData, INTEGER_ELEVEN));
        // when index is equal or greater than zero and less than ten should remove correspondent organisation policy
        OrganisationPolicy emptyPolicy = OrganisationPolicy.builder().build();
        RoleUtils.removeOrganisationPolicyByIndex(caseData, NumberUtils.INTEGER_ZERO);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isEqualTo(emptyPolicy);
        RoleUtils.removeOrganisationPolicyByIndex(caseData, NumberUtils.INTEGER_ONE);
        assertThat(caseData.getRespondentOrganisationPolicy1()).isEqualTo(emptyPolicy);
        RoleUtils.removeOrganisationPolicyByIndex(caseData, NumberUtils.INTEGER_TWO);
        assertThat(caseData.getRespondentOrganisationPolicy2()).isEqualTo(emptyPolicy);
        RoleUtils.removeOrganisationPolicyByIndex(caseData, INTEGER_THREE);
        assertThat(caseData.getRespondentOrganisationPolicy3()).isEqualTo(emptyPolicy);
        RoleUtils.removeOrganisationPolicyByIndex(caseData, INTEGER_FOUR);
        assertThat(caseData.getRespondentOrganisationPolicy4()).isEqualTo(emptyPolicy);
        RoleUtils.removeOrganisationPolicyByIndex(caseData, INTEGER_FIVE);
        assertThat(caseData.getRespondentOrganisationPolicy5()).isEqualTo(emptyPolicy);
        RoleUtils.removeOrganisationPolicyByIndex(caseData, INTEGER_SIX);
        assertThat(caseData.getRespondentOrganisationPolicy6()).isEqualTo(emptyPolicy);
        RoleUtils.removeOrganisationPolicyByIndex(caseData, INTEGER_SEVEN);
        assertThat(caseData.getRespondentOrganisationPolicy7()).isEqualTo(emptyPolicy);
        RoleUtils.removeOrganisationPolicyByIndex(caseData, INTEGER_EIGHT);
        assertThat(caseData.getRespondentOrganisationPolicy8()).isEqualTo(emptyPolicy);
        RoleUtils.removeOrganisationPolicyByIndex(caseData, INTEGER_NINE);
        assertThat(caseData.getRespondentOrganisationPolicy9()).isEqualTo(emptyPolicy);
    }

    @Test
    void theRemoveNocAnswersByIndex() {
        // when case data is empty should not throw any exception
        assertDoesNotThrow(() -> RoleUtils.removeNocAnswersByIndex(null, NumberUtils.INTEGER_ZERO));
        // when index is less than zero should not throw any exception
        CaseData caseData = new CaseData();
        setAllNoticeOfChangeAnswers(caseData);
        assertDoesNotThrow(() -> RoleUtils.removeNocAnswersByIndex(caseData, NumberUtils.INTEGER_MINUS_ONE));
        // when index is equal to 10 should not throw any exception
        assertDoesNotThrow(() -> RoleUtils.removeNocAnswersByIndex(caseData, INTEGER_TEN));
        // when index is greater than 10 should not throw any exception
        assertDoesNotThrow(() -> RoleUtils.removeNocAnswersByIndex(caseData, INTEGER_ELEVEN));
        // when index is equal or greater than zero and less than ten should remove correspondent organisation policy
        NoticeOfChangeAnswers emptyNoticeOfChangeAnswers = NoticeOfChangeAnswers.builder().build();
        RoleUtils.removeNocAnswersByIndex(caseData, NumberUtils.INTEGER_ZERO);
        assertThat(caseData.getNoticeOfChangeAnswers0()).isEqualTo(emptyNoticeOfChangeAnswers);
        RoleUtils.removeNocAnswersByIndex(caseData, NumberUtils.INTEGER_ONE);
        assertThat(caseData.getNoticeOfChangeAnswers1()).isEqualTo(emptyNoticeOfChangeAnswers);
        RoleUtils.removeNocAnswersByIndex(caseData, NumberUtils.INTEGER_TWO);
        assertThat(caseData.getNoticeOfChangeAnswers2()).isEqualTo(emptyNoticeOfChangeAnswers);
        RoleUtils.removeNocAnswersByIndex(caseData, INTEGER_THREE);
        assertThat(caseData.getNoticeOfChangeAnswers3()).isEqualTo(emptyNoticeOfChangeAnswers);
        RoleUtils.removeNocAnswersByIndex(caseData, INTEGER_FOUR);
        assertThat(caseData.getNoticeOfChangeAnswers4()).isEqualTo(emptyNoticeOfChangeAnswers);
        RoleUtils.removeNocAnswersByIndex(caseData, INTEGER_FIVE);
        assertThat(caseData.getNoticeOfChangeAnswers5()).isEqualTo(emptyNoticeOfChangeAnswers);
        RoleUtils.removeNocAnswersByIndex(caseData, INTEGER_SIX);
        assertThat(caseData.getNoticeOfChangeAnswers6()).isEqualTo(emptyNoticeOfChangeAnswers);
        RoleUtils.removeNocAnswersByIndex(caseData, INTEGER_SEVEN);
        assertThat(caseData.getNoticeOfChangeAnswers7()).isEqualTo(emptyNoticeOfChangeAnswers);
        RoleUtils.removeNocAnswersByIndex(caseData, INTEGER_EIGHT);
        assertThat(caseData.getNoticeOfChangeAnswers8()).isEqualTo(emptyNoticeOfChangeAnswers);
        RoleUtils.removeNocAnswersByIndex(caseData, INTEGER_NINE);
        assertThat(caseData.getNoticeOfChangeAnswers9()).isEqualTo(emptyNoticeOfChangeAnswers);
    }

    @Test
    void theRemoveOrganisationPolicyAndNocAnswersByRepresentative() {
        // when case data is null should not throw any exception
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        assertDoesNotThrow(() -> RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(null, representative));
        // when representative is null should not throw any exception
        CaseData caseData = new CaseData();
        assertDoesNotThrow(() -> RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, null));
        setAllRespondentOrganisationPolicy(caseData);
        setAllNoticeOfChangeAnswers(caseData);
        // should remove respondent organisation policy and notice of change answers
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_A).build());
        RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, representative);
        OrganisationPolicy emptyPolicy = OrganisationPolicy.builder().build();
        assertThat(caseData.getRespondentOrganisationPolicy0()).isEqualTo(emptyPolicy);
        NoticeOfChangeAnswers emptyNoticeOfChangeAnswers = NoticeOfChangeAnswers.builder().build();
        assertThat(caseData.getNoticeOfChangeAnswers0()).isEqualTo(emptyNoticeOfChangeAnswers);

        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_B).build());
        RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, representative);
        assertThat(caseData.getRespondentOrganisationPolicy1()).isEqualTo(emptyPolicy);
        assertThat(caseData.getNoticeOfChangeAnswers1()).isEqualTo(emptyNoticeOfChangeAnswers);

        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_C).build());
        RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, representative);
        assertThat(caseData.getRespondentOrganisationPolicy2()).isEqualTo(emptyPolicy);
        assertThat(caseData.getNoticeOfChangeAnswers2()).isEqualTo(emptyNoticeOfChangeAnswers);

        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_D).build());
        RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, representative);
        assertThat(caseData.getRespondentOrganisationPolicy3()).isEqualTo(emptyPolicy);
        assertThat(caseData.getNoticeOfChangeAnswers3()).isEqualTo(emptyNoticeOfChangeAnswers);

        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_E).build());
        RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, representative);
        assertThat(caseData.getRespondentOrganisationPolicy4()).isEqualTo(emptyPolicy);
        assertThat(caseData.getNoticeOfChangeAnswers4()).isEqualTo(emptyNoticeOfChangeAnswers);

        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_F).build());
        RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, representative);
        assertThat(caseData.getRespondentOrganisationPolicy5()).isEqualTo(emptyPolicy);
        assertThat(caseData.getNoticeOfChangeAnswers5()).isEqualTo(emptyNoticeOfChangeAnswers);

        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_G).build());
        RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, representative);
        assertThat(caseData.getRespondentOrganisationPolicy6()).isEqualTo(emptyPolicy);
        assertThat(caseData.getNoticeOfChangeAnswers6()).isEqualTo(emptyNoticeOfChangeAnswers);

        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_H).build());
        RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, representative);
        assertThat(caseData.getRespondentOrganisationPolicy7()).isEqualTo(emptyPolicy);
        assertThat(caseData.getNoticeOfChangeAnswers7()).isEqualTo(emptyNoticeOfChangeAnswers);

        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_I).build());
        RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, representative);
        assertThat(caseData.getRespondentOrganisationPolicy8()).isEqualTo(emptyPolicy);
        assertThat(caseData.getNoticeOfChangeAnswers8()).isEqualTo(emptyNoticeOfChangeAnswers);

        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_J).build());
        RoleUtils.removeOrganisationPolicyAndNocAnswersByRepresentative(caseData, representative);
        assertThat(caseData.getRespondentOrganisationPolicy9()).isEqualTo(emptyPolicy);
        assertThat(caseData.getNoticeOfChangeAnswers9()).isEqualTo(emptyNoticeOfChangeAnswers);
    }
}
