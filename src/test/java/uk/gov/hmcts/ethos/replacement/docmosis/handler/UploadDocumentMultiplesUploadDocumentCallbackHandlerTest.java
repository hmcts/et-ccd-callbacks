package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REJECTION_OF_CLAIM;

@ExtendWith(MockitoExtension.class)
class UploadDocumentMultiplesUploadDocumentCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    private UploadDocumentMultiplesUploadDocumentCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UploadDocumentMultiplesUploadDocumentCallbackHandler(
            caseDetailsConverter,
            caseManagementForCaseWorkerService
        );
    }

    @Test
    void aboutToSubmitShouldSetDocumentTypeAndAddClaimantDocuments() {
        MultipleData caseData = multipleData();
        MultipleDetails multipleDetails = multipleDetails(caseData, "ET_EnglandWales_Multiple", "123");
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class)))
            .thenReturn(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).addClaimantDocuments(caseData);
        assertThat(caseData.getDocumentCollection().get(0).getValue().getDocumentType())
            .isEqualTo(REJECTION_OF_CLAIM);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private MultipleData multipleData() {
        DocumentType rejectionDocument = new DocumentType();
        rejectionDocument.setTypeOfDocument(REJECTION_OF_CLAIM);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId("1");
        documentTypeItem.setValue(rejectionDocument);
        MultipleData caseData = new MultipleData();
        caseData.setDocumentCollection(List.of(documentTypeItem));
        return caseData;
    }

    private MultipleDetails multipleDetails(MultipleData caseData, String caseTypeId, String caseId) {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(caseData);
        multipleDetails.setCaseTypeId(caseTypeId);
        multipleDetails.setCaseId(caseId);
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
