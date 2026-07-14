package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import uk.gov.hmcts.et.common.model.ccd.SingleRole;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.ComplexFieldSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EventFieldSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EventSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.GrantSpec;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({
    "checkstyle:LineLength",
    "checkstyle:RightCurlyAlone",
    "PMD.AvoidDuplicateLiterals"
})
final class SingleReferralLifecycleRows {
    private static final int CFTLIB_ENGLAND_WALES = 1;
    private static final int CFTLIB_SCOTLAND = 2;
    private static final int CFTLIB = 3;
    private static final int PROD_ENGLAND_WALES = 4;
    private static final int ENGLAND_WALES = 5;
    private static final int PROD_SCOTLAND = 8;
    private static final int SCOTLAND = 10;
    private static final int PROD = 12;
    private static final int ALL = 15;

    private static final String LOCALHOST = "http://localhost:8081";
    private static final String PRODUCTION =
            "http://et-cos-prod.service.core-compute-prod.internal";

    private static final String CREATE_REFERRAL = "createReferral";
    private static final String UPDATE_REFERRAL = "updateReferral";
    private static final String REPLY_TO_REFERRAL = "replyToReferral";
    private static final String CLOSE_REFERRAL = "closeReferral";

    static final EventSpec[] EVENTS = referralEvents();
    static final EventFieldSpec[] EVENT_FIELDS = eventFields();
    static final ComplexFieldSpec[] COMPLEX_FIELDS = complexFields();
    static final GrantSpec[] EVENT_GRANTS = eventGrants();

    private SingleReferralLifecycleRows() {}

    private static EventSpec[] referralEvents() {
        List<EventSpec> rows = new ArrayList<>();
        addEvents(rows, CFTLIB_ENGLAND_WALES, LOCALHOST, false);
        addEvents(rows, CFTLIB_SCOTLAND, LOCALHOST, true);
        addEvents(rows, PROD_ENGLAND_WALES, PRODUCTION, false);
        addEvents(rows, PROD_SCOTLAND, PRODUCTION, true);
        return rows.toArray(EventSpec[]::new);
    }

    private static void addEvents(List<EventSpec> rows, int mask, String base, boolean scotland) {
        rows.add(
                event(
                        mask,
                        UPDATE_REFERRAL,
                        "Update Referral",
                        "Update Referral",
                        scotland ? 47 : 49,
                        base + "/updateReferral",
                        null));
        rows.add(
                event(
                        mask,
                        CREATE_REFERRAL,
                        "Create Referral",
                        scotland ? "Referral" : "Create Referral",
                        scotland ? 46 : 48,
                        base + "/createReferral",
                        "completeCreateReferral"));
        rows.add(
                event(
                        mask,
                        REPLY_TO_REFERRAL,
                        "Reply to Referral",
                        "Refer to admin, legal officer or judge",
                        scotland ? 48 : 50,
                        base + "/replyReferral",
                        "completeReplyToReferral"));
        rows.add(
                event(
                        mask,
                        CLOSE_REFERRAL,
                        "Close Referral",
                        "Close referral",
                        scotland ? 49 : 51,
                        base + "/closeReferral",
                        "completeCloseReferral"));
    }

    private static EventSpec event(
            int mask,
            String eventId,
            String name,
            String description,
            int displayOrder,
            String callbackRoot,
            String submittedPath) {
        return new EventSpec(
                mask,
                eventId,
                name,
                description,
                displayOrder,
                "*",
                "*",
                "caseType =\"dummy\"",
                "Y",
                "N",
                "Y",
                false,
                null,
                callbackRoot + "/aboutToStart",
                callbackRoot + "/aboutToSubmit",
                submittedPath == null ? null : callbackRoot + "/" + submittedPath);
    }

    private static EventFieldSpec[] eventFields() {
        List<EventFieldSpec> rows = new ArrayList<>();
        addCreateFields(rows);
        addUpdateFields(rows);
        addReplyFields(rows);
        addCloseFields(rows);
        return rows.toArray(EventFieldSpec[]::new);
    }

