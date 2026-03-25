package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrateCaseLinkDetailsCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    private MigrateCaseLinkDetailsCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new MigrateCaseLinkDetailsCallbackHandler(
            caseDetailsConverter,
            caseManagementForCaseWorkerService
        );
    }

    @Test
    void aboutToSubmitShouldDelegateToCaseManagementService() {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails = stubConverter();

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).setMigratedCaseLinkDetails(null, ccdCaseDetails);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private uk.gov.hmcts.et.common.model.ccd.CaseDetails stubConverter() {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(new CaseData());
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
