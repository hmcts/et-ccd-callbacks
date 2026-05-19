package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.listing.ListingData;

public abstract class ListingCaseTypeConfig<T extends ListingData> implements CCDConfig<T, EtState, EtUserRole> {

    private final String caseType;
    private final String name;
    private final String description;
    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;

    protected ListingCaseTypeConfig(
        String caseType,
        String name,
        String description,
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole
    ) {
        this.caseType = caseType;
        this.name = name;
        this.description = description;
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        configBuilder.jurisdiction("EMPLOYMENT", "Employment Tribunal", "Employment Tribunal");
        configBuilder.caseType(caseType, name, description);
        configBuilder.caseTypeColumn(
            "PrintableDocumentsUrl",
            "${CCD_DEF_URL}/callback/jurisdictions/EMPLOYMENT/case-types/" + caseType + "/documents"
        );
        configBuilder.caseTypeColumn("EnableForDeletion", "No");
        configBuilder.eventDefaults()
            .omitLiveFrom()
            .omitPublish()
            .noEndButtonLabel();

        configBuilder.event("hearingDocumentation")
            .forState(EtState.SUBMITTED)
            .name("Hearing Documentation")
            .caseEventColumn("Description", null)
            .displayOrder(3)
            .endButtonLabel("Print Cause List");

        configBuilder.event("printCauseList")
            .forState(EtState.SUBMITTED)
            .name("Print List")
            .caseEventColumn("Description", null)
            .displayOrder(4)
            .endButtonLabel("Print List")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/generateHearingDocument")
            .submittedCallbackUrl("${ET_COS_URL}/generateHearingDocumentConfirmation")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole);
    }
}
