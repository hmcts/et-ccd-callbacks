package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AcasService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetrieveAcasCertificateCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private AcasService acasService;

    private RetrieveAcasCertificateCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RetrieveAcasCertificateCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            acasService
        );
    }

    @Test
    void aboutToSubmitShouldNotCallAcasServiceWhenTokenInvalid() {
        stubConverter(caseData("markup"), "ET_EnglandWales", "123");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(acasService, never()).getAcasCertificate(any(), any(), any());
    }

    @Test
    void aboutToSubmitShouldCallAcasServiceWhenTokenValid() {
        CaseData caseData = caseData("markup");
        stubConverter(caseData, "ET_EnglandWales", "123");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(acasService.getAcasCertificate(eq(caseData), eq(null), eq("ET_EnglandWales")))
            .thenReturn(List.of());

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        verify(acasService).getAcasCertificate(caseData, null, "ET_EnglandWales");
        assertThat(response.getData()).isNotNull();
    }

    @Test
    void submittedShouldReturnConfirmationBodyWhenTokenValid() {
        stubConverter(caseData("/documents/abc"), "ET_EnglandWales", "123");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("/documents/abc");
    }

    @Test
    void submittedShouldNotCallAcasServiceWhenTokenInvalid() {
        stubConverter(caseData("markup"), "ET_EnglandWales", "123");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.submitted(callbackCaseDetails());

        verify(acasService, never()).getAcasCertificate(any(), any(), any());
    }

    private void stubConverter(CaseData caseData, String caseTypeId, String caseId) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId(caseTypeId);
        ccdCaseDetails.setCaseId(caseId);
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
    }

    private CaseData caseData(String docMarkUp) {
        CaseData caseData = new CaseData();
        caseData.setDocMarkUp(docMarkUp);
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
