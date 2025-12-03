package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class NocUtilsTest {

    private static final String EXPECTED_ERROR_INVALID_RESPONDENT_EXISTS = "Invalid respondent exists.";
    private static final String EXPECTED_ERROR_SELECTED_RESPONDENT_NOT_FOUND =
            "Selected respondent with name Valid Respondent Name not found.";
    private static final String EXPECTED_ERROR_INVALID_CASE_DATA = "Invalid case data";
    private static final String EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES =
            "Respondent with name Invalid Respondent Name has more than one representative";

    private static final String INVALID_RESPONDENT_NAME = "Invalid Respondent Name";
    private static final String VALID_RESPONDENT_NAME = "Valid Respondent Name";

    @Test
    void theValidateRepresentativeRespondentMapping() {
        // when representative collection is empty should return an empty list
        List<RespondentSumTypeItem> respondents = new ArrayList<>();
        assertThat(NocUtils.validateRepresentativeRespondentMapping(null, respondents)).isEmpty();

        // when respondent collection has null element
        List<RepresentedTypeRItem> representedTypes = new ArrayList<>();
        representedTypes.add(buildRepresentedTypeRItem(VALID_RESPONDENT_NAME));
        respondents.add(null);
        List<String> errors = NocUtils.validateRepresentativeRespondentMapping(representedTypes, respondents);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_RESPONDENT_EXISTS);
        // when respondent collection has respondent without value should return
        // ERROR_INVALID_RESPONDENT_EXISTS
        respondents = new ArrayList<>();
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondents.add(respondentSumTypeItem);
        errors = NocUtils.validateRepresentativeRespondentMapping(representedTypes, respondents);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_RESPONDENT_EXISTS);
        // when respondent collection has respondent without respondent name should return
        // ERROR_INVALID_RESPONDENT_EXISTS
        respondents.getFirst().setValue(RespondentSumType.builder().build());
        errors = NocUtils.validateRepresentativeRespondentMapping(representedTypes, respondents);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_RESPONDENT_EXISTS);
        // when respondent collection has respondent with an invalid name should return
        // ERROR_SELECTED_RESPONDENT_NOT_FOUND
        respondents.getFirst().getValue().setRespondentName(INVALID_RESPONDENT_NAME);
        errors = NocUtils.validateRepresentativeRespondentMapping(representedTypes, respondents);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_SELECTED_RESPONDENT_NOT_FOUND);
        // when respondent collection has respondent with a valid name should return an empty list
        respondents.getFirst().getValue().setRespondentName(VALID_RESPONDENT_NAME);
        errors = NocUtils.validateRepresentativeRespondentMapping(representedTypes, respondents);
        assertThat(errors).isEmpty();
    }

    @Test
    void theValidateNocCaseData() {
        // when case data is empty should return invalid case data error
        List<String> errors = NocUtils.validateNocCaseData(null);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_CASE_DATA);
        // when case data not has any respondent
        CaseData caseData = new CaseData();
        errors = NocUtils.validateNocCaseData(caseData);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_CASE_DATA);
        // when case data representative collection has duplicated respondent name
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(INVALID_RESPONDENT_NAME).build());
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        caseData.setRepCollection(List.of(buildRepresentedTypeRItem(INVALID_RESPONDENT_NAME),
                buildRepresentedTypeRItem(INVALID_RESPONDENT_NAME)));
        errors = NocUtils.validateNocCaseData(caseData);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES);
        // when case data has invalid respondent name
        caseData.setRepCollection(List.of(buildRepresentedTypeRItem(VALID_RESPONDENT_NAME)));
        errors = NocUtils.validateNocCaseData(caseData);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_SELECTED_RESPONDENT_NOT_FOUND);
        // when case data has valid respondent name
        caseData.getRespondentCollection().getFirst().getValue().setRespondentName(VALID_RESPONDENT_NAME);
        errors = NocUtils.validateNocCaseData(caseData);
        assertThat(errors).isEmpty();
    }

    private static RepresentedTypeRItem buildRepresentedTypeRItem(String respondentName) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(respondentName);
        dynamicFixedListType.setValue(dynamicValueType);
        return RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().dynamicRespRepName(dynamicFixedListType)
                .build()).build();
    }

}
