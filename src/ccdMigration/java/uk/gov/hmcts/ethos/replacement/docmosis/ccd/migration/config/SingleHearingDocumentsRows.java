package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SingleComplexTypes;
import uk.gov.hmcts.et.common.model.ccd.SingleRole;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.ComplexFieldSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EventFieldSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.EventSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.GrantSpec;
import uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.SingleDefinitionRows.StandaloneComplexSpec;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({
    "checkstyle:LineLength",
    "checkstyle:RightCurlyAlone",
    "PMD.AvoidDuplicateLiterals"
})
final class SingleHearingDocumentsRows {
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

    private static final String PREPARE_DOCUMENTS = "bundlesRespondentPrepareDoc";
    private static final String SUBMIT_CLAIMANT_BUNDLES = "SUBMIT_CLAIMANT_BUNDLES";
    private static final String REMOVE_HEARING_BUNDLES = "removeHearingBundles";
    private static final String UPLOAD_HEARING_DOCUMENTS = "uploadHearingDocuments";
    private static final String CREATE_DCF = "createDcf";
    private static final String ASYNC_STITCHING_COMPLETE = "asyncStitchingComplete";

    private static final SingleRole[] RESPONDENT_SOLICITORS = {
        SingleRole.SOLICITORA,
        SingleRole.SOLICITORB,
        SingleRole.SOLICITORC,
        SingleRole.SOLICITORD,
        SingleRole.SOLICITORE,
        SingleRole.SOLICITORF,
        SingleRole.SOLICITORG,
        SingleRole.SOLICITORH,
        SingleRole.SOLICITORI,
        SingleRole.SOLICITORJ
    };

    static final GrantSpec[] EVENT_GRANTS = eventGrants();

    static final EventSpec[] EVENTS = {
        prepareDocuments(
                CFTLIB_ENGLAND_WALES, LOCALHOST, "[STATE]!=\"AWAITING_SUBMISSION_TO_HMCTS\""),
        prepareDocuments(CFTLIB_SCOTLAND, LOCALHOST, " "),
        prepareDocuments(
                PROD_ENGLAND_WALES, PRODUCTION, "[STATE]!=\"AWAITING_SUBMISSION_TO_HMCTS\""),
        prepareDocuments(PROD_SCOTLAND, PRODUCTION, " "),
        submitClaimantBundles(),
        removeHearingBundles(CFTLIB_ENGLAND_WALES, LOCALHOST, "Remove Hearing Documents"),
        removeHearingBundles(CFTLIB_SCOTLAND, LOCALHOST, "Remove hearing documents"),
        removeHearingBundles(PROD_ENGLAND_WALES, PRODUCTION, "Remove Hearing Documents"),
        removeHearingBundles(PROD_SCOTLAND, PRODUCTION, "Remove hearing documents"),
        uploadHearingDocuments(CFTLIB, LOCALHOST),
        uploadHearingDocuments(PROD, PRODUCTION),
        createDcf(CFTLIB, LOCALHOST),
        createDcf(PROD, PRODUCTION),
        asyncStitchingComplete(CFTLIB, LOCALHOST),
        asyncStitchingComplete(PROD, PRODUCTION)
    };

