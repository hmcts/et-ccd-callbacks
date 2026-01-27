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
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.SendNotificationUtil.getPseResponseType;

@ExtendWith(SpringExtension.class)
class MultipleNotificationsHelperTest {
    private static final String MULTIPLE_REF = "60001";
    private static final String ETHOS_CASE_REFERENCE_1 = "6047765/2023";
    private static final String ETHOS_CASE_REFERENCE_2 = "6047766/2024";
    private static final String DATE_6_AUG_2022 = "6 Aug 2022";
    private static final String DATE_7_AUG_2022 = "7 Aug 2022";
    private static final String SUBMIT_HEARING_AGENDA = "Submit hearing agenda";
    private static final String VIEW_NOTICE_OF_HEARING = "View notice of hearing";

    List<NotificationSchedulePayload> schedulePayloads;

    @BeforeEach
    void setUp() {
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
    void shouldOrderGroupNotificationsByDate() {
        List<Map.Entry<Pair<String, String>, List<NotificationGroup>>> result = MultipleNotificationsHelper
                .flattenGroupAndSortNotificationsWithCaseRef(schedulePayloads, MULTIPLE_REF);

        assertEquals(2, result.size(), "There should be 2 groups of notifications");

        // Group 1 assertions
        var firstGroup = result.getFirst();
        assertEquals(Pair.of(SUBMIT_HEARING_AGENDA, DATE_6_AUG_2022), firstGroup.getKey(),
                "First group key should match");
        assertEquals(2, firstGroup.getValue().size(), "First group should have 2 notifications");
        for (NotificationGroup notification : firstGroup.getValue()) {
            assertEquals(DATE_6_AUG_2022, notification.getDate(), "Notification date should be 6 Aug 2022");
            assertEquals(YES, notification.getResponseReceived(), "Notification response should be YES");
        }

        // Group 2 assertions
        var secondGroup = result.get(1);
        assertEquals(Pair.of(VIEW_NOTICE_OF_HEARING, DATE_7_AUG_2022), secondGroup.getKey(),
                "Second group key should match");
        assertEquals(2, secondGroup.getValue().size(), "Second group should have 2 notifications");
        for (NotificationGroup notification : secondGroup.getValue()) {
            assertEquals(DATE_7_AUG_2022, notification.getDate(), "Notification date should be 7 Aug 2022");
            assertEquals(NO, notification.getResponseReceived(), "Notification response should be NO");
        }
    }

    @Test
    void shouldFormatMultipleReplies() {
        List<PseResponseTypeItem> responses =
                SendNotificationUtil.sendNotificationNotifyBothPartiesWithResponse().getValue().getRespondCollection();
        String result = MultipleNotificationsHelper.getAndFormatReplies(responses);
        assertEquals("""
                Reply: Please cancel
                Name: Barry White, Claimant
                -------------
                Reply: Please cancel
                Name: Barry White, Claimant""", result);
    }

    @Test
    void shouldFormatSingleReply() {
        List<PseResponseTypeItem> responses = List.of(getPseResponseType());
        String result = MultipleNotificationsHelper.getAndFormatReplies(responses);
        assertEquals("""
                Reply: Please cancel
                Name: Barry White, Claimant""", result);
    }
}
