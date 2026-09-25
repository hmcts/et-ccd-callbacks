package uk.gov.hmcts.ethos.replacement.docmosis.domain.notifications;

import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * Applies the claimant's views recorded in the notification_view table to the "viewed" markers held in the
 * case data blob.
 */
public final class NotificationViews {

    private NotificationViews() {
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
            // in the blob, and its new id will not have been recorded yet. Responses in other states, such as
            // one the claimant has replied to, are never marked viewed so cannot be what reset it.
            if (NOT_VIEWED_YET.equals(notification.getNotificationState())
                && viewedIds.contains(item.getId())
                && tribunalResponses.stream()
                    .filter(NotificationViews::isUnviewed)
                    .allMatch(response -> viewedIds.contains(response.getId()))) {
                notification.setNotificationState(VIEWED);
            }
            for (GenericTypeItem<RespondNotificationType> response : tribunalResponses) {
                if (isUnviewed(response) && viewedIds.contains(response.getId())) {
                    response.getValue().setState(VIEWED);
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

    private static boolean isUnviewed(GenericTypeItem<RespondNotificationType> response) {
        RespondNotificationType value = response.getValue();
        // Matches the claimant notification service, which treats a blank state as unviewed.
        return value != null && (isNullOrEmpty(value.getState()) || NOT_VIEWED_YET.equals(value.getState()));
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
}
