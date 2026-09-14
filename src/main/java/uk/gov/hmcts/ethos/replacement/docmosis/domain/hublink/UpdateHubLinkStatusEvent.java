package uk.gov.hmcts.ethos.replacement.docmosis.domain.hublink;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
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
    public void configure(ConfigBuilder<CaseData, CaseState, PlaceholderRole> builder) {
        builder.event(EVENT_ID)
            .forAllStates()
            .aboutToSubmitCallback((details, detailsBefore) -> submit(details));
    }

    private AboutToStartOrSubmitResponse<CaseData, CaseState> submit(
        CaseDetails<CaseData, CaseState> details
    ) {
        CaseData data = details.getData();
        if (data.getHubLinksStatuses() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hub-link statuses are required");
        }
        hubLinkStatusRepository.save(
            HubLinkStatus.create(details.getId(), data.getHubLinksStatuses())
        );

        return AboutToStartOrSubmitResponse.<CaseData, CaseState>builder()
            .data(data)
            .build();
    }
}
