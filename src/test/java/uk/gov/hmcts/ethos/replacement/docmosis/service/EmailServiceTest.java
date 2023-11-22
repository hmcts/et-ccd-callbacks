package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class EmailServiceTest {
    public static final String CITIZEN_HUB_URL = "https://et-sya.test.platform.hmcts.net/citizen-hub/";
    public static final String EXUI_URL = "https://manage-case.test.platform.hmcts.net/cases/case-details/";

    @InjectMocks
    private transient EmailService emailService;

    @Mock
    private transient NotificationClient emailClient;

    private SendEmailResponse sendEmailResponse;

    @BeforeEach
    public void setUp() {
        ReflectionTestUtils.setField(emailService, "exuiUrl", EXUI_URL);
        ReflectionTestUtils.setField(emailService, "citizenUrl", CITIZEN_HUB_URL);

        sendEmailResponse = new SendEmailResponse("""
            {
              "id": "8835039a-3544-439b-a3da-882490d959eb",
              "reference": "TEST_EMAIL_ALERT",
              "template": {
                "id": "8835039a-3544-439b-a3da-882490d959eb",
                "version": "3",
                "uri": "TEST"
              },
              "content": {
                "body": "test body",
                "subject": "ET Test email created",
                "from_email": "TEST@GMAIL.COM"
              }
            }
            """);
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
        personalisation.put("caseNumber", "caseRef");
        personalisation.put("emailFlag", "URGENT");
        personalisation.put("claimant", "claimant");
        personalisation.put("respondents", "Andrew Smith");
        personalisation.put("date", "11 Nov 2030");
        personalisation.put("body", "You have a new message about this employment tribunal case.");
        return personalisation;
    }

    @Test
    void getsCitizenCaseLink() {
        assertThat(emailService.getCitizenCaseLink("123"))
            .isEqualTo("https://et-sya.test.platform.hmcts.net/citizen-hub/123");
    }

    @Test
    void getsExuiCaseLink() {
        assertThat(emailService.getExuiCaseLink("123"))
            .isEqualTo("https://manage-case.test.platform.hmcts.net/cases/case-details/123");
    }
}