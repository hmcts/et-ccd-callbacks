package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;

@Component
public class ScotlandCaseFlagsConfig implements CCDConfig<ScotlandCaseData, EtState, EtUserRole> {

    @Override
    public void configure(ConfigBuilder<ScotlandCaseData, EtState, EtUserRole> configBuilder) {
        configBuilder.event("createFlag")
            .forAllStates()
            .name("Create a case flag")
            .description("Create Flag")
            .showSummary()
            .caseEventColumn("DisplayOrder", null)
            .caseEventColumn("EventEnablingCondition", "")
            .blankCallbackUrls()
            .fields()
            .page("1")
            .field(ScotlandCaseData::getCaseFlags)
            .optional()
            .showCondition("flagLauncher=\"hidden\"")
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("RetainHiddenValue", "Yes")
            .done()
            .field(ScotlandCaseData::getFlagLauncher)
            .optional()
            .displayContextParameter("#ARGUMENT(CREATE)")
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(ScotlandCaseData::getRespondentFlags)
            .optional()
            .showCondition("flagLauncher=\"hidden\"")
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("RetainHiddenValue", "Yes")
            .done()
            .field(ScotlandCaseData::getClaimantFlags)
            .optional()
            .showCondition("flagLauncher=\"hidden\"")
            .caseEventColumn("PageColumnNumber", null)
            .caseEventColumn("RetainHiddenValue", "Yes")
            .done()
            .done()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_SCOTLAND);
    }
}
