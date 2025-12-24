package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

final class RespondentRepresentativeUtilsTest {

    private static final String DUMMY_CASE_REFERENCE = "1234567890123456";
    private static final String DUMMY_REPRESENTATIVE_ID = "dummy12_representative34_id56";
    private static final String EXCEPTION_REPRESENTATIVE_NOT_FOUND =
            "Representative not found for case ID 1234567890123456.";
    private static final String EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND =
            "Representative ID not found for case ID 1234567890123456.";
    private static final String EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXISTS =
            "Representative details not found for representative ID dummy12_representative34_id56 "
                    + "in case 1234567890123456.";

    private static final String EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS = "Invalid representative exists.";
    private static final String EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES =
            "Respondent with name Respondent name 1 has more than one representative";

    private static final String RESPONDENT_NAME_1 = "Respondent name 1";
    private static final String RESPONDENT_NAME_2 = "Respondent name 2";
    private static final String RESPONDENT_ID_1 = "Respondent id 1";
    private static final String RESPONDENT_ID_2 = "Respondent id 2";
    private static final String REPRESENTATIVE_NAME_1 = "Representative name 1";
    private static final String REPRESENTATIVE_NAME_2 = "Representative name 2";
    private static final String REPRESENTATIVE_EMAIL_ADDRESS_1 = "representative1@hmcts.com";
    private static final String REPRESENTATIVE_EMAIL_ADDRESS_2 = "representative2@hmcts.com";

    private static final String SOLICITOR_A = "[SOLICITORA]";
    private static final String SOLICITOR_B = "[SOLICITORB]";
    private static final String SOLICITOR_C = "[SOLICITORC]";
    private static final String SOLICITOR_D = "[SOLICITORD]";
    private static final String SOLICITOR_E = "[SOLICITORE]";
    private static final String SOLICITOR_F = "[SOLICITORF]";
    private static final String SOLICITOR_G = "[SOLICITORG]";
    private static final String SOLICITOR_H = "[SOLICITORH]";
    private static final String SOLICITOR_I = "[SOLICITORI]";
    private static final String SOLICITOR_J = "[SOLICITORJ]";
    private static final String SOLICITOR_K = "[SOLICITORK]";
    private static final String SOLICITOR_L = "[SOLICITORL]";
    private static final String SOLICITOR_A_LOWERCASE = "[solicitora]";
    private static final String SOLICITOR_B_LOWERCASE = "[colicitorb]";

    @Test
    void theValidateRespondentRepresentative() {
        // when representative is null
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> RespondentRepresentativeUtils.validateRespondentRepresentative(null, DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_NOT_FOUND);
        // when representative does not have id
        RepresentedTypeRItem representativeWithoutId = RepresentedTypeRItem.builder().build();
        gse = assertThrows(GenericServiceException.class,
                () -> RespondentRepresentativeUtils.validateRespondentRepresentative(representativeWithoutId,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND);
        // when respondent details not found
        RepresentedTypeRItem representativeWithoutDetails = RepresentedTypeRItem.builder().build();
        representativeWithoutDetails.setId(DUMMY_REPRESENTATIVE_ID);
        gse = assertThrows(GenericServiceException.class,
                () -> RespondentRepresentativeUtils.validateRespondentRepresentative(representativeWithoutDetails,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXISTS);
    }

    @Test
    void theHasDuplicateRespondentNames() {
        // when representatives are empty should not return any error.
        List<String> errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(null);
        assertThat(errors).isEmpty();
        // when representative is null should return ERROR_INVALID_REPRESENTATIVE_EXISTS.
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        representatives.add(null);
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when representative does not have a value should return ERROR_INVALID_REPRESENTATIVE_EXISTS.
        representatives = List.of(RepresentedTypeRItem.builder().build());
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when representative does not have a dynamic respondent name (dynamicRespRepName) should return
        // ERROR_INVALID_REPRESENTATIVE_EXISTS.
        representatives.getFirst().setValue(RepresentedTypeR.builder().build());
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when dynamic respondent name does not have any value should return
        // ERROR_INVALID_REPRESENTATIVE_EXISTS
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        representatives.getFirst().getValue().setDynamicRespRepName(dynamicFixedListType);
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when dynamic respondent name value does not have any label should return
        // ERROR_INVALID_REPRESENTATIVE_EXISTS
        DynamicValueType dynamicValueType = new DynamicValueType();
        representatives.getFirst().getValue().getDynamicRespRepName().setValue(dynamicValueType);
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when representatives have more than one respondent with the same name should return
        // ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES.
        representatives = List.of(RepresentedTypeRItem.builder().id(DUMMY_REPRESENTATIVE_ID).value(
                RepresentedTypeR.builder().nameOfRepresentative(REPRESENTATIVE_NAME_1)
                        .dynamicRespRepName(createDynamicFixedListType(RESPONDENT_NAME_1)).build())
                        .build(), RepresentedTypeRItem.builder().id(DUMMY_REPRESENTATIVE_ID).value(
                                RepresentedTypeR.builder().nameOfRepresentative(REPRESENTATIVE_NAME_2)
                                        .dynamicRespRepName(createDynamicFixedListType(RESPONDENT_NAME_1))
                                        .build()).build());
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES);
        // when representatives have no duplicated respondent names should not return any error.
        representatives = List.of(RepresentedTypeRItem.builder().id(DUMMY_REPRESENTATIVE_ID)
                        .value(RepresentedTypeR.builder().nameOfRepresentative(REPRESENTATIVE_NAME_1)
                                .dynamicRespRepName(createDynamicFixedListType(RESPONDENT_NAME_1)).build()).build(),
                RepresentedTypeRItem.builder().id(DUMMY_REPRESENTATIVE_ID).value(RepresentedTypeR.builder()
                        .nameOfRepresentative(REPRESENTATIVE_NAME_2).dynamicRespRepName(
                                createDynamicFixedListType(RESPONDENT_NAME_2)).build()).build());
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isEmpty();
    }

    private static DynamicFixedListType createDynamicFixedListType(String respondentName) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(respondentName);
        dynamicFixedListType.setValue(dynamicValueType);
        return dynamicFixedListType;
    }

