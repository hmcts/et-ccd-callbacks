package uk.gov.hmcts.ethos.replacement.docmosis.domain.notifications;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_STARTED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NOT_VIEWED_YET;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.VIEWED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

class NotificationViewsTest {

    @Test
    void collectsIdsOfEveryViewedItem() {
        CaseData caseData = caseData(VIEWED, VIEWED, VIEWED, YES);
        caseData.getSendNotificationCollection().add(notification("unviewed", NOT_VIEWED_YET, List.of()));

        assertThat(NotificationViews.viewedItemIds(caseData))
            .containsExactlyInAnyOrder("notification", "tribunalResponse", "partyResponse", "tseResponse");
    }

    @Test
    void collectsNothingFromCaseWithoutNotificationsOrApplications() {
        assertThat(NotificationViews.viewedItemIds(new CaseData())).isEmpty();
    }

    @Test
    void appliesRecordedViewsToUnviewedItems() {
        CaseData caseData = caseData(NOT_VIEWED_YET, NOT_VIEWED_YET, null, null);

        NotificationViews.apply(caseData,
            Set.of("notification", "tribunalResponse", "partyResponse", "tseResponse"));

        SendNotificationType notification = caseData.getSendNotificationCollection().getFirst().getValue();
        assertThat(notification.getNotificationState()).isEqualTo(VIEWED);
        assertThat(notification.getRespondNotificationTypeCollection().getFirst().getValue().getState())
            .isEqualTo(VIEWED);
        assertThat(notification.getRespondCollection().getFirst().getValue().getResponseState()).isEqualTo(VIEWED);
        assertThat(tseResponse(caseData).getViewedByClaimant()).isEqualTo(YES);
    }

    @Test
    void leavesItemsWithoutRecordedViewsUnchanged() {
        CaseData caseData = caseData(NOT_VIEWED_YET, NOT_VIEWED_YET, null, null);

        NotificationViews.apply(caseData, Set.of("somethingElse"));

        SendNotificationType notification = caseData.getSendNotificationCollection().getFirst().getValue();
        assertThat(notification.getNotificationState()).isEqualTo(NOT_VIEWED_YET);
        assertThat(notification.getRespondNotificationTypeCollection().getFirst().getValue().getState())
            .isEqualTo(NOT_VIEWED_YET);
        assertThat(notification.getRespondCollection().getFirst().getValue().getResponseState()).isNull();
        assertThat(tseResponse(caseData).getViewedByClaimant()).isNull();
    }

    @Test
    void tribunalResponseAddedAfterViewingKeepsNotificationUnviewed() {
        CaseData caseData = caseData(NOT_VIEWED_YET, VIEWED, null, null);
        SendNotificationType notification = caseData.getSendNotificationCollection().getFirst().getValue();
        notification.getRespondNotificationTypeCollection().add(tribunalResponse("newResponse", NOT_VIEWED_YET));

        NotificationViews.apply(caseData, Set.of("notification", "tribunalResponse"));

        assertThat(notification.getNotificationState()).isEqualTo(NOT_VIEWED_YET);
        assertThat(notification.getRespondNotificationTypeCollection().get(1).getValue().getState())
            .isEqualTo(NOT_VIEWED_YET);
    }

    @Test
    void neverOverridesWorkflowStatesOtherThanUnviewed() {
        CaseData caseData = caseData(NOT_STARTED_YET, NOT_STARTED_YET, null, null);

        NotificationViews.apply(caseData, Set.of("notification", "tribunalResponse"));

        SendNotificationType notification = caseData.getSendNotificationCollection().getFirst().getValue();
        assertThat(notification.getNotificationState()).isEqualTo(NOT_STARTED_YET);
        assertThat(notification.getRespondNotificationTypeCollection().getFirst().getValue().getState())
            .isEqualTo(NOT_STARTED_YET);
    }

    @Test
    void treatsMissingTribunalResponseStateAsUnviewed() {
        CaseData caseData = caseData(NOT_VIEWED_YET, null, null, null);

        NotificationViews.apply(caseData, Set.of("notification", "tribunalResponse"));

        SendNotificationType notification = caseData.getSendNotificationCollection().getFirst().getValue();
        assertThat(notification.getNotificationState()).isEqualTo(VIEWED);
        assertThat(notification.getRespondNotificationTypeCollection().getFirst().getValue().getState())
            .isEqualTo(VIEWED);
    }

    static CaseData caseData(String notificationState, String tribunalResponseState,
                             String partyResponseState, String viewedByClaimant) {
        SendNotificationTypeItem notification = notification("notification", notificationState,
            new ArrayList<>(List.of(tribunalResponse("tribunalResponse", tribunalResponseState))));
        notification.getValue().setRespondCollection(List.of(PseResponseTypeItem.builder()
            .id("partyResponse")
            .value(PseResponseType.builder().responseState(partyResponseState).build())
            .build()));

        CaseData caseData = new CaseData();
        caseData.setSendNotificationCollection(new ArrayList<>(List.of(notification)));
        caseData.setGenericTseApplicationCollection(List.of(GenericTseApplicationTypeItem.builder()
            .id("application")
            .value(GenericTseApplicationType.builder()
                .respondCollection(List.of(TseRespondTypeItem.builder()
                    .id("tseResponse")
                    .value(TseRespondType.builder().viewedByClaimant(viewedByClaimant).build())
                    .build()))
                .build())
            .build()));
        return caseData;
    }

    private static SendNotificationTypeItem notification(String id, String state,
                                                         List<GenericTypeItem<RespondNotificationType>> responses) {
        return SendNotificationTypeItem.builder()
            .id(id)
            .value(SendNotificationType.builder()
                .notificationState(state)
                .respondNotificationTypeCollection(responses)
                .build())
            .build();
    }

    private static GenericTypeItem<RespondNotificationType> tribunalResponse(String id, String state) {
        return GenericTypeItem.<RespondNotificationType>builder()
            .id(id)
            .value(RespondNotificationType.builder().state(state).build())
            .build();
    }

    private static TseRespondType tseResponse(CaseData caseData) {
        return caseData.getGenericTseApplicationCollection().getFirst().getValue()
            .getRespondCollection().getFirst().getValue();
    }
}
