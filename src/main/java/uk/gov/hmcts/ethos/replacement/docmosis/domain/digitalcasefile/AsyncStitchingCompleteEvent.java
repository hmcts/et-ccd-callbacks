package uk.gov.hmcts.ethos.replacement.docmosis.domain.digitalcasefile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.DecentralisedConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.EventPayload;
import uk.gov.hmcts.ccd.sdk.api.callback.SubmitResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.EtJsonCcdConfig.PlaceholderRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DigitalCaseFileService;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
@RequiredArgsConstructor
public class AsyncStitchingCompleteEvent implements CCDConfig<CaseData, CaseState, PlaceholderRole> {

    public static final String EVENT_ID = "asyncStitchingComplete";

    private final DigitalCaseFileService digitalCaseFileService;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public void configureDecentralised(DecentralisedConfigBuilder<CaseData, CaseState, PlaceholderRole> builder) {
        builder.decentralisedEvent(EVENT_ID, this::submit).forAllStates();
    }

    private SubmitResponse<CaseState> submit(EventPayload<CaseData, CaseState> payload) {
        digitalCaseFileService.completeDcf(payload.caseReference(), payload.caseData());
        return SubmitResponse.defaultResponse();
    }
}
