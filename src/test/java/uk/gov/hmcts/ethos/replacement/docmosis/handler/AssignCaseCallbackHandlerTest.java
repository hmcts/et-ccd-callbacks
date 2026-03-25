package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssignCaseCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private DefaultValuesReaderService defaultValuesReaderService;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;
    @Mock
    private FeatureToggleService featureToggleService;

    private AssignCaseCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AssignCaseCallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            defaultValuesReaderService,
            caseManagementLocationService,
            featureToggleService
        );
    }

    @Test
    void aboutToSubmitShouldReturnForbiddenWhenTokenInvalid() {
        CaseData caseData = caseDataWithAssignOffice();
        stubConverter(caseData, "ET_EnglandWales");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verifyNoInteractions(defaultValuesReaderService);
        verifyNoInteractions(caseManagementLocationService);
    }

    @Test
    void aboutToSubmitShouldAssignCaseWithoutLocationUpdatesWhenFeatureFlagsDisabled() {
        CaseData caseData = caseDataWithAssignOffice();
        stubConverter(caseData, "ET_EnglandWales");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(featureToggleService.isHmcEnabled()).thenReturn(false);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(false);
        when(defaultValuesReaderService.getDefaultValues(anyString())).thenReturn(DefaultValues.builder().build());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(defaultValuesReaderService).getDefaultValues("Leeds");
        verify(defaultValuesReaderService).setCaseData(any(CaseData.class), any(DefaultValues.class));
        verify(caseManagementLocationService, never()).setCaseManagementLocation(any());
        verify(caseManagementLocationService, never()).setCaseManagementLocationCode(any());
    }

    @Test
    void aboutToSubmitShouldSetLocationWhenHmcEnabled() {
        CaseData caseData = caseDataWithAssignOffice();
        stubConverter(caseData, "ET_EnglandWales");
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
        when(defaultValuesReaderService.getDefaultValues(anyString())).thenReturn(DefaultValues.builder().build());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementLocationService).setCaseManagementLocation(caseData);
        verify(caseManagementLocationService, org.mockito.Mockito.times(2)).setCaseManagementLocationCode(caseData);
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private CaseData caseDataWithAssignOffice() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("12345/2026");

        DynamicFixedListType assignOffice = new DynamicFixedListType();
        DynamicValueType selected = new DynamicValueType();
        selected.setCode("Leeds");
        selected.setLabel("Leeds");
        assignOffice.setValue(selected);
        caseData.setAssignOffice(assignOffice);

        return caseData;
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
