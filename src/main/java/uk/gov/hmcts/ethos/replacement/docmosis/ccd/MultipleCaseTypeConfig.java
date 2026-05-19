package uk.gov.hmcts.ethos.replacement.docmosis.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.Permission;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.List;

import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Complex;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Mandatory;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.Optional;
import static uk.gov.hmcts.ccd.sdk.api.DisplayContext.ReadOnly;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.EventFieldSpec.field;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.LegacyEventDefinition.complex;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.LegacyEventDefinition.event;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.LegacyEventDefinition.grant;

@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.ExcessiveMethodLength", "checkstyle:LineLength"})
public abstract class MultipleCaseTypeConfig<T extends MultipleData> implements CCDConfig<T, EtState, EtUserRole> {

    private final String caseType;
    private final String name;
    private final String description;
    private final EtUserRole regionalCaseworkerRole;
    private final EtUserRole regionalJudgeRole;

    protected MultipleCaseTypeConfig(
        String caseType,
        String name,
        String description,
        EtUserRole regionalCaseworkerRole,
        EtUserRole regionalJudgeRole
    ) {
        this.caseType = caseType;
        this.name = name;
        this.description = description;
        this.regionalCaseworkerRole = regionalCaseworkerRole;
        this.regionalJudgeRole = regionalJudgeRole;
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

        configBuilder.event("amendMultipleAPI")
            .forAllStates()
            .name("Amend Multiple Details API")
            .description("Amend Multiple Details API")
            .displayOrder(4)
            .showCondition("multipleSource=\"dummy\"")
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/amendMultipleAPI")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)
            .grant(Permission.CRU, regionalCaseworkerRole, regionalJudgeRole);

        configBuilder.event("updatePayloadMultiple")
            .forAllStates()
            .name("Update Multiple via callback")
            .description("Updates payload when needed")
            .displayOrder(4)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/updatePayloadMultiple")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        configBuilder.event("resetMultipleState")
            .forAllStates()
            .name("Reset Multiple State")
            .description("Reset Multiple State")
            .displayOrder(11)
            .aboutToSubmitCallbackUrl("${ET_COS_URL}/resetMultipleState")
            .grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API);