    static final EventFieldSpec[] EVENT_FIELDS = {
        field(
                ALL,
                PREPARE_DOCUMENTS,
                "bundlesRespondentPrepareDocNotesShow",
                "OPTIONAL",
                1,
                1,
                false,
                null,
                "bundlesRespondentPrepareDocNotes1=\"dummy\"",
                null,
                "Prepare and submit documents for a hearing",
                null),
        field(
                ALL,
                PREPARE_DOCUMENTS,
                "bundlesRespondentPrepareDocNotes1",
                "READONLY",
                1,
                2,
                false,
                null,
                null,
                null,
                null,
                null),
        field(
                ALL,
                PREPARE_DOCUMENTS,
                "bundlesRespondentPrepareDocNotes2",
                "READONLY",
                1,
                3,
                false,
                null,
                "bundlesRespondentPrepareDocNotesShow=\"Yes\"",
                null,
                null,
                null),
        field(
                CFTLIB,
                PREPARE_DOCUMENTS,
                "bundlesRespondentAgreedDocWith",
                "MANDATORY",
                2,
                1,
                false,
                "Y",
                null,
                null,
                "Have you agreed these documents with the other party?",
                LOCALHOST + "/bundlesRespondent/midPopulateHearings"),
        field(
                PROD,
                PREPARE_DOCUMENTS,
                "bundlesRespondentAgreedDocWith",
                "MANDATORY",
                2,
                1,
                false,
                "Y",
                null,
                null,
                "Have you agreed these documents with the other party?",
                PRODUCTION + "/bundlesRespondent/midPopulateHearings"),
        field(
                ALL,
                PREPARE_DOCUMENTS,
                "bundlesRespondentAgreedDocWithBut",
                "MANDATORY",
                2,
                2,
                false,
                "Y",
                "bundlesRespondentAgreedDocWith=\"But\"",
                "No",
                null,
                null),
        field(
                ALL,
                PREPARE_DOCUMENTS,
                "bundlesRespondentAgreedDocWithNo",
                "MANDATORY",
                2,
                3,
                false,
                "Y",
                "bundlesRespondentAgreedDocWith=\"No\"",
                "No",
                null,
                null),
        field(
                ALL,
                PREPARE_DOCUMENTS,
                "bundlesRespondentSelectHearing",
                "MANDATORY",
                3,
                1,
                false,
                "Y",
                null,
                null,
                "About your hearing documents",
                null),
        field(
                ALL,
                PREPARE_DOCUMENTS,
                "bundlesRespondentWhoseDocuments",
                "MANDATORY",
                3,
                2,
                false,
                "Y",
                null,
                null,
                null,
                null),
        field(
                ALL,
                PREPARE_DOCUMENTS,
                "bundlesRespondentWhatDocuments",
                "MANDATORY",
                3,
                3,
                false,
                "Y",
                null,
                null,
                null,
                null),
        field(
                CFTLIB,
                PREPARE_DOCUMENTS,
                "bundlesRespondentUploadFileLabel",
                "READONLY",
                4,
                1,
                false,
                null,
                null,
                null,
                "Upload your file of documents",
                LOCALHOST + "/bundlesRespondent/midValidateUpload"),
        field(
                PROD,
                PREPARE_DOCUMENTS,
                "bundlesRespondentUploadFileLabel",
                "READONLY",
                4,
                1,
                false,
                null,
                null,
                null,
                "Upload your file of documents",
                PRODUCTION + "/bundlesRespondent/midValidateUpload"),
        field(
                ALL,
                PREPARE_DOCUMENTS,
                "bundlesRespondentUploadFile",
                "COMPLEX",
                4,
                2,
                false,
                "Y",
                null,
                null,
                null,
                null),
        field(
                CFTLIB,
                REMOVE_HEARING_BUNDLES,
                "removeBundleDropDownSelectedParty",
                "MANDATORY",
                1,
                1,
                false,
                "N",
                null,
                null,
                "Please specify the party whose hearing bundles are to be removed",
                LOCALHOST + "/bundlesRespondent/midPopulateRemoveHearingBundles"),
        field(
                PROD,
                REMOVE_HEARING_BUNDLES,
                "removeBundleDropDownSelectedParty",
                "MANDATORY",
                1,
                1,
                false,
                "N",
                null,
                null,
                "Please specify the party whose hearing bundles are to be removed",
                PRODUCTION + "/bundlesRespondent/midPopulateRemoveHearingBundles"),
        field(
                ALL,
                REMOVE_HEARING_BUNDLES,
                "removeHearingBundleSelect",
                "MANDATORY",
                2,
                1,
                false,
                "Y",
                null,
                null,
                "Remove Hearing Bundle",
                null),
        field(
                ALL,
                REMOVE_HEARING_BUNDLES,
                "hearingBundleRemoveReason",
                "MANDATORY",
                2,
                2,
                false,
                "Y",
                null,
                null,
                "Remove Hearing Bundle",
                null),
        field(
                ALL,
                UPLOAD_HEARING_DOCUMENTS,
                "uploadHearingDocumentsSelectPastOrFutureHearing",
                "MANDATORY",
                1,
                1,
                false,
                "Y",
                null,
                null,
                "Upload Hearing Documents",
                null),
        field(
                ALL,
                UPLOAD_HEARING_DOCUMENTS,
                "uploadHearingDocumentsSelectPastHearing",
                "MANDATORY",
                1,
                2,
                false,
                "Y",
                "uploadHearingDocumentsSelectPastOrFutureHearing=\"Past\"",
                null,
                null,
                null),
        field(
                ALL,
                UPLOAD_HEARING_DOCUMENTS,
                "uploadHearingDocumentsSelectFutureHearing",
                "MANDATORY",
                1,
                2,
                false,
                "Y",
                "uploadHearingDocumentsSelectPastOrFutureHearing=\"Future\"",
                null,
                null,
                null),
        field(
                ALL,
                UPLOAD_HEARING_DOCUMENTS,
                "uploadHearingDocumentType",
                "MANDATORY",
                1,
                3,
                false,
                "Y",
                null,
                null,
                null,
                null),
        field(
                ALL,
                UPLOAD_HEARING_DOCUMENTS,
                "uploadHearingDocumentsWhoseDocuments",
                "MANDATORY",
                1,
                4,
                false,
                "Y",
                null,
                null,
                null,
                null),
        field(
                ALL,
                UPLOAD_HEARING_DOCUMENTS,
                "uploadHearingDocumentsDateSubmitted",
                "MANDATORY",
                1,
                5,
                false,
                "Y",
                null,
                null,
                null,
                null),
        field(
                ALL,
                CREATE_DCF,
                "uploadOrRemoveDcf",
                "MANDATORY",
                1,
                1,
                true,
                "N",
                null,
                null,
                null,
                null),
        field(
                ALL,
                CREATE_DCF,
                "digitalCaseFile",
                "COMPLEX",
                1,
                2,
                true,
                "N",
                "uploadOrRemoveDcf=\"Upload\"",
                null,
                null,
                null)
    };

