package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.EnumSet;

public abstract class AllocateHearingConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private static final String POST_CONDITION_STATES = "Accepted(preAcceptCase.caseAccepted=\"Yes\"):1;Rejected";
    private static final String HANDLE_LISTING_SELECTED =
        "${ET_COS_URL}/allocatehearing/handleListingSelected";
    private static final String HANDLE_MANAGING_OFFICE_SELECTED =
        "${ET_COS_URL}/allocatehearing/handleManagingOfficeSelected";
    private static final String POPULATE_ROOMS = "${ET_COS_URL}/allocatehearing/populateRooms";

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean includeManagingOffice;
    private final boolean includeReadingDeliberation;

    protected AllocateHearingConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean includeManagingOffice,
        boolean includeReadingDeliberation
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.includeManagingOffice = includeManagingOffice;
        this.includeReadingDeliberation = includeReadingDeliberation;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        allocateHearingFields(
            configBuilder.event("allocateHearing")
                .forStateTransition(EnumSet.of(EtState.ACCEPTED, EtState.REJECTED), EtState.REJECTED)
                .name("Allocate Hearing")
                .description("Allocate a Hearing")
                .displayOrder(23)
                .showCondition("managingOffice !=\"Unassigned\"")
                .caseEventColumn("PostConditionState", POST_CONDITION_STATES)
                .aboutToStartCallbackUrl("${ET_COS_URL}/allocatehearing/initialiseHearings")
                .aboutToSubmitCallbackUrl("${ET_COS_URL}/allocatehearing/aboutToSubmit")
        )
            .grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole)
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private Event.EventBuilder<T, EtUserRole, EtState> allocateHearingFields(
        Event.EventBuilder<T, EtUserRole, EtState> event
    ) {
        var fields = event.fields()
            .page("1")
            .field(CaseData::getAllocateHearingHearing)
            .mandatory()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", HANDLE_LISTING_SELECTED)
            .caseEventColumn("PageFieldDisplayOrder", 1)
            .caseEventColumn("PageColumnNumber", 1)
            .done();

        if (includeManagingOffice) {
            fields.page("2")
                .field(CaseData::getAllocateHearingManagingOffice)
                .mandatory()
                .showSummary()
                .caseEventColumn("CallBackURLMidEvent", HANDLE_MANAGING_OFFICE_SELECTED)
                .caseEventColumn("PageFieldDisplayOrder", 1)
                .caseEventColumn("PageColumnNumber", 1)
                .done();
        }

        int hearingDetailsPage = includeManagingOffice ? 3 : 2;
        int panelOffset = includeReadingDeliberation ? 1 : 0;
        fields.page(String.valueOf(hearingDetailsPage))
            .field(CaseData::getAllocateHearingSitAlone)
            .mandatory()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 1)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getAllocateHearingJudge)
            .optional()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 2)
            .caseEventColumn("PageColumnNumber", 1)
            .done();

        if (includeReadingDeliberation) {
            fields.field(CaseData::getAllocateHearingReadingDeliberation)
                .optional()
                .showSummary()
                .caseEventColumn("PageFieldDisplayOrder", 3)
                .caseEventColumn("PageColumnNumber", 1)
                .done();
        }

        fields.field(CaseData::getAllocateHearingAdditionalJudge)
            .optional()
            .showSummary()
            .showCondition("allocateHearingSitAlone=\"Two Judges\"")
            .caseEventColumn("PageFieldDisplayOrder", 3)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getAllocateHearingEmployerMember)
            .optional()
            .showSummary()
            .showCondition("allocateHearingSitAlone=\"Full Panel\"")
            .caseEventColumn("PageFieldDisplayOrder", 3 + panelOffset)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getAllocateHearingEmployeeMember)
            .optional()
            .showSummary()
            .showCondition("allocateHearingSitAlone=\"Full Panel\"")
            .caseEventColumn("PageFieldDisplayOrder", 4 + panelOffset)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getAllocateHearingStatus)
            .optional()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 5 + panelOffset)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getAllocateHearingPostponedBy)
            .mandatory()
            .showSummary()
            .showCondition("allocateHearingStatus=\"Postponed\"")
            .caseEventColumn("PageFieldDisplayOrder", 6 + panelOffset)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getAllocateHearingVenue)
            .mandatory()
            .showSummary()
            .caseEventColumn("CallBackURLMidEvent", POPULATE_ROOMS)
            .caseEventColumn("PageFieldDisplayOrder", 7 + panelOffset)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .field(CaseData::getAllocateHearingClerk)
            .optional()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 8 + panelOffset)
            .caseEventColumn("PageColumnNumber", 1)
            .done();

        fields.page(String.valueOf(hearingDetailsPage + 1))
            .field(CaseData::getAllocateHearingRoom)
            .optional()
            .showSummary()
            .caseEventColumn("PageFieldDisplayOrder", 1)
            .caseEventColumn("PageColumnNumber", 1)
            .done()
            .done();

        return event;
    }
}
