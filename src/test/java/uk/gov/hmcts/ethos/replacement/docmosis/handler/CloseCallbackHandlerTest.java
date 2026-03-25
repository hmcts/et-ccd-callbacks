package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCloseEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleHelperService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private MultipleCloseEventValidationService multipleCloseEventValidationService;
    @Mock
    private MultipleHelperService multipleHelperService;

    private CloseCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CloseCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            multipleCloseEventValidationService,
            multipleHelperService
        );
    }

    @Test
    void aboutToSubmitShouldNotValidateWhenTokenInvalid() {
        MultipleDetails multipleDetails = multipleDetails();
        stubMultipleConverter(multipleDetails);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(multipleCloseEventValidationService, never()).validateCasesBeforeCloseEvent(any(), any());
        verify(multipleHelperService, never()).sendCloseToSinglesWithoutConfirmation(any(), any(), any());
    }

    @Test
    void aboutToSubmitShouldReturnValidationErrorsWithoutSendingCloseToSingles() {
        MultipleDetails multipleDetails = multipleDetails();
        stubMultipleConverter(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(multipleCloseEventValidationService.validateCasesBeforeCloseEvent(null, multipleDetails))
            .thenReturn(List.of("error"));

        handler.aboutToSubmit(callbackCaseDetails());

        verify(multipleHelperService, never()).sendCloseToSinglesWithoutConfirmation(any(), any(), any());
    }

    @Test
    void aboutToSubmitShouldSendCloseToSinglesWhenNoValidationErrors() {
        MultipleDetails multipleDetails = multipleDetails();
        stubMultipleConverter(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(multipleCloseEventValidationService.validateCasesBeforeCloseEvent(null, multipleDetails))
            .thenReturn(List.of());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(multipleHelperService).sendCloseToSinglesWithoutConfirmation(eq(null), eq(multipleDetails), any());
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private void stubMultipleConverter(MultipleDetails multipleDetails) {
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class)))
            .thenReturn(multipleDetails);
    }

    private MultipleDetails multipleDetails() {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(new MultipleData());
        multipleDetails.setCaseTypeId("ET_EnglandWales_Multiple");
        multipleDetails.setCaseId("123");
        multipleDetails.setState("Open");
        return multipleDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales_Multiple")
            .state("Open")
            .build();
    }
}
