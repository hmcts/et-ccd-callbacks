package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.Data;

@Data
public class TSEAdminEmailRecipientsData {

    private String recipientTemplate;
    private String recipientEmail;

    private String recipientName;
    private String customisedText;

    public TSEAdminEmailRecipientsData(String recipientTemplate, String recipientEmail) {
        this.recipientTemplate = recipientTemplate;
        this.recipientEmail = recipientEmail;
    }

}
