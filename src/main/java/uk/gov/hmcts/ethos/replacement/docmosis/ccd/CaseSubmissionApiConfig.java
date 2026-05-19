package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public abstract class CaseSubmissionApiConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        configBuilder.event("UPDATE_CASE_SUBMITTED")
            .forAllStates()
            .name("UPDATE_CASE_SUBMITTED")
            .description("Update a submitted case")
            .displayOrder(5)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/postDefaultValues")
            .fields()
            .page("1")
            .field(CaseData::getHubLinksStatuses)
            .mandatory()
            .caseEventColumn("PageFieldDisplayOrder", null)
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .field(CaseData::getEt1OnlineSubmission)
            .mandatory()
            .caseEventColumn("PageFieldDisplayOrder", null)
            .caseEventColumn("PageColumnNumber", null)
            .done()
            .done()
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API, EtUserRole.CITIZEN);
    }
}
