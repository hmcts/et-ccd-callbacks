package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;

import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;

public final class SendNotificationUtil {
    private SendNotificationUtil() {
    }

    public static SendNotificationTypeItem sendNotificationRequest() {
        return SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                        .number("1")
                        .date("5 Aug 2022")
                        .sendNotificationTitle("View notice of hearing")
                        .sendNotificationLetter(NO)
                        .sendNotificationSubject(List.of("Case management orders / requests"))
                        .sendNotificationCaseManagement("Request")
                        .sendNotificationResponseTribunal("No")
                        .sendNotificationRequestMadeBy("Judge")
                        .sendNotificationFullName("Mr Lee Gal Officer")
                        .sendNotificationNotify(BOTH_PARTIES)
                        .build())
                .build();
    }

    public static SendNotificationTypeItem sendNotificationNotifyBothParties() {
        return SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                        .number("1")
                        .date("7 Aug 2022")
                        .sendNotificationTitle("View notice of hearing")
                        .sendNotificationNotify(BOTH_PARTIES)
                        .build())
                .build();
    }

    public static SendNotificationTypeItem sendNotificationNotifyClaimant() {
        return SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                        .number("2")
                        .sendNotificationTitle("Submit hearing agenda")
                        .sendNotificationNotify(CLAIMANT_ONLY)
                        .build())
                .build();
    }

    public static SendNotificationTypeItem sendNotificationNotifyRespondent() {
        return SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                        .number("3")
                        .date("8 Aug 2022")
                        .sendNotificationTitle("Send Notification Title")
                        .sendNotificationNotify(RESPONDENT_ONLY)
                        .build())
                .build();
    }

    public static SendNotificationTypeItem sendNotificationNotifyBothPartiesWithResponse() {
        return SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                        .number("2")
                        .date("6 Aug 2022")
                        .sendNotificationTitle("Submit hearing agenda")
                        .sendNotificationNotify(BOTH_PARTIES)
                        .respondCollection(List.of(PseResponseTypeItem.builder()
                                .id(UUID.randomUUID().toString())
                                .value(PseResponseType.builder()
                                        .from(CLAIMANT_TITLE)
                                        .author("Barry White")
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    public static List<SendNotificationTypeItem> listOfSendNotificationTypeItemsOnMultiple(String multipleRef) {
        List<SendNotificationTypeItem> list = List.of(
                sendNotificationNotifyBothPartiesWithResponse(),
                sendNotificationNotifyBothParties(),
                sendNotificationRequest(),
                sendNotificationNotifyClaimant()
        );

        for (int i = 0; i < 2; i++) {
            list.get(i).getValue().setNotificationSentFrom(multipleRef);
        }
        return list;
    }
}
