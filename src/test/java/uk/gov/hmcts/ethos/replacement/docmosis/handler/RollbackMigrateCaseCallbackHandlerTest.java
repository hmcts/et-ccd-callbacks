package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EcmMigrationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RollbackMigrateCaseCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private EcmMigrationService ecmMigrationService;

    private RollbackMigrateCaseCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RollbackMigrateCaseCallbackHandler(caseDetailsConverter, ecmMigrationService);
    }

    @Test
    void aboutToSubmitShouldRollbackMigration() throws IOException {
        CaseData caseData = new CaseData();
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails = stubConverter(caseData);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(ecmMigrationService).rollbackEcmMigration(ccdCaseDetails);
    }

    @Test
    void aboutToSubmitShouldWrapIOExceptionFromRollback() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        org.mockito.Mockito.doThrow(new IOException("rollback failed"))
            .when(ecmMigrationService)
            .rollbackEcmMigration(any());

        assertThatThrownBy(() -> handler.aboutToSubmit(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to rollback migrated case")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
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
