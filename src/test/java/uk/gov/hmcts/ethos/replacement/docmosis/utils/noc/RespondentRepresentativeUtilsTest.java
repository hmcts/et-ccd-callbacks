package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

final class RespondentRepresentativeUtilsTest {

    private static final String DUMMY_CASE_REFERENCE = "1234567890123456";
    private static final String RESPONDENT_ID_1 = "1abc957b-e8f5-3487-84c5-a736eb6605b8";
    private static final String RESPONDENT_ID_2 = "2abc957b-e8f5-3487-84c5-a736eb6605b8";
    private static final String REPRESENTATIVE_ID_1 = "6abc957b-e8f5-3487-84c5-a736eb6605b8";
    private static final String REPRESENTATIVE_ID_2 = "6abc957b-e8f5-3487-84c5-a736eb6605b9";
    private static final String ORGANISATION_ID_1 = "dummy12_organisation34_id56";
    private static final String ORGANISATION_ID_2 = "dummy65_organisation43_id21";
    private static final String EXCEPTION_REPRESENTATIVE_NOT_FOUND =
            "Representative not found for case ID 1234567890123456.";
    private static final String EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND =
            "Representative ID not found for case ID 1234567890123456.";
    private static final String EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXISTS =
            "Representative details not found for representative ID 6abc957b-e8f5-3487-84c5-a736eb6605b8 "
                    + "in case 1234567890123456.";

    private static final String EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS = "Invalid representative exists.";
    private static final String EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES =
            "Respondent with name Respondent name 1 has more than one representative";

    private static final String RESPONDENT_NAME_1 = "Respondent name 1";
    private static final String RESPONDENT_NAME_2 = "Respondent name 2";
    private static final String REPRESENTATIVE_NAME_1 = "Representative name 1";
    private static final String REPRESENTATIVE_NAME_2 = "Representative name 2";

    private static final String REPRESENTATIVE_EMAIL_1 = "representative1@testmail.com";
    private static final String REPRESENTATIVE_EMAIL_1_CAPITALISED = "REPRESENTATIVE1@TESTMAIL.COM";
    private static final String REPRESENTATIVE_EMAIL_2 = "representative2@testmail.com";

