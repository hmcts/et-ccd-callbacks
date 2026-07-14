package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.ccd.sdk.api.Webhook;
import uk.gov.hmcts.et.common.model.ccd.ListingRole;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.ListingCaseState;

import java.util.Set;

import static uk.gov.hmcts.ccd.sdk.api.Permission.CRU;
import static uk.gov.hmcts.ccd.sdk.api.Permission.CRUD;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.EMPLOYMENT_API;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.ENGLAND_WALES_CASEWORKER;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.ENGLAND_WALES_JUDGE;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.RAS_VALIDATION;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.SCOTLAND_CASEWORKER;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.SCOTLAND_JUDGE;
import static uk.gov.hmcts.et.common.model.ccd.ListingRole.WA_TASK_CONFIGURATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.ListingCaseState.Submitted;
import static uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.ListingCaseState.SubmittedReport;

final class ListingDefinitionSupport {

    private static final String DATE_RANGE = "hearingDateType=\"Range\"";
    private static final String SINGLE_DATE = "hearingDateType=\"Single\"";
    private static final String RETAIN_HIDDEN = "Yes";
    private static final String DATE_RANGE_MID_EVENT =
        "${ET_COS_URL}/listingsDateRangeMidEventValidation";
    private static final String LISTING_TABLE =
        "#TABLE(causeListDate, causeListTime, causeListVenue, elmoCaseReference, "
            + "Hearing_room, jurisdictionCodesList, hearingType, estHearingLength)";

    private ListingDefinitionSupport() {
    }

    static void configure(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        boolean scotland
    ) {
        configureStates(builder);
        configureAccessProfiles(builder, scotland);
        configureEvents(builder, scotland);
        configureTabs(builder, scotland);
        configureSearch(builder, scotland);
    }

    private static void configureStates(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder
    ) {
        builder.grant(Submitted, CRU, ENGLAND_WALES_CASEWORKER, ENGLAND_WALES_JUDGE,
            SCOTLAND_CASEWORKER, SCOTLAND_JUDGE, WA_TASK_CONFIGURATION);
        builder.grant(Submitted, CRUD, EMPLOYMENT_API);
        builder.grant(Submitted, Set.of(Permission.R), RAS_VALIDATION);
        builder.grant(SubmittedReport, CRU, ENGLAND_WALES_CASEWORKER, ENGLAND_WALES_JUDGE,
            SCOTLAND_CASEWORKER, SCOTLAND_JUDGE, WA_TASK_CONFIGURATION);
        builder.grant(SubmittedReport, CRUD, EMPLOYMENT_API);
        builder.grant(SubmittedReport, Set.of(Permission.R), RAS_VALIDATION);
    }

    private static void configureAccessProfiles(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        boolean scotland
    ) {
        ListingRole caseworker = scotland ? SCOTLAND_CASEWORKER : ENGLAND_WALES_CASEWORKER;
        ListingRole judge = scotland ? SCOTLAND_JUDGE : ENGLAND_WALES_JUDGE;
        addAccessProfile(builder, caseworker);
        addAccessProfile(builder, judge);
        addAccessProfile(builder, EMPLOYMENT_API);
        addAccessProfile(builder, WA_TASK_CONFIGURATION);
        addAccessProfile(builder, RAS_VALIDATION);
    }

    private static void addAccessProfile(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        ListingRole role
    ) {
        builder.caseRoleToAccessProfile(role)
            .legacyIdamRole()
            .retainLiveFrom()
            .omitDisabled()
            .accessProfiles(role.getRole());
    }

    private static void configureEvents(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        boolean scotland
    ) {
        configureCreateCase(builder);
        configureGenerateListing(builder, scotland);
        configureHearingDocumentation(builder);
        configurePrintCauseList(builder);
        configureFixReport(builder);
        configureCreateReport(builder, scotland);
        configureGenerateReport(builder, scotland);
    }

    private static Event.EventBuilder<ListingData, ListingRole, ListingCaseState> event(
        Event.EventBuilder<ListingData, ListingRole, ListingCaseState> event,
        String name,
        int displayOrder
    ) {
        return event.name(name)
            .description(null)
            .displayOrder(displayOrder)
            .omitPublish();
    }

    private static void configureCreateCase(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder
    ) {
        var event = event(builder.event("createCase").initialState(Submitted), "Create Case", 1)
            .omitEndButtonLabel()
            .externalCallbackUrl(Webhook.AboutToSubmit, "${ET_COS_URL}/listingCaseCreation")
            .grant(Permission.CRUD, EMPLOYMENT_API);
        var fields = event.fields();
        fields.field(ListingData::getHearingDocType).mandatory().showSummary().done();
        fields.field(ListingData::getManagingOffice).mandatory().showSummary().done();
    }

