package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationApprovalStatus.APPROVED;

final class NocUtilsTest {

    private static final String EXPECTED_ERROR_INVALID_RESPONDENT_EXISTS = "Invalid respondent exists.";
    private static final String EXPECTED_ERROR_SELECTED_RESPONDENT_NOT_FOUND =
            "Selected respondent with name Respondent Name Two not found.";
    private static final String EXPECTED_ERROR_INVALID_CASE_DATA = "Invalid case data";
    private static final String EXPECTED_ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES =
            "Respondent with name Respondent Name One has more than one representative";

    private static final String EXPECTED_EXCEPTION_CALLBACK_REQUEST_NOT_FOUND = "Callback request not found.";
    private static final String EXPECTED_EXCEPTION_NEW_CASE_DETAILS_NOT_FOUND =
            "New case details are missing.";
    private static final String EXPECTED_EXCEPTION_OLD_CASE_DETAILS_NOT_FOUND =
            "Old case details are missing.";
    private static final String EXPECTED_EXCEPTION_NEW_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND =
            "New case details are missing the submission reference.";
    private static final String EXPECTED_EXCEPTION_OLD_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND =
            "Old case details are missing the submission reference.";
    public static final String EXPECTED_EXCEPTION_OLD_AND_NEW_SUBMISSION_REFERENCES_NOT_EQUAL =
            "Old and new submission references do not match (old: 9876543210654321, new: 1234567890123456).";
    private static final String EXPECTED_EXCEPTION_NEW_CASE_DATA_NOT_FOUND =
            "New case data is missing for case ID 1234567890123456.";
    private static final String EXPECTED_EXCEPTION_OLD_CASE_DATA_NOT_FOUND =
            "Old case data is missing for case ID 1234567890123456.";
    private static final String EXPECTED_EXCEPTION_NEW_RESPONDENT_COLLECTION_IS_EMPTY =
            "New respondent collection is missing for case ID 1234567890123456.";
    private static final String EXPECTED_EXCEPTION_OLD_RESPONDENT_COLLECTION_IS_EMPTY =
            "Old respondent collection is missing for case ID 1234567890123456.";
    private static final String EXPECTED_EXCEPTION_OLD_AND_NEW_RESPONDENTS_ARE_DIFFERENT =
            "Old and new respondent collections contain different respondents for case ID 1234567890123456.";

    private static final String NOC_WARNING = "Dummy NOC warning";

