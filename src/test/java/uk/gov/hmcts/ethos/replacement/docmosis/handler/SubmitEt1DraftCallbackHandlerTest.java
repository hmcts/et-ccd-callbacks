package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.CaseActionsForCaseWorkerCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(MockitoExtension.class)
class SubmitEt1DraftCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Et1ReppedService et1ReppedService;
    @Mock
    private CaseActionsForCaseWorkerCallbackService caseActionsForCaseWorkerCallbackService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private Et1SubmissionService et1SubmissionService;
    @Mock
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    private SubmitEt1DraftCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SubmitEt1DraftCallbackHandler(
            caseDetailsConverter,
            et1ReppedService,
            caseActionsForCaseWorkerCallbackService,
            featureToggleService,
            et1SubmissionService,
            nocRespondentRepresentativeService,
            caseManagementForCaseWorkerService
        );
    }

    @Test
    void aboutToSubmitShouldSetRequiresDocumentsWhenDocGenFeatureEnabled() {
        CaseData caseData = validEt1CaseData();
        stubConverter(caseData);
        when(featureToggleService.isEt1DocGenEnabled()).thenReturn(true);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData)).thenReturn(caseData);
        when(caseActionsForCaseWorkerCallbackService.postDefaultValues(any(), eq(null)))
            .thenReturn(ResponseEntity.ok(CCDCallbackResponse.builder().data(caseData).build()));

        handler.aboutToSubmit(callbackCaseDetails());

        verify(et1ReppedService).addDefaultData("ET_EnglandWales", caseData);
        verify(et1ReppedService).addClaimantRepresentativeDetails(caseData, null);
        verify(caseActionsForCaseWorkerCallbackService).postDefaultValues(any(), eq(null));
        verify(et1SubmissionService, never()).createAndUploadEt1Docs(any(), any());
        verify(et1SubmissionService).sendEt1ConfirmationMyHmcts(any(), eq(null));
        assertThat(caseData.getRequiresSubmissionDocuments()).isEqualTo(YES);
    }

    @Test
    void aboutToSubmitShouldGenerateDocumentsWhenDocGenFeatureDisabled() {
        CaseData caseData = validEt1CaseData();
        stubConverter(caseData);
        when(featureToggleService.isEt1DocGenEnabled()).thenReturn(false);
        when(nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData)).thenReturn(caseData);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(et1SubmissionService).createAndUploadEt1Docs(any(), eq(null));
        verify(et1SubmissionService).sendEt1ConfirmationMyHmcts(any(), eq(null));
    }

    @Test
    void submittedShouldReturnEt1SubmittedConfirmation() throws IOException {
        stubConverter(validEt1CaseData());

        var response = handler.submitted(callbackCaseDetails());

        verify(caseManagementForCaseWorkerService).setHmctsServiceIdSupplementary(any());
        assertThat(response.getConfirmationHeader()).contains("submitted the ET1 claim");
    }

    @Test
    void submittedShouldWrapIOException() throws IOException {
        stubConverter(validEt1CaseData());
        doThrow(new IOException("boom"))
            .when(caseManagementForCaseWorkerService).setHmctsServiceIdSupplementary(any());

        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to set HMCTS service id");
    }

    private CaseData validEt1CaseData() {
        CaseData caseData = new CaseData();
        caseData.setRespondentType("Organisation");
        caseData.setRespondentOrganisationName("Org Ltd");
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