    @Test
    void theHasRepresentatives() {
        // when case data is empty then return false
        assertThat(RespondentRepresentativeUtils.hasRepresentatives(null)).isFalse();
        // when case data not has any representative then return false
        CaseData caseData = new CaseData();
        assertThat(RespondentRepresentativeUtils.hasRepresentatives(caseData)).isFalse();
        // when case data has representative then return true
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().build()));
        assertThat(RespondentRepresentativeUtils.hasRepresentatives(caseData)).isTrue();
    }

    @Test
    void theIsRespondentRepresentativeRole() {
        // null input
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(null)).isFalse();
        // valid enum values
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_A)).isTrue();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_B)).isTrue();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_C)).isTrue();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_D)).isTrue();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_E)).isTrue();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_F)).isTrue();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_G)).isTrue();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_H)).isTrue();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_I)).isTrue();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_J)).isTrue();
        // invalid values
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_K)).isFalse();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_L)).isFalse();
        // case sensitivity
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_A_LOWERCASE)).isFalse();
        assertThat(RespondentRepresentativeUtils.isRespondentRepresentativeRole(SOLICITOR_B_LOWERCASE)).isFalse();
    }

    @Test
    void theHasSameRespondentButDifferentEmail() {
        // when representative1 is null should return false
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(null, null)).isFalse();
        // when representative1 does not have any value should return false
        RepresentedTypeRItem representative1 = RepresentedTypeRItem.builder().build();
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(representative1, null)).isFalse();
        // when representative2 is null should return false
        representative1.setValue(RepresentedTypeR.builder().build());
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(representative1, null)).isFalse();
        // when representative2 does not have any value should return false
        RepresentedTypeRItem representative2 = RepresentedTypeRItem.builder().build();
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(representative1, representative2))
                .isFalse();
        // when representative1 does not have respondent id and respondent name
        representative2.setValue(RepresentedTypeR.builder().build());
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(representative1, representative2))
                .isFalse();
        // when representative1 has respondent name and that respondent name is equal to representative2 respondent name
        // and both has same e-mail address should return false
        representative1.getValue().setRespRepName(REPRESENTATIVE_NAME_1);
        representative2.getValue().setRespRepName(REPRESENTATIVE_NAME_1);
        representative1.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_ADDRESS_1);
        representative2.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_ADDRESS_1);
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(representative1, representative2))
                .isFalse();
        // when representative1 has respondent name and that respondent name is not equal to representative2
        // and both has same e-mail address should return false
        representative1.getValue().setRespRepName(REPRESENTATIVE_NAME_1);
        representative2.getValue().setRespRepName(REPRESENTATIVE_NAME_2);
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(representative1, representative2))
                .isFalse();
        // when representative1 has respondent id and that respondent id is not equal to representative2 respondent id
        // and both has different e-mail address should return false
        representative1.getValue().setRespondentId(RESPONDENT_ID_1);
        representative2.getValue().setRespondentId(RESPONDENT_ID_2);
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(representative1, representative2))
                .isFalse();
        // when representative1 has respondent id and that respondent id is equal to representative2 respondent id
        // and both has different e-mail address and representative1's organisation is not my hmcts organisation
        // should return false
        representative2.getValue().setRespondentId(RESPONDENT_ID_1);
        representative1.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_ADDRESS_1);
        representative2.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_ADDRESS_2);
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(representative1, representative2))
                .isFalse();
        // when representative1 has respondent id and that respondent id is equal to representative2 respondent id
        // and both has different e-mail address and representative1's organisation is my hmcts organisation
        // should return true
        representative1.getValue().setMyHmctsYesNo(YES);
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(representative1, representative2))
                .isTrue();
        // when representative 1 email address is empty should return false
        representative1.getValue().setRepresentativeEmailAddress(StringUtils.EMPTY);
        assertThat(RespondentRepresentativeUtils.hasSameRespondentButDifferentEmail(representative1, representative2))
                .isFalse();
    }
}