    private static final String REPRESENTATIVE_NAME = "Representative Name";
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
    private static final String RESPONDENT_ID_ONE = "dummy_respondent_id_1";
    private static final String RESPONDENT_ID_TWO = "dummy_respondent_id_2";
    private static final String DUMMY_REPRESENTATIVE_ID = "dummy_representative_id";
    private static final String DUMMY_CASE_SUBMISSION_REFERENCE_1 = "1234567890123456";
    private static final String DUMMY_CASE_SUBMISSION_REFERENCE_2 = "9876543210654321";
    private static final String ORGANISATION_ID_ONE = "Organisation id one";
    private static final String ORGANISATION_ID_TWO = "Organisation id two";
    private static final String ORGANISATION_ID_THREE = "Organisation id three";
    private static final String ORGANISATION_ID_FOUR = "Organisation id four";
    private static final String ORGANISATION_ID_FIVE = "Organisation id five";
    private static final String ORGANISATION_ID_SIX = "Organisation id six";
    private static final String ORGANISATION_ID_SEVEN = "Organisation id seven";
    private static final String ORGANISATION_ID_EIGHT = "Organisation id eight";
    private static final String ORGANISATION_ID_NINE = "Organisation id nine";
    private static final String ORGANISATION_ID_TEN = "Organisation id ten";
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
    private static final int MAX_NOC_ANSWERS_COUNT = 10;
    private static final String USER_TOKEN = "userToken";
    private static final String CASE_ID = "1234567890123456";

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
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE_1));
        // when case data respondent collection is not empty but representative collection is empty should not throw
        // any exception
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        caseData.setRespondentCollection(List.of(respondentSumTypeItem));
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE_1));
        // when case data both respondent and representative collections are not empty but respondent in
        // respondent collection doesn't have any value should not throw any exception
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().build()));
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE_1));
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
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE_1));
        // when respondent name is equal to selected name in representative collection
        caseData.getRepCollection().getFirst().getValue().getDynamicRespRepName().getValue()
                .setLabel(RESPONDENT_NAME_ONE);
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE_1));
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

        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE_1));
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresented()).isEqualTo(YES);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeRemoved()).isEqualTo(NO);
        assertThat(caseData.getRespondentCollection().getFirst().getValue().getRepresentativeId())
                .isEqualTo(DUMMY_REPRESENTATIVE_ID);
        assertThat(caseData.getRepCollection().getFirst().getValue().getRespondentId()).isEqualTo(RESPONDENT_ID_ONE);
        assertThat(caseData.getRepCollection().getFirst().getValue().getRespRepName()).isEqualTo(RESPONDENT_NAME_ONE);
        assertThat(caseData.getRepCollection().getFirst().getId()).isEqualTo(DUMMY_REPRESENTATIVE_ID);
        // when both respondent id and respondent names are equal in respondent collection and representative
        // collection
        assertDoesNotThrow(() -> NocUtils.mapRepresentativesToRespondents(caseData, DUMMY_CASE_SUBMISSION_REFERENCE_1));
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

    @Test
    @SneakyThrows
    void theValidateCallbackRequest() {
        // when callback request is empty should throw EXCEPTION_CALLBACK_REQUEST_NOT_FOUND
        GenericServiceException genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(null));
        assertThat(genericServiceException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_CALLBACK_REQUEST_NOT_FOUND);
        // when callback request does not have new case details should throw EXCEPTION_NEW_CASE_DETAILS_NOT_FOUND
        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(callbackRequest));
        assertThat(genericServiceException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_NEW_CASE_DETAILS_NOT_FOUND);
        // when callback request does not have old case details should throw EXCEPTION_OLD_CASE_DETAILS_NOT_FOUND
        callbackRequest.setCaseDetails(new CaseDetails());
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(callbackRequest));
        assertThat(genericServiceException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_OLD_CASE_DETAILS_NOT_FOUND);
        // when case details not have submission reference should throw
        // EXCEPTION_NEW_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND
        callbackRequest.setCaseDetailsBefore(new CaseDetails());
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(callbackRequest));
        assertThat(genericServiceException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_NEW_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND);
        // when case details not have submission reference should throw
        // EXCEPTION_OLD_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND
        callbackRequest.getCaseDetails().setCaseId(DUMMY_CASE_SUBMISSION_REFERENCE_1);
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(callbackRequest));
        assertThat(genericServiceException.getMessage()).isEqualTo(
                EXPECTED_EXCEPTION_OLD_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND);
        // when case details not have submission reference should throw
        // EXCEPTION_OLD_AND_NEW_SUBMISSION_REFERENCES_NOT_EQUAL
        callbackRequest.getCaseDetailsBefore().setCaseId(DUMMY_CASE_SUBMISSION_REFERENCE_2);
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(callbackRequest));
        assertThat(genericServiceException.getMessage())
                .isEqualTo(EXPECTED_EXCEPTION_OLD_AND_NEW_SUBMISSION_REFERENCES_NOT_EQUAL);
        // when case details not have case data should throw EXPECTED_EXCEPTION_NEW_CASE_DATA_NOT_FOUND
        callbackRequest.getCaseDetailsBefore().setCaseId(DUMMY_CASE_SUBMISSION_REFERENCE_1);
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(callbackRequest));
        assertThat(genericServiceException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_NEW_CASE_DATA_NOT_FOUND);
        // when case details not have case data should throw EXCEPTION_OLD_CASE_DATA_NOT_FOUND
        callbackRequest.getCaseDetails().setCaseData(new CaseData());
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(callbackRequest));
        assertThat(genericServiceException.getMessage()).isEqualTo(EXPECTED_EXCEPTION_OLD_CASE_DATA_NOT_FOUND);
        // when new case data does not have any respondent should throw EXCEPTION_NEW_RESPONDENT_COLLECTION_IS_EMPTY
        callbackRequest.getCaseDetailsBefore().setCaseData(new CaseData());
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(callbackRequest));
        assertThat(genericServiceException.getMessage())
                .isEqualTo(EXPECTED_EXCEPTION_NEW_RESPONDENT_COLLECTION_IS_EMPTY);
        // when old case data does not have any respondent should throw EXCEPTION_OLD_RESPONDENT_COLLECTION_IS_EMPTY
        callbackRequest.getCaseDetails().getCaseData().setRespondentCollection(List.of(new RespondentSumTypeItem()));
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(callbackRequest));
        assertThat(genericServiceException.getMessage())
                .isEqualTo(EXPECTED_EXCEPTION_OLD_RESPONDENT_COLLECTION_IS_EMPTY);
        // when old and new case data has different respondent collection should throw
        // EXCEPTION_OLD_AND_NEW_RESPONDENTS_ARE_DIFFERENT
        callbackRequest.getCaseDetailsBefore().getCaseData().setRespondentCollection(
                List.of(new RespondentSumTypeItem()));
        callbackRequest.getCaseDetails().getCaseData().getRespondentCollection().getFirst()
                .setId(RESPONDENT_ID_ONE);
        callbackRequest.getCaseDetailsBefore().getCaseData().getRespondentCollection().getFirst()
                .setId(RESPONDENT_ID_TWO);
        genericServiceException = assertThrows(GenericServiceException.class,
                () -> NocUtils.validateCallbackRequest(callbackRequest));
        assertThat(genericServiceException.getMessage())
                .isEqualTo(EXPECTED_EXCEPTION_OLD_AND_NEW_RESPONDENTS_ARE_DIFFERENT);
        // when callback request is valid should not throw any exception.
        callbackRequest.getCaseDetailsBefore().getCaseData().getRespondentCollection().getFirst()
                .setId(RESPONDENT_ID_ONE);
        assertDoesNotThrow(() -> NocUtils.validateCallbackRequest(callbackRequest));
    }

    @Test
    void theResetOrganisationPolicies() {
        // when case data is empty should not throw exception
        RepresentedTypeRItem representative = RepresentedTypeRItem.builder().build();
        List<RepresentedTypeRItem> representatives = new ArrayList<>(List.of(representative));
        assertDoesNotThrow(() -> NocUtils.resetOrganisationPolicies(null, representatives));
        // when representatives are empty should not remove organisation policy or noc answer
        CaseData caseData = new CaseData();
        setAllNoticeOfChangeAnswers(caseData);
        setAllRespondentOrganisationPolicy(caseData);
        assertDoesNotThrow(() -> NocUtils.resetOrganisationPolicies(caseData, null));
        // when representatives have all respondent representative roles, should remove all organisation policies and
        // noc answers
        representatives.getFirst().setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_A).build());
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().role(ROLE_SOLICITOR_B)
                .build()).build());
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().role(ROLE_SOLICITOR_C)
                .build()).build());
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().role(ROLE_SOLICITOR_D)
                .build()).build());
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().role(ROLE_SOLICITOR_E)
                .build()).build());
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().role(ROLE_SOLICITOR_F)
                .build()).build());
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().role(ROLE_SOLICITOR_G)
                .build()).build());
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().role(ROLE_SOLICITOR_H)
                .build()).build());
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().role(ROLE_SOLICITOR_I)
                .build()).build());
        representatives.add(RepresentedTypeRItem.builder().value(RepresentedTypeR.builder().role(ROLE_SOLICITOR_J)
                .build()).build());
        NocUtils.resetOrganisationPolicies(caseData, representatives);
        assertThat(caseData.getRespondentOrganisationPolicy0()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_A).organisation(Organisation.builder().build()).build());
        assertThat(caseData.getRespondentOrganisationPolicy1()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_B).organisation(Organisation.builder().build()).build());
        assertThat(caseData.getRespondentOrganisationPolicy2()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_C).organisation(Organisation.builder().build()).build());
        assertThat(caseData.getRespondentOrganisationPolicy3()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_D).organisation(Organisation.builder().build()).build());
        assertThat(caseData.getRespondentOrganisationPolicy4()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_E).organisation(Organisation.builder().build()).build());
        assertThat(caseData.getRespondentOrganisationPolicy5()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_F).organisation(Organisation.builder().build()).build());
        assertThat(caseData.getRespondentOrganisationPolicy6()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_G).organisation(Organisation.builder().build()).build());
        assertThat(caseData.getRespondentOrganisationPolicy7()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_H).organisation(Organisation.builder().build()).build());
        assertThat(caseData.getRespondentOrganisationPolicy8()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_I).organisation(Organisation.builder().build()).build());
        assertThat(caseData.getRespondentOrganisationPolicy9()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_J).organisation(Organisation.builder().build()).build());
    }

    private static void setAllRespondentOrganisationPolicy(CaseData caseData) {
        caseData.setRespondentOrganisationPolicy0(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_SOLICITOR_A).organisation(Organisation.builder().organisationID(ORGANISATION_ID_ONE).build())
                .build());
        caseData.setRespondentOrganisationPolicy1(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_SOLICITOR_B).organisation(Organisation.builder().organisationID(ORGANISATION_ID_TWO).build())
                .build());
        caseData.setRespondentOrganisationPolicy2(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_SOLICITOR_C).organisation(Organisation.builder().organisationID(ORGANISATION_ID_THREE).build())
                .build());
        caseData.setRespondentOrganisationPolicy3(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_SOLICITOR_D).organisation(Organisation.builder().organisationID(ORGANISATION_ID_FOUR).build())
                .build());
        caseData.setRespondentOrganisationPolicy4(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_SOLICITOR_E).organisation(Organisation.builder().organisationID(ORGANISATION_ID_FIVE).build())
                .build());
        caseData.setRespondentOrganisationPolicy5(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_SOLICITOR_F).organisation(Organisation.builder().organisationID(ORGANISATION_ID_SIX).build())
                .build());
        caseData.setRespondentOrganisationPolicy6(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_SOLICITOR_G).organisation(Organisation.builder().organisationID(ORGANISATION_ID_SEVEN).build())
                .build());
        caseData.setRespondentOrganisationPolicy7(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_SOLICITOR_H).organisation(Organisation.builder().organisationID(ORGANISATION_ID_EIGHT).build())
                .build());
        caseData.setRespondentOrganisationPolicy8(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_SOLICITOR_I).organisation(Organisation.builder().organisationID(ORGANISATION_ID_NINE).build())
                .build());
        caseData.setRespondentOrganisationPolicy9(OrganisationPolicy.builder().orgPolicyCaseAssignedRole(
                ROLE_SOLICITOR_J).organisation(Organisation.builder().organisationID(ORGANISATION_ID_TEN).build())
                .build());
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
    void theBuildApprovedChangeOrganisationRequest() {
        // if both old and new organisations are empty should return empty change organisation request
        ChangeOrganisationRequest emptyOrganisationRequest = ChangeOrganisationRequest.builder().build();
        assertThat(NocUtils.buildApprovedChangeOrganisationRequest(null, null, ROLE_SOLICITOR_A))
                .isEqualTo(emptyOrganisationRequest);
        // if role is blank should return empty change organisation request
        Organisation newOrganisation = Organisation.builder().organisationID(ORGANISATION_ID_ONE).build();
        Organisation oldOrganisation = Organisation.builder().organisationID(ORGANISATION_ID_TWO).build();
        assertThat(NocUtils.buildApprovedChangeOrganisationRequest(newOrganisation, oldOrganisation, StringUtils.EMPTY))
                .isEqualTo(emptyOrganisationRequest);
        // if role is invalid should return empty change organisation request
        assertThat(NocUtils.buildApprovedChangeOrganisationRequest(newOrganisation, oldOrganisation, ROLE_SOLICITOR_K))
                .isEqualTo(emptyOrganisationRequest);
        // if role is valid should return valid change organisation request
        DynamicFixedListType roleItem = new DynamicFixedListType(ROLE_SOLICITOR_A);
        ChangeOrganisationRequest changeOrganisationRequest = NocUtils.buildApprovedChangeOrganisationRequest(
                newOrganisation, oldOrganisation, ROLE_SOLICITOR_A);
        assertThat(changeOrganisationRequest.getOrganisationToAdd()).isEqualTo(newOrganisation);
        assertThat(changeOrganisationRequest.getOrganisationToRemove()).isEqualTo(oldOrganisation);
        assertThat(changeOrganisationRequest.getCaseRoleId()).isEqualTo(roleItem);
        assertThat(changeOrganisationRequest.getApprovalStatus()).isEqualTo(APPROVED);
    }

    @Test
    void theSetNoticeOfChangeAnswersAtIndex() {
        // when case data is empty should do nothing
        assertDoesNotThrow(() -> NocUtils.setNoticeOfChangeAnswerAtIndex(null, RESPONDENT_NAME_ONE,
                NumberUtils.INTEGER_ZERO));
        // when respondent name is empty should do nothing
        CaseData caseData = new CaseData();
        assertDoesNotThrow(() -> NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, StringUtils.EMPTY,
                NumberUtils.INTEGER_ZERO));
        assertThat(caseData.getNoticeOfChangeAnswers0()).isNull();
        // when index is less than zero should do nothing
        assertDoesNotThrow(() -> NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_ONE,
                NumberUtils.INTEGER_MINUS_ONE));
        assertThat(caseData.getNoticeOfChangeAnswers0()).isNull();
        // when index is equal to max noc answers count should do nothing
        assertDoesNotThrow(() -> NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_ONE,
                MAX_NOC_ANSWERS_COUNT));
        assertThat(caseData.getNoticeOfChangeAnswers0()).isNull();
        // when index is greater than max noc answers count should do nothing
        assertDoesNotThrow(() -> NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_ONE,
                LoggerTestUtils.INTEGER_ELEVEN));
        // when all input is correct should set notice of change answer to the given index
        NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_ONE, LoggerTestUtils.INTEGER_ZERO);
        NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_TWO, LoggerTestUtils.INTEGER_ONE);
        NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_THREE, LoggerTestUtils.INTEGER_TWO);
        NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_FOUR, LoggerTestUtils.INTEGER_THREE);
        NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_FIVE, LoggerTestUtils.INTEGER_FOUR);
        NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_SIX, LoggerTestUtils.INTEGER_FIVE);
        NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_SEVEN, LoggerTestUtils.INTEGER_SIX);
        NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_EIGHT, LoggerTestUtils.INTEGER_SEVEN);
        NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_NINE, LoggerTestUtils.INTEGER_EIGHT);
        NocUtils.setNoticeOfChangeAnswerAtIndex(caseData, RESPONDENT_NAME_TEN, LoggerTestUtils.INTEGER_NINE);

        assertThat(caseData.getNoticeOfChangeAnswers0().getRespondentName()).isEqualTo(RESPONDENT_NAME_ONE);
        assertThat(caseData.getNoticeOfChangeAnswers1().getRespondentName()).isEqualTo(RESPONDENT_NAME_TWO);
        assertThat(caseData.getNoticeOfChangeAnswers2().getRespondentName()).isEqualTo(RESPONDENT_NAME_THREE);
        assertThat(caseData.getNoticeOfChangeAnswers3().getRespondentName()).isEqualTo(RESPONDENT_NAME_FOUR);
        assertThat(caseData.getNoticeOfChangeAnswers4().getRespondentName()).isEqualTo(RESPONDENT_NAME_FIVE);
        assertThat(caseData.getNoticeOfChangeAnswers5().getRespondentName()).isEqualTo(RESPONDENT_NAME_SIX);
        assertThat(caseData.getNoticeOfChangeAnswers6().getRespondentName()).isEqualTo(RESPONDENT_NAME_SEVEN);
        assertThat(caseData.getNoticeOfChangeAnswers7().getRespondentName()).isEqualTo(RESPONDENT_NAME_EIGHT);
        assertThat(caseData.getNoticeOfChangeAnswers8().getRespondentName()).isEqualTo(RESPONDENT_NAME_NINE);
        assertThat(caseData.getNoticeOfChangeAnswers9().getRespondentName()).isEqualTo(RESPONDENT_NAME_TEN);
    }

    @Test
    void thePopulateNoticeOfChangeAnswers() {
        // when case data is null should not do anything
        assertDoesNotThrow(() -> NocUtils.populateNoticeOfChangeAnswers(null));
        // when respondent collection is empty should not do anything
        CaseData caseData = new CaseData();
        caseData.setRespondentCollection(new ArrayList<>());
        assertDoesNotThrow(() -> NocUtils.populateNoticeOfChangeAnswers(caseData));
        // when respondent collection has a respondent with valid notice of change answers should not do anything
        RespondentSumTypeItem respondent1 = new RespondentSumTypeItem();
        caseData.getRespondentCollection().add(respondent1);
        NoticeOfChangeAnswers answers = NoticeOfChangeAnswers.builder().respondentName(RESPONDENT_NAME_ONE).build();
        caseData.setNoticeOfChangeAnswers0(answers);
        assertDoesNotThrow(() -> NocUtils.populateNoticeOfChangeAnswers(caseData));
        // when respondent collection has invalid respondent should not do anything
        caseData.setNoticeOfChangeAnswers0(null);
        assertDoesNotThrow(() -> NocUtils.populateNoticeOfChangeAnswers(caseData));
        // when respondent collection has valid and invalid respondent(s) should set notice of change answers for valid
        // respondents
        // should not set notice of change answers as it already has a respondent with a valid notice of change answer
        caseData.setNoticeOfChangeAnswers0(answers);
        caseData.getRespondentCollection().getFirst().setId(RESPONDENT_ID_ONE);
        caseData.getRespondentCollection().getFirst().setValue(RespondentSumType.builder()
                .respondentName(RESPONDENT_NAME_ONE).build());
        // should not set notice of change answers as respondent is invalid
        RespondentSumTypeItem respondent2 = new RespondentSumTypeItem();
        caseData.getRespondentCollection().add(respondent2);
        // should set notice of change answers for this respondent
        RespondentSumTypeItem respondent3 = new RespondentSumTypeItem();
        respondent3.setId(RESPONDENT_ID_TWO);
        respondent3.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_TWO).build());
        caseData.getRespondentCollection().add(respondent3);
        assertDoesNotThrow(() -> NocUtils.populateNoticeOfChangeAnswers(caseData));
        assertThat(caseData.getNoticeOfChangeAnswers0()).isEqualTo(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_ONE).build());
        assertThat(caseData.getNoticeOfChangeAnswers1()).isNull();
        assertThat(caseData.getNoticeOfChangeAnswers2()).isEqualTo(NoticeOfChangeAnswers.builder()
                .respondentName(RESPONDENT_NAME_TWO).build());
    }

    @Test
    void theApplyRespondentOrganisationPolicyForRole() {
        // when case data is empty should do nothing
        RepresentedTypeRItem representative = new RepresentedTypeRItem();
        representative.setValue(RepresentedTypeR.builder().build());
        assertDoesNotThrow(() -> NocUtils.applyRespondentOrganisationPolicyForRole(null, representative));
        // when representative is empty should do nothing
        CaseData caseData = new CaseData();
        assertDoesNotThrow(() -> NocUtils.applyRespondentOrganisationPolicyForRole(caseData, null));
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNull();
        // when representative value is empty should do nothing
        representative.setValue(null);
        assertDoesNotThrow(() -> NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative));
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNull();
        // when representative does not have role should do nothing
        representative.setValue(RepresentedTypeR.builder().build());
        assertDoesNotThrow(() -> NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative));
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNull();
        // when representative does not have respondent organisation should do nothing
        representative.setValue(RepresentedTypeR.builder().role(
                ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel()).build());
        assertDoesNotThrow(() -> NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative));
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNull();
        // when representative role is not a valid respondent representative role should do nothing
        Organisation organisation1 = Organisation.builder().organisationID(ORGANISATION_ID_ONE).build();
        representative.getValue().setRespondentOrganisation(organisation1);
        assertDoesNotThrow(() -> NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative));
        assertThat(caseData.getRespondentOrganisationPolicy0()).isNull();
        // when representative role is valid representative role should set organisation policy
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_A)
                .respondentOrganisation(organisation1).build());
        NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);

        Organisation organisation2 = Organisation.builder().organisationID(ORGANISATION_ID_TWO).build();
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_B)
                .respondentOrganisation(organisation2).build());
        NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);

        Organisation organisation3 = Organisation.builder().organisationID(ORGANISATION_ID_THREE).build();
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_C)
                .respondentOrganisation(organisation3).build());
        NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);

        Organisation organisation4 = Organisation.builder().organisationID(ORGANISATION_ID_FOUR).build();
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_D)
                .respondentOrganisation(organisation4).build());
        NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);

        Organisation organisation5 = Organisation.builder().organisationID(ORGANISATION_ID_FIVE).build();
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_E)
                .respondentOrganisation(organisation5).build());
        NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);

        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_F).build());
        Organisation organisation6 = Organisation.builder().organisationID(ORGANISATION_ID_SIX).build();
        representative.getValue().setRespondentOrganisation(organisation6);
        NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);

        Organisation organisation7 = Organisation.builder().organisationID(ORGANISATION_ID_SEVEN).build();
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_G)
                .respondentOrganisation(organisation7).build());
        NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);

        Organisation organisation8 = Organisation.builder().organisationID(ORGANISATION_ID_EIGHT).build();
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_H)
                .respondentOrganisation(organisation8).build());
        NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);

        Organisation organisation9 = Organisation.builder().organisationID(ORGANISATION_ID_NINE).build();
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_I)
                .respondentOrganisation(organisation9).build());
        NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);

        Organisation organisation10 = Organisation.builder().organisationID(ORGANISATION_ID_TEN).build();
        representative.setValue(RepresentedTypeR.builder().role(ROLE_SOLICITOR_J)
                .respondentOrganisation(organisation10).build());
        NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);

        assertThat(caseData.getRespondentOrganisationPolicy0()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_A).organisation(organisation1).build());
        assertThat(caseData.getRespondentOrganisationPolicy1()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_B).organisation(organisation2).build());
        assertThat(caseData.getRespondentOrganisationPolicy2()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_C).organisation(organisation3).build());
        assertThat(caseData.getRespondentOrganisationPolicy3()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_D).organisation(organisation4).build());
        assertThat(caseData.getRespondentOrganisationPolicy4()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_E).organisation(organisation5).build());
        assertThat(caseData.getRespondentOrganisationPolicy5()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_F).organisation(organisation6).build());
        assertThat(caseData.getRespondentOrganisationPolicy6()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_G).organisation(organisation7).build());
        assertThat(caseData.getRespondentOrganisationPolicy7()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_H).organisation(organisation8).build());
        assertThat(caseData.getRespondentOrganisationPolicy8()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_I).organisation(organisation9).build());
        assertThat(caseData.getRespondentOrganisationPolicy9()).isEqualTo(OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(ROLE_SOLICITOR_J).organisation(organisation10).build());
    }

    @Test
    void theClearNocWarningIfPresent() {
        // when case data is empty should not throw any exception
        assertDoesNotThrow(() -> NocUtils.clearNocWarningIfPresent(null));
        // when case data does not have any noc warning should not throw any exception
        CaseData caseData = new CaseData();
        assertDoesNotThrow(() -> NocUtils.clearNocWarningIfPresent(caseData));
        assertThat(caseData.getNocWarning()).isNull();
        // when case data has noc warning should clear it
        caseData.setNocWarning(NOC_WARNING);
        assertDoesNotThrow(() -> NocUtils.clearNocWarningIfPresent(caseData));
        assertThat(caseData.getNocWarning()).isNull();
    }

    @Test
    void theCanRevokeRepresentativeAccess() {
        // when callback request is empty should return false
        List<RepresentedTypeRItem> representatives = List.of(RepresentedTypeRItem.builder().build());
        assertThat(NocUtils.canRevokeRepresentativeAccess(null, USER_TOKEN, representatives)).isFalse();
        // when callback request not has case details before should return false
        CallbackRequest callbackRequest = new CallbackRequest();
        assertThat(NocUtils.canRevokeRepresentativeAccess(callbackRequest, USER_TOKEN, representatives)).isFalse();
        // when case details before not have case id should return false
        callbackRequest.setCaseDetailsBefore(new CaseDetails());
        assertThat(NocUtils.canRevokeRepresentativeAccess(callbackRequest, USER_TOKEN, representatives)).isFalse();
        // when case details before not have case data should return false
        callbackRequest.getCaseDetailsBefore().setCaseId(CASE_ID);
        assertThat(NocUtils.canRevokeRepresentativeAccess(callbackRequest, USER_TOKEN, representatives)).isFalse();
        // when user token is empty should return false
        callbackRequest.getCaseDetailsBefore().setCaseData(new CaseData());
        assertThat(NocUtils.canRevokeRepresentativeAccess(callbackRequest, StringUtils.EMPTY, representatives))
                .isFalse();
        // when representative list is empty should return false
        assertThat(NocUtils.canRevokeRepresentativeAccess(callbackRequest, USER_TOKEN, new ArrayList<>())).isFalse();
        // when all parameters are valid should return true
        assertThat(NocUtils.canRevokeRepresentativeAccess(callbackRequest, USER_TOKEN, representatives)).isTrue();
    }
}
