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
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchUpdateCasesCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private MultipleUpdateService multipleUpdateService;

    private BatchUpdateCasesCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new BatchUpdateCasesCallbackHandler(caseDetailsConverter, verifyTokenService, multipleUpdateService);
    }

    @Test
    void aboutToSubmitShouldNotBatchUpdateWhenTokenInvalid() throws IOException {
        MultipleDetails multipleDetails = multipleDetails();
        stubMultipleConverter(multipleDetails);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(multipleUpdateService, never()).bulkUpdateLogic(any(), any(), any());
    }

    @Test
    void aboutToSubmitShouldBatchUpdateWhenTokenValid() throws IOException {
        MultipleDetails multipleDetails = multipleDetails();
        stubMultipleConverter(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(multipleUpdateService).bulkUpdateLogic(eq(null), eq(multipleDetails), any());
    }

    @Test
    void aboutToSubmitShouldWrapIOExceptionFromBatchUpdate() throws IOException {
        MultipleDetails multipleDetails = multipleDetails();
        stubMultipleConverter(multipleDetails);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        doThrow(new IOException("update failed")).when(multipleUpdateService)
            .bulkUpdateLogic(any(), any(), any());

        assertThatThrownBy(() -> handler.aboutToSubmit(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to batch update multiple");
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
