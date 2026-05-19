package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.listing.ListingData;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Mandatory;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Optional;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.ReadOnly;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.EventFieldSpec.field;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.LegacyEventDefinition.event;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.LegacyEventDefinition.grant;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.ExcessiveMethodLength", "checkstyle:LineLength"})
public abstract class ListingCaseTypeConfig<T extends ListingData> implements CCDConfig<T, EtState, EtUserRole> {

    private final String caseType;
    private final String name;
    private final String description;
    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean scotland;

    protected ListingCaseTypeConfig(
        String caseType,
        String name,
        String description,
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean scotland
    ) {
        this.caseType = caseType;
        this.name = name;
        this.description = description;
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.scotland = scotland;
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

        listingEvents().forEach(spec -> LegacyEventDefinition.addTo(configBuilder, spec));
    }

    private List<LegacyEventDefinition.EventSpec> listingEvents() {
        return scotland ? scotlandListingEvents() : englandWalesListingEvents();
    }

    private List<LegacyEventDefinition.EventSpec> englandWalesListingEvents() {
        return List.of(
            event("createCase", "Create Case", null, 1, null, "Submitted", null, null, "${ET_COS_URL}/listingCaseCreation", null, null, null, false,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("hearingDocType", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("managingOffice", Mandatory, 1, 1, 2).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("generateListing", "Generate Report", null, 2, "Submitted", "Submitted", null, "${ET_COS_URL}/dynamicListingVenue", "${ET_COS_URL}/listingHearings", null, null, null, false,
                List.of(
                    grant(Permission.CRU, regionalCaseworkerRole),
                    grant(Permission.CRU, regionalJudgeRole),
                    grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
                ),
                List.of(
                    field("hearingDateType", Mandatory, 2, 2, 1).summary("Y").pageColumn(1),
                    field("listingDateFrom", Mandatory, 2, 2, 2).show("hearingDateType=\"Range\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingDateTo", Mandatory, 2, 2, 3).show("hearingDateType=\"Range\"").mid("${ET_COS_URL}/listingsDateRangeMidEventValidation").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingDate", Mandatory, 2, 2, 2).show("hearingDateType=\"Single\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("hearingDocType", Mandatory, 2, 2, 4).show("listingVenue=\"dummy\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("hearingDocETCL", Mandatory, 2, 2, 5).show("hearingDocType=\"ETCL - Cause List\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("roomOrNoRoom", Mandatory, 2, 2, 6).show("hearingDocETCL=\"Public\" OR hearingDocETCL=\"Staff\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("showAll", Mandatory, 2, 2, 7).show("hearingDocType=\"ETCL - Cause List\" AND hearingDocETCL=\"Staff\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingVenue", Mandatory, 2, 2, 8).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("fixReport", "Fix Report API", null, 5, "Submitted", "Submitted", null, null, null, null, null, null, false,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(field("hearingDocType", Optional, 1, 1, 1).summary("Y").pageColumn(1)),
                List.of()
            ),
            event("createReport", "Create Report", null, 5, null, "SubmittedReport", null, null, "${ET_COS_URL}/listingCaseCreation", null, null, null, false,
                List.of(
                    grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API),
                    grant(Permission.CRU, regionalCaseworkerRole)
                ),
                List.of(
                    field("reportType", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("managingOffice", Mandatory, 1, 1, 2).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("generateReport", "Generate Report", null, 6, "SubmittedReport", "SubmittedReport", null, null, "${ET_COS_URL}/generateReport", "${ET_COS_URL}/generateHearingDocumentConfirmation", "Generate Report", null, false,
                List.of(
                    grant(Permission.CRU, regionalCaseworkerRole),
                    grant(Permission.CRU, regionalJudgeRole),
                    grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
                ),
                List.of(
                    field("hearingDateType", Mandatory, 1, 1, 1).show("reportType !=\"No Change In Current Position\" AND reportType != \"Cases Awaiting Judgment\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("reportDate", Mandatory, 1, 1, 2).show("reportType=\"No Change In Current Position\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingDateFrom", Mandatory, 1, 1, 3).show("hearingDateType=\"Range\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingDateTo", Mandatory, 1, 1, 4).show("hearingDateType=\"Range\"").mid("${ET_COS_URL}/listingsDateRangeMidEventValidation").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingDate", Mandatory, 1, 1, 5).show("hearingDateType=\"Single\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("reportType", Mandatory, 1, 1, 6).show("hearingDateType=\"dummy\"").pageLabel("f").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("clerkResponsible", Optional, 1, 1, 7).show("reportType=\"dummy\"").summary("Y").pageColumn(1).retainHidden("Yes")
                ),
                List.of()
            )
        );
    }

    private List<LegacyEventDefinition.EventSpec> scotlandListingEvents() {
        return List.of(
            event("createCase", "Create Case", null, 1, null, "Submitted", null, null, "${ET_COS_URL}/listingCaseCreation", null, null, null, false,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("hearingDocType", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("managingOffice", Mandatory, 1, 1, 2).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("generateListing", "Generate Report", null, 2, "Submitted", "Submitted", null, "${ET_COS_URL}/dynamicListingVenue", "${ET_COS_URL}/listingHearings", null, null, null, false,
                List.of(
                    grant(Permission.CRU, regionalCaseworkerRole),
                    grant(Permission.CRU, regionalJudgeRole),
                    grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
                ),
                List.of(
                    field("hearingDateType", Mandatory, 2, 2, 1).summary("Y").pageColumn(1),
                    field("listingDateFrom", Mandatory, 2, 2, 2).show("hearingDateType=\"Range\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingDateTo", Mandatory, 2, 2, 3).show("hearingDateType=\"Range\"").mid("${ET_COS_URL}/listingsDateRangeMidEventValidation").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingDate", Mandatory, 2, 2, 2).show("hearingDateType=\"Single\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("hearingDocType", Mandatory, 2, 2, 4).show("listingVenue=\"dummy\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("hearingDocETCL", Mandatory, 2, 2, 5).show("hearingDocType=\"ETCL - Cause List\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("roomOrNoRoom", Mandatory, 2, 2, 6).show("hearingDocETCL=\"Public\" OR hearingDocETCL=\"Staff\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("showAll", Mandatory, 2, 1, 7).show("hearingDocType=\"ETCL - Cause List\" AND hearingDocETCL=\"Staff\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("managingOffice", ReadOnly, 2, 2, 8).summary("Y").pageColumn(1),
                    field("listingVenue", Mandatory, 2, 2, 9).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("fixReport", "Fix Report API", null, 5, "Submitted", "Submitted", null, null, null, null, null, null, false,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(field("hearingDocType", Optional, 1, 1, 1).summary("Y").pageColumn(1)),
                List.of()
            ),
            event("createReport", "Create Report", null, 5, null, "SubmittedReport", null, null, "${ET_COS_URL}/listingCaseCreation", null, null, null, false,
                List.of(
                    grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API),
                    grant(Permission.CRUD, regionalCaseworkerRole)
                ),
                List.of(field("reportType", Mandatory, 1, 1, 1).summary("Y").pageColumn(1)),
                List.of()
            ),
            event("generateReport", "Generate Report", null, 6, "SubmittedReport", "SubmittedReport", null, null, "${ET_COS_URL}/generateReport", "${ET_COS_URL}/generateHearingDocumentConfirmation", "Generate Report", null, false,
                List.of(
                    grant(Permission.CRU, regionalCaseworkerRole),
                    grant(Permission.CRU, regionalJudgeRole),
                    grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
                ),
                List.of(
                    field("hearingDateType", Mandatory, 1, 1, 1).show("reportType !=\"No Change In Current Position\"").pageShow("reportType != \"Cases Awaiting Judgment\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingDateFrom", Mandatory, 1, 1, 2).show("hearingDateType=\"Range\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingDateTo", Mandatory, 1, 1, 3).show("hearingDateType=\"Range\"").mid("${ET_COS_URL}/listingsDateRangeMidEventValidation").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("listingDate", Mandatory, 1, 1, 4).show("hearingDateType=\"Single\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("reportType", Optional, 1, 1, 5).show("hearingDateType=\"dummy\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("reportDate", Mandatory, 1, 1, 6).show("reportType=\"No Change In Current Position\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("clerkResponsible", Optional, 1, 1, 7).show("reportType=\"dummy\"").summary("Y").pageColumn(1).retainHidden("Yes")
                ),
                List.of()
            )
        );
    }
}
