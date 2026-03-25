package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestrictedCasesCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private EventValidationService eventValidationService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @Mock
    private CaseFlagsService caseFlagsService;

    private RestrictedCasesCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestrictedCasesCallbackHandler(
            caseDetailsConverter,
            eventValidationService,
            featureToggleService,
            caseManagementForCaseWorkerService,
            caseFlagsService
        );
    }

    @Test
    void aboutToSubmitShouldOnlyValidateWhenHmcDisabled() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(featureToggleService.isHmcEnabled()).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(eventValidationService).validateRestrictedReportingNames(caseData);
        verify(caseManagementForCaseWorkerService, never()).setPublicCaseName(any());
        verify(caseFlagsService, never()).setPrivateHearingFlag(any());
    }

    @Test
    void aboutToSubmitShouldSetCaseManagementAndFlagsWhenHmcEnabled() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(featureToggleService.isHmcEnabled()).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).setPublicCaseName(caseData);
        verify(caseFlagsService).setPrivateHearingFlag(caseData);
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
