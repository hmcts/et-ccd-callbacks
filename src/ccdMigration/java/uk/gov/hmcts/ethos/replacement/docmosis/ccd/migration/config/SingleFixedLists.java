package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.HasCode;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SingleRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;

@SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "PMD.AvoidDuplicateLiterals"})
final class SingleFixedLists {
    private SingleFixedLists() {
    }

    static void registerEnglandWalesSingleCftlibDefinition(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder) {
        builder.registerFixedList(
                " fl_respondNotificationResponseRequired",
                List001FlRespondNotificationResponseRequired.values());
        builder.registerFixedList("caseSourceList", List003CaseSourceList.values());
        builder.registerFixedList(
                "claimant_hearingPanelPreference", List004ClaimantHearingPanelPreference.values());
        builder.registerFixedList("configurationFiles", List005ConfigurationFiles.values());
        builder.registerFixedList("createUploadOrRemove", List006CreateUploadOrRemove.values());
        builder.registerFixedList("fl_AddressLabelNumber", List007FlAddressLabelNumber.values());
        builder.registerFixedList("fl_Adjustment", List008FlAdjustment.values());
        builder.registerFixedList("fl_Attendee", List009FlAttendee.values());
        builder.registerFixedList("fl_BFActionsCW", List010FlBFActionsCWV1.values());
        builder.registerFixedList("fl_CaseManagement", List011FlCaseManagement.values());
        builder.registerFixedList("fl_CaseType", List012FlCaseType.values());
        builder.registerFixedList(
                "fl_ClaimantOrRespondent", List013FlClaimantOrRespondent.values());
        builder.registerFixedList(
                "fl_ClaimantRespondentBothParties",
                List014FlClaimantRespondentBothParties.values());
        builder.registerFixedList("fl_Conciliation", List015FlConciliationV1.values());
        builder.registerFixedList("fl_ContactPreference", List016FlContactPreference.values());
        builder.registerFixedList("fl_DepositCovers", List017FlDepositCovers.values());
        builder.registerFixedList("fl_DepositRequestedBy", List018FlDepositRequestedBy.values());
        builder.registerFixedList("fl_DocumentCategories", List019FlDocumentCategories.values());
        builder.registerFixedList("fl_DocumentType", List020FlDocumentTypeV1.values());
        builder.registerFixedList(
                "fl_EmployerContractClaim", List021FlEmployerContractClaim.values());
        builder.registerFixedList(
                "fl_Et3NotificationDocType", List022FlEt3NotificationDocTypeV1.values());
        builder.registerFixedList(
                "fl_FinalHearingIsEJSitAloneReason",
                List023FlFinalHearingIsEJSitAloneReason.values());
        builder.registerFixedList("fl_Gender", List024FlGender.values());
        builder.registerFixedList("fl_Hearing", List025FlHearingV1.values());
        builder.registerFixedList("fl_HearingDateType", List026FlHearingDateType.values());
        builder.registerFixedList("fl_HearingDocETCL", List027FlHearingDocETCL.values());
        builder.registerFixedList("fl_HearingDocType", List028FlHearingDocType.values());
        builder.registerFixedList("fl_HearingLength", List029FlHearingLength.values());
        builder.registerFixedList(
                "fl_HearingPanelPreference", List030FlHearingPanelPreference.values());
        builder.registerFixedList("fl_HearingStatus", List032FlHearingStatusV1.values());
        builder.registerFixedList("fl_Hearings", List033FlHearings.values());
        builder.registerFixedList(
                "fl_InitialConsideration", List034FlInitialConsideration.values());
        builder.registerFixedList("fl_JudgeDecision", List035FlJudgeDecision.values());
        builder.registerFixedList("fl_JudgeDirection", List036FlJudgeDirection.values());
        builder.registerFixedList("fl_JudgementType", List037FlJudgementType.values());
        builder.registerFixedList("fl_JudgmentAndReasons", List038FlJudgmentAndReasons.values());
        builder.registerFixedList("fl_JudgmentOutcome", List039FlJudgmentOutcome.values());
        builder.registerFixedList("fl_Liability", List040FlLiabilityV1.values());
        builder.registerFixedList("fl_Misc", List041FlMisc.values());
        builder.registerFixedList("fl_Part_0", List042FlPart0.values());
        builder.registerFixedList("fl_Part_1", List043FlPart1V1.values());
        builder.registerFixedList("fl_Part_10", List044FlPart10V1.values());
        builder.registerFixedList("fl_Part_11", List045FlPart11V1.values());
        builder.registerFixedList("fl_Part_12", List046FlPart12V1.values());
        builder.registerFixedList("fl_Part_13", List047FlPart13V1.values());
        builder.registerFixedList("fl_Part_14", List048FlPart14V1.values());
        builder.registerFixedList("fl_Part_15", List049FlPart15V1.values());
        builder.registerFixedList("fl_Part_16", List050FlPart16V1.values());
        builder.registerFixedList("fl_Part_17", List051FlPart17.values());
        builder.registerFixedList("fl_Part_18", List052FlPart18.values());
        builder.registerFixedList("fl_Part_2", List053FlPart2V1.values());
        builder.registerFixedList("fl_Part_20", List054FlPart20.values());
        builder.registerFixedList("fl_Part_3", List055FlPart3V1.values());
        builder.registerFixedList("fl_Part_4", List056FlPart4V1.values());
        builder.registerFixedList("fl_Part_5", List057FlPart5V1.values());
        builder.registerFixedList("fl_Part_6", List058FlPart6V1.values());
        builder.registerFixedList("fl_Part_7", List059FlPart7V1.values());
        builder.registerFixedList("fl_Part_8", List060FlPart8V1.values());
        builder.registerFixedList("fl_Part_9", List061FlPart9V1.values());
        builder.registerFixedList("fl_Position", List062FlPosition.values());
        builder.registerFixedList("fl_PositionCT", List063FlPositionCT.values());
        builder.registerFixedList("fl_PostponedBy", List064FlPostponedBy.values());
        builder.registerFixedList("fl_PreferredTitle", List065FlPreferredTitle.values());
        builder.registerFixedList("fl_PublicPrivate", List066FlPublicPrivate.values());
        builder.registerFixedList("fl_Reconsideration", List067FlReconsideration.values());
        builder.registerFixedList("fl_ReferralSubject", List068FlReferralSubject.values());
        builder.registerFixedList("fl_Reinstate", List069FlReinstate.values());
        builder.registerFixedList("fl_Reinstated", List070FlReinstated.values());
        builder.registerFixedList(
                "fl_RepresentativeContact", List071FlRepresentativeContact.values());
        builder.registerFixedList(
                "fl_RepresentativeOccupation", List072FlRepresentativeOccupation.values());
        builder.registerFixedList("fl_ResponseStatus", List073FlResponseStatus.values());
        builder.registerFixedList("fl_ResponseToAClaim", List074FlResponseToAClaim.values());
        builder.registerFixedList(
                "fl_RestrictedExcludedRegister", List075FlRestrictedExcludedRegister.values());
        builder.registerFixedList(
                "fl_RestrictedRequestedBy", List076FlRestrictedRequestedBy.values());
        builder.registerFixedList(
                "fl_ServingDocumentRecipient", List077FlServingDocumentRecipient.values());
        builder.registerFixedList(
                "fl_ServingDocumentType", List078FlServingDocumentTypeV1.values());
        builder.registerFixedList("fl_Sex", List079FlSex.values());
        builder.registerFixedList("fl_Stage", List080FlStage.values());
        builder.registerFixedList("fl_StartingAClaim", List081FlStartingAClaim.values());
        builder.registerFixedList("fl_StillWorking", List082FlStillWorking.values());
        builder.registerFixedList("fl_Title", List083FlTitle.values());
        builder.registerFixedList("fl_TopLevel", List084FlTopLevelV1.values());
        builder.registerFixedList("fl_TribunalOffice", List085FlTribunalOfficeV1.values());
        builder.registerFixedList("fl_WithdrawalSettled", List086FlWithdrawalSettled.values());
        builder.registerFixedList("fl_YesOrNoOrPreferNot", List087FlYesOrNoOrPreferNot.values());
        builder.registerFixedList(
                "fl_claimantTseSelectApp", List088FlClaimantTseSelectApp.values());
        builder.registerFixedList("fl_contest_claim_status", List089FlContestClaimStatus.values());
        builder.registerFixedList(
                "fl_costs_pro_bono_awarded_to", List090FlCostsProBonoAwardedTo.values());
        builder.registerFixedList("fl_employer_type", List091FlEmployerType.values());
        builder.registerFixedList("fl_et3Struckout", List092FlEt3Struckout.values());
        builder.registerFixedList("fl_et3_pay_frequency", List093FlEt3PayFrequency.values());
        builder.registerFixedList("fl_et3_suggested_issues", List094FlEt3SuggestedIssues.values());
        builder.registerFixedList(
                "fl_et3_tribunal_location_change", List095FlEt3TribunalLocationChange.values());
        builder.registerFixedList(
                "fl_etICUDLHearingFormat", List096FlEtICUDLHearingFormatV1.values());
        builder.registerFixedList(
                "fl_hearingJudgeAloneOrWithMembers",
                List097FlHearingJudgeAloneOrWithMembers.values());
        builder.registerFixedList("fl_isLocationCorrect", List098FlIsLocationCorrect.values());
        builder.registerFixedList(
                "fl_isTrackAllocationCorrect", List099FlIsTrackAllocationCorrect.values());
        builder.registerFixedList("fl_jurisdictionCodes", List100FlJurisdictionCodes.values());
        builder.registerFixedList("fl_languages", List101FlLanguages.values());
        builder.registerFixedList("fl_notice_period_unit", List103FlNoticePeriodUnit.values());
        builder.registerFixedList("fl_pay_cycle", List104FlPayCycle.values());
        builder.registerFixedList("fl_pension_contribution", List105FlPensionContribution.values());
        builder.registerFixedList(
                "fl_pro_bono_awarded_against", List106FlProBonoAwardedAgainst.values());
        builder.registerFixedList(
                "fl_representativeContactChangeOptions",
                List107FlRepresentativeContactChangeOptions.values());
        builder.registerFixedList("fl_resTseSelectApp", List108FlResTseSelectApp.values());
        builder.registerFixedList(
                "fl_respondent_legal_entity", List109FlRespondentLegalEntity.values());
        builder.registerFixedList("fl_rule2728ClaimToBe", List110FlRule2728ClaimToBe.values());
        builder.registerFixedList(
                "fl_selectHearingBundlesCollection",
                List111FlSelectHearingBundlesCollection.values());
        builder.registerFixedList(
                "fl_sendNotificationCaseManagement",
                List112FlSendNotificationCaseManagement.values());
        builder.registerFixedList(
                "fl_sendNotificationDecision", List113FlSendNotificationDecision.values());
        builder.registerFixedList(
                "fl_sendNotificationParties", List114FlSendNotificationParties.values());
        builder.registerFixedList(
                "fl_sendNotificationSubject", List115FlSendNotificationSubject.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoCaseOrder", List116FlSendNotificationWhoCaseOrder.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoMadeJudgement",
                List117FlSendNotificationWhoMadeJudgement.values());
        builder.registerFixedList("fl_suggestAnotherTrack", List118FlSuggestAnotherTrack.values());
        builder.registerFixedList(
                "fl_tseAdmReplyCmoMadeBy", List119FlTseAdmReplyCmoMadeBy.values());
        builder.registerFixedList(
                "fl_tseAdmReplyRequestMadeBy", List120FlTseAdmReplyRequestMadeBy.values());
        builder.registerFixedList("frl_ACAS", List121FrlACAS.values());
        builder.registerFixedList(
                "frl_ClaimantCopyToOtherPartyYesOrNo",
                List122FrlClaimantCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList(
                "frl_ClaimantOrRespondents", List123FrlClaimantOrRespondents.values());
        builder.registerFixedList(
                "frl_ClaimantTseCopyToOtherPartyYesOrNo",
                List124FrlClaimantTseCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList("frl_ClaimantType", List125FrlClaimantType.values());
        builder.registerFixedList(
                "frl_CopyToOtherPartyYesOrNo", List126FrlCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList("frl_ReferCaseTo", List127FrlReferCaseTo.values());
        builder.registerFixedList("frl_SitAlone", List128FrlSitAlone.values());
        builder.registerFixedList("frl_bundleType", List129FrlBundleType.values());
        builder.registerFixedList(
                "frl_bundlesRespondentAgreedDocWith",
                List130FrlBundlesRespondentAgreedDocWith.values());
        builder.registerFixedList(
                "frl_bundlesWhatDocuments", List131FrlBundlesWhatDocuments.values());
        builder.registerFixedList(
                "frl_bundlesWhoseDocuments", List132FrlBundlesWhoseDocuments.values());
        builder.registerFixedList("frl_editOrDelete", List133FrlEditOrDelete.values());
        builder.registerFixedList(
                "frl_et3_contact_preference", List134FrlEt3ContactPreference.values());
        builder.registerFixedList("frl_et3_contest_claim", List135FrlEt3ContestClaim.values());
        builder.registerFixedList(
                "frl_et3_yes_no_not_sure_yet", List136FrlEt3YesNoNotSureYet.values());
        builder.registerFixedList(
                "frl_finalHearingListedJudgeOrMembers",
                List139FrlFinalHearingListedJudgeOrMembers.values());
        builder.registerFixedList(
                "frl_futureOrPastHearing", List140FrlFutureOrPastHearing.values());
        builder.registerFixedList(
                "frl_listedCmPreliminaryHearing_Jsa",
                List141FrlListedCmPreliminaryHearingJsa.values());
        builder.registerFixedList("frl_noAcasReason", List142FrlNoAcasReason.values());
        builder.registerFixedList(
                "frl_partyUnavailability", List143FrlPartyUnavailability.values());
        builder.registerFixedList(
                "frl_respondNotificationCmoRequestBy",
                List144FrlRespondNotificationCmoRequestBy.values());
        builder.registerFixedList(
                "frl_respondNotificationRequestBy",
                List145FrlRespondNotificationRequestBy.values());
        builder.registerFixedList("frl_respondentType", List146FrlRespondentType.values());
        builder.registerFixedList(
                "frl_sendNotificationRequestMadeBy",
                List147FrlSendNotificationRequestMadeBy.values());
        builder.registerFixedList(
                "frl_tseAdmReplyIsCmoOrRequest", List148FrlTseAdmReplyIsCmoOrRequest.values());
        builder.registerFixedList("frl_tseAdminDecision", List149FrlTseAdminDecision.values());
        builder.registerFixedList(
                "frl_tseAdminDecisionMadeBy", List150FrlTseAdminDecisionMadeBy.values());
        builder.registerFixedList(
                "frl_tseAdminIsResponseRequired", List151FrlTseAdminIsResponseRequired.values());
        builder.registerFixedList(
                "frl_tseAdminSelectPartyNotify", List152FrlTseAdminSelectPartyNotify.values());
        builder.registerFixedList(
                "frl_tseAdminTypeOfDecision", List153FrlTseAdminTypeOfDecision.values());
        builder.registerFixedList(
                "frl_tseApplicationsOpenOrClosed", List154FrlTseApplicationsOpenOrClosed.values());
        builder.registerFixedList(
                "frl_yes_no_not_applicable", List155FrlYesNoNotApplicable.values());
        builder.registerFixedList("imageRendering", List156ImageRendering.values());
        builder.registerFixedList("imageRenderingLocation", List157ImageRenderingLocation.values());
        builder.registerFixedList(
                "ms_FinalHearingIsEJSitAloneReasonNo",
                List158MsFinalHearingIsEJSitAloneReasonNo.values());
        builder.registerFixedList(
                "ms_FinalHearingIsEJSitAloneReasonYes",
                List159MsFinalHearingIsEJSitAloneReasonYes.values());
        builder.registerFixedList("msl_Defects", List160MslDefectsV1.values());
        builder.registerFixedList("msl_HearingAttendence", List161MslHearingAttendence.values());
        builder.registerFixedList("msl_HearingFormat", List162MslHearingFormat.values());
        builder.registerFixedList("msl_HearingPreferences", List163MslHearingPreferences.values());
        builder.registerFixedList("msl_NoticePeriod", List166MslNoticePeriod.values());
        builder.registerFixedList("msl_NoticePeriodLength", List167MslNoticePeriodLength.values());
        builder.registerFixedList("msl_PayFrequency", List168MslPayFrequency.values());
        builder.registerFixedList(
                "msl_PreAcceptanceResponse", List169MslPreAcceptanceResponseV1.values());
        builder.registerFixedList("msl_REJOrVP", List170MslREJOrVP.values());
        builder.registerFixedList("msl_Response", List171MslResponse.values());
        builder.registerFixedList("msl_StillWorking", List172MslStillWorking.values());
        builder.registerFixedList(
                "msl_WorkPayNoticePeriod", List173MslWorkPayNoticePeriod.values());
        builder.registerFixedList("msl_Yes", List174MslYes.values());
        builder.registerFixedList("msl_YesNo", List175MslYesNo.values());
        builder.registerFixedList("msl_claimOutcomes", List176MslClaimOutcomes.values());
        builder.registerFixedList(
                "msl_closeApplicationYes", List177MslCloseApplicationYes.values());
        builder.registerFixedList(
                "msl_confirmCloseReferral", List178MslConfirmCloseReferral.values());
        builder.registerFixedList("msl_confirmSubmitEt3", List179MslConfirmSubmitEt3.values());
        builder.registerFixedList(
                "msl_discriminationClaims", List180MslDiscriminationClaims.values());
        builder.registerFixedList(
                "msl_et1DiscriminationClaims", List181MslEt1DiscriminationClaims.values());
        builder.registerFixedList("msl_et1TypesOfClaim", List182MslEt1TypesOfClaim.values());
        builder.registerFixedList("msl_et3_hearing_type", List183MslEt3HearingType.values());
        builder.registerFixedList(
                "msl_etICHearingAlreadyListed", List185MslEtICHearingAlreadyListedV1.values());
        builder.registerFixedList(
                "msl_etICHearingNotListed", List186MslEtICHearingNotListedV1.values());
        builder.registerFixedList(
                "msl_etICHearingNotListed_v2", List188MslEtICHearingNotListedV2.values());
        builder.registerFixedList(
                "msl_etICPurposeOfPrelimHearing", List189MslEtICPurposeOfPrelimHearing.values());
        builder.registerFixedList(
                "msl_etICPurposeOfPrelimHearing_v2",
                List190MslEtICPurposeOfPrelimHearingV2.values());
        builder.registerFixedList(
                "msl_etICTypeOfCvpHearing", List192MslEtICTypeOfCvpHearing.values());
        builder.registerFixedList("msl_etICTypeOfHearing", List193MslEtICTypeOfHearingV1.values());
        builder.registerFixedList(
                "msl_etICTypeOfHearing_v2", List195MslEtICTypeOfHearingV2.values());
        builder.registerFixedList("msl_etICUDLCVPIssue", List196MslEtICUDLCVPIssue.values());
        builder.registerFixedList(
                "msl_etICUDLFinalF2FIssue", List197MslEtICUDLFinalF2FIssueV1.values());
        builder.registerFixedList("msl_etICUDLGiveReasons", List198MslEtICUDLGiveReasons.values());
        builder.registerFixedList(
                "msl_finalHearingWithJudgeOrMembersReasonsJsa",
                List199MslFinalHearingWithJudgeOrMembersReasonsJsa.values());
        builder.registerFixedList(
                "msl_finalHearingWithJudgeOrMembersReasonsMembers",
                List200MslFinalHearingWithJudgeOrMembersReasonsMembers.values());
        builder.registerFixedList("msl_furtherInformation", List201MslFurtherInformation.values());
        builder.registerFixedList(
                "msl_hearingWithJudgeOrMembersReasons",
                List202MslHearingWithJudgeOrMembersReasonsV1.values());
        builder.registerFixedList("msl_judgeOrLO", List203MslJudgeOrLO.values());
        builder.registerFixedList("msl_otherFactors", List204MslOtherFactors.values());
        builder.registerFixedList("msl_payClaims", List205MslPayClaims.values());
        builder.registerFixedList("msl_removeDocument", List206MslRemoveDocument.values());
        builder.registerFixedList("msl_rule27direction", List207MslRule27direction.values());
        builder.registerFixedList("msl_submitEt1", List208MslSubmitEt1.values());
        builder.registerFixedList("msl_typeOfClaim", List209MslTypeOfClaim.values());
        builder.registerFixedList("pageNumberFormat", List210PageNumberFormat.values());
        builder.registerFixedList("paginationStyle", List211PaginationStyle.values());
        builder.registerFixedList(
                "sendNotificationEccQuestion", List212SendNotificationEccQuestion.values());
        builder.registerFixedList("sendNotificationNotify", List213SendNotificationNotify.values());
        builder.registerFixedList(
                "sendNotificationResponseTribunal",
                List214SendNotificationResponseTribunal.values());
    }

    static void registerScotlandSingleCftlibDefinition(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder) {
        builder.registerFixedList(
                " fl_respondNotificationResponseRequired",
                List001FlRespondNotificationResponseRequired.values());
        builder.registerFixedList("VenueScotland", List002VenueScotland.values());
        builder.registerFixedList("caseSourceList", List003CaseSourceList.values());
        builder.registerFixedList(
                "claimant_hearingPanelPreference", List004ClaimantHearingPanelPreference.values());
        builder.registerFixedList("configurationFiles", List005ConfigurationFiles.values());
        builder.registerFixedList("createUploadOrRemove", List006CreateUploadOrRemove.values());
        builder.registerFixedList("fl_AddressLabelNumber", List007FlAddressLabelNumber.values());
        builder.registerFixedList("fl_Adjustment", List008FlAdjustment.values());
        builder.registerFixedList("fl_Attendee", List009FlAttendee.values());
        builder.registerFixedList("fl_BFActionsCW", List010FlBFActionsCWV2.values());
        builder.registerFixedList("fl_CaseManagement", List011FlCaseManagement.values());
        builder.registerFixedList("fl_CaseType", List012FlCaseType.values());
        builder.registerFixedList(
                "fl_ClaimantOrRespondent", List013FlClaimantOrRespondent.values());
        builder.registerFixedList(
                "fl_ClaimantRespondentBothParties",
                List014FlClaimantRespondentBothParties.values());
        builder.registerFixedList("fl_Conciliation", List015FlConciliationV2.values());
        builder.registerFixedList("fl_ContactPreference", List016FlContactPreference.values());
        builder.registerFixedList("fl_DepositCovers", List017FlDepositCovers.values());
        builder.registerFixedList("fl_DepositRequestedBy", List018FlDepositRequestedBy.values());
        builder.registerFixedList("fl_DocumentCategories", List019FlDocumentCategories.values());
        builder.registerFixedList("fl_DocumentType", List020FlDocumentTypeV2.values());
        builder.registerFixedList(
                "fl_EmployerContractClaim", List021FlEmployerContractClaim.values());
        builder.registerFixedList(
                "fl_Et3NotificationDocType", List022FlEt3NotificationDocTypeV2.values());
        builder.registerFixedList(
                "fl_FinalHearingIsEJSitAloneReason",
                List023FlFinalHearingIsEJSitAloneReason.values());
        builder.registerFixedList("fl_Gender", List024FlGender.values());
        builder.registerFixedList("fl_Hearing", List025FlHearingV2.values());
        builder.registerFixedList("fl_HearingDateType", List026FlHearingDateType.values());
        builder.registerFixedList("fl_HearingDocETCL", List027FlHearingDocETCL.values());
        builder.registerFixedList("fl_HearingDocType", List028FlHearingDocType.values());
        builder.registerFixedList("fl_HearingLength", List029FlHearingLength.values());
        builder.registerFixedList(
                "fl_HearingPanelPreference", List030FlHearingPanelPreference.values());
        builder.registerFixedList("fl_HearingReadingDelib", List031FlHearingReadingDelib.values());
        builder.registerFixedList("fl_HearingStatus", List032FlHearingStatusV2.values());
        builder.registerFixedList("fl_Hearings", List033FlHearings.values());
        builder.registerFixedList(
                "fl_InitialConsideration", List034FlInitialConsideration.values());
        builder.registerFixedList("fl_JudgeDecision", List035FlJudgeDecision.values());
        builder.registerFixedList("fl_JudgeDirection", List036FlJudgeDirection.values());
        builder.registerFixedList("fl_JudgementType", List037FlJudgementType.values());
        builder.registerFixedList("fl_JudgmentAndReasons", List038FlJudgmentAndReasons.values());
        builder.registerFixedList("fl_JudgmentOutcome", List039FlJudgmentOutcome.values());
        builder.registerFixedList("fl_Liability", List040FlLiabilityV2.values());
        builder.registerFixedList("fl_Misc", List041FlMisc.values());
        builder.registerFixedList("fl_Part_0", List042FlPart0.values());
        builder.registerFixedList("fl_Part_1", List043FlPart1V2.values());
        builder.registerFixedList("fl_Part_10", List044FlPart10V2.values());
        builder.registerFixedList("fl_Part_11", List045FlPart11V2.values());
        builder.registerFixedList("fl_Part_12", List046FlPart12V2.values());
        builder.registerFixedList("fl_Part_13", List047FlPart13V2.values());
        builder.registerFixedList("fl_Part_14", List048FlPart14V2.values());
        builder.registerFixedList("fl_Part_15", List049FlPart15V2.values());
        builder.registerFixedList("fl_Part_16", List050FlPart16V2.values());
        builder.registerFixedList("fl_Part_2", List053FlPart2V2.values());
        builder.registerFixedList("fl_Part_3", List055FlPart3V2.values());
        builder.registerFixedList("fl_Part_4", List056FlPart4V2.values());
        builder.registerFixedList("fl_Part_5", List057FlPart5V2.values());
        builder.registerFixedList("fl_Part_6", List058FlPart6V2.values());
        builder.registerFixedList("fl_Part_7", List059FlPart7V2.values());
        builder.registerFixedList("fl_Part_8", List060FlPart8V2.values());
        builder.registerFixedList("fl_Part_9", List061FlPart9V2.values());
        builder.registerFixedList("fl_Position", List062FlPosition.values());
        builder.registerFixedList("fl_PositionCT", List063FlPositionCT.values());
        builder.registerFixedList("fl_PostponedBy", List064FlPostponedBy.values());
        builder.registerFixedList("fl_PreferredTitle", List065FlPreferredTitle.values());
        builder.registerFixedList("fl_PublicPrivate", List066FlPublicPrivate.values());
        builder.registerFixedList("fl_Reconsideration", List067FlReconsideration.values());
        builder.registerFixedList("fl_ReferralSubject", List068FlReferralSubject.values());
        builder.registerFixedList("fl_Reinstate", List069FlReinstate.values());
        builder.registerFixedList("fl_Reinstated", List070FlReinstated.values());
        builder.registerFixedList(
                "fl_RepresentativeContact", List071FlRepresentativeContact.values());
        builder.registerFixedList(
                "fl_RepresentativeOccupation", List072FlRepresentativeOccupation.values());
        builder.registerFixedList("fl_ResponseStatus", List073FlResponseStatus.values());
        builder.registerFixedList("fl_ResponseToAClaim", List074FlResponseToAClaim.values());
        builder.registerFixedList(
                "fl_RestrictedExcludedRegister", List075FlRestrictedExcludedRegister.values());
        builder.registerFixedList(
                "fl_RestrictedRequestedBy", List076FlRestrictedRequestedBy.values());
        builder.registerFixedList(
                "fl_ServingDocumentRecipient", List077FlServingDocumentRecipient.values());
        builder.registerFixedList(
                "fl_ServingDocumentType", List078FlServingDocumentTypeV2.values());
        builder.registerFixedList("fl_Sex", List079FlSex.values());
        builder.registerFixedList("fl_Stage", List080FlStage.values());
        builder.registerFixedList("fl_StartingAClaim", List081FlStartingAClaim.values());
        builder.registerFixedList("fl_StillWorking", List082FlStillWorking.values());
        builder.registerFixedList("fl_Title", List083FlTitle.values());
        builder.registerFixedList("fl_TopLevel", List084FlTopLevelV2.values());
        builder.registerFixedList("fl_TribunalOffice", List085FlTribunalOfficeV2.values());
        builder.registerFixedList("fl_WithdrawalSettled", List086FlWithdrawalSettled.values());
        builder.registerFixedList("fl_YesOrNoOrPreferNot", List087FlYesOrNoOrPreferNot.values());
        builder.registerFixedList(
                "fl_claimantTseSelectApp", List088FlClaimantTseSelectApp.values());
        builder.registerFixedList("fl_contest_claim_status", List089FlContestClaimStatus.values());
        builder.registerFixedList(
                "fl_costs_pro_bono_awarded_to", List090FlCostsProBonoAwardedTo.values());
        builder.registerFixedList("fl_employer_type", List091FlEmployerType.values());
        builder.registerFixedList("fl_et3Struckout", List092FlEt3Struckout.values());
        builder.registerFixedList("fl_et3_pay_frequency", List093FlEt3PayFrequency.values());
        builder.registerFixedList("fl_et3_suggested_issues", List094FlEt3SuggestedIssues.values());
        builder.registerFixedList(
                "fl_et3_tribunal_location_change", List095FlEt3TribunalLocationChange.values());
        builder.registerFixedList(
                "fl_etICUDLHearingFormat", List096FlEtICUDLHearingFormatV2.values());
        builder.registerFixedList(
                "fl_hearingJudgeAloneOrWithMembers",
                List097FlHearingJudgeAloneOrWithMembers.values());
        builder.registerFixedList("fl_isLocationCorrect", List098FlIsLocationCorrect.values());
        builder.registerFixedList(
                "fl_isTrackAllocationCorrect", List099FlIsTrackAllocationCorrect.values());
        builder.registerFixedList("fl_jurisdictionCodes", List100FlJurisdictionCodes.values());
        builder.registerFixedList("fl_languages", List101FlLanguages.values());
        builder.registerFixedList("fl_letterAddress", List102FlLetterAddress.values());
        builder.registerFixedList("fl_notice_period_unit", List103FlNoticePeriodUnit.values());
        builder.registerFixedList("fl_pay_cycle", List104FlPayCycle.values());
        builder.registerFixedList("fl_pension_contribution", List105FlPensionContribution.values());
        builder.registerFixedList(
                "fl_pro_bono_awarded_against", List106FlProBonoAwardedAgainst.values());
        builder.registerFixedList(
                "fl_representativeContactChangeOptions",
                List107FlRepresentativeContactChangeOptions.values());
        builder.registerFixedList("fl_resTseSelectApp", List108FlResTseSelectApp.values());
        builder.registerFixedList(
                "fl_respondent_legal_entity", List109FlRespondentLegalEntity.values());
        builder.registerFixedList(
                "fl_selectHearingBundlesCollection",
                List111FlSelectHearingBundlesCollection.values());
        builder.registerFixedList(
                "fl_sendNotificationCaseManagement",
                List112FlSendNotificationCaseManagement.values());
        builder.registerFixedList(
                "fl_sendNotificationDecision", List113FlSendNotificationDecision.values());
        builder.registerFixedList(
                "fl_sendNotificationParties", List114FlSendNotificationParties.values());
        builder.registerFixedList(
                "fl_sendNotificationSubject", List115FlSendNotificationSubject.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoCaseOrder", List116FlSendNotificationWhoCaseOrder.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoMadeJudgement",
                List117FlSendNotificationWhoMadeJudgement.values());
        builder.registerFixedList("fl_suggestAnotherTrack", List118FlSuggestAnotherTrack.values());
        builder.registerFixedList(
                "fl_tseAdmReplyCmoMadeBy", List119FlTseAdmReplyCmoMadeBy.values());
        builder.registerFixedList(
                "fl_tseAdmReplyRequestMadeBy", List120FlTseAdmReplyRequestMadeBy.values());
        builder.registerFixedList("frl_ACAS", List121FrlACAS.values());
        builder.registerFixedList(
                "frl_ClaimantCopyToOtherPartyYesOrNo",
                List122FrlClaimantCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList(
                "frl_ClaimantOrRespondents", List123FrlClaimantOrRespondents.values());
        builder.registerFixedList(
                "frl_ClaimantTseCopyToOtherPartyYesOrNo",
                List124FrlClaimantTseCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList("frl_ClaimantType", List125FrlClaimantType.values());
        builder.registerFixedList(
                "frl_CopyToOtherPartyYesOrNo", List126FrlCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList("frl_ReferCaseTo", List127FrlReferCaseTo.values());
        builder.registerFixedList("frl_SitAlone", List128FrlSitAlone.values());
        builder.registerFixedList("frl_bundleType", List129FrlBundleType.values());
        builder.registerFixedList(
                "frl_bundlesRespondentAgreedDocWith",
                List130FrlBundlesRespondentAgreedDocWith.values());
        builder.registerFixedList(
                "frl_bundlesWhatDocuments", List131FrlBundlesWhatDocuments.values());
        builder.registerFixedList(
                "frl_bundlesWhoseDocuments", List132FrlBundlesWhoseDocuments.values());
        builder.registerFixedList("frl_editOrDelete", List133FrlEditOrDelete.values());
        builder.registerFixedList(
                "frl_et3_contact_preference", List134FrlEt3ContactPreference.values());
        builder.registerFixedList("frl_et3_contest_claim", List135FrlEt3ContestClaim.values());
        builder.registerFixedList(
                "frl_et3_yes_no_not_sure_yet", List136FrlEt3YesNoNotSureYet.values());
        builder.registerFixedList(
                "frl_etICRule27ClaimToBe", List137FrlEtICRule27ClaimToBe.values());
        builder.registerFixedList(
                "frl_etICRule28ClaimToBe", List138FrlEtICRule28ClaimToBe.values());
        builder.registerFixedList(
                "frl_futureOrPastHearing", List140FrlFutureOrPastHearing.values());
        builder.registerFixedList(
                "frl_listedCmPreliminaryHearing_Jsa",
                List141FrlListedCmPreliminaryHearingJsa.values());
        builder.registerFixedList("frl_noAcasReason", List142FrlNoAcasReason.values());
        builder.registerFixedList(
                "frl_partyUnavailability", List143FrlPartyUnavailability.values());
        builder.registerFixedList(
                "frl_respondNotificationCmoRequestBy",
                List144FrlRespondNotificationCmoRequestBy.values());
        builder.registerFixedList(
                "frl_respondNotificationRequestBy",
                List145FrlRespondNotificationRequestBy.values());
        builder.registerFixedList("frl_respondentType", List146FrlRespondentType.values());
        builder.registerFixedList(
                "frl_sendNotificationRequestMadeBy",
                List147FrlSendNotificationRequestMadeBy.values());
        builder.registerFixedList(
                "frl_tseAdmReplyIsCmoOrRequest", List148FrlTseAdmReplyIsCmoOrRequest.values());
        builder.registerFixedList("frl_tseAdminDecision", List149FrlTseAdminDecision.values());
        builder.registerFixedList(
                "frl_tseAdminDecisionMadeBy", List150FrlTseAdminDecisionMadeBy.values());
        builder.registerFixedList(
                "frl_tseAdminIsResponseRequired", List151FrlTseAdminIsResponseRequired.values());
        builder.registerFixedList(
                "frl_tseAdminSelectPartyNotify", List152FrlTseAdminSelectPartyNotify.values());
        builder.registerFixedList(
                "frl_tseAdminTypeOfDecision", List153FrlTseAdminTypeOfDecision.values());
        builder.registerFixedList(
                "frl_tseApplicationsOpenOrClosed", List154FrlTseApplicationsOpenOrClosed.values());
        builder.registerFixedList(
                "frl_yes_no_not_applicable", List155FrlYesNoNotApplicable.values());
        builder.registerFixedList("imageRendering", List156ImageRendering.values());
        builder.registerFixedList("imageRenderingLocation", List157ImageRenderingLocation.values());
        builder.registerFixedList(
                "ms_FinalHearingIsEJSitAloneReasonNo",
                List158MsFinalHearingIsEJSitAloneReasonNo.values());
        builder.registerFixedList(
                "ms_FinalHearingIsEJSitAloneReasonYes",
                List159MsFinalHearingIsEJSitAloneReasonYes.values());
        builder.registerFixedList("msl_Defects", List160MslDefectsV2.values());
        builder.registerFixedList("msl_HearingAttendence", List161MslHearingAttendence.values());
        builder.registerFixedList("msl_HearingFormat", List162MslHearingFormat.values());
        builder.registerFixedList("msl_HearingPreferences", List163MslHearingPreferences.values());
        builder.registerFixedList("msl_IcF2FOrders", List164MslIcF2FOrders.values());
        builder.registerFixedList("msl_IcVideoOrders", List165MslIcVideoOrders.values());
        builder.registerFixedList("msl_NoticePeriod", List166MslNoticePeriod.values());
        builder.registerFixedList("msl_NoticePeriodLength", List167MslNoticePeriodLength.values());
        builder.registerFixedList("msl_PayFrequency", List168MslPayFrequency.values());
        builder.registerFixedList(
                "msl_PreAcceptanceResponse", List169MslPreAcceptanceResponseV2.values());
        builder.registerFixedList("msl_REJOrVP", List170MslREJOrVP.values());
        builder.registerFixedList("msl_Response", List171MslResponse.values());
        builder.registerFixedList("msl_StillWorking", List172MslStillWorking.values());
        builder.registerFixedList(
                "msl_WorkPayNoticePeriod", List173MslWorkPayNoticePeriod.values());
        builder.registerFixedList("msl_Yes", List174MslYes.values());
        builder.registerFixedList("msl_YesNo", List175MslYesNo.values());
        builder.registerFixedList("msl_claimOutcomes", List176MslClaimOutcomes.values());
        builder.registerFixedList(
                "msl_closeApplicationYes", List177MslCloseApplicationYes.values());
        builder.registerFixedList(
                "msl_confirmCloseReferral", List178MslConfirmCloseReferral.values());
        builder.registerFixedList("msl_confirmSubmitEt3", List179MslConfirmSubmitEt3.values());
        builder.registerFixedList(
                "msl_discriminationClaims", List180MslDiscriminationClaims.values());
        builder.registerFixedList(
                "msl_et1DiscriminationClaims", List181MslEt1DiscriminationClaims.values());
        builder.registerFixedList("msl_et1TypesOfClaim", List182MslEt1TypesOfClaim.values());
        builder.registerFixedList("msl_et3_hearing_type", List183MslEt3HearingType.values());
        builder.registerFixedList(
                "msl_etICFurtherInformation", List184MslEtICFurtherInformation.values());
        builder.registerFixedList(
                "msl_etICHearingAlreadyListed", List185MslEtICHearingAlreadyListedV2.values());
        builder.registerFixedList(
                "msl_etICHearingNotListed", List186MslEtICHearingNotListedV2.values());
        builder.registerFixedList(
                "msl_etICHearingNotListedUpdated", List187MslEtICHearingNotListedUpdated.values());
        builder.registerFixedList(
                "msl_etICPurposeOfPrelimHearing", List189MslEtICPurposeOfPrelimHearing.values());
        builder.registerFixedList(
                "msl_etICRule27Direction", List191MslEtICRule27Direction.values());
        builder.registerFixedList(
                "msl_etICTypeOfCvpHearing", List192MslEtICTypeOfCvpHearing.values());
        builder.registerFixedList("msl_etICTypeOfHearing", List193MslEtICTypeOfHearingV2.values());
        builder.registerFixedList(
                "msl_etICTypeOfHearingUpdated", List194MslEtICTypeOfHearingUpdated.values());
        builder.registerFixedList("msl_etICUDLCVPIssue", List196MslEtICUDLCVPIssue.values());
        builder.registerFixedList(
                "msl_etICUDLFinalF2FIssue", List197MslEtICUDLFinalF2FIssueV2.values());
        builder.registerFixedList("msl_etICUDLGiveReasons", List198MslEtICUDLGiveReasons.values());
        builder.registerFixedList(
                "msl_finalHearingWithJudgeOrMembersReasonsJsa",
                List199MslFinalHearingWithJudgeOrMembersReasonsJsa.values());
        builder.registerFixedList(
                "msl_finalHearingWithJudgeOrMembersReasonsMembers",
                List200MslFinalHearingWithJudgeOrMembersReasonsMembers.values());
        builder.registerFixedList(
                "msl_hearingWithJudgeOrMembersReasons",
                List202MslHearingWithJudgeOrMembersReasonsV2.values());
        builder.registerFixedList("msl_judgeOrLO", List203MslJudgeOrLO.values());
        builder.registerFixedList("msl_otherFactors", List204MslOtherFactors.values());
        builder.registerFixedList("msl_payClaims", List205MslPayClaims.values());
        builder.registerFixedList("msl_removeDocument", List206MslRemoveDocument.values());
        builder.registerFixedList("msl_submitEt1", List208MslSubmitEt1.values());
        builder.registerFixedList("msl_typeOfClaim", List209MslTypeOfClaim.values());
        builder.registerFixedList("pageNumberFormat", List210PageNumberFormat.values());
        builder.registerFixedList("paginationStyle", List211PaginationStyle.values());
        builder.registerFixedList(
                "sendNotificationEccQuestion", List212SendNotificationEccQuestion.values());
        builder.registerFixedList("sendNotificationNotify", List213SendNotificationNotify.values());
        builder.registerFixedList(
                "sendNotificationResponseTribunal",
                List214SendNotificationResponseTribunal.values());
    }

    static void registerEnglandWalesSingleProdDefinition(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder) {
        builder.registerFixedList(
                " fl_respondNotificationResponseRequired",
                List001FlRespondNotificationResponseRequired.values());
        builder.registerFixedList("caseSourceList", List003CaseSourceList.values());
        builder.registerFixedList(
                "claimant_hearingPanelPreference", List004ClaimantHearingPanelPreference.values());
        builder.registerFixedList("configurationFiles", List005ConfigurationFiles.values());
        builder.registerFixedList("createUploadOrRemove", List006CreateUploadOrRemove.values());
        builder.registerFixedList("fl_AddressLabelNumber", List007FlAddressLabelNumber.values());
        builder.registerFixedList("fl_Adjustment", List008FlAdjustment.values());
        builder.registerFixedList("fl_Attendee", List009FlAttendee.values());
        builder.registerFixedList("fl_BFActionsCW", List010FlBFActionsCWV1.values());
        builder.registerFixedList("fl_CaseManagement", List011FlCaseManagement.values());
        builder.registerFixedList("fl_CaseType", List012FlCaseType.values());
        builder.registerFixedList(
                "fl_ClaimantOrRespondent", List013FlClaimantOrRespondent.values());
        builder.registerFixedList(
                "fl_ClaimantRespondentBothParties",
                List014FlClaimantRespondentBothParties.values());
        builder.registerFixedList("fl_Conciliation", List015FlConciliationV1.values());
        builder.registerFixedList("fl_ContactPreference", List016FlContactPreference.values());
        builder.registerFixedList("fl_DepositCovers", List017FlDepositCovers.values());
        builder.registerFixedList("fl_DepositRequestedBy", List018FlDepositRequestedBy.values());
        builder.registerFixedList("fl_DocumentCategories", List019FlDocumentCategories.values());
        builder.registerFixedList("fl_DocumentType", List020FlDocumentTypeV1.values());
        builder.registerFixedList(
                "fl_EmployerContractClaim", List021FlEmployerContractClaim.values());
        builder.registerFixedList(
                "fl_Et3NotificationDocType", List022FlEt3NotificationDocTypeV1.values());
        builder.registerFixedList(
                "fl_FinalHearingIsEJSitAloneReason",
                List023FlFinalHearingIsEJSitAloneReason.values());
        builder.registerFixedList("fl_Gender", List024FlGender.values());
        builder.registerFixedList("fl_Hearing", List025FlHearingV1.values());
        builder.registerFixedList("fl_HearingDateType", List026FlHearingDateType.values());
        builder.registerFixedList("fl_HearingDocETCL", List027FlHearingDocETCL.values());
        builder.registerFixedList("fl_HearingDocType", List028FlHearingDocType.values());
        builder.registerFixedList("fl_HearingLength", List029FlHearingLength.values());
        builder.registerFixedList(
                "fl_HearingPanelPreference", List030FlHearingPanelPreference.values());
        builder.registerFixedList("fl_HearingStatus", List032FlHearingStatusV1.values());
        builder.registerFixedList("fl_Hearings", List033FlHearings.values());
        builder.registerFixedList(
                "fl_InitialConsideration", List034FlInitialConsideration.values());
        builder.registerFixedList("fl_JudgeDecision", List035FlJudgeDecision.values());
        builder.registerFixedList("fl_JudgeDirection", List036FlJudgeDirection.values());
        builder.registerFixedList("fl_JudgementType", List037FlJudgementType.values());
        builder.registerFixedList("fl_JudgmentAndReasons", List038FlJudgmentAndReasons.values());
        builder.registerFixedList("fl_JudgmentOutcome", List039FlJudgmentOutcome.values());
        builder.registerFixedList("fl_Liability", List040FlLiabilityV1.values());
        builder.registerFixedList("fl_Misc", List041FlMisc.values());
        builder.registerFixedList("fl_Part_0", List042FlPart0.values());
        builder.registerFixedList("fl_Part_1", List043FlPart1V1.values());
        builder.registerFixedList("fl_Part_10", List044FlPart10V1.values());
        builder.registerFixedList("fl_Part_11", List045FlPart11V1.values());
        builder.registerFixedList("fl_Part_12", List046FlPart12V1.values());
        builder.registerFixedList("fl_Part_13", List047FlPart13V1.values());
        builder.registerFixedList("fl_Part_14", List048FlPart14V1.values());
        builder.registerFixedList("fl_Part_15", List049FlPart15V1.values());
        builder.registerFixedList("fl_Part_16", List050FlPart16V1.values());
        builder.registerFixedList("fl_Part_17", List051FlPart17.values());
        builder.registerFixedList("fl_Part_18", List052FlPart18.values());
        builder.registerFixedList("fl_Part_2", List053FlPart2V1.values());
        builder.registerFixedList("fl_Part_20", List054FlPart20.values());
        builder.registerFixedList("fl_Part_3", List055FlPart3V1.values());
        builder.registerFixedList("fl_Part_4", List056FlPart4V1.values());
        builder.registerFixedList("fl_Part_5", List057FlPart5V1.values());
        builder.registerFixedList("fl_Part_6", List058FlPart6V1.values());
        builder.registerFixedList("fl_Part_7", List059FlPart7V1.values());
        builder.registerFixedList("fl_Part_8", List060FlPart8V1.values());
        builder.registerFixedList("fl_Part_9", List061FlPart9V1.values());
        builder.registerFixedList("fl_Position", List062FlPosition.values());
        builder.registerFixedList("fl_PositionCT", List063FlPositionCT.values());
        builder.registerFixedList("fl_PostponedBy", List064FlPostponedBy.values());
        builder.registerFixedList("fl_PreferredTitle", List065FlPreferredTitle.values());
        builder.registerFixedList("fl_PublicPrivate", List066FlPublicPrivate.values());
        builder.registerFixedList("fl_Reconsideration", List067FlReconsideration.values());
        builder.registerFixedList("fl_ReferralSubject", List068FlReferralSubject.values());
        builder.registerFixedList("fl_Reinstate", List069FlReinstate.values());
        builder.registerFixedList("fl_Reinstated", List070FlReinstated.values());
        builder.registerFixedList(
                "fl_RepresentativeContact", List071FlRepresentativeContact.values());
        builder.registerFixedList(
                "fl_RepresentativeOccupation", List072FlRepresentativeOccupation.values());
        builder.registerFixedList("fl_ResponseStatus", List073FlResponseStatus.values());
        builder.registerFixedList("fl_ResponseToAClaim", List074FlResponseToAClaim.values());
        builder.registerFixedList(
                "fl_RestrictedExcludedRegister", List075FlRestrictedExcludedRegister.values());
        builder.registerFixedList(
                "fl_RestrictedRequestedBy", List076FlRestrictedRequestedBy.values());
        builder.registerFixedList(
                "fl_ServingDocumentRecipient", List077FlServingDocumentRecipient.values());
        builder.registerFixedList(
                "fl_ServingDocumentType", List078FlServingDocumentTypeV1.values());
        builder.registerFixedList("fl_Sex", List079FlSex.values());
        builder.registerFixedList("fl_Stage", List080FlStage.values());
        builder.registerFixedList("fl_StartingAClaim", List081FlStartingAClaim.values());
        builder.registerFixedList("fl_StillWorking", List082FlStillWorking.values());
        builder.registerFixedList("fl_Title", List083FlTitle.values());
        builder.registerFixedList("fl_TopLevel", List084FlTopLevelV1.values());
        builder.registerFixedList("fl_TribunalOffice", List085FlTribunalOfficeV1.values());
        builder.registerFixedList("fl_WithdrawalSettled", List086FlWithdrawalSettled.values());
        builder.registerFixedList("fl_YesOrNoOrPreferNot", List087FlYesOrNoOrPreferNot.values());
        builder.registerFixedList(
                "fl_claimantTseSelectApp", List088FlClaimantTseSelectApp.values());
        builder.registerFixedList("fl_contest_claim_status", List089FlContestClaimStatus.values());
        builder.registerFixedList(
                "fl_costs_pro_bono_awarded_to", List090FlCostsProBonoAwardedTo.values());
        builder.registerFixedList("fl_employer_type", List091FlEmployerType.values());
        builder.registerFixedList("fl_et3Struckout", List092FlEt3Struckout.values());
        builder.registerFixedList("fl_et3_pay_frequency", List093FlEt3PayFrequency.values());
        builder.registerFixedList("fl_et3_suggested_issues", List094FlEt3SuggestedIssues.values());
        builder.registerFixedList(
                "fl_et3_tribunal_location_change", List095FlEt3TribunalLocationChange.values());
        builder.registerFixedList(
                "fl_etICUDLHearingFormat", List096FlEtICUDLHearingFormatV1.values());
        builder.registerFixedList(
                "fl_hearingJudgeAloneOrWithMembers",
                List097FlHearingJudgeAloneOrWithMembers.values());
        builder.registerFixedList("fl_isLocationCorrect", List098FlIsLocationCorrect.values());
        builder.registerFixedList(
                "fl_isTrackAllocationCorrect", List099FlIsTrackAllocationCorrect.values());
        builder.registerFixedList("fl_jurisdictionCodes", List100FlJurisdictionCodes.values());
        builder.registerFixedList("fl_languages", List101FlLanguages.values());
        builder.registerFixedList("fl_notice_period_unit", List103FlNoticePeriodUnit.values());
        builder.registerFixedList("fl_pay_cycle", List104FlPayCycle.values());
        builder.registerFixedList("fl_pension_contribution", List105FlPensionContribution.values());
        builder.registerFixedList(
                "fl_pro_bono_awarded_against", List106FlProBonoAwardedAgainst.values());
        builder.registerFixedList(
                "fl_representativeContactChangeOptions",
                List107FlRepresentativeContactChangeOptions.values());
        builder.registerFixedList("fl_resTseSelectApp", List108FlResTseSelectApp.values());
        builder.registerFixedList(
                "fl_respondent_legal_entity", List109FlRespondentLegalEntity.values());
        builder.registerFixedList("fl_rule2728ClaimToBe", List110FlRule2728ClaimToBe.values());
        builder.registerFixedList(
                "fl_selectHearingBundlesCollection",
                List111FlSelectHearingBundlesCollection.values());
        builder.registerFixedList(
                "fl_sendNotificationCaseManagement",
                List112FlSendNotificationCaseManagement.values());
        builder.registerFixedList(
                "fl_sendNotificationDecision", List113FlSendNotificationDecision.values());
        builder.registerFixedList(
                "fl_sendNotificationParties", List114FlSendNotificationParties.values());
        builder.registerFixedList(
                "fl_sendNotificationSubject", List115FlSendNotificationSubject.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoCaseOrder", List116FlSendNotificationWhoCaseOrder.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoMadeJudgement",
                List117FlSendNotificationWhoMadeJudgement.values());
        builder.registerFixedList("fl_suggestAnotherTrack", List118FlSuggestAnotherTrack.values());
        builder.registerFixedList(
                "fl_tseAdmReplyCmoMadeBy", List119FlTseAdmReplyCmoMadeBy.values());
        builder.registerFixedList(
                "fl_tseAdmReplyRequestMadeBy", List120FlTseAdmReplyRequestMadeBy.values());
        builder.registerFixedList("frl_ACAS", List121FrlACAS.values());
        builder.registerFixedList(
                "frl_ClaimantCopyToOtherPartyYesOrNo",
                List122FrlClaimantCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList(
                "frl_ClaimantOrRespondents", List123FrlClaimantOrRespondents.values());
        builder.registerFixedList(
                "frl_ClaimantTseCopyToOtherPartyYesOrNo",
                List124FrlClaimantTseCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList("frl_ClaimantType", List125FrlClaimantType.values());
        builder.registerFixedList(
                "frl_CopyToOtherPartyYesOrNo", List126FrlCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList("frl_ReferCaseTo", List127FrlReferCaseTo.values());
        builder.registerFixedList("frl_SitAlone", List128FrlSitAlone.values());
        builder.registerFixedList("frl_bundleType", List129FrlBundleType.values());
        builder.registerFixedList(
                "frl_bundlesRespondentAgreedDocWith",
                List130FrlBundlesRespondentAgreedDocWith.values());
        builder.registerFixedList(
                "frl_bundlesWhatDocuments", List131FrlBundlesWhatDocuments.values());
        builder.registerFixedList(
                "frl_bundlesWhoseDocuments", List132FrlBundlesWhoseDocuments.values());
        builder.registerFixedList("frl_editOrDelete", List133FrlEditOrDelete.values());
        builder.registerFixedList(
                "frl_et3_contact_preference", List134FrlEt3ContactPreference.values());
        builder.registerFixedList("frl_et3_contest_claim", List135FrlEt3ContestClaim.values());
        builder.registerFixedList(
                "frl_et3_yes_no_not_sure_yet", List136FrlEt3YesNoNotSureYet.values());
        builder.registerFixedList(
                "frl_finalHearingListedJudgeOrMembers",
                List139FrlFinalHearingListedJudgeOrMembers.values());
        builder.registerFixedList(
                "frl_futureOrPastHearing", List140FrlFutureOrPastHearing.values());
        builder.registerFixedList(
                "frl_listedCmPreliminaryHearing_Jsa",
                List141FrlListedCmPreliminaryHearingJsa.values());
        builder.registerFixedList("frl_noAcasReason", List142FrlNoAcasReason.values());
        builder.registerFixedList(
                "frl_respondNotificationCmoRequestBy",
                List144FrlRespondNotificationCmoRequestBy.values());
        builder.registerFixedList(
                "frl_respondNotificationRequestBy",
                List145FrlRespondNotificationRequestBy.values());
        builder.registerFixedList("frl_respondentType", List146FrlRespondentType.values());
        builder.registerFixedList(
                "frl_sendNotificationRequestMadeBy",
                List147FrlSendNotificationRequestMadeBy.values());
        builder.registerFixedList(
                "frl_tseAdmReplyIsCmoOrRequest", List148FrlTseAdmReplyIsCmoOrRequest.values());
        builder.registerFixedList("frl_tseAdminDecision", List149FrlTseAdminDecision.values());
        builder.registerFixedList(
                "frl_tseAdminDecisionMadeBy", List150FrlTseAdminDecisionMadeBy.values());
        builder.registerFixedList(
                "frl_tseAdminIsResponseRequired", List151FrlTseAdminIsResponseRequired.values());
        builder.registerFixedList(
                "frl_tseAdminSelectPartyNotify", List152FrlTseAdminSelectPartyNotify.values());
        builder.registerFixedList(
                "frl_tseAdminTypeOfDecision", List153FrlTseAdminTypeOfDecision.values());
        builder.registerFixedList(
                "frl_tseApplicationsOpenOrClosed", List154FrlTseApplicationsOpenOrClosed.values());
        builder.registerFixedList(
                "frl_yes_no_not_applicable", List155FrlYesNoNotApplicable.values());
        builder.registerFixedList("imageRendering", List156ImageRendering.values());
        builder.registerFixedList("imageRenderingLocation", List157ImageRenderingLocation.values());
        builder.registerFixedList(
                "ms_FinalHearingIsEJSitAloneReasonNo",
                List158MsFinalHearingIsEJSitAloneReasonNo.values());
        builder.registerFixedList(
                "ms_FinalHearingIsEJSitAloneReasonYes",
                List159MsFinalHearingIsEJSitAloneReasonYes.values());
        builder.registerFixedList("msl_Defects", List160MslDefectsV1.values());
        builder.registerFixedList("msl_HearingAttendence", List161MslHearingAttendence.values());
        builder.registerFixedList("msl_HearingFormat", List162MslHearingFormat.values());
        builder.registerFixedList("msl_HearingPreferences", List163MslHearingPreferences.values());
        builder.registerFixedList("msl_NoticePeriod", List166MslNoticePeriod.values());
        builder.registerFixedList("msl_NoticePeriodLength", List167MslNoticePeriodLength.values());
        builder.registerFixedList("msl_PayFrequency", List168MslPayFrequency.values());
        builder.registerFixedList(
                "msl_PreAcceptanceResponse", List169MslPreAcceptanceResponseV1.values());
        builder.registerFixedList("msl_REJOrVP", List170MslREJOrVP.values());
        builder.registerFixedList("msl_Response", List171MslResponse.values());
        builder.registerFixedList("msl_StillWorking", List172MslStillWorking.values());
        builder.registerFixedList(
                "msl_WorkPayNoticePeriod", List173MslWorkPayNoticePeriod.values());
        builder.registerFixedList("msl_Yes", List174MslYes.values());
        builder.registerFixedList("msl_YesNo", List175MslYesNo.values());
        builder.registerFixedList("msl_claimOutcomes", List176MslClaimOutcomes.values());
        builder.registerFixedList(
                "msl_closeApplicationYes", List177MslCloseApplicationYes.values());
        builder.registerFixedList(
                "msl_confirmCloseReferral", List178MslConfirmCloseReferral.values());
        builder.registerFixedList("msl_confirmSubmitEt3", List179MslConfirmSubmitEt3.values());
        builder.registerFixedList(
                "msl_discriminationClaims", List180MslDiscriminationClaims.values());
        builder.registerFixedList(
                "msl_et1DiscriminationClaims", List181MslEt1DiscriminationClaims.values());
        builder.registerFixedList("msl_et1TypesOfClaim", List182MslEt1TypesOfClaim.values());
        builder.registerFixedList("msl_et3_hearing_type", List183MslEt3HearingType.values());
        builder.registerFixedList(
                "msl_etICHearingAlreadyListed", List185MslEtICHearingAlreadyListedV1.values());
        builder.registerFixedList(
                "msl_etICHearingNotListed", List186MslEtICHearingNotListedV1.values());
        builder.registerFixedList(
                "msl_etICHearingNotListed_v2", List188MslEtICHearingNotListedV2.values());
        builder.registerFixedList(
                "msl_etICPurposeOfPrelimHearing", List189MslEtICPurposeOfPrelimHearing.values());
        builder.registerFixedList(
                "msl_etICPurposeOfPrelimHearing_v2",
                List190MslEtICPurposeOfPrelimHearingV2.values());
        builder.registerFixedList(
                "msl_etICTypeOfCvpHearing", List192MslEtICTypeOfCvpHearing.values());
        builder.registerFixedList("msl_etICTypeOfHearing", List193MslEtICTypeOfHearingV1.values());
        builder.registerFixedList(
                "msl_etICTypeOfHearing_v2", List195MslEtICTypeOfHearingV2.values());
        builder.registerFixedList("msl_etICUDLCVPIssue", List196MslEtICUDLCVPIssue.values());
        builder.registerFixedList(
                "msl_etICUDLFinalF2FIssue", List197MslEtICUDLFinalF2FIssueV1.values());
        builder.registerFixedList("msl_etICUDLGiveReasons", List198MslEtICUDLGiveReasons.values());
        builder.registerFixedList(
                "msl_finalHearingWithJudgeOrMembersReasonsJsa",
                List199MslFinalHearingWithJudgeOrMembersReasonsJsa.values());
        builder.registerFixedList(
                "msl_finalHearingWithJudgeOrMembersReasonsMembers",
                List200MslFinalHearingWithJudgeOrMembersReasonsMembers.values());
        builder.registerFixedList("msl_furtherInformation", List201MslFurtherInformation.values());
        builder.registerFixedList(
                "msl_hearingWithJudgeOrMembersReasons",
                List202MslHearingWithJudgeOrMembersReasonsV1.values());
        builder.registerFixedList("msl_judgeOrLO", List203MslJudgeOrLO.values());
        builder.registerFixedList("msl_otherFactors", List204MslOtherFactors.values());
        builder.registerFixedList("msl_payClaims", List205MslPayClaims.values());
        builder.registerFixedList("msl_removeDocument", List206MslRemoveDocument.values());
        builder.registerFixedList("msl_rule27direction", List207MslRule27direction.values());
        builder.registerFixedList("msl_submitEt1", List208MslSubmitEt1.values());
        builder.registerFixedList("msl_typeOfClaim", List209MslTypeOfClaim.values());
        builder.registerFixedList("pageNumberFormat", List210PageNumberFormat.values());
        builder.registerFixedList("paginationStyle", List211PaginationStyle.values());
        builder.registerFixedList(
                "sendNotificationEccQuestion", List212SendNotificationEccQuestion.values());
        builder.registerFixedList("sendNotificationNotify", List213SendNotificationNotify.values());
        builder.registerFixedList(
                "sendNotificationResponseTribunal",
                List214SendNotificationResponseTribunal.values());
    }

    static void registerScotlandSingleProdDefinition(
            ConfigBuilder<CaseData, CaseState, SingleRole> builder) {
        builder.registerFixedList(
                " fl_respondNotificationResponseRequired",
                List001FlRespondNotificationResponseRequired.values());
        builder.registerFixedList("VenueScotland", List002VenueScotland.values());
        builder.registerFixedList("caseSourceList", List003CaseSourceList.values());
        builder.registerFixedList(
                "claimant_hearingPanelPreference", List004ClaimantHearingPanelPreference.values());
        builder.registerFixedList("configurationFiles", List005ConfigurationFiles.values());
        builder.registerFixedList("createUploadOrRemove", List006CreateUploadOrRemove.values());
        builder.registerFixedList("fl_AddressLabelNumber", List007FlAddressLabelNumber.values());
        builder.registerFixedList("fl_Adjustment", List008FlAdjustment.values());
        builder.registerFixedList("fl_Attendee", List009FlAttendee.values());
        builder.registerFixedList("fl_BFActionsCW", List010FlBFActionsCWV2.values());
        builder.registerFixedList("fl_CaseManagement", List011FlCaseManagement.values());
        builder.registerFixedList("fl_CaseType", List012FlCaseType.values());
        builder.registerFixedList(
                "fl_ClaimantOrRespondent", List013FlClaimantOrRespondent.values());
        builder.registerFixedList(
                "fl_ClaimantRespondentBothParties",
                List014FlClaimantRespondentBothParties.values());
        builder.registerFixedList("fl_Conciliation", List015FlConciliationV2.values());
        builder.registerFixedList("fl_ContactPreference", List016FlContactPreference.values());
        builder.registerFixedList("fl_DepositCovers", List017FlDepositCovers.values());
        builder.registerFixedList("fl_DepositRequestedBy", List018FlDepositRequestedBy.values());
        builder.registerFixedList("fl_DocumentCategories", List019FlDocumentCategories.values());
        builder.registerFixedList("fl_DocumentType", List020FlDocumentTypeV2.values());
        builder.registerFixedList(
                "fl_EmployerContractClaim", List021FlEmployerContractClaim.values());
        builder.registerFixedList(
                "fl_Et3NotificationDocType", List022FlEt3NotificationDocTypeV2.values());
        builder.registerFixedList(
                "fl_FinalHearingIsEJSitAloneReason",
                List023FlFinalHearingIsEJSitAloneReason.values());
        builder.registerFixedList("fl_Gender", List024FlGender.values());
        builder.registerFixedList("fl_Hearing", List025FlHearingV2.values());
        builder.registerFixedList("fl_HearingDateType", List026FlHearingDateType.values());
        builder.registerFixedList("fl_HearingDocETCL", List027FlHearingDocETCL.values());
        builder.registerFixedList("fl_HearingDocType", List028FlHearingDocType.values());
        builder.registerFixedList("fl_HearingLength", List029FlHearingLength.values());
        builder.registerFixedList(
                "fl_HearingPanelPreference", List030FlHearingPanelPreference.values());
        builder.registerFixedList("fl_HearingReadingDelib", List031FlHearingReadingDelib.values());
        builder.registerFixedList("fl_HearingStatus", List032FlHearingStatusV2.values());
        builder.registerFixedList("fl_Hearings", List033FlHearings.values());
        builder.registerFixedList(
                "fl_InitialConsideration", List034FlInitialConsideration.values());
        builder.registerFixedList("fl_JudgeDecision", List035FlJudgeDecision.values());
        builder.registerFixedList("fl_JudgeDirection", List036FlJudgeDirection.values());
        builder.registerFixedList("fl_JudgementType", List037FlJudgementType.values());
        builder.registerFixedList("fl_JudgmentAndReasons", List038FlJudgmentAndReasons.values());
        builder.registerFixedList("fl_JudgmentOutcome", List039FlJudgmentOutcome.values());
        builder.registerFixedList("fl_Liability", List040FlLiabilityV2.values());
        builder.registerFixedList("fl_Misc", List041FlMisc.values());
        builder.registerFixedList("fl_Part_0", List042FlPart0.values());
        builder.registerFixedList("fl_Part_1", List043FlPart1V2.values());
        builder.registerFixedList("fl_Part_10", List044FlPart10V2.values());
        builder.registerFixedList("fl_Part_11", List045FlPart11V2.values());
        builder.registerFixedList("fl_Part_12", List046FlPart12V2.values());
        builder.registerFixedList("fl_Part_13", List047FlPart13V2.values());
        builder.registerFixedList("fl_Part_14", List048FlPart14V2.values());
        builder.registerFixedList("fl_Part_15", List049FlPart15V2.values());
        builder.registerFixedList("fl_Part_16", List050FlPart16V2.values());
        builder.registerFixedList("fl_Part_2", List053FlPart2V2.values());
        builder.registerFixedList("fl_Part_3", List055FlPart3V2.values());
        builder.registerFixedList("fl_Part_4", List056FlPart4V2.values());
        builder.registerFixedList("fl_Part_5", List057FlPart5V2.values());
        builder.registerFixedList("fl_Part_6", List058FlPart6V2.values());
        builder.registerFixedList("fl_Part_7", List059FlPart7V2.values());
        builder.registerFixedList("fl_Part_8", List060FlPart8V2.values());
        builder.registerFixedList("fl_Part_9", List061FlPart9V2.values());
        builder.registerFixedList("fl_Position", List062FlPosition.values());
        builder.registerFixedList("fl_PositionCT", List063FlPositionCT.values());
        builder.registerFixedList("fl_PostponedBy", List064FlPostponedBy.values());
        builder.registerFixedList("fl_PreferredTitle", List065FlPreferredTitle.values());
        builder.registerFixedList("fl_PublicPrivate", List066FlPublicPrivate.values());
        builder.registerFixedList("fl_Reconsideration", List067FlReconsideration.values());
        builder.registerFixedList("fl_ReferralSubject", List068FlReferralSubject.values());
        builder.registerFixedList("fl_Reinstate", List069FlReinstate.values());
        builder.registerFixedList("fl_Reinstated", List070FlReinstated.values());
        builder.registerFixedList(
                "fl_RepresentativeContact", List071FlRepresentativeContact.values());
        builder.registerFixedList(
                "fl_RepresentativeOccupation", List072FlRepresentativeOccupation.values());
        builder.registerFixedList("fl_ResponseStatus", List073FlResponseStatus.values());
        builder.registerFixedList("fl_ResponseToAClaim", List074FlResponseToAClaim.values());
        builder.registerFixedList(
                "fl_RestrictedExcludedRegister", List075FlRestrictedExcludedRegister.values());
        builder.registerFixedList(
                "fl_RestrictedRequestedBy", List076FlRestrictedRequestedBy.values());
        builder.registerFixedList(
                "fl_ServingDocumentRecipient", List077FlServingDocumentRecipient.values());
        builder.registerFixedList(
                "fl_ServingDocumentType", List078FlServingDocumentTypeV2.values());
        builder.registerFixedList("fl_Sex", List079FlSex.values());
        builder.registerFixedList("fl_Stage", List080FlStage.values());
        builder.registerFixedList("fl_StartingAClaim", List081FlStartingAClaim.values());
        builder.registerFixedList("fl_StillWorking", List082FlStillWorking.values());
        builder.registerFixedList("fl_Title", List083FlTitle.values());
        builder.registerFixedList("fl_TopLevel", List084FlTopLevelV2.values());
        builder.registerFixedList("fl_TribunalOffice", List085FlTribunalOfficeV2.values());
        builder.registerFixedList("fl_WithdrawalSettled", List086FlWithdrawalSettled.values());
        builder.registerFixedList("fl_YesOrNoOrPreferNot", List087FlYesOrNoOrPreferNot.values());
        builder.registerFixedList(
                "fl_claimantTseSelectApp", List088FlClaimantTseSelectApp.values());
        builder.registerFixedList("fl_contest_claim_status", List089FlContestClaimStatus.values());
        builder.registerFixedList(
                "fl_costs_pro_bono_awarded_to", List090FlCostsProBonoAwardedTo.values());
        builder.registerFixedList("fl_employer_type", List091FlEmployerType.values());
        builder.registerFixedList("fl_et3Struckout", List092FlEt3Struckout.values());
        builder.registerFixedList("fl_et3_pay_frequency", List093FlEt3PayFrequency.values());
        builder.registerFixedList("fl_et3_suggested_issues", List094FlEt3SuggestedIssues.values());
        builder.registerFixedList(
                "fl_et3_tribunal_location_change", List095FlEt3TribunalLocationChange.values());
        builder.registerFixedList(
                "fl_etICUDLHearingFormat", List096FlEtICUDLHearingFormatV2.values());
        builder.registerFixedList(
                "fl_hearingJudgeAloneOrWithMembers",
                List097FlHearingJudgeAloneOrWithMembers.values());
        builder.registerFixedList("fl_isLocationCorrect", List098FlIsLocationCorrect.values());
        builder.registerFixedList(
                "fl_isTrackAllocationCorrect", List099FlIsTrackAllocationCorrect.values());
        builder.registerFixedList("fl_jurisdictionCodes", List100FlJurisdictionCodes.values());
        builder.registerFixedList("fl_languages", List101FlLanguages.values());
        builder.registerFixedList("fl_letterAddress", List102FlLetterAddress.values());
        builder.registerFixedList("fl_notice_period_unit", List103FlNoticePeriodUnit.values());
        builder.registerFixedList("fl_pay_cycle", List104FlPayCycle.values());
        builder.registerFixedList("fl_pension_contribution", List105FlPensionContribution.values());
        builder.registerFixedList(
                "fl_pro_bono_awarded_against", List106FlProBonoAwardedAgainst.values());
        builder.registerFixedList(
                "fl_representativeContactChangeOptions",
                List107FlRepresentativeContactChangeOptions.values());
        builder.registerFixedList("fl_resTseSelectApp", List108FlResTseSelectApp.values());
        builder.registerFixedList(
                "fl_respondent_legal_entity", List109FlRespondentLegalEntity.values());
        builder.registerFixedList(
                "fl_selectHearingBundlesCollection",
                List111FlSelectHearingBundlesCollection.values());
        builder.registerFixedList(
                "fl_sendNotificationCaseManagement",
                List112FlSendNotificationCaseManagement.values());
        builder.registerFixedList(
                "fl_sendNotificationDecision", List113FlSendNotificationDecision.values());
        builder.registerFixedList(
                "fl_sendNotificationParties", List114FlSendNotificationParties.values());
        builder.registerFixedList(
                "fl_sendNotificationSubject", List115FlSendNotificationSubject.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoCaseOrder", List116FlSendNotificationWhoCaseOrder.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoMadeJudgement",
                List117FlSendNotificationWhoMadeJudgement.values());
        builder.registerFixedList("fl_suggestAnotherTrack", List118FlSuggestAnotherTrack.values());
        builder.registerFixedList(
                "fl_tseAdmReplyCmoMadeBy", List119FlTseAdmReplyCmoMadeBy.values());
        builder.registerFixedList(
                "fl_tseAdmReplyRequestMadeBy", List120FlTseAdmReplyRequestMadeBy.values());
        builder.registerFixedList("frl_ACAS", List121FrlACAS.values());
        builder.registerFixedList(
                "frl_ClaimantCopyToOtherPartyYesOrNo",
                List122FrlClaimantCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList(
                "frl_ClaimantOrRespondents", List123FrlClaimantOrRespondents.values());
        builder.registerFixedList(
                "frl_ClaimantTseCopyToOtherPartyYesOrNo",
                List124FrlClaimantTseCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList("frl_ClaimantType", List125FrlClaimantType.values());
        builder.registerFixedList(
                "frl_CopyToOtherPartyYesOrNo", List126FrlCopyToOtherPartyYesOrNo.values());
        builder.registerFixedList("frl_ReferCaseTo", List127FrlReferCaseTo.values());
        builder.registerFixedList("frl_SitAlone", List128FrlSitAlone.values());
        builder.registerFixedList("frl_bundleType", List129FrlBundleType.values());
        builder.registerFixedList(
                "frl_bundlesRespondentAgreedDocWith",
                List130FrlBundlesRespondentAgreedDocWith.values());
        builder.registerFixedList(
                "frl_bundlesWhatDocuments", List131FrlBundlesWhatDocuments.values());
        builder.registerFixedList(
                "frl_bundlesWhoseDocuments", List132FrlBundlesWhoseDocuments.values());
        builder.registerFixedList("frl_editOrDelete", List133FrlEditOrDelete.values());
        builder.registerFixedList(
                "frl_et3_contact_preference", List134FrlEt3ContactPreference.values());
        builder.registerFixedList("frl_et3_contest_claim", List135FrlEt3ContestClaim.values());
        builder.registerFixedList(
                "frl_et3_yes_no_not_sure_yet", List136FrlEt3YesNoNotSureYet.values());
        builder.registerFixedList(
                "frl_etICRule27ClaimToBe", List137FrlEtICRule27ClaimToBe.values());
        builder.registerFixedList(
                "frl_etICRule28ClaimToBe", List138FrlEtICRule28ClaimToBe.values());
        builder.registerFixedList(
                "frl_futureOrPastHearing", List140FrlFutureOrPastHearing.values());
        builder.registerFixedList(
                "frl_listedCmPreliminaryHearing_Jsa",
                List141FrlListedCmPreliminaryHearingJsa.values());
        builder.registerFixedList("frl_noAcasReason", List142FrlNoAcasReason.values());
        builder.registerFixedList(
                "frl_respondNotificationCmoRequestBy",
                List144FrlRespondNotificationCmoRequestBy.values());
        builder.registerFixedList(
                "frl_respondNotificationRequestBy",
                List145FrlRespondNotificationRequestBy.values());
        builder.registerFixedList("frl_respondentType", List146FrlRespondentType.values());
        builder.registerFixedList(
                "frl_sendNotificationRequestMadeBy",
                List147FrlSendNotificationRequestMadeBy.values());
        builder.registerFixedList(
                "frl_tseAdmReplyIsCmoOrRequest", List148FrlTseAdmReplyIsCmoOrRequest.values());
        builder.registerFixedList("frl_tseAdminDecision", List149FrlTseAdminDecision.values());
        builder.registerFixedList(
                "frl_tseAdminDecisionMadeBy", List150FrlTseAdminDecisionMadeBy.values());
        builder.registerFixedList(
                "frl_tseAdminIsResponseRequired", List151FrlTseAdminIsResponseRequired.values());
        builder.registerFixedList(
                "frl_tseAdminSelectPartyNotify", List152FrlTseAdminSelectPartyNotify.values());
        builder.registerFixedList(
                "frl_tseAdminTypeOfDecision", List153FrlTseAdminTypeOfDecision.values());
        builder.registerFixedList(
                "frl_tseApplicationsOpenOrClosed", List154FrlTseApplicationsOpenOrClosed.values());
        builder.registerFixedList(
                "frl_yes_no_not_applicable", List155FrlYesNoNotApplicable.values());
        builder.registerFixedList("imageRendering", List156ImageRendering.values());
        builder.registerFixedList("imageRenderingLocation", List157ImageRenderingLocation.values());
        builder.registerFixedList(
                "ms_FinalHearingIsEJSitAloneReasonNo",
                List158MsFinalHearingIsEJSitAloneReasonNo.values());
        builder.registerFixedList(
                "ms_FinalHearingIsEJSitAloneReasonYes",
                List159MsFinalHearingIsEJSitAloneReasonYes.values());
        builder.registerFixedList("msl_Defects", List160MslDefectsV2.values());
        builder.registerFixedList("msl_HearingAttendence", List161MslHearingAttendence.values());
        builder.registerFixedList("msl_HearingFormat", List162MslHearingFormat.values());
        builder.registerFixedList("msl_HearingPreferences", List163MslHearingPreferences.values());
        builder.registerFixedList("msl_IcF2FOrders", List164MslIcF2FOrders.values());
        builder.registerFixedList("msl_IcVideoOrders", List165MslIcVideoOrders.values());
        builder.registerFixedList("msl_NoticePeriod", List166MslNoticePeriod.values());
        builder.registerFixedList("msl_NoticePeriodLength", List167MslNoticePeriodLength.values());
        builder.registerFixedList("msl_PayFrequency", List168MslPayFrequency.values());
        builder.registerFixedList(
                "msl_PreAcceptanceResponse", List169MslPreAcceptanceResponseV2.values());
        builder.registerFixedList("msl_REJOrVP", List170MslREJOrVP.values());
        builder.registerFixedList("msl_Response", List171MslResponse.values());
        builder.registerFixedList("msl_StillWorking", List172MslStillWorking.values());
        builder.registerFixedList(
                "msl_WorkPayNoticePeriod", List173MslWorkPayNoticePeriod.values());
        builder.registerFixedList("msl_Yes", List174MslYes.values());
        builder.registerFixedList("msl_YesNo", List175MslYesNo.values());
        builder.registerFixedList("msl_claimOutcomes", List176MslClaimOutcomes.values());
        builder.registerFixedList(
                "msl_closeApplicationYes", List177MslCloseApplicationYes.values());
        builder.registerFixedList(
                "msl_confirmCloseReferral", List178MslConfirmCloseReferral.values());
        builder.registerFixedList("msl_confirmSubmitEt3", List179MslConfirmSubmitEt3.values());
        builder.registerFixedList(
                "msl_discriminationClaims", List180MslDiscriminationClaims.values());
        builder.registerFixedList(
                "msl_et1DiscriminationClaims", List181MslEt1DiscriminationClaims.values());
        builder.registerFixedList("msl_et1TypesOfClaim", List182MslEt1TypesOfClaim.values());
        builder.registerFixedList("msl_et3_hearing_type", List183MslEt3HearingType.values());
        builder.registerFixedList(
                "msl_etICFurtherInformation", List184MslEtICFurtherInformation.values());
        builder.registerFixedList(
                "msl_etICHearingAlreadyListed", List185MslEtICHearingAlreadyListedV2.values());
        builder.registerFixedList(
                "msl_etICHearingNotListed", List186MslEtICHearingNotListedV2.values());
        builder.registerFixedList(
                "msl_etICHearingNotListedUpdated", List187MslEtICHearingNotListedUpdated.values());
        builder.registerFixedList(
                "msl_etICPurposeOfPrelimHearing", List189MslEtICPurposeOfPrelimHearing.values());
        builder.registerFixedList(
                "msl_etICRule27Direction", List191MslEtICRule27Direction.values());
        builder.registerFixedList(
                "msl_etICTypeOfCvpHearing", List192MslEtICTypeOfCvpHearing.values());
        builder.registerFixedList("msl_etICTypeOfHearing", List193MslEtICTypeOfHearingV2.values());
        builder.registerFixedList(
                "msl_etICTypeOfHearingUpdated", List194MslEtICTypeOfHearingUpdated.values());
        builder.registerFixedList("msl_etICUDLCVPIssue", List196MslEtICUDLCVPIssue.values());
        builder.registerFixedList(
                "msl_etICUDLFinalF2FIssue", List197MslEtICUDLFinalF2FIssueV2.values());
        builder.registerFixedList("msl_etICUDLGiveReasons", List198MslEtICUDLGiveReasons.values());
        builder.registerFixedList(
                "msl_finalHearingWithJudgeOrMembersReasonsJsa",
                List199MslFinalHearingWithJudgeOrMembersReasonsJsa.values());
        builder.registerFixedList(
                "msl_finalHearingWithJudgeOrMembersReasonsMembers",
                List200MslFinalHearingWithJudgeOrMembersReasonsMembers.values());
        builder.registerFixedList(
                "msl_hearingWithJudgeOrMembersReasons",
                List202MslHearingWithJudgeOrMembersReasonsV2.values());
        builder.registerFixedList("msl_judgeOrLO", List203MslJudgeOrLO.values());
        builder.registerFixedList("msl_otherFactors", List204MslOtherFactors.values());
        builder.registerFixedList("msl_payClaims", List205MslPayClaims.values());
        builder.registerFixedList("msl_removeDocument", List206MslRemoveDocument.values());
        builder.registerFixedList("msl_submitEt1", List208MslSubmitEt1.values());
        builder.registerFixedList("msl_typeOfClaim", List209MslTypeOfClaim.values());
        builder.registerFixedList("pageNumberFormat", List210PageNumberFormat.values());
        builder.registerFixedList("paginationStyle", List211PaginationStyle.values());
        builder.registerFixedList(
                "sendNotificationEccQuestion", List212SendNotificationEccQuestion.values());
        builder.registerFixedList("sendNotificationNotify", List213SendNotificationNotify.values());
        builder.registerFixedList(
                "sendNotificationResponseTribunal",
                List214SendNotificationResponseTribunal.values());
    }

    @Getter
    @RequiredArgsConstructor
    private enum List001FlRespondNotificationResponseRequired implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes - view document for details"),
        @CCD(displayOrder = 2)
        V002("No", "No");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List002VenueScotland implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Glasgow", "Glasgow"),
        @CCD(displayOrder = 2)
        V002("Aberdeen", "Aberdeen"),
        @CCD(displayOrder = 3)
        V003("Dundee", "Dundee"),
        @CCD(displayOrder = 4)
        V004("Edinburgh", "Edinburgh"),
        @CCD(displayOrder = 5)
        V005("Unassigned", "Unassigned");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List003CaseSourceList implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("ET1 Online", "ET1 Online"),
        @CCD(displayOrder = 2)
        V002("Manually Created", "Manually Created"),
        @CCD(displayOrder = 3)
        V003("Migration", "Migration"),
        @CCD(displayOrder = 4)
        V004("ECC", "ECC"),
        @CCD(displayOrder = 5)
        V005("MyHMCTS", "MyHMCTS");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List004ClaimantHearingPanelPreference implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("No preference", "No preference"),
        @CCD(displayOrder = 2)
        V002("Judge", "Judge"),
        @CCD(displayOrder = 3)
        V003("Panel", "Panel");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List005ConfigurationFiles implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("et-dcf-2.yaml", "ET Digital Case File"),
        @CCD(displayOrder = 2)
        V002("et-dcf-ordered.yaml", "ET Digital Case File (Ordered)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List006CreateUploadOrRemove implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Create", "Create"),
        @CCD(displayOrder = 2)
        V002("Upload", "Upload"),
        @CCD(displayOrder = 3)
        V003("Remove", "Remove");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List007FlAddressLabelNumber implements HasLabel, HasCode {
        @CCD(displayOrder = 1, numericListElementCode = "1", numericListElement = 1)
        V001("1", "1"),
        @CCD(displayOrder = 2, numericListElementCode = "2", numericListElement = 2)
        V002("2", "2"),
        @CCD(displayOrder = 3, numericListElementCode = "3", numericListElement = 3)
        V003("3", "3"),
        @CCD(displayOrder = 4, numericListElementCode = "4", numericListElement = 4)
        V004("4", "4"),
        @CCD(displayOrder = 5, numericListElementCode = "5", numericListElement = 5)
        V005("5", "5"),
        @CCD(displayOrder = 6, numericListElementCode = "6", numericListElement = 6)
        V006("6", "6"),
        @CCD(displayOrder = 7, numericListElementCode = "7", numericListElement = 7)
        V007("7", "7"),
        @CCD(displayOrder = 8, numericListElementCode = "8", numericListElement = 8)
        V008("8", "8"),
        @CCD(displayOrder = 9, numericListElementCode = "9", numericListElement = 9)
        V009("9", "9"),
        @CCD(displayOrder = 10, numericListElementCode = "10", numericListElement = 10)
        V010("10", "10"),
        @CCD(displayOrder = 11, numericListElementCode = "11", numericListElement = 11)
        V011("11", "11"),
        @CCD(displayOrder = 12, numericListElementCode = "12", numericListElement = 12)
        V012("12", "12"),
        @CCD(displayOrder = 13, numericListElementCode = "13", numericListElement = 13)
        V013("13", "13"),
        @CCD(displayOrder = 14, numericListElementCode = "14", numericListElement = 14)
        V014("14", "14");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List008FlAdjustment implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Mitigation (Old regs)", "Mitigation (Old regs)"),
        @CCD(displayOrder = 2)
        V002("Enhancement (Old regs)", "Enhancement (Old regs)"),
        @CCD(displayOrder = 3)
        V003("Mit & Enhance (Old regs)", "Mit & Enhance (Old regs)"),
        @CCD(displayOrder = 4)
        V004("Increase (New regs)", "Increase (New regs)"),
        @CCD(displayOrder = 5)
        V005("Decrease (New regs)", "Decrease (New regs)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List009FlAttendee implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Claimant and representative", "Claimant and Representative"),
        @CCD(displayOrder = 2)
        V002("Claimant only", "Claimant only"),
        @CCD(displayOrder = 3)
        V003("Claimant representative only", "Claimant Representative only"),
        @CCD(displayOrder = 4)
        V004("No attendance", "No attendance");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List010FlBFActionsCWV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Application of letter to ACAS/RPO", "Application of letter to ACAS/RPO"),
        @CCD(displayOrder = 2)
        V002("Case listed", "Case listed"),
        @CCD(displayOrder = 3)
        V003("Case papers prepared", "Case papers prepared"),
        @CCD(displayOrder = 4)
        V004("Case transferred", "Case transferred"),
        @CCD(displayOrder = 5)
        V005("Draft to members", "Draft to members"),
        @CCD(displayOrder = 6)
        V006("Enquiry letter issued", "Enquiry letter issued"),
        @CCD(displayOrder = 7)
        V007("Enquiry letter received", "Enquiry letter received"),
        @CCD(displayOrder = 8)
        V008("Exhibits returned", "Exhibits returned"),
        @CCD(displayOrder = 9)
        V009("Interlocutory order requested", "Interlocutory order requested"),
        @CCD(displayOrder = 10)
        V010("IT3 received", "IT3 received"),
        @CCD(displayOrder = 11)
        V011("Other action", "Other action"),
        @CCD(displayOrder = 12)
        V012("Postponement requested", "Postponement requested"),
        @CCD(displayOrder = 13)
        V013(
                "Refer to chairman for listing instructions",
                "Refer to chairman for listing instructions"),
        @CCD(displayOrder = 14)
        V014("Reply to enquiry letter sent", "Reply to enquiry letter sent"),
        @CCD(displayOrder = 15)
        V015("Striking out warning issued", "Striking out warning issued"),
        @CCD(displayOrder = 16)
        V016("Witnesses recorded", "Witnesses recorded");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List010FlBFActionsCWV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Case listed", "Case listed"),
        @CCD(displayOrder = 2)
        V002("Postponement requested", "Postponement requested"),
        @CCD(displayOrder = 3)
        V003("Enquiry letter received", "Enquiry letter received"),
        @CCD(displayOrder = 4)
        V004("Reply to enquiry letter sent", "Reply to enquiry letter sent"),
        @CCD(displayOrder = 5)
        V005("Case papers prepared", "Case papers prepared"),
        @CCD(displayOrder = 6)
        V006("Interlocutory order requested", "Interlocutory order requested"),
        @CCD(displayOrder = 7)
        V007("Exhibits returned", "Exhibits returned"),
        @CCD(displayOrder = 8)
        V008("Draft to members", "Draft to members"),
        @CCD(displayOrder = 9)
        V009("Application of letter to ACAS/RPO", "Application of letter to ACAS/RPO"),
        @CCD(displayOrder = 10)
        V010("Witnesses recorded", "Witnesses recorded"),
        @CCD(displayOrder = 11)
        V011("Case transferred", "Case transferred"),
        @CCD(displayOrder = 12)
        V012("Other action", "Other action"),
        @CCD(displayOrder = 13)
        V013("Enquiry letter issued", "Enquiry letter issued"),
        @CCD(displayOrder = 14)
        V014("Striking out warning issued", "Striking out warning issued"),
        @CCD(displayOrder = 15)
        V015("IT3 received", "IT3 received"),
        @CCD(displayOrder = 16)
        V016(
                "Refer to chairman for listing instructions",
                "Refer to chairman for listing instructions");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List011FlCaseManagement implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Tribunal Order", "Tribunal Order"),
        @CCD(displayOrder = 2)
        V002("Deposit Order", "Deposit Order"),
        @CCD(displayOrder = 3)
        V003("Unless Order", "Unless Order"),
        @CCD(displayOrder = 4)
        V004("Tribunal Notice", "Tribunal Notice"),
        @CCD(displayOrder = 5)
        V005("App to vary an order – C", "App to vary an order – C"),
        @CCD(displayOrder = 6)
        V006("App to vary an order – R", "App to vary an order – R"),
        @CCD(displayOrder = 7)
        V007("App to revoke an order - C", "App to revoke an order - C"),
        @CCD(displayOrder = 8)
        V008("App to revoke an order – R", "App to revoke an order – R"),
        @CCD(displayOrder = 9)
        V009("App to vary or revoke an order - C", "App to vary or revoke an order - C"),
        @CCD(displayOrder = 10)
        V010("App to vary or revoke an order – R", "App to vary or revoke an order – R"),
        @CCD(displayOrder = 11)
        V011(
                "App to extend time to comply to an order/directions – C",
                "App to extend time to comply to an order/directions – C"),
        @CCD(displayOrder = 12)
        V012(
                "App to extend time to comply to an order/directions – R",
                "App to extend time to comply to an order/directions – R"),
        @CCD(displayOrder = 13)
        V013("App to Order the R to do something", "App to Order the R to do something"),
        @CCD(displayOrder = 14)
        V014("App to Order the C to do something", "App to Order the C to do something"),
        @CCD(displayOrder = 15)
        V015("App to amend claim", "App to amend claim"),
        @CCD(displayOrder = 16)
        V016("App to amend response", "App to amend response"),
        @CCD(displayOrder = 17)
        V017("App for a Witness Order - C", "App for a Witness Order - C"),
        @CCD(displayOrder = 18)
        V018("App for a Witness Order - R", "App for a Witness Order - R"),
        @CCD(displayOrder = 19)
        V019("Disability Impact statement", "Disability Impact statement"),
        @CCD(displayOrder = 20)
        V020("R has not complied with an order - C", "R has not complied with an order - C"),
        @CCD(displayOrder = 21)
        V021("C has not complied with an order - R", "C has not complied with an order - R"),
        @CCD(displayOrder = 22)
        V022(
                "App to Strike out all or part of the claim",
                "App to Strike out all or part of the claim"),
        @CCD(displayOrder = 23)
        V023(
                "App to Strike out all or part of the response",
                "App to Strike out all or part of the response"),
        @CCD(displayOrder = 24)
        V024("Referral/Judicial Direction", "Referral/Judicial Direction"),
        @CCD(displayOrder = 25)
        V025("Change of party’s details", "Change of party’s details"),
        @CCD(displayOrder = 26)
        V026(
                "Contact the tribunal about something else - C",
                "Contact the tribunal about something else - C"),
        @CCD(displayOrder = 27)
        V027(
                "Contact the tribunal about something else - R",
                "Contact the tribunal about something else - R");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List012FlCaseType implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Single", "Single"),
        @CCD(displayOrder = 2)
        V002("Multiple", "Multiple");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List013FlClaimantOrRespondent implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Claimant", "Claimant"),
        @CCD(displayOrder = 2)
        V002("Respondent", "Respondent");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List014FlClaimantRespondentBothParties implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Both parties", "Both parties"),
        @CCD(displayOrder = 2)
        V002("Claimant", "Claimant"),
        @CCD(displayOrder = 3)
        V003("Respondent", "Respondent");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List015FlConciliationV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("No Conciliation", "No Track"),
        @CCD(displayOrder = 2)
        V002("Fast Track", "Short Track"),
        @CCD(displayOrder = 3)
        V003("Standard Track", "Standard Track"),
        @CCD(displayOrder = 4)
        V004("Open Track", "Open Track");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List015FlConciliationV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("No Conciliation", "No track"),
        @CCD(displayOrder = 2)
        V002("Fast Track", "Short track"),
        @CCD(displayOrder = 3)
        V003("Standard Track", "Standard track"),
        @CCD(displayOrder = 4)
        V004("Open Track", "Open track");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List016FlContactPreference implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Email", "Email"),
        @CCD(displayOrder = 2)
        V002("Post", "Post");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List017FlDepositCovers implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("All", "All"),
        @CCD(displayOrder = 2)
        V002("Part", "Part");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List018FlDepositRequestedBy implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Tribunal", "Tribunal"),
        @CCD(displayOrder = 2)
        V002("Claimant", "Claimant"),
        @CCD(displayOrder = 3)
        V003("Respondent", "Respondent");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List019FlDocumentCategories implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Starting a Claim", "Starting a Claim"),
        @CCD(displayOrder = 2)
        V002("Response to a Claim", "Response to a Claim"),
        @CCD(displayOrder = 3)
        V003("Initial Consideration", "Initial Consideration"),
        @CCD(displayOrder = 4)
        V004("Case Management", "Case Management"),
        @CCD(displayOrder = 5)
        V005("Employer Contract Claim", "Employer Contract Claim"),
        @CCD(displayOrder = 6)
        V006("Withdrawal/Settled", "Withdrawal/Settled"),
        @CCD(displayOrder = 7)
        V007("Hearings", "Hearings"),
        @CCD(displayOrder = 8)
        V008("Judgment and Reasons", "Judgment and Reasons"),
        @CCD(displayOrder = 9)
        V009("Reconsideration", "Reconsideration"),
        @CCD(displayOrder = 10)
        V010("Misc", "Misc"),
        @CCD(displayOrder = 11)
        V011("Legacy Document Names", "Legacy Document Names");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List020FlDocumentTypeV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("ET1", "ET1"),
        @CCD(displayOrder = 2)
        V002("ET1 Attachment", "ET1 Attachment"),
        @CCD(displayOrder = 3)
        V003("ACAS Certificate", "ACAS Certificate"),
        @CCD(displayOrder = 4)
        V004("Acknowledgement of claim", "Acknowledgement of claim"),
        @CCD(displayOrder = 5)
        V005("Notice of a claim", "Notice of a claim"),
        @CCD(displayOrder = 6)
        V006("ET3", "ET3"),
        @CCD(displayOrder = 7)
        V007("ET3 Attachment", "ET3 Attachment"),
        @CCD(displayOrder = 8)
        V008("Claimant correspondence", "Claimant correspondence"),
        @CCD(displayOrder = 9)
        V009("Respondent correspondence", "Respondent correspondence"),
        @CCD(displayOrder = 10)
        V010("Notice of Hearing", "Notice of Hearing"),
        @CCD(displayOrder = 11)
        V011("Tribunal case file", "Tribunal case file"),
        @CCD(displayOrder = 12)
        V012("Tribunal correspondence", "Tribunal correspondence"),
        @CCD(displayOrder = 13)
        V013("Tribunal Order/Deposit Order", "Tribunal Order/Deposit Order"),
        @CCD(displayOrder = 14)
        V014("Tribunal Judgment/Reasons", "Tribunal Judgment/Reasons"),
        @CCD(displayOrder = 15)
        V015("Referral/Judicial direction", "Referral/Judicial direction"),
        @CCD(displayOrder = 16)
        V016("Rejection of claim", "Rejection of claim"),
        @CCD(displayOrder = 17)
        V017("Other ", "Other "),
        @CCD(displayOrder = 18)
        V018("Tse admin correspondence", "Tse admin correspondence");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List020FlDocumentTypeV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("ET1", "ET1"),
        @CCD(displayOrder = 2)
        V002("ET1 Attachment", "ET1 Attachment"),
        @CCD(displayOrder = 3)
        V003("ACAS Certificate", "ACAS Certificate"),
        @CCD(displayOrder = 4)
        V004("Notice of a claim", "Notice of a claim"),
        @CCD(displayOrder = 4)
        V005("Acknowledgement of claim", "Acknowledgement of claim"),
        @CCD(displayOrder = 5)
        V006("ET3", "ET3"),
        @CCD(displayOrder = 6)
        V007("ET3 Attachment", "ET3 Attachment"),
        @CCD(displayOrder = 7)
        V008("Claimant correspondence", "Claimant correspondence"),
        @CCD(displayOrder = 8)
        V009("Respondent correspondence", "Respondent correspondence"),
        @CCD(displayOrder = 9)
        V010("Notice of Hearing", "Notice of Hearing"),
        @CCD(displayOrder = 10)
        V011("Tribunal case file", "Tribunal case file"),
        @CCD(displayOrder = 11)
        V012("Tribunal correspondence", "Tribunal correspondence"),
        @CCD(displayOrder = 12)
        V013("Tribunal Order/Deposit Order", "Tribunal Order/Deposit Order"),
        @CCD(displayOrder = 13)
        V014("Tribunal Judgment/Reasons", "Tribunal Judgment/Reasons"),
        @CCD(displayOrder = 14)
        V015("Referral/Judicial direction", "Referral/Judicial direction"),
        @CCD(displayOrder = 15)
        V016("Rejection of claim", "Rejection of claim"),
        @CCD(displayOrder = 16)
        V017("Other ", "Other "),
        @CCD(displayOrder = 17)
        V018("Tse admin correspondence", "Tse admin correspondence");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List021FlEmployerContractClaim implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Notice of Employer Contract Claim", "Notice of Employer Contract Claim"),
        @CCD(displayOrder = 2)
        V002("Acceptance of ECC response", "Acceptance of ECC response"),
        @CCD(displayOrder = 3)
        V003("Rejection of ECC response", "Rejection of ECC response");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List022FlEt3NotificationDocTypeV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("2.11", "2.11 Response accepted"),
        @CCD(displayOrder = 2)
        V002("2.12", "2.12 Response rejection - prescribed form - Rule 18(1)(a)"),
        @CCD(displayOrder = 3)
        V003("2.13", "2.13 Response rejection - minimum information - Rule 18(1)(b)"),
        @CCD(displayOrder = 4)
        V004("2.14", "2.14 Response rejection - out of time - Rule 19"),
        @CCD(displayOrder = 5)
        V005("2.15", "2.15 Response rejection - your questions answered");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List022FlEt3NotificationDocTypeV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Letter 10", "Letter 10 Rejection of response by staff"),
        @CCD(displayOrder = 2)
        V002("Letter 11", "Letter 11 Rejection of response by EJ"),
        @CCD(displayOrder = 3)
        V003("Letter 13", "Letter 13 To respondent - response accepted"),
        @CCD(displayOrder = 4)
        V004("Letter 14", "Letter 14 Notice to claimant of accepted response");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List023FlFinalHearingIsEJSitAloneReason implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "Members experience is likely to add significant value to the process of"
                        + " adjudication",
                "Members experience is likely to add significant value to the adjudication"
                        + " process"),
        @CCD(displayOrder = 2)
        V002(
                "Members experience is not likely to add significant value to the process of"
                        + " adjudication",
                "Members experience is not likely to add significant value to the adjudication"
                        + " process"),
        @CCD(displayOrder = 3)
        V003("No views expressed by parties", "No views expressed by parties"),
        @CCD(displayOrder = 4)
        V004("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List024FlGender implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Male", "Male"),
        @CCD(displayOrder = 2)
        V002("Female", "Female"),
        @CCD(displayOrder = 3)
        V003("Not Known", "Not Known"),
        @CCD(displayOrder = 4)
        V004("Non-binary", "Non-binary");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List025FlHearingV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Costs Hearing", "Costs Hearing"),
        @CCD(displayOrder = 2)
        V002("Hearing", "Final Hearing"),
        @CCD(displayOrder = 3)
        V003("Preliminary Hearing", "Preliminary Hearing"),
        @CCD(displayOrder = 4)
        V004("Preliminary Hearing(CM)", "Preliminary Hearing (CM)"),
        @CCD(displayOrder = 5)
        V005("Reconsideration", "Reconsideration Hearing"),
        @CCD(displayOrder = 6)
        V006("Remedy", "Remedy Hearing");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List025FlHearingV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Expenses/Wasted Costs Hearing", "Expenses/Wasted Costs Hearing"),
        @CCD(displayOrder = 2)
        V002("Hearing", "Final Hearing"),
        @CCD(displayOrder = 5)
        V003("Preliminary Hearing", "Preliminary Hearing"),
        @CCD(displayOrder = 7)
        V004("Preliminary Hearing(CM)", "Preliminary Hearing (CM)"),
        @CCD(displayOrder = 8)
        V005("Reconsideration", "Reconsideration Hearing"),
        @CCD(displayOrder = 9)
        V006("Remedy", "Remedy Hearing");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List026FlHearingDateType implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Single", "Single"),
        @CCD(displayOrder = 2)
        V002("Range", "Range");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List027FlHearingDocETCL implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Public", "Public"),
        @CCD(displayOrder = 2)
        V002("Staff", "Staff"),
        @CCD(displayOrder = 3)
        V003("Press List", "Press List");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List028FlHearingDocType implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("IT56 - List of Exhibits", "IT56 - List of Exhibits"),
        @CCD(displayOrder = 2)
        V002("ETCL - Cause List", "ETCL - Cause List"),
        @CCD(displayOrder = 3)
        V003("ETRP - ET recording of proceeding", "ETRP - ET recording of proceeding");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List029FlHearingLength implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Days", "Days"),
        @CCD(displayOrder = 2)
        V002("Hours", "Hours"),
        @CCD(displayOrder = 3)
        V003("Minutes", "Minutes");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List030FlHearingPanelPreference implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("No preference", "No preference"),
        @CCD(displayOrder = 2)
        V002("Judge", "Judge"),
        @CCD(displayOrder = 3)
        V003("Panel", "Panel");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List031FlHearingReadingDelib implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Reading Day", "Reading Day"),
        @CCD(displayOrder = 2)
        V002("Deliberation Day", "Deliberation Day"),
        @CCD(displayOrder = 3)
        V003("Members meeting", "Members meeting"),
        @CCD(displayOrder = 4)
        V004("In Chambers", "In Chambers"),
        @CCD(displayOrder = 5)
        V005("Neither", "None of the above");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List032FlHearingStatusV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Postponed", "Postponed"),
        @CCD(displayOrder = 2)
        V002("Heard", "Heard"),
        @CCD(displayOrder = 3)
        V003("Listed", "Listed"),
        @CCD(displayOrder = 4)
        V004("Settled", "Settled"),
        @CCD(displayOrder = 5)
        V005("Vacated", "Vacated"),
        @CCD(displayOrder = 6)
        V006("Withdrawn", "Withdrawn");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List032FlHearingStatusV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Listed", "Listed"),
        @CCD(displayOrder = 2)
        V002("Heard", "Heard"),
        @CCD(displayOrder = 3)
        V003("Postponed", "Postponed"),
        @CCD(displayOrder = 4)
        V004("Settled", "Settled"),
        @CCD(displayOrder = 5)
        V005("Vacated", "Vacated"),
        @CCD(displayOrder = 6)
        V006("Withdrawn", "Withdrawn");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List033FlHearings implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("App to restrict publicity - C", "App to restrict publicity - C"),
        @CCD(displayOrder = 2)
        V002("App to restrict publicity - R", "App to restrict publicity - R"),
        @CCD(displayOrder = 3)
        V003("Anonymity Order", "Anonymity Order"),
        @CCD(displayOrder = 4)
        V004("Notice of Hearing", "Notice of Hearing"),
        @CCD(displayOrder = 5)
        V005("App to postpone – C", "App to postpone – C"),
        @CCD(displayOrder = 6)
        V006("App to postpone – R", "App to postpone – R"),
        @CCD(displayOrder = 7)
        V007("Hearing Bundle", "Hearing Bundle"),
        @CCD(displayOrder = 8)
        V008("Schedule of loss", "Schedule of loss"),
        @CCD(displayOrder = 9)
        V009("Counter Schedule of Loss", "Counter Schedule of Loss");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List034FlInitialConsideration implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Initial Consideration", "Initial Consideration"),
        @CCD(displayOrder = 2)
        V002("Rule 27 Notice", "Rule 28 Notice"),
        @CCD(displayOrder = 3)
        V003("Rule 28 Notice", "Rule 29 Notice");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List035FlJudgeDecision implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Judgment Confirmed", "Judgment confirmed"),
        @CCD(displayOrder = 2)
        V002("Judgment Revoked", "Judgment revoked"),
        @CCD(displayOrder = 3)
        V003("Judgment Varied", "Judgment varied"),
        @CCD(displayOrder = 4)
        V004("Application Refused", "Application refused");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List036FlJudgeDirection implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "Application to be considered at Reconsideration hearing",
                "Application to be considered at Reconsideration hearing"),
        @CCD(displayOrder = 2)
        V002(
                "Application to be considered on the written submissions",
                "Application to be considered on the written submissions"),
        @CCD(displayOrder = 3)
        V003("Application Refused", "Application refused");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List037FlJudgementType implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Case Management", "Case Management"),
        @CCD(displayOrder = 2)
        V002("Judgment", "Judgment"),
        @CCD(displayOrder = 3)
        V003("Jurisdiction", "Jurisdiction"),
        @CCD(displayOrder = 4)
        V004("Reserved", "Reserved"),
        @CCD(displayOrder = 5)
        V005("Reconsideration ", "Reconsideration "),
        @CCD(displayOrder = 6)
        V006("Rule21", "Rule 22");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List038FlJudgmentAndReasons implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Judgment", "Judgment"),
        @CCD(displayOrder = 2)
        V002("Judgment with Reasons", "Judgment with Reasons"),
        @CCD(displayOrder = 3)
        V003("Reasons", "Reasons"),
        @CCD(displayOrder = 4)
        V004("Extract of Judgment", "Extract of Judgment");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List039FlJudgmentOutcome implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Not allocated", "Not allocated"),
        @CCD(displayOrder = 2)
        V002("Acas conciliated settlement", "ACAS conciliated settlement"),
        @CCD(displayOrder = 3)
        V003("Withdrawn or private settlement", "Withdrawn or private settlement"),
        @CCD(displayOrder = 4)
        V004("Successful at hearing", "Successful at hearing"),
        @CCD(displayOrder = 5)
        V005("Unsuccessful at hearing", "Unsuccessful at hearing"),
        @CCD(displayOrder = 6)
        V006("Dismissed at hearing - out of scope", "Dismissed at hearing - out of scope"),
        @CCD(displayOrder = 7)
        V007("Disposed of / other", "Disposed of / other"),
        @CCD(displayOrder = 8)
        V008("Input in error", "Input in error"),
        @CCD(displayOrder = 9)
        V009("Default judgment, claimant successful", "Default judgment, claimant successful"),
        @CCD(displayOrder = 10)
        V010("Default judgment, claimant unsuccessful", "Default judgment, claimant unsuccessful"),
        @CCD(displayOrder = 11)
        V011("Dismissed under Rule 27", "Dismissed under Rule 28"),
        @CCD(displayOrder = 12)
        V012("Dismissed on withdrawal", "Dismissed on withdrawal"),
        @CCD(displayOrder = 13)
        V013("Case discontinued", "Case discontinued"),
        @CCD(displayOrder = 14)
        V014("Struck Out", "Struck Out");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List040FlLiabilityV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Liability", "Liability"),
        @CCD(displayOrder = 2)
        V002("Remedy", "Remedy"),
        @CCD(displayOrder = 3)
        V003("Liability and Remedy", "Liability and Remedy");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List040FlLiabilityV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Liability", "Liability"),
        @CCD(omitDisplayOrder = true)
        V002("Remedy", "Remedy"),
        @CCD(displayOrder = 2)
        V003("Liability and Remedy", "Liability and Remedy");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List041FlMisc implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Certificate of Correction", "Certificate of Correction"),
        @CCD(displayOrder = 2)
        V002("Tribunal case file", "Tribunal case file"),
        @CCD(displayOrder = 3)
        V003("Other", "Other"),
        @CCD(displayOrder = 4)
        V004("Needs updating", "NEEDS UPDATING");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List042FlPart0 implements HasLabel, HasCode {
        @CCD(displayOrder = 1, numericListElementCode = "0.1")
        V001("0.1", "0.1 - Customise selected addresses"),
        @CCD(displayOrder = 2, numericListElementCode = "0.2")
        V002("0.2", "0.2 - All available addresses"),
        @CCD(displayOrder = 3, numericListElementCode = "0.3")
        V003("0.3", "0.3 - Claimant address"),
        @CCD(displayOrder = 4, numericListElementCode = "0.4")
        V004("0.4", "0.4 - Claimant Rep address"),
        @CCD(displayOrder = 5, numericListElementCode = "0.5")
        V005("0.5", "0.5 - Claimant and Claimant Rep addresses"),
        @CCD(displayOrder = 6, numericListElementCode = "0.6")
        V006("0.6", "0.6 - Respondents addresses"),
        @CCD(displayOrder = 7, numericListElementCode = "0.7")
        V007("0.7", "0.7 - Respondents Reps addresses"),
        @CCD(displayOrder = 8, numericListElementCode = "0.8")
        V008("0.8", "0.8 - Respondents and Respondents Reps addresses"),
        @CCD(displayOrder = 9, numericListElementCode = "0.9")
        V009("0.9", "0.9 - Claimant and Respondents addresses"),
        @CCD(displayOrder = 10)
        V010("0.10", "0.10 - Claimant Rep and Respondents Reps addresses"),
        @CCD(displayOrder = 11, numericListElementCode = "0.11")
        V011("0.11", "0.11 - Claimant and Respondents Reps addresses"),
        @CCD(displayOrder = 12, numericListElementCode = "0.12")
        V012("0.12", "0.12 - Claimint Rep and Respondents addresses");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List043FlPart1V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("1.1", "1.1 - ET5 Acknowledgment of claim"),
        @CCD(displayOrder = 2)
        V002("1.1A", "1.1A - Claim - documents returned"),
        @CCD(displayOrder = 3)
        V003("1.1B", "1.1B - Identity of respondent"),
        @CCD(displayOrder = 4)
        V004("1.2", "1.2 - Interim relief – acknowledgement"),
        @CCD(displayOrder = 5)
        V005("1.3", "1.3 - PID claim - notification to Regulator - Rule 14"),
        @CCD(displayOrder = 6)
        V006("1.3A", "1.3A - Claim - request not to serve on respondent"),
        @CCD(displayOrder = 7)
        V007("1.3B", "1.3B - Claim Re-direct"),
        @CCD(displayOrder = 8)
        V008("1.4", "1.4 - Claim rejection - not E&W"),
        @CCD(displayOrder = 9)
        V009("1.5", "1.5 - Claim rejection - not on prescribed form - Rule 12(1)(a)"),
        @CCD(displayOrder = 10)
        V010("1.6", "1.6 - Claim rejection - minimum information - Rule 12(1)(b)"),
        @CCD(displayOrder = 11)
        V011(
                "1.6A",
                "1.6A - Claim rejection - lack of early conciliation information - Rule 12(1)(c) "),
        @CCD(displayOrder = 12)
        V012(
                "1.6B",
                "1.6B - Claim rejection in part – lack of early conciliation information Rule"
                        + " 12(1)(c)"),
        @CCD(displayOrder = 13)
        V013("1.7", "1.7 - Claim rejection - no jurisdiction – whole claim - Rule 13(1)(a)"),
        @CCD(displayOrder = 14)
        V014("1.8", "1.8 - Claim rejection - no jurisdiction - part claim - Rule 13(1)(a)"),
        @CCD(displayOrder = 15)
        V015("1.9", "1.9 - Claim rejection - abuse of process etc- whole claim – Rule 13(1)(b)"),
        @CCD(displayOrder = 16)
        V016("1.9A", "1.9A - Claim rejection - no early conciliation - Rule 13(1)(c) "),
        @CCD(displayOrder = 17)
        V017(
                "1.9B",
                "1.9B - Claim rejection - early conciliation exemption does not apply - Rule"
                        + " 13(1)(d)"),
        @CCD(displayOrder = 18)
        V018("1.9C", "1.9C - Claim rejection - Rule 13(1)(g) - name of respondent differs"),
        @CCD(displayOrder = 19)
        V019("1.9D", "1.9D - Claim rejection - Rule 13(1)(f) - prospective claimant differs"),
        @CCD(displayOrder = 20)
        V020("1.9E", "1.9E - Claim rejection - rule 13(1)(e) - early conciliation number differs"),
        @CCD(displayOrder = 21)
        V021("1.10", "1.10 - Claim rejection - abuse of process etc - part claim - Rule 13(1)(b) "),
        @CCD(displayOrder = 22)
        V022("1.11", "1.11 - Claim rejection - your questions answered"),
        @CCD(displayOrder = 23)
        V023(
                "1.11A",
                "1.11A - \tClaim rejection - Your Questions Answered (Requirement to Contact ACAS)"
                        + " "),
        @CCD(displayOrder = 24)
        V024("1.12", "1.12 - Claim rejection - reconsideration - apply again - Rule 14"),
        @CCD(displayOrder = 25)
        V025("1.13", "1.13 - Claim rejection - reconsideration - claim accepted - Rule 14"),
        @CCD(displayOrder = 26)
        V026("1.14", "1.14 - Claim rejection - reconsideration - hearing - Rule 14(3)"),
        @CCD(displayOrder = 27)
        V027("1.14A", "1.14A - Claim rejection - reconsideration - dismissed - Rule 14"),
        @CCD(displayOrder = 28)
        V028("1.14B", "1.14B - Claim rejection - reconsideration - successful in part - Rule 14"),
        @CCD(displayOrder = 29)
        V029("1.15", "1.15 - UDL - length of service - show cause - whole claim"),
        @CCD(displayOrder = 30)
        V030("1.15A", "1.15A - RPT - length of service - show cause - whole claim"),
        @CCD(displayOrder = 31)
        V031("1.16", "1.16 - UDL - length of service - show cause - part claim"),
        @CCD(displayOrder = 32)
        V032("1.16A", "1.16A - RPT - length of service - show cause - part claim"),
        @CCD(displayOrder = 33)
        V033("1.17", "1.17 - UDL - length of service - strike out judgment - whole claim"),
        @CCD(displayOrder = 34)
        V034("1.17A", "1.17A - RPT - length of service - strike out judgment - whole claim"),
        @CCD(displayOrder = 35)
        V035("1.18", "1.18 - UDL - length of service - strike out judgment - part claim"),
        @CCD(displayOrder = 36)
        V036("1.18A", "1.18A - RPT - length of service - strike out judgment - part claim"),
        @CCD(displayOrder = 37)
        V037("1.19", "1.19 - Claim – amendment granted");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List043FlPart1V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("1", "1 Rejection of claim by staff"),
        @CCD(displayOrder = 2)
        V002("2", "2 Rejection of claim by Employment Judge"),
        @CCD(displayOrder = 3)
        V003("2.A", "2A Rejection of claim by Employment Judge - Early Conciliation"),
        @CCD(displayOrder = 4)
        V004("3", "3  Acknowledgment of claim standard"),
        @CCD(displayOrder = 5)
        V005("3.5", "3.5 Letter to claimant when time bar issue identified"),
        @CCD(displayOrder = 6)
        V006(
                "4",
                "4 Application for reconsideration not considered – failure to comply with rule"
                        + " 14(2)"),
        @CCD(displayOrder = 7)
        V007("5", "5 Application for reconsideration granted"),
        @CCD(displayOrder = 8)
        V008("6", "6 Application for reconsideration – rejection confirmed"),
        @CCD(displayOrder = 8)
        V009("7", "7 Notice of claim standard"),
        @CCD(displayOrder = 9)
        V010("7 Reform", "7 Reform Notice of claim standard"),
        @CCD(displayOrder = 10)
        V011("7.5", "7.5 Letter to respondent when time bar issue identified");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List044FlPart10V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("10.1", "10.1 Judgment after oral summary reasons given Rule 59"),
        @CCD(displayOrder = 2)
        V002("10.2", "10.2 Judgment after oral full reasons given Rule 59"),
        @CCD(displayOrder = 3)
        V003("10.3", "10.3 Judgment after oral judgment with reasons reserved Rule 59, 60(3)"),
        @CCD(displayOrder = 4)
        V004("10.4", "10.4 Letter confirming judgment reserved Rule 60(2)"),
        @CCD(displayOrder = 5)
        V005("10.5", "10.5 Reserved judgment with reasons template Rule 60(2)"),
        @CCD(displayOrder = 6)
        V006("10.6", "10.6 Judgment cover letter"),
        @CCD(displayOrder = 7)
        V007("10.6A", "10.6A Judgment cover letter Enforcement Notice appeals"),
        @CCD(displayOrder = 8)
        V008("10.6B", "10.6B Judgment cover letter Rule 22 judgments"),
        @CCD(displayOrder = 9)
        V009(
                "10.7",
                "10.7 Judgment plus full reasons template (reasons requested at hearing) Rule 60"),
        @CCD(displayOrder = 10)
        V010(
                "10.7A",
                "10.7A Judgment plus summary reasons template (reasons requested at hearing) Rule"
                        + " 60"),
        @CCD(displayOrder = 11)
        V011("10.8", "10.8 Written full reasons template Rule 60"),
        @CCD(displayOrder = 12)
        V012("10.8A", "10.8A Written summary reasons template Rule 60"),
        @CCD(displayOrder = 13)
        V013("10.9", "10.9 Letter confirming extension of time to request written summary reasons"),
        @CCD(displayOrder = 14)
        V014("10.10", "10.10 Letter confirming extension of time to request written full reasons"),
        @CCD(displayOrder = 15)
        V015(
                "10.11",
                "10.11 Letter confirming refusal of extension of time to request written summary"
                        + " reasons"),
        @CCD(displayOrder = 16)
        V016(
                "10.12",
                "10.12 Letter confirming refusal of extension of time to request written full"
                        + " reasons"),
        @CCD(displayOrder = 17)
        V017("10.13", "10.13 Consent judgment Rule 62"),
        @CCD(displayOrder = 18)
        V018("10.14", "10.14 Consent order Rule 62"),
        @CCD(displayOrder = 19)
        V019("10.15", "10.15 Certificate of correction Rule 67"),
        @CCD(displayOrder = 20)
        V020("10.16", "10.16 Corrected judgment with full reasons Rule 67"),
        @CCD(displayOrder = 21)
        V021("10.16A", "10.16A Corrected judgment with summary reasons Rule 67"),
        @CCD(displayOrder = 22)
        V022("10.17", "10.17 Letter sending corrected judgment Rule 67");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List044FlPart10V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("92", "92 Cover letter for judgment"),
        @CCD(displayOrder = 2)
        V002("92.A", "92A Judgment template"),
        @CCD(displayOrder = 3)
        V003("92.B", "92B Recoupment annex monetary award"),
        @CCD(displayOrder = 4)
        V004("92.C", "92C Recoupment annex protective award"),
        @CCD(displayOrder = 5)
        V005("92.D", "92D Recoupment notice Reg 4 monetary award"),
        @CCD(displayOrder = 6)
        V006("92.E", "92E Recoupment notice Reg 5 protective award"),
        @CCD(displayOrder = 7)
        V007("93", "93 Cover letter for reasons"),
        @CCD(displayOrder = 8)
        V008("93.A", "93A Written reasons refused as late"),
        @CCD(displayOrder = 9)
        V009("93.B", "93B Written reasons allowed although late"),
        @CCD(displayOrder = 10)
        V010("94", "94 Cover letter for Note from PH"),
        @CCD(displayOrder = 11)
        V011("95", "95 Certificate of correction"),
        @CCD(displayOrder = 12)
        V012("96", "96 Cover letter for certificate of correction"),
        @CCD(displayOrder = 13)
        V013("97", "97 Interests on Awards claim lodged before 29/7/13"),
        @CCD(displayOrder = 14)
        V014("97.A", "97A Interests on all awards claim lodged after 29/7/13"),
        @CCD(displayOrder = 15)
        V015("98", "98 Interest on awards (discrimination) lodged before 29/7/13"),
        @CCD(displayOrder = 16)
        V016("224", "224 Financial Penalty - Annex to the Judgment");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List045FlPart11V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("11.1C", "11.1C - Reconsideration - non-compliance - claimant - Rule 69"),
        @CCD(displayOrder = 2)
        V002("11.1R", "11.1R - Reconsideration - non-compliance - respondent - Rule 69"),
        @CCD(displayOrder = 3)
        V003("11.2C", "11.2C - Reconsideration of judgment - EOT granted- claimant Rule 69"),
        @CCD(displayOrder = 4)
        V004("11.2R", "11.2R - Reconsideration of judgment - EOT granted respondent - Rule 69"),
        @CCD(displayOrder = 5)
        V005("11.3C", "11.3C - Reconsideration of decision - rejected - claimant - Rule 69"),
        @CCD(displayOrder = 6)
        V006("11.3R", "11.3R - Reconsideration of decision - rejected - respondent- Rule 69"),
        @CCD(displayOrder = 7)
        V007("11.4", "11.4 - Reconsideration on Tribunal's initiative - Rule 71"),
        @CCD(displayOrder = 8)
        V008("11.5C", "11.5C - Reconsideration of judgment - claimant - Rule 70"),
        @CCD(displayOrder = 9)
        V009("11.5R", "11.5R - Reconsideration of judgment - respondent - Rule 70"),
        @CCD(displayOrder = 10)
        V010("11.6C", "11.6C - Judgment – reconsideration refused - claimant - Rule 70"),
        @CCD(displayOrder = 11)
        V011("11.6R", "11.6R - Judgment - reconsideration refused - respondent - Rule 70"),
        @CCD(displayOrder = 12)
        V012("11.7", "11.7 - Reconsideration of judgment - hearing not required - Rule 70"),
        @CCD(displayOrder = 13)
        V013("11.8", "11.8 - Notice of reconsideration hearing - merits adjourned - Rule 70"),
        @CCD(displayOrder = 14)
        V014("11.9", "11.9 - Notice of reconsideration hearing - merits that day - Rule 70"),
        @CCD(displayOrder = 15)
        V015("11.10", "11.10 - Judgment on reconsideration of judgment - hearing - Rules 68 & 71"),
        @CCD(displayOrder = 16)
        V016("11.11", "11.11 - Judgment on reconsideration of Rule 21 judgment - hearing- Rule 68"),
        @CCD(displayOrder = 17)
        V017(
                "11.12",
                "11.12 - Judgment on reconsideration of judgment – no hearing - Rules 68 & 71"),
        @CCD(displayOrder = 18)
        V018(
                "11.13",
                "11.13 - Judgment on reconsideration of Rule 20 judgment - no hearing - Rule 68");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List045FlPart11V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("99", "99 Application refused for noncompliance"),
        @CCD(displayOrder = 2)
        V002("100", "100 Application refused no reasonable prospects"),
        @CCD(displayOrder = 3)
        V003(
                "101",
                "101 Letter to parties, application for reconsideration not refused at stage 1"),
        @CCD(displayOrder = 4)
        V004("102", "102 Reconsideration tribunal’s own initiative"),
        @CCD(displayOrder = 5)
        V005("103", "103 Notice that reconsideration will take place without a hearing");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List046FlPart12V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("12.1C", "12.1C - Costs - party to give reasons - claimant - Rule 75"),
        @CCD(displayOrder = 2)
        V002("12.1R", "12.1R - Costs - party to give reasons - respondent - Rule 75"),
        @CCD(displayOrder = 3)
        V003("12.2C", "12.2C - Costs - out of time - rejected - claimant - Rule 75"),
        @CCD(displayOrder = 4)
        V004("12.2R", "12.2R - Costs – out of time - rejected - respondent -Rule 75"),
        @CCD(displayOrder = 5)
        V005("12.3C", "12.3C - Preparation time - party to give reasons - claimant - Rule 75"),
        @CCD(displayOrder = 6)
        V006("12.3R", "12.3R - Preparation time - party to give reasons - respondent - Rule 75"),
        @CCD(displayOrder = 7)
        V007("12.4C", "12.4C - Preparation time order - rejected - claimant - Rule 75"),
        @CCD(displayOrder = 8)
        V008("12.4R", "12.4R - Preparation time order -rejected - respondent - Rule 75"),
        @CCD(displayOrder = 9)
        V009("12.5C", "12.5C - Wasted costs - party to give reasons - claimant's rep - Rule 80"),
        @CCD(displayOrder = 10)
        V010("12.5R", "12.5R - Wasted costs - party to give reasons - respondent's rep -Rule 80"),
        @CCD(displayOrder = 11)
        V011(
                "12.6C",
                "12.6C - Wasted costs - party to give reasons - tribunal's initiative - Rule 80"),
        @CCD(displayOrder = 12)
        V012(
                "12.6R",
                "12.6R - Wasted costs - party to give reasons - tribunal's initiative- resp rep -"
                        + " Rule 80"),
        @CCD(displayOrder = 13)
        V013(
                "12.7C",
                "12.7C - Wasted costs application - out of time - rejected - claimant's rep - Rule"
                        + " 80"),
        @CCD(displayOrder = 14)
        V014(
                "12.7R",
                "12.7R - Wasted costs application - out of time - rejected - respondent's rep -"
                        + " Rule 80"),
        @CCD(displayOrder = 15)
        V015("12.8C", "12.8C - Wasted costs order - out of time -rejected - claimant - Rule 80"),
        @CCD(displayOrder = 16)
        V016("12.8R", "12.8R - Wasted costs order - out of time - rejected - respondent - Rule 80");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List046FlPart12V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("104", "104 Application for expenses/prep time/wasted costs refused as out of time"),
        @CCD(displayOrder = 2)
        V002("105", "105 Show cause expenses/prep time."),
        @CCD(displayOrder = 3)
        V003("105.A", "105A Show cause expenses/prep time EJ own initiative"),
        @CCD(displayOrder = 4)
        V004("106", "106 Show cause wasted costs"),
        @CCD(displayOrder = 5)
        V005("106.A", "106A Show cause wasted costs - EJ own initiative");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List047FlPart13V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("13.1C", "13.1C - Listing stencil - claimant"),
        @CCD(displayOrder = 2)
        V002("13.1R", "13.1R - Listing stencil - respondent"),
        @CCD(displayOrder = 3)
        V003("13.2A", "13.2A Correspondence to both parties"),
        @CCD(displayOrder = 4)
        V004("13.2C", "13.2C - Acknowledgment of correspondence - claimant"),
        @CCD(displayOrder = 5)
        V005("13.2R", "13.2R - Acknowledgment of correspondence - respondent"),
        @CCD(displayOrder = 6)
        V006("13.3C", "13.3C - Ack and copy - claimant"),
        @CCD(displayOrder = 7)
        V007("13.3R", "13.3R - Ack and copy - respondent"),
        @CCD(displayOrder = 8)
        V008("13.4C", "13.4C - Correspondence - reminder - claimant"),
        @CCD(displayOrder = 9)
        V009("13.4R", "13.4R - Correspondence - reminder - respondent"),
        @CCD(displayOrder = 10)
        V010("13.5C", "13.5C - Request for comments – claimant "),
        @CCD(displayOrder = 11)
        V011("13.5R", "13.5R - Request for comments – respondent "),
        @CCD(displayOrder = 12)
        V012("13.6C", "13.6C - Current position - claimant"),
        @CCD(displayOrder = 13)
        V013("13.6R", "13.6R - Current position - respondent "),
        @CCD(displayOrder = 14)
        V014("13.7C", "13.7C - Change of correspondence address - claimant"),
        @CCD(displayOrder = 15)
        V015("13.7R", "13.7R - Change of correspondence address - respondent"),
        @CCD(displayOrder = 16)
        V016("13.8C", "13.8C - Correspondence to EJ - claimant"),
        @CCD(displayOrder = 17)
        V017("13.8R", "13.8R - Correspondence to EJ - respondent"),
        @CCD(displayOrder = 18)
        V018("13.9C", "13.9C - Do not copy to tribunal - claimant"),
        @CCD(displayOrder = 19)
        V019("13.9R", "13.9R - Do not copy to tribunal – respondent"),
        @CCD(displayOrder = 20)
        V020("13.10C", "13.10C - Advice – claimant "),
        @CCD(displayOrder = 21)
        V021("13.10R", "13.10R - Advice - respondent  "),
        @CCD(displayOrder = 22)
        V022("13.11C", "13.11C - Case transfer request - comments - claimant"),
        @CCD(displayOrder = 23)
        V023("13.11R", "13.11R - Case transfer request - comments - respondent"),
        @CCD(displayOrder = 24)
        V024("13.12C", "13.12C - Case transfer request - refused - claimant"),
        @CCD(displayOrder = 25)
        V025("13.12R", "13.12R - Case transfer request - refused - respondent"),
        @CCD(displayOrder = 26)
        V026("13.13", "13.13 - Case transferred"),
        @CCD(displayOrder = 27)
        V027("13.14C", "13.14C - Case transfer request - Scotland - refused - claimant"),
        @CCD(displayOrder = 28)
        V028("13.14R", "13.14R - Case transfer request - Scotland - refused - respondent"),
        @CCD(displayOrder = 29)
        V029("13.15", "13.15 - Enforcement of award"),
        @CCD(displayOrder = 30)
        V030("13.16", "13.16 - Consent to Employment Judge sitting alone"),
        @CCD(displayOrder = 31)
        V031("13.17", "13.17 - Consent to two person tribunal"),
        @CCD(displayOrder = 32)
        V032("13.18", "13.18 - Authorisation to act in proceedings"),
        @CCD(displayOrder = 33)
        V033("13.19", "13.19 - Claim settled?"),
        @CCD(displayOrder = 34)
        V034("13.20", "13.20 - Settlement of claim"),
        @CCD(displayOrder = 35)
        V035("13.21", "13.21 - Evidence from Abroad"),
        @CCD(displayOrder = 36)
        V036("ETF1", "ETF1 - Returned form notice");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List047FlPart13V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("110", "110 Acknowledgment of correspondence"),
        @CCD(displayOrder = 2)
        V002("110.A", "110A Correspondence not copied"),
        @CCD(displayOrder = 3)
        V003("111", "111 Request for advice"),
        @CCD(displayOrder = 4)
        V004("112", "112 Cover letter for correspondence "),
        @CCD(displayOrder = 5)
        V005("113", "113 Correspondence to EJ"),
        @CCD(displayOrder = 6)
        V006("114", "114 Authorisation to act in ET proceedings – death of claimant"),
        @CCD(displayOrder = 7)
        V007("115", "115 Transfer comments"),
        @CCD(displayOrder = 8)
        V008("116", "116 Claim transferred"),
        @CCD(displayOrder = 9)
        V009("117", "117 Request for information"),
        @CCD(displayOrder = 10)
        V010("118", "118 Do not copy correspondence to the tribunal"),
        @CCD(displayOrder = 11)
        V011("119.C", "119C Notification of claimant’s new rep"),
        @CCD(displayOrder = 12)
        V012("119.R", "119R New rep respondent"),
        @CCD(displayOrder = 13)
        V013("120", "120 PF enquiry"),
        @CCD(displayOrder = 14)
        V014("122", "122 Request for comments on correspondence"),
        @CCD(displayOrder = 15)
        V015("123.C", "123C Return of documents to claimant"),
        @CCD(displayOrder = 16)
        V016("123.R", "123R Return of documents to respondent"),
        @CCD(displayOrder = 17)
        V017("124.C", "124C Return of productions to claimant"),
        @CCD(displayOrder = 18)
        V018("124.R", "124R Return of productions to respondent"),
        @CCD(displayOrder = 19)
        V019("125.C", "125C Change of address claimant"),
        @CCD(displayOrder = 20)
        V020("125.R", "125R Change of address respondent"),
        @CCD(displayOrder = 21)
        V021("126", "126 Decision on transfer between offices in Scotland"),
        @CCD(displayOrder = 22)
        V022("127", "127 Blank Scottish letter template"),
        @CCD(displayOrder = 23)
        V023("128", "128 Sist police enquiries"),
        @CCD(displayOrder = 24)
        V024("129.C", "129C Soul and conscience certificate for claimant"),
        @CCD(displayOrder = 25)
        V025("129.R", "129R Soul and conscience certificate for respondent"),
        @CCD(displayOrder = 26)
        V026("130", "130 Request not to copy to respondent"),
        @CCD(displayOrder = 27)
        V027("131", "131 Claim in which S of S may be liable"),
        @CCD(displayOrder = 28)
        V028("132", "132 Serve afresh"),
        @CCD(displayOrder = 29)
        V029("133", "133 Trading name respondent"),
        @CCD(displayOrder = 30)
        V030("134", "134 Recall of sist"),
        @CCD(displayOrder = 31)
        V031("135", "135 Respondent in administration – consent required"),
        @CCD(displayOrder = 32)
        V032("136", "136 Administration show cause"),
        @CCD(displayOrder = 33)
        V033("137", "137 Strike out judgment administration"),
        @CCD(displayOrder = 34)
        V034("138", "138 Compulsory liquidation-consent required"),
        @CCD(displayOrder = 35)
        V035("139", "139 Compulsory liquidation show cause"),
        @CCD(displayOrder = 36)
        V036("140", "140 Strike out judgment comp liquidation"),
        @CCD(displayOrder = 37)
        V037("141", "141 Consent to EJ sitting alone"),
        @CCD(displayOrder = 38)
        V038("142", "142 Consent to EJ and 1 member"),
        @CCD(displayOrder = 39)
        V039("144", "144 Change in length of hearing"),
        @CCD(displayOrder = 40)
        V040("145", "145 Letter enclosing extract of award"),
        @CCD(displayOrder = 41)
        V041("146", "146 Letter certified judgment –respondent in E & W"),
        @CCD(displayOrder = 42)
        V042("147", "147 Extract refused – have not received recoupment notice"),
        @CCD(displayOrder = 43)
        V043("148", "148 Company dissolved cannot proceed"),
        @CCD(displayOrder = 44)
        V044("149", "149 Company dissolved show cause"),
        @CCD(displayOrder = 45)
        V045("150", "150 Company dissolved strike out judgment"),
        @CCD(displayOrder = 46)
        V046("151", "151 Insolvency RPO claims enquiry"),
        @CCD(displayOrder = 47)
        V047("219", "219 Letter to Companies House Strike Out/Dissolution"),
        @CCD(displayOrder = 48)
        V048("220", "220 Letter to claimant enclosing reply from Companies House"),
        @CCD(displayOrder = 49)
        V049("221", "221 Equal Pay Status Enquiry"),
        @CCD(displayOrder = 50)
        V050("226", "226 Serve Afresh Respondent");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List048FlPart14V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("14.1", "14.1 - Insolvency - RPO claims enquiry"),
        @CCD(displayOrder = 2)
        V002("14.2", "14.2 - Insolvency - administration – proceedings stayed"),
        @CCD(displayOrder = 3)
        V003("14.3", "14.3 - Insolvency - administration – strike out warning"),
        @CCD(displayOrder = 4)
        V004("14.4", "14.4 - Insolvency - administration – judgment"),
        @CCD(displayOrder = 5)
        V005("14.5", "14.5 - Insolvency - compulsory liquidation – proceedings stayed"),
        @CCD(displayOrder = 6)
        V006("14.6", "14.6 - Insolvency - compulsory liquidation – strike out warning"),
        @CCD(displayOrder = 7)
        V007("14.7", "14.7 - Insolvency - compulsory liquidation – judgment"),
        @CCD(displayOrder = 8)
        V008("14.8", "14.8 - Dissolved company - proceedings stayed"),
        @CCD(displayOrder = 9)
        V009("14.9", "14.9 - Dissolved company - strike out warning"),
        @CCD(displayOrder = 10)
        V010("14.10", "14.10 - Dissolved company – judgment");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List048FlPart14V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("159", "159 Judicial mediation information pack"),
        @CCD(displayOrder = 2)
        V002("160", "160 Criteria"),
        @CCD(displayOrder = 3)
        V003("161", "161 Form A"),
        @CCD(displayOrder = 4)
        V004("162", "162 JM Note to parties"),
        @CCD(displayOrder = 5)
        V005("163", "163 JM1 7 days to consider"),
        @CCD(displayOrder = 6)
        V006("164", "164 JM2 follow up if no reply from 1 party"),
        @CCD(displayOrder = 7)
        V007("165", "165 JM3 interested party explain delay"),
        @CCD(displayOrder = 8)
        V008("166", "166 JM4 failure to state interest"),
        @CCD(displayOrder = 9)
        V009("167", "167 JM5 no interest"),
        @CCD(displayOrder = 10)
        V010("168", "168 JM6 offer"),
        @CCD(displayOrder = 11)
        V011("169", "169 JM7 refusal"),
        @CCD(displayOrder = 12)
        V012("170", "170 JM Notice of arrangements PH"),
        @CCD(displayOrder = 13)
        V013("171", "171 JM Notice of mediation PH"),
        @CCD(displayOrder = 14)
        V014("172", "172 JM report");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List049FlPart15V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("15.1", "15.1 - JM by video Notice of hearing and CMOs"),
        @CCD(displayOrder = 2)
        V002("15.2", "15.2 - Attended JM Notice of hearing and CMOs"),
        @CCD(displayOrder = 3)
        V003("15.3", "15.3 - DRA by video Notice of Hearing and CMOs"),
        @CCD(displayOrder = 4)
        V004("15.4", "15.4 - Attended DRA Notice of Hearing and CMOs"),
        @CCD(displayOrder = 5)
        V005("15.5", "15.5 - DRA appointments - Information Sheet");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List049FlPart15V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("180", "180 Receipt of interim relief"),
        @CCD(displayOrder = 2)
        V002("181", "181 Notice of application for interim relief"),
        @CCD(displayOrder = 3)
        V003("182", "182 Notice of hearing interim relief"),
        @CCD(displayOrder = 4)
        V004("183", "183 H & S ack of application to suspend notice"),
        @CCD(displayOrder = 5)
        V005("184", "184 H & S notice to respondent of application to suspend notice"),
        @CCD(displayOrder = 6)
        V006("185", "185 H & S decision on application to suspend notice"),
        @CCD(displayOrder = 7)
        V007("186", "186 H & S appointment of assessor"),
        @CCD(displayOrder = 8)
        V008("189", "189 EQV notice of stage 1 Equal Value hearing"),
        @CCD(displayOrder = 9)
        V009("190", "190 EQV order issued after stage 1 independent expert appointed"),
        @CCD(displayOrder = 10)
        V010("191", "191 EQV requirement expert report"),
        @CCD(displayOrder = 11)
        V011("192", "192 EQV Order issued after stage 1 no IE appointed"),
        @CCD(displayOrder = 12)
        V012("193", "193 EQV Receipt of independent expert report"),
        @CCD(displayOrder = 13)
        V013("194", "194 EQV notice of stage 2 equal value hearing");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List050FlPart16V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("16.1", "16.1 - Notice of Stage 1 EQV hearing "),
        @CCD(displayOrder = 2)
        V002("16.2", "16.2 - Notice of Stage 2 EQV hearing "),
        @CCD(displayOrder = 3)
        V003("16.3", "16.3 - EQV - Order - Independent Expert  "),
        @CCD(displayOrder = 4)
        V004("16.4", "16.4 - EQV - Requirement – Expert Report"),
        @CCD(displayOrder = 5)
        V005("16.5", "16.5 - EQV - Receipt of Expert’s Report");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List050FlPart16V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("LO1", "LO1 Rejection rule 13"),
        @CCD(displayOrder = 2)
        V002("LO2", "LO2 Rule 21 granted"),
        @CCD(displayOrder = 3)
        V003("LO3", "LO3 Rule 21 App Refused"),
        @CCD(displayOrder = 4)
        V004("LO4", "LO4 Withdrawal Letter"),
        @CCD(displayOrder = 5)
        V005("LO5", "LO5 CMO amendment granted"),
        @CCD(displayOrder = 6)
        V006("LO6", "LO6 CMO Additional Information"),
        @CCD(displayOrder = 7)
        V007("LO7", "LO7 CMO claims to be considered together"),
        @CCD(displayOrder = 8)
        V008("LO8", "LO8 CMO postponement"),
        @CCD(displayOrder = 9)
        V009("LO9", "LO9 CMO lead case"),
        @CCD(displayOrder = 10)
        V010("LO10", "LO10 CMPH Listing"),
        @CCD(displayOrder = 11)
        V011("LO11", "LO11 CMO application refused"),
        @CCD(displayOrder = 12)
        V012("LO12", "LO12 CMO Extension of time for compliance"),
        @CCD(displayOrder = 13)
        V013("LO13", "LO13 Strike out warning to Claimant"),
        @CCD(displayOrder = 14)
        V014("LO14", "LO14 Strike out warning to Respondent"),
        @CCD(displayOrder = 15)
        V015("LO15", "LO15 Rule 51 Judgment template");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List051FlPart17 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("17.1", "17.1 - Acknowledgment of appeal "),
        @CCD(displayOrder = 2)
        V002("17.2", "17.2 - Notice of appeal received"),
        @CCD(displayOrder = 3)
        V003("17.3", "17.3 - Notice of appeal hearing "),
        @CCD(displayOrder = 4)
        V004(
                "17.4",
                "17.4 - H & S appeal  - suspension of prohibition notice - written"
                        + " representations "),
        @CCD(displayOrder = 5)
        V005("17.5", "17.5 - H & S appeal -  direction - prohibition notice suspended "),
        @CCD(displayOrder = 6)
        V006(
                "17.6",
                "17.6 - Notes for H & S or Training Levy Appeal Judgment                           "
                        + "                                         "),
        @CCD(displayOrder = 7)
        V007("17.7", "17.7 - Acknowledgement of withdrawal of appeal ");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List052FlPart18 implements HasLabel, HasCode {
        @CCD(displayOrder = 1, numericListElementCode = "18.1")
        V001("18.1", "18.1 – Recoupment Annex (Reg 4). Monetary Awards"),
        @CCD(displayOrder = 2, numericListElementCode = "18.2")
        V002("18.2", "18.2 – Recoupment Annex (Reg 5(2)). Protective Award"),
        @CCD(displayOrder = 3, numericListElementCode = "18.3")
        V003("18.3", "18.3 – Recoupment Notice (Reg 4). Monetary Awards"),
        @CCD(displayOrder = 4, numericListElementCode = "18.4")
        V004("18.4", "18.4 – Recoupment Notice (Reg 5(1)). Protective Award"),
        @CCD(displayOrder = 5)
        V005("FPJA", "FPJA - Financial Penalties Judgment Annex"),
        @CCD(displayOrder = 8)
        V006("IOJULY", "Interest Order from 29 July ");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List053FlPart2V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("2.1", "2.1 - UDL - length of service - no response required - whole claim"),
        @CCD(displayOrder = 2)
        V002("2.2", "2.2 - UDL - length of service - no response required - part claim"),
        @CCD(displayOrder = 3)
        V003("2.3", "2.3 - UDL - length of service - response now required"),
        @CCD(displayOrder = 4)
        V004("2.4", "2.4 - Interim relief - notice of application"),
        @CCD(displayOrder = 5)
        V005("2.5", "2.5 - Interim relief - notice of hearing"),
        @CCD(displayOrder = 6)
        V006("2.6", "2.6 - ET2 - no hearing date"),
        @CCD(displayOrder = 7)
        V007("2.6A", "2.6A - Multiple claims - Rule 17(3)"),
        @CCD(displayOrder = 8)
        V008("2.7", "2.7 - ET2 - short track"),
        @CCD(displayOrder = 9)
        V009("2.7A", "2.7A - ET2 - short track - Video"),
        @CCD(displayOrder = 10)
        V010("2.8", "2.8 - ET2 - UDL plus"),
        @CCD(displayOrder = 11)
        V011("2.8A", "2.8A - ET2 - UDL plus - Video"),
        @CCD(displayOrder = 12)
        V012(
                "2.9",
                "2.9 - Response required - claim reconsideration - further complaints accepted"),
        @CCD(displayOrder = 13)
        V013("2.10", "2.10 - Resending claim"),
        @CCD(displayOrder = 14)
        V014("2.10A", "2.10A - Substituted service - rule 87"),
        @CCD(displayOrder = 15)
        V015("2.10B", "2.10B - Service on Secretary of State - Rule 95 "),
        @CCD(displayOrder = 16)
        V016("2.11", "2.11 - Response accepted"),
        @CCD(displayOrder = 17)
        V017("2.11A", "2.11A - Response - documents returned"),
        @CCD(displayOrder = 18)
        V018("2.12", "2.12 - Response rejection - prescribed form - Rule 18(1)(a)"),
        @CCD(displayOrder = 19)
        V019("2.12A", "2.12A - Response rejection - insufficient evidence"),
        @CCD(displayOrder = 20)
        V020("2.13", "2.13 - Response rejection - minimum information – Rule 18(1)(b)"),
        @CCD(displayOrder = 21)
        V021("2.14", "2.14 - Response rejection - out of time – Rule 19"),
        @CCD(displayOrder = 22)
        V022("2.15", "2.15 - Response Rejection - Your Questions Answered"),
        @CCD(displayOrder = 23)
        V023("2.16", "2.16 - Response rejection - apply again – Rule 20"),
        @CCD(displayOrder = 24)
        V024("2.17", "2.17 - Response reconsideration – successful - Rule 20"),
        @CCD(displayOrder = 25)
        V025("2.18", "2.18 - Response rejection - reconsideration hearing - Rule 20(3) "),
        @CCD(displayOrder = 26)
        V026("2.18A", "2.18A - Response rejection - reconsideration - dismissed – Rule 20"),
        @CCD(displayOrder = 27)
        V027("2.19", "2.19 - Response - extension of time - apply again - Rules 5 & 21"),
        @CCD(displayOrder = 28)
        V028(
                "2.20",
                "2.20 - Response - extension of time - time limit not yet expired - granted – Rules"
                        + " 5 & 21"),
        @CCD(displayOrder = 29)
        V029(
                "2.21",
                "2.21 - Response - extension of time - time limit not yet expired - refused – Rules"
                        + " 5 & 21"),
        @CCD(displayOrder = 30)
        V030("2.22", "2.22 - Response submitted late - extension of time - refused – Rules 5 & 21"),
        @CCD(displayOrder = 31)
        V031(
                "2.23",
                "2.23 - Response submitted late - extension of time – copy to claimant – Rules 5 &"
                        + " 21"),
        @CCD(displayOrder = 32)
        V032("2.24", "2.24 - Response submitted late - extension of time – granted – Rules 5 & 21"),
        @CCD(displayOrder = 33)
        V033("2.24A", "2.24A - Response late by email - extension of time - granted"),
        @CCD(displayOrder = 34)
        V034("2.25", "2.25 - Response – extension of time - hearing required – Rule 21"),
        @CCD(displayOrder = 35)
        V035("2.26", "2.26 - No response received - Rule 22"),
        @CCD(displayOrder = 36)
        V036("2.26A", "2.26A - Case not contested – Rule 22"),
        @CCD(displayOrder = 37)
        V037("2.27", "2.27 - Response – amendment granted");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List053FlPart2V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("8", "8 Extension of time for response granted"),
        @CCD(displayOrder = 2)
        V002("9", "9 Extension of time for response refused"),
        @CCD(displayOrder = 3)
        V003("10", "10 Rejection of response by staff"),
        @CCD(displayOrder = 4)
        V004("11", "11 Rejection of response by Employment Judge"),
        @CCD(displayOrder = 5)
        V005("12", "12 Reconsideration of decision to reject response"),
        @CCD(displayOrder = 6)
        V006("13", "13 Acknowledgment of response (response accepted)"),
        @CCD(displayOrder = 7)
        V007("14", "14 Notice to claimant of response accepted"),
        @CCD(displayOrder = 8)
        V008(
                "14A",
                "14A Notice to respondent that part of claim accepted following reconsideration");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List054FlPart20 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("LO1", "R21 EOT Granted"),
        @CCD(displayOrder = 2)
        V002("LO2", "R21 EOT Refused - Application in Time"),
        @CCD(displayOrder = 3)
        V003("LO3", "R21 EOT Refused - Application OOT"),
        @CCD(displayOrder = 4)
        V004("LO4", "R51 Dismissal Judgments"),
        @CCD(displayOrder = 5)
        V005("LO5", "General Order");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List055FlPart3V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("3.1", "3.1 - Employer's contract claim rejected - jurisdiction - Rule 24"),
        @CCD(displayOrder = 2)
        V002(
                "3.2",
                "3.2 - Employer's contract claim rejected - out of time - not part of response – r."
                        + " 24"),
        @CCD(displayOrder = 3)
        V003("3.3", "3.3 - Employer's contract claim rejected - Rule 24"),
        @CCD(displayOrder = 4)
        V004("3.4", "3.4 - Employer's contract claim rejection of part - Rule 24"),
        @CCD(displayOrder = 5)
        V005(
                "3.5",
                "3.5 - Employer's contract claim rejection - reconsideration – re-apply - Rule 14"),
        @CCD(displayOrder = 6)
        V006(
                "3.6",
                "3.6 - Employer's contract claim rejection - reconsideration - claim accepted – r."
                        + " 14"),
        @CCD(displayOrder = 7)
        V007(
                "3.7",
                "3.7 - Employer's contract claim rejection - reconsideration - hearing - Rule 14"),
        @CCD(displayOrder = 8)
        V008("3.8", "3.8 - Notice of employer's contract claim - Rule 25"),
        @CCD(displayOrder = 9)
        V009("3.9", "3.9 - Employer's contract claim - no response required - whole claim"),
        @CCD(displayOrder = 10)
        V010("3.10", "3.10 - Employer's contract claim - response required to part"),
        @CCD(displayOrder = 11)
        V011("3.11", "3.11 - Employer's contract claim - response now required"),
        @CCD(displayOrder = 12)
        V012(
                "3.12",
                "3.12 - Response to employer's contract claim - extension of time - apply again -"
                        + " Rules 26 & 21"),
        @CCD(displayOrder = 13)
        V013(
                "3.13",
                "3.13 - Response to employer's contract claim - extension of time - time limit not"
                        + " yet expired - granted - Rules 26 & 21"),
        @CCD(displayOrder = 14)
        V014(
                "3.14",
                "3.14 - Response to employer's contract claim- extension of time - time limit not"
                        + " yet expired - refused -Rules 26 & 21"),
        @CCD(displayOrder = 15)
        V015(
                "3.15",
                "3.15 - Response to employer's contract claim submitted late - extension of"
                        + " time-refused - Rules 26 & 21"),
        @CCD(displayOrder = 16)
        V016(
                "3.16",
                "3.16 - \tResponse to employer's contract claim submitted late - extension of time"
                    + " - granted - Rules 26 & 21"),
        @CCD(displayOrder = 17)
        V017(
                "3.17",
                "3.17 - Reply to employer's contract claim-extension of time-hearing required- Rule"
                        + " 21"),
        @CCD(displayOrder = 18)
        V018("3.18", "3.18 - Employer's contract claim - no reply received- Rule 22"),
        @CCD(displayOrder = 19)
        V019("3.19", "3.19 - Response to employer's contract claim - amendment granted"),
        @CCD(displayOrder = 20)
        V020(
                "3.20",
                "3.20 - Employer's contract claim- response submitted late - extension of time"),
        @CCD(displayOrder = 21)
        V021("3.21", "3.21 - Employer's contract claim - response accepted"),
        @CCD(displayOrder = 22)
        V022("3.22", "3.22 - Employer's contract claim - rejection of response - out of time "),
        @CCD(displayOrder = 23)
        V023(
                "3.23",
                "3.23 - Employer's contract claim - Response Rejection - Your Questions Answered");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List055FlPart3V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("18", "18 Acknowledgement of employer's contract claim (ECC accepted)"),
        @CCD(displayOrder = 2)
        V002("19", "19 Rejection of employer’s contract claim"),
        @CCD(displayOrder = 3)
        V003("19.A", "19A Reconsideration of decision to reject employer’s contract claim"),
        @CCD(displayOrder = 4)
        V004("20", "20 Notice of contract claim (ECC accepted)"),
        @CCD(displayOrder = 5)
        V005("21", "21 Acknowledgment of reply to contract claim"),
        @CCD(displayOrder = 6)
        V006("22", "22 Notice of reply to contract claim"),
        @CCD(displayOrder = 7)
        V007("23", "23 Rejection of response to contract claim"),
        @CCD(displayOrder = 8)
        V008("24", "24 Extension of time to submit reply to contract claim granted"),
        @CCD(displayOrder = 9)
        V009("25", "25 Extension of time to submit reply to contract claim refused");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List056FlPart4V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("4.1", "4.1 - Further information - Rule 27"),
        @CCD(displayOrder = 2)
        V002("4.1A", "4.1A - Disability Information"),
        @CCD(displayOrder = 3)
        V003("4.2", "4.2 - Notice and order-whole claim- both parties - Rule 28(1) "),
        @CCD(displayOrder = 4)
        V004("4.3", "4.3 - Notice and order-part of claim- both parties - Rule 28(1) "),
        @CCD(displayOrder = 5)
        V005("4.4", "4.4 - Letter following dismissal - both parties - Rule 28(2) "),
        @CCD(displayOrder = 6)
        V006(
                "4.5",
                "4.5 - Notice of preliminary hearing after initial consideration of claim - Rule"
                        + " 28(3)"),
        @CCD(displayOrder = 7)
        V007("4.6", "4.6 - Notice and order-whole response- both parties - Rule 29(1) "),
        @CCD(displayOrder = 8)
        V008("4.7", "4.7 - Notice and order-part of response- both parties - Rule 29(1)"),
        @CCD(displayOrder = 9)
        V009("4.8", "4.8 - Letter following dismissal - both parties - Rule 29(2)"),
        @CCD(displayOrder = 10)
        V010(
                "4.8A",
                "4.8A - Notice of preliminary hearing after initial consideration of response -"
                        + " Rule 29(3)"),
        @CCD(displayOrder = 11)
        V011("4.9", "4.9 - Trading name of respondent "),
        @CCD(displayOrder = 12)
        V012("4.9A", "4.9A - Respondent’s name on response "),
        @CCD(displayOrder = 13)
        V013("4.10", "4.10 - Rule 22 judgment - remedy hearing required"),
        @CCD(displayOrder = 14)
        V014("4.11", "4.11 - Rule 22 judgment - reconsideration"),
        @CCD(displayOrder = 15)
        V015(
                "4.12",
                "4.12 - Rule 22 judgment - reconsideration – application out of time – accepted"),
        @CCD(displayOrder = 16)
        V016("4.13", "4.13 - Rule 22 judgment - reconsideration – application rejected"),
        @CCD(displayOrder = 17)
        V017("4.14", "4.14 - Rule 22 judgment - reconsideration – denied"),
        @CCD(displayOrder = 18)
        V018("4.15", "4.15 - Rule 22 judgment – claim not quantified "),
        @CCD(displayOrder = 19)
        V019("4.16", "4.16 - Rule 22 judgment not appropriate"),
        @CCD(displayOrder = 20)
        V020("4.17", "4.17 - Rule 22 judgment – universal template "),
        @CCD(displayOrder = 21)
        V021("4.18", "4.18 - Rule 27 referral to EJ - response received"),
        @CCD(displayOrder = 22)
        V022("4.19", "4.19 - Rule 22 referral to EJ – no response");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List056FlPart4V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("15", "15 Further information required"),
        @CCD(displayOrder = 2)
        V002("16", "16 Rule 22 judgment template"),
        @CCD(displayOrder = 3)
        V003("16.A", "16A Simple Rule 22 judgment template"),
        @CCD(displayOrder = 4)
        V004("16.B", "16B Rule 22 judgment - liability only"),
        @CCD(displayOrder = 5)
        V005("17", "17 Notice of Rule 22 Judgment");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List057FlPart5V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("5.1A", "5.1A - Application not copied – not considered = rules 31 and 90"),
        @CCD(displayOrder = 2)
        V002(
                "5.1B",
                "5.1B - Correspondence not copied – sent to other party(ies) by Tribunal office -"
                        + " rule 90"),
        @CCD(displayOrder = 3)
        V003("5.1C", "5.1C - Application not copied - claimant - Rule 31"),
        @CCD(displayOrder = 4)
        V004("5.1R", "5.1R - Application not copied - respondent - Rule 31"),
        @CCD(displayOrder = 5)
        V005("5.2C", "5.2C - Application not copied but sent on by ET - claimant - Rules 31 & 90"),
        @CCD(displayOrder = 6)
        V006("5.2R", "5.2R - Application not copied but sent on by ET - respondent - Rule 31 & 90"),
        @CCD(displayOrder = 7)
        V007("5.3C", "5.3C - Application refused - claimant - Rule 31"),
        @CCD(displayOrder = 8)
        V008("5.3R", "5.3R - Application refused - respondent - Rule 31"),
        @CCD(displayOrder = 9)
        V009("5.4C", "5.4C - Consider application at hearing - claimant - Rule 31(3)"),
        @CCD(displayOrder = 10)
        V010("5.4R", "5.4R - Consider application at hearing - respondent - Rule 31(3)"),
        @CCD(displayOrder = 11)
        V011("5.5", "5.5 - Considering case management prelim hearing - Rule 30"),
        @CCD(displayOrder = 12)
        V012("5.5A", "5.5A - Short track directions- rule 30"),
        @CCD(displayOrder = 13)
        V013("5.6", "5.6 - Case management order as proposed by parties - Rule 30"),
        @CCD(displayOrder = 14)
        V014("5.7", "5.7 - Case management prelim hearing necessary - Rule 30"),
        @CCD(displayOrder = 15)
        V015("5.8", "5.8 - Case management order signed by EJ - Rule 30"),
        @CCD(displayOrder = 16)
        V016("5.9C", "5.9C - Leave to amend claim - Rule 30"),
        @CCD(displayOrder = 17)
        V017("5.9R", "5.9R - Leave to amend response – respondent - Rule 30"),
        @CCD(displayOrder = 18)
        V018("5.10", "5.10 - Postponement by order of REJ"),
        @CCD(displayOrder = 19)
        V019("5.11C", "5.11C - Postponement order - claimant"),
        @CCD(displayOrder = 20)
        V020("5.11R", "5.11R - Postponement order - respondent"),
        @CCD(displayOrder = 21)
        V021("5.12C", "5.12C - Postponement refused – claimant "),
        @CCD(displayOrder = 22)
        V022("5.12R", "5.12R - Postponement refused - respondent "),
        @CCD(displayOrder = 23)
        V023("5.13C", "5.13C - Disclose information – claimant - Rule 33"),
        @CCD(displayOrder = 24)
        V024("5.13R", "5.13R - Disclose information - respondent - Rule 33"),
        @CCD(displayOrder = 25)
        V025("5.14C", "5.14C - Disclose documents - claimant - Rule 33"),
        @CCD(displayOrder = 26)
        V026("5.14R", "5.14R - Disclose documents - respondent - Rule 33"),
        @CCD(displayOrder = 27)
        V027("5.15C", "5.15C - Witness order query - claimant - Rule 34"),
        @CCD(displayOrder = 28)
        V028("5.15R", "5.15R - Witness order query - respondent - Rule 34"),
        @CCD(displayOrder = 29)
        V029("5.16", "5.16 - Refusal of Witness Order - respondent - Rule 34"),
        @CCD(displayOrder = 30)
        V030("5.17", "5.17 - Witness Order - to produce documents or information - Rule 34"),
        @CCD(displayOrder = 31)
        V031("5.18", "5.18 - Witness Order – to give evidence - Rule 34"),
        @CCD(displayOrder = 32)
        V032("5.18A", "5.18A Witness Order – to give evidence - Rule 34"),
        @CCD(displayOrder = 33)
        V033("5.19", "5.19 - Witness Order - accompanying letter - claimant - Rule 34"),
        @CCD(displayOrder = 34)
        V034("5.19A", "5.19A - Witness order - Notice to other party - rule 34(2)"),
        @CCD(displayOrder = 35)
        V035("5.20", "5.20 - Witness order set aside - Rule 34"),
        @CCD(displayOrder = 36)
        V036("5.21", "5.21 - Witness order not set aside - Rule 34"),
        @CCD(displayOrder = 37)
        V037("5.22", "5.22 - Order adding a party - Rule 35"),
        @CCD(displayOrder = 38)
        V038("5.23", "5.23 - Order removing a party - Rule 35"),
        @CCD(displayOrder = 39)
        V039("5.24", "5.24 - Considering cases together"),
        @CCD(displayOrder = 40)
        V040("5.25", "5.25 - Order considering cases together"),
        @CCD(displayOrder = 41)
        V041("5.26", "5.26 - Unless Order - claimant - Rule 39"),
        @CCD(displayOrder = 42)
        V042("5.27", "5.27 - Unless order - respondent - Rule 39"),
        @CCD(displayOrder = 43)
        V043("5.28", "5.28 - Dismissal of claim - Rule 39"),
        @CCD(displayOrder = 44)
        V044("5.29", "5.29 - Dismissal of response - Rule 39"),
        @CCD(displayOrder = 45)
        V045("5.30", "5.30 - Setting aside dismissal of claim - granted - Rule 39"),
        @CCD(displayOrder = 46)
        V046("5.31", "5.31 - Setting aside dismissal of response - granted - Rule 39"),
        @CCD(displayOrder = 47)
        V047("5.32", "5.32 - Hearing to consider setting dismissal of claim - Rule 39"),
        @CCD(displayOrder = 48)
        V048("5.33", "5.33 - Hearing to consider setting dismissal of response - Rule 39"),
        @CCD(displayOrder = 49)
        V049("5.34", "5.34 - Application to set aside dismissal of claim - refused - Rule 39"),
        @CCD(displayOrder = 50)
        V050("5.35", "5.35 - Application to set aside dismissal of response - refused - Rule 39"),
        @CCD(displayOrder = 51)
        V051("5.36", "5.36 - Proposal to strike out – claim not struck out - Rule 38"),
        @CCD(displayOrder = 52)
        V052("5.37", "5.37 - Proposal to strike out - response not struck out – Rule 38"),
        @CCD(displayOrder = 53)
        V053("5.38", "5.38 - Order - non-compliance - claimant - Rule 38"),
        @CCD(displayOrder = 54)
        V054("5.39", "5.39 - Order - non-compliance - respondent - Rule 38"),
        @CCD(displayOrder = 55)
        V055("5.42", "5.42 - Stay order - Rule 30"),
        @CCD(displayOrder = 56)
        V056("5.43", "5.43 - Schedule of Loss"),
        @CCD(displayOrder = 57)
        V057("5.44", "5.44 - PHCM Order");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List057FlPart5V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("26", "26 Further information required"),
        @CCD(displayOrder = 2)
        V002("27", "27 Notice that claim will be dismissed after initial consideration"),
        @CCD(displayOrder = 3)
        V003("28", "28 Notice that response will be dismissed after initial consideration"),
        @CCD(displayOrder = 4)
        V004("29", "29 Letter from tribunal confirming dismissal under Rule 28"),
        @CCD(displayOrder = 5)
        V005("30", "30 Letter from tribunal confirming dismissal under Rule 29"),
        @CCD(displayOrder = 6)
        V006("31", "31 Claim allowed to proceed after representations"),
        @CCD(displayOrder = 7)
        V007(
                "31.1",
                "31.1 Short and Standard version - Claim allowed to proceed after representations"),
        @CCD(displayOrder = 8)
        V008("32", "32 Response allowed to proceed after representations"),
        @CCD(displayOrder = 9)
        V009(
                "32.1",
                "32.1 Short and Standard version - Response allowed to proceed after"
                        + " representations"),
        @CCD(displayOrder = 10)
        V010(
                "33",
                "33 Initial consideration, claim and response allowed to proceed – orders issued"),
        @CCD(displayOrder = 11)
        V011(
                "33.1",
                "33.1 Short and Standard version - Initial consideration, claim and response"
                        + " allowed to proceed - orders issued"),
        @CCD(displayOrder = 12)
        V012(
                "33.A",
                "33A Initial consideration, claim, response, employers contract claim and reply"
                        + " allowed to proceed – orders issued"),
        @CCD(displayOrder = 13)
        V013(
                "33.A.1",
                "33A.1 Short and Standard version - Initial consideration, claim, response,"
                    + " employers contract claim and response allowed to proceed - orders issued");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List058FlPart6V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("6.1C", "6.1C - Strike out warning - claimant - Rule 38"),
        @CCD(displayOrder = 2)
        V002("6.1R", "6.1R - Strike out warning - respondent - Rule 38"),
        @CCD(displayOrder = 3)
        V003("6.2", "6.2 - Strike out Judgment claim - Rule 38"),
        @CCD(displayOrder = 4)
        V004("6.3", "6.3 - Strike out Judgment - claim - part - Rule 38"),
        @CCD(displayOrder = 5)
        V005("6.4", "6.4 - Strike out Judgment response - Rule 38"),
        @CCD(displayOrder = 6)
        V006("6.5", "6.5 - Strike out Judgment – response - part - Rule 38"),
        @CCD(displayOrder = 7)
        V007("6.6", "6.6 - Withdrawal of claim - Rule 50"),
        @CCD(displayOrder = 8)
        V008("6.7", "6.7 - Withdrawal of claim - part - Rule 50"),
        @CCD(displayOrder = 9)
        V009("6.8", "6.8 - Judgment on withdrawal - Rule 51"),
        @CCD(displayOrder = 10)
        V010("6.9", "6.9 - Judgment on withdrawal - part – Rule 51"),
        @CCD(displayOrder = 11)
        V011("6.10", "6.10 - Letter declining withdrawal judgment - Rule 51"),
        @CCD(displayOrder = 12)
        V012("6.11C", "6.11C - Case remains listed - claimant"),
        @CCD(displayOrder = 13)
        V013("6.11R", "6.11R - Case remains listed - respondent");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List058FlPart6V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("34", "34 Case management order including option for unless order"),
        @CCD(displayOrder = 2)
        V002("34.A", "34A Standard orders"),
        @CCD(displayOrder = 3)
        V003("34.B", "34B CVP standard orders"),
        @CCD(displayOrder = 4)
        V004("34.C", "34C CVP standard orders with witness statements"),
        @CCD(displayOrder = 5)
        V005("34.D", "34D CVP standard orders - No ET3 response"),
        @CCD(displayOrder = 6)
        V006("35", "35 Documents order"),
        @CCD(displayOrder = 7)
        V007("35.A", "35A Documents order 3rd party"),
        @CCD(displayOrder = 8)
        V008("36", "36 Inspection of documents order"),
        @CCD(displayOrder = 9)
        V009("36.A", "36A Inspection of documents order 3rd party"),
        @CCD(displayOrder = 10)
        V010("37", "37 Information order"),
        @CCD(displayOrder = 11)
        V011("37.A", "37A Information order 3rd party"),
        @CCD(displayOrder = 12)
        V012("38", "38 Order that claims be considered together"),
        @CCD(displayOrder = 13)
        V013("38.A", "38A Letter to be sent before 38 Order issued."),
        @CCD(displayOrder = 14)
        V014("39", "39 Witness order"),
        @CCD(displayOrder = 15)
        V015("40", "40 Witness order query letter"),
        @CCD(displayOrder = 16)
        V016("41", "41 Witness order notification to party applicant"),
        @CCD(displayOrder = 17)
        V017("42", "42 Witness order notification to other party"),
        @CCD(displayOrder = 18)
        V018("43", "43 Letter advising of sist"),
        @CCD(displayOrder = 19)
        V019("44", "44 Order adding a party"),
        @CCD(displayOrder = 20)
        V020("45", "45 Order removing a party"),
        @CCD(displayOrder = 21)
        V021("46", "46 Order substituting a party"),
        @CCD(displayOrder = 22)
        V022("47", "47 Covering letter for order granted – not contested"),
        @CCD(displayOrder = 23)
        V023("48", "48 Application for an order cannot be considered"),
        @CCD(displayOrder = 24)
        V024("49", "49 Refusal of case management application"),
        @CCD(displayOrder = 25)
        V025("50", "50 Granting of case management application -contested"),
        @CCD(displayOrder = 26)
        V026("50.A", "50A Order giving leave to amend"),
        @CCD(displayOrder = 27)
        V027("51", "51 Notification of lead case"),
        @CCD(displayOrder = 28)
        V028("52", "52 Order specifying lead case(s)"),
        @CCD(displayOrder = 29)
        V029("53", "53 Notification of change to lead case"),
        @CCD(displayOrder = 30)
        V030("54", "54 Restricted Reporting Order "),
        @CCD(displayOrder = 31)
        V031("55", "55 Notice of Restricted Reporting Order"),
        @CCD(displayOrder = 32)
        V032("56", "56 Order Rule 49(3)b)"),
        @CCD(displayOrder = 33)
        V033("57", "57 Claim/response dismissed"),
        @CCD(displayOrder = 34)
        V034("57.A", "57A Application to set aside dismissal under rule 39 refused"),
        @CCD(displayOrder = 35)
        V035("57.B", "57B Dismissal under Rule 39 set aside"),
        @CCD(displayOrder = 36)
        V036(
                "57.C",
                "57C Application to set aside dismissal under rule 39 – hearing will be fixed"),
        @CCD(displayOrder = 37)
        V037("197", "197 Case management application acknowledgement"),
        @CCD(displayOrder = 38)
        V038("225", "225 Pensions Information Order");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List059FlPart7V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "7.3",
                "7.3 - Preliminary hearing - deposit order guidance note for parties - Rule 40"),
        @CCD(displayOrder = 2)
        V002("7.4C", "7.4C - Deposit Order and payment guidance - claimant - Rule 40"),
        @CCD(displayOrder = 3)
        V003("7.4R", "7.4R - Deposit Order and payment guidance - respondent - Rule 40"),
        @CCD(displayOrder = 4)
        V004(
                "7.5",
                "7.5 - Preliminary hearing - deposit order payment guidance for parties - Rule 40"),
        @CCD(displayOrder = 5)
        V005("7.6C", "7.6C - Deposit not paid – Judgment – claimant - Rule 40(4)"),
        @CCD(displayOrder = 6)
        V006("7.6R", "7.6R - Deposit not paid - Judgment – respondent - Rule 40(4)"),
        @CCD(displayOrder = 7)
        V007(
                "7.7",
                "7.7 - Notice of attended preliminary hearing for case management listed on"
                        + " service"),
        @CCD(displayOrder = 8)
        V008(
                "7.8",
                "7.8 - Notice of telephone preliminary hearing for case management listed on"
                        + " service"),
        @CCD(displayOrder = 9)
        V009(
                "7.8A",
                "7.8A - Notice of video preliminary hearing for case management listed on service"),
        @CCD(displayOrder = 10)
        V010("7.9", "7.9 - Notice of any other attended preliminary hearing"),
        @CCD(displayOrder = 11)
        V011("7.9A", "7.9A - Notice of any other video preliminary hearing"),
        @CCD(displayOrder = 12)
        V012("7.10", "7.10 - Notice of any other telephone preliminary hearing"),
        @CCD(displayOrder = 13)
        V013("7.11", "7.11 - Information about video hearings, to be used with 7.8A and 7.9A.");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List059FlPart7V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("58", "58 Strike out warning to claimant"),
        @CCD(displayOrder = 2)
        V002("59", "59 Strike out warning to respondent"),
        @CCD(displayOrder = 3)
        V003("60", "60 Strike out judgment"),
        @CCD(displayOrder = 4)
        V004("61", "61 Intimation of decision not to strike out"),
        @CCD(displayOrder = 5)
        V005("62", "62 Deposit order"),
        @CCD(displayOrder = 6)
        V006("63", "63 Judgment strike out for failure to pay deposit");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List060FlPart8V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("8.1", "8.1 - Notice of attended final hearing"),
        @CCD(displayOrder = 2)
        V002("8.1A", "8.1A - Notice of video final hearing"),
        @CCD(displayOrder = 3)
        V003("8.2", "8.2 - Notice of attended remedy hearing"),
        @CCD(displayOrder = 4)
        V004("8.2A", "8.2A - Notice of video remedy hearing"),
        @CCD(displayOrder = 5)
        V005("8.3", "8.3 - Notice of attended rule 22 remedy hearing"),
        @CCD(displayOrder = 6)
        V006("8.3A", "8.3A - Notice of video rule 22 remedy hearing"),
        @CCD(displayOrder = 7)
        V007("8.3C", "8.3C - Notice of attended rule 22 hearing"),
        @CCD(displayOrder = 8)
        V008("8.3D", "8.3D - Notice of video rule 22 hearing"),
        @CCD(displayOrder = 9)
        V009("8.4", "8.4 - Notice of attended costs or preparation time order hearing"),
        @CCD(displayOrder = 10)
        V010("8.4A", "8.4A - Notice of video costs or preparation time order hearing"),
        @CCD(displayOrder = 11)
        V011("8.5", "8.5 - Notice of attended wasted costs hearing"),
        @CCD(displayOrder = 12)
        V012("8.5A", "8.5A - Notice of video wasted costs hearing"),
        @CCD(displayOrder = 13)
        V013("8.6", "8.6 - Adjournment of attended hearing and notice of new hearing date"),
        @CCD(displayOrder = 14)
        V014("8.6A", "8.6A - Adjournment of video hearing and notice of new hearing date"),
        @CCD(displayOrder = 15)
        V015("8.7", "8.7 - Adjournment of hearing without new hearing date"),
        @CCD(displayOrder = 16)
        V016("8.8", "8.8 - Order postponing attended hearing and notice of new hearing date"),
        @CCD(displayOrder = 17)
        V017("8.8A", "8.8A - Order postponing telephone hearing and notice of new hearing date"),
        @CCD(displayOrder = 18)
        V018("8.8B", "8.8B - Order postponing video hearing and notice of new hearing date"),
        @CCD(displayOrder = 19)
        V019("8.9", "8.9 - Information on CVP");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List060FlPart8V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("64", "64 Acknowledgment of withdrawal - options "),
        @CCD(displayOrder = 2)
        V002("66", "66 Case remains listed pending settlement"),
        @CCD(displayOrder = 3)
        V003("67", "67 Settlement intimated by parties"),
        @CCD(displayOrder = 4)
        V004("68", "68 Settlement intimated by ACAS"),
        @CCD(displayOrder = 5)
        V005("69", "69 Judgment rule 51 dismissal on withdrawal");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List061FlPart9V1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("9.1A", "9.1A - Hearing in private - interest of justice - Rule 49(3)(a)"),
        @CCD(displayOrder = 2)
        V002("9.1B", "9.1B - Hearing in private - other specified provision - Rule 49(3)(a)"),
        @CCD(displayOrder = 3)
        V003("9.2A", "9.2A - Anonymisation Order - interests of justice - Rule 49(3)(b)"),
        @CCD(displayOrder = 4)
        V004("9.2B", "9.2B - Anonymisation Order etc - other specified provision - Rule 49(3)(b)"),
        @CCD(displayOrder = 5)
        V005("9.3A", "9.3A - Witness protection - interests of justice - Rule 49(3)(c)"),
        @CCD(displayOrder = 6)
        V006("9.3B", "9.3B - Witness protection - other specified provision – Rule 49(3)(c)"),
        @CCD(displayOrder = 7)
        V007("9.4", "9.4 - RRO – sexual misconduct - r. 49(3)(d) + s. 11 ETA"),
        @CCD(displayOrder = 8)
        V008("9.5", "9.5 - RRO - disability - r. 49(3)(d) + s. 12 ETA"),
        @CCD(displayOrder = 9)
        V009(
                "9.6A",
                "9.6A - RRO - sexual misconduct – indefinite duration/scope - interests of justice"
                        + " - Rule 49(3)(e) + s. 11 ETA"),
        @CCD(displayOrder = 10)
        V010(
                "9.6B",
                "9.6B - RRO - sexual misconduct – indefinite duration/scope - other specified"
                        + " provision - Rule 49(3)(e) + s. 11 ETA"),
        @CCD(displayOrder = 11)
        V011(
                "9.7A",
                "9.7A - RRO – disability - indefinite duration - interests of justice - Rule"
                        + " 49(3)(e) + s.12 ETA "),
        @CCD(displayOrder = 12)
        V012(
                "9.7B",
                "9.7B - RRO – disability - indefinite duration - other specified provision – Rule"
                        + " 49(3)(e) + s. 12 ETA +"),
        @CCD(displayOrder = 13)
        V013("9.8A", "9.8A - RRO – other cases – interests of justice - Rule 49"),
        @CCD(displayOrder = 14)
        V014("9.8B", "9.8B - RRO – other cases - other specified provision - Rule 49"),
        @CCD(displayOrder = 15)
        V015("9.9", "9.9 - Restricted reporting order - Notice - Rule 49(5)(c) ");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List061FlPart9V2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("71", "71 Date listing letters preliminary hearing"),
        @CCD(displayOrder = 2)
        V002("73", "73 Notice of preliminary hearing"),
        @CCD(displayOrder = 3)
        V003("74", "74 Notice of final hearing"),
        @CCD(displayOrder = 4)
        V004("74.2", "74.2 CVP Notice of Final hearing"),
        @CCD(displayOrder = 5)
        V005("75", "75 Acknowledgment of claim and notice of preliminary hearing open track"),
        @CCD(displayOrder = 6)
        V006("76", "76 Notice of claim and notice of preliminary hearing open track"),
        @CCD(displayOrder = 7)
        V007("76 Reform", "76 Reform Notice of claim and notice of preliminary hearing open track"),
        @CCD(displayOrder = 8)
        V008("77.1", "77.1 Ack of OT claim and notice of CVP CMPH"),
        @CCD(displayOrder = 9)
        V009("77.2", "77.2 Notice of OT claim and notice of CVP CMPH"),
        @CCD(displayOrder = 10)
        V010("77.2 Reform", "77.2 Reform Notice of OT claim and notice of CVP CMPH"),
        @CCD(displayOrder = 11)
        V011("78", "78 Date listing letters final hearing (not UDL)"),
        @CCD(displayOrder = 12)
        V012("79", "79 Date listing letters UDL"),
        @CCD(displayOrder = 13)
        V013("80", "80 Decision on request for full tribunal for preliminary hearing"),
        @CCD(displayOrder = 14)
        V014(
                "81",
                "81 Notice of hearing to reconsider rejection of claim/response/employer’s contract"
                        + " claim/reply to contract claim"),
        @CCD(displayOrder = 15)
        V015("82", "82 Notice of hearing to consider dismissal of claim response under Rule 28"),
        @CCD(displayOrder = 16)
        V016("83", "83 Notice of hearing to consider dismissal of response under Rule 29"),
        @CCD(displayOrder = 17)
        V017("84", "84 Notice of reconsideration hearing (judgment)"),
        @CCD(displayOrder = 18)
        V018("85", "85 Notice of remedy hearing"),
        @CCD(displayOrder = 19)
        V019("86", "86 Notice of expenses hearing"),
        @CCD(displayOrder = 20)
        V020("87", "87 Notice of hearing to consider Rule 22 judgment"),
        @CCD(displayOrder = 21)
        V021("90", "90 Notice of continued hearing"),
        @CCD(displayOrder = 22)
        V022("91", "91 Notice of postponement of hearing"),
        @CCD(displayOrder = 23)
        V023(
                "91.A",
                "91A Notice of hearing to consider respondent’s application for extension of time"
                        + " to submit a response"),
        @CCD(displayOrder = 24)
        V024(
                "91.B",
                "91B Notice of hearing to consider claimant’s application for extension of time to"
                        + " submit a reply to an employer’s contract claim"),
        @CCD(displayOrder = 25)
        V025("195", "195 Acknowledgement of appeal and notice of preliminary hearing"),
        @CCD(displayOrder = 26)
        V026("196", "196 Notice of appeal and notice of preliminary hearing"),
        @CCD(displayOrder = 27)
        V027("198", "198 Notice of preliminary hearing (deposit)"),
        @CCD(displayOrder = 28)
        V028("199", "199 Notice of preliminary hearing (preliminary issue)"),
        @CCD(displayOrder = 29)
        V029("199.2", "199.2 CVP Notice of Preliminary Hearing (Preliminary Issue)"),
        @CCD(displayOrder = 30)
        V030("200", "200 Notice of preliminary hearing (strike out)"),
        @CCD(displayOrder = 31)
        V031("201", "201 Notice of preliminary hearing (case management)"),
        @CCD(displayOrder = 32)
        V032("201.2", "201.2 - CVP Notice of Preliminary Hearing (Case Management)"),
        @CCD(displayOrder = 33)
        V033("202", "202 Notice of preliminary hearing (strike out and deposit)"),
        @CCD(displayOrder = 34)
        V034("222", "222 Postponement Granted Rule 32"),
        @CCD(displayOrder = 35)
        V035("223", "223 Postponement Refused Rule 32");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List062FlPosition implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Awaiting appeal judgment", "Awaiting appeal judgment"),
        @CCD(displayOrder = 2)
        V002("Awaiting appeal order", "Awaiting appeal order"),
        @CCD(displayOrder = 3)
        V003(
                "Awaiting copy of improvement or prohibition notice",
                "Awaiting copy of improvement or prohibition notice"),
        @CCD(displayOrder = 4)
        V004("Awaiting default judgment", "Awaiting default judgment"),
        @CCD(displayOrder = 5)
        V005(
                "Awaiting default judgment reconsideration",
                "Awaiting default judgment reconsideration"),
        @CCD(displayOrder = 6)
        V006("Awaiting discovery/inspection", "Awaiting discovery/inspection"),
        @CCD(displayOrder = 7)
        V007("Awaiting disposal of claim application", "Awaiting disposal of claim application"),
        @CCD(displayOrder = 8)
        V008("Awaiting draft judgment from chairman", "Awaiting draft judgment from Judge"),
        @CCD(displayOrder = 9)
        V009("Awaiting ET3", "Awaiting ET3"),
        @CCD(displayOrder = 10)
        V010(
                "Awaiting ET3 - extension of time granted",
                "Awaiting ET3 - extension of time granted"),
        @CCD(displayOrder = 11)
        V011("Awaiting ET3 (c)", "Awaiting ET3 (c)"),
        @CCD(displayOrder = 12)
        V012("Awaiting further and better particulars", "Awaiting further and better particulars"),
        @CCD(displayOrder = 13)
        V013("Awaiting instructions from chairman", "Awaiting instructions from Judge"),
        @CCD(displayOrder = 14)
        V014(
                "Awaiting judgment being sent to the parties, other",
                "Awaiting judgment being sent to the parties, other"),
        @CCD(displayOrder = 15)
        V015(
                "Awaiting listing for Preliminary Hearing",
                "Awaiting listing for Preliminary Hearing"),
        @CCD(displayOrder = 16)
        V016(
                "Awaiting listing for preliminary hearing(CM)",
                "Awaiting listing for preliminary hearing(CM)"),
        @CCD(displayOrder = 17)
        V017("Awaiting listing Hearing", "Awaiting listing Hearing"),
        @CCD(displayOrder = 18)
        V018(
                "Awaiting listing reconsideration application",
                "Awaiting listing reconsideration application"),
        @CCD(displayOrder = 19)
        V019("Awaiting listing remedy/costs hearing", "Awaiting listing remedy/costs hearing"),
        @CCD(displayOrder = 20)
        V020("Awaiting outside proceeding", "Awaiting outside proceeding"),
        @CCD(displayOrder = 21)
        V021("Awaiting reply to a pre-listing stencil", "Awaiting reply to a pre-listing stencil"),
        @CCD(displayOrder = 22)
        V022("Awaiting settlement confirmation", "Awaiting settlement confirmation"),
        @CCD(displayOrder = 23)
        V023("Awaiting withdrawal confirmation", "Awaiting withdrawal confirmation"),
        @CCD(displayOrder = 24)
        V024("Awaiting written answer", "Awaiting written answer"),
        @CCD(displayOrder = 25)
        V025("Awaiting written reasons", "Awaiting written reasons"),
        @CCD(displayOrder = 26)
        V026("Case closed", "Case closed"),
        @CCD(displayOrder = 27)
        V027("Case input in error", "Case input in error"),
        @CCD(displayOrder = 28)
        V028("Case transferred - other country", "Case transferred - other country"),
        @CCD(displayOrder = 29)
        V029("Case transferred - same country", "Case transferred - same country"),
        @CCD(displayOrder = 30)
        V030("Conciliation paused", "Conciliation paused"),
        @CCD(displayOrder = 31)
        V031(
                "Draft judgment received, awaiting typing",
                "Draft judgment received, awaiting typing"),
        @CCD(displayOrder = 32)
        V032(
                "Draft judgment typed, to chairman for amendment",
                "Draft judgment typed, to Judge for amendment"),
        @CCD(displayOrder = 33)
        V033("Draft with members", "Draft with members"),
        @CCD(displayOrder = 34)
        V034("ET1 Online submission", "ET1 Online submission"),
        @CCD(displayOrder = 35)
        V035("ET3 receiving attention", "ET3 receiving attention"),
        @CCD(displayOrder = 36)
        V036("ET3 referred to chairman", "ET3 referred to Judge"),
        @CCD(displayOrder = 37)
        V037("Fair copy, to chairman for signature", "Fair copy, to Judge for signature"),
        @CCD(displayOrder = 38)
        V038("Fixed period of conciliation", "Fixed period of conciliation"),
        @CCD(displayOrder = 39)
        V039(
                "Heard awaiting judgment being sent to the parties",
                "Heard awaiting judgment being sent to the parties"),
        @CCD(displayOrder = 40)
        V040("Listed for a Hearing", "Listed for a Hearing"),
        @CCD(displayOrder = 41)
        V041("Listed for a interim relief hearing", "Listed for a interim relief hearing"),
        @CCD(displayOrder = 42)
        V042("Listed for a preliminary hearing", "Listed for a preliminary hearing"),
        @CCD(displayOrder = 43)
        V043("Listed for a preliminary hearing(CM)", "Listed for a preliminary hearing(CM)"),
        @CCD(displayOrder = 44)
        V044("Listed for a reconsideration hearing", "Listed for a reconsideration hearing"),
        @CCD(displayOrder = 45)
        V045("Listed for a remedy/costs hearing", "Listed for a remedy/costs hearing"),
        @CCD(displayOrder = 46)
        V046("Live EAT case", "Live EAT case"),
        @CCD(displayOrder = 47)
        V047("Manually Created", "Manually created"),
        @CCD(displayOrder = 48)
        V048("Part heard awaiting listing", "Part heard awaiting listing"),
        @CCD(displayOrder = 49)
        V049("Part heard case relisted", "Part heard case relisted"),
        @CCD(displayOrder = 50)
        V050("Postponed by tribunal awaiting listing", "Postponed by tribunal awaiting listing"),
        @CCD(displayOrder = 51)
        V051("Received by Auto-Import", "Received by Auto-Import"),
        @CCD(displayOrder = 52)
        V052("REJECTED", "Rejected"),
        @CCD(displayOrder = 53)
        V053("Revised draft received, awaiting typing", "Revised draft received, awaiting typing"),
        @CCD(displayOrder = 54)
        V054(
                "Settled awaiting notification being sent to the parties",
                "Settled awaiting notification being sent to the parties"),
        @CCD(displayOrder = 55)
        V055("Signed fair copy received", "Signed fair copy received"),
        @CCD(displayOrder = 56)
        V056("Striking out warning issued", "Striking out warning issued"),
        @CCD(displayOrder = 57)
        V057(
                "Withdrawn awaiting notification being sent to the parties",
                "Withdrawn awaiting notification being sent to the parties");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List063FlPositionCT implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Case transferred - same country", "Case transferred - same country"),
        @CCD(displayOrder = 2)
        V002("Case transferred - other country", "Case transferred - other country");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List064FlPostponedBy implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Claimant", "Claimant"),
        @CCD(displayOrder = 2)
        V002("Tribunal", "Tribunal"),
        @CCD(displayOrder = 3)
        V003("Respondent", "Respondent"),
        @CCD(displayOrder = 4)
        V004("Joint", "Joint");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List065FlPreferredTitle implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Mr", "Mr"),
        @CCD(displayOrder = 2)
        V002("Mrs", "Mrs"),
        @CCD(displayOrder = 3)
        V003("Miss", "Miss"),
        @CCD(displayOrder = 4)
        V004("Ms", "Ms"),
        @CCD(displayOrder = 5)
        V005("Mx", "Mx"),
        @CCD(displayOrder = 6)
        V006("Other", "Other"),
        @CCD(displayOrder = 7)
        V007("Prefer not to say", "Prefer not to say");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List066FlPublicPrivate implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Public", "Public"),
        @CCD(displayOrder = 2)
        V002("Private", "Private");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List067FlReconsideration implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "App to have a Legal Officer decision considered afresh - C",
                "App to have a Legal Officer decision considered afresh - C"),
        @CCD(displayOrder = 2)
        V002(
                "App to have a Legal Officer decision considered afresh - R",
                "App to have a Legal Officer decision considered afresh - R"),
        @CCD(displayOrder = 3)
        V003(
                "App for a judgment to be reconsidered - C",
                "App for a judgment to be reconsidered - C"),
        @CCD(displayOrder = 4)
        V004(
                "App for a judgment to be reconsidered - R",
                "App for a judgment to be reconsidered - R");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List068FlReferralSubject implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("ET1", "ET1"),
        @CCD(displayOrder = 2)
        V002("ET3/ECC", "ET3/ECC"),
        @CCD(displayOrder = 3)
        V003("Amend claim", "Amend claim"),
        @CCD(displayOrder = 4)
        V004("Amend response", "Amend response"),
        @CCD(displayOrder = 5)
        V005("Strike out all or part of claim", "Strike out all or part of claim"),
        @CCD(displayOrder = 6)
        V006("Strike out all or part of response", "Strike out all or part of response"),
        @CCD(displayOrder = 7)
        V007("Withdraw claim", "Withdraw claim"),
        @CCD(displayOrder = 8)
        V008("Orders", "Orders"),
        @CCD(displayOrder = 9)
        V009("Party not responded/compiled", "Party not responded/complied"),
        @CCD(displayOrder = 10)
        V010("Order other party", "Order other party"),
        @CCD(displayOrder = 11)
        V011("Rule 21", "Rule 22"),
        @CCD(displayOrder = 12)
        V012("Rule 50 application", "Rule 49 application"),
        @CCD(displayOrder = 13)
        V013("Order a witness to attend", "Order a witness to attend"),
        @CCD(displayOrder = 14)
        V014("Hearings", "Hearings"),
        @CCD(displayOrder = 15)
        V015("Postpone a hearing", "Postpone a hearing"),
        @CCD(displayOrder = 16)
        V016("Judgment", "Judgment"),
        @CCD(displayOrder = 17)
        V017("Reconsider decision", "Reconsider decision"),
        @CCD(displayOrder = 18)
        V018("Reconsider judgment", "Reconsider judgment"),
        @CCD(displayOrder = 19)
        V019("Extension of time request", "Extension of time request"),
        @CCD(displayOrder = 20)
        V020("Transfer request", "Transfer request"),
        @CCD(displayOrder = 21)
        V021("Initial Consideration", "Initial Consideration"),
        @CCD(displayOrder = 22)
        V022("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List069FlReinstate implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Neither", "Neither"),
        @CCD(displayOrder = 2)
        V002("Reengage", "Reengage"),
        @CCD(displayOrder = 3)
        V003("Reinstate", "Reinstate");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List070FlReinstated implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Neither", "Neither"),
        @CCD(displayOrder = 2)
        V002("Reengaged", "Re-engaged"),
        @CCD(displayOrder = 3)
        V003("Reinstated", "Reinstated");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List071FlRepresentativeContact implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Email", "Email"),
        @CCD(displayOrder = 2)
        V002("DX Number", "DX Number"),
        @CCD(displayOrder = 3)
        V003("Post", "Post");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List072FlRepresentativeOccupation implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Solicitor", "Solicitor"),
        @CCD(displayOrder = 2)
        V002("CAB", "CAB"),
        @CCD(displayOrder = 3)
        V003("FRU", "FRU"),
        @CCD(displayOrder = 4)
        V004("Law Centre", "Law Centre"),
        @CCD(displayOrder = 5)
        V005("Union", "Union"),
        @CCD(displayOrder = 6)
        V006("Private Individual", "Private Individual"),
        @CCD(displayOrder = 7)
        V007("Trade Association", "Trade Association"),
        @CCD(displayOrder = 8)
        V008("In-house Representative", "In-house Representative"),
        @CCD(displayOrder = 9)
        V009("Liquidator", "Liquidator"),
        @CCD(displayOrder = 10)
        V010("Administrator", "Administrator"),
        @CCD(displayOrder = 11)
        V011("Trustee", "Trustee"),
        @CCD(displayOrder = 12)
        V012("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List073FlResponseStatus implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Accepted", "Accepted"),
        @CCD(displayOrder = 2)
        V002("Not Received", "Not Received"),
        @CCD(displayOrder = 3)
        V003("Not Accepted", "Not Accepted"),
        @CCD(displayOrder = 4)
        V004("Rejected", "Rejected");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List074FlResponseToAClaim implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("ET3", "ET3"),
        @CCD(displayOrder = 2)
        V002("ET3 Attachment", "ET3 Attachment"),
        @CCD(displayOrder = 3)
        V003("Response accepted", "Response accepted"),
        @CCD(displayOrder = 4)
        V004("Response rejected", "Response rejected"),
        @CCD(displayOrder = 5)
        V005(
                "App to extend time to present a response",
                "App to extend time to present a response"),
        @CCD(displayOrder = 6)
        V006("ET3 Processing", "ET3 Processing");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List075FlRestrictedExcludedRegister implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("No", "No"),
        @CCD(displayOrder = 2)
        V002("Definite", "Definite"),
        @CCD(displayOrder = 3)
        V003("Provisional", "Provisional");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List076FlRestrictedRequestedBy implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Respondent", "Respondent"),
        @CCD(displayOrder = 2)
        V002("Claimant", "Claimant"),
        @CCD(displayOrder = 3)
        V003("Judge", "Judge"),
        @CCD(displayOrder = 4)
        V004("Both Parties", "Both Parties"),
        @CCD(displayOrder = 5)
        V005("Other", "Other"),
        @CCD(displayOrder = 6)
        V006("None", "None");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List077FlServingDocumentRecipient implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Acas", "Acas"),
        @CCD(displayOrder = 2)
        V002("Claimant", "Claimant"),
        @CCD(displayOrder = 3)
        V003("Respondent", "Respondent");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List078FlServingDocumentTypeV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("1.1", "1.1 ET5 Acknowledgment of Claim"),
        @CCD(displayOrder = 2)
        V002("2.6", "2.6 Notice of Claim"),
        @CCD(displayOrder = 3)
        V003("2.7", "2.7 ET2 short track claim"),
        @CCD(displayOrder = 4)
        V004("2.8", "2.8 ET2 UDL plus"),
        @CCD(displayOrder = 5)
        V005("7.7", "7.7 In person preliminary hearing - notice of case management discussion"),
        @CCD(displayOrder = 6)
        V006("7.8", "7.8 Telephone preliminary hearing - notice of case management discussion"),
        @CCD(displayOrder = 7)
        V007("7.8a", "7.8a Video preliminary hearing - notice of case management discussion");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List078FlServingDocumentTypeV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("1.1", "1.1 ET5 Acknowledgement of Claim"),
        @CCD(displayOrder = 2)
        V002("2.6", "2.6 Notice of Claim"),
        @CCD(displayOrder = 3)
        V003("2.7", "2.7 ET2 short track claim"),
        @CCD(displayOrder = 4)
        V004("2.8", "2.8 ET2 UDL plus"),
        @CCD(displayOrder = 5)
        V005("7.7", "7.7 In person preliminary hearing - notice of case management discussion"),
        @CCD(displayOrder = 6)
        V006("7.8", "7.8 Telephone preliminary hearing - notice of case management discussion"),
        @CCD(displayOrder = 7)
        V007("7.8a", "7.8a Video preliminary hearing - notice of case management discussion"),
        @CCD(displayOrder = 8)
        V008("Acknowledgement of Claim", "Acknowledgement of Claim"),
        @CCD(displayOrder = 9)
        V009("Claimant Guidance Notes", "Claimant Guidance Notes"),
        @CCD(displayOrder = 10)
        V010("Claimant Standard Agenda for CMPH", "Claimant Standard Agenda for CMPH"),
        @CCD(displayOrder = 11)
        V011("EC Certification", "EC Certification"),
        @CCD(displayOrder = 12)
        V012("ET1 claim form", "ET1 claim form"),
        @CCD(displayOrder = 13)
        V013("ET3 form", "ET3 form"),
        @CCD(displayOrder = 14)
        V014("Letter 3", "Letter 3"),
        @CCD(displayOrder = 15)
        V015("Letter 7", "Letter 7"),
        @CCD(displayOrder = 16)
        V016(
                "Letter 72 short track notice of claim and notice of hearing",
                "Letter 72 short track notice of claim and notice of hearing"),
        @CCD(displayOrder = 17)
        V017("Letter 75", "Letter 75"),
        @CCD(displayOrder = 18)
        V018("Letter 76 Notice of Claim", "Letter 76 Notice of Claim"),
        @CCD(displayOrder = 19)
        V019("Letter 162 JM Note to Parties", "Letter 162 JM Note to Parties"),
        @CCD(displayOrder = 20)
        V020("Notice of Claim", "Notice of Claim"),
        @CCD(displayOrder = 21)
        V021("Notice of Preliminary Hearing", "Notice of Preliminary Hearing"),
        @CCD(displayOrder = 22)
        V022("Particulars of Claim", "Particulars of Claim"),
        @CCD(displayOrder = 23)
        V023("Respondent Guidance Notes", "Respondent Guidance Notes"),
        @CCD(displayOrder = 24)
        V024("Respondent Standard Agenda for CMPH", "Respondent Standard Agenda for CMPH"),
        @CCD(displayOrder = 25)
        V025("Another type of document", "Another type of document");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List079FlSex implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Male", "Male"),
        @CCD(displayOrder = 2)
        V002("Female", "Female"),
        @CCD(displayOrder = 3)
        V003("Prefer not to say", "Prefer not to say");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List080FlStage implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Stage 1", "Stage 1"),
        @CCD(displayOrder = 2)
        V002("Stage 2", "Stage 2"),
        @CCD(displayOrder = 3)
        V003("Stage 3", "Stage 3");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List081FlStartingAClaim implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("ET1", "ET1"),
        @CCD(displayOrder = 2)
        V002("ET1 Attachment", "ET1 Attachment"),
        @CCD(displayOrder = 3)
        V003("ACAS Certificate", "ACAS Certificate"),
        @CCD(displayOrder = 4)
        V004("Notice of claim", "Notice of claim"),
        @CCD(displayOrder = 5)
        V005("Claim accepted", "Claim accepted"),
        @CCD(displayOrder = 6)
        V006("Claim rejected", "Claim rejected"),
        @CCD(displayOrder = 7)
        V007("Claim part rejected", "Claim part rejected"),
        @CCD(displayOrder = 8)
        V008("ET1 Vetting", "ET1 Vetting");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List082FlStillWorking implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Working", "Working"),
        @CCD(displayOrder = 2)
        V002("Notice", "Notice"),
        @CCD(displayOrder = 3)
        V003("No longer working", "No longer working");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List083FlTitle implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Mr", "Mr"),
        @CCD(displayOrder = 2)
        V002("Mrs", "Mrs"),
        @CCD(displayOrder = 3)
        V003("Miss", "Miss"),
        @CCD(displayOrder = 4)
        V004("Ms", "Ms"),
        @CCD(displayOrder = 5)
        V005("Mx", "Mx"),
        @CCD(displayOrder = 6)
        V006("Dr", "Dr"),
        @CCD(displayOrder = 7)
        V007("Prof", "Prof"),
        @CCD(displayOrder = 8)
        V008("Sir", "Sir"),
        @CCD(displayOrder = 9)
        V009("Lord", "Lord"),
        @CCD(displayOrder = 10)
        V010("Lady", "Lady"),
        @CCD(displayOrder = 11)
        V011("Dame", "Dame"),
        @CCD(displayOrder = 12)
        V012("Capt", "Capt"),
        @CCD(displayOrder = 13)
        V013("Rev", "Rev"),
        @CCD(displayOrder = 14)
        V014("Other ", "Other "),
        @CCD(displayOrder = 15)
        V015("N/K", "N/K");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List084FlTopLevelV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("EM-TRB-LET-ENG-00544", "PART 0 – Print Address Labels"),
        @CCD(displayOrder = 2)
        V002("EM-TRB-EGW-ENG-00026", "PART 1 – Starting a claim: Rules 8 – 14"),
        @CCD(displayOrder = 3)
        V003("EM-TRB-EGW-ENG-00027", "PART 2 – The response to the claim: Rules 16-23"),
        @CCD(displayOrder = 4)
        V004("EM-TRB-EGW-ENG-00028", "PART 3 – Employer’s Contract Claims: Rules 23 - 25"),
        @CCD(displayOrder = 5)
        V005(
                "EM-TRB-EGW-ENG-00029",
                "PART 4 – Initial Consideration of Claim and Response (including Rule 22"
                        + " Judgments): Rules 27 - 29"),
        @CCD(displayOrder = 6)
        V006("EM-TRB-EGW-ENG-00030", "PART 5 – Case management orders & other powers: Rules 29-40"),
        @CCD(displayOrder = 7)
        V007("EM-TRB-EGW-ENG-00031", "PART 6 – Striking out & withdrawal: Rules 38 & 50-51"),
        @CCD(displayOrder = 8)
        V008(
                "EM-TRB-EGW-ENG-00032",
                "PART 7 – Preliminary hearings: Rules 39 and 53 – 56 (and Rules 41-49)"),
        @CCD(displayOrder = 9)
        V009(
                "EM-TRB-EGW-ENG-00065",
                "PART 8 - Notices of hearings: Rules 55 – 57 (and Rules 41-48, 72 -82)"),
        @CCD(displayOrder = 10)
        V010("EM-TRB-EGW-ENG-00033", "PART 9 –  Privacy and restrictions on disclosure: Rule 49 "),
        @CCD(displayOrder = 11)
        V011("EM-TRB-EGW-ENG-00034", "PART 10 – Decisions and Reasons: Rules 58–67"),
        @CCD(displayOrder = 12)
        V012("EM-TRB-EGW-ENG-00035", "PART 11 – Reconsideration of Judgments: Rules 70-73"),
        @CCD(displayOrder = 13)
        V013("EM-TRB-EGW-ENG-00036", "PART 12 – Costs & Preparation Time Orders: Rules 72 – 82"),
        @CCD(displayOrder = 14)
        V014("EM-TRB-EGW-ENG-00037", "PART 13 – General correspondence"),
        @CCD(displayOrder = 15)
        V015("EM-TRB-EGW-ENG-00038", "PART 14 – Insolvency and Dissolved companies"),
        @CCD(displayOrder = 16)
        V016("EM-TRB-EGW-ENG-00039", "PART 15 – Judicial Mediation"),
        @CCD(displayOrder = 17)
        V017("EM-TRB-EGW-ENG-00040", "PART 16 – Schedule 2 Procedure in Equal Value claims"),
        @CCD(displayOrder = 18)
        V018("EM-TRB-EGW-ENG-00041", "PART 17 – Appeals: Rules 104 - 106"),
        @CCD(displayOrder = 19)
        V019("EM-TRB-EGW-ENG-00066", "PART 18 – Recoupment"),
        @CCD(displayOrder = 20)
        V020("EM-TRB-EGW-ENG-00043", "PART 20 – Legal Officer Letters");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List084FlTopLevelV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("EM-TRB-LET-ENG-00544", "PART 0 – Print Address Labels"),
        @CCD(displayOrder = 2)
        V002("EM-TRB-SCO-ENG-00042", "PART 1  – Claims"),
        @CCD(displayOrder = 3)
        V003("EM-TRB-SCO-ENG-00043", "PART 2  – Responses"),
        @CCD(displayOrder = 4)
        V004("EM-TRB-SCO-ENG-00044", "PART 3  – Employer's contract claim"),
        @CCD(displayOrder = 5)
        V005("EM-TRB-SCO-ENG-00045", "PART 4  – Rule 22 Judgments"),
        @CCD(displayOrder = 6)
        V006("EM-TRB-SCO-ENG-00046", "PART 5  – Initial consideration Rules 26 – 29"),
        @CCD(displayOrder = 7)
        V007("EM-TRB-SCO-ENG-00047", "PART 6  – Orders"),
        @CCD(displayOrder = 8)
        V008("EM-TRB-SCO-ENG-00048", "PART 7  – Strike out / deposit orders"),
        @CCD(displayOrder = 9)
        V009("EM-TRB-SCO-ENG-00049", "PART 8  – Settlement / withdrawal"),
        @CCD(displayOrder = 10)
        V010("EM-TRB-SCO-ENG-00050", "PART 9  – Hearing notices"),
        @CCD(displayOrder = 11)
        V011(
                "EM-TRB-SCO-ENG-00051",
                "PART 10  – Judgments, decisions and notes, certificate of correction"),
        @CCD(displayOrder = 12)
        V012("EM-TRB-SCO-ENG-00052", "PART 11  – Reconsideration"),
        @CCD(displayOrder = 13)
        V013("EM-TRB-SCO-ENG-00053", "PART 12  – Expenses / preparation time / wasted costs"),
        @CCD(displayOrder = 14)
        V014("EM-TRB-SCO-ENG-00054", "PART 13  – Miscellaneous"),
        @CCD(displayOrder = 15)
        V015("EM-TRB-SCO-ENG-00055", "PART 14  – Mediation"),
        @CCD(displayOrder = 16)
        V016("EM-TRB-SCO-ENG-00056", "PART 15  – Special types of claim"),
        @CCD(displayOrder = 17)
        V017("EM-TRB-SCO-ENG-00057", "PART 16  – Legal Officer Letters");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List085FlTribunalOfficeV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Bristol", "Bristol"),
        @CCD(displayOrder = 2)
        V002("Leeds", "Leeds"),
        @CCD(displayOrder = 3)
        V003("London Central", "London Central"),
        @CCD(displayOrder = 4)
        V004("London East", "London East"),
        @CCD(displayOrder = 5)
        V005("London South", "London South"),
        @CCD(displayOrder = 6)
        V006("Manchester", "Manchester"),
        @CCD(displayOrder = 7)
        V007("Midlands East", "Midlands East"),
        @CCD(displayOrder = 8)
        V008("Midlands West", "Midlands West"),
        @CCD(displayOrder = 9)
        V009("Newcastle", "Newcastle"),
        @CCD(displayOrder = 10)
        V010("Wales", "Wales"),
        @CCD(displayOrder = 11)
        V011("Watford", "Watford"),
        @CCD(displayOrder = 12)
        V012("Unassigned", "Unassigned");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List085FlTribunalOfficeV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Glasgow", "Glasgow"),
        @CCD(displayOrder = 2)
        V002("Aberdeen", "Aberdeen"),
        @CCD(displayOrder = 3)
        V003("Dundee", "Dundee"),
        @CCD(displayOrder = 4)
        V004("Edinburgh", "Edinburgh"),
        @CCD(displayOrder = 5)
        V005("Unassigned", "Unassigned");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List086FlWithdrawalSettled implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Withdrawal of entire claim", "Withdrawal of entire claim"),
        @CCD(displayOrder = 2)
        V002("Withdrawal of part of claim", "Withdrawal of part of claim"),
        @CCD(displayOrder = 3)
        V003("Withdrawal of all or part of claim", "Withdrawal of all or part of claim"),
        @CCD(displayOrder = 4)
        V004("COT3", "COT3");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List087FlYesOrNoOrPreferNot implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002("No", "No"),
        @CCD(displayOrder = 3)
        V003("Prefer not to say", "Prefer not to say");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List088FlClaimantTseSelectApp implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Amend claim", "Amend claim"),
        @CCD(displayOrder = 2)
        V002("Change personal details", "Change personal details"),
        @CCD(displayOrder = 3)
        V003("Consider decision afresh", "Consider decision afresh"),
        @CCD(displayOrder = 4)
        V004("Contact the tribunal", "Contact the tribunal"),
        @CCD(displayOrder = 5)
        V005(
                "Order a witness to attend to give evidence",
                "Order a witness to attend to give evidence"),
        @CCD(displayOrder = 6)
        V006("Order other party", "Order other party"),
        @CCD(displayOrder = 7)
        V007("Postpone a hearing", "Postpone a hearing"),
        @CCD(displayOrder = 8)
        V008("Reconsider judgment", "Reconsider judgment"),
        @CCD(displayOrder = 9)
        V009("Respondent not complied", "Respondent not complied"),
        @CCD(displayOrder = 10)
        V010("Restrict publicity", "Restrict publicity"),
        @CCD(displayOrder = 11)
        V011("Strike out all or part of the response", "Strike out all or part of the response"),
        @CCD(displayOrder = 12)
        V012("Vary or revoke an order", "Vary or revoke an order"),
        @CCD(displayOrder = 13)
        V013("Withdraw all or part of claim", "Withdraw all or part of claim");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List089FlContestClaimStatus implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002("No", "No"),
        @CCD(displayOrder = 3)
        V003("Unknown - no answer given", "Unknown - no answer given");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List090FlCostsProBonoAwardedTo implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Access to Justice Foundation", "Access to Justice Foundation");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List091FlEmployerType implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Individual", "Individual"),
        @CCD(displayOrder = 2)
        V002("Limited company", "Limited company"),
        @CCD(displayOrder = 3)
        V003("Partnership", "Partnership"),
        @CCD(displayOrder = 4)
        V004("Unincorporated association", "Unincorporated association (such as a sports club)"),
        @CCD(displayOrder = 5)
        V005("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List092FlEt3Struckout implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Chairman's decision", "Judge's decision"),
        @CCD(displayOrder = 2)
        V002("Entered in error", "Entered in error");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List093FlEt3PayFrequency implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Weekly", "Weekly"),
        @CCD(displayOrder = 2)
        V002("Monthly", "Monthly"),
        @CCD(displayOrder = 3)
        V003("Annually", "Annually");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List094FlEt3SuggestedIssues implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Applications for strike out or deposit", "Applications for strike out or deposit"),
        @CCD(displayOrder = 2)
        V002("Interpreters", "Interpreters"),
        @CCD(displayOrder = 3)
        V003("Jurisdictional issues", "Jurisdictional issues"),
        @CCD(displayOrder = 4)
        V004("Request for adjustments", "Request for adjustments"),
        @CCD(displayOrder = 5)
        V005("Rule 50", "Rule 49"),
        @CCD(displayOrder = 6)
        V006("Time points", "Time points");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List095FlEt3TribunalLocationChange implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002("No - suggest another location", "No - suggest another location");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List096FlEtICUDLHearingFormatV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Video hearing", "Video hearing"),
        @CCD(displayOrder = 2)
        V002("Final F2F hearings (not Aberdeen)", "Final F2F hearings (not Aberdeen)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List096FlEtICUDLHearingFormatV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("CVP hearing", "CVP hearing"),
        @CCD(displayOrder = 2)
        V002("Final F2F hearings (not Aberdeen)", "Final F2F hearings (not Aberdeen)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List097FlHearingJudgeAloneOrWithMembers implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("JSA", "JSA"),
        @CCD(displayOrder = 2)
        V002("With members", "With members");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List098FlIsLocationCorrect implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002("No", "No - suggest another location");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List099FlIsTrackAllocationCorrect implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002("No", "No - suggest another track");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List100FlJurisdictionCodes implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("ADT", "ADT"),
        @CCD(displayOrder = 2)
        V002("ADG", "ADG"),
        @CCD(displayOrder = 3)
        V003("ADT(ST)", "ADT(ST)"),
        @CCD(displayOrder = 4)
        V004("APA", "APA"),
        @CCD(displayOrder = 5)
        V005("AWR", "AWR"),
        @CCD(displayOrder = 6)
        V006("BOC", "BOC"),
        @CCD(displayOrder = 7)
        V007("CCP", "CCP"),
        @CCD(displayOrder = 8)
        V008("COM", "COM"),
        @CCD(displayOrder = 9)
        V009("DAG", "DAG"),
        @CCD(displayOrder = 10)
        V010("DDA", "DDA"),
        @CCD(displayOrder = 11)
        V011("DOD", "DOD"),
        @CCD(displayOrder = 12)
        V012("DRB", "DRB"),
        @CCD(displayOrder = 13)
        V013("DSO", "DSO"),
        @CCD(displayOrder = 14)
        V014("EAP", "EAP"),
        @CCD(displayOrder = 15)
        V015("ECC", "ECC"),
        @CCD(displayOrder = 16)
        V016("EQP", "EQP"),
        @CCD(displayOrder = 17)
        V017("FCT", "FCT"),
        @CCD(displayOrder = 18)
        V018("FLW", "FLW"),
        @CCD(displayOrder = 19)
        V019("FML", "FML"),
        @CCD(displayOrder = 20)
        V020("FPA", "FPA"),
        @CCD(displayOrder = 21)
        V021("FPI", "FPI"),
        @CCD(displayOrder = 22)
        V022("FT1", "FT1"),
        @CCD(displayOrder = 23)
        V023("FTC", "FTC"),
        @CCD(displayOrder = 24)
        V024("FTE", "FTE"),
        @CCD(displayOrder = 25)
        V025("FTO", "FTO"),
        @CCD(displayOrder = 26)
        V026("FTP", "FTP"),
        @CCD(displayOrder = 27)
        V027("FTR", "FTR"),
        @CCD(displayOrder = 28)
        V028("FTS", "FTS"),
        @CCD(displayOrder = 29)
        V029("FTU", "FTU"),
        @CCD(displayOrder = 30)
        V030("FWP", "FWP"),
        @CCD(displayOrder = 31)
        V031("FWS", "FWS"),
        @CCD(displayOrder = 32)
        V032("GRA", "GRA"),
        @CCD(displayOrder = 33)
        V033("HAS", "HAS"),
        @CCD(displayOrder = 34)
        V034("HSD", "HSD"),
        @CCD(displayOrder = 35)
        V035("HSR", "HSR"),
        @CCD(displayOrder = 36)
        V036("IRF", "IRF"),
        @CCD(displayOrder = 37)
        V037("ISV", "ISV"),
        @CCD(displayOrder = 38)
        V038("LEV ", "LEV "),
        @CCD(displayOrder = 39)
        V039("LSO", "LSO"),
        @CCD(displayOrder = 40)
        V040("MAT", "MAT"),
        @CCD(displayOrder = 41)
        V041("MWA", "MWA"),
        @CCD(displayOrder = 42)
        V042("MWD", "MWD"),
        @CCD(displayOrder = 43)
        V043("NNA", "NNA"),
        @CCD(displayOrder = 44)
        V044("PAC", "PAC"),
        @CCD(displayOrder = 45)
        V045("PAY", "PAY"),
        @CCD(displayOrder = 46)
        V046("PEN", "PEN"),
        @CCD(displayOrder = 47)
        V047("PID", "PID"),
        @CCD(displayOrder = 48)
        V048("PLD", "PLD"),
        @CCD(displayOrder = 49)
        V049("PTE", "PTE"),
        @CCD(displayOrder = 50)
        V050("RPT", "RPT"),
        @CCD(displayOrder = 51)
        V051("RPT(S) ", "RPT(S) "),
        @CCD(displayOrder = 52)
        V052("RRD", "RRD"),
        @CCD(displayOrder = 53)
        V053("RTR", "RTR"),
        @CCD(displayOrder = 54)
        V054("RTR(ST)", "RTR(ST)"),
        @CCD(displayOrder = 55)
        V055("SUN", "SUN"),
        @CCD(displayOrder = 56)
        V056("SXD", "SXD"),
        @CCD(displayOrder = 57)
        V057("TBA", "TBA"),
        @CCD(displayOrder = 58)
        V058("TIP", "TIP"),
        @CCD(displayOrder = 59)
        V059("TPE", "TPE"),
        @CCD(displayOrder = 60)
        V060("TT", "TT"),
        @CCD(displayOrder = 61)
        V061("TUE", "TUE"),
        @CCD(displayOrder = 62)
        V062("TUI", "TUI"),
        @CCD(displayOrder = 63)
        V063("TUM", "TUM"),
        @CCD(displayOrder = 64)
        V064("TUR", "TUR"),
        @CCD(displayOrder = 65)
        V065("TUS", "TUS"),
        @CCD(displayOrder = 66)
        V066("TXC", "TXC"),
        @CCD(displayOrder = 67)
        V067("TXC(ST)", "TXC(ST)"),
        @CCD(displayOrder = 68)
        V068("UDC", "UDC"),
        @CCD(displayOrder = 69)
        V069("UDL", "UDL"),
        @CCD(displayOrder = 70)
        V070("UIA", "UIA"),
        @CCD(displayOrder = 71)
        V071("VIC", "VIC"),
        @CCD(displayOrder = 72)
        V072("WA", "WA"),
        @CCD(displayOrder = 73)
        V073("WTA", "WTA"),
        @CCD(displayOrder = 74)
        V074("WTR", "WTR"),
        @CCD(displayOrder = 75)
        V075("WTR(AL)", "WTR(AL)"),
        @CCD(displayOrder = 76)
        V076("ZNON", "ZNON");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List101FlLanguages implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("English", "English"),
        @CCD(displayOrder = 2)
        V002("Welsh", "Welsh");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List102FlLetterAddress implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Managing Office", "Managing Office"),
        @CCD(displayOrder = 2)
        V002("Allocated Office", "Allocated Office");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List103FlNoticePeriodUnit implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Weeks", "Weeks"),
        @CCD(displayOrder = 2)
        V002("Months", "Months");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List104FlPayCycle implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Weeks", "Weeks"),
        @CCD(displayOrder = 2)
        V002("Months", "Months"),
        @CCD(displayOrder = 3)
        V003("Annual", "Annual");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List105FlPensionContribution implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002("No", "No"),
        @CCD(displayOrder = 3)
        V003("Not Sure", "Not Sure");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List106FlProBonoAwardedAgainst implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Claimant's Representative", "Claimant's Representative"),
        @CCD(displayOrder = 2)
        V002("Respondent's Representative", "Respondent's Representative");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List107FlRepresentativeContactChangeOptions implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Amend contact details", "Amend contact details"),
        @CCD(displayOrder = 2)
        V002("Use MyHMCTS details", "Use MyHMCTS details");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List108FlResTseSelectApp implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Amend response", "Amend response"),
        @CCD(displayOrder = 2)
        V002("Change personal details", "Change personal details"),
        @CCD(displayOrder = 3)
        V003("Claimant not complied", "Claimant not complied"),
        @CCD(displayOrder = 4)
        V004("Consider a decision afresh", "Consider a decision afresh"),
        @CCD(displayOrder = 5)
        V005("Contact the tribunal", "Contact the tribunal"),
        @CCD(displayOrder = 6)
        V006("Order other party", "Order other party"),
        @CCD(displayOrder = 7)
        V007(
                "Order a witness to attend to give evidence",
                "Order a witness to attend to give evidence"),
        @CCD(displayOrder = 8)
        V008("Postpone a hearing", "Postpone a hearing"),
        @CCD(displayOrder = 9)
        V009("Reconsider judgement", "Reconsider judgment"),
        @CCD(displayOrder = 10)
        V010("Restrict publicity", "Restrict publicity"),
        @CCD(displayOrder = 11)
        V011("Strike out all or part of a claim", "Strike out all or part of a claim"),
        @CCD(displayOrder = 12)
        V012("Vary or revoke an order", "Vary or revoke an order");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List109FlRespondentLegalEntity implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002("No", "No"),
        @CCD(displayOrder = 3)
        V003("Don't know", "Don't know");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List110FlRule2728ClaimToBe implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Dismissed in full", "Dismissed in full"),
        @CCD(displayOrder = 2)
        V002("Dismissed in part", "Dismissed in part");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List111FlSelectHearingBundlesCollection implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("selectClaimantHearingBundles", "Claimant's Hearing Bundles"),
        @CCD(displayOrder = 2)
        V002("selectRespondentHearingBundles", "Respondent's Hearing Bundles");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List112FlSendNotificationCaseManagement implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Case management order", "Case management order"),
        @CCD(displayOrder = 2)
        V002("Request", "Request");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List113FlSendNotificationDecision implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Granted", "Granted"),
        @CCD(displayOrder = 2)
        V002("Granted in part", "Granted in part"),
        @CCD(displayOrder = 3)
        V003("Refused", "Refused"),
        @CCD(displayOrder = 4)
        V004("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List114FlSendNotificationParties implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Both parties", "Both parties"),
        @CCD(displayOrder = 2)
        V002("Claimant only", "Claimant only"),
        @CCD(displayOrder = 3)
        V003("Respondent only", "Respondent only");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List115FlSendNotificationSubject implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Claimant / Respondent details", "Claimant / Respondent details"),
        @CCD(displayOrder = 2)
        V002("Claim (ET1)", "Claim (ET1)"),
        @CCD(displayOrder = 3)
        V003("Response (ET3)", "Response (ET3)"),
        @CCD(displayOrder = 4)
        V004("Hearing", "Hearing"),
        @CCD(displayOrder = 5)
        V005("Case management orders / requests", "Case management orders / requests"),
        @CCD(displayOrder = 6)
        V006("Judgment", "Judgment"),
        @CCD(displayOrder = 7)
        V007("Employer Contract Claim", "Employer Contract Claim"),
        @CCD(displayOrder = 8)
        V008("Other (General correspondence)", "Other (General correspondence)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List116FlSendNotificationWhoCaseOrder implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Legal officer", "Legal officer"),
        @CCD(displayOrder = 2)
        V002("Judge", "Judge");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List117FlSendNotificationWhoMadeJudgement implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Judge", "Judge"),
        @CCD(displayOrder = 2)
        V002("Legal officer", "Legal officer");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List118FlSuggestAnotherTrack implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Open", "Open"),
        @CCD(displayOrder = 2)
        V002("Short", "Short"),
        @CCD(displayOrder = 3)
        V003("Standard", "Standard"),
        @CCD(displayOrder = 4)
        V004("No track", "No track");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List119FlTseAdmReplyCmoMadeBy implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Legal officer", "Legal officer"),
        @CCD(displayOrder = 2)
        V002("Judge", "Judge");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List120FlTseAdmReplyRequestMadeBy implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Legal officer", "Legal officer"),
        @CCD(displayOrder = 2)
        V002("Judge", "Judge"),
        @CCD(displayOrder = 3)
        V003("Case worker", "Case worker");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List121FrlACAS implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "Another person",
                "Another Person I'm making the claim with has an early ACAS concilication"
                        + " certificate number"),
        @CCD(displayOrder = 2)
        V002("No Power", "ACAS doesn't have the power to conciliate on all or part of my claim"),
        @CCD(displayOrder = 3)
        V003("Employer already in touch", "My employer has already been in touch with ACAS"),
        @CCD(displayOrder = 4)
        V004(
                "Unfair Dismissal",
                "My claim consists only of a claim of unfair dismissal which contains an"
                        + " application for interim relief"),
        @CCD(displayOrder = 5)
        V005("ECC", "Claim type is Employer Contract Claim");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List122FrlClaimantCopyToOtherPartyYesOrNo implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "Yes",
                "Yes I confirm I will copy this correspondence to the other party to satisfy the"
                    + " Employment Tribunal Rules of Procedure. I will also notify the other party"
                    + " that any objections to the application should be sent to the tribunal as"
                    + " soon as possible and in any event within 7 days."),
        @CCD(displayOrder = 2)
        V002(
                "No",
                "No, I do not want to copy this correspondence to the other party. You must tell"
                    + " the tribunal why you do not want to inform the other party. The tribunal"
                    + " will consider your reasons and decide if you must inform the other party or"
                    + " not.");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List123FrlClaimantOrRespondents implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Claimant", "Claimant"),
        @CCD(displayOrder = 2)
        V002("Respondent", "Respondent(s)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List124FrlClaimantTseCopyToOtherPartyYesOrNo implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "Yes",
                "Yes I confirm I will copy this correspondence to the other party to satisfy the"
                    + " Employment Tribunal Rules of Procedure. I will also notify the other party"
                    + " that any objections to the application should be sent to the tribunal as"
                    + " soon as possible and in any event within 7 days."),
        @CCD(displayOrder = 2)
        V002(
                "No",
                "No, I do not want to copy this correspondence to the other party. You must tell"
                    + " the tribunal why you do not want to inform the other party. The tribunal"
                    + " will consider your reasons and decide if you must inform the other party or"
                    + " not.");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List125FrlClaimantType implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Individual", "Individual"),
        @CCD(displayOrder = 2)
        V002("Company", "Company");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List126FrlCopyToOtherPartyYesOrNo implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "Yes",
                "Yes, I confirm I want to copy this correspondence to the other party to satisfy"
                    + " the Employment Tribunal Rules of Procedure. By selecting this option, a"
                    + " copy of this correspondence will be sent to the other party upon"
                    + " submission. The other party will be notified that any objections to your"
                    + " application should be sent to the tribunal as soon as possible and in any"
                    + " event within 7 days."),
        @CCD(displayOrder = 2)
        V002(
                "No",
                "No, I do not want to copy this correspondence to the other party. You must tell"
                    + " the tribunal why you do not want to inform the other party. The tribunal"
                    + " will consider your reasons and decide if you must inform the other party or"
                    + " not.");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List127FrlReferCaseTo implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Admin", "Admin"),
        @CCD(displayOrder = 2)
        V002("Judge", "Judge"),
        @CCD(displayOrder = 3)
        V003("Legal officer", "Legal officer");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List128FrlSitAlone implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Sit Alone", "Sit Alone"),
        @CCD(displayOrder = 2)
        V002("Full Panel", "Full Panel"),
        @CCD(displayOrder = 3)
        V003("Two Judges", "Two Judges");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List129FrlBundleType implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Hearing Bundle", "Hearing Bundle"),
        @CCD(displayOrder = 2)
        V002("Witness Statements", "Witness Statements"),
        @CCD(displayOrder = 3)
        V003("Case management agenda", "Case management agenda"),
        @CCD(displayOrder = 4)
        V004("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List130FrlBundlesRespondentAgreedDocWith implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002(
                "But",
                "We have agreed that this set of documents will be uploaded but we disagree about"
                        + " whether some of the documents should be referred to at the hearing."),
        @CCD(displayOrder = 3)
        V003("No", "No, we have not agreed and I want to provide my own documents");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List131FrlBundlesWhatDocuments implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "Supplementary hearing documents",
                "Documents that will be referred to by witnesses when giving evidence, sometimes"
                        + " called the \"hearing bundle\""),
        @CCD(displayOrder = 2)
        V002(
                "Witness statements only",
                "Witness statements (only upload these if the tribunal has said witness statements"
                        + " are to be used)"),
        @CCD(displayOrder = 3)
        V003(
                "Hearing documents, including witness statements",
                "A combined hearing bundles and witness statements");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List132FrlBundlesWhoseDocuments implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Respondent's documents only", "Respondent's documents only"),
        @CCD(displayOrder = 2)
        V002(
                "Both parties' hearing documents combined",
                "Both parties' hearing documents combined");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List133FrlEditOrDelete implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Edit", "Edit"),
        @CCD(displayOrder = 2)
        V002("Delete", "Delete");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List134FrlEt3ContactPreference implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Email", "Email"),
        @CCD(displayOrder = 2)
        V002("Post", "Post");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List135FrlEt3ContestClaim implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes - the respondent contests all or part of the claim"),
        @CCD(displayOrder = 2)
        V002("No", "No - the respondent does not contest any of the claim");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List136FrlEt3YesNoNotSureYet implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002("No", "No"),
        @CCD(displayOrder = 3)
        V003("not sure", "I'm not sure yet");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List137FrlEtICRule27ClaimToBe implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Dismissed in full", "Dismissed in full"),
        @CCD(displayOrder = 2)
        V002("Dismissed in part", "Dismissed in part");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List138FrlEtICRule28ClaimToBe implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Dismissed in full", "Dismissed in full"),
        @CCD(displayOrder = 2)
        V002("Dismissed in part", "Dismissed in part");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List139FrlFinalHearingListedJudgeOrMembers implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("JSA", "JSA"),
        @CCD(displayOrder = 2)
        V002("With members", "With members");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List140FrlFutureOrPastHearing implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Past", "Past"),
        @CCD(displayOrder = 2)
        V002("Future", "Future");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List141FrlListedCmPreliminaryHearingJsa implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Case management only", "Case management only"),
        @CCD(displayOrder = 2)
        V002("Already decided", "Already decided"),
        @CCD(displayOrder = 3)
        V003("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List142FrlNoAcasReason implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Another person", "Another person making the claim has an Acas certificate number"),
        @CCD(displayOrder = 2)
        V002("No Power", "Acas doesn’t have the power to conciliate on some or all of this claim"),
        @CCD(displayOrder = 3)
        V003("Employer already in touch", "The respondent has already been in touch with Acas"),
        @CCD(displayOrder = 4)
        V004(
                "Unfair Dismissal",
                "The claim consists only of a complaint of unfair dismissal which contains an"
                        + " application for interim relief");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List143FrlPartyUnavailability implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("claimant", "Claimant"),
        @CCD(displayOrder = 2)
        V002("respondent", "Respondent");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List144FrlRespondNotificationCmoRequestBy implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Legal officer", "Legal officer"),
        @CCD(displayOrder = 2)
        V002("Judge", "Judge");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List145FrlRespondNotificationRequestBy implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Legal officer", "Legal officer"),
        @CCD(displayOrder = 2)
        V002("Judge", "Judge"),
        @CCD(displayOrder = 3)
        V003("Case worker", "Case worker");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List146FrlRespondentType implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Organisation", "Organisation"),
        @CCD(displayOrder = 2)
        V002("Individual", "Individual");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List147FrlSendNotificationRequestMadeBy implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Legal officer", "Legal officer"),
        @CCD(displayOrder = 2)
        V002("Judge", "Judge"),
        @CCD(displayOrder = 3)
        V003("Caseworker", "Caseworker");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List148FrlTseAdmReplyIsCmoOrRequest implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Case management order", "Case management order"),
        @CCD(displayOrder = 2)
        V002("Request", "Request"),
        @CCD(displayOrder = 3)
        V003("Neither", "Neither");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List149FrlTseAdminDecision implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Granted", "Granted"),
        @CCD(displayOrder = 2)
        V002("Granted in part", "Granted in part"),
        @CCD(displayOrder = 3)
        V003("Refused", "Refused"),
        @CCD(displayOrder = 4)
        V004("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List150FrlTseAdminDecisionMadeBy implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Legal officer", "Legal officer"),
        @CCD(displayOrder = 2)
        V002("Judge", "Judge");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List151FrlTseAdminIsResponseRequired implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes - view document for details"),
        @CCD(displayOrder = 2)
        V002("No", "No");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List152FrlTseAdminSelectPartyNotify implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Both parties", "Both parties"),
        @CCD(displayOrder = 2)
        V002("Claimant only", "Claimant only"),
        @CCD(displayOrder = 3)
        V003("Respondent only", "Respondent only");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List153FrlTseAdminTypeOfDecision implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Judgment", "Judgment"),
        @CCD(displayOrder = 2)
        V002("Case management order", "Case management order");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List154FrlTseApplicationsOpenOrClosed implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Open", "Open"),
        @CCD(displayOrder = 2)
        V002("Closed", "Closed");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List155FrlYesNoNotApplicable implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002("No", "No"),
        @CCD(displayOrder = 3)
        V003("Not applicable", "Not applicable");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List156ImageRendering implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("opaque", "Opaque"),
        @CCD(displayOrder = 2)
        V002("translucent", "Translucent");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List157ImageRenderingLocation implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("allPages", "Image on all pages of the Document"),
        @CCD(displayOrder = 2)
        V002("firstPage", "Image on the First Page of each document");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List158MsFinalHearingIsEJSitAloneReasonNo implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "Members experience is likely to add significant value to the adjudication process",
                "Members experience is likely to add significant value to the adjudication"
                        + " process"),
        @CCD(displayOrder = 2)
        V002("No views expressed by parties", "No views expressed by parties"),
        @CCD(displayOrder = 3)
        V003("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List159MsFinalHearingIsEJSitAloneReasonYes implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "Members experience is not likely to add significant value to the adjudication"
                        + " process",
                "Members experience is not likely to add significant value to the adjudication"
                        + " process"),
        @CCD(displayOrder = 2)
        V002("No views expressed by parties", "No views expressed by parties"),
        @CCD(displayOrder = 3)
        V003("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List160MslDefectsV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("rule121a", "The tribunal has no jurisdiction to consider - Rule 13(1)(a)"),
        @CCD(displayOrder = 2)
        V002(
                "rule121b",
                "Is in a form which cannot sensibly be responded to or otherwise an abuse of"
                        + " process - Rule 13(1)(b)"),
        @CCD(displayOrder = 3)
        V003(
                "rule121c",
                "Has neither an EC number nor claims one of the EC exemptions - Rule 13(1)(c)"),
        @CCD(displayOrder = 4)
        V004(
                "rule121d",
                "States that one of the EC exceptions applies but it might not - Rule 13(1)(d)"),
        @CCD(displayOrder = 5)
        V005(
                "rule121 da",
                "Institutes relevant proceedings and the EC number on the claim form does not match"
                        + " the EC number on the Acas certificate - Rule 13(1)(e)"),
        @CCD(displayOrder = 6)
        V006(
                "rule121e",
                "Has a different claimant name on the ET1 to the claimant name on the Acas"
                        + " certificate - Rule 13(1)(f)"),
        @CCD(displayOrder = 7)
        V007(
                "rule121f",
                "Has a different respondent name on the ET1 to the respondent name on the Acas"
                        + " certificate - Rule 13(1)(g)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List160MslDefectsV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("rule121a", "The tribunal has no jurisdiction to consider - Rule 13 (1)(a)"),
        @CCD(displayOrder = 2)
        V002(
                "rule121b",
                "Is in a form which cannot sensibly be responded to or otherwise an abuse of"
                        + " process - Rule 13 (1)(b)"),
        @CCD(displayOrder = 3)
        V003(
                "rule121c",
                "Has neither an EC number nor claims one of the EC exemptions - Rule 13 (1)(c)"),
        @CCD(displayOrder = 4)
        V004(
                "rule121d",
                "States that one of the EC exceptions applies but it might not - Rule 13 (1)(d)"),
        @CCD(displayOrder = 5)
        V005(
                "rule121 da",
                "Institutes relevant proceedings and the EC number on the claim form does not match"
                        + " the EC number on the Acas certificate - Rule 13 (1)(e)"),
        @CCD(displayOrder = 6)
        V006(
                "rule121e",
                "Has a different claimant name on the ET1 to the claimant name on the Acas"
                        + " certificate - Rule 13 (1)(f)"),
        @CCD(displayOrder = 7)
        V007(
                "rule121f",
                "Has a different respondent name on the ET1 to the respondent name on the Acas"
                        + " certificate - Rule 13 (1)(g)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List161MslHearingAttendence implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Phone", "Phone hearings"),
        @CCD(displayOrder = 2)
        V002("Video", "Video hearings"),
        @CCD(displayOrder = 3)
        V003("In person", "In person hearings");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List162MslHearingFormat implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("In person", "In person"),
        @CCD(displayOrder = 2)
        V002("Telephone", "Telephone"),
        @CCD(displayOrder = 3)
        V003("Video", "Video"),
        @CCD(displayOrder = 4)
        V004("Hybrid", "Hybrid");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List163MslHearingPreferences implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Video", "Video"),
        @CCD(displayOrder = 2)
        V002("Phone", "Phone"),
        @CCD(displayOrder = 3)
        V003("Neither", "Neither");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List164MslIcF2FOrders implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("1 Disclosure", "1 Documents"),
        @CCD(displayOrder = 2)
        V002("2 Productions", "2 Productions"),
        @CCD(displayOrder = 3)
        V003("3 Schedule of loss", "3 Schedule of loss"),
        @CCD(displayOrder = 4)
        V004("4 Mitigation", "4 Mitigation"),
        @CCD(displayOrder = 5)
        V005("5 Updated schedule of loss", "5 Updated schedule of loss");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List165MslIcVideoOrders implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("1 Disclosure and Productions", "1 Disclosure and Productions"),
        @CCD(displayOrder = 2)
        V002(
                "2 Schedule of loss, mitigation & updated schedule of loss",
                "2 Schedule of loss, mitigation & updated schedule of loss"),
        @CCD(displayOrder = 3)
        V003("3 Witness statements", "3 Witness statements");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List166MslNoticePeriod implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Months", "Yes, in months"),
        @CCD(displayOrder = 2)
        V002("Weeks", "Yes, in weeks"),
        @CCD(displayOrder = 3)
        V003("No", "No");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List167MslNoticePeriodLength implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Months", "Notice period was in months"),
        @CCD(displayOrder = 2)
        V002("Weeks", "Notice period was in weeks");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List168MslPayFrequency implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Weekly", "Weekly"),
        @CCD(displayOrder = 2)
        V002("Monthly", "Monthly"),
        @CCD(displayOrder = 3)
        V003("Annual", "Annual"),
        @CCD(displayOrder = 4)
        V004("Not sure", "Not sure");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List169MslPreAcceptanceResponseV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Not on Prescribed Form", "Not on Prescribed Form"),
        @CCD(displayOrder = 2)
        V002("Required Info. Absent", "Required Info. Absent"),
        @CCD(displayOrder = 3)
        V003("No Jurisdiction", "No Jurisdiction"),
        @CCD(displayOrder = 4)
        V004(
                "Admissibility  (Not applicable to dismissal only claims)",
                "Admissibility  (Not applicable to dismissal only claims)"),
        @CCD(displayOrder = 5)
        V005("Defect", "Defect");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List169MslPreAcceptanceResponseV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Not on Prescribed Form", "Not on Prescribed Form"),
        @CCD(displayOrder = 2)
        V002("Required Info. Absent", "Required Info. Absent"),
        @CCD(displayOrder = 3)
        V003("No Jurisdiction", "No Jurisdiction"),
        @CCD(displayOrder = 4)
        V004("/", "Admissibility"),
        @CCD(displayOrder = 5)
        V005("Defect", "Defect");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List170MslREJOrVP implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("vexatiousLitigantOrder", "A claimant covered by vexatious litigant order"),
        @CCD(displayOrder = 2)
        V002("aNationalSecurityIssue", "A national security issue"),
        @CCD(displayOrder = 3)
        V003(
                "nationalMultipleOrPresidentialOrder",
                "A part of national multiple / covered by Presidential case management order"),
        @CCD(displayOrder = 4)
        V004("transferToOtherRegion", "A request for transfer to another ET region"),
        @CCD(displayOrder = 5)
        V005("serviceAbroad", "A request for service abroad"),
        @CCD(displayOrder = 6)
        V006(
                "aSensitiveIssue",
                "A sensitive issue which may attract publicity or need early allocation to a"
                        + " specific judge"),
        @CCD(displayOrder = 7)
        V007(
                "anyPotentialConflict",
                "Any potential conflict involving judge, non-legal member or HMCTS staff member"),
        @CCD(displayOrder = 8)
        V008(
                "anotherReasonREJOrVP",
                "Another reason for Regional Employment Judge / Vice-President referral");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List171MslResponse implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Out of Time", "Out of Time"),
        @CCD(displayOrder = 2)
        V002("Not on Prescribed Form", "Not on Prescribed Form"),
        @CCD(displayOrder = 3)
        V003("Required Info. Absent", "Required Info. Absent"),
        @CCD(displayOrder = 4)
        V004("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List172MslStillWorking implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Working", "Yes, they are still working for the respondent "),
        @CCD(displayOrder = 2)
        V002("Notice", "Yes, they are working a notice period for the respondent "),
        @CCD(displayOrder = 3)
        V003("No longer working", "No, they are no longer working for the respondent");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List173MslWorkPayNoticePeriod implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Worked", "Yes, the claimant worked a notice period"),
        @CCD(displayOrder = 2)
        V002("Paid", "Yes, the claimant got paid a notice period");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List174MslYes implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List175MslYesNo implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V002("No", "No");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List176MslClaimOutcomes implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("compensation", "Compensation only"),
        @CCD(displayOrder = 2)
        V002("tribunal", "If claiming discrimination, a tribunal recommendation"),
        @CCD(displayOrder = 3)
        V003("oldJob", "If claiming unfair dismissal, to get your old job back and compensation"),
        @CCD(displayOrder = 4)
        V004(
                "anotherJob",
                "If claiming unfair dismissal, to get another job with the same employer or"
                        + " associated employer and compensation(re-engagement)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List177MslCloseApplicationYes implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List178MslConfirmCloseReferral implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List179MslConfirmSubmitEt3 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes - I want to submit this ET3");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List180MslDiscriminationClaims implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Age", "Age"),
        @CCD(displayOrder = 2)
        V002("Disability", "Disability"),
        @CCD(displayOrder = 3)
        V003("Ethnicity", "Ethnicity"),
        @CCD(displayOrder = 4)
        V004("Gender reassignment", "Gender reassignment"),
        @CCD(displayOrder = 5)
        V005("Marriage or civil partnership", "Marriage or civil partnership"),
        @CCD(displayOrder = 6)
        V006("Pregnancy or maternity", "Pregnancy or maternity"),
        @CCD(displayOrder = 7)
        V007("Race", "Race (including colour, nationality, and ethnic or national origins"),
        @CCD(displayOrder = 8)
        V008("Religion or belief", "Religion or belief"),
        @CCD(displayOrder = 9)
        V009("Sex", "Sex (including equal pay)"),
        @CCD(displayOrder = 10)
        V010("Sexual orientation", "Sexual orientation");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List181MslEt1DiscriminationClaims implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Age", "Age"),
        @CCD(displayOrder = 2)
        V002("Disability", "Disability"),
        @CCD(displayOrder = 4)
        V003("Gender reassignment", "Gender reassignment"),
        @CCD(displayOrder = 5)
        V004("Marriage or civil partnership", "Marriage or civil partnership"),
        @CCD(displayOrder = 6)
        V005("Pregnancy or maternity", "Pregnancy or maternity"),
        @CCD(displayOrder = 7)
        V006("Race", "Race (including colour, nationality, and ethnic or national origins)"),
        @CCD(displayOrder = 8)
        V007("Religion or belief", "Religion or belief"),
        @CCD(displayOrder = 9)
        V008("Sex", "Sex (including equal pay)"),
        @CCD(displayOrder = 10)
        V009("Sexual orientation", "Sexual orientation");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List182MslEt1TypesOfClaim implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("discrimination", "Discrimination of any type"),
        @CCD(displayOrder = 2)
        V002("payRelated", "Pay related claim"),
        @CCD(displayOrder = 3)
        V003("unfairDismissal", "Unfair dismissal, including constructive dismissal"),
        @CCD(displayOrder = 4)
        V004("whistleBlowing", "Whistleblowing"),
        @CCD(displayOrder = 5)
        V005("otherTypesOfClaims", "Other type of claim");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List183MslEt3HearingType implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Phone hearings", "Phone hearings"),
        @CCD(displayOrder = 2)
        V002("Video hearings", "Video hearings");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List184MslEtICFurtherInformation implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("furtherInformationRequired", "Further information required"),
        @CCD(displayOrder = 2)
        V002("issueRule27", "Issue Rule 28 Notice and order"),
        @CCD(displayOrder = 3)
        V003("issueRule28", "Issue Rule 29 Notice and order");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List185MslEtICHearingAlreadyListedV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Proceed to the hearing already listed", "Proceed to the hearing already listed"),
        @CCD(displayOrder = 2)
        V002("Postpone hearing", "Postpone hearing"),
        @CCD(displayOrder = 3)
        V003("Extend duration of hearing", "Extend duration of hearing"),
        @CCD(displayOrder = 4)
        V004(
                "Convert final hearing to preliminary hearing",
                "Convert final hearing to preliminary hearing"),
        @CCD(displayOrder = 5)
        V005("Convert to F2F hearing", "Convert to F2F hearing"),
        @CCD(displayOrder = 6)
        V006("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List185MslEtICHearingAlreadyListedV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("proceedToHearing", "Proceed to the hearing already listed"),
        @CCD(displayOrder = 2)
        V002("postponeHearing", "Postpone hearing"),
        @CCD(displayOrder = 3)
        V003("extendHearingDuration", "Extend duration of hearing"),
        @CCD(displayOrder = 4)
        V004("convertFinalToPreliminaryHearing", "Convert final hearing to preliminary hearing"),
        @CCD(displayOrder = 5)
        V005("convertToF2FHearing", "Convert to F2F hearing"),
        @CCD(displayOrder = 6)
        V006("other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List186MslEtICHearingNotListedV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Seek comments on the video hearing", "Seek comments on the video hearing"),
        @CCD(displayOrder = 2)
        V002("List for preliminary hearing", "List for preliminary hearing"),
        @CCD(displayOrder = 3)
        V003("List for final hearing", "List for final hearing"),
        @CCD(displayOrder = 4)
        V004("UDL hearing", "UDL hearing");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List186MslEtICHearingNotListedV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Seek comments on CVP hearing", "Seek comments on CVP hearing"),
        @CCD(displayOrder = 2)
        V002("List for preliminary hearing", "List for preliminary hearing"),
        @CCD(displayOrder = 3)
        V003("List for final hearing", "List for final hearing"),
        @CCD(displayOrder = 4)
        V004("UDL hearing", "UDL hearing");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List187MslEtICHearingNotListedUpdated implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("List for preliminary hearing", "List for preliminary hearing"),
        @CCD(displayOrder = 2)
        V002("List for final hearing", "List for final hearing"),
        @CCD(displayOrder = 3)
        V003(
                "Do not list at present (give other directions below)",
                "Do not list at present (give other directions below)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List188MslEtICHearingNotListedV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("List for preliminary hearing", "List for preliminary hearing"),
        @CCD(displayOrder = 2)
        V002("List for final hearing", "List for final hearing"),
        @CCD(displayOrder = 3)
        V003(
                "Do not list at present (give other directions below)",
                "Do not list at present (give other directions below)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List189MslEtICPurposeOfPrelimHearing implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Case management", "Case management"),
        @CCD(displayOrder = 2)
        V002("Preliminary issue", "Preliminary issue"),
        @CCD(displayOrder = 3)
        V003("Strike out", "Strike out"),
        @CCD(displayOrder = 4)
        V004("Deposit", "Deposit");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List190MslEtICPurposeOfPrelimHearingV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Case management", "Case management"),
        @CCD(displayOrder = 2)
        V002("Preliminary issue", "Preliminary issue"),
        @CCD(displayOrder = 3)
        V003("Strike out", "Strike out"),
        @CCD(displayOrder = 4)
        V004("Deposit", "Deposit");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List191MslEtICRule27Direction implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("noJurisdiction", "No jurisdiction"),
        @CCD(displayOrder = 2)
        V002("noReasonableProspectOfSuccess", "No reasonable prospect of success");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List192MslEtICTypeOfCvpHearing implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Final", "Final"),
        @CCD(displayOrder = 2)
        V002("Preliminary", "Preliminary");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List193MslEtICTypeOfHearingV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Telephone", "Telephone"),
        @CCD(displayOrder = 2)
        V002("Video", "Video"),
        @CCD(displayOrder = 3)
        V003("F2F", "F2F");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List193MslEtICTypeOfHearingV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Telephone", "Telephone"),
        @CCD(displayOrder = 2)
        V002("CVP", "CVP"),
        @CCD(displayOrder = 3)
        V003("F2F", "F2F");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List194MslEtICTypeOfHearingUpdated implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Video", "Video"),
        @CCD(displayOrder = 2)
        V002("F2F", "F2F");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List195MslEtICTypeOfHearingV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Video", "Video"),
        @CCD(displayOrder = 2)
        V002("F2F", "F2F");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List196MslEtICUDLCVPIssue implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("1 Disclosure and Productions", "1 Disclosure and Productions"),
        @CCD(displayOrder = 2)
        V002(
                "2 Schedule of loss, mitigation & updated schedule of loss",
                "2 Schedule of loss, mitigation & updated schedule of loss"),
        @CCD(displayOrder = 3)
        V003("3 Witness statements", "3 Witness statements");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List197MslEtICUDLFinalF2FIssueV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("1 Disclosure", "1 Disclosure"),
        @CCD(displayOrder = 2)
        V002("2 Bundles", "2 Bundles"),
        @CCD(displayOrder = 3)
        V003("3 Schedule of loss", "3 Schedule of loss"),
        @CCD(displayOrder = 4)
        V004("4 Mitigation", "4 Mitigation"),
        @CCD(displayOrder = 5)
        V005("5 Updated schedule of loss", "5 Updated schedule of loss");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List197MslEtICUDLFinalF2FIssueV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("1 Disclosure", "1 Disclosure"),
        @CCD(displayOrder = 2)
        V002("2 Productions", "2 Productions"),
        @CCD(displayOrder = 3)
        V003("3 Schedule of loss", "3 Schedule of loss"),
        @CCD(displayOrder = 4)
        V004("4 Mitigation", "4 Mitigation"),
        @CCD(displayOrder = 5)
        V005("5 Updated schedule of loss", "5 Updated schedule of loss");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List198MslEtICUDLGiveReasons implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001(
                "Likelihood of dispute on facts makes full tribunal desirable",
                "Likelihood of dispute on facts makes full tribunal desirable"),
        @CCD(displayOrder = 2)
        V002("Little or no agreement on facts", "Little or no agreement on facts"),
        @CCD(displayOrder = 3)
        V003(
                "Likelihood of issue of law arising makes EJSA desirable",
                "Likelihood of issue of law arising makes EJSA desirable"),
        @CCD(displayOrder = 4)
        V004("Views of parties", "Views of parties"),
        @CCD(displayOrder = 5)
        V005("No views expressed by parties", "No views expressed by parties"),
        @CCD(displayOrder = 6)
        V006("Concurrent proceedings", "Concurrent proceedings"),
        @CCD(displayOrder = 7)
        V007("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List199MslFinalHearingWithJudgeOrMembersReasonsJsa implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Already decided", "Already decided"),
        @CCD(displayOrder = 2)
        V002(
                "Members' experience is not likely to add significant value to the adjudication"
                        + " process",
                "Members' experience is not likely to add significant value to the adjudication"
                        + " process"),
        @CCD(displayOrder = 3)
        V003("No views expressed by parties", "No views expressed by parties"),
        @CCD(displayOrder = 4)
        V004("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List200MslFinalHearingWithJudgeOrMembersReasonsMembers
            implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Already decided", "Already decided"),
        @CCD(displayOrder = 2)
        V002(
                "Members' experience is likely to add significant value to the adjudication"
                        + " process",
                "Members' experience is likely to add significant value to the adjudication"
                        + " process"),
        @CCD(displayOrder = 3)
        V003("No views expressed by parties", "No views expressed by parties"),
        @CCD(displayOrder = 4)
        V004("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List201MslFurtherInformation implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Further information required", "Further information required"),
        @CCD(displayOrder = 2)
        V002("Issue Rule 27 Notice and order", "Issue Rule 28 Notice and order"),
        @CCD(displayOrder = 3)
        V003("Issue Rule 28 Notice and order", "Issue Rule 29 Notice and order");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List202MslHearingWithJudgeOrMembersReasonsV1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Already decided", "Already decided"),
        @CCD(displayOrder = 2)
        V002(
                "Members' experience is likely to add significant value to the adjudication"
                        + " process",
                "Members' experience is likely to add significant value to the adjudication"
                        + " process"),
        @CCD(displayOrder = 3)
        V003(
                "Members' experience is not likely to add significant value to the adjudication"
                        + " process",
                "Members' experience is not likely to add significant value to the adjudication"
                        + " process"),
        @CCD(displayOrder = 4)
        V004("No views expressed by parties", "No views expressed by parties"),
        @CCD(displayOrder = 5)
        V005("Other", "Other"),
        @CCD(displayOrder = 1)
        V006("Already decided", "Already decided"),
        @CCD(displayOrder = 2)
        V007(
                "Members' experience is likely to add significant value to the process of"
                        + " adjudication",
                "Members' experience is likely to add significant value to the process of"
                        + " adjudication"),
        @CCD(displayOrder = 3)
        V008(
                "Members' experience is not likely to add significant value to the process of"
                        + " adjudication",
                "Members' experience is not likely to add significant value to the process of"
                        + " adjudication"),
        @CCD(displayOrder = 4)
        V009("No views expressed by parties", "No views expressed by parties"),
        @CCD(displayOrder = 5)
        V010("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List202MslHearingWithJudgeOrMembersReasonsV2 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Already decided", "Already decided"),
        @CCD(displayOrder = 2)
        V002(
                "Members' experience is likely to add significant value to the adjudication"
                        + " process",
                "Members' experience is likely to add significant value to the adjudication"
                        + " process"),
        @CCD(displayOrder = 3)
        V003(
                "Members' experience is not likely to add significant value to the adjudication"
                        + " process",
                "Members' experience is not likely to add significant value to the adjudication"
                        + " process"),
        @CCD(displayOrder = 4)
        V004("No views expressed by parties", "No views expressed by parties"),
        @CCD(displayOrder = 5)
        V005("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List203MslJudgeOrLO implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("aClaimOfInterimRelief", "A claim of interim relief"),
        @CCD(displayOrder = 2)
        V002("aStatutoryAppeal", "A statutory appeal"),
        @CCD(displayOrder = 3)
        V003(
                "anAllegationOfCommissionOfSexualOffence",
                "An allegation of commission of sexual offence"),
        @CCD(displayOrder = 4)
        V004("insolvency", "Insolvency"),
        @CCD(displayOrder = 5)
        V005("jurisdictionsUnclear", "Jurisdictions unclear"),
        @CCD(displayOrder = 6)
        V006("lengthOfService", "Length of service"),
        @CCD(displayOrder = 7)
        V007("potentiallyLinkedCasesInTheEcm", "Potentially linked cases in the ECM"),
        @CCD(displayOrder = 8)
        V008("rule50Issues", "Rule 49 issues"),
        @CCD(displayOrder = 9)
        V009("anotherReasonForJudicialReferral", "Another reason for judicial referral");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List204MslOtherFactors implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("claimOutOfTime", "The whole or any part of the claim is out of time"),
        @CCD(displayOrder = 2)
        V002("multipleClaim", "The claim is part of a multiple claim"),
        @CCD(displayOrder = 3)
        V003("employmentStatusIssues", "The claim has a potential issue about employment status"),
        @CCD(displayOrder = 4)
        V004(
                "pidJurisdictionRegulator",
                "The claim has PID jurisdiction and claimant wants it forwarded to relevant"
                        + " regulator - Box 10.1"),
        @CCD(displayOrder = 5)
        V005("videoHearingPreference", "The claimant prefers a video hearing"),
        @CCD(displayOrder = 6)
        V006("rule50IssuesOtherFactors", "The claim has Rule 49 issues"),
        @CCD(displayOrder = 7)
        V007("otherRelevantFactors", "The claim has other relevant factors for judicial referral");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List205MslPayClaims implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Arrears", "Arrears (pay you are owed)"),
        @CCD(displayOrder = 2)
        V002("Holiday pay", "Holiday pay"),
        @CCD(displayOrder = 3)
        V003("Notice pay", "Notice pay"),
        @CCD(displayOrder = 4)
        V004("Redundancy pay", "Redundancy pay"),
        @CCD(displayOrder = 5)
        V005("Other payments", "Other payments");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List206MslRemoveDocument implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes - Remove document", "Yes - Remove document");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List207MslRule27direction implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("No jurisdiction", "No jurisdiction"),
        @CCD(displayOrder = 2)
        V002("No reasonable prospect of success", "No reasonable prospect of success");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List208MslSubmitEt1 implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes", "Yes - I want to submit this ET1 claim");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List209MslTypeOfClaim implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("discrimination", "discrimination"),
        @CCD(displayOrder = 2)
        V002("breachOfContract", "breachOfContract"),
        @CCD(displayOrder = 3)
        V003("payRelated", "payRelated"),
        @CCD(displayOrder = 4)
        V004("unfairDismissal", "unfairDismissal"),
        @CCD(displayOrder = 5)
        V005("whistleBlowing", "whistleBlowing"),
        @CCD(displayOrder = 6)
        V006("otherTypesOfClaims", "otherTypesOfClaims");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List210PageNumberFormat implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("pageRange", "Page Range"),
        @CCD(displayOrder = 2)
        V002("numberOfPages", "Number Of Pages");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List211PaginationStyle implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("off", "Off"),
        @CCD(displayOrder = 2)
        V002("topLeft", "Top Left"),
        @CCD(displayOrder = 3)
        V003("topCenter", "Top Center"),
        @CCD(displayOrder = 4)
        V004("topRight", "Top Right"),
        @CCD(displayOrder = 5)
        V005("bottomLeft", "Bottom Left"),
        @CCD(displayOrder = 6)
        V006("bottomCenter", "Bottom Center"),
        @CCD(displayOrder = 7)
        V007("bottomRight", "Bottom Right");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List212SendNotificationEccQuestion implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Notice of Employer Contract Claim", "Notice of Employer Contract Claim"),
        @CCD(displayOrder = 2)
        V002("Acceptance of ECC response", "Acceptance of ECC response"),
        @CCD(displayOrder = 3)
        V003("Rejection of ECC response", "Rejection of ECC response");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List213SendNotificationNotify implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Both parties", "Both parties"),
        @CCD(displayOrder = 2)
        V002("Claimant only", "Claimant only"),
        @CCD(displayOrder = 3)
        V003("Respondent only", "Respondent only");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum List214SendNotificationResponseTribunal implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V001("Yes - view document for details", "Yes - view document for details"),
        @CCD(displayOrder = 2)
        V002("No", "No");

        private final String code;
        private final String label;
    }
}