    private static void configureGenerateListing(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        boolean scotland
    ) {
        var event = event(builder.event("generateListing").forState(Submitted),
            "Generate Report", 2)
            .omitEndButtonLabel()
            .externalCallbackUrl(Webhook.AboutToStart, "${ET_COS_URL}/dynamicListingVenue")
            .externalCallbackUrl(Webhook.AboutToSubmit, "${ET_COS_URL}/listingHearings")
            .grant(CRU, ENGLAND_WALES_CASEWORKER, ENGLAND_WALES_JUDGE,
                SCOTLAND_CASEWORKER, SCOTLAND_JUDGE)
            .grant(Permission.CRUD, EMPLOYMENT_API);
        var fields = event.fields().page("unused").page("2");
        mandatory(fields, ListingData::getHearingDateType, null, 1, 2, false);
        mandatory(fields, ListingData::getListingDateFrom, DATE_RANGE, 2, 2, true);
        mandatory(fields, ListingData::getListingDateTo, DATE_RANGE, 3, 2, true)
            .externalMidEventCallbackUrl(DATE_RANGE_MID_EVENT);
        mandatory(fields, ListingData::getListingDate, SINGLE_DATE, 2, 2, true);
        mandatory(fields, ListingData::getHearingDocType, "listingVenue=\"dummy\"", 4, 2, true);
        mandatory(fields, ListingData::getHearingDocETCL,
            "hearingDocType=\"ETCL - Cause List\"", 5, 2, true);
        mandatory(fields, ListingData::getRoomOrNoRoom,
            "hearingDocETCL=\"Public\" OR hearingDocETCL=\"Staff\"", 6, 2, true);
        mandatory(fields, ListingData::getShowAll,
            "hearingDocType=\"ETCL - Cause List\" AND hearingDocETCL=\"Staff\"",
            7, scotland ? 1 : 2, true);
        if (scotland) {
            fields.field(ListingData::getManagingOffice)
                .readOnly()
                .showSummary()
                .pageFieldDisplayOrder(8)
                .pageDisplayOrder(2)
                .done();
            mandatory(fields, ListingData::getListingVenue, null, 9, 2, false);
        } else {
            mandatory(fields, ListingData::getListingVenue, null, 8, 2, false);
        }
    }

    private static void configureHearingDocumentation(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder
    ) {
        event(builder.event("hearingDocumentation").forState(Submitted),
            "Hearing Documentation", 3)
            .endButtonLabel("Print Cause List");
    }

    private static void configurePrintCauseList(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder
    ) {
        event(builder.event("printCauseList").forState(Submitted), "Print List", 4)
            .endButtonLabel("Print List")
            .externalCallbackUrl(Webhook.AboutToSubmit,
                "${ET_COS_URL}/generateHearingDocument")
            .externalCallbackUrl(Webhook.Submitted,
                "${ET_COS_URL}/generateHearingDocumentConfirmation")
            .grant(CRU, ENGLAND_WALES_CASEWORKER, ENGLAND_WALES_JUDGE,
                SCOTLAND_CASEWORKER, SCOTLAND_JUDGE)
            .grant(Permission.CRUD, EMPLOYMENT_API);
    }

    private static void configureFixReport(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder
    ) {
        var event = event(builder.event("fixReport").forState(Submitted), "Fix Report API", 5)
            .omitEndButtonLabel()
            .grant(Permission.CRUD, EMPLOYMENT_API);
        event.fields().field(ListingData::getHearingDocType).optional().showSummary().done();
    }

    private static void configureCreateReport(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        boolean scotland
    ) {
        var event = event(builder.event("createReport").initialState(SubmittedReport),
            "Create Report", 5)
            .omitEndButtonLabel()
            .externalCallbackUrl(Webhook.AboutToSubmit, "${ET_COS_URL}/listingCaseCreation")
            .grant(CRU, ENGLAND_WALES_CASEWORKER, SCOTLAND_CASEWORKER)
            .grant(Permission.D, SCOTLAND_CASEWORKER)
            .grant(Permission.CRUD, EMPLOYMENT_API);
        var fields = event.fields();
        fields.field(ListingData::getReportType)
            .mandatory()
            .showSummary()
            .immutable()
            .done();
        if (!scotland) {
            fields.field(ListingData::getManagingOffice).mandatory().showSummary().done();
        }
    }

