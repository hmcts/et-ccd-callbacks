package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.EtCcdCallbacksConstants.CASE_NUMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.EtCcdCallbacksConstants.CLAIMANT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.EtCcdCallbacksConstants.RESPONDENTS;

@ExtendWith(SpringExtension.class)
class EmailServiceTest {

    @InjectMocks
    private transient EmailService emailService;
    @Mock
    private transient NotificationClient emailClient;

    private SendEmailResponse sendEmailResponse;

    @BeforeEach
    public void setUp() {
        sendEmailResponse = new SendEmailResponse("{\n"
            + "  \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
            + "  \"reference\": \"TEST_EMAIL_ALERT\",\n"
            + "  \"template\": {\n"
            + "    \"id\": \"8835039a-3544-439b-a3da-882490d959eb\",\n"
            + "    \"version\": \"3\",\n"
            + "    \"uri\": \"TEST\"\n"
            + "  },\n"
            + "  \"content\": {\n"
            + "    \"body\": \"test body\",\n"
            + "    \"subject\": \"ET Test email created\",\n"
            + "    \"from_email\": \"TEST@GMAIL.COM\"\n"
            + "  }\n"
            + "}\n");
    }

    @Test
    void sendEmail_success() throws NotificationClientException {
        when(emailClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenReturn(sendEmailResponse);

        emailService.sendEmail("templateId", "emailAddress", createPersonalisation());

        verify(emailClient, times(1)).sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void sendEmail_fail() throws NotificationClientException {
        when(emailClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(new NotificationClientException("FailedToSendEmail"));

        assertThatThrownBy(() -> emailService.sendEmail("templateId",
            "emailAddress", createPersonalisation()))
            .isInstanceOf(RuntimeException.class);
    }

    private Map<String, String> createPersonalisation() {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, "caseRef");
        personalisation.put("emailFlag", "URGENT");
        personalisation.put(CLAIMANT, "claimant");
        personalisation.put(RESPONDENTS, "Andrew Smith");
        personalisation.put("date", "11 Nov 2030");
        personalisation.put("body", "You have a new message about this employment tribunal case.");
        return personalisation;
    }

}