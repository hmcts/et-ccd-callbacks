package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateDraftEt1CallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Et1ReppedService et1ReppedService;

    private CreateDraftEt1CallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateDraftEt1CallbackHandler(caseDetailsConverter, et1ReppedService);
    }

    @Test
    void aboutToSubmitShouldInvokeDraftCreationServices() {
        CaseData caseData = new CaseData();
        caseData.setRespondentType("ORGANISATION");
        caseData.setRespondentOrganisationName("Resp Org");
        final uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails = stubConverter(caseData);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(et1ReppedService).addDefaultData("ET_EnglandWales", caseData);
        verify(et1ReppedService).addClaimantRepresentativeDetails(caseData, null);
        verify(et1ReppedService).createDraftEt1(ccdCaseDetails, null);
    }

    @Test
    void submittedShouldReturnDraftDownloadConfirmationBody() {
        CaseData caseData = new CaseData();
        caseData.setDocMarkUp("http://doc/link");
        stubConverter(caseData);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("Please download the draft ET1 from :");
        assertThat(response.getConfirmationBody()).contains("http://doc/link");
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
