package uk.gov.hmcts.ethos.replacement.docmosis.domain.hublink;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.DecentralisedConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig.PlaceholderRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd.HubLinkStatusRepository;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
@RequiredArgsConstructor
public class UpdateHubLinkStatusEvent implements CCDConfig<CaseData, CaseState, PlaceholderRole> {

    static final String EVENT_ID = "UPDATE_HUBLINK_STATUS";

    private final HubLinkStatusRepository hubLinkStatusRepository;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public void configureDecentralised(DecentralisedConfigBuilder<CaseData, CaseState, PlaceholderRole> builder) {
        // Decentralised events do not touch the legacy case_data.data json blob.
        // We persist to our table without touching the blob, avoiding conflicts.
        builder.decentralisedEvent(EVENT_ID, this::submit)
            .forAllStates();
    }

    private SubmitResponse<CaseState> submit(EventPayload<CaseData, CaseState> eventPayload) {
        CaseData data = eventPayload.caseData();
        if (data.getHubLinksStatuses() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hub-link statuses are required");
        }
        // Intentionally last write wins mirroring the SYA frontend client.
        // Do not copy this pattern for data requiring concurrency protection;
        // use versioning and optimistic locking by default.
        hubLinkStatusRepository.save(
            HubLinkStatus.create(eventPayload.caseReference(), data.getHubLinksStatuses())
        );

        return SubmitResponse.defaultResponse();
    }
}
