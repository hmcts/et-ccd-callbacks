package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.NotificationsExcelReportService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractNotificationsCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private NotificationsExcelReportService notificationsExcelReportService;

    private ExtractNotificationsCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ExtractNotificationsCallbackHandler(caseDetailsConverter, notificationsExcelReportService);
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
    }

    @Test
    void aboutToSubmitShouldTriggerNotificationExtractGeneration() {
        MultipleDetails multipleDetails = multipleDetails();
        stubMultipleConverter(multipleDetails);

        handler.aboutToSubmit(callbackCaseDetails());

        verify(notificationsExcelReportService).generateReportAsync(eq(multipleDetails), isNull(), anyList());
    }

    @Test
    void submittedShouldReturnExtractTaskSubmittedMessage() {
        var response = handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("Extract task submitted");
    }

    private void stubMultipleConverter(MultipleDetails multipleDetails) {
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class)))
            .thenReturn(multipleDetails);
    }

    private MultipleDetails multipleDetails() {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(new MultipleData());
        multipleDetails.setCaseTypeId("ET_EnglandWales_Multiple");
        multipleDetails.setCaseId("123");
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