        multipleEvents().forEach(spec -> LegacyEventDefinition.addTo(configBuilder, spec));
    }

    private List<LegacyEventDefinition.EventSpec> multipleEvents() {
        return caseType.startsWith("ET_Scotland") ? scotlandMultipleEvents() : englandWalesMultipleEvents();
    }

    private List<LegacyEventDefinition.EventSpec> englandWalesMultipleEvents() {
        return List.of(
            event("extractNotifications", "Extract notifications", "Extract notifications from sub cases", null, "*", "*", "state=\"dummy\"", "", "${ET_COS_URL}/multiples/extractNotifications/aboutToSubmit", "${ET_COS_URL}/multiples/extractNotifications/submitted", null, null, false,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRU, regionalCaseworkerRole)),
                List.of(
                    field("extractEventText", ReadOnly, 1, 1, 1).pageLabel("Notification Extract").pageColumn(1)
                ),
                List.of()
            ),
            event("createReferral", "Referral", "Referral", 50, "*", "*", "", "${ET_COS_URL}/multiples/createReferral/aboutToStart", "${ET_COS_URL}/multiples/createReferral/aboutToSubmit", "${ET_COS_URL}/multiples/createReferral/completeCreateReferral", "Close and Return to Multiple Cases tab", "Y", true,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRUD, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API), grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)),
                List.of(
                    field("referralHearingDetails", ReadOnly, 1, 1, 1).show("referralHearingDetailsLabel=\"dummy\"").pageLabel("Refer to admin, legal officer or judge").mid("${ET_COS_URL}/createReferral/validateReferentEmail").summary("Y").pageColumn(1),
                    field("referralHearingDetailsLabel", ReadOnly, 1, 1, 2).pageColumn(1),
                    field("referCaseTo", Mandatory, 1, 1, 3).summary("Y").pageColumn(1),
                    field("referentEmail", Mandatory, 1, 1, 4).summary("Y").pageColumn(1),
                    field("isUrgent", Mandatory, 1, 1, 5).summary("Y").pageColumn(1),
                    field("referralSubject", Mandatory, 1, 1, 6).summary("Y").pageColumn(1),
                    field("referralSubjectSpecify", Mandatory, 1, 1, 7).show("referralSubject =\"Other\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("referralDetails", Mandatory, 1, 1, 8).summary("Y").pageColumn(1),
                    field("referralDocument", Complex, 1, 1, 9).summary("Y").pageColumn(1),
                    field("referralInstruction", Optional, 1, 1, 10).summary("Y").pageColumn(1),
                    field("referralCollection", Complex, 1, 1, 11).show("referralHearingDetailsLabel=\"dummy\"").summary("N").pageColumn(1)
                ),
                List.of(
                    complex("referralCollection", "referCaseTo", Optional, 1, " ", "ReferralDetails", "Y", null),
                    complex("referralCollection", "referralSubject", Optional, 2, " ", "ReferralDetails", "Y", null),
                    complex("referralCollection", "referralSubjectSpecify", Optional, 2, " ", "ReferralDetails", "Y", null),
                    complex("referralCollection", "isUrgent", Optional, 3, " ", "ReferralDetails", "Y", null),
                    complex("referralCollection", "referralNumber", Optional, 4, " ", "ReferralDetails", "Y", null),
                    complex("referralDocument", "uploadedDocument", Optional, 1, "Document", "referralDocument", null, null),
                    complex("referralDocument", "shortDescription", Optional, 2, "Short description of document", "referralDocument", null, null)
                )
            ),
            event("replyToReferral", "Referral", "Refer to admin, legal officer or judge", 51, "*", "*", "multipleReference =\"dummy\"", "${ET_COS_URL}/multiples/replyReferral/aboutToStart", "${ET_COS_URL}/multiples/replyReferral/aboutToSubmit", "${ET_COS_URL}/multiples/replyReferral/completeReplyToReferral", "Close and Return to Multiple Cases tab", "Y", true,
                List.of(grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("nextListedDate", Optional, 1, 1, 4).show("selectReferral=\"dummy\"").summary("N").pageColumn(1).publishColumn("Y"),
                    field("isUrgentReply", Mandatory, 2, 2, 7).summary("Y").pageColumn(1),
                    field("directionDetails", Mandatory, 2, 2, 7).show("isJudge=\"True\"").summary("Y").pageColumn(1),
                    field("directionTo", Mandatory, 2, 2, 3).show("isJudge=\"True\"").summary("Y").pageColumn(1),
                    field("hearingAndReferralDetails", ReadOnly, 2, 2, 1).show("hearingAndReferralDetailsLabel=\"dummy\"").pageLabel("Refer to admin, legal officer or judge").mid("${ET_COS_URL}/replyReferral/validateReplyToEmail").pageColumn(1),
                    field("hearingAndReferralDetailsLabel", ReadOnly, 2, 2, 2).pageColumn(1),
                    field("isJudge", ReadOnly, 1, 1, 2).show("selectReferral=\"dummy\"").pageColumn(1),
                    field("referralCollection", Complex, 1, 1, 3).show("selectReferral=\"dummy\"").summary("N").pageColumn(1),
                    field("replyDetails", Mandatory, 2, 2, 8).show("isJudge=\"False\"").summary("Y").pageColumn(1),
                    field("replyDocument", Complex, 2, 2, 9).summary("Y").pageColumn(1),
                    field("replyGeneralNotes", Optional, 2, 2, 10).summary("Y").pageColumn(1),
                    field("replyTo", Mandatory, 2, 2, 4).show("isJudge=\"False\"").summary("Y").pageColumn(1),
                    field("replyToEmailAddress", Mandatory, 2, 2, 5).summary("Y").pageColumn(1),
                    field("selectReferral", Mandatory, 1, 1, 1).pageLabel("Refer to admin, legal officer or judge").mid("${ET_COS_URL}/multiples/replyReferral/initHearingAndReferralDetails")
                ),
                List.of(
                    complex("referralCollection", "referralSubject", Optional, 1, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralSubjectSpecify", Optional, 2, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referCaseTo", Optional, 2, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralNumber", ReadOnly, 3, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection", Optional, 4, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection.directionTo", Optional, 5, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection.isUrgentReply", Optional, 6, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection.replyDateTime", Optional, 7, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection.referralSubject", Optional, 8, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection.referralNumber", Optional, 9, " ", "ReferralReplyDetails", "Y", null),
                    complex("replyDocument", "uploadedDocument", Optional, 1, "Document", "replyDocument", null, null),
                    complex("replyDocument", "shortDescription", Optional, 2, "Short description of document", "replyDocument", null, null)
                )
            ),
            event("closeReferral", "Referral", "Close referral", 51, "*", "*", "multipleReference =\"dummy\"", "${ET_COS_URL}/multiples/closeReferral/aboutToStart", "${ET_COS_URL}/multiples/closeReferral/aboutToSubmit", "${ET_COS_URL}/multiples/closeReferral/completeCloseReferral", "Close and Return to Multiple Cases tab", "Y", true,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("selectReferral", Mandatory, 1, 1, 1).pageLabel("Close referral").mid("${ET_COS_URL}/multiples/closeReferral/initHearingAndReferralDetails"),
                    field("closeReferralHearingDetails", ReadOnly, 2, 2, 1).show("closeReferralHearingDetailsLabel=\"dummy\"").pageLabel("Close referral").pageColumn(1),
                    field("closeReferralHearingDetailsLabel", ReadOnly, 2, 2, 2).pageColumn(1),
                    field("confirmCloseReferral", Mandatory, 2, 2, 3).summary("Y").pageColumn(1),
                    field("closeReferralGeneralNotes", Optional, 2, 2, 4).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("uploadDocument", "Upload Document", "Upload a Document", 16, "*", "*", null, "${ET_COS_URL}/multiples/uploadDocument/aboutToStart", "${ET_COS_URL}/multiples/uploadDocument/aboutToSubmit", null, null, null, false,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("documentCollection", Complex, 1, 1, 1).summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("documentCollection", "excludeFromDcf", Optional, 15, "Do you want to exclude this document from the DCF?", "Documents", null, null),
                    complex("documentCollection", "topLevelDocuments", Mandatory, 1, "Document Category", "Documents", null, null),
                    complex("documentCollection", "startingClaimDocuments", Mandatory, 2, "Starting a Claim", "Documents", null, null),
                    complex("documentCollection", "responseClaimDocuments", Mandatory, 3, "Response to a Claim", "Documents", null, null),
                    complex("documentCollection", "initialConsiderationDocuments", Mandatory, 4, "Initial Consideration", "Documents", null, null),
                    complex("documentCollection", "caseManagementDocuments", Mandatory, 5, "Case Management", "Documents", null, null),
                    complex("documentCollection", "eccDocuments", Mandatory, 6, "Employer Contract Claim", "Documents", null, null),
                    complex("documentCollection", "withdrawalSettledDocuments", Mandatory, 7, "Withdrawal/Settled", "Documents", null, null),
                    complex("documentCollection", "hearingsDocuments", Mandatory, 8, "Hearings", "Documents", null, null),
                    complex("documentCollection", "judgmentAndReasonsDocuments", Mandatory, 9, "Judgment and Reasons", "Documents", null, null),
                    complex("documentCollection", "reconsiderationDocuments", Mandatory, 10, "Reconsideration", "Documents", null, null),
                    complex("documentCollection", "miscDocuments", Mandatory, 11, "Misc", "Documents", null, null),
                    complex("documentCollection", "typeOfDocument", Mandatory, 12, "Type of Document", "Documents", null, null),
                    complex("documentCollection", "uploadedDocument", Mandatory, 13, "Document", "Documents", null, null),
                    complex("documentCollection", "shortDescription", Optional, 14, "Short Description", "Documents", null, null),
                    complex("documentCollection", "dateOfCorrespondence", Optional, 15, "Date of Correspondence", "Documents", null, null)
                )
            ),
            event("sendNotification", "Send a notification", "Send a notification", null, "*", "*", "state=\"dummy\"", "${ET_COS_URL}/multiples/sendNotification/aboutToStart", "${ET_COS_URL}/multiples/sendNotification/aboutToSubmit", "${ET_COS_URL}/multiples/sendNotification/submitted", null, null, true,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRU, regionalCaseworkerRole)),
                List.of(
                    field("sendNotificationInfo", ReadOnly, 1, 1, 1).pageLabel("Send a notification"),
                    field("sendNotificationLetter", Mandatory, 1, 1, 3).summary("Y"),
                    field("sendNotificationUploadDocument", Complex, 1, 1, 4).show("sendNotificationLetter=\"Yes\"").summary("Y").retainHidden("No"),
                    field("sendNotificationSubject", Mandatory, 1, 1, 5).summary("Y"),
                    field("sendNotificationSelectHearing", Mandatory, 1, 1, 6).show("sendNotificationSubject CONTAINS \"Hearing\"").summary("Y").retainHidden("No"),
                    field("sendNotificationCaseManagement", Mandatory, 1, 1, 7).show("sendNotificationSubject CONTAINS \"Case management orders / requests\"").summary("Y").retainHidden("No"),
                    field("sendNotificationResponseTribunal", Mandatory, 1, 1, 8).show("sendNotificationSubject CONTAINS \"Case management orders / requests\"").summary("Y").retainHidden("No"),
                    field("sendNotificationWhoCaseOrder", Mandatory, 1, 1, 10).show("sendNotificationSubject CONTAINS \"Case management orders / requests\" AND sendNotificationCaseManagement=\"Case management order\"").summary("Y").retainHidden("No"),
                    field("sendNotificationFullName", Mandatory, 1, 1, 11).show("sendNotificationSubject CONTAINS \"Case management orders / requests\" AND sendNotificationCaseManagement!=\"\"").summary("Y").retainHidden("No"),
                    field("sendNotificationSelectParties", Mandatory, 1, 1, 9).show("sendNotificationResponseTribunal=\"Yes - view document for details\" AND sendNotificationSubject CONTAINS \"Case management orders / requests\"").summary("Y").retainHidden("No"),
                    field("sendNotificationTitle", Mandatory, 1, 1, 2).summary("Y"),
                    field("sendNotificationWhoMadeJudgement", Mandatory, 1, 1, 12).show(" sendNotificationSubject CONTAINS \"Judgment\"").summary("Y").retainHidden("No"),
                    field("sendNotificationFullName2", Mandatory, 1, 1, 13).show("sendNotificationSubject CONTAINS \"Judgment\" AND sendNotificationWhoMadeJudgement!=\"\"").summary("Y").retainHidden("No"),
                    field("sendNotificationDecision", Mandatory, 1, 1, 14).show("sendNotificationSubject CONTAINS \"Judgment\"").summary("Y").retainHidden("No"),
                    field("sendNotificationEccQuestion", Mandatory, 1, 1, 16).show("sendNotificationSubject CONTAINS \"Employer Contract Claim\"").summary("Y").retainHidden("No"),
                    field("sendNotificationNotify", Mandatory, 1, 1, 18).summary("Y"),
                    field("sendNotificationNotifyLeadCase", Mandatory, 1, 1, 19).show("sendNotificationNotify=\"Lead case\"").summary("Y"),
                    field("sendNotificationNotifyAll", Mandatory, 1, 1, 20).show("sendNotificationNotify=\"Lead and sub cases\"").summary("Y"),
                    field("sendNotificationNotifySelected", Mandatory, 1, 1, 21).show("sendNotificationNotify=\"Selected cases\"").summary("Y"),
                    field("sendNotificationAdditionalInfo", Optional, 1, 1, 17).summary("Y"),
                    field("sendNotificationDetails", Mandatory, 1, 1, 15).show("sendNotificationDecision=\"Other\" AND sendNotificationSubject CONTAINS \"Judgment\"").summary("Y").retainHidden("No"),
                    field("sendNotificationRequestMadeBy", Mandatory, 1, 1, 11).show("sendNotificationSubject CONTAINS \"Case management orders / requests\" AND sendNotificationCaseManagement=\"Request\"").summary("Y").retainHidden("No"),
                    field("subMultiple", Mandatory, 2, 2, 1).pageShow("sendNotificationNotify=\"Selected cases\"").summary("Y").pageColumn(1),
                    field("flag1", Mandatory, 2, 2, 3).summary("Y").pageColumn(1),
                    field("flag2", Mandatory, 2, 2, 4).summary("Y").pageColumn(1),
                    field("flag3", Mandatory, 2, 2, 5).summary("Y").pageColumn(1),
                    field("flag4", Mandatory, 2, 2, 6).summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("sendNotificationUploadDocument", "uploadedDocument", Mandatory, 1, null, "sendNotificationUploadDocument", null, null),
                    complex("sendNotificationUploadDocument", "shortDescription", Mandatory, 2, "Short description of document", "sendNotificationUploadDocument", null, null)
                )
            ),
            event("updateReferral", "Update Referral", "Update Referral", 49, "*", "*", "multipleReference =\"dummy\"", "${ET_COS_URL}/multiples/updateReferral/aboutToStart", "${ET_COS_URL}/multiples/updateReferral/aboutToSubmit", "", "Close and Return to Multiple Cases tab", null, true,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("referralHearingDetails", ReadOnly, 2, 2, 1).show("referralHearingDetailsLabel=\"dummy\"").pageLabel("Refer to admin, legal officer or judge").pageColumn(1),
                    field("referralHearingDetailsLabel", ReadOnly, 2, 2, 2).pageColumn(1),
                    field("selectReferral", Mandatory, 1, 1, 1).pageLabel("Update Referral").mid("${ET_COS_URL}/multiples/updateReferral/initHearingAndReferralDetails"),
                    field("updateIsUrgent", Mandatory, 2, 2, 5).pageColumn(1),
                    field("updateReferCaseTo", Mandatory, 2, 2, 3).pageColumn(1),
                    field("updateReferentEmail", Mandatory, 2, 2, 4).pageColumn(1),
                    field("updateReferralDetails", Mandatory, 2, 2, 7).pageColumn(1),
                    field("updateReferralDocument", Complex, 2, 2, 8).pageColumn(1),
                    field("updateReferralInstruction", Optional, 2, 2, 10).pageColumn(1),
                    field("updateReferralSubject", Mandatory, 2, 2, 6).pageColumn(1),
                    field("updateReferralSubjectSpecify", Mandatory, 2, 2, 9).show("updateReferralSubject =\"Other\"").pageColumn(1)
                ),
                List.of(
                    complex("referralCollection", "referralSubject", Optional, 1, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "referralSubjectSpecify", Optional, 2, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "referCaseTo", Optional, 3, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "referralNumber", ReadOnly, 4, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection", Optional, 5, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralNumber", Optional, 6, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralHearingDate", Optional, 7, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferCaseTo", Optional, 8, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferentEmail", Optional, 9, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralDetails", Optional, 10, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateIsUrgent", Optional, 11, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralSubject", Optional, 12, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralSubjectSpecify", Optional, 13, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralDocument", Optional, 14, " ", "UpdateReferralDetails", null, null),
                    complex("referralCollection", "updateReferralCollection.updateReferralInstruction", Optional, 15, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferredBy", Optional, 16, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralDate", Optional, 17, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralDateTime", Optional, 18, " ", "UpdateReferralDetails", "Y", null),
                    complex("updateReferralDocument", "uploadedDocument", Optional, 2, "Document", "updateReferralDocument", null, null),
                    complex("updateReferralDocument", "shortDescription", Optional, 3, "Short description of document", "updateReferralDocument", null, null)
                )
            ),
            event("addCaseNote", "Add note", "Add note", 50, "*", "*", null, "", "${ET_COS_URL}/caseNotes/multiples/aboutToSubmit", "", null, null, true,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("caseNote", Complex, 1, 1, 1).summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("caseNote", "title", Mandatory, 1, "Title", "caseNote", null, null),
                    complex("caseNote", "note", Mandatory, 2, "Note", "caseNote", null, null),
                    complex("addCaseNote", "title", Mandatory, 1, "Title", "addCaseNote", null, null),
                    complex("addCaseNote", "note", Mandatory, 2, "Note", "addCaseNote", null, null)
                )
            ),
            event("documentSelect", "Document Access", "Document Access", 17, "*", "*", null, "${ET_COS_URL}/multiples/documentAccess/aboutToStart", "${ET_COS_URL}/multiples/documentAccess/aboutToSubmit", null, null, null, true,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API), grant(Permission.CRUD, regionalJudgeRole)),
                List.of(
                    field("documentAccess", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("documentSelect", Mandatory, 2, 2, 1).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("createDcf", "Create Digital Case File", "Create Digital Case File", 29, "*", "*", null, "${ET_COS_URL}/multiples/dcf/selectDcf", "${ET_COS_URL}/multiples/dcf/aboutToSubmit", null, "Create Digital Case File", null, false,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("dcfYesNo", Mandatory, 1, 1, 1).pageLabel("Configure case file").summary("N").pageColumn(1),
                    field("caseBundles", Complex, 1, 1, 2).show("dcfYesNo=\"No\"").pageLabel("Configure case file").summary("N").pageColumn(1)
                ),
                List.of(
                    complex("caseBundles", "documents", Mandatory, 1, "Digital Case File documents", "DigitalCaseFile", null, null),
                    complex("caseBundles", "documents.name", Mandatory, 2, "Document name", "DigitalCaseFile", null, null),
                    complex("caseBundles", "documents.sourceDocument", Mandatory, 3, "Document", "DigitalCaseFile", null, null),
                    complex("digitalCaseFile", "uploadedDocument", Mandatory, 1, "Digital Case File", "CreateDCF", null, null)
                )
            ),
            event("asyncStitchingComplete", "Stitching bundle complete", "Stitching bundle complete", 9000, "*", "*", "multipleReference=\"dummy\"", null, null, null, null, null, false,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(),
                List.of()
            ),
            event("close", "Close", "Close Multiple", 10, "Open", "Closed", null, "${ET_COS_URL}/initialiseCloseMultiple", "${ET_COS_URL}/closeMultiple", null, null, "Y", false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("positionLabel", ReadOnly, 1, 1, 1).summary("Y").pageColumn(1),
                    field("clerkResponsible", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("fileLocation", Optional, 1, 1, 3).summary("Y").pageColumn(1),
                    field("notes", Optional, 1, 1, 4).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("createMultiple", "Create Multiple", "Create Multiple", 1, null, "Open", null, null, "${ET_COS_URL}/createMultiple", null, null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("multipleName", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("managingOffice", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("leadCase", Optional, 2, 2, 1).mid("${ET_COS_URL}/multipleCreationMidEventValidation").summary("Y").pageColumn(1),
                    field("caseIdCollection", Optional, 2, 2, 2).mid("${ET_COS_URL}/multipleCreationMidEventValidation").summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("preAcceptMultiple", "Accept/Reject Multiple", "Accept/Reject Multiple", 2, "Open", "*", null, null, "${ET_COS_URL}/preAcceptMultiple", null, null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("preAcceptMultiple", Mandatory, 1, 1, 1).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("amendMultipleDetails", "Amend Multiple Details", "Amend Multiple Details", 3, "Open", "Open", null, null, "${ET_COS_URL}/amendMultiple", null, null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("multipleNote", Mandatory, 1, 1, 4).show("typeOfAmendment CONTAINS \"Amend multiple note\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("removeCasesLabel", ReadOnly, 1, 1, 5).show("typeOfAmendment CONTAINS \"Remove cases from multiple\"").summary("Y").pageColumn(1),
                    field("altCaseIdCollection", Mandatory, 3, 3, 1).show("typeOfAmendment CONTAINS \"Remove cases from multiple\"").pageShow("typeOfAmendment CONTAINS \"Remove cases from multiple\"").mid("${ET_COS_URL}/multipleRemoveCaseIdsMidEventValidation").summary("Y").pageColumn(1),
                    field("typeOfAmendment", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("multipleName", Mandatory, 1, 1, 2).show("typeOfAmendment CONTAINS \"Amend multiple name\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("leadCase", ReadOnly, 1, 1, 3).show("typeOfAmendment =\"dummy\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("leadCaseLabel", ReadOnly, 1, 1, 3).show("typeOfAmendment CONTAINS \"Amend lead case\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("newLeadCase", Mandatory, 1, 1, 4).show("typeOfAmendment CONTAINS \"Amend lead case\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("addCasesLabel", ReadOnly, 1, 1, 5).show("typeOfAmendment CONTAINS \"Add cases to multiple\"").summary("Y").pageColumn(1),
                    field("caseIdCollection", Mandatory, 2, 2, 1).show("typeOfAmendment CONTAINS \"Add cases to multiple\"").pageShow("typeOfAmendment CONTAINS \"Add cases to multiple\"").mid("${ET_COS_URL}/multipleAmendCaseIdsMidEventValidation").summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("importFile", "Import File", "Import the data file containing details of the cases within the Multiple", 5, "Open", "Open", null, null, "${ET_COS_URL}/importMultiple", null, null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("caseImporterFile", Complex, 1, 1, 1).summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("caseImporterFile", "uploadedDocument", Mandatory, 1, "File", "importerFileUpload", null, null)
                )
            ),
            event("printSchedule", "Print Schedule", null, 6, "Open", "Open", null, "${ET_COS_URL}/dynamicListFlags", "${ET_COS_URL}/printSchedule", "${ET_COS_URL}/printDocumentConfirmation", null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("liveCases", Mandatory, 2, 2, 2).summary("Y").pageColumn(1),
                    field("subMultiple", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("flag1", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("flag2", Mandatory, 1, 1, 3).summary("Y").pageColumn(1),
                    field("flag3", Mandatory, 1, 1, 4).summary("Y").pageColumn(1),
                    field("flag4", Mandatory, 1, 1, 5).summary("Y").pageColumn(1),
                    field("scheduleDocName", Mandatory, 2, 2, 1).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("batchUpdateCases", "Batch Update Cases", "Batch update of cases based on flag values", 7, "Open", "Open", null, "${ET_COS_URL}/initialiseBatchUpdate", "${ET_COS_URL}/batchUpdate", null, "Flag based batch update of cases in Multiple", null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("batchCaseStayed", Optional, 3, 3, 6).publishColumn("N"),
                    field("batchUpdateType", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("subMultiple", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("liveCases", Mandatory, 1, 1, 3).summary("Y").pageColumn(1),
                    field("flag1", Mandatory, 1, 1, 4).summary("Y").pageColumn(1),
                    field("flag2", Mandatory, 1, 1, 5).summary("Y").pageColumn(1),
                    field("flag3", Mandatory, 1, 1, 6).summary("Y").pageColumn(1),
                    field("flag4", Mandatory, 1, 1, 7).summary("Y").pageColumn(1),
                    field("batchUpdateType", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("subMultiple", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("flag1", Mandatory, 1, 1, 3).summary("Y").pageColumn(1),
                    field("flag2", Mandatory, 1, 1, 4).summary("Y").pageColumn(1),
                    field("flag3", Mandatory, 1, 1, 5).summary("Y").pageColumn(1),
                    field("flag4", Mandatory, 1, 1, 6).summary("Y").pageColumn(1),
                    field("batchUpdateCase", Mandatory, 2, 2, 1).pageShow("batchUpdateType=\"batchUpdateType3\"").mid("${ET_COS_URL}/multipleSingleMidEventValidation").summary("Y").pageColumn(1),
                    field("positionType", Optional, 3, 3, 1).pageShow("batchUpdateType =\"batchUpdateType1\"").mid("${ET_COS_URL}/multipleMidBatch1Validation").summary("Y").pageColumn(1),
                    field("clerkResponsible", Optional, 3, 3, 2).summary("Y").pageColumn(1),
                    field("hearingStageEQP", Optional, 3, 3, 3).show("batchUpdateType = \"hide until we get a solution\"").summary("Y").pageColumn(1),
                    field("receiptDate", Optional, 3, 3, 4).summary("Y").pageColumn(1),
                    field("fileLocation", Optional, 3, 3, 5).summary("Y").pageColumn(1),
                    field("batchMoveCases", Complex, 4, 4, 1).pageShow("batchUpdateType = \"batchUpdateType2\"").mid("${ET_COS_URL}/multipleMidEventValidation").summary("Y").pageColumn(1),
                    field("batchUpdateClaimantRep", Mandatory, 5, 5, 1).pageShow("batchUpdateType = \"batchUpdateType3\"").summary("Y").pageColumn(1),
                    field("batchRemoveClaimantRep", Mandatory, 5, 5, 2).show("batchUpdateClaimantRep!=\"None\"").summary("Y").pageColumn(1),
                    field("batchUpdateJurisdiction", Mandatory, 5, 5, 2).show("batchUpdateType=\"dummy\"").summary("Y").pageColumn(1),
                    field("batchUpdateJurisdictionList", Mandatory, 5, 2, 5).summary("Y").publishColumn("N"),
                    field("batchUpdateRespondent", Mandatory, 5, 5, 3).summary("Y").pageColumn(1),
                    field("batchUpdateJudgment", Mandatory, 5, 5, 4).summary("Y").pageColumn(1),
                    field("batchUpdateRespondentRep", Mandatory, 5, 5, 5).summary("Y").pageColumn(1),
                    field("batchRemoveRespondentRep", Mandatory, 5, 5, 7).show("batchUpdateRespondentRep!=\"None\"").summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("batchMoveCases", "convertToSingle", Mandatory, 1, "Will the cases be moved out of the multiple and treated as single cases?", "moveCases", null, null),
                    complex("batchMoveCases", "updatedMultipleRef", Mandatory, 2, "Updated multiple reference number", "moveCases", null, "Enter the existing multiple reference number if only the submultiple is changing"),
                    complex("batchMoveCases", "updatedSubMultipleRef", Optional, 3, "Updated submultiple reference number", "moveCases", null, null)
                )
            ),
            event("updateSubMultiple", "Manage SubMultiple", null, 8, "Open", "Open", null, null, "${ET_COS_URL}/updateSubMultiple", null, null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("subMultipleAction", Mandatory, 1, 1, 1).mid("${ET_COS_URL}/subMultipleMidEventValidation").summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("generateCorrespondence", "Letters", "Generate Letters", 9, "Open", "Open", null, "${ET_COS_URL}/dynamicMultipleLetters", "${ET_COS_URL}/printLetter", "${ET_COS_URL}/printDocumentConfirmation", null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("subMultiple", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("flag1", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("flag2", Mandatory, 1, 1, 3).summary("Y").pageColumn(1),
                    field("flag3", Mandatory, 1, 1, 4).summary("Y").pageColumn(1),
                    field("flag4", Mandatory, 1, 1, 5).summary("Y").pageColumn(1),
                    field("correspondenceType", Mandatory, 2, 2, 1).mid("${ET_COS_URL}/midSelectedAddressLabelsMultiple").summary("Y").pageColumn(1),
                    field("addressLabelsSelectionTypeMSL", Mandatory, 2, 2, 2).show("correspondenceType.topLevel_Documents=\"EM-TRB-LET-ENG-00544\"").summary("Y").pageColumn(1),
                    field("addressLabelsAttributesType", Complex, 3, 3, 1).pageShow("correspondenceType.topLevel_Documents=\"EM-TRB-LET-ENG-00544\"").mid("${ET_COS_URL}/midValidateAddressLabelsMultiple").summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("addressLabelCollection", "printLabel", Mandatory, 1, "Print label?", "AddressLabels", null, null),
                    complex("addressLabelCollection", "fullName", ReadOnly, 2, "Full name", "AddressLabels", null, null),
                    complex("addressLabelCollection", "fullAddress", ReadOnly, 3, "Full address", "AddressLabels", null, null),
                    complex("addressLabelsAttributesType", "numberOfSelectedLabels", ReadOnly, 1, "Number of selected labels to print in this run:", "AddressLabels", null, null),
                    complex("addressLabelsAttributesType", "numberOfCopies", Mandatory, 2, "Number of copies of each label", "AddressLabels", null, null),
                    complex("addressLabelsAttributesType", "startingLabel", Mandatory, 3, "Select the label to start printing from", "AddressLabels", null, null),
                    complex("addressLabelsAttributesType", "showTelFax", Mandatory, 4, "Show Tel / Fax Numbers?", "AddressLabels", null, null)
                )
            ),
            event("multipleTransferSameCountry", "Multiple Transfer (Eng/Wales)", "Transfer multiple to another office within England/Wales", 12, "Open", "*", null, "${ET_COS_URL}/caseTransferMultiples/initTransferToEnglandWales", "${ET_COS_URL}/caseTransferMultiples/transferSameCountry", null, "Transfer Multiple", null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("officeMultipleCT", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("reasonForCT", Mandatory, 1, 1, 2).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("multipleTransferDifferentCountry", "Multiple Transfer (Scotland)", "Transfer multiple to Scotland", 13, "Open", "Transferred", null, "${ET_COS_URL}/caseTransferMultiples/initTransferToScotland", "${ET_COS_URL}/caseTransferMultiples/transferDifferentCountry", null, "Transfer Multiple", null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("officeMultipleCT", ReadOnly, 1, 1, 1).summary("Y").pageColumn(1),
                    field("reasonForCT", Mandatory, 1, 1, 2).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("fixMultipleTransferAPI", "Fix Multiple API Only", "Fix broken multiple", 14, "*", "Open(state=\"Open\"):1;Transferred(state=\"Transferred\"):2;Error(state=\"Error\"):3;Updating(state=\"Updating\"):4;Closed(state=\"Closed\"):5;*", null, null, "${ET_COS_URL}/fixMultipleCaseApi", null, null, null, false,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("typeOfAmendment", Optional, 1, 1, 1).summary("Y").pageColumn(1),
                    field("multipleReference", Optional, 1, 1, 2).summary("Y").pageColumn(1),
                    field("multipleName", Optional, 1, 1, 3).summary("Y").pageColumn(1),
                    field("newLeadCase", Optional, 1, 1, 4).summary("Y").pageColumn(1),
                    field("caseIdCollection", Optional, 1, 1, 5).summary("Y").pageColumn(1),
                    field("multipleSource", Optional, 1, 1, 6).summary("Y").pageColumn(1),
                    field("preAcceptDone", Optional, 1, 1, 7).summary("Y").pageColumn(1),
                    field("state", Optional, 1, 1, 8).summary("Y").pageColumn(1),
                    field("isFixCase", Optional, 1, 1, 9).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("bulkAddSingleCases", "Bulk Add Single Cases", "Add one or more single cases to a multiple", 15, "Open", "Open", "multipleSource=\"Manually Created\" OR multipleSource=\"Migrated\" OR preAcceptDone =\"Yes\"", null, "${ET_COS_URL}/bulkAddSingleCasesToMultiple", null, "Add Cases to Multiple", null, false,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("bulkAddSingleCasesImportFile", Complex, 1, 1, 1).mid("${ET_COS_URL}/bulkAddSingleCasesImportFileMidEventValidation").summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("bulkAddSingleCasesImportFile", "uploadedDocument", Mandatory, 1, "File", "bulkAddSingleCasesFileUpload", null, null)
                )
            )
        );
    }

    private List<LegacyEventDefinition.EventSpec> scotlandMultipleEvents() {
        return List.of(
            event("createReferral", "Referral", "Referral", 49, "*", "*", "", "${ET_COS_URL}/multiples/createReferral/aboutToStart", "${ET_COS_URL}/multiples/createReferral/aboutToSubmit", "${ET_COS_URL}/multiples/createReferral/completeCreateReferral", "Close and Return to Multiple Cases tab", "Y", true,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API), grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION)),
                List.of(
                    field("referralHearingDetails", ReadOnly, 1, 1, 1).show("referralHearingDetailsLabel=\"dummy\"").pageLabel("Refer to admin, legal officer or judge").mid("${ET_COS_URL}/createReferral/validateReferentEmail").summary("Y").pageColumn(1),
                    field("referralHearingDetailsLabel", ReadOnly, 1, 1, 2).pageColumn(1),
                    field("referCaseTo", Mandatory, 1, 1, 3).summary("Y").pageColumn(1),
                    field("referentEmail", Mandatory, 1, 1, 4).summary("Y").pageColumn(1),
                    field("isUrgent", Mandatory, 1, 1, 5).summary("Y").pageColumn(1),
                    field("referralSubject", Mandatory, 1, 1, 6).summary("Y").pageColumn(1),
                    field("referralSubjectSpecify", Mandatory, 1, 1, 7).show("referralSubject =\"Other\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("referralDetails", Mandatory, 1, 1, 8).summary("Y").pageColumn(1),
                    field("referralDocument", Complex, 1, 1, 9).summary("Y").pageColumn(1),
                    field("referralInstruction", Optional, 1, 1, 10).summary("Y").pageColumn(1),
                    field("referralCollection", Complex, 1, 1, 11).show("referralHearingDetailsLabel=\"dummy\"").summary("N").pageColumn(1)
                ),
                List.of(
                    complex("referralCollection", "referCaseTo", Optional, 1, " ", "ReferralDetails", "Y", null),
                    complex("referralCollection", "referralSubject", Optional, 2, " ", "ReferralDetails", "Y", null),
                    complex("referralCollection", "referralSubjectSpecify", Optional, 2, " ", "ReferralDetails", "Y", null),
                    complex("referralCollection", "isUrgent", Optional, 3, " ", "ReferralDetails", "Y", null),
                    complex("referralCollection", "referralNumber", Optional, 4, " ", "ReferralDetails", "Y", null),
                    complex("referralDocument", "uploadedDocument", Optional, 1, "Document", "referralDocument", null, null),
                    complex("referralDocument", "shortDescription", Optional, 2, "Short description of document", "referralDocument", null, null)
                )
            ),
            event("sendNotification", "Send a notification", "Send a notification", null, "*", "*", "state=\"dummy\"", "${ET_COS_URL}/multiples/sendNotification/aboutToStart", "${ET_COS_URL}/multiples/sendNotification/aboutToSubmit", "${ET_COS_URL}/multiples/sendNotification/submitted", null, null, true,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRU, regionalCaseworkerRole)),
                List.of(
                    field("sendNotificationInfo", ReadOnly, 1, 1, 1).pageLabel("Send a notification"),
                    field("sendNotificationLetter", Mandatory, 1, 1, 3).summary("Y"),
                    field("sendNotificationUploadDocument", Complex, 1, 1, 4).show("sendNotificationLetter=\"Yes\"").summary("Y").retainHidden("No"),
                    field("sendNotificationSubject", Mandatory, 1, 1, 5).summary("Y"),
                    field("sendNotificationSelectHearing", Mandatory, 1, 1, 6).show("sendNotificationSubject CONTAINS \"Hearing\"").summary("Y").retainHidden("No"),
                    field("sendNotificationCaseManagement", Mandatory, 1, 1, 7).show("sendNotificationSubject CONTAINS \"Case management orders / requests\"").summary("Y").retainHidden("No"),
                    field("sendNotificationResponseTribunal", Mandatory, 1, 1, 8).show("sendNotificationSubject CONTAINS \"Case management orders / requests\"").summary("Y").retainHidden("No"),
                    field("sendNotificationWhoCaseOrder", Mandatory, 1, 1, 10).show("sendNotificationSubject CONTAINS \"Case management orders / requests\" AND sendNotificationCaseManagement=\"Case management order\"").summary("Y").retainHidden("No"),
                    field("sendNotificationFullName", Mandatory, 1, 1, 11).show("sendNotificationSubject CONTAINS \"Case management orders / requests\" AND sendNotificationCaseManagement!=\"\"").summary("Y").retainHidden("No"),
                    field("sendNotificationSelectParties", Mandatory, 1, 1, 9).show("sendNotificationResponseTribunal=\"Yes - view document for details\" AND sendNotificationSubject CONTAINS \"Case management orders / requests\"").summary("Y").retainHidden("No"),
                    field("sendNotificationTitle", Mandatory, 1, 1, 2).summary("Y"),
                    field("sendNotificationWhoMadeJudgement", Mandatory, 1, 1, 12).show(" sendNotificationSubject CONTAINS \"Judgment\"").summary("Y").retainHidden("No"),
                    field("sendNotificationFullName2", Mandatory, 1, 1, 13).show("sendNotificationSubject CONTAINS \"Judgment\" AND sendNotificationWhoMadeJudgement!=\"\"").summary("Y").retainHidden("No"),
                    field("sendNotificationDecision", Mandatory, 1, 1, 14).show("sendNotificationSubject CONTAINS \"Judgment\"").summary("Y").retainHidden("No"),
                    field("sendNotificationEccQuestion", Mandatory, 1, 1, 16).show("sendNotificationSubject CONTAINS \"Employer Contract Claim\"").summary("Y").retainHidden("No"),
                    field("sendNotificationNotify", Mandatory, 1, 1, 18).summary("Y"),
                    field("sendNotificationNotifyLeadCase", Mandatory, 1, 1, 19).show("sendNotificationNotify=\"Lead case\"").summary("Y"),
                    field("sendNotificationNotifyAll", Mandatory, 1, 1, 20).show("sendNotificationNotify=\"Lead and sub cases\"").summary("Y"),
                    field("sendNotificationNotifySelected", Mandatory, 1, 1, 21).show("sendNotificationNotify=\"Selected cases\"").summary("Y"),
                    field("sendNotificationAdditionalInfo", Optional, 1, 1, 17).summary("Y"),
                    field("sendNotificationDetails", Mandatory, 1, 1, 15).show("sendNotificationDecision=\"Other\" AND sendNotificationSubject CONTAINS \"Judgment\"").summary("Y").retainHidden("No"),
                    field("sendNotificationRequestMadeBy", Mandatory, 1, 1, 11).show("sendNotificationSubject CONTAINS \"Case management orders / requests\" AND sendNotificationCaseManagement=\"Request\"").summary("Y").retainHidden("No"),
                    field("subMultiple", Mandatory, 2, 2, 1).pageShow("sendNotificationNotify=\"Selected cases\"").summary("Y").pageColumn(1),
                    field("flag1", Mandatory, 2, 2, 3).summary("Y").pageColumn(1),
                    field("flag2", Mandatory, 2, 2, 4).summary("Y").pageColumn(1),
                    field("flag3", Mandatory, 2, 2, 5).summary("Y").pageColumn(1),
                    field("flag4", Mandatory, 2, 2, 6).summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("sendNotificationUploadDocument", "shortDescription", Mandatory, 2, "Short description of document", "sendNotificationUploadDocument", null, null),
                    complex("sendNotificationUploadDocument", "uploadedDocument", Mandatory, 1, null, "sendNotificationUploadDocument", null, null)
                )
            ),
            event("extractNotifications", "Extract notifications", "Extract notifications from sub cases", null, "*", "*", "state=\"dummy\"", "", "${ET_COS_URL}/multiples/extractNotifications/aboutToSubmit", "${ET_COS_URL}/multiples/extractNotifications/submitted", null, null, false,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRU, regionalCaseworkerRole)),
                List.of(
                    field("extractEventText", ReadOnly, 1, 1, 1).pageLabel("Notification Extract").pageColumn(1)
                ),
                List.of()
            ),
            event("replyToReferral", "Referral", "Refer to admin, legal officer or judge", 48, "*", "*", "multipleReference =\"dummy\"", "${ET_COS_URL}/multiples/replyReferral/aboutToStart", "${ET_COS_URL}/multiples/replyReferral/aboutToSubmit", "${ET_COS_URL}/multiples/replyReferral/completeReplyToReferral", "Close and Return to Multiple Cases tab", "Y", true,
                List.of(grant(Permission.CRU, EtUserRole.CASEWORKER_WA_TASK_CONFIGURATION), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("nextListedDate", Optional, 1, 1, 4).show("selectReferral=\"dummy\"").summary("N").pageColumn(1).publishColumn("Y"),
                    field("isUrgentReply", Mandatory, 2, 2, 7).summary("Y").pageColumn(1),
                    field("directionDetails", Mandatory, 2, 2, 7).show("isJudge=\"True\"").summary("Y").pageColumn(1),
                    field("directionTo", Mandatory, 2, 2, 3).show("isJudge=\"True\"").summary("Y").pageColumn(1),
                    field("hearingAndReferralDetails", ReadOnly, 2, 2, 1).show("hearingAndReferralDetailsLabel=\"dummy\"").pageLabel("Refer to admin, legal officer or judge").mid("${ET_COS_URL}/replyReferral/validateReplyToEmail").pageColumn(1),
                    field("hearingAndReferralDetailsLabel", ReadOnly, 2, 2, 2).pageColumn(1),
                    field("isJudge", ReadOnly, 1, 1, 2).show("selectReferral=\"dummy\"").pageColumn(1),
                    field("referralCollection", Complex, 1, 1, 3).show("selectReferral=\"dummy\"").summary("N").pageColumn(1),
                    field("replyDetails", Mandatory, 2, 2, 8).show("isJudge=\"False\"").summary("Y").pageColumn(1),
                    field("replyDocument", Complex, 2, 2, 9).summary("Y").pageColumn(1),
                    field("replyGeneralNotes", Optional, 2, 2, 10).summary("Y").pageColumn(1),
                    field("replyTo", Mandatory, 2, 2, 4).show("isJudge=\"False\"").summary("Y").pageColumn(1),
                    field("replyToEmailAddress", Mandatory, 2, 2, 5).summary("Y").pageColumn(1),
                    field("selectReferral", Mandatory, 1, 1, 1).pageLabel("Refer to admin, legal officer or judge").mid("${ET_COS_URL}/multiples/replyReferral/initHearingAndReferralDetails")
                ),
                List.of(
                    complex("referralCollection", "referralSubject", Optional, 1, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralSubjectSpecify", Optional, 2, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referCaseTo", Optional, 2, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralNumber", ReadOnly, 3, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection", Optional, 4, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection.directionTo", Optional, 5, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection.isUrgentReply", Optional, 6, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection.replyDateTime", Optional, 7, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection.referralSubject", Optional, 8, " ", "ReferralReplyDetails", "Y", null),
                    complex("referralCollection", "referralReplyCollection.referralNumber", Optional, 9, " ", "ReferralReplyDetails", "Y", null),
                    complex("replyDocument", "uploadedDocument", Optional, 1, "Document", "replyDocument", null, null),
                    complex("replyDocument", "shortDescription", Optional, 2, "Short description of document", "replyDocument", null, null)
                )
            ),
            event("updateReferral", "Update Referral", "Update Referral", 49, "*", "*", "multipleReference =\"dummy\"", "${ET_COS_URL}/multiples/updateReferral/aboutToStart", "${ET_COS_URL}/multiples/updateReferral/aboutToSubmit", "", "Close and Return to Multiple Cases tab", null, true,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("referralHearingDetails", ReadOnly, 2, 2, 1).show("referralHearingDetailsLabel=\"dummy\"").pageLabel("Refer to admin, legal officer or judge").pageColumn(1),
                    field("referralHearingDetailsLabel", ReadOnly, 2, 2, 2).pageColumn(1),
                    field("selectReferral", Mandatory, 1, 1, 1).pageLabel("Update Referral").mid("${ET_COS_URL}/multiples/updateReferral/initHearingAndReferralDetails"),
                    field("updateIsUrgent", Mandatory, 2, 2, 5).pageColumn(1),
                    field("updateReferCaseTo", Mandatory, 2, 2, 3).pageColumn(1),
                    field("updateReferentEmail", Mandatory, 2, 2, 4).pageColumn(1),
                    field("updateReferralDetails", Mandatory, 2, 2, 7).pageColumn(1),
                    field("updateReferralDocument", Complex, 2, 2, 8).pageColumn(1),
                    field("updateReferralInstruction", Optional, 2, 2, 10).pageColumn(1),
                    field("updateReferralSubject", Mandatory, 2, 2, 6).pageColumn(1),
                    field("updateReferralSubjectSpecify", Mandatory, 2, 2, 9).show("updateReferralSubject =\"Other\"").pageColumn(1).retainHidden("Yes")
                ),
                List.of(
                    complex("referralCollection", "referralSubject", Optional, 1, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "referralSubjectSpecify", Optional, 2, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "referCaseTo", Optional, 3, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "referralNumber", ReadOnly, 4, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection", Optional, 5, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralNumber", Optional, 6, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralHearingDate", Optional, 7, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferCaseTo", Optional, 8, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferentEmail", Optional, 9, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralDetails", Optional, 10, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateIsUrgent", Optional, 11, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralSubject", Optional, 12, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralSubjectSpecify", Optional, 13, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralDocument", Optional, 14, " ", "UpdateReferralDetails", null, null),
                    complex("referralCollection", "updateReferralCollection.updateReferralInstruction", Optional, 15, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferredBy", Optional, 16, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralDate", Optional, 17, " ", "UpdateReferralDetails", "Y", null),
                    complex("referralCollection", "updateReferralCollection.updateReferralDateTime", Optional, 18, " ", "UpdateReferralDetails", "Y", null),
                    complex("updateReferralDocument", "uploadedDocument", Optional, 2, "Document", "updateReferralDocument", null, null),
                    complex("updateReferralDocument", "shortDescription", Optional, 3, "Short description of document", "updateReferralDocument", null, null)
                )
            ),
            event("closeReferral", "Referral", "Close referral", 51, "*", "*", "multipleReference =\"dummy\"", "${ET_COS_URL}/multiples/closeReferral/aboutToStart", "${ET_COS_URL}/multiples/closeReferral/aboutToSubmit", "${ET_COS_URL}/multiples/closeReferral/completeCloseReferral", "Close and Return to Multiple Cases tab", "Y", true,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("selectReferral", Mandatory, 1, 1, 1).pageLabel("Close referral").mid("${ET_COS_URL}/multiples/closeReferral/initHearingAndReferralDetails"),
                    field("closeReferralHearingDetails", ReadOnly, 2, 2, 1).show("closeReferralHearingDetailsLabel=\"dummy\"").pageLabel("Close referral").pageColumn(1),
                    field("closeReferralHearingDetailsLabel", ReadOnly, 2, 2, 2).pageColumn(1),
                    field("confirmCloseReferral", Mandatory, 2, 2, 3).summary("Y").pageColumn(1),
                    field("closeReferralGeneralNotes", Optional, 2, 2, 4).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("addCaseNote", "Add note", "Add note", 50, "*", "*", null, "", "${ET_COS_URL}/caseNotes/multiples/aboutToSubmit", "", null, null, true,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("caseNote", Complex, 1, 1, 1).summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("caseNote", "title", Mandatory, 1, "Title", "caseNote", null, null),
                    complex("caseNote", "note", Mandatory, 2, "Note", "caseNote", null, null),
                    complex("addCaseNote", "title", Mandatory, 1, "Title", "addCaseNote", null, null),
                    complex("addCaseNote", "note", Mandatory, 2, "Note", "addCaseNote", null, null)
                )
            ),
            event("documentSelect", "Document Access", "Document Access", 17, "*", "*", null, "${ET_COS_URL}/multiples/documentAccess/aboutToStart", "${ET_COS_URL}/multiples/documentAccess/aboutToSubmit", null, null, null, true,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API), grant(Permission.CRUD, regionalJudgeRole)),
                List.of(
                    field("documentAccess", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("documentSelect", Mandatory, 2, 2, 1).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("createDcf", "Create Digital Case File", "Create Digital Case File", 29, "*", "*", null, "${ET_COS_URL}/multiples/dcf/selectDcf", "${ET_COS_URL}/multiples/dcf/aboutToSubmit", null, "Create Digital Case File", null, false,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("dcfYesNo", Mandatory, 1, 1, 1).pageLabel("Configure case file").summary("N").pageColumn(1),
                    field("caseBundles", Complex, 1, 1, 2).show("dcfYesNo=\"No\"").pageLabel("Configure case file").summary("N").pageColumn(1)
                ),
                List.of(
                    complex("caseBundles", "documents", Mandatory, 1, "Digital Case File documents", "DigitalCaseFile", null, null),
                    complex("caseBundles", "documents.name", Mandatory, 2, "Document name", "DigitalCaseFile", null, null),
                    complex("caseBundles", "documents.sourceDocument", Mandatory, 3, "Document", "DigitalCaseFile", null, null),
                    complex("digitalCaseFile", "uploadedDocument", Mandatory, 1, "Digital Case File", "CreateDCF", null, null)
                )
            ),
            event("asyncStitchingComplete", "Stitching bundle complete", "Stitching bundle complete", 9000, "*", "*", "multipleReference=\"dummy\"", null, null, null, null, null, false,
                List.of(grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(),
                List.of()
            ),
            event("close", "Close", "Close Multiple", 10, "Open", "Closed", "multipleSource=\"Manually Created\" OR multipleSource=\"Migrated\" OR preAcceptDone =\"Yes\"", null, "${ET_COS_URL}/closeMultiple", null, null, "Y", false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("managingOffice", Mandatory, 1, 1, 1).mid("${ET_COS_URL}/initialiseCloseMultiple").summary("Y").pageColumn(1),
                    field("positionLabel", ReadOnly, 2, 2, 1).summary("Y").pageColumn(1),
                    field("clerkResponsible", Mandatory, 2, 2, 2).summary("Y").pageColumn(1),
                    field("fileLocationGlasgow", Optional, 2, 2, 4).show("managingOffice=\"Glasgow\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("fileLocationAberdeen", Optional, 2, 2, 4).show("managingOffice=\"Aberdeen\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("fileLocationDundee", Optional, 2, 2, 4).show("managingOffice=\"Dundee\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("fileLocationEdinburgh", Optional, 2, 2, 4).show("managingOffice=\"Edinburgh\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("notes", Optional, 2, 2, 5).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("createMultiple", "Create Multiple", "Create Multiple", 1, null, "Open", null, null, "${ET_COS_URL}/createMultiple", null, null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("multipleName", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("managingOffice", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("leadCase", Optional, 2, 2, 1).mid("${ET_COS_URL}/multipleCreationMidEventValidation").summary("Y").pageColumn(1),
                    field("caseIdCollection", Optional, 2, 2, 2).mid("${ET_COS_URL}/multipleCreationMidEventValidation").summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("preAcceptMultiple", "Accept/Reject Multiple", "Accept/Reject Multiple", 2, "Open", "*", "multipleSource=\"ET1 Online\" AND preAcceptDone =\"No\"", null, "${ET_COS_URL}/preAcceptMultiple", null, null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("preAcceptMultiple", Mandatory, 1, 1, 1).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("amendMultipleDetails", "Amend Multiple Details", "Amend Multiple Details", 3, "Open", "Open", "multipleSource=\"Manually Created\" OR multipleSource=\"Migrated\" OR preAcceptDone =\"Yes\"", null, "${ET_COS_URL}/amendMultiple", null, null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("multipleNote", Mandatory, 1, 1, 4).show("typeOfAmendment CONTAINS \"Amend multiple note\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("removeCasesLabel", ReadOnly, 1, 1, 5).show("typeOfAmendment CONTAINS \"Remove cases from multiple\"").summary("Y").pageColumn(1),
                    field("altCaseIdCollection", Mandatory, 3, 3, 1).show("typeOfAmendment CONTAINS \"Remove cases from multiple\"").pageShow("typeOfAmendment CONTAINS \"Remove cases from multiple\"").mid("${ET_COS_URL}/multipleRemoveCaseIdsMidEventValidation").summary("Y").pageColumn(1),
                    field("typeOfAmendment", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("multipleName", Mandatory, 1, 1, 2).show("typeOfAmendment CONTAINS \"Amend multiple name\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("leadCase", ReadOnly, 1, 1, 3).show("typeOfAmendment =\"dummy\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("leadCaseLabel", ReadOnly, 1, 1, 3).show("typeOfAmendment CONTAINS \"Amend lead case\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("newLeadCase", Mandatory, 1, 1, 4).show("typeOfAmendment CONTAINS \"Amend lead case\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("addCasesLabel", ReadOnly, 1, 1, 5).show("typeOfAmendment CONTAINS \"Add cases to multiple\"").summary("Y").pageColumn(1),
                    field("caseIdCollection", Mandatory, 2, 2, 1).show("typeOfAmendment CONTAINS \"Add cases to multiple\"").pageShow("typeOfAmendment CONTAINS \"Add cases to multiple\"").mid("${ET_COS_URL}/multipleAmendCaseIdsMidEventValidation").summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("importFile", "Import File", "Import the data file containing details of the cases within the Multiple", 5, "Open", "Open", "multipleSource=\"Migrated\" OR multipleSource=\"Manually Created\" OR preAcceptDone =\"Yes\"", null, "${ET_COS_URL}/importMultiple", null, null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("caseImporterFile", Complex, 1, 1, 1).summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("caseImporterFile", "uploadedDocument", Mandatory, 1, "File", "importerFileUpload", null, null)
                )
            ),
            event("printSchedule", "Print Schedule", null, 6, "Open", "Open", "multipleSource=\"Manually Created\" OR multipleSource=\"Migrated\" OR preAcceptDone =\"Yes\"", "${ET_COS_URL}/dynamicListFlags", "${ET_COS_URL}/printSchedule", "${ET_COS_URL}/printDocumentConfirmation", null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("liveCases", Mandatory, 2, 2, 2).summary("Y").pageColumn(1),
                    field("subMultiple", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("flag1", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("flag2", Mandatory, 1, 1, 3).summary("Y").pageColumn(1),
                    field("flag3", Mandatory, 1, 1, 4).summary("Y").pageColumn(1),
                    field("flag4", Mandatory, 1, 1, 5).summary("Y").pageColumn(1),
                    field("scheduleDocName", Mandatory, 2, 2, 1).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("batchUpdateCases", "Batch Update Cases", "Batch update of cases based on flag values", 7, "Open", "Open", "multipleSource=\"Manually Created\" OR multipleSource=\"Migrated\" OR preAcceptDone =\"Yes\"", "${ET_COS_URL}/initialiseBatchUpdate", "${ET_COS_URL}/batchUpdate", null, "Flag based batch update of cases in Multiple", null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("batchCaseStayed", Optional, 3, 3, 6).publishColumn("N"),
                    field("batchUpdateType", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("liveCases", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("subMultiple", Mandatory, 1, 1, 3).summary("Y").pageColumn(1),
                    field("flag1", Mandatory, 1, 1, 4).summary("Y").pageColumn(1),
                    field("flag2", Mandatory, 1, 1, 5).summary("Y").pageColumn(1),
                    field("flag3", Mandatory, 1, 1, 6).summary("Y").pageColumn(1),
                    field("flag4", Mandatory, 1, 1, 7).summary("Y").pageColumn(1),
                    field("batchUpdateType", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("subMultiple", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("flag1", Mandatory, 1, 1, 3).summary("Y").pageColumn(1),
                    field("flag2", Mandatory, 1, 1, 4).summary("Y").pageColumn(1),
                    field("flag3", Mandatory, 1, 1, 5).summary("Y").pageColumn(1),
                    field("flag4", Mandatory, 1, 1, 6).summary("Y").pageColumn(1),
                    field("batchUpdateCase", Mandatory, 2, 2, 1).pageShow("batchUpdateType=\"batchUpdateType3\"").mid("${ET_COS_URL}/multipleSingleMidEventValidation").summary("Y").pageColumn(1),
                    field("positionType", Optional, 3, 3, 1).pageShow("batchUpdateType =\"batchUpdateType1\"").mid("${ET_COS_URL}/multipleMidBatch1Validation").summary("Y").pageColumn(1),
                    field("clerkResponsible", Optional, 3, 3, 2).summary("Y").pageColumn(1),
                    field("hearingStageEQP", Optional, 3, 3, 3).show("batchUpdateType = \"hide until we get a solution\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("receiptDate", Optional, 3, 3, 4).summary("Y").pageColumn(1),
                    field("managingOffice", Optional, 3, 2, 5).summary("Y").pageColumn(1),
                    field("fileLocationGlasgow", Optional, 3, 2, 5).show("managingOffice=\"Glasgow\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("fileLocationAberdeen", Optional, 3, 2, 5).show("managingOffice=\"Aberdeen\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("fileLocationDundee", Optional, 3, 2, 5).show("managingOffice=\"Dundee\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("fileLocationEdinburgh", Optional, 3, 2, 5).show("managingOffice=\"Edinburgh\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("batchMoveCases", Complex, 4, 4, 1).pageShow("batchUpdateType = \"batchUpdateType2\"").mid("${ET_COS_URL}/multipleMidEventValidation").summary("Y").pageColumn(1),
                    field("batchUpdateClaimantRep", Mandatory, 5, 5, 1).pageShow("batchUpdateType = \"batchUpdateType3\"").summary("Y").pageColumn(1),
                    field("batchRemoveClaimantRep", Mandatory, 5, 5, 2).show("batchUpdateClaimantRep!=\"None\"").summary("Y").pageColumn(1),
                    field("batchUpdateJurisdiction", Mandatory, 5, 5, 2).show("batchUpdateType=\"dummy\"").summary("Y").pageColumn(1),
                    field("batchUpdateJurisdictionList", Mandatory, 5, 5, 2).summary("Y").publishColumn("N"),
                    field("batchUpdateRespondent", Mandatory, 5, 5, 3).summary("Y").pageColumn(1),
                    field("batchUpdateJudgment", Mandatory, 5, 5, 4).summary("Y").pageColumn(1),
                    field("batchUpdateRespondentRep", Mandatory, 5, 5, 5).summary("Y").pageColumn(1),
                    field("batchRemoveRespondentRep", Mandatory, 5, 5, 7).show("batchUpdateRespondentRep!=\"None\"").summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("batchMoveCases", "convertToSingle", Mandatory, 1, "Will the cases be moved out of the multiple and treated as single cases?", "moveCases", null, null),
                    complex("batchMoveCases", "updatedMultipleRef", Mandatory, 2, "Updated multiple reference number", "moveCases", null, "Enter the existing multiple reference number if only the submultiple is changing"),
                    complex("batchMoveCases", "updatedSubMultipleRef", Optional, 3, "Updated submultiple reference number", "moveCases", null, null)
                )
            ),
            event("updateSubMultiple", "Manage SubMultiple", null, 8, "Open", "Open", "multipleSource=\"Manually Created\" OR multipleSource=\"Migrated\" OR preAcceptDone =\"Yes\"", null, "${ET_COS_URL}/updateSubMultiple", null, null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("subMultipleAction", Mandatory, 1, 1, 1).mid("${ET_COS_URL}/subMultipleMidEventValidation").summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("generateCorrespondence", "Letters", "Generate Letters", 9, "Open", "Open", "multipleSource=\"Manually Created\" OR multipleSource=\"Migrated\" OR preAcceptDone =\"Yes\"", "${ET_COS_URL}/dynamicMultipleLetters", "${ET_COS_URL}/printLetter", "${ET_COS_URL}/printDocumentConfirmation", null, null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRU, regionalJudgeRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("subMultiple", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("flag1", Mandatory, 1, 1, 2).summary("Y").pageColumn(1),
                    field("flag2", Mandatory, 1, 1, 3).summary("Y").pageColumn(1),
                    field("flag3", Mandatory, 1, 1, 4).summary("Y").pageColumn(1),
                    field("flag4", Mandatory, 1, 1, 5).summary("Y").pageColumn(1),
                    field("correspondenceScotType", Mandatory, 2, 2, 1).mid("${ET_COS_URL}/midSelectedAddressLabelsMultiple").summary("Y").pageColumn(1),
                    field("addressLabelsSelectionTypeMSL", Mandatory, 2, 2, 2).show("correspondenceScotType.topLevel_Scot_Documents=\"EM-TRB-LET-ENG-00544\"").summary("Y").pageColumn(1).retainHidden("Yes"),
                    field("addressLabelsAttributesType", Complex, 3, 3, 1).pageShow("correspondenceScotType.topLevel_Scot_Documents=\"EM-TRB-LET-ENG-00544\"").mid("${ET_COS_URL}/midValidateAddressLabelsMultiple").summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("addressLabelCollection", "printLabel", Mandatory, 1, "Print label?", "AddressLabels", null, null),
                    complex("addressLabelCollection", "fullName", ReadOnly, 2, "Full name", "AddressLabels", null, null),
                    complex("addressLabelCollection", "fullAddress", ReadOnly, 3, "Full address", "AddressLabels", null, null),
                    complex("addressLabelsAttributesType", "numberOfSelectedLabels", ReadOnly, 1, "Number of selected labels to print in this run:", "AddressLabels", null, null),
                    complex("addressLabelsAttributesType", "numberOfCopies", Mandatory, 2, "Number of copies of each label", "AddressLabels", null, null),
                    complex("addressLabelsAttributesType", "startingLabel", Mandatory, 3, "Select the label to start printing from", "AddressLabels", null, null),
                    complex("addressLabelsAttributesType", "showTelFax", Mandatory, 4, "Show Tel / Fax Numbers?", "AddressLabels", null, null)
                )
            ),
            event("multipleTransferDifferentCountry", "Multiple Transfer (Eng/Wales)", "Transfer multiple to England/Wales", 12, "Open", "Transferred", null, "${ET_COS_URL}/caseTransferMultiples/initTransferToEnglandWales", "${ET_COS_URL}/caseTransferMultiples/transferDifferentCountry", null, "Transfer Multiple", null, false,
                List.of(grant(Permission.CRU, regionalCaseworkerRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("officeMultipleCT", Mandatory, 1, 1, 1).summary("Y").pageColumn(1),
                    field("reasonForCT", Mandatory, 1, 1, 2).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("fixMultipleTransferAPI", "Fix Multiple API Only", "Fix broken multiple", 13, "*", "Open(state=\"Open\"):1;Transferred(state=\"Transferred\"):2;Error(state=\"Error\"):3;Updating(state=\"Updating\"):4;Closed(state=\"Closed\"):5;*", null, null, "${ET_COS_URL}/fixMultipleCaseApi", null, null, null, false,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("typeOfAmendment", Optional, 1, 1, 1).summary("Y").pageColumn(1),
                    field("multipleReference", Optional, 1, 1, 2).summary("Y").pageColumn(1),
                    field("multipleName", Optional, 1, 1, 3).summary("Y").pageColumn(1),
                    field("newLeadCase", Optional, 1, 1, 4).summary("Y").pageColumn(1),
                    field("caseIdCollection", Optional, 1, 1, 5).summary("Y").pageColumn(1),
                    field("multipleSource", Optional, 1, 1, 6).summary("Y").pageColumn(1),
                    field("preAcceptDone", Optional, 1, 1, 7).summary("Y").pageColumn(1),
                    field("state", Optional, 1, 1, 8).summary("Y").pageColumn(1),
                    field("isFixCase", Optional, 1, 1, 9).summary("Y").pageColumn(1)
                ),
                List.of()
            ),
            event("bulkAddSingleCases", "Bulk Add Single Cases", "Add one or more single cases to a multiple", 14, "Open", "Open", "multipleSource=\"Manually Created\" OR multipleSource=\"Migrated\" OR preAcceptDone =\"Yes\"", null, "${ET_COS_URL}/bulkAddSingleCasesToMultiple", null, "Add Cases to Multiple", null, false,
                List.of(grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API)),
                List.of(
                    field("bulkAddSingleCasesImportFile", Complex, 1, 1, 1).mid("${ET_COS_URL}/bulkAddSingleCasesImportFileMidEventValidation").summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("bulkAddSingleCasesImportFile", "uploadedDocument", Mandatory, 1, "File", "bulkAddSingleCasesFileUpload", null, null)
                )
            ),
            event("uploadDocument", "Upload Document", "Upload a Document", 15, "*", "*", null, "${ET_COS_URL}/multiples/uploadDocument/aboutToStart", "${ET_COS_URL}/multiples/uploadDocument/aboutToSubmit", null, null, null, false,
                List.of(grant(Permission.CRUD, regionalCaseworkerRole), grant(Permission.CRUD, EtUserRole.CASEWORKER_EMPLOYMENT_API), grant(Permission.R, EtUserRole.CASEWORKER_EMPLOYMENT_ETJUDGE), grant(Permission.R, regionalJudgeRole)),
                List.of(
                    field("documentCollection", Complex, 1, 1, 1).summary("Y").pageColumn(1)
                ),
                List.of(
                    complex("documentCollection", "excludeFromDcf", Optional, 15, "Do you want to exclude this document from the DCF?", "Documents", null, null),
                    complex("documentCollection", "topLevelDocuments", Mandatory, 1, "Document Category", "Documents", null, null),
                    complex("documentCollection", "startingClaimDocuments", Mandatory, 2, "Starting a Claim", "Documents", null, null),
                    complex("documentCollection", "responseClaimDocuments", Mandatory, 3, "Response to a Claim", "Documents", null, null),
                    complex("documentCollection", "initialConsiderationDocuments", Mandatory, 4, "Initial Consideration", "Documents", null, null),
                    complex("documentCollection", "caseManagementDocuments", Mandatory, 5, "Case Management", "Documents", null, null),
                    complex("documentCollection", "withdrawalSettledDocuments", Mandatory, 6, "Withdrawal/Settled", "Documents", null, null),
                    complex("documentCollection", "hearingsDocuments", Mandatory, 7, "Hearings", "Documents", null, null),
                    complex("documentCollection", "judgmentAndReasonsDocuments", Mandatory, 8, "Judgment and Reasons", "Documents", null, null),
                    complex("documentCollection", "reconsiderationDocuments", Mandatory, 9, "Reconsideration", "Documents", null, null),
                    complex("documentCollection", "miscDocuments", Mandatory, 10, "Misc", "Documents", null, null),
                    complex("documentCollection", "typeOfDocument", Mandatory, 11, "Type of Document", "Documents", null, null),
                    complex("documentCollection", "uploadedDocument", Mandatory, 12, "Document", "Documents", null, null),
                    complex("documentCollection", "shortDescription", Optional, 13, "Short Description", "Documents", null, null),
                    complex("documentCollection", "dateOfCorrespondence", Optional, 14, "Date of Correspondence", "Documents", null, null)
                )
            )
        );
    }
}
