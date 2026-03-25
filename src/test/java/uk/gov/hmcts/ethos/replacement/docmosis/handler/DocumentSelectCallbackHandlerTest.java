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
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultiplesDocumentAccessService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentSelectCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private MultiplesDocumentAccessService multiplesDocumentAccessService;

    private DocumentSelectCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DocumentSelectCallbackHandler(caseDetailsConverter, multiplesDocumentAccessService);
    }

    @Test
    void aboutToSubmitShouldDelegateToDocumentAccessService() {
        MultipleData multipleData = new MultipleData();
        MultipleDetails multipleDetails = multipleDetails(multipleData);
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class)))
            .thenReturn(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(multiplesDocumentAccessService).setMultipleDocumentsToCorrectTab(multipleData);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private MultipleDetails multipleDetails(MultipleData caseData) {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(caseData);
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
