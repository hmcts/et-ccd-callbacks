package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.callback.SendNotificationCallbackService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendNotificationMultipleCallbackHandlerTest {

    @Mock
    private SendNotificationCallbackService sendNotificationCallbackService;

    private SendNotificationMultipleCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SendNotificationMultipleCallbackHandler(
            new CaseDetailsConverter(new ObjectMapper()),
            sendNotificationCallbackService
        );
    }

    @Test
    void aboutToSubmitShouldDelegateToCallbackService() {
        when(sendNotificationCallbackService.aboutToSubmit(any(), isNull()))
            .thenReturn(ResponseEntity.ok(CCDCallbackResponse.builder().build()));

        handler.aboutToSubmit(caseDetails());

        verify(sendNotificationCallbackService).aboutToSubmit(any(), isNull());
    }

    @Test
    void submittedShouldThrowUnsupportedException() {
        assertThatThrownBy(() -> handler.submitted(caseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private CaseDetails caseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .data(new HashMap<>())
            .build();
    }
}

