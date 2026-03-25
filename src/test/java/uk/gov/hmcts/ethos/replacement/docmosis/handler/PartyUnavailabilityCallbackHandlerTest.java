package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartyUnavailabilityCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;

    private PartyUnavailabilityCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PartyUnavailabilityCallbackHandler(caseDetailsConverter, verifyTokenService);
    }

    @Test
    void aboutToSubmitShouldNotClearPartySelectionWhenTokenInvalid() {
        CaseData caseData = new CaseData();
        caseData.setPartySelection(List.of("Claimant"));
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        assertThat(caseData.getPartySelection()).isNotNull();
    }

    @Test
    void aboutToSubmitShouldClearPartySelectionWhenTokenValid() {
        CaseData caseData = new CaseData();
        caseData.setPartySelection(List.of("Claimant"));
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        assertThat(caseData.getPartySelection()).isNull();
    }

    @Test
    void submittedShouldReturnConfirmationWhenTokenValid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationHeader()).contains("Unavailability dates added");
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