    private static void addCreateFields(List<EventFieldSpec> rows) {
        rows.add(
                field(
                        CFTLIB,
                        CREATE_REFERRAL,
                        "referralHearingDetails",
                        "READONLY",
                        1,
                        1,
                        true,
                        "Y",
                        "referralHearingDetailsLabel=\"dummy\"",
                        null,
                        "Refer to admin, legal officer or judge",
                        LOCALHOST + "/createReferral/validateReferentEmail",
                        null));
        rows.add(
                field(
                        PROD,
                        CREATE_REFERRAL,
                        "referralHearingDetails",
                        "READONLY",
                        1,
                        1,
                        true,
                        "Y",
                        "referralHearingDetailsLabel=\"dummy\"",
                        null,
                        "Refer to admin, legal officer or judge",
                        PRODUCTION + "/createReferral/validateReferentEmail",
                        null));
        rows.add(createField("referralHearingDetailsLabel", "READONLY", 2, null, null, null));
        rows.add(createField("referCaseTo", "MANDATORY", 3, "Y", null, null));
        rows.add(
                field(
                        ENGLAND_WALES,
                        CREATE_REFERRAL,
                        "referentEmail",
                        "OPTIONAL",
                        1,
                        13,
                        true,
                        "Y",
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        SCOTLAND,
                        CREATE_REFERRAL,
                        "referentEmail",
                        "OPTIONAL",
                        1,
                        4,
                        true,
                        "Y",
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(createField("isUrgent", "MANDATORY", 5, "Y", null, null));
        rows.add(createField("referralSubject", "MANDATORY", 6, "Y", null, null));
        rows.add(
                field(
                        ALL,
                        CREATE_REFERRAL,
                        "referralSubjectSpecify",
                        "MANDATORY",
                        1,
                        7,
                        true,
                        "Y",
                        "referralSubject =\"Other\"",
                        "Yes",
                        null,
                        null,
                        null));
        rows.add(createField("referralDetails", "MANDATORY", 8, "Y", null, null));
        rows.add(createField("referralDocument", "COMPLEX", 9, "Y", null, null));
        rows.add(createField("referralInstruction", "OPTIONAL", 10, "Y", null, null));
        rows.add(
                field(
                        ALL,
                        CREATE_REFERRAL,
                        "referralCollection",
                        "COMPLEX",
                        1,
                        11,
                        true,
                        "N",
                        "referralHearingDetailsLabel=\"dummy\"",
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ENGLAND_WALES,
                        CREATE_REFERRAL,
                        "nextListedDate",
                        "OPTIONAL",
                        1,
                        12,
                        true,
                        "N",
                        "referralHearingDetailsLabel=\"dummy\"",
                        null,
                        null,
                        null,
                        "Y"));
        rows.add(
                field(
                        SCOTLAND,
                        CREATE_REFERRAL,
                        "nextListedDate",
                        "OPTIONAL",
                        1,
                        11,
                        true,
                        "N",
                        "referralHearingDetailsLabel=\"dummy\"",
                        null,
                        null,
                        null,
                        "Y"));
    }

    private static EventFieldSpec createField(
            String fieldId,
            String context,
            int order,
            String showSummary,
            String condition,
            String retainHidden) {
        return field(
                ALL,
                CREATE_REFERRAL,
                fieldId,
                context,
                1,
                order,
                true,
                showSummary,
                condition,
                retainHidden,
                null,
                null,
                null);
    }

    private static void addUpdateFields(List<EventFieldSpec> rows) {
        addUpdateSelection(rows, CFTLIB_ENGLAND_WALES, LOCALHOST, true);
        addUpdateSelection(rows, CFTLIB_SCOTLAND, LOCALHOST, false);
        addUpdateSelection(rows, PROD_ENGLAND_WALES, PRODUCTION, true);
        addUpdateSelection(rows, PROD_SCOTLAND, PRODUCTION, false);
        rows.add(
                field(
                        ALL,
                        UPDATE_REFERRAL,
                        "referralCollection",
                        "COMPLEX",
                        1,
                        3,
                        true,
                        "Y",
                        "selectReferral=\"dummy\"",
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPDATE_REFERRAL,
                        "nextListedDate",
                        "OPTIONAL",
                        1,
                        4,
                        true,
                        null,
                        "selectReferral=\"dummy\"",
                        null,
                        null,
                        null,
                        "Y"));
        rows.add(
                updateField(
                        "referralHearingDetails",
                        "READONLY",
                        1,
                        "referralHearingDetailsLabel=\"dummy\"",
                        null,
                        "Refer to admin, legal officer or judge"));
        rows.add(updateField("referralHearingDetailsLabel", "READONLY", 2, null, null, null));
        rows.add(updateField("updateReferCaseTo", "MANDATORY", 3, null, null, null));
        rows.add(updateField("updateReferentEmail", "OPTIONAL", 4, null, null, null));
        rows.add(updateField("updateIsUrgent", "MANDATORY", 5, null, null, null));
        rows.add(updateField("updateReferralSubject", "MANDATORY", 6, null, null, null));
        rows.add(updateField("updateReferralDetails", "MANDATORY", 7, null, null, null));
        rows.add(updateField("updateReferralDocument", "COMPLEX", 8, null, null, null));
        rows.add(
                field(
                        ENGLAND_WALES,
                        UPDATE_REFERRAL,
                        "updateReferralSubjectSpecify",
                        "MANDATORY",
                        2,
                        9,
                        true,
                        null,
                        "updateReferralSubject =\"Other\"",
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        SCOTLAND,
                        UPDATE_REFERRAL,
                        "updateReferralSubjectSpecify",
                        "MANDATORY",
                        2,
                        9,
                        true,
                        null,
                        "updateReferralSubject =\"Other\"",
                        "Yes",
                        null,
                        null,
                        null));
        rows.add(updateField("updateReferralInstruction", "OPTIONAL", 10, null, null, null));
    }

    private static void addUpdateSelection(
            List<EventFieldSpec> rows, int mask, String base, boolean pageColumn) {
        rows.add(
                field(
                        mask,
                        UPDATE_REFERRAL,
                        "selectReferral",
                        "MANDATORY",
                        1,
                        1,
                        pageColumn,
                        null,
                        null,
                        null,
                        "Update Referral",
                        base + "/updateReferral/initHearingAndReferralDetails",
                        null));
    }

    private static EventFieldSpec updateField(
            String fieldId,
            String context,
            int order,
            String condition,
            String retainHidden,
            String pageLabel) {
        return field(
                ALL,
                UPDATE_REFERRAL,
                fieldId,
                context,
                2,
                order,
                true,
                null,
                condition,
                retainHidden,
                pageLabel,
                null,
                null);
    }

    private static void addReplyFields(List<EventFieldSpec> rows) {
        rows.add(
                field(
                        CFTLIB,
                        REPLY_TO_REFERRAL,
                        "selectReferral",
                        "MANDATORY",
                        1,
                        1,
                        false,
                        null,
                        null,
                        null,
                        "Refer to admin, legal officer or judge",
                        LOCALHOST + "/replyReferral/initHearingAndReferralDetails",
                        null));
        rows.add(
                field(
                        PROD,
                        REPLY_TO_REFERRAL,
                        "selectReferral",
                        "MANDATORY",
                        1,
                        1,
                        false,
                        null,
                        null,
                        null,
                        "Refer to admin, legal officer or judge",
                        PRODUCTION + "/replyReferral/initHearingAndReferralDetails",
                        null));
        rows.add(replyField("isJudge", "READONLY", 1, 2, null, "selectReferral=\"dummy\"", null));
        rows.add(
                replyField(
                        "referralCollection",
                        "COMPLEX",
                        1,
                        3,
                        "N",
                        "selectReferral=\"dummy\"",
                        null));
        rows.add(
                field(
                        ALL,
                        REPLY_TO_REFERRAL,
                        "nextListedDate",
                        "OPTIONAL",
                        1,
                        3,
                        true,
                        "N",
                        "selectReferral=\"dummy\"",
                        null,
                        null,
                        null,
                        "Y"));
        rows.add(
                field(
                        CFTLIB,
                        REPLY_TO_REFERRAL,
                        "replyToReferralDcfLink",
                        "READONLY",
                        2,
                        1,
                        true,
                        null,
                        "replyToReferralDcfLinkLabel=\"dummy\"",
                        null,
                        "Refer to admin, legal officer or judge",
                        LOCALHOST + "/replyReferral/validateReplyToEmail",
                        null));
        rows.add(
                field(
                        PROD,
                        REPLY_TO_REFERRAL,
                        "replyToReferralDcfLink",
                        "READONLY",
                        2,
                        1,
                        true,
                        null,
                        "replyToReferralDcfLinkLabel=\"dummy\"",
                        null,
                        "Refer to admin, legal officer or judge",
                        PRODUCTION + "/replyReferral/validateReplyToEmail",
                        null));
        rows.add(replyField("replyToReferralDcfLinkLabel", "READONLY", 2, 2, null, null, null));
        rows.add(
                replyField(
                        "hearingAndReferralDetails",
                        "READONLY",
                        2,
                        3,
                        null,
                        "hearingAndReferralDetailsLabel=\"dummy\"",
                        null));
        rows.add(replyField("hearingAndReferralDetailsLabel", "READONLY", 2, 4, null, null, null));
        rows.add(replyField("directionTo", "MANDATORY", 2, 5, "Y", "isJudge=\"True\"", null));
        rows.add(replyField("replyTo", "MANDATORY", 2, 6, "Y", "isJudge=\"False\"", null));
        rows.add(replyField("replyToEmailAddress", "OPTIONAL", 2, 7, "Y", null, null));
        rows.add(replyField("isUrgentReply", "MANDATORY", 2, 8, "Y", null, null));
        rows.add(replyField("directionDetails", "MANDATORY", 2, 9, "Y", "isJudge=\"True\"", null));
        rows.add(replyField("replyDetails", "MANDATORY", 2, 10, "Y", "isJudge=\"False\"", null));
        rows.add(replyField("replyDocument", "COMPLEX", 2, 11, "Y", null, null));
        rows.add(replyField("replyGeneralNotes", "OPTIONAL", 2, 12, "Y", null, null));
    }

    private static EventFieldSpec replyField(
            String fieldId,
            String context,
            int page,
            int order,
            String showSummary,
            String condition,
            String pageLabel) {
        return field(
                ALL,
                REPLY_TO_REFERRAL,
                fieldId,
                context,
                page,
                order,
                true,
                showSummary,
                condition,
                null,
                pageLabel,
                null,
                null);
    }

    private static void addCloseFields(List<EventFieldSpec> rows) {
        rows.add(
                field(
                        CFTLIB,
                        CLOSE_REFERRAL,
                        "selectReferral",
                        "MANDATORY",
                        1,
                        1,
                        false,
                        null,
                        null,
                        null,
                        "Close referral",
                        LOCALHOST + "/closeReferral/initHearingAndReferralDetails",
                        null));
        rows.add(
                field(
                        PROD,
                        CLOSE_REFERRAL,
                        "selectReferral",
                        "MANDATORY",
                        1,
                        1,
                        false,
                        null,
                        null,
                        null,
                        "Close referral",
                        PRODUCTION + "/closeReferral/initHearingAndReferralDetails",
                        null));
        rows.add(
                closeField(
                        "closeReferralHearingDetails",
                        "READONLY",
                        1,
                        null,
                        "closeReferralHearingDetailsLabel=\"dummy\"",
                        "Close referral"));
        rows.add(closeField("closeReferralHearingDetailsLabel", "READONLY", 2, null, null, null));
        rows.add(closeField("confirmCloseReferral", "MANDATORY", 3, "Y", null, null));
        rows.add(closeField("closeReferralGeneralNotes", "OPTIONAL", 4, "Y", null, null));
    }

    private static EventFieldSpec closeField(
            String fieldId,
            String context,
            int order,
            String showSummary,
            String condition,
            String pageLabel) {
        return field(
                ALL,
                CLOSE_REFERRAL,
                fieldId,
                context,
                2,
                order,
                true,
                showSummary,
                condition,
                null,
                pageLabel,
                null,
                null);
    }

    private static ComplexFieldSpec[] complexFields() {
        List<ComplexFieldSpec> rows = new ArrayList<>();
        addUpdateComplexFields(rows);
        addCreateComplexFields(rows);
        addReplyComplexFields(rows);
        return rows.toArray(ComplexFieldSpec[]::new);
    }

    private static void addUpdateComplexFields(List<ComplexFieldSpec> rows) {
        addReferralElement(
                rows, ALL, UPDATE_REFERRAL, "UpdateReferralDetails", "referralSubject", 1, false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "referralSubjectSpecify",
                2,
                false);
        addReferralElement(
                rows, ALL, UPDATE_REFERRAL, "UpdateReferralDetails", "referCaseTo", 3, false);
        addReferralElement(
                rows, ALL, UPDATE_REFERRAL, "UpdateReferralDetails", "referralNumber", 4, true);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection",
                5,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferralNumber",
                6,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferralHearingDate",
                7,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferCaseTo",
                8,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferentEmail",
                9,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferralDetails",
                10,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateIsUrgent",
                11,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferralSubject",
                12,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferralSubjectSpecify",
                13,
                false);
        rows.add(
                element(
                        ALL,
                        "UpdateReferralDetails",
                        UPDATE_REFERRAL,
                        "referralCollection",
                        "updateReferralCollection.updateReferralDocument",
                        "OPTIONAL",
                        14,
                        " ",
                        null));
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferralInstruction",
                15,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferredBy",
                16,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferralDate",
                17,
                false);
        addReferralElement(
                rows,
                ALL,
                UPDATE_REFERRAL,
                "UpdateReferralDetails",
                "updateReferralCollection.updateReferralDateTime",
                18,
                false);
        rows.add(
                documentElement(
                        PROD,
                        UPDATE_REFERRAL,
                        "updateReferralDocument",
                        "uploadedDocument",
                        2,
                        "Document"));
        rows.add(
                documentElement(
                        PROD,
                        UPDATE_REFERRAL,
                        "updateReferralDocument",
                        "shortDescription",
                        3,
                        "Short description of document"));
    }

