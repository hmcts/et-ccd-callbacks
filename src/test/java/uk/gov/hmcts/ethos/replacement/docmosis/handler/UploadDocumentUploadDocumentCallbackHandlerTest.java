package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REJECTION_OF_CLAIM;

@ExtendWith(MockitoExtension.class)
class UploadDocumentUploadDocumentCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private EmailService emailService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    private UploadDocumentUploadDocumentCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new UploadDocumentUploadDocumentCallbackHandler(
            caseDetailsConverter,
            "template-id",
            verifyTokenService,
            emailService,
            caseManagementForCaseWorkerService
        );
    }

    @Test
    void aboutToSubmitShouldNotCallServicesWhenTokenInvalid() {
        stubConverter(caseData(), "Open", "123");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService, never()).addClaimantDocuments(any());
        verify(emailService, never()).sendEmail(any(), any(), any());
    }

    @Test
    void aboutToSubmitShouldAddClaimantDocumentsWhenTokenValid() {
        CaseData caseData = caseData();
        stubConverter(caseData, "Open", "123");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).addClaimantDocuments(caseData);
        verify(emailService, never()).sendEmail(any(), any(), any());
        assertThat(caseData.getCaseRejectedEmailSent()).isNull();
    }

    @Test
    void aboutToSubmitShouldSendRejectionEmailWhenConditionsAreMet() {
        CaseData caseData = caseDataForRejectedClaim();
        stubConverter(caseData, REJECTED_STATE, "123");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(emailService.getCitizenCaseLink("123")).thenReturn("citizen-link");

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).addClaimantDocuments(caseData);
        verify(emailService).sendEmail(
            eq("template-id"),
            eq("person@email.com"),
            any()
        );
        assertThat(caseData.getCaseRejectedEmailSent()).isEqualTo(YES);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private void stubConverter(CaseData caseData, String state, String caseId) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setState(state);
        ccdCaseDetails.setCaseId(caseId);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
    }

    private CaseData caseData() {
        return new CaseData();
    }

    private CaseData caseDataForRejectedClaim() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("1234");
        caseData.setClaimant("First Last");

        ClaimantType claimantType = new ClaimantType();
        claimantType.setClaimantEmailAddress("person@email.com");
        caseData.setClaimantType(claimantType);

        ClaimantIndType claimantIndType = new ClaimantIndType();
        claimantIndType.setClaimantTitle("Mr");
        caseData.setClaimantIndType(claimantIndType);

        DocumentType rejectionDocument = new DocumentType();
        rejectionDocument.setTypeOfDocument(REJECTION_OF_CLAIM);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId("1");
        documentTypeItem.setValue(rejectionDocument);
        caseData.setDocumentCollection(List.of(documentTypeItem));
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
