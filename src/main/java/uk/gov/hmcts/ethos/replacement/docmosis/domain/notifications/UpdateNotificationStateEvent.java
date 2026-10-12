package uk.gov.hmcts.ethos.replacement.docmosis.domain.notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.DecentralisedConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig.PlaceholderRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.NotificationViewRepository;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
@RequiredArgsConstructor
public class UpdateNotificationStateEvent implements CCDConfig<CaseData, CaseState, PlaceholderRole> {

    public static final String EVENT_ID = "UPDATE_NOTIFICATION_STATE";

    private final NotificationViewRepository notificationViewRepository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public void configureDecentralised(DecentralisedConfigBuilder<CaseData, CaseState, PlaceholderRole> builder) {
        // Record views in our table without touching the case data blob, avoiding conflicts with other updates.
        builder.decentralisedEvent(EVENT_ID, this::submit).forAllStates();
    }

    private SubmitResponse<CaseState> submit(EventPayload<CaseData, CaseState> payload) {
        long caseReference = payload.caseReference();
        Set<String> alreadyViewed = notificationViewRepository.findItemIds(caseReference);
        NotificationViews.viewedItemIds(payload.caseData()).stream()
            .filter(id -> !alreadyViewed.contains(id))
            .forEach(id -> notificationViewRepository.markViewed(caseReference, id));
        return SubmitResponse.defaultResponse();
    }
}
