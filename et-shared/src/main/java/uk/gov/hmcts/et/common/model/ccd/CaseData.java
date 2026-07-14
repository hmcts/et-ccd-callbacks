package uk.gov.hmcts.et.common.model.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Tolerate;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.items.AddressLabelTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.BFActionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DepositTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DynamicListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.EccCounterClaimTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingDetailTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JudgementTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RemovedHearingBundleItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.VettingJurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AddressLabelsAttributesType;
import uk.gov.hmcts.et.common.model.ccd.types.AddressLabelsSelectionType;
import uk.gov.hmcts.et.common.model.ccd.types.CaseFlagsType;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLink;
import uk.gov.hmcts.et.common.model.ccd.types.CaseNote;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.CompanyPremisesType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.et.common.model.ccd.types.CreateRespondentType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.DraftAndSignJudgement;
import uk.gov.hmcts.et.common.model.ccd.types.HearingBundleType;
import uk.gov.hmcts.et.common.model.ccd.types.NextHearingDetails;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentTse;
import uk.gov.hmcts.et.common.model.ccd.types.RestrictedReportingType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.UploadHearingDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse;
import uk.gov.hmcts.et.common.model.hmc.CaseCategory;
import uk.gov.hmcts.et.common.model.hmc.HearingLocation;
import uk.gov.hmcts.et.common.model.hmc.HearingWindow;
import uk.gov.hmcts.et.common.model.hmc.Judiciary;
import uk.gov.hmcts.et.common.model.hmc.PanelRequirements;
import uk.gov.hmcts.et.common.model.hmc.PartyDetails;
import uk.gov.hmcts.et.common.model.hmc.ScreenNavigation;
import uk.gov.hmcts.et.common.model.hmc.UnavailabilityRanges;
import uk.gov.hmcts.et.common.model.hmc.Vocabulary;
import uk.gov.hmcts.et.common.model.listing.ListingData;

import java.util.List;

