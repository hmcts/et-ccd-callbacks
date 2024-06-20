package uk.gov.hmcts.ethos.replacement.docmosis.helpers.multiples;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.DateUtils;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.ecm.common.model.helper.NotificationSchedulePayload;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.multiples.NotificationGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;

@Slf4j
public final class MultipleNotificationsHelper {

    private MultipleNotificationsHelper() {

    }

    /**
     * Flattened notifications joined with ethos case reference.
     *
     * @param schedulePayloads response
     * @param multipleRef      multiple case Id
     * @return list of notification group type with all data needed for report
     */
    public static @NotNull List<NotificationGroup> flattenNotificationsWithCaseRef(
            List<NotificationSchedulePayload> schedulePayloads, String multipleRef) {
        List<NotificationGroup> notificationGroups = new ArrayList<>();
        for (NotificationSchedulePayload schedulePayload : schedulePayloads) {
            for (SendNotificationTypeItem sendNotificationTypeItem : schedulePayload.getSendNotificationCollection()) {
                SendNotificationType notification = sendNotificationTypeItem.getValue();
                // Filter notifications sent from the multiple
                if (StringUtils.isNotEmpty(notification.getNotificationSentFrom())
                        && notification.getNotificationSentFrom().equals(multipleRef)) {

                    String responseReceived;
                    List<PseResponseTypeItem> responses;
                    if (isEmpty(notification.getRespondCollection())) {
                        responseReceived = NO;
                        responses = new ArrayList<>();
                    } else {
                        responseReceived = YES;
                        responses = notification.getRespondCollection();
                    }

                    notificationGroups.add(NotificationGroup.builder()
                            .caseNumber(schedulePayload.getEthosCaseRef())
                            .date(notification.getDate())
                            .responseReceived(responseReceived)
                            .notificationTitle(notification.getSendNotificationTitle())
                            .notificationSubjectString(notification.getSendNotificationSubjectString())
                            .respondCollection(responses)
                            .build()
                    );
                }
            }
        }
        return notificationGroups;
    }

    /**
     * Identify related notifications by grouping them by title and date.
     *
     * @param notificationGroups flat list of all notifications
     * @return grouped notifications
     */
    public static @NotNull Map<Pair<String, String>, List<NotificationGroup>> groupNotificationsByTitleAndDate(
            List<NotificationGroup> notificationGroups) {
        return notificationGroups.stream()
                .collect(groupingBy(notificationGroup ->
                        new ImmutablePair<>(
                                notificationGroup.getNotificationTitle(),
                                notificationGroup.getDate()))
                );
    }

    /**
     * Sort notifications so they come out in the same order each time.
     *
     * @param notificationsGroupedByTitle notifications for report
     * @return grouped notifications in ascending date order
     */
    public static @NotNull List<Map.Entry<Pair<String, String>,
            List<NotificationGroup>>> groupedNotificationsSortedByDate(Map<Pair<String, String>,
            List<NotificationGroup>> notificationsGroupedByTitle) {
        return
                notificationsGroupedByTitle.entrySet().stream().sorted(
                        Comparator.comparing(e ->
                                DateUtils.parseDate(e.getKey().getRight(), new String[]{MONTH_STRING_DATE_FORMAT})
                        )
                ).toList();
    }
}
