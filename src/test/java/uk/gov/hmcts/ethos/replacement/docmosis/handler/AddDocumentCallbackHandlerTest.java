package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddDocumentCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private DocumentManagementService documentManagementService;

    private AddDocumentCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AddDocumentCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            documentManagementService
        );
    }

    @Test
    void aboutToSubmitShouldNotCallDocumentServiceWhenTokenInvalid() {
        CaseData caseData = caseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(documentManagementService, never()).addUploadedDocsToCaseDocCollection(any());
    }

    @Test
    void aboutToSubmitShouldAddDocumentsAndClearTempCollectionWhenTokenValid() {
        CaseData caseData = caseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(documentManagementService).addUploadedDocsToCaseDocCollection(caseData);
        assertThat(caseData.getAddDocumentCollection()).isEmpty();
    }

    @Test
    void aboutToSubmitShouldReturnErrorsWhenDocumentServiceThrows() {
        CaseData caseData = caseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        doThrow(new RuntimeException("failed to upload"))
            .when(documentManagementService).addUploadedDocsToCaseDocCollection(caseData);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getErrors()).containsExactly("failed to upload");
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

    private CaseData caseData() {
        CaseData caseData = new CaseData();
        caseData.setAddDocumentCollection(new ArrayList<DocumentTypeItem>());
        caseData.getAddDocumentCollection().add(new DocumentTypeItem());
        return caseData;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}
