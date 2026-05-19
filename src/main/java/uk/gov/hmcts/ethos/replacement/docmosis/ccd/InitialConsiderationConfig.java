package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.Event.EventBuilder;
import uk.gov.hmcts.ccd.sdk.api.FieldCollection.FieldCollectionBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.EtICHearingListedAnswers;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForFinalHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearing;
import uk.gov.hmcts.et.common.model.ccd.EtICListForPreliminaryHearingUpdated;
import uk.gov.hmcts.et.common.model.ccd.EtICSeekComments;
import uk.gov.hmcts.et.common.model.ccd.EtIcudlHearing;
import uk.gov.hmcts.et.common.model.ccd.EtInitialConsiderationRule27;
import uk.gov.hmcts.et.common.model.ccd.EtInitialConsiderationRule28;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Complex;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Mandatory;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Optional;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.ReadOnly;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.EventComplexFieldSpec.complexField;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.EventFieldSpec.field;

@SuppressWarnings({"checkstyle:LineLength", "PMD.ExcessiveClassLength", "PMD.ExcessiveMethodLength", "PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals"})
public abstract class InitialConsiderationConfig<T extends CaseData> implements CCDConfig<T, EtState, EtUserRole> {

    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;
    private final boolean scotland;

    protected InitialConsiderationConfig(
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole,
        boolean scotland
    ) {
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
        this.scotland = scotland;
    }

