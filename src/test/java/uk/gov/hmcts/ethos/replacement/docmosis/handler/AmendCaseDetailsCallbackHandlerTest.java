package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AddSingleCaseToMultipleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendCaseDetailsCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private EventValidationService eventValidationService;
    @Mock
    private DefaultValuesReaderService defaultValuesReaderService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @Mock
    private AddSingleCaseToMultipleService addSingleCaseToMultipleService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CaseManagementLocationService caseManagementLocationService;

    private AmendCaseDetailsCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AmendCaseDetailsCallbackHandler(
            caseDetailsConverter,
            eventValidationService,
            defaultValuesReaderService,
            caseManagementForCaseWorkerService,
            addSingleCaseToMultipleService,
            featureToggleService,
            caseManagementLocationService
        );
    }

    @Test
    void aboutToSubmitShouldReturnValidationErrorsAndSkipMutations() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("12345/2025");
        stubConverter(caseData, "ET_EnglandWales");
        when(eventValidationService.validateReceiptDate(any()))
            .thenReturn(new ArrayList<>(List.of("invalid receipt date")));
        when(eventValidationService.validateCaseState(any())).thenReturn(false);
        when(eventValidationService.validateCurrentPosition(any())).thenReturn(false);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getErrors()).contains("invalid receipt date");
        assertThat(response.getErrors()).contains("12345/2025 Case has not been Accepted.");
        assertThat(response.getErrors())
            .contains("To set the current position to 'Case closed' and to close the case, "
                + "please take the Close Case action.");
        verifyNoInteractions(defaultValuesReaderService);
        verifyNoInteractions(caseManagementForCaseWorkerService);
        verifyNoInteractions(addSingleCaseToMultipleService);
        verifyNoInteractions(caseManagementLocationService);
        verify(featureToggleService, never()).isWorkAllocationEnabled();
    }

    @Test
    void aboutToSubmitShouldInvokeMutationServicesWhenValidationPasses() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("Glasgow");
        stubConverter(caseData, Constants.SCOTLAND_CASE_TYPE_ID);
        when(eventValidationService.validateReceiptDate(any())).thenReturn(new ArrayList<>());
        when(eventValidationService.validateCaseState(any())).thenReturn(true);
        when(eventValidationService.validateCurrentPosition(any())).thenReturn(true);

        DefaultValues defaultValues = DefaultValues.builder().build();
        when(defaultValuesReaderService.getDefaultValues("Glasgow")).thenReturn(defaultValues);
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getErrors()).isEmpty();
        verify(defaultValuesReaderService).setCaseData(caseData, defaultValues);
        verify(caseManagementForCaseWorkerService).dateToCurrentPosition(caseData);
        verify(caseManagementForCaseWorkerService).setEt3ResponseDueDate(caseData);
        verify(caseManagementForCaseWorkerService).setNextListedDate(caseData);

        verify(addSingleCaseToMultipleService).addSingleCaseToMultipleLogic(
            nullable(String.class),
            eq(caseData),
            eq(Constants.SCOTLAND_CASE_TYPE_ID),
            eq("EMPLOYMENT"),
            eq("123"),
            argThat(List::isEmpty)
        );

        verify(caseManagementLocationService).setCaseManagementLocation(caseData);
    }

    @Test
    void aboutToSubmitShouldNotSetCaseManagementLocationForNonScotlandCase() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice("London South");
        stubConverter(caseData, "ET_EnglandWales");
        when(eventValidationService.validateReceiptDate(any())).thenReturn(new ArrayList<>());
        when(eventValidationService.validateCaseState(any())).thenReturn(true);
        when(eventValidationService.validateCurrentPosition(any())).thenReturn(true);
        when(defaultValuesReaderService.getDefaultValues("London South"))
            .thenReturn(DefaultValues.builder().build());
        when(featureToggleService.isWorkAllocationEnabled()).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementLocationService, never()).setCaseManagementLocation(any());
        verify(addSingleCaseToMultipleService).addSingleCaseToMultipleLogic(
            nullable(String.class),
            eq(caseData),
            eq("ET_EnglandWales"),
            eq("EMPLOYMENT"),
            eq("123"),
            anyList()
        );
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
        ccdCaseDetails.setJurisdiction("EMPLOYMENT");
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
