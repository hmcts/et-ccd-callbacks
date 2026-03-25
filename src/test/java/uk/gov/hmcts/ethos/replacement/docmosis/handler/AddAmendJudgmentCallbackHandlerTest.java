package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.DraftAndSignJudgement;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.JudgmentValidationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.text.ParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddAmendJudgmentCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private JudgmentValidationService judgmentValidationService;

    private AddAmendJudgmentCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AddAmendJudgmentCallbackHandler(caseDetailsConverter, judgmentValidationService);
    }

    @Test
    void aboutToSubmitShouldValidateJudgmentsAndClearDraft() throws ParseException {
        CaseData caseData = new CaseData();
        caseData.setDraftAndSignJudgement(DraftAndSignJudgement.builder().build());
        stubConverter(caseData);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(judgmentValidationService).validateJudgments(caseData);
        assertThat(caseData.getDraftAndSignJudgement()).isNull();
    }

    @Test
    void aboutToSubmitShouldThrowWhenValidationFails() throws ParseException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        doThrow(new ParseException("bad date", 0))
            .when(judgmentValidationService)
            .validateJudgments(caseData);

        assertThatThrownBy(() -> handler.aboutToSubmit(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to validate judgments")
            .hasCauseInstanceOf(ParseException.class);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
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
