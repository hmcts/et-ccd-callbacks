package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class RetainAndDisposeConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        configBuilder.event("deleteDraftCase")
            .forStateTransition(EtState.AWAITING_SUBMISSION_TO_HMCTS, EtState.DELETE)
            .name("Delete Draft Case")
            .description("Delete draft case as a legal representative before submission")
            .displayOrder(1000)
            .significantEvent()
            .ttlIncrement("0")
            .fields()
            .page("1")
            .field(CaseData::getDeleteDraftCaseWarningLabel)
            .readOnly()
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(CaseData::getDeleteDraftCaseWarning1)
            .readOnly()
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(CaseData::getDeleteDraftCaseWarning2)
            .readOnly()
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .done()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_LEGALREP_SOLICITOR);
    }
}
