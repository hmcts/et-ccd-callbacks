package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendRespondentRepresentativeCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private EventValidationService eventValidationService;
    @Mock
    private NocRespondentHelper nocRespondentHelper;
    @Mock
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;
    @Mock
    private FeatureToggleService featureToggleService;

    private AmendRespondentRepresentativeCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AmendRespondentRepresentativeCallbackHandler(
            caseDetailsConverter,
            eventValidationService,
            nocRespondentHelper,
            nocRespondentRepresentativeService,
            featureToggleService
        );
    }

    @Test
    void aboutToSubmitShouldReturnValidationErrorsWithoutCallingMutationServices() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(eventValidationService.validateRespRepNames(caseData))
            .thenReturn(new ArrayList<>(List.of("bad representative name")));

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getErrors()).containsExactly("bad representative name");
        verify(nocRespondentHelper, never()).updateWithRespondentIds(any());
        verify(nocRespondentRepresentativeService, never()).prepopulateOrgAddress(any(), any());
        verify(nocRespondentRepresentativeService, never()).updateNonMyHmctsOrgIds(any());
    }

    @Test
    void aboutToSubmitShouldUpdateOrgDataWhenValidationPasses() {
        CaseData caseData = new CaseData();
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().value(new RepresentedTypeR()).build()));
        stubConverter(caseData);
        when(eventValidationService.validateRespRepNames(caseData)).thenReturn(new ArrayList<>());
        when(nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, null)).thenReturn(caseData);
        when(featureToggleService.isHmcEnabled()).thenReturn(true);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        verify(nocRespondentHelper).updateWithRespondentIds(caseData);
        verify(nocRespondentRepresentativeService).prepopulateOrgAddress(caseData, null);
        verify(nocRespondentRepresentativeService).updateNonMyHmctsOrgIds(caseData.getRepCollection());
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void submittedShouldDelegateToAccessUpdateService() throws IOException, GenericServiceException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);

        handler.submitted(callbackCaseDetails());

        verify(nocRespondentRepresentativeService).updateRespondentRepresentativesAccess(any());
    }

    @Test
    void submittedShouldWrapIOExceptionFromAccessUpdateService() throws IOException, GenericServiceException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        doThrow(new IOException("ccd unavailable"))
            .when(nocRespondentRepresentativeService)
            .updateRespondentRepresentativesAccess(any());

        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(CcdInputOutputException.class)
            .hasMessageContaining("Failed to update respondent representatives accesses")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void submittedShouldWrapGenericServiceExceptionInRuntimeException()
        throws IOException, GenericServiceException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        doThrow(new GenericServiceException("generic error", new RuntimeException("boom"), "", "", "", ""))
            .when(nocRespondentRepresentativeService)
            .updateRespondentRepresentativesAccess(any());

        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(RuntimeException.class)
            .hasCauseInstanceOf(GenericServiceException.class);
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
