package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class FixCaseApiConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String POST_CONDITION_STATES =
        "Submitted(stateAPI=\"Submitted\"):1;Accepted(stateAPI=\"Accepted\"):2;"
            + "Rejected(stateAPI=\"Rejected\"):3;Transferred(stateAPI=\"Transferred\"):4;"
            + "Closed(stateAPI=\"Closed\"):5;Vetted(stateAPI=\"Vetted\"):6;*";

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        fixCaseApiFields(
            configBuilder.event("fixCaseAPI")
                .forAllStates()
                .name("Fix Case API")
                .description("Fix Case API")
                .displayOrder(1)
                .caseEventColumn("PostConditionState", POST_CONDITION_STATES)
        )
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> fixCaseApiFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        return event.fields()
            .page("1")
            .field(CaseData::getMultipleFlag)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getEcmCaseType)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getMultipleReference)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getPositionType)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getReasonForCT)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getEthosCaseReference)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getJurCodesCollection)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getPreAcceptCase)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getStateAPI)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getJudgementCollection)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getSendNotificationCollection)
            .optional()
            .showSummary()
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .done();
    }
}
