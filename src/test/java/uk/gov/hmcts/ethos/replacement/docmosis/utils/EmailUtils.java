package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.ethos.replacement.docmosis.service.EmailService;
import uk.gov.service.notify.NotificationClient;

import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE_PARAM;

public class EmailUtils extends EmailService {
    public EmailUtils() {
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

    @Override
    public String getSyrCaseLink(String caseId, String respondentId) {
        return "syrUrl" + caseId + "/" + respondentId;
    }

    @Override
    public String getRespondentCitizenCaseLink(String caseId, String respondentId, boolean welsh) {
        String link = "syrUrl/case-details/" + caseId + "/" + respondentId;
        if (welsh) {
            return link + WELSH_LANGUAGE_PARAM;
        }
        return link;
    }

    @Override
    public String getExuiHearingDocumentsLink(String caseId) {
        return "exuiUrl" + caseId + "#Hearing%20Documents";
    }
}
