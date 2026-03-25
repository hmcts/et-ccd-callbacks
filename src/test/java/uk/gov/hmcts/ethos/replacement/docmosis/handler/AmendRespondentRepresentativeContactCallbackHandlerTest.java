package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3ResponseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendRespondentRepresentativeContactCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Et3ResponseService et3ResponseService;

    private AmendRespondentRepresentativeContactCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AmendRespondentRepresentativeContactCallbackHandler(caseDetailsConverter, et3ResponseService);
    }

    @Test
    void aboutToSubmitShouldSetRespondentRepresentativeValuesAndClearAddressText() throws GenericServiceException {
        CaseData caseData = new CaseData();
        caseData.setMyHmctsAddressText("address");
        stubConverter(caseData);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        verify(et3ResponseService).setRespondentRepresentsContactDetails(nullable(String.class), eq(caseData),
            eq("123"));
        assertThat(caseData.getMyHmctsAddressText()).isNull();
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void aboutToSubmitShouldReturnErrorWhenServiceThrows() throws GenericServiceException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        doThrow(new GenericServiceException("bad respondent rep", new RuntimeException("boom"),
            "", "", "", ""))
            .when(et3ResponseService)
            .setRespondentRepresentsContactDetails(nullable(String.class), eq(caseData), eq("123"));

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getErrors()).containsExactly("bad respondent rep");
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private void stubConverter(CaseData caseData) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setCaseId("123");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}
