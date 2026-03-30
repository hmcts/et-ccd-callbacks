package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PseRespondToTribunalService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClaimantRespondToNotificationHandlerTest {

    @Mock
    private PseRespondToTribunalService pseRespondToTribunalService;

    private ClaimantRespondToNotificationHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ClaimantRespondToNotificationHandler(
            new CaseDetailsConverter(new ObjectMapper()),
            pseRespondToTribunalService
        );
    }

    @Test
    void aboutToSubmitShouldPersistAndClearClaimantResponse() {
        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(caseDetails());

        verify(pseRespondToTribunalService).saveClaimantResponse(any());
        verify(pseRespondToTribunalService).clearClaimantNotificationDetails(any());
        assertThat(response.getData()).isNotNull();
    }

    @Test
    void submittedShouldSendEmailsAndBuildConfirmationBody() {
        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(caseDetails());

        verify(pseRespondToTribunalService).sendEmailsForClaimantResponse(any(), isNull());
        assertThat(response.getConfirmationBody()).contains("What happens next");
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