    static final ComplexFieldSpec[] COMPLEX_FIELDS = {
        element(
                ALL,
                "bundlesRespondentUploadFile",
                PREPARE_DOCUMENTS,
                "bundlesRespondentUploadFile",
                "uploadedDocument",
                1,
                "Document",
                "No"),
        element(
                PROD,
                "DigitalCaseFile",
                CREATE_DCF,
                "caseBundles",
                "documents",
                1,
                "Digital Case File documents",
                null),
        element(
                PROD,
                "DigitalCaseFile",
                CREATE_DCF,
                "caseBundles",
                "documents.name",
                2,
                "Document name",
                null),
        element(
                PROD,
                "DigitalCaseFile",
                CREATE_DCF,
                "caseBundles",
                "documents.sourceDocument",
                3,
                "Document",
                null),
        element(
                ALL,
                "CreateDCF",
                CREATE_DCF,
                "digitalCaseFile",
                "uploadedDocument",
                1,
                "Digital Case File",
                null)
    };

    static final StandaloneComplexSpec[] STANDALONE_COMPLEX_FIELDS = {
        new StandaloneComplexSpec(
                PROD, CREATE_DCF, CaseData::getCaseBundles, SingleComplexTypes.BundleType.class)
    };

    private SingleHearingDocumentsRows() {}

