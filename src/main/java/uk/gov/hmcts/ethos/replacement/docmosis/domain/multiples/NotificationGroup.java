package uk.gov.hmcts.ethos.replacement.docmosis.domain.multiples;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationGroup {
    private String caseNumber;
    private String date;
    private String notificationTitle;
    private String responseReceived;
    private String notificationSubjectString;
}
