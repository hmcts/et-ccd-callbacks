package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendRespondentDetailsCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private EventValidationService eventValidationService;
    @Mock
    private NocRespondentHelper nocRespondentHelper;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    @Mock
    private CaseFlagsService caseFlagsService;

    private AmendRespondentDetailsCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AmendRespondentDetailsCallbackHandler(
            caseDetailsConverter,
            eventValidationService,
            nocRespondentHelper,
            featureToggleService,
            caseManagementForCaseWorkerService,
            caseFlagsService
        );
    }

    @Test
    void aboutToSubmitShouldRunFullMutationFlowWhenValidationPasses() {
        CaseData caseData = caseDataWithRespondentsAndRepresentatives();
        stubConverter(caseData);

        when(eventValidationService.validateActiveRespondents(caseData)).thenReturn(new ArrayList<>());
        when(eventValidationService.validateET3ResponseFields(caseData)).thenReturn(new ArrayList<>());
        when(eventValidationService.validateMaximumSize(caseData)).thenReturn(Optional.empty());
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(true);
        when(featureToggleService.isHmcEnabled()).thenReturn(true);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).continuingRespondent(any());
        verify(caseManagementForCaseWorkerService).struckOutRespondents(any());
        verify(nocRespondentHelper).amendRespondentNameRepresentativeNames(caseData);
        verify(caseManagementForCaseWorkerService).updateListOfRespondentsWithAnEcc(caseData);
        verify(caseManagementForCaseWorkerService).setCaseNameHmctsInternal(caseData);
        verify(caseManagementForCaseWorkerService).setPublicCaseName(caseData);
        verify(caseFlagsService).setupCaseFlags(caseData);
        verify(caseManagementForCaseWorkerService).updateWorkAllocationField(anyList(), eq(caseData));
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void aboutToSubmitShouldReturnErrorsAndSkipRespondentMutationWhenValidationFails() {
        CaseData caseData = caseDataWithRespondentsAndRepresentatives();
        stubConverter(caseData);

        when(eventValidationService.validateActiveRespondents(caseData))
            .thenReturn(new ArrayList<>(List.of("active respondent error")));
        when(eventValidationService.validateMaximumSize(caseData)).thenReturn(Optional.empty());
        when(featureToggleService.isGlobalSearchEnabled()).thenReturn(false);
        when(featureToggleService.isHmcEnabled()).thenReturn(false);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getErrors()).containsExactly("active respondent error");
        verify(caseManagementForCaseWorkerService, never()).continuingRespondent(any());
        verify(caseManagementForCaseWorkerService, never()).struckOutRespondents(any());
        verify(nocRespondentHelper, never()).amendRespondentNameRepresentativeNames(any());
        verify(caseManagementForCaseWorkerService, never()).updateListOfRespondentsWithAnEcc(any());
        verify(caseManagementForCaseWorkerService, never()).setCaseNameHmctsInternal(any());
        verify(caseManagementForCaseWorkerService, never()).setPublicCaseName(any());
        verify(caseFlagsService).setupCaseFlags(caseData);
        verify(caseManagementForCaseWorkerService).updateWorkAllocationField(anyList(), eq(caseData));
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private CaseData caseDataWithRespondentsAndRepresentatives() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("12345/2026");
        caseData.setClaimant("Claimant Name");

        RespondentSumType respondent = RespondentSumType.builder().respondentName("Respondent Name").build();
        RespondentSumTypeItem respondentItem = new RespondentSumTypeItem();
        respondentItem.setId("resp-1");
        respondentItem.setValue(respondent);

        RepresentedTypeR representative = RepresentedTypeR.builder().nameOfRepresentative("Rep Name").build();
        RepresentedTypeRItem representedTypeRItem = RepresentedTypeRItem.builder()
            .id("rep-1")
            .value(representative)
            .build();

        caseData.setRespondentCollection(List.of(respondentItem));
        caseData.setRepCollection(List.of(representedTypeRItem));
        return caseData;
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
