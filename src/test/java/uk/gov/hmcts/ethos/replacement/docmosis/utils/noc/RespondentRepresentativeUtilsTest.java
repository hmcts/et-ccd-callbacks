package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    private static final String ROLE_SOLICITOR_A = "SOLICITORA";
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
    void theCanModifyAccess() {
        // when representative is empty should return false
        assertThat(RespondentRepresentativeUtils.canModifyAccess(null)).isFalse();

        //when representative doesn't my hmcts organisation should return false
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1)
                .value(RepresentedTypeR.builder().build()).build();
        assertThat(RespondentRepresentativeUtils.canModifyAccess(representative)).isFalse();

        // when representative doesn't have organisation should return false
        representative.getValue().setMyHmctsYesNo(YES);
        assertThat(RespondentRepresentativeUtils.canModifyAccess(representative)).isFalse();

        // when representative doesn't have organisation id should return false
        representative.getValue().setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.canModifyAccess(representative)).isFalse();

        // when representative doesn't have email should return false
        representative.getValue().getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.canModifyAccess(representative)).isFalse();

        // when it is possible to remove representative from assignments should return true
        representative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        assertThat(RespondentRepresentativeUtils.canModifyAccess(representative)).isTrue();
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
        // when old representatives is empty should return empty list
        assertThat(RespondentRepresentativeUtils.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives)).isEmpty();
        // when new representatives is empty should return old representative list
        oldRepresentatives.add(RepresentedTypeRItem.builder().build());
        assertThat(RespondentRepresentativeUtils.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives)).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE)
                .isEqualTo(oldRepresentatives);
        // when old representatives list has invalid representative should return empty list
        newRepresentatives.add(RepresentedTypeRItem.builder().build());
        assertThat(RespondentRepresentativeUtils.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives)).isEmpty();
        // when old representatives list has valid representative but there is no new representative should return
        // that valid representative in a list
        RepresentedTypeRItem validOldRepresentative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1).value(
                RepresentedTypeR.builder().respRepName(RESPONDENT_NAME_1).build()).build();
        oldRepresentatives.clear();
        oldRepresentatives.add(validOldRepresentative);
        List<RepresentedTypeRItem> representativesToRemove = RespondentRepresentativeUtils
                .findRepresentativesToRemove(oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
        // when new representative list has invalid representative should return list of valid old representatives
        representativesToRemove = RespondentRepresentativeUtils.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
        // when new representative list has valid representative that doesn't represent same respondent in old
        // representatives should return list of non-representing old representatives
        RepresentedTypeRItem validNewRepresentative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_2).value(
                RepresentedTypeR.builder().respRepName(RESPONDENT_NAME_2).build()).build();
        newRepresentatives.clear();
        newRepresentatives.add(validNewRepresentative);
        representativesToRemove = RespondentRepresentativeUtils.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
        // when both new and old representatives represent the same respondent but old and new representatives not have
        // organisation and email should return empty list
        validNewRepresentative.getValue().setRespRepName(RESPONDENT_NAME_1);
        representativesToRemove = RespondentRepresentativeUtils.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isEmpty();
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
        // when old representative has organisation but new not should return list of old representative
        validNewRepresentative.getValue().setRespRepName(RESPONDENT_NAME_2);
        validNewRepresentative.getValue().setRespondentOrganisation(Organisation.builder().build());
        representativesToRemove = RespondentRepresentativeUtils.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
        // when old representative has email but new not should return list of old representative
        validNewRepresentative.getValue().setRespondentOrganisation(null);
        validNewRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1_CAPITALISED);
        representativesToRemove = RespondentRepresentativeUtils.findRepresentativesToRemove(
                oldRepresentatives, newRepresentatives);
        assertThat(representativesToRemove).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(representativesToRemove.getFirst()).isEqualTo(validOldRepresentative);
        assertThat(newRepresentatives).hasSize(NumberUtils.INTEGER_ONE);
    }

    @Test
    void theFindNewOrUpdatedRepresentatives() {
        List<RepresentedTypeRItem> newRepresentatives = new ArrayList<>();
        // when old representatives list is empty should return new representative list
        newRepresentatives.add(RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_2).build());
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                null)).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE).isEqualTo(newRepresentatives);
        newRepresentatives.clear();
        // when new representatives list is empty should return empty list
        RepresentedTypeRItem oldRepresentative = RepresentedTypeRItem.builder().build();
        List<RepresentedTypeRItem> oldRepresentatives = List.of(oldRepresentative);
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                oldRepresentatives)).isEmpty();
        // when new representatives list has invalid representative should return empty list
        RepresentedTypeRItem newRepresentative = RepresentedTypeRItem.builder().build();
        newRepresentatives.add(newRepresentative);
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                oldRepresentatives)).isEmpty();
        // when old representative is not valid should return empty list
        newRepresentative.setId(REPRESENTATIVE_ID_1);
        newRepresentative.setValue(RepresentedTypeR.builder().build());
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                oldRepresentatives)).hasSize(NumberUtils.INTEGER_ONE);
        // when old representative and new representative does not represent same representative should return a list of
        // new representative
        oldRepresentative.setId(REPRESENTATIVE_ID_2);
        oldRepresentative.setValue(RepresentedTypeR.builder().build());
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                oldRepresentatives)).hasSize(NumberUtils.INTEGER_ONE);
        // when old representative and new representative has different organisations should return a list of new
        // representative
        newRepresentative.getValue().setRespondentId(RESPONDENT_ID_1);
        newRepresentative.getValue().setRespondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_1)
                .build());
        oldRepresentative.getValue().setRespondentId(RESPONDENT_ID_1);
        oldRepresentative.getValue().setRespondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_2)
                .build());
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                oldRepresentatives)).hasSize(NumberUtils.INTEGER_ONE);
        // when old representative and new representative has different emails should return a list of new
        // representative
        oldRepresentative.getValue().setRespondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_1)
                .build());
        oldRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_2);
        newRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                oldRepresentatives)).hasSize(NumberUtils.INTEGER_ONE);
        // when new representative has same email with old representative should return empty list
        oldRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                oldRepresentatives)).isEmpty();
    }

    @Test
    void theFilterModifiableRepresentatives() {
        // if representative list is empty should return an empty list
        assertThat(RespondentRepresentativeUtils.filterModifiableRepresentatives(null)).isEmpty();
        // if representative list does not have a modifiable representative should return empty list
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        List<RepresentedTypeRItem> representatives = List.of(representative);
        assertThat(RespondentRepresentativeUtils.filterModifiableRepresentatives(representatives)).isEmpty();
        // if representative list has a modifiable representative should return that representative in a list
        representative.setId(REPRESENTATIVE_ID_1);
        representative.setValue(RepresentedTypeR.builder().myHmctsYesNo(YES).respondentOrganisation(
                Organisation.builder().organisationID(ORGANISATION_ID_1).build()).representativeEmailAddress(
                        REPRESENTATIVE_EMAIL_1).build());
        List<RepresentedTypeRItem> expectedRepresentatives = RespondentRepresentativeUtils
                .filterModifiableRepresentatives(representatives);
        assertThat(expectedRepresentatives).isNotEmpty().hasSize(NumberUtils.INTEGER_ONE);
        assertThat(expectedRepresentatives.getFirst()).isEqualTo(representative);

    }

    @Test
    void theFindRespondentByRepresentative() {
        // when case data is null should return null
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        assertThat(RespondentRepresentativeUtils.findRespondentByRepresentative(null, representative))
                .isNull();
        // when case data respondent collection is empty should return null
        CaseData tmpCaseData = new CaseData();
        tmpCaseData.setRespondentCollection(new ArrayList<>());
        assertThat(RespondentRepresentativeUtils.findRespondentByRepresentative(tmpCaseData, representative)).isNull();
        // when representative is null should return null
        RespondentSumTypeItem tmpRespondentSumTypeItem = new RespondentSumTypeItem();
        tmpCaseData.getRespondentCollection().add(tmpRespondentSumTypeItem);
        assertThat(RespondentRepresentativeUtils.findRespondentByRepresentative(tmpCaseData, null))
                .isNull();
        // when representative not has value should return null
        assertThat(RespondentRepresentativeUtils.findRespondentByRepresentative(tmpCaseData, representative)).isNull();
        // when representative not has id, respondent name or respondent id should return null
        representative.setValue(RepresentedTypeR.builder().build());
        assertThat(RespondentRepresentativeUtils.findRespondentByRepresentative(tmpCaseData, representative)).isNull();
        // When respondent found by id should return that respondent
        tmpRespondentSumTypeItem.setId(RESPONDENT_ID_1);
        tmpRespondentSumTypeItem.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1)
                .representativeId(REPRESENTATIVE_ID_1).build());
        representative.getValue().setRespondentId(RESPONDENT_ID_1);
        assertThat(RespondentRepresentativeUtils.findRespondentByRepresentative(tmpCaseData, representative))
                .isEqualTo(tmpRespondentSumTypeItem);
        // When respondent found by name should return that respondent
        representative.getValue().setRespondentId(RESPONDENT_ID_2);
        representative.getValue().setRespRepName(RESPONDENT_NAME_1);
        assertThat(RespondentRepresentativeUtils.findRespondentByRepresentative(tmpCaseData, representative))
                .isEqualTo(tmpRespondentSumTypeItem);
        // when respondent found by representative id should return that respondent.
        representative.getValue().setRespRepName(RESPONDENT_NAME_2);
        representative.setId(REPRESENTATIVE_ID_1);
        assertThat(RespondentRepresentativeUtils.findRespondentByRepresentative(tmpCaseData, representative))
                .isEqualTo(tmpRespondentSumTypeItem);
        // when respondent not found by id, name and representative id should return null
        representative.setId(REPRESENTATIVE_ID_2);
        assertThat(RespondentRepresentativeUtils.findRespondentByRepresentative(tmpCaseData, representative)).isNull();
    }

    @Test
    void theFindRepresentativeById() {
        // when case data is empty should return null
        assertThat(RespondentRepresentativeUtils.findRepresentativeById(null, REPRESENTATIVE_ID_1)).isNull();
        // when case data does not have representative should return null
        CaseData caseData = new CaseData();
        assertThat(RespondentRepresentativeUtils.findRepresentativeById(caseData, REPRESENTATIVE_ID_1)).isNull();
        // when representative id is empty should return null
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        caseData.setRepCollection(List.of(representative));
        assertThat(RespondentRepresentativeUtils.findRepresentativeById(caseData, StringUtils.EMPTY)).isNull();
        // when representative is not a valid representative should return null
        assertThat(RespondentRepresentativeUtils.findRepresentativeById(caseData, REPRESENTATIVE_ID_1)).isNull();
        // when representative id is not equal to parameter id should return null
        representative.setId(REPRESENTATIVE_ID_2);
        representative.setValue(RepresentedTypeR.builder().build());
        assertThat(RespondentRepresentativeUtils.findRepresentativeById(caseData, REPRESENTATIVE_ID_1)).isNull();
        // when representative is equal to parameter id should return that representative
        representative.setId(REPRESENTATIVE_ID_1);
        assertThat(RespondentRepresentativeUtils.findRepresentativeById(caseData, REPRESENTATIVE_ID_1))
                .isEqualTo(representative);
    }

    @Test
    void theExtractValidRespondentRepresentativeOrganisationIds() {
        // when case data is empty should return an empty arraylist
        assertThat(RespondentRepresentativeUtils.extractValidRespondentRepresentativeOrganisationIds(null)).isEmpty();
        // when case data representative collection is empty should return an empty arraylist
        CaseData caseData = new CaseData();
        caseData.setRepCollection(new ArrayList<>());
        assertThat(RespondentRepresentativeUtils.extractValidRespondentRepresentativeOrganisationIds(caseData))
                .isEmpty();
        // when representative is not valid should return an empty arraylist
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        caseData.getRepCollection().add(representative);
        assertThat(RespondentRepresentativeUtils.extractValidRespondentRepresentativeOrganisationIds(caseData))
                .isEmpty();
        // when representative does not have organisation should return an empty list
        representative.setId(REPRESENTATIVE_ID_1);
        representative.setValue(RepresentedTypeR.builder().build());
        assertThat(RespondentRepresentativeUtils.extractValidRespondentRepresentativeOrganisationIds(caseData))
                .isEmpty();
        // when representative's organisation id is empty should return an empty list
        representative.getValue().setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.extractValidRespondentRepresentativeOrganisationIds(caseData))
                .isEmpty();
        // when representative has organisation id should return that id in a string list
        representative.getValue().getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.extractValidRespondentRepresentativeOrganisationIds(caseData))
                .isNotEmpty().isEqualTo(List.of(ORGANISATION_ID_1));
    }

    @Test
    void testTheClearRolesForRepresentatives() {
        // when case data is empty should not throw any exception
        RepresentedTypeRItem representative1 = RepresentedTypeRItem.builder().build();
        representative1.setId(REPRESENTATIVE_ID_1);
        representative1.setValue(RepresentedTypeR.builder().build());
        List<RepresentedTypeRItem> representatives = List.of(representative1);
        assertDoesNotThrow(() -> RespondentRepresentativeUtils.clearRolesForRepresentatives(null, representatives));
        // when case data rep collection is empty should not throw any exception
        CaseData caseData = new CaseData();
        assertDoesNotThrow(() -> RespondentRepresentativeUtils.clearRolesForRepresentatives(caseData, representatives));
        // when representatives are different should not throw any exception
        RepresentedTypeRItem representative2 = RepresentedTypeRItem.builder().build();
        representative2.setId(REPRESENTATIVE_ID_2);
        representative2.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_A).build());
        caseData.setRepCollection(List.of(representative2));
        assertDoesNotThrow(() -> RespondentRepresentativeUtils.clearRolesForRepresentatives(caseData, representatives));
        assertThat(representative2.getValue().getRole()).isEqualTo(ROLE_SOLICITOR_A);
        // when representatives are same should clear representative role
        representative2.setId(REPRESENTATIVE_ID_1);
        assertDoesNotThrow(() -> RespondentRepresentativeUtils.clearRolesForRepresentatives(caseData, representatives));
        assertThat(representative2.getValue().getRole()).isNull();
    }

    @Test
    void theHasRepresentatives() {
        // when case data is empty should return false
        assertThat(RespondentRepresentativeUtils.hasRepresentatives(null)).isFalse();
        // when representative collection is empty should return false
        CaseData caseData = new CaseData();
        assertThat(RespondentRepresentativeUtils.hasRepresentatives(caseData)).isFalse();
        // when representative collection size is zero should return false
        caseData.setRepCollection(new ArrayList<>());
        assertThat(RespondentRepresentativeUtils.hasRepresentatives(caseData)).isFalse();
        // when has representative should return true
        caseData.getRepCollection().add(RepresentedTypeRItem.builder().build());
        assertThat(RespondentRepresentativeUtils.hasRepresentatives(caseData)).isTrue();
    }

    @Test
    void theHasOrganisation() {
        // when representative is empty should return false
        assertThat(RespondentRepresentativeUtils.hasOrganisation(null)).isFalse();
        // when representative does not have organisation should return false
        RepresentedTypeR representative = RepresentedTypeR.builder().build();
        assertThat(RespondentRepresentativeUtils.hasOrganisation(representative)).isFalse();
        // when representative does not have organisation id should return false
        representative.setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.hasOrganisation(representative)).isFalse();
        // when has organisation with id should return true
        representative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.hasOrganisation(representative)).isTrue();
    }

    @Test
    void theIsEligibleForAccessRevocation() {
        // when representative is empty should return false
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().caseRole(ROLE_SOLICITOR_A).build();
        assertThat(RespondentRepresentativeUtils.isEligibleForAccessRevocation(null, caseUserAssignment,
                RESPONDENT_NAME_1)).isFalse();
        // when respondent name and case user assignments are empty should return false
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1).value(
                RepresentedTypeR.builder().myHmctsYesNo(YES).representativeEmailAddress(REPRESENTATIVE_EMAIL_1)
                        .respondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_1).build())
                        .build()).build();
        assertThat(RespondentRepresentativeUtils.isEligibleForAccessRevocation(representative, null,
                StringUtils.EMPTY)).isFalse();
        // when respondent name not equal to selected respondent name should return false
        representative.getValue().setRespRepName(RESPONDENT_NAME_2);
        assertThat(RespondentRepresentativeUtils.isEligibleForAccessRevocation(representative, null,
                RESPONDENT_NAME_1)).isFalse();
        // when respondent name equals to selected respondent name should return true
        representative.getValue().setRespRepName(RESPONDENT_NAME_1);
        assertThat(RespondentRepresentativeUtils.isEligibleForAccessRevocation(representative, null,
                RESPONDENT_NAME_1)).isTrue();
        // when case user assignment is not empty but not has role should return false
        representative.getValue().setRespRepName(RESPONDENT_NAME_2);
        caseUserAssignment.setCaseRole(null);
        assertThat(RespondentRepresentativeUtils.isEligibleForAccessRevocation(representative, caseUserAssignment,
                RESPONDENT_NAME_1)).isFalse();
        // when case user assignment role is not equal to representative role should return false
        caseUserAssignment.setCaseRole(ROLE_SOLICITOR_A);
        assertThat(RespondentRepresentativeUtils.isEligibleForAccessRevocation(representative, caseUserAssignment,
                RESPONDENT_NAME_1)).isFalse();
        // when case user assignment role is equal to representative role should return true
        representative.getValue().setRole(ROLE_SOLICITOR_A);
        assertThat(RespondentRepresentativeUtils.isEligibleForAccessRevocation(representative, caseUserAssignment,
                RESPONDENT_NAME_1)).isTrue();
    }
}
