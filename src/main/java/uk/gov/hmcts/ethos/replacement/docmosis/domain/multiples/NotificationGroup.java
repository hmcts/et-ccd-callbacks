package uk.gov.hmcts.ethos.replacement.docmosis.domain.multiples;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;

import java.util.List;

@Data
@Builder
public class NotificationGroup {
    private String caseNumber;
    private String date;
    private String notificationTitle;
    private String responseReceived;
    private String notificationSubjectString;
    private List<PseResponseTypeItem> respondCollection;
}
