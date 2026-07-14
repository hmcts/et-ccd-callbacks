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
final class SingleCorrespondenceDocumentsRows {
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

    private static final String UPLOAD_DOCUMENT = "uploadDocument";
    private static final String ADD_DOCUMENT = "addDocument";
    private static final String GENERATE_CORRESPONDENCE = "generateCorrespondence";
    private static final String UPLOAD_DOCUMENT_FOR_SERVING = "uploadDocumentForServing";

    static final EventSpec[] EVENTS = {
        uploadDocument(CFTLIB, LOCALHOST),
        uploadDocument(PROD, PRODUCTION),
        addDocument(CFTLIB, LOCALHOST),
        addDocument(PROD, PRODUCTION),
        generateCorrespondence(CFTLIB, LOCALHOST),
        generateCorrespondence(PROD, PRODUCTION),
        uploadDocumentForServing(CFTLIB, LOCALHOST),
        uploadDocumentForServing(PROD, PRODUCTION)
    };

    static final EventFieldSpec[] EVENT_FIELDS = eventFields();
    static final ComplexFieldSpec[] COMPLEX_FIELDS = complexFields();
    static final GrantSpec[] EVENT_GRANTS = eventGrants();

    private SingleCorrespondenceDocumentsRows() {}

    private static EventFieldSpec[] eventFields() {
        List<EventFieldSpec> rows = new ArrayList<>();
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT,
                        "documentCollection",
                        "COMPLEX",
                        1,
                        1,
                        "Y",
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        ADD_DOCUMENT,
                        "addDocumentCollection",
                        "COMPLEX",
                        1,
                        1,
                        "Y",
                        null,
                        null,
                        null,
                        null,
                        null));

        addCorrespondenceFields(rows, CFTLIB_ENGLAND_WALES, LOCALHOST, false);
        addCorrespondenceFields(rows, CFTLIB_SCOTLAND, LOCALHOST, true);
        addCorrespondenceFields(rows, PROD_ENGLAND_WALES, PRODUCTION, false);
        addCorrespondenceFields(rows, PROD_SCOTLAND, PRODUCTION, true);
        addServingFields(rows);
        return rows.toArray(EventFieldSpec[]::new);
    }

    private static void addCorrespondenceFields(
            List<EventFieldSpec> rows, int mask, String base, boolean scotland) {
        String correspondenceField = scotland ? "correspondenceScotType" : "correspondenceType";
        String topLevel =
                scotland
                        ? "correspondenceScotType.topLevel_Scot_Documents"
                        : "correspondenceType.topLevel_Documents";
        String part =
                scotland
                        ? "correspondenceScotType.part_0_Scot_Documents"
                        : "correspondenceType.part_0_Documents";
        rows.add(
                field(
                        mask,
                        GENERATE_CORRESPONDENCE,
                        correspondenceField,
                        "MANDATORY",
                        1,
                        1,
                        "Y",
                        null,
                        null,
                        null,
                        base + "/midAddressLabels",
                        null));
        rows.add(
                field(
                        mask,
                        GENERATE_CORRESPONDENCE,
                        "addressLabelsSelectionType",
                        "MANDATORY",
                        2,
                        1,
                        "Y",
                        null,
                        topLevel + "=\"EM-TRB-LET-ENG-00544\" AND " + part + "=\"0.1\"",
                        null,
                        base + "/midAddressLabels",
                        null));
        rows.add(
                field(
                        mask,
                        GENERATE_CORRESPONDENCE,
                        "addressLabelCollection",
                        "COMPLEX",
                        3,
                        1,
                        "Y",
                        null,
                        topLevel + "=\"EM-TRB-LET-ENG-00544\"",
                        null,
                        base + "/midSelectedAddressLabels",
                        null));
        rows.add(
                field(
                        mask,
                        GENERATE_CORRESPONDENCE,
                        "addressLabelsAttributesType",
                        "COMPLEX",
                        4,
                        1,
                        "Y",
                        null,
                        topLevel + "=\"EM-TRB-LET-ENG-00544\"",
                        null,
                        base + "/midValidateAddressLabels",
                        null));
    }

    private static void addServingFields(List<EventFieldSpec> rows) {
        rows.add(
                field(
                        CFTLIB,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "servingDocumentCollection",
                        "COMPLEX",
                        1,
                        1,
                        "Y",
                        null,
                        null,
                        "Upload documents",
                        LOCALHOST + "/midServingDocumentOtherTypeNames",
                        null));
        rows.add(
                field(
                        PROD,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "servingDocumentCollection",
                        "COMPLEX",
                        1,
                        1,
                        "Y",
                        null,
                        null,
                        "Upload documents",
                        PRODUCTION + "/midServingDocumentOtherTypeNames",
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "horizontalLine",
                        "READONLY",
                        2,
                        1,
                        "Y",
                        null,
                        "otherTypeDocumentName != \"\"",
                        "Who are you sending this document to?",
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "otherTypeDocumentName",
                        "MANDATORY",
                        2,
                        2,
                        null,
                        "horizontalLine=\"dummy\"",
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "otherTypeDocumentNameLabel",
                        "READONLY",
                        2,
                        3,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "selectAllThatApply",
                        "READONLY",
                        2,
                        4,
                        "Y",
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "servingDocumentRecipient",
                        "MANDATORY",
                        2,
                        5,
                        "Y",
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "printAndSendPaperDocuments",
                        "READONLY",
                        3,
                        1,
                        null,
                        null,
                        null,
                        "Send documents",
                        null,
                        null));
        rows.add(
                field(
                        ENGLAND_WALES,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "sendDocByFirstClass",
                        "READONLY",
                        3,
                        2,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        SCOTLAND,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "sendDocByFirstClass",
                        "READONLY",
                        3,
                        2,
                        null,
                        null,
                        null,
                        "Send documents",
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "claimantAndRespondentAddresses",
                        "READONLY",
                        3,
                        3,
                        null,
                        "printAndSendPaperDocuments=\"dummy\"",
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "claimantAndRespondentAddressesLabel",
                        "READONLY",
                        3,
                        4,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "emailDocsToAcasLine",
                        "READONLY",
                        4,
                        1,
                        null,
                        null,
                        null,
                        "Email Acas",
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "emailDocsToAcasTitle",
                        "READONLY",
                        4,
                        2,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "emailLinkToAcas",
                        "MANDATORY",
                        4,
                        3,
                        null,
                        "emailDocsToAcasTitle=\"dummy\"",
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "emailDocsToAcasLink",
                        "READONLY",
                        4,
                        4,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "emailDocsToAcasInstructions",
                        "READONLY",
                        4,
                        5,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "bfActions",
                        "COMPLEX",
                        1,
                        2,
                        "N",
                        "otherTypeDocumentName=\"dummy\"",
                        null,
                        null,
                        null,
                        null));
        rows.add(
                field(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "nextListedDate",
                        "OPTIONAL",
                        1,
                        2,
                        "N",
                        "servingDocumentCollection=\"dummy\"",
                        null,
                        null,
                        null,
                        "Y"));
    }

    private static ComplexFieldSpec[] complexFields() {
        List<ComplexFieldSpec> rows = new ArrayList<>();
        rows.add(
                element(
                        ALL,
                        "AddressLabels",
                        GENERATE_CORRESPONDENCE,
                        "addressLabelCollection",
                        "printLabel",
                        "MANDATORY",
                        1,
                        "Print label?",
                        null));
        rows.add(
                element(
                        ALL,
                        "AddressLabels",
                        GENERATE_CORRESPONDENCE,
                        "addressLabelCollection",
                        "fullName",
                        "READONLY",
                        2,
                        "Full name",
                        null));
        rows.add(
                element(
                        ALL,
                        "AddressLabels",
                        GENERATE_CORRESPONDENCE,
                        "addressLabelCollection",
                        "fullAddress",
                        "READONLY",
                        3,
                        "Full address",
                        null));

        rows.add(
                element(
                        ALL,
                        "ServingDocumentUpload",
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "servingDocumentCollection",
                        "typeOfDocument",
                        "MANDATORY",
                        1,
                        null,
                        null));
        rows.add(
                element(
                        ALL,
                        "ServingDocumentUpload",
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "servingDocumentCollection",
                        "uploadedDocument",
                        "MANDATORY",
                        2,
                        null,
                        null));
        rows.add(
                element(
                        ALL,
                        "ServingDocumentUpload",
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "servingDocumentCollection",
                        "shortDescription",
                        "OPTIONAL",
                        3,
                        null,
                        null));
        rows.add(
                element(
                        ALL,
                        "BFActions",
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "bfActions",
                        "bfDate",
                        "OPTIONAL",
                        1,
                        " ",
                        "Y"));
        rows.add(
                element(
                        ALL,
                        "BFActions",
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        "bfActions",
                        "allActions",
                        "OPTIONAL",
                        2,
                        " ",
                        "Y"));

        addDocumentElements(
                rows, ENGLAND_WALES, ADD_DOCUMENT, "addDocumentCollection", true, true, 16);
        addDocumentElements(rows, SCOTLAND, ADD_DOCUMENT, "addDocumentCollection", false, true, 16);
        addDocumentElements(
                rows, PROD_ENGLAND_WALES, UPLOAD_DOCUMENT, "documentCollection", true, false, 15);
        addDocumentElements(
                rows, PROD_SCOTLAND, UPLOAD_DOCUMENT, "documentCollection", false, false, 15);
        return rows.toArray(ComplexFieldSpec[]::new);
    }

    private static void addDocumentElements(
            List<ComplexFieldSpec> rows,
            int mask,
            String eventId,
            String caseFieldId,
            boolean employerContractClaim,
            boolean documentIndex,
            int excludeOrder) {
        int order = 1;
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "topLevelDocuments",
                        order++,
                        "Document Category"));
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "startingClaimDocuments",
                        order++,
                        "Starting a Claim"));
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "responseClaimDocuments",
                        order++,
                        "Response to a Claim"));
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "initialConsiderationDocuments",
                        order++,
                        "Initial Consideration"));
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "caseManagementDocuments",
                        order++,
                        "Case Management"));
        if (employerContractClaim) {
            rows.add(
                    documentElement(
                            mask,
                            eventId,
                            caseFieldId,
                            "eccDocuments",
                            order++,
                            "Employer Contract Claim"));
        }
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "withdrawalSettledDocuments",
                        order++,
                        "Withdrawal/Settled"));
        rows.add(
                documentElement(
                        mask, eventId, caseFieldId, "hearingsDocuments", order++, "Hearings"));
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "judgmentAndReasonsDocuments",
                        order++,
                        "Judgment and Reasons"));
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "reconsiderationDocuments",
                        order++,
                        "Reconsideration"));
        rows.add(documentElement(mask, eventId, caseFieldId, "miscDocuments", order++, "Misc"));
        rows.add(
                documentElement(
                        mask, eventId, caseFieldId, "typeOfDocument", order++, "Type of Document"));
        rows.add(
                documentElement(
                        mask, eventId, caseFieldId, "uploadedDocument", order++, "Document"));
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "shortDescription",
                        "OPTIONAL",
                        order++,
                        "Short Description"));
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "dateOfCorrespondence",
                        "OPTIONAL",
                        order++,
                        "Date of Correspondence"));
        if (documentIndex) {
            rows.add(
                    documentElement(
                            mask,
                            eventId,
                            caseFieldId,
                            "documentIndex",
                            "OPTIONAL",
                            order,
                            "Document Number"));
        }
        rows.add(
                documentElement(
                        mask,
                        eventId,
                        caseFieldId,
                        "excludeFromDcf",
                        "OPTIONAL",
                        excludeOrder,
                        "Do you want to exclude this document from the DCF?"));
    }

    private static GrantSpec[] eventGrants() {
        List<GrantSpec> rows = new ArrayList<>();
        addTribunalGrants(rows, UPLOAD_DOCUMENT);
        addTribunalGrants(rows, ADD_DOCUMENT);
        addTribunalGrants(rows, GENERATE_CORRESPONDENCE);
        addTribunalGrants(rows, UPLOAD_DOCUMENT_FOR_SERVING);
        rows.add(
                new GrantSpec(
                        ALL,
                        UPLOAD_DOCUMENT_FOR_SERVING,
                        SingleRole.CASEWORKER_WA_TASK_CONFIGURATION,
                        "CRU"));
        return rows.toArray(GrantSpec[]::new);
    }

    private static void addTribunalGrants(List<GrantSpec> rows, String eventId) {
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
        rows.add(
                new GrantSpec(SCOTLAND, eventId, SingleRole.CASEWORKER_EMPLOYMENT_SCOTLAND, "CRU"));
        rows.add(
                new GrantSpec(
                        SCOTLAND,
                        eventId,
                        SingleRole.CASEWORKER_EMPLOYMENT_ETJUDGE_SCOTLAND,
                        "CRU"));
        rows.add(new GrantSpec(ALL, eventId, SingleRole.CASEWORKER_EMPLOYMENT_API, "CRUD"));
    }

    private static EventSpec uploadDocument(int mask, String base) {
        return event(
                mask,
                UPLOAD_DOCUMENT,
                "Upload Document",
                "Upload a Document",
                29,
                "Submitted;Vetted;Accepted;Rejected;Closed",
                "managingOffice !=\"Unassigned\"",
                null,
                base + "/uploadDocument/aboutToStart",
                base + "/uploadDocument/aboutToSubmit",
                null);
    }

    private static EventSpec addDocument(int mask, String base) {
        return event(
                mask,
                ADD_DOCUMENT,
                "Add Document",
                "Add New Documents",
                65,
                "Submitted;Vetted;Accepted;Rejected;Closed",
                "managingOffice !=\"Unassigned\"",
                null,
                null,
                base + "/addDocument/aboutToSubmit",
                null);
    }

    private static EventSpec generateCorrespondence(int mask, String base) {
        return event(
                mask,
                GENERATE_CORRESPONDENCE,
                "Letters",
                "Generate Letters",
                30,
                "Accepted;Rejected;Submitted;Vetted;Closed",
                null,
                "Y",
                base + "/dynamicLetters",
                base + "/generateDocument",
                base + "/generateDocumentConfirmation");
    }

    private static EventSpec uploadDocumentForServing(int mask, String base) {
        return event(
                mask,
                UPLOAD_DOCUMENT_FOR_SERVING,
                "ET1 serving",
                "Upload a Document",
                15,
                "Accepted",
                "managingOffice !=\"Unassigned\"",
                "Y",
                null,
                null,
                base + "/et1Serving/submitted");
    }

    private static EventSpec event(
            int mask,
            String id,
            String name,
            String description,
            int displayOrder,
            String preState,
            String condition,
            String publish,
            String aboutToStart,
            String aboutToSubmit,
            String submitted) {
        return new EventSpec(
                mask,
                id,
                name,
                description,
                displayOrder,
                preState,
                "*",
                condition,
                "N",
                "N",
                publish,
                false,
                null,
                aboutToStart,
                aboutToSubmit,
                submitted,
                null);
    }

    private static EventFieldSpec field(
            int mask,
            String eventId,
            String fieldId,
            String context,
            int page,
            int order,
            String showSummary,
            String condition,
            String pageCondition,
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
                true,
                showSummary,
                condition,
                null,
                pageCondition,
                pageLabel,
                midEvent,
                publish);
    }

    private static ComplexFieldSpec documentElement(
            int mask,
            String eventId,
            String caseFieldId,
            String elementCode,
            int order,
            String label) {
        return documentElement(mask, eventId, caseFieldId, elementCode, "MANDATORY", order, label);
    }

    private static ComplexFieldSpec documentElement(
            int mask,
            String eventId,
            String caseFieldId,
            String elementCode,
            String context,
            int order,
            String label) {
        return element(
                mask, "Documents", eventId, caseFieldId, elementCode, context, order, label, null);
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