    private static GrantSpec[] eventGrants() {
        List<GrantSpec> grants = new ArrayList<>();

        for (SingleRole role : RESPONDENT_SOLICITORS) {
            grants.add(new GrantSpec(ENGLAND_WALES, PREPARE_DOCUMENTS, role, "CRU"));
            grants.add(new GrantSpec(SCOTLAND, PREPARE_DOCUMENTS, role, "CRUD"));
        }
        grants.add(
                new GrantSpec(
                        ALL, PREPARE_DOCUMENTS, SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        grants.add(new GrantSpec(ALL, PREPARE_DOCUMENTS, SingleRole.CLAIMANTSOLICITOR, "D"));
        grants.add(new GrantSpec(ALL, PREPARE_DOCUMENTS, SingleRole.ET_ACAS_API, "R"));

        grants.add(new GrantSpec(ALL, SUBMIT_CLAIMANT_BUNDLES, SingleRole.CREATOR, "CRUD"));
        grants.add(
                new GrantSpec(
                        SCOTLAND,
                        SUBMIT_CLAIMANT_BUNDLES,
                        SingleRole.CASEWORKER_EMPLOYMENT_API,
                        "CRUD"));

        grants.add(
                new GrantSpec(
                        ALL, REMOVE_HEARING_BUNDLES, SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        grants.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        REMOVE_HEARING_BUNDLES,
                        SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
                        "CRUD"));
        grants.add(
                new GrantSpec(
                        SCOTLAND,
                        REMOVE_HEARING_BUNDLES,
                        SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
                        "CRUD"));

        addRegionalTribunalGrants(grants, UPLOAD_HEARING_DOCUMENTS, "CRUD");
        addRegionalTribunalGrants(grants, CREATE_DCF, "CRU");
        addRegionalTribunalGrants(grants, ASYNC_STITCHING_COMPLETE, "CRU");
        return grants.toArray(GrantSpec[]::new);
    }

    private static void addRegionalTribunalGrants(
            List<GrantSpec> grants, String eventId, String regionalCrud) {
        grants.add(new GrantSpec(ALL, eventId, SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
        grants.add(new GrantSpec(ALL, eventId, SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE, "R"));
        grants.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        eventId,
                        SingleRole.CASEWORKER_EMPLOYMENT_ENGLANDWALES,
                        regionalCrud));
        grants.add(
                new GrantSpec(
                        ENGLAND_WALES,
                        eventId,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_ENGLANDWALES,
                        regionalCrud));
        grants.add(
                new GrantSpec(
                        SCOTLAND,
                        eventId,
                        SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND,
                        regionalCrud));
        grants.add(
                new GrantSpec(
                        SCOTLAND,
                        eventId,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
                        regionalCrud));
    }

    private static EventSpec prepareDocuments(int mask, String base, String condition) {
        return new EventSpec(
                mask,
                PREPARE_DOCUMENTS,
                "Upload documents for hearing",
                "Upload documents for a hearing",
                1,
                "*",
                "*",
                condition,
                "Y",
                "N",
                null,
                false,
                null,
                base + "/bundlesRespondent/aboutToStart",
                base + "/bundlesRespondent/aboutToSubmit",
                base + "/bundlesRespondent/submitted",
                null);
    }

    private static EventSpec submitClaimantBundles() {
        return new EventSpec(
                ALL,
                SUBMIT_CLAIMANT_BUNDLES,
                "Submit hearing docs",
                "Submit hearing docs",
                null,
                "*",
                "*",
                "caseType=\"dummy\"",
                "N",
                "N",
                null,
                false,
                null,
                null,
                null,
                null,
                null);
    }

    private static EventSpec removeHearingBundles(int mask, String base, String description) {
        return new EventSpec(
                mask,
                REMOVE_HEARING_BUNDLES,
                "Remove hearing documents",
                description,
                null,
                "*",
                "*",
                " ",
                "N",
                "N",
                null,
                false,
                null,
                null,
                base + "/bundlesRespondent/removeHearingBundle",
                null,
                null);
    }

    private static EventSpec uploadHearingDocuments(int mask, String base) {
        return new EventSpec(
                mask,
                UPLOAD_HEARING_DOCUMENTS,
                "Upload Hearing Documents",
                "Upload Hearing Documents",
                null,
                "Accepted",
                "*",
                null,
                "N",
                "N",
                null,
                false,
                null,
                base + "/uploadHearingDocuments/aboutToStart",
                base + "/uploadHearingDocuments/aboutToSubmit",
                null,
                null);
    }

    private static EventSpec createDcf(int mask, String base) {
        return new EventSpec(
                mask,
                CREATE_DCF,
                "Create, Upload or Remove DCF",
                "Create, Upload or Remove DCF",
                29,
                "*",
                "*",
                null,
                "N",
                "N",
                null,
                false,
                null,
                null,
                base + "/dcf/asyncAboutToSubmit",
                null,
                null);
    }

    private static EventSpec asyncStitchingComplete(int mask, String base) {
        return new EventSpec(
                mask,
                ASYNC_STITCHING_COMPLETE,
                "Stitching bundle complete",
                "Stitching bundle complete",
                9000,
                "*",
                "*",
                "caseType=\"dummy\"",
                "N",
                "N",
                null,
                false,
                null,
                null,
                base + "/dcf/asyncCompleteAboutToSubmit",
                null,
                null);
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
            String midEvent) {
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
                null);
    }

    private static ComplexFieldSpec element(
            int mask,
            String rowId,
            String eventId,
            String caseFieldId,
            String elementCode,
            int order,
            String label,
            String retainHidden) {
        return new ComplexFieldSpec(
                mask,
                rowId,
                eventId,
                caseFieldId,
                elementCode,
                "MANDATORY",
                order,
                label,
                null,
                null,
                retainHidden,
                null,
                null);
    }
}
