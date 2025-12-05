package uk.gov.hmcts.ethos.replacement.docmosis.utils;

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

final class RepresentativeUtilsTest {

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

    @Test
    void theValidateRespondentRepresentative() {
        // when representative is null
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> RepresentativeUtils.validateRespondentRepresentative(null, DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_NOT_FOUND);
        // when representative does not have id
        RepresentedTypeRItem representativeWithoutId = RepresentedTypeRItem.builder().build();
        gse = assertThrows(GenericServiceException.class,
                () -> RepresentativeUtils.validateRespondentRepresentative(representativeWithoutId,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND);
        // when respondent details not found
        RepresentedTypeRItem representativeWithoutDetails = RepresentedTypeRItem.builder().build();
        representativeWithoutDetails.setId(DUMMY_REPRESENTATIVE_ID);
        gse = assertThrows(GenericServiceException.class,
                () -> RepresentativeUtils.validateRespondentRepresentative(representativeWithoutDetails,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXISTS);
    }

    @Test
    void theHasDuplicateRespondentNames() {
        // when representatives are empty should not return any error.
        List<String> errors = RepresentativeUtils.hasDuplicateRespondentNames(null);
        assertThat(errors).isEmpty();
        // when representative is null should return ERROR_INVALID_REPRESENTATIVE_EXISTS.
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        representatives.add(null);
        errors = RepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when representative does not have a value should return ERROR_INVALID_REPRESENTATIVE_EXISTS.
        representatives = List.of(RepresentedTypeRItem.builder().build());
        errors = RepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when representative does not have a dynamic respondent name (dynamicRespRepName) should return
        // ERROR_INVALID_REPRESENTATIVE_EXISTS.
        representatives.getFirst().setValue(RepresentedTypeR.builder().build());
        errors = RepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when dynamic respondent name does not have any value should return
        // ERROR_INVALID_REPRESENTATIVE_EXISTS
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        representatives.getFirst().getValue().setDynamicRespRepName(dynamicFixedListType);
        errors = RepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when dynamic respondent name value does not have any label should return
        // ERROR_INVALID_REPRESENTATIVE_EXISTS
        DynamicValueType dynamicValueType = new DynamicValueType();
        representatives.getFirst().getValue().getDynamicRespRepName().setValue(dynamicValueType);
        errors = RepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when representatives have more than one respondent with the same name should return
        // ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES.
        representatives = List.of(RepresentedTypeRItem.builder().id(DUMMY_REPRESENTATIVE_ID).value(
                RepresentedTypeR.builder().dynamicRespRepName(createDynamicFixedListType(RESPONDENT_NAME_1)).build())
                        .build(), RepresentedTypeRItem.builder().id(DUMMY_REPRESENTATIVE_ID).value(
                                RepresentedTypeR.builder().dynamicRespRepName(
                                        createDynamicFixedListType(RESPONDENT_NAME_1)).build()).build());
        errors = RepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES);
        // when representatives have no duplicated respondent names should not return any error.
        representatives = List.of(RepresentedTypeRItem.builder().id(DUMMY_REPRESENTATIVE_ID)
                        .value(RepresentedTypeR.builder().dynamicRespRepName(
                                createDynamicFixedListType(RESPONDENT_NAME_1)).build()).build(),
                RepresentedTypeRItem.builder().id(DUMMY_REPRESENTATIVE_ID).value(RepresentedTypeR.builder()
                        .dynamicRespRepName(createDynamicFixedListType(RESPONDENT_NAME_2)).build()).build());
        errors = RepresentativeUtils.hasDuplicateRespondentNames(representatives);
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
        assertThat(RepresentativeUtils.hasRepresentatives(null)).isFalse();
        // when case data not has any representative then return false
        CaseData caseData = new CaseData();
        assertThat(RepresentativeUtils.hasRepresentatives(caseData)).isFalse();
        // when case data has representative then return true
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().build()));
        assertThat(RepresentativeUtils.hasRepresentatives(caseData)).isTrue();
    }

}
