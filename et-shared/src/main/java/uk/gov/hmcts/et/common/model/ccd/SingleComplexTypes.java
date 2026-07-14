package uk.gov.hmcts.et.common.model.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

public final class SingleComplexTypes {
    private SingleComplexTypes() {}

    public static Class<?>[] all() {
        return new Class<?>[] {
            AdditionalCaseDetailsType.class,
            AdrDocumentUploadDetailsType.class,
            AppealDocumentUploadDetailsType.class,
            BFActionsType.class,
            BundleType.class,
            BundleDocumentType.class,
            BundleFolderType.class,
            BundleSubfolderType.class,
            BundleSubfolder2Type.class,
            ClaimantCorrespondenceType.class,
            ClaimantIndividualType.class,
            ClaimantRepresentativeType.class,
            ClaimantRequestType.class,
            CompanyPremisesType.class,
            CounterClaimType.class,
            CreateRespondentType.class,
            DateListedType.class,
            DepositType.class,
            DigitalCaseFileType.class,
            DocumentImageType.class,
            DocumentUploadType.class,
            DraftAndSignJudgementType.class,
            DynamicListCollectionType.class,
            ET3VettingType.class,
            EmploymentDetailsType.class,
            Et3NotificationDocUploadType.class,
            HearingType.class,
            HearingBundleType.class,
            HearingDetailsType.class,
            HearingDocumentUploadType.class,
            HearingPreferenceType.class,
            JudgmentType.class,
            JudgmentCostsType.class,
            JudgmentDetailsType.class,
            JurisdictionType.class,
            JurisdictionCodeType.class,
            LettersType.class,
            ListingItemTypeType.class,
            ListingTypeType.class,
            NewEmploymentDetailsType.class,
            NoticeOfChangeAnswersType.class,
            OrganisationUsersIdamUserType.class,
            PiiDocumentUploadDetailsType.class,
            RemovedHearingBundleType.class,
            RespondentType.class,
            RespondentRepresentativeType.class,
            RestrictedCaseType.class,
            ServingDocumentUploadType.class,
            TaskListCheckType.class,
            TriageQuestionsType.class,
            UnavailabilityDateRangeType.class,
            WorkAddressDetailsType.class,
            AcceptOrRejectCaseType.class,
            AddressLabelType.class,
            AddressLabelsAttributesType.class,
            AddressLabelsSelectionType.class,
            CaseNoteType.class,
            ClaimantTseType.class,
            Et3CaseDetailsLinksStatusesType.class,
            Et3HubLinksStatusesType.class,
            EtICFurtherInfoAnswersType.class,
            EtICHearingListedAnswersType.class,
            EtICHearingNotListedListForFinalHearingType.class,
            EtICHearingNotListedListForFinalHearingUpdatedType.class,
            EtICHearingNotListedListForPrelimHearingType.class,
            EtICHearingNotListedListForPrelimHearingUpdatedType.class,
            EtICHearingNotListedSeekCommentsType.class,
            EtICHearingNotListedUDLHearingType.class,
            EtInitialConsiderationRule27Type.class,
            EtInitialConsiderationRule28Type.class,
            GenericTseDetailsType.class,
            HubLinksStatusesType.class,
            IcDocumentUploadType.class,
            JudgmentReconsiderationType.class,
            NextHearingDetailsType.class,
            PseRespondCollectionType.class,
            PseStatusType.class,
            ReferralDetailsType.class,
            ReferralReplyType.class,
            RespondNotificationTypeCollectionType.class,
            RespondentTseType.class,
            SendNotificationCollectionType.class,
            TseAdminDecisionType.class,
            TseReplyType.class,
            TseRespondentResponseType.class,
            TseStatusType.class,
            UpdateReferralDetailsType.class
        };
    }

