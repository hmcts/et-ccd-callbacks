package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;

import static org.assertj.core.api.Assertions.assertThat;

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
    private static final String SOLICITOR_A_LOWERCASE = "[solicitora]";
    private static final String SOLICITOR_B_LOWERCASE = "[solicitorb]";

    private static final int INTEGER_THREE = 3;
    private static final int INTEGER_FOUR = 4;
    private static final int INTEGER_FIVE = 5;
    private static final int INTEGER_SIX = 6;
    private static final int INTEGER_SEVEN = 7;
    private static final int INTEGER_EIGHT = 8;
    private static final int INTEGER_NINE = 9;
    private static final int INTEGER_TEN = 10;

    @Test
    void theIsRespondentRepresentativeRole() {
        // null input
        assertThat(RoleUtils.isRespondentRepresentativeRole(null)).isFalse();
        // valid enum values
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
        // invalid values
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_K)).isFalse();
        assertThat(RoleUtils.isRespondentRepresentativeRole(ROLE_SOLICITOR_L)).isFalse();
        // case sensitivity
        assertThat(RoleUtils.isRespondentRepresentativeRole(SOLICITOR_A_LOWERCASE)).isFalse();
        assertThat(RoleUtils.isRespondentRepresentativeRole(SOLICITOR_B_LOWERCASE)).isFalse();
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

}