    private static void addCreateComplexFields(List<ComplexFieldSpec> rows) {
        rows.add(
                documentElement(
                        PROD,
                        CREATE_REFERRAL,
                        "referralDocument",
                        "uploadedDocument",
                        1,
                        "Document"));
        rows.add(
                documentElement(
                        PROD,
                        CREATE_REFERRAL,
                        "referralDocument",
                        "shortDescription",
                        2,
                        "Short description of document"));
        addReferralElement(rows, PROD, CREATE_REFERRAL, "ReferralDetails", "referCaseTo", 1, false);
        addReferralElement(
                rows, PROD, CREATE_REFERRAL, "ReferralDetails", "referralSubject", 2, false);
        addReferralElement(
                rows, PROD, CREATE_REFERRAL, "ReferralDetails", "referralSubjectSpecify", 2, false);
        addReferralElement(rows, PROD, CREATE_REFERRAL, "ReferralDetails", "isUrgent", 3, false);
        addReferralElement(
                rows, PROD, CREATE_REFERRAL, "ReferralDetails", "referralNumber", 4, false);
    }

    private static void addReplyComplexFields(List<ComplexFieldSpec> rows) {
        rows.add(
                documentElement(
                        PROD,
                        REPLY_TO_REFERRAL,
                        "replyDocument",
                        "uploadedDocument",
                        1,
                        "Document"));
        rows.add(
                documentElement(
                        PROD,
                        REPLY_TO_REFERRAL,
                        "replyDocument",
                        "shortDescription",
                        2,
                        "Short description of document"));
        addReferralElement(
                rows, PROD, REPLY_TO_REFERRAL, "ReferralReplyDetails", "referralSubject", 1, false);
        addReferralElement(
                rows,
                PROD,
                REPLY_TO_REFERRAL,
                "ReferralReplyDetails",
                "referralSubjectSpecify",
                2,
                false);
        addReferralElement(
                rows, PROD, REPLY_TO_REFERRAL, "ReferralReplyDetails", "referCaseTo", 2, false);
        addReferralElement(
                rows, PROD, REPLY_TO_REFERRAL, "ReferralReplyDetails", "referralNumber", 3, true);
        addReferralElement(
                rows,
                PROD,
                REPLY_TO_REFERRAL,
                "ReferralReplyDetails",
                "referralReplyCollection",
                4,
                false);
        addReferralElement(
                rows,
                PROD,
                REPLY_TO_REFERRAL,
                "ReferralReplyDetails",
                "referralReplyCollection.directionTo",
                5,
                false);
        addReferralElement(
                rows,
                PROD,
                REPLY_TO_REFERRAL,
                "ReferralReplyDetails",
                "referralReplyCollection.isUrgentReply",
                6,
                false);
        addReferralElement(
                rows,
                PROD,
                REPLY_TO_REFERRAL,
                "ReferralReplyDetails",
                "referralReplyCollection.replyDateTime",
                7,
                false);
        addReferralElement(
                rows,
                PROD,
                REPLY_TO_REFERRAL,
                "ReferralReplyDetails",
                "referralReplyCollection.referralSubject",
                8,
                false);
        addReferralElement(
                rows,
                PROD,
                REPLY_TO_REFERRAL,
                "ReferralReplyDetails",
                "referralReplyCollection.referralNumber",
                9,
                false);
    }

