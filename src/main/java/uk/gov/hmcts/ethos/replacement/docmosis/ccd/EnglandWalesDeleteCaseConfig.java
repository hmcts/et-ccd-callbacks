package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import java.util.EnumSet;

@Component
public class EnglandWalesDeleteCaseConfig implements CCDConfig<EnglandWalesCaseData, EtState, EtUserRole> {

    @Override
    public void configure(ConfigBuilder<EnglandWalesCaseData, EtState, EtUserRole> configBuilder) {
        configBuilder.jurisdiction("EMPLOYMENT", "Employment Tribunal", "Employment Tribunal");
        configBuilder.caseType("ET_EnglandWales", "Eng/Wales - Singles", "England/Wales - Singles");
        configBuilder.caseTypeColumn(
            "PrintableDocumentsUrl",
            "${CCD_DEF_URL}/callback/jurisdictions/EMPLOYMENT/case-types/ET_EnglandWales/documents"
        );
        configBuilder.caseTypeColumn("EnableForDeletion", "Yes");

        configBuilder.event("DELETE_CASE")
            .forStateTransition(EnumSet.allOf(EtState.class), EtState.DELETE)
            .name("Delete Case")
            .description("Delete case")
            .displayOrder(1000)
            .endButtonLabel("")
            .omitLiveFrom()
            .omitPublish()
            .significantEvent()
            .ttlIncrement("0")
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("manageCaseTTL")
            .forAllStates()
            .name("Manage Case TTL")
            .description("Manage Case TTL")
            .displayOrder(100)
            .showCondition("caseType=\"dummy\"")
            .endButtonLabel("")
            .omitLiveFrom()
            .omitPublish()
            .fields()
            .page("1")
            .complex(EnglandWalesCaseData::getTtl)
            .done()
            .done()
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }
}
