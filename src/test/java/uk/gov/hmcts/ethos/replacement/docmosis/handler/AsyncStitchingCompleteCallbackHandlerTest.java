package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.DocumentLink;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AsyncStitchingCompleteCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    private AsyncStitchingCompleteCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AsyncStitchingCompleteCallbackHandler(caseDetailsConverter);
    }

    @Test
    void aboutToSubmitShouldSetDcfAndClearCaseBundles() {
        CaseData caseData = caseDataWithDoneBundle();
        stubConverter(caseData);

        handler.aboutToSubmit(callbackCaseDetails());

        assertThat(caseData.getDigitalCaseFile()).isNotNull();
        assertThat(caseData.getDigitalCaseFile().getUploadedDocument()).isNotNull();
        assertThat(caseData.getDigitalCaseFile().getUploadedDocument().getDocumentFilename())
            .isEqualTo("dcf.pdf");
        assertThat(caseData.getCaseBundles()).isNull();
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private CaseData caseDataWithDoneBundle() {
        DocumentLink stitchedDocument = DocumentLink.builder()
            .documentFilename("dcf.pdf")
            .documentUrl("http://dm-store/documents/abc")
            .documentBinaryUrl("http://dm-store/documents/abc/binary")
            .build();

        BundleDetails bundleDetails = BundleDetails.builder()
            .stitchStatus("DONE")
            .stitchedDocument(stitchedDocument)
            .build();

        CaseData caseData = new CaseData();
        caseData.setCaseBundles(List.of(Bundle.builder().value(bundleDetails).build()));
        return caseData;
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
