package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultiplesDigitalCaseFileService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDcfMultiplesDcfCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private MultiplesDigitalCaseFileService multiplesDigitalCaseFileService;

    private CreateDcfMultiplesDcfCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateDcfMultiplesDcfCallbackHandler(caseDetailsConverter, multiplesDigitalCaseFileService);
    }

    @Test
    void aboutToSubmitShouldStitchDcfAndClearBundles() {
        MultipleData multipleData = new MultipleData();
        MultipleDetails multipleDetails = multipleDetails(multipleData);
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class)))
            .thenReturn(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
        when(multiplesDigitalCaseFileService.stitchCaseFile(multipleDetails, null)).thenReturn(List.of(doneBundle()));

        handler.aboutToSubmit(callbackCaseDetails());

        verify(multiplesDigitalCaseFileService).stitchCaseFile(multipleDetails, null);
        assertThat(multipleData.getDigitalCaseFile()).isNotNull();
        assertThat(multipleData.getCaseBundles()).isNull();
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private Bundle doneBundle() {
        DocumentLink stitchedDocument = DocumentLink.builder()
            .documentFilename("dcf.pdf")
            .documentUrl("http://dm-store/documents/abc")
            .documentBinaryUrl("http://dm-store/documents/abc/binary")
            .build();
        return Bundle.builder()
            .value(BundleDetails.builder().stitchStatus("DONE").stitchedDocument(stitchedDocument).build())
            .build();
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
