package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RespondNotificationService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RespondNotificationCallbackHandlerTest {

    @Mock
    private RespondNotificationService respondNotificationService;

    private RespondNotificationCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RespondNotificationCallbackHandler(
            new CaseDetailsConverter(new ObjectMapper()),
            respondNotificationService
        );
    }

    @Test
    void aboutToSubmitShouldDelegateToRespondNotificationService() {
        handler.aboutToSubmit(caseDetails());

        verify(respondNotificationService).handleAboutToSubmit(any());
    }

    @Test
    void submittedShouldBuildConfirmationBody() {
        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(caseDetails());

        assertThat(response.getConfirmationBody()).contains("123");
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