    @ComplexType(name = "AdditionalCaseDetails", generate = true)
    public static final class AdditionalCaseDetailsType {
        @CCD(
                id = "additional_live_appeal",
                label = "Live appeal?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "additional_sensitive",
                label = "Sensitive case?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "doNotPostpone",
                label = "Do not postpone",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "digitalFile",
                label = "Digital File?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "reasonableAdjustment",
                label = "Reasonable Adjustment",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "interventionRequired",
                label = "Speak to REJ",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "interventionRequired",
                label = "Speak to VP",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field006;

        @CCD(
                id = "reservedToJudge",
                label = "Reserved to Judge",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field007;
    }

    @ComplexType(name = "AdrDocumentUploadDetails", generate = true)
    public static final class AdrDocumentUploadDetailsType {
        @CCD(
                id = "uploadedDocument",
                label = "Document Link",
                typeOverride = FieldType.Document,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "uploadedDocument",
                label = "Document Link",
                typeOverride = FieldType.Document,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "shortDescription",
                label = "Short Description",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "creationDate",
                label = "Date Uploaded",
                typeOverride = FieldType.Date,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "AppealDocumentUploadDetails", generate = true)
    public static final class AppealDocumentUploadDetailsType {
        @CCD(
                id = "uploadedDocument",
                label = "Document Link",
                typeOverride = FieldType.Document,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "shortDescription",
                label = "Short Description",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "creationDate",
                label = "Date Uploaded",
                typeOverride = FieldType.Date,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "BFActions", generate = true)
    public static final class BFActionsType {
        @CCD(
                id = "imported",
                label = "Migrated from Ethos",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "letters",
                label = "From Letter generation",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "dateEntered",
                label = "Date/Time",
                displayOrder = 1,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "cwActions",
                label = "Description",
                showCondition = "imported != \"Yes\" OR letters != \"Yes\"",
                displayOrder = 2,
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_BFActionsCW",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "allActions",
                label = "Action",
                showCondition =
                        "(imported = \"Yes\" OR letters = \"Yes\") OR allActions = \"ECC served\"",
                displayOrder = 2,
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "bfDate",
                label = "B/F Date",
                displayOrder = 3,
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "cleared",
                label = "Date Cleared",
                displayOrder = 4,
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "notes",
                label = "Comments",
                displayOrder = 5,
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "isWaTaskCreated",
                label = "Is wa task created?",
                displayOrder = 6,
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field009;
    }

    @ComplexType(name = "Bundle", generate = true)
    public static final class BundleType {
        @CCD(
                id = "id",
                label = "Bundle ID",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "title",
                label = "Name of bundle",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "description",
                label = "Description",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "eligibleForStitching",
                label = "Is this the bundle you want to stitch?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "eligibleForCloning",
                label = "Is this the bundle you want to clone?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "documents",
                label = "Bundle document",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleDocument",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "folders",
                label = "Bundle folder",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleFolder",
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "stitchStatus",
                label = "Stitch Status",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "stitchedDocument",
                label = "Stitched Document",
                typeOverride = FieldType.Document,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "hasCoversheets",
                label = "Should this bundle have coversheets separating each document?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "hasTableOfContents",
                label = "Should this bundle have a title page with a table of contents?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "hasFolderCoversheets",
                label = "Should this bundle’s folders have a coversheet?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "stitchingFailureMessage",
                label = "Error from Stitching service",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "fileName",
                label = "Name of the PDF",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "fileNameIdentifier",
                label = "Unique filename identifier",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "paginationStyle",
                label = "Pagination Style",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "paginationStyle",
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "coverpageTemplate",
                label = "Cover page template",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "pageNumberFormat",
                label = "Page Number Format",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "pageNumberFormat",
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "enableEmailNotification",
                label = "Enable Email Notification?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "documentImage",
                label = "Configure image for stitched PDF?",
                typeNameOverride = "DocumentImage",
                typeParameterOverride = "DocumentImage",
                includeInProfiles = SingleDefinition.class)
        private Object field020;
    }

    @ComplexType(name = "BundleDocument", generate = true)
    public static final class BundleDocumentType {
        @CCD(
                id = "name",
                label = "Document Name",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "sortIndex",
                label = "Sort Index",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "description",
                label = "Description",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "sourceDocument",
                label = "Source Document",
                typeOverride = FieldType.Document,
                includeInProfiles = SingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "BundleFolder", generate = true)
    public static final class BundleFolderType {
        @CCD(
                id = "name",
                label = "Folder Name",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "documents",
                label = "Folder Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleDocument",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "folders",
                label = "Subfolders",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleSubfolder",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "sortIndex",
                label = "Sort Index",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "BundleSubfolder", generate = true)
    public static final class BundleSubfolderType {
        @CCD(
                id = "name",
                label = "Subfolder Name",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "documents",
                label = "Folder Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleDocument",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "folders",
                label = "Subfolders",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleSubfolder2",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "sortIndex",
                label = "Sort Index",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "BundleSubfolder2", generate = true)
    public static final class BundleSubfolder2Type {
        @CCD(
                id = "name",
                label = "Subfolder Name",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "documents",
                label = "Folder Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "BundleDocument",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "sortIndex",
                label = "Sort Index",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "ClaimantCorrespondence", generate = true)
    public static final class ClaimantCorrespondenceType {
        @CCD(
                id = "claimant_addressUK",
                label = "Address",
                typeNameOverride = "AddressUK",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "claimant_phone_number",
                label = "Phone number",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "claimant_mobile_number",
                label = "Alternative number",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "claimant_email_address",
                label = "Email address",
                typeOverride = FieldType.Email,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "claimant_contact_preference",
                label = "Contact preference",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ContactPreference",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "claimant_contact_language",
                label = "Contact language",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_languages",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "claimant_contact_language",
                label = "Deprecated contact language",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_languages",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field006;

        @CCD(
                id = "claimant_hearing_language",
                label = "Hearing language",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_languages",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field007;
    }

    @ComplexType(name = "ClaimantIndividual", generate = true)
    public static final class ClaimantIndividualType {
        @CCD(
                id = "claimant_preferred_title",
                label = "Title",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_PreferredTitle",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "claimant_title1",
                label = "Deprecated",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Title",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "claimant_title_other",
                label = "Other title",
                showCondition = "claimant_preferred_title=\"Other\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "claimant_first_names",
                label = "First Name",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "claimant_last_name",
                label = "Last Name",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "claimant_date_of_birth",
                label = "Date of birth",
                typeOverride = FieldType.Date,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "claimant_gender",
                label = "Gender",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Gender",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "claimant_sex",
                label = "Sex",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Sex",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "claimant_gender_identity_same",
                label = "Is the claimant's identity and sex registered at birth the same?",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_YesOrNoOrPreferNot",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "claimant_gender_identity",
                label = "Gender Identity description",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field010;
    }

    @ComplexType(name = "ClaimantRepresentative", generate = true)
    public static final class ClaimantRepresentativeType {
        @CCD(
                id = "name_of_representative",
                label = "Name of Representative",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "name_of_organisation",
                label = "Name of Organisation",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "representative_reference",
                label = "Reference",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "representative_occupation",
                label = "Occupation",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_RepresentativeOccupation",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "representative_occupation_other",
                label = "What is the Representative's occupation?",
                showCondition = "representative_occupation=\"Other\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "representative_address",
                label = "Address",
                typeNameOverride = "AddressUK",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "representative_phone_number",
                label = "Phone number",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "representative_mobile_number",
                label = "Alternative number",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "representative_email_address",
                label = "Email address",
                typeOverride = FieldType.Email,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "representative_preference",
                label = "Contact preference",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_RepresentativeContact",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "organisationUsers",
                label = "Organisation users",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "OrganisationUsersIdamUser",
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "myHmctsOrganisation",
                label = "MyHMCTS Organisation",
                typeOverride = FieldType.Organisation,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "hearingContactLanguage",
                label = "Hearing Language",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "fl_languages",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "contactLanguageQuestion",
                label = "Contact Language",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "fl_languages",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "representativeAttendHearing",
                label = "Hearing Attendence",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_HearingAttendence",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "representative_id",
                label = " ",
                showCondition = "representative_occupation=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "organisationId",
                label = " ",
                showCondition = "representative_id=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field017;
    }

    @ComplexType(name = "ClaimantRequest", generate = true)
    public static final class ClaimantRequestType {
        @CCD(
                id = "claim_outcome",
                label = "What do you want if your claim is successful?",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_claimOutcomes",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "claim_outcome",
                label = "What do you want if your claim is successful",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_claimOutcomes",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "claimant_compensation_text",
                label = "What compensation are you seeking?",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "claimant_compensation_amount",
                label = "Compensation amount requested",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "claimant_tribunal_recommendation",
                label = "Tribunal recommendation request",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "whistleblowing",
                label = "Whistleblowing claim",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "whistleblowing_authority",
                label = "Whistleblowing authority",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "claim_description",
                label = "Describe what happened to you",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "claim_description_document",
                label = "Describe what happened to you",
                typeOverride = FieldType.Document,
                categoryID = "C12",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "claim_description_document",
                label = "Upload your summary as a separate document",
                typeOverride = FieldType.Document,
                categoryID = "C12",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field008;

        @CCD(
                id = "discrimination_claims",
                label = "What type of discrimination are you claiming?",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_discriminationClaims",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "pay_claims",
                label = "What type of pay claim are you making?",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_payClaims",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "linked_cases",
                label = " ",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "linked_cases_detail",
                label = " ",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "other_claim",
                label = "Please describe what type of claim you want to make",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field013;
    }

    @ComplexType(name = "CompanyPremises", generate = true)
    public static final class CompanyPremisesType {
        @CCD(
                id = "premises",
                label = "Premises",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "address",
                label = "Address",
                typeNameOverride = "AddressUK",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;
    }

    @ComplexType(name = "CounterClaim", generate = true)
    public static final class CounterClaimType {
        @CCD(
                id = "counterClaim",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;
    }

    @ComplexType(name = "CreateRespondent", generate = true)
    public static final class CreateRespondentType {
        @CCD(
                id = "respondentType",
                label = "Respondent name",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_respondentType",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "respondentFirstName",
                label = "Enter the first name of the individual",
                showCondition = "respondentType=\"Individual\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "respondentLastName",
                label = "Enter the last name of the individual",
                showCondition = "respondentType=\"Individual\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "respondentOrganisation",
                label = "Enter the name of the organisation",
                showCondition = "respondentType=\"Organisation\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "respondent_address",
                label = "Respondent address",
                typeNameOverride = "AddressUK",
                typeParameterOverride = "Please enter address details",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "respondent_ACAS_question",
                label = "Do you have an Acas certificate number for this respondent",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "msl_YesNo",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "respondent_ACAS",
                label = "Enter the Acas certificate number",
                showCondition = "respondent_ACAS_question=\"Yes\"",
                regex = "[a-zA-Z]{1,2}\\d{6}\\/\\d{2}\\/\\d{2}",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "respondent_ACAS_no",
                label = "Why is there no Acas certificate number?",
                hint =
                        "Incorrectly claiming an exemption may lead to the claim being rejected. If"
                            + " in doubt, please contact Acas",
                showCondition = "respondent_ACAS_question=\"No\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_noAcasReason",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field008;
    }

    @ComplexType(name = "DateListed", generate = true)
    public static final class DateListedType {
        @CCD(
                id = "listedDate",
                label = "Hearing Date",
                typeOverride = FieldType.DateTime,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "Hearing_status",
                label = "Hearing Status",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingStatus",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "Postponed_by",
                label = "Postponed by",
                showCondition = "Hearing_status=\"Postponed\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_PostponedBy",
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "postponedDate",
                label = "Postponed Date",
                showCondition = "Hearing_status=\"Postponed\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "hearingVenueDay",
                label = "Hearing Venue",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "hearingRoom",
                label = "Room",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "hearingRoom",
                label = "Room",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "Yes",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field006;

        @CCD(
                id = "hearingClerk",
                label = "Clerk",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "hearingClerk",
                label = "Clerk",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field007;

        @CCD(
                id = "hearingCaseDisposed",
                label = "Has the case or part of the case been disposed?",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.YesOrNo,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "Hearing_part_heard",
                label = "Has the hearing been part heard?",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.YesOrNo,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "Hearing_reserved_judgement",
                label = "Is there a reserved Judgment?",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.YesOrNo,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "attendee_claimant",
                label = "Attendees (Claimant)",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Attendee",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "attendee_non_attendees",
                label = "Number of Non Attendees (Respondent) ",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.Number,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "attendee_resp_no_rep",
                label = "Respondent Attended - No Representative",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.Number,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "attendee_resp_&_rep",
                label = "Respondent and Representative Attended",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.Number,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "attendee_rep_only",
                label = "Respondent representative only attended",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.Number,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "hearingTimingStart",
                label = "Start Time",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.DateTime,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "hearingTimingBreak",
                label = "Break",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.DateTime,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "hearingTimingResume",
                label = "Resume",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.DateTime,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "hearingTimingFinish",
                label = "Finish",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.DateTime,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "hearingTimingDuration",
                label = "Duration",
                showCondition = "Hearing_status=\"Heard\"",
                typeOverride = FieldType.Number,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field020;

        @CCD(
                id = "HearingNotes2",
                label = "Hearing Notes",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field021;

        @CCD(
                id = "showListingDetails",
                label = "Show Listing Details",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field022;

        @CCD(
                id = "Hearing_typeReadingDeliberation",
                label = "Reading Day, Deliberation Day, Members Meeting or In Chambers?",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingReadingDelib",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field023;

        @CCD(
                id = "hearingVenueDayScotland",
                label = "Managing Office",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "VenueScotland",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field024;

        @CCD(
                id = "Hearing_Glasgow",
                label = "Venue",
                showCondition = "hearingVenueDayScotland=\"Glasgow\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field025;

        @CCD(
                id = "Hearing_Aberdeen",
                label = "Venue",
                showCondition = "hearingVenueDayScotland=\"Aberdeen\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field026;

        @CCD(
                id = "Hearing_Dundee",
                label = "Venue",
                showCondition = "hearingVenueDayScotland=\"Dundee\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field027;

        @CCD(
                id = "Hearing_Edinburgh",
                label = "Venue",
                showCondition = "hearingVenueDayScotland=\"Edinburgh\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field028;
    }

    @ComplexType(name = "Deposit", generate = true)
    public static final class DepositType {
        @CCD(
                id = "Deposit_amount",
                label = "Deposit amount (£)",
                hint = "Enter a value less than £10,000",
                typeOverride = FieldType.MoneyGBP,
                min = 0,
                max = 999900,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "depositOrderAgainst",
                label = "Deposit ordered against",
                showCondition = "deposit_covers=\"dummy\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ClaimantOrRespondent",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "dynamicDepositOrderAgainst",
                label = "Deposit ordered against",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "deposit_requested_by",
                label = "Deposit requested by",
                showCondition = "deposit_covers=\"dummy\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_DepositRequestedBy",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "dynamicDepositRequestedBy",
                label = "Deposit requested by",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "deposit_covers",
                label = "Deposit covers",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_DepositCovers",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "deposit_order_sent",
                label = "Deposit order sent",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "deposit_due",
                label = "Deposit due",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "deposit_time_ext",
                label = "Deposit time extension",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "deposit_time_ext_due",
                label = "Deposit time extension due",
                showCondition = "deposit_time_ext=\"Yes\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "depositReceived",
                label = "Deposit received",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "depositReceivedDate",
                label = "Date deposit received",
                showCondition = "depositReceived=\"Yes\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "deposit_refund",
                label = "Deposit refund",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "depositAmountRefunded",
                label = "Amount Refunded",
                showCondition = "deposit_refund=\"Yes\"",
                typeOverride = FieldType.MoneyGBP,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "deposit_refund_date",
                label = "Deposit refund date",
                showCondition = "deposit_refund=\"Yes\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "depositRefundedTo",
                label = "Deposit refunded to",
                showCondition = "deposit_covers=\"dummy\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ClaimantOrRespondent",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "dynamicDepositRefundedTo",
                label = "Deposit refunded to",
                showCondition = "deposit_refund=\"Yes\"",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "dynamicDepositRefundedTo",
                label = "Deposit refunded to",
                showCondition = "deposit_refund=\"Yes\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "Yes",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field017;

        @CCD(
                id = "depositNotes",
                label = "Notes",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "depositDoc",
                label = "Document Upload",
                typeOverride = FieldType.Document,
                categoryID = "C27",
                includeInProfiles = SingleDefinition.class)
        private Object field019;
    }

    @ComplexType(name = "DigitalCaseFile", generate = true)
    public static final class DigitalCaseFileType {
        @CCD(
                id = "uploadedDocument",
                label = "Digital Case File",
                typeOverride = FieldType.Document,
                categoryID = "C69",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "dateGenerated",
                label = "Date Generated",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "status",
                label = "Status",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "error",
                label = "Error",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "DocumentImage", generate = true)
    public static final class DocumentImageType {
        @CCD(
                id = "imageRendering",
                label = "How would you like to have the image rendered?",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "imageRendering",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "imageRenderingLocation",
                label = "Where would you like to embed the image in the stitched document?",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "imageRenderingLocation",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "docmosisAssetId",
                label = "The filename of the image you wish to embed",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "coordinateX",
                label = "X position of the image",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "coordinateY",
                label = "Y position of the image",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field005;
    }

    @ComplexType(name = "DocumentUpload", generate = true)
    public static final class DocumentUploadType {
        @CCD(
                id = "topLevelDocuments",
                label = "Document Category",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_DocumentCategories",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "startingClaimDocuments",
                label = "Starting a Claim",
                showCondition = "topLevelDocuments=\"Starting a Claim\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_StartingAClaim",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "responseClaimDocuments",
                label = "Response to a Claim",
                showCondition = "topLevelDocuments=\"Response to a Claim\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ResponseToAClaim",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "initialConsiderationDocuments",
                label = "Initial Consideration",
                showCondition = "topLevelDocuments=\"Initial Consideration\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_InitialConsideration",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "caseManagementDocuments",
                label = "Case Management",
                showCondition = "topLevelDocuments=\"Case Management\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_CaseManagement",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "eccDocuments",
                label = "Employer Contract Claim",
                showCondition = "topLevelDocuments=\"Employer Contract Claim\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_EmployerContractClaim",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "withdrawalSettledDocuments",
                label = "Withdrawal/Settled",
                showCondition = "topLevelDocuments=\"Withdrawal/Settled\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_WithdrawalSettled",
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "hearingsDocuments",
                label = "Hearings",
                showCondition = "topLevelDocuments=\"Hearings\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Hearings",
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "judgmentAndReasonsDocuments",
                label = "Judgment and Reasons",
                showCondition = "topLevelDocuments=\"Judgment and Reasons\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_JudgmentAndReasons",
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "reconsiderationDocuments",
                label = "Reconsideration",
                showCondition = "topLevelDocuments=\"Reconsideration\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Reconsideration",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "miscDocuments",
                label = "Misc",
                showCondition = "topLevelDocuments=\"Misc\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Misc",
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "typeOfDocument",
                label = "Type of Document",
                showCondition = "topLevelDocuments=\"Legacy Document Names\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_DocumentType",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "typeOfDocument",
                label = "Type of Document",
                showCondition =
                        "topLevelDocuments=\"Legacy Document Names\" OR topLevelDocuments != \"*\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_DocumentType",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field012;

        @CCD(
                id = "uploadedDocument",
                label = "Document",
                typeOverride = FieldType.Document,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "shortDescription",
                label = "Short Description",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "documentType",
                label = "Type of Document",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "dateOfCorrespondence",
                label = "Date of Correspondence",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "docNumber",
                label = "Number",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "documentIndex",
                label = "Document Number",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "excludeFromDcf",
                label = "Exclude from Digital Case File",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field019;
    }

    @ComplexType(name = "DraftAndSignJudgement", generate = true)
    public static final class DraftAndSignJudgementType {
        @CCD(
                id = "isJudgement",
                label = "Is this a Judgment",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "draftAndSignJudgementDocument",
                label = "Document upload",
                showCondition = "isJudgement=\"dummy\"",
                typeOverride = FieldType.Document,
                typeParameterOverride = "DocumentUpload",
                categoryID = "C60",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "draftAndSignJudgementDocuments",
                label = "Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "furtherDirections",
                label = "Any further directions",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "DynamicListCollection", generate = true)
    public static final class DynamicListCollectionType {
        @CCD(
                id = "dynamicList",
                label = " ",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = SingleDefinition.class)
        private Object field001;
    }

    @ComplexType(name = "ET3Vetting", generate = true)
    public static final class ET3VettingType {
        @CCD(
                id = "et3VettingDocument",
                label = "ET3 Processing Document",
                typeOverride = FieldType.Document,
                categoryID = "C72",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "et3IsThereAnEt3Response",
                label = "Is there an ET3 response?",
                showCondition = "et3IsThereAnEt3Response!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "et3NoEt3Response",
                label = "Give details",
                showCondition = "et3NoEt3Response!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "et3GeneralNotes",
                label = "General Notes",
                showCondition = "et3GeneralNotes!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "et3IsThereACompaniesHouseSearchDocument",
                label = "Is there a Companies House search document?",
                showCondition = "et3IsThereACompaniesHouseSearchDocument!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "et3CompanyHouseDocument",
                label = "Upload the Companies House search document",
                showCondition = "et3CompanyHouseDocument!=\"\"",
                typeOverride = FieldType.Document,
                categoryID = "C18",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "et3GeneralNotesCompanyHouse",
                label = "General Notes",
                showCondition = "et3GeneralNotesCompanyHouse!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "et3IsThereAnIndividualSearchDocument",
                label = "Is there an individual insolvency search document?",
                showCondition = "et3IsThereAnIndividualSearchDocument!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "et3IndividualInsolvencyDocument",
                label = "Upload the individual insolvency search document",
                showCondition = "et3IndividualInsolvencyDocument!=\"\"",
                typeOverride = FieldType.Document,
                categoryID = "C18",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "et3GeneralNotesIndividualInsolvency",
                label = "General Notes",
                showCondition = "et3GeneralNotesIndividualInsolvency!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "et3LegalIssue",
                label = "Is there an issue with whether the respondent is a legal entity?",
                showCondition = "et3LegalIssue!=\"\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_respondent_legal_entity",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "et3LegalIssueGiveDetails",
                label = "Give details",
                showCondition = "et3LegalIssueGiveDetails!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "et3GeneralNotesLegalEntity",
                label = "Give details",
                showCondition = "et3GeneralNotesLegalEntity!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "et3GeneralNotesLegalEntity",
                label = "General Notes",
                showCondition = "et3GeneralNotesLegalEntity!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field013;

        @CCD(
                id = "et3ChooseRespondent",
                label = "Select the respondent you are processing",
                showCondition = "et3ChooseRespondent!=\"\"",
                typeOverride = FieldType.DynamicList,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "et3ResponseInTime",
                label = "Did we receive the ET3 response in time?",
                showCondition = "et3ResponseInTime!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "et3ResponseInTimeDetails",
                label = "Give details",
                showCondition = "et3ResponseInTimeDetails!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "et3DoWeHaveRespondentsName",
                label = "Do we have the respondent's name?",
                showCondition = "et3DoWeHaveRespondentsName!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "et3GeneralNotesRespondentName",
                label = "General notes",
                showCondition = "et3GeneralNotesRespondentName!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "et3DoesRespondentsNameMatch",
                label = "Does the respondent's name match?",
                showCondition = "et3DoesRespondentsNameMatch!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "et3RespondentNameMismatchDetails",
                label = "Give details",
                showCondition = "et3RespondentNameMismatchDetails!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field020;

        @CCD(
                id = "et3GeneralNotesRespondentNameMatch",
                label = "General notes",
                showCondition = "et3GeneralNotesRespondentNameMatch!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field021;

        @CCD(
                id = "et3DoWeHaveRespondentsAddress",
                label = "Do we have the respondent's address?",
                showCondition = "et3DoWeHaveRespondentsAddress!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field022;

        @CCD(
                id = "et3GeneralNotesRespondentAddress",
                label = "General notes",
                showCondition = "et3GeneralNotesRespondentAddress!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field023;

        @CCD(
                id = "et3DoesRespondentsAddressMatch",
                label = "Does the respondent's address match?",
                showCondition = "et3DoesRespondentsAddressMatch!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field024;

        @CCD(
                id = "et3RespondentAddressMismatchDetails",
                label = "Give details",
                showCondition = "et3RespondentAddressMismatchDetails!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field025;

        @CCD(
                id = "et3GeneralNotesAddressMatch",
                label = "General notes",
                showCondition = "et3GeneralNotesAddressMatch!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field026;

        @CCD(
                id = "et3IsCaseListedForHearing",
                label = "Is the case listed for hearing?",
                showCondition = "et3IsCaseListedForHearing!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field027;

        @CCD(
                id = "et3IsCaseListedForHearingDetails",
                label = "Give details",
                showCondition = "et3IsCaseListedForHearingDetails!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field028;

        @CCD(
                id = "et3GeneralNotesCaseListed",
                label = "General notes",
                showCondition = "et3GeneralNotesCaseListed!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field029;

        @CCD(
                id = "et3IsThisLocationCorrect",
                label = "Is this location correct?",
                showCondition = "et3IsThisLocationCorrect!=\"\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_et3_tribunal_location_change",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field030;

        @CCD(
                id = "et3RegionalOffice",
                label = "England & Wales regional office",
                showCondition = "et3RegionalOffice!=\"\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_TribunalOffice",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field031;

        @CCD(
                id = "et3WhyWeShouldChangeTheOffice",
                label = "Why should we change the office?",
                showCondition = "et3WhyWeShouldChangeTheOffice!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field032;

        @CCD(
                id = "et3GeneralNotesTransferApplication",
                label = "General notes",
                showCondition = "et3GeneralNotesTransferApplication!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field033;

        @CCD(
                id = "et3ContestClaim",
                label = "Does the respondent wish to contest any part of the claim?",
                showCondition = "et3ContestClaim!=\"\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_contest_claim_status",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field034;

        @CCD(
                id = "et3ContestClaimGiveDetails",
                label = "Give details",
                showCondition = "et3ContestClaimGiveDetails!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field035;

        @CCD(
                id = "et3GeneralNotesContestClaim",
                label = "General notes",
                showCondition = "et3GeneralNotesContestClaim!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field036;

        @CCD(
                id = "et3ContractClaimSection7",
                label = "Is there an Employer's Contract Claim in section 7 of the ET3 response?",
                showCondition = "et3ContractClaimSection7!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field037;

        @CCD(
                id = "et3ContractClaimSection7Details",
                label = "Give details",
                showCondition = "et3ContractClaimSection7Details!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field038;

        @CCD(
                id = "et3GeneralNotesContractClaimSection7",
                label = "General notes",
                showCondition = "et3GeneralNotesContractClaimSection7!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field039;

        @CCD(
                id = "et3Rule26",
                label =
                        "Are there any issues identified for the judge's initial consideration -"
                            + " prospects of claim / response arguable? (Rule 27)",
                showCondition = "et3Rule26!=\"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field040;

        @CCD(
                id = "et3Rule26Details",
                label = "Give details",
                showCondition = "et3Rule26Details!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field041;

        @CCD(
                id = "et3SuggestedIssuesStrikeOut",
                label = "Applications for strike out or deposit",
                showCondition = "et3SuggestedIssuesStrikeOut!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field042;

        @CCD(
                id = "et3SuggestedIssueInterpreters",
                label = "Interpreters",
                showCondition = "et3SuggestedIssueInterpreters!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field043;

        @CCD(
                id = "et3SuggestedIssueJurisdictional",
                label = "Jurisdictional issues",
                showCondition = "et3SuggestedIssueJurisdictional!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field044;

        @CCD(
                id = "et3SuggestedIssueAdjustments",
                label = "Request for adjustments",
                showCondition = "et3SuggestedIssueAdjustments!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field045;

        @CCD(
                id = "et3SuggestedIssueRule50",
                label = "Rule 49",
                showCondition = "et3SuggestedIssueRule50!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field046;

        @CCD(
                id = "et3SuggestedIssueTimePoints",
                label = "Time points",
                showCondition = "et3SuggestedIssueTimePoints!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field047;

        @CCD(
                id = "et3GeneralNotesRule26",
                label = "General notes",
                showCondition = "et3GeneralNotesRule26!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field048;

        @CCD(
                id = "et3AdditionalInformation",
                label = "Additional information",
                showCondition = "et3AdditionalInformation!=\"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field049;

        @CCD(
                id = "et3SuggestedIssues",
                label = "Are there any other suggested orders, directions or issues?",
                showCondition = "et3IsThereAnEt3Response=\"dummy\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "fl_et3_suggested_issues",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field050;
    }

    @ComplexType(name = "EmploymentDetails", generate = true)
    public static final class EmploymentDetailsType {
        @CCD(
                id = "claimantEmploymentDetails",
                label = "Employment Details",
                typeOverride = FieldType.Label,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "claimant_occupation",
                label = "Occupation",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "claimant_employed_from",
                label = "Employed from",
                typeOverride = FieldType.Date,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "claimant_employed_currently",
                label = "Is the employment continuing?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "claimant_employed_to",
                label = "Employed to",
                showCondition = "claimant_employed_currently=\"No\"",
                typeOverride = FieldType.Date,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "claimant_employed_notice_period",
                label = "Notice Period End Date",
                showCondition = "claimant_employed_currently=\"Yes\"",
                typeOverride = FieldType.Date,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "claimant_disabled",
                label = "Are there any disabilities or special requirements?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "claimant_disabled_details",
                label = "Please provide details",
                showCondition = "claimant_disabled=\"Yes\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "claimant_notice_period",
                label = "Notice Period",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "claimant_notice_period_unit",
                label = "Notice Weeks or Months",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_notice_period_unit",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "claimant_notice_period_duration",
                label = "Notice Period Duration",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "claimant_average_weekly_hours",
                label = "Average weekly hours",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "claimant_pay_before_tax",
                label = "Pay before tax",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "claimant_pay_after_tax",
                label = "Pay after tax",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "claimant_pay_cycle",
                label = "Weekly, monthly or annual pay",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_pay_cycle",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "claimant_pension_contribution",
                label = "Pension Scheme",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_pension_contribution",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "claimant_pension_weekly_contribution",
                label = "Pension Contribution",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "claimant_benefits",
                label = "Employee Benefits",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "claimant_benefits_detail",
                label = "Employee Benefits Details",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "pastEmployer",
                label = "Did you work for the organisation or person",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field020;

        @CCD(
                id = "stillWorking",
                label = "Are you still working for the organisation or person",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_StillWorking",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field021;
    }

    @ComplexType(name = "Et3NotificationDocUpload", generate = true)
    public static final class Et3NotificationDocUploadType {
        @CCD(
                id = "typeOfDocument",
                label = "Type of Document",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Et3NotificationDocType",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "uploadedDocument",
                label = "Document",
                typeOverride = FieldType.Document,
                categoryID = "C18",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "shortDescription",
                label = "Short Description",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "Hearing", generate = true)
    public static final class HearingType {
        @CCD(
                id = "hearingShowDetails",
                label = "Show Hearing Details",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "Hearing_type",
                label = "Hearing type",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Hearing",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "hearingNotesDocument",
                label = "Hearing notes",
                typeOverride = FieldType.Document,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "hearingDates",
                label = "Hearing Dates",
                typeOverride = FieldType.Text,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "hearingDates",
                label = "Hearing Days",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field004;

        @CCD(
                id = "hearingFormat",
                label = "Hearing Format",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_HearingFormat",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "judicialMediation",
                label = "Judicial Mediation",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "hearingPublicPrivate",
                label = "Public or Private?",
                showCondition = "Hearing_type=\"Preliminary Hearing\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_PublicPrivate",
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "hearingPublicPrivate",
                label = "Public or Private?",
                showCondition = "Hearing_type=\"Preliminary Hearing\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_PublicPrivate",
                retainHiddenValueValue = "Yes",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field007;

        @CCD(
                id = "hearingNumber",
                label = "Hearing Number",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "Hearing_venue",
                label = "Hearing Venue",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field009;

        @CCD(
                id = "hearingEstLengthNum",
                label = "Estimated hearing length",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "hearingEstLengthNumType",
                label = "Days, Hours or Minutes",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingLength",
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "hearingSitAlone",
                label = "Panel Type",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_SitAlone",
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "judge",
                label = "Employment Judge",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "additionalJudge",
                label = "Employment Judge",
                showCondition = "hearingSitAlone = \"Two Judges\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "hearingERMember",
                label = "Employer Member",
                showCondition = "hearingSitAlone = \"Full Panel\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "hearingEEMember",
                label = "Employee Member",
                showCondition = "hearingSitAlone = \"Full Panel\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "Hearing_stage",
                label = "EQP Stage Hearing",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Stage",
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "Hearing_notes",
                label = "Hearing Notes",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "hearingDateCollection",
                label = "Day",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DateListed",
                min = 1,
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "Hearing_venue_Scotland",
                label = "Managing Office",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "VenueScotland",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field020;

        @CCD(
                id = "Hearing_Glasgow",
                label = "Venue",
                showCondition = "Hearing_venue_Scotland=\"Glasgow\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "Yes",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field021;

        @CCD(
                id = "Hearing_Aberdeen",
                label = "Venue",
                showCondition = "Hearing_venue_Scotland=\"Aberdeen\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "Yes",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field022;

        @CCD(
                id = "Hearing_Dundee",
                label = "Venue",
                showCondition = "Hearing_venue_Scotland=\"Dundee\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "Yes",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field023;

        @CCD(
                id = "Hearing_Edinburgh",
                label = "Venue",
                showCondition = "Hearing_venue_Scotland=\"Edinburgh\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field024;
    }

    @ComplexType(name = "HearingBundle", generate = true)
    public static final class HearingBundleType {
        @CCD(
                id = "agreedDocWith",
                label = "Have you agreed these documents with the other party?",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "agreedDocWithBut",
                label = "Which documents are disputed",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "agreedDocWithNo",
                label = "Why you've not been able to agree with the other party",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "hearing",
                label = "Hearing",
                showCondition = "formattedSelectedHearing=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "formattedSelectedHearing",
                label = "Hearing",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "formattedSelectedHearing",
                label = "Hearing these documents are for",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "whatDocuments",
                label = "Type",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "whatDocumentsOther",
                label = "Type (other)",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "whoseDocuments",
                label = "Whose hearing documents are you uploading?",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "uploadFile",
                label = "Document",
                typeOverride = FieldType.Document,
                typeParameterOverride = "DocumentUpload",
                categoryID = "C57",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "uploadDateTime",
                label = "Uploaded date",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "submittedDate",
                label = "Submitted",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "uploadedBy",
                label = "Uploaded by",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field012;
    }

    @ComplexType(name = "HearingDetails", generate = true)
    public static final class HearingDetailsType {
        @CCD(
                id = "hearingDetailsDate",
                label = "Hearing Date",
                typeOverride = FieldType.DateTime,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "hearingDetailsStatus",
                label = "Hearing Status",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingStatus",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "hearingDetailsPostponedBy",
                label = "Postponed by",
                showCondition = "hearingDetailsStatus=\"Postponed\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_PostponedBy",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "hearingDetailsCaseDisposed",
                label = "Has the case or part of the case been disposed?",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "hearingDetailsPartHeard",
                label = "Has the hearing been part heard?",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "hearingDetailsReservedJudgment",
                label = "Is there a reserved Judgment?",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "hearingDetailsAttendeeClaimant",
                label = "Attendees (Claimant)",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Attendee",
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "hearingDetailsAttendeeNonAttendees",
                label = "Number of Non Attendees (Respondent) ",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "hearingDetailsAttendeeRespNoRep",
                label = "Respondent Attended - No Representative",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "hearingDetailsAttendeeRespAndRep",
                label = "Respondent and Representative Attended",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "hearingDetailsAttendeeRepOnly",
                label = "Respondent representative only attended",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "hearingDetailsTimingStart",
                label = "Start Time",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.DateTime,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "hearingDetailsTimingBreak",
                label = "Break",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.DateTime,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "hearingDetailsTimingResume",
                label = "Resume",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.DateTime,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "hearingDetailsTimingFinish",
                label = "Finish",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.DateTime,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "hearingDetailsTimingDuration",
                label = "Duration",
                showCondition = "hearingDetailsStatus=\"Heard\"",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "hearingDetailsHearingNotes2",
                label = "Hearing Notes",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field017;
    }

    @ComplexType(name = "HearingDocumentUpload", generate = true)
    public static final class HearingDocumentUploadType {
        @CCD(
                id = "document",
                label = "Upload Hearing Documents",
                hint = "Upload a single PDF file containing the hearing documents",
                regex = ".pdf",
                typeOverride = FieldType.Document,
                typeParameterOverride = "DocumentUpload",
                categoryID = "C57",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "type",
                label = "What are these hearing documents?",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_bundleType",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "typeOther",
                label = "Please specify",
                showCondition = "type=\"Other\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "HearingPreference", generate = true)
    public static final class HearingPreferenceType {
        @CCD(
                id = "hearing_preferences",
                label = "What are the claimant's hearing preferences",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_HearingPreferences",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "hearing_preferences",
                label = "What are the claimant's hearing preferences",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_HearingPreferences",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "hearing_assistance",
                label = "Why is the claimant unable to take part in video or phone hearings",
                showCondition = "hearing_preferences CONTAINS \"Neither\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "hearing_assistance",
                label = "Why is the claimant unable to take part in video or phone hearings",
                showCondition = "hearing_preferences CONTAINS \"Neither\"",
                typeOverride = FieldType.TextArea,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "reasonable_adjustments",
                label =
                        "Do you have a physical, mental or learning disability or long term health"
                            + " condition that means you need support during your case?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "reasonable_adjustments",
                label =
                        "Do you have a physical, mental or learning disability or long term health"
                            + " condition that means you need support during your case?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field003;

        @CCD(
                id = "reasonable_adjustments_detail",
                label = "Tell us what support you need to request",
                showCondition = "reasonable_adjustments=\"Yes\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "reasonable_adjustments_detail",
                label = "Tell us what support you need to request",
                showCondition = "reasonable_adjustments=\"Yes\"",
                typeOverride = FieldType.TextArea,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field004;

        @CCD(
                id = "contact_language",
                label = "Contact language",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_languages",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "hearing_language",
                label =
                        "If a hearing is required, what language do you want to speak at a"
                            + " hearing?",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_languages",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field006;

        @CCD(
                id = "claimant_hearing_panel_preference",
                label = "Hearing panel preference",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "claimant_hearingPanelPreference",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "claimant_hearing_panel_preference_why",
                label = "Panel preference reason",
                showCondition =
                        "claimant_hearing_panel_preference=\"Judge\" OR"
                            + " claimant_hearing_panel_preference=\"Panel\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field008;
    }

    @ComplexType(name = "Judgment", generate = true)
    public static final class JudgmentType {
        @CCD(
                id = "jurisdictionCodes",
                label = "Jurisdiction",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "Jurisdiction",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "non_hearing_judgment",
                label = "Non Hearing Judgment?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "judgmentHearingDate",
                label = "Date of Hearing",
                showCondition = "non_hearing_judgment=\"No\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "dynamicJudgementHearing",
                label = "Hearing Details",
                showCondition = "non_hearing_judgment=\"No\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "judgement_type",
                label = "Judgment Type",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_JudgementType",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "liability_optional",
                label = "Liability",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Liability",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "date_judgment_made",
                label = "Date Judgment made",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "date_judgment_sent",
                label = "Date Judgment sent",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "judgment_notes",
                label = "Judgment Notes",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "judgement_outcome_doc",
                label = "Upload outcome of Judgment",
                typeOverride = FieldType.Document,
                categoryID = "C60",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "judgement_details",
                label = "Judgment details",
                hint = " ",
                typeNameOverride = "JudgmentDetails",
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "Judgement_costs",
                label = "Costs",
                typeNameOverride = "JudgmentCosts",
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "reconsiderations",
                label = "Reconsideration",
                hint = " ",
                typeNameOverride = "judgmentReconsideration",
                includeInProfiles = SingleDefinition.class)
        private Object field013;
    }

    @ComplexType(name = "JudgmentCosts", generate = true)
    public static final class JudgmentCostsType {
        @CCD(
                id = "costs_question",
                label = "Have costs been awarded?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "costs_expenses_awarded_to",
                label = "Costs/Expenses awarded to",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ClaimantOrRespondent",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "costs_expenses_awarded_against",
                label = "Costs/Expenses awarded against",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ClaimantOrRespondent",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "costs_expenses_awarded_amount",
                label = "Costs/Expenses amount awarded",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.MoneyGBP,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "preparation_of_time_awarded_to",
                label = "Preparation of time awarded to",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ClaimantOrRespondent",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "preparation_of_time_awarded_against",
                label = "Preparation of time awarded against",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ClaimantOrRespondent",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "preparation_of_time_amount_awarded",
                label = "Preparation of time amount awarded",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.MoneyGBP,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "wasted_cost_awarded_to",
                label = "Wasted cost awarded to",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ClaimantOrRespondent",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "wasted_cost_awarded_against",
                label = "Wasted cost awarded against",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ClaimantOrRespondent",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "wasted_cost_amount_awarded",
                label = "Wasted cost amount awarded",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.MoneyGBP,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "pro_bono_costs_awarded_to",
                label = "Pro Bono costs awarded to",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_costs_pro_bono_awarded_to",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "pro_bono_costs_awarded_against",
                label = "Pro Bono costs awarded against",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_pro_bono_awarded_against",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "pro_bono_costs_amount_awarded",
                label = "Pro Bono costs amount awarded",
                showCondition = "costs_question=\"Yes\"",
                typeOverride = FieldType.MoneyGBP,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field013;
    }

    @ComplexType(name = "JudgmentDetails", generate = true)
    public static final class JudgmentDetailsType {
        @CCD(
                id = "folio_number",
                label = "Folio Number",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "reasons_given",
                label = "Reasons given",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "date_reasons_issued",
                label = "Date reasons issued",
                showCondition = "reasons_given=\"Yes\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "awardMade",
                label = "Award made?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "financialAwardMade",
                label = "Financial award made?",
                showCondition = "awardMade=\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "remedy_left_to_parties",
                label = "Remedy left to parties",
                showCondition = "non-financial_award=\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "reinstate_reengage_order",
                label = "Reinstate / reengage order",
                showCondition = "non-financial_award=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Reinstate",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "reinstated_reengaged",
                label = "Reinstated / reengaged",
                showCondition = "non-financial_award=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Reinstated",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "certificateOfCorrection",
                label = "Certificate of Correction Issued?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "cert_of_correction_date",
                label = "Cert. of correction date",
                showCondition = "certificateOfCorrection=\"Yes\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "cert_of_correction_sent",
                label = "Cert. of correction sent",
                showCondition = "certificateOfCorrection=\"Yes\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "non-financial_award",
                label = "Non-financial award",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "total_award",
                label = "Total award £",
                showCondition = "financialAwardMade=\"Yes\"",
                typeOverride = FieldType.MoneyGBP,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "adjustment",
                label = "Adjustment (Old Regs) / ACAS Code Adj (New Regs) ",
                showCondition = "financialAwardMade=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Adjustment",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "adjustmentPercentage",
                label = "Adjustment % (New Regs)",
                hint = "0 - 25%",
                showCondition =
                        "adjustment=\"Increase (New regs)\" OR adjustment=\"Decrease (New regs)\"",
                typeOverride = FieldType.Number,
                min = 0,
                max = 25,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field015;
    }

    @ComplexType(name = "Jurisdiction", generate = true)
    public static final class JurisdictionType {
        @CCD(
                id = "juridictionCodesList",
                label = "Jurisdiction Code",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_jurisdictionCodes",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "jurisdictionCodeADT",
                label = "Discriminatory terms or rules",
                showCondition = "juridictionCodesList=\"ADT\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "jurisdictionCodeAPA",
                label =
                        "Application by an employee, their representative or trade union for a"
                            + " protective award as a result of an employer’s failure to consult"
                            + " over a redundancy situation",
                showCondition = "juridictionCodesList = \"APA\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "jurisdictionCodeCCP",
                label =
                        "Failure of the employer to consult with an employee representative or"
                            + " trade union about a proposed contracting out of a pension scheme",
                showCondition = "juridictionCodesList = \"CCP\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "jurisdictionCodeCOM",
                label =
                        "Application or complaint by the EHRC in respect of discriminatory"
                            + " advertisements or instructions or pressure to discriminate"
                            + " (including preliminary action before a claim to the county court)",
                showCondition = "juridictionCodesList = \"COM\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "jurisdictionCodeEAP",
                label =
                        "Application by the Secretary of State for Business, Innovation & Skills to"
                            + " prohibit a person from running an Employment Agency",
                showCondition = "juridictionCodesList = \"EAP\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "jurisdictionCodeHAS",
                label =
                        "Appeal against an enforcement, improvement or prohibition notice imposed"
                            + " by the HSE or Environmental Health Inspector, or by the Environment"
                            + " Agency",
                showCondition = "juridictionCodesList = \"HAS\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "jurisdictionCodeISV",
                label =
                        "Failure by the SOS to make an insolvency payment in lieu of wages and/or"
                            + " redundancy",
                showCondition = "juridictionCodesList = \"ISV\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "jurisdictionCodeLEV ",
                label = "Appeal against the levy assessment of an Industrial training Board",
                showCondition = "juridictionCodesList = \"LEV\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "jurisdictionCodeLSO",
                label = "Loss of office as a result of the reorganisation of a statutory body",
                showCondition = "juridictionCodesList = \"LSO\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "jurisdictionCodeMWA",
                label =
                        "Appeal against an enforcement or penalty notice issued by Her Majesty's"
                            + " Revenue & Customs",
                showCondition = "juridictionCodesList = \"MWA\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "jurisdictionCodeNNA",
                label = "Appeal against an unlawful act on a notice issued by EHRC",
                showCondition = "juridictionCodesList = \"NNA\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "jurisdictionCodePEN",
                label =
                        "Failure of the Secretary of State to pay unpaid contributions to a"
                            + " pensions scheme following an application for payment to be made",
                showCondition = "juridictionCodesList = \"PEN\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "jurisdictionCodeRPT(S) ",
                label =
                        "Failure of the Secretary of State for Trade & Industry to pay a redundancy"
                            + " payment following an application to the National Insurance Fund",
                showCondition = "juridictionCodesList = \"RPT(S)\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "jurisdictionCodeRTR",
                label =
                        "Appeal against an improvement by a VOSA inspector (JST to be notified if"
                            + " claim received)",
                showCondition = "juridictionCodesList = \"RTR\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "jurisdictionCodeTXC",
                label = "Appeal against \"Failure to pay an Employment Tribunal award Penalty\"",
                showCondition = "juridictionCodesList = \"TXC\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "jurisdictionCodeWTA",
                label =
                        "Appeal by a person who has been served with an improvement or prohibition"
                            + " notice under the Working Time Regulations 1998",
                showCondition = "juridictionCodesList = \"WTA\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "jurisdictionCodeDAG",
                label =
                        "Discrimination, including harassment or discrimination based on"
                            + " association or perception on grounds of age",
                showCondition = "juridictionCodesList = \"DAG\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "jurisdictionCodeDDA",
                label =
                        "Suffered a detriment, discrimination, including indirect discrimination,"
                            + " and discrimination based on association or perception, harassment"
                            + " and/or dismissal on grounds of disability or failure of employer to"
                            + " make reasonable adjustments",
                showCondition = "juridictionCodesList = \"DDA\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "jurisdictionCodeDRB",
                label =
                        "Discrimination, including indirect discrimination, discrimination based on"
                            + " association or perception or harassment on grounds of religion or"
                            + " belief",
                showCondition = "juridictionCodesList = \"DRB\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field020;

        @CCD(
                id = "jurisdictionCodeDSO",
                label =
                        "Discrimination, including indirect discrimination, discrimination based on"
                            + " association or perception or harassment on grounds of sexual"
                            + " orientation",
                showCondition = "juridictionCodesList = \"DSO\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field021;

        @CCD(
                id = "jurisdictionCodeEQP",
                label = "Failure to provide equal pay for equal value work",
                showCondition = "juridictionCodesList = \"EQP\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field022;

        @CCD(
                id = "jurisdictionCodePID",
                label =
                        "Suffered a detriment and/or dismissal due to exercising rights under the"
                            + " Public Interest Disclosure Act",
                showCondition = "juridictionCodesList = \"PID\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field023;

        @CCD(
                id = "jurisdictionCodeRRD",
                label =
                        "Discrimination, including indirect discrimination based on association or"
                            + " perception or harassment on grounds of race or ethnic origin",
                showCondition = "juridictionCodesList = \"RRD\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field024;

        @CCD(
                id = "jurisdictionCodeSXD",
                label =
                        "Discrimination, including indirect discrimination, discrimination based on"
                            + " association or perception, or harassment on grounds of sex,"
                            + " marriage and civil partnership",
                showCondition = "juridictionCodesList = \"SXD\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field025;

        @CCD(
                id = "jurisdictionCodeBOC",
                label = "Claim of an employee for breach of contract of employment",
                showCondition = "juridictionCodesList = \"BOC\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field026;

        @CCD(
                id = "jurisdictionCodeECC",
                label = "Employer contract claim",
                showCondition = "juridictionCodesList = \"ECC\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field027;

        @CCD(
                id = "jurisdictionCodeFML",
                label =
                        "Failure to pay remuneration whilst suspended from work for health and"
                            + " safety reasons whilst pregnant or on mat. Leave",
                showCondition = "juridictionCodesList = \"FML\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field028;

        @CCD(
                id = "jurisdictionCodeFPA",
                label =
                        "Application by an employee that an employer has failed to pay a protected"
                            + " award as ordered by a tribunal",
                showCondition = "juridictionCodesList = \"FPA\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field029;

        @CCD(
                id = "jurisdictionCodeFPI",
                label =
                        "Failure of a transferor to provide employee liability information to a"
                            + " transferee",
                showCondition = "juridictionCodesList = \"FPI\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field030;

        @CCD(
                id = "jurisdictionCodeFTC",
                label =
                        "Failure to provide a written statement of terms and conditions and any"
                            + " subsequent changes to those terms",
                showCondition = "juridictionCodesList = \"FTC\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field031;

        @CCD(
                id = "jurisdictionCodeFTO",
                label =
                        "Failure to allow time off for trade union activities or duties, for"
                            + " ante-natal care, for adoption, for parental leave, for carer’s"
                            + " leave or for public duties",
                showCondition = "juridictionCodesList = \"FTO\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field032;

        @CCD(
                id = "jurisdictionCodeFTP",
                label = "Failure to provide a guarantee payment",
                showCondition = "juridictionCodesList = \"FTP\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field033;

        @CCD(
                id = "jurisdictionCodeFTR",
                label = "Failure to pay remuneration whilst suspended for medical reasons",
                showCondition = "juridictionCodesList = \"FTR\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field034;

        @CCD(
                id = "jurisdictionCodeFTS",
                label = "Failure to allow time off to seek work during a redundancy situation",
                showCondition = "juridictionCodesList = \"FTS\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field035;

        @CCD(
                id = "jurisdictionCodeFTU",
                label =
                        "Failure of an employer to comply with an award by a tribunal following a"
                            + " finding that the employer had previously failed to consult about a"
                            + " proposed transfer of an undertaking",
                showCondition = "juridictionCodesList = \"FTU\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field036;

        @CCD(
                id = "jurisdictionCodeGRA",
                label =
                        "Discrimination, including indirect discrimination, discrimination based on"
                            + " association or perception, or harassment because of gender"
                            + " reassignment",
                showCondition = "juridictionCodesList = \"GRA\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field037;

        @CCD(
                id = "jurisdictionCodePAY",
                label =
                        "Failure of the employer to prevent unauthorised or excessive deductions in"
                            + " the form of union subscriptions",
                showCondition = "juridictionCodesList = \"PAY\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field038;

        @CCD(
                id = "jurisdictionCodeRPT",
                label = "Failure to pay a redundancy payment",
                showCondition = "juridictionCodesList = \"RPT\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field039;

        @CCD(
                id = "jurisdictionCodeWA",
                label = "Failure of employer to pay or unauthorised deductions have been made",
                showCondition = "juridictionCodesList = \"WA\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field040;

        @CCD(
                id = "jurisdictionCodeWTR(AL)",
                label =
                        "Complaint by a worker that employer has failed to allow them to take or to"
                            + " pay them for statutory annual leave entitlement",
                showCondition = "juridictionCodesList = \"WTR(AL)\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field041;

        @CCD(
                id = "jurisdictionCodeADG",
                label =
                        "Suffer a detriment and/or dismissal resulting from a failure to allow an"
                            + " employee to be accompanied or tp accompany a fellow employee at a"
                            + " disciplinary/ grievance hearing",
                showCondition = "juridictionCodesList = \"ADG\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field042;

        @CCD(
                id = "jurisdictionCodeADT(ST)",
                label =
                        "Application for a declaration that the inclusion of discriminatory"
                            + " terms/rules within certain agreements or rules causes the aforesaid"
                            + " to be invalid",
                showCondition = "juridictionCodesList = \"ADT(ST)\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field043;

        @CCD(
                id = "jurisdictionCodeAWR",
                label =
                        "Suffered less favourable treatment and/or dismissal as an agency worker,"
                            + " than a directly recruited employee",
                showCondition = "juridictionCodesList = \"AWR\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field044;

        @CCD(
                id = "jurisdictionCodeDOD",
                label =
                        "Suffered a detriment and/or dismissal resulting from requiring time off"
                            + " for other (non-work but not Health and Safety) duties, study,"
                            + " training or seeking work",
                showCondition = "juridictionCodesList = \"DOD\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field045;

        @CCD(
                id = "jurisdictionCodeFCT",
                label =
                        "Failure of the employer to consult with an employee rep. or trade union or"
                            + " a transferor with a transferee about a proposed transfer",
                showCondition = "juridictionCodesList = \"FCT\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field046;

        @CCD(
                id = "jurisdictionCodeFLW",
                label =
                        "Suffer a detriment and/or dismissal for claiming under the flexible"
                            + " working regulations or be subject to a breach of procedure",
                showCondition = "juridictionCodesList = \"FLW\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field047;

        @CCD(
                id = "jurisdictionCodeFTE",
                label =
                        "Suffered less favourable treatment and/or dismissal as a fixed term"
                            + " employee, than a full-time employee or, on becoming permanent,"
                            + " failed to receive a written statement of confirmation from"
                            + " employer",
                showCondition = "juridictionCodesList = \"FTE\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field048;

        @CCD(
                id = "jurisdictionCodeFT1",
                label =
                        "Failure to allow or to pay for time off for care of dependants, union"
                            + " learning representatives duties, pension scheme trustee duties,"
                            + " employee representatives duties, young person studying/training and"
                            + " European Works Council duties",
                showCondition = "juridictionCodesList = \"FT1\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field049;

        @CCD(
                id = "jurisdictionCodeFWP",
                label = "Failure to provide a written pay statement or an adequate pay statement",
                showCondition = "juridictionCodesList = \"FWP\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field050;

        @CCD(
                id = "jurisdictionCodeFWS",
                label =
                        "Failure to provide a written statement of reasons for dismissal or the"
                            + " contents of the statement are disputed",
                showCondition = "juridictionCodesList = \"FWS\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field051;

        @CCD(
                id = "jurisdictionCodeHSD",
                label =
                        "Failure to pay for or allow time off to carry out Safety Rep duties or"
                            + " undertake training",
                showCondition = "juridictionCodesList = \"HSD\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field052;

        @CCD(
                id = "jurisdictionCodeHSR",
                label = "Suffer a detriment, dismissal or redundancy for health and safety reasons",
                showCondition = "juridictionCodesList = \"HSR\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field053;

        @CCD(
                id = "jurisdictionCodeIRF",
                label = "Application for interim relief",
                showCondition = "juridictionCodesList = \"IRF\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field054;

        @CCD(
                id = "jurisdictionCodeMAT",
                label =
                        "Suffer a detriment and/or dismissal on grounds of pregnancy, child birth"
                            + " or maternity",
                showCondition = "juridictionCodesList = \"MAT\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field055;

        @CCD(
                id = "jurisdictionCodeMWD",
                label =
                        "Suffer a detriment and/or dismissal related to failure to pay the minimum"
                            + " wage or allow access to records",
                showCondition = "juridictionCodesList = \"MWD\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field056;

        @CCD(
                id = "jurisdictionCodePAC",
                label =
                        "Failure of the employer to comply with a certificate of exemption or to"
                            + " deduct funds from employees pay in order to contribute to a trade"
                            + " union political fund",
                showCondition = "juridictionCodesList = \"PAC\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field057;

        @CCD(
                id = "jurisdictionCodePLD",
                label =
                        "Suffer a detriment and/or dismissal due to requesting or taking leave for"
                            + " family and domestic reasons including maternity, paternity,"
                            + " adoption, parental bereavement, ante-natal care or carers leave or"
                            + " time off to assist a dependant",
                showCondition = "juridictionCodesList = \"PLD\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field058;

        @CCD(
                id = "jurisdictionCodePTE",
                label =
                        "Suffer less favourable treatment and/or dismissal as a result of being a"
                            + " part time employee by comparison to a full-time employee",
                showCondition = "juridictionCodesList = \"PTE\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field059;

        @CCD(
                id = "jurisdictionCodeRTR(ST)",
                label =
                        "Suffer a detriment or dismissal as a result of doing work under another"
                            + " contract or arrangement (zero hours contract exclusivity clause)",
                showCondition = "juridictionCodesList = \"RTR(ST)\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field060;

        @CCD(
                id = "jurisdictionCodeSUN",
                label = "Suffer a detriment and/or dismissal for refusing to work on a Sunday",
                showCondition = "juridictionCodesList = \"SUN\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field061;

        @CCD(
                id = "jurisdictionCodeTIP",
                label =
                        "Suffer a failure by an employer to comply with provisions about"
                            + " distribution of or information about tips",
                showCondition = "juridictionCodesList = \"TIP\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field062;

        @CCD(
                id = "jurisdictionCodeTPE",
                label =
                        "Suffer less favourable treatment and/or dismissal as a temp. employee than"
                            + " a full-time employee",
                showCondition = "juridictionCodesList = \"TPE\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field063;

        @CCD(
                id = "jurisdictionCodeTT",
                label =
                        "Suffer a detriment and/or dismissal for requesting time to train. Failure"
                            + " of employer to follow correct procedures/reject request based on"
                            + " incorrect facts",
                showCondition = "juridictionCodesList = \"TT\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field064;

        @CCD(
                id = "jurisdictionCodeTUE",
                label =
                        "Suffer discrimination in obtaining employment due to membership or"
                            + " non-membership of a trade union; or refused employment or suffered"
                            + " a detriment for a reason related to a blacklist",
                showCondition = "juridictionCodesList = \"TUE\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field065;

        @CCD(
                id = "jurisdictionCodeTUI",
                label =
                        "Suffer an inducement relating to union membership, activities or"
                            + " collective bargaining",
                showCondition = "juridictionCodesList = \"TUI\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field066;

        @CCD(
                id = "jurisdictionCodeTUM",
                label =
                        "Suffer a detriment and/or dismissal relating to being, not being or"
                            + " proposing to become a trade union member",
                showCondition = "juridictionCodesList = \"TUM\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field067;

        @CCD(
                id = "jurisdictionCodeTUR",
                label =
                        "(a) Failure of the employer to consult or report about training in"
                            + " relation to a bargaining unit\n"
                            + "(b) Suffered a detriment on grounds related to recognition of a"
                            + " trade union for collective bargaining",
                showCondition = "juridictionCodesList = \"TUR\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field068;

        @CCD(
                id = "jurisdictionCodeTUS",
                label =
                        "Suffer discrimination in obtaining the services of an employment agency"
                            + " due to membership or non-membership of a trade union; or refused"
                            + " employment agency services or suffered a detriment for a reason"
                            + " related to a blacklist",
                showCondition = "juridictionCodesList = \"TUS\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field069;

        @CCD(
                id = "jurisdictionCodeTXC(ST)",
                label =
                        "Suffered a detriment and/or dismissal due to exercising rights under the"
                            + " Tax Credits Act (JST to be notified if a claim is received)",
                showCondition = "juridictionCodesList = \"TXC(ST)\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field070;

        @CCD(
                id = "jurisdictionCodeUDC",
                label = "Unfair dismissal after exercising or claiming a statutory right",
                showCondition = "juridictionCodesList = \"UDC\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field071;

        @CCD(
                id = "jurisdictionCodeUDL",
                label =
                        "Unfair dismissal on grounds of capability, conduct or some other general"
                            + " reason including the result of a transfer of an undertaking",
                showCondition = "juridictionCodesList = \"UDL\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field072;

        @CCD(
                id = "jurisdictionCodeUIA",
                label =
                        "unfair dismissal in connection to a lock out, strike or other industrial"
                            + " action",
                showCondition = "juridictionCodesList = \"UIA\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field073;

        @CCD(
                id = "jurisdictionCodeVIC",
                label = "Subjected to a detriment because of a protected act ",
                showCondition = "juridictionCodesList = \"VIC\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "jurisdictionCodeVIC",
                label = "Subjected to a detriment because of a protected act",
                showCondition = "juridictionCodesList = \"VIC\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field074;

        @CCD(
                id = "jurisdictionCodeWTR",
                label = "Failure to limit weekly or night working time, or to ensure rest breaks",
                showCondition = "juridictionCodesList = \"WTR\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field075;

        @CCD(
                id = "jurisdictionCodeZNON",
                label =
                        "Dummy code for purposes of record closure only used when undertaken as"
                            + " part of a reasonable and proportionate data cleansing checking"
                            + " exercise where the actual jurisdiction and outcome are unknown",
                showCondition = "juridictionCodesList = \"ZNON\"",
                typeOverride = FieldType.Label,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field076;

        @CCD(
                id = "judgmentOutcome",
                label = "Outcome",
                showCondition = "juridictionCodesList != \"\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_JudgmentOutcome",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field077;

        @CCD(
                id = "dateNotified",
                label = "Date notified",
                showCondition = "judgmentOutcome = \"dummy\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field078;

        @CCD(
                id = "disposalDate",
                label = "Disposal date",
                hint =
                        "For a hearing disposal, enter the hearing date that disposed of this"
                            + " jurisdiction. For a non-hearing disposal, enter the judgment sent"
                            + " date for this jurisdiction.",
                showCondition =
                        "judgmentOutcome != \"Not allocated\" AND judgmentOutcome != \"Input in"
                            + " error\" AND judgmentOutcome != \"\"",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field079;
    }

    @ComplexType(name = "JurisdictionCode", generate = true)
    public static final class JurisdictionCodeType {
        @CCD(
                id = "et1VettingJurCodeList",
                label = "Jurisdiction code",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_jurisdictionCodes",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "et1VettingJurCodeList",
                label = "Jurisdiction code",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_jurisdictionCodes",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;
    }

    @ComplexType(name = "Letters", generate = true)
    public static final class LettersType {
        @CCD(
                id = "topLevel_Documents",
                label = "Top Level",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_TopLevel",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "part_0_Documents",
                label = "Part 0",
                showCondition = "topLevel_Documents=\"EM-TRB-LET-ENG-00544\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_0",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "part_1_Documents",
                label = "Part 1",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00026\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_1",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field003;

        @CCD(
                id = "part_2_Documents",
                label = "Part 2",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00027\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_2",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field004;

        @CCD(
                id = "part_3_Documents",
                label = "Part 3",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00028\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_3",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "part_4_Documents",
                label = "Part 4",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00029\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_4",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field006;

        @CCD(
                id = "part_5_Documents",
                label = "Part 5",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00030\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_5",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field007;

        @CCD(
                id = "Part_6_Documents",
                label = "Part 6",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00031\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_6",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field008;

        @CCD(
                id = "Part_7_Documents",
                label = "Part 7",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00032\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_7",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field009;

        @CCD(
                id = "Part_8_Documents",
                label = "Part 8",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00065\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_8",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field010;

        @CCD(
                id = "Part_9_Documents",
                label = "Part 9",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00033\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_9",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field011;

        @CCD(
                id = "Part_10_Documents",
                label = "Part 10",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00034\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_10",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field012;

        @CCD(
                id = "Part_11_Documents",
                label = "Part 11",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00035\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_11",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field013;

        @CCD(
                id = "Part_12_Documents",
                label = "Part 12",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00036\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_12",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field014;

        @CCD(
                id = "Part_13_Documents",
                label = "Part 13",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00037\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_13",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field015;

        @CCD(
                id = "Part_14_Documents",
                label = "Part 14",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00038\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_14",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field016;

        @CCD(
                id = "Part_15_Documents",
                label = "Part 15",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00039\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_15",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field017;

        @CCD(
                id = "Part_16_Documents",
                label = "Part 16",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00040\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_16",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field018;

        @CCD(
                id = "Part_17_Documents",
                label = "Part 17",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00041\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_17",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field019;

        @CCD(
                id = "Part_18_Documents",
                label = "Part 18",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00066\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_18",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field020;

        @CCD(
                id = "hearingNumber",
                label = "Hearing Number",
                showCondition = "topLevel_Documents=\"dummy\"",
                typeOverride = FieldType.Number,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "hearingNumber",
                label = "Hearing Number",
                showCondition = "topLevel_Scot_Documents=\"dummy\"",
                typeOverride = FieldType.Number,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field021;

        @CCD(
                id = "dynamicHearingNumber",
                label = "Hearing Number",
                showCondition =
                        "topLevel_Documents=\"EM-TRB-EGW-ENG-00032\" OR"
                            + " topLevel_Documents=\"EM-TRB-EGW-ENG-00065\" OR part_2_Documents ="
                            + " \"2.5\" OR part_2_Documents = \"2.7\" OR part_2_Documents = \"2.7"
                            + " Reform\" OR part_2_Documents = \"2.7A\" OR part_2_Documents ="
                            + " \"2.7A Reform\" OR part_2_Documents = \"2.8\" OR part_2_Documents ="
                            + " \"2.8 Reform\" OR part_2_Documents = \"2.8A\" OR part_2_Documents ="
                            + " \"2.8A Reform\" OR part_4_Documents = \"4.5\" OR part_4_Documents ="
                            + " \"4.8A\"  OR part_5_Documents=\"5.18\" OR"
                            + " part_5_Documents=\"5.18A\" OR Part_7_Documents=\"7.8A\" OR"
                            + " Part_7_Documents=\"7.9A\" OR Part_8_Documents=\"8.1A\" OR"
                            + " Part_11_Documents = \"11.8\" OR Part_11_Documents = \"11.9\" OR"
                            + " Part_15_Documents = \"15.2\" OR Part_15_Documents = \"15.1\" OR"
                            + " Part_15_Documents = \"15.3\" OR Part_15_Documents = \"15.4\" OR"
                            + " Part_16_Documents = \"16.1\" OR Part_16_Documents = \"16.2\" OR"
                            + " Part_17_Documents = \"17.3\"",
                typeOverride = FieldType.DynamicList,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "dynamicHearingNumber",
                label = "Hearing Number",
                showCondition =
                        "Part_9_Scot_Documents=\"73\" OR Part_9_Scot_Documents=\"74\" OR"
                            + " Part_9_Scot_Documents=\"75\" OR Part_9_Scot_Documents=\"76\" OR"
                            + " Part_9_Scot_Documents=\"76 Reform\" OR"
                            + " Part_9_Scot_Documents=\"77.1\" OR Part_9_Scot_Documents=\"77.2\" OR"
                            + " Part_9_Scot_Documents=\"80\" OR Part_9_Scot_Documents=\"81\" OR"
                            + " Part_9_Scot_Documents=\"82\" OR Part_9_Scot_Documents=\"83\" OR"
                            + " Part_9_Scot_Documents=\"84\" OR Part_9_Scot_Documents=\"85\" OR"
                            + " Part_9_Scot_Documents=\"86\" OR Part_9_Scot_Documents=\"87\" OR"
                            + " Part_9_Scot_Documents=\"90\" OR Part_9_Scot_Documents=\"91\" OR"
                            + " Part_9_Scot_Documents=\"91.A\" OR Part_9_Scot_Documents=\"91.B\" OR"
                            + " Part_9_Scot_Documents=\"195\" OR Part_9_Scot_Documents=\"196\" OR"
                            + " Part_9_Scot_Documents=\"198\" OR Part_9_Scot_Documents=\"199\" OR"
                            + " Part_9_Scot_Documents=\"200\" OR Part_9_Scot_Documents=\"201\" OR"
                            + " Part_9_Scot_Documents=\"202\" OR Part_9_Scot_Documents=\"222\" OR"
                            + " Part_9_Scot_Documents=\"223\" OR Part_9_Scot_Documents=\"74.2\" OR"
                            + " Part_9_Scot_Documents=\"199.2\" OR Part_9_Scot_Documents=\"201.2\"",
                typeOverride = FieldType.DynamicList,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field022;

        @CCD(
                id = "Part_20_Documents",
                label = "Part 20",
                showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00043\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_20",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field023;

        @CCD(
                id = "dynamicRespondentsWithEcc",
                label = "Respondents with ECC",
                showCondition =
                        "topLevel_Documents=\"EM-TRB-EGW-ENG-00028\" AND part_3_Documents !="
                            + " \"3.1\" AND part_3_Documents != \"3.2\" AND part_3_Documents !="
                            + " \"3.3\" AND part_3_Documents != \"3.4\" AND part_3_Documents !="
                            + " \"3.5\" AND part_3_Documents != \"3.6\" AND part_3_Documents !="
                            + " \"3.7\" AND part_3_Documents != \"3.22\" AND part_3_Documents !="
                            + " \"3.23\"",
                typeOverride = FieldType.DynamicList,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "dynamicRespondentsWithEcc",
                label = "Respondents with ECC",
                showCondition =
                        "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00044\" AND"
                            + " part_3_Scot_Documents!=\"19\"",
                typeOverride = FieldType.DynamicList,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field024;

        @CCD(
                id = "flipRespondentAndClaimantValues",
                label = "Address ECC letter to Claimant?",
                showCondition =
                        "topLevel_Documents=\"EM-TRB-EGW-ENG-00028\" AND dynamicRespondentsWithEcc"
                            + " != \"\" AND dynamicRespondentsWithEcc != \"No respondents with"
                            + " ECC\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "flipRespondentAndClaimantValues",
                label = "Address ECC letter to Claimant?",
                showCondition =
                        "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00044\" AND"
                            + " dynamicRespondentsWithEcc != \"\" AND dynamicRespondentsWithEcc !="
                            + " \"No respondents with ECC\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field025;

        @CCD(
                id = "letterAddress",
                label = "Which address on the letter?",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_letterAddress",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field026;

        @CCD(
                id = "topLevel_Scot_Documents",
                label = "Top Level",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_TopLevel",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field027;

        @CCD(
                id = "part_0_Scot_Documents",
                label = "Part 0",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-LET-ENG-00544\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_0",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field028;

        @CCD(
                id = "part_1_Scot_Documents",
                label = "Part 1",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00042\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_1",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field029;

        @CCD(
                id = "part_2_Scot_Documents",
                label = "Part 2",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00043\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_2",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field030;

        @CCD(
                id = "part_3_Scot_Documents",
                label = "Part 3",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00044\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_3",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field031;

        @CCD(
                id = "part_4_Scot_Documents",
                label = "Part 4",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00045\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_4",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field032;

        @CCD(
                id = "part_5_Scot_Documents",
                label = "Part 5",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00046\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_5",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field033;

        @CCD(
                id = "Part_6_Scot_Documents",
                label = "Part 6",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00047\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_6",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field034;

        @CCD(
                id = "Part_7_Scot_Documents",
                label = "Part 7",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00048\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_7",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field035;

        @CCD(
                id = "Part_8_Scot_Documents",
                label = "Part 8",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00049\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_8",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field036;

        @CCD(
                id = "Part_9_Scot_Documents",
                label = "Part 9",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00050\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_9",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field037;

        @CCD(
                id = "Part_10_Scot_Documents",
                label = "Part 10",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00051\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_10",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field038;

        @CCD(
                id = "Part_11_Scot_Documents",
                label = "Part 11",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00052\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_11",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field039;

        @CCD(
                id = "Part_12_Scot_Documents",
                label = "Part 12",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00053\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_12",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field040;

        @CCD(
                id = "Part_13_Scot_Documents",
                label = "Part 13",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00054\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_13",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field041;

        @CCD(
                id = "Part_14_Scot_Documents",
                label = "Part 14",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00055\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_14",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field042;

        @CCD(
                id = "Part_15_Scot_Documents",
                label = "Part 15",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00056\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_15",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field043;

        @CCD(
                id = "Part_16_Scot_Documents",
                label = "Part 16",
                showCondition = "topLevel_Scot_Documents=\"EM-TRB-SCO-ENG-00057\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_Part_16",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field044;
    }

    @ComplexType(name = "ListingItemType", generate = true)
    public static final class ListingItemTypeType {
        @CCD(
                id = "causeListDate",
                label = "Date",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "causeListTime",
                label = "Time",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "causeListVenue",
                label = "Location",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "elmoCaseReference",
                label = "Case Number",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "jurisdictionCodesList",
                label = "Jurisdiction",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "hearingType",
                label = "Case Type",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "positionType",
                label = "Position",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "hearingJudgeName",
                label = "Employment Judge",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "additionalJudge",
                label = "Employment Judge",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "hearingEEMember",
                label = "Employee Member",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "hearingERMember",
                label = "Employer Member",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "hearingClerk",
                label = "Hearing Clerk",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "hearingDay",
                label = "Hearing Day",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "claimantName",
                label = "Claimant",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "claimantTown",
                label = "Claimant Town",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "claimantRepresentative",
                label = "Representative",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "respondent",
                label = "Respondent",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "respondentTown",
                label = "Respondent Town",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "respondentRepresentative",
                label = "Representative",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "estHearingLength",
                label = "Estimated Duration",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field020;

        @CCD(
                id = "Hearing_panel",
                label = "Hearing Panel",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field021;

        @CCD(
                id = "Hearing_room",
                label = "Room",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field022;

        @CCD(
                id = "Hearing_notes",
                label = "Hearing Notes",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field023;

        @CCD(
                id = "resp_others",
                label = "Respondent Others",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field024;

        @CCD(
                id = "judicialMediation",
                label = "Judicial Mediation",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field025;

        @CCD(
                id = "hearingFormat",
                label = "Hearing Format",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field026;

        @CCD(
                id = "hearingReadingDeliberationMembersChambers",
                label = "Reading, Deliberation day, Members Meeting",
                showCondition = "hearingFormat=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "hearingReadingDeliberationMembersChambers",
                label = "Reading, Deliberation day, Members Meeting",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field027;
    }

    @ComplexType(name = "ListingType", generate = true)
    public static final class ListingTypeType {
        @CCD(
                id = "hearingDateType",
                label = "Single or Range",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_HearingDateType",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "listingDate",
                label = "Hearing Date",
                showCondition = "hearingDateType=\"Single\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "listingDateFrom",
                label = "Hearing From",
                showCondition = "hearingDateType=\"Range\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "listingDateTo",
                label = "Hearing To",
                showCondition = "hearingDateType=\"Range\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "listingVenue",
                label = "Hearing Venue",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "listingCollection",
                label = "Daily Cause List",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "ListingItemType",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "listingLabel",
                label = "Daily Cause List for ${listingDate}",
                typeOverride = FieldType.Label,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "hearingDocType",
                label = "Hearing Document",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingDocType",
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "hearingDocETCL",
                label = "Type",
                showCondition = "hearingDocType=\"ETCL - Cause List\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_HearingDocETCL",
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "tribunalCorrespondenceAddress",
                label = "Correspondence Address",
                typeNameOverride = "AddressUK",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "tribunalCorrespondenceTelephone",
                label = "Correspondence Telephone",
                hint = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "tribunalCorrespondenceTelephone",
                label = "Correspondence Telephone",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field011;

        @CCD(
                id = "tribunalCorrespondenceFax",
                label = "Correspondence Fax",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "tribunalCorrespondenceDX",
                label = "Correspondence DX",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "tribunalCorrespondenceEmail",
                label = "Correspondence Email",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "roomOrNoRoom",
                label = "Is there a room?",
                showCondition = "hearingDocETCL=\"Public\" OR hearingDocETCL=\"Staff\"",
                typeOverride = FieldType.YesOrNo,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "managingOffice",
                label = "Managing Office",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "listingVenueScotland",
                label = "Managing Office",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "VenueScotland",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field017;

        @CCD(
                id = "listingVenueOfficeGlas",
                label = "Hearing Venue",
                showCondition = "listingVenueScotland=\"Glasgow\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field018;

        @CCD(
                id = "listingVenueOfficeAber",
                label = "Hearing Venue",
                showCondition = "listingVenueScotland=\"Aberdeen\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field019;

        @CCD(
                id = "venueGlasgow",
                label = "Hearing Venue",
                showCondition = "listingVenueScotland=\"Glasgow\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field020;

        @CCD(
                id = "venueAberdeen",
                label = "Hearing Venue",
                showCondition = "listingVenueScotland=\"Aberdeen\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field021;

        @CCD(
                id = "venueDundee",
                label = "Hearing Venue",
                showCondition = "listingVenueScotland=\"Dundee\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field022;

        @CCD(
                id = "venueEdinburgh",
                label = "Hearing Venue",
                showCondition = "listingVenueScotland=\"Edinburgh\"",
                typeOverride = FieldType.DynamicList,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field023;
    }

    @ComplexType(name = "NewEmploymentDetails", generate = true)
    public static final class NewEmploymentDetailsType {
        @CCD(
                id = "new_job",
                label = "Have you got a new job",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "newly_employed_from",
                label = "Employed from",
                typeOverride = FieldType.Date,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "new_pay_before_tax",
                label = "Pay before tax",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "new_job_pay_interval",
                label = "Is this your weekly, monthly or annual pay",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_pay_cycle",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "NoticeOfChangeAnswers", generate = true)
    public static final class NoticeOfChangeAnswersType {
        @CCD(
                id = "respondentName",
                label = " ",
                typeOverride = FieldType.Text,
                includeSearchable = true,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "claimantFirstName",
                label = " ",
                typeOverride = FieldType.Text,
                includeSearchable = true,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "claimantLastName",
                label = " ",
                typeOverride = FieldType.Text,
                includeSearchable = true,
                includeInProfiles = SingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "OrganisationUsersIdamUser", generate = true)
    public static final class OrganisationUsersIdamUserType {
        @CCD(
                id = "userIdentifier",
                label = "User Id",
                showCondition = "firstName=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "firstName",
                label = "First name",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "lastName",
                label = "Last name",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "email",
                label = "Email address",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "idamStatus",
                label = "Idam Status",
                showCondition = "firstName=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;
    }

    @ComplexType(name = "PiiDocumentUploadDetails", generate = true)
    public static final class PiiDocumentUploadDetailsType {
        @CCD(
                id = "uploadedDocument",
                label = "Document Link",
                typeOverride = FieldType.Document,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "shortDescription",
                label = "Short Description",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "creationDate",
                label = "Date Uploaded",
                typeOverride = FieldType.Date,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "RemovedHearingBundle", generate = true)
    public static final class RemovedHearingBundleType {
        @CCD(
                id = "bundleName",
                label = "Document Name",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "removedDateTime",
                label = "Removed Date",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "removedReason",
                label = "Reason for removal",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "Respondent", generate = true)
    public static final class RespondentType {
        @CCD(
                id = "respondentType",
                label = "Type of respondent",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_respondentType",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "respondentFirstName",
                label = "Respondent First Name",
                showCondition = "respondentType=\"Individual\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "respondentLastName",
                label = "Respondent Last Name",
                showCondition = "respondentType=\"Individual\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "respondentOrganisation",
                label = "Organisation or business name",
                showCondition = "respondentType=\"Organisation\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "respondent_name",
                label = "Name of respondent",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "respondent_hearing_panel_preference",
                label = "Hearing panel preference",
                showCondition = "responseReceived=\"Yes\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_HearingPanelPreference",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "respondent_hearing_panel_preference_reason",
                label = "Hearing panel preference reason",
                showCondition =
                        "respondent_hearing_panel_preference=\"Judge\" OR"
                            + " respondent_hearing_panel_preference=\"Panel\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "responseContinue",
                label = "Is the claim against this Respondent continuing? ",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "extensionRequested",
                label = "Has there been a request for an extension?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "extensionGranted",
                label = "Has the request for extension been granted?",
                showCondition = "extensionRequested=\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "extensionDate",
                label = "Enter the extension date",
                showCondition = "extensionGranted=\"Yes\" AND extensionRequested=\"Yes\"",
                typeOverride = FieldType.Date,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "extensionResubmitted",
                label = "Has the ET3 form been resubmitted?",
                showCondition = "extensionDate=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "responseReceived",
                label = "Has the ET3 form been received?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "responseReceivedDate",
                label = "Response received date",
                showCondition = "responseReceived=\"Yes\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "responseStruckOut",
                label = "Response Struck Out",
                showCondition = "responseReceived=\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "responseRespondentNameQuestion",
                label = "Title",
                showCondition = "responseReceived=\"Yes\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "responseRespondentName",
                label = "Title",
                showCondition = "responseReceived=\"Yes\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "responseReference",
                label = "Reference",
                showCondition = "responseReceived=\"Yes\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "response_status",
                label = "Response",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ResponseStatus",
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "respondentEcc",
                label = "Is there an ECC?",
                showCondition = "responseReceived=\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field020;

        @CCD(
                id = "respondentEccReply",
                label = "Has a reply to the ECC been received?",
                showCondition = "respondentEcc=\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field021;

        @CCD(
                id = "respondentEccReplyCount",
                label = " ",
                showCondition = "respondentEccReply=\"dummy\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field022;

        @CCD(
                id = "responseToClaim",
                label = "Is the claim resisted?",
                showCondition = "response_status=\"Accepted\" AND responseReceived =\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field023;

        @CCD(
                id = "rejection_reason",
                label = "Reason for the rejection",
                showCondition = "response_status=\"Rejected\" AND responseReceived =\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "msl_Response",
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field024;

        @CCD(
                id = "rejection_reason_other",
                label = "Reason for the rejection",
                showCondition = "rejection_reason=\"Other\" AND responseReceived =\"Yes\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field025;

        @CCD(
                id = "responseOutOfTime",
                label = "Response received outside of time allowed?",
                showCondition = "response_status=\"Not accepted\" AND responseReceived =\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field026;

        @CCD(
                id = "responseNotOnPrescribedForm",
                label = "Not on prescribed form?",
                showCondition = "response_status=\"Not accepted\" AND responseReceived =\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field027;

        @CCD(
                id = "responseRequiredInfoAbsent",
                label = "Required information missing?",
                showCondition = "response_status=\"Not accepted\" AND responseReceived =\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field028;

        @CCD(
                id = "responseStruckOutDate",
                label = "Struck Out Date",
                showCondition = "responseStruckOut=\"Yes\"",
                typeOverride = FieldType.Date,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field029;

        @CCD(
                id = "responseStruckOutChairman",
                label = "Judge's only consent",
                showCondition = "responseStruckOut=\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field030;

        @CCD(
                id = "responseStruckOutReason",
                label = "Why struck out",
                showCondition = "responseStruckOut=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_et3Struckout",
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field031;

        @CCD(
                id = "respondent_address",
                label = "Respondent Address",
                showCondition = "responseStruckOut !=\"Yes\" ",
                typeNameOverride = "AddressUK",
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field032;

        @CCD(
                id = "respondent_phone1",
                label = "Phone number",
                showCondition = "responseStruckOut !=\"Yes\" ",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field033;

        @CCD(
                id = "respondent_phone2",
                label = "Alternative number",
                showCondition = "responseStruckOut !=\"Yes\" ",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field034;

        @CCD(
                id = "respondent_email",
                label = "Email address",
                showCondition = "responseStruckOut !=\"Yes\" ",
                typeOverride = FieldType.Email,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field035;

        @CCD(
                id = "respondent_contact_preference",
                label = "Contact preference",
                showCondition = "responseStruckOut !=\"Yes\" ",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ContactPreference",
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field036;

        @CCD(
                id = "responseRespondentAddress",
                label = "Respondent Address (from the ET3 form)",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeNameOverride = "AddressUK",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field037;

        @CCD(
                id = "responseRespondentPhone1",
                label = "Phone number (from the ET3 form)",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field038;

        @CCD(
                id = "responseRespondentPhone2",
                label = "Alternative number (from the ET3 form)",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field039;

        @CCD(
                id = "responseRespondentEmail",
                label = "Email address (from the ET3 form)",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.Email,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field040;

        @CCD(
                id = "responseRespondentContactPreference",
                label = "Contact preference (from the ET3 form)",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ContactPreference",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field041;

        @CCD(
                id = "respondent_ACAS_question",
                label = "Is there an ACAS Certificate number?",
                showCondition = "responseStruckOut !=\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field042;

        @CCD(
                id = "respondent_ACAS",
                label = "ACAS Certificate Number",
                showCondition = "respondent_ACAS_question=\"Yes\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field043;

        @CCD(
                id = "respondent_ACAS_no",
                label = "What are the reasons for not having an ACAS Certificate number?",
                showCondition = "respondent_ACAS_question=\"No\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_ACAS",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field044;

        @CCD(
                id = "response_referred_to_judge",
                label = "Date referred to Judge",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.Date,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field045;

        @CCD(
                id = "response_returned_from_judge",
                label = "Date returned from Judge",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.Date,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field046;

        @CCD(
                id = "responseNotes",
                label = "Notes",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field047;

        @CCD(
                id = "et3VettingCompleted",
                label = "et3VettingCompleted",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field048;

        @CCD(
                id = "et3Vetting",
                label = "ET3 vetting",
                showCondition = "et3VettingCompleted=\"Yes\"",
                typeNameOverride = "ET3Vetting",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field049;

        @CCD(
                id = "et3ResponseIsClaimantNameCorrect",
                label = "Is this the correct claimant for the claim you're responding to?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field050;

        @CCD(
                id = "et3ResponseClaimantNameCorrection",
                label = "What is the correct name of the claimant?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field051;

        @CCD(
                id = "et3ResponseRespondentCompanyNumber",
                label = "Enter the company number if applicable",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field052;

        @CCD(
                id = "et3ResponseRespondentEmployerType",
                label = "What type of employer is the respondent?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_employer_type",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field053;

        @CCD(
                id = "et3ResponseRespondentPreferredTitle",
                label = "If individual, what is their preferred title?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field054;

        @CCD(
                id = "et3ResponseRespondentContactName",
                label = "Name of contact at respondent's address if not you as the representative",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field055;

        @CCD(
                id = "et3ResponseContactReason",
                label = "Provide a reason why you have selected post",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field056;

        @CCD(
                id = "et3ResponseDXAddress",
                label = "DX address (if known)",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field057;

        @CCD(
                id = "et3ResponseHearingRepresentative",
                label = "Which types of hearing can you, as the representative, attend?",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_et3_hearing_type",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field058;

        @CCD(
                id = "et3ResponseHearingRespondent",
                label = "Which types of hearing can the respondent attend?",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_et3_hearing_type",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field059;

        @CCD(
                id = "et3ResponseEmploymentCount",
                label = "How many people does the respondent employ in Great Britain?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.Number,
                searchable = false,
                minValue = "0",
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field060;

        @CCD(
                id = "et3ResponseMultipleSites",
                label = "Does the respondent have more than one site in Great Britain?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field061;

        @CCD(
                id = "et3ResponseSiteEmploymentCount",
                label = "How many people are employed at the place where the claimant worked?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.Number,
                searchable = false,
                minValue = "0",
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field062;

        @CCD(
                id = "et3ResponseAcasAgree",
                label =
                        "Do you agree with the details given by the claimant about early"
                            + " conciliation with Acas?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field063;

        @CCD(
                id = "et3ResponseAcasAgreeReason",
                label = "Why do you disagree with the Acas conciliation details given?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field064;

        @CCD(
                id = "et3ResponseAreDatesCorrect",
                label = "Are the dates of employment given by the claimant correct?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_yes_no_not_applicable",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field065;

        @CCD(
                id = "et3ResponseEmploymentStartDate",
                label = "Enter the employment start date",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.Date,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field066;

        @CCD(
                id = "et3ResponseEmploymentEndDate",
                label = "Enter employment end date",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.Date,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field067;

        @CCD(
                id = "et3ResponseEmploymentInformation",
                label =
                        "Do you want to provide any further information about the claimant's"
                            + " employment dates?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field068;

        @CCD(
                id = "et3ResponseContinuingEmployment",
                label = "Is the claimant's employment with the respondent continuing?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_yes_no_not_applicable",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field069;

        @CCD(
                id = "et3ResponseIsJobTitleCorrect",
                label = "Is the claimant's description of their job or job title correct?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_yes_no_not_applicable",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field070;

        @CCD(
                id = "et3ResponseClaimantWeeklyHours",
                label = "Are the claimant's total weekly work hours correct?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_yes_no_not_applicable",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field071;

        @CCD(
                id = "et3ResponseCorrectJobTitle",
                label = "What is or was the claimant's correct job title?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field072;

        @CCD(
                id = "et3ResponseClaimantCorrectHours",
                label = "What are the claimant's correct total weekly work hours?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field073;

        @CCD(
                id = "et3ResponseEarningDetailsCorrect",
                label = "Are the earnings details given by the claimant correct?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_yes_no_not_applicable",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field074;

        @CCD(
                id = "et3ResponsePayFrequency",
                label = "How often was the claimant paid?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_et3_pay_frequency",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field075;

        @CCD(
                id = "et3ResponsePayBeforeTax",
                label = "Enter the claimant's pay BEFORE tax",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.MoneyGBP,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field076;

        @CCD(
                id = "et3ResponsePayTakehome",
                label = "Enter the claimant's normal take-home pay",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.MoneyGBP,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field077;

        @CCD(
                id = "et3ResponseIsNoticeCorrect",
                label = "Is the information given by the claimant correct about their notice?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_yes_no_not_applicable",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field078;

        @CCD(
                id = "et3ResponseCorrectNoticeDetails",
                label = "What are the claimant's correct notice details?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field079;

        @CCD(
                id = "et3ResponseIsPensionCorrect",
                label = "Are the details about pension and other benefits correct?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_yes_no_not_applicable",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field080;

        @CCD(
                id = "et3ResponsePensionCorrectDetails",
                label = "What are the correct pension and benefit details?",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field081;

        @CCD(
                id = "et3ResponseRespondentContestClaim",
                label = "Does the respondent contest the claim?",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_et3_contest_claim",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field082;

        @CCD(
                id = "et3ResponseContestClaimDocument",
                label = "Upload a document to your response",
                showCondition =
                        "et3ResponseRespondentContestClaim=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field083;

        @CCD(
                id = "et3ResponseContestClaimDetails",
                label = "Use this text box for any accompanying information",
                showCondition =
                        "et3ResponseRespondentContestClaim=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field084;

        @CCD(
                id = "et3ResponseEmployerClaim",
                label = "Does the respondent wish to make an Employer's Contract Claim?",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field085;

        @CCD(
                id = "et3ResponseEmployerClaimDetails",
                label = "Provide the background and details of your Employer's Contract Claim",
                showCondition = "et3ResponseEmployerClaim=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field086;

        @CCD(
                id = "et3ResponseEmployerClaimDocument",
                label = "Add a document",
                showCondition = "et3ResponseEmployerClaim=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.Document,
                typeParameterOverride = "DocumentUpload",
                categoryID = "C19",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field087;

        @CCD(
                id = "et3ResponseRespondentSupportNeeded",
                label =
                        "In the respondent party - are you aware of any physical, mental or"
                            + " learning disability or health conditions which requires support?",
                showCondition = "responseStruckOut !=\"Yes\" AND responseReceived=\"Yes\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_et3_yes_no_not_sure_yet",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field088;

        @CCD(
                id = "et3ResponseRespondentSupportDetails",
                label = "Use this text box or upload the requirements in a document",
                showCondition = "et3ResponseRespondentSupportNeeded=\"Yes\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field089;

        @CCD(
                id = "et3ResponseRespondentSupportDocument",
                label = "Add document",
                showCondition = "et3ResponseRespondentSupportNeeded=\"Yes\"",
                typeOverride = FieldType.Document,
                typeParameterOverride = "DocumentUpload",
                categoryID = "C19",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field090;

        @CCD(
                id = "et3Form",
                label = "ET3 Form",
                showCondition = "responseReceived=\"Yes\"",
                typeOverride = FieldType.Document,
                typeParameterOverride = "DocumentUpload",
                categoryID = "C18",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field091;

        @CCD(
                id = "et3FormWelsh",
                label = "ET3 Form",
                typeOverride = FieldType.Document,
                typeParameterOverride = "DocumentUpload",
                categoryID = "C18",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field092;

        @CCD(
                id = "personalDetailsSection",
                label = "Section complete",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field093;

        @CCD(
                id = "claimDetailsSection",
                label = "Section complete",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field094;

        @CCD(
                id = "employmentDetailsSection",
                label = "Section complete",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field095;

        @CCD(
                id = "idamId",
                label = "Respondent idam id",
                showCondition = "responseReceived=\"Dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "idamId",
                label = "Respondent idam id",
                showCondition = "response_referred_to_judge=\"Dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field096;

        @CCD(
                id = "et3CaseDetailsLinksStatuses",
                label = "ET3 case details links statuses",
                showCondition = "responseReceived=\"Dummy\"",
                typeNameOverride = "et3CaseDetailsLinksStatuses",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field097;

        @CCD(
                id = "et3HubLinksStatuses",
                label = "ET3 hub links statuses",
                showCondition = "responseReceived=\"Dummy\"",
                typeNameOverride = "et3HubLinksStatuses",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field098;

        @CCD(
                id = "responseReceivedCount",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field099;

        @CCD(
                id = "et3ResponseLanguagePreference",
                label = "Language Preference",
                showCondition = "responseReceived=\"Yes\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_languages",
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field100;

        @CCD(
                id = "et3Status",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field101;

        @CCD(
                id = "et3IsRespondentAddressCorrect",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field102;

        @CCD(
                id = "contactDetailsSection",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field103;

        @CCD(
                id = "employerDetailsSection",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field104;

        @CCD(
                id = "conciliationAndEmployeeDetailsSection",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field105;

        @CCD(
                id = "payPensionBenefitDetailsSection",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field106;

        @CCD(
                id = "contestClaimSection",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field107;

        @CCD(
                id = "employersContractClaimSection",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field108;

        @CCD(
                id = "et3NotificationAcceptedDate",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                includeSearchable = true,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field109;

        @CCD(
                id = "representativeRemoved",
                label = " ",
                showCondition = "responseReceived=\"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field110;
    }

    @ComplexType(name = "RespondentRepresentative", generate = true)
    public static final class RespondentRepresentativeType {
        @CCD(
                id = "resp_rep_name",
                label = "Respondent who is being represented",
                showCondition =
                        "representative_occupation=\"dummy\" OR dynamic_resp_rep_name!=\"*\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "respondentId",
                label = "Respondent Id",
                showCondition = "resp_rep_name=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "dynamic_resp_rep_name",
                label = "Respondent who is being represented",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "name_of_representative",
                label = "Name of Representative",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "name_of_organisation",
                label = "Name of Organisation",
                typeOverride = FieldType.Text,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "name_of_organisation",
                label = "Name of Organisation",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "myHmctsYesNo",
                label = "Does the representative have a MyHMCTS account?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "myHmctsYesNo",
                label = "Does the representative have a MyHMCTS account?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field006;

        @CCD(
                id = "respondentOrganisation",
                label = "MyHMCTS Organisation",
                showCondition = "myHmctsYesNo=\"Yes\"",
                typeOverride = FieldType.Organisation,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "representative_reference",
                label = "Reference",
                showCondition = "myHmctsYesNo=\"No\"",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "representative_occupation",
                label = "Occupation",
                showCondition = "myHmctsYesNo=\"No\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_RepresentativeOccupation",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "representative_occupation_other",
                label = "What is the Representative's occupation?",
                showCondition = "representative_occupation=\"Other\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "representative_address",
                label = "Address",
                typeNameOverride = "AddressUK",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "representative_phone_number",
                label = "Phone number",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "representative_mobile_number",
                label = "Alternative number",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "representative_email_address",
                label = "Email address",
                typeOverride = FieldType.Email,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "representativeContactLanguage",
                label = "Representative contact language",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_languages",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "representative_preference_reason",
                label = "Representative contact preference reason",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "organisationUsers",
                label = "Organisation users",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "OrganisationUsersIdamUser",
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "representative_preference",
                label = "Contact preference",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_RepresentativeContact",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "nonMyHmctsOrganisationId",
                label = " ",
                showCondition = "myHmctsYesNo=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleCftlibDefinition.class)
        private Object field019;
    }

    @ComplexType(name = "RestrictedCase", generate = true)
    public static final class RestrictedCaseType {
        @CCD(
                id = "dynamicRequestedBy",
                label = "Requested By",
                typeOverride = FieldType.DynamicList,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "requestedBy",
                label = "Requested By",
                showCondition = "excludedRegister=\"dummy\" OR dynamicRequestedBy!=\"*\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_RestrictedRequestedBy",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "dateCeased",
                label = "Date Ceased",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "imposed",
                label = "Rule 49(3)(d) Applies",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "rule503b",
                label = "Rule 49(3)(b) Applies",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "excludedRegister",
                label = "Excluded from Register",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_RestrictedExcludedRegister",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "startDate",
                label = "Start Date",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "deletedPhyRegister",
                label = "Deleted from Physical Register",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "excludedNames",
                label = "Names not for public release",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field009;
    }

    @ComplexType(name = "ServingDocumentUpload", generate = true)
    public static final class ServingDocumentUploadType {
        @CCD(
                id = "typeOfDocument",
                label = "Type of Document",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ServingDocumentType",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "uploadedDocument",
                label = "Document",
                typeOverride = FieldType.Document,
                categoryID = "C4",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "shortDescription",
                label = "Short Description",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "TaskListCheck", generate = true)
    public static final class TaskListCheckType {
        @CCD(
                id = "personalDetailsCheck",
                label = "Have you completed this section?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "personalDetailsCheck",
                label = "Have you completed this section?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "employmentAndRespondentCheck",
                label = "Have you completed this section?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "employmentAndRespondentCheck",
                label = "Have you completed this section?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "claimDetailsCheck",
                label = "Have you completed this section?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "claimDetailsCheck",
                label = "Have you completed this section?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "TriageQuestions", generate = true)
    public static final class TriageQuestionsType {
        @CCD(
                id = "postcode",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "claimJurisdiction",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "claimantRepresentedQuestion",
                label = " ",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "caseType",
                label = " ",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_CaseType",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "acasMultiple",
                label = " ",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "validNoAcasReason",
                label = " ",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "typesOfClaim",
                label = " ",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_typeOfClaim",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;
    }

    @ComplexType(name = "UnavailabilityDateRange", generate = true)
    public static final class UnavailabilityDateRangeType {
        @CCD(
                id = "unavailableFromDate",
                label = "Unavailable from:",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleCftlibDefinition.class)
        private Object field001;

        @CCD(
                id = "unavailableToDate",
                label = "Unavailable to:",
                typeOverride = FieldType.Date,
                includeInProfiles = SingleCftlibDefinition.class)
        private Object field002;
    }

    @ComplexType(name = "WorkAddressDetails", generate = true)
    public static final class WorkAddressDetailsType {
        @CCD(
                id = "claimant_work_address",
                label = "Claimant Work Address",
                typeNameOverride = "AddressUK",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "claimant_work_phone_number",
                label = "Work phone number",
                typeOverride = FieldType.PhoneUK,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;
    }

    @ComplexType(name = "acceptOrRejectCase", generate = true)
    public static final class AcceptOrRejectCaseType {
        @CCD(
                id = "caseAccepted",
                label = "Case Accepted?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "dateAccepted",
                label = "Date Accepted",
                showCondition = "caseAccepted=\"Yes\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "dateRejected",
                label = "Date Rejected",
                showCondition = "caseAccepted=\"No\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "rejectReason",
                label = "Reason for the rejection",
                showCondition = "caseAccepted=\"No\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_PreAcceptanceResponse",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "addressLabel", generate = true)
    public static final class AddressLabelType {
        @CCD(
                id = "printLabel",
                label = "Print label?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "fullName",
                label = "Full name",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "fullAddress",
                label = "Full address",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "labelEntityName01",
                label = "Entity name 1",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "labelEntityName02",
                label = "Entity name 2",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "labelEntityAddress",
                label = "Address",
                typeNameOverride = "AddressUK",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "labelEntityTelephone",
                label = "Telephone",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "labelEntityFax",
                label = "Fax",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "labelEntityReference",
                label = "Entity Reference",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "labelCaseReference",
                label = "Case Reference",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field010;
    }

    @ComplexType(name = "addressLabelsAttributes", generate = true)
    public static final class AddressLabelsAttributesType {
        @CCD(
                id = "numberOfSelectedLabels",
                label = "Number of selected labels to print in this run",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "numberOfCopies",
                label = "Number of copies of each label",
                typeOverride = FieldType.Number,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "startingLabel",
                label = "Select the label to start printing from",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_AddressLabelNumber",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "showTelFax",
                label = "Show Tel / Fax Numbers?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "addressLabelsSelection", generate = true)
    public static final class AddressLabelsSelectionType {
        @CCD(
                id = "claimantAddressLabel",
                label = "Print claimant address label?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "claimantAddressLabel",
                label = "Print claimant address label?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "claimantRepAddressLabel",
                label = "Print claimant representative address label?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "claimantRepAddressLabel",
                label = "Print claimant representative address label?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "respondentsAddressLabel",
                label = "Print respondents address label?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "respondentsAddressLabel",
                label = "Print respondents address label?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field003;

        @CCD(
                id = "respondentsRepsAddressLabel",
                label = "Print respondents representatives address label?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "respondentsRepsAddressLabel",
                label = "Print respondents representatives address label?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "caseNote", generate = true)
    public static final class CaseNoteType {
        @CCD(
                id = "title",
                label = "Title",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "note",
                label = "Note",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "author",
                label = "Author",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "date",
                label = "Date",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "claimantTse", generate = true)
    public static final class ClaimantTseType {
        @CCD(
                id = "contactApplicationType",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "contactApplicationText",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "contactApplicationFile",
                label = " ",
                typeOverride = FieldType.Document,
                categoryID = "C4",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "copyToOtherPartyYesOrNo",
                label = " ",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "copyToOtherPartyText",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "storedApplicationId",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field006;
    }

    @ComplexType(name = "et3CaseDetailsLinksStatuses", generate = true)
    public static final class Et3CaseDetailsLinksStatusesType {
        @CCD(
                id = "personalDetails",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "et1ClaimForm",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "respondentResponse",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "hearingDetails",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "respondentRequestsAndApplications",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "claimantApplications",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "otherRespondentApplications",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "contactTribunal",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "tribunalOrders",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "tribunalJudgements",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "documents",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field011;
    }

    @ComplexType(name = "et3HubLinksStatuses", generate = true)
    public static final class Et3HubLinksStatusesType {
        @CCD(
                id = "contactDetails",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "employerDetails",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "conciliationAndEmployeeDetails",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "payPensionBenefitDetails",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "contestClaim",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "employersContractClaim",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "checkYorAnswers",
                label = " ",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field007;
    }

    @ComplexType(name = "etICFurtherInfoAnswers", generate = true)
    public static final class EtICFurtherInfoAnswersType {
        @CCD(
                id = "etICFurtherInformationGiveDetails",
                label = "Give details to include in the letter",
                hint = "Give details",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "etICFurtherInformationTimeToComply",
                label = "How much time to comply? (days)",
                hint = "Give details",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field002;
    }

    @ComplexType(name = "etICHearingListedAnswers", generate = true)
    public static final class EtICHearingListedAnswersType {
        @CCD(
                id = "etICHearingListed",
                label = "Hearing already listed",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICHearingAlreadyListed",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "etICExtendDurationGiveDetails",
                label = "Extend duration of hearing",
                hint = "Give details",
                showCondition = "etICHearingListed CONTAINS \"Extend duration of hearing\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICExtendDurationGiveDetails",
                label = "Extend duration of hearing",
                hint = "Give details",
                showCondition = "etICHearingListed CONTAINS \"extendHearingDuration\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "etICOtherGiveDetails",
                label = "Other",
                hint = "Give details",
                showCondition = "etICHearingListed CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICOtherGiveDetails",
                label = "Other",
                hint = "Give details",
                showCondition = "etICHearingListed CONTAINS \"other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field003;

        @CCD(
                id = "etICIsHearingWithJudgeOrMembers",
                label = "Is this hearing judge alone or with members?",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_hearingJudgeAloneOrWithMembers",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "etInitialConsiderationListedHearingType",
                label = "Initial Consideration Listed Hearing Type",
                showCondition = "etICIsHearingWithJudgeOrMembersReason = \"dummy\"",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "etICIsHearingWithJudgeOrMembersReason",
                label = "Reasons",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers != \"\" AND"
                            + " etInitialConsiderationListedHearingType != \"\" AND"
                            + " etInitialConsiderationListedHearingType = \"dummy\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_hearingWithJudgeOrMembersReasons",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICIsHearingWithJudgeOrMembersReason",
                label = "Reasons",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers != \"\" AND"
                            + " etInitialConsiderationListedHearingType != \"\" AND"
                            + " etInitialConsiderationListedHearingType = \"dummy\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_hearingWithJudgeOrMembersReasons",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field006;

        @CCD(
                id = "etICIsFinalHearingWithJudgeOrMembersJsaReason",
                label = "Hearing JSA reasons",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers != \"\" AND"
                            + " etICIsHearingWithJudgeOrMembers = \"JSA\" AND"
                            + " etInitialConsiderationListedHearingType != \"\" AND"
                            + " etInitialConsiderationListedHearingType != \"Preliminary"
                            + " Hearing(CM)\" OR etICHearingListed != \"\" AND"
                            + " etICIsHearingWithJudgeOrMembers = \"JSA\" AND"
                            + " etInitialConsiderationListedHearingType != \"Preliminary"
                            + " Hearing(CM)\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_finalHearingWithJudgeOrMembersReasonsJsa",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "etICIsFinalHearingWithJudgeOrMembersReason",
                label = "Hearing With Members reasons",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers != \"\" AND"
                            + " etICIsHearingWithJudgeOrMembers = \"With members\" AND"
                            + " etInitialConsiderationListedHearingType != \"\" AND"
                            + " etInitialConsiderationListedHearingType != \"Preliminary"
                            + " Hearing(CM)\" OR etICHearingListed != \"\" AND"
                            + " etICIsHearingWithJudgeOrMembers = \"With members\" AND"
                            + " etInitialConsiderationListedHearingType != \"Preliminary"
                            + " Hearing(CM)\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_finalHearingWithJudgeOrMembersReasonsMembers",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "etICIsHearingWithJsa",
                label = "Hearing With Jsa",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers=\"JSA\" AND"
                            + " etInitialConsiderationListedHearingType!=\"\" AND"
                            + " etInitialConsiderationListedHearingType=\"Preliminary"
                            + " Hearing(CM)\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_listedCmPreliminaryHearing_Jsa",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICIsHearingWithJsa",
                label = "Hearing With Jsa",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers=\"JSA\" AND"
                            + " etInitialConsiderationListedHearingType!=\"\" AND"
                            + " etInitialConsiderationListedHearingType=\"Preliminary"
                            + " Hearing(CM)\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_listedCmPreliminaryHearing_Jsa",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field009;

        @CCD(
                id = "etICIsHearingWithJsaReasonOther",
                label = "Jsa Other - Details:",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers != \"\" AND"
                            + " etICIsHearingWithJudgeOrMembers = \"JSA\" AND"
                            + " etInitialConsiderationListedHearingType != \"\" AND"
                            + " etInitialConsiderationListedHearingType != \"Final Hearing\" AND"
                            + " etICIsFinalHearingWithJudgeOrMembersJsaReason CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICIsHearingWithJsaReasonOther",
                label = "Jsa Other - Details:",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers != \"\" AND"
                            + " etICIsHearingWithJudgeOrMembers = \"JSA\" AND"
                            + " etInitialConsiderationListedHearingType != \"\" AND"
                            + " etInitialConsiderationListedHearingType != \"Final Hearing\" AND"
                            + " etICIsFinalHearingWithJudgeOrMembersJsaReason CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field010;

        @CCD(
                id = "etICIsHearingWithJudgeOrMembersReasonOther",
                label = "Members Other - Details:",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers != \"\" AND"
                            + " etICIsHearingWithJudgeOrMembers = \"With members\" AND"
                            + " etInitialConsiderationListedHearingType != \"Final Hearing\" AND"
                            + " etICIsFinalHearingWithJudgeOrMembersReason CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICIsHearingWithJudgeOrMembersReasonOther",
                label = "Members Other - Details:",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers != \"\" AND"
                            + " etICIsHearingWithJudgeOrMembers = \"With members\" AND"
                            + " etInitialConsiderationListedHearingType != \"Final Hearing\" AND"
                            + " etICIsFinalHearingWithJudgeOrMembersReason CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field011;

        @CCD(
                id = "etICJsaFinalHearingReasonOther",
                label = "Jsa Final Other - Details:",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers = \"JSA\" AND"
                            + " etInitialConsiderationListedHearingType = \"Final Hearing\" AND"
                            + " etICIsFinalHearingWithJudgeOrMembersJsaReason CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICJsaFinalHearingReasonOther",
                label = "Jsa Final Other - Details:",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers = \"JSA\" AND"
                            + " etInitialConsiderationListedHearingType != \"\" AND"
                            + " etInitialConsiderationListedHearingType = \"Final Hearing\" AND"
                            + " etICIsFinalHearingWithJudgeOrMembersJsaReason CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field012;

        @CCD(
                id = "etICMembersFinalHearingReasonOther",
                label = "Members Final Other - Details:",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers = \"With members\" AND"
                            + " etInitialConsiderationListedHearingType != \"\" AND"
                            + " etInitialConsiderationListedHearingType = \"Final Hearing\" AND"
                            + " etICIsFinalHearingWithJudgeOrMembersReason CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "etICJsaCmPreliminaryHearingReasonOther",
                label = "Other - Details:",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers = \"JSA\" AND"
                            + " etInitialConsiderationListedHearingType = \"Preliminary"
                            + " Hearing(CM)\" AND etICIsHearingWithJsa = \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "etICIsHearingWithMembers",
                label =
                        "Even though this is a case management hearing in private, members’"
                            + " experience is likely to add significant value to the process. \n"
                            + "Give reasons for this choice",
                showCondition =
                        "etICIsHearingWithJudgeOrMembers = \"With members\" AND"
                            + " etInitialConsiderationListedHearingType != \"\" AND"
                            + " etInitialConsiderationListedHearingType = \"Preliminary"
                            + " Hearing(CM)\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "etICIsHearingWithJudgeOrMembersFurtherDetails",
                label = "Further details",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "otherDirectionsLabel",
                label =
                        "<h3>Any other directions (Optional)</h3><p>Are there any other issues or"
                            + " instructions to consider, or further orders to give?</p><p>This"
                            + " could include:</p><ul><li>Rule"
                            + " 49</li><li>Interpreters</li><li>Adjustments required for"
                            + " hearings</li><li>Further information required</li><li>Employer’s"
                            + " Contract Claim</li><li>Respondent’s identity</li><li>Time limits:"
                            + " claim or response</li></ul>",
                typeOverride = FieldType.Label,
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "etICHearingAnyOtherDirections",
                label = " ",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "etICPostponeGiveDetails",
                label = "Postpone hearing",
                hint = "Give details",
                showCondition = "etICHearingListed CONTAINS \"postponeHearing\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field019;

        @CCD(
                id = "etICConvertPreliminaryGiveDetails",
                label = "Convert final hearing to preliminary hearing",
                hint = "Give details",
                showCondition = "etICHearingListed CONTAINS \"convertFinalToPreliminaryHearing\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field020;

        @CCD(
                id = "etICConvertF2fGiveDetails",
                label = "Convert to F2F hearing",
                hint = "Give details",
                showCondition = "etICHearingListed CONTAINS \"convertToF2FHearing\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field021;
    }

    @ComplexType(name = "etICHearingNotListedListForFinalHearing", generate = true)
    public static final class EtICHearingNotListedListForFinalHearingType {
        @CCD(
                id = "etICTypeOfFinalHearing",
                label = "Type of final hearing",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICTypeOfHearing",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICTypeOfFinalHearing",
                label = "Type of final hearing",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICTypeOfHearing",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "etICLengthOfFinalHearing",
                label = "Length of hearing",
                typeOverride = FieldType.Number,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICLengthOfFinalHearing",
                label = "Length of hearing",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "finalHearingLengthNumType",
                label = "Days, Hours or Minutes",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingLength",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field003;

        @CCD(
                id = "etICFinalHearingLengthNumType",
                label = "Days, Hours or Minutes",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingLength",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field004;

        @CCD(
                id = "etICFinalHearingIsEJSitAlone",
                label = "EJ sit alone?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "etICFinalHearingIsEJSitAloneReason",
                label = "Give reason for requesting EJ sit alone:",
                showCondition = "etICFinalHearingIsEJSitAlone=\"dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_FinalHearingIsEJSitAloneReason",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field006;

        @CCD(
                id = "etICFinalHearingIsEJSitAloneFurtherDetails",
                label = "Further details",
                showCondition = "etICFinalHearingIsEJSitAlone=\"Yes\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field007;
    }

    @ComplexType(name = "etICHearingNotListedListForFinalHearingUpdated", generate = true)
    public static final class EtICHearingNotListedListForFinalHearingUpdatedType {
        @CCD(
                id = "etICTypeOfFinalHearingV2",
                label = "Type of final hearing",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICTypeOfHearing_v2",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICTypeOfFinalHearingV2",
                label = "Type of final hearing",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICTypeOfHearingUpdated",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "etICLengthOfFinalHearingV2",
                label = "Length of hearing",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICLengthOfFinalHearingV2",
                label = "Length of hearing",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "finalHearingLengthNumTypeV2",
                label = "Days, Hours or Minutes",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingLength",
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "etICFinalHearingIsEJSitAlone",
                label = "Listed judge or members",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_finalHearingListedJudgeOrMembers",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICFinalHearingIsEJSitAlone",
                label = "EJ sit alone?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field004;

        @CCD(
                id = "etICFinalHearingIsEJSitAloneReason",
                label = "Give reason for requesting EJ sit alone:",
                hint = "Select all that apply",
                showCondition = "etICFinalHearingIsEJSitAlone = \"dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_FinalHearingIsEJSitAloneReason",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICFinalHearingIsEJSitAloneReason",
                label = "Give reason for requesting EJ sit alone:",
                hint = "Select one that apply",
                showCondition = "etICFinalHearingIsEJSitAlone=\"dummy\"",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_FinalHearingIsEJSitAloneReason",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "etICFinalHearingIsEJSitAloneReasonYes",
                label = "Listed judge or members reason",
                hint = "Select all that apply",
                showCondition = "etICFinalHearingIsEJSitAlone = \"JSA\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "ms_FinalHearingIsEJSitAloneReasonYes",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICFinalHearingIsEJSitAloneReasonYes",
                label = "Give reason for requesting EJ sit alone:",
                hint = "Select all that apply",
                showCondition = "etICFinalHearingIsEJSitAlone=\"Yes\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "ms_FinalHearingIsEJSitAloneReasonYes",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field006;

        @CCD(
                id = "etICNoLFinalHearingIsEJSitAloneReasonsJsaOther",
                label = "EJ Sit Alone Reason - JSA Other",
                showCondition =
                        "etICFinalHearingIsEJSitAlone = \"JSA\" AND"
                            + " etICFinalHearingIsEJSitAloneReasonYes CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICNoLFinalHearingIsEJSitAloneReasonsJsaOther",
                label = "EJ Sit Alone Reason - JSA Other",
                showCondition =
                        "etICFinalHearingIsEJSitAlone = \"JSA\" AND"
                            + " etICFinalHearingIsEJSitAloneReason = \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field007;

        @CCD(
                id = "etICFinalHearingIsEJSitAloneReasonNo",
                label = "Listed judge or members reason",
                hint = "Select all that apply",
                showCondition = "etICFinalHearingIsEJSitAlone = \"With members\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "ms_FinalHearingIsEJSitAloneReasonNo",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICFinalHearingIsEJSitAloneReasonNo",
                label = "Give reason for requesting EJ sit alone:",
                hint = "Select all that apply",
                showCondition = "etICFinalHearingIsEJSitAlone=\"No\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "ms_FinalHearingIsEJSitAloneReasonNo",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field008;

        @CCD(
                id = "etICNoLFinalHearingIsEJSitAloneReasonsMembersOther",
                label = "EJ Sit Alone Reason - JSA Other",
                showCondition =
                        "etICFinalHearingIsEJSitAlone = \"With members\" AND"
                            + " etICFinalHearingIsEJSitAloneReasonNo CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICNoLFinalHearingIsEJSitAloneReasonsMembersOther",
                label = "EJ Sit Alone Reason - JSA Other",
                showCondition =
                        "etICFinalHearingIsEJSitAlone = \"With members\" AND"
                            + " etICFinalHearingIsEJSitAloneReason = \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field009;

        @CCD(
                id = "etICFinalHearingIsEJSitAloneFurtherDetails",
                label = "Further details",
                showCondition = "etICFinalHearingIsEJSitAlone != \"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICFinalHearingIsEJSitAloneFurtherDetails",
                label = "Further details",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field010;

        @CCD(
                id = "etICTypeOfVideoHearingOrder",
                label = "Issue standard video orders when listed",
                showCondition = "etICTypeOfFinalHearingV2 CONTAINS \"Video\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_IcVideoOrders",
                searchableValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field011;

        @CCD(
                id = "etICTypeOfF2fHearingOrder",
                label = "Issue standard orders when listed",
                showCondition = "etICTypeOfFinalHearingV2 CONTAINS \"F2F\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_IcF2FOrders",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field012;

        @CCD(
                id = "etICHearingOrderBUCompliance",
                label = "BU to check compliance with orders?",
                showCondition =
                        "etICTypeOfVideoHearingOrder != \"\" OR etICTypeOfF2fHearingOrder != \"\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field013;

        @CCD(
                id = "etICFinalHearingPanelComposition",
                label = "<hr><h3>Panel Composition</h3>",
                typeOverride = FieldType.Label,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field014;

        @CCD(
                id = "etICFinalHearingIsEJSitAloneReasonYesOther",
                label = "Other Reason (EJ Sit Alone: Yes)",
                showCondition =
                        "etICFinalHearingIsEJSitAlone=\"Yes\" AND"
                            + " etICFinalHearingIsEJSitAloneReasonYes CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field015;

        @CCD(
                id = "etICFinalHearingIsEJSitAloneReasonNoOther",
                label = "Other Reason (EJ Sit Alone: No)",
                showCondition =
                        "etICFinalHearingIsEJSitAlone=\"No\" AND"
                            + " etICFinalHearingIsEJSitAloneReasonNo CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field016;

        @CCD(
                id = "etICFinalHearingPanelCompositionBottomDivider",
                label = "<hr>",
                typeOverride = FieldType.Label,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field017;
    }

    @ComplexType(name = "etICHearingNotListedListForPrelimHearing", generate = true)
    public static final class EtICHearingNotListedListForPrelimHearingType {
        @CCD(
                id = "etICTypeOfPreliminaryHearing",
                label = "Type of preliminary hearing",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICTypeOfHearing",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "etICPurposeOfPreliminaryHearing",
                label = "Purpose of preliminary hearing",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICPurposeOfPrelimHearing",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "etICGiveDetailsOfHearingNotice",
                label = "Give details of hearing notice",
                showCondition = "etICPurposeOfPreliminaryHearing != \"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "etICLengthOfPrelimHearing",
                label = "Length of hearing",
                showCondition = "etICPurposeOfPreliminaryHearing != \"\"",
                typeOverride = FieldType.Number,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICLengthOfPrelimHearing",
                label = "Length of hearing",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field004;

        @CCD(
                id = "prelimHearingLengthNumType",
                label = "Days, Hours or Minutes",
                showCondition = "etICPurposeOfPreliminaryHearing != \"\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingLength",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field005;
    }

    @ComplexType(name = "etICHearingNotListedListForPrelimHearingUpdated", generate = true)
    public static final class EtICHearingNotListedListForPrelimHearingUpdatedType {
        @CCD(
                id = "etICTypeOfPreliminaryHearingV2",
                label = "Type of preliminary hearing",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICTypeOfHearing_v2",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICTypeOfPreliminaryHearingV2",
                label = "Type of preliminary hearing",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICTypeOfHearingUpdated",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "etICPurposeOfPreliminaryHearingV2",
                label = "Purpose of preliminary hearing",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICPurposeOfPrelimHearing_v2",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICPurposeOfPreliminaryHearingV2",
                label = "Purpose of preliminary hearing",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICPurposeOfPrelimHearing",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "etICGiveDetailsOfHearingNoticeV2",
                label = "Give details of hearing notice",
                showCondition = "etICPurposeOfPreliminaryHearingV2 != \"\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "etICLengthOfPrelimHearingV2",
                label = "Length of hearing",
                typeOverride = FieldType.Number,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICLengthOfPrelimHearingV2",
                label = "Length of hearing",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field004;

        @CCD(
                id = "prelimHearingLengthNumTypeV2",
                label = "Days, Hours or Minutes",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingLength",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "prelimHearingLengthNumTypeV2",
                label = "Days, Hours or Minutes",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_HearingLength",
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "etICIsPreliminaryHearingWithMembersV2",
                label = "Do you consider this preliminary hearing should be listed with members?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "etICIsPreliminaryHearingWithMembersReasonV2",
                label = "Give reasons for requiring members",
                showCondition = "etICIsPreliminaryHearingWithMembersV2=\"Yes\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field007;
    }

    @ComplexType(name = "etICHearingNotListedSeekComments", generate = true)
    public static final class EtICHearingNotListedSeekCommentsType {
        @CCD(
                id = "etICTypeOfCvpHearing",
                label = "Type of Video hearing",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICTypeOfCvpHearing",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICTypeOfCvpHearing",
                label = "Type of CVP hearing",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICTypeOfCvpHearing",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "etICFinalHearingDetails",
                label = "Give details of final hearing",
                showCondition = "etICTypeOfCvpHearing CONTAINS \"Final\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "etICPrelimHearingDetails",
                label = "Give details of preliminary hearing",
                showCondition = "etICTypeOfCvpHearing CONTAINS \"Preliminary\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "etICPrelimHearingYesNo",
                label = "Should the case be listed for a private preliminary hearing?",
                showCondition = "etICTypeOfCvpHearing CONTAINS \"Preliminary\"",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "etICHearingNotListedUDLHearing", generate = true)
    public static final class EtICHearingNotListedUDLHearingType {
        @CCD(
                id = "etICEJSitAlone",
                label = "EJ sit alone?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICEJSitAlone",
                label = "EJ sit alone?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "etICUDLGiveReasons",
                label = "Give reasons",
                hint = "Select all that apply",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICUDLGiveReasons",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "etICUDLDisputeOnFacts",
                label = "Likelihood of dispute on facts makes full tribunal desirable",
                showCondition =
                        "etICUDLGiveReasons CONTAINS \"Likelihood of dispute on facts makes full"
                            + " tribunal desirable\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "etICUDLLittleOrNoAgreement",
                label = "Little or no agreement on facts",
                showCondition = "etICUDLGiveReasons CONTAINS \"Little or no agreement on facts\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "etICUDLIssueOfLawArising",
                label = "Likelihood of issue of law arising makes EJSA desirable",
                showCondition =
                        "etICUDLGiveReasons CONTAINS \"Likelihood of issue of law arising makes"
                            + " EJSA desirable\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "etICUDLViewsOfParties",
                label = "Views of parties",
                showCondition = "etICUDLGiveReasons CONTAINS \"Views of parties\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "etICUDLNoViewsExpressedByParties",
                label = "No views expressed by parties",
                showCondition = "etICUDLGiveReasons CONTAINS \"No views expressed by parties\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "etICUDLConcurrentProceedings",
                label = "Concurrent proceedings",
                showCondition = "etICUDLGiveReasons CONTAINS \"Concurrent proceedings\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "etICUDLOther",
                label = "Other",
                showCondition = "etICUDLGiveReasons CONTAINS \"Other\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "etICUDLHearFormat",
                label = "Hearing format",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_etICUDLHearingFormat",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "etICUDLCVPIssue",
                label = "Issue standard Video orders when listed",
                showCondition = "etICUDLHearFormat = \"Video hearing\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICUDLCVPIssue",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICUDLCVPIssue",
                label = "Issue standard CVP orders when listed",
                showCondition = "etICUDLHearFormat = \"CVP hearing\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICUDLCVPIssue",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field011;

        @CCD(
                id = "etICUDLFinalF2FIssue",
                label = "Issue standard orders when listed",
                showCondition = "etICUDLHearFormat = \"Final F2F hearings (not Aberdeen)\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICUDLFinalF2FIssue",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "etICBUCheckComplianceOrders",
                label = "BU to check compliance with orders?",
                typeOverride = FieldType.YesOrNo,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field013;
    }

    @ComplexType(name = "etInitialConsiderationRule27", generate = true)
    public static final class EtInitialConsiderationRule27Type {
        @CCD(
                id = "etICRule27ClaimToBe",
                label = "Claim to be",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_rule2728ClaimToBe",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICRule27ClaimToBe",
                label = "Claim to be",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_etICRule27ClaimToBe",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "etICRule27WhichPart",
                label = "Which part?",
                showCondition = "etICRule27ClaimToBe=\"Dismissed in part\"",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICRule27WhichPart",
                label = "Which part?",
                showCondition = "etICRule27ClaimToBe=\"Dismissed in part\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "etICRule27Direction",
                label = "Employment Judge's direction to Rule 28 Notice",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_rule27direction",
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICRule27Direction",
                label = "Employment Judge's direction for Rule 28 Notice",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "msl_etICRule27Direction",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field003;

        @CCD(
                id = "etICRule27DirectionReason",
                label = "Set out reason",
                showCondition =
                        "etICRule27Direction CONTAINS \"No reasonable prospect of success\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICRule27DirectionReason",
                label = "No reasonable prospect of success - Set out reason",
                showCondition = "etICRule27Direction CONTAINS \"noReasonableProspectOfSuccess\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field004;

        @CCD(
                id = "etICRule27NumberOfDays",
                label = "Number of days for claimant to provide written representations",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICRule27NumberOfDays",
                label = "Number of days for claimant to provide written representations",
                typeOverride = FieldType.Number,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "etICRule27NoJurisdictionReason",
                label = "No jurisdiction - Set out reason",
                showCondition = "etICRule27Direction CONTAINS \"noJurisdiction\"",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field006;
    }

    @ComplexType(name = "etInitialConsiderationRule28", generate = true)
    public static final class EtInitialConsiderationRule28Type {
        @CCD(
                id = "etICRule28ClaimToBe",
                label = "Response to be",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "fl_rule2728ClaimToBe",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICRule28ClaimToBe",
                label = "Response to be",
                typeOverride = FieldType.FixedRadioList,
                typeParameterOverride = "frl_etICRule28ClaimToBe",
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "etICRule28WhichPart",
                label = "Which part?",
                showCondition = "etICRule28ClaimToBe=\"Dismissed in part\"",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICRule28WhichPart",
                label = "Which part?",
                showCondition = "etICRule28ClaimToBe=\"Dismissed in part\"",
                typeOverride = FieldType.Text,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "etICRule28DirectionReason",
                label = "Employment Judge’s reasons",
                hint = "No reasonable prospect of success because",
                typeOverride = FieldType.TextArea,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICRule28DirectionReason",
                label = "Employment Judge's reasons",
                typeOverride = FieldType.TextArea,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field003;

        @CCD(
                id = "etICRule28NumberOfDays",
                label = "Number of days for respondent to provide written representations",
                typeOverride = FieldType.Number,
                searchable = false,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "etICRule28NumberOfDays",
                label = "Number of days for respondent to provide written representations",
                typeOverride = FieldType.Number,
                searchable = false,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field004;
    }

    @ComplexType(name = "genericTseDetails", generate = true)
    public static final class GenericTseDetailsType {
        @CCD(
                id = "number",
                label = "No",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "type",
                label = "Type",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "applicant",
                label = "Applicant",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "applicantIdamId",
                label = "Applicant id",
                showCondition = "applicant=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "date",
                label = "Application date",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "details",
                label = "What do you want to tell or ask the tribunal?",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "documentUpload",
                label = "Supporting material",
                typeOverride = FieldType.Document,
                categoryID = "C4",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "documentUpload",
                label = "Supporting material",
                typeOverride = FieldType.Document,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field007;

        @CCD(
                id = "copyToOtherPartyYesOrNo",
                label =
                        "Do you want to copy this correspondence to the other party to satisfy the"
                            + " Rules of Procedure?",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "copyToOtherPartyText",
                label = "Details of why you do not want to inform the other party",
                showCondition = "copyToOtherPartyYesOrNo=\"No\"",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "respondCollection",
                label = "Responses",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "tseReply",
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "adminDecision",
                label = "Record a decision",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "tseAdminDecision",
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "dueDate",
                label = "Response due",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "responsesCount",
                label = "Number of responses",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "status",
                label = "Status",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "applicationState",
                label = "Application State",
                showCondition = "status=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "respondentState",
                label = "Respondent application State",
                showCondition = "status=\"dummy\"",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "tseStatus",
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "closeApplicationNotes",
                label = "General notes",
                typeOverride = FieldType.Text,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field017;

        @CCD(
                id = "respondentResponseRequired",
                label =
                        "If there are tribunal requests/orders that required a response from"
                            + " Respondent",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "claimantResponseRequired",
                label =
                        "If there are tribunal requests/orders that required a response from"
                            + " Claimant",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "respondStoredCollection",
                label = "Responses",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "tseReply",
                includeInProfiles = SingleDefinition.class)
        private Object field020;

        @CCD(
                id = "respondentReply",
                label = "Response",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "tseRespondentResponse",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field021;

        @CCD(
                id = "closeApplicationNote",
                label = "General notes",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field022;
    }

    @ComplexType(name = "hubLinksStatuses", generate = true)
    public static final class HubLinksStatusesType {
        @CCD(
                id = "personalDetails",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "et1ClaimForm",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "respondentResponse",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "hearingDetails",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "requestsAndApplications",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "respondentApplications",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "contactTribunal",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "tribunalOrders",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "tribunalJudgements",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "documents",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field010;
    }

    @ComplexType(name = "icDocumentUpload", generate = true)
    public static final class IcDocumentUploadType {
        @CCD(
                id = "uploadedDocument",
                label = "Document",
                typeOverride = FieldType.Document,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "shortDescription",
                label = "Short Description",
                typeOverride = FieldType.Text,
                searchable = false,
                includeInProfiles = SingleDefinition.class)
        private Object field002;
    }

    @ComplexType(name = "judgmentReconsideration", generate = true)
    public static final class JudgmentReconsiderationType {
        @CCD(
                id = "reconsideration",
                label = "Has a reconsideration application been made?",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "reconsiderationDate",
                label = "Date of application for reconsideration",
                showCondition = "reconsideration=\"Yes\"",
                typeOverride = FieldType.Date,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "reconsiderationOwnInitiative",
                label = "Did the Tribunal reconsider the Judgment on its own initiative",
                showCondition = "reconsideration=\"Yes\"",
                typeOverride = FieldType.YesOrNo,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "reconsiderationPartyInitiative",
                label = "Who applied for reconsideration?",
                showCondition = "reconsiderationOwnInitiative=\"No\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_ClaimantOrRespondent",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "reconsiderationDirection",
                label = "Employment Judge's direction",
                showCondition = "reconsideration=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_JudgeDirection",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "reconsiderationDecision",
                label = "Employment Judge's decision",
                showCondition = "reconsideration=\"Yes\"",
                typeOverride = FieldType.FixedList,
                typeParameterOverride = "fl_JudgeDecision",
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field006;
    }

    @ComplexType(name = "nextHearingDetails", generate = true)
    public static final class NextHearingDetailsType {
        @CCD(
                id = "hearingID",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "hearingDateTime",
                label = " ",
                typeOverride = FieldType.DateTime,
                includeInProfiles = SingleDefinition.class)
        private Object field002;
    }

    @ComplexType(name = "pseRespondCollection", generate = true)
    public static final class PseRespondCollectionType {
        @CCD(
                id = "date",
                label = "Response date",
                displayOrder = 2,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "from",
                label = "Response from",
                displayOrder = 1,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "response",
                label = "What's your response to the tribunal?",
                displayOrder = 3,
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "fromIdamId",
                label = "Response from Idam id",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "hasSupportingMaterial",
                label = "Has supporting material",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "supportingMaterial",
                label = "Supporting material",
                displayOrder = 4,
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "copyToOtherParty",
                label =
                        "Do you want to copy this correspondence to the other party to satisfy the"
                            + " Rules of Procedure?",
                showCondition = " ",
                displayOrder = 5,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "copyNoGiveDetails",
                label = " ",
                showCondition = "copyToOtherParty=\"dummy\"",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "responseState",
                label = " ",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "author",
                label = " ",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "dateTime",
                label = " ",
                showCondition = "copyToOtherParty = \"dummy\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "Yes",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "dateTime",
                label = " ",
                showCondition = "copyToOtherParty=\"dummy\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "Yes",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field011;

        @CCD(
                id = "isECC",
                label = " ",
                showCondition = "copyToOtherParty=\"dummy\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "Yes",
                includeInProfiles = SingleDefinition.class)
        private Object field012;
    }

    @ComplexType(name = "pseStatus", generate = true)
    public static final class PseStatusType {
        @CCD(
                id = "userIdamId",
                label = "User Idam Id",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "notificationState",
                label = "Notification state",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "dateTime",
                label = "Updated date time",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;
    }

    @ComplexType(name = "referralDetails", generate = true)
    public static final class ReferralDetailsType {
        @CCD(
                id = "referralNumber",
                label = "No",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "referralHearingDate",
                label = "Hearing date",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "referCaseTo",
                label = "Referred to",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "referentEmail",
                label = "Email address",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "isUrgent",
                label = "Urgent",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "referralSubject",
                label = "Subject",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "referralSubjectSpecify",
                label = "Referral subject",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "referralDetails",
                label = "Details of the referral",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "referralDocument",
                label = "Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "referralInstruction",
                label = "Recommended instructions",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "referredBy",
                label = "Referred by",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "referralDate",
                label = "Referral date",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "referralStatus",
                label = "Status",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "closeReferralGeneralNotes",
                label = "General notes",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "updateReferralCollection",
                label = "Updates",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "updateReferralDetails",
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "referralReplyCollection",
                label = "Reply",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "referralReply",
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "referralSummaryPdf",
                label = "Referral Document",
                typeOverride = FieldType.Document,
                categoryID = "C4",
                includeInProfiles = SingleDefinition.class)
        private Object field017;
    }

    @ComplexType(name = "referralReply", generate = true)
    public static final class ReferralReplyType {
        @CCD(
                id = "replyBy",
                label = "Reply by",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "directionTo",
                label = "Reply to",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "replyToEmailAddress",
                label = "Email address",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "replyDate",
                label = "Date",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "isUrgentReply",
                label = "Urgent",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "directionDetails",
                label = "Directions",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "replyDocument",
                label = "Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "replyGeneralNotes",
                label = "General notes",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "replyDateTime",
                label = " ",
                showCondition = "replyDateTime = \"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "referralSubject",
                label = " ",
                showCondition = "replyDateTime = \"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "referralNumber",
                label = " ",
                showCondition = "replyDateTime = \"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field011;
    }

    @ComplexType(name = "respondNotificationTypeCollection", generate = true)
    public static final class RespondNotificationTypeCollectionType {
        @CCD(
                id = "respondNotificationCmoOrRequest",
                label = "Is this a case management order or request?",
                displayOrder = 5,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "respondNotificationDate",
                label = "Date",
                displayOrder = 2,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "respondNotificationRequestMadeBy",
                label = "Request made by",
                displayOrder = 7,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "state",
                label = " ",
                showCondition = "respondNotificationDate=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "isClaimantResponseDue",
                label = " ",
                showCondition = "state=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "dateTime",
                label = "Updated date time",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "respondNotificationTitle",
                label = "Response title",
                displayOrder = 1,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "respondNotificationAdditionalInfo",
                label = "Additional information",
                displayOrder = 4,
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "respondNotificationUploadDocument",
                label = "Documents",
                displayOrder = 3,
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "respondNotificationResponseRequired",
                label = "Response due",
                displayOrder = 8,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "respondNotificationWhoRespond",
                label = "Party or parties to respoond",
                displayOrder = 9,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "respondNotificationCaseManagementMadeBy",
                label = "Case management order made by",
                displayOrder = 6,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "respondNotificationFullName",
                label = "Full name",
                displayOrder = 7,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "respondNotificationPartyToNotify",
                label = "Sent to",
                displayOrder = 10,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field014;
    }

    @ComplexType(name = "respondentTse", generate = true)
    public static final class RespondentTseType {
        @CCD(
                id = "contactApplicationType",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "contactApplicationText",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "contactApplicationFile",
                label = " ",
                typeOverride = FieldType.Document,
                categoryID = "C4",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "copyToOtherPartyYesOrNo",
                label = " ",
                typeOverride = FieldType.YesOrNo,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "copyToOtherPartyText",
                label = " ",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "respondentIdamId",
                label = "Respondent id",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "storedApplicationId",
                label = "Stored Application ID",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field007;
    }

    @ComplexType(name = "sendNotificationCollection", generate = true)
    public static final class SendNotificationCollectionType {
        @CCD(
                id = "sendNotificationTitle",
                label = "Notification",
                displayOrder = 1,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "sendNotificationLetter",
                label = " ",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "sendNotificationUploadDocument",
                label = "Documents",
                displayOrder = 7,
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "sendNotificationSubject",
                label = " ",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.MultiSelectList,
                typeParameterOverride = "fl_sendNotificationSubject",
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "sendNotificationAdditionalInfo",
                label = "Additional information",
                displayOrder = 6,
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "sendNotificationNotify",
                label = "To party",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "sendNotificationAnotherLetter",
                label = "sendNotificationAnotherLetter",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "sendNotificationSelectHearing",
                label = "Hearing",
                displayOrder = 2,
                typeOverride = FieldType.DynamicList,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "sendNotificationCaseManagement",
                label = "Case management order or request",
                displayOrder = 4,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "sendNotificationResponseTribunal",
                label = "Response due",
                displayOrder = 4,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "sendNotificationWhoCaseOrder",
                label = "Case management order made by",
                displayOrder = 8,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "sendNotificationSelectParties",
                label = "Party or parties to respond",
                displayOrder = 5,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "sendNotificationFullName",
                label = "Full name",
                displayOrder = 9,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "sendNotificationFullName2",
                label = "Full name",
                displayOrder = 11,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "sendNotificationDecision",
                label = "Decision",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "sendNotificationDetails",
                label = "Details",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "number",
                label = "No",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "date",
                label = "Date sent",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "respondCollection",
                label = "Responses",
                displayOrder = 12,
                typeOverride = FieldType.Collection,
                typeParameterOverride = "pseRespondCollection",
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "notificationState",
                label = "Notification State",
                showCondition = "number=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field020;

        @CCD(
                id = "respondentState",
                label = "Respondent notification State",
                showCondition = "number=\"dummy\"",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "pseStatus",
                includeInProfiles = SingleDefinition.class)
        private Object field021;

        @CCD(
                id = "sendNotificationResponsesCount",
                label = "Number of responses",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field022;

        @CCD(
                id = "sendNotificationSubjectString",
                label = "Subject",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field023;

        @CCD(
                id = "sendNotificationSentBy",
                label = "Sent by",
                displayOrder = 3,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field024;

        @CCD(
                id = "sendNotificationEccQuestion",
                label = " ",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field025;

        @CCD(
                id = "sendNotificationWhoMadeJudgement",
                label = "Judgment made by",
                displayOrder = 10,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field026;

        @CCD(
                id = "sendNotificationResponseTribunalTable",
                label = "Response due",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field027;

        @CCD(
                id = "sendNotificationRequestMadeBy",
                label = "Request made by",
                displayOrder = 8,
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field028;

        @CCD(
                id = "respondNotificationTypeCollection",
                label = "Tribunal Responses",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "respondNotificationTypeCollection",
                includeInProfiles = SingleDefinition.class)
        private Object field029;

        @CCD(
                id = "sendNotificationNotifyLeadCase",
                label = "To",
                showCondition = "sendNotificationNotify=\"Lead case\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleCftlibDefinition.class)
        private Object field030;

        @CCD(
                id = "sendNotificationNotifyAll",
                label = "To",
                showCondition = "sendNotificationNotify=\"Lead and sub cases\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleCftlibDefinition.class)
        private Object field031;

        @CCD(
                id = "sendNotificationNotifySelected",
                label = "To",
                showCondition = "sendNotificationNotify=\"Selected cases\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleCftlibDefinition.class)
        private Object field032;

        @CCD(
                id = "notificationSentFrom",
                label = "Multiple ref",
                showCondition = "sendNotificationNotify=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleCftlibDefinition.class)
        private Object field033;

        @CCD(
                id = "respondStoredCollection",
                label = "Responses",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "pseRespondCollection",
                includeInProfiles = SingleDefinition.class)
        private Object field034;

        @CCD(
                id = "respondentRespondStoredCollection",
                label = "Responses",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "pseRespondCollection",
                includeInProfiles = SingleDefinition.class)
        private Object field035;
    }

    @ComplexType(name = "tseAdminDecision", generate = true)
    public static final class TseAdminDecisionType {
        @CCD(
                id = "date",
                label = "Decision date",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "enterNotificationTitle",
                label = "Enter notification title",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "decision",
                label = "Decision",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "decisionDetails",
                label = "Decision details",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "typeOfDecision",
                label = "Type of decision",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "isResponseRequired",
                label = "Is a response to the tribunal required?",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "selectPartyRespond",
                label = "Select the party or parties who must respond",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "additionalInformation",
                label = "Additional information",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "responseRequiredDoc",
                label = "Add document",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "decisionMadeBy",
                label = "Decision was made by",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "decisionMadeByFullName",
                label = "Enter their full name",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "selectPartyNotify",
                label = "Select the party or parties to notify",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "decisionState",
                label = "Decision State",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        private Object field013;
    }

    @ComplexType(name = "tseReply", generate = true)
    public static final class TseReplyType {
        @CCD(
                id = "from",
                label = "Response from",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "date",
                label = "Response date",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "fromIdamId",
                label = "Response from Idam id",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "response",
                label = "What's your response to the claimant's application",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "hasSupportingMaterial",
                label = " ",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "No",
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "supportingMaterial",
                label = "Supporting material",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "copyToOtherParty",
                label =
                        "Do you want to copy this correspondence to the other party to satisfy the"
                            + " Rules of Procedure?",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "copyNoGiveDetails",
                label = "Give details",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "summaryPdf",
                label = " ",
                showCondition = "from=\"dummy\"",
                typeOverride = FieldType.Document,
                categoryID = "C4",
                retainHiddenValueValue = "No",
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "summaryPdf",
                label = " ",
                showCondition = "from=\"dummy\"",
                typeOverride = FieldType.Document,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field009;

        @CCD(
                id = "enterResponseTitle",
                label = "Enter response title",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "additionalInformation",
                label = "Additional information",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "addDocument",
                label = "Add document",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "isCmoOrRequest",
                label = "Is this a case management order or request?",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field013;

        @CCD(
                id = "cmoMadeBy",
                label = "Case management order made by",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field014;

        @CCD(
                id = "requestMadeBy",
                label = "Request made by",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field015;

        @CCD(
                id = "madeByFullName",
                label = "Enter their full name",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field016;

        @CCD(
                id = "isResponseRequired",
                label = "Is a response to the tribunal required?",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field017;

        @CCD(
                id = "selectPartyRespond",
                label = "Select the party or parties who must respond",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field018;

        @CCD(
                id = "selectPartyNotify",
                label = "Select the party or parties to notify",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field019;

        @CCD(
                id = "viewedByClaimant",
                label = "Viewed by the claimant",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field020;

        @CCD(
                id = "dateTime",
                label = " ",
                showCondition = "dateTime = \"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "dateTime",
                label = " ",
                showCondition = "date = \"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field021;

        @CCD(
                id = "applicationType",
                label = " ",
                showCondition = "applicationType = \"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = EnglandWalesSingleDefinition.class)
        @CCD(
                id = "applicationType",
                label = " ",
                showCondition = "date = \"dummy\"",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field022;
    }

    @ComplexType(name = "tseRespondentResponse", generate = true)
    public static final class TseRespondentResponseType {
        @CCD(
                id = "from",
                label = "Response from",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field001;

        @CCD(
                id = "date",
                label = "Response date",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field002;

        @CCD(
                id = "response",
                label = "What's your response to the claimant's application",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field003;

        @CCD(
                id = "hasSupportingMaterial",
                label = " ",
                showCondition = "date=\"dummy\"",
                typeOverride = FieldType.Text,
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field004;

        @CCD(
                id = "supportingMaterial",
                label = "Supporting material",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field005;

        @CCD(
                id = "copyToOtherParty",
                label =
                        "Do you want to copy this correspondence to the other party to satisfy the"
                            + " Rules of Procedure?",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field006;

        @CCD(
                id = "copyNoGiveDetails",
                label = "Give details",
                typeOverride = FieldType.Text,
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field007;

        @CCD(
                id = "summaryPdf",
                label = " ",
                showCondition = "from=\"dummy\"",
                typeOverride = FieldType.Document,
                categoryID = "C4",
                retainHiddenValueValue = "No",
                includeInProfiles = ScotlandSingleDefinition.class)
        private Object field008;
    }

    @ComplexType(name = "tseStatus", generate = true)
    public static final class TseStatusType {
        @CCD(
                id = "userIdamId",
                label = "User Idam Id",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "applicationState",
                label = "Application state",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;
    }

    @ComplexType(name = "updateReferralDetails", generate = true)
    public static final class UpdateReferralDetailsType {
        @CCD(
                id = "updateReferralNumber",
                label = "No",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field001;

        @CCD(
                id = "updateReferralHearingDate",
                label = "Hearing date",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field002;

        @CCD(
                id = "updateReferCaseTo",
                label = "Referred to",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field003;

        @CCD(
                id = "updateReferentEmail",
                label = "Email address",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field004;

        @CCD(
                id = "updateReferralDetails",
                label = "Details of the referral",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field005;

        @CCD(
                id = "updateIsUrgent",
                label = "Urgent",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field006;

        @CCD(
                id = "updateReferralSubject",
                label = "Subject",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field007;

        @CCD(
                id = "updateReferralSubjectSpecify",
                label = "Referral subject",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field008;

        @CCD(
                id = "updateReferralDocument",
                label = "Documents",
                typeOverride = FieldType.Collection,
                typeParameterOverride = "DocumentUpload",
                includeInProfiles = SingleDefinition.class)
        private Object field009;

        @CCD(
                id = "updateReferralInstruction",
                label = "Recommended instructions",
                typeOverride = FieldType.TextArea,
                includeInProfiles = SingleDefinition.class)
        private Object field010;

        @CCD(
                id = "updateReferredBy",
                label = "Updated by",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field011;

        @CCD(
                id = "updateReferralDate",
                label = "Updated date",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field012;

        @CCD(
                id = "updateReferralDateTime",
                label = "Update Referral DateTime",
                typeOverride = FieldType.Text,
                includeInProfiles = SingleDefinition.class)
        private Object field013;
    }
}
