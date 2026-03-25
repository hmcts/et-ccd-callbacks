package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultiplesSendNotificationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationMultiplesSendNotificationCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private MultiplesSendNotificationService multiplesSendNotificationService;

    private SendNotificationMultiplesSendNotificationCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SendNotificationMultiplesSendNotificationCallbackHandler(
            caseDetailsConverter,
            multiplesSendNotificationService
        );
    }

    @Test
    void aboutToSubmitShouldDelegateToNotificationServiceMethods() {
        MultipleData caseData = new MultipleData();
        MultipleDetails multipleDetails = multipleDetails(caseData, "ET_EnglandWales_Multiple", "123");
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class)))
            .thenReturn(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());

        handler.aboutToSubmit(callbackCaseDetails());

        verify(multiplesSendNotificationService)
            .sendNotificationToSingles(eq(caseData), eq(multipleDetails), isNull(), anyList());
        verify(multiplesSendNotificationService).setSendNotificationDocumentsToDocumentCollection(caseData);
        verify(multiplesSendNotificationService).clearSendNotificationFields(caseData);
    }

    @Test
    void submittedShouldBuildConfirmationBodyWithCaseId() {
        MultipleDetails multipleDetails = multipleDetails(new MultipleData(), "ET_EnglandWales_Multiple", "123");
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class)))
            .thenReturn(multipleDetails);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());

        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("/cases/case-details/123/trigger/sendNotification");
    }

    private MultipleDetails multipleDetails(MultipleData caseData, String caseTypeId, String caseId) {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(caseData);
        multipleDetails.setCaseTypeId(caseTypeId);
        multipleDetails.setCaseId(caseId);
        multipleDetails.setState("Open");
        return multipleDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales_Multiple")
            .state("Open")
            .build();
    }
}