    private static void configureGenerateReport(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        boolean scotland
    ) {
        var event = event(builder.event("generateReport").forState(SubmittedReport),
            "Generate Report", 6)
            .endButtonLabel("Generate Report")
            .externalCallbackUrl(Webhook.AboutToSubmit, "${ET_COS_URL}/generateReport")
            .externalCallbackUrl(Webhook.Submitted,
                "${ET_COS_URL}/generateHearingDocumentConfirmation")
            .grant(CRU, ENGLAND_WALES_CASEWORKER, ENGLAND_WALES_JUDGE,
                SCOTLAND_CASEWORKER, SCOTLAND_JUDGE)
            .grant(Permission.CRUD, EMPLOYMENT_API);
        var fields = event.fields();
        if (scotland) {
            fields.showCondition("reportType != \"Cases Awaiting Judgment\"");
            mandatory(fields, ListingData::getHearingDateType,
                "reportType !=\"No Change In Current Position\"", 1, 1, true);
            mandatory(fields, ListingData::getListingDateFrom, DATE_RANGE, 2, 1, true);
            mandatory(fields, ListingData::getListingDateTo, DATE_RANGE, 3, 1, true)
                .externalMidEventCallbackUrl(DATE_RANGE_MID_EVENT);
            mandatory(fields, ListingData::getListingDate, SINGLE_DATE, 4, 1, true);
            optional(fields, ListingData::getReportType, "hearingDateType=\"dummy\"", 5, true);
            mandatory(fields, ListingData::getReportDate,
                "reportType=\"No Change In Current Position\"", 6, 1, true);
        } else {
            mandatory(fields, ListingData::getHearingDateType,
                "reportType !=\"No Change In Current Position\" AND "
                    + "reportType != \"Cases Awaiting Judgment\"", 1, 1, true);
            mandatory(fields, ListingData::getReportDate,
                "reportType=\"No Change In Current Position\"", 2, 1, true);
            mandatory(fields, ListingData::getListingDateFrom, DATE_RANGE, 3, 1, true);
            mandatory(fields, ListingData::getListingDateTo, DATE_RANGE, 4, 1, true)
                .externalMidEventCallbackUrl(DATE_RANGE_MID_EVENT);
            mandatory(fields, ListingData::getListingDate, SINGLE_DATE, 5, 1, true);
            mandatory(fields, ListingData::getReportType, "hearingDateType=\"dummy\"", 6, 1, true)
                .pageLabel("f");
        }
        optional(fields, ListingData::getClerkResponsible, "reportType=\"dummy\"", 7, true);
    }

    private static <T> uk.gov.hmcts.ccd.sdk.api.Field.FieldBuilder<
        T, ListingCaseState, ListingData,
        Event.EventBuilder<ListingData, ListingRole, ListingCaseState>> mandatory(
        FieldCollection.FieldCollectionBuilder<ListingData, ListingCaseState,
            Event.EventBuilder<ListingData, ListingRole, ListingCaseState>> fields,
        uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter<ListingData, T> getter,
        String condition,
        int fieldOrder,
        int pageOrder,
        boolean retainHidden
    ) {
        var field = fields.field(getter)
            .mandatory()
            .showCondition(condition)
            .showSummary()
            .pageFieldDisplayOrder(fieldOrder)
            .pageDisplayOrder(pageOrder);
        if (retainHidden) {
            field.retainHiddenValue(RETAIN_HIDDEN);
        }
        return field;
    }

    private static <T> uk.gov.hmcts.ccd.sdk.api.Field.FieldBuilder<
        T, ListingCaseState, ListingData,
        Event.EventBuilder<ListingData, ListingRole, ListingCaseState>> optional(
        FieldCollection.FieldCollectionBuilder<ListingData, ListingCaseState,
            Event.EventBuilder<ListingData, ListingRole, ListingCaseState>> fields,
        uk.gov.hmcts.ccd.sdk.api.TypedPropertyGetter<ListingData, T> getter,
        String condition,
        int fieldOrder,
        boolean retainHidden
    ) {
        var field = fields.field(getter)
            .optional()
            .showCondition(condition)
            .showSummary()
            .pageFieldDisplayOrder(fieldOrder)
            .pageDisplayOrder(1);
        if (retainHidden) {
            field.retainHiddenValue(RETAIN_HIDDEN);
        }
        return field;
    }

    private static void configureTabs(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        boolean scotland
    ) {
        causeListTab(builder);
        it56Tab(builder, scotland);
        etrpTab(builder);
        reportTab(builder, scotland);
        if (!scotland) {
            memberDaysTab(builder);
        }
    }

    private static void causeListTab(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder
    ) {
        builder.tab("ListingsOverview", "Cause List")
            .metadataOnFirstFieldOnly()
            .showCondition("hearingDocType=\"ETCL - Cause List\"")
            .field("hearingDocType", "hearingDocType=\"ETCL - Cause List\"", 1)
            .field("hearingDateType", "hearingDateType=\"dummy\"", 1)
            .field("listingDate", "listingDate=\"dummy\"", 1)
            .field("listingDateFrom", "listingDate=\"dummy\"", 1)
            .field("listingDateTo", "listingDate=\"dummy\"", 1)
            .field("listingLabel", SINGLE_DATE, 2)
            .field("listingLabelRange", DATE_RANGE, 2)
            .field("listingCollection", null, 3, LISTING_TABLE);
    }

