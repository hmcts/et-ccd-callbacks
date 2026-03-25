package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AddAmendClaimantRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocClaimantRepresentativeService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddAmendClaimantRepresentativeCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private AddAmendClaimantRepresentativeService addAmendClaimantRepresentativeService;
    @Mock
    private NocClaimantRepresentativeService nocClaimantRepresentativeService;

    private AddAmendClaimantRepresentativeCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AddAmendClaimantRepresentativeCallbackHandler(
            caseDetailsConverter,
            addAmendClaimantRepresentativeService,
            nocClaimantRepresentativeService
        );
    }

    @Test
    void aboutToSubmitShouldInvokeAddAmendService() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        verify(addAmendClaimantRepresentativeService).addAmendClaimantRepresentative(caseData);
        assertThat(response.getData()).isNotNull();
    }

    @Test
    void submittedShouldInvokeNocUpdate() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);

        handler.submitted(callbackCaseDetails());

        verify(nocClaimantRepresentativeService).updateClaimantRepAccess(any());
    }

    @Test
    void submittedShouldWrapIOException() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        doThrow(new IOException("ccd down"))
            .when(nocClaimantRepresentativeService)
            .updateClaimantRepAccess(any());

        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(CcdInputOutputException.class)
            .hasMessageContaining("Failed to update claimant representatives access")
            .hasCauseInstanceOf(IOException.class);
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
