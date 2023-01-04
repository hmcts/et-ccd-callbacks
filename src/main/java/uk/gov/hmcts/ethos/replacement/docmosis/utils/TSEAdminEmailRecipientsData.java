package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.Data;

@Data
public class TSEAdminEmailRecipientsData {

    private String recipientTemplate;
    private String recipientEmail;
    private String recipientName;

    public TSEAdminEmailRecipientsData(String recipientTemplate, String recipientEmail, String recipientName) {
        this.recipientTemplate = recipientTemplate;
        this.recipientEmail = recipientEmail;
        this.recipientName = recipientName;
    }

}
