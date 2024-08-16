package uk.gov.hmcts.ethos.replacement.docmosis.helpers.multiples;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.ScheduleConstants.NEW_LINE_CELL;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;

@Slf4j
public final class MultipleNotificationsHelper {
    private static final String UNKNOWN = "Unknown";

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
        return schedulePayloads.stream()
                .flatMap(schedulePayload -> {
                    String ethosCaseRef = schedulePayload.getEthosCaseRef();
                    return schedulePayload.getSendNotificationCollection().stream()
                            .map(SendNotificationTypeItem::getValue)
                            .filter(notification -> multipleRef.equals(notification.getNotificationSentFrom()))
                            .map(notification -> createNotificationGroup(ethosCaseRef, notification));
                })
                .toList();
    }

    private static NotificationGroup createNotificationGroup(String ethosCaseRef, SendNotificationType notification) {
        String responseReceived = isEmpty(notification.getRespondCollection()) ? NO : YES;
        List<PseResponseTypeItem> responses = Optional.ofNullable(notification.getRespondCollection())
                .orElseGet(ArrayList::new);

        return NotificationGroup.builder()
                .caseNumber(ethosCaseRef)
                .date(notification.getDate())
                .responseReceived(responseReceived)
                .notificationTitle(notification.getSendNotificationTitle())
                .notificationSubjectString(notification.getSendNotificationSubjectString())
                .respondCollection(responses)
                .build();
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

        // Create a cache for parsed dates to reduce overhead
        Map<Pair<String, String>, Date> dateCache = new HashMap<>();

        return notificationsGroupedByTitle.entrySet().stream()
                .sorted(Comparator.comparing(entry ->
                        dateCache.computeIfAbsent(entry.getKey(), key ->
                                DateUtils.parseDate(key.getRight(), new String[]{MONTH_STRING_DATE_FORMAT})
                        )
                ))
                .toList();
    }

    public static @NotNull String getAndFormatReplies(List<PseResponseTypeItem> respondCollection) {
        if (respondCollection.isEmpty()) {
            return "";
        }

        return respondCollection.stream()
                .map(PseResponseTypeItem::getValue)
                .map(response -> {
                    String name = isNotEmpty(response.getAuthor()) ? response.getAuthor() : UNKNOWN;
                    return new StringJoiner(NEW_LINE_CELL)
                            .add("Reply: " + response.getResponse())
                            .add("Name: " + name + ", " + response.getFrom())
                            .add("-------------")
                            .toString();
                })
                .collect(Collectors.joining(NEW_LINE_CELL));
    }
}
