package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.TseService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.respondent.RespondentTellSomethingElseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.TSEConstants.RESPONDENT_REP_TITLE;

@ExtendWith(MockitoExtension.class)
class RespondentTSECallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private RespondentTellSomethingElseService resTseService;
    @Mock
    private TseService tseService;
    @Mock
    private CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    private RespondentTSECallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RespondentTSECallbackHandler(
            caseDetailsConverter,
            verifyTokenService,
            resTseService,
            tseService,
            caseManagementForCaseWorkerService
        );
    }

    @Test
    void aboutToSubmitShouldNotProceedWhenTokenInvalid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(tseService, never()).createApplication(any(), any());
        verify(resTseService, never()).sendEmails(any(), any());
    }

    @Test
    void aboutToSubmitShouldCreateRespondentApplicationWhenTokenValid() {
        CaseData caseData = new CaseData();
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(tseService).createApplication(caseData, RESPONDENT_REP_TITLE);
        verify(resTseService).sendEmails(any(), any());
        verify(resTseService).generateAndAddTsePdf(caseData, null, "ET_EnglandWales");
        verify(tseService).clearApplicationData(caseData);
        verify(caseManagementForCaseWorkerService).setNextListedDate(caseData);
    }

    @Test
    void submittedShouldIncludeRule92CopyMessageWhenYes() {
        CaseData caseData = new CaseData();
        GenericTseApplicationType application = new GenericTseApplicationType();
        application.setCopyToOtherPartyYesOrNo(YES);
        GenericTseApplicationTypeItem item = new GenericTseApplicationTypeItem();
        item.setValue(application);
        caseData.setGenericTseApplicationCollection(List.of(item));
        stubConverter(caseData);
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("sent a copy of your application to the claimant");
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
