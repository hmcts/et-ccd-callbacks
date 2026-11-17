package uk.gov.hmcts.ethos.replacement.docmosis.domain.notifications.respondent;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.DecentralisedConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig.PlaceholderRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
public class ViewAllNotificationsEvent implements CCDConfig<CaseData, CaseState, PlaceholderRole> {

    static final String EVENT_ID = "viewAllNotifications";

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public void configureDecentralised(DecentralisedConfigBuilder<CaseData, CaseState, PlaceholderRole> builder) {
        // The existing about-to-start callback builds the view. Submitting it must not write to the case data blob.
        builder.decentralisedEvent(EVENT_ID, eventPayload -> SubmitResponse.defaultResponse())
            .forAllStates();
    }
}