    private static void it56Tab(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        boolean scotland
    ) {
        var tab = builder.tab("IT56Overview", "IT56 - List of Exhibits")
            .metadataOnFirstFieldOnly();
        if (scotland) {
            tab.showCondition("hearingDocType=\"IT56 - List of Exhibits\"")
                .showConditionOnField("listingDate")
                .field("hearingDateType", "hearingDateType=\"dummy\"", 1)
                .field("listingDate", "listingDate=\"dummy\"", 1);
        } else {
            tab.showCondition("hearingDocType=\"IT56 - List of Exhibits\"")
                .field("hearingDateType", "hearingDateType=\"dummy\"", 1)
                .field("listingDate", "listingDate=\"dummy\"", 1);
        }
        tab.field("listingDateFrom", "listingDate=\"dummy\"", 1)
            .field("listingDateTo", "listingDate=\"dummy\"", 1)
            .field("listingLabel", SINGLE_DATE, 2)
            .field("listingLabelRange", DATE_RANGE, 2)
            .field("listingCollection", null, 3, LISTING_TABLE);
    }

    private static void etrpTab(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder
    ) {
        builder.tab("IT57Overview", "ETRP - Recording of Proceedings")
            .metadataOnFirstFieldOnly()
            .showCondition("hearingDocType=\"ETRP - ET recording of proceeding\"")
            .field("listingDate", "listingDate=\"dummy\"", 1)
            .field("hearingDateType", "hearingDateType=\"dummy\"", 1)
            .field("listingDateFrom", "listingDate=\"dummy\"", 1)
            .field("listingDateTo", "listingDate=\"dummy\"", 1)
            .field("listingLabel", SINGLE_DATE, 2)
            .field("listingLabelRange", DATE_RANGE, 2)
            .field("listingCollection", null, 3, LISTING_TABLE);
    }

    private static void reportTab(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        boolean scotland
    ) {
        builder.tab(scotland ? "ReportOverview" : "BroughtForwardReport",
                "Brought Forward Report")
            .metadataOnFirstFieldOnly()
            .showCondition("reportType=\"Brought Forward Report\"")
            .field("reportType", "reportType=\"Brought Forward Report\"", 1)
            .field("hearingDateType", "hearingDateType=\"dummy\"", 1)
            .field("listingDate", "listingDate=\"dummy\"", 1)
            .field("listingDateFrom", "listingDate=\"dummy\"", 1)
            .field("listingDateTo", "listingDate=\"dummy\"", 1)
            .field("listingLabel", SINGLE_DATE, 2)
            .field("listingLabelRange", DATE_RANGE, 2)
            .field("bfDateCollection", null, 3, scotland
                ? "#TABLE(caseReference, broughtForwardDate, broughtForwardDateReason, "
                    + "broughtForwardDateCleared)"
                : "#TABLE(caseReference, broughtForwardDate, broughtForwardDateReason)");
    }

    private static void memberDaysTab(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder
    ) {
        builder.tab("MemberDayReport", "Member Days Report")
            .metadataOnFirstFieldOnly()
            .showCondition("reportType=\"Member Days\"")
            .field("reportType", "reportType=\"Member Days\"", 1)
            .field("hearingDateType", "hearingDateType=\"dummy\"", 1)
            .field("listingDate", "listingDate=\"dummy\"", 1)
            .field("listingDateFrom", "listingDate=\"dummy\"", 1)
            .field("listingDateTo", "listingDate=\"dummy\"", 1)
            .field("listingLabel", SINGLE_DATE, 2)
            .field("listingLabelRange", DATE_RANGE, 2);
    }

    private static void configureSearch(
        ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder,
        boolean scotland
    ) {
        builder.searchInputFields().field(ListingData::getDocumentName, "Document Name");
        builder.searchResultFields().field(ListingData::getDocumentName, "Document Name");
        if (scotland) {
            builder.searchInputFields().field(ListingData::getListingVenue, "Managing Office");
            builder.searchResultFields().field(ListingData::getListingVenue, "Managing Office");
        }
        builder.workBasketInputFields()
            .field(ListingData::getManagingOffice, "Managing Office")
            .field(ListingData::getDocumentName, "Document Name");
        builder.workBasketResultFields()
            .field(ListingData::getDocumentName, "Document Name")
            .field(ListingData::getManagingOffice, "Managing Office");
    }
}
