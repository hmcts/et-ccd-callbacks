package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.et.common.model.enums.RespondentSolicitorType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.model.TestData;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.MapperUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.NoticeOfChangeUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.CLAIMANT_SOLICITOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_INVALID_RESPONDENT_INDEX;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_INVALID_RESPONDENT_INDEX_WITH_CASE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_RESPONDENT_NOT_FOUND_WITH_INDEX;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.CaseRoleServiceTest.CASE_USER_ROLE_CLAIMANT_SOLICITOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.CASE_USER_ROLE_INVALID;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_CASE_DETAILS_WITH_ID_123_NOT_HAVE_CASE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_CASE_DETAILS_WITH_ID_1646225213651590_NOT_HAVE_CASE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_CASE_USER_ROLE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_EMPTY_RESPONDENT_COLLECTION;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_INVALID_CASE_USER_ROLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_INVALID_RESPONDENT_INDEX_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_INVALID_RESPONDENT_INDEX_NEGATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_NUMERIC;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_WITHIN_BOUNDS;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.INVALID_INTEGER;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.STRING_MINUS_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.STRING_NINE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.STRING_TEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.STRING_ZERO;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_ID_LONG;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_ID_STRING;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_INVALID_INTEGER;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_RESPONDENT_NAME;

public class RespondentServiceUtilsTest {

    MockedStatic<MapperUtils> mapperUtils = mockStatic(MapperUtils.class);
    MockedStatic<NoticeOfChangeUtils> noticeOfChangeUtil = mockStatic(NoticeOfChangeUtils.class);

    @AfterEach
    void afterEach() {
        if (!mapperUtils.isClosed()) {
            mapperUtils.close();
        }
        if (!noticeOfChangeUtil.isClosed()) {
            noticeOfChangeUtil.close();
        }
    }

