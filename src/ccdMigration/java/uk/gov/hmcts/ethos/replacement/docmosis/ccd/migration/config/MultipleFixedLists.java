package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.ccd.sdk.api.HasCode;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;
import uk.gov.hmcts.et.common.model.ccd.MultipleRole;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.MultipleCaseState;

final class MultipleFixedLists {

    private MultipleFixedLists() {
    }

    static void registerCftlibEnglandWales(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder) {
        builder.registerFixedList("DocumentAccess", ListDocumentAccess.values());
        builder.registerFixedList("caseSourceList", ListcaseSourceList.values());
        builder.registerFixedList("configurationFiles", ListconfigurationFiles.values());
        builder.registerFixedList("fl_CaseManagement", FlCaseManagement.values());
        builder.registerFixedList("fl_DocumentCategories", FlDocumentCategories.values());
        builder.registerFixedList("fl_DocumentType", FlDocumentTypeEnglandWales.values());
        builder.registerFixedList("fl_EmployerContractClaim", FlEmployerContractClaim.values());
        builder.registerFixedList("fl_Hearings", FlHearings.values());
        builder.registerFixedList("fl_InitialConsideration", FlInitialConsideration.values());
        builder.registerFixedList("fl_JudgmentAndReasons", FlJudgmentAndReasons.values());
        builder.registerFixedList("fl_Misc", FlMisc.values());
        builder.registerFixedList("fl_Position", FlPosition.values());
        builder.registerFixedList("fl_PositionCT", FlPositionCT.values());
        builder.registerFixedList("fl_Reconsideration", FlReconsideration.values());
        builder.registerFixedList("fl_ReferralSubject", FlReferralSubject.values());
        builder.registerFixedList("fl_ResponseToAClaim", FlResponseToAClaim.values());
        builder.registerFixedList("fl_Stage", FlStage.values());
        builder.registerFixedList("fl_StartingAClaim", FlStartingAClaim.values());
        builder.registerFixedList("fl_TribunalOffice", FlTribunalOffice.values());
        builder.registerFixedList("fl_WithdrawalSettled", FlWithdrawalSettled.values());
        builder.registerFixedList("fl_batchUpdate", FlBatchUpdate.values());
        builder.registerFixedList("fl_scheduleDoc", FlScheduleDoc.values());
        builder.registerFixedList(
                "fl_sendNotificationCaseManagement", FlSendNotificationCaseManagement.values());
        builder.registerFixedList(
                "fl_sendNotificationDecision", FlSendNotificationDecision.values());
        builder.registerFixedList("fl_sendNotificationParties", FlSendNotificationParties.values());
        builder.registerFixedList("fl_sendNotificationSubject", FlSendNotificationSubject.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoCaseOrder", FlSendNotificationWhoCaseOrder.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoMadeJudgement", FlSendNotificationWhoMadeJudgement.values());
        builder.registerFixedList("frl_ReferCaseTo", FrlReferCaseTo.values());
        builder.registerFixedList("frl_liveCases", FrlLiveCases.values());
        builder.registerFixedList(
                "frl_sendNotificationRequestMadeBy", FrlSendNotificationRequestMadeBy.values());
        builder.registerFixedList("imageRendering", ListimageRendering.values());
        builder.registerFixedList("imageRenderingLocation", ListimageRenderingLocation.values());
        builder.registerFixedList("msl_SelectLabels", MslSelectLabels.values());
        builder.registerFixedList("msl_Yes", MslYes.values());
        builder.registerFixedList("msl_confirmCloseReferral", MslConfirmCloseReferral.values());
        builder.registerFixedList("multiplesAmendment", ListmultiplesAmendment.values());
        builder.registerFixedList("notifyMultiple", ListnotifyMultiple.values());
        builder.registerFixedList("pageNumberFormat", ListpageNumberFormat.values());
        builder.registerFixedList("paginationStyle", ListpaginationStyle.values());
        builder.registerFixedList(
                "sendNotificationEccQuestion", ListsendNotificationEccQuestion.values());
        builder.registerFixedList(
                "sendNotificationNotifyAll", ListsendNotificationNotifyAll.values());
        builder.registerFixedList(
                "sendNotificationNotifyLead", ListsendNotificationNotifyLead.values());
        builder.registerFixedList(
                "sendNotificationNotifySelected", ListsendNotificationNotifySelected.values());
        builder.registerFixedList(
                "sendNotificationResponseTribunal", ListsendNotificationResponseTribunal.values());
    }

