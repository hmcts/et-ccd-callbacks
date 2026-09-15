package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import java.time.LocalDateTime;

public record CaseTimelineEvent(
    LocalDateTime created,
    String eventId,
    String eventName,
    String stateName,
    String userFirstName,
    String userLastName
) {
}
