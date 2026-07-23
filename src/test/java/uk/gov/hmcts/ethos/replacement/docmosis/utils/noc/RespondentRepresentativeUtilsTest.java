package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

final class RespondentRepresentativeUtilsTest {

    private static final String CASE_ID = "1234567890123456";
    private static final String SUBMISSION_REFERENCE = "1234567890123456";

    private static final String RESPONDENT_ID_1 = "1abc957b-e8f5-3487-84c5-a736eb6605b8";
    private static final String RESPONDENT_ID_2 = "2abc957b-e8f5-3487-84c5-a736eb6605b8";
    private static final String RESPONDENT_EMAIL_1 = "respondent1@testmail.com";
    private static final String REPRESENTATIVE_ID_1 = "6abc957b-e8f5-3487-84c5-a736eb6605b8";
    private static final String REPRESENTATIVE_ID_2 = "6abc957b-e8f5-3487-84c5-a736eb6605b9";
    private static final String REPRESENTATIVE_ID_IDAM_ID_1 = "6abc9888-e8888-3488-84c5-a736eb660588";
    private static final String REPRESENTATIVE_ID_IDAM_ID_2 = "6abc9999-e8999-3499-84c5-a736eb660599";
    private static final String REPRESENTATIVE_NAME_1 = "Representative name 1";
    private static final String REPRESENTATIVE_NAME_2 = "Representative name 2";
    private static final String REPRESENTATIVE_EMAIL_1 = "representative1@testmail.com";
    private static final String REPRESENTATIVE_EMAIL_1_CAPITALISED = "REPRESENTATIVE1@TESTMAIL.COM";
    private static final String REPRESENTATIVE_EMAIL_2 = "representative2@testmail.com";
    private static final String REPRESENTATIVE_1_PHONE = "07444518903";
    private static final String REPRESENTATIVE_1_ADDRESS_LINE_1 = "50 Tithe";
    private static final String REPRESENTATIVE_1_ADDRESS_LINE_2 = "Barn";
    private static final String REPRESENTATIVE_1_ADDRESS_LINE_3 = "Drive";
    private static final String REPRESENTATIVE_1_POST_TOWN = "Maidenhead";
    private static final String REPRESENTATIVE_1_COUNTY = "Berkshire";
    private static final String REPRESENTATIVE_1_COUNTRY = "United Kingdom";
    private static final String REPRESENTATIVE_1_POSTCODE = "SL6 2DE";

    private static final String ORGANISATION_ID_1 = "dummy12_organisation34_id56";
    private static final String ORGANISATION_ID_2 = "dummy65_organisation43_id21";

    private static final String ROLE_SOLICITOR_A = "[SOLICITORA]";
    private static final String ROLE_SOLICITOR_B = "[SOLICITORB]";
    private static final String ROLE_INVALID = "[INVALID]";

    private static final String EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND =
            "No representative found for case ID 1234567890123456.";
    private static final String EXPECTED_EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND =
            "Representative ID not found for case ID 1234567890123456.";
    private static final String EXPECTED_EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXISTS =
            "Representative details not found for representative ID 6abc957b-e8f5-3487-84c5-a736eb6605b8 "
                    + "in case 1234567890123456.";

    private static final String EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS = "Invalid representative exists.";
    private static final String EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES =
            "Respondent with name Respondent name 1 has more than one representative";

    private static final String RESPONDENT_NAME_1 = "Respondent name 1";
    private static final String RESPONDENT_NAME_2 = "Respondent name 2";