    @Test
    void theGetRespondentSolicitorType() {
        // Should throw CallbacksRuntimeException when caseDetails is null or empty
        CallbacksRuntimeException callbacksRuntimeException = assertThrows(CallbacksRuntimeException.class,
                () -> RespondentServiceUtils.getRespondentSolicitorType(null,
                        NumberUtils.INTEGER_ONE.toString()));
        assertThat(callbacksRuntimeException.getMessage()).isEqualTo(EXCEPTION_CASE_DETAILS_NOT_FOUND);

        // Should throw CallbacksRuntimeException when caseDetails not have case data
        final CaseDetails caseDetailsWithoutCaseData = CaseDetails.builder().id(TEST_CASE_ID_LONG).build();
        callbacksRuntimeException = assertThrows(CallbacksRuntimeException.class,
                () -> RespondentServiceUtils.getRespondentSolicitorType(caseDetailsWithoutCaseData,
                                NumberUtils.INTEGER_ONE.toString()));
        assertThat(callbacksRuntimeException.getMessage())
                .isEqualTo(EXCEPTION_CASE_DETAILS_WITH_ID_123_NOT_HAVE_CASE_DATA);

        // Should throw CallbacksRuntimeException when respondentIndex is empty
        final CaseDetails caseDetails = new TestData().getCaseDetails();
        callbacksRuntimeException = assertThrows(CallbacksRuntimeException.class,
                () -> RespondentServiceUtils.getRespondentSolicitorType(caseDetails, StringUtils.EMPTY));
        assertThat(callbacksRuntimeException.getMessage()).isEqualTo(EXCEPTION_INVALID_RESPONDENT_INDEX_EMPTY);

        // Should throw CallbacksRuntimeException when respondentIndex is not numeric
        callbacksRuntimeException = assertThrows(CallbacksRuntimeException.class,
                () -> RespondentServiceUtils.getRespondentSolicitorType(caseDetails, TEST_INVALID_INTEGER));
        assertThat(callbacksRuntimeException.getMessage()).isEqualTo(EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_NUMERIC);

        // Should throw CallbacksRuntimeException when respondentIndex is negative
        callbacksRuntimeException = assertThrows(CallbacksRuntimeException.class,
                () -> RespondentServiceUtils.getRespondentSolicitorType(caseDetails,
                        NumberUtils.INTEGER_MINUS_ONE.toString()));
        assertThat(callbacksRuntimeException.getMessage()).isEqualTo(EXCEPTION_INVALID_RESPONDENT_INDEX_NEGATIVE);

        // Should throw CallbacksRuntimeException when not able to map case details' case data map to case data object

        mapperUtils.when(() -> MapperUtils.convertCaseDataMapToCaseDataObject(caseDetails.getData()))
                .thenReturn(null);
        callbacksRuntimeException = assertThrows(CallbacksRuntimeException.class, () -> RespondentServiceUtils
                .getRespondentSolicitorType(caseDetails, NumberUtils.INTEGER_ZERO.toString()));
        assertThat(callbacksRuntimeException.getMessage())
                .isEqualTo(EXCEPTION_CASE_DETAILS_WITH_ID_1646225213651590_NOT_HAVE_CASE_DATA);

        // Should throw CallbacksRuntimeException when caseDetails' caseData doesn't have respondent collection
        final CaseData caseDataWithoutRespondentCollection = new CaseData();
        mapperUtils.when(() -> MapperUtils.convertCaseDataMapToCaseDataObject(caseDetails.getData()))
                .thenReturn(caseDataWithoutRespondentCollection);
        callbacksRuntimeException = assertThrows(CallbacksRuntimeException.class, () -> RespondentServiceUtils
                .getRespondentSolicitorType(caseDetails, NumberUtils.INTEGER_ZERO.toString()));
        assertThat(callbacksRuntimeException.getMessage()).isEqualTo(EXCEPTION_EMPTY_RESPONDENT_COLLECTION);

        // Should throw CallbacksRuntimeException when respondent not found with the given index
        mapperUtils.close();
        callbacksRuntimeException = assertThrows(CallbacksRuntimeException.class, () -> RespondentServiceUtils
                .getRespondentSolicitorType(caseDetails, STRING_TEN));
        assertThat(callbacksRuntimeException.getMessage())
                .isEqualTo(EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_WITHIN_BOUNDS);

        // Should throw CallbacksRuntimeException when notice of change answer not found
        final CaseData validCaseData = MapperUtils.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        noticeOfChangeUtil.when(() -> NoticeOfChangeUtils.findNoticeOfChangeAnswerIndex(validCaseData,
                TEST_RESPONDENT_NAME)).thenReturn(NumberUtils.INTEGER_MINUS_ONE);
        callbacksRuntimeException = assertThrows(CallbacksRuntimeException.class, () -> RespondentServiceUtils
                .getRespondentSolicitorType(caseDetails, NumberUtils.INTEGER_ZERO.toString()));
        assertThat(callbacksRuntimeException.getMessage()).isEqualTo(EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND);

        // Should throw CallbacksRuntimeException when respondent solicitor type not found
        noticeOfChangeUtil.when(() -> NoticeOfChangeUtils.findNoticeOfChangeAnswerIndex(validCaseData,
                        TEST_RESPONDENT_NAME))
                .thenReturn(NumberUtils.createInteger(STRING_TEN));
        callbacksRuntimeException = assertThrows(CallbacksRuntimeException.class, () -> RespondentServiceUtils
                .getRespondentSolicitorType(caseDetails, NumberUtils.INTEGER_ZERO.toString()));
        assertThat(callbacksRuntimeException.getMessage()).isEqualTo(EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND);

        // Should return valid respondent solicitor type
        noticeOfChangeUtil.when(() -> NoticeOfChangeUtils.findNoticeOfChangeAnswerIndex(validCaseData,
                        TEST_RESPONDENT_NAME))
                .thenReturn(NumberUtils.INTEGER_ZERO);
        noticeOfChangeUtil.when(() -> NoticeOfChangeUtils.findRespondentSolicitorTypeByIndex(
                NumberUtils.INTEGER_ZERO)).thenReturn(RespondentSolicitorType.SOLICITORA);
        assertThat(RespondentServiceUtils.getRespondentSolicitorType(
                caseDetails, NumberUtils.INTEGER_ZERO.toString())).isEqualTo(RespondentSolicitorType.SOLICITORA);
        noticeOfChangeUtil.close();
    }

