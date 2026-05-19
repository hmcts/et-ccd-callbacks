package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

public abstract class MultipleCaseTypeConfig<T extends MultipleData> implements CCDConfig<T, EtState, EtUserRole> {

    private final String caseType;
    private final String name;
    private final String description;
    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;

    protected MultipleCaseTypeConfig(
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

        configBuilder.event("amendMultipleAPI")
            .forAllStates()
            .name("Amend Multiple Details API")
            .description("Amend Multiple Details API")
            .displayOrder(4)
            .showCondition("multipleSource=\"dummy\"")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/amendMultipleAPI")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole);

        configBuilder.event("updatePayloadMultiple")
            .forAllStates()
            .name("Update Multiple via callback")
            .description("Updates payload when needed")
            .displayOrder(4)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/updatePayloadMultiple")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("resetMultipleState")
            .forAllStates()
            .name("Reset Multiple State")
            .description("Reset Multiple State")
            .displayOrder(11)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/resetMultipleState")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }
}