    @Test
    void theValidateRepresentative() {
        // when representative is null
        GenericServiceException gse = assertThrows(GenericServiceException.class,
                () -> RespondentRepresentativeUtils.validateRepresentative(null, SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_REPRESENTATIVE_NOT_FOUND);
        // when representative does not have id
        RepresentedTypeRItem representativeWithoutId = RepresentedTypeRItem.builder().build();
        gse = assertThrows(GenericServiceException.class,
                () -> RespondentRepresentativeUtils.validateRepresentative(representativeWithoutId,
                        SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND);
        // when respondent details not found
        RepresentedTypeRItem representativeWithoutDetails = RepresentedTypeRItem.builder().build();
        representativeWithoutDetails.setId(REPRESENTATIVE_ID_1);
        gse = assertThrows(GenericServiceException.class,
                () -> RespondentRepresentativeUtils.validateRepresentative(representativeWithoutDetails,
                        SUBMISSION_REFERENCE));
        assertThat(gse.getMessage()).isEqualTo(EXPECTED_EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXISTS);
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
        assertThat(errors).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when representative does not have a value should return ERROR_INVALID_REPRESENTATIVE_EXISTS.
        representatives = List.of(RepresentedTypeRItem.builder().build());
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when representative does not have a dynamic respondent name (dynamicRespRepName) should return
        // ERROR_INVALID_REPRESENTATIVE_EXISTS.
        representatives.getFirst().setValue(RepresentedTypeR.builder().build());
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when dynamic respondent name does not have any value should return
        // ERROR_INVALID_REPRESENTATIVE_EXISTS
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        representatives.getFirst().getValue().setDynamicRespRepName(dynamicFixedListType);
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(errors.getFirst()).isEqualTo(EXPECTED_ERROR_INVALID_REPRESENTATIVE_EXISTS);
        // when dynamic respondent name value does not have any label should return
        // ERROR_INVALID_REPRESENTATIVE_EXISTS
        DynamicValueType dynamicValueType = new DynamicValueType();
        representatives.getFirst().getValue().getDynamicRespRepName().setValue(dynamicValueType);
        errors = RespondentRepresentativeUtils.hasDuplicateRespondentNames(representatives);
        assertThat(errors).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
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
        assertThat(errors).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
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
    void theHasRespondentRepresentativeOrganisationChanged() {
        RepresentedTypeR oldRepresentative = RepresentedTypeR.builder().build();
        RepresentedTypeR newRepresentative = RepresentedTypeR.builder().build();
        // when both old and new representatives not have organisation should return false
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isFalse();
        // when old representative has organisation but new not should return true
        oldRepresentative.setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isTrue();
        // when new representative has organisation but old not should return true
        oldRepresentative.setRespondentOrganisation(null);
        newRepresentative.setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isTrue();
        // when both representatives not have organisation ids should return false
        oldRepresentative.setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isFalse();
        // when old representative has organisation id but new not should return true
        oldRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isTrue();
        // when new representative has organisation id but old not should return true
        oldRepresentative.getRespondentOrganisation().setOrganisationID(StringUtils.EMPTY);
        newRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isTrue();
        // when both of them have different organisation ids should return true
        oldRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        newRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_2);
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentativeOrganisationChanged(oldRepresentative,
                newRepresentative)).isTrue();
        // when both of them have same organisation ids should return false
        oldRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        newRepresentative.getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.hasRespondentRepresentativeOrganisationChanged(oldRepresentative,
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
    void theFindNewOrUpdatedRepresentatives() {
        List<RepresentedTypeRItem> newRepresentatives = new ArrayList<>();
        // when old representatives list is empty should return new representative list
        newRepresentatives.add(RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_2).build());
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                null)).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE).isEqualTo(newRepresentatives);
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
                oldRepresentatives)).hasSize(LoggerTestUtils.INTEGER_ONE);
        // when old representative and new representative does not represent same representative should return a list of
        // new representative
        oldRepresentative.setId(REPRESENTATIVE_ID_2);
        oldRepresentative.setValue(RepresentedTypeR.builder().build());
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                oldRepresentatives)).hasSize(LoggerTestUtils.INTEGER_ONE);
        // when old representative and new representative has different organisations should return a list of new
        // representative
        newRepresentative.getValue().setRespondentId(RESPONDENT_ID_1);
        newRepresentative.getValue().setRespondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_1)
                .build());
        oldRepresentative.getValue().setRespondentId(RESPONDENT_ID_1);
        oldRepresentative.getValue().setRespondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_2)
                .build());
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                oldRepresentatives)).hasSize(LoggerTestUtils.INTEGER_ONE);
        // when old representative and new representative has different emails should return a list of new
        // representative
        oldRepresentative.getValue().setRespondentOrganisation(Organisation.builder().organisationID(ORGANISATION_ID_1)
                .build());
        oldRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_2);
        newRepresentative.getValue().setRepresentativeEmailAddress(REPRESENTATIVE_EMAIL_1);
        assertThat(RespondentRepresentativeUtils.findNewOrUpdatedRepresentatives(newRepresentatives,
                oldRepresentatives)).hasSize(LoggerTestUtils.INTEGER_ONE);
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
        assertThat(expectedRepresentatives).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE);
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
        representative.setId(REPRESENTATIVE_ID_1);
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
        // when representative is not valid should return an empty arraylist
        CaseData caseData = new CaseData();
        caseData.setRepCollection(new ArrayList<>());
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
        assertDoesNotThrow(() -> RespondentRepresentativeUtils.clearRolesForRepresentatives(null,
                representatives));
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

    @Test
    void theHasValidAssignmentContext() {
        // when representative list is empty should return false
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId(SUBMISSION_REFERENCE);
        caseDetails.setCaseId(CASE_ID);
        assertThat(RespondentRepresentativeUtils.hasValidAssignmentContext(representatives, caseDetails)).isFalse();
        // when case details is empty should return false
        representatives.add(RepresentedTypeRItem.builder().build());
        assertThat(RespondentRepresentativeUtils.hasValidAssignmentContext(representatives, null)).isFalse();
        // when case details not have case id should return false
        caseDetails.setCaseId(StringUtils.EMPTY);
        assertThat(RespondentRepresentativeUtils.hasValidAssignmentContext(representatives, caseDetails)).isFalse();
        // when case details have case id should return true
        caseDetails.setCaseId(SUBMISSION_REFERENCE);
        caseDetails.setCaseId(CASE_ID);
        assertThat(RespondentRepresentativeUtils.hasValidAssignmentContext(representatives, caseDetails)).isTrue();
    }

    @Test
    void theIsMatchingValidRepresentative() {
        RepresentedTypeRItem oldRepresentative = RepresentedTypeRItem.builder().value(RepresentedTypeR.builder()
                .build()).build();
        RepresentedTypeRItem newRepresentative = RepresentedTypeRItem.builder().value(RepresentedTypeR.builder()
                .build()).build();
        // when new representative is not valid should return false
        assertThat(RespondentRepresentativeUtils.isMatchingValidRepresentative(oldRepresentative, newRepresentative))
                .isFalse();
        // when new representative is valid but old and new representatives not represent same respondent should return
        // false
        newRepresentative.setId(REPRESENTATIVE_ID_1);
        newRepresentative.setValue(RepresentedTypeR.builder().respondentId(RESPONDENT_ID_1).build());
        assertThat(RespondentRepresentativeUtils.isMatchingValidRepresentative(oldRepresentative, newRepresentative))
                .isFalse();
        // when new and old representatives represent same respondent should return true
        oldRepresentative.setId(REPRESENTATIVE_ID_2);
        oldRepresentative.setValue(RepresentedTypeR.builder().respondentId(RESPONDENT_ID_1).build());
        assertThat(RespondentRepresentativeUtils.isMatchingValidRepresentative(oldRepresentative, newRepresentative))
                .isTrue();
    }

    @Test
    void theFindRepresentativeByRespondentName() {
        // when respondent name is empty should return null
        CaseData caseData = new CaseData();
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRespondentName(caseData, StringUtils.EMPTY))
                .isNull();
        // when case data has representative with respondent name should return that representative
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1).value(
                RepresentedTypeR.builder().respRepName(RESPONDENT_NAME_1).build()).build();
        caseData.setRepCollection(List.of(representative));
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRespondentName(caseData, RESPONDENT_NAME_1))
                .isEqualTo(representative);
    }

    @Test
    void theFindRepresentativeByRespondentId() {
        // when respondent id is empty should return null
        CaseData caseData = new CaseData();
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRespondentId(caseData, StringUtils.EMPTY))
                .isNull();
        // when case data has representative with respondent id should return that representative
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1).value(
                RepresentedTypeR.builder().respondentId(RESPONDENT_ID_1).build()).build();
        caseData.setRepCollection(List.of(representative));
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRespondentId(caseData, RESPONDENT_ID_1))
                .isEqualTo(representative);
    }

    @Test
    void theFindRepresentativesByOrganisationId() {
        // when representative is not valid should return empty list
        CaseData caseData = new CaseData();
        RepresentedTypeRItem representative = new RepresentedTypeRItem();
        representative.setValue(RepresentedTypeR.builder().build());
        caseData.setRepCollection(List.of(representative));
        assertThat(RespondentRepresentativeUtils.findRepresentativesByOrganisationId(caseData, ORGANISATION_ID_1))
                .hasSize(LoggerTestUtils.INTEGER_ZERO);
        // when representative does not have organisation should return empty list
        representative.setId(REPRESENTATIVE_ID_1);
        assertThat(RespondentRepresentativeUtils.findRepresentativesByOrganisationId(caseData, ORGANISATION_ID_1))
                .hasSize(LoggerTestUtils.INTEGER_ZERO);
        // when representative does not have organisation id should return empty list
        representative.getValue().setRespondentOrganisation(Organisation.builder().build());
        assertThat(RespondentRepresentativeUtils.findRepresentativesByOrganisationId(caseData, ORGANISATION_ID_1))
                .hasSize(LoggerTestUtils.INTEGER_ZERO);
        // when representative organisation id not equals to the parameter organisation id should return empty list
        representative.getValue().getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_2);
        assertThat(RespondentRepresentativeUtils.findRepresentativesByOrganisationId(caseData, ORGANISATION_ID_1))
                .hasSize(LoggerTestUtils.INTEGER_ZERO);
        // when representative organisation id equals to the parameter organisation id should return list with
        // representative
        representative.getValue().getRespondentOrganisation().setOrganisationID(ORGANISATION_ID_1);
        assertThat(RespondentRepresentativeUtils.findRepresentativesByOrganisationId(caseData, ORGANISATION_ID_1))
                .hasSize(LoggerTestUtils.INTEGER_ONE).contains(representative);
    }

    @Test
    void theFindRepresentativeByRole() {
        // when case data is empty should return null
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(null, ROLE_SOLICITOR_A)).isNull();
        // when case data representative collection is empty should return null
        CaseData caseData = new CaseData();
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(caseData, ROLE_SOLICITOR_A)).isNull();
        // when role is empty should return null
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1).value(
                RepresentedTypeR.builder().role(ROLE_SOLICITOR_B).build()).build();
        caseData.setRepCollection(List.of(representative));
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(caseData, StringUtils.EMPTY)).isNull();
        // when role of representative is different from role parameter should return null
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(caseData, ROLE_SOLICITOR_A)).isNull();
        // when case data has representative with role should return that representative
        representative.getValue().setRole(ROLE_SOLICITOR_A);
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(caseData, ROLE_SOLICITOR_A))
                .isEqualTo(representative);
        // when respondent not found by role should return null
        representative.getValue().setRole(null);
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(caseData, ROLE_SOLICITOR_A)).isNull();
        // when respondent does not have value should return null
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        caseData.setRespondentCollection(List.of(respondent));
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(caseData, ROLE_SOLICITOR_A)).isNull();
        // when representative not found by respondent id should return null
        respondent.setId(RESPONDENT_ID_1);
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(caseData, ROLE_SOLICITOR_A)).isNull();
        // when representative found and valid should return that representative
        representative.getValue().setRespondentId(RESPONDENT_ID_1);
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(caseData, ROLE_SOLICITOR_A))
                .isEqualTo(representative);
        // when representative found by respondent name should return that representative
        NoticeOfChangeAnswers noticeOfChangeAnswers = NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_1)
                .build();
        caseData.setNoticeOfChangeAnswers0(noticeOfChangeAnswers);
        representative = RepresentedTypeRItem.builder().id(REPRESENTATIVE_ID_1).value(
                        RepresentedTypeR.builder().respRepName(RESPONDENT_NAME_1).respondentId(RESPONDENT_ID_2).build())
                .build();
        caseData.setRepCollection(List.of(representative));
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(caseData, ROLE_SOLICITOR_A))
                .isEqualTo(representative);
        // when representative not found by role, respondent id and respondent name should return null
        representative.getValue().setRespRepName(RESPONDENT_NAME_2);
        assertThat(RespondentRepresentativeUtils.findRepresentativeByRole(caseData, ROLE_SOLICITOR_A)).isNull();

    }

    @Test
    void theFindRepresentativeInListByRoleOrRespondentName() {
        // when representative not found in case data should return null
        CaseData caseData = new CaseData();
        caseData.setRepCollection(new ArrayList<>());
        assertThat(RespondentRepresentativeUtils.findRepresentativeInListByRoleOrRespondentName(caseData,
                ROLE_SOLICITOR_A, new ArrayList<>())).isNull();
        // when representative in list is not valid should return null
        RepresentedTypeRItem representative1 = new  RepresentedTypeRItem();
        representative1.setId(REPRESENTATIVE_ID_1);
        representative1.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_A).build());
        caseData.setRepCollection(List.of(representative1));
        RepresentedTypeRItem representative2 = new  RepresentedTypeRItem();
        List<RepresentedTypeRItem> representedTypeRItems = List.of(representative2);
        assertThat(RespondentRepresentativeUtils.findRepresentativeInListByRoleOrRespondentName(caseData,
                ROLE_SOLICITOR_A, representedTypeRItems)).isNull();
        // when representative in case data has different representative id should return null
        representative2.setId(REPRESENTATIVE_ID_2);
        representative2.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_A).build());
        assertThat(RespondentRepresentativeUtils.findRepresentativeInListByRoleOrRespondentName(caseData,
                ROLE_SOLICITOR_A, representedTypeRItems)).isNull();
        // when representative in case data has same representative id in list should return representative
        representative2.setId(REPRESENTATIVE_ID_1);
        assertThat(RespondentRepresentativeUtils.findRepresentativeInListByRoleOrRespondentName(caseData,
                ROLE_SOLICITOR_A, representedTypeRItems)).isNotNull().isEqualTo(representative1);
    }

    @Test
    void theRemoveRespondentRepresentatives() {
        // when representative list is empty should not remove any representative
        RepresentedTypeRItem representative1 = new  RepresentedTypeRItem();
        representative1.setId(REPRESENTATIVE_ID_1);
        representative1.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_A).build());
        CaseData caseData = new CaseData();
        caseData.setRepCollection(List.of(representative1));
        RespondentRepresentativeUtils.removeRespondentRepresentatives(caseData, new  ArrayList<>());
        assertThat(caseData.getRepCollection()).hasSize(LoggerTestUtils.INTEGER_ONE);
        // when respondent not found should not reset respondent representation
        RespondentSumTypeItem  respondent = new RespondentSumTypeItem();
        respondent.setId(RESPONDENT_ID_1);
        RespondentSumType respondentValue = RespondentSumType.builder().respondentName(RESPONDENT_NAME_1)
                .representativeId(REPRESENTATIVE_ID_1).represented(YES).representativeRemoved(NO).build();
        respondent.setValue(respondentValue);
        caseData.setRespondentCollection(List.of(respondent));
        RepresentedTypeRItem representative2 = new  RepresentedTypeRItem();
        representative2.setId(REPRESENTATIVE_ID_2);
        representative2.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_A).respondentId(RESPONDENT_ID_2)
                .build());
        List<RepresentedTypeRItem> representedTypeRItems = List.of(representative2);
        RespondentRepresentativeUtils.removeRespondentRepresentatives(caseData, representedTypeRItems);
        assertThat(caseData.getRepCollection()).hasSize(LoggerTestUtils.INTEGER_ONE);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isEqualTo(REPRESENTATIVE_ID_1);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresented()).isEqualTo(YES);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeRemoved()).isEqualTo(NO);
        // when respondent found should reset respondent representation
        representative2.getValue().setRespondentId(RESPONDENT_ID_1);
        representative2.setId(REPRESENTATIVE_ID_1);
        RespondentRepresentativeUtils.removeRespondentRepresentatives(caseData, representedTypeRItems);
        assertThat(caseData.getRepCollection()).hasSize(LoggerTestUtils.INTEGER_ZERO);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeId()).isNull();
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresented()).isEqualTo(NO);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeRemoved()).isEqualTo(YES);
    }

    @Test
    void theFindCaseUserAssignmentsByRepresentativeIdamId() {
        // when representative not found should return empty list
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().userId(REPRESENTATIVE_ID_1).build();
        List<CaseUserAssignment> caseUserAssignments = List.of(caseUserAssignment);
        assertThat(RespondentRepresentativeUtils.findCaseUserAssignmentsByRepresentativeIdamId(caseUserAssignments,
                REPRESENTATIVE_ID_2)).isEmpty();
        // when representative found should return a list with that representative
        assertThat(RespondentRepresentativeUtils.findCaseUserAssignmentsByRepresentativeIdamId(caseUserAssignments,
                REPRESENTATIVE_ID_1)).hasSize(LoggerTestUtils.INTEGER_ONE).contains(caseUserAssignment);
    }

    @Test
    void theUpdateRepresentativeContactDetails() {
        CaseData caseData = new CaseData();
        caseData.setEt3ResponsePhone(REPRESENTATIVE_1_PHONE);
        Address address = new Address();
        address.setAddressLine1(REPRESENTATIVE_1_ADDRESS_LINE_1);
        address.setAddressLine2(REPRESENTATIVE_1_ADDRESS_LINE_2);
        address.setAddressLine3(REPRESENTATIVE_1_ADDRESS_LINE_3);
        address.setPostTown(REPRESENTATIVE_1_POST_TOWN);
        address.setCounty(REPRESENTATIVE_1_COUNTY);
        address.setCountry(REPRESENTATIVE_1_COUNTRY);
        address.setPostCode(REPRESENTATIVE_1_POSTCODE);
        caseData.setEt3ResponseAddress(address);
        // when case data representative collection is empty should not update any representative contact details
        List<String> roles = new ArrayList<>();
        RespondentRepresentativeUtils.updateRepresentativeContactDetails(caseData, roles);
        assertThat(caseData.getRepCollection()).isNull();
        // when represented respondent indexes is empty should not update any representative contact details
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().build())
                .build();
        caseData.setRepCollection(List.of(representative));
        RespondentRepresentativeUtils.updateRepresentativeContactDetails(caseData, roles);
        assertThat(representative.getValue().getRepresentativePhoneNumber()).isNull();
        assertThat(representative.getValue().getRepresentativeAddress()).isNull();
        // when case data respondent collection is empty should not update representative contact details
        roles.add(ROLE_SOLICITOR_A);
        RespondentRepresentativeUtils.updateRepresentativeContactDetails(caseData, roles);
        assertThat(representative.getValue().getRepresentativePhoneNumber()).isNull();
        assertThat(representative.getValue().getRepresentativeAddress()).isNull();
        // when represented respondent indexes has any value greater than respondent collection size should not update
        // representative contact details
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        caseData.setRespondentCollection(List.of(respondent));
        RespondentRepresentativeUtils.updateRepresentativeContactDetails(caseData, roles);
        // when respondent does not have a valid id should not update representative contact details
        roles.clear();
        roles.add(ROLE_SOLICITOR_A);
        RespondentRepresentativeUtils.updateRepresentativeContactDetails(caseData, roles);
        assertThat(representative.getValue().getRepresentativePhoneNumber()).isNull();
        assertThat(representative.getValue().getRepresentativeAddress()).isNull();
        // when representative in the rep collection is not a valid representative should not update contact details
        respondent.setId(RESPONDENT_ID_1);
        RespondentRepresentativeUtils.updateRepresentativeContactDetails(caseData, roles);
        assertThat(representative.getValue().getRepresentativePhoneNumber()).isNull();
        assertThat(representative.getValue().getRepresentativeAddress()).isNull();
        // when representative respondent id does not match with respondent id should not update representative contact
        // details
        representative.setId(REPRESENTATIVE_ID_1);
        representative.setValue(RepresentedTypeR.builder().respondentId(RESPONDENT_ID_2).build());
        RespondentRepresentativeUtils.updateRepresentativeContactDetails(caseData, roles);
        assertThat(representative.getValue().getRepresentativePhoneNumber()).isNull();
        assertThat(representative.getValue().getRepresentativeAddress()).isNull();
        // when representative and respondent matches, should update representative contact details
        representative.setValue(RepresentedTypeR.builder().respondentId(RESPONDENT_ID_1).build());
        RespondentRepresentativeUtils.updateRepresentativeContactDetails(caseData, roles);
        assertThat(representative.getValue().getRepresentativePhoneNumber()).isEqualTo(REPRESENTATIVE_1_PHONE);
        assertThat(representative.getValue().getRepresentativeAddress()).isEqualTo(address);
    }

    @Test
    void theUpdateET3ResponseContactDetails() {
        // when case data representative collection is empty should not update et3 response address and phone number.
        List<String> roles = new ArrayList<>();
        CaseData caseData = new CaseData();
        RespondentRepresentativeUtils.updateET3ResponseContactDetails(caseData, roles);
        assertThat(caseData.getEt3ResponseAddress()).isNull();
        assertThat(caseData.getEt3ResponsePhone()).isNull();
        // when roles are empty should not update et3 response address and phone number.
        RepresentedTypeRItem representative = new RepresentedTypeRItem();
        caseData.setRepCollection(List.of(representative));
        RespondentRepresentativeUtils.updateET3ResponseContactDetails(caseData, roles);
        assertThat(caseData.getEt3ResponseAddress()).isNull();
        assertThat(caseData.getEt3ResponsePhone()).isNull();
        // when case data respondent collection is empty should not update et3 response address and phone number.
        roles.add(ROLE_SOLICITOR_B);
        RespondentRepresentativeUtils.updateET3ResponseContactDetails(caseData, roles);
        assertThat(caseData.getEt3ResponseAddress()).isNull();
        assertThat(caseData.getEt3ResponsePhone()).isNull();
        // when respondent for the role not found should not update et3 response address and phone number
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        caseData.setRespondentCollection(List.of(respondent));
        RespondentRepresentativeUtils.updateET3ResponseContactDetails(caseData, roles);
        assertThat(caseData.getEt3ResponseAddress()).isNull();
        assertThat(caseData.getEt3ResponsePhone()).isNull();
        // when respondent does not have a valid id should not update et3 response address and phone number
        roles.clear();
        roles.add(ROLE_SOLICITOR_A);
        RespondentRepresentativeUtils.updateET3ResponseContactDetails(caseData, roles);
        assertThat(caseData.getEt3ResponseAddress()).isNull();
        assertThat(caseData.getEt3ResponsePhone()).isNull();
        // when representative in the rep collection is not a valid representative should not update et3 response
        // address and phone number
        respondent.setId(RESPONDENT_ID_1);
        RespondentRepresentativeUtils.updateET3ResponseContactDetails(caseData, roles);
        assertThat(caseData.getEt3ResponseAddress()).isNull();
        assertThat(caseData.getEt3ResponsePhone()).isNull();
        // when representative respondent id does not match with respondent id should not update should not update et3
        // response address and phone number
        Address address = new Address();
        address.setAddressLine1(REPRESENTATIVE_1_ADDRESS_LINE_1);
        address.setAddressLine2(REPRESENTATIVE_1_ADDRESS_LINE_2);
        address.setAddressLine3(REPRESENTATIVE_1_ADDRESS_LINE_3);
        address.setPostTown(REPRESENTATIVE_1_POST_TOWN);
        address.setCounty(REPRESENTATIVE_1_COUNTY);
        address.setCountry(REPRESENTATIVE_1_COUNTRY);
        address.setPostCode(REPRESENTATIVE_1_POSTCODE);
        RepresentedTypeR representativeValue = RepresentedTypeR.builder().build();
        representativeValue.setRepresentativeAddress(address);
        representativeValue.setRepresentativePhoneNumber(REPRESENTATIVE_1_PHONE);
        representativeValue.setRespondentId(RESPONDENT_ID_2);
        representative.setValue(RepresentedTypeR.builder().respondentId(RESPONDENT_ID_1).build());
        representative.setValue(representativeValue);
        representative.setId(REPRESENTATIVE_ID_1);
        RespondentRepresentativeUtils.updateET3ResponseContactDetails(caseData, roles);
        assertThat(caseData.getEt3ResponseAddress()).isNull();
        assertThat(caseData.getEt3ResponsePhone()).isNull();
        // when representative and respondent matches, should update et3 response address and phone number
        representative.getValue().setRespondentId(RESPONDENT_ID_1);
        RespondentRepresentativeUtils.updateET3ResponseContactDetails(caseData, roles);
        assertThat(caseData.getEt3ResponseAddress()).isEqualTo(address);
        assertThat(caseData.getEt3ResponsePhone()).isEqualTo(REPRESENTATIVE_1_PHONE);
    }

    @Test
    void theFindRepresentativeByIdamId() {
        CaseData caseData = new CaseData();
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        caseData.setRepCollection(List.of(representative));
        // when representative in case data is not valid should return null
        assertThat(RespondentRepresentativeUtils.findRepresentativeByIdamId(caseData, REPRESENTATIVE_ID_IDAM_ID_1))
                .isNull();
        // when representative idam id not matches with given idam id should return false
        representative.setId(REPRESENTATIVE_ID_1);
        RepresentedTypeR representativeValue = RepresentedTypeR.builder().idamId(REPRESENTATIVE_ID_IDAM_ID_2).build();
        representative.setValue(representativeValue);
        assertThat(RespondentRepresentativeUtils.findRepresentativeByIdamId(caseData, REPRESENTATIVE_ID_IDAM_ID_1))
                .isNull();
        // when representative found should return found representative
        representativeValue.setIdamId(REPRESENTATIVE_ID_IDAM_ID_1);
        assertThat(RespondentRepresentativeUtils.findRepresentativeByIdamId(caseData, REPRESENTATIVE_ID_IDAM_ID_1))
                .isEqualTo(representative);
    }

    @Test
    void theFindRepresentativeByIdamIdOrRole() {
        // when case data is empty should return null
        assertThat(RespondentRepresentativeUtils.findRepresentativeByIdamIdOrRole(null,
                REPRESENTATIVE_ID_IDAM_ID_1, ROLE_SOLICITOR_A)).isNull();
        // when case data not has representative collection should return null
        CaseData caseData = new CaseData();
        assertThat(RespondentRepresentativeUtils.findRepresentativeByIdamIdOrRole(caseData, REPRESENTATIVE_ID_IDAM_ID_1,
                ROLE_SOLICITOR_A)).isNull();
        // when role is not a respondent representative role and idam id values are empty should return null
        RepresentedTypeR representativeValue =  RepresentedTypeR.builder().build();
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().value(representativeValue).build();
        caseData.setRepCollection(List.of(representative));
        assertThat(RespondentRepresentativeUtils.findRepresentativeByIdamIdOrRole(caseData, StringUtils.EMPTY,
                StringUtils.EMPTY)).isNull();
        // when representative not found by idam id and role should return null
        assertThat(RespondentRepresentativeUtils.findRepresentativeByIdamIdOrRole(caseData, REPRESENTATIVE_ID_IDAM_ID_1,
                ROLE_SOLICITOR_A)).isNull();
        // when representative found by idam id should return that representative
        representativeValue.setIdamId(REPRESENTATIVE_ID_IDAM_ID_1);
        representative.setId(REPRESENTATIVE_ID_1);
        assertThat(RespondentRepresentativeUtils.findRepresentativeByIdamIdOrRole(caseData, REPRESENTATIVE_ID_IDAM_ID_1,
                ROLE_SOLICITOR_A)).isEqualTo(representative);
        // when representative found by role should return that representative
        representativeValue.setIdamId(StringUtils.EMPTY);
        representativeValue.setRole(ROLE_SOLICITOR_A);
        assertThat(RespondentRepresentativeUtils.findRepresentativeByIdamIdOrRole(caseData, REPRESENTATIVE_ID_IDAM_ID_1,
                ROLE_SOLICITOR_A)).isEqualTo(representative);
    }

    @Test
    void theIsCaseUserAssignmentForRepresentativeByRespondentName() {
        // when representative is not a valid respondent representative should return false
        CaseData caseData = new CaseData();
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentName(caseData, representative, caseUserAssignment))
                .isFalse();
        // when representative does not have respondent name should return false
        RepresentedTypeR representativeValue = RepresentedTypeR.builder().build();
        representative.setValue(representativeValue);
        representative.setId(REPRESENTATIVE_ID_1);
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentName(caseData, representative, caseUserAssignment))
                .isFalse();
        // when case user assignment is empty should return false
        representativeValue.setRespRepName(RESPONDENT_NAME_1);
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentName(caseData, representative, null)).isFalse();
        // when case user assignment does not have role should return false
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentName(caseData, representative, caseUserAssignment))
                .isFalse();
        // when role index is -1 should return false
        caseUserAssignment.setCaseRole(ROLE_SOLICITOR_A);
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentName(caseData, representative, caseUserAssignment))
                .isFalse();
        // when role not matches with case user assignment role should return false
        NoticeOfChangeAnswers noticeOfChangeAnswers = NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_1)
                .build();
        caseData.setNoticeOfChangeAnswers1(noticeOfChangeAnswers);
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentName(caseData, representative, caseUserAssignment))
                .isFalse();
        // when role matches with case user assignment role should return true
        caseData.setNoticeOfChangeAnswers1(null);
        caseData.setNoticeOfChangeAnswers0(noticeOfChangeAnswers);
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentName(caseData, representative, caseUserAssignment))
                .isTrue();
    }

    @Test
    void theIsCaseUserAssignmentForRepresentativeByRespondentId() {
        // when representative is not a valid respondent representative should return false
        CaseData caseData = new CaseData();
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().build();
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentId(caseData, representative, caseUserAssignment))
                .isFalse();
        // when representative does not have respondent id should return false
        RepresentedTypeR representativeValue = RepresentedTypeR.builder().build();
        representative.setValue(representativeValue);
        representative.setId(REPRESENTATIVE_ID_1);
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentId(caseData, representative, caseUserAssignment))
                .isFalse();
        // when case user assignment is empty should return false
        representativeValue.setRespondentId(RESPONDENT_ID_2);
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentId(caseData, representative, null)).isFalse();
        // when case user assignment does not have role should return false
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentId(caseData, representative, caseUserAssignment))
                .isFalse();
        // when role index is -1 should return false
        caseUserAssignment.setCaseRole(ROLE_INVALID);
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentId(caseData, representative, caseUserAssignment))
                .isFalse();
        // when respondent not found should return false
        caseUserAssignment.setCaseRole(ROLE_SOLICITOR_A);
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentId(caseData, representative, caseUserAssignment))
                .isFalse();
        // when respondent id not matches should return false
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        respondent.setId(RESPONDENT_ID_1);
        caseData.setRespondentCollection(List.of(respondent));
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentId(caseData, representative, caseUserAssignment))
                .isFalse();
        // when respondent id matches should return true
        representativeValue.setRespondentId(RESPONDENT_ID_1);
        assertThat(RespondentRepresentativeUtils
                .isCaseUserAssignmentForRepresentativeByRespondentId(caseData, representative, caseUserAssignment))
                .isTrue();
    }

    @Test
    void theFindManualAssignments() {
        // when case user assignments collection is empty should return an empty list
        CaseData caseData = new CaseData();
        List<CaseUserAssignment> caseUserAssignments = new ArrayList<>();
        RepresentedTypeRItem representative1 = RepresentedTypeRItem.builder().build();
        assertThat(RespondentRepresentativeUtils.findManualAssignments(caseData, caseUserAssignments,
                representative1)).isEmpty();
        // when representative is not a valid representative1 should return an empty list
        CaseUserAssignment caseUserAssignment = CaseUserAssignment.builder().userId(REPRESENTATIVE_ID_IDAM_ID_1)
                .caseId(SUBMISSION_REFERENCE).caseRole(ROLE_SOLICITOR_A).organisationId(ORGANISATION_ID_1).build();
        caseUserAssignments.add(caseUserAssignment);
        assertThat(RespondentRepresentativeUtils.findManualAssignments(caseData, caseUserAssignments,
                representative1)).isEmpty();
        // when representative not found should return an empty list
        representative1.setId(REPRESENTATIVE_ID_1);
        representative1.setValue(RepresentedTypeR.builder().idamId(REPRESENTATIVE_ID_IDAM_ID_1).role(ROLE_SOLICITOR_A)
                .build());
        assertThat(RespondentRepresentativeUtils.findManualAssignments(caseData, caseUserAssignments,
                representative1)).isEmpty();
        // when representative found but representative ids not equal should return empty list.
        RepresentedTypeRItem representative2 = RepresentedTypeRItem.builder().value(RepresentedTypeR.builder()
                .idamId(REPRESENTATIVE_ID_IDAM_ID_2).role(ROLE_SOLICITOR_A).build()).build();
        representative2.setId(REPRESENTATIVE_ID_2);
        caseData.setRepCollection(List.of(representative2));
        assertThat(RespondentRepresentativeUtils.findManualAssignments(caseData, caseUserAssignments,
                representative1)).isEmpty();
        // when representative found and representative ids matches should return that representative
        caseData.setRepCollection(List.of(representative1));
        assertThat(RespondentRepresentativeUtils.findManualAssignments(caseData, caseUserAssignments,
                representative1)).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE)
                .isEqualTo(List.of(caseUserAssignment));
    }

    @Test
    void theFindAutoAssignments() {
        // when case user assignments is empty should return an empty list
        List<CaseUserAssignment> caseUserAssignments = new ArrayList<>();
        List<CaseUserAssignment> manualAssignments = new ArrayList<>();
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        assertThat(RespondentRepresentativeUtils.findAutoAssignments(representative, manualAssignments,
                caseUserAssignments)).isEmpty();
        // when representative is not a valid representative should return empty list
        CaseUserAssignment caseUserAssignmentMatchingRole = CaseUserAssignment.builder().caseRole(ROLE_SOLICITOR_A)
                .userId(REPRESENTATIVE_ID_IDAM_ID_1).organisationId(ORGANISATION_ID_1).caseId(SUBMISSION_REFERENCE)
                .build();
        caseUserAssignments.add(caseUserAssignmentMatchingRole);
        assertThat(RespondentRepresentativeUtils.findAutoAssignments(representative, manualAssignments,
                caseUserAssignments)).isEmpty();
        // when case user assignments has matching role with manual case user assignments should return that assignment
        representative.setId(REPRESENTATIVE_ID_1);
        representative.setValue(RepresentedTypeR.builder().idamId(REPRESENTATIVE_ID_IDAM_ID_2).role(ROLE_SOLICITOR_A)
                .build());
        manualAssignments.add(caseUserAssignmentMatchingRole);
        assertThat(RespondentRepresentativeUtils.findAutoAssignments(representative, manualAssignments,
                caseUserAssignments)).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE)
                .isEqualTo(List.of(caseUserAssignmentMatchingRole));
        // when representative idam id matches with case user assignments idam id should add that assignment
        manualAssignments.clear();
        representative.getValue().setIdamId(REPRESENTATIVE_ID_IDAM_ID_1);
        assertThat(RespondentRepresentativeUtils.findAutoAssignments(representative, manualAssignments,
                caseUserAssignments)).isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE)
                .isEqualTo(List.of(caseUserAssignmentMatchingRole));
    }

    @Test
    void theFindRespondentsByRepresentatives() {
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setId(RESPONDENT_ID_1);
        RespondentSumType respondentValue = RespondentSumType.builder().respondentName(RESPONDENT_NAME_1)
                .representativeId(REPRESENTATIVE_ID_1).respondentEmail(RESPONDENT_EMAIL_1).build();
        respondent.setValue(respondentValue);
        List<RespondentSumTypeItem>  respondents = new ArrayList<>();
        respondents.add(respondent);
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(respondents);
        // when representative list is empty should return an empty list
        List<RepresentedTypeRItem> representatives = new ArrayList<>();
        assertThat(RespondentRepresentativeUtils.findRespondentsByRepresentatives(caseData, representatives))
                .isEmpty();
        // when respondent not found should return an empty list
        RepresentedTypeRItem representative = new RepresentedTypeRItem();
        representatives.add(representative);
        assertThat(RespondentRepresentativeUtils.findRespondentsByRepresentatives(caseData, representatives))
                .isEmpty();
        // when respondent found should return that respondent
        representative.setId(REPRESENTATIVE_ID_1);
        representative.setValue(RepresentedTypeR.builder().respondentId(RESPONDENT_ID_1).build());
        assertThat(RespondentRepresentativeUtils.findRespondentsByRepresentatives(caseData, representatives))
                .isNotEmpty().hasSize(LoggerTestUtils.INTEGER_ONE).isEqualTo(List.of(respondent));
    }
}
