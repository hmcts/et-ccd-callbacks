package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.enums.RespondentSolicitorType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.model.TestData;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.MapperUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.NoticeOfChangeUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_CASE_DETAILS_WITH_ID_123_NOT_HAVE_CASE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_CASE_DETAILS_WITH_ID_1646225213651590_NOT_HAVE_CASE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_EMPTY_RESPONDENT_COLLECTION;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_INVALID_RESPONDENT_INDEX_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_INVALID_RESPONDENT_INDEX_NEGATIVE;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_NUMERIC;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_INVALID_RESPONDENT_INDEX_NOT_WITHIN_BOUNDS;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.STRING_TEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.UtilsTestConstants.TEST_CASE_ID_LONG;
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
}
