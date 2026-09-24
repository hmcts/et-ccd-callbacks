package uk.gov.hmcts.ethos.replacement.docmosis.domain.notifications;

import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * Translates between the claimant's "viewed" markers held in the case data blob and the item ids
 * recorded in the notification_view table.
 */
public final class NotificationViews {

    private NotificationViews() {
    }

    /**
     * Collects the ids of every item the submitted case shows as viewed by the claimant.
     * Items already viewed in the blob are included; recording them again is harmless.
     */
    public static Set<String> viewedItemIds(CaseData caseData) {
        Set<String> ids = new HashSet<>();
        for (SendNotificationTypeItem item : nonNull(caseData.getSendNotificationCollection())) {
            SendNotificationType notification = item.getValue();
            if (notification == null) {
                continue;
            }
            if (VIEWED.equals(notification.getNotificationState())) {
                addId(ids, item.getId());
            }
            for (GenericTypeItem<RespondNotificationType> response
                : nonNull(notification.getRespondNotificationTypeCollection())) {
                if (response.getValue() != null && VIEWED.equals(response.getValue().getState())) {
                    addId(ids, response.getId());
                }
            }
            for (PseResponseTypeItem response : nonNull(notification.getRespondCollection())) {
                if (response.getValue() != null && VIEWED.equals(response.getValue().getResponseState())) {
                    addId(ids, response.getId());
                }
            }
        }
        for (TseRespondTypeItem response : tseResponses(caseData)) {
            if (response.getValue() != null && YES.equals(response.getValue().getViewedByClaimant())) {
                addId(ids, response.getId());
            }
        }
        return ids;
    }

    /**
     * Applies recorded views to the blob, only ever upgrading unviewed items to viewed.
     */
    public static void apply(CaseData caseData, Set<String> viewedIds) {
        if (viewedIds.isEmpty()) {
            return;
        }
        for (SendNotificationTypeItem item : nonNull(caseData.getSendNotificationCollection())) {
            SendNotificationType notification = item.getValue();
            if (notification == null) {
                continue;
            }
            List<GenericTypeItem<RespondNotificationType>> tribunalResponses =
                nonNull(notification.getRespondNotificationTypeCollection());
            // A tribunal response added after the claimant last looked resets the notification to unviewed
            // in the blob, and its new id will not have been recorded yet.
            if (NOT_VIEWED_YET.equals(notification.getNotificationState())
                && viewedIds.contains(item.getId())
                && tribunalResponses.stream().allMatch(response -> viewedIds.contains(response.getId()))) {
                notification.setNotificationState(VIEWED);
            }
            for (GenericTypeItem<RespondNotificationType> response : tribunalResponses) {
                RespondNotificationType value = response.getValue();
                if (value != null && viewedIds.contains(response.getId())
                    && (value.getState() == null || NOT_VIEWED_YET.equals(value.getState()))) {
                    value.setState(VIEWED);
                }
            }
            for (PseResponseTypeItem response : nonNull(notification.getRespondCollection())) {
                if (response.getValue() != null && viewedIds.contains(response.getId())) {
                    response.getValue().setResponseState(VIEWED);
                }
            }
        }
        for (TseRespondTypeItem response : tseResponses(caseData)) {
            if (response.getValue() != null && viewedIds.contains(response.getId())) {
                response.getValue().setViewedByClaimant(YES);
            }
        }
    }

    private static List<TseRespondTypeItem> tseResponses(CaseData caseData) {
        return nonNull(caseData.getGenericTseApplicationCollection()).stream()
            .map(GenericTseApplicationTypeItem::getValue)
            .filter(Objects::nonNull)
            .flatMap(application -> nonNull(application.getRespondCollection()).stream())
            .toList();
    }

    private static <T> List<T> nonNull(List<T> items) {
        return items == null ? List.of() : items.stream().filter(Objects::nonNull).toList();
    }

    private static void addId(Set<String> ids, String id) {
        if (id != null) {
            ids.add(id);
        }
    }
}
