package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3ResponseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Et3ResponseSubmitSectionCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private Et3ResponseService et3ResponseService;

    private Et3ResponseSubmitSectionCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new Et3ResponseSubmitSectionCallbackHandler(caseDetailsConverter, et3ResponseService);
    }

    @Test
    void aboutToSubmitShouldSetRepresentativeContactWhenEventIsEt3Response() throws Exception {
        CaseData caseData = new CaseData();
        Et3ResponseSubmitSectionCallbackHandler spyHandler = spy(handler);
        doReturn(ccdRequest(caseData, "et3Response"))
            .when(spyHandler).toCcdRequest(any(CaseDetails.class));

        spyHandler.aboutToSubmit(callbackCaseDetails());

        verify(et3ResponseService).setRespondentRepresentsContactDetails(eq(null), eq(caseData), eq("123"));
    }

    @Test
    void submittedShouldReturnSectionCompleteLinks() {
        stubConverter(new CaseData());

        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("Download draft ET3 Form");
        assertThat(response.getConfirmationBody()).contains("/cases/case-details/123/trigger/et3Response");
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