    private static void addReferralElement(
            List<ComplexFieldSpec> rows,
            int mask,
            String eventId,
            String rowId,
            String elementCode,
            int order,
            boolean readonly) {
        rows.add(
                element(
                        mask,
                        rowId,
                        eventId,
                        "referralCollection",
                        elementCode,
                        readonly ? "READONLY" : "OPTIONAL",
                        order,
                        " ",
                        "Y"));
    }

    private static ComplexFieldSpec documentElement(
            int mask,
            String eventId,
            String caseFieldId,
            String elementCode,
            int order,
            String label) {
        return element(
                mask,
                caseFieldId,
                eventId,
                caseFieldId,
                elementCode,
                "OPTIONAL",
                order,
                label,
                null);
    }

    private static GrantSpec[] eventGrants() {
        List<GrantSpec> rows = new ArrayList<>();
        addTribunalGrants(rows, CREATE_REFERRAL, true);
        addTribunalGrants(rows, UPDATE_REFERRAL, true);
        addTribunalGrants(rows, REPLY_TO_REFERRAL, true);
        addTribunalGrants(rows, CLOSE_REFERRAL, false);
        return rows.toArray(GrantSpec[]::new);
    }

    private static void addTribunalGrants(
            List<GrantSpec> rows, String eventId, boolean workAllocation) {
        rows.add(new GrantSpec(ALL, eventId, SingleRole.CASEWORKER_EMPLOYMENT, "R"));
        rows.add(new GrantSpec(ALL, eventId, SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"));
        rows.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        eventId,
                        SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
                        "CRU"));
        rows.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        eventId,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
                        "CRU"));
        rows.add(new GrantSpec(ALL, eventId, SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        if (workAllocation) {
            rows.add(
                    new GrantSpec(
                            ALL, eventId, SingleRole.CASEWORKER_WA_TASK_CONFIGURATION, "CRU"));
        }
        rows.add(
                new GrantSpec(SCOTLAND, eventId, SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        rows.add(
                new GrantSpec(
                        SCOTLAND,
                        eventId,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
                        "CRU"));
    }

    private static EventFieldSpec field(
            int mask,
            String eventId,
            String fieldId,
            String context,
            int page,
            int order,
            boolean pageColumn,
            String showSummary,
            String condition,
            String retainHidden,
            String pageLabel,
            String midEvent,
            String publish) {
        return new EventFieldSpec(
                mask,
                eventId,
                fieldId,
                context,
                Integer.toString(page),
                page,
                order,
                pageColumn,
                showSummary,
                condition,
                retainHidden,
                null,
                pageLabel,
                midEvent,
                publish);
    }

    private static ComplexFieldSpec element(
            int mask,
            String rowId,
            String eventId,
            String caseFieldId,
            String elementCode,
            String context,
            int order,
            String label,
            String publish) {
        return new ComplexFieldSpec(
                mask,
                rowId,
                eventId,
                caseFieldId,
                elementCode,
                context,
                order,
                label,
                null,
                null,
                null,
                null,
                publish);
    }
}
