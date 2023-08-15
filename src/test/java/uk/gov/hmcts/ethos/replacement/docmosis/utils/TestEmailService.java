package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.service.notify.NotificationClient;

import static org.mockito.Mockito.mock;

public class TestEmailService extends EmailService {
    public TestEmailService() {
        super(mock(NotificationClient.class));
    }

    @Override
    public String getCitizenCaseLink(String caseId) {
        return "citizenUrl" + caseId;
    }

    @Override
    public String getExuiCaseLink(String caseId) {
        return "exuiUrl" + caseId;
    }
}
