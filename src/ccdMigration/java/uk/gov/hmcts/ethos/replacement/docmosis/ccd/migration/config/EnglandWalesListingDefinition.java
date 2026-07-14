package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseType;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Jurisdiction;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesDefinition;
import uk.gov.hmcts.et.common.model.ccd.ListingRole;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.ListingCaseState;

import static uk.gov.hmcts.et.common.model.ccd.ListingRole.EMPLOYMENT_API;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.ENGLAND_WALES_CASEWORKER;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.ENGLAND_WALES_JUDGE;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.RAS_VALIDATION;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.WA_TASK_CONFIGURATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.BRISTOL;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.LEEDS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.LONDON_CENTRAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.LONDON_EAST;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.LONDON_SOUTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.MANCHESTER;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.MIDLANDS_EAST;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.MIDLANDS_WEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.NEWCASTLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.UNASSIGNED;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.WALES;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.WATFORD;

@Component
public class EnglandWalesListingDefinition implements
    CCDConfig<ListingData, ListingCaseState, ListingRole> {

    static final String CASE_TYPE = "ET_EnglandWales_Listings";

    @Override
    public String groupingKey() {
        return CASE_TYPE;
    }

    @Override
    public void configure(ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder) {
        builder.caseType(CaseType.builder()
            .id(CASE_TYPE)
            .name("Eng/Wales - Hearings/Reports")
            .description("England/Wales - Hearings/Reports")
            .printableDocumentsUrl("${CCD_DEF_URL}/callback/jurisdictions/EMPLOYMENT/"
                + "case-types/ET_EnglandWales_Listings/documents")
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
        builder.schemaProfile(EnglandWalesDefinition.class);
        builder.applicableRoles(ENGLAND_WALES_CASEWORKER, ENGLAND_WALES_JUDGE,
            EMPLOYMENT_API, WA_TASK_CONFIGURATION, RAS_VALIDATION);
        builder.legacyCaseAuthorisationIdColumn();
        builder.registerFixedList("fl_TribunalOffice", BRISTOL, LEEDS, LONDON_CENTRAL,
            LONDON_EAST, LONDON_SOUTH, MANCHESTER, MIDLANDS_EAST, MIDLANDS_WEST,
            NEWCASTLE, WALES, WATFORD, UNASSIGNED);
        ListingDefinitionSupport.configure(builder, false);
    }
}
