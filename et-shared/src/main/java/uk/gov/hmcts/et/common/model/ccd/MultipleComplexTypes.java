package uk.gov.hmcts.et.common.model.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

public final class MultipleComplexTypes {

    private MultipleComplexTypes() {}

    public static Class<?>[] all() {
        return new Class<?>[] {
            BundleType.class,
            BundleDocumentType.class,
            BundleFolderType.class,
            BundleSubfolderType.class,
            BundleSubfolder2Type.class,
            CaseNumberType.class,
            DocumentImageType.class,
            DocumentUploadType.class,
            DynamicListCollectionType.class,
            SubCaseLegalRepDetailsType.class,
            caseNoteType.class,
            multipleMigrationDataType.class,
            pseRespondCollectionType.class,
            pseStatusType.class,
            referralDetailsType.class,
            referralReplyType.class,
            respondNotificationTypeCollectionType.class,
            sendNotificationCollectionType.class,
            subMultipleTypeType.class,
            updateReferralDetailsType.class
        };
    }

    @ComplexType(name = "Bundle", generate = true)
    public static final class BundleType {
        @CCD(
                id = "id",
                label = "Bundle ID",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "title",
                label = "Name of bundle",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "description",
                label = "Description",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "eligibleForStitching",
                label = "Is this the bundle you want to stitch?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = MultipleDefinition.class)
        private Object field04;

        @CCD(
                id = "eligibleForCloning",
                label = "Is this the bundle you want to clone?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = MultipleDefinition.class)
        private Object field05;

        @CCD(
                id = "documents",
                label = "Bundle document",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleDocument",
                includeInProfiles = MultipleDefinition.class)
        private Object field06;

        @CCD(
                id = "folders",
                label = "Bundle folder",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleFolder",
                includeInProfiles = MultipleDefinition.class)
        private Object field07;

        @CCD(
                id = "stitchStatus",
                label = "Stitch Status",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field08;

        @CCD(
                id = "stitchedDocument",
                label = "Stitched Document",
                typeOverride = FieldType.Document,
                includeInProfiles = MultipleDefinition.class)
        private Object field09;

        @CCD(
                id = "hasCoversheets",
                label = "Should this bundle have coversheets separating each document?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = MultipleDefinition.class)
        private Object field10;

        @CCD(
                id = "hasTableOfContents",
                label = "Should this bundle have a title page with a table of contents?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = MultipleDefinition.class)
        private Object field11;

        @CCD(
                id = "hasFolderCoversheets",
                label = "Should this bundle’s folders have a coversheet?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = MultipleDefinition.class)
        private Object field12;

        @CCD(
                id = "stitchingFailureMessage",
                label = "Error from Stitching service",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field13;

        @CCD(
                id = "fileName",
                label = "Name of the PDF",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field14;

        @CCD(
                id = "fileNameIdentifier",
                label = "Unique filename identifier",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field15;

        @CCD(
                id = "paginationStyle",
                label = "Pagination Style",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "paginationStyle",
                includeInProfiles = MultipleDefinition.class)
        private Object field16;

        @CCD(
                id = "coverpageTemplate",
                label = "Cover page template",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field17;

        @CCD(
                id = "pageNumberFormat",
                label = "Page Number Format",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "pageNumberFormat",
                includeInProfiles = MultipleDefinition.class)
        private Object field18;

        @CCD(
                id = "enableEmailNotification",
                label = "Enable Email Notification?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = MultipleDefinition.class)
        private Object field19;

        @CCD(
                id = "documentImage",
                label = "Configure image for stitched PDF?",
                typeNameOverride = "DocumentImage",
                typeParameterOverride = "DocumentImage",
                includeInProfiles = MultipleDefinition.class)
        private Object field20;
    }

    @ComplexType(name = "BundleDocument", generate = true)
    public static final class BundleDocumentType {
        @CCD(
                id = "name",
                label = "Document Name",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "sortIndex",
                label = "Sort Index",
                typeNameOverride = "Number",
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "description",
                label = "Description",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "sourceDocument",
                label = "Source Document",
                typeOverride = FieldType.Document,
                includeInProfiles = MultipleDefinition.class)
        private Object field04;
    }

    @ComplexType(name = "BundleFolder", generate = true)
    public static final class BundleFolderType {
        @CCD(
                id = "name",
                label = "Folder Name",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "documents",
                label = "Folder Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleDocument",
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "folders",
                label = "Subfolders",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleSubfolder",
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "sortIndex",
                label = "Sort Index",
                typeNameOverride = "Number",
                includeInProfiles = MultipleDefinition.class)
        private Object field04;
    }

    @ComplexType(name = "BundleSubfolder", generate = true)
    public static final class BundleSubfolderType {
        @CCD(
                id = "name",
                label = "Subfolder Name",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "documents",
                label = "Folder Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleDocument",
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "folders",
                label = "Subfolders",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleSubfolder2",
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "sortIndex",
                label = "Sort Index",
                typeNameOverride = "Number",
                includeInProfiles = MultipleDefinition.class)
        private Object field04;
    }

    @ComplexType(name = "BundleSubfolder2", generate = true)
    public static final class BundleSubfolder2Type {
        @CCD(
                id = "name",
                label = "Subfolder Name",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "documents",
                label = "Folder Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleDocument",
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "sortIndex",
                label = "Sort Index",
                typeNameOverride = "Number",
                includeInProfiles = MultipleDefinition.class)
        private Object field03;
    }

    @ComplexType(name = "CaseNumber", generate = true)
    public static final class CaseNumberType {
        @CCD(
                id = "ethos_CaseReference ",
                label = "Case Number",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;
    }

    @ComplexType(name = "DocumentImage", generate = true)
    public static final class DocumentImageType {
        @CCD(
                id = "imageRendering",
                label = "How would you like to have the image rendered?",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "imageRendering",
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "imageRenderingLocation",
                label = "Where would you like to embed the image in the stitched document?",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "imageRenderingLocation",
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "docmosisAssetId",
                label = "The filename of the image you wish to embed",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "coordinateX",
                label = "X position of the image",
                typeNameOverride = "Number",
                includeInProfiles = MultipleDefinition.class)
        private Object field04;

        @CCD(
                id = "coordinateY",
                label = "Y position of the image",
                typeNameOverride = "Number",
                includeInProfiles = MultipleDefinition.class)
        private Object field05;
    }

    @ComplexType(name = "DocumentUpload", generate = true)
    public static final class DocumentUploadType {
        @CCD(
                id = "topLevelDocuments",
                label = "Document Category",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_DocumentCategories",
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "startingClaimDocuments",
                label = "Starting a Claim",
                showCondition = "topLevelDocuments=\"Starting a Claim\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_StartingAClaim",
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "responseClaimDocuments",
                label = "Response to a Claim",
                showCondition = "topLevelDocuments=\"Response to a Claim\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ResponseToAClaim",
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "initialConsiderationDocuments",
                label = "Initial Consideration",
                showCondition = "topLevelDocuments=\"Initial Consideration\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_InitialConsideration",
                includeInProfiles = MultipleDefinition.class)
        private Object field04;

        @CCD(
                id = "caseManagementDocuments",
                label = "Case Management",
                showCondition = "topLevelDocuments=\"Case Management\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_CaseManagement",
                includeInProfiles = MultipleDefinition.class)
        private Object field05;

        @CCD(
                id = "eccDocuments",
                label = "Employer Contract Claim",
                showCondition = "topLevelDocuments=\"Employer Contract Claim\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_EmployerContractClaim",
                includeInProfiles = MultipleDefinition.class)
        private Object field06;

        @CCD(
                id = "withdrawalSettledDocuments",
                label = "Withdrawal/Settled",
                showCondition = "topLevelDocuments=\"Withdrawal/Settled\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_WithdrawalSettled",
                includeInProfiles = MultipleDefinition.class)
        private Object field07;

        @CCD(
                id = "hearingsDocuments",
                label = "Hearings",
                showCondition = "topLevelDocuments=\"Hearings\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Hearings",
                includeInProfiles = MultipleDefinition.class)
        private Object field08;

        @CCD(
                id = "judgmentAndReasonsDocuments",
                label = "Judgment and Reasons",
                showCondition = "topLevelDocuments=\"Judgment and Reasons\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_JudgmentAndReasons",
                includeInProfiles = MultipleDefinition.class)
        private Object field09;

        @CCD(
                id = "reconsiderationDocuments",
                label = "Reconsideration",
                showCondition = "topLevelDocuments=\"Reconsideration\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Reconsideration",
                includeInProfiles = MultipleDefinition.class)
        private Object field10;

        @CCD(
                id = "miscDocuments",
                label = "Misc",
                showCondition = "topLevelDocuments=\"Misc\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Misc",
                includeInProfiles = MultipleDefinition.class)
        private Object field11;

        @CCD(
                id = "typeOfDocument",
                label = "Type of Document",
                showCondition = "topLevelDocuments=\"Legacy Document Names\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_DocumentType",
                includeInProfiles = EnglandWalesMultipleDefinition.class)
        @CCD(
                id = "typeOfDocument",
                label = "Type of Document",
                showCondition =
                        "topLevelDocuments=\"Legacy Document Names\" OR topLevelDocuments != \"*\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_DocumentType",
                includeInProfiles = ScotlandMultipleDefinition.class)
        private Object field12;

        @CCD(
                id = "uploadedDocument",
                label = "Document",
                typeOverride = FieldType.Document,
                includeInProfiles = MultipleDefinition.class)
        private Object field13;

        @CCD(
                id = "shortDescription",
                label = "Short Description",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field14;

        @CCD(
                id = "documentType",
                label = "Type of Document",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field15;

        @CCD(
                id = "dateOfCorrespondence",
                label = "Date of Correspondence",
                typeOverride = FieldType.Date,
                includeInProfiles = MultipleDefinition.class)
        private Object field16;

        @CCD(
                id = "docNumber",
                label = "Number",
                typeNameOverride = "Number",
                includeInProfiles = MultipleDefinition.class)
        private Object field17;

        @CCD(
                id = "documentIndex",
                label = "Document Number",
                typeNameOverride = "Number",
                includeInProfiles = MultipleDefinition.class)
        private Object field18;

        @CCD(
                id = "excludeFromDcf",
                label = "Exclude from Digital Case File",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_Yes",
                includeInProfiles = MultipleDefinition.class)
        private Object field19;
    }

    @ComplexType(name = "DynamicListCollection", generate = true)
    public static final class DynamicListCollectionType {
        @CCD(
                id = "dynamicList",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;
    }

    @ComplexType(name = "SubCaseLegalRepDetails", generate = true)
    public static final class SubCaseLegalRepDetailsType {
        @CCD(
                id = "caseReference",
                label = "caseReference",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleCftlibDefinition.class)
        private Object field01;

        @CCD(
                id = "legalRepIds",
                label = "legalRepIds",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "Text",
                includeInProfiles = MultipleCftlibDefinition.class)
        private Object field02;
    }

    @ComplexType(name = "caseNote", generate = true)
    public static final class caseNoteType {
        @CCD(
                id = "title",
                label = "Title",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "note",
                label = "Note",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "author",
                label = "Author",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "date",
                label = "Date",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field04;
    }

    @ComplexType(name = "multipleMigrationData", generate = true)
    public static final class multipleMigrationDataType {
        @CCD(
                id = "ethosCaseRef",
                label = "Ethos Case Reference",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "subMultiple",
                label = "Sub Multiple",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "flag1",
                label = "Flag 1",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "flag2",
                label = "Flag 2",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field04;

        @CCD(
                id = "flag3",
                label = "Flag 3",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field05;

        @CCD(
                id = "flag4",
                label = "Flag 4",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field06;
    }

    @ComplexType(name = "pseRespondCollection", generate = true)
    public static final class pseRespondCollectionType {
        @CCD(
                id = "date",
                label = "Response date",
                displayOrder = 2,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "from",
                label = "Response from",
                displayOrder = 1,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "response",
                label = "What's your response to the tribunal?",
                displayOrder = 3,
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "fromIdamId",
                label = "Response from Idam id",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field04;

        @CCD(
                id = "hasSupportingMaterial",
                label = "Has supporting material",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field05;

        @CCD(
                id = "supportingMaterial",
                label = "Supporting material",
                displayOrder = 4,
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = MultipleDefinition.class)
        private Object field06;

        @CCD(
                id = "copyToOtherParty",
                label =
                        "Do you want to copy this correspondence to the other party to satisfy the"
                                + " Rules of Procedure?",
                showCondition = " ",
                displayOrder = 5,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field07;

        @CCD(
                id = "copyNoGiveDetails",
                showCondition = "copyToOtherParty=\"dummy\"",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field08;

        @CCD(
                id = "responseState",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field09;

        @CCD(
                id = "author",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field10;

        @CCD(
                id = "dateTime",
                showCondition = "copyToOtherParty = \"dummy\"",
                retainHiddenValueValue = "Yes",
                typeOverride = FieldType.Text,
                includeInProfiles = EnglandWalesMultipleDefinition.class)
        @CCD(
                id = "dateTime",
                showCondition = "copyToOtherParty=\"dummy\"",
                retainHiddenValueValue = "Yes",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandMultipleDefinition.class)
        private Object field11;

        @CCD(
                id = "isECC",
                showCondition = "copyToOtherParty=\"dummy\"",
                retainHiddenValueValue = "Yes",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field12;
    }

    @ComplexType(name = "pseStatus", generate = true)
    public static final class pseStatusType {
        @CCD(
                id = "userIdamId",
                label = "User Idam Id",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "notificationState",
                label = "Notification state",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "dateTime",
                label = "Updated date time",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;
    }

    @ComplexType(name = "referralDetails", generate = true)
    public static final class referralDetailsType {
        @CCD(
                id = "referralNumber",
                label = "No",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "referralHearingDate",
                label = "Hearing date",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "referCaseTo",
                label = "Referred to",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "referentEmail",
                label = "Email address",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field04;

        @CCD(
                id = "isUrgent",
                label = "Urgent",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field05;

        @CCD(
                id = "referralSubject",
                label = "Subject",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field06;

        @CCD(
                id = "referralSubjectSpecify",
                label = "Referral subject",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field07;

        @CCD(
                id = "referralDetails",
                label = "Details of the referral",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field08;

        @CCD(
                id = "referralDocument",
                label = "Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = MultipleDefinition.class)
        private Object field09;

        @CCD(
                id = "referralInstruction",
                label = "Recommended instructions",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field10;

        @CCD(
                id = "referredBy",
                label = "Referred by",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field11;

        @CCD(
                id = "referralDate",
                label = "Referral date",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field12;

        @CCD(
                id = "referralStatus",
                label = "Status",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field13;

        @CCD(
                id = "closeReferralGeneralNotes",
                label = "General notes",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field14;

        @CCD(
                id = "updateReferralCollection",
                label = "Updates",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "updateReferralDetails",
                includeInProfiles = MultipleDefinition.class)
        private Object field15;

        @CCD(
                id = "referralReplyCollection",
                label = "Reply",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "referralReply",
                includeInProfiles = MultipleDefinition.class)
        private Object field16;

        @CCD(
                id = "referralSummaryPdf",
                label = "Referral Document",
                categoryID = "C4",
                typeOverride = FieldType.Document,
                includeInProfiles = MultipleDefinition.class)
        private Object field17;
    }

    @ComplexType(name = "referralReply", generate = true)
    public static final class referralReplyType {
        @CCD(
                id = "replyBy",
                label = "Reply by",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "directionTo",
                label = "Reply to",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "replyToEmailAddress",
                label = "Email address",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "replyDate",
                label = "Date",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field04;

        @CCD(
                id = "isUrgentReply",
                label = "Urgent",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field05;

        @CCD(
                id = "directionDetails",
                label = "Directions",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field06;

        @CCD(
                id = "replyDocument",
                label = "Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = MultipleDefinition.class)
        private Object field07;

        @CCD(
                id = "replyGeneralNotes",
                label = "General notes",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field08;

        @CCD(
                id = "replyDateTime",
                showCondition = "replyDateTime = \"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field09;

        @CCD(
                id = "referralSubject",
                showCondition = "replyDateTime = \"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field10;

        @CCD(
                id = "referralNumber",
                showCondition = "replyDateTime = \"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field11;
    }

    @ComplexType(name = "respondNotificationTypeCollection", generate = true)
    public static final class respondNotificationTypeCollectionType {
        @CCD(
                id = "respondNotificationCmoOrRequest",
                label = "Is this a case management order or request?",
                displayOrder = 5,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "respondNotificationDate",
                label = "Date",
                displayOrder = 2,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "respondNotificationRequestMadeBy",
                label = "Request made by",
                displayOrder = 7,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "state",
                showCondition = "respondNotificationDate=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field04;

        @CCD(
                id = "isClaimantResponseDue",
                showCondition = "state=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field05;

        @CCD(
                id = "dateTime",
                label = "Updated date time",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field06;

        @CCD(
                id = "respondNotificationTitle",
                label = "Response title",
                displayOrder = 1,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field07;

        @CCD(
                id = "respondNotificationAdditionalInfo",
                label = "Additional information",
                displayOrder = 4,
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field08;

        @CCD(
                id = "respondNotificationUploadDocument",
                label = "Documents",
                displayOrder = 3,
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = MultipleDefinition.class)
        private Object field09;

        @CCD(
                id = "respondNotificationResponseRequired",
                label = "Response due",
                displayOrder = 8,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field10;

        @CCD(
                id = "respondNotificationWhoRespond",
                label = "Party or parties to respoond",
                displayOrder = 9,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field11;

        @CCD(
                id = "respondNotificationCaseManagementMadeBy",
                label = "Case management order made by",
                displayOrder = 6,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field12;

        @CCD(
                id = "respondNotificationFullName",
                label = "Full name",
                displayOrder = 7,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field13;

        @CCD(
                id = "respondNotificationPartyToNotify",
                label = "Sent to",
                displayOrder = 10,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field14;
    }

    @ComplexType(name = "sendNotificationCollection", generate = true)
    public static final class sendNotificationCollectionType {
        @CCD(
                id = "sendNotificationTitle",
                label = "Notification",
                displayOrder = 1,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "sendNotificationLetter",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "sendNotificationUploadDocument",
                label = "Documents",
                displayOrder = 7,
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "sendNotificationSubject",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "fl_sendNotificationSubject",
                includeInProfiles = MultipleDefinition.class)
        private Object field04;

        @CCD(
                id = "sendNotificationAdditionalInfo",
                label = "Additional information",
                displayOrder = 6,
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field05;

        @CCD(
                id = "sendNotificationNotify",
                label = "To party",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field06;

        @CCD(
                id = "sendNotificationAnotherLetter",
                label = "sendNotificationAnotherLetter",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field07;

        @CCD(
                id = "sendNotificationSelectHearing",
                label = "Hearing",
                displayOrder = 2,
                typeOverride = FieldType.DynamicList,
                includeInProfiles = MultipleDefinition.class)
        private Object field08;

        @CCD(
                id = "sendNotificationCaseManagement",
                label = "Case management order or request",
                displayOrder = 4,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field09;

        @CCD(
                id = "sendNotificationResponseTribunal",
                label = "Response due",
                displayOrder = 4,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field10;

        @CCD(
                id = "sendNotificationWhoCaseOrder",
                label = "Case management order made by",
                displayOrder = 8,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field11;

        @CCD(
                id = "sendNotificationSelectParties",
                label = "Party or parties to respond",
                displayOrder = 5,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field12;

        @CCD(
                id = "sendNotificationFullName",
                label = "Full name",
                displayOrder = 9,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field13;

        @CCD(
                id = "sendNotificationFullName2",
                label = "Full name",
                displayOrder = 11,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field14;

        @CCD(
                id = "sendNotificationDecision",
                label = "Decision",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field15;

        @CCD(
                id = "sendNotificationDetails",
                label = "Details",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field16;

        @CCD(
                id = "number",
                label = "No",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field17;

        @CCD(
                id = "date",
                label = "Date sent",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field18;

        @CCD(
                id = "respondCollection",
                label = "Responses",
                displayOrder = 12,
                typeOverride = FieldType.Collection,
                typeParameterOverride = "pseRespondCollection",
                includeInProfiles = MultipleDefinition.class)
        private Object field19;

        @CCD(
                id = "notificationState",
                label = "Notification State",
                showCondition = "number=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field20;

        @CCD(
                id = "respondentState",
                label = "Respondent notification State",
                showCondition = "number=\"dummy\"",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "pseStatus",
                includeInProfiles = MultipleDefinition.class)
        private Object field21;

        @CCD(
                id = "sendNotificationResponsesCount",
                label = "Number of responses",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field22;

        @CCD(
                id = "sendNotificationSubjectString",
                label = "Subject",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field23;

        @CCD(
                id = "sendNotificationSentBy",
                label = "Sent by",
                displayOrder = 3,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field24;

        @CCD(
                id = "sendNotificationEccQuestion",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field25;

        @CCD(
                id = "sendNotificationWhoMadeJudgement",
                label = "Judgment made by",
                displayOrder = 10,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field26;

        @CCD(
                id = "sendNotificationResponseTribunalTable",
                label = "Response due",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field27;

        @CCD(
                id = "sendNotificationRequestMadeBy",
                label = "Request made by",
                displayOrder = 8,
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field28;

        @CCD(
                id = "respondNotificationTypeCollection",
                label = "Tribunal Responses",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "respondNotificationTypeCollection",
                includeInProfiles = MultipleDefinition.class)
        private Object field29;

        @CCD(
                id = "sendNotificationNotifyLeadCase",
                label = "To",
                showCondition = "sendNotificationNotify=\"Lead case\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleCftlibDefinition.class)
        private Object field30;

        @CCD(
                id = "sendNotificationNotifyAll",
                label = "To",
                showCondition = "sendNotificationNotify=\"Lead and sub cases\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleCftlibDefinition.class)
        private Object field31;

        @CCD(
                id = "sendNotificationNotifySelected",
                label = "To",
                showCondition = "sendNotificationNotify=\"Selected cases\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleCftlibDefinition.class)
        private Object field32;

        @CCD(
                id = "notificationSentFrom",
                label = "Multiple ref",
                showCondition = "sendNotificationNotify=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleCftlibDefinition.class)
        private Object field33;

        @CCD(
                id = "respondStoredCollection",
                label = "Responses",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "pseRespondCollection",
                includeInProfiles = MultipleDefinition.class)
        private Object field34;

        @CCD(
                id = "respondentRespondStoredCollection",
                label = "Responses",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "pseRespondCollection",
                includeInProfiles = MultipleDefinition.class)
        private Object field35;
    }

    @ComplexType(name = "subMultipleType", generate = true)
    public static final class subMultipleTypeType {
        @CCD(
                id = "subMultipleName",
                label = "Submultiple Name",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "subMultipleRef",
                label = "Submultiple Reference",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;
    }

    @ComplexType(name = "updateReferralDetails", generate = true)
    public static final class updateReferralDetailsType {
        @CCD(
                id = "updateReferralNumber",
                label = "No",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field01;

        @CCD(
                id = "updateReferralHearingDate",
                label = "Hearing date",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field02;

        @CCD(
                id = "updateReferCaseTo",
                label = "Referred to",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field03;

        @CCD(
                id = "updateReferentEmail",
                label = "Email address",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field04;

        @CCD(
                id = "updateReferralDetails",
                label = "Details of the referral",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field05;

        @CCD(
                id = "updateIsUrgent",
                label = "Urgent",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field06;

        @CCD(
                id = "updateReferralSubject",
                label = "Subject",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field07;

        @CCD(
                id = "updateReferralSubjectSpecify",
                label = "Referral subject",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field08;

        @CCD(
                id = "updateReferralDocument",
                label = "Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = MultipleDefinition.class)
        private Object field09;

        @CCD(
                id = "updateReferralInstruction",
                label = "Recommended instructions",
                typeOverride = FieldType.TextArea,
                includeInProfiles = MultipleDefinition.class)
        private Object field10;

        @CCD(
                id = "updateReferredBy",
                label = "Updated by",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field11;

        @CCD(
                id = "updateReferralDate",
                label = "Updated date",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field12;

        @CCD(
                id = "updateReferralDateTime",
                label = "Update Referral DateTime",
                typeOverride = FieldType.Text,
                includeInProfiles = MultipleDefinition.class)
        private Object field13;
    }
}
