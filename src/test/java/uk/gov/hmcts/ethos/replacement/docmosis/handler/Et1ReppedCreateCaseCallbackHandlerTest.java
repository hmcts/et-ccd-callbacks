package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SearchCriteria;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Et1ReppedCreateCaseCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Et1ReppedService et1ReppedService;

    private Et1ReppedCreateCaseCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new Et1ReppedCreateCaseCallbackHandler(caseDetailsConverter, et1ReppedService);
    }

    @Test
    void aboutToSubmitShouldSetDraftDataAndClearSearchCriteria() {
        CaseData caseData = new CaseData();
        caseData.setSearchCriteria(new SearchCriteria());
        stubConverter(caseData);

        handler.aboutToSubmit(callbackCaseDetails());

        assertThat(caseData.getEt1ReppedSectionOne()).isEqualTo("No");
        assertThat(caseData.getSearchCriteria()).isNull();
    }

    @Test
    void submittedShouldAssignCaseAccess() throws IOException {
        CaseData caseData = new CaseData();
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails = stubConverter(caseData);

        handler.submitted(callbackCaseDetails());

        verify(et1ReppedService).assignCaseAccess(eq(ccdCaseDetails), eq(null));
    }

    @Test
    void submittedShouldWrapIOException() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        doThrow(new IOException("boom")).when(et1ReppedService).assignCaseAccess(any(), eq(null));

        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to assign case access");
    }

    private uk.gov.hmcts.et.common.model.ccd.CaseDetails stubConverter(CaseData caseData) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setCaseId("123");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
        return ccdCaseDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}
