package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CcdCaseAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplyNocDecisionCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private NocNotificationService nocNotificationService;
    @Mock
    private NocRepresentativeService noCRepresentativeService;
    @Mock
    private CcdCaseAssignment ccdCaseAssignment;

    private ApplyNocDecisionCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApplyNocDecisionCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            nocNotificationService,
            noCRepresentativeService,
            ccdCaseAssignment
        );
    }

    @Test
    void aboutToSubmitShouldNotApplyNocWhenTokenInvalid() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(noCRepresentativeService, never()).updateRepresentation(any(), any());
        verify(ccdCaseAssignment, never()).applyNoc(any(), any());
    }

    @Test
    void aboutToSubmitShouldApplyNocWhenTokenValid() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(noCRepresentativeService.updateRepresentation(any(), eq(null))).thenReturn(caseData);
        when(ccdCaseAssignment.applyNoc(any(), eq(null)))
            .thenReturn(CCDCallbackResponse.builder().data(caseData).build());

        var response = handler.aboutToSubmit(callbackCaseDetails());

        verify(noCRepresentativeService).updateRepresentation(any(), eq(null));
        verify(ccdCaseAssignment).applyNoc(any(), eq(null));
        assertThat(response.getData()).isEqualTo(caseData);
    }

    @Test
    void submittedShouldNotSendNotificationsWhenEventIdNotPresent() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());

        var response = handler.submitted(callbackCaseDetails());

        verify(nocNotificationService, never()).sendNotificationOfChangeEmails(any(), any(), any());
        assertThat(response.getConfirmationHeader()).isNull();
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