    @Test
    void theFindRespondentSumTypeItemByIndex() {
        // Test no respondents
        assertThrows(
                CallbacksRuntimeException.class, () ->
                        RespondentServiceUtils.findRespondentSumTypeItemByIndex(null,
                                STRING_MINUS_ONE,
                                TEST_CASE_ID_STRING));
        // Setup valid list
        List<RespondentSumTypeItem> validList = new ArrayList<>();
        RespondentSumType validType0 = new RespondentSumType(); // assume default is non-empty
        RespondentSumType validType1 = new RespondentSumType();
        RespondentSumTypeItem validTypeItem0 = new RespondentSumTypeItem();
        validTypeItem0.setValue(validType0);
        RespondentSumTypeItem validTypeItem1 = new RespondentSumTypeItem();
        validTypeItem1.setValue(validType1);
        validList.add(validTypeItem0);
        validList.add(validTypeItem1);

        // Test valid index "0"
        RespondentSumTypeItem result0 = RespondentServiceUtils.findRespondentSumTypeItemByIndex(
                validList, NumberUtils.INTEGER_ZERO.toString(), TEST_CASE_ID_STRING);
        assertThat(validType0).isEqualTo(result0.getValue());

        // Test valid index "1"
        RespondentSumTypeItem result1 = RespondentServiceUtils.findRespondentSumTypeItemByIndex(
                validList, NumberUtils.INTEGER_ONE.toString(), TEST_CASE_ID_STRING);
        assertThat(validType1).isEqualTo(result1.getValue());

        // Test invalid: non-numeric input
        CallbacksRuntimeException ex1 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.findRespondentSumTypeItemByIndex(
                        validList, INVALID_INTEGER, TEST_CASE_ID_STRING));
        assertThat(ex1.getMessage()).contains(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX,
                INVALID_INTEGER,
                TEST_CASE_ID_STRING));

        // Test invalid: index out of bounds
        CallbacksRuntimeException ex2 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.findRespondentSumTypeItemByIndex(validList, STRING_NINE, TEST_CASE_ID_STRING));
        assertThat(ex2.getMessage()).contains(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX,
                STRING_NINE,
                TEST_CASE_ID_STRING));

        // Test invalid: null list
        List<RespondentSumTypeItem> emptyList = new ArrayList<>();
        assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.findRespondentSumTypeItemByIndex(
                        emptyList, STRING_MINUS_ONE, TEST_CASE_ID_STRING));

        // Test invalid: null item in list
        List<RespondentSumTypeItem> listWithNull = new ArrayList<>();
        listWithNull.add(null);
        CallbacksRuntimeException ex4 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.findRespondentSumTypeItemByIndex(
                        listWithNull, STRING_ZERO, TEST_CASE_ID_STRING));
        assertThat(ex4.getMessage()).contains(String.format(
                EXCEPTION_RESPONDENT_NOT_FOUND_WITH_INDEX,
                NumberUtils.INTEGER_ZERO));

        // Test invalid: item with null value
        List<RespondentSumTypeItem> listWithNullValue = new ArrayList<>();
        RespondentSumTypeItem itemWithNullValue = new RespondentSumTypeItem();
        itemWithNullValue.setValue(null);
        listWithNullValue.add(itemWithNullValue);
        CallbacksRuntimeException ex5 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.findRespondentSumTypeItemByIndex(
                        listWithNullValue, STRING_MINUS_ONE, TEST_CASE_ID_STRING));
        assertThat(ex5.getMessage()).contains(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX,
                STRING_MINUS_ONE,
                TEST_CASE_ID_STRING));
    }

    @Test
    void testResetOrganizationPolicy_AllScenarios() {
        String caseId = "CASE-001";

        // -- Test 1: Null caseData --
        CallbacksRuntimeException ex1 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.resetOrganizationPolicy(null,
                        CLAIMANT_SOLICITOR,
                        caseId));
        assertThat(ex1.getMessage()).isEqualTo(EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA);

        // -- Test 2: Blank caseUserRole --
        CaseData blankRoleCase = new CaseData();
        CallbacksRuntimeException ex2 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.resetOrganizationPolicy(blankRoleCase, StringUtils.EMPTY, caseId));
        assertThat(ex2.getMessage()).isEqualTo(EXCEPTION_CASE_USER_ROLE_NOT_FOUND);

        // -- Test 3: Valid claimant role --
        CaseData claimantCase = new CaseData();
        RespondentServiceUtils.resetOrganizationPolicy(claimantCase, CASE_USER_ROLE_CLAIMANT_SOLICITOR, caseId);
        OrganisationPolicy claimantPolicy = claimantCase.getClaimantRepresentativeOrganisationPolicy();
        assertThat(claimantPolicy).isNotNull();
        assertThat(claimantPolicy.getOrgPolicyCaseAssignedRole()).isEqualTo(CASE_USER_ROLE_CLAIMANT_SOLICITOR);

        // -- Test 4: Valid respondent solicitor roles --
        List<String> roles = List.of(
                RespondentSolicitorType.SOLICITORA.getLabel(),
                RespondentSolicitorType.SOLICITORB.getLabel(),
                RespondentSolicitorType.SOLICITORC.getLabel(),
                RespondentSolicitorType.SOLICITORD.getLabel(),
                RespondentSolicitorType.SOLICITORE.getLabel(),
                RespondentSolicitorType.SOLICITORF.getLabel(),
                RespondentSolicitorType.SOLICITORG.getLabel(),
                RespondentSolicitorType.SOLICITORH.getLabel(),
                RespondentSolicitorType.SOLICITORI.getLabel(),
                RespondentSolicitorType.SOLICITORJ.getLabel()
        );

        for (int i = 0; i < roles.size(); i++) {
            String role = roles.get(i);
            CaseData caseData = new CaseData();
            RespondentServiceUtils.resetOrganizationPolicy(caseData, role, caseId);

            OrganisationPolicy actualPolicy = switch (i) {
                case 0 -> caseData.getRespondentOrganisationPolicy0();
                case 1 -> caseData.getRespondentOrganisationPolicy1();
                case 2 -> caseData.getRespondentOrganisationPolicy2();
                case 3 -> caseData.getRespondentOrganisationPolicy3();
                case 4 -> caseData.getRespondentOrganisationPolicy4();
                case 5 -> caseData.getRespondentOrganisationPolicy5();
                case 6 -> caseData.getRespondentOrganisationPolicy6();
                case 7 -> caseData.getRespondentOrganisationPolicy7();
                case 8 -> caseData.getRespondentOrganisationPolicy8();
                case 9 -> caseData.getRespondentOrganisationPolicy9();
                default -> throw new CallbacksRuntimeException(new Exception(
                        EXCEPTION_INVALID_RESPONDENT_INDEX_EMPTY));
            };

            assertThat(actualPolicy).isNotNull();
            assertThat(role).isEqualTo(actualPolicy.getOrgPolicyCaseAssignedRole());
        }
        // -- Test 5: Invalid role --
        CaseData claimantCase2 = new CaseData();
        CallbacksRuntimeException ex5 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.resetOrganizationPolicy(claimantCase2, CASE_USER_ROLE_INVALID, caseId));
        assertThat(ex5.getMessage()).isEqualTo(EXCEPTION_INVALID_CASE_USER_ROLE);
    }

    @Test
    void testFindRespondentRepresentative_AllScenarios() {
        String matchingRespondentId = "resp-001";

        // Set up a valid RespondentSumTypeItem
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setId(matchingRespondentId);

        // Set up a matching representative
        RepresentedTypeR matchingRepValue = new RepresentedTypeR();
        matchingRepValue.setRespondentId(matchingRespondentId);
        RepresentedTypeRItem matchingRep = new RepresentedTypeRItem();
        matchingRep.setValue(matchingRepValue);

        // Non-matching representative
        RepresentedTypeR nonMatchingRepValue = new RepresentedTypeR();
        nonMatchingRepValue.setRespondentId("some-other-id");
        RepresentedTypeRItem nonMatchingRep = new RepresentedTypeRItem();
        nonMatchingRep.setValue(nonMatchingRepValue);

        // Valid list with match
        List<RepresentedTypeRItem> repListWithMatch = new ArrayList<>();
        repListWithMatch.add(nonMatchingRep);
        repListWithMatch.add(matchingRep);

        // Valid case: should return matchingRep
        String caseId = "12345";
        RepresentedTypeRItem result = RespondentServiceUtils.findRespondentRepresentative(
                respondent, repListWithMatch, caseId);
        assertThat(matchingRep).isEqualTo(result);

        // Test null respondent
        CallbacksRuntimeException ex1 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.findRespondentRepresentative(null, repListWithMatch, caseId));
        assertThat(ex1.getMessage()).isEqualTo(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX_WITH_CASE_ID, caseId));

        // Test empty representative list
        List<RepresentedTypeRItem> emptyRepresentativeList = new ArrayList<>();
        CallbacksRuntimeException ex2 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.findRespondentRepresentative(respondent, emptyRepresentativeList, caseId));
        assertThat(ex2.getMessage()).isEqualTo(EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND);

        // Test no match
        List<RepresentedTypeRItem> noMatchList = new ArrayList<>();
        noMatchList.add(nonMatchingRep);
        CallbacksRuntimeException ex3 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.findRespondentRepresentative(respondent, noMatchList, caseId));
        assertThat(ex3.getMessage()).isEqualTo(EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND);

        // Test representative with null value
        RepresentedTypeRItem nullValueRep = new RepresentedTypeRItem();
        nullValueRep.setValue(null);
        List<RepresentedTypeRItem> nullValueList = new ArrayList<>();
        nullValueList.add(nullValueRep);
        CallbacksRuntimeException ex4 = assertThrows(CallbacksRuntimeException.class, () ->
                RespondentServiceUtils.findRespondentRepresentative(respondent, nullValueList, caseId));
        assertThat(ex4.getMessage()).isEqualTo(EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND);
    }

}
