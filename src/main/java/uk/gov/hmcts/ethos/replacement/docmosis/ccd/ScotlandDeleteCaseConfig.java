package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;

import java.util.EnumSet;

@Component
public class ScotlandDeleteCaseConfig implements CCDConfig<ScotlandCaseData, EtState, EtUserRole> {

    @Override
    public void configure(ConfigBuilder<ScotlandCaseData, EtState, EtUserRole> configBuilder) {
        configBuilder.jurisdiction("EMPLOYMENT", "Employment Tribunal", "Employment Tribunal");
        configBuilder.caseType("ET_Scotland", "Scotland - Singles (RET) ", "Scotland - Singles (RET)");
        configBuilder.caseTypeColumn(
            "PrintableDocumentsUrl",
            "${CCD_DEF_URL}/callback/jurisdictions/EMPLOYMENT/case-types/ET_Scotland/documents"
        );
        configBuilder.caseTypeColumn("EnableForDeletion", "Yes");
        configBuilder.eventDefaults()
            .omitLiveFrom()
            .omitPublish()
            .noEndButtonLabel();

        configBuilder.event("DELETE_CASE")
            .forStateTransition(EnumSet.allOf(EtState.class), EtState.DELETE)
            .name("Delete Case")
            .description("Delete case")
            .displayOrder(1000)
            .significantEvent()
            .ttlIncrement("0")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("manageCaseTTL")
            .forAllStates()
            .name("Manage Case TTL")
            .description("Manage Case TTL")
            .displayOrder(100)
            .showCondition("caseType=\"dummy\"")
            .fields()
            .page("11")
            .pageDisplayOrder(11)
            .complex(ScotlandCaseData::getTtl)
            .done()
            .done()
            .grant(Permission.CRU, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        ttlMigrationEvent(configBuilder, "migrateCaseTTLDetails", "Migrate Case TTL", 200)
            .description("Migrate Case TTL Details")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        ttlMigrationEvent(configBuilder, "rollbackMigrateCaseTTLDetails", "Rollback Migrate Case TTL", 201)
            .description("Rollback Migrate Case TTL Details")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("DELETE_DRAFT_CASE")
            .forStateTransition(EtState.AWAITING_SUBMISSION_TO_HMCTS, EtState.DELETE)
            .name("Delete draft case")
            .description("Delete draft cases as a claimant")
            .displayOrder(1000)
            .significantEvent()
            .ttlIncrement("0")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API, EtUserRole.CREATOR);
    }

    private Event.EventBuilder<ScotlandCaseData, EtUserRole, EtState> ttlMigrationEvent(
        ConfigBuilder<ScotlandCaseData, EtState, EtUserRole> configBuilder,
        String eventId,
        String name,
        int displayOrder
    ) {
        return configBuilder.event(eventId)
            .forAllStates()
            .name(name)
            .displayOrder(displayOrder);
    }
}
