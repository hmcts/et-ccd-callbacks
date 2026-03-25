package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Et1ReppedAboutToSubmitSectionCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Et1ReppedService et1ReppedService;

    private Et1ReppedAboutToSubmitSectionCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new Et1ReppedAboutToSubmitSectionCallbackHandler(caseDetailsConverter, et1ReppedService);
    }

    @Test
    void aboutToSubmitShouldSetSectionStatusAndAddRepresentativeDetails() {
        CaseData caseData = new CaseData();
        Et1ReppedAboutToSubmitSectionCallbackHandler spyHandler = spy(handler);
        doReturn(ccdRequest(caseData, "et1SectionThree"))
            .when(spyHandler).toCcdRequest(any(CaseDetails.class));

        spyHandler.aboutToSubmit(callbackCaseDetails());

        verify(et1ReppedService).addClaimantRepresentativeDetails(caseData, null);
        assertThat(caseData.getEt1ReppedSectionThree()).isNotNull();
        assertThat(caseData.getEt1ClaimStatuses()).contains("123");
    }

    @Test
    void submittedShouldReturnSectionCompleteMessage() {
        CaseData caseData = new CaseData();
        caseData.setEt1ReppedSectionOne("Yes");
        caseData.setEt1ReppedSectionTwo("Yes");
        caseData.setEt1ReppedSectionThree("Yes");
        stubConverter(caseData);

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("Your answers have been saved");
    }

    private void stubConverter(CaseData caseData) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setCaseId("123");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
    }

    private CCDRequest ccdRequest(CaseData caseData, String eventId) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setCaseId("123");
        CCDRequest request = new CCDRequest();
        request.setCaseDetails(ccdCaseDetails);
        request.setEventId(eventId);
        return request;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}
