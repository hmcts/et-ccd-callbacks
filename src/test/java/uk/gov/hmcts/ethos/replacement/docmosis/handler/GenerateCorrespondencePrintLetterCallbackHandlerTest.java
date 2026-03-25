package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.MultipleDocGenerationCallbackService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleLetterService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCorrespondencePrintLetterCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private MultipleLetterService multipleLetterService;
    @Mock
    private MultipleDocGenerationCallbackService multipleDocGenerationCallbackService;

    private GenerateCorrespondencePrintLetterCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GenerateCorrespondencePrintLetterCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            multipleLetterService,
            multipleDocGenerationCallbackService
        );
    }

    @Test
    void aboutToSubmitShouldNotPrintLetterWhenTokenInvalid() {
        MultipleDetails multipleDetails = multipleDetails();
        stubMultipleConverter(multipleDetails);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(multipleLetterService, never()).bulkLetterLogic(any(), any(), any(), anyBoolean());
    }

    @Test
    void aboutToSubmitShouldPrintLetterWhenTokenValid() {
        MultipleDetails multipleDetails = multipleDetails();
        stubMultipleConverter(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(multipleLetterService.bulkLetterLogic(eq(null), eq(multipleDetails), any(), eq(false)))
            .thenReturn(DocumentInfo.builder().markUp("letter").build());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(multipleLetterService).bulkLetterLogic(eq(null), eq(multipleDetails), any(), eq(false));
    }

    @Test
    void submittedShouldDelegateToPrintDocumentConfirmation() {
        MultipleDetails multipleDetails = multipleDetails();
        stubMultipleConverter(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
        when(multipleDocGenerationCallbackService.printDocumentConfirmation(any(), eq(null)))
            .thenReturn(ResponseEntity.ok(MultipleCallbackResponse.builder().build()));

        handler.submitted(callbackCaseDetails());

        verify(multipleDocGenerationCallbackService).printDocumentConfirmation(any(), eq(null));
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