    @Test
    void theValidateRepresentative() {
        // when representative is null
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> RespondentRepresentativeUtils.validateRepresentative(null, DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_NOT_FOUND);
        // when representative does not have id
        RepresentedTypeRItem representativeWithoutId = RepresentedTypeRItem.builder().build();
        gse = assertThrows(GenericServiceException.class,
                () -> RespondentRepresentativeUtils.validateRepresentative(representativeWithoutId,
                        DUMMY_CASE_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND);
        // when respondent details not found
        RepresentedTypeRItem representativeWithoutDetails = RepresentedTypeRItem.builder().build();
        representativeWithoutDetails.setId(REPRESENTATIVE_ID_1);
        gse = assertThrows(GenericServiceException.class,
                () -> RespondentRepresentativeUtils.validateRepresentative(representativeWithoutDetails,
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
        representatives = List.of(RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1).value(
                RepresentedTypeR.builder().nameOfRepresentative(REPRESENTATIVE_NAME_1)
                        .dynamicRespRepName(createDynamicFixedListType(RESPONDENT_NAME_1)).build())
                        .build(), RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1).value(
                                RepresentedTypeR.builder().nameOfRepresentative(REPRESENTATIVE_NAME_2)
                                        .dynamicRespRepName(createDynamicFixedListType(RESPONDENT_NAME_1))
                                        .build()).build());
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES);
        // when representatives have no duplicated respondent names should not return any error.
        representatives = List.of(RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1)
                        .value(RepresentedTypeR.builder().nameOfRepresentative(REPRESENTATIVE_NAME_1)
                                .dynamicRespRepName(createDynamicFixedListType(RESPONDENT_NAME_1)).build()).build(),
                RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1).value(RepresentedTypeR.builder()
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
    void theHasRespondentRepresentative() {
        // when case data is empty then return false
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentative(null)).isFalse();
        // when case data not has any representative then return false
        CaseData caseData = new CaseData();
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentative(caseData)).isFalse();
        // when case data has representative then return true
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().build()));
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentative(caseData)).isTrue();
    }

    @Test
    void theIsValidRepresentative() {
        // when representative is empty should return false
        assertThat(RespondentRepresentativeUtils.isValidRepresentative(null)).isFalse();
        // when representative if is empty should return false
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(StringUtils.EMPTY).build();
        assertThat(RespondentRepresentativeUtils.isValidRepresentative(representative)).isFalse();
        // when representative value is empty should return false
        representative.setId(REPRESENTATIVE_ID_1);
        assertThat(RespondentRepresentativeUtils.isValidRepresentative(representative)).isFalse();
        // when a valid representative should return true
        representative.setValue(RepresentedTypeR.builder().build());
        assertThat(RespondentRepresentativeUtils.isValidRepresentative(representative)).isTrue();
    }

    @Test
    void theCanRemoveRepresentativeFromAssignments() {
        // when representative is empty should return false
        assertThat(RespondentRepresentativeUtils.canRemoveRepresentativeFromAssignments(null)).isFalse();

        //when representative doesn't my hmcts organisation should return false
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1)
                .value(RepresentedTypeR.builder().build()).build();
        assertThat(RespondentRepresentativeUtils.canRemoveRepresentativeFromAssignments(representative)).isFalse();

        // when representative doesn't have organisation should return false
        representative.getValue().setMyHmctsYesNo(YES);
        assertThat(RespondentRepresentativeUtils.canRemoveRepresentativeFromAssignments(representative)).isFalse();

        // when representative doesn't have organisation id should return false
        representative.getValue().setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.canRemoveRepresentativeFromAssignments(representative)).isFalse();

        // when representative doesn't have email should return false
        representative.getValue().getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.canRemoveRepresentativeFromAssignments(representative)).isFalse();

        // when it is possible to remove representative from assignments should return true
        representative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        assertThat(RespondentRepresentativeUtils.canRemoveRepresentativeFromAssignments(representative)).isTrue();
    }

    @Test
    void theIsRepresentativeOrganisationChanged() {
        RepresentedTypeR oldRepresentative = RepresentedTypeR.builder().build();
        RepresentedTypeR newRepresentative = RepresentedTypeR.builder().build();
        // when both old and new representatives not have organisation should return false
        assertThat(RespondentRepresentativeUtils.isRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isFalse();
        // when old representative has organisation but new not should return true
        oldRepresentative.setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.isRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isTrue();
        // when new representative has organisation but old not should return true
        oldRepresentative.setRespondentOrganisation(null);
        newRepresentative.setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.isRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isTrue();
        // when both representatives not have organisation ids should return false
        oldRepresentative.setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.isRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isFalse();
        // when old representative has organisation id but new not should return true
        oldRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.isRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isTrue();
        // when new representative has organisation id but old not should return true
        oldRepresentative.getRespondentOrganisation().setOrganisationID(StringUtils.EMPTY);
        newRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.isRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isTrue();
        // when both of them have different organisation ids should return true
        oldRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        newRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_2);
        assertThat(RespondentRepresentativeUtils.isRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isTrue();
        // when both of them have same organisation ids should return false
        oldRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        newRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.isRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isFalse();
    }

    @Test
    void theRepresentsSameRespondent() {
        RepresentedTypeRItem oldRespondent = RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().build())
                .build();
        RepresentedTypeRItem newRespondent = RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().build())
                .build();
        // when old representative does not have any respondent id should return false
        assertThat(RespondentRepresentativeUtils.representsSameRespondent(oldRespondent, newRespondent)).isFalse();
        // when new representative does not have any respondent id should return false
        oldRespondent.getValue().setRespondentId(RESPONDENT_ID_1);
        assertThat(RespondentRepresentativeUtils.representsSameRespondent(oldRespondent, newRespondent)).isFalse();
        // when old and new representatives have different respondent ids should return false
        newRespondent.getValue().setRespondentId(RESPONDENT_ID_2);
        assertThat(RespondentRepresentativeUtils.representsSameRespondent(oldRespondent, newRespondent)).isFalse();
        // when old and new representatives have same respondent ids should return true
        newRespondent.getValue().setRespondentId(RESPONDENT_ID_1);
        assertThat(RespondentRepresentativeUtils.representsSameRespondent(oldRespondent, newRespondent)).isTrue();
        // when old and new representatives' respondent ids does not match
        newRespondent.getValue().setRespondentId(RESPONDENT_ID_2);
        // when old representative does not have any respondent name should return false
        assertThat(RespondentRepresentativeUtils.representsSameRespondent(oldRespondent, newRespondent)).isFalse();
        // when new representative does not have any respondent name should return false
        oldRespondent.getValue().setRespRepName(RESPONDENT_NAME_1);
        assertThat(RespondentRepresentativeUtils.representsSameRespondent(oldRespondent, newRespondent)).isFalse();
        // when old and new representatives' respondent names are different should return false
        newRespondent.getValue().setRespRepName(RESPONDENT_NAME_2);
        assertThat(RespondentRepresentativeUtils.representsSameRespondent(oldRespondent, newRespondent)).isFalse();
        // when old and new representatives' respondent names are same should return true
        newRespondent.getValue().setRespRepName(RESPONDENT_NAME_1);
        assertThat(RespondentRepresentativeUtils.representsSameRespondent(oldRespondent, newRespondent)).isTrue();
    }

    @Test
    void theIsRepresentativeEmailChanged() {
        RepresentedTypeR oldRepresentative = RepresentedTypeR.builder().build();
        RepresentedTypeR newRepresentative = RepresentedTypeR.builder().build();
        // when both emails are null should return false
        assertThat(RespondentRepresentativeUtils.isRepresentativeEmailChanged(oldRepresentative, newRepresentative))
                .isFalse();
        // when one of the email is null should return true
        oldRepresentative.setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        assertThat(RespondentRepresentativeUtils.isRepresentativeEmailChanged(oldRepresentative, newRepresentative))
                .isTrue();
        // when both emails are empty should return false
        oldRepresentative.setRepresentativeEmailAddress(StringUtils.EMPTY);
        newRepresentative.setRepresentativeEmailAddress(StringUtils.EMPTY);
        assertThat(RespondentRepresentativeUtils.isRepresentativeEmailChanged(oldRepresentative, newRepresentative))
                .isFalse();
        // when has different emails should return true
        oldRepresentative.setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        newRepresentative.setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_2);
        assertThat(RespondentRepresentativeUtils.isRepresentativeEmailChanged(oldRepresentative, newRepresentative))
                .isTrue();
        // when has same emails should return false
        newRepresentative.setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        assertThat(RespondentRepresentativeUtils.isRepresentativeEmailChanged(oldRepresentative, newRepresentative))
                .isFalse();
        // when one of the emails is capitalised should return false
        newRepresentative.setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1_CAPITALISED);
        assertThat(RespondentRepresentativeUtils.isRepresentativeEmailChanged(oldRepresentative, newRepresentative))
                .isFalse();
    }

    @Test
    void theFindRepresentativesToRemove() {
        List<RepresentedTypeRItem> oldRepresentatives = new ArrayList<>();
        List<RepresentedTypeRItem> newRepresentatives = new ArrayList<>();
        // when old respondent collection is empty should return empty list
        assertThat(RespondentRepresentativeUtils.findRepresentativesToRemove(oldRepresentatives, newRepresentatives))
                .isEmpty();
        // when old representatives list has invalid representative should return empty list
        oldRepresentatives.add(RepresentedTypeRItem.builder().build());
        assertThat(RespondentRepresentativeUtils.findRepresentativesToRemove(oldRepresentatives, newRepresentatives))
                .isEmpty();
        // when old representatives list has valid representative but there is no new representative should return
        // that valid representative in a list
        RepresentedTypeRItem validOldRepresentative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1).value(
                RepresentedTypeR.builder().build()).build();
        oldRepresentatives.add(validOldRepresentative);
        List<RepresentedTypeRItem> representativesToRemove = RespondentRepresentativeUtils.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        // when new representative list has invalid representative should return list of valid old representatives
        newRepresentatives.add(RepresentedTypeRItem.builder().build());
        representativesToRemove = RespondentRepresentativeUtils.findRepresentativesToRemove(oldRepresentatives,
                newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        // when new representative list has valid representative that doesn't represent same respondent in old
        // representatives should return list of non-representing old representatives
        RepresentedTypeRItem validNewRepresentative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_2).value(
                RepresentedTypeR.builder().build()).build();
        newRepresentatives.add(validNewRepresentative);
        representativesToRemove = RespondentRepresentativeUtils.findRepresentativesToRemove(oldRepresentatives,
                newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        // when both new and old representatives represent the same respondent but old and new representatives not have
        // organisation and email should return empty list
        validOldRepresentative.getValue().setRespRepName(RESPONDENT_NAME_1);
        validNewRepresentative.getValue().setRespRepName(RESPONDENT_NAME_1);
        assertThat(RespondentRepresentativeUtils.findRepresentativesToRemove(oldRepresentatives, newRepresentatives))
                .isEmpty();
        // when old representative has organisation but new not should return list of old representative
        validNewRepresentative.getValue().setRespondentOrganisation(Organisation.builder().build());
        representativesToRemove = RespondentRepresentativeUtils.findRepresentativesToRemove(oldRepresentatives,
                newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        // when old representative has email but new not should return list of old representative
        validNewRepresentative.getValue().setRespondentOrganisation(null);
        validNewRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1_CAPITALISED);
        representativesToRemove = RespondentRepresentativeUtils.findRepresentativesToRemove(oldRepresentatives,
                newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
    }

}