    static void registerCftlibScotland(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder) {
        builder.registerFixedList("DocumentAccess", ListDocumentAccess.values());
        builder.registerFixedList("caseSourceList", ListcaseSourceList.values());
        builder.registerFixedList("configurationFiles", ListconfigurationFiles.values());
        builder.registerFixedList("fl_CaseManagement", FlCaseManagement.values());
        builder.registerFixedList("fl_DocumentCategories", FlDocumentCategories.values());
        builder.registerFixedList("fl_DocumentType", FlDocumentTypeScotland.values());
        builder.registerFixedList("fl_EmployerContractClaim", FlEmployerContractClaim.values());
        builder.registerFixedList("fl_Hearings", FlHearings.values());
        builder.registerFixedList("fl_InitialConsideration", FlInitialConsideration.values());
        builder.registerFixedList("fl_JudgmentAndReasons", FlJudgmentAndReasons.values());
        builder.registerFixedList("fl_Misc", FlMisc.values());
        builder.registerFixedList("fl_Position", FlPosition.values());
        builder.registerFixedList("fl_PositionCT", FlPositionCT.values());
        builder.registerFixedList("fl_Reconsideration", FlReconsideration.values());
        builder.registerFixedList("fl_ReferralSubject", FlReferralSubject.values());
        builder.registerFixedList("fl_ResponseToAClaim", FlResponseToAClaim.values());
        builder.registerFixedList("fl_Stage", FlStage.values());
        builder.registerFixedList("fl_StartingAClaim", FlStartingAClaim.values());
        builder.registerFixedList("VenueScotland", ListVenueScotland.values());
        builder.registerFixedList("fl_WithdrawalSettled", FlWithdrawalSettled.values());
        builder.registerFixedList("fl_batchUpdate", FlBatchUpdate.values());
        builder.registerFixedList("fl_scheduleDoc", FlScheduleDoc.values());
        builder.registerFixedList(
                "fl_sendNotificationCaseManagement", FlSendNotificationCaseManagement.values());
        builder.registerFixedList(
                "fl_sendNotificationDecision", FlSendNotificationDecision.values());
        builder.registerFixedList("fl_sendNotificationParties", FlSendNotificationParties.values());
        builder.registerFixedList("fl_sendNotificationSubject", FlSendNotificationSubject.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoCaseOrder", FlSendNotificationWhoCaseOrder.values());
        builder.registerFixedList(
                "fl_sendNotificationWhoMadeJudgement", FlSendNotificationWhoMadeJudgement.values());
        builder.registerFixedList("frl_ReferCaseTo", FrlReferCaseTo.values());
        builder.registerFixedList("frl_liveCases", FrlLiveCases.values());
        builder.registerFixedList(
                "frl_sendNotificationRequestMadeBy", FrlSendNotificationRequestMadeBy.values());
        builder.registerFixedList("imageRendering", ListimageRendering.values());
        builder.registerFixedList("imageRenderingLocation", ListimageRenderingLocation.values());
        builder.registerFixedList("msl_SelectLabels", MslSelectLabels.values());
        builder.registerFixedList("msl_Yes", MslYes.values());
        builder.registerFixedList("msl_confirmCloseReferral", MslConfirmCloseReferral.values());
        builder.registerFixedList("multiplesAmendment", ListmultiplesAmendment.values());
        builder.registerFixedList("notifyMultiple", ListnotifyMultiple.values());
        builder.registerFixedList("pageNumberFormat", ListpageNumberFormat.values());
        builder.registerFixedList("paginationStyle", ListpaginationStyle.values());
        builder.registerFixedList(
                "sendNotificationEccQuestion", ListsendNotificationEccQuestion.values());
        builder.registerFixedList(
                "sendNotificationNotifyAll", ListsendNotificationNotifyAll.values());
        builder.registerFixedList(
                "sendNotificationNotifyLead", ListsendNotificationNotifyLead.values());
        builder.registerFixedList(
                "sendNotificationNotifySelected", ListsendNotificationNotifySelected.values());
        builder.registerFixedList(
                "sendNotificationResponseTribunal", ListsendNotificationResponseTribunal.values());
    }

    static void registerProdEnglandWales(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder) {
        builder.registerFixedList("caseSourceList", ListcaseSourceList.values());
        builder.registerFixedList("fl_Position", FlPosition.values());
        builder.registerFixedList("fl_PositionCT", FlPositionCT.values());
        builder.registerFixedList("fl_Stage", FlStage.values());
        builder.registerFixedList("fl_TribunalOffice", FlTribunalOffice.values());
        builder.registerFixedList("fl_batchUpdate", FlBatchUpdate.values());
        builder.registerFixedList("fl_scheduleDoc", FlScheduleDoc.values());
        builder.registerFixedList("msl_SelectLabels", MslSelectLabels.values());
        builder.registerFixedList(
                "multiplesAmendment",
                ListmultiplesAmendment.V01,
                ListmultiplesAmendment.V02,
                ListmultiplesAmendment.V03);
    }

    static void registerProdScotland(
            ConfigBuilder<MultipleData, MultipleCaseState, MultipleRole> builder) {
        builder.registerFixedList("caseSourceList", ListcaseSourceList.values());
        builder.registerFixedList("fl_Position", FlPosition.values());
        builder.registerFixedList("fl_PositionCT", FlPositionCT.values());
        builder.registerFixedList("fl_Stage", FlStage.values());
        builder.registerFixedList("VenueScotland", ListVenueScotland.values());
        builder.registerFixedList("fl_batchUpdate", FlBatchUpdate.values());
        builder.registerFixedList("fl_scheduleDoc", FlScheduleDoc.values());
        builder.registerFixedList("msl_SelectLabels", MslSelectLabels.values());
        builder.registerFixedList(
                "multiplesAmendment",
                ListmultiplesAmendment.V01,
                ListmultiplesAmendment.V02,
                ListmultiplesAmendment.V03);
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListDocumentAccess implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Citizens", "Citizens"),
        @CCD(displayOrder = 2)
        V02("Legal rep/respondents", "Legal rep/respondents"),
        @CCD(displayOrder = 3)
        V03("Both Citizens and Legal rep/respondents", "Both Citizens and Legal rep/respondents"),
        @CCD(displayOrder = 4)
        V04("None (clear access)", "None (clear access)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListVenueScotland implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Glasgow", "Glasgow"),
        @CCD(displayOrder = 2)
        V02("Aberdeen", "Aberdeen"),
        @CCD(displayOrder = 3)
        V03("Dundee", "Dundee"),
        @CCD(displayOrder = 4)
        V04("Edinburgh", "Edinburgh"),
        @CCD(displayOrder = 5)
        V05("Unassigned", "Unassigned");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListcaseSourceList implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("ET1 Online", "ET1 Online"),
        @CCD(displayOrder = 2)
        V02("Manually Created", "Manually Created"),
        @CCD(displayOrder = 3)
        V03("Migration", "Migration"),
        @CCD(displayOrder = 4)
        V04("ECC", "ECC"),
        @CCD(displayOrder = 5)
        V05("MyHMCTS", "MyHMCTS");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListconfigurationFiles implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("et-dcf-2.yaml", "ET Digital Case File"),
        @CCD(displayOrder = 2)
        V02("et-dcf-ordered.yaml", "ET Digital Case File (Ordered)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlCaseManagement implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Tribunal Order", "Tribunal Order"),
        @CCD(displayOrder = 2)
        V02("Deposit Order", "Deposit Order"),
        @CCD(displayOrder = 3)
        V03("Unless Order", "Unless Order"),
        @CCD(displayOrder = 4)
        V04("Tribunal Notice", "Tribunal Notice"),
        @CCD(displayOrder = 5)
        V05("App to vary an order – C", "App to vary an order – C"),
        @CCD(displayOrder = 6)
        V06("App to vary an order – R", "App to vary an order – R"),
        @CCD(displayOrder = 7)
        V07("App to revoke an order - C", "App to revoke an order - C"),
        @CCD(displayOrder = 8)
        V08("App to revoke an order – R", "App to revoke an order – R"),
        @CCD(displayOrder = 9)
        V09("App to vary or revoke an order - C", "App to vary or revoke an order - C"),
        @CCD(displayOrder = 10)
        V10("App to vary or revoke an order – R", "App to vary or revoke an order – R"),
        @CCD(displayOrder = 11)
        V11(
                "App to extend time to comply to an order/directions – C",
                "App to extend time to comply to an order/directions – C"),
        @CCD(displayOrder = 12)
        V12(
                "App to extend time to comply to an order/directions – R",
                "App to extend time to comply to an order/directions – R"),
        @CCD(displayOrder = 13)
        V13("App to Order the R to do something", "App to Order the R to do something"),
        @CCD(displayOrder = 14)
        V14("App to Order the C to do something", "App to Order the C to do something"),
        @CCD(displayOrder = 15)
        V15("App to amend claim", "App to amend claim"),
        @CCD(displayOrder = 16)
        V16("App to amend response", "App to amend response"),
        @CCD(displayOrder = 17)
        V17("App for a Witness Order - C", "App for a Witness Order - C"),
        @CCD(displayOrder = 18)
        V18("App for a Witness Order - R", "App for a Witness Order - R"),
        @CCD(displayOrder = 19)
        V19("Disability Impact statement", "Disability Impact statement"),
        @CCD(displayOrder = 20)
        V20("R has not complied with an order - C", "R has not complied with an order - C"),
        @CCD(displayOrder = 21)
        V21("C has not complied with an order - R", "C has not complied with an order - R"),
        @CCD(displayOrder = 22)
        V22(
                "App to Strike out all or part of the claim",
                "App to Strike out all or part of the claim"),
        @CCD(displayOrder = 23)
        V23(
                "App to Strike out all or part of the response",
                "App to Strike out all or part of the response"),
        @CCD(displayOrder = 24)
        V24("Referral/Judicial Direction", "Referral/Judicial Direction"),
        @CCD(displayOrder = 25)
        V25("Change of party’s details", "Change of party’s details"),
        @CCD(displayOrder = 26)
        V26(
                "Contact the tribunal about something else - C",
                "Contact the tribunal about something else - C"),
        @CCD(displayOrder = 27)
        V27(
                "Contact the tribunal about something else - R",
                "Contact the tribunal about something else - R");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlDocumentCategories implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Starting a Claim", "Starting a Claim"),
        @CCD(displayOrder = 2)
        V02("Response to a Claim", "Response to a Claim"),
        @CCD(displayOrder = 3)
        V03("Initial Consideration", "Initial Consideration"),
        @CCD(displayOrder = 4)
        V04("Case Management", "Case Management"),
        @CCD(displayOrder = 5)
        V05("Employer Contract Claim", "Employer Contract Claim"),
        @CCD(displayOrder = 6)
        V06("Withdrawal/Settled", "Withdrawal/Settled"),
        @CCD(displayOrder = 7)
        V07("Hearings", "Hearings"),
        @CCD(displayOrder = 8)
        V08("Judgment and Reasons", "Judgment and Reasons"),
        @CCD(displayOrder = 9)
        V09("Reconsideration", "Reconsideration"),
        @CCD(displayOrder = 10)
        V10("Misc", "Misc"),
        @CCD(displayOrder = 11)
        V11("Legacy Document Names", "Legacy Document Names");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlDocumentTypeEnglandWales implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("ET1", "ET1"),
        @CCD(displayOrder = 2)
        V02("ET1 Attachment", "ET1 Attachment"),
        @CCD(displayOrder = 3)
        V03("ACAS Certificate", "ACAS Certificate"),
        @CCD(displayOrder = 4)
        V04("Acknowledgement of claim", "Acknowledgement of claim"),
        @CCD(displayOrder = 5)
        V05("Notice of a claim", "Notice of a claim"),
        @CCD(displayOrder = 6)
        V06("ET3", "ET3"),
        @CCD(displayOrder = 7)
        V07("ET3 Attachment", "ET3 Attachment"),
        @CCD(displayOrder = 8)
        V08("Claimant correspondence", "Claimant correspondence"),
        @CCD(displayOrder = 9)
        V09("Respondent correspondence", "Respondent correspondence"),
        @CCD(displayOrder = 10)
        V10("Notice of Hearing", "Notice of Hearing"),
        @CCD(displayOrder = 11)
        V11("Tribunal case file", "Tribunal case file"),
        @CCD(displayOrder = 12)
        V12("Tribunal correspondence", "Tribunal correspondence"),
        @CCD(displayOrder = 13)
        V13("Tribunal Order/Deposit Order", "Tribunal Order/Deposit Order"),
        @CCD(displayOrder = 14)
        V14("Tribunal Judgment/Reasons", "Tribunal Judgment/Reasons"),
        @CCD(displayOrder = 15)
        V15("Referral/Judicial direction", "Referral/Judicial direction"),
        @CCD(displayOrder = 16)
        V16("Rejection of claim", "Rejection of claim"),
        @CCD(displayOrder = 17)
        V17("Other ", "Other "),
        @CCD(displayOrder = 18)
        V18("Tse admin correspondence", "Tse admin correspondence");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlDocumentTypeScotland implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("ET1", "ET1"),
        @CCD(displayOrder = 2)
        V02("ET1 Attachment", "ET1 Attachment"),
        @CCD(displayOrder = 3)
        V03("ACAS Certificate", "ACAS Certificate"),
        @CCD(displayOrder = 4)
        V04("Notice of a claim", "Notice of a claim"),
        @CCD(displayOrder = 4)
        V05("Acknowledgement of claim", "Acknowledgement of claim"),
        @CCD(displayOrder = 5)
        V06("ET3", "ET3"),
        @CCD(displayOrder = 6)
        V07("ET3 Attachment", "ET3 Attachment"),
        @CCD(displayOrder = 7)
        V08("Claimant correspondence", "Claimant correspondence"),
        @CCD(displayOrder = 8)
        V09("Respondent correspondence", "Respondent correspondence"),
        @CCD(displayOrder = 9)
        V10("Notice of Hearing", "Notice of Hearing"),
        @CCD(displayOrder = 10)
        V11("Tribunal case file", "Tribunal case file"),
        @CCD(displayOrder = 11)
        V12("Tribunal correspondence", "Tribunal correspondence"),
        @CCD(displayOrder = 12)
        V13("Tribunal Order/Deposit Order", "Tribunal Order/Deposit Order"),
        @CCD(displayOrder = 13)
        V14("Tribunal Judgment/Reasons", "Tribunal Judgment/Reasons"),
        @CCD(displayOrder = 14)
        V15("Referral/Judicial direction", "Referral/Judicial direction"),
        @CCD(displayOrder = 15)
        V16("Rejection of claim", "Rejection of claim"),
        @CCD(displayOrder = 16)
        V17("Other ", "Other "),
        @CCD(displayOrder = 17)
        V18("Tse admin correspondence", "Tse admin correspondence");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlEmployerContractClaim implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Notice of Employer Contract Claim", "Notice of Employer Contract Claim"),
        @CCD(displayOrder = 2)
        V02("Acceptance of ECC response", "Acceptance of ECC response"),
        @CCD(displayOrder = 3)
        V03("Rejection of ECC response", "Rejection of ECC response");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlHearings implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("App to restrict publicity - C", "App to restrict publicity - C"),
        @CCD(displayOrder = 2)
        V02("App to restrict publicity - R", "App to restrict publicity - R"),
        @CCD(displayOrder = 3)
        V03("Anonymity Order", "Anonymity Order"),
        @CCD(displayOrder = 4)
        V04("Notice of Hearing", "Notice of Hearing"),
        @CCD(displayOrder = 5)
        V05("App to postpone – C", "App to postpone – C"),
        @CCD(displayOrder = 6)
        V06("App to postpone – R", "App to postpone – R"),
        @CCD(displayOrder = 7)
        V07("Hearing Bundle", "Hearing Bundle"),
        @CCD(displayOrder = 8)
        V08("Schedule of loss", "Schedule of loss"),
        @CCD(displayOrder = 9)
        V09("Counter Schedule of Loss", "Counter Schedule of Loss");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlInitialConsideration implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Initial Consideration", "Initial Consideration"),
        @CCD(displayOrder = 2)
        V02("Rule 27 Notice", "Rule 28 Notice"),
        @CCD(displayOrder = 3)
        V03("Rule 28 Notice", "Rule 29 Notice");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlJudgmentAndReasons implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Judgment", "Judgment"),
        @CCD(displayOrder = 2)
        V02("Judgment with Reasons", "Judgment with Reasons"),
        @CCD(displayOrder = 3)
        V03("Reasons", "Reasons"),
        @CCD(displayOrder = 4)
        V04("Extract of Judgment", "Extract of Judgment");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlMisc implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Certificate of Correction", "Certificate of Correction"),
        @CCD(displayOrder = 2)
        V02("Tribunal case file", "Tribunal case file"),
        @CCD(displayOrder = 3)
        V03("Other", "Other"),
        @CCD(displayOrder = 4)
        V04("Needs updating", "NEEDS UPDATING");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlPosition implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Awaiting appeal judgment", "Awaiting appeal judgment"),
        @CCD(displayOrder = 2)
        V02("Awaiting appeal order", "Awaiting appeal order"),
        @CCD(displayOrder = 3)
        V03(
                "Awaiting copy of improvement or prohibition notice",
                "Awaiting copy of improvement or prohibition notice"),
        @CCD(displayOrder = 4)
        V04("Awaiting default judgment", "Awaiting default judgment"),
        @CCD(displayOrder = 5)
        V05(
                "Awaiting default judgment reconsideration",
                "Awaiting default judgment reconsideration"),
        @CCD(displayOrder = 6)
        V06("Awaiting discovery/inspection", "Awaiting discovery/inspection"),
        @CCD(displayOrder = 7)
        V07("Awaiting disposal of claim application", "Awaiting disposal of claim application"),
        @CCD(displayOrder = 8)
        V08("Awaiting draft judgment from chairman", "Awaiting draft judgment from Judge"),
        @CCD(displayOrder = 9)
        V09("Awaiting ET3", "Awaiting ET3"),
        @CCD(displayOrder = 10)
        V10("Awaiting ET3 - extension of time granted", "Awaiting ET3 - extension of time granted"),
        @CCD(displayOrder = 11)
        V11("Awaiting ET3 (c)", "Awaiting ET3 (c)"),
        @CCD(displayOrder = 12)
        V12("Awaiting further and better particulars", "Awaiting further and better particulars"),
        @CCD(displayOrder = 13)
        V13("Awaiting instructions from chairman", "Awaiting instructions from Judge"),
        @CCD(displayOrder = 14)
        V14(
                "Awaiting judgment being sent to the parties, other",
                "Awaiting judgment being sent to the parties, other"),
        @CCD(displayOrder = 15)
        V15("Awaiting listing for Preliminary Hearing", "Awaiting listing for Preliminary Hearing"),
        @CCD(displayOrder = 16)
        V16(
                "Awaiting listing for preliminary hearing(CM)",
                "Awaiting listing for preliminary hearing(CM)"),
        @CCD(displayOrder = 17)
        V17("Awaiting listing Hearing", "Awaiting listing Hearing"),
        @CCD(displayOrder = 18)
        V18(
                "Awaiting listing reconsideration application",
                "Awaiting listing reconsideration application"),
        @CCD(displayOrder = 19)
        V19("Awaiting listing remedy/costs hearing", "Awaiting listing remedy/costs hearing"),
        @CCD(displayOrder = 20)
        V20("Awaiting outside proceeding", "Awaiting outside proceeding"),
        @CCD(displayOrder = 21)
        V21("Awaiting reply to a pre-listing stencil", "Awaiting reply to a pre-listing stencil"),
        @CCD(displayOrder = 22)
        V22("Awaiting settlement confirmation", "Awaiting settlement confirmation"),
        @CCD(displayOrder = 23)
        V23("Awaiting withdrawal confirmation", "Awaiting withdrawal confirmation"),
        @CCD(displayOrder = 24)
        V24("Awaiting written answer", "Awaiting written answer"),
        @CCD(displayOrder = 25)
        V25("Awaiting written reasons", "Awaiting written reasons"),
        @CCD(displayOrder = 26)
        V26("Case closed", "Case closed"),
        @CCD(displayOrder = 27)
        V27("Case input in error", "Case input in error"),
        @CCD(displayOrder = 28)
        V28("Case transferred - other country", "Case transferred - other country"),
        @CCD(displayOrder = 29)
        V29("Case transferred - same country", "Case transferred - same country"),
        @CCD(displayOrder = 30)
        V30("Conciliation paused", "Conciliation paused"),
        @CCD(displayOrder = 31)
        V31("Draft judgment received, awaiting typing", "Draft judgment received, awaiting typing"),
        @CCD(displayOrder = 32)
        V32(
                "Draft judgment typed, to chairman for amendment",
                "Draft judgment typed, to Judge for amendment"),
        @CCD(displayOrder = 33)
        V33("Draft with members", "Draft with members"),
        @CCD(displayOrder = 34)
        V34("ET1 Online submission", "ET1 Online submission"),
        @CCD(displayOrder = 35)
        V35("ET3 receiving attention", "ET3 receiving attention"),
        @CCD(displayOrder = 36)
        V36("ET3 referred to chairman", "ET3 referred to Judge"),
        @CCD(displayOrder = 37)
        V37("Fair copy, to chairman for signature", "Fair copy, to Judge for signature"),
        @CCD(displayOrder = 38)
        V38("Fixed period of conciliation", "Fixed period of conciliation"),
        @CCD(displayOrder = 39)
        V39(
                "Heard awaiting judgment being sent to the parties",
                "Heard awaiting judgment being sent to the parties"),
        @CCD(displayOrder = 40)
        V40("Listed for a Hearing", "Listed for a Hearing"),
        @CCD(displayOrder = 41)
        V41("Listed for a interim relief hearing", "Listed for a interim relief hearing"),
        @CCD(displayOrder = 42)
        V42("Listed for a preliminary hearing", "Listed for a preliminary hearing"),
        @CCD(displayOrder = 43)
        V43("Listed for a preliminary hearing(CM)", "Listed for a preliminary hearing(CM)"),
        @CCD(displayOrder = 44)
        V44("Listed for a reconsideration hearing", "Listed for a reconsideration hearing"),
        @CCD(displayOrder = 45)
        V45("Listed for a remedy/costs hearing", "Listed for a remedy/costs hearing"),
        @CCD(displayOrder = 46)
        V46("Live EAT case", "Live EAT case"),
        @CCD(displayOrder = 47)
        V47("Manually Created", "Manually created"),
        @CCD(displayOrder = 48)
        V48("Part heard awaiting listing", "Part heard awaiting listing"),
        @CCD(displayOrder = 49)
        V49("Part heard case relisted", "Part heard case relisted"),
        @CCD(displayOrder = 50)
        V50("Postponed by tribunal awaiting listing", "Postponed by tribunal awaiting listing"),
        @CCD(displayOrder = 51)
        V51("Received by Auto-Import", "Received by Auto-Import"),
        @CCD(displayOrder = 52)
        V52("REJECTED", "Rejected"),
        @CCD(displayOrder = 53)
        V53("Revised draft received, awaiting typing", "Revised draft received, awaiting typing"),
        @CCD(displayOrder = 54)
        V54(
                "Settled awaiting notification being sent to the parties",
                "Settled awaiting notification being sent to the parties"),
        @CCD(displayOrder = 55)
        V55("Signed fair copy received", "Signed fair copy received"),
        @CCD(displayOrder = 56)
        V56("Striking out warning issued", "Striking out warning issued"),
        @CCD(displayOrder = 57)
        V57(
                "Withdrawn awaiting notification being sent to the parties",
                "Withdrawn awaiting notification being sent to the parties");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlPositionCT implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Case transferred - same country", "Case transferred - same country"),
        @CCD(displayOrder = 2)
        V02("Case transferred - other country", "Case transferred - other country");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlReconsideration implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01(
                "App to have a Legal Officer decision considered afresh - C",
                "App to have a Legal Officer decision considered afresh - C"),
        @CCD(displayOrder = 2)
        V02(
                "App to have a Legal Officer decision considered afresh - R",
                "App to have a Legal Officer decision considered afresh - R"),
        @CCD(displayOrder = 3)
        V03(
                "App for a judgment to be reconsidered - C",
                "App for a judgment to be reconsidered - C"),
        @CCD(displayOrder = 4)
        V04(
                "App for a judgment to be reconsidered - R",
                "App for a judgment to be reconsidered - R");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlReferralSubject implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("ET1", "ET1"),
        @CCD(displayOrder = 2)
        V02("ET3/ECC", "ET3/ECC"),
        @CCD(displayOrder = 3)
        V03("Amend claim", "Amend claim"),
        @CCD(displayOrder = 4)
        V04("Amend response", "Amend response"),
        @CCD(displayOrder = 5)
        V05("Strike out all or part of claim", "Strike out all or part of claim"),
        @CCD(displayOrder = 6)
        V06("Strike out all or part of response", "Strike out all or part of response"),
        @CCD(displayOrder = 7)
        V07("Withdraw claim", "Withdraw claim"),
        @CCD(displayOrder = 8)
        V08("Orders", "Orders"),
        @CCD(displayOrder = 9)
        V09("Party not responded/compiled", "Party not responded/complied"),
        @CCD(displayOrder = 10)
        V10("Order other party", "Order other party"),
        @CCD(displayOrder = 11)
        V11("Rule 21", "Rule 22"),
        @CCD(displayOrder = 12)
        V12("Rule 50 application", "Rule 49 application"),
        @CCD(displayOrder = 13)
        V13("Order a witness to attend", "Order a witness to attend"),
        @CCD(displayOrder = 14)
        V14("Hearings", "Hearings"),
        @CCD(displayOrder = 15)
        V15("Postpone a hearing", "Postpone a hearing"),
        @CCD(displayOrder = 16)
        V16("Judgment", "Judgment"),
        @CCD(displayOrder = 17)
        V17("Reconsider decision", "Reconsider decision"),
        @CCD(displayOrder = 18)
        V18("Reconsider judgment", "Reconsider judgment"),
        @CCD(displayOrder = 19)
        V19("Extension of time request", "Extension of time request"),
        @CCD(displayOrder = 20)
        V20("Transfer request", "Transfer request"),
        @CCD(displayOrder = 21)
        V21("Initial Consideration", "Initial Consideration"),
        @CCD(displayOrder = 22)
        V22("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlResponseToAClaim implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("ET3", "ET3"),
        @CCD(displayOrder = 2)
        V02("ET3 Attachment", "ET3 Attachment"),
        @CCD(displayOrder = 3)
        V03("Response accepted", "Response accepted"),
        @CCD(displayOrder = 4)
        V04("Response rejected", "Response rejected"),
        @CCD(displayOrder = 5)
        V05("App to extend time to present a response", "App to extend time to present a response"),
        @CCD(displayOrder = 6)
        V06("ET3 Processing", "ET3 Processing");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlStage implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Stage 1", "Stage 1"),
        @CCD(displayOrder = 2)
        V02("Stage 2", "Stage 2"),
        @CCD(displayOrder = 3)
        V03("Stage 3", "Stage 3");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlStartingAClaim implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("ET1", "ET1"),
        @CCD(displayOrder = 2)
        V02("ET1 Attachment", "ET1 Attachment"),
        @CCD(displayOrder = 3)
        V03("ACAS Certificate", "ACAS Certificate"),
        @CCD(displayOrder = 4)
        V04("Notice of claim", "Notice of claim"),
        @CCD(displayOrder = 5)
        V05("Claim accepted", "Claim accepted"),
        @CCD(displayOrder = 6)
        V06("Claim rejected", "Claim rejected"),
        @CCD(displayOrder = 7)
        V07("Claim part rejected", "Claim part rejected"),
        @CCD(displayOrder = 8)
        V08("ET1 Vetting", "ET1 Vetting");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlTribunalOffice implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Bristol", "Bristol"),
        @CCD(displayOrder = 2)
        V02("Leeds", "Leeds"),
        @CCD(displayOrder = 3)
        V03("London Central", "London Central"),
        @CCD(displayOrder = 4)
        V04("London East", "London East"),
        @CCD(displayOrder = 5)
        V05("London South", "London South"),
        @CCD(displayOrder = 6)
        V06("Manchester", "Manchester"),
        @CCD(displayOrder = 7)
        V07("Midlands East", "Midlands East"),
        @CCD(displayOrder = 8)
        V08("Midlands West", "Midlands West"),
        @CCD(displayOrder = 9)
        V09("Newcastle", "Newcastle"),
        @CCD(displayOrder = 10)
        V10("Wales", "Wales"),
        @CCD(displayOrder = 11)
        V11("Watford", "Watford"),
        @CCD(displayOrder = 12)
        V12("Unassigned", "Unassigned");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlWithdrawalSettled implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Withdrawal of entire claim", "Withdrawal of entire claim"),
        @CCD(displayOrder = 2)
        V02("Withdrawal of part of claim", "Withdrawal of part of claim"),
        @CCD(displayOrder = 3)
        V03("Withdrawal of all or part of claim", "Withdrawal of all or part of claim"),
        @CCD(displayOrder = 4)
        V04("COT3", "COT3");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlBatchUpdate implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("batchUpdateType1", "Batch update based on flag criteria"),
        @CCD(displayOrder = 2)
        V02("batchUpdateType2", "Batch transfer of cases to another multiple or submultiple"),
        @CCD(displayOrder = 3)
        V03("batchUpdateType3", "Update cases with details of a case in the Multiple");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlScheduleDoc implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("List Cases", "List Cases"),
        @CCD(displayOrder = 2)
        V02("Multiple Schedule", "Multiple Schedule"),
        @CCD(displayOrder = 3)
        V03("Multiple Schedule (Detailed)", "Multiple Schedule (Detailed)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlSendNotificationCaseManagement implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Case management order", "Case management order"),
        @CCD(displayOrder = 2)
        V02("Request", "Request");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlSendNotificationDecision implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Granted", "Granted"),
        @CCD(displayOrder = 2)
        V02("Granted in part", "Granted in part"),
        @CCD(displayOrder = 3)
        V03("Refused", "Refused"),
        @CCD(displayOrder = 4)
        V04("Other", "Other");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlSendNotificationParties implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Both parties", "Both parties"),
        @CCD(displayOrder = 2)
        V02("Claimant only", "Claimant only"),
        @CCD(displayOrder = 3)
        V03("Respondent only", "Respondent only");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlSendNotificationSubject implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Claimant / Respondent details", "Claimant / Respondent details"),
        @CCD(displayOrder = 2)
        V02("Claim (ET1)", "Claim (ET1)"),
        @CCD(displayOrder = 3)
        V03("Response (ET3)", "Response (ET3)"),
        @CCD(displayOrder = 4)
        V04("Hearing", "Hearing"),
        @CCD(displayOrder = 5)
        V05("Case management orders / requests", "Case management orders / requests"),
        @CCD(displayOrder = 6)
        V06("Judgment", "Judgment"),
        @CCD(displayOrder = 7)
        V07("Employer Contract Claim", "Employer Contract Claim"),
        @CCD(displayOrder = 8)
        V08("Other (General correspondence)", "Other (General correspondence)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlSendNotificationWhoCaseOrder implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Legal officer", "Legal officer"),
        @CCD(displayOrder = 2)
        V02("Judge", "Judge");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FlSendNotificationWhoMadeJudgement implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Judge", "Judge"),
        @CCD(displayOrder = 2)
        V02("Legal officer", "Legal officer");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FrlReferCaseTo implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Admin", "Admin"),
        @CCD(displayOrder = 2)
        V02("Judge", "Judge"),
        @CCD(displayOrder = 3)
        V03("Legal officer", "Legal officer");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FrlLiveCases implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Yes", "Yes"),
        @CCD(displayOrder = 2)
        V02("No", "No - all cases");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum FrlSendNotificationRequestMadeBy implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Legal officer", "Legal officer"),
        @CCD(displayOrder = 2)
        V02("Judge", "Judge"),
        @CCD(displayOrder = 3)
        V03("Caseworker", "Caseworker");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListimageRendering implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("opaque", "Opaque"),
        @CCD(displayOrder = 2)
        V02("translucent", "Translucent");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListimageRenderingLocation implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("allPages", "Image on all pages of the Document"),
        @CCD(displayOrder = 2)
        V02("firstPage", "Image on the First Page of each document");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum MslSelectLabels implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("claimantAddressLabel", "Claimant"),
        @CCD(displayOrder = 2)
        V02("claimantRepAddressLabel", "Claimant Representative"),
        @CCD(displayOrder = 3)
        V03("respondentsAddressLabel", "Respondent(s)"),
        @CCD(displayOrder = 4)
        V04("respondentsRepsAddressLabel", "Respondent Representative(s)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum MslYes implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Yes", "Yes");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum MslConfirmCloseReferral implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Yes", "Yes");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListmultiplesAmendment implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Amend multiple name", "Amend multiple name"),
        @CCD(displayOrder = 2)
        V02("Amend lead case", "Amend lead case"),
        @CCD(displayOrder = 3)
        V03("Add cases to multiple", "Add cases to multiple"),
        @CCD(displayOrder = 4)
        V04("Remove cases from multiple", "Remove cases from multiple"),
        @CCD(displayOrder = 5)
        V05("Amend multiple note", "Amend multiple note");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListnotifyMultiple implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Lead case", "Parties from lead case"),
        @CCD(displayOrder = 2)
        V02("Lead and sub cases", "Parties from lead case and sub cases"),
        @CCD(displayOrder = 3)
        V03("Selected cases", "Parties from selected cases (batch processing)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListpageNumberFormat implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("pageRange", "Page Range"),
        @CCD(displayOrder = 2)
        V02("numberOfPages", "Number Of Pages");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListpaginationStyle implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("off", "Off"),
        @CCD(displayOrder = 2)
        V02("topLeft", "Top Left"),
        @CCD(displayOrder = 3)
        V03("topCenter", "Top Center"),
        @CCD(displayOrder = 4)
        V04("topRight", "Top Right"),
        @CCD(displayOrder = 5)
        V05("bottomLeft", "Bottom Left"),
        @CCD(displayOrder = 6)
        V06("bottomCenter", "Bottom Center"),
        @CCD(displayOrder = 7)
        V07("bottomRight", "Bottom Right");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListsendNotificationEccQuestion implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Notice of Employer Contract Claim", "Notice of Employer Contract Claim"),
        @CCD(displayOrder = 2)
        V02("Acceptance of ECC response", "Acceptance of ECC response"),
        @CCD(displayOrder = 3)
        V03("Rejection of ECC response", "Rejection of ECC response");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListsendNotificationNotifyAll implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Both parties", "All parties from lead case and sub cases"),
        @CCD(displayOrder = 2)
        V02("Claimant only", "All Claimants from lead case and sub cases"),
        @CCD(displayOrder = 3)
        V03("Respondent only", "All Respondents from lead case and sub cases");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListsendNotificationNotifyLead implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Both parties", "Both parties from Lead Case"),
        @CCD(displayOrder = 2)
        V02("Claimant only", "Claimant (Lead Case only)"),
        @CCD(displayOrder = 3)
        V03("Respondent only", "Respondent (Lead Case only)");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListsendNotificationNotifySelected implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Claimant only", "Claimants from selected cases"),
        @CCD(displayOrder = 2)
        V02("Respondent only", "Respondents from selected cases"),
        @CCD(displayOrder = 3)
        V03("Both parties", "All parties from selected cases");

        private final String code;
        private final String label;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ListsendNotificationResponseTribunal implements HasLabel, HasCode {
        @CCD(displayOrder = 1)
        V01("Yes - view document for details", "Yes - view document for details"),
        @CCD(displayOrder = 2)
        V02("No", "No");

        private final String code;
        private final String label;
    }
}
