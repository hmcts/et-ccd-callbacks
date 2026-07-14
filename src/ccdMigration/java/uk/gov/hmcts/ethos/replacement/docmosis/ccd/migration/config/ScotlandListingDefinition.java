package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseType;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Jurisdiction;
import uk.gov.hmcts.et.common.model.ccd.ListingRole;
import uk.gov.hmcts.et.common.model.ccd.ScotlandDefinition;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.ListingCaseState;

import static uk.gov.hmcts.et.common.model.ccd.ListingRole.EMPLOYMENT_API;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.RAS_VALIDATION;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.SCOTLAND_CASEWORKER;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.SCOTLAND_JUDGE;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.WA_TASK_CONFIGURATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.ABERDEEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.ALL;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.DUNDEE;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.EDINBURGH;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.GLASGOW;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.UNASSIGNED;

@Component
public class ScotlandListingDefinition implements
    CCDConfig<ListingData, ListingCaseState, ListingRole> {

    static final String CASE_TYPE = "ET_Scotland_Listings";

    @Override
    public String groupingKey() {
        return CASE_TYPE;
    }

    @Override
    public void configure(ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder) {
        builder.caseType(CaseType.builder()
            .id(CASE_TYPE)
            .name("Scotland - Reports (RET)")
            .description("Scotland - Hearings/Reports (RET)")
            .printableDocumentsUrl("${CCD_DEF_URL}/callback/jurisdictions/EMPLOYMENT/"
                + "case-types/ET_Scotland_Listings/documents")
            .enableForDeletion(false)
            .build());
        builder.jurisdiction(Jurisdiction.builder()
            .id("EMPLOYMENT")
            .name("Employment")
            .description("Employment")
            .shuttered(true)
            .build());
        builder.omitDefaultLiveFrom();
        builder.omitCaseHistory();
        builder.schemaProfile(ScotlandDefinition.class);
        builder.applicableRoles(SCOTLAND_CASEWORKER, SCOTLAND_JUDGE,
            EMPLOYMENT_API, WA_TASK_CONFIGURATION, RAS_VALIDATION);
        builder.legacyCaseAuthorisationIdColumn();
        builder.retainCaseTypeAuthorisationLiveFrom(
            SCOTLAND_CASEWORKER, SCOTLAND_JUDGE, EMPLOYMENT_API);
        builder.registerFixedList("VenueScotlandAll", GLASGOW, ABERDEEN, DUNDEE,
            EDINBURGH, UNASSIGNED, ALL);
        ListingDefinitionSupport.configure(builder, true);
    }
}
