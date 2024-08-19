package uk.gov.hmcts.ethos.replacement.docmosis.helpers.multiples;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.ecm.common.model.helper.NotificationSchedulePayload;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.multiples.NotificationGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.ScheduleConstants.NEW_LINE_CELL;

@Slf4j
public final class MultipleNotificationsHelper {
    private static final String UNKNOWN = "Unknown";
    private static final String REPLY_SEPARATOR = "-------------";
    private static final int DEFAULT_RESPONSE_CAPACITY = 200;

    private MultipleNotificationsHelper() {
    }

    /**
     * Flattens notifications and joins with ethos case reference.
     * Identifies related notifications by grouping them by title and date
     * Sorts notifications by date to preserve report order.
     *
     * @param schedulePayloads response
     * @param multipleRef      multiple case Id
     * @return list of notification group type with all data needed for report
     */
    public static @NotNull List<Map.Entry<Pair<String, String>, List<NotificationGroup>>>
        flattenGroupAndSortNotificationsWithCaseRef(List<NotificationSchedulePayload> schedulePayloads,
                                                String multipleRef) {
        Map<Pair<String, String>, List<NotificationGroup>> groupedNotifications = schedulePayloads.parallelStream()
                .flatMap(schedulePayload -> {
                    String ethosCaseRef = schedulePayload.getEthosCaseRef();
                    return schedulePayload.getSendNotificationCollection().stream()
                            .map(SendNotificationTypeItem::getValue)
                            .filter(notification -> multipleRef.equals(notification.getNotificationSentFrom()))
                            .map(notification -> createNotificationGroup(ethosCaseRef, notification));
                })
                .collect(Collectors.groupingByConcurrent(
                        notificationGroup -> new ImmutablePair<>(
                                notificationGroup.getNotificationTitle(),
                                notificationGroup.getDate()),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    list.sort(Comparator.comparing(NotificationGroup::getDate));
                                    return list;
                                }
                        )
                ));

        return new ArrayList<>(groupedNotifications.entrySet());
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

    public static @NotNull String getAndFormatReplies(List<PseResponseTypeItem> respondCollection) {
        if (respondCollection.isEmpty()) {
            return "";
        }

        int size = respondCollection.size();
        StringBuilder resultBuilder = new StringBuilder(DEFAULT_RESPONSE_CAPACITY * respondCollection.size());

        IntStream.range(0, size)
                .forEach(i -> {
                    PseResponseType response = respondCollection.get(i).getValue();
                    String name = isNotEmpty(response.getAuthor()) ? response.getAuthor() : UNKNOWN;
                    resultBuilder.append("Reply: ").append(response.getResponse()).append(NEW_LINE_CELL)
                            .append("Name: ").append(name).append(", ").append(response.getFrom());
                    if (i < size - 1) {
                        resultBuilder.append(NEW_LINE_CELL).append(REPLY_SEPARATOR).append(NEW_LINE_CELL);
                    }
                });

        return resultBuilder.toString();
    }
}
