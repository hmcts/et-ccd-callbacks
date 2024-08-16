package uk.gov.hmcts.ethos.replacement.docmosis.helpers.multiples;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.NotificationSchedulePayload;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.multiples.NotificationGroup;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.SendNotificationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class MultipleNotificationsHelperTest {
    public static final String MULTIPLE_REF = "60001";
    public static final String ETHOS_CASE_REFERENCE_1 = "6047765/2023";
    public static final String ETHOS_CASE_REFERENCE_2 = "6047766/2024";
    List<NotificationSchedulePayload> schedulePayloads;

    @BeforeEach
    public void setUp() {
        schedulePayloads = new ArrayList<>();
        List<NotificationSchedulePayload> list1 = MultipleUtil
                .getNotificationSchedulePayloadList(ETHOS_CASE_REFERENCE_1, MULTIPLE_REF);
        List<NotificationSchedulePayload> list2 = MultipleUtil
                .getNotificationSchedulePayloadList(ETHOS_CASE_REFERENCE_2, MULTIPLE_REF);
        List<NotificationSchedulePayload> list3 = MultipleUtil
                .getNotificationSchedulePayloadList(ETHOS_CASE_REFERENCE_2, "60002");
        schedulePayloads.addAll(list1);
        schedulePayloads.addAll(list2);
        schedulePayloads.addAll(list3);
    }

    @Test
    void shouldGetNotificationSchedulePayloadsWithMultipleRefAndSetResponseReceived() {
        List<NotificationGroup> result = MultipleNotificationsHelper
                .flattenNotificationsWithCaseRef(schedulePayloads, MULTIPLE_REF);

        assertEquals(4, result.size());
        for (int i = 0; i < result.size(); i++) {
            if (i % 2 == 0) {
                assertEquals(YES, result.get(i).getResponseReceived());
            } else {
                assertEquals(NO, result.get(i).getResponseReceived());
            }
        }
    }

    @Test
    void shouldGroupNotificationsByTitleAndDate() {
        List<NotificationGroup> notificationGroups = MultipleNotificationsHelper
                .flattenNotificationsWithCaseRef(schedulePayloads, MULTIPLE_REF);
        Map<Pair<String, String>, List<NotificationGroup>> result =
                MultipleNotificationsHelper.groupNotificationsByTitleAndDate(notificationGroups);
        List<NotificationGroup> group1 = result.get(Pair.of("View notice of hearing", "7 Aug 2022"));
        List<NotificationGroup> group2 = result.get(Pair.of("Submit hearing agenda", "6 Aug 2022"));
        assertEquals(2, result.size());
        assertEquals(2, group1.size());
        assertEquals(2, group2.size());
    }

    @Test
    void shouldOrderGroupNotificationsByDate() {
        List<NotificationGroup> notificationGroups = MultipleNotificationsHelper
                .flattenNotificationsWithCaseRef(schedulePayloads, MULTIPLE_REF);
        Map<Pair<String, String>, List<NotificationGroup>> grouped =
                MultipleNotificationsHelper.groupNotificationsByTitleAndDate(notificationGroups);

        List<Map.Entry<Pair<String, String>, List<NotificationGroup>>> result =
                MultipleNotificationsHelper.groupedNotificationsSortedByDate(grouped);

        assertEquals(2, result.size());
        assertEquals("6 Aug 2022", result.get(0).getKey().getRight());
        assertEquals("7 Aug 2022", result.get(1).getKey().getRight());
    }

    @Test
    void shouldFormatReply() {
        List<PseResponseTypeItem> responses =
                SendNotificationUtil.sendNotificationNotifyBothPartiesWithResponse().getValue().getRespondCollection();
        String result = MultipleNotificationsHelper.getAndFormatReplies(responses);
        assertEquals("""
                Reply: Please cancel
                Name: Barry White, Claimant
                -------------
                Reply: Please cancel
                Name: Barry White, Claimant
                -------------""", result);
    }
}