/**
 * Employment Tribunal claim data. This class contains all the data for a citizen's claim.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CaseData extends Et1CaseData {
    @JsonProperty("tribunalCorrespondenceAddress")
    @CCD(
            label = "Correspondence Address",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Correspondence Address",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Address tribunalCorrespondenceAddress;
    @JsonProperty("tribunalCorrespondenceTelephone")
    @CCD(
            label = "Correspondence Telephone",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Correspondence Telephone",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tribunalCorrespondenceTelephone;
    @JsonProperty("tribunalCorrespondenceFax")
    @CCD(
            label = "Correspondence Fax",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Correspondence Fax",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tribunalCorrespondenceFax;
    @JsonProperty("tribunalCorrespondenceDX")
    @CCD(
            label = "Correspondence DX",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Correspondence DX",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tribunalCorrespondenceDX;
    @JsonProperty("tribunalCorrespondenceEmail")
    @CCD(
            label = "Correspondence Email",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Correspondence Email",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tribunalCorrespondenceEmail;
    @JsonProperty("ethosCaseReference")
    @CCD(
            label = "Case Number",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access027.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Case Number",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access028.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String ethosCaseReference;
    @JsonProperty("multipleName")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String multipleName;
    @JsonProperty("multipleReference")
    @CCD(
            label = "Multiple reference",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access137.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "Multiple reference",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access168.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    @CCD(
            label = "Multiple reference",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleProdDefinition.class
    )
    @CCD(
            label = "Multiple reference",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleProdDefinition.class
    )
    private String multipleReference;
    @JsonProperty("multipleReferenceLinkMarkUp")
    @CCD(
            label = "Go to:",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access137.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "Go to:",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access168.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    @CCD(
            label = "Go to:",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleProdDefinition.class
    )
    @CCD(
            label = "Go to:",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleProdDefinition.class
    )
    private String multipleReferenceLinkMarkUp;
    @JsonProperty("parentMultipleCaseId")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String parentMultipleCaseId;
    @JsonProperty("subMultipleName")
    @CCD(
            authorisationId = "subMultipleName",
            access = SingleAccess.Access056.class,
            omitFromCaseField = true,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            authorisationId = "subMultipleName",
            access = SingleAccess.Access093.class,
            omitFromCaseField = true,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String subMultipleName;
    @JsonProperty("leadClaimant")
    @CCD(
            label = "Lead claimant",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access137.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "Lead claimant",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access168.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    @CCD(
            label = "Lead claimant",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleProdDefinition.class
    )
    @CCD(
            label = "Lead claimant",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleProdDefinition.class
    )
    private String leadClaimant;
    @JsonProperty("multipleFlag")
    @CCD(
            label = "check multiple",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access147.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "check multiple",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access181.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String multipleFlag;

    @JsonProperty("claimant_TypeOfClaimant")
    @CCD(
            id = "claimant_TypeOfClaimant",
            label = "Type of claimant",
            hint = "Is the claimant an individual or a company?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantType",
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimant_TypeOfClaimant",
            label = "Type of claimant",
            hint = "Is the claimant an individual or a company?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantType",
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTypeOfClaimant;
    @JsonProperty("claimant_Company")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String claimantCompany;
    @JsonProperty("preAcceptCase")
    @CCD(
            label = " ",
            typeNameOverride = "acceptOrRejectCase",
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "acceptOrRejectCase",
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private CasePreAcceptType preAcceptCase;

    @JsonProperty("claimServedDate")
    @CCD(
            label = "Claim Served Date",
            typeOverride = FieldType.Date,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claim Served Date",
            typeOverride = FieldType.Date,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimServedDate;
    @JsonProperty("et3DueDate")
    @CCD(
            label = "ET3 Due Date",
            typeOverride = FieldType.Date,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "ET3 Due Date",
            typeOverride = FieldType.Date,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3DueDate;

    @JsonProperty("feeGroupReference")
    @CCD(
            label = "Digital Case Reference",
            hint = "Digital Case Reference (12 or 16 digit number)",
            regex = "^([0-9]{12}|[0-9]{16})$",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Digital Case Reference",
            hint = "Digital Case Reference (12 or 16 digit number)",
            regex = "^([0-9]{12}|[0-9]{16})$",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access168.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String feeGroupReference;
    @JsonProperty("claimantWorkAddressQRespondent")
    @CCD(
            label = "Please select the Respondent whose address should be used",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Please select the Respondent whose address should be used",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType claimantWorkAddressQRespondent;
    @JsonProperty("repCollection")
    @CCD(
            label = "Respondent Representative(s)",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentRepresentative",
            access = SingleAccess.Access065.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondent Representative(s)",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentRepresentative",
            access = SingleAccess.Access098.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<RepresentedTypeRItem> repCollection;
    @JsonProperty("positionType")
    @CCD(
            label = "Current Position",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Position",
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Current Position",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Position",
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String positionType;
    @JsonProperty("dateToPosition")
    @CCD(
            label = "Date To Position",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Date To Position",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String dateToPosition;
    @JsonProperty("currentPosition")
    @CCD(
            label = "Current Position",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Current Position",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String currentPosition;
    @JsonProperty("fileLocation")
    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private DynamicFixedListType fileLocation;
    @JsonProperty("fileLocationGlasgow")
    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType fileLocationGlasgow;
    @JsonProperty("fileLocationAberdeen")
    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType fileLocationAberdeen;
    @JsonProperty("fileLocationDundee")
    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType fileLocationDundee;
    @JsonProperty("fileLocationEdinburgh")
    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType fileLocationEdinburgh;
    @JsonProperty("hearingCollection")
    @CCD(
            label = " ",
            hint = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Hearing",
            access = SingleAccess.Access034.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            hint = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Hearing",
            access = SingleAccess.Access042.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<HearingTypeItem> hearingCollection;
    @JsonProperty("hearingDetailsCollection")
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingDetails",
            searchable = false,
            access = SingleAccess.Access066.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingDetails",
            searchable = false,
            access = SingleAccess.Access101.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<HearingDetailTypeItem> hearingDetailsCollection;
    @JsonProperty("nextHearingDetails")
    @CCD(
            label = "Next Hearing Details",
            typeNameOverride = "nextHearingDetails",
            searchable = false,
            access = SingleAccess.Access159.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Next Hearing Details",
            typeNameOverride = "nextHearingDetails",
            searchable = false,
            access = SingleAccess.Access183.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NextHearingDetails nextHearingDetails;
    @JsonProperty("depositType")
    @CCD(
            id = "depositType",
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Deposit",
            searchable = false,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "depositType",
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Deposit",
            searchable = false,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DepositTypeItem> depositCollection;
    @JsonProperty("judgementCollection")
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Judgment",
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Judgment",
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<JudgementTypeItem> judgementCollection;
    @JsonProperty("bfActions")
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "BFActions",
            access = SingleAccess.Access139.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "BFActions",
            access = SingleAccess.Access170.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<BFActionTypeItem> bfActions;
    @JsonProperty("clerkResponsible")
    @CCD(
            label = "Clerk Responsible",
            typeOverride = FieldType.DynamicList,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Clerk Responsible",
            typeOverride = FieldType.DynamicList,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType clerkResponsible;
    @JsonProperty("userLocation")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String userLocation;
    @JsonProperty("addDocumentCollection")
    @CCD(
            label = "Adds Case Documents",
            hint = "Adds documentation for the case(excludes already uploaded docs)",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            min = 1,
            access = SingleAccess.Access156.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Adds Case Documents",
            hint = "Adds documentation for the case(excludes already uploaded docs)",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            min = 1,
            access = SingleAccess.Access179.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> addDocumentCollection;
    @JsonProperty("correspondenceScotType")
    @CCD(
            label = "List of correspondence items",
            typeNameOverride = "Letters",
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private CorrespondenceScotType correspondenceScotType;
    @JsonProperty("correspondenceType")
    @CCD(
            label = "List of correspondence items",
            typeNameOverride = "Letters",
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private CorrespondenceType correspondenceType;
    @JsonProperty("addressLabelsSelectionType")
    @CCD(
            label = "Address labels selection",
            typeNameOverride = "addressLabelsSelection",
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Address labels selection",
            typeNameOverride = "addressLabelsSelection",
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private AddressLabelsSelectionType addressLabelsSelectionType;
    @JsonProperty("addressLabelCollection")
    @CCD(
            label = "Address labels",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "addressLabel",
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Address labels",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "addressLabel",
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<AddressLabelTypeItem> addressLabelCollection;
    @JsonProperty("addressLabelsAttributesType")
    @CCD(
            label = "Address labels attributes",
            typeNameOverride = "addressLabelsAttributes",
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Address labels attributes",
            typeNameOverride = "addressLabelsAttributes",
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private AddressLabelsAttributesType addressLabelsAttributesType;
    @JsonProperty("allocatedOffice")
    @CCD(
            label = "Allocated Office",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "VenueScotland",
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String allocatedOffice;
    @JsonProperty("conciliationTrack")
    @CCD(
            label = "Conciliation Track",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Conciliation",
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Conciliation Track",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Conciliation",
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String conciliationTrack;
    @JsonProperty("counterClaim")
    @CCD(
            label = "Employer Contract Claim case",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access048.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Employer Contract Claim case",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String counterClaim;
    @JsonProperty("eccCases")
    @CCD(
            label = "Employer Contract Claim cases",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CounterClaim",
            access = SingleAccess.Access048.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Employer Contract Claim cases",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CounterClaim",
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<EccCounterClaimTypeItem> eccCases;
    @JsonProperty("respondentsWithEcc")
    @CCD(
            label = "Respondents with an ECC",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access155.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondents with an ECC",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access177.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondentsWithEcc;
    @JsonProperty("restrictedReporting")
    @CCD(
            label = "Restricted Case",
            typeNameOverride = "RestrictedCase",
            searchable = false,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Restricted Case",
            typeNameOverride = "RestrictedCase",
            searchable = false,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private RestrictedReportingType restrictedReporting;
    @JsonProperty("printHearingDetails")
    @CCD(
            label = "Hearing List",
            typeNameOverride = "ListingType",
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing List",
            typeNameOverride = "ListingType",
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private ListingData printHearingDetails;
    @JsonProperty("printHearingCollection")
    @CCD(
            label = "Hearing List",
            typeNameOverride = "ListingType",
            searchable = false,
            access = SingleAccess.Access068.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing List",
            typeNameOverride = "ListingType",
            searchable = false,
            access = SingleAccess.Access102.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private ListingData printHearingCollection;
    @JsonProperty("targetHearingDate")
    @CCD(
            label = "Target Hearing Date",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Target Hearing Date",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String targetHearingDate;
    @JsonProperty("claimant")
    @CCD(
            label = "Claimant",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access027.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access028.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimant;
    @JsonProperty("claimantId")
    @CCD(
            label = "claimantId",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "claimantId",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access094.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantId;
    @JsonProperty("respondent")
    @CCD(
            label = "Respondent",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access027.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondent",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access028.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondent;

    @JsonProperty("EQP")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String eqp;
    @JsonProperty("flag1")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String flag1;
    @JsonProperty("flag2")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String flag2;

    @JsonProperty("docMarkUp")
    @CCD(
            label = "Doc MarkUp",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Doc MarkUp",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String docMarkUp;
    @JsonProperty("caseRefNumberCount")
    @CCD(
            label = "Case Ref Number Count",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Case Ref Number Count",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String caseRefNumberCount;
    @JsonProperty("startCaseRefNumber")
    @CCD(
            label = "Start Case Ref Number",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Start Case Ref Number",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String startCaseRefNumber;
    @JsonProperty("multipleRefNumber")
    @CCD(
            label = "Multiple Ref Number",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Multiple Ref Number",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String multipleRefNumber;

    @JsonProperty("caseRefECC")
    @CCD(
            label = "Enter the Case Number that this ECC relates to",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the Case Number that this ECC relates to",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String caseRefECC;
    @JsonProperty("respondentECC")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private DynamicFixedListType respondentECC;
    @JsonProperty("ccdID")
    @CCD(
            label = "Employer Contract Claim",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Employer Contract Claim",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String ccdID;

    @JsonProperty("flagsImageFileName")
    @CCD(
            label = "Flags Image File Name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Flags Image File Name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String flagsImageFileName;
    @JsonProperty("flagsImageAltText")
    @CCD(
            label = "Flags Image Alt Text",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Flags Image Alt Text",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String flagsImageAltText;

    // add hearing - page1
    @JsonProperty("hearingNumbers")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingNumbers;
    @JsonProperty("hearingTypes")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingTypes;
    @JsonProperty("hearingPublicPrivate")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingPublicPrivate;
    @JsonProperty("hearingVenue")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private DynamicFixedListType hearingVenue;
    @JsonProperty("hearingEstLengthNum")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingEstLengthNum;
    @JsonProperty("hearingEstLengthNumType")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingEstLengthNumType;
    @JsonProperty("hearingSitAlone")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingSitAlone;
    @JsonProperty("Hearing_stage")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingStage;
    @JsonProperty("listedDate")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String listedDate;
    @JsonProperty("Hearing_notes")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingNotes;
    // amend hearing - page1
    @JsonProperty("hearingSelection")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private DynamicFixedListType hearingSelection;
    // amend hearing - page2
    @JsonProperty("hearingActions")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingActions;
    // amend hearing - page3
    @JsonProperty("hearingERMember")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingERMember;
    @JsonProperty("hearingEEMember")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingEEMember;
    @JsonProperty("hearingDatesRequireAmending")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingDatesRequireAmending;
    @JsonProperty("hearingDateSelection")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private DynamicFixedListType hearingDateSelection;
    // amend hearing - page4
    @JsonProperty("hearingDateActions")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingDateActions;
    // amend hearing - page5
    @JsonProperty("hearingStatus")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingStatus;
    @JsonProperty("Postponed_by")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String postponedBy;
    @JsonProperty("hearingRoom")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private DynamicFixedListType hearingRoom;
    @JsonProperty("hearingClerk")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private DynamicFixedListType hearingClerk;
    @JsonProperty("hearingJudge")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private DynamicFixedListType hearingJudge;
    // amend hearing - page6
    @JsonProperty("hearingCaseDisposed")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingCaseDisposed;
    @JsonProperty("Hearing_part_heard")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingPartHeard;
    @JsonProperty("Hearing_reserved_judgement")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingReservedJudgement;
    @JsonProperty("attendee_claimant")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String attendeeClaimant;
    @JsonProperty("attendee_non_attendees")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String attendeeNonAttendees;
    @JsonProperty("attendee_resp_no_rep")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String attendeeRespNoRep;
    @JsonProperty("attendee_resp_&_rep")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String attendeeRespAndRep;
    @JsonProperty("attendee_rep_only")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String attendeeRepOnly;
    @JsonProperty("companyPremises")
    @CCD(
            label = "Premises",
            typeNameOverride = "CompanyPremises",
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Premises",
            typeNameOverride = "CompanyPremises",
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private CompanyPremisesType companyPremises;

    @JsonProperty("officeCT")
    @CCD(
            label = "Select the office you want to transfer the case to",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select office to transfer case to",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType officeCT;
    @JsonProperty("reasonForCT")
    @CCD(
            label = "Reason for Case Transfer",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Reason for Case Transfer",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String reasonForCT;
    @JsonProperty("relatedCaseCT")
    @CCD(
            label = "Link to related case",
            typeOverride = FieldType.CaseLink,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Link to related case",
            typeOverride = FieldType.CaseLink,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String relatedCaseCT;
    @JsonProperty("positionTypeCT")
    @CCD(
            label = "Current Position",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PositionCT",
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Current Position",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PositionCT",
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String positionTypeCT;
    @JsonProperty("linkedCaseCT")
    @CCD(
            label = "Link to related case",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Link to related case",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String linkedCaseCT;
    @JsonProperty("transferredCaseLink")
    @CCD(
            label = "Case Link:",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access142.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Case Link:",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access173.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String transferredCaseLink;
    @JsonProperty("transferredCaseLinkSourceCaseId")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String transferredCaseLinkSourceCaseId;
    @JsonProperty("transferredCaseLinkSourceCaseTypeId")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String transferredCaseLinkSourceCaseTypeId;
    @JsonProperty("ecmOfficeCT")
    @CCD(
            label = "Select office to transfer case to",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice",
            searchable = false,
            access = SingleAccess.Access067.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select office to transfer case to",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice",
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String ecmOfficeCT;
    @JsonProperty("assignOffice")
    @CCD(
            label = "Select the office you want to assign the case to",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the office you want to assign the case to",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType assignOffice;

    @JsonProperty("retrospectiveTTL")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String retrospectiveTTL;

    @JsonProperty("stateAPI")
    @CCD(
            label = "state",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "state",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String stateAPI;

    // Allocate Hearing fields
    @JsonProperty("allocateHearingHearing")
    @CCD(
            label = "Select Hearing",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select Hearing",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType allocateHearingHearing;
    @JsonProperty("allocateHearingManagingOffice")
    @CCD(
            label = "Managing Office",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "VenueScotland",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String allocateHearingManagingOffice;
    @JsonProperty("allocateHearingVenue")
    @CCD(
            label = "Select Hearing Venue",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select Hearing Venue",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType allocateHearingVenue;
    @JsonProperty("allocateHearingRoom")
    @CCD(
            label = "Select Room",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select Room",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType allocateHearingRoom;
    @JsonProperty("allocateHearingClerk")
    @CCD(
            label = "Select Clerk",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select Clerk",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType allocateHearingClerk;
    @JsonProperty("allocateHearingSitAlone")
    @CCD(
            label = "Panel Type",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_SitAlone",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Panel Type",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_SitAlone",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String allocateHearingSitAlone;
    @JsonProperty("allocateHearingJudge")
    @CCD(
            label = "Employment Judge",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Employment Judge",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access111.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType allocateHearingJudge;
    @JsonProperty("allocateHearingAdditionalJudge")
    @CCD(
            label = "Employment Judge",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access077.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Employment Judge",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access109.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType allocateHearingAdditionalJudge;
    @JsonProperty("allocateHearingEmployerMember")
    @CCD(
            label = "Employer Member",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Employer Member",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType allocateHearingEmployerMember;
    @JsonProperty("allocateHearingEmployeeMember")
    @CCD(
            label = "EmployeeMember",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "EmployeeMember",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType allocateHearingEmployeeMember;
    @JsonProperty("allocateHearingPostponedBy")
    @CCD(
            label = "Postponed by",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PostponedBy",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Postponed by",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PostponedBy",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String allocateHearingPostponedBy;
    @JsonProperty("allocateHearingStatus")
    @CCD(
            label = "Hearing Status",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingStatus",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing Status",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingStatus",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String allocateHearingStatus;
    @JsonProperty("allocateHearingReadingDeliberation")
    @CCD(
            label = "Reading Day, Deliberation Day or Members Meeting?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingReadingDelib",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String allocateHearingReadingDeliberation;

    // Hearing Details fields
    @JsonProperty("hearingDetailsHearing")
    @CCD(
            label = "Select Hearing",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select Hearing",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType hearingDetailsHearing;
    @JsonProperty("uploadHearingNotesDocument")
    @CCD(
            label = "Upload hearing notes",
            typeOverride = FieldType.Document,
            searchable = false,
            access = SingleAccess.Access158.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload hearing notes",
            typeOverride = FieldType.Document,
            searchable = false,
            access = SingleAccess.Access179.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Document uploadHearingNotesDocument;
    @JsonProperty("doesHearingNotesDocExist")
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access158.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access179.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String doesHearingNotesDocExist;
    @JsonProperty("removeHearingNotesDocument")
    @CCD(
            label = "Do you want to remove the uploaded hearing notes document?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_removeDocument",
            searchable = false,
            access = SingleAccess.Access158.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to remove the uploaded hearing notes document?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_removeDocument",
            searchable = false,
            access = SingleAccess.Access179.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> removeHearingNotesDocument;
    @JsonProperty("hearingDetailsStatus")
    @CCD(
            label = "Hearing Status",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingStatus",
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing Status",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingStatus",
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsStatus;
    @JsonProperty("hearingDetailsPostponedBy")
    @CCD(
            label = "Postponed by",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PostponedBy",
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Postponed by",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PostponedBy",
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsPostponedBy;
    @JsonProperty("hearingDetailsCaseDisposed")
    @CCD(
            label = "Has the case or part of the case been disposed?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has the case or part of the case been disposed?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsCaseDisposed;
    @JsonProperty("hearingDetailsPartHeard")
    @CCD(
            label = "Has the hearing been part heard?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has the hearing been part heard?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsPartHeard;
    @JsonProperty("hearingDetailsReservedJudgment")
    @CCD(
            label = "Is there a reserved Judgment?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there a reserved Judgment?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsReservedJudgment;
    @JsonProperty("hearingDetailsAttendeeClaimant")
    @CCD(
            label = "Attendees (Claimant)",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Attendee",
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Attendees (Claimant)",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Attendee",
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsAttendeeClaimant;
    @JsonProperty("hearingDetailsAttendeeNonAttendees")
    @CCD(
            label = "Number of Non Attendees (Respondent) ",
            typeOverride = FieldType.Number,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Number of Non Attendees (Respondent) ",
            typeOverride = FieldType.Number,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsAttendeeNonAttendees;
    @JsonProperty("hearingDetailsAttendeeRespNoRep")
    @CCD(
            label = "Respondent Attended - No Representative",
            typeOverride = FieldType.Number,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondent Attended - No Representative",
            typeOverride = FieldType.Number,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsAttendeeRespNoRep;
    @JsonProperty("hearingDetailsAttendeeRespAndRep")
    @CCD(
            label = "Respondent and Representative Attended",
            typeOverride = FieldType.Number,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondent and Representative Attended",
            typeOverride = FieldType.Number,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsAttendeeRespAndRep;
    @JsonProperty("hearingDetailsAttendeeRepOnly")
    @CCD(
            label = "Respondent representative only attended",
            typeOverride = FieldType.Number,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondent representative only attended",
            typeOverride = FieldType.Number,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsAttendeeRepOnly;
    @JsonProperty("hearingDetailsTimingStart")
    @CCD(
            label = "Start Time",
            typeOverride = FieldType.DateTime,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Start Time",
            typeOverride = FieldType.DateTime,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsTimingStart;
    @JsonProperty("hearingDetailsTimingBreak")
    @CCD(
            label = "Break",
            typeOverride = FieldType.DateTime,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Break",
            typeOverride = FieldType.DateTime,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsTimingBreak;
    @JsonProperty("hearingDetailsTimingResume")
    @CCD(
            label = "Resume",
            typeOverride = FieldType.DateTime,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Resume",
            typeOverride = FieldType.DateTime,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsTimingResume;
    @JsonProperty("hearingDetailsTimingFinish")
    @CCD(
            label = "Finish",
            typeOverride = FieldType.DateTime,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Finish",
            typeOverride = FieldType.DateTime,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsTimingFinish;
    @JsonProperty("hearingDetailsTimingDuration")
    @CCD(
            label = "Duration",
            typeOverride = FieldType.Number,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Duration",
            typeOverride = FieldType.Number,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsTimingDuration;
    @JsonProperty("hearingDetailsHearingNotes2")
    @CCD(
            label = "Hearing Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingDetailsHearingNotes2;

    // ET1 Vetting
    @JsonProperty("trackType")
    @CCD(
            label = "Track Type",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Track Type",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String trackType;
    @JsonProperty("et1VettingDocument")
    @CCD(
            label = "ET1 Vetting Document",
            typeOverride = FieldType.Document,
            categoryID = "C71",
            access = SingleAccess.Access158.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "ET1 Vetting Document",
            typeOverride = FieldType.Document,
            categoryID = "C11",
            searchable = false,
            access = SingleAccess.Access179.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType et1VettingDocument;
    @JsonProperty("et1VettingBeforeYouStart")
    @CCD(
            label = "Before You Start placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access044.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Before You Start placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access046.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingBeforeYouStart;
    // ET1 Vetting - Can we serve the claim?
    @JsonProperty("et1VettingClaimantDetailsMarkUp")
    @CCD(
            label = "Claimant Details placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access044.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant Details placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access046.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingClaimantDetailsMarkUp;
    @JsonProperty("et1VettingRespondentDetailsMarkUp")
    @CCD(
            label = "Respondent Details placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access044.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondent Details placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access046.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingRespondentDetailsMarkUp;
    @JsonProperty("et1VettingCanServeClaimYesOrNo")
    @CCD(
            label = "Can we serve the claim with these contact details?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Can we serve the claim with these contact details?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingCanServeClaimYesOrNo;
    @JsonProperty("et1VettingCanServeClaimNoReason")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingCanServeClaimNoReason;
    @JsonProperty("et1VettingCanServeClaimGeneralNote")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access076.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingCanServeClaimGeneralNote;
    // ET1 Vetting - Acas certificate?
    @JsonProperty("et1VettingRespondentAcasDetails1")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access044.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access046.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingRespondentAcasDetails1;
    @JsonProperty("et1VettingRespondentAcasDetails2")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access044.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access046.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingRespondentAcasDetails2;
    @JsonProperty("et1VettingRespondentAcasDetails3")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access044.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access046.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingRespondentAcasDetails3;
    @JsonProperty("et1VettingRespondentAcasDetails4")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingRespondentAcasDetails4;
    @JsonProperty("et1VettingRespondentAcasDetails5")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access044.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access046.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingRespondentAcasDetails5;
    @JsonProperty("et1VettingRespondentAcasDetails6")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access044.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access046.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingRespondentAcasDetails6;
    @JsonProperty("et1VettingAcasCertIsYesOrNo1")
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertIsYesOrNo1;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo1")
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertExemptYesOrNo1;
    @JsonProperty("et1VettingAcasCertIsYesOrNo2")
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertIsYesOrNo2;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo2")
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertExemptYesOrNo2;
    @JsonProperty("et1VettingAcasCertIsYesOrNo3")
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertIsYesOrNo3;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo3")
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertExemptYesOrNo3;
    @JsonProperty("et1VettingRespondentAcasDetailsLabel4")
    @CCD(
            label = "${et1VettingRespondentAcasDetails4}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access129.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "${et1VettingRespondentAcasDetails4}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access132.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingRespondentAcasDetailsLabel4;
    @JsonProperty("et1VettingAcasCertIsYesOrNo4")
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertIsYesOrNo4;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo4")
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertExemptYesOrNo4;
    @JsonProperty("et1VettingAcasCertIsYesOrNo5")
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertIsYesOrNo5;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo5")
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertExemptYesOrNo5;
    @JsonProperty("et1VettingAcasCertIsYesOrNo6")
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there an Acas certificate?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertIsYesOrNo6;
    @JsonProperty("et1VettingAcasCertExemptYesOrNo6")
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertExemptYesOrNo6;
    @JsonProperty("et1VettingAcasCertGeneralNote")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAcasCertGeneralNote;
    @JsonProperty("et1VettingCompletedBy")
    @CCD(
            label = "Vetting completed by:",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Vetting completed by:",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access210.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingCompletedBy;
    @JsonProperty("et1DateCompleted")
    @CCD(
            label = "Date completed:",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Date completed:",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1DateCompleted;
    @JsonProperty("icCompletedBy")
    @CCD(
            label = "Initial consideration completed by:",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Initial consideration completed by:",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String icCompletedBy;
    @JsonProperty("icDateCompleted")
    @CCD(
            label = "Date completed:",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Date completed:",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String icDateCompleted;

    //ET1 Vetting -  Substantive Defects
    @JsonProperty("substantiveDefectsList")
    @CCD(
            label = "Possible substantive defects",
            hint = "Select all that apply. Does the claim, or part of it, appear to be a claim which:",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_Defects",
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Possible substantive defects",
            hint = "Select all that apply. Does the claim, or part of it, appear to be a claim which:",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_Defects",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> substantiveDefectsList;
    @JsonProperty("rule121aTextArea")
    @CCD(
            label = "The tribunal has no jurisdiction to consider",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "The tribunal has no jurisdiction to consider",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String rule121aTextArea;
    @JsonProperty("rule121bTextArea")
    @CCD(
            label = "Is in a form which cannot sensibly be responded to or otherwise an abuse of process",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is in a form which cannot sensibly be responded to or otherwise an abuse of process",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String rule121bTextArea;
    @JsonProperty("rule121cTextArea")
    @CCD(
            label = "Has neither an EC number nor claims one of the EC exemptions",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has neither an EC number nor claims one of the EC exemptions",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String rule121cTextArea;
    @JsonProperty("rule121dTextArea")
    @CCD(
            label = "States that one of the EC exceptions applies but it might not",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "States that one of the EC exceptions applies but it might not",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String rule121dTextArea;
    @JsonProperty("rule121daTextArea")
    @CCD(
            label = "Institutes relevant proceedings and the EC number on the claim form does not match the EC number on the Acas certificate",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Institutes relevant proceedings and the EC number on the claim form does not match the EC number on the Acas certificate",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String rule121daTextArea;
    @JsonProperty("rule121eTextArea")
    @CCD(
            label = "Has a different claimant name on the ET1 to the claimant name on the Acas certificate",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has a different claimant name on the ET1 to the claimant name on the Acas certificate",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String rule121eTextArea;
    @JsonProperty("rule121fTextArea")
    @CCD(
            label = "Has a different respondent name on the ET1 to the respondent name on the Acas certificate",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has a different respondent name on the ET1 to the respondent name on the Acas certificate",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String rule121fTextArea;
    @JsonProperty("et1SubstantiveDefectsGeneralNotes")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1SubstantiveDefectsGeneralNotes;
    @JsonProperty("icEt1SubstantiveDefects")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String icEt1SubstantiveDefects;
    @JsonProperty("icEt1ReferralToJudgeOrLOListWithDetails")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String icEt1ReferralToJudgeOrLOListWithDetails;
    @JsonProperty("icEt1ReferralToREJOrVPListWithDetails")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String icEt1ReferralToREJOrVPListWithDetails;
    @JsonProperty("icEt1OtherReferralListDetails")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String icEt1OtherReferralListDetails;


    // ET1 Vetting - Jurisdiction codes
    @JsonProperty("areTheseCodesCorrect")
    @CCD(
            label = "Are these codes correct?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Are these codes correct?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String areTheseCodesCorrect;
    @JsonProperty("codesCorrectGiveDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String codesCorrectGiveDetails;
    @JsonProperty("et1JurisdictionCodeGeneralNotes")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1JurisdictionCodeGeneralNotes;
    @JsonProperty("existingJurisdictionCodes")
    @CCD(
            label = "existingJurisdictionCodes",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "existingJurisdictionCodes",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String existingJurisdictionCodes;
    @JsonProperty("vettingJurisdictionCodeCollection")
    @CCD(
            label = "Jurisdiction code",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "JurisdictionCode",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Jurisdiction code",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "JurisdictionCode",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<VettingJurCodesTypeItem> vettingJurisdictionCodeCollection;

    // ET1 Vetting - Track allocation
    @JsonProperty("isTrackAllocationCorrect")
    @CCD(
            label = "Is the track allocation correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_isTrackAllocationCorrect",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is the track allocation correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_isTrackAllocationCorrect",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String isTrackAllocationCorrect;
    @JsonProperty("suggestAnotherTrack")
    @CCD(
            label = "Track allocation",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_suggestAnotherTrack",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Track allocation",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_suggestAnotherTrack",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String suggestAnotherTrack;
    @JsonProperty("whyChangeTrackAllocation")
   @CCD(
           label = "Why should we change the track allocation?",
           typeOverride = FieldType.TextArea,
           searchable = false,
           access = SingleAccess.Access157.class,
           includeInProfiles = EnglandWalesSingleDefinition.class
   )
   @CCD(
           label = "Why should we change the track allocation?",
           typeOverride = FieldType.TextArea,
           searchable = false,
           access = SingleAccess.Access178.class,
           includeInProfiles = ScotlandSingleDefinition.class
   )
    private String whyChangeTrackAllocation;
    @JsonProperty("trackAllocationGeneralNotes")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String trackAllocationGeneralNotes;
    @JsonProperty("isLocationCorrect")
    @CCD(
            label = "Is this location correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_isLocationCorrect",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this location correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_isLocationCorrect",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String isLocationCorrect;
    @JsonProperty("whyChangeOffice")
   @CCD(
           label = "Why should we change the office?",
           typeOverride = FieldType.TextArea,
           searchable = false,
           access = SingleAccess.Access157.class,
           includeInProfiles = EnglandWalesSingleDefinition.class
   )
   @CCD(
           label = "Why should we change the office?",
           typeOverride = FieldType.TextArea,
           searchable = false,
           access = SingleAccess.Access178.class,
           includeInProfiles = ScotlandSingleDefinition.class
   )
    private String whyChangeOffice;
    @JsonProperty("et1LocationGeneralNotes")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1LocationGeneralNotes;
    @JsonProperty("trackAllocation")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String trackAllocation;
    @JsonProperty("tribunalAndOfficeLocation")
    @CCD(
            label = "tribunalAndOfficeLocation",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "tribunalAndOfficeLocation",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tribunalAndOfficeLocation;
    @JsonProperty("regionalOffice")
    @CCD(
            label = "regionalOffice",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "regionalOffice",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String regionalOffice;
    @JsonProperty("regionalOfficeList")
    @CCD(
            label = "Local or regional office",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Local or regional office",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType regionalOfficeList;
    // ET1 Vetting - Hearing venues
    @JsonProperty("et1AddressDetails")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access044.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access046.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1AddressDetails;
    @JsonProperty("et1TribunalRegion")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access044.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access046.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1TribunalRegion;
    @JsonProperty("et1HearingVenues")
    @CCD(
            label = "Hearing venue selected",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing venue selected",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType et1HearingVenues;
    @JsonProperty("et1SuggestHearingVenue")
    @CCD(
            label = "Do you want to suggest a hearing venue?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to suggest a hearing venue?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1SuggestHearingVenue;
    @JsonProperty("et1HearingVenueGeneralNotes")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1HearingVenueGeneralNotes;
    @JsonProperty("et1GovOrMajorQuestion")
    @CCD(
            label = "Is the respondent a government agency or a major employer?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is the respondent a government agency or a major employer?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1GovOrMajorQuestion;

    // ET1 Vetting - Further questions
    @JsonProperty("et1ReasonableAdjustmentsQuestion")
    @CCD(
            label = "Are reasonable adjustments required?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Are reasonable adjustments required?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1ReasonableAdjustmentsQuestion;
    @JsonProperty("et1ReasonableAdjustmentsTextArea")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1ReasonableAdjustmentsTextArea;
    @JsonProperty("et1VideoHearingQuestion")
    @CCD(
            label = "Can the claimant attend a video hearing?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Can the claimant attend a video hearing?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VideoHearingQuestion;
    @JsonProperty("et1VideoHearingTextArea")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VideoHearingTextArea;
    @JsonProperty("et1FurtherQuestionsGeneralNotes")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1FurtherQuestionsGeneralNotes;

    // ET1 Vetting - Referral to judge
    @JsonProperty("referralToJudgeOrLOList")
    @CCD(
            label = "Possible referral to a judge or legal officer",
            hint = "Does the claim include any of the following?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_judgeOrLO",
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Possible referral to a judge or legal officer",
            hint = "Does the claim include any of the following?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_judgeOrLO",
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> referralToJudgeOrLOList;
    @JsonProperty("aClaimOfInterimReliefTextArea")
    @CCD(
            id = "aClaimOfInterimReliefTextArea",
            label = "A claim of interim relief",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "aClaimOfInterimReliefTextArea",
            label = "A claim of interim relief",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String aclaimOfInterimReliefTextArea;
    @JsonProperty("aStatutoryAppealTextArea")
    @CCD(
            id = "aStatutoryAppealTextArea",
            label = "A statutory appeal",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "aStatutoryAppealTextArea",
            label = "A statutory appeal",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String astatutoryAppealTextArea;
    @JsonProperty("anAllegationOfCommissionOfSexualOffenceTextArea")
    @CCD(
            label = "An allegation of commission of sexual offence",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "An allegation of commission of sexual offence",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String anAllegationOfCommissionOfSexualOffenceTextArea;
    @JsonProperty("insolvencyTextArea")
    @CCD(
            label = "Insolvency",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Insolvency",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String insolvencyTextArea;
    @JsonProperty("jurisdictionsUnclearTextArea")
    @CCD(
            label = "Jurisdictions unclear",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Jurisdictions unclear",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String jurisdictionsUnclearTextArea;
    @JsonProperty("lengthOfServiceTextArea")
    @CCD(
            label = "Length of service",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Length of service",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String lengthOfServiceTextArea;
    @JsonProperty("potentiallyLinkedCasesInTheEcmTextArea")
    @CCD(
            label = "Potentially linked cases in the ECM",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Potentially linked cases in the ECM",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String potentiallyLinkedCasesInTheEcmTextArea;
    @JsonProperty("rule50IssuesTextArea")
    @CCD(
            label = "Rule 49 issues",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Rule 49 issues",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String rule50IssuesTextArea;
    @JsonProperty("anotherReasonForJudicialReferralTextArea")
    @CCD(
            label = "Another reason for judicial referral",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Another reason for judicial referral",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String anotherReasonForJudicialReferralTextArea;
    @JsonProperty("et1JudgeReferralGeneralNotes")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1JudgeReferralGeneralNotes;
    @JsonProperty("referralToREJOrVPList")
    @CCD(
            label = "Possible referral to Regional Employment Judge or Vice-President",
            hint = "Does the claim include any of the following?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_REJOrVP",
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Possible referral to Regional Employment Judge or Vice-President",
            hint = "Does the claim include any of the following?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_REJOrVP",
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> referralToREJOrVPList;

    // ET1 Vetting - Referral to Regional Employment judge
    @JsonProperty("vexatiousLitigantOrderTextArea")
    @CCD(
            label = "A claimant covered by vexatious litigant order",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "A claimant covered by vexatious litigant order",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String vexatiousLitigantOrderTextArea;
    @JsonProperty("aNationalSecurityIssueTextArea")
    @CCD(
            id = "aNationalSecurityIssueTextArea",
            label = "A national security issue",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "aNationalSecurityIssueTextArea",
            label = "A national security issue",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String anationalSecurityIssueTextArea;
    @JsonProperty("nationalMultipleOrPresidentialOrderTextArea")
    @CCD(
            label = "A part of national multiple / covered by Presidential case management order",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "A part of national multiple / covered by Presidential case management order",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String nationalMultipleOrPresidentialOrderTextArea;
    @JsonProperty("transferToOtherRegionTextArea")
    @CCD(
            label = "A request for transfer to another ET region",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "A request for transfer to another ET region",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String transferToOtherRegionTextArea;
    @JsonProperty("serviceAbroadTextArea")
    @CCD(
            label = "A request for service abroad",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "A request for service abroad",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String serviceAbroadTextArea;
    @JsonProperty("aSensitiveIssueTextArea")
    @CCD(
            id = "aSensitiveIssueTextArea",
            label = "A sensitive issue which may attract publicity or need early allocation to a specific judge",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "aSensitiveIssueTextArea",
            label = "A sensitive issue which may attract publicity or need early allocation to a specific judge",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String asensitiveIssueTextArea;
    @JsonProperty("anyPotentialConflictTextArea")
    @CCD(
            label = "Any potential conflict involving judge, non-legal member or HMCTS staff member",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Any potential conflict involving judge, non-legal member or HMCTS staff member",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String anyPotentialConflictTextArea;
    @JsonProperty("anotherReasonREJOrVPTextArea")
    @CCD(
            label = "Another reason for Regional Employment Judge / Vice-President referral",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Another reason for Regional Employment Judge / Vice-President referral",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String anotherReasonREJOrVPTextArea;
    @JsonProperty("et1REJOrVPReferralGeneralNotes")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1REJOrVPReferralGeneralNotes;
    @JsonProperty("otherReferralList")
    @CCD(
            label = "Does the claim include any other factors",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_otherFactors",
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Does the claim include any other factors?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_otherFactors",
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> otherReferralList;

    // ET1 Vetting - Other Factors
    @JsonProperty("claimOutOfTimeTextArea")
    @CCD(
            label = "The whole or any part of the claim is out of time",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "The whole or any part of the claim is out of time",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimOutOfTimeTextArea;
    @JsonProperty("multipleClaimTextArea")
    @CCD(
            label = "The claim is part of a multiple claim",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "The claim is part of a multiple claim",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String multipleClaimTextArea;
    @JsonProperty("employmentStatusIssuesTextArea")
    @CCD(
            label = "The claim has a potential issue about employment status",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "The claim has a potential issue about employment status",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String employmentStatusIssuesTextArea;
    @JsonProperty("pidJurisdictionRegulatorTextArea")
    @CCD(
            label = "The claim has PID jurisdiction and claimant wants it forwarded to relevant regulator - Box 10.1",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "The claim has PID jurisdiction and claimant wants it forwarded to relevant regulator - Box 10.1",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String pidJurisdictionRegulatorTextArea;
    @JsonProperty("videoHearingPreferenceTextArea")
    @CCD(
            label = "The claimant prefers a video hearing",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "The claimant prefers a video hearing",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String videoHearingPreferenceTextArea;
    @JsonProperty("rule50IssuesForOtherReferralTextArea")
    @CCD(
            label = "The claim has Rule 49 issues",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "The claim has Rule 49 issues",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String rule50IssuesForOtherReferralTextArea;
    @JsonProperty("anotherReasonForOtherReferralTextArea")
    @CCD(
            label = "The claim has other relevant factors for judicial referral",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "The claim has other relevant factors for judicial referral",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access163.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String anotherReasonForOtherReferralTextArea;
    @JsonProperty("et1OtherReferralGeneralNotes")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1OtherReferralGeneralNotes;
    @JsonProperty("et1VettingAdditionalInformationTextArea")
    @CCD(
            label = "Additional Information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Additional Information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1VettingAdditionalInformationTextArea;


    // ET1 Serving
    @JsonProperty("servingDocumentCollection")
    @CCD(
            label = "Upload document PDF",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ServingDocumentUpload",
            searchable = false,
            access = SingleAccess.Access069.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document PDF",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ServingDocumentUpload",
            searchable = false,
            access = SingleAccess.Access103.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> servingDocumentCollection;
    @JsonProperty("otherTypeDocumentName")
    @CCD(
            label = "Serving document other type name placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access035.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Serving document other type name placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access038.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String otherTypeDocumentName;
    @JsonProperty("servingDocumentRecipient")
    @CCD(
            label = " ",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_ServingDocumentRecipient",
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_ServingDocumentRecipient",
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> servingDocumentRecipient;
    @JsonProperty("claimantAndRespondentAddresses")
    @CCD(
            label = "claimantAndRespondentAddresses",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access035.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "claimantAndRespondentAddresses",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantAndRespondentAddresses;
    @JsonProperty("emailLinkToAcas")
    @CCD(
            label = "Email link to Acas placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access035.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Email link to Acas placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String emailLinkToAcas;

    //    et3vetting
    @JsonProperty("et3ChooseRespondent")
    @CCD(
            label = "Select the respondent you are processing",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the respondent you are processing",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType et3ChooseRespondent;
    @JsonProperty("et3Date")
    @CCD(
            label = "et3Date",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "et3Date",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3Date;
    // ET3 Response Page
    @JsonProperty("et3IsThereAnEt3Response")
    @CCD(
            label = "Is there an ET3 response?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access070.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there an ET3 response?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access104.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3IsThereAnEt3Response;
    @JsonProperty("et3NoEt3Response")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3NoEt3Response;
    @JsonProperty("et3GeneralNotes")
    @CCD(
            label = "General Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotes;
    // ET3 Company House search document page
    @JsonProperty("et3IsThereACompaniesHouseSearchDocument")
    @CCD(
            label = "Is there a Companies House search document?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there a Companies House search document?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3IsThereACompaniesHouseSearchDocument;
    @JsonProperty("et3CompanyHouseDocument")
    @CCD(
            label = "Upload the Companies House search document",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            categoryID = "C18",
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload the Companies House search document",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            categoryID = "C18",
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType et3CompanyHouseDocument;
    @JsonProperty("et3GeneralNotesCompanyHouse")
    @CCD(
            label = "General Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesCompanyHouse;
    // ET3 Individual insolvency search document page
    @JsonProperty("et3IsThereAnIndividualSearchDocument")
    @CCD(
            label = "Is there an individual insolvency search document?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there an individual insolvency search document?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3IsThereAnIndividualSearchDocument;
    @JsonProperty("et3IndividualInsolvencyDocument")
    @CCD(
            label = "Upload the individual insolvency search document",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            categoryID = "C18",
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload the individual insolvency search document",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            categoryID = "C18",
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType et3IndividualInsolvencyDocument;
    @JsonProperty("et3GeneralNotesIndividualInsolvency")
    @CCD(
            label = "General Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesIndividualInsolvency;
    // ET3 Legal issue page
    @JsonProperty("et3LegalIssue")
    @CCD(
            label = "Is there an issue with whether the respondent is a legal entity?",
            hint = "Check all respondents. If any appear to have an issue with their legal status, select Yes.",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_respondent_legal_entity",
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there an issue with whether the respondent is a legal entity?",
            hint = "Check all respondents. If any appear to have an issue with their legal status, select Yes.",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_respondent_legal_entity",
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3LegalIssue;
    @JsonProperty("et3LegalIssueGiveDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3LegalIssueGiveDetails;
    @JsonProperty("et3GeneralNotesLegalEntity")
    @CCD(
            label = "General Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesLegalEntity;
    // ET3 Response in time page
    @JsonProperty("et3ResponseInTime")
    @CCD(
            label = "Did we receive the ET3 response in time?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Did we receive the ET3 response in time?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseInTime;
    @JsonProperty("et3ResponseInTimeDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseInTimeDetails;
    // ET3 Respondents Name page
    @JsonProperty("et3NameAddressRespondent")
    @CCD(
            label = "et3NameAddressRespondent",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "et3NameAddressRespondent",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3NameAddressRespondent;
    @JsonProperty("et3DoWeHaveRespondentsName")
    @CCD(
            label = "Do we have the respondent's name?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do we have the respondent's name?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3DoWeHaveRespondentsName;
    @JsonProperty("et3GeneralNotesRespondentName")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesRespondentName;
    @JsonProperty("et3DoesRespondentsNameMatch")
    @CCD(
            label = "Does the respondent's name match?",
            hint = "Check the ET1 name.",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Does the respondent's name match?",
            hint = "Check the ET1 name.",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3DoesRespondentsNameMatch;
    @JsonProperty("et3RespondentNameMismatchDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3RespondentNameMismatchDetails;
    @JsonProperty("et3GeneralNotesRespondentNameMatch")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesRespondentNameMatch;
    // ET3 Respondents Address page
    @JsonProperty("et3DoWeHaveRespondentsAddress")
    @CCD(
            label = "Do we have the respondent's address?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do we have the respondent's address?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3DoWeHaveRespondentsAddress;
    @JsonProperty("et3DoesRespondentsAddressMatch")
    @CCD(
            label = "Does the respondent's address match?",
            hint = "Check the ET1 address.",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Does the respondent's address match?",
            hint = "Check the ET1 address.",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3DoesRespondentsAddressMatch;
    @JsonProperty("et3RespondentAddressMismatchDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3RespondentAddressMismatchDetails;
    @JsonProperty("et3GeneralNotesRespondentAddress")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesRespondentAddress;
    @JsonProperty("et3GeneralNotesAddressMatch")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesAddressMatch;
    // ET3 Case Listed Page
    @JsonProperty("et3HearingDetails")
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3HearingDetails;
    @JsonProperty("et3IsCaseListedForHearing")
    @CCD(
            label = "Is the case listed for hearing?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is the case listed for hearing?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3IsCaseListedForHearing;
    @JsonProperty("et3IsCaseListedForHearingDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3IsCaseListedForHearingDetails;
    @JsonProperty("et3GeneralNotesCaseListed")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesCaseListed;
    // ET3 Transfer Application
    @JsonProperty("et3TribunalLocation")
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3TribunalLocation;
    @JsonProperty("et3IsThisLocationCorrect")
    @CCD(
            label = "Is this location correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_et3_tribunal_location_change",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this location correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_et3_tribunal_location_change",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3IsThisLocationCorrect;
    @JsonProperty("et3GeneralNotesTransferApplication")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesTransferApplication;
    @JsonProperty("et3RegionalOffice")
    @CCD(
            label = "England & Wales regional office",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Scotland regional office",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3RegionalOffice;
    @JsonProperty("et3WhyWeShouldChangeTheOffice")
    @CCD(
            label = "Why should we change the office?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Why should we change the office?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3WhyWeShouldChangeTheOffice;
    // ET3 Resist the claim
    @JsonProperty("et3ContestClaim")
    @CCD(
            label = "Does the respondent wish to contest any part of the claim?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_contest_claim_status",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Does the respondent wish to contest any part of the claim?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_contest_claim_status",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ContestClaim;
    @JsonProperty("et3ContestClaimGiveDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ContestClaimGiveDetails;
    @JsonProperty("et3GeneralNotesContestClaim")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesContestClaim;
    // ET3 Contract claim section 7
    @JsonProperty("et3ContractClaimSection7")
    @CCD(
            label = "Is there an Employer's Contract Claim in section 7 of the ET3 response?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there an Employer's Contract Claim in section 7 of the ET3 response?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ContractClaimSection7;
    @JsonProperty("et3ContractClaimSection7Details")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ContractClaimSection7Details;
    @JsonProperty("et3GeneralNotesContractClaimSection7")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesContractClaimSection7;
    // ET3 suggested issues
    @JsonProperty("et3Rule26")
    @CCD(
            label = "Are there any issues identified for the judge's initial consideration - prospects of claim / response arguable? (Rule 27)",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access051.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Are there any issues identified for the judge's initial consideration - prospects of claim / response arguable? (Rule 27)",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access088.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3Rule26;
    @JsonProperty("et3Rule26Details")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3Rule26Details;
    @JsonProperty("et3SuggestedIssues")
    @CCD(
            label = "Are there any other suggested orders, directions or issues?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_et3_suggested_issues",
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Are there any other suggested orders, directions or issues?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_et3_suggested_issues",
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> et3SuggestedIssues;
    @JsonProperty("et3SuggestedIssuesStrikeOut")
    @CCD(
            label = "Applications for strike out or deposit",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Applications for strike out or deposit",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3SuggestedIssuesStrikeOut;
    @JsonProperty("et3SuggestedIssueInterpreters")
    @CCD(
            label = "Interpreters",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Interpreters",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3SuggestedIssueInterpreters;
    @JsonProperty("et3SuggestedIssueJurisdictional")
    @CCD(
            label = "Jurisdictional issues",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Jurisdictional issues",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3SuggestedIssueJurisdictional;
    @JsonProperty("et3SuggestedIssueAdjustments")
    @CCD(
            label = "Request for adjustments",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Request for adjustments",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3SuggestedIssueAdjustments;
    @JsonProperty("et3SuggestedIssueRule50")
    @CCD(
            label = "Rule 49",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Rule 49",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3SuggestedIssueRule50;
    @JsonProperty("et3SuggestedIssueTimePoints")
    @CCD(
            label = "Time points",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Time points",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3SuggestedIssueTimePoints;
    @JsonProperty("et3GeneralNotesRule26")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3GeneralNotesRule26;
    // ET3 Final notes
    @JsonProperty("et3AdditionalInformation")
    @CCD(
            label = "Additional information",
            hint = "Enter any additional information which may be useful for a judge or legal officer to consider.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Additional information",
            hint = "Enter any additional information which may be useful for a judge or legal officer to consider.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3AdditionalInformation;

    // ET Initial Consideration
    @JsonProperty("icEt1VettingIssuesDetail")
    @CCD(
            label = "ET1 Vetting Issues",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "ET1 Vetting Issues",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String icEt1VettingIssuesDetail;
    @JsonProperty("icEt3ProcessingIssuesDetail")
    @CCD(
            label = "ET3 Processing Issues",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "ET3 Processing Issues",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String icEt3ProcessingIssuesDetail;

    @JsonProperty("initialConsiderationBeforeYouStart")
    @CCD(
            label = "To help you complete this placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "To help you complete this placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access043.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String initialConsiderationBeforeYouStart;
    @JsonProperty("etInitialConsiderationDocument")
    @CCD(
            label = "Initial Consideration Document",
            typeOverride = FieldType.Document,
            categoryID = "C23",
            access = SingleAccess.Access202.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Initial Consideration Document",
            typeOverride = FieldType.Document,
            categoryID = "C23",
            searchable = false,
            access = SingleAccess.Access205.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType etInitialConsiderationDocument;
    @JsonProperty("etInitialConsiderationRespondent")
    @CCD(
            label = "etInitialConsiderationRespondent",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "etInitialConsiderationRespondent",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etInitialConsiderationRespondent;
    @JsonProperty("icRespondentHearingPanelPreference")
    @CCD(
            label = "icRespondentHearingPanelPreference",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "icRespondentHearingPanelPreference",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String icRespondentHearingPanelPreference;
    @JsonProperty("icRespondentHearingPanelPreferenceReason")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String icRespondentHearingPanelPreferenceReason;
    @JsonProperty("etInitialConsiderationHearing")
    @CCD(
            label = "etInitialConsiderationHearing",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "etInitialConsiderationHearing",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etInitialConsiderationHearing;

    @JsonProperty("etIcPartiesHearingPanelPreferenceHeader")
    @CCD(
            label = "etIcPartiesHearingPanelPreferenceHeader",
            hint = "<h2>Parties Hearing Panel Preferences</h2>",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "etIcPartiesHearingPanelPreferenceHeader",
            hint = "<h2>Parties Hearing Panel Preferences</h2>",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etIcPartiesHearingPanelPreferenceHeader;
    @JsonProperty("etIcPartiesHearingPanelPreference")
    @CCD(
            label = "etIcPartiesHearingPanelPreference",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "etIcPartiesHearingPanelPreference",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etIcPartiesHearingPanelPreference;
    @JsonProperty("etIcPartiesHearingFormat")
    @CCD(
            label = "etIcPartiesHearingFormat",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "etIcPartiesHearingFormat",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etIcPartiesHearingFormat;

    @JsonProperty("etIcHearingPanelPreference")
    @CCD(
            label = "etIcHearingPanelPreference",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "etIcHearingPanelPreference",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etIcHearingPanelPreference;
    @JsonProperty("etInitialConsiderationJurisdictionCodes")
    @CCD(
            label = "etInitialConsiderationJurisdictionCodes",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "etInitialConsiderationJurisdictionCodes",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etInitialConsiderationJurisdictionCodes;
    @JsonProperty("icReceiptET3FormIssues")
    @CCD(
            label = "Are there any issues or instructions regarding the receipt of the ET3 form?",
            hint = "For example the date it was due, the date it was received or any application for extension of time (Rule 18 and 20)",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icReceiptET3FormIssues;
    @JsonProperty("icRespondentsNameIdentityIssues")
    @CCD(
            label = "Are there any issues or instructions regarding the respondent's name or identity?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icRespondentsNameIdentityIssues;
    @JsonProperty("icJurisdictionCodeIssues")
    @CCD(
            label = "Are there any issues or instructions regarding the jurisdiction codes?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icJurisdictionCodeIssues;
    @JsonProperty("icApplicationIssues")
    @CCD(
            label = "Are there any issues or instructions regarding the applications?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icApplicationIssues;
    @JsonProperty("icEmployersContractClaimIssues")
    @CCD(
            label = "Are there any issues or instructions regarding an Employer’s Contract Claim?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icEmployersContractClaimIssues;
    @JsonProperty("icClaimProspectIssues")
    @CCD(
            label = "Are there any issues or instructions regarding the prospects of the claim or response?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icClaimProspectIssues;
    @JsonProperty("icListingIssues")
    @CCD(
            label = "Are there any issues or instructions regarding the listing?",
            hint = "For example list for PH, if public or private and set out the issues to be determined with directions, in person or video",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icListingIssues;
    @JsonProperty("icListingPreliminaryHearing")
    @CCD(
            label = "Should the case be listed for a private preliminary hearing?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icListingPreliminaryHearing;
    @JsonProperty("icDdaDisabilityIssues")
    @CCD(
            label = "Are there any issues or instructions where DDA or disability is disputed?",
            hint = "The claimant is to provide a statement - limited to 750 words, explaining the length of the disability, the nature of its effects upon daily activities and any existing medical evidence relied upon - the the respondent by [date]. The respondent should state their position on disability, giving reasons for any denial, by [date]",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icDdaDisabilityIssues;
    @JsonProperty("icOrderForFurtherInformation")
    @CCD(
            label = "Should either side be ordered to provide any further information?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icOrderForFurtherInformation;
    @JsonProperty("icOtherIssuesOrFinalOrders")
    @CCD(
            label = "Are there any issues or instructions to consider or final orders to be given?",
            hint = "For example, Rule 50, transfer to another region, interpreters, adjustments required for hearing",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private String icOtherIssuesOrFinalOrders;
    @JsonProperty("etICJuridictionCodesInvalid")
    @CCD(
            label = "Are there any issues or instructions regarding the jurisdiction codes?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Are there any issues or instructions regarding the jurisdiction codes?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICJuridictionCodesInvalid;
    @JsonProperty("etICInvalidDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICInvalidDetails;
    @JsonProperty("etICCanProceed")
    @CCD(
            label = "Can the claim proceed due to an arguable claim and/or response?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Can the claim and response proceed?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICCanProceed;
    @JsonProperty("etICHearingAlreadyListed")
    @CCD(
            label = "Is the hearing already listed?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is the hearing already listed?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICHearingAlreadyListed;
    // ET Initial Consideration - Hearing Not Listed
    @JsonProperty("etICHearingNotListedList")
    @CCD(
            label = "Hearing not listed",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICHearingNotListed",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing not listed",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICHearingNotListed",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> etICHearingNotListedList;

    @JsonProperty("etICHearingNotListedSeekComments")
    @CCD(
            label = "Seek comments on the video hearing",
            typeNameOverride = "etICHearingNotListedSeekComments",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Seek comments on CVP hearing",
            typeNameOverride = "etICHearingNotListedSeekComments",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private EtICSeekComments etICHearingNotListedSeekComments;
    @JsonProperty("etICHearingNotListedListForPrelimHearing")
    @CCD(
            label = "List for preliminary hearing",
            typeNameOverride = "etICHearingNotListedListForPrelimHearing",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "List for preliminary hearing",
            typeNameOverride = "etICHearingNotListedListForPrelimHearing",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private EtICListForPreliminaryHearing etICHearingNotListedListForPrelimHearing;
    @JsonProperty("etICHearingNotListedListForFinalHearing")
    @CCD(
            label = "List for final hearing",
            typeNameOverride = "etICHearingNotListedListForFinalHearing",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "List for final hearing",
            typeNameOverride = "etICHearingNotListedListForFinalHearing",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private EtICListForFinalHearing etICHearingNotListedListForFinalHearing;
    @JsonProperty("etICHearingNotListedUDLHearing")
    @CCD(
            label = "UDL hearing",
            typeNameOverride = "etICHearingNotListedUDLHearing",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "UDL hearing",
            typeNameOverride = "etICHearingNotListedUDLHearing",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private EtIcudlHearing etICHearingNotListedUDLHearing;
    @JsonProperty("etICHearingNotListedAnyOtherDirections")
    @CCD(
            label = " ",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Any other directions",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICHearingNotListedAnyOtherDirections;

    //New fields to replace the hidden "hearing not listed" related fields
    @JsonProperty("etICHearingNotListedListUpdated")
    @CCD(
            label = "Hearing not listed",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICHearingNotListed_v2",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing not listed",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICHearingNotListedUpdated",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> etICHearingNotListedListUpdated;
    @JsonProperty("etICHearingNotListedListForPrelimHearingUpdated")
    @CCD(
            label = "List for preliminary hearing",
            typeNameOverride = "etICHearingNotListedListForPrelimHearingUpdated",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "List for preliminary hearing",
            typeNameOverride = "etICHearingNotListedListForPrelimHearingUpdated",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private EtICListForPreliminaryHearingUpdated etICHearingNotListedListForPrelimHearingUpdated;
    @JsonProperty("etICHearingNotListedListForFinalHearingUpdated")
    @CCD(
            label = "List for final hearing",
            typeNameOverride = "etICHearingNotListedListForFinalHearingUpdated",
            searchable = false,
            access = SingleAccess.Access152.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "List for final hearing",
            typeNameOverride = "etICHearingNotListedListForFinalHearingUpdated",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private EtICListForFinalHearingUpdated etICHearingNotListedListForFinalHearingUpdated;
    @JsonProperty("etICHearingNotListedDoNotListHearingDirections")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String etICHearingNotListedDoNotListHearingDirections;
    @JsonProperty("etICHearingNotListedOtherDirections")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String etICHearingNotListedOtherDirections;
    // ET Initial Consideration - Hearing already listed

    @JsonProperty("etICHearingListedAnswers")
    @CCD(
            label = "Hearing already listed",
            typeNameOverride = "etICHearingListedAnswers",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing already listed",
            typeNameOverride = "etICHearingListedAnswers",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private EtICHearingListedAnswers etICHearingListedAnswers;
    @JsonProperty("etICHearingListed")
    @CCD(
            label = "Hearing already listed",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICHearingAlreadyListed",
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> etICHearingListed;
    @JsonProperty("etICExtendDurationGiveDetails")
    @CCD(
            label = "Extend duration of hearing",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICExtendDurationGiveDetails;
    @JsonProperty("etICOtherGiveDetails")
    @CCD(
            label = "Other",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICOtherGiveDetails;
    @JsonProperty("etICHearingAnyOtherDirections")
    @CCD(
            label = "Any other directions",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Any other directions",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICHearingAnyOtherDirections;
    @JsonProperty("etICPostponeGiveDetails")
    @CCD(
            label = "Postpone hearing",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICPostponeGiveDetails;
    @JsonProperty("etICConvertPreliminaryGiveDetails")
    @CCD(
            label = "Convert final hearing to preliminary hearing",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICConvertPreliminaryGiveDetails;
    @JsonProperty("etICConvertF2fGiveDetails")
    @CCD(
            label = "Convert to F2F hearing",
            hint = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICConvertF2fGiveDetails;
    // ET Initial Consideration – Further Info
    @JsonProperty("etICFurtherInformation")
    @CCD(
            label = "Further information",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_furtherInformation",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Further information",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICFurtherInformation",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> etICFurtherInformation;
    @JsonProperty("etICFurtherInfoAnswers")
    @CCD(
            label = "Further information required",
            typeNameOverride = "etICFurtherInfoAnswers",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private EtICFurtherInfoAnswers etICFurtherInfoAnswers;
    @JsonProperty("etICFurtherInformationHearingAnyOtherDirections")
    @CCD(
            label = " ",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Any other directions",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICFurtherInformationHearingAnyOtherDirections;
    @JsonProperty("etICFurtherInformationGiveDetails")
    @CCD(
            label = "Give details to include in the letter",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details to include in the letter",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICFurtherInformationGiveDetails;
    @JsonProperty("etICFurtherInformationTimeToComply")
    @CCD(
            label = "How much time to comply?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How much time to comply?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String etICFurtherInformationTimeToComply;
    @JsonProperty("etInitialConsiderationRule27")
    @CCD(
            label = "Issue Rule 28 Notice and order",
            typeNameOverride = "etInitialConsiderationRule27",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Issue Rule 28 Notice and order",
            typeNameOverride = "etInitialConsiderationRule27",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private EtInitialConsiderationRule27 etInitialConsiderationRule27;
    @JsonProperty("etInitialConsiderationRule28")
    @CCD(
            label = "Issue Rule 29 Notice and order",
            typeNameOverride = "etInitialConsiderationRule28",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Issue Rule 29 Notice and order",
            typeNameOverride = "etInitialConsiderationRule28",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private EtInitialConsiderationRule28 etInitialConsiderationRule28;

    // Initial Consideration Document Collections
    @JsonProperty("icDocumentCollection1")
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> icDocumentCollection1;
    @JsonProperty("icDocumentCollection2")
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> icDocumentCollection2;
    @JsonProperty("icDocumentCollection3")
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> icDocumentCollection3;
    @JsonProperty("icAllDocumentCollection")
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> icAllDocumentCollection;

    // ET3 Response
    @JsonProperty("et3ResponseShowInset")
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseShowInset;
    // ET3 Response - Claimant name page (3)
    @JsonProperty("et3ResponseClaimantName")
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseClaimantName;
    @JsonProperty("et3ResponseIsClaimantNameCorrect")
    @CCD(
            label = "Is this the correct claimant for the claim you're responding to?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this the correct claimant for the claim you're responding to?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseIsClaimantNameCorrect;
    @JsonProperty("et3ResponseClaimantNameCorrection")
    @CCD(
            label = "What is the correct name of the claimant?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is the correct name of the claimant?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseClaimantNameCorrection;
    // ET3 Response - What is the respondent's name (4)
    @JsonProperty("et3ResponseNameShowInset")
    @CCD(
            label = "hidden",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "hidden",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseNameShowInset;
    @JsonProperty("et3ResponseRespondentLegalName")
    @CCD(
            label = "Enter the respondent's registered or legal name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the respondent's registered or legal name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseRespondentLegalName;
    @JsonProperty("et3ResponseRespondentCompanyNumber")
    @CCD(
            label = "Enter the company number if applicable",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the company number if applicable",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseRespondentCompanyNumber;
    @JsonProperty("et3ResponseRespondentEmployerType")
    @CCD(
            label = "What type of employer is the respondent?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_employer_type",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What type of employer is the respondent?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_employer_type",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseRespondentEmployerType;
    @JsonProperty("et3ResponseRespondentPreferredTitle")
    @CCD(
            label = "If individual, what is their preferred title?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "If individual, what is their preferred title?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseRespondentPreferredTitle;
    @JsonProperty("et3ResponseRespondentContactName")
    @CCD(
            label = "Name of contact at respondent's address if not you as the representative",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Name of contact at respondent's address if not you as the representative",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseRespondentContactName;
    // ET3 Response - Respondent address (5)
    @JsonProperty("et3RespondentAddress")
    @CCD(
            label = "Enter a UK postcode",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter a UK postcode",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Address et3RespondentAddress;
    @JsonProperty("et3ResponseDXAddress")
    @CCD(
            label = "DX address (if known)",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "DX address (if known)",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseDXAddress;
    // ET3 Response - Representative Contact method, page (6)
    @JsonProperty("et3ResponseContactPreference")
    @CCD(
            label = "How would you prefer to be contacted?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_contact_preference",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How would you prefer to be contacted?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_contact_preference",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseContactPreference;
    @JsonProperty("et3ResponseContactReason")
    @CCD(
            label = "Provide a reason why you have selected post",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Provide a reason why you have selected post",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseContactReason;
    // ET3 Response - Representative Contact language, page (6)
    @JsonProperty("et3ResponseContactLanguage")
    @CCD(
            label = "What language do you want us to use when we contact you?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_languages",
            searchable = false,
            access = SingleAccess.Access012.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What language do you want us to use when we contact you?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_languages",
            searchable = false,
            access = SingleAccess.Access012.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseContactLanguage;
    // ET3 Response - Representative Phone number page (6)
    @JsonProperty("et3ResponsePhone")
    @CCD(
            label = "What is your contact phone number?",
            hint = "Where we can contact you during the day. For international numbers include the country code.",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is your contact phone number?",
            hint = "Where we can contact you during the day. For international numbers include the country code.",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponsePhone;
    // ET3 Response - Representative reference number (6)
    @JsonProperty("et3ResponseAddress")
    @CCD(
            label = "Contact address if different from registered address",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access012.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Contact address if different from registered address",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access012.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Address et3ResponseAddress;
    // ET3 Response - Representative reference number (6)
    @JsonProperty("et3ResponseReference")
    @CCD(
            label = "What is your reference number?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is your reference number?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseReference;
    // ET3 Response - Hearing format page (9)
    @JsonProperty("et3ResponseHearingRepresentative")
    @CCD(
            label = "Which types of hearing can you, as the representative, attend?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et3_hearing_type",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Which types of hearing can you, as the representative, attend?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et3_hearing_type",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> et3ResponseHearingRepresentative;
    @JsonProperty("et3ResponseHearingRespondent")
    @CCD(
            label = "Which types of hearing can the respondent attend?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et3_hearing_type",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Which types of hearing can the respondent attend?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et3_hearing_type",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> et3ResponseHearingRespondent;
    // ET3 Response - Respondent's workforce page (10)
    @JsonProperty("et3ResponseEmploymentCount")
    @CCD(
            label = "How many people does the respondent employ in Great Britain?",
            hint = "It can help the tribunal to have an indication of the employer's size.\n\nEnter a rough amount in digits or leave blank if you're not sure",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How many people does the respondent employ in Great Britain?",
            hint = "It can help the tribunal to have an indication of the employer's size.\n\nEnter a rough amount in digits or leave blank if you're not sure",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseEmploymentCount;
    @JsonProperty("et3ResponseMultipleSites")
    @CCD(
            label = "Does the respondent have more than one site in Great Britain?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Does the respondent have more than one site in Great Britain?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseMultipleSites;
    @JsonProperty("et3ResponseSiteEmploymentCount")
    @CCD(
            label = "How many people are employed at the place where the claimant worked?",
            hint = "Enter a rough amount in digits or leave blank if you're not sure",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How many people are employed at the place where the claimant worked?",
            hint = "Enter a rough amount in digits or leave blank if you're not sure",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseSiteEmploymentCount;
    // ET3 Response - Acas page (11)
    @JsonProperty("et3ResponseAcasAgree")
    @CCD(
            label = "Do you agree with the details given by the claimant about early conciliation with Acas?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you agree with the details given by the claimant about early conciliation with Acas?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseAcasAgree;
    @JsonProperty("et3ResponseAcasAgreeReason")
    @CCD(
            label = "Why do you disagree with the Acas conciliation details given?",
            hint = "For example, you may consider that the claimant gave an incorrect Acas early conciliation number of that they were wrong to say they were exempt from early conciliation.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Why do you disagree with the Acas conciliation details given?",
            hint = "For example, you may consider that the claimant gave an incorrect Acas early conciliation number of that they were wrong to say they were exempt from early conciliation.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseAcasAgreeReason;
    // ET3 Response - Are the employment dates correct page (12)
    @JsonProperty("et3ResponseAreDatesCorrect")
    @CCD(
            label = "Are the dates of employment given by the claimant correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Are the dates of employment given by the claimant correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseAreDatesCorrect;
    // ET3 Response - Employment dates page (13)
    @JsonProperty("et3ResponseEmploymentStartDate")
    @CCD(
            label = "Enter the employment start date",
            hint = "For example, 12 11 2020",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the employment start date",
            hint = "For example, 12 11 2020",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseEmploymentStartDate;
    @JsonProperty("et3ResponseEmploymentEndDate")
    @CCD(
            label = "Enter employment end date",
            hint = "For example, 12 11 2020",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter employment end date",
            hint = "For example, 12 11 2020",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseEmploymentEndDate;
    @JsonProperty("et3ResponseEmploymentInformation")
    @CCD(
            label = "Do you want to provide any further information about the claimant's employment dates?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to provide any further information about the claimant's employment dates?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseEmploymentInformation;
    // ET3 Response - Is employment continuing page (14)
    @JsonProperty("et3ResponseContinuingEmployment")
    @CCD(
            label = "Is the claimant's employment with the respondent continuing?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is the claimant's employment with the respondent continuing?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseContinuingEmployment;
    // ET3 Response - Is claimant job title/description correct? (15)
    @JsonProperty("et3ResponseIsJobTitleCorrect")
    @CCD(
            label = "Is the claimant's description of their job or job title correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is the claimant's description of their job or job title correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseIsJobTitleCorrect;
    @JsonProperty("et3ResponseCorrectJobTitle")
    @CCD(
            label = "What is or was the claimant's correct job title?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is or was the claimant's correct job title?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseCorrectJobTitle;
    // ET3 Response - Claimant total weekly work hours (16)
    @JsonProperty("et3ResponseClaimantWeeklyHours")
    @CCD(
            label = "Are the claimant's total weekly work hours correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Are the claimant's total weekly work hours correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseClaimantWeeklyHours;
    @JsonProperty("et3ResponseClaimantCorrectHours")
    @CCD(
            label = "What are the claimant's correct total weekly work hours?",
            hint = "Enter their hours work per week",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What are the claimant's correct total weekly work hours?",
            hint = "Enter their hours work per week",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseClaimantCorrectHours;
    // ET3 Response - Earning details (17)
    @JsonProperty("et3ResponseEarningDetailsCorrect")
    @CCD(
            label = "Are the earnings details given by the claimant correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Are the earnings details given by the claimant correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseEarningDetailsCorrect;
    // ET3 Response - Correct pay details (18)
    @JsonProperty("et3ResponsePayFrequency")
    @CCD(
            label = "How often was the claimant paid?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_et3_pay_frequency",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How often was the claimant paid?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_et3_pay_frequency",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponsePayFrequency;
    @JsonProperty("et3ResponsePayBeforeTax")
    @CCD(
            label = "Enter the claimant's pay BEFORE tax",
            hint = "Include overtime, commission and bonuses.",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the claimant's pay BEFORE tax",
            hint = "Include overtime, commission and bonuses.",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponsePayBeforeTax;
    @JsonProperty("et3ResponsePayTakehome")
    @CCD(
            label = "Enter the claimant's normal take-home pay",
            hint = "Take-home pay is the pay after tax and insurance deductions\n\nInclude overtime, commission and bonuses.",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the claimant's normal take-home pay",
            hint = "Take-home pay is the pay after tax and insurance deductions\n\nInclude overtime, commission and bonuses.",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponsePayTakehome;
    // ET3 Response - Notice given (19)
    @JsonProperty("et3ResponseIsNoticeCorrect")
    @CCD(
            label = "Is the information given by the claimant correct about their notice?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is the information given by the claimant correct about their notice?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseIsNoticeCorrect;
    @JsonProperty("et3ResponseCorrectNoticeDetails")
    @CCD(
            label = "What are the claimant's correct notice details?",
            hint = "For example, you may wish to clarify that the claimant was dismissed without notice or paid them a sum in lieu of notice.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What are the claimant's correct notice details?",
            hint = "For example, you may wish to clarify that the claimant was dismissed without notice or paid them a sum in lieu of notice.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseCorrectNoticeDetails;
    // ET3 Response - pension details (20)
    @JsonProperty("et3ResponseIsPensionCorrect")
    @CCD(
            label = "Are the details about pension and other benefits correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Are the details about pension and other benefits correct?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseIsPensionCorrect;
    @JsonProperty("et3ResponsePensionCorrectDetails")
    @CCD(
            label = "What are the correct pension and benefit details?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What are the correct pension and benefit details?",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponsePensionCorrectDetails;
    // ET3 Response - contest claim (21)
    @JsonProperty("et3ResponseRespondentContestClaim")
    @CCD(
            label = "Does the respondent contest the claim?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_contest_claim",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Does the respondent contest the claim?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_contest_claim",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseRespondentContestClaim;
    // ET3 Response - explain contest claim (22)
    @JsonProperty("et3ResponseContestClaimDocument")
    @CCD(
            label = "Upload a document to your response",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            max = 1,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload a document to your response",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            max = 1,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> et3ResponseContestClaimDocument;
    @JsonProperty("et3ResponseContestClaimDetails")
    @CCD(
            label = "Use this text box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this text box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseContestClaimDetails;
    // ET3 Response - employer claim (23)
    @JsonProperty("et3ResponseEmployerClaim")
    @CCD(
            label = "Does the respondent wish to make an Employer's Contract Claim?",
            hint = "A respondent can make this claim against the claimant if the claimant breached their employment terms which resulted in financial loss. This typically happens when a claimant has made a notice pay claim.",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Does the respondent wish to make an Employer's Contract Claim?",
            hint = "A respondent can make this claim against the claimant if the claimant breached their employment terms which resulted in financial loss. This typically happens when a claimant has made a notice pay claim.",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseEmployerClaim;
    // ET3 Response - explain employer claim (24)
    @JsonProperty("et3ResponseEmployerClaimDetails")
    @CCD(
            label = "Provide the background and details of your Employer's Contract Claim",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Provide the background and details of your Employer's Contract Claim",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseEmployerClaimDetails;
    @JsonProperty("et3ResponseEmployerClaimDocument")
    @CCD(
            label = "Add a document",
            hint = "Files should be a maximum of 100MB in size.",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            categoryID = "C2",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Add a document",
            hint = "Files should be a maximum of 100MB in size.",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            categoryID = "C2",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType et3ResponseEmployerClaimDocument;
    // ET3 Response - health conditions (25)
    @JsonProperty("et3ResponseRespondentSupportNeeded")
    @CCD(
            label = "In the respondent party - are you aware of any physical, mental or learning disability or health conditions which requires support?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_yes_no_not_sure_yet",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "In the respondent party - are you aware of any physical, mental or learning disability or health conditions which requires support?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_yes_no_not_sure_yet",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseRespondentSupportNeeded;
    // ET3 Response - Details on health conditions (26)
    @JsonProperty("et3ResponseRespondentSupportDetails")
    @CCD(
            label = "Use this text box or upload the requirements in a document",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this text box or upload the requirements in a document",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ResponseRespondentSupportDetails;
    @JsonProperty("et3ResponseRespondentSupportDocument")
    @CCD(
            label = "Add document",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            categoryID = "C2",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Add document",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            categoryID = "C2",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType et3ResponseRespondentSupportDocument;

    // ET3 Notification
    @JsonProperty("et3NotificationDocCollection")
    @CCD(
            label = "Upload document PDF",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Et3NotificationDocUpload",
            access = SingleAccess.Access064.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document PDF",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Et3NotificationDocUpload",
            searchable = false,
            access = SingleAccess.Access097.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> et3NotificationDocCollection;
    @JsonProperty("et3OtherTypeDocumentName")
    @CCD(
            label = "Serving document other type name placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Serving document other type name placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access086.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3OtherTypeDocumentName;
    @JsonProperty("et3NotificationDocRecipient")
    @CCD(
            label = " ",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_ServingDocumentRecipient",
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_ServingDocumentRecipient",
            searchable = false,
            access = SingleAccess.Access100.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> et3NotificationDocRecipient;
    @JsonProperty("et3ClaimantAndRespondentAddresses")
    @CCD(
            label = "Deprecated",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access078.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Deprecated",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access110.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3ClaimantAndRespondentAddresses;
    @JsonProperty("et3EmailLinkToAcas")
    @CCD(
            label = "Email link to Acas placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Email link to Acas placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et3EmailLinkToAcas;

    //Referral
    @JsonProperty("referralCollection")
    @CCD(
            label = "Referrals",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "referralDetails",
            searchable = false,
            access = SingleAccess.Access140.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Referrals",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "referralDetails",
            searchable = false,
            access = SingleAccess.Access171.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<ReferralTypeItem> referralCollection;
    @JsonProperty("referralHearingDetails")
    @CCD(
            label = "Referral Hearing Details placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access030.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Referral Hearing Details placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String referralHearingDetails;
    @JsonProperty("selectReferral")
    @CCD(
            label = "Select a referral",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select a referral",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType selectReferral;
    @JsonProperty("replyToReferralDcfLink")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String replyToReferralDcfLink;
    //Referral Type
    @JsonProperty("referCaseTo")
    @CCD(
            label = "Who are you referring this case to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            searchable = false,
            access = SingleAccess.Access029.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Who are you referring this case to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            searchable = false,
            access = SingleAccess.Access081.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String referCaseTo;
    @JsonProperty("referentEmail")
    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access030.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String referentEmail;
    @JsonProperty("isUrgent")
    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access029.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access081.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String isUrgent;
    @JsonProperty("referralSubject")
    @CCD(
            label = "What is the referral subject?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ReferralSubject",
            searchable = false,
            access = SingleAccess.Access029.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is the referral subject?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ReferralSubject",
            searchable = false,
            access = SingleAccess.Access081.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String referralSubject;
    @JsonProperty("referralSubjectSpecify")
    @CCD(
            label = "Please specify",
            typeOverride = FieldType.Text,
            searchable = false,
            max = 30,
            access = SingleAccess.Access030.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Please specify",
            typeOverride = FieldType.Text,
            searchable = false,
            max = 30,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String referralSubjectSpecify;
    @JsonProperty("referralDetails")
    @CCD(
            label = "Give details of the referral",
            hint = "Explain what you're referring and why, include answers or directions you need.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access030.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details of the referral",
            hint = "Explain what you're referring and why, include answers or directions you need.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String referralDetails;
    @JsonProperty("referralDocument")
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access036.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access084.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> referralDocument;
    @JsonProperty("referralInstruction")
    @CCD(
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access030.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String referralInstruction;
    @JsonProperty("referredBy")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String referredBy;
    @JsonProperty("referralDate")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String referralDate;

    //Referral Update
    @JsonProperty("updateReferralNumber")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String updateReferralNumber;
    @JsonProperty("updateReferCaseTo")
    @CCD(
            label = "Who are you referring this case to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            searchable = false,
            access = SingleAccess.Access031.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Who are you referring this case to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            searchable = false,
            access = SingleAccess.Access032.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String updateReferCaseTo;
    @JsonProperty("updateReferentEmail")
    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access031.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access032.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String updateReferentEmail;
    @JsonProperty("updateIsUrgent")
    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access031.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access032.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String updateIsUrgent;
    @JsonProperty("updateReferralSubject")
    @CCD(
            label = "What is the referral subject?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ReferralSubject",
            searchable = false,
            access = SingleAccess.Access031.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is the referral subject?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ReferralSubject",
            searchable = false,
            access = SingleAccess.Access032.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String updateReferralSubject;
    @JsonProperty("updateReferralSubjectSpecify")
    @CCD(
            label = "Please specify",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access031.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Please specify",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access032.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String updateReferralSubjectSpecify;
    @JsonProperty("updateReferralDetails")
    @CCD(
            label = "Give details of the referral",
            hint = "Explain what you're referring and why, include answers or directions you need.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access031.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details of the referral",
            hint = "Explain what you're referring and why, include answers or directions you need.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access032.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String updateReferralDetails;
    @JsonProperty("updateReferralDocument")
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access037.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access040.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> updateReferralDocument;
    @JsonProperty("updateReferralInstruction")
    @CCD(
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access031.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access032.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String updateReferralInstruction;

    //Referral Reply
    @JsonProperty("hearingAndReferralDetails")
    @CCD(
            label = "Hearing and Referral Details placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing and Referral Details placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingAndReferralDetails;
    @JsonProperty("directionTo")
    @CCD(
            label = "Who are you sending these directions to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Who are you sending these directions to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String directionTo;
    @JsonProperty("replyToEmailAddress")
    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String replyToEmailAddress;
    @JsonProperty("isUrgentReply")
    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String isUrgentReply;
    @JsonProperty("directionDetails")
    @CCD(
            label = "What are your directions?",
            hint = "Give details of your reply to the referral and any issues you've identified",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What are your directions?",
            hint = "Give details of your reply to the referral and any issues you've identified",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String directionDetails;
    @JsonProperty("replyDocument")
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access063.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access083.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> replyDocument;
    @JsonProperty("replyGeneralNotes")
    @CCD(
            label = "General notes",
            hint = "Give details.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            hint = "Give details.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String replyGeneralNotes;
    @JsonProperty("replyTo")
    @CCD(
            label = "Who are you referring or replying to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Who are you referring or replying to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String replyTo;
    @JsonProperty("replyDetails")
    @CCD(
            label = "Give details of your reply or referral",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details of your reply or referral",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String replyDetails;
    @JsonProperty("isJudge")
    @CCD(
            label = "For checking if user is a judge",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "For checking if user is a judge",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access079.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String isJudge;

    //Close Referral
    @JsonProperty("closeReferralHearingDetails")
    @CCD(
            label = "Hearing and Referral Details placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Hearing and Referral Details placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String closeReferralHearingDetails;
    @JsonProperty("confirmCloseReferral")
    @CCD(
            label = "Do you want to close this referral?",
            hint = "All directions must be completed before closing referrals.",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_confirmCloseReferral",
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to close this referral?",
            hint = "All directions must be completed before closing referrals.",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_confirmCloseReferral",
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> confirmCloseReferral;
    @JsonProperty("closeReferralGeneralNotes")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access062.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access082.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String closeReferralGeneralNotes;

    // Upload Documents Rejection
    @JsonProperty("caseRejectedEmailSent")
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String caseRejectedEmailSent;

    // Respondent Organisation Policies
    @JsonProperty("respondentOrganisationPolicy0")
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy respondentOrganisationPolicy0;
    @JsonProperty("respondentOrganisationPolicy1")
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy respondentOrganisationPolicy1;
    @JsonProperty("respondentOrganisationPolicy2")
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy respondentOrganisationPolicy2;
    @JsonProperty("respondentOrganisationPolicy3")
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy respondentOrganisationPolicy3;
    @JsonProperty("respondentOrganisationPolicy4")
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy respondentOrganisationPolicy4;
    @JsonProperty("respondentOrganisationPolicy5")
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy respondentOrganisationPolicy5;
    @JsonProperty("respondentOrganisationPolicy6")
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy respondentOrganisationPolicy6;
    @JsonProperty("respondentOrganisationPolicy7")
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy respondentOrganisationPolicy7;
    @JsonProperty("respondentOrganisationPolicy8")
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy respondentOrganisationPolicy8;
    @JsonProperty("respondentOrganisationPolicy9")
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "respondent's legal representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access025.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy respondentOrganisationPolicy9;
    @JsonProperty("suggestedHearingVenues")
    @CCD(
            label = "Suggested hearing venue",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Suggested hearing venue",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType suggestedHearingVenues;
    @JsonProperty("listedDateInPastWarning")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String listedDateInPastWarning;
    @JsonProperty("noticeOfChangeAnswers0")
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access016.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access026.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NoticeOfChangeAnswers noticeOfChangeAnswers0;
    @JsonProperty("noticeOfChangeAnswers1")
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access016.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access026.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NoticeOfChangeAnswers noticeOfChangeAnswers1;
    @JsonProperty("noticeOfChangeAnswers2")
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access016.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access026.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NoticeOfChangeAnswers noticeOfChangeAnswers2;
    @JsonProperty("noticeOfChangeAnswers3")
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access016.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access026.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NoticeOfChangeAnswers noticeOfChangeAnswers3;
    @JsonProperty("noticeOfChangeAnswers4")
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access016.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access026.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NoticeOfChangeAnswers noticeOfChangeAnswers4;
    @JsonProperty("noticeOfChangeAnswers5")
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access016.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access026.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NoticeOfChangeAnswers noticeOfChangeAnswers5;
    @JsonProperty("noticeOfChangeAnswers6")
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access016.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access026.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NoticeOfChangeAnswers noticeOfChangeAnswers6;
    @JsonProperty("noticeOfChangeAnswers7")
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access016.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access026.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NoticeOfChangeAnswers noticeOfChangeAnswers7;
    @JsonProperty("noticeOfChangeAnswers8")
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access016.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access026.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NoticeOfChangeAnswers noticeOfChangeAnswers8;
    @JsonProperty("noticeOfChangeAnswers9")
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access016.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "NoticeOfChangeAnswers",
            includeSearchable = true,
            access = SingleAccess.Access026.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NoticeOfChangeAnswers noticeOfChangeAnswers9;
    @JsonProperty("changeOrganisationRequestField")
    @CCD(
            label = "Change Organisation Request",
            typeNameOverride = "ChangeOrganisationRequest",
            securityClassification = "PUBLIC",
            access = SingleAccess.Access024.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Change Organisation Request",
            typeNameOverride = "ChangeOrganisationRequest",
            securityClassification = "PUBLIC",
            access = SingleAccess.Access024.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private ChangeOrganisationRequest changeOrganisationRequestField;

    // Claimant TSE
    @JsonProperty("claimantTse")
    @CCD(
            label = " ",
            typeNameOverride = "claimantTse",
            searchable = false,
            access = SingleAccess.Access199.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "claimantTse",
            searchable = false,
            access = SingleAccess.Access199.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private ClaimantTse claimantTse;

    // Respondent TSE
    @CCD(
            label = " ",
            typeNameOverride = "respondentTse",
            searchable = false,
            access = SingleAccess.Access199.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "respondentTse",
            searchable = false,
            access = SingleAccess.Access199.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private RespondentTse respondentTse;

    //Respondent Tell Something Else
    @JsonProperty("resTseNotAvailableWarning")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access060.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access079.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseNotAvailableWarning;
    @JsonProperty("tseRespondNotAvailableWarning")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access060.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access079.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseRespondNotAvailableWarning;
    @JsonProperty("respondToTribunalNotAvailableWarning")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access060.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access079.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondToTribunalNotAvailableWarning;
    @JsonProperty("resTseSelectApplication")
    @CCD(
            label = "Select an application",
            hint = "For multiple actions you can complete each one separately and explain in the text box that they are linked. Or use Contact the tribunal.",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_resTseSelectApp",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select an application",
            hint = "For multiple actions you can complete each one separately and explain in the text box that they are linked. Or use Contact the tribunal.",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_resTseSelectApp",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseSelectApplication;
    @JsonProperty("resTseVariableContent")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String resTseVariableContent;
    @JsonProperty("resTseDocument1")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument1;
    @JsonProperty("resTseTextBox1")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox1;
    @JsonProperty("resTseDocument2")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument2;
    @JsonProperty("resTseTextBox2")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox2;
    @JsonProperty("resTseDocument3")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument3;
    @JsonProperty("resTseTextBox3")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox3;
    @JsonProperty("resTseDocument4")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument4;
    @JsonProperty("resTseTextBox4")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox4;
    @JsonProperty("resTseDocument5")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument5;
    @JsonProperty("resTseTextBox5")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox5;
    @JsonProperty("resTseDocument6")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument6;
    @JsonProperty("resTseTextBox6")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox6;
    @JsonProperty("resTseDocument7")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument7;
    @JsonProperty("resTseTextBox7")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox7;
    @JsonProperty("resTseDocument8")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument8;
    @JsonProperty("resTseTextBox8")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox8;
    @JsonProperty("resTseDocument9")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument9;
    @JsonProperty("resTseTextBox9")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox9;
    @JsonProperty("resTseDocument10")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument10;
    @JsonProperty("resTseTextBox10")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox10;
    @JsonProperty("resTseDocument11")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument11;
    @JsonProperty("resTseTextBox11")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox11;
    @JsonProperty("resTseDocument12")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType resTseDocument12;
    @JsonProperty("resTseTextBox12")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTextBox12;
    @JsonProperty("resTseCopyToOtherPartyYesOrNo")
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access112.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access112.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseCopyToOtherPartyYesOrNo;
    @JsonProperty("resTseCopyToOtherPartyTextArea")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseCopyToOtherPartyTextArea;
    @JsonProperty("genericTseApplicationCollection")
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "genericTseDetails",
            access = SingleAccess.Access009.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "genericTseDetails",
            access = SingleAccess.Access010.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTseApplicationTypeItem> genericTseApplicationCollection;
    @JsonProperty("tseApplicationStoredCollection")
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "genericTseDetails",
            searchable = false,
            access = SingleAccess.Access015.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "genericTseDetails",
            searchable = false,
            access = SingleAccess.Access015.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTseApplicationTypeItem> tseApplicationStoredCollection;
    @JsonProperty("tseRespondentStoredCollection")
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "genericTseDetails",
            searchable = false,
            access = SingleAccess.Access018.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "genericTseDetails",
            searchable = false,
            access = SingleAccess.Access018.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTseApplicationTypeItem> tseRespondentStoredCollection;

    // Claimant tell something else
    @JsonProperty("claimantTseSelectApplication")
    @CCD(
            label = "Select an application",
            hint = "For multiple actions you can complete each one separately and explain in the text box that they are linked. Or use Contact the tribunal.",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_claimantTseSelectApp",
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select an application",
            hint = "For multiple actions you can complete each one separately and explain in the text box that they are linked. Or use Contact the tribunal.",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_claimantTseSelectApp",
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseSelectApplication;
    @JsonProperty("claimantTseRule92")
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantTseCopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantTseCopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseRule92;
    @JsonProperty("claimantTseRespNotAvailable")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseRespNotAvailable;
    @JsonProperty("claimantTseDocument1")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument1;
    @JsonProperty("claimantTseTextBox1")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox1;
    @JsonProperty("claimantTseDocument2")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument2;
    @JsonProperty("claimantTseTextBox2")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox2;
    @JsonProperty("claimantTseDocument3")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument3;
    @JsonProperty("claimantTseTextBox3")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox3;
    @JsonProperty("claimantTseDocument4")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument4;
    @JsonProperty("claimantTseTextBox4")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox4;
    @JsonProperty("claimantTseDocument5")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument5;
    @JsonProperty("claimantTseTextBox5")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox5;
    @JsonProperty("claimantTseDocument6")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument6;
    @JsonProperty("claimantTseTextBox6")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox6;
    @JsonProperty("claimantTseDocument7")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument7;
    @JsonProperty("claimantTseTextBox7")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox7;
    @JsonProperty("claimantTseDocument8")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument8;
    @JsonProperty("claimantTseTextBox8")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox8;
    @JsonProperty("claimantTseDocument9")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument9;
    @JsonProperty("claimantTseTextBox9")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox9;
    @JsonProperty("claimantTseDocument10")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument10;
    @JsonProperty("claimantTseTextBox10")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox10;
    @JsonProperty("claimantTseDocument11")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument11;
    @JsonProperty("claimantTseTextBox11")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox11;
    @JsonProperty("claimantTseDocument12")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument12;
    @JsonProperty("claimantTseTextBox12")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox12;
    @JsonProperty("claimantTseDocument13")
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Document",
            typeOverride = FieldType.Document,
            categoryID = "C4",
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType claimantTseDocument13;
    @JsonProperty("claimantTseTextBox13")
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Use this box for any accompanying information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTextBox13;
    @JsonProperty("claimantTseRule92AnsNoGiveDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseRule92AnsNoGiveDetails;
    @JsonProperty("claimantTseTableMarkUp")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access214.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access214.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantTseTableMarkUp;

    //TSE Admin Record a Decision
    @JsonProperty("tseAdminSelectApplication")
    @CCD(
            label = "Select an application",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select an application",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType tseAdminSelectApplication;
    @JsonProperty("tseAdminTableMarkUp")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminTableMarkUp;
    @JsonProperty("tseAdminEnterNotificationTitle")
    @CCD(
            label = "Enter notification title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing.",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter notification title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing.",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminEnterNotificationTitle;
    @JsonProperty("tseAdminDecision")
    @CCD(
            label = "Decision",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminDecision",
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Decision",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminDecision",
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminDecision;
    @JsonProperty("tseAdminDecisionDetails")
    @CCD(
            label = "Decision details",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Decision details",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminDecisionDetails;
    @JsonProperty("tseAdminTypeOfDecision")
    @CCD(
            label = "Type of decision",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminTypeOfDecision",
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Type of decision",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminTypeOfDecision",
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminTypeOfDecision;
    @JsonProperty("tseAdminIsResponseRequired")
    @CCD(
            label = "Is a response to the tribunal required?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminIsResponseRequired",
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is a response to the tribunal required?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminIsResponseRequired",
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminIsResponseRequired;
    @JsonProperty("tseAdminSelectPartyRespond")
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantRespondentBothParties",
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantRespondentBothParties",
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminSelectPartyRespond;
    @JsonProperty("tseAdminAdditionalInformation")
    @CCD(
            label = "Additional information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Additional information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminAdditionalInformation;
    @JsonProperty("tseAdminResponseRequiredYesDoc")
    @CCD(
            label = "Supporting material",
            hint = "Upload a document to the system",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            minValue = "1",
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Supporting material",
            hint = "Upload a document to the system",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            minValue = "1",
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<DocumentType>> tseAdminResponseRequiredYesDoc;
    @JsonProperty("tseAdminResponseRequiredNoDoc")
    @CCD(
            label = "Supporting material",
            hint = "Upload a document to the system",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Supporting material",
            hint = "Upload a document to the system",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<DocumentType>> tseAdminResponseRequiredNoDoc;
    @JsonProperty("tseAdminDecisionMadeBy")
    @CCD(
            label = "Decision was made by",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminDecisionMadeBy",
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Decision was made by",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminDecisionMadeBy",
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminDecisionMadeBy;
    @JsonProperty("tseAdminDecisionMadeByFullName")
    @CCD(
            label = "Full name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter their full name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminDecisionMadeByFullName;
    @JsonProperty("tseAdminSelectPartyNotify")
    @CCD(
            label = "Select the party or parties to notify",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminSelectPartyNotify",
            searchable = false,
            access = SingleAccess.Access128.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the party or parties to notify",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminSelectPartyNotify",
            searchable = false,
            access = SingleAccess.Access131.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminSelectPartyNotify;

    //TSE Response
    @JsonProperty("tseRespondSelectApplication")
    @CCD(
            label = "Select an application",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select an application",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType tseRespondSelectApplication;
    @JsonProperty("tseResponseIntro")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseResponseIntro;
    @JsonProperty("tseResponseTable")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseResponseTable;
    @JsonProperty("tseResponseText")
    @CCD(
            label = "What's your response to the application?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access112.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What's your response to the application?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access112.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseResponseText;
    @JsonProperty("tseResponseHasSupportingMaterial")
    @CCD(
            label = "Supporting material",
            hint = "Do you have any supporting material you want to provide?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Supporting material",
            hint = "Do you have any supporting material you want to provide?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseResponseHasSupportingMaterial;
    @JsonProperty("tseResponseSupportingMaterial")
    @CCD(
            label = "Upload a document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            minValue = "1",
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload a document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            minValue = "1",
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<DocumentType>> tseResponseSupportingMaterial;
    @JsonProperty("tseResponseCopyToOtherParty")
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseResponseCopyToOtherParty;
    @JsonProperty("tseResponseCopyNoGiveDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseResponseCopyNoGiveDetails;
    @JsonProperty("resTseTableMarkUp")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String resTseTableMarkUp;
    //  if Respondent is responding to Tribunal
    @JsonProperty("tseRespondingToTribunal")
    @CCD(
            label = "Respondent is responding to Tribunal request/order",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access112.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondent is responding to Tribunal request/order",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access112.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseRespondingToTribunal;
    @JsonProperty("tseRespondingToTribunalText")
    @CCD(
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access112.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access112.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseRespondingToTribunalText;

    // Claimant Representative Response
    @JsonProperty("claimantRepRespondSelectApplication")
    @CCD(
            label = "Select an application",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select an application",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType claimantRepRespondSelectApplication;
    @JsonProperty("claimantRepResponseIntro")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantRepResponseIntro;
    @JsonProperty("claimantRepResponseTable")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantRepResponseTable;
    @JsonProperty("claimantRepResponseText")
    @CCD(
            label = "What's your response to the application?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What's your response to the application?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantRepResponseText;
    @JsonProperty("claimantRepResponseHasSupportingMaterial")
    @CCD(
            label = "Supporting material",
            hint = "Do you have any supporting material you want to provide?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Supporting material",
            hint = "Do you have any supporting material you want to provide?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantRepResponseHasSupportingMaterial;
    @JsonProperty("claimantRepResSupportingMaterial")
    @CCD(
            label = "Upload a document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            minValue = "1",
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload a document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            minValue = "1",
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<DocumentType>> claimantRepResSupportingMaterial;
    @JsonProperty("claimantRepResponseCopyToOtherParty")
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantRepResponseCopyToOtherParty;
    @JsonProperty("claimantRepResponseCopyNoGiveDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantRepResponseCopyNoGiveDetails;
    @JsonProperty("resClaimantRepTableMarkUp")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String resClaimantRepTableMarkUp;
    // if Claimant Representative is responding to Tribunal
    @JsonProperty("claimantRepResToTribunal")
    @CCD(
            id = "claimantRepResToTribunal",
            label = "Claimant Representative is responding to Tribunal request/order",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantRepResToTribunal",
            label = "Claimant Representative is responding to Tribunal request/order",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantRepRespondingToTribunal;
    @JsonProperty("claimantRepResToTribunalText")
    @CCD(
            id = "claimantRepResToTribunalText",
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantRepResToTribunalText",
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            max = 2500,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantRepRespondingToTribunalText;

    //TSE Admin Respond to an application
    @JsonProperty("tseAdmReplyTableMarkUp")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access136.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access166.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyTableMarkUp;
    @JsonProperty("tseAdmReplyEnterResponseTitle")
    @CCD(
            label = "Enter response title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing.",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter response title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing.",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyEnterResponseTitle;
    @JsonProperty("tseAdmReplyAdditionalInformation")
    @CCD(
            label = "Additional information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Additional information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyAdditionalInformation;
    @JsonProperty("tseAdmReplyAddDocument")
    @CCD(
            label = "Add document",
            hint = "Upload a document to the system",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access136.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Add document",
            hint = "Upload a document to the system",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access166.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<DocumentType>> tseAdmReplyAddDocument;
    @JsonProperty("tseAdmReplyIsCmoOrRequest")
    @CCD(
            label = "Is this a case management order or request?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdmReplyIsCmoOrRequest",
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this a case management order or request?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdmReplyIsCmoOrRequest",
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyIsCmoOrRequest;
    @JsonProperty("tseAdmReplyCmoMadeBy")
    @CCD(
            label = "Case management order made by",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_tseAdmReplyCmoMadeBy",
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Case management order made by",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_tseAdmReplyCmoMadeBy",
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyCmoMadeBy;
    @JsonProperty("tseAdmReplyRequestMadeBy")
    @CCD(
            label = "Request made by",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_tseAdmReplyRequestMadeBy",
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Request made by",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_tseAdmReplyRequestMadeBy",
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyRequestMadeBy;
    @JsonProperty("tseAdmReplyCmoEnterFullName")
    @CCD(
            label = "Enter their full name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter their full name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyCmoEnterFullName;
    @JsonProperty("tseAdmReplyCmoIsResponseRequired")
    @CCD(
            label = "Is a response to the tribunal required?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "frl_tseAdminIsResponseRequired",
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is a response to the tribunal required?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "frl_tseAdminIsResponseRequired",
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyCmoIsResponseRequired;
    @JsonProperty("tseAdmReplyRequestEnterFullName")
    @CCD(
            label = "Enter their full name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter their full name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyRequestEnterFullName;
    @JsonProperty("tseAdmReplyRequestIsResponseRequired")
    @CCD(
            label = "Is a response to the tribunal required?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "frl_tseAdminIsResponseRequired",
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is a response to the tribunal required?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "frl_tseAdminIsResponseRequired",
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyRequestIsResponseRequired;
    @JsonProperty("tseAdmReplyRequestSelectPartyRespond")
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantRespondentBothParties",
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantRespondentBothParties",
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyRequestSelectPartyRespond;
    @JsonProperty("tseAdmReplyCmoSelectPartyRespond")
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantRespondentBothParties",
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantRespondentBothParties",
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplyCmoSelectPartyRespond;
    @JsonProperty("tseAdmReplySelectPartyNotify")
    @CCD(
            label = "Select the party or parties to notify",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminSelectPartyNotify",
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the party or parties to notify",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminSelectPartyNotify",
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdmReplySelectPartyNotify;

    // TSe Admin Close an application
    @JsonProperty("tseAdminCloseApplicationTable")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminCloseApplicationTable;
    @JsonProperty("tseAdminCloseApplicationText")
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseAdminCloseApplicationText;

    // Tell something else - view an application
    @JsonProperty("tseViewApplicationOpenOrClosed")
    @CCD(
            label = "What application do you wish to view?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseApplicationsOpenOrClosed",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What application do you wish to view?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseApplicationsOpenOrClosed",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseViewApplicationOpenOrClosed;
    @JsonProperty("tseViewApplicationSelect")
    @CCD(
            label = "Select Application",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select Application",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType tseViewApplicationSelect;
    @JsonProperty("tseApplicationSummaryAndResponsesMarkup")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tseApplicationSummaryAndResponsesMarkup;

    // Provide Something Else to tribunal - Respondent - Respond to an order or request from the tribunal
    @JsonProperty("pseRespondentSelectOrderOrRequest")
    @CCD(
            label = "Select an order or request from the tribunal",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select an order or request from the tribunal",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType pseRespondentSelectOrderOrRequest;
    @JsonProperty("pseRespondentOrdReqTableMarkUp")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String pseRespondentOrdReqTableMarkUp;
    @JsonProperty("pseRespondentOrdReqResponseText")
    @CCD(
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            maxValue = "2500",
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            maxValue = "2500",
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String pseRespondentOrdReqResponseText;
    @JsonProperty("pseRespondentOrdReqHasSupportingMaterial")
    @CCD(
            label = "Supporting material",
            hint = "Do you have any supporting material you want to provide?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Supporting material",
            hint = "Do you have any supporting material you want to provide?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String pseRespondentOrdReqHasSupportingMaterial;
    @JsonProperty("pseRespondentOrdReqUploadDocument")
    @CCD(
            label = "Upload a document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            min = 1,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload a document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            min = 1,
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<DocumentType>> pseRespondentOrdReqUploadDocument;
    @JsonProperty("pseRespondentOrdReqCopyToOtherParty")
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String pseRespondentOrdReqCopyToOtherParty;
    @JsonProperty("pseRespondentOrdReqCopyNoGiveDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String pseRespondentOrdReqCopyNoGiveDetails;

    // Provide Something Else to tribunal - Respondent - View a judgment, order or notification
    @JsonProperty("pseRespondentSelectJudgmentOrderNotification")
    @CCD(
            label = "Select a judgment, order or notification",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select a judgment, order or notification",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType pseRespondentSelectJudgmentOrderNotification;

    //sendNotification
    @JsonProperty("sendNotificationCollection")
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "sendNotificationCollection",
            searchable = false,
            access = SingleAccess.Access143.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "sendNotificationCollection",
            access = SingleAccess.Access174.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<SendNotificationTypeItem> sendNotificationCollection;
    @JsonProperty("sendNotificationTitle")
    @CCD(
            label = "Enter notification title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing. Please note the text entered here will be displayed in the notification e-mail sent to the parties.",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter notification title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing. Please note the text entered here will be displayed in the notification e-mail sent to the parties.",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationTitle;
    @JsonProperty("sendNotificationLetter")
    @CCD(
            label = "Is there a letter to send out?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there a letter to send out?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationLetter;
    @JsonProperty("sendNotificationUploadDocument")
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            min = 1,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            min = 1,
            access = SingleAccess.Access094.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> sendNotificationUploadDocument;
    @JsonProperty("sendNotificationSubject")
    @CCD(
            label = "Notification subject",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_sendNotificationSubject",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Notification subject",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_sendNotificationSubject",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> sendNotificationSubject;
    @JsonProperty("sendNotificationAdditionalInfo")
    @CCD(
            label = "Additional information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Additional information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationAdditionalInfo;
    @JsonProperty("sendNotificationNotify")
    @CCD(
            label = "Select the party or parties to notify",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationNotify",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the party or parties to notify",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationNotify",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationNotify;
    @JsonProperty("sendNotificationNotifyLeadCase")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String sendNotificationNotifyLeadCase;
    @JsonProperty("sendNotificationSelectHearing")
    @CCD(
            label = "Select the hearing",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the hearing",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType sendNotificationSelectHearing;
    @JsonProperty("sendNotificationCaseManagement")
    @CCD(
            label = "Is this a case management order or request?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationCaseManagement",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this a case management order or request?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationCaseManagement",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationCaseManagement;
    @JsonProperty("sendNotificationResponseTribunal")
    @CCD(
            label = "Is a response to the tribunal required?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationResponseTribunal",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is a response to the tribunal required?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationResponseTribunal",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationResponseTribunal;
    @JsonProperty("sendNotificationWhoCaseOrder")
    @CCD(
            label = "Who made the case management order?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationWhoCaseOrder",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Who made the case management order?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationWhoCaseOrder",
            searchable = false,
            access = SingleAccess.Access094.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationWhoCaseOrder;
    @JsonProperty("sendNotificationSelectParties")
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_sendNotificationParties",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_sendNotificationParties",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationSelectParties;
    @JsonProperty("sendNotificationFullName")
    @CCD(
            label = "Full name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Full name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationFullName;
    @JsonProperty("sendNotificationFullName2")
    @CCD(
            label = "Full name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Full name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationFullName2;
    @JsonProperty("sendNotificationDetails")
    @CCD(
            label = "Details",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Details",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationDetails;
    @JsonProperty("sendNotificationDecision")
    @CCD(
            label = "Decision",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationDecision",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Decision",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationDecision",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationDecision;
    @JsonProperty("sendNotificationRequestMadeBy")
    @CCD(
            label = "Request was made by",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_sendNotificationRequestMadeBy",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Request was made by",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_sendNotificationRequestMadeBy",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationRequestMadeBy;
    @JsonProperty("sendNotificationEccQuestion")
    @CCD(
            label = "What is the ECC notification?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationEccQuestion",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is the ECC notification?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationEccQuestion",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationEccQuestion;
    @JsonProperty("sendNotificationWhoMadeJudgement")
    @CCD(
            label = "Who made the judgment?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationWhoMadeJudgement",
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Who made the judgment?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationWhoMadeJudgement",
            searchable = false,
            access = SingleAccess.Access094.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String sendNotificationWhoMadeJudgement;
    @JsonProperty("notificationSentFrom")
    @CCD(
            label = "Sent from multiple",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access150.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "Sent from multiple",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access194.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private String notificationSentFrom;

    @JsonProperty("selectNotificationDropdown")
    @CCD(
            label = "Select a notification",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select a notification",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType selectNotificationDropdown;

    @JsonProperty("notificationMarkdown")
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String notificationMarkdown;

    @JsonProperty("pseViewNotifications")
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access086.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String pseViewNotifications;

    @JsonProperty("et3RepresentingRespondent")
    @CCD(
            label = "Select which Respondent this ET3 Form is for",
            hint = "If you wish to select multiple respondents, please click the Add New button",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DynamicListCollection",
            searchable = false,
            access = SingleAccess.Access189.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select which Respondent this ET3 Form is for",
            hint = "If you wish to select multiple respondents, please click the Add New button",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DynamicListCollection",
            searchable = false,
            access = SingleAccess.Access189.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DynamicListTypeItem> et3RepresentingRespondent;

    @JsonProperty("respondNotificationTitle")
    @CCD(
            label = "Enter response title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing.",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter response title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing.",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondNotificationTitle;
    @JsonProperty("respondNotificationAdditionalInfo")
    @CCD(
            label = "Additional information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Additional information",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondNotificationAdditionalInfo;
    @JsonProperty("respondNotificationUploadDocument")
    @CCD(
            label = "Add document",
            hint = "Upload a document to the system",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Add document",
            hint = "Upload a document to the system",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> respondNotificationUploadDocument;
    @JsonProperty("respondNotificationCmoOrRequest")
    @CCD(
            label = "Is this a case management order or request?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationCaseManagement",
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this a case management order or request?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationCaseManagement",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondNotificationCmoOrRequest;
    @JsonProperty("respondNotificationResponseRequired")
    @CCD(
            label = "Is a response to the tribunal required",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = " fl_respondNotificationResponseRequired",
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is a response to the tribunal required",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = " fl_respondNotificationResponseRequired",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondNotificationResponseRequired;
    @JsonProperty("respondNotificationWhoRespond")
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_sendNotificationParties",
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_sendNotificationParties",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondNotificationWhoRespond;
    @JsonProperty("respondNotificationCaseManagementMadeBy")
    @CCD(
            label = "Case management order made by",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_respondNotificationCmoRequestBy",
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Case management order made by",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_respondNotificationCmoRequestBy",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondNotificationCaseManagementMadeBy;
    @JsonProperty("respondNotificationRequestMadeBy")
    @CCD(
            label = "Request made by",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_respondNotificationRequestBy",
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Request made by",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_respondNotificationRequestBy",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondNotificationRequestMadeBy;
    @JsonProperty("respondNotificationFullName")
    @CCD(
            label = "Full name",
            hint = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Full name",
            hint = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondNotificationFullName;
    @JsonProperty("respondNotificationPartyToNotify")
    @CCD(
            label = "Select the party or parties to notify",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationParties",
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the party or parties to notify",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationParties",
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondNotificationPartyToNotify;

    // Bundles Respondent
    @JsonProperty("bundlesRespondentPrepareDocNotesShow")
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Placeholder",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String bundlesRespondentPrepareDocNotesShow;

    @JsonProperty("bundlesRespondentAgreedDocWith")
    @CCD(
            label = "Have you agreed with the other party that this PDF set of documents will be used by both parties at the hearing and that no other documents will be referred to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundlesRespondentAgreedDocWith",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Have you agreed with the other party that this PDF set of documents will be used by both parties at the hearing and that no other documents will be referred to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundlesRespondentAgreedDocWith",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String bundlesRespondentAgreedDocWith;
    @JsonProperty("bundlesRespondentAgreedDocWithBut")
    @CCD(
            label = "Tell us which documents are disputed",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Tell us which documents are disputed",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String bundlesRespondentAgreedDocWithBut;
    @JsonProperty("bundlesRespondentAgreedDocWithNo")
    @CCD(
            label = "Tell us why you’ve not been able to agree with the other party",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Tell us why you’ve not been able to agree with the other party",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String bundlesRespondentAgreedDocWithNo;

    @JsonProperty("bundlesRespondentSelectHearing")
    @CCD(
            label = "Select the hearing these documents are for",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the hearing these documents are for",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType bundlesRespondentSelectHearing;

    @JsonProperty("submitEt3Respondent")
    @CCD(
            label = "Select which respondent this ET3 is for",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access187.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select which respondent this ET3 is for",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access017.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType submitEt3Respondent;

    @JsonProperty("bundlesRespondentWhatDocuments")
    @CCD(
            label = "What are these documents?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundlesWhatDocuments",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What are these documents?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundlesWhatDocuments",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String bundlesRespondentWhatDocuments;

    @JsonProperty("bundlesRespondentWhoseDocuments")
    @CCD(
            label = "Whose hearing documents are you uploading?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundlesWhoseDocuments",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Whose hearing documents are you uploading?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundlesWhoseDocuments",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String bundlesRespondentWhoseDocuments;

    @JsonProperty("bundlesRespondentUploadFile")
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            categoryID = "C57",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            categoryID = "C57",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType bundlesRespondentUploadFile;
    @JsonProperty("bundlesRespondentCollection")
    @CCD(
            label = "Respondent Hearing Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingBundle",
            searchable = false,
            access = SingleAccess.Access019.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondent Hearing Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingBundle",
            searchable = false,
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<HearingBundleType>> bundlesRespondentCollection;

    // Claimant Bundles
    @JsonProperty("bundlesClaimantCollection")
    @CCD(
            label = "Claimant Hearing Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingBundle",
            searchable = false,
            access = SingleAccess.Access019.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant Hearing Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingBundle",
            searchable = false,
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<HearingBundleType>> bundlesClaimantCollection;

    // Remove Hearing Bundle
    @JsonProperty("removedHearingBundlesCollection")
    @CCD(
            label = "Removed Hearing Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RemovedHearingBundle",
            searchable = false,
            access = SingleAccess.Access130.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Removed Hearing Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RemovedHearingBundle",
            searchable = false,
            access = SingleAccess.Access133.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<RemovedHearingBundleItem>> removedHearingBundlesCollection;

    @JsonProperty("removeHearingBundleSelect")
    @CCD(
            label = "Please select the hearing bundle to be removed",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Please select the hearing bundle to be removed",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access184.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType removeHearingBundleSelect;

    @JsonProperty("removeBundleDropDownSelectedParty")
    @CCD(
            label = "Please specify the party whose hearing bundles are to be removed",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_selectHearingBundlesCollection",
            searchable = false,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Please specify the party whose hearing bundles are to be removed",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_selectHearingBundlesCollection",
            searchable = false,
            access = SingleAccess.Access184.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String removeBundleDropDownSelectedParty;

    @JsonProperty("hearingBundleRemoveReason")
    @CCD(
            label = "Reason for removing the hearing bundle",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Reason for removing the hearing bundle",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access184.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hearingBundleRemoveReason;

    @JsonProperty("adrDocumentCollection")
    @CCD(
            label = "Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdrDocumentUploadDetails",
            securityClassification = "PUBLIC",
            searchable = false,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdrDocumentUploadDetails",
            securityClassification = "PUBLIC",
            searchable = false,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> adrDocumentCollection;
    @JsonProperty("piiDocumentCollection")
    @CCD(
            label = "Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PiiDocumentUploadDetails",
            securityClassification = "PUBLIC",
            searchable = false,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PiiDocumentUploadDetails",
            securityClassification = "PUBLIC",
            searchable = false,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> piiDocumentCollection;
    @JsonProperty("appealDocumentCollection")
    @CCD(
            label = "Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AppealDocumentUploadDetails",
            securityClassification = "PUBLIC",
            searchable = false,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AppealDocumentUploadDetails",
            securityClassification = "PUBLIC",
            searchable = false,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<DocumentTypeItem> appealDocumentCollection;

    // Case Flags
    @CCD(
            label = "Case Flags",
            typeOverride = FieldType.Flags,
            access = SingleAccess.Access149.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Case Flags",
            typeOverride = FieldType.Flags,
            access = SingleAccess.Access186.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private CaseFlagsType caseFlags;
    @CCD(
            label = "Claimant Flags",
            typeOverride = FieldType.Flags,
            access = SingleAccess.Access149.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant Flags",
            typeOverride = FieldType.Flags,
            access = SingleAccess.Access186.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private CaseFlagsType claimantFlags;
    @CCD(
            label = "Respondent Flags",
            typeOverride = FieldType.Flags,
            access = SingleAccess.Access149.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondent Flags",
            typeOverride = FieldType.Flags,
            access = SingleAccess.Access186.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private CaseFlagsType respondentFlags;

    //et-hearings-api
    @JsonProperty("autoListFlag")
    @CCD(
            label = "hidden",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access114.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "hidden",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access114.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private String autoListFlag;

    @JsonProperty("caseAdditionalSecurityFlag")
    @CCD(
            label = "text",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "text",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access094.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private String caseAdditionalSecurityFlag;
    @JsonProperty("caseCategories")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private List<CaseCategory> caseCategories;
    @JsonProperty("caseDeepLink")
    @CCD(
            label = "hidden",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access209.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "hidden",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access215.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private String caseDeepLink;

    @JsonProperty("caseInterpreterRequiredFlag")
    @CCD(
            label = "text",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "text",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access094.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private String caseInterpreterRequiredFlag;
    @JsonProperty("caseManagementLocationCode")
    @CCD(
            label = "caseManagementLocationCode",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "caseManagementLocationCode",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access094.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String caseManagementLocationCode;
    @JsonProperty("caseSLAStartDate")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String caseSLAStartDate;
    @JsonProperty("caseRestrictedFlag")
    @CCD(
            label = "hidden",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access114.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "hidden",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access114.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private String caseRestrictedFlag;

    @JsonProperty("duration")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private Integer duration;

    @JsonProperty("externalCaseReference")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String externalCaseReference;

    @JsonProperty("facilitiesRequiredList")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private List<String> facilitiesRequiredList;

    @JsonProperty("hearingChannels")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private List<String> hearingChannels;
    @JsonProperty("hearingInWelshFlag")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingInWelshFlag;
    @JsonProperty("hearingIsLinkedFlag")
    @CCD(
            label = "hidden",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access209.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "hidden",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access215.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private String hearingIsLinkedFlag;
    @JsonProperty("hearingLocations")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private List<HearingLocation> hearingLocations;
    @JsonProperty("hearingPriorityType")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingPriorityType;
    @JsonProperty("hearingRequester")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingRequester;
    @JsonProperty("hearingType")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hearingType;
    @JsonProperty("hearingWindow")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private HearingWindow hearingWindow;
    @JsonProperty("caseNameHmctsInternal")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            includeSearchable = true,
            access = SingleAccess.Access151.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            includeSearchable = true,
            access = SingleAccess.Access195.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String caseNameHmctsInternal;
    @JsonProperty("caseManagementCategory")
    @CCD(
            label = " ",
            typeOverride = FieldType.DynamicList,
            includeSearchable = true,
            access = SingleAccess.Access197.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.DynamicList,
            includeSearchable = true,
            access = SingleAccess.Access196.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType caseManagementCategory;
    @JsonProperty("hmctsServiceID")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String hmctsServiceID;
    @JsonProperty("hmctsCaseCategory")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            includeSearchable = true,
            access = SingleAccess.Access218.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            includeSearchable = true,
            access = SingleAccess.Access218.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String hmctsCaseCategory;

    @JsonProperty("judiciary")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private Judiciary judiciary;
    @JsonProperty("leadJudgeContractType")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String leadJudgeContractType;

    @JsonProperty("listingComments")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String listingComments;

    @JsonProperty("numberOfPhysicalAttendees")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private Integer numberOfPhysicalAttendees;

    @JsonProperty("panelRequirements")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private PanelRequirements panelRequirements;

    @JsonProperty("parties")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private List<PartyDetails> parties;

    @JsonProperty("draftAndSignJudgement")
    @CCD(
            label = "Judgment/Order to be issued",
            typeNameOverride = "DraftAndSignJudgement",
            searchable = false,
            access = SingleAccess.Access162.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Judgment/Order to be issued",
            typeNameOverride = "DraftAndSignJudgement",
            searchable = false,
            access = SingleAccess.Access185.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DraftAndSignJudgement draftAndSignJudgement;

    @JsonProperty("privateHearingRequiredFlag")
    @CCD(
            label = "text",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "text",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access094.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private String privateHearingRequiredFlag;

    @JsonProperty("publicCaseName")
    @CCD(
            label = "Public case name",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "Public case name",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access094.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private String publicCaseName;

    @JsonProperty("screenFlow")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private List<ScreenNavigation> screenFlow;

    @JsonProperty("vocabulary")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private List<Vocabulary> vocabulary;

    @JsonProperty("caseLinks")
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseLink",
            includeSearchable = true,
            access = SingleAccess.Access208.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseLink",
            includeSearchable = true,
            access = SingleAccess.Access126.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseLink",
            includeSearchable = true,
            includeInProfiles = EnglandWalesSingleProdDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseLink",
            includeSearchable = true,
            includeInProfiles = ScotlandSingleProdDefinition.class
    )
    private ListTypeItem<CaseLink> caseLinks;

    @JsonProperty("partySelection")
    @CCD(
            label = "Which parties do you have hearing unavailability dates for?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "frl_partyUnavailability",
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "Which parties do you have hearing unavailability dates for?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "frl_partyUnavailability",
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private List<String> partySelection;

    @JsonProperty("claimantUnavailability")
    @CCD(
            label = "Claimant unavailability date ranges",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UnavailabilityDateRange",
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "Claimant unavailability date ranges",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UnavailabilityDateRange",
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private ListTypeItem<UnavailabilityRanges> claimantUnavailability;

    @JsonProperty("respondentUnavailability")
    @CCD(
            label = "Respondent unavailability date ranges",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UnavailabilityDateRange",
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "Respondent unavailability date ranges",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UnavailabilityDateRange",
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private ListTypeItem<UnavailabilityRanges> respondentUnavailability;
    @JsonProperty("acasCertificate")
    @CCD(
            label = "Please enter an ACAS Certificate number",
            hint = "For example R123456/12/34, MU123456/12/34",
            regex = "[a-zA-Z]{1,2}\\d{6}\\/\\d{2}\\/\\d{2}",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access059.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Please enter an ACAS Certificate number",
            hint = "For example R123456/12/34, MU123456/12/34",
            regex = "[a-zA-Z]{1,2}\\d{6}\\/\\d{2}\\/\\d{2}",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access096.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String acasCertificate;

    @JsonProperty("SearchCriteria")
    @CCD(
            id = "SearchCriteria",
            label = " ",
            typeOverride = FieldType.SearchCriteria,
            includeSearchable = true,
            access = SingleAccess.Access198.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "SearchCriteria",
            label = " ",
            typeOverride = FieldType.SearchCriteria,
            includeSearchable = true,
            access = SingleAccess.Access198.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private SearchCriteria searchCriteria;

   @CCD(
           label = "text",
           typeOverride = FieldType.YesOrNo,
           access = SingleAccess.Access200.class,
           includeInProfiles = EnglandWalesSingleDefinition.class
   )
   @CCD(
           label = "text",
           typeOverride = FieldType.YesOrNo,
           access = SingleAccess.Access200.class,
           includeInProfiles = ScotlandSingleDefinition.class
   )
    private String waRule21ReferralSent;

    @JsonProperty("batchCaseStayed")
    @CCD(
            label = "Case stayed",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            label = "Case stayed",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private String batchCaseStayed;

    /**
     * Convenience method for using the new ListTypeItem pattern for setting repCollection.
     * @param repCollection Collection of respondent representatives
     */
    @JsonIgnore
    @Tolerate
    void setRepCollection(ListTypeItem<RepresentedTypeR> repCollection) {
        this.repCollection = repCollection.stream()
                .map(o -> RepresentedTypeRItem.builder().id(o.getId()).value(o.getValue()).build())
                .toList();
    }

    @JsonProperty("et1ReppedTriageAddress")
    @CCD(
            label = " ",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Address et1ReppedTriageAddress;
    @JsonProperty("et1ReppedTriageYesNo")
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1ReppedTriageYesNo;
    @JsonProperty("et1ClaimStatuses")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1ClaimStatuses;

    @JsonProperty("et1ReppedSectionOne")
    @CCD(
            label = "ET1 Repped Section One",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "ET1 Repped Section One",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1ReppedSectionOne;
    @JsonProperty("et1ReppedSectionTwo")
    @CCD(
            label = "ET1 Repped Section Two",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "ET1 Repped Section Two",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1ReppedSectionTwo;
    @JsonProperty("et1ReppedSectionThree")
    @CCD(
            label = "ET1 Repped Section Three",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "ET1 Repped Section Three",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access192.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1ReppedSectionThree;

    @JsonProperty("claimantFirstName")
    @CCD(
            label = "Claimant's First Name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant's First Name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantFirstName;
    @JsonProperty("claimantLastName")
    @CCD(
            label = "Claimant's Last Name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant's Last Name",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantLastName;
    @JsonProperty("claimantDateOfBirth")
    @CCD(
            label = "Claimant's Date of Birth",
            hint = "For example, 23 04 1981",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant's Date of Birth",
            hint = "For example, 23 04 1981",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantDateOfBirth;
    @JsonProperty("claimantSex")
    @CCD(
            label = "Select the claimant's sex",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_Sex",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select the claimant's sex",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_Sex",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantSex;
    @JsonProperty("claimantPreferredTitle")
    @CCD(
            label = "What is the claimant’s preferred title?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is the claimant’s preferred title?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantPreferredTitle;
    @JsonProperty("claimantContactAddress")
    @CCD(
            label = " ",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Address claimantContactAddress;
    @JsonProperty("representativeAttendHearing")
    @CCD(
            label = "Which types of hearing can you, as the representative, attend?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_HearingAttendence",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Which types of hearing can you, as the representative, attend?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_HearingAttendence",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> representativeAttendHearing;
    @JsonProperty("claimantAttendHearing")
    @CCD(
            label = "Which types of hearing can the claimant attend?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_HearingAttendence",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Which types of hearing can the claimant attend?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_HearingAttendence",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantAttendHearing;
    @JsonProperty("claimantSupportQuestion")
    @CCD(
            label = " Are there any support requirements?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "frl_et3_yes_no_not_sure_yet",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " Are there any support requirements?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "frl_et3_yes_no_not_sure_yet",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantSupportQuestion;
    @JsonProperty("claimantSupportQuestionReason")
    @CCD(
            label = "Give details of the support required",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details of the support required",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantSupportQuestionReason;
    @JsonProperty("representativeContactPreference")
    @CCD(
            label = "How would you prefer to be contacted?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_ContactPreference",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How would you prefer to be contacted?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_ContactPreference",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> representativeContactPreference;
    @JsonProperty("contactPreferencePostReason")
    @CCD(
            label = "Provide a reason why you have selected post",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Provide a reason why you have selected post",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String contactPreferencePostReason;
    @JsonProperty("representativePhoneNumber")
    @CCD(
            label = "What is you contact phone number?",
            hint = "Where we can contact you during the day. For international numbers include the country code.",
            typeOverride = FieldType.PhoneUK,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is you contact phone number?",
            hint = "Where we can contact you during the day. For international numbers include the country code.",
            typeOverride = FieldType.PhoneUK,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String representativePhoneNumber;
    @JsonProperty("representativeReferenceNumber")
    @CCD(
            label = "What is your representative reference number?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What is your representative reference number?",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String representativeReferenceNumber;
    @JsonProperty("representativeAddress")
    @CCD(
            label = "Contact address if different from registered address",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access005.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Contact address if different from registered address",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access005.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Address representativeAddress;
    @JsonProperty("didClaimantWorkForOrg")
    @CCD(
            label = "Did the claimant work for the respondent?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Did the claimant work for the respondent?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> didClaimantWorkForOrg;
    @JsonProperty("claimantStillWorking")
    @CCD(
            label = "Is the claimant still working for the respondent?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_StillWorking",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is the claimant still working for the respondent?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_StillWorking",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantStillWorking;
    @JsonProperty("claimantJobTitle")
    @CCD(
            label = "Enter job title",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter job title",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantJobTitle;
    @JsonProperty("claimantStartDate")
    @CCD(
            label = "Enter employment start date",
            hint = "For example, 12 11 2020",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter employment start date",
            hint = "For example, 12 11 2020",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantStartDate;
    @JsonProperty("claimantEndDate")
    @CCD(
            label = "Enter employment end date",
            hint = "For example, 12 11 2020",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter employment end date",
            hint = "For example, 12 11 2020",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantEndDate;
    @JsonProperty("claimantStillWorkingNoticePeriod")
    @CCD(
            label = "Is there a notice period?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_NoticePeriod",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is there a notice period?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_NoticePeriod",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantStillWorkingNoticePeriod;
    @JsonProperty("claimantStillWorkingNoticePeriodMonths")
    @CCD(
            label = "How many months is the notice period?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How many months is the notice period?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantStillWorkingNoticePeriodMonths;
    @JsonProperty("claimantStillWorkingNoticePeriodWeeks")
    @CCD(
            label = "How many weeks is the notice period?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How many weeks is the notice period?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantStillWorkingNoticePeriodWeeks;
    @JsonProperty("claimantWorkingNoticePeriod")
    @CCD(
            label = "Notice period length",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_notice_period_unit",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Notice period length",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_notice_period_unit",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantWorkingNoticePeriod;
    @JsonProperty("claimantWorkingNoticePeriodMonths")
    @CCD(
            label = "How many months of the notice period is the claimant being paid for?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How many months of the notice period is the claimant being paid for?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantWorkingNoticePeriodMonths;
    @JsonProperty("claimantWorkingNoticePeriodWeeks")
    @CCD(
            label = "How many weeks of the notice period is the claimant being paid for?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How many weeks of the notice period is the claimant being paid for?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantWorkingNoticePeriodWeeks;
    @JsonProperty("claimantWorkingNoticePeriodEndDate")
    @CCD(
            label = "When does the notice period end?",
            hint = "For example, 23 04 1981",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "When does the notice period end?",
            hint = "For example, 23 04 1981",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantWorkingNoticePeriodEndDate;
    @JsonProperty("claimantNoLongerWorkingQuestion")
    @CCD(
            label = "Work or paid a notice period",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_WorkPayNoticePeriod",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Work or paid a notice period",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_WorkPayNoticePeriod",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantNoLongerWorkingQuestion;
    @JsonProperty("claimantNoLongerWorking")
    @CCD(
            label = "Notice period length",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_NoticePeriodLength",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Notice period length",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_NoticePeriodLength",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantNoLongerWorking;
    @JsonProperty("claimantNoLongerWorkingMonths")
    @CCD(
            label = "How many months was the notice period?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How many months was the notice period?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantNoLongerWorkingMonths;
    @JsonProperty("claimantNoLongerWorkingWeeks")
    @CCD(
            label = "How many weeks was the notice period?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "How many weeks was the notice period?",
            typeOverride = FieldType.Number,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantNoLongerWorkingWeeks;
    @JsonProperty("claimantNoLongerWorkingPay")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private String claimantNoLongerWorkingPay;
    @JsonProperty("claimantAverageWeeklyWorkHours")
    @CCD(
            label = "Enter average weekly hours",
            typeOverride = FieldType.Number,
            searchable = false,
            min = 0,
            max = 168,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter average weekly hours",
            typeOverride = FieldType.Number,
            searchable = false,
            min = 0,
            max = 168,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantAverageWeeklyWorkHours;
    @JsonProperty("claimantPayBeforeTax")
    @CCD(
            label = "Enter the claimant's pay BEFORE tax and National Insurance",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the claimant's pay BEFORE tax and National Insurance",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantPayBeforeTax;
    @JsonProperty("claimantPayAfterTax")
    @CCD(
            label = "Enter the claimant's pay AFTER tax and National Insurance",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the claimant's pay AFTER tax and National Insurance",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantPayAfterTax;
    @JsonProperty("claimantPayType")
    @CCD(
            label = "Is this weekly, monthly or annual pay?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_PayFrequency",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this weekly, monthly or annual pay?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_PayFrequency",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantPayType;
    @JsonProperty("claimantPensionContribution")
    @CCD(
            label = "Did the respondent make contributions to the claimant's pension?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_pension_contribution",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Did the respondent make contributions to the claimant's pension?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_pension_contribution",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantPensionContribution;
    @JsonProperty("claimantWeeklyPension")
    @CCD(
            label = "Enter their pension contributions - worked out weekly",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter their pension contributions - worked out weekly",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantWeeklyPension;
    @JsonProperty("claimantEmployeeBenefits")
    @CCD(
            label = "Did the claimant receive any employee benefits?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Did the claimant receive any employee benefits?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantEmployeeBenefits;
    @JsonProperty("claimantBenefits")
    @CCD(
            label = "Tell us about any benefits",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Tell us about any benefits",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantBenefits;
    @JsonProperty("claimantNewJob")
    @CCD(
            label = "Has the claimant got a new job?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Has the claimant got a new job?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantNewJob;
    @JsonProperty("claimantNewJobStartDate")
    @CCD(
            label = "When did the claimant start?",
            hint = "If you do not know the exact date then enter the best estimate",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "When did the claimant start?",
            hint = "If you do not know the exact date then enter the best estimate",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantNewJobStartDate;
    @JsonProperty("claimantNewJobPayBeforeTax")
    @CCD(
            label = "Enter pay BEFORE tax and National Insurance",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter pay BEFORE tax and National Insurance",
            typeOverride = FieldType.MoneyGBP,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantNewJobPayBeforeTax;
    @JsonProperty("claimantNewJobPayPeriod")
    @CCD(
            label = "Is this weekly, monthly or annual pay?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_pay_cycle",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this weekly, monthly or annual pay?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_pay_cycle",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimantNewJobPayPeriod;

    @JsonProperty("respondentType")
    @CCD(
            label = " ",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_respondentType",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_respondentType",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondentType;
    @JsonProperty("respondentOrganisationName")
    @CCD(
            label = "Enter the name of the organisation",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the name of the organisation",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondentOrganisationName;
    @JsonProperty("respondentFirstName")
    @CCD(
            label = "Enter the first name of the individual",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the first name of the individual",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondentFirstName;
    @JsonProperty("respondentLastName")
    @CCD(
            label = "Enter the last name of the individual",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the last name of the individual",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondentLastName;
    @JsonProperty("respondentAddress")
    @CCD(
            label = " ",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "AddressUK",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Address respondentAddress;
    @JsonProperty("didClaimantWorkAtSameAddressPreamble")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String didClaimantWorkAtSameAddressPreamble;
    @JsonProperty("didClaimantWorkAtSameAddress")
    @CCD(
            label = "Did the claimant work at this address?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Did the claimant work at this address?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String didClaimantWorkAtSameAddress;
    @JsonProperty("claimantWorkAddressYes")
    @CCD(ignore = true, includeInProfiles = SingleDefinition.class)
    private Address claimantWorkAddressYes;
    @JsonProperty("respondentAcasYesNo")
    @CCD(
            label = "Do you have an Acas certificate number for the respondent?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "msl_YesNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you have an Acas certificate number for the respondent?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "msl_YesNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondentAcasYesNo;
    @JsonProperty("respondentAcasNumber")
    @CCD(
            label = "Enter the Acas number",
            regex = "[a-zA-Z]{1,2}\\d{6}\\/\\d{2}\\/\\d{2}",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the Acas number",
            regex = "[a-zA-Z]{1,2}\\d{6}\\/\\d{2}\\/\\d{2}",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondentAcasNumber;
    @JsonProperty("respondentNoAcasCertificateReason")
    @CCD(
            label = "Why is there no certificate number?",
            hint = "Incorrectly claiming an exemption may lead to the claim being rejected. If in doubt, please contact Acas.",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_noAcasReason",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Why is there no certificate number?",
            hint = "Incorrectly claiming an exemption may lead to the claim being rejected. If in doubt, please contact Acas.",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_noAcasReason",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String respondentNoAcasCertificateReason;
    @JsonProperty("addAdditionalRespondentPreamble")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String addAdditionalRespondentPreamble;
    @JsonProperty("addAdditionalRespondent")
    @CCD(
            label = "Do you want to add another respondent?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to add another respondent?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String addAdditionalRespondent;
    @JsonProperty("et1ReppedRespondentCollection")
    @CCD(
            label = "Additional respondents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CreateRespondent",
            searchable = false,
            max = 5,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Additional respondents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CreateRespondent",
            searchable = false,
            max = 5,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<CreateRespondentType>> et1ReppedRespondentCollection;

    @JsonProperty("et1SectionThreeClaimDetails")
    @CCD(
            label = "Enter details of the claim",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter details of the claim",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1SectionThreeClaimDetails;
    @JsonProperty("et1SectionThreeDocumentUpload")
    @CCD(
            label = "Upload the details of the claim",
            typeOverride = FieldType.Document,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload the details of the claim",
            typeOverride = FieldType.Document,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private UploadedDocumentType et1SectionThreeDocumentUpload;
    @JsonProperty("et1SectionThreeTypeOfClaim")
    @CCD(
            label = "What type of claim is this?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et1TypesOfClaim",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What type of claim is this?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et1TypesOfClaim",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> et1SectionThreeTypeOfClaim;
    @JsonProperty("discriminationTypesOfClaim")
    @CCD(
            label = "What type of discrimination are you claiming?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et1DiscriminationClaims",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What type of discrimination are you claiming?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et1DiscriminationClaims",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> discriminationTypesOfClaim;
    @JsonProperty("payTypesOfClaim")
    @CCD(
            label = "What type of pay claim are you making?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_payClaims",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What type of pay claim are you making?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_payClaims",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> payTypesOfClaim;
    @JsonProperty("whistleblowingYesNo")
   @CCD(
           label = "Do you want us to forward this claim to a relevant regulator or body?",
           typeOverride = FieldType.MultiSelectList,
           typeParameterOverride = "msl_YesNo",
           searchable = false,
           access = SingleAccess.Access007.class,
           includeInProfiles = EnglandWalesSingleDefinition.class
   )
   @CCD(
           label = "Do you want us to forward this claim to a relevant regulator or body?",
           typeOverride = FieldType.MultiSelectList,
           typeParameterOverride = "msl_YesNo",
           searchable = false,
           access = SingleAccess.Access007.class,
           includeInProfiles = ScotlandSingleDefinition.class
   )
    private List<String> whistleblowingYesNo;
    @JsonProperty("whistleblowingRegulator")
   @CCD(
           label = "Enter the name of the relevant regulator or body you want us to send this to",
           hint = "If you cannot find one, enter N/A",
           typeOverride = FieldType.Text,
           searchable = false,
           access = SingleAccess.Access007.class,
           includeInProfiles = EnglandWalesSingleDefinition.class
   )
   @CCD(
           label = "Enter the name of the relevant regulator or body you want us to send this to",
           hint = "If you cannot find one, enter N/A",
           typeOverride = FieldType.Text,
           searchable = false,
           access = SingleAccess.Access007.class,
           includeInProfiles = ScotlandSingleDefinition.class
   )
    private String whistleblowingRegulator;
    @JsonProperty("otherTypeOfClaimDetails")
    @CCD(
            label = "Enter the type of claim",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Enter the type of claim",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String otherTypeOfClaimDetails;
    @JsonProperty("claimSuccessful")
    @CCD(
            label = "What does the claimant want if their claim is successful?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_claimOutcomes",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What does the claimant want if their claim is successful?",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_claimOutcomes",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> claimSuccessful;
    @JsonProperty("compensationDetails")
    @CCD(
            label = "What compensation is the claimant seeking?",
            hint = "Set out all compensation the claimant is claiming for, and provide a total if possible.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What compensation is the claimant seeking?",
            hint = "Set out all compensation the claimant is claiming for, and provide a total if possible.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String compensationDetails;
    @JsonProperty("tribunalRecommendationDetails")
    @CCD(
            label = "What tribunal recommendation would you like to make?",
            hint = "Tell us what action you’d like the tribunal to recommend the respondent makes to reduce the impact of any discrimination which has occurred.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What tribunal recommendation would you like to make?",
            hint = "Tell us what action you’d like the tribunal to recommend the respondent makes to reduce the impact of any discrimination which has occurred.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String tribunalRecommendationDetails;
    @JsonProperty("linkedCasesYesNo")
    @CCD(
            label = "Are there any existing cases which may be linked to this new claim",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Are there any existing cases which may be linked to this new claim",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> linkedCasesYesNo;
    @JsonProperty("linkedCasesDetails")
    @CCD(
            label = "Details of linked cases",
            hint = "Enter the case numbers and names of the people in the existing case or cases.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Details of linked cases",
            hint = "Enter the case numbers and names of the people in the existing case or cases.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String linkedCasesDetails;
    @JsonProperty("et1SectionOneDateCompleted")
    @CCD(
            label = "Date completed",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Date completed",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1SectionOneDateCompleted;
    @JsonProperty("et1SectionTwoDateCompleted")
    @CCD(
            label = "Date completed",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Date completed",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1SectionTwoDateCompleted;
    @JsonProperty("et1SectionThreeDateCompleted")
    @CCD(
            label = "Date completed",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Date completed",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1SectionThreeDateCompleted;
    @JsonProperty("claimantRepresentativeOrganisationPolicy")
    @CCD(
            label = "Claimants Representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access006.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimants Representative",
            typeOverride = FieldType.OrganisationPolicy,
            access = SingleAccess.Access006.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private OrganisationPolicy claimantRepresentativeOrganisationPolicy;
    @JsonProperty("downloadDraftEt1Date")
    @CCD(
            label = " ",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String downloadDraftEt1Date;
    @JsonProperty("hearingContactLanguage")
    @CCD(
            label = "If a hearing is required, what language do you, as the representative, want to speak at a hearing?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_languages",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private List<String> hearingContactLanguage;
    @JsonProperty("claimantHearingContactLanguage")
    @CCD(
            label = "If a hearing is required, what language does the claimant want to speak at a hearing?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_languages",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private List<String> claimantHearingContactLanguage;
    @JsonProperty("contactLanguageQuestion")
    @CCD(
            label = "What language do you want us to use when we contact you?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_languages",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private List<String> contactLanguageQuestion;
    @JsonProperty("requiresSubmissionDocuments")
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String requiresSubmissionDocuments;
    @JsonProperty("legalRepDocumentsMarkdown")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access113.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access113.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String legalRepDocumentsMarkdown;

    @JsonProperty("claimantSelectNotification")
    @CCD(
            label = "Select a judgment, order or notification",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select a judgment, order or notification",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType claimantSelectNotification;
    @JsonProperty("claimantNotificationTableMarkdown")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantNotificationTableMarkdown;
    @JsonProperty("claimantNotificationResponseText")
    @CCD(
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer to the tribunal.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer to the tribunal.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantNotificationResponseText;
    @JsonProperty("claimantNotificationSupportingMaterial")
    @CCD(
            label = "Do you have any supporting material?",
            hint = "Use this option if you have any documents or other material to support your response.",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you have any supporting material?",
            hint = "Use this option if you have any documents or other material to support your response.",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantNotificationSupportingMaterial;
    @JsonProperty("claimantNotificationDocuments")
    @CCD(
            label = "Upload a document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload a document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<DocumentType>> claimantNotificationDocuments;
    @JsonProperty("isRespondentSystemUser")
    @CCD(
            label = "Is the respondent a system user?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is the respondent a system user?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String isRespondentSystemUser;
    @JsonProperty("claimantNotificationCopyToOtherParty")
    @CCD(
            label = "Do you want to send a copy of this response to the other party?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantCopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Do you want to send a copy of this response to the other party?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantCopyToOtherPartyYesOrNo",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantNotificationCopyToOtherParty;
    @JsonProperty("claimantNotificationsCopyNoDetails")
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Give details",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantNotificationsCopyNoDetails;
    @JsonProperty("uploadOrRemoveDcf")
    @CCD(
            label = "Create, Upload or Remove DCF",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "createUploadOrRemove",
            searchable = false,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Create, Upload or Remove DCF",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "createUploadOrRemove",
            searchable = false,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String uploadOrRemoveDcf;

    // Migration fields ECM to Reform
    @JsonProperty("ecmCaseLink")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String ecmCaseLink;
    @JsonProperty("ecmFeeGroupReference")
    @CCD(
            label = "ECM Submission Reference",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "ECM Submission Reference",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String ecmFeeGroupReference;
    @JsonProperty("migratedFromEcm")
    @CCD(
            label = "Migrated from ECM",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Migrated from ECM",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String migratedFromEcm;

    // NOC fields - to find if any claimant representative has been removed.
    @JsonProperty("claimantRepresentativeRemoved")
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access219.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access219.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantRepresentativeRemoved;
    // new fields: et3RepresentativeContactChangeOption and claimantRepresentativeContactChangeOption
    // to determine whether the representative's contact information should be updated using the
    // MyHMCTS address or a manually entered address.
    @JsonProperty("representativeContactChangeOption")
    @CCD(
            label = " ",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_representativeContactChangeOptions",
            searchable = false,
            access = SingleAccess.Access004.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_representativeContactChangeOptions",
            searchable = false,
            access = SingleAccess.Access004.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String representativeContactChangeOption;
    // Unable to remove respondent representative from repCollection as a respondent (remove legal representation).
    // To resolve this problem added new field to identify which respondent representatives needs to be removed.
    @JsonProperty("repCollectionToRemove")
    @CCD(
            label = "Respondent representative(s) to remove",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentRepresentative",
            access = SingleAccess.Access199.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondent representative(s) to remove",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentRepresentative",
            access = SingleAccess.Access199.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<RepresentedTypeRItem> repCollectionToRemove;

    @JsonProperty("acasCertificateRequired")
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            includeSearchable = true,
            access = SingleAccess.Access002.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            includeSearchable = true,
            access = SingleAccess.Access002.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String acasCertificateRequired;

    @JsonProperty("uploadHearingDocumentsSelectPastOrFutureHearing")
    @CCD(
            label = "Select if the hearing is in the past or in the future",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_futureOrPastHearing",
            searchable = false,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select if the hearing is in the past or in the future",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_futureOrPastHearing",
            searchable = false,
            access = SingleAccess.Access184.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String uploadHearingDocumentsSelectPastOrFutureHearing;
    @JsonProperty("uploadHearingDocumentsSelectPastHearing")
    @CCD(
            label = "Select which hearing these documents are for",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select which hearing these documents are for",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access184.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType uploadHearingDocumentsSelectPastHearing;
    @JsonProperty("uploadHearingDocumentsSelectFutureHearing")
    @CCD(
            label = "Select which hearing these documents are for",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Select which hearing these documents are for",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access184.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType uploadHearingDocumentsSelectFutureHearing;
    @JsonProperty("uploadHearingDocumentType")
    @CCD(
            label = "Upload Hearing Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingDocumentUpload",
            searchable = false,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Upload Hearing Documents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingDocumentUpload",
            searchable = false,
            access = SingleAccess.Access184.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<UploadHearingDocumentType>> uploadHearingDocumentType;
    @JsonProperty("uploadHearingDocumentsWhoseDocuments")
    @CCD(
            label = "Which party has submitted these documents?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantOrRespondents",
            searchable = false,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Which party has submitted these documents?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantOrRespondents",
            searchable = false,
            access = SingleAccess.Access184.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String uploadHearingDocumentsWhoseDocuments;
    @JsonProperty("uploadHearingDocumentsDateSubmitted")
    @CCD(
            label = "What date were these documents submitted?",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "What date were these documents submitted?",
            typeOverride = FieldType.Date,
            searchable = false,
            access = SingleAccess.Access184.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String uploadHearingDocumentsDateSubmitted;
    @JsonProperty("myHmctsAddressText")
    @CCD(
            label = "Chosen address",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access004.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Chosen address",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access004.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String myHmctsAddressText;
  
    @CCD(
            id = "LinkedCasesComponentLauncher",
            label = "Component Launcher (for displaying Linked Cases data)",
            typeOverride = FieldType.ComponentLauncher,
            access = SingleAccess.Access123.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            id = "LinkedCasesComponentLauncher",
            label = "Component Launcher (for displaying Linked Cases data)",
            typeOverride = FieldType.ComponentLauncher,
            searchable = false,
            access = SingleAccess.Access127.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    @CCD(
            id = "LinkedCasesComponentLauncher",
            label = "Component Launcher (for displaying Linked Cases data)",
            typeOverride = FieldType.ComponentLauncher,
            includeInProfiles = EnglandWalesSingleProdDefinition.class
    )
    @CCD(
            id = "LinkedCasesComponentLauncher",
            label = "Component Launcher (for displaying Linked Cases data)",
            typeOverride = FieldType.ComponentLauncher,
            searchable = false,
            includeInProfiles = ScotlandSingleProdDefinition.class
    )
    private Object singleDefinitionField002;







    @CCD(
            id = "addAdditionalRespondentPreambleLabel",
            label = "${addAdditionalRespondentPreamble}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "addAdditionalRespondentPreambleLabel",
            label = "${addAdditionalRespondentPreamble}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField009;


    @CCD(
            id = "applicationsAdminLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmin/tseAdmin1\">Record a decision</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmReply/tseAdmReply1\">Respond to an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdminCloseAnApplication/tseAdminCloseAnApplication1\">Close application</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "applicationsAdminLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmin/tseAdmin1\">Record a decision</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmReply/tseAdmReply1\">Respond to an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdminCloseAnApplication/tseAdminCloseAnApplication1\">Close application</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField011;

    @CCD(
            id = "applicationsLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/respondentTseAllApplications/respondentTseAllApplications1\">All applications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/viewRespondentTSEApplications/viewRespondentTSEApplications1\">View an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/respondentTSE/respondentTSE1\">Make an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseRespond/tseRespond1\">Respond to an application</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access011.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "applicationsLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/respondentTseAllApplications/respondentTseAllApplications1\">All applications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/viewRespondentTSEApplications/viewRespondentTSEApplications1\">View an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/respondentTSE/respondentTSE1\">Make an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseRespond/tseRespond1\">Respond to an application</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access011.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField012;

    @CCD(
            id = "applicationsLinksClaimantRep",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantTseAllApplications/claimantTseAllApplications1\">All applications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/viewClaimantTSEApplications/viewClaimantTSEApplications1\">View an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantTSE/claimantTSE1\">Make an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseClaimantRepResponse/tseClaimantRepResponse1\"> Respond to an application</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access008.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "applicationsLinksClaimantRep",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantTseAllApplications/claimantTseAllApplications1\">All applications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/viewClaimantTSEApplications/viewClaimantTSEApplications1\">View an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantTSE/claimantTSE1\">Make an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseClaimantRepResponse/tseClaimantRepResponse1\"> Respond to an application</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access008.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField013;

    @CCD(
            id = "applicationsTab",
            label = "#### Applications",
            typeOverride = FieldType.Label,
            securityClassification = "PUBLIC",
            access = SingleAccess.Access003.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "applicationsTab",
            label = "#### Applications",
            typeOverride = FieldType.Label,
            securityClassification = "PUBLIC",
            access = SingleAccess.Access003.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField014;

    @CCD(
            id = "bundlesRespondentPrepareDocNotes1",
            label = "Please refer to any instructions or guidance given by the Tribunal as to what documents are needed for the hearing.<br><br>These documents, usually agreed by both parties, will form part of the evidence at the hearing. You should include all the documents that are important to your case, and that both parties want to refer to at the hearing.<br><br>The combined set of documents must be in one PDF file, usually submitted by one party unless instructed otherwise by the tribunal.<br> <br>Generally the documents should be included in the PDF file in date order, and the first page or pages of the pdf numbering should be an index showing the name of the document, its date and the page number.<br><br>The single PDF document should be created offline before uploading through this form.<br><br>You must submit hearing documents according to any time limits set out by the Tribunal.<br><br>Refer to the orders from the tribunal about whether you need to include your witness statements here or separately.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "bundlesRespondentPrepareDocNotes1",
            label = "Please refer to any instructions or guidance given by the Tribunal as to what documents are needed for the hearing.<br><br>These documents, usually agreed by both parties, will form part of the evidence at the hearing. You should include all the documents that are important to your case, and that both parties want to refer to at the hearing.<br><br>The combined set of documents must be in one PDF file, usually submitted by one party unless instructed otherwise by the tribunal.<br> <br>Generally the documents should be included in the PDF file in date order, and the first page or pages of the pdf numbering should be an index showing the name of the document, its date and the page number.<br><br>The single PDF document should be created offline before uploading through this form.<br><br>You must submit hearing documents according to any time limits set out by the Tribunal.<br><br>Refer to the orders from the tribunal about whether you need to include your witness statements here or separately.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField015;

    @CCD(
            id = "bundlesRespondentPrepareDocNotes2",
            label = "Providing a file which contains all relevant documents, and only relevant documents, will help ensure a fair hearing and minimise any delays.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "bundlesRespondentPrepareDocNotes2",
            label = "Providing a file which contains all relevant documents, and only relevant documents, will help ensure a fair hearing and minimise any delays.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField016;

    @CCD(
            id = "bundlesRespondentUploadFileLabel",
            label = "Once you’ve read the orders from the tribunal, make sure:<br><ul><li>you have the set of documents in one PDF file</li><li>the documents are in date order or some other logical order and comply with any directions given by the Tribunal</li><li>the index item refers to the correct page in the document</li><li>you only include relevant documents and material for your case</li><li>the PDF document falls within any page limits given by the tribunal</li></ul>If you’re referencing one section of a document like a contract of employment, say which part in the index.<br><br>If you need to refer to previous case documents, go to your <a href=\"/cases/case-details/${[CASE_REFERENCE]}#Documents\" target=\"_blank\">case documents (opens in new tab)</a>.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access193.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "bundlesRespondentUploadFileLabel",
            label = "Once you’ve read the orders from the tribunal, make sure:<br><ul><li>you have the set of documents in one PDF file</li><li>the documents are in date order or some other logical order and comply with any directions given by the Tribunal</li><li>the index item refers to the correct page in the document</li><li>you only include relevant documents and material for your case</li><li>the PDF document falls within any page limits given by the tribunal</li></ul>If you’re referencing one section of a document like a contract of employment, say which part in the index.<br><br>If you need to refer to previous case documents, go to your <a href=\"/cases/case-details/${[CASE_REFERENCE]}#Documents\" target=\"_blank\">case documents (opens in new tab)</a>.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access193.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField017;

    @CCD(
            id = "bundlesTabTitle",
            label = "### Hearing Documents",
            typeOverride = FieldType.Label,
            securityClassification = "PUBLIC",
            access = SingleAccess.Access021.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "bundlesTabTitle",
            label = "### Hearing Documents",
            typeOverride = FieldType.Label,
            securityClassification = "PUBLIC",
            access = SingleAccess.Access020.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField018;

    @CCD(
            id = "caseHistory",
            label = "History",
            typeNameOverride = "CaseHistoryViewer",
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "caseHistory",
            label = "History",
            typeNameOverride = "CaseHistoryViewer",
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField019;

    @CCD(
            id = "caseStateDesc",
            label = "#### Case Status:  ${[STATE]} ",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access142.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "caseStateDesc",
            label = "#### Case Status:  ${[STATE]} ",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access173.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField020;


    @CCD(
            id = "claimantAndRespondentAddressesLabel",
            label = "${claimantAndRespondentAddresses}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantAndRespondentAddressesLabel",
            label = "${claimantAndRespondentAddresses}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField022;

    @CCD(
            id = "claimantAverageWeeklyWorkHoursPreamble",
            label = "If the number of hours the claimant worked changed each week (for example, they had a zero-hours contract), use the final 12 weeks of payslips to work out their weekly average.\n\nDo not include overtime hours.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantAverageWeeklyWorkHoursPreamble",
            label = "If the number of hours the claimant worked changed each week (for example, they had a zero-hours contract), use the final 12 weeks of payslips to work out their weekly average.\n\nDo not include overtime hours.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField023;

    @CCD(
            id = "claimantJonsLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantViewAllNotifications/claimantViewAllNotifications1\">All judgments, orders and notifications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantViewNotification/claimantViewNotification1\">View a judgment, order or notification</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantRespondToNotification/claimantRespondToNotification1\">Respond to an order or request from the tribunal</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantJonsLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantViewAllNotifications/claimantViewAllNotifications1\">All judgments, orders and notifications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantViewNotification/claimantViewNotification1\">View a judgment, order or notification</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantRespondToNotification/claimantRespondToNotification1\">Respond to an order or request from the tribunal</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField024;

    @CCD(
            id = "claimantNewJobPayPreamble",
            label = "Tell us about the claimant's gross pay before tax and other deductions",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantNewJobPayPreamble",
            label = "Tell us about the claimant's gross pay before tax and other deductions",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField025;

    @CCD(
            id = "claimantNotificationSupportingMaterialLabel",
            label = "Consider when providing material that:<br><ul><li>you can upload letters, photos or documents to support your application</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantNotificationSupportingMaterialLabel",
            label = "Consider when providing material that:<br><ul><li>you can upload letters, photos or documents to support your application</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField026;

    @CCD(
            id = "claimantNotificationTableMarkdownLabel",
            label = "${claimantNotificationTableMarkdown}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantNotificationTableMarkdownLabel",
            label = "${claimantNotificationTableMarkdown}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField027;

    @CCD(
            id = "claimantPayPreamble",
            label = "For unfair dismissal and some other claims, the tribunal will need the average of the claimant’s last 12 weeks’ pay. Provide your best estimate for the average weekly pay.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantPayPreamble",
            label = "For unfair dismissal and some other claims, the tribunal will need the average of the claimant’s last 12 weeks’ pay. Provide your best estimate for the average weekly pay.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField028;



    @CCD(
            id = "claimantRepResponseTableLabel",
            label = "${claimantRepResponseTable}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantRepResponseTableLabel",
            label = "${claimantRepResponseTable}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField031;

    @CCD(
            id = "claimantRepTseRespIntroLabel",
            label = "${claimantRepResponseIntro}",
            typeOverride = FieldType.Label,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantRepTseRespIntroLabel",
            label = "${claimantRepResponseIntro}",
            typeOverride = FieldType.Label,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField032;

    @CCD(
            id = "claimantRule92TextWhenRespOffline",
            label = "<h3>To copy this correspondence to the other party, you must send it to them by post or email. They cannot view it in this service.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantRule92TextWhenRespOffline",
            label = "<h3>To copy this correspondence to the other party, you must send it to them by post or email. They cannot view it in this service.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField033;

    @CCD(
            id = "claimantTseAllApplicationsLabel",
            label = "All applications <hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access214.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseAllApplicationsLabel",
            label = "All applications <hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access214.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField034;

    @CCD(
            id = "claimantTseGuidanceLabel1",
            label = "<p>Use this form to apply to amend the ET1 claim.</p> <p>The tribunal will decide if you can make the amendment by judging the fairness of the application. This decision may be made at a hearing.</p> <p>The tribunal also needs to know if you consider the amendment minor or substantial.</p> <p>Providing details of why you want to amend the claim and the importance of the amendment will help the tribunal to decide your application more quickly.</p> <h3>Details to include in your application:</h3> <ul><li>What you want to amend in the claim</li><li>If you consider it a minor or substantial amendment</li><li>Why you want to make this amendment</li><li>Why you are asking to make this amendment now</li><li>How this amendment will benefit you and how could it disadvantage you if not granted</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel1",
            label = "<p>Use this form to apply to amend the ET1 claim.</p> <p>The tribunal will decide if you can make the amendment by judging the fairness of the application. This decision may be made at a hearing.</p> <p>The tribunal also needs to know if you consider the amendment minor or substantial.</p> <p>Providing details of why you want to amend the claim and the importance of the amendment will help the tribunal to decide your application more quickly.</p> <h3>Details to include in your application:</h3> <ul><li>What you want to amend in the claim</li><li>If you consider it a minor or substantial amendment</li><li>Why you want to make this amendment</li><li>Why you are asking to make this amendment now</li><li>How this amendment will benefit you and how could it disadvantage you if not granted</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField035;

    @CCD(
            id = "claimantTseGuidanceLabel10",
            label = "<p>Use this form to apply to prevent or restrict publicity in this case.</p> <p>It is an important principle that justice should normally be delivered in public. However, the tribunal has the power to prevent or restrict the public disclosure of any aspect of your case if necessary in the interests of justice or to protect the Convention rights of any person.</p> <p>The tribunal also has certain other powers to sit in private or to restrict publicity in accordance with sections 10A, 10B, 11 and 12 of the Employment Tribunals Act 1996.</p> <p>The tribunal may issue an order:</p><ul><li>That a hearing that would otherwise be in public be conducted, in whole or in part, in private</li><li>That the identities of specified parties, witnesses or other persons referred to in the proceedings should not be disclosed to the public, by the use of anonymisation or otherwise</li><li>For measures preventing witnesses at a public hearing being identifiable by members of the public</li><li>Restricting the reporting of the case in the media</li></ul> <h3>Details to include in your application: </h3> <ul><li>How the tribunal should prevent or restrict publicity in this case</li><li>Why the restrictions you want are needed. The tribunal must make the least restrictive order possible. Tell us why fewer restrictions would not be enough</li><li>Any media interest in this case. Include names of media organisations or journalists that are interested. The tribunal might seek their comments before making a decision</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel10",
            label = "<p>Use this form to apply to prevent or restrict publicity in this case.</p> <p>It is an important principle that justice should normally be delivered in public. However, the tribunal has the power to prevent or restrict the public disclosure of any aspect of your case if necessary in the interests of justice or to protect the Convention rights of any person.</p> <p>The tribunal also has certain other powers to sit in private or to restrict publicity in accordance with sections 10A, 10B, 11 and 12 of the Employment Tribunals Act 1996.</p> <p>The tribunal may issue an order:</p><ul><li>That a hearing that would otherwise be in public be conducted, in whole or in part, in private</li><li>That the identities of specified parties, witnesses or other persons referred to in the proceedings should not be disclosed to the public, by the use of anonymisation or otherwise</li><li>For measures preventing witnesses at a public hearing being identifiable by members of the public</li><li>Restricting the reporting of the case in the media</li></ul> <h3>Details to include in your application: </h3> <ul><li>How the tribunal should prevent or restrict publicity in this case</li><li>Why the restrictions you want are needed. The tribunal must make the least restrictive order possible. Tell us why fewer restrictions would not be enough</li><li>Any media interest in this case. Include names of media organisations or journalists that are interested. The tribunal might seek their comments before making a decision</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField036;

    @CCD(
            id = "claimantTseGuidanceLabel11",
            label = "<p>You can request that the tribunal strike out all or parts of the response.</p> <p>If something is ‘struck out’ it is removed from the claim or response and cannot be relied upon.</p> <p>The tribunal can strike out all or parts of the respondent's response on their own initiative or after a request from the claimant.</p> <p>A strike out request must be based on at least one of the grounds of Rule 38 of the Employment Tribunals Rules of Procedure.</p> <h3>Details to include in your application: </h3> <ul><li>Why you think the response (or parts of it) should be struck out</li><li>Which ground or grounds in Rule 38 you say applies in your case</li><li>If you are referring to numbered points or paragraphs in a respondent's response, include these numbers or other references</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel11",
            label = "<p>You can request that the tribunal strike out all or parts of the response.</p> <p>If something is ‘struck out’ it is removed from the claim or response and cannot be relied upon.</p> <p>The tribunal can strike out all or parts of the respondent's response on their own initiative or after a request from the claimant.</p> <p>A strike out request must be based on at least one of the grounds of Rule 38 of the Employment Tribunals Rules of Procedure.</p> <h3>Details to include in your application: </h3> <ul><li>Why you think the response (or parts of it) should be struck out</li><li>Which ground or grounds in Rule 38 you say applies in your case</li><li>If you are referring to numbered points or paragraphs in a respondent's response, include these numbers or other references</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField037;

    @CCD(
            id = "claimantTseGuidanceLabel12",
            label = "<p>Use this form to apply to vary or revoke an order the tribunal has issued.</p> <p>Tribunal orders will not usually be varied or revoked unless there has been a material change in circumstances since the order was made, which make the variation in the interests of justice.</p> <p>The tribunal will consider your reasons for varying or revoking the order and any supporting materials you provide then make a decision.</p><h3>Details to include in your application: </h3> <ul><li>The order you want to vary or revoke</li><li>The date the tribunal issued the order</li><li>Explain which part of the order you want to vary or revoke</li><li>How to vary the order</li><li>Why it is in the interests of justice to vary or revoke this order. Tell us any relevant changes of circumstance since the order was made and consider the requirements of the ‘overriding objective’ in Rule 3 of the Employment Tribunal Rules of Procedure</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel12",
            label = "<p>Use this form to apply to vary or revoke an order the tribunal has issued.</p> <p>Tribunal orders will not usually be varied or revoked unless there has been a material change in circumstances since the order was made, which make the variation in the interests of justice.</p> <p>The tribunal will consider your reasons for varying or revoking the order and any supporting materials you provide then make a decision.</p><h3>Details to include in your application: </h3> <ul><li>The order you want to vary or revoke</li><li>The date the tribunal issued the order</li><li>Explain which part of the order you want to vary or revoke</li><li>How to vary the order</li><li>Why it is in the interests of justice to vary or revoke this order. Tell us any relevant changes of circumstance since the order was made and consider the requirements of the ‘overriding objective’ in Rule 3 of the Employment Tribunal Rules of Procedure</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField038;

    @CCD(
            id = "claimantTseGuidanceLabel13",
            label = "<p>Use this form when you’ve reached a settlement or do not want to continue with all or part of the claim.</p> <p>You can withdraw the whole claim or tell us which parts you want to withdraw.</p> <p>This brings the claim or part of it to an end.</p> <p>You can withdraw at any point before or during a hearing.</p> <p>If you’re withdrawing because of a settlement, make sure you have the settlement in writing.</p> <h3>Withdrawal and dismissal</h3> <p>Once you have told the tribunal you want to withdraw the claim, the tribunal will usually issue a judgment dismissing the claim or part of the claim.</p> <p>A dismissal judgment stops you from making that claim against the same respondents in the future.</p> <p>The tribunal will use Rule 51 of the Employment Tribunal Rules of Procedure to decide whether a dismissal judgment is issued following a withdrawal.</p> <p>Withdrawal and dismissal through this service will be final unless:</p> <ul><li>The tribunal contacts you needing more information</li><li>You have given an acceptable reason why dismissal should not happen</li></ul> <h3>Details to include in your application:</h3> <ul><li>Which parts of the claim you want withdrawn and dismissed</li><li>If you do not want the claim (or part of it) dismissed, you must explain why</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel13",
            label = "<p>Use this form when you’ve reached a settlement or do not want to continue with all or part of the claim.</p> <p>You can withdraw the whole claim or tell us which parts you want to withdraw.</p> <p>This brings the claim or part of it to an end.</p> <p>You can withdraw at any point before or during a hearing.</p> <p>If you’re withdrawing because of a settlement, make sure you have the settlement in writing.</p> <h3>Withdrawal and dismissal</h3> <p>Once you have told the tribunal you want to withdraw the claim, the tribunal will usually issue a judgment dismissing the claim or part of the claim.</p> <p>A dismissal judgment stops you from making that claim against the same respondents in the future.</p> <p>The tribunal will use Rule 51 of the Employment Tribunal Rules of Procedure to decide whether a dismissal judgment is issued following a withdrawal.</p> <p>Withdrawal and dismissal through this service will be final unless:</p> <ul><li>The tribunal contacts you needing more information</li><li>You have given an acceptable reason why dismissal should not happen</li></ul> <h3>Details to include in your application:</h3> <ul><li>Which parts of the claim you want withdrawn and dismissed</li><li>If you do not want the claim (or part of it) dismissed, you must explain why</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField039;

    @CCD(
            id = "claimantTseGuidanceLabel2",
            label = "<p>Use this form to apply to change the personal details given when making the claim.</p> <p>If you change the postal or email address, we’ll send any letters to the new address.</p> <p>If you change the telephone number, we’ll contact you using the new number if we have questions about the claim.</p> <h3>Details you can apply to change:</h3> <ul><li>Name</li><li>Sex and preferred title</li><li>Contact or home address</li><li>Telephone number</li><li>Email address</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel2",
            label = "<p>Use this form to apply to change the personal details given when making the claim.</p> <p>If you change the postal or email address, we’ll send any letters to the new address.</p> <p>If you change the telephone number, we’ll contact you using the new number if we have questions about the claim.</p> <h3>Details you can apply to change:</h3> <ul><li>Name</li><li>Sex and preferred title</li><li>Contact or home address</li><li>Telephone number</li><li>Email address</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField040;

    @CCD(
            id = "claimantTseGuidanceLabel3",
            label = "<p>Use this form to have a judgment or order made by a legal officer considered afresh by an Employment Judge.</p> <p>Considered afresh means that if you disagree with a judgment or order made by a legal officer in this case, you can ask an Employment Judge to look at that decision again.</p> <p>The judge will take the decision afresh on the basis of the same information as was before the legal officer, unless either side chooses to supply additional information.</p> <h3>Details to include in your application: </h3> <ul><li>The decision you want considered afresh</li><li>The date the tribunal issued the decision</li><li>Why you want the decision considered afresh</li><li>Any relevant additional information</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel3",
            label = "<p>Use this form to have a judgment or order made by a legal officer considered afresh by an Employment Judge.</p> <p>Considered afresh means that if you disagree with a judgment or order made by a legal officer in this case, you can ask an Employment Judge to look at that decision again.</p> <p>The judge will take the decision afresh on the basis of the same information as was before the legal officer, unless either side chooses to supply additional information.</p> <h3>Details to include in your application: </h3> <ul><li>The decision you want considered afresh</li><li>The date the tribunal issued the decision</li><li>Why you want the decision considered afresh</li><li>Any relevant additional information</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField041;

    @CCD(
            id = "claimantTseGuidanceLabel4",
            label = "<p>Tell or ask the tribunal about something relevant to your case.</p> <h3>Do not use this form to: </h3> <ul><li>Seek legal advice from the tribunal. The tribunal is an independent judicial body and cannot give legal advice</li><li>Tell us about settlement offers or discussions, which are private and confidential to the parties</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel4",
            label = "<p>Tell or ask the tribunal about something relevant to your case.</p> <h3>Do not use this form to: </h3> <ul><li>Seek legal advice from the tribunal. The tribunal is an independent judicial body and cannot give legal advice</li><li>Tell us about settlement offers or discussions, which are private and confidential to the parties</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField042;

    @CCD(
            id = "claimantTseGuidanceLabel5",
            label = "<p>You can ask the tribunal to order a witness to attend to give evidence.</p> <p>The witness must be in Great Britain (England, Scotland and Wales). This does not include Northern Ireland.</p> <p>The tribunal can limit the number of witnesses to be called to give evidence on a particular issue, especially if that issue is not central to the case.</p> <p>The claimant may also be liable for the costs incurred by the witness’s attendance.</p> <p>You should consider whether the evidence of this witness is likely to help your case.</p> <h3>Details to include in your application: </h3> <ul><li>The witness’s full name and address </li><li>Why the attendance of this witness is necessary for a fair hearing. The tribunal needs to understand why the evidence is relevant, and why there is no alternative way of establishing the same points at the hearing without ordering a witness to attend</li><li>If you have asked this witness to attend voluntarily. If you have, tell us when you asked and what their response was</li><li>If you have not asked the witness to attend yet, explain why</li></ul> <p>If the order is granted, the witness will be ordered to attend on the first day of the hearing. If you want them to attend on another day or days, tell us:</p> <ul><li>The dates apart from the first day of the hearing that you want the witness to attend</li><li>Why their attendance is necessary on those dates</li></ul> <p>If you want the witness to bring documents, tell us: </p> <ul><li>Why these documents are relevant to the issues in this case</li><li>Why an order to disclose the documents would not be enough</li><li>If you have already asked the witness or anyone else to provide these documents. If you have, tell us when you asked and what their response was</li><li>If you have not asked the witness to provide the documents yet, explain why</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel5",
            label = "<p>You can ask the tribunal to order a witness to attend to give evidence.</p> <p>The witness must be in Great Britain (England, Scotland and Wales). This does not include Northern Ireland.</p> <p>The tribunal can limit the number of witnesses to be called to give evidence on a particular issue, especially if that issue is not central to the case.</p> <p>The claimant may also be liable for the costs incurred by the witness’s attendance.</p> <p>You should consider whether the evidence of this witness is likely to help your case.</p> <h3>Details to include in your application: </h3> <ul><li>The witness’s full name and address </li><li>Why the attendance of this witness is necessary for a fair hearing. The tribunal needs to understand why the evidence is relevant, and why there is no alternative way of establishing the same points at the hearing without ordering a witness to attend</li><li>If you have asked this witness to attend voluntarily. If you have, tell us when you asked and what their response was</li><li>If you have not asked the witness to attend yet, explain why</li></ul> <p>If the order is granted, the witness will be ordered to attend on the first day of the hearing. If you want them to attend on another day or days, tell us:</p> <ul><li>The dates apart from the first day of the hearing that you want the witness to attend</li><li>Why their attendance is necessary on those dates</li></ul> <p>If you want the witness to bring documents, tell us: </p> <ul><li>Why these documents are relevant to the issues in this case</li><li>Why an order to disclose the documents would not be enough</li><li>If you have already asked the witness or anyone else to provide these documents. If you have, tell us when you asked and what their response was</li><li>If you have not asked the witness to provide the documents yet, explain why</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField043;

    @CCD(
            id = "claimantTseGuidanceLabel6",
            label = "<p>Use this form to ask the tribunal to order the respondent to do or provide something.</p> <h3>Details to include in your application: </h3> <ul><li>What you want the respondent to do</li><li>Why it is relevant to your claim</li><li>If you have already asked the respondent to do or provide this thing. If you have, tell us when you asked and what their response was</li><li>If you have not asked the respondent to do or provide this thing yet, explain why</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel6",
            label = "<p>Use this form to ask the tribunal to order the respondent to do or provide something.</p> <h3>Details to include in your application: </h3> <ul><li>What you want the respondent to do</li><li>Why it is relevant to your claim</li><li>If you have already asked the respondent to do or provide this thing. If you have, tell us when you asked and what their response was</li><li>If you have not asked the respondent to do or provide this thing yet, explain why</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField044;

    @CCD(
            id = "claimantTseGuidanceLabel7",
            label = "<p>Use this form to ask the tribunal to postpone a hearing to a later date.</p> <h3>Details to include in your application: </h3> <ul><li>which hearing you want to postpone. If the hearing you want to postpone is listed over multiple days, tell us which day or days you are applying to postpone</li><li>the reason you want to postpone. The tribunal will use this to decide whether to grant your application</li><li>any essential documents to support your application</li><li>Weekday dates within the next 3 months that you, your witnesses or representatives cannot attend a rescheduled hearing</li><li>The reason you cannot attend on those dates</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access213.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel7",
            label = "<p>Use this form to ask the tribunal to postpone a hearing to a later date.</p> <h3>Details to include in your application: </h3> <ul><li>which hearing you want to postpone. If the hearing you want to postpone is listed over multiple days, tell us which day or days you are applying to postpone</li><li>the reason you want to postpone. The tribunal will use this to decide whether to grant your application</li><li>any essential documents to support your application</li><li>Weekday dates within the next 3 months that you, your witnesses or representatives cannot attend a rescheduled hearing</li><li>The reason you cannot attend on those dates</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access213.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField045;

    @CCD(
            id = "claimantTseGuidanceLabel8",
            label = "<p>Use this form to have a judgment made by a tribunal reconsidered. This may be a judgment made by a judge alone or by a judge sitting with non-legal members.</p> <p>If reconsideration is necessary in the interests of justice you can ask the tribunal to look at the judgment again.</p> <p>Only a judgment which finally determines an issue in your case can be reconsidered. This therefore excludes any decision that does not comprise a final determination of the claim, or part of a claim. If you wish to ask for a case management order to be varied or revoked, use the vary or revoke an order application.</p> <p>You can only ask the tribunal to reconsider a judgment within 14 days of the date the judgment was sent to you.</p> <p>If you want the tribunal to reconsider a judgment that was sent to you over 14 days ago you must explain why your application is late.</p> <p>A reconsideration application does not affect the time limit for appealing to the Employment Appeal Tribunal.</p> <h3>Details to include in your application: </h3> <ul><li>The judgment you want reconsidered</li><li>The date the tribunal issued the judgment</li><li>Your reason for a late application if the judgment was sent over 14 days ago</li><li>Why it is in the interests of justice to reconsider this judgment</li><li>If the tribunal should vary or revoke the judgment</li><li>Any additional information or material which the tribunal does not already have to support your application</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel8",
            label = "<p>Use this form to have a judgment made by a tribunal reconsidered. This may be a judgment made by a judge alone or by a judge sitting with non-legal members.</p> <p>If reconsideration is necessary in the interests of justice you can ask the tribunal to look at the judgment again.</p> <p>Only a judgment which finally determines an issue in your case can be reconsidered. This therefore excludes any decision that does not comprise a final determination of the claim, or part of a claim. If you wish to ask for a case management order to be varied or revoked, use the vary or revoke an order application.</p> <p>You can only ask the tribunal to reconsider a judgment within 14 days of the date the judgment was sent to you.</p> <p>If you want the tribunal to reconsider a judgment that was sent to you over 14 days ago you must explain why your application is late.</p> <p>A reconsideration application does not affect the time limit for appealing to the Employment Appeal Tribunal.</p> <h3>Details to include in your application: </h3> <ul><li>The judgment you want reconsidered</li><li>The date the tribunal issued the judgment</li><li>Your reason for a late application if the judgment was sent over 14 days ago</li><li>Why it is in the interests of justice to reconsider this judgment</li><li>If the tribunal should vary or revoke the judgment</li><li>Any additional information or material which the tribunal does not already have to support your application</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField046;

    @CCD(
            id = "claimantTseGuidanceLabel9",
            label = "<p>Use this form to tell us that the respondent has not complied with all or part of an order from the tribunal.</p> <p>You should try to resolve your complaint with the respondent. Only use this form if that is not possible.</p> <h3>Details to include in your application: </h3> <ul><li>Which order has not been complied with</li><li>The date the tribunal issued the order</li><li>What the respondent has not done</li><li>What you want the tribunal to do next</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseGuidanceLabel9",
            label = "<p>Use this form to tell us that the respondent has not complied with all or part of an order from the tribunal.</p> <p>You should try to resolve your complaint with the respondent. Only use this form if that is not possible.</p> <h3>Details to include in your application: </h3> <ul><li>Which order has not been complied with</li><li>The date the tribunal issued the order</li><li>What the respondent has not done</li><li>What you want the tribunal to do next</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField047;

    @CCD(
            id = "claimantTseRule92TextArea",
            label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exceptional circumstances.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseRule92TextArea",
            label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exceptional circumstances.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField048;

    @CCD(
            id = "claimantTseRule92TextWhenRespOffline",
            label = "<h3>To copy this correspondence to the other party, you must send it to them by post or email. They cannot view it in this service.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseRule92TextWhenRespOffline",
            label = "<h3>To copy this correspondence to the other party, you must send it to them by post or email. They cannot view it in this service.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access001.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField049;

    @CCD(
            id = "claimantTseTableLabel",
            label = "${claimantTseTableMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access214.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimantTseTableLabel",
            label = "${claimantTseTableMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access214.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField050;

    @CCD(
            id = "claimant_Company ",
            label = "Company",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "claimant_Company ",
            label = "Company",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField051;


    @CCD(
            id = "closeReferralHearingDetailsLabel",
            label = "${closeReferralHearingDetails}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access063.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "closeReferralHearingDetailsLabel",
            label = "${closeReferralHearingDetails}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access083.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField053;

    @CCD(
            id = "componentLauncher",
            label = "Component Launcher (for displaying Case View categories)",
            typeOverride = FieldType.ComponentLauncher,
            searchable = false,
            access = SingleAccess.Access146.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "componentLauncher",
            label = "Component Launcher (for displaying Case View categories)",
            typeOverride = FieldType.ComponentLauncher,
            searchable = false,
            access = SingleAccess.Access180.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField054;

    @CCD(
            id = "confirmEt3Submit",
            label = "Do you want to submit this ET3?",
            hint = "If you wish to submit the ET3 to the tribunal, please select the option below and continue. You will not be able to make any further changes once submitted.",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_confirmSubmitEt3",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "confirmEt3Submit",
            label = "Do you want to submit this ET3?",
            hint = "If you wish to submit the ET3 to the tribunal, please select the option below and continue. You will not be able to make any further changes once submitted.",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_confirmSubmitEt3",
            searchable = false,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField055;

    @CCD(
            id = "createDigitalCaseFileLink",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/createDcf/createDcf1\">Create, Upload or Remove DCF</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "createDigitalCaseFileLink",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/createDcf/createDcf1\">Create, Upload or Remove DCF</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField056;

    @CCD(
            id = "dcfYesNo",
            label = "Do you want to automatically generate the DCF?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "dcfYesNo",
            label = "Do you want to automatically generate the DCF?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField057;

    @CCD(
            id = "deleteDraftCaseWarning1",
            label = "You are about the delete the draft claim for ${[CASE_REFERENCE]}.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access214.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "deleteDraftCaseWarning1",
            label = "You are about the delete the draft claim for ${[CASE_REFERENCE]}.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access214.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField058;

    @CCD(
            id = "deleteDraftCaseWarning2",
            label = "Once deleted, you will not be able to recover it.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access214.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "deleteDraftCaseWarning2",
            label = "Once deleted, you will not be able to recover it.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access214.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField059;

    @CCD(
            id = "deleteDraftCaseWarningLabel",
            label = "# Do you want to delete this draft claim?",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access214.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "deleteDraftCaseWarningLabel",
            label = "# Do you want to delete this draft claim?",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access214.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField060;


    @CCD(
            id = "didClaimantWorkAtSameAddressPreambleLabel",
            label = "${didClaimantWorkAtSameAddressPreamble}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "didClaimantWorkAtSameAddressPreambleLabel",
            label = "${didClaimantWorkAtSameAddressPreamble}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField062;

    @CCD(
            id = "downloadDraftEt3Label",
            label = "Click submit to download a draft copy of the ET3 form completed so far for the respondent selected.\n\nThis will not submit the ET3 to the tribunal. If you wish to submit the ET3, please use the Submit ET3 event",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access134.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "downloadDraftEt3Label",
            label = "Click submit to download a draft copy of the ET3 form completed so far for the respondent selected.\n\nThis will not submit the ET3 to the tribunal. If you wish to submit the ET3, please use the Submit ET3 event",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access134.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField063;

    @CCD(
            id = "draftAndSignJudgementLink",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/draftAndSignJudgement/draftAndSignJudgement1\">Draft and sign judgment/order</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access211.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "draftAndSignJudgementLink",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/draftAndSignJudgement/draftAndSignJudgement1\">Draft and sign judgment/order</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access212.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField064;

    @CCD(
            id = "ecmCaseLinkLabel",
            label = "ECM Case: ${ecmCaseLink}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access145.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "ecmCaseLinkLabel",
            label = "ECM Case: ${ecmCaseLink}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access176.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField065;

    @CCD(
            id = "emailDocsToAcasInstructions",
            label = "Instructions for the content of the email are on the 'Sending general correspondence to Acas via email' job card.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "emailDocsToAcasInstructions",
            label = "Instructions for the content of the email are on the 'Sending general correspondence to Acas via email' job card.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField066;

    @CCD(
            id = "emailDocsToAcasLine",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "emailDocsToAcasLine",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField067;

    @CCD(
            id = "emailDocsToAcasLink",
            label = "Attach and send document PDFs to Acas at [et3@acas.org.uk](${emailLinkToAcas})",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "emailDocsToAcasLink",
            label = "Attach and send document PDFs to Acas at [et3@acas.org.uk](${emailLinkToAcas})",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField068;

    @CCD(
            id = "emailDocsToAcasTitle",
            label = "<h2>Email documents to Acas</h2>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "emailDocsToAcasTitle",
            label = "<h2>Email documents to Acas</h2>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField069;

    @CCD(
            id = "et1AddressDetailsLabel",
            label = "${et1AddressDetails}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1AddressDetailsLabel",
            label = "${et1AddressDetails}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField070;

    @CCD(
            id = "et1ClaimStatusesLabel",
            label = "${et1ClaimStatuses}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access192.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1ClaimStatusesLabel",
            label = "${et1ClaimStatuses}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access192.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField071;

    @CCD(
            id = "et1DoNotSubmitDraftMessage",
            label = "Click submit to download a draft copy of the ET1 form completed so far",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1DoNotSubmitDraftMessage",
            label = "Click submit to download a draft copy of the ET1 form completed so far",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField072;

    @CCD(
            id = "et1JudgeReferralLine1",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access153.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1JudgeReferralLine1",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access164.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField073;

    @CCD(
            id = "et1JudgeReferralLine2",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access036.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1JudgeReferralLine2",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access039.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField074;

    @CCD(
            id = "et1OtherReferralLine1",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access036.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1OtherReferralLine1",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access039.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField075;

    @CCD(
            id = "et1OtherReferralLine2",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access036.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1OtherReferralLine2",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access039.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField076;

    @CCD(
            id = "et1REJOrVPReferralLine1",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access036.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1REJOrVPReferralLine1",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access039.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField077;

    @CCD(
            id = "et1REJOrVPReferralLine2",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access036.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1REJOrVPReferralLine2",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access039.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField078;

    @CCD(
            id = "et1ReppedTriageError",
            label = "The postcode you entered is not included under the early adopter sites yet.\n\nUntil your submitted location is supported, complete this <a href=\"https://www.claim-employment-tribunals.service.gov.uk\" target=\"_blank\">ET1 claim form</a>.\n\n<div class=\"govuk-inset-text\">\n  This service is only available to solicitors representing single claimants, for a limited number of claims within our early adopter sites who meet the current reform criteria.\n</div>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access192.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1ReppedTriageError",
            label = "The postcode you entered is not included under the early adopter sites yet.\n\nUntil your submitted location is supported, complete this <a href=\"https://www.claim-employment-tribunals.service.gov.uk\" target=\"_blank\">ET1 claim form</a>.\n\n<div class=\"govuk-inset-text\">\n  This service is only available to solicitors representing single claimants, for a limited number of claims within our four early adopter sites who meet the current reform criteria.\n</div>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access192.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField079;

    @CCD(
            id = "et1ReppedTriageLabel",
            label = "If the claimant works or worked at home occasionally or full time, enter the postcode of where they travel into work or where they would have travelled to.\n\nIf they're claiming against a respondent that they've not worked for - as best as you can, enter the postcode of where the respondent is based\n\n<a href=\"https://www.claim-employment-tribunals.service.gov.uk\" target=\"_blank\">Complete this ET1 claim form if the respondent's work location is outside the UK.</a>\n\n<a href=\"cases/case-create/EMPLOYMENT/ET_Scotland/et1ReppedCreateCase/et1ReppedCreateCase1\" target=\"_blank\">Complete this ET1 claim form if the the respondent's work location is in Scotland.</a>\n\n </div>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access192.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1ReppedTriageLabel",
            label = "If the claimant works or worked at home occasionally or full time, enter the postcode of where they travel into work or where they would have travelled to.\n\nIf they're claiming against a respondent that they've not worked for - as best as you can, enter the postcode of where the respondent is based.\n\n<a href=\"https://www.claim-employment-tribunals.service.gov.uk\" target=\"_blank\">Complete this ET1 claim form if the respondent's work location is outside the UK</a>\n\n<a href=\"cases/case-create/EMPLOYMENT/ET_EnglandWales/et1ReppedCreateCase/et1ReppedCreateCase1\" target=\"_blank\">Complete this ET1 claim form if the the respondent's work location is in England and Wales</a>\n\n<div class=\"govuk-inset-text\">\n  This service is only available to professional users presenting a single claim in Scotland.\n</div>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access192.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField080;

    @CCD(
            id = "et1Section1PreambleLabel",
            label = "In this section, you’ll need to provide:\n<ul>\n<li>your contact details (as the representative)</li>\n<li>the claimant or claimants’ contact details</li>\n<li>Acas details</li>\n<li>any support requirements for the party</li>\n<li>hearing format preferences</li>\n</ul>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1Section1PreambleLabel",
            label = "In this section, you’ll need to provide:\n<ul>\n<li>your contact details (as the representative)</li>\n<li>the claimant or claimants’ contact details</li>\n<li>Acas details</li>\n<li>any support requirements for the party</li>\n<li>hearing format preferences</li>\n</ul>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField081;

    @CCD(
            id = "et1Section2PreambleLabel",
            label = "In this section, you’ll need to provide:\n<ul>\n<li>employment status</li>\n<li>respondent details</li>\n</ul>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1Section2PreambleLabel",
            label = "In this section, you’ll need to provide:\n<ul>\n<li>employment status</li>\n<li>respondent details</li>\n</ul>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField082;

    @CCD(
            id = "et1SectionThreeDetailsPreamble",
            label = "Upload the claim details in a document or use the text box.\nYou can use both if you need to.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1SectionThreeDetailsPreamble",
            label = "Upload the claim details in a document or use the text box.\nYou can use both if you need to.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField083;

    @CCD(
            id = "et1SectionThreePreamble",
            label = "You’ll need to provide details of the claim. This can be uploaded in a document.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1SectionThreePreamble",
            label = "You’ll need to provide details of the claim. This can be uploaded in a document.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField084;

    @CCD(
            id = "et1TribunalRegionLabel",
            label = "<h3>${et1TribunalRegion} hearing venues</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1TribunalRegionLabel",
            label = "<h3>${et1TribunalRegion} hearing venues</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField085;

    @CCD(
            id = "et1VettingBeforeYouStartLabel",
            label = "${et1VettingBeforeYouStart}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingBeforeYouStartLabel",
            label = "${et1VettingBeforeYouStart}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField086;

    @CCD(
            id = "et1VettingClaimantDetailsLabel",
            label = "${et1VettingClaimantDetailsMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingClaimantDetailsLabel",
            label = "${et1VettingClaimantDetailsMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField087;

    @CCD(
            id = "et1VettingClaimantDetailsLabel2",
            label = "${et1VettingClaimantDetailsMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingClaimantDetailsLabel2",
            label = "${et1VettingClaimantDetailsMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField088;

    @CCD(
            id = "et1VettingContactDetailsLabel",
            label = "<hr><h2>Contact Details</h2>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access122.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingContactDetailsLabel",
            label = "<hr><h2>Contact Details</h2>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField089;

    @CCD(
            id = "et1VettingJurCodeList",
            label = " ",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_jurisdictionCodes",
            searchable = false,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingJurCodeList",
            label = " ",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_jurisdictionCodes",
            searchable = false,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField090;

    @CCD(
            id = "et1VettingRespondentAcasDetailsLabel1",
            label = "${et1VettingRespondentAcasDetails1}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access045.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingRespondentAcasDetailsLabel1",
            label = "${et1VettingRespondentAcasDetails1}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access047.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField091;

    @CCD(
            id = "et1VettingRespondentAcasDetailsLabel2",
            label = "${et1VettingRespondentAcasDetails2}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access045.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingRespondentAcasDetailsLabel2",
            label = "${et1VettingRespondentAcasDetails2}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access047.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField092;

    @CCD(
            id = "et1VettingRespondentAcasDetailsLabel3",
            label = "${et1VettingRespondentAcasDetails3}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access045.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingRespondentAcasDetailsLabel3",
            label = "${et1VettingRespondentAcasDetails3}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access047.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField093;

    @CCD(
            id = "et1VettingRespondentAcasDetailsLabel5",
            label = "${et1VettingRespondentAcasDetails5}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access045.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingRespondentAcasDetailsLabel5",
            label = "${et1VettingRespondentAcasDetails5}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access047.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField094;

    @CCD(
            id = "et1VettingRespondentAcasDetailsLabel6",
            label = "${et1VettingRespondentAcasDetails6}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access045.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingRespondentAcasDetailsLabel6",
            label = "${et1VettingRespondentAcasDetails6}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access047.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField095;

    @CCD(
            id = "et1VettingRespondentDetailsLabel",
            label = "${et1VettingRespondentDetailsMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et1VettingRespondentDetailsLabel",
            label = "${et1VettingRespondentDetailsMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField096;

    @CCD(
            id = "et3ClaimantTaskListChecks",
            label = "Task list check",
            typeNameOverride = "TaskListCheck",
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ClaimantTaskListChecks",
            label = "Task list check",
            typeNameOverride = "TaskListCheck",
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField097;

    @CCD(
            id = "et3ContactDetailsAddressLabel",
            label = "<hr><h2>Contact details</h2><hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ContactDetailsAddressLabel",
            label = "<hr><h2>Contact details</h2><hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField098;

    @CCD(
            id = "et3ContactDetailsAddressMismatchLabel",
            label = "<hr><h2>Contact details</h2><hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ContactDetailsAddressMismatchLabel",
            label = "<hr><h2>Contact details</h2><hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField099;

    @CCD(
            id = "et3ContactDetailsLabel",
            label = "<hr><h2>Contact details</h2><hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ContactDetailsLabel",
            label = "<hr><h2>Contact details</h2><hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField100;

    @CCD(
            id = "et3ContactDetailsNameMismatchLabel",
            label = "<hr><h2>Contact details</h2><hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ContactDetailsNameMismatchLabel",
            label = "<hr><h2>Contact details</h2><hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField101;

    @CCD(
            id = "et3DateCompanyHousePage",
            label = "${et3Date}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3DateCompanyHousePage",
            label = "${et3Date}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField102;

    @CCD(
            id = "et3DateIndividualInsolvency",
            label = "${et3Date}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3DateIndividualInsolvency",
            label = "${et3Date}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField103;

    @CCD(
            id = "et3DateLabel",
            label = "${et3Date}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3DateLabel",
            label = "${et3Date}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField104;

    @CCD(
            id = "et3EmailDocsToAcasInstructions",
            label = "Instructions for the content of the email are on the 'Sending general correspondence to Acas via email' job card.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access115.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3EmailDocsToAcasInstructions",
            label = "Instructions for the content of the email are on the 'Sending general correspondence to Acas via email' job card.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access119.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField105;

    @CCD(
            id = "et3EmailDocsToAcasLink",
            label = "Attach and send document PDFs to Acas at [ET3@acas.org.uk](${et3EmailLinkToAcas})",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access115.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3EmailDocsToAcasLink",
            label = "Attach and send document PDFs to Acas at [ET3@acas.org.uk](${et3EmailLinkToAcas})",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access119.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField106;

    @CCD(
            id = "et3EmailDocsToAcasTitle",
            label = "<hr>\n<h2>Email documents to Acas</h2>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access115.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3EmailDocsToAcasTitle",
            label = "<hr>\n<h2>Email documents to Acas</h2>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access119.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField107;

    @CCD(
            id = "et3HearingDetailsLabel",
            label = "${et3HearingDetails}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3HearingDetailsLabel",
            label = "${et3HearingDetails}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField108;

    @CCD(
            id = "et3NameAddressRespondentAddressLabel",
            label = "${et3NameAddressRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3NameAddressRespondentAddressLabel",
            label = "${et3NameAddressRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField109;

    @CCD(
            id = "et3NameAddressRespondentAddressMismatchLabel",
            label = "${et3NameAddressRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3NameAddressRespondentAddressMismatchLabel",
            label = "${et3NameAddressRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField110;

    @CCD(
            id = "et3NameAddressRespondentLabel",
            label = "${et3NameAddressRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3NameAddressRespondentLabel",
            label = "${et3NameAddressRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField111;

    @CCD(
            id = "et3NameAddressRespondentNameMismatchLabel",
            label = "${et3NameAddressRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3NameAddressRespondentNameMismatchLabel",
            label = "${et3NameAddressRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField112;

    @CCD(
            id = "et3OtherTypeDocumentNameLabel",
            label = "${et3OtherTypeDocumentName}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access115.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3OtherTypeDocumentNameLabel",
            label = "${et3OtherTypeDocumentName}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access118.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField113;

    @CCD(
            id = "et3RepresentativeInfoFirstWords",
            label = "In most circumstances the tribunal will communicate with you (as the representative) by email. If you need communication by post, please tell us why.",
            typeOverride = FieldType.Label,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3RepresentativeInfoFirstWords",
            label = "In most circumstances the tribunal will communicate with you (as the representative) by email. If you need communication by post, please tell us why.",
            typeOverride = FieldType.Label,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField114;

    @CCD(
            id = "et3ResponseClaimantNameLabel",
            label = "${et3ResponseClaimantName}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ResponseClaimantNameLabel",
            label = "${et3ResponseClaimantName}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField115;

    @CCD(
            id = "et3ResponseContestClaimPreamble",
            label = "Consider in your response:\n\n* setting out the aspects of the claim you agree or disagree with, including if you're contesting only part of the claim\n* making sure that any facts or events are in date order\n* whether you think another person or company may be liable and why\n\nYou can upload a statement of case below.\n\nFile should be a maximum of 100MB in size. If there is a need of uploading several documents, they should be made into one and uploaded as one document.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ResponseContestClaimPreamble",
            label = "Consider in your response:\n\n* setting out the aspects of the claim you agree or disagree with, including if you're contesting only part of the claim\n* making sure that any facts or events are in date order\n* whether you think another person or company may be liable and why\n\nYou can upload a statement of case below.\n\nFile should be a maximum of 100MB in size. If there is a need of uploading several documents, they should be made into one and uploaded as one document.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField116;

    @CCD(
            id = "et3ResponseEmployerClaimDetailsPreamble",
            label = "<h3>Consider in your answer:</h3>\n\n* the background and details of your claim\n* all important dates\n*the value of your claim and how you calculated it",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ResponseEmployerClaimDetailsPreamble",
            label = "<h3>Consider in your answer:</h3>\n\n* the background and details of your claim\n* all important dates\n*the value of your claim and how you calculated it",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField117;

    @CCD(
            id = "et3ResponseHealthDetailsPreamble",
            label = "<h3>Consider in your answer:</h3>\n\nTell us what this disability or condition is and what support that anyone in the respondent party, including representative and witnesses would need as the claim progresses through the system. Consider any hearings that may take place at tribunal buildings.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ResponseHealthDetailsPreamble",
            label = "<h3>Consider in your answer:</h3>\n\nTell us what this disability or condition is and what support that anyone in the respondent party, including representative and witnesses would need as the claim progresses through the system. Consider any hearings that may take place at tribunal buildings.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField118;

    @CCD(
            id = "et3ResponseHealthInsetPreamble",
            label = "<details class=\"govuk-details\"> <summary class=\"govuk-details__summary\"><span class=\"govuk-details__summary-text\"> What support is available? </span></summary> <div class=\"govuk-details__text\"> We know people with disabilities sometimes need support to access information and use our services. We call this a reasonable adjustment. Some reasonable adjustments need to be agreed by a judge, and you can discuss with the tribunal if your needs change.<br><br>Reasonable adjustments can include:<br><br>\n\n* documents in alternative formats, colours and fonts\n* help with communicating, sight, hearing, speaking and interpretation\n* access and mobility support if a hearing takes place in person </div> </details>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ResponseHealthInsetPreamble",
            label = "<details class=\"govuk-details\"> <summary class=\"govuk-details__summary\"><span class=\"govuk-details__summary-text\"> What support is available? </span></summary> <div class=\"govuk-details__text\"> We know people with disabilities sometimes need support to access information and use our services. We call this a reasonable adjustment. Some reasonable adjustments need to be agreed by a judge, and you can discuss with the tribunal if your needs change.<br><br>Reasonable adjustments can include:<br><br>\n\n* documents in alternative formats, colours and fonts\n* help with communicating, sight, hearing, speaking and interpretation\n* access and mobility support if a hearing takes place in person </div> </details>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField119;

    @CCD(
            id = "et3ResponseHearingPreamble",
            label = "The parties can express their preference of in-person, video or phone hearings.\n\nRequests have to be agreed by a judge and it can depend on the type of hearing. If the case goes to a final hearing, this will normally be in-person but parties will be informed in advance.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ResponseHearingPreamble",
            label = "The parties can express their preference of in-person, video or phone hearings.\n\nRequests have to be agreed by a judge and it can depend on the type of hearing. If the case goes to a final hearing, this will normally be in-person but parties will be informed in advance.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField120;

    @CCD(
            id = "et3ResponseInTimeDateLabel",
            label = "${et3Date}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access071.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ResponseInTimeDateLabel",
            label = "${et3Date}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access105.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField121;

    @CCD(
            id = "et3ResponseNameInset",
            label = "If you consider another person or company may be liable, you should still submit a response.\n\nYou will have a chance in this form to explain why you think someone else may be liable.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ResponseNameInset",
            label = "If you consider another person or company may be liable, you should still submit a response.\n\nYou will have a chance in this form to explain why you think someone else may be liable.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField122;

    @CCD(
            id = "et3ResponseNamePreamble",
            label = "This will be the respondent recorded in ET1 that you're representing.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ResponseNamePreamble",
            label = "This will be the respondent recorded in ET1 that you're representing.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField123;

    @CCD(
            id = "et3ResponsePayDetailsPreamble",
            label = "If pay details are provided on the ET1 form, check whether the pay is weekly, monthly or annual. Use the same time period here.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3ResponsePayDetailsPreamble",
            label = "If pay details are provided on the ET1 form, check whether the pay is weekly, monthly or annual. Use the same time period here.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField124;

    @CCD(
            id = "et3SelectAllThatApply",
            label = "Select all that apply.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access115.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3SelectAllThatApply",
            label = "Select all that apply.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access118.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField125;

    @CCD(
            id = "et3StartPageInset",
            label = "This response must be completed and submitted within 28 days of the date of the claim form being sent by the tribunal.\n\nYou must provide your response to the claim even if you believe that another respondent is liable.\n\nIf you do not provide a response, a judgment may be issued against you without a hearing. If you consider another person or company maybe liable, you should still submit a response. You will have a chance in this response to explain why you think someone else may be liable.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3StartPageInset",
            label = "This response must be completed and submitted within 28 days of the date of the claim form being sent by the tribunal.\n\nYou must provide your response to the claim even if you believe that another respondent is liable.\n\nIf you do not provide a response, a judgment may be issued against you without a hearing. If you consider another person or company maybe liable, you should still submit a response. You will have a chance in this response to explain why you think someone else may be liable.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField126;

    @CCD(
            id = "et3StartPageMainBody",
            label = "It will help the tribunal and you as the representative if you have: \n\n* the claimant's employment start and end dates, hours of work and notice period\n* details of claimant's pay and benefits, before and after tax\n* your response to the claim, which you can upload in document format with a text field for accompanying information\n\n### How to fill in this form\n\n* read the questions carefully to make sure you're providing your details (as the representative) or the respondent's details\n* optional questions will be marked as such\n* you can review and edit your answers before you submit the form",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3StartPageMainBody",
            label = "It will help the tribunal and you as the representative if you have: \n\n* the claimant's employment start and end dates, hours of work and notice period\n* details of claimant's pay and benefits, before and after tax\n* your response to the claim, which you can upload in document format with a text field for accompanying information\n\n### How to fill in this form\n\n* read the questions carefully to make sure you're providing your details (as the representative) or the respondent's details\n* optional questions will be marked as such\n* you can review and edit your answers before you submit the form",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField127;

    @CCD(
            id = "et3StartPagePreamble",
            label = "To help you complete this, open the ET1 form, ACAS certificate and other documents in the <strong>Case Documents</strong> event in the next steps menu.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3StartPagePreamble",
            label = "To help you complete this, open the ET1 form, ACAS certificate and other documents in the <strong>Case Documents</strong> event in the next steps menu.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField128;

    @CCD(
            id = "et3TribunalLocationLabel",
            label = "${et3TribunalLocation}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3TribunalLocationLabel",
            label = "${et3TribunalLocation}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access091.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField129;

    @CCD(
            id = "et3VettingBeforeYouStart",
            label = "To help you complete this, open the ET1 form and additional documents the claimant or respondent may have uploaded in the <a href=\"/cases/case-details/${[CASE_REFERENCE]}#Documents\" target=\"_blank\"> Documents tab (opens in a new tab)</a>.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access115.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "et3VettingBeforeYouStart",
            label = "To help you complete this, open the ET1 form and additional documents the claimant or respondent may have uploaded in the <a href=\"/cases/case-details/${[CASE_REFERENCE]}#Documents\" target=\"_blank\"> Documents tab (opens in a new tab)</a>.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access119.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField130;

    @CCD(
            id = "etICAnyOtherDirectionsDividerHrLabel",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField131;

    @CCD(
            id = "etICAnyOtherDirectionsDividerHrLabel3",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField132;

    @CCD(
            id = "etICFurtherInfoHearingLabel",
            label = "${etInitialConsiderationHearing}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access077.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField133;

    @CCD(
            id = "etICFurtherInfoJurisdictionCodesLabel",
            label = "${etInitialConsiderationJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access077.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField134;

    @CCD(
            id = "etICFurtherInfoRespondentLabel",
            label = "${etInitialConsiderationRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access077.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField135;

    @CCD(
            id = "etICFurtherInformationHearingAnyOtherDirectionsLabel",
            label = "<h3>Any other directions (Optional)</h3><p>Are there any other issues or instructions to consider, or further orders to give?</p><p>This could include:</p><ul><li>Rule 49</li><li>Interpreters</li><li>Adjustments required for hearings</li><li>Further information required</li><li>Employer’s Contract Claim</li><li>Respondent’s identity</li><li>Time limits: claim or response</li></ul>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField136;

    @CCD(
            id = "etICHearingFurtherInfo",
            label = "${etInitialConsiderationHearing}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICHearingFurtherInfo",
            label = "${etInitialConsiderationHearing}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField137;

    @CCD(
            id = "etICHearingHearingListed",
            label = "${etInitialConsiderationHearing}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICHearingHearingListed",
            label = "${etInitialConsiderationHearing}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField138;

    @CCD(
            id = "etICHearingListedHearingLabel",
            label = "${etInitialConsiderationHearing}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access077.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField139;

    @CCD(
            id = "etICHearingListedJurisdictionCodesLabel",
            label = "${etInitialConsiderationJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access077.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField140;

    @CCD(
            id = "etICHearingListedRespondentLabel",
            label = "${etInitialConsiderationRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access077.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField141;

    @CCD(
            id = "etICHearingNotListed",
            label = "Hearing NOT Listed",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField142;

    @CCD(
            id = "etICHearingNotListedAnyOtherDirectionsLabel",
            label = "<h3>Any other directions (Optional)</h3><p>Are there any other issues or instructions to consider, or further orders to give?</p><p>This could include:</p><ul><li>Rule 49</li><li>Interpreters</li><li>Adjustments required for hearings</li><li>Further information required</li><li>Employer’s Contract Claim</li><li>Respondent’s identity</li><li>Time limits: claim or response</li></ul>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField143;

    @CCD(
            id = "etICHearingNotListedHearingLabel",
            label = "${etInitialConsiderationHearing}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access117.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICHearingNotListedHearingLabel",
            label = "${etInitialConsiderationHearing}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField144;

    @CCD(
            id = "etICHearingNotListedJurCodesLabel",
            label = "${etInitialConsiderationJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access117.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICHearingNotListedJurCodesLabel",
            label = "${etInitialConsiderationJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField145;

    @CCD(
            id = "etICHearingNotListedListUpdatedDividerHrLabel",
            label = "<hr>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField146;

    @CCD(
            id = "etICHearingNotListedRespondentLabel",
            label = "${etInitialConsiderationRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access117.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICHearingNotListedRespondentLabel",
            label = "${etInitialConsiderationRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField147;

    @CCD(
            id = "etICIssuesArisingFromVettingLabel",
            label = "<br><hr><h1>Issues identified at ET1 vetting and ET3 processing</h1>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICIssuesArisingFromVettingLabel",
            label = "<br><hr><h1>Issues identified at ET1 vetting and ET3 processing</h1>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField148;

    @CCD(
            id = "etICJurisdictionCodesFurtherInfo",
            label = "${etInitialConsiderationJurisdictionCodes}",
            typeOverride = FieldType.Label,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICJurisdictionCodesFurtherInfo",
            label = "${etInitialConsiderationJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField149;

    @CCD(
            id = "etICJurisdictionCodesHearingListed",
            label = "${etInitialConsiderationJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICJurisdictionCodesHearingListed",
            label = "${etInitialConsiderationJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField150;

    @CCD(
            id = "etICMinimumInfoFromVettingLabel",
            label = "<hr><h2>ET1 Vetting Issues</h2>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICMinimumInfoFromVettingLabel",
            label = "<hr><h2>ET1 Vetting Issues</h2>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField151;

    @CCD(
            id = "etICNavigationButtonsDividerHrLabel",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField152;

    @CCD(
            id = "etICNavigationButtonsDividerHrLabel3",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField153;

    @CCD(
            id = "etICRespondentFurtherInfo",
            label = "${etInitialConsiderationRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICRespondentFurtherInfo",
            label = "${etInitialConsiderationRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField154;

    @CCD(
            id = "etICRespondentHearingListed",
            label = "${etInitialConsiderationRespondent}",
            typeOverride = FieldType.Label,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etICRespondentHearingListed",
            label = "${etInitialConsiderationRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField155;

    @CCD(
            id = "etICUploadDocDividerHrLabel",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField156;

    @CCD(
            id = "etICUploadDocDividerHrLabel3",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField157;

    @CCD(
            id = "etIcHearingPanelPreferenceLabel",
            label = "${etIcHearingPanelPreference}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etIcHearingPanelPreferenceLabel",
            label = "${etIcHearingPanelPreference}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField158;

    @CCD(
            id = "etIcPartiesHearingFormatLabel",
            label = "${etIcPartiesHearingFormat}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etIcPartiesHearingFormatLabel",
            label = "${etIcPartiesHearingFormat}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField159;

    @CCD(
            id = "etIcPartiesHearingPanelPreferenceHeaderLabel",
            label = "${etIcPartiesHearingPanelPreferenceHeader}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etIcPartiesHearingPanelPreferenceHeaderLabel",
            label = "${etIcPartiesHearingPanelPreferenceHeader}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField160;

    @CCD(
            id = "etIcPartiesHearingPanelPreferenceLabel",
            label = "${etIcPartiesHearingPanelPreference}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etIcPartiesHearingPanelPreferenceLabel",
            label = "${etIcPartiesHearingPanelPreference}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField161;

    @CCD(
            id = "etInitialConsiderationHearingLabel",
            label = "${etInitialConsiderationHearing}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etInitialConsiderationHearingLabel",
            label = "${etInitialConsiderationHearing}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField162;

    @CCD(
            id = "etInitialConsiderationJurisdictionCodesLabel",
            label = "${etInitialConsiderationJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etInitialConsiderationJurisdictionCodesLabel",
            label = "${etInitialConsiderationJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField163;

    @CCD(
            id = "etInitialConsiderationRespondentLabel",
            label = "${etInitialConsiderationRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "etInitialConsiderationRespondentLabel",
            label = "${etInitialConsiderationRespondent}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField164;

    @CCD(
            id = "existingJurisdictionCodesLabel",
            label = "${existingJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "existingJurisdictionCodesLabel",
            label = "${existingJurisdictionCodes}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField165;

    @CCD(
            id = "flagLauncher",
            label = "Flag Launcher",
            typeNameOverride = "FlagLauncher",
            access = SingleAccess.Access149.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "flagLauncher",
            label = "Flag Launcher",
            typeNameOverride = "FlagLauncher",
            searchable = false,
            access = SingleAccess.Access186.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField166;

    @CCD(
            id = "flagsImagePlaceHolder",
            label = "<h2>${flagsImageAltText}</h2>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access053.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "flagsImagePlaceHolder",
            label = "<h2>${flagsImageAltText}</h2>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access090.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField167;

    @CCD(
            id = "hearingAndReferralDetailsLabel",
            label = "${hearingAndReferralDetails}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "hearingAndReferralDetailsLabel",
            label = "${hearingAndReferralDetails}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access121.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField168;

    @CCD(
            id = "hearingFormatPreamble",
            label = "The parties can express their preference of phone, video or in person hearings. Requests have to be agreed by a judge and it can depend on the type of hearing.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "hearingFormatPreamble",
            label = "The parties can express their preference of phone, video or in person hearings. Requests have to be agreed by a judge and it can depend on the type of hearing.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField169;

    @CCD(
            id = "horizontalLine",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access074.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "horizontalLine",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access107.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField170;

    @CCD(
            id = "horizontalLine2",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access075.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "horizontalLine2",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access108.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField171;

    @CCD(
            id = "horizontalLine3",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access075.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "horizontalLine3",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access108.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField172;

    @CCD(
            id = "horizontalLine4",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access074.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "horizontalLine4",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access107.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField173;

    @CCD(
            id = "horizontalLine5",
            label = "<hr>",
            typeOverride = FieldType.Label,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField174;

    @CCD(
            id = "icEt1VettingIssuesBottomDividerHrLabel",
            label = "<hr>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "icEt1VettingIssuesBottomDividerHrLabel",
            label = "<hr>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField175;

    @CCD(
            id = "icEt1VettingIssuesDetailLabel",
            label = "${icEt1VettingIssuesDetail}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "icEt1VettingIssuesDetailLabel",
            label = "${icEt1VettingIssuesDetail}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField176;

    @CCD(
            id = "icEt1VettingIssuesDividerHrLabel",
            label = "<hr><h2>ET1 Vetting Issues</h2>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "icEt1VettingIssuesDividerHrLabel",
            label = "<hr><h2>ET1 Vetting Issues</h2>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField177;

    @CCD(
            id = "icEt3IsThereAnEt3ResponseLabel",
            label = "${et3IsThereAnEt3Response}",
            typeOverride = FieldType.Label,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "icEt3IsThereAnEt3ResponseLabel",
            label = "${et3IsThereAnEt3Response}",
            typeOverride = FieldType.Label,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField178;

    @CCD(
            id = "icEt3ProcessingIssuesBottomDividerHrLabel",
            label = "<hr>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "icEt3ProcessingIssuesBottomDividerHrLabel",
            label = "<hr>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField179;

    @CCD(
            id = "icEt3ProcessingIssuesDetailDividerHrLabel",
            label = "<hr><h2>ET3 Processing Issues</h2>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "icEt3ProcessingIssuesDetailDividerHrLabel",
            label = "<hr><h2>ET3 Processing Issues</h2>",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField180;

    @CCD(
            id = "icEt3ProcessingIssuesDetailLabel",
            label = "${icEt3ProcessingIssuesDetail}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access157.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "icEt3ProcessingIssuesDetailLabel",
            label = "${icEt3ProcessingIssuesDetail}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access178.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField181;

    @CCD(
            id = "icRespondentHearingPanelPreferenceLabel",
            label = "${icRespondentHearingPanelPreference}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "icRespondentHearingPanelPreferenceLabel",
            label = "${icRespondentHearingPanelPreference}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField182;

    @CCD(
            id = "initialConsiderationBeforeYouStartLabel",
            label = "${initialConsiderationBeforeYouStart}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "initialConsiderationBeforeYouStartLabel",
            label = "${initialConsiderationBeforeYouStart}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access043.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField183;

    @CCD(
            id = "initialConsiderationBeforeYouStartLabel2",
            label = "${initialConsiderationBeforeYouStart}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "initialConsiderationBeforeYouStartLabel2",
            label = "${initialConsiderationBeforeYouStart}",
            typeOverride = FieldType.Label,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField184;

    @CCD(
            id = "initialConsiderationBeforeYouStartLabel3",
            label = "${initialConsiderationBeforeYouStart}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "initialConsiderationBeforeYouStartLabel3",
            label = "${initialConsiderationBeforeYouStart}",
            typeOverride = FieldType.Label,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField185;

    @CCD(
            id = "initialConsiderationBeforeYouStartLabel4",
            label = "${initialConsiderationBeforeYouStart}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access160.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField186;

    @CCD(
            id = "initialConsiderationTabTitle",
            label = "### Initial Consideration",
            typeOverride = FieldType.Label,
            securityClassification = "PUBLIC",
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    private Object singleDefinitionField187;

    @CCD(
            id = "jonsLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/viewAllNotifications/viewAllNotifications1\">All judgments, orders and notifications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/pseRespondentViewNotification/pseRespondentViewNotification1\">View a judgment, order or notification</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/pseRespondentRespondToTribunal/pseRespondentRespondToTribunal1\">Respond to an order or request from the tribunal</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access023.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "jonsLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/viewAllNotifications/viewAllNotifications1\">All judgments, orders and notifications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/pseRespondentViewNotification/pseRespondentViewNotification1\">View a judgment, order or notification</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/pseRespondentRespondToTribunal/pseRespondentRespondToTribunal1\">Respond to an order or request from the tribunal</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access022.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField188;

    @CCD(
            id = "jonsTab",
            label = "#### Judgments, orders & notifications",
            typeOverride = FieldType.Label,
            securityClassification = "PUBLIC",
            access = SingleAccess.Access023.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "jonsTab",
            label = "#### Judgments, orders & notifications",
            typeOverride = FieldType.Label,
            securityClassification = "PUBLIC",
            access = SingleAccess.Access022.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField189;

    @CCD(
            id = "legalRepDocumentsMarkdownLabel",
            label = "${legalRepDocumentsMarkdown}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access113.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "legalRepDocumentsMarkdownLabel",
            label = "${legalRepDocumentsMarkdown}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access113.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField190;

    @CCD(
            id = "linkedCaseCTLabel",
            label = "Case Transfer: ${linkedCaseCT} ${transferredCaseLink}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access142.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "linkedCaseCTLabel",
            label = "Case Transfer: ${linkedCaseCT} ${transferredCaseLink}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access173.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField191;

    @CCD(
            id = "linkedCasesPreamble",
            label = "Tell us if there are any existing cases this claim could be linked to.\n\nThis could be: \n\n* a case or cases you have already brought\n* a case or cases brought by other people against the same employer with the same or similar circumstances\n\nThis will help the tribunal consider whether the cases should be linked in any way.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "linkedCasesPreamble",
            label = "Tell us if there are any existing cases this claim could be linked to.\n\nThis could be: \n\n* a case or cases you have already brought\n* a case or cases brought by other people against the same employer with the same or similar circumstances\n\nThis will help the tribunal consider whether the cases should be linked in any way.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField192;

    @CCD(
            id = "listedDateInPastWarningLabel",
            label = "<h3>One of the listed dates are in the past. If you want to change it please click Previous and enter a date after today otherwise click Continue.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "listedDateInPastWarningLabel",
            label = "<h3>One of the listed dates are in the past. If you want to change it please click Previous and enter a date after today otherwise click Continue.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField193;

    @CCD(
            id = "listedHearingPartiesHearingFormatLabel",
            label = "${etIcPartiesHearingFormat}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "listedHearingPartiesHearingFormatLabel",
            label = "${etIcPartiesHearingFormat}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField194;

    @CCD(
            id = "listedHearingPartiesPanelPreferenceLabel",
            label = "${etIcPartiesHearingPanelPreference}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "listedHearingPartiesPanelPreferenceLabel",
            label = "${etIcPartiesHearingPanelPreference}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField195;

    @CCD(
            id = "multipleLeadClaim",
            label = "<h1>LEAD CLAIM</h1><p>This case is assigned as the lead case in a group claim<p><h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access203.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            id = "multipleLeadClaim",
            label = "<h1>LEAD CLAIM</h1><p>This case is assigned as the lead case in a group claim<p><h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access206.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private Object singleDefinitionField196;

    @CCD(
            id = "multipleNotStayedCase",
            label = "<h1></h1><p>This case is linked to a group claim<p><h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access203.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            id = "multipleNotStayedCase",
            label = "<h1></h1><p>This case is linked to a group claim<p><h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access206.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private Object singleDefinitionField197;

    @CCD(
            id = "multipleReferenceLinkLabel",
            label = "<h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access138.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            id = "multipleReferenceLinkLabel",
            label = "<h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access169.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    @CCD(
            id = "multipleReferenceLinkLabel",
            label = "<h3> Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access142.class,
            includeInProfiles = EnglandWalesSingleProdDefinition.class
    )
    @CCD(
            id = "multipleReferenceLinkLabel",
            label = "<h3> Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access173.class,
            includeInProfiles = ScotlandSingleProdDefinition.class
    )
    private Object singleDefinitionField198;

    @CCD(
            id = "multipleStayedCase",
            label = "<h1>STAYED CASE</h1><p>This case is stayed and linked to a group claim<p><h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access203.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            id = "multipleStayedCase",
            label = "<h1>STAYED CASE</h1><p>This case is stayed and linked to a group claim<p><h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access206.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private Object singleDefinitionField199;

    @CCD(
            id = "notListedHearingPartiesHearingFormatLabel",
            label = "${etIcPartiesHearingFormat}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "notListedHearingPartiesHearingFormatLabel",
            label = "${etIcPartiesHearingFormat}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField200;

    @CCD(
            id = "notListedHearingPartiesPanelPreferenceLabel",
            label = "${etIcPartiesHearingPanelPreference}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access072.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "notListedHearingPartiesPanelPreferenceLabel",
            label = "${etIcPartiesHearingPanelPreference}",
            typeOverride = FieldType.Label,
            searchable = false,
            access = SingleAccess.Access106.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField201;

    @CCD(
            id = "notificationMarkdownLabel",
            label = "${notificationMarkdown}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "notificationMarkdownLabel",
            label = "${notificationMarkdown}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField202;

    @CCD(
            id = "notificationsTabLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/sendNotification/sendNotification1\">Send a notification</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/respondNotification/respondNotification1\">Respond to a notification</a><br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/generateNotificationSummary/generateNotificationSummary1\">Generate Notification Summary</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "notificationsTabLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/sendNotification/sendNotification1\">Send a notification</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/respondNotification/respondNotification1\">Respond to a notification</a><br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/generateNotificationSummary/generateNotificationSummary1\">Generate Notification Summary</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField203;

    @CCD(
            id = "otherTypeDocumentNameLabel",
            label = "${otherTypeDocumentName}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "otherTypeDocumentNameLabel",
            label = "${otherTypeDocumentName}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField204;

    @CCD(
            id = "pcqId",
            label = "PCQ ID",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access217.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "pcqId",
            label = "PCQ ID",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access216.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField205;

    @CCD(
            id = "printAndSendPaperDocuments",
            label = "<hr><h2>Print and send paper documents<h2>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "printAndSendPaperDocuments",
            label = "<hr><h2>Print and send paper documents<h2>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField206;

    @CCD(
            id = "pseRespondentOrdReqCopyPartyIntro",
            label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exception circumstances.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "pseRespondentOrdReqCopyPartyIntro",
            label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exception circumstances.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField207;

    @CCD(
            id = "pseRespondentOrdReqPage2TableLabel",
            label = "Consider when providing material that:<br><ul><li>you can upload letters, photos or documents to support your application</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "pseRespondentOrdReqPage2TableLabel",
            label = "Consider when providing material that:<br><ul><li>you can upload letters, photos or documents to support your application</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access085.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField208;

    @CCD(
            id = "pseRespondentRequestOrderTableLabel",
            label = "${pseRespondentOrdReqTableMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access207.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "pseRespondentRequestOrderTableLabel",
            label = "${pseRespondentOrdReqTableMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access207.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField209;

    @CCD(
            id = "pseViewNotificationsLabel",
            label = "${pseViewNotifications}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access049.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "pseViewNotificationsLabel",
            label = "${pseViewNotifications}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access086.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField210;

    @CCD(
            id = "referralHearingDetailsLabel",
            label = "${referralHearingDetails}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "referralHearingDetailsLabel",
            label = "${referralHearingDetails}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access121.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField211;

    @CCD(
            id = "referralLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/createReferral/createReferral1\">Send a new referral</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/updateReferral/updateReferral1\">Update a referral</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/replyToReferral/replyToReferral1\">Reply to a referral</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/closeReferral/closeReferral1\">Close a referral</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access202.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "referralLinks",
            label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/createReferral/createReferral1\">Send a new referral</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/updateReferral/updateReferral1\">Update a referral</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/replyToReferral/replyToReferral1\">Reply to a referral</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/closeReferral/closeReferral1\">Close a referral</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access205.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField212;

    @CCD(
            id = "referralsLabel",
            label = "**Referrals**",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access202.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "referralsLabel",
            label = "**Referrals**",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access205.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField213;

    @CCD(
            id = "regionalOfficeLabel",
            label = "${regionalOffice}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "regionalOfficeLabel",
            label = "${regionalOffice}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField214;

    @CCD(
            id = "replyToReferralDcfLinkLabel",
            label = "${replyToReferralDcfLink}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access054.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "replyToReferralDcfLinkLabel",
            label = "${replyToReferralDcfLink}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access121.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField215;

    @CCD(
            id = "representativeInformationPreamble",
            label = "In most circumstances the tribunal will communicate with you (as the representative) by email. If you need communication by post, please tell us why.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "representativeInformationPreamble",
            label = "In most circumstances the tribunal will communicate with you (as the representative) by email. If you need communication by post, please tell us why.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField216;

    @CCD(
            id = "resTseAllApplicationsLabel",
            label = "All applications <hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseAllApplicationsLabel",
            label = "All applications <hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField217;

    @CCD(
            id = "resTseCopyThisCorrespondenceText",
            label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exceptional circumstances.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseCopyThisCorrespondenceText",
            label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exceptional circumstances.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField218;

    @CCD(
            id = "resTseGuidanceLabel1",
            label = " <hr>Use this form to apply to amend the ET3 response.<br><br>The tribunal will decide if you can make the amendment by judging the fairness of the application. This decision may be made at a hearing.<br><br>The tribunal also needs to know if you consider the amendment minor or substantial.<br><br>Providing details of why you want to amend the response and the importance of the amendment will help the tribunal to decide your application more quickly.<h3>Details to include in your application:</h3><ul><li>what you want to amend in your response. Be specific and refer to your ET3 response form if possible</li><li>if you consider it a minor or substantial amendment</li><li>why you want to make this amendment</li><li>why you are asking to make this amendment now</li><li>how this amendment will benefit you and how could it disadvantage you if not granted</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel1",
            label = " <hr>Use this form to apply to amend the ET3 response.<br><br>The tribunal will decide if you can make the amendment by judging the fairness of the application. This decision may be made at a hearing.<br><br>The tribunal also needs to know if you consider the amendment minor or substantial.<br><br>Providing details of why you want to amend the response and the importance of the amendment will help the tribunal to decide your application more quickly.<h3>Details to include in your application:</h3><ul><li>what you want to amend in your response. Be specific and refer to your ET3 response form if possible</li><li>if you consider it a minor or substantial amendment</li><li>why you want to make this amendment</li><li>why you are asking to make this amendment now</li><li>how this amendment will benefit you and how could it disadvantage you if not granted</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField219;

    @CCD(
            id = "resTseGuidanceLabel10",
            label = "<hr>Use this form to apply to prevent or restrict publicity in this case.<br><br>It is an important principle that justice should normally be delivered in public.<br><br>However, the tribunal has the power to prevent or restrict the public disclosure of any aspect of this case if necessary in the interests of justice or to protect the Convention rights of any person.<br><br>The tribunal also has certain other powers to sit in private or to restrict publicity in accordance with sections 10A, 10B, 11 and 12 of the Employment Tribunals Act 1996.<br><br>The tribunal may issue an order:<ul><li>that a hearing that would otherwise be in public be conducted, in whole or in part, in private</li><li>that the identities of specified parties, witnesses or other persons referred to in the proceedings should not be disclosed to the public, by the use of anonymisation or otherwise</li><li>for measures preventing witnesses at a public hearing being identifiable by members of the public</li><li>restricting the reporting of the case in the media</li></ul><h3>Details to include in your application:</h3><ul><li>how the tribunal should prevent or restrict publicity in this case</li><li>why the restrictions you want are needed. The tribunal must make the least restrictive order possible. Tell us why fewer restrictions would not be enough</li><li>any media interest in this case. Include names of media organisations or journalists that are interested. The tribunal might seek their comments before making a decision</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel10",
            label = "<hr>Use this form to apply to prevent or restrict publicity in this case.<br><br>It is an important principle that justice should normally be delivered in public.<br><br>However, the tribunal has the power to prevent or restrict the public disclosure of any aspect of this case if necessary in the interests of justice or to protect the Convention rights of any person.<br><br>The tribunal also has certain other powers to sit in private or to restrict publicity in accordance with sections 10A, 10B, 11 and 12 of the Employment Tribunals Act 1996.<br><br>The tribunal may issue an order:<ul><li>that a hearing that would otherwise be in public be conducted, in whole or in part, in private</li><li>that the identities of specified parties, witnesses or other persons referred to in the proceedings should not be disclosed to the public, by the use of anonymisation or otherwise</li><li>for measures preventing witnesses at a public hearing being identifiable by members of the public</li><li>restricting the reporting of the case in the media</li></ul><h3>Details to include in your application:</h3><ul><li>how the tribunal should prevent or restrict publicity in this case</li><li>why the restrictions you want are needed. The tribunal must make the least restrictive order possible. Tell us why fewer restrictions would not be enough</li><li>any media interest in this case. Include names of media organisations or journalists that are interested. The tribunal might seek their comments before making a decision</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField220;

    @CCD(
            id = "resTseGuidanceLabel11",
            label = "<hr>You can request that the tribunal strike out all or parts of the claim.<br><br>If something is ‘struck out’ it is removed from the claim or response and cannot be relied upon.<br><br>The tribunal can strike out all or parts of the claimant’s claim on their own initiative or after a request from the respondent.<br><br>A strike out request must be based on at least one of the grounds of Rule 38 of the Employment Tribunals Rules of Procedure.<h3>Details to include in your application:</h3><ul><li>why you think the claim (or parts of it) should be struck out</li><li>which ground or grounds in Rule 38 you say applies in this case</li><li>if you are referring to numbered points or paragraphs in a claim, include these numbers or other references</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel11",
            label = "<hr>You can request that the tribunal strike out all or parts of the claim.<br><br>If something is ‘struck out’ it is removed from the claim or response and cannot be relied upon.<br><br>The tribunal can strike out all or parts of the claimant’s claim on their own initiative or after a request from the respondent.<br><br>A strike out request must be based on at least one of the grounds of Rule 38 of the Employment Tribunals Rules of Procedure.<h3>Details to include in your application:</h3><ul><li>why you think the claim (or parts of it) should be struck out</li><li>which ground or grounds in Rule 38 you say applies in this case</li><li>if you are referring to numbered points or paragraphs in a claim, include these numbers or other references</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField221;

    @CCD(
            id = "resTseGuidanceLabel12",
            label = "<hr>Use this form to apply to vary or revoke an order the tribunal has issued.<br><br>Tribunal orders will not usually be varied or revoked unless there has been a material change in circumstances since the order was made, which make the variation in the interests of justice.<br><br>The tribunal will consider your reasons for varying or revoking the order and any supporting materials you provide then make a decision.<h3>Details to include in your application:</h3><ul><li>the order you want to vary or revoke </li><li>the date the tribunal issued the order</li><li>explain which part of the order you want to vary or revoke</li><li>how to vary the order</li><li>why it is in the interests of justice to vary or revoke this order. Tell us any relevant changes of circumstance since the order was made and consider the requirements of the ‘overriding objective’ in Rule 3 of the Employment Tribunal Rules of Procedure</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel12",
            label = "<hr>Use this form to apply to vary or revoke an order the tribunal has issued.<br><br>Tribunal orders will not usually be varied or revoked unless there has been a material change in circumstances since the order was made, which make the variation in the interests of justice.<br><br>The tribunal will consider your reasons for varying or revoking the order and any supporting materials you provide then make a decision.<h3>Details to include in your application:</h3><ul><li>the order you want to vary or revoke </li><li>the date the tribunal issued the order</li><li>explain which part of the order you want to vary or revoke</li><li>how to vary the order</li><li>why it is in the interests of justice to vary or revoke this order. Tell us any relevant changes of circumstance since the order was made and consider the requirements of the ‘overriding objective’ in Rule 3 of the Employment Tribunal Rules of Procedure</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField222;

    @CCD(
            id = "resTseGuidanceLabel2",
            label = "<hr>Use this form to apply to change details like the company address, email or telephone number. <br><br>If you change the postal or email address, we’ll send any letters to the new address.<br><br>If you change the telephone number, we’ll contact you using the new number if we have questions about your response.<h3>Details you can apply to change:</h3><ul><li>name</li><li>sex and preferred title</li><li>address</li><li>telephone number</li><li>email address</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel2",
            label = "<hr>Use this form to apply to change details like the company address, email or telephone number. <br><br>If you change the postal or email address, we’ll send any letters to the new address.<br><br>If you change the telephone number, we’ll contact you using the new number if we have questions about your response.<h3>Details you can apply to change:</h3><ul><li>name</li><li>sex and preferred title</li><li>address</li><li>telephone number</li><li>email address</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField223;

    @CCD(
            id = "resTseGuidanceLabel3",
            label = "<hr>Use this form to tell us that the claimant has not complied with all or part of an order from the tribunal.<br><br>You should try to resolve your complaint with the claimant. Only use this form if that is not possible.<h3>Details to include in your application:</h3><ul><li>which order has not been complied with</li><li>the date the tribunal issued the order</li><li>what the claimant has not done</li><li>what you want the tribunal to do next</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel3",
            label = "<hr>Use this form to tell us that the claimant has not complied with all or part of an order from the tribunal.<br><br>You should try to resolve your complaint with the claimant. Only use this form if that is not possible.<h3>Details to include in your application:</h3><ul><li>which order has not been complied with</li><li>the date the tribunal issued the order</li><li>what the claimant has not done</li><li>what you want the tribunal to do next</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField224;

    @CCD(
            id = "resTseGuidanceLabel4",
            label = "<hr>Use this form to have a judgment or order made by a legal officer considered afresh by an Employment Judge.<br><br>Considered afresh means that if you disagree with a judgment or order made by a legal officer in this case, you can ask an Employment Judge to look at that decision again.<br><br>The judge will take the decision afresh on the basis of the same information as was before the legal officer, unless either side chooses to supply additional information.<h3>Details to include in your application:</h3><ul><li>the decision you want considered afresh</li><li>the date the tribunal issued the decision</li><li>why you want the decision considered afresh</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel4",
            label = "<hr>Use this form to have a judgment or order made by a legal officer considered afresh by an Employment Judge.<br><br>Considered afresh means that if you disagree with a judgment or order made by a legal officer in this case, you can ask an Employment Judge to look at that decision again.<br><br>The judge will take the decision afresh on the basis of the same information as was before the legal officer, unless either side chooses to supply additional information.<h3>Details to include in your application:</h3><ul><li>the decision you want considered afresh</li><li>the date the tribunal issued the decision</li><li>why you want the decision considered afresh</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField225;

    @CCD(
            id = "resTseGuidanceLabel5",
            label = "<hr>Tell or ask the tribunal about something relevant to this case.<h3>Do not use this form to:</h3><ul><li>seek legal advice from the tribunal. The tribunal is an independent judicial body and cannot give legal advice</li><li>tell us about settlement offers or discussions, which are private and confidential to the parties</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel5",
            label = "<hr>Tell or ask the tribunal about something relevant to this case.<h3>Do not use this form to:</h3><ul><li>seek legal advice from the tribunal. The tribunal is an independent judicial body and cannot give legal advice</li><li>tell us about settlement offers or discussions, which are private and confidential to the parties</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField226;

    @CCD(
            id = "resTseGuidanceLabel6",
            label = "<hr>Use this form to ask the tribunal to order the claimant to do or provide something.<h3>Details to include in your application:</h3><ul><li>what you want the claimant to do</li><li>why it is relevant to your response</li><li>if you have already asked the claimant to do or provide this thing. If you have, tell us when you asked and what their response was</li><li>if you have not asked the claimant to do or provide this thing yet, explain why</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel6",
            label = "<hr>Use this form to ask the tribunal to order the claimant to do or provide something.<h3>Details to include in your application:</h3><ul><li>what you want the claimant to do</li><li>why it is relevant to your response</li><li>if you have already asked the claimant to do or provide this thing. If you have, tell us when you asked and what their response was</li><li>if you have not asked the claimant to do or provide this thing yet, explain why</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField227;

    @CCD(
            id = "resTseGuidanceLabel7",
            label = "<hr>You can ask the tribunal to order a witness to attend to give evidence.<br><br>The witness must be in Great Britain (England, Scotland and Wales). This does not include Northern Ireland.<br><br>The tribunal can limit the number of witnesses to be called to give evidence on a particular issue, especially if that issue is not central to the case.<br><br>The respondent may also be liable for the costs incurred by the witness’s attendance.<br><br>You should consider whether the evidence of this witness is likely to help your case.<h3>Details to include in your application:</h3><ul><li>the witness’s full name and address</li><li>why the attendance of this witness is necessary for a fair hearing. The tribunal needs to understand why the evidence is relevant, and why there is no alternative way of establishing the same points at the hearing without ordering a witness to attend</li><li>if you have asked this witness to attend voluntarily. If you have, tell us when you asked and what their response was</li><li>if you have not asked the witness to attend yet, explain why</li></ul>If the order is granted, the witness will be ordered to attend on the first day of the hearing. If you want them to attend on another day or days, tell us: <br><br><ul><li>the dates apart from the first day of the hearing that you want the witness to attend</li><li>why their attendance is necessary on those dates</li></ul>If you want the witness to bring documents, tell us: <br/><br/><ul><li>why these documents are relevant to the issues in this case</li><li>why an order to disclose the documents would not be enough</li><li>if you have already asked the witness or anyone else to provide these documents. If you have, tell us when you asked and what their response was</li><li>if you have not asked the witness to provide the documents yet, explain why</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel7",
            label = "<hr>You can ask the tribunal to order a witness to attend to give evidence.<br><br>The witness must be in Great Britain (England, Scotland and Wales). This does not include Northern Ireland.<br><br>The tribunal can limit the number of witnesses to be called to give evidence on a particular issue, especially if that issue is not central to the case.<br><br>The respondent may also be liable for the costs incurred by the witness’s attendance.<br><br>You should consider whether the evidence of this witness is likely to help your case.<h3>Details to include in your application:</h3><ul><li>the witness’s full name and address</li><li>why the attendance of this witness is necessary for a fair hearing. The tribunal needs to understand why the evidence is relevant, and why there is no alternative way of establishing the same points at the hearing without ordering a witness to attend</li><li>if you have asked this witness to attend voluntarily. If you have, tell us when you asked and what their response was</li><li>if you have not asked the witness to attend yet, explain why</li></ul>If the order is granted, the witness will be ordered to attend on the first day of the hearing. If you want them to attend on another day or days, tell us: <br><br><ul><li>the dates apart from the first day of the hearing that you want the witness to attend</li><li>why their attendance is necessary on those dates</li></ul>If you want the witness to bring documents, tell us: <br/><br/><ul><li>why these documents are relevant to the issues in this case</li><li>why an order to disclose the documents would not be enough</li><li>if you have already asked the witness or anyone else to provide these documents. If you have, tell us when you asked and what their response was</li><li>if you have not asked the witness to provide the documents yet, explain why</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField228;

    @CCD(
            id = "resTseGuidanceLabel8",
            label = "<hr>Use this form to ask the tribunal to postpone a hearing to a later date. <h3>Details to include in your application:</h3><ul><li>which hearing you want to postpone. If the hearing you want to postpone is listed over multiple days, tell us which day or days you are applying to postpone</li><li>the reason you want to postpone. The tribunal will use this to decide whether to grant your application</li><li>any essential documents to support your application</li><li>weekday dates within the next 3 months that you, your witnesses or representatives cannot attend a rescheduled hearing</li><li>the reason you cannot attend on those dates</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel8",
            label = "<hr>Use this form to ask the tribunal to postpone a hearing to a later date. <h3>Details to include in your application:</h3><ul><li>which hearing you want to postpone. If the hearing you want to postpone is listed over multiple days, tell us which day or days you are applying to postpone</li><li>the reason you want to postpone. The tribunal will use this to decide whether to grant your application</li><li>any essential documents to support your application</li><li>weekday dates within the next 3 months that you, your witnesses or representatives cannot attend a rescheduled hearing</li><li>the reason you cannot attend on those dates</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField229;

    @CCD(
            id = "resTseGuidanceLabel9",
            label = "<hr>Use this form to have a judgment made by a tribunal reconsidered. This may be a judgment made by a judge alone or by a judge sitting with non-legal members.<br><br>If reconsideration is necessary in the interests of justice you can ask the tribunal to look at the judgment again.<br><br>Only a judgment which finally determines an issue in your case can be reconsidered. This therefore excludes any decision that does not comprise a final determination of the claim, or part of a claim. If you wish to ask for a case management order to be varied or revoked, use the vary or revoke an order application.<br><br>You can only ask the tribunal to reconsider a judgment within 14 days of the date the judgment was sent to you.<br><br>If you want the tribunal to reconsider a judgment that was sent to you over 14 days ago you must explain why your application is late.<br><br>A reconsideration application does not affect the time limit for appealing to the Employment Appeal Tribunal.<h3>Details to include in your application:</h3><ul><li>the judgment you want reconsidered </li><li>the date the tribunal issued the judgment</li><li>your reason for a late application if the judgment was sent over 14 days ago</li><li>why it is in the interests of justice to reconsider this judgment</li><li>if the tribunal should vary or revoke the judgment</li><li>any additional information or material which the tribunal does not already have to support your application</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseGuidanceLabel9",
            label = "<hr>Use this form to have a judgment made by a tribunal reconsidered. This may be a judgment made by a judge alone or by a judge sitting with non-legal members.<br><br>If reconsideration is necessary in the interests of justice you can ask the tribunal to look at the judgment again.<br><br>Only a judgment which finally determines an issue in your case can be reconsidered. This therefore excludes any decision that does not comprise a final determination of the claim, or part of a claim. If you wish to ask for a case management order to be varied or revoked, use the vary or revoke an order application.<br><br>You can only ask the tribunal to reconsider a judgment within 14 days of the date the judgment was sent to you.<br><br>If you want the tribunal to reconsider a judgment that was sent to you over 14 days ago you must explain why your application is late.<br><br>A reconsideration application does not affect the time limit for appealing to the Employment Appeal Tribunal.<h3>Details to include in your application:</h3><ul><li>the judgment you want reconsidered </li><li>the date the tribunal issued the judgment</li><li>your reason for a late application if the judgment was sent over 14 days ago</li><li>why it is in the interests of justice to reconsider this judgment</li><li>if the tribunal should vary or revoke the judgment</li><li>any additional information or material which the tribunal does not already have to support your application</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField230;

    @CCD(
            id = "resTseHorizontalLine",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseHorizontalLine",
            label = "<hr>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField231;

    @CCD(
            id = "resTseNotAvailableWarningLabel",
            label = "<h3>This function is not available for this case, please click cancel to return to the main page, you will need to submit your application outside the portal via email or post.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access061.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseNotAvailableWarningLabel",
            label = "<h3>This function is not available for this case, please click cancel to return to the main page, you will need to submit your application outside the portal via email or post.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access080.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField232;

    @CCD(
            id = "resTseTableLabel",
            label = "${resTseTableMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "resTseTableLabel",
            label = "${resTseTableMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField233;

    @CCD(
            id = "respondNotificationDate",
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access057.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "respondNotificationDate",
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField234;

    @CCD(
            id = "respondToTribunalNotAvailableWarningLabel",
            label = "<h3>This function is not available for this case, please click cancel to return to the main page, you will need to submit your application outside the portal via email or post.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access061.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "respondToTribunalNotAvailableWarningLabel",
            label = "<h3>This function is not available for this case, please click cancel to return to the main page, you will need to submit your application outside the portal via email or post.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access080.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField235;

    @CCD(
            id = "respondentEndLabel",
            label = "You can add more respondents later if you need to",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "respondentEndLabel",
            label = "You can add more respondents later if you need to",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField236;

    @CCD(
            id = "respondentNamePreamble",
            label = "Enter the name of the organisation. If there’s no organisation involved - for example, because the claimant was employed by an individual acting as a sole trader - enter the individual’s name. \n\nThis should be the same as the name of the respondent on the Acas certificate.\n\nYou will be able to add more respondents later.\n",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "respondentNamePreamble",
            label = "Enter the name of the organisation. If there’s no organisation involved - for example, because the claimant was employed by an individual acting as a sole trader - enter the individual’s name. \n\nThis should be the same as the name of the respondent on the Acas certificate.\n\nYou will be able to add more respondents later.\n",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField237;

    @CCD(
            id = "selectAllThatApply",
            label = "Select all that apply.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "selectAllThatApply",
            label = "Select all that apply.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField238;

    @CCD(
            id = "sendDocByFirstClass",
            label = "Send documents by first class mail to:",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "sendDocByFirstClass",
            label = "Send documents by first class mail to:",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField239;

    @CCD(
            id = "sendNotificationInfo",
            label = "Use this service to notify one or both parties about this case. You can do this by uploading standard letter documents.\n\n You can send multiple letters in one notification\n\n Do not use this service to notify parties about:\n * ET1 serving - use the <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/uploadDocumentForServing/uploadDocumentForServing1 \">ET1 serving service</a>\n * ET3 notification - use the <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/et3Notification/et3Notification1 \">ET3 notification service</a>\n * Responding to an application - use the <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmin/tseAdmin1\">Record a decision</a> or <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmReply/tseAdmReply1\">Respond to an application service</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access058.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "sendNotificationInfo",
            label = "Use this service to notify one or both parties about this case. You can do this by uploading standard letter documents.\n\n You can send multiple letters in one notification\n\n Do not use this service to notify parties about:\n * ET1 serving - use the <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/uploadDocumentForServing/uploadDocumentForServing1 \">ET1 serving service</a>\n * ET3 notification - use the <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/et3Notification/et3Notification1 \">ET3 notification service</a>\n * Responding to an application - use the <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmin/tseAdmin1\">Record a decision</a> or <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmReply/tseAdmReply1\">Respond to an application service</a>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access095.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField240;

    @CCD(
            id = "state",
            label = "state",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "state",
            label = "state",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField241;

    @CCD(
            id = "subMultipleName ",
            label = "Submultiple Name",
            typeOverride = FieldType.Text,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "subMultipleName ",
            label = "Submultiple Name",
            typeOverride = FieldType.Text,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField242;

    @CCD(
            id = "submitEt1Confirmation",
            label = "Do you want to submit this ET1 claim?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_submitEt1",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "submitEt1Confirmation",
            label = "Do you want to submit this ET1 claim?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_submitEt1",
            searchable = false,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField243;

    @CCD(
            id = "submitEt1Preamble",
            label = "If you need to check your answers you can access individual sections from the claim overview. You can then edit your answers within the section.\n\nYou will not be able to make any further changes to this form once submitted.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "submitEt1Preamble",
            label = "If you need to check your answers you can access individual sections from the claim overview. You can then edit your answers within the section.\n\nYou will not be able to make any further changes to this form once submitted.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access007.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField244;

    @CCD(
            id = "trackAllocationLabel",
            label = "${trackAllocation}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "trackAllocationLabel",
            label = "${trackAllocation}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField245;

    @CCD(
            id = "tribunalAndOfficeLocationLabel",
            label = "${tribunalAndOfficeLocation}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access116.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tribunalAndOfficeLocationLabel",
            label = "${tribunalAndOfficeLocation}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access120.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField246;

    @CCD(
            id = "tseAdmReplyTableLabel",
            label = "${tseAdmReplyTableMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseAdmReplyTableLabel",
            label = "${tseAdmReplyTableMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField247;

    @CCD(
            id = "tseAdminCloseApplicationTableLabel",
            label = "${tseAdminCloseApplicationTable}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseAdminCloseApplicationTableLabel",
            label = "${tseAdminCloseApplicationTable}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField248;

    @CCD(
            id = "tseAdminCloseApplicationYes",
            label = "Do you want to close this application?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_closeApplicationYes",
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseAdminCloseApplicationYes",
            label = "Do you want to close this application?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_closeApplicationYes",
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField249;

    @CCD(
            id = "tseAdminTableLabel",
            label = "${tseAdminTableMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access201.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseAdminTableLabel",
            label = "${tseAdminTableMarkUp}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access204.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField250;

    @CCD(
            id = "tseApplicationSummaryAndResponsesMarkupLabel",
            label = "${tseApplicationSummaryAndResponsesMarkup}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseApplicationSummaryAndResponsesMarkupLabel",
            label = "${tseApplicationSummaryAndResponsesMarkup}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField251;

    @CCD(
            id = "tseRespondCopyPartyIntro",
            label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exception circumstances.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseRespondCopyPartyIntro",
            label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exception circumstances.",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField252;

    @CCD(
            id = "tseRespondNotAvailableWarningLabel",
            label = "<h3>This function is not available for this case, please click cancel to return to the main page, you will need to submit your application outside the portal via email or post.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access061.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseRespondNotAvailableWarningLabel",
            label = "<h3>This function is not available for this case, please click cancel to return to the main page, you will need to submit your application outside the portal via email or post.</h3>",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access080.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField253;

    @CCD(
            id = "tseResponseConsider",
            label = "### Consider in your answer:\n + If you agree or disagree with the application \n + that if you disagree with the application, you must give reasons \n + that you can apply for a hearing instead of responding in writing",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access191.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseResponseConsider",
            label = "### Consider in your answer:\n + If you agree or disagree with the application \n + that if you disagree with the application, you must give reasons \n + that you can apply for a hearing instead of responding in writing",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access191.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField254;

    @CCD(
            id = "tseResponseConsiderMaterial",
            label = "Consider when providing material that:\n + you can upload letters, photos or documents to support your application\n + if you are taking a picture of a letter, place it on a flat surface and take the picture from above\n + If you are uploading written documents with tracked changes, make sure that tracked changes are turned on\n",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseResponseConsiderMaterial",
            label = "Consider when providing material that:\n + you can upload letters, photos or documents to support your application\n + if you are taking a picture of a letter, place it on a flat surface and take the picture from above\n + If you are uploading written documents with tracked changes, make sure that tracked changes are turned on\n",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField255;

    @CCD(
            id = "tseResponseIntroLabel",
            label = "${tseResponseIntro}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseResponseIntroLabel",
            label = "${tseResponseIntro}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField256;

    @CCD(
            id = "tseResponseTableLabel",
            label = "${tseResponseTable}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "tseResponseTableLabel",
            label = "${tseResponseTable}",
            typeOverride = FieldType.Label,
            access = SingleAccess.Access188.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField257;

    @CCD(
            id = "unavailabilityTabTitle",
            label = "# Unavailability Dates",
            typeOverride = FieldType.Label,
            securityClassification = "PUBLIC",
            access = SingleAccess.Access148.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            id = "unavailabilityTabTitle",
            label = "# Unavailability Dates",
            typeOverride = FieldType.Label,
            securityClassification = "PUBLIC",
            access = SingleAccess.Access182.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    private Object singleDefinitionField258;

    @CCD(
            id = "updateReferralHearingDate",
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "updateReferralHearingDate",
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            typeOverride = FieldType.TextArea,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField259;

    @CCD(
            id = "uploadHearingDocumentsWhatAreDocuments",
            label = "What are these hearing documents?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundleType",
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "uploadHearingDocumentsWhatAreDocuments",
            label = "What are these hearing documents?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundleType",
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField260;

    @CCD(
            id = "uploadHearingDocumentsWhatAreDocumentsOther",
            label = "Please specify",
            typeOverride = FieldType.Text,
            searchable = false,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "uploadHearingDocumentsWhatAreDocumentsOther",
            label = "Please specify",
            typeOverride = FieldType.Text,
            searchable = false,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private Object singleDefinitionField261;

    @JsonProperty("caseNotesCollection")
    @CCD(
            label = "Telephone notes",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "caseNote",
            searchable = false,
            access = SingleAccess.Access158.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Telephone notes",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "caseNote",
            searchable = false,
            access = SingleAccess.Access179.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<GenericTypeItem<CaseNote>> caseNotesCollection;
    @JsonProperty("addCaseNote")
    @CCD(
            label = "Telephone note",
            typeNameOverride = "caseNote",
            searchable = false,
            access = SingleAccess.Access158.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Add telephone note",
            typeNameOverride = "caseNote",
            searchable = false,
            access = SingleAccess.Access179.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private CaseNote addCaseNote;
    @JsonProperty("editOrDeleteCaseNote")
    @CCD(
            label = "Edit or delete telephone note",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_editOrDelete",
            searchable = false,
            access = SingleAccess.Access158.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Edit or delete telephone note",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_editOrDelete",
            searchable = false,
            access = SingleAccess.Access179.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String editOrDeleteCaseNote;
    @JsonProperty("caseNoteList")
    @CCD(
            label = "Telephone notes to edit or delete",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access158.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Telephone notes to edit or delete",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access179.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private DynamicFixedListType caseNoteList;
}
