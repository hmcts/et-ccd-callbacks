package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ConciliationTrackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddAmendJurisdictionCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ConciliationTrackService conciliationTrackService;

    private AddAmendJurisdictionCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AddAmendJurisdictionCallbackHandler(caseDetailsConverter, conciliationTrackService);
    }

    @Test
    void aboutToSubmitShouldPopulateConciliationTrackAndReturnData() {
        CaseData caseData = new CaseData();
        stubConverter(caseData, "ET_EnglandWales");

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        verify(conciliationTrackService).populateConciliationTrackForJurisdiction(caseData);
        assertThat(response.getData()).isNotNull();
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private void stubConverter(CaseData caseData, String caseTypeId) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId(caseTypeId);
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
