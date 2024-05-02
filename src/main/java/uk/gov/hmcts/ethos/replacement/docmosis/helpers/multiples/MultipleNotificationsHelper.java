package uk.gov.hmcts.ethos.replacement.docmosis.helpers.multiples;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.client.utils.DateUtils;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.ecm.common.model.helper.NotificationSchedulePayload;
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

@Slf4j
public final class MultipleNotificationsHelper {

    public static final String DATE_FORMAT = "dd MMM yyyy";

    private MultipleNotificationsHelper() {

    }

    public static @NotNull List<NotificationGroup> flattenNotificationsWithCaseRef(
            List<NotificationSchedulePayload> schedulePayloads, String multipleRef) {
        List<NotificationGroup> notificationGroups = new ArrayList<>();
        for (NotificationSchedulePayload schedulePayload : schedulePayloads) {
            for (SendNotificationTypeItem sendNotificationTypeItem : schedulePayload.getSendNotificationCollection()) {
                SendNotificationType notification = sendNotificationTypeItem.getValue();
                // Filter notifications sent from the multiple
                if (StringUtils.isNotEmpty(notification.getNotificationSentFrom())
                        && notification.getNotificationSentFrom().equals(multipleRef)) {
                    String responseReceived = isEmpty(notification.getRespondCollection()) ? NO : YES;
                    notificationGroups.add(NotificationGroup.builder()
                            .caseNumber(schedulePayload.getEthosCaseRef())
                            .date(notification.getDate())
                            .responseReceived(responseReceived)
                            .notificationTitle(notification.getSendNotificationTitle())
                            .notificationSubjectString(notification.getSendNotificationSubjectString())
                            .build()
                    );
                }
            }
        }
        return notificationGroups;
    }

    public static @NotNull Map<Pair<String, String>, List<NotificationGroup>> groupNotificationsByTitleAndDate(
            List<NotificationGroup> notificationGroups) {
        return notificationGroups.stream()
                .collect(groupingBy(notificationGroup ->
                        new ImmutablePair<>(
                                notificationGroup.getNotificationTitle(),
                                notificationGroup.getDate()))
                );
    }

    public static @NotNull List<Map.Entry<Pair<String, String>,
            List<NotificationGroup>>> groupedNotificationsSortedByDate(Map<Pair<String, String>,
            List<NotificationGroup>> notificationsGroupedByTitle) {
        return
                notificationsGroupedByTitle.entrySet().stream().sorted(
                        Comparator.comparing(e ->
                                DateUtils.parseDate(e.getKey().getRight(), new String[]{DATE_FORMAT})
                        )
                ).toList();
    }
}
