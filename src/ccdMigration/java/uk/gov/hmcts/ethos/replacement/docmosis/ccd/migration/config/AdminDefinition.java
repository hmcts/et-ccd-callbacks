package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.CaseType;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Jurisdiction;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminState;

import java.time.LocalDate;

import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.BRISTOL;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.CLERK;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.EMPLOYEE_MEMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.EMPLOYER_MEMBER;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.FEE_PAID;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.LEEDS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.LONDON_CENTRAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.LONDON_EAST;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.LONDON_SOUTH;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.MANCHESTER;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.MIDLANDS_EAST;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.MIDLANDS_WEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.NEWCASTLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.SALARIED;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.SCOTLAND;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.UNKNOWN;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.WALES;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.AdminFixedListValue.WATFORD;
import static uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminRole.EMPLOYMENT_API;

@Component
public class AdminDefinition implements CCDConfig<AdminData, AdminState, AdminRole> {

    static final String CASE_TYPE = "ET_Admin";

    @Override
    public String groupingKey() {
        return CASE_TYPE;
    }

    @Override
    public void configure(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.caseType(CaseType.builder()
            .id(CASE_TYPE)
            .name("ECM Admin")
            .description("ECM Administration")
            .liveFrom(LocalDate.of(2017, 1, 1))
            .retriesTimeoutUrlPrintEvent(20)
            .build());
        builder.jurisdiction(Jurisdiction.builder()
            .id("EMPLOYMENT")
            .name("Employment")
            .description("Employment")
            .shuttered(true)
            .build());
        builder.omitDefaultLiveFrom();
        builder.caseHistoryLabel("History");
        builder.omitHistoryForRoles(EMPLOYMENT_API);

        registerFixedLists(builder);
        configureCreateEvent(builder);
        configureTabs(builder);
        configureSearch(builder);
        configureComplexTypeAccess(builder);
    }

    private void registerFixedLists(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.registerFixedList("importOffice", BRISTOL, LEEDS, LONDON_CENTRAL, LONDON_EAST,
            LONDON_SOUTH, MANCHESTER, MIDLANDS_EAST, MIDLANDS_WEST, NEWCASTLE, SCOTLAND, WALES,
            WATFORD);
        builder.registerFixedList("fl_EmploymentStatus", FEE_PAID, SALARIED, UNKNOWN);
        builder.registerFixedList("fl_CourtWorker", CLERK, EMPLOYEE_MEMBER, EMPLOYER_MEMBER);
    }

    private void configureCreateEvent(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.event("create")
            .initialState(AdminState.Open)
            .name("Create Admin Case")
            .displayOrder(1)
            .omitEndButtonLabel()
            .omitShowSummary()
            .omitShowEventNotes()
            .omitPublish()
            .externalCallbackUrl(
                uk.gov.hmcts.ccd.sdk.api.Webhook.AboutToSubmit,
                "${ET_COS_URL}/admin/create/aboutToSubmitEvent"
            )
            .grant(Permission.CRUD, EMPLOYMENT_API);
    }

    private void configureTabs(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.tab("importFiles", "Import Files")
            .withoutChannel()
            .field(AdminData::getStaffImportFile)
            .field(AdminData::getVenueImport);
        builder.tab("CaseHistory", "History")
            .withoutChannel()
            .field("caseHistory");
    }

    private void configureSearch(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.searchInputFields().field(AdminData::getName, "Name");
        builder.searchResultFields().field(AdminData::getName, "Name");
        builder.workBasketInputFields().field(AdminData::getName, "Name");
        builder.workBasketResultFields().field(AdminData::getName, "Name");
    }

    private void configureComplexTypeAccess(ConfigBuilder<AdminData, AdminState, AdminRole> builder) {
        builder.grantComplexType(AdminData::getVenueImport, "venueImportFile", Permission.CRUD,
            EMPLOYMENT_API);
        builder.grantComplexType(AdminData::getVenueImport, "venueImportOffice", Permission.CRUD,
            EMPLOYMENT_API);
        builder.grantComplexType(AdminData::getAdminCourtWorker, "tribunalOffice", Permission.CRUD,
            EMPLOYMENT_API);
        builder.grantComplexType(AdminData::getAdminCourtWorker, "courtWorkerType", Permission.CRUD,
            EMPLOYMENT_API);
        builder.grantComplexType(AdminData::getAdminCourtWorker, "courtWorkerCode", Permission.CRUD,
            EMPLOYMENT_API);
        builder.grantComplexType(AdminData::getAdminCourtWorker, "courtWorkerName", Permission.CRUD,
            EMPLOYMENT_API);
    }
}