    @Override
    public void configure(ConfigBuilder<T, EtState, EtUserRole> configBuilder) {
        Event.EventBuilder<T, EtUserRole, EtState> event = configBuilder.event("initialConsideration")
            .forAllStates()
            .name("Initial Consideration")
            .description("Initial Consideration")
            .displayOrder(scotland ? 44 : 20)
            .showSummary()
            .aboutToStartCallbackUrl("${ET_COS_URL}/startInitialConsideration")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/submitInitialConsideration")
            .submittedCallbackUrl("${ET_COS_URL}/completeInitialConsideration")
            .caseEventColumn("PreConditionState(s)", "Accepted")
            .caseEventColumn("PostConditionState", "*")
            .caseEventColumn("Publish", "Y");
        addInitialConsiderationDocumentCollection(event, "icAllDocumentCollection");

        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields = event.fields();
        fieldSpecs().forEach(spec -> addField(fields, spec));

        fields.done()
            .grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)
            .grant(Permission.CRUD, regionalCaseworkerRole, regionalJudgeRole, EtUserRole.CASEWORKER_EMPLOYMENT_API);
    }

    private List<EventFieldSpec> fieldSpecs() {
        return scotland ? scotlandFieldSpecs() : englandWalesFieldSpecs();
    }

    private void addInitialConsiderationDocumentCollection(
        EventBuilder<T, EtUserRole, EtState> event,
        String caseFieldId
    ) {
        event.caseEventToComplexType(caseFieldId, "uploadedDocument", Mandatory, 1, null, null, "icDocumentUpload",
                null)
            .caseEventToComplexType(caseFieldId, "shortDescription", Optional, 1, null, null, "icDocumentUpload",
                null);
    }

    private void addField(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        switch ((scotland ? "Scotland" : "EnglandWales") + spec.id()) {
            case "EnglandWalesetICHearingNotListedSeekComments" -> addEnglandWalesEtICHearingNotListedSeekComments(fields, spec);
            case "ScotlandetICHearingNotListedSeekComments" -> addScotlandEtICHearingNotListedSeekComments(fields, spec);
            case "EnglandWalesetICHearingNotListedListForPrelimHearing" -> addEnglandWalesEtICHearingNotListedListForPrelimHearing(fields, spec);
            case "ScotlandetICHearingNotListedListForPrelimHearing" -> addScotlandEtICHearingNotListedListForPrelimHearing(fields, spec);
            case "EnglandWalesetICHearingNotListedListForPrelimHearingUpdated" -> addEnglandWalesEtICHearingNotListedListForPrelimHearingUpdated(fields, spec);
            case "ScotlandetICHearingNotListedListForPrelimHearingUpdated" -> addScotlandEtICHearingNotListedListForPrelimHearingUpdated(fields, spec);
            case "EnglandWalesetICHearingNotListedListForFinalHearing" -> addEnglandWalesEtICHearingNotListedListForFinalHearing(fields, spec);
            case "ScotlandetICHearingNotListedListForFinalHearing" -> addScotlandEtICHearingNotListedListForFinalHearing(fields, spec);
            case "EnglandWalesetICHearingNotListedListForFinalHearingUpdated" -> addEnglandWalesEtICHearingNotListedListForFinalHearingUpdated(fields, spec);
            case "ScotlandetICHearingNotListedListForFinalHearingUpdated" -> addScotlandEtICHearingNotListedListForFinalHearingUpdated(fields, spec);
            case "EnglandWalesetICHearingNotListedUDLHearing" -> addEnglandWalesEtICHearingNotListedUDLHearing(fields, spec);
            case "ScotlandetICHearingNotListedUDLHearing" -> addScotlandEtICHearingNotListedUDLHearing(fields, spec);
            case "EnglandWalesetICHearingListedAnswers" -> addEnglandWalesEtICHearingListedAnswers(fields, spec);
            case "ScotlandetICHearingListedAnswers" -> addScotlandEtICHearingListedAnswers(fields, spec);
            case "EnglandWalesetInitialConsiderationRule27" -> addEnglandWalesEtInitialConsiderationRule27(fields, spec);
            case "ScotlandetInitialConsiderationRule27" -> addScotlandEtInitialConsiderationRule27(fields, spec);
            case "EnglandWalesetInitialConsiderationRule28" -> addEnglandWalesEtInitialConsiderationRule28(fields, spec);
            case "ScotlandetInitialConsiderationRule28" -> addScotlandEtInitialConsiderationRule28(fields, spec);
            case "EnglandWalesicAllDocumentCollection" -> addEnglandWalesIcAllDocumentCollection(fields, spec);
            case "ScotlandicAllDocumentCollection" -> addScotlandIcAllDocumentCollection(fields, spec);
            case "EnglandWalesicDocumentCollection1" -> addEnglandWalesIcDocumentCollection1(fields, spec);
            case "ScotlandicDocumentCollection1" -> addScotlandIcDocumentCollection1(fields, spec);
            case "EnglandWalesicDocumentCollection2" -> addEnglandWalesIcDocumentCollection2(fields, spec);
            case "ScotlandicDocumentCollection2" -> addScotlandIcDocumentCollection2(fields, spec);
            case "EnglandWalesicDocumentCollection3" -> addEnglandWalesIcDocumentCollection3(fields, spec);
            case "ScotlandicDocumentCollection3" -> addScotlandIcDocumentCollection3(fields, spec);
            default -> spec.addTo(fields).done();
        }
    }

    private List<EventFieldSpec> englandWalesFieldSpecs() {
        return List.of(
            field("initialConsiderationBeforeYouStart", ReadOnly, 1, 1, 1).show("initialConsiderationBeforeYouStartLabel=\"dummy\"").pageLabel("Before you start").summary("N").pageColumn(1),
            field("initialConsiderationBeforeYouStartLabel", ReadOnly, 1, 1, 2).summary("N").pageColumn(1),
            field("etInitialConsiderationRespondent", ReadOnly, 1, 1, 3).show("etInitialConsiderationRespondentLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etInitialConsiderationRespondentLabel", ReadOnly, 1, 1, 4).summary("N").pageColumn(1),
            field("etInitialConsiderationHearing", ReadOnly, 1, 1, 5).show("etInitialConsiderationHearingLabel=\"dummy\"").pageLabel("Before you start").summary("N").pageColumn(1),
            field("etInitialConsiderationHearingLabel", ReadOnly, 1, 1, 6).summary("N").pageColumn(1),
            field("etIcPartiesHearingPanelPreferenceHeader", ReadOnly, 1, 1, 7).noPageDisplayOrder().show("etIcPartiesHearingPanelPreferenceHeaderLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etIcPartiesHearingPanelPreferenceHeaderLabel", ReadOnly, 1, 1, 8).noPageDisplayOrder().summary("N").pageColumn(1),
            field("etIcPartiesHearingPanelPreference", ReadOnly, 1, 1, 9).noPageDisplayOrder().show("etIcPartiesHearingPanelPreferenceLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etIcPartiesHearingPanelPreferenceLabel", ReadOnly, 1, 1, 10).noPageDisplayOrder().summary("N").pageColumn(1),
            field("etIcPartiesHearingFormat", ReadOnly, 1, 1, 11).noPageDisplayOrder().show("etIcPartiesHearingFormatLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etIcPartiesHearingFormatLabel", ReadOnly, 1, 1, 12).noPageDisplayOrder().summary("N").pageColumn(1),
            field("etInitialConsiderationJurisdictionCodes", ReadOnly, 1, 1, 13).show("etInitialConsiderationJurisdictionCodesLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etInitialConsiderationJurisdictionCodesLabel", ReadOnly, 1, 1, 14).summary("N").pageColumn(1),
            field("icRespondentHearingPanelPreference", ReadOnly, 1, 1, 15).show("icRespondentHearingPanelPreferenceLabel=\"dummy\"").summary("N").pageColumn(1),
            field("nextListedDate", Optional, 1, 1, 16).show("etICJuridictionCodesInvalid=\"dummy\"").summary("N").pageColumn(1).publishColumn(),
            field("icRespondentHearingPanelPreferenceLabel", ReadOnly, 1, 1, 17).summary("N").pageColumn(1),
            field("etICIssuesArisingFromVettingLabel", ReadOnly, 1, 1, 18).summary("N").pageColumn(1),
            field("icEt1VettingIssuesDetail", ReadOnly, 1, 1, 19).show("icEt1VettingIssuesDetailLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etICMinimumInfoFromVettingLabel", ReadOnly, 1, 1, 20).summary("N").pageColumn(1),
            field("icEt1VettingIssuesDetailLabel", ReadOnly, 1, 1, 21).summary("N").pageColumn(1),
            field("et3DoWeHaveRespondentsName", ReadOnly, 1, 1, 22).show("etICIssuesArisingFromVettingLabel=\"dummy\"").summary("N").pageColumn(1),
            field("et3DoesRespondentsNameMatch", ReadOnly, 1, 1, 23).show("et3DoWeHaveRespondentsName=\"dummy\"").summary("N").pageColumn(1),
            field("et3RespondentNameMismatchDetails", ReadOnly, 1, 1, 24).show("et3DoesRespondentsNameMatch=\"dummy\"").summary("N").pageColumn(1),
            field("icEt3ProcessingIssuesDetailDividerHrLabel", ReadOnly, 1, 1, 25).summary("N").pageColumn(1),
            field("icEt3ProcessingIssuesDetail", ReadOnly, 1, 1, 26).show("icEt3ProcessingIssuesDetailLabel=\"dummy\"").summary("N").pageColumn(1),
            field("icEt3ProcessingIssuesDetailLabel", ReadOnly, 1, 1, 27).summary("N").pageColumn(1),
            field("icEt3ProcessingIssuesBottomDividerHrLabel", ReadOnly, 1, 1, 28).summary("N").pageColumn(1),
            field("etIcHearingPanelPreferenceLabel", ReadOnly, 1, 1, 29).show("etIcHearingPanelPreference=\"dummy\"").summary("N").pageColumn(1),
            field("etIcHearingPanelPreference", ReadOnly, 1, 1, 30).noPageDisplayOrder().show("etIcHearingPanelPreferenceLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etICJuridictionCodesInvalid", Mandatory, 1, 1, 31).summary("Y").pageColumn(1),
            field("etICInvalidDetails", Mandatory, 1, 1, 32).show("etICJuridictionCodesInvalid=\"Yes\"").summary("Y").pageColumn(1),
            field("etICCanProceed", Mandatory, 1, 1, 33).summary("Y").pageColumn(1).publishColumn(),
            field("initialConsiderationBeforeYouStartLabel2", ReadOnly, 2, 2, 1).pageLabel("Rule 27").summary("N").pageColumn(1),
            field("etICHearingNotListedRespondentLabel", ReadOnly, 2, 2, 2).pageShow("etICCanProceed=\"Yes\" AND etICHearingAlreadyListed=\"No\"").summary("N").pageColumn(1),
            field("etICHearingNotListedHearingLabel", ReadOnly, 2, 2, 3).summary("N"),
            field("notListedHearingPartiesPanelPreferenceLabel", ReadOnly, 2, 2, 4).summary("N"),
            field("notListedHearingPartiesHearingFormatLabel", ReadOnly, 2, 2, 5).summary("N"),
            field("etICHearingNotListedJurCodesLabel", ReadOnly, 2, 2, 6).summary("N"),
            field("etICHearingNotListedList", Mandatory, 2, 2, 7).show("etICCanProceed=\"dummy\"").summary("Y").publishColumn(),
            field("etICHearingNotListedListUpdatedDividerHrLabel", ReadOnly, 2, 2, 8).summary("Y").publishColumn(),
            field("etICHearingNotListedListUpdated", Mandatory, 2, 2, 9).summary("Y").publishColumn(),
            field("etICHearingNotListedSeekComments", Complex, 2, 2, 10).show("etICHearingNotListedList=\"dummy\"").summary("Y"),
            field("etICHearingNotListedListForPrelimHearing", Complex, 2, 2, 11).show("etICHearingNotListedList=\"dummy\"").summary("Y"),
            field("etICHearingNotListedListForPrelimHearingUpdated", Complex, 2, 2, 11).show("etICHearingNotListedListUpdated CONTAINS \"List for preliminary hearing\"").summary("Y"),
            field("etICHearingNotListedListForFinalHearing", Complex, 2, 2, 12).show("etICHearingNotListedList=\"dummy\"").summary("Y"),
            field("etICHearingNotListedListForFinalHearingUpdated", Complex, 2, 2, 12).show("etICHearingNotListedListUpdated CONTAINS \"List for final hearing\"").summary("Y"),
            field("etICHearingNotListedUDLHearing", Complex, 2, 2, 13).show("etICHearingNotListedList=\"dummy\"").summary("Y"),
            field("etICHearingAlreadyListed", Optional, 2, 2, 14).show("etICCanProceed=\"dummy\"").retainHidden("Yes"),
            field("etICAnyOtherDirectionsDividerHrLabel", ReadOnly, 2, 2, 15).summary("N"),
            field("etICHearingNotListedAnyOtherDirectionsLabel", ReadOnly, 2, 2, 16).summary("N"),
            field("etICHearingNotListedAnyOtherDirections", Optional, 2, 2, 17).summary("Y"),
            field("etICUploadDocDividerHrLabel", ReadOnly, 2, 2, 18).summary("N"),
            field("icDocumentCollection1", Complex, 2, 2, 19).summary("Y"),
            field("etICNavigationButtonsDividerHrLabel", ReadOnly, 2, 2, 20).summary("N"),
            field("initialConsiderationBeforeYouStartLabel3", ReadOnly, 3, 3, 1).summary("N"),
            field("etICHearingListedRespondentLabel", ReadOnly, 3, 3, 2).summary("N"),
            field("etICHearingListedHearingLabel", ReadOnly, 3, 3, 3).summary("N").pageColumn(1),
            field("listedHearingPartiesPanelPreferenceLabel", ReadOnly, 3, 3, 4).summary("N").pageColumn(1),
            field("listedHearingPartiesHearingFormatLabel", ReadOnly, 3, 3, 5).summary("N").pageColumn(1),
            field("etICHearingListedJurisdictionCodesLabel", ReadOnly, 3, 3, 6).summary("N").pageColumn(1),
            field("etICHearingListedAnswers", Complex, 3, 3, 7).pageShow("etICCanProceed=\"Yes\" AND etICHearingAlreadyListed=\"Yes\"").summary("Y"),
            field("etICUploadDocDividerHrLabel3", ReadOnly, 3, 3, 8).summary("N"),
            field("icDocumentCollection2", Complex, 3, 3, 9).summary("Y"),
            field("initialConsiderationBeforeYouStartLabel4", ReadOnly, 4, 4, 1).summary("N"),
            field("etICFurtherInfoRespondentLabel", ReadOnly, 4, 4, 2).pageShow("etICCanProceed=\"No\"").summary("N"),
            field("etICFurtherInfoHearingLabel", ReadOnly, 4, 4, 3).summary("N").pageColumn(1),
            field("etICFurtherInfoJurisdictionCodesLabel", ReadOnly, 4, 4, 4).summary("N").pageColumn(1),
            field("etICFurtherInformation", Optional, 4, 4, 5).summary("Y").retainHidden("No"),
            field("etICFurtherInfoAnswers", Mandatory, 4, 4, 6).show("etICFurtherInformation CONTAINS \"Further information required\"").summary("Y").retainHidden("No"),
            field("etInitialConsiderationRule27", Mandatory, 4, 4, 7).show("etICFurtherInformation CONTAINS \"Issue Rule 27 Notice and order\"").summary("Y").retainHidden("No").publishColumn(),
            field("etInitialConsiderationRule28", Mandatory, 4, 4, 8).show("etICFurtherInformation CONTAINS \"Issue Rule 28 Notice and order\"").summary("Y").retainHidden("No").publishColumn(),
            field("etICFurtherInformationHearingAnyOtherDirectionsLabel", ReadOnly, 4, 4, 9).summary("N"),
            field("etICFurtherInformationHearingAnyOtherDirections", Optional, 4, 4, 10).summary("Y"),
            field("icDocumentCollection3", Complex, 4, 4, 11).summary("Y")
        );
    }

    private List<EventFieldSpec> scotlandFieldSpecs() {
        return List.of(
            field("initialConsiderationBeforeYouStart", ReadOnly, 1, 1, 1).show("initialConsiderationBeforeYouStartLabel=\"dummy\"").pageLabel("Before you start").summary("N").pageColumn(1),
            field("initialConsiderationBeforeYouStartLabel", ReadOnly, 1, 1, 2).summary("N").pageColumn(1),
            field("etInitialConsiderationRespondent", ReadOnly, 1, 1, 3).show("etInitialConsiderationRespondentLabel=\"dummy\"").pageLabel("Rule 27").summary("N").pageColumn(1),
            field("etInitialConsiderationRespondentLabel", ReadOnly, 1, 1, 4).summary("N").pageColumn(1),
            field("etInitialConsiderationHearing", ReadOnly, 1, 1, 5).show("etInitialConsiderationHearingLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etInitialConsiderationHearingLabel", ReadOnly, 1, 1, 6).summary("N").pageColumn(1),
            field("etIcPartiesHearingPanelPreferenceHeader", ReadOnly, 1, 1, 7).noPageDisplayOrder().show("etIcPartiesHearingPanelPreferenceHeaderLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etIcPartiesHearingPanelPreferenceHeaderLabel", ReadOnly, 1, 1, 8).noPageDisplayOrder().summary("N").pageColumn(1),
            field("etIcPartiesHearingPanelPreference", ReadOnly, 1, 1, 9).noPageDisplayOrder().show("etIcPartiesHearingPanelPreferenceLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etIcPartiesHearingPanelPreferenceLabel", ReadOnly, 1, 1, 10).noPageDisplayOrder().summary("N").pageColumn(1),
            field("etIcPartiesHearingFormat", ReadOnly, 1, 1, 11).noPageDisplayOrder().show("etIcPartiesHearingFormatLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etIcPartiesHearingFormatLabel", ReadOnly, 1, 1, 12).noPageDisplayOrder().summary("N").pageColumn(1),
            field("etIcHearingPanelPreference", ReadOnly, 1, 1, 13).noPageDisplayOrder().show("etIcHearingPanelPreferenceLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etIcHearingPanelPreferenceLabel", ReadOnly, 1, 1, 14).show("etIcPartiesHearingFormatLabel=\"dummy\"").summary("N").pageColumn(1),
            field("icRespondentHearingPanelPreference", ReadOnly, 1, 1, 15).show("icRespondentHearingPanelPreferenceLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etInitialConsiderationJurisdictionCodes", ReadOnly, 1, 1, 16).show("etInitialConsiderationJurisdictionCodesLabel=\"dummy\"").summary("N").pageColumn(1),
            field("etInitialConsiderationJurisdictionCodesLabel", ReadOnly, 1, 1, 17).summary("N").pageColumn(1),
            field("nextListedDate", Optional, 1, 1, 18).show("etICJuridictionCodesInvalid=\"dummy\"").summary("N").pageColumn(1).publishColumn(),
            field("icRespondentHearingPanelPreferenceLabel", ReadOnly, 1, 1, 19).summary("N").pageColumn(1),
            field("etICIssuesArisingFromVettingLabel", ReadOnly, 1, 1, 20).show("etICJuridictionCodesInvalid=\"dummy\"").summary("N").pageColumn(1),
            field("icEt1VettingIssuesDetail", ReadOnly, 1, 1, 21).show("etICJuridictionCodesInvalid=\"dummy\"").summary("N").pageColumn(1),
            field("etICMinimumInfoFromVettingLabel", ReadOnly, 1, 1, 22).show("etICJuridictionCodesInvalid=\"dummy\"").summary("N").pageColumn(1),
            field("icEt1VettingIssuesDetailLabel", ReadOnly, 1, 1, 23).show("etICJuridictionCodesInvalid=\"dummy\"").summary("N").pageColumn(1),
            field("et3DoWeHaveRespondentsName", ReadOnly, 1, 1, 24).show("etICJuridictionCodesInvalid=\"dummy\"").summary("N").pageColumn(1),
            field("et3DoesRespondentsNameMatch", ReadOnly, 1, 1, 25).show("et3DoWeHaveRespondentsName=\"Yes\"").summary("N").pageColumn(1),
            field("et3RespondentNameMismatchDetails", ReadOnly, 1, 1, 26).show("et3DoesRespondentsNameMatch=\"No\"").summary("N").pageColumn(1),
            field("icEt3ProcessingIssuesDetailDividerHrLabel", ReadOnly, 1, 1, 27).show("etICJuridictionCodesInvalid=\"dummy\"").summary("N").pageColumn(1),
            field("icEt3ProcessingIssuesDetail", ReadOnly, 1, 1, 28).show("etICJuridictionCodesInvalid=\"dummy\"").summary("N").pageColumn(1),
            field("icEt3ProcessingIssuesDetailLabel", ReadOnly, 1, 1, 29).show("etICJuridictionCodesInvalid=\"dummy\"").summary("N").pageColumn(1),
            field("icEt3ProcessingIssuesBottomDividerHrLabel", ReadOnly, 1, 1, 30).summary("N").pageColumn(1),
            field("etICJuridictionCodesInvalid", Mandatory, 1, 1, 31).summary("Y").pageColumn(1),
            field("etICInvalidDetails", Mandatory, 1, 1, 32).show("etICJuridictionCodesInvalid=\"Yes\"").summary("Y").pageColumn(1),
            field("etICCanProceed", Mandatory, 1, 1, 33).pageLabel("Rule 26").summary("Y").pageColumn(1).publishColumn(),
            field("etICHearingNotListedRespondentLabel", ReadOnly, 2, 2, 1).pageShow("etICCanProceed=\"Yes\" AND etICHearingAlreadyListed=\"No\"").pageLabel("Rule 27").summary("N").pageColumn(1),
            field("etICHearingNotListedHearingLabel", ReadOnly, 2, 2, 2).summary("N"),
            field("notListedHearingPartiesPanelPreferenceLabel", ReadOnly, 2, 2, 3).summary("N"),
            field("notListedHearingPartiesHearingFormatLabel", ReadOnly, 2, 2, 4).summary("N"),
            field("etICHearingNotListedJurCodesLabel", ReadOnly, 2, 2, 5).summary("N"),
            field("etICHearingNotListedList", Mandatory, 2, 2, 5).show("etICCanProceed=\"dummy\"").summary("Y").publishColumn(),
            field("etICHearingNotListedListUpdated", Mandatory, 2, 2, 6).summary("Y").publishColumn(),
            field("etICHearingNotListedListForPrelimHearingUpdated", Complex, 2, 2, 7).show("etICHearingNotListedListUpdated CONTAINS \"List for preliminary hearing\"").summary("Y"),
            field("etICHearingNotListedListForFinalHearingUpdated", Complex, 2, 2, 8).show("etICHearingNotListedListUpdated CONTAINS \"List for final hearing\"").summary("Y"),
            field("etICHearingNotListedAnyOtherDirections", Optional, 2, 2, 9).summary("Y"),
            field("icDocumentCollection1", Complex, 2, 2, 10).summary("Y"),
            field("etICHearingAlreadyListed", Mandatory, 2, 2, 11).show("etICCanProceed=\"dummy\"").retainHidden("Yes").publishColumn(),
            field("etICHearingNotListedSeekComments", Complex, 2, 2, 55).show("etICHearingNotListedList CONTAINS \"dummy\"").summary("Y"),
            field("etICHearingNotListedListForPrelimHearing", Complex, 2, 2, 56).show("etICHearingNotListedList CONTAINS \"dummy\"").summary("Y"),
            field("etICHearingNotListedListForFinalHearing", Complex, 2, 2, 57).show("etICHearingNotListedList CONTAINS \"dummy\"").summary("Y"),
            field("etICHearingNotListedUDLHearing", Complex, 2, 2, 58).show("etICHearingNotListedList CONTAINS \"dummy\"").summary("Y"),
            field("etICRespondentHearingListed", ReadOnly, 3, 3, 1).summary("N").pageColumn(1),
            field("etICHearingHearingListed", ReadOnly, 3, 3, 2).summary("N").pageColumn(1),
            field("listedHearingPartiesPanelPreferenceLabel", ReadOnly, 3, 3, 3).summary("N"),
            field("listedHearingPartiesHearingFormatLabel", ReadOnly, 3, 3, 4).summary("N"),
            field("etICJurisdictionCodesHearingListed", ReadOnly, 3, 3, 5).summary("N").pageColumn(1),
            field("etICHearingListedAnswers", Complex, 3, 3, 6).pageShow("etICCanProceed=\"Yes\" AND etICHearingAlreadyListed=\"Yes\"").summary("Y"),
            field("icDocumentCollection2", Complex, 3, 3, 7).summary("Y"),
            field("etICRespondentFurtherInfo", ReadOnly, 4, 4, 1).pageShow("etICCanProceed=\"No\"").pageLabel("Rule 27").summary("N").pageColumn(1),
            field("etICHearingFurtherInfo", ReadOnly, 4, 4, 2).summary("N").pageColumn(1),
            field("etICJurisdictionCodesFurtherInfo", ReadOnly, 4, 4, 3).summary("N").pageColumn(1),
            field("etICFurtherInformation", Optional, 4, 4, 4).summary("Y").pageColumn(1),
            field("etICFurtherInformationGiveDetails", Mandatory, 4, 4, 5).show("etICFurtherInformation CONTAINS \"furtherInformationRequired\"").pageLabel("Rule 27").summary("Y").pageColumn(1),
            field("etICFurtherInformationTimeToComply", Mandatory, 4, 4, 6).show("etICFurtherInformation CONTAINS \"furtherInformationRequired\"").pageLabel("Rule 27").summary("Y").pageColumn(1),
            field("etInitialConsiderationRule27", Complex, 4, 4, 7).show("etICFurtherInformation CONTAINS \"issueRule27\"").summary("Y").pageColumn(1),
            field("etInitialConsiderationRule28", Complex, 4, 4, 8).show("etICFurtherInformation CONTAINS \"issueRule28\"").summary("Y").pageColumn(1),
            field("etICFurtherInformationHearingAnyOtherDirections", Optional, 4, 4, 9).pageLabel("Rule 27").summary("Y").pageColumn(1),
            field("icDocumentCollection3", Complex, 4, 4, 10).summary("Y")
        );
    }

    private void addEnglandWalesEtICHearingNotListedSeekComments(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICSeekComments, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICSeekComments.class);
        complexField(EtICSeekComments::getEtICTypeOfCvpHearing, Mandatory, 1).label("Type of Video hearing").addTo(complexFields);
        complexField(EtICSeekComments::getEtICFinalHearingDetails, Mandatory, 2).label("Give details of final hearing").addTo(complexFields);
        complexField(EtICSeekComments::getEtICPrelimHearingDetails, Mandatory, 3).label("Give details of preliminary hearing").addTo(complexFields);
        complexField(EtICSeekComments::getEtICPrelimHearingYesNo, Mandatory, 4).label("Should the case be listed for a private preliminary hearing?").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesEtICHearingNotListedListForPrelimHearing(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICListForPreliminaryHearing, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICListForPreliminaryHearing.class);
        complexField(EtICListForPreliminaryHearing::getEtICTypeOfPreliminaryHearing, Mandatory, 1).label("Type of preliminary hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearing::getEtICPurposeOfPreliminaryHearing, Mandatory, 2).label("Purpose of preliminary hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearing::getEtICGiveDetailsOfHearingNotice, Mandatory, 3).label("Give details of hearing notice").addTo(complexFields);
        complexField(EtICListForPreliminaryHearing::getPrelimHearingLengthNumType, Mandatory, 5).label("Days, Hours or Minutes").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesEtICHearingNotListedListForPrelimHearingUpdated(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICListForPreliminaryHearingUpdated, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICListForPreliminaryHearingUpdated.class);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICTypeOfPreliminaryHearing, Mandatory, 1).label("Type of preliminary hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICPurposeOfPreliminaryHearing, Mandatory, 2).label("Purpose of preliminary hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICGiveDetailsOfHearingNotice, Mandatory, 3).label("Give details of hearing notice").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICLengthOfPrelimHearingLegacy, Mandatory, 4).label("Length of hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICLengthOfPrelimHearing, Mandatory, 4).label("Length of hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getPrelimHearingLengthNumType, Mandatory, 5).label("Days, Hours or Minutes").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICIsPreliminaryHearingWithMembers, Mandatory, 6).label("Do you consider this preliminary hearing should be listed with members?").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICIsPreliminaryHearingWithMembersReason, Mandatory, 7).label("Give reasons for requiring members").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesEtICHearingNotListedListForFinalHearing(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICListForFinalHearing, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICListForFinalHearing.class);
        complexField(EtICListForFinalHearing::getEtICTypeOfFinalHearing, Mandatory, 1).label("Type of final hearing").addTo(complexFields);
        complexField(EtICListForFinalHearing::getEtICLengthOfFinalHearing, Mandatory, 2).label("Length of hearing").retainHidden("No").addTo(complexFields);
        complexField(EtICListForFinalHearing::getFinalHearingLengthNumType, Mandatory, 3).label("Days, Hours or Minutes").retainHidden("No").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesEtICHearingNotListedListForFinalHearingUpdated(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICListForFinalHearingUpdated, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICListForFinalHearingUpdated.class);
        complexField(EtICListForFinalHearingUpdated::getEtICTypeOfFinalHearing, Mandatory, 1).label("Type of final hearing").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICLengthOfFinalHearing, Mandatory, 2).label("Length of hearing").retainHidden("No").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getFinalHearingLengthNumType, Mandatory, 3).label("Days, Hours or Minutes").retainHidden("No").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAlone, Mandatory, 4).label("Do you consider this final hearing should be listed judge alone or with members?").retainHidden("No").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAloneReasonYes, Mandatory, 5).label("Reasons for JSA").retainHidden("No").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICNoLFinalHearingIsEJSitAloneReasonsJsaOther, Mandatory, 6).label("Reason for JSA - Other").retainHidden("No").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAloneReasonNo, Mandatory, 7).label("Reasons for Members").retainHidden("No").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICNoLFinalHearingIsEJSitAloneReasonsMembersOther, Mandatory, 8).label("Reason for Members - Other").retainHidden("No").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAloneFurtherDetails, Optional, 9).label("Further details").retainHidden("No").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesEtICHearingNotListedUDLHearing(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtIcudlHearing, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtIcudlHearing.class);
        complexField(EtIcudlHearing::getEtIcejSitAlone, Mandatory, 1).label("EJ sit alone?").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlGiveReasons, Mandatory, 2).label("Give reasons").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlDisputeOnFacts, Mandatory, 3).label("Likelihood of dispute on facts makes full tribunal desirable").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlLittleOrNoAgreement, Mandatory, 4).label("Little or no agreement on facts").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlIssueOfLawArising, Mandatory, 5).label("Likelihood of issue of law arising makes EJSA desirable").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlViewsOfParties, Mandatory, 6).label("Views of parties").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlNoViewsExpressedByParties, Mandatory, 7).label("No views expressed by parties").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlConcurrentProceedings, Mandatory, 8).label("Concurrent proceedings").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlOther, Mandatory, 9).label("Other").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlHearFormat, Mandatory, 10).label("Hearing format").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlCvpIssue, Mandatory, 11).label("Issue standard Video orders when listed").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlFinalF2FIssue, Mandatory, 12).label("Issue standard orders when listed").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcbuCheckComplianceOrders, Mandatory, 13).label("BU to check compliance with orders?").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesEtICHearingListedAnswers(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICHearingListedAnswers, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICHearingListedAnswers.class);
        complexField(EtICHearingListedAnswers::getEtICHearingListed, Mandatory, 1).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICExtendDurationGiveDetails, Mandatory, 2).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICOtherGiveDetails, Mandatory, 3).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJudgeOrMembers, Mandatory, 4).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJudgeOrMembersReason, Mandatory, 5).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsFinalHearingWithJudgeOrMembersJsaReason, Mandatory, 6).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsFinalHearingWithJudgeOrMembersReason, Mandatory, 7).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJudgeOrMembersReasonOther, Mandatory, 8).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICJsaFinalHearingReasonOther, Mandatory, 9).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICMembersFinalHearingReasonOther, Mandatory, 10).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtInitialConsiderationListedHearingType, ReadOnly, 11).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJsa, Mandatory, 12).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJsaReasonOther, Mandatory, 13).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICJsaCmPreliminaryHearingReasonOther, Mandatory, 14).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithMembers, Mandatory, 15).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJudgeOrMembersFurtherDetails, Optional, 16).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getOtherDirectionsLabel, ReadOnly, 17).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICAnyOtherDirectionsDividerHrLabel3, Optional, 18).label(" ").retainHidden("No").addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICHearingAnyOtherDirections, Optional, 19).label(" ").retainHidden("No").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesEtInitialConsiderationRule27(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtInitialConsiderationRule27, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtInitialConsiderationRule27.class);
        complexField(EtInitialConsiderationRule27::getEtICRule27ClaimToBe, Optional, 1).retainHidden("No").addTo(complexFields);
        complexField(EtInitialConsiderationRule27::getEtICRule27WhichPart, Optional, 2).retainHidden("No").addTo(complexFields);
        complexField(EtInitialConsiderationRule27::getEtICRule27Direction, Optional, 3).retainHidden("No").addTo(complexFields);
        complexField(EtInitialConsiderationRule27::getEtICRule27DirectionReason, Optional, 4).retainHidden("No").addTo(complexFields);
        complexField(EtInitialConsiderationRule27::getEtICRule27NumberOfDays, Optional, 5).retainHidden("No").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesEtInitialConsiderationRule28(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtInitialConsiderationRule28, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtInitialConsiderationRule28.class);
        complexField(EtInitialConsiderationRule28::getEtICRule28ClaimToBe, Optional, 1).retainHidden("No").addTo(complexFields);
        complexField(EtInitialConsiderationRule28::getEtICRule28WhichPart, Optional, 2).retainHidden("No").addTo(complexFields);
        complexField(EtInitialConsiderationRule28::getEtICRule28DirectionReason, Optional, 3).retainHidden("No").addTo(complexFields);
        complexField(EtInitialConsiderationRule28::getEtICRule28NumberOfDays, Optional, 4).retainHidden("No").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesIcAllDocumentCollection(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<DocumentType, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(DocumentType.class);
        complexField(DocumentType::getUploadedDocument, Mandatory, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexField(DocumentType::getShortDescription, Optional, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesIcDocumentCollection1(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<DocumentType, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(DocumentType.class);
        complexField(DocumentType::getUploadedDocument, Mandatory, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexField(DocumentType::getShortDescription, Optional, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesIcDocumentCollection2(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<DocumentType, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(DocumentType.class);
        complexField(DocumentType::getUploadedDocument, Mandatory, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexField(DocumentType::getShortDescription, Optional, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexFields.done().done();
    }

    private void addEnglandWalesIcDocumentCollection3(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<DocumentType, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(DocumentType.class);
        complexField(DocumentType::getUploadedDocument, Mandatory, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexField(DocumentType::getShortDescription, Optional, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandEtInitialConsiderationRule27(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtInitialConsiderationRule27, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtInitialConsiderationRule27.class);
        complexField(EtInitialConsiderationRule27::getEtICRule27ClaimToBe, Mandatory, 1).label("Claim to be").publishColumn().addTo(complexFields);
        complexField(EtInitialConsiderationRule27::getEtICRule27WhichPart, Mandatory, 2).label("Which part?").addTo(complexFields);
        complexField(EtInitialConsiderationRule27::getEtICRule27Direction, Mandatory, 3).label("Employment Judge's direction for Rule 28 Notice").addTo(complexFields);
        complexField(EtInitialConsiderationRule27::getEtICRule27NoJurisdictionReason, Mandatory, 4).label("No jurisdiction - Set out reason").addTo(complexFields);
        complexField(EtInitialConsiderationRule27::getEtICRule27DirectionReason, Mandatory, 5).label("No reasonable prospect of success - Set out reason").addTo(complexFields);
        complexField(EtInitialConsiderationRule27::getEtICRule27NumberOfDays, Mandatory, 6).label("Number of days for claimant to provide written representations").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandEtInitialConsiderationRule28(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtInitialConsiderationRule28, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtInitialConsiderationRule28.class);
        complexField(EtInitialConsiderationRule28::getEtICRule28ClaimToBe, Mandatory, 1).label("Response to be").publishColumn().addTo(complexFields);
        complexField(EtInitialConsiderationRule28::getEtICRule28WhichPart, Mandatory, 2).label("Which part?").addTo(complexFields);
        complexField(EtInitialConsiderationRule28::getEtICRule28DirectionReason, Mandatory, 3).label("Employment Judge's reasons").addTo(complexFields);
        complexField(EtInitialConsiderationRule28::getEtICRule28NumberOfDays, Mandatory, 4).label("Number of days for respondent to provide written representations").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandEtICHearingNotListedSeekComments(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICSeekComments, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICSeekComments.class);
        complexField(EtICSeekComments::getEtICPrelimHearingYesNo, Mandatory, 4).label("Should the case be listed for a private preliminary hearing?").addTo(complexFields);
        complexField(EtICSeekComments::getEtICTypeOfCvpHearing, Mandatory, 1).label("Type of CVP hearing").addTo(complexFields);
        complexField(EtICSeekComments::getEtICFinalHearingDetails, Mandatory, 2).label("Give details of final hearing").addTo(complexFields);
        complexField(EtICSeekComments::getEtICPrelimHearingDetails, Mandatory, 3).label("Give details of preliminary hearing").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandIcAllDocumentCollection(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<DocumentType, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(DocumentType.class);
        complexField(DocumentType::getUploadedDocument, Mandatory, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexField(DocumentType::getShortDescription, Optional, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandIcDocumentCollection1(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<DocumentType, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(DocumentType.class);
        complexField(DocumentType::getUploadedDocument, Mandatory, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexField(DocumentType::getShortDescription, Optional, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandIcDocumentCollection2(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<DocumentType, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(DocumentType.class);
        complexField(DocumentType::getUploadedDocument, Mandatory, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexField(DocumentType::getShortDescription, Optional, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandIcDocumentCollection3(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<DocumentType, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(DocumentType.class);
        complexField(DocumentType::getUploadedDocument, Mandatory, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexField(DocumentType::getShortDescription, Optional, 1).eventTypeId("icDocumentUpload").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandEtICHearingNotListedListForPrelimHearing(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICListForPreliminaryHearing, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICListForPreliminaryHearing.class);
        complexField(EtICListForPreliminaryHearing::getEtICTypeOfPreliminaryHearing, Mandatory, 1).label("Type of preliminary hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearing::getEtICPurposeOfPreliminaryHearing, Mandatory, 2).label("Purpose of preliminary hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearing::getEtICGiveDetailsOfHearingNotice, Optional, 3).label("Give details of hearing notice").addTo(complexFields);
        complexField(EtICListForPreliminaryHearing::getEtICLengthOfPrelimHearing, Mandatory, 4).label("Length of hearing").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandEtICHearingNotListedListForFinalHearing(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICListForFinalHearing, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICListForFinalHearing.class);
        complexField(EtICListForFinalHearing::getEtICTypeOfFinalHearing, Mandatory, 1).label("Type of final hearing").addTo(complexFields);
        complexField(EtICListForFinalHearing::getEtICLengthOfFinalHearing, Mandatory, 2).label("Length of hearing").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandEtICHearingNotListedListForPrelimHearingUpdated(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICListForPreliminaryHearingUpdated, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICListForPreliminaryHearingUpdated.class);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICTypeOfPreliminaryHearing, Mandatory, 1).label("Type of preliminary hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICPurposeOfPreliminaryHearing, Mandatory, 2).label("Purpose of preliminary hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICGiveDetailsOfHearingNotice, Optional, 3).label("Give details of hearing notice").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICLengthOfPrelimHearing, Mandatory, 4).label("Length of hearing").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getPrelimHearingLengthNumType, Mandatory, 5).label("Days, Hours or Minutes").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICIsPreliminaryHearingWithMembers, Mandatory, 6).label("Do you consider this preliminary hearing should be listed with members?").addTo(complexFields);
        complexField(EtICListForPreliminaryHearingUpdated::getEtICIsPreliminaryHearingWithMembersReason, Mandatory, 7).label("Give reasons for requiring members").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandEtICHearingNotListedListForFinalHearingUpdated(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICListForFinalHearingUpdated, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICListForFinalHearingUpdated.class);
        complexField(EtICListForFinalHearingUpdated::getEtICTypeOfFinalHearing, Mandatory, 1).label("Type of final hearing").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICTypeOfVideoHearingOrder, Optional, 2).label("Issue standard video orders when listed").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICTypeOfF2fHearingOrder, Optional, 2).label("Issue standard orders when listed").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICHearingOrderBUCompliance, Optional, 2).label("BU to check compliance with orders?").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICLengthOfFinalHearing, Mandatory, 2).label("Length of hearing").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getFinalHearingLengthNumType, Mandatory, 3).label("Days, Hours or Minutes").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingPanelComposition, Mandatory, 4).label("<hr><h3>Panel Composition</h3>").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAlone, Mandatory, 5).label("EJ sit alone?").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAloneReason, Mandatory, 6).label("Give reason for requesting EJ sit alone:").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICNoLFinalHearingIsEJSitAloneReasonsJsaOther, Mandatory, 7).label("Reasons").retainHidden("No").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICNoLFinalHearingIsEJSitAloneReasonsMembersOther, Mandatory, 8).label("Reasons").retainHidden("No").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAloneReasonYes, Mandatory, 9).label("Reason for JSA").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAloneReasonYesOther, Mandatory, 10).label("Reason for JSA").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAloneReasonNo, Mandatory, 11).label("Reason for full Panel").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAloneReasonNoOther, Mandatory, 12).label("Other Reason (EJ Sit Alone: No)").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingPanelCompositionBottomDivider, ReadOnly, 13).label("<hr>").addTo(complexFields);
        complexField(EtICListForFinalHearingUpdated::getEtICFinalHearingIsEJSitAloneFurtherDetails, Optional, 114).label("Further details").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandEtICHearingNotListedUDLHearing(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtIcudlHearing, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtIcudlHearing.class);
        complexField(EtIcudlHearing::getEtIcejSitAlone, Mandatory, 1).label("EJ sit alone?").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlGiveReasons, Mandatory, 2).label("Give reasons").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlDisputeOnFacts, Mandatory, 3).label("Likelihood of dispute on facts makes full tribunal desirable").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlLittleOrNoAgreement, Mandatory, 4).label("Little or no agreement on facts").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlIssueOfLawArising, Mandatory, 5).label("Likelihood of issue of law arising makes EJSA desirable").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlViewsOfParties, Mandatory, 6).label("Views of parties").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlNoViewsExpressedByParties, Mandatory, 7).label("No views expressed by parties").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlConcurrentProceedings, Mandatory, 8).label("Concurrent proceedings").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlOther, Mandatory, 9).label("Other").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlHearFormat, Mandatory, 10).label("Hearing format").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlCvpIssue, Mandatory, 11).label("Issue standard CVP orders when listed").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcudlFinalF2FIssue, Optional, 12).label("Issue standard orders when listed").addTo(complexFields);
        complexField(EtIcudlHearing::getEtIcbuCheckComplianceOrders, Optional, 13).label("BU to check compliance with orders?").addTo(complexFields);
        complexFields.done().done();
    }

    private void addScotlandEtICHearingListedAnswers(
        FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>> fields,
        EventFieldSpec spec
    ) {
        FieldCollectionBuilder<EtICHearingListedAnswers, EtState, FieldCollectionBuilder<T, EtState, EventBuilder<T, EtUserRole, EtState>>>
            complexFields = spec.addTo(fields).complex(EtICHearingListedAnswers.class);
        complexField(EtICHearingListedAnswers::getEtICHearingListed, Mandatory, 1).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICExtendDurationGiveDetails, Mandatory, 2).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICOtherGiveDetails, Mandatory, 3).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICPostponeGiveDetails, Mandatory, 4).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICConvertPreliminaryGiveDetails, Mandatory, 5).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICConvertF2fGiveDetails, Mandatory, 6).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICExtendDurationGiveDetails, Mandatory, 7).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICOtherGiveDetails, Mandatory, 8).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJudgeOrMembers, Mandatory, 9).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJudgeOrMembersReason, Mandatory, 10).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsFinalHearingWithJudgeOrMembersJsaReason, Mandatory, 11).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsFinalHearingWithJudgeOrMembersReason, Mandatory, 12).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJudgeOrMembersReasonOther, Mandatory, 13).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICJsaFinalHearingReasonOther, Mandatory, 14).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICMembersFinalHearingReasonOther, Mandatory, 15).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtInitialConsiderationListedHearingType, ReadOnly, 16).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJsa, Mandatory, 17).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJsaReasonOther, Mandatory, 18).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICJsaCmPreliminaryHearingReasonOther, Mandatory, 19).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithMembers, Mandatory, 20).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICIsHearingWithJudgeOrMembersFurtherDetails, Optional, 21).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getOtherDirectionsLabel, ReadOnly, 22).addTo(complexFields);
        complexField(EtICHearingListedAnswers::getEtICHearingAnyOtherDirections, Optional, 23).label(" ").retainHidden("No").addTo(complexFields);
        complexFields.done().done();
    }
}
