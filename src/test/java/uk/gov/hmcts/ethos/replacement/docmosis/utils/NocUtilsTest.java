package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

final class NocUtilsTest {

    private static final String EXPECTED_ERROR_INVALID_RESPONDENT_EXISTS = "Invalid respondent exists.";
    private static final String EXPECTED_ERROR_SELECTED_RESPONDENT_NOT_FOUND =
            "Selected respondent with name Respondent Name Two not found.";
    private static final String EXPECTED_ERROR_INVALID_CASE_DATA = "Invalid case data";
    private static final String EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES =
            "Respondent with name Respondent Name One has more than one representative";

    private static final String REPRESENTATIVE_NAME = "Representative Name";
    private static final String RESPONDENT_NAME_ONE = "Respondent Name One";
    private static final String RESPONDENT_NAME_TWO = "Respondent Name Two";
    private static final String RESPONDENT_ID_ONE = "dummy_respondent_id_1";
    private static final String RESPONDENT_ID_TWO = "dummy_respondent_id_2";
    private static final String DUMMY_REPRESENTATIVE_ID = "dummy_representative_id";
    private static final String DUMMY_CASE_SUBMISSION_REFERENCE = "1234567890123456";

    @Test
    void theValidateRepresentativeRespondentMapping() {
        // when representative collection is empty should return an empty list
        List<RespondentSumTypeItem> respondents = new ArrayList<>();
        assertThat(NocUtils.validateRepresentativeRespondentMapping(null, respondents)).isEmpty();

        // when respondent collection has null element
        List<RepresentedTypeRItem> representedTypes = new ArrayList<>();
        representedTypes.add(buildRepresentedTypeRItem(RESPONDENT_NAME_TWO));
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
        respondents.getFirst().getValue().setRespondentName(RESPONDENT_NAME_ONE);
        errors = NocUtils.validateRepresentativeRespondentMapping(representedTypes, respondents);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_SELECTED_RESPONDENT_NOT_FOUND);
        // when respondent collection has respondent with a valid name should return an empty list
        respondents.getFirst().getValue().setRespondentName(RESPONDENT_NAME_TWO);
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
        respondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_ONE).build());
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        caseData.setRepCollection(List.of(buildRepresentedTypeRItem(RESPONDENT_NAME_ONE),
                buildRepresentedTypeRItem(RESPONDENT_NAME_ONE)));
        errors = NocUtils.validateNocCaseData(caseData);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES);
        // when case data has invalid respondent name
        caseData.setRepCollection(List.of(buildRepresentedTypeRItem(RESPONDENT_NAME_TWO)));
        errors = NocUtils.validateNocCaseData(caseData);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_SELECTED_RESPONDENT_NOT_FOUND);
        // when case data has valid respondent name
        caseData.getRespondentCollection().getFirst().getValue().setRespondentName(RESPONDENT_NAME_TWO);
        errors = NocUtils.validateNocCaseData(caseData);
        assertThat(errors).isEmpty();
    }

    private static RepresentedTypeRItem buildRepresentedTypeRItem(String respondentName) {
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(respondentName);
        dynamicFixedListType.setValue(dynamicValueType);
        return RepresentedTypeRItem.builder().id(DUMMY_REPRESENTATIVE_ID).value(RepresentedTypeR.builder()
                .dynamicRespRepName(dynamicFixedListType).nameOfRepresentative(REPRESENTATIVE_NAME).build()).build();
    }

    @Test
    void theIsValidNocRepresentative() {
        // when representative is empty should return false
        assertThat(NocUtils.isValidNocRepresentative(null)).isFalse();
        // when representative id is empty should return false
        RepresentedTypeRItem respondentRepresentative = RepresentedTypeRItem.builder().build();
        assertThat(NocUtils.isValidNocRepresentative(respondentRepresentative)).isFalse();
        // when representative value is empty should return false
        respondentRepresentative.setId(DUMMY_REPRESENTATIVE_ID);
        assertThat(NocUtils.isValidNocRepresentative(respondentRepresentative)).isFalse();
        // when representative does not have name should return false
        respondentRepresentative.setValue(RepresentedTypeR.builder().build());
        assertThat(NocUtils.isValidNocRepresentative(respondentRepresentative)).isFalse();
        // when representative dynamic respondent name is empty should return false
        respondentRepresentative.setValue(RepresentedTypeR.builder().nameOfRepresentative(REPRESENTATIVE_NAME).build());
        assertThat(NocUtils.isValidNocRepresentative(respondentRepresentative)).isFalse();
        // when dynamic respondent name does not have any value should return false
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        respondentRepresentative.getValue().setDynamicRespRepName(dynamicFixedListType);
        assertThat(NocUtils.isValidNocRepresentative(respondentRepresentative)).isFalse();
        // when dynamic respondent name value does not have any value should return false
        respondentRepresentative.getValue().getDynamicRespRepName().setValue(new DynamicValueType());
        assertThat(NocUtils.isValidNocRepresentative(respondentRepresentative)).isFalse();
        // when dynamic respondent name value does not have any respondent name should return false
        respondentRepresentative.getValue().getDynamicRespRepName().getValue().setLabel(StringUtils.EMPTY);
        assertThat(NocUtils.isValidNocRepresentative(respondentRepresentative)).isFalse();
        // when dynamic respondent name value is not empty and has a respondent name should return true
        respondentRepresentative.getValue().getDynamicRespRepName().getValue().setLabel(RESPONDENT_NAME_TWO);
        assertThat(NocUtils.isValidNocRepresentative(respondentRepresentative)).isTrue();
    }

    @Test
    @SneakyThrows
    void theMapRepresentativesToRespondents() {
        // when case data respondent collection is empty should not throw any exception
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(new ArrayList<>());
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE));
        // when case data respondent collection is not empty but representative collection is empty should not throw
        // any exception
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE));
        // when case data both respondent and representative collections are not empty but respondent in
        // respondent collection doesn't have any value should not throw any exception
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().build()));
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE));
        // when both respondent id and name exists but not matches with representative respondent id and
        // selected respondent name(dynamicRespRepName).
        caseData.getRespondentCollection().getFirst().setId(RESPONDENT_ID_ONE);
        caseData.getRespondentCollection().getFirst().setValue(RespondentSumType.builder()
                .respondentName(RESPONDENT_NAME_ONE).represented(NO).representativeRemoved(NO).build());
        caseData.getRepCollection().getFirst().setId(DUMMY_REPRESENTATIVE_ID);
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(RESPONDENT_NAME_TWO);
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.getRepCollection().getFirst().setValue(RepresentedTypeR.builder()
                .nameOfRepresentative(REPRESENTATIVE_NAME).respondentId(RESPONDENT_ID_TWO)
                .dynamicRespRepName(dynamicFixedListType).build());
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE));
        // when respondent name is equal to selected name in representative collection
        caseData.getRepCollection().getFirst().getValue().getDynamicRespRepName().getValue()
                .setLabel(RESPONDENT_NAME_ONE);
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE));
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresented()).isEqualTo(YES);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeRemoved()).isEqualTo(NO);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isEqualTo(DUMMY_REPRESENTATIVE_ID);
        assertThat(caseData.getRepCollection().getFirst().getValue().getRespondentId()).isEqualTo(RESPONDENT_ID_ONE);
        assertThat(caseData.getRepCollection().getFirst().getId()).isEqualTo(DUMMY_REPRESENTATIVE_ID);
        assertThat(caseData.getRepCollection().getFirst().getValue().getRespRepName()).isEqualTo(RESPONDENT_NAME_ONE);
        // when respondent id is equal to respondent id in representative collection
        caseData.getRespondentCollection().getFirst().getValue().setRepresented(YES);
        caseData.getRespondentCollection().getFirst().getValue().setRepresentativeRemoved(NO);
        caseData.getRespondentCollection().getFirst().getValue().setRepresentativeId(StringUtils.EMPTY);

        caseData.getRepCollection().getFirst().getValue().getDynamicRespRepName().getValue()
                .setLabel(RESPONDENT_NAME_TWO);
        caseData.getRepCollection().getFirst().getValue().setRespondentId(RESPONDENT_ID_ONE);
        caseData.getRepCollection().getFirst().getValue().setRespRepName(StringUtils.EMPTY);

        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE));
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresented()).isEqualTo(YES);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeRemoved()).isEqualTo(NO);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isEqualTo(DUMMY_REPRESENTATIVE_ID);
        assertThat(caseData.getRepCollection().getFirst().getValue().getRespondentId()).isEqualTo(RESPONDENT_ID_ONE);
        assertThat(caseData.getRepCollection().getFirst().getValue().getRespRepName()).isEqualTo(RESPONDENT_NAME_ONE);
        assertThat(caseData.getRepCollection().getFirst().getId()).isEqualTo(DUMMY_REPRESENTATIVE_ID);
        // when both respondent id and respondent names are equal in respondent collection and representative
        // collection
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE));
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresented()).isEqualTo(YES);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeRemoved()).isEqualTo(NO);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isEqualTo(DUMMY_REPRESENTATIVE_ID);
        assertThat(caseData.getRepCollection().getFirst().getValue().getRespondentId()).isEqualTo(RESPONDENT_ID_ONE);
        assertThat(caseData.getRepCollection().getFirst().getValue().getRespRepName()).isEqualTo(RESPONDENT_NAME_ONE);
        assertThat(caseData.getRepCollection().getFirst().getId()).isEqualTo(DUMMY_REPRESENTATIVE_ID);
    }

    @Test
    void theAssignNonMyHmctsOrganisationIds() {
        // when representative collection is empty
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        // when a representative is null in a not empty representative collection
        representatives.add(null);
        // when a representative does not have value in a non-empty representative collection.
        representatives.add(RepresentedTypeRItem.builder().build());
        // when a representative is already an HMCTS representative
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().myHmctsYesNo(YES).build())
                .build());
        // when a representative is non HMCTS representative
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().myHmctsYesNo(NO).build())
                .build());
        assertDoesNotThrow(() -> NocUtils.assignNonMyHmctsOrganisationIds(representatives));
        assertThat(representatives.getFirst()).isNull();
        assertThat(representatives.get(NumberUtils.INTEGER_ONE).getValue()).isNull();
        assertThat(representatives.get(NumberUtils.INTEGER_TWO).getValue().getMyHmctsYesNo()).isEqualTo(YES);
        assertThat(representatives.getLast().getValue().getMyHmctsYesNo()).isEqualTo(NO);
        assertThat(representatives.getLast().getValue().getNonMyHmctsOrganisationId()).isNotEmpty();
    }

}
