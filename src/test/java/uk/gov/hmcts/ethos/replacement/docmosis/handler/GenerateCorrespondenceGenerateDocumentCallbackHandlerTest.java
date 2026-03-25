package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentGenerationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateCorrespondenceGenerateDocumentCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private DocumentGenerationService documentGenerationService;
    @Mock
    private DefaultValuesReaderService defaultValuesReaderService;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private EventValidationService eventValidationService;

    private GenerateCorrespondenceGenerateDocumentCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GenerateCorrespondenceGenerateDocumentCallbackHandler(
            caseDetailsConverter,
            documentGenerationService,
            defaultValuesReaderService,
            verifyTokenService,
            eventValidationService
        );
    }

    @Test
    void aboutToSubmitShouldNotGenerateDocumentWhenTokenInvalid() {
        stubConverter(new CaseData());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(documentGenerationService, never()).processDocumentRequest(any(), any());
    }

    @Test
    void aboutToSubmitShouldReturnValidationErrorsWhenPresent() {
        stubConverter(new CaseData());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(eventValidationService.validateHearingNumber(any(), any(), any()))
            .thenReturn(List.of("hearing number invalid"));

        var response = handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getErrors()).contains("hearing number invalid");
        verify(documentGenerationService, never()).processDocumentRequest(any(), any());
    }

    @Test
    void submittedShouldReturnGeneratedDocumentLinkWhenTokenValid() {
        CaseData caseData = new CaseData();
        caseData.setDocMarkUp("doc-link");
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("Please download the document from : doc-link");
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
