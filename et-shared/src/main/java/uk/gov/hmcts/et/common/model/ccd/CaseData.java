package uk.gov.hmcts.et.common.model.ccd;

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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRPlus5RolesBbxtjpAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudCitizenCuAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEnglandwalesCuPlus1RolesDqbnwvAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudCitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess;
import uk.gov.hmcts.et.common.ccd.access.CitizenCuAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudEtAcasApiRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEnglandwalesCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEtjudgeEnglandwalesCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorRCitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.DefaultAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorCuAccess;
import uk.gov.hmcts.et.common.ccd.access.CitizenCudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudCitizenCudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorCudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEtjudgeEtAcasApiRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorCrudCitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEtjudgeRAccess;
import uk.gov.hmcts.et.common.ccd.access.CitizenCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRPlus2RolesYovydhAccess;
import uk.gov.hmcts.et.common.ccd.access.EtAcasApiRAccess;
import uk.gov.hmcts.et.common.ccd.access.CitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorDAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRPlus2RolesHozjvjAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEtjudgeCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentCrudPlus2RolesWfyqbwAccess;
import uk.gov.hmcts.et.common.ccd.access.CitizenRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorDPlus1RolesZwtixtAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEtjudgeCudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorCruAccess;
import uk.gov.hmcts.et.common.ccd.access.SOLICITORACrudPlus9RolesLhlklnAccess;
import uk.gov.hmcts.et.common.ccd.access.CLAIMANTSOLICITORDAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEnglandwalesRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEtjudgeEnglandwalesRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEtjudgeCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerApproverCrudPlus4RolesMlxkxlAccess;
import uk.gov.hmcts.et.common.ccd.access.DEFENDANTCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerApproverCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerCaaCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCitizenCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRPlus2RolesSiihenAccess;
import uk.gov.hmcts.et.common.ccd.access.CLAIMANTSOLICITORCudPlus15RolesEslhbeAccess;
import uk.gov.hmcts.et.common.ccd.access.CREATORCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CLAIMANTSOLICITORCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCruEtAcasApiRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudCaseworkerEmploymentEnglandwalesCruAccess;
import uk.gov.hmcts.et.common.ccd.access.SOLICITORACuPlus9RolesXrnczvAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCuAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRCaseworkerEmploymentApiCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEnglandwalesCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.GSProfileRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEnglandwalesCudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerRasValidationRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEtjudgeEnglandwalesCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCaseworkerEmploymentLegalrepSolicitorCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CLAIMANTSOLICITORCrudPlus11RolesKpylwqAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerCaaCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.SOLICITORACruPlus13RolesJweikaAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEnglandwalesCuAccess;
import uk.gov.hmcts.et.common.ccd.access.CLAIMANTSOLICITORCudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEtPcqextractorRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRPlus2RolesCuxzijAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorCrudAccess;
import uk.gov.hmcts.et.common.model.ccd.TaskListCheck;

/**
 * Employment Tribunal claim data. This class contains all the data for a citizen's claim.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CaseData extends Et1CaseData {
    @CCD(
            label = "Correspondence Address",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("tribunalCorrespondenceAddress")
    private Address tribunalCorrespondenceAddress;
    @CCD(
            label = "Correspondence Telephone",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("tribunalCorrespondenceTelephone")
    private String tribunalCorrespondenceTelephone;
    @CCD(
            label = "Correspondence Fax",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("tribunalCorrespondenceFax")
    private String tribunalCorrespondenceFax;
    @CCD(
            label = "Correspondence DX",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("tribunalCorrespondenceDX")
    private String tribunalCorrespondenceDX;
    @CCD(
            label = "Correspondence Email",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("tribunalCorrespondenceEmail")
    private String tribunalCorrespondenceEmail;
    @CCD(
            label = "Case Number",
            access = {CaseworkerEmploymentApiCrudCitizenCuAccess.class, CaseworkerEmploymentEnglandwalesCuPlus1RolesDqbnwvAccess.class}
    )
    @JsonProperty("ethosCaseReference")
    private String ethosCaseReference;
    @CCD(ignore = true)
    @JsonProperty("multipleName")
    private String multipleName;
    @CCD(
            label = "Multiple reference",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("multipleReference")
    private String multipleReference;
    @CCD(
            label = "Go to:",
            searchable = false,
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("multipleReferenceLinkMarkUp")
    private String multipleReferenceLinkMarkUp;
    @CCD(ignore = true)
    @JsonProperty("parentMultipleCaseId")
    private String parentMultipleCaseId;
    @CCD(
            label = "Submultiple Name",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class, CaseworkerEmploymentRAccess.class}
    )
    @JsonProperty("subMultipleName")
    private String subMultipleName;
    @CCD(
            label = "Lead claimant",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("leadClaimant")
    private String leadClaimant;
    @CCD(
            label = "check multiple",
            searchable = false,
            access = {CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class, CaseworkerEmploymentEnglandwalesCruAccess.class, CaseworkerEmploymentEtjudgeEnglandwalesCruAccess.class}
    )
    @JsonProperty("multipleFlag")
    private String multipleFlag;

    @CCD(
            label = "Type of claimant",
            hint = "Is the claimant an individual or a company?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantType",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("claimant_TypeOfClaimant")
    private String claimantTypeOfClaimant;
    @CCD(
            label = "Company",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("claimant_Company")
    private String claimantCompany;
    @CCD(
            label = " ",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("preAcceptCase")
    private CasePreAcceptType preAcceptCase;

    @CCD(
            label = "Claim Served Date",
            typeOverride = FieldType.Date,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("claimServedDate")
    private String claimServedDate;
    @CCD(
            label = "ET3 Due Date",
            typeOverride = FieldType.Date,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("et3DueDate")
    private String et3DueDate;

    @CCD(
            label = "Digital Case Reference",
            hint = "Digital Case Reference (12 or 16 digit number)",
            regex = "^([0-9]{12}|[0-9]{16})$",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("feeGroupReference")
    private String feeGroupReference;
    @CCD(
            label = "Please select the Respondent whose address should be used",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorRCitizenCruAccess.class}
    )
    @JsonProperty("claimantWorkAddressQRespondent")
    private DynamicFixedListType claimantWorkAddressQRespondent;
    @CCD(
            label = "Respondent Representative(s)",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentRepresentative",
            access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class, CaseworkerEmploymentLegalrepSolicitorCuAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("repCollection")
    private List<RepresentedTypeRItem> repCollection;
    @CCD(
            label = "Current Position",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Position",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("positionType")
    private String positionType;
    @CCD(
            label = "Date To Position",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("dateToPosition")
    private String dateToPosition;
    @CCD(
            label = "Current Position",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("currentPosition")
    private String currentPosition;
    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("fileLocation")
    private DynamicFixedListType fileLocation;
    @CCD(ignore = true)
    @JsonProperty("fileLocationGlasgow")
    private DynamicFixedListType fileLocationGlasgow;
    @CCD(ignore = true)
    @JsonProperty("fileLocationAberdeen")
    private DynamicFixedListType fileLocationAberdeen;
    @CCD(ignore = true)
    @JsonProperty("fileLocationDundee")
    private DynamicFixedListType fileLocationDundee;
    @CCD(ignore = true)
    @JsonProperty("fileLocationEdinburgh")
    private DynamicFixedListType fileLocationEdinburgh;
    @CCD(
            label = " ",
            hint = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Hearing",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCudAccess.class, CaseworkerEmploymentLegalrepSolicitorCudAccess.class, CaseworkerEmploymentCrudAccess.class}
    )
    @JsonProperty("hearingCollection")
    private List<HearingTypeItem> hearingCollection;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingDetails",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorCrudCitizenCruAccess.class, CaseworkerEmploymentRAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    @JsonProperty("hearingDetailsCollection")
    private List<HearingDetailTypeItem> hearingDetailsCollection;
    @CCD(
            label = "Next Hearing Details",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    @JsonProperty("nextHearingDetails")
    private NextHearingDetails nextHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Deposit",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("depositType")
    private List<DepositTypeItem> depositCollection;
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Judgment",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("judgementCollection")
    private List<JudgementTypeItem> judgementCollection;
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "BFActions",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCaseworkerWaTaskConfigurationRAccess.class}
    )
    @JsonProperty("bfActions")
    private List<BFActionTypeItem> bfActions;
    @CCD(
            label = "Clerk Responsible",
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("clerkResponsible")
    private DynamicFixedListType clerkResponsible;
    @CCD(ignore = true)
    @JsonProperty("userLocation")
    private String userLocation;
    @CCD(
            label = "Adds Case Documents",
            hint = "Adds documentation for the case(excludes already uploaded docs)",
            searchable = false,
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class, CitizenCrudAccess.class}
    )
    @JsonProperty("addDocumentCollection")
    private List<DocumentTypeItem> addDocumentCollection;
    @CCD(ignore = true)
    @JsonProperty("correspondenceScotType")
    private CorrespondenceScotType correspondenceScotType;
    @CCD(
            label = "List of correspondence items",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("correspondenceType")
    private CorrespondenceType correspondenceType;
    @CCD(
            label = "Address labels selection",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("addressLabelsSelectionType")
    private AddressLabelsSelectionType addressLabelsSelectionType;
    @CCD(
            label = "Address labels",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "addressLabel",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("addressLabelCollection")
    private List<AddressLabelTypeItem> addressLabelCollection;
    @CCD(
            label = "Address labels attributes",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("addressLabelsAttributesType")
    private AddressLabelsAttributesType addressLabelsAttributesType;
    @CCD(ignore = true)
    @JsonProperty("allocatedOffice")
    private String allocatedOffice;
    @CCD(
            label = "Conciliation Track",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Conciliation",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("conciliationTrack")
    private String conciliationTrack;
    @CCD(
            label = "Employer Contract Claim case",
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CaseworkerEmploymentLegalrepSolicitorCuAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("counterClaim")
    private String counterClaim;
    @CCD(
            label = "Employer Contract Claim cases",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CounterClaim",
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CaseworkerEmploymentLegalrepSolicitorCuAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("eccCases")
    private List<EccCounterClaimTypeItem> eccCases;
    @CCD(
            label = "Respondents with an ECC",
            searchable = false,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("respondentsWithEcc")
    private String respondentsWithEcc;
    @CCD(
            label = "Restricted Case",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("restrictedReporting")
    private RestrictedReportingType restrictedReporting;
    @CCD(
            label = "Hearing List",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("printHearingDetails")
    private ListingData printHearingDetails;
    @CCD(
            label = "Hearing List",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CitizenCrudAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("printHearingCollection")
    private ListingData printHearingCollection;
    @CCD(
            label = "Target Hearing Date",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("targetHearingDate")
    private String targetHearingDate;
    @CCD(label = "Claimant", access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CitizenCuAccess.class})
    @JsonProperty("claimant")
    private String claimant;
    @CCD(
            label = "claimantId",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("claimantId")
    private String claimantId;
    @CCD(label = "Respondent", access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CitizenCuAccess.class})
    @JsonProperty("respondent")
    private String respondent;

    @CCD(ignore = true)
    @JsonProperty("EQP")
    private String eqp;
    @CCD(ignore = true)
    @JsonProperty("flag1")
    private String flag1;
    @CCD(ignore = true)
    @JsonProperty("flag2")
    private String flag2;

    @CCD(
            label = "Doc MarkUp",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("docMarkUp")
    private String docMarkUp;
    @CCD(
            label = "Case Ref Number Count",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("caseRefNumberCount")
    private String caseRefNumberCount;
    @CCD(
            label = "Start Case Ref Number",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("startCaseRefNumber")
    private String startCaseRefNumber;
    @CCD(
            label = "Multiple Ref Number",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorRCitizenCruAccess.class}
    )
    @JsonProperty("multipleRefNumber")
    private String multipleRefNumber;

    @CCD(
            label = "Enter the Case Number that this ECC relates to",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("caseRefECC")
    private String caseRefECC;
    @CCD(ignore = true)
    @JsonProperty("respondentECC")
    private DynamicFixedListType respondentECC;
    @CCD(
            label = "Employer Contract Claim",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("ccdID")
    private String ccdID;

    @CCD(
            label = "Flags Image File Name",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("flagsImageFileName")
    private String flagsImageFileName;
    @CCD(
            label = "Flags Image Alt Text",
            searchable = false,
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("flagsImageAltText")
    private String flagsImageAltText;

    // add hearing - page1
    @CCD(ignore = true)
    @JsonProperty("hearingNumbers")
    private String hearingNumbers;
    @CCD(ignore = true)
    @JsonProperty("hearingTypes")
    private String hearingTypes;
    @CCD(ignore = true)
    @JsonProperty("hearingPublicPrivate")
    private String hearingPublicPrivate;
    @CCD(ignore = true)
    @JsonProperty("hearingVenue")
    private DynamicFixedListType hearingVenue;
    @CCD(ignore = true)
    @JsonProperty("hearingEstLengthNum")
    private String hearingEstLengthNum;
    @CCD(ignore = true)
    @JsonProperty("hearingEstLengthNumType")
    private String hearingEstLengthNumType;
    @CCD(ignore = true)
    @JsonProperty("hearingSitAlone")
    private String hearingSitAlone;
    @CCD(ignore = true)
    @JsonProperty("Hearing_stage")
    private String hearingStage;
    @CCD(ignore = true)
    @JsonProperty("listedDate")
    private String listedDate;
    @CCD(ignore = true)
    @JsonProperty("Hearing_notes")
    private String hearingNotes;
    // amend hearing - page1
    @CCD(ignore = true)
    @JsonProperty("hearingSelection")
    private DynamicFixedListType hearingSelection;
    // amend hearing - page2
    @CCD(ignore = true)
    @JsonProperty("hearingActions")
    private String hearingActions;
    // amend hearing - page3
    @CCD(ignore = true)
    @JsonProperty("hearingERMember")
    private String hearingERMember;
    @CCD(ignore = true)
    @JsonProperty("hearingEEMember")
    private String hearingEEMember;
    @CCD(ignore = true)
    @JsonProperty("hearingDatesRequireAmending")
    private String hearingDatesRequireAmending;
    @CCD(ignore = true)
    @JsonProperty("hearingDateSelection")
    private DynamicFixedListType hearingDateSelection;
    // amend hearing - page4
    @CCD(ignore = true)
    @JsonProperty("hearingDateActions")
    private String hearingDateActions;
    // amend hearing - page5
    @CCD(ignore = true)
    @JsonProperty("hearingStatus")
    private String hearingStatus;
    @CCD(ignore = true)
    @JsonProperty("Postponed_by")
    private String postponedBy;
    @CCD(ignore = true)
    @JsonProperty("hearingRoom")
    private DynamicFixedListType hearingRoom;
    @CCD(ignore = true)
    @JsonProperty("hearingClerk")
    private DynamicFixedListType hearingClerk;
    @CCD(ignore = true)
    @JsonProperty("hearingJudge")
    private DynamicFixedListType hearingJudge;
    // amend hearing - page6
    @CCD(ignore = true)
    @JsonProperty("hearingCaseDisposed")
    private String hearingCaseDisposed;
    @CCD(ignore = true)
    @JsonProperty("Hearing_part_heard")
    private String hearingPartHeard;
    @CCD(ignore = true)
    @JsonProperty("Hearing_reserved_judgement")
    private String hearingReservedJudgement;
    @CCD(ignore = true)
    @JsonProperty("attendee_claimant")
    private String attendeeClaimant;
    @CCD(ignore = true)
    @JsonProperty("attendee_non_attendees")
    private String attendeeNonAttendees;
    @CCD(ignore = true)
    @JsonProperty("attendee_resp_no_rep")
    private String attendeeRespNoRep;
    @CCD(ignore = true)
    @JsonProperty("attendee_resp_&_rep")
    private String attendeeRespAndRep;
    @CCD(ignore = true)
    @JsonProperty("attendee_rep_only")
    private String attendeeRepOnly;
    @CCD(
            label = "Premises",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("companyPremises")
    private CompanyPremisesType companyPremises;

    @CCD(
            label = "Select the office you want to transfer the case to",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("officeCT")
    private DynamicFixedListType officeCT;
    @CCD(
            label = "Reason for Case Transfer",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("reasonForCT")
    private String reasonForCT;
    @CCD(
            label = "Link to related case",
            searchable = false,
            typeOverride = FieldType.CaseLink,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("relatedCaseCT")
    private String relatedCaseCT;
    @CCD(
            label = "Current Position",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PositionCT",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("positionTypeCT")
    private String positionTypeCT;
    @CCD(
            label = "Link to related case",
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("linkedCaseCT")
    private String linkedCaseCT;
    @CCD(
            label = "Case Link:",
            access = {CaseworkerEmploymentApiCrudCitizenCuAccess.class, CaseworkerEmploymentEnglandwalesCuPlus1RolesDqbnwvAccess.class}
    )
    @JsonProperty("transferredCaseLink")
    private String transferredCaseLink;
    @CCD(ignore = true)
    @JsonProperty("transferredCaseLinkSourceCaseId")
    private String transferredCaseLinkSourceCaseId;
    @CCD(ignore = true)
    @JsonProperty("transferredCaseLinkSourceCaseTypeId")
    private String transferredCaseLinkSourceCaseTypeId;
    @CCD(
            label = "Select office to transfer case to",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice",
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CitizenCruAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("ecmOfficeCT")
    private String ecmOfficeCT;
    @CCD(
            label = "Select the office you want to assign the case to",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("assignOffice")
    private DynamicFixedListType assignOffice;

    @CCD(ignore = true)
    @JsonProperty("retrospectiveTTL")
    private String retrospectiveTTL;

    @CCD(
            label = "state",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("stateAPI")
    private String stateAPI;

    // Allocate Hearing fields
    @CCD(
            label = "Select Hearing",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("allocateHearingHearing")
    private DynamicFixedListType allocateHearingHearing;
    @CCD(ignore = true)
    @JsonProperty("allocateHearingManagingOffice")
    private String allocateHearingManagingOffice;
    @CCD(
            label = "Select Hearing Venue",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("allocateHearingVenue")
    private DynamicFixedListType allocateHearingVenue;
    @CCD(
            label = "Select Room",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("allocateHearingRoom")
    private DynamicFixedListType allocateHearingRoom;
    @CCD(
            label = "Select Clerk",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("allocateHearingClerk")
    private DynamicFixedListType allocateHearingClerk;
    @CCD(
            label = "Panel Type",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_SitAlone",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("allocateHearingSitAlone")
    private String allocateHearingSitAlone;
    @CCD(
            label = "Employment Judge",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("allocateHearingJudge")
    private DynamicFixedListType allocateHearingJudge;
    @CCD(
            label = "Employment Judge",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("allocateHearingAdditionalJudge")
    private DynamicFixedListType allocateHearingAdditionalJudge;
    @CCD(
            label = "Employer Member",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("allocateHearingEmployerMember")
    private DynamicFixedListType allocateHearingEmployerMember;
    @CCD(
            label = "EmployeeMember",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("allocateHearingEmployeeMember")
    private DynamicFixedListType allocateHearingEmployeeMember;
    @CCD(
            label = "Postponed by",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PostponedBy",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("allocateHearingPostponedBy")
    private String allocateHearingPostponedBy;
    @CCD(
            label = "Hearing Status",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingStatus",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("allocateHearingStatus")
    private String allocateHearingStatus;
    @CCD(ignore = true)
    @JsonProperty("allocateHearingReadingDeliberation")
    private String allocateHearingReadingDeliberation;

    // Hearing Details fields
    @CCD(
            label = "Select Hearing",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("hearingDetailsHearing")
    private DynamicFixedListType hearingDetailsHearing;
    @CCD(
            label = "Upload hearing notes",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("uploadHearingNotesDocument")
    private Document uploadHearingNotesDocument;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("doesHearingNotesDocExist")
    private String doesHearingNotesDocExist;
    @CCD(
            label = "Do you want to remove the uploaded hearing notes document?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_removeDocument",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("removeHearingNotesDocument")
    private List<String> removeHearingNotesDocument;
    @CCD(
            label = "Hearing Status",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_HearingStatus"
    )
    @JsonProperty("hearingDetailsStatus")
    private String hearingDetailsStatus;
    @CCD(
            label = "Postponed by",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PostponedBy"
    )
    @JsonProperty("hearingDetailsPostponedBy")
    private String hearingDetailsPostponedBy;
    @CCD(
            label = "Has the case or part of the case been disposed?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("hearingDetailsCaseDisposed")
    private String hearingDetailsCaseDisposed;
    @CCD(label = "Has the hearing been part heard?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("hearingDetailsPartHeard")
    private String hearingDetailsPartHeard;
    @CCD(label = "Is there a reserved Judgment?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("hearingDetailsReservedJudgment")
    private String hearingDetailsReservedJudgment;
    @CCD(
            label = "Attendees (Claimant)",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Attendee"
    )
    @JsonProperty("hearingDetailsAttendeeClaimant")
    private String hearingDetailsAttendeeClaimant;
    @CCD(label = "Number of Non Attendees (Respondent) ", searchable = false)
    @JsonProperty("hearingDetailsAttendeeNonAttendees")
    private String hearingDetailsAttendeeNonAttendees;
    @CCD(label = "Respondent Attended - No Representative", searchable = false)
    @JsonProperty("hearingDetailsAttendeeRespNoRep")
    private String hearingDetailsAttendeeRespNoRep;
    @CCD(label = "Respondent and Representative Attended", searchable = false)
    @JsonProperty("hearingDetailsAttendeeRespAndRep")
    private String hearingDetailsAttendeeRespAndRep;
    @CCD(label = "Respondent representative only attended", searchable = false)
    @JsonProperty("hearingDetailsAttendeeRepOnly")
    private String hearingDetailsAttendeeRepOnly;
    @CCD(label = "Start Time", searchable = false)
    @JsonProperty("hearingDetailsTimingStart")
    private String hearingDetailsTimingStart;
    @CCD(label = "Break", searchable = false)
    @JsonProperty("hearingDetailsTimingBreak")
    private String hearingDetailsTimingBreak;
    @CCD(label = "Resume", searchable = false)
    @JsonProperty("hearingDetailsTimingResume")
    private String hearingDetailsTimingResume;
    @CCD(label = "Finish", searchable = false)
    @JsonProperty("hearingDetailsTimingFinish")
    private String hearingDetailsTimingFinish;
    @CCD(label = "Duration", searchable = false)
    @JsonProperty("hearingDetailsTimingDuration")
    private String hearingDetailsTimingDuration;
    @CCD(label = "Hearing Notes", searchable = false, typeOverride = FieldType.TextArea)
    @JsonProperty("hearingDetailsHearingNotes2")
    private String hearingDetailsHearingNotes2;

    // ET1 Vetting
    @CCD(
            label = "Track Type",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("trackType")
    private String trackType;
    @CCD(
            label = "ET1 Vetting Document",
            categoryID = "C71",
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("et1VettingDocument")
    private UploadedDocumentType et1VettingDocument;
    @CCD(
            label = "Before You Start placeholder",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingBeforeYouStart")
    private String et1VettingBeforeYouStart;
    // ET1 Vetting - Can we serve the claim?
    @CCD(
            label = "Claimant Details placeholder",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingClaimantDetailsMarkUp")
    private String et1VettingClaimantDetailsMarkUp;
    @CCD(
            label = "Respondent Details placeholder",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingRespondentDetailsMarkUp")
    private String et1VettingRespondentDetailsMarkUp;
    @CCD(
            label = "Can we serve the claim with these contact details?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("et1VettingCanServeClaimYesOrNo")
    private String et1VettingCanServeClaimYesOrNo;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("et1VettingCanServeClaimNoReason")
    private String et1VettingCanServeClaimNoReason;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("et1VettingCanServeClaimGeneralNote")
    private String et1VettingCanServeClaimGeneralNote;
    // ET1 Vetting - Acas certificate?
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingRespondentAcasDetails1")
    private String et1VettingRespondentAcasDetails1;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingRespondentAcasDetails2")
    private String et1VettingRespondentAcasDetails2;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingRespondentAcasDetails3")
    private String et1VettingRespondentAcasDetails3;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingRespondentAcasDetails4")
    private String et1VettingRespondentAcasDetails4;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingRespondentAcasDetails5")
    private String et1VettingRespondentAcasDetails5;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingRespondentAcasDetails6")
    private String et1VettingRespondentAcasDetails6;
    @CCD(
            label = "Is there an Acas certificate?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertIsYesOrNo1")
    private String et1VettingAcasCertIsYesOrNo1;
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertExemptYesOrNo1")
    private String et1VettingAcasCertExemptYesOrNo1;
    @CCD(
            label = "Is there an Acas certificate?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertIsYesOrNo2")
    private String et1VettingAcasCertIsYesOrNo2;
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertExemptYesOrNo2")
    private String et1VettingAcasCertExemptYesOrNo2;
    @CCD(
            label = "Is there an Acas certificate?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertIsYesOrNo3")
    private String et1VettingAcasCertIsYesOrNo3;
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertExemptYesOrNo3")
    private String et1VettingAcasCertExemptYesOrNo3;
    @CCD(
            label = "${et1VettingRespondentAcasDetails4}",
            typeOverride = FieldType.Label,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingRespondentAcasDetailsLabel4")
    private String et1VettingRespondentAcasDetailsLabel4;
    @CCD(
            label = "Is there an Acas certificate?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertIsYesOrNo4")
    private String et1VettingAcasCertIsYesOrNo4;
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertExemptYesOrNo4")
    private String et1VettingAcasCertExemptYesOrNo4;
    @CCD(
            label = "Is there an Acas certificate?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertIsYesOrNo5")
    private String et1VettingAcasCertIsYesOrNo5;
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertExemptYesOrNo5")
    private String et1VettingAcasCertExemptYesOrNo5;
    @CCD(
            label = "Is there an Acas certificate?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertIsYesOrNo6")
    private String et1VettingAcasCertIsYesOrNo6;
    @CCD(
            label = "Has the claimant ticked one of the exemptions?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertExemptYesOrNo6")
    private String et1VettingAcasCertExemptYesOrNo6;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1VettingAcasCertGeneralNote")
    private String et1VettingAcasCertGeneralNote;
    @CCD(
            label = "Vetting completed by:",
            searchable = false,
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class}
    )
    @JsonProperty("et1VettingCompletedBy")
    private String et1VettingCompletedBy;
    @CCD(
            label = "Date completed:",
            searchable = false,
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class}
    )
    @JsonProperty("et1DateCompleted")
    private String et1DateCompleted;
    @CCD(
            label = "Initial consideration completed by:",
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class}
    )
    @JsonProperty("icCompletedBy")
    private String icCompletedBy;
    @CCD(label = "Date completed:", access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class})
    @JsonProperty("icDateCompleted")
    private String icDateCompleted;

    //ET1 Vetting -  Substantive Defects
    @CCD(
            label = "Possible substantive defects",
            hint = "Select all that apply. Does the claim, or part of it, appear to be a claim which:",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_Defects",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("substantiveDefectsList")
    private List<String> substantiveDefectsList;
    @CCD(
            label = "The tribunal has no jurisdiction to consider",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("rule121aTextArea")
    private String rule121aTextArea;
    @CCD(
            label = "Is in a form which cannot sensibly be responded to or otherwise an abuse of process",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("rule121bTextArea")
    private String rule121bTextArea;
    @CCD(
            label = "Has neither an EC number nor claims one of the EC exemptions",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("rule121cTextArea")
    private String rule121cTextArea;
    @CCD(
            label = "States that one of the EC exceptions applies but it might not",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("rule121dTextArea")
    private String rule121dTextArea;
    @CCD(
            label = "Institutes relevant proceedings and the EC number on the claim form does not match the EC number on the Acas certificate",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("rule121daTextArea")
    private String rule121daTextArea;
    @CCD(
            label = "Has a different claimant name on the ET1 to the claimant name on the Acas certificate",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("rule121eTextArea")
    private String rule121eTextArea;
    @CCD(
            label = "Has a different respondent name on the ET1 to the respondent name on the Acas certificate",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("rule121fTextArea")
    private String rule121fTextArea;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1SubstantiveDefectsGeneralNotes")
    private String et1SubstantiveDefectsGeneralNotes;
    @CCD(ignore = true)
    @JsonProperty("icEt1SubstantiveDefects")
    private String icEt1SubstantiveDefects;
    @CCD(ignore = true)
    @JsonProperty("icEt1ReferralToJudgeOrLOListWithDetails")
    private String icEt1ReferralToJudgeOrLOListWithDetails;
    @CCD(ignore = true)
    @JsonProperty("icEt1ReferralToREJOrVPListWithDetails")
    private String icEt1ReferralToREJOrVPListWithDetails;
    @CCD(ignore = true)
    @JsonProperty("icEt1OtherReferralListDetails")
    private String icEt1OtherReferralListDetails;


    // ET1 Vetting - Jurisdiction codes
    @CCD(
            label = "Are these codes correct?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("areTheseCodesCorrect")
    private String areTheseCodesCorrect;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("codesCorrectGiveDetails")
    private String codesCorrectGiveDetails;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("et1JurisdictionCodeGeneralNotes")
    private String et1JurisdictionCodeGeneralNotes;
    @CCD(
            label = "existingJurisdictionCodes",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("existingJurisdictionCodes")
    private String existingJurisdictionCodes;
    @CCD(
            label = "Jurisdiction code",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "JurisdictionCode",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("vettingJurisdictionCodeCollection")
    private List<VettingJurCodesTypeItem> vettingJurisdictionCodeCollection;

    // ET1 Vetting - Track allocation
    @CCD(
            label = "Is the track allocation correct?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_isTrackAllocationCorrect",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("isTrackAllocationCorrect")
    private String isTrackAllocationCorrect;
    @CCD(
            label = "Track allocation",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_suggestAnotherTrack",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("suggestAnotherTrack")
    private String suggestAnotherTrack;
    @CCD(
            label = "Why should we change the track allocation?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("whyChangeTrackAllocation")
    private String whyChangeTrackAllocation;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("trackAllocationGeneralNotes")
    private String trackAllocationGeneralNotes;
    @CCD(
            label = "Is this location correct?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_isLocationCorrect",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("isLocationCorrect")
    private String isLocationCorrect;
    @CCD(
            label = "Why should we change the office?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("whyChangeOffice")
    private String whyChangeOffice;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1LocationGeneralNotes")
    private String et1LocationGeneralNotes;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("trackAllocation")
    private String trackAllocation;
    @CCD(
            label = "tribunalAndOfficeLocation",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("tribunalAndOfficeLocation")
    private String tribunalAndOfficeLocation;
    @CCD(
            label = "regionalOffice",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("regionalOffice")
    private String regionalOffice;
    @CCD(
            label = "Local or regional office",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("regionalOfficeList")
    private DynamicFixedListType regionalOfficeList;
    // ET1 Vetting - Hearing venues
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1AddressDetails")
    private String et1AddressDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("et1TribunalRegion")
    private String et1TribunalRegion;
    @CCD(
            label = "Hearing venue selected",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1HearingVenues")
    private DynamicFixedListType et1HearingVenues;
    @CCD(
            label = "Do you want to suggest a hearing venue?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1SuggestHearingVenue")
    private String et1SuggestHearingVenue;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1HearingVenueGeneralNotes")
    private String et1HearingVenueGeneralNotes;
    @CCD(
            label = "Is the respondent a government agency or a major employer?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("et1GovOrMajorQuestion")
    private String et1GovOrMajorQuestion;

    // ET1 Vetting - Further questions
    @CCD(
            label = "Are reasonable adjustments required?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("et1ReasonableAdjustmentsQuestion")
    private String et1ReasonableAdjustmentsQuestion;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("et1ReasonableAdjustmentsTextArea")
    private String et1ReasonableAdjustmentsTextArea;
    @CCD(
            label = "Can the claimant attend a video hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("et1VideoHearingQuestion")
    private String et1VideoHearingQuestion;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("et1VideoHearingTextArea")
    private String et1VideoHearingTextArea;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("et1FurtherQuestionsGeneralNotes")
    private String et1FurtherQuestionsGeneralNotes;

    // ET1 Vetting - Referral to judge
    @CCD(
            label = "Possible referral to a judge or legal officer",
            hint = "Does the claim include any of the following?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_judgeOrLO",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("referralToJudgeOrLOList")
    private List<String> referralToJudgeOrLOList;
    @CCD(
            label = "A claim of interim relief",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("aClaimOfInterimReliefTextArea")
    private String aclaimOfInterimReliefTextArea;
    @CCD(
            label = "A statutory appeal",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("aStatutoryAppealTextArea")
    private String astatutoryAppealTextArea;
    @CCD(
            label = "An allegation of commission of sexual offence",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("anAllegationOfCommissionOfSexualOffenceTextArea")
    private String anAllegationOfCommissionOfSexualOffenceTextArea;
    @CCD(
            label = "Insolvency",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("insolvencyTextArea")
    private String insolvencyTextArea;
    @CCD(
            label = "Jurisdictions unclear",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("jurisdictionsUnclearTextArea")
    private String jurisdictionsUnclearTextArea;
    @CCD(
            label = "Length of service",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("lengthOfServiceTextArea")
    private String lengthOfServiceTextArea;
    @CCD(
            label = "Potentially linked cases in the ECM",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("potentiallyLinkedCasesInTheEcmTextArea")
    private String potentiallyLinkedCasesInTheEcmTextArea;
    @CCD(
            label = "Rule 49 issues",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("rule50IssuesTextArea")
    private String rule50IssuesTextArea;
    @CCD(
            label = "Another reason for judicial referral",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("anotherReasonForJudicialReferralTextArea")
    private String anotherReasonForJudicialReferralTextArea;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("et1JudgeReferralGeneralNotes")
    private String et1JudgeReferralGeneralNotes;
    @CCD(
            label = "Possible referral to Regional Employment Judge or Vice-President",
            hint = "Does the claim include any of the following?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_REJOrVP",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("referralToREJOrVPList")
    private List<String> referralToREJOrVPList;

    // ET1 Vetting - Referral to Regional Employment judge
    @CCD(
            label = "A claimant covered by vexatious litigant order",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("vexatiousLitigantOrderTextArea")
    private String vexatiousLitigantOrderTextArea;
    @CCD(
            label = "A national security issue",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("aNationalSecurityIssueTextArea")
    private String anationalSecurityIssueTextArea;
    @CCD(
            label = "A part of national multiple / covered by Presidential case management order",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("nationalMultipleOrPresidentialOrderTextArea")
    private String nationalMultipleOrPresidentialOrderTextArea;
    @CCD(
            label = "A request for transfer to another ET region",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("transferToOtherRegionTextArea")
    private String transferToOtherRegionTextArea;
    @CCD(
            label = "A request for service abroad",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("serviceAbroadTextArea")
    private String serviceAbroadTextArea;
    @CCD(
            label = "A sensitive issue which may attract publicity or need early allocation to a specific judge",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("aSensitiveIssueTextArea")
    private String asensitiveIssueTextArea;
    @CCD(
            label = "Any potential conflict involving judge, non-legal member or HMCTS staff member",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("anyPotentialConflictTextArea")
    private String anyPotentialConflictTextArea;
    @CCD(
            label = "Another reason for Regional Employment Judge / Vice-President referral",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("anotherReasonREJOrVPTextArea")
    private String anotherReasonREJOrVPTextArea;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("et1REJOrVPReferralGeneralNotes")
    private String et1REJOrVPReferralGeneralNotes;
    @CCD(
            label = "Does the claim include any other factors",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_otherFactors",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("otherReferralList")
    private List<String> otherReferralList;

    // ET1 Vetting - Other Factors
    @CCD(
            label = "The whole or any part of the claim is out of time",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("claimOutOfTimeTextArea")
    private String claimOutOfTimeTextArea;
    @CCD(
            label = "The claim is part of a multiple claim",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("multipleClaimTextArea")
    private String multipleClaimTextArea;
    @CCD(
            label = "The claim has a potential issue about employment status",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("employmentStatusIssuesTextArea")
    private String employmentStatusIssuesTextArea;
    @CCD(
            label = "The claim has PID jurisdiction and claimant wants it forwarded to relevant regulator - Box 10.1",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("pidJurisdictionRegulatorTextArea")
    private String pidJurisdictionRegulatorTextArea;
    @CCD(
            label = "The claimant prefers a video hearing",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("videoHearingPreferenceTextArea")
    private String videoHearingPreferenceTextArea;
    @CCD(
            label = "The claim has Rule 49 issues",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("rule50IssuesForOtherReferralTextArea")
    private String rule50IssuesForOtherReferralTextArea;
    @CCD(
            label = "The claim has other relevant factors for judicial referral",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("anotherReasonForOtherReferralTextArea")
    private String anotherReasonForOtherReferralTextArea;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
    )
    @JsonProperty("et1OtherReferralGeneralNotes")
    private String et1OtherReferralGeneralNotes;
    @CCD(
            label = "Additional Information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1VettingAdditionalInformationTextArea")
    private String et1VettingAdditionalInformationTextArea;


    // ET1 Serving
    @CCD(
            label = "Upload document PDF",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ServingDocumentUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CitizenCrudAccess.class}
    )
    @JsonProperty("servingDocumentCollection")
    private List<DocumentTypeItem> servingDocumentCollection;
    @CCD(
            label = "Serving document other type name placeholder",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentCrudPlus2RolesWfyqbwAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("otherTypeDocumentName")
    private String otherTypeDocumentName;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_ServingDocumentRecipient",
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("servingDocumentRecipient")
    private List<String> servingDocumentRecipient;
    @CCD(
            label = "claimantAndRespondentAddresses",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentCrudPlus2RolesWfyqbwAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("claimantAndRespondentAddresses")
    private String claimantAndRespondentAddresses;
    @CCD(
            label = "Email link to Acas placeholder",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentCrudPlus2RolesWfyqbwAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("emailLinkToAcas")
    private String emailLinkToAcas;

    //    et3vetting
    @CCD(
            label = "Select the respondent you are processing",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3ChooseRespondent")
    private DynamicFixedListType et3ChooseRespondent;
    @CCD(
            label = "et3Date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3Date")
    private String et3Date;
    // ET3 Response Page
    @CCD(
            label = "Is there an ET3 response?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CitizenRAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3IsThereAnEt3Response")
    private String et3IsThereAnEt3Response;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3NoEt3Response")
    private String et3NoEt3Response;
    @CCD(
            label = "General Notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3GeneralNotes")
    private String et3GeneralNotes;
    // ET3 Company House search document page
    @CCD(
            label = "Is there a Companies House search document?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3IsThereACompaniesHouseSearchDocument")
    private String et3IsThereACompaniesHouseSearchDocument;
    @CCD(
            label = "Upload the Companies House search document",
            categoryID = "C18",
            searchable = false,
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3CompanyHouseDocument")
    private UploadedDocumentType et3CompanyHouseDocument;
    @CCD(
            label = "General Notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3GeneralNotesCompanyHouse")
    private String et3GeneralNotesCompanyHouse;
    // ET3 Individual insolvency search document page
    @CCD(
            label = "Is there an individual insolvency search document?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3IsThereAnIndividualSearchDocument")
    private String et3IsThereAnIndividualSearchDocument;
    @CCD(
            label = "Upload the individual insolvency search document",
            categoryID = "C18",
            searchable = false,
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3IndividualInsolvencyDocument")
    private UploadedDocumentType et3IndividualInsolvencyDocument;
    @CCD(
            label = "General Notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3GeneralNotesIndividualInsolvency")
    private String et3GeneralNotesIndividualInsolvency;
    // ET3 Legal issue page
    @CCD(
            label = "Is there an issue with whether the respondent is a legal entity?",
            hint = "Check all respondents. If any appear to have an issue with their legal status, select Yes.",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_respondent_legal_entity",
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3LegalIssue")
    private String et3LegalIssue;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3LegalIssueGiveDetails")
    private String et3LegalIssueGiveDetails;
    @CCD(
            label = "General Notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3GeneralNotesLegalEntity")
    private String et3GeneralNotesLegalEntity;
    // ET3 Response in time page
    @CCD(
            label = "Did we receive the ET3 response in time?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3ResponseInTime")
    private String et3ResponseInTime;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3ResponseInTimeDetails")
    private String et3ResponseInTimeDetails;
    // ET3 Respondents Name page
    @CCD(
            label = "et3NameAddressRespondent",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3NameAddressRespondent")
    private String et3NameAddressRespondent;
    @CCD(
            label = "Do we have the respondent's name?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3DoWeHaveRespondentsName")
    private String et3DoWeHaveRespondentsName;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3GeneralNotesRespondentName")
    private String et3GeneralNotesRespondentName;
    @CCD(
            label = "Does the respondent's name match?",
            hint = "Check the ET1 name.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3DoesRespondentsNameMatch")
    private String et3DoesRespondentsNameMatch;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3RespondentNameMismatchDetails")
    private String et3RespondentNameMismatchDetails;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3GeneralNotesRespondentNameMatch")
    private String et3GeneralNotesRespondentNameMatch;
    // ET3 Respondents Address page
    @CCD(
            label = "Do we have the respondent's address?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3DoWeHaveRespondentsAddress")
    private String et3DoWeHaveRespondentsAddress;
    @CCD(
            label = "Does the respondent's address match?",
            hint = "Check the ET1 address.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3DoesRespondentsAddressMatch")
    private String et3DoesRespondentsAddressMatch;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3RespondentAddressMismatchDetails")
    private String et3RespondentAddressMismatchDetails;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3GeneralNotesRespondentAddress")
    private String et3GeneralNotesRespondentAddress;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3GeneralNotesAddressMatch")
    private String et3GeneralNotesAddressMatch;
    // ET3 Case Listed Page
    @CCD(
            label = "Placeholder",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3HearingDetails")
    private String et3HearingDetails;
    @CCD(
            label = "Is the case listed for hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3IsCaseListedForHearing")
    private String et3IsCaseListedForHearing;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3IsCaseListedForHearingDetails")
    private String et3IsCaseListedForHearingDetails;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3GeneralNotesCaseListed")
    private String et3GeneralNotesCaseListed;
    // ET3 Transfer Application
    @CCD(
            label = "Placeholder",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3TribunalLocation")
    private String et3TribunalLocation;
    @CCD(
            label = "Is this location correct?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_et3_tribunal_location_change",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3IsThisLocationCorrect")
    private String et3IsThisLocationCorrect;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3GeneralNotesTransferApplication")
    private String et3GeneralNotesTransferApplication;
    @CCD(
            label = "England & Wales regional office",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3RegionalOffice")
    private String et3RegionalOffice;
    @CCD(
            label = "Why should we change the office?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3WhyWeShouldChangeTheOffice")
    private String et3WhyWeShouldChangeTheOffice;
    // ET3 Resist the claim
    @CCD(
            label = "Does the respondent wish to contest any part of the claim?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_contest_claim_status",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3ContestClaim")
    private String et3ContestClaim;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3ContestClaimGiveDetails")
    private String et3ContestClaimGiveDetails;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3GeneralNotesContestClaim")
    private String et3GeneralNotesContestClaim;
    // ET3 Contract claim section 7
    @CCD(
            label = "Is there an Employer's Contract Claim in section 7 of the ET3 response?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3ContractClaimSection7")
    private String et3ContractClaimSection7;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3ContractClaimSection7Details")
    private String et3ContractClaimSection7Details;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3GeneralNotesContractClaimSection7")
    private String et3GeneralNotesContractClaimSection7;
    // ET3 suggested issues
    @CCD(
            label = "Are there any issues identified for the judge's initial consideration - prospects of claim / response arguable? (Rule 27)",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDPlus1RolesZwtixtAccess.class}
    )
    @JsonProperty("et3Rule26")
    private String et3Rule26;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3Rule26Details")
    private String et3Rule26Details;
    @CCD(
            label = "Are there any other suggested orders, directions or issues?",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_et3_suggested_issues",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3SuggestedIssues")
    private List<String> et3SuggestedIssues;
    @CCD(
            label = "Applications for strike out or deposit",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3SuggestedIssuesStrikeOut")
    private String et3SuggestedIssuesStrikeOut;
    @CCD(
            label = "Interpreters",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3SuggestedIssueInterpreters")
    private String et3SuggestedIssueInterpreters;
    @CCD(
            label = "Jurisdictional issues",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3SuggestedIssueJurisdictional")
    private String et3SuggestedIssueJurisdictional;
    @CCD(
            label = "Request for adjustments",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3SuggestedIssueAdjustments")
    private String et3SuggestedIssueAdjustments;
    @CCD(
            label = "Rule 49",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3SuggestedIssueRule50")
    private String et3SuggestedIssueRule50;
    @CCD(
            label = "Time points",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3SuggestedIssueTimePoints")
    private String et3SuggestedIssueTimePoints;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3GeneralNotesRule26")
    private String et3GeneralNotesRule26;
    // ET3 Final notes
    @CCD(
            label = "Additional information",
            hint = "Enter any additional information which may be useful for a judge or legal officer to consider.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3AdditionalInformation")
    private String et3AdditionalInformation;

    // ET Initial Consideration
    @CCD(
            label = "ET1 Vetting Issues",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
    )
    @JsonProperty("icEt1VettingIssuesDetail")
    private String icEt1VettingIssuesDetail;
    @CCD(
            label = "ET3 Processing Issues",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
    )
    @JsonProperty("icEt3ProcessingIssuesDetail")
    private String icEt3ProcessingIssuesDetail;

    @CCD(
            label = "To help you complete this placeholder",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
    )
    @JsonProperty("initialConsiderationBeforeYouStart")
    private String initialConsiderationBeforeYouStart;
    @CCD(label = "Initial Consideration Document", categoryID = "C23", typeOverride = FieldType.Document)
    @JsonProperty("etInitialConsiderationDocument")
    private UploadedDocumentType etInitialConsiderationDocument;
    @CCD(
            label = "etInitialConsiderationRespondent",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("etInitialConsiderationRespondent")
    private String etInitialConsiderationRespondent;
    @CCD(
            label = "icRespondentHearingPanelPreference",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("icRespondentHearingPanelPreference")
    private String icRespondentHearingPanelPreference;
    @CCD(ignore = true)
    @JsonProperty("icRespondentHearingPanelPreferenceReason")
    private String icRespondentHearingPanelPreferenceReason;
    @CCD(
            label = "etInitialConsiderationHearing",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("etInitialConsiderationHearing")
    private String etInitialConsiderationHearing;

    @CCD(
            label = "etIcPartiesHearingPanelPreferenceHeader",
            hint = "<h2>Parties Hearing Panel Preferences</h2>",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
    )
    @JsonProperty("etIcPartiesHearingPanelPreferenceHeader")
    private String etIcPartiesHearingPanelPreferenceHeader;
    @CCD(
            label = "etIcPartiesHearingPanelPreference",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
    )
    @JsonProperty("etIcPartiesHearingPanelPreference")
    private String etIcPartiesHearingPanelPreference;
    @CCD(
            label = "etIcPartiesHearingFormat",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
    )
    @JsonProperty("etIcPartiesHearingFormat")
    private String etIcPartiesHearingFormat;

    @CCD(
            label = "etIcHearingPanelPreference",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("etIcHearingPanelPreference")
    private String etIcHearingPanelPreference;
    @CCD(
            label = "etInitialConsiderationJurisdictionCodes",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("etInitialConsiderationJurisdictionCodes")
    private String etInitialConsiderationJurisdictionCodes;
    @CCD(
            label = "Are there any issues or instructions regarding the receipt of the ET3 form?",
            hint = "For example the date it was due, the date it was received or any application for extension of time (Rule 18 and 20)",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icReceiptET3FormIssues")
    private String icReceiptET3FormIssues;
    @CCD(
            label = "Are there any issues or instructions regarding the respondent's name or identity?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icRespondentsNameIdentityIssues")
    private String icRespondentsNameIdentityIssues;
    @CCD(
            label = "Are there any issues or instructions regarding the jurisdiction codes?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icJurisdictionCodeIssues")
    private String icJurisdictionCodeIssues;
    @CCD(
            label = "Are there any issues or instructions regarding the applications?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icApplicationIssues")
    private String icApplicationIssues;
    @CCD(
            label = "Are there any issues or instructions regarding an Employer’s Contract Claim?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icEmployersContractClaimIssues")
    private String icEmployersContractClaimIssues;
    @CCD(
            label = "Are there any issues or instructions regarding the prospects of the claim or response?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icClaimProspectIssues")
    private String icClaimProspectIssues;
    @CCD(
            label = "Are there any issues or instructions regarding the listing?",
            hint = "For example list for PH, if public or private and set out the issues to be determined with directions, in person or video",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icListingIssues")
    private String icListingIssues;
    @CCD(
            label = "Should the case be listed for a private preliminary hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("icListingPreliminaryHearing")
    private String icListingPreliminaryHearing;
    @CCD(
            label = "Are there any issues or instructions where DDA or disability is disputed?",
            hint = "The claimant is to provide a statement - limited to 750 words, explaining the length of the disability, the nature of its effects upon daily activities and any existing medical evidence relied upon - the the respondent by [date]. The respondent should state their position on disability, giving reasons for any denial, by [date]",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icDdaDisabilityIssues")
    private String icDdaDisabilityIssues;
    @CCD(
            label = "Should either side be ordered to provide any further information?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icOrderForFurtherInformation")
    private String icOrderForFurtherInformation;
    @CCD(
            label = "Are there any issues or instructions to consider or final orders to be given?",
            hint = "For example, Rule 50, transfer to another region, interpreters, adjustments required for hearing",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icOtherIssuesOrFinalOrders")
    private String icOtherIssuesOrFinalOrders;
    @CCD(
            label = "Are there any issues or instructions regarding the jurisdiction codes?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class}
    )
    @JsonProperty("etICJuridictionCodesInvalid")
    private String etICJuridictionCodesInvalid;
    @CCD(label = "Give details", searchable = false, typeOverride = FieldType.TextArea, access = {DefaultAccess.class})
    @JsonProperty("etICInvalidDetails")
    private String etICInvalidDetails;
    @CCD(
            label = "Can the claim proceed due to an arguable claim and/or response?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class}
    )
    @JsonProperty("etICCanProceed")
    private String etICCanProceed;
    @CCD(
            label = "Is the hearing already listed?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class}
    )
    @JsonProperty("etICHearingAlreadyListed")
    private String etICHearingAlreadyListed;
    // ET Initial Consideration - Hearing Not Listed
    @CCD(
            label = "Hearing not listed",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICHearingNotListed",
            access = {DefaultAccess.class}
    )
    @JsonProperty("etICHearingNotListedList")
    private List<String> etICHearingNotListedList;

    @CCD(label = "Seek comments on the video hearing", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("etICHearingNotListedSeekComments")
    private EtICSeekComments etICHearingNotListedSeekComments;
    @CCD(label = "List for preliminary hearing", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("etICHearingNotListedListForPrelimHearing")
    private EtICListForPreliminaryHearing etICHearingNotListedListForPrelimHearing;
    @CCD(label = "List for final hearing", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("etICHearingNotListedListForFinalHearing")
    private EtICListForFinalHearing etICHearingNotListedListForFinalHearing;
    @CCD(label = "UDL hearing", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("etICHearingNotListedUDLHearing")
    private EtIcudlHearing etICHearingNotListedUDLHearing;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea, access = {DefaultAccess.class})
    @JsonProperty("etICHearingNotListedAnyOtherDirections")
    private String etICHearingNotListedAnyOtherDirections;

    //New fields to replace the hidden "hearing not listed" related fields
    @CCD(
            label = "Hearing not listed",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICHearingNotListed_v2",
            access = {DefaultAccess.class}
    )
    @JsonProperty("etICHearingNotListedListUpdated")
    private List<String> etICHearingNotListedListUpdated;
    @CCD(label = "List for preliminary hearing", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("etICHearingNotListedListForPrelimHearingUpdated")
    private EtICListForPreliminaryHearingUpdated etICHearingNotListedListForPrelimHearingUpdated;
    @CCD(
            label = "List for final hearing",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCudAccess.class}
    )
    @JsonProperty("etICHearingNotListedListForFinalHearingUpdated")
    private EtICListForFinalHearingUpdated etICHearingNotListedListForFinalHearingUpdated;
    @CCD(ignore = true)
    @JsonProperty("etICHearingNotListedDoNotListHearingDirections")
    private String etICHearingNotListedDoNotListHearingDirections;
    @CCD(ignore = true)
    @JsonProperty("etICHearingNotListedOtherDirections")
    private String etICHearingNotListedOtherDirections;
    // ET Initial Consideration - Hearing already listed

    @CCD(label = "Hearing already listed", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("etICHearingListedAnswers")
    private EtICHearingListedAnswers etICHearingListedAnswers;
    @CCD(ignore = true)
    @JsonProperty("etICHearingListed")
    private List<String> etICHearingListed;
    @CCD(ignore = true)
    @JsonProperty("etICExtendDurationGiveDetails")
    private String etICExtendDurationGiveDetails;
    @CCD(ignore = true)
    @JsonProperty("etICOtherGiveDetails")
    private String etICOtherGiveDetails;
    @CCD(
            label = "Any other directions",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
    )
    @JsonProperty("etICHearingAnyOtherDirections")
    private String etICHearingAnyOtherDirections;
    @CCD(ignore = true)
    @JsonProperty("etICPostponeGiveDetails")
    private String etICPostponeGiveDetails;
    @CCD(ignore = true)
    @JsonProperty("etICConvertPreliminaryGiveDetails")
    private String etICConvertPreliminaryGiveDetails;
    @CCD(ignore = true)
    @JsonProperty("etICConvertF2fGiveDetails")
    private String etICConvertF2fGiveDetails;
    // ET Initial Consideration – Further Info
    @CCD(
            label = "Further information",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_furtherInformation",
            access = {DefaultAccess.class}
    )
    @JsonProperty("etICFurtherInformation")
    private List<String> etICFurtherInformation;
    @CCD(label = "Further information required", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("etICFurtherInfoAnswers")
    private EtICFurtherInfoAnswers etICFurtherInfoAnswers;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea, access = {DefaultAccess.class})
    @JsonProperty("etICFurtherInformationHearingAnyOtherDirections")
    private String etICFurtherInformationHearingAnyOtherDirections;
    @CCD(
            label = "Give details to include in the letter",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
    )
    @JsonProperty("etICFurtherInformationGiveDetails")
    private String etICFurtherInformationGiveDetails;
    @CCD(
            label = "How much time to comply?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
    )
    @JsonProperty("etICFurtherInformationTimeToComply")
    private String etICFurtherInformationTimeToComply;
    @CCD(label = "Issue Rule 28 Notice and order", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("etInitialConsiderationRule27")
    private EtInitialConsiderationRule27 etInitialConsiderationRule27;
    @CCD(label = "Issue Rule 29 Notice and order", searchable = false, access = {DefaultAccess.class})
    @JsonProperty("etInitialConsiderationRule28")
    private EtInitialConsiderationRule28 etInitialConsiderationRule28;

    // Initial Consideration Document Collections
    @CCD(
            label = "Upload document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
    )
    @JsonProperty("icDocumentCollection1")
    private List<DocumentTypeItem> icDocumentCollection1;
    @CCD(
            label = "Upload document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
    )
    @JsonProperty("icDocumentCollection2")
    private List<DocumentTypeItem> icDocumentCollection2;
    @CCD(
            label = "Upload document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
    )
    @JsonProperty("icDocumentCollection3")
    private List<DocumentTypeItem> icDocumentCollection3;
    @CCD(
            label = "Upload document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "icDocumentUpload",
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("icAllDocumentCollection")
    private List<DocumentTypeItem> icAllDocumentCollection;

    // ET3 Response
    @CCD(
            label = "Placeholder",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseShowInset")
    private String et3ResponseShowInset;
    // ET3 Response - Claimant name page (3)
    @CCD(
            label = "Placeholder",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseClaimantName")
    private String et3ResponseClaimantName;
    @CCD(
            label = "Is this the correct claimant for the claim you're responding to?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseIsClaimantNameCorrect")
    private String et3ResponseIsClaimantNameCorrect;
    @CCD(
            label = "What is the correct name of the claimant?",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseClaimantNameCorrection")
    private String et3ResponseClaimantNameCorrection;
    // ET3 Response - What is the respondent's name (4)
    @CCD(
            label = "hidden",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseNameShowInset")
    private String et3ResponseNameShowInset;
    @CCD(
            label = "Enter the respondent's registered or legal name",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseRespondentLegalName")
    private String et3ResponseRespondentLegalName;
    @CCD(
            label = "Enter the company number if applicable",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseRespondentCompanyNumber")
    private String et3ResponseRespondentCompanyNumber;
    @CCD(
            label = "What type of employer is the respondent?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_employer_type",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseRespondentEmployerType")
    private String et3ResponseRespondentEmployerType;
    @CCD(
            label = "If individual, what is their preferred title?",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseRespondentPreferredTitle")
    private String et3ResponseRespondentPreferredTitle;
    @CCD(
            label = "Name of contact at respondent's address if not you as the representative",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseRespondentContactName")
    private String et3ResponseRespondentContactName;
    // ET3 Response - Respondent address (5)
    @CCD(
            label = "Enter a UK postcode",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3RespondentAddress")
    private Address et3RespondentAddress;
    @CCD(
            label = "DX address (if known)",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseDXAddress")
    private String et3ResponseDXAddress;
    // ET3 Response - Representative Contact method, page (6)
    @CCD(
            label = "How would you prefer to be contacted?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_contact_preference",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseContactPreference")
    private String et3ResponseContactPreference;
    @CCD(
            label = "Provide a reason why you have selected post",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseContactReason")
    private String et3ResponseContactReason;
    // ET3 Response - Representative Contact language, page (6)
    @CCD(
            label = "What language do you want us to use when we contact you?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_languages",
            access = {SOLICITORACrudPlus9RolesLhlklnAccess.class, CLAIMANTSOLICITORDAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et3ResponseContactLanguage")
    private String et3ResponseContactLanguage;
    // ET3 Response - Representative Phone number page (6)
    @CCD(
            label = "What is your contact phone number?",
            hint = "Where we can contact you during the day. For international numbers include the country code.",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponsePhone")
    private String et3ResponsePhone;
    // ET3 Response - Representative reference number (6)
    @CCD(
            label = "Contact address if different from registered address",
            searchable = false,
            access = {SOLICITORACrudPlus9RolesLhlklnAccess.class, CLAIMANTSOLICITORDAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et3ResponseAddress")
    private Address et3ResponseAddress;
    // ET3 Response - Representative reference number (6)
    @CCD(
            label = "What is your reference number?",
            searchable = false,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("et3ResponseReference")
    private String et3ResponseReference;
    // ET3 Response - Hearing format page (9)
    @CCD(
            label = "Which types of hearing can you, as the representative, attend?",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et3_hearing_type",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseHearingRepresentative")
    private List<String> et3ResponseHearingRepresentative;
    @CCD(
            label = "Which types of hearing can the respondent attend?",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et3_hearing_type",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseHearingRespondent")
    private List<String> et3ResponseHearingRespondent;
    // ET3 Response - Respondent's workforce page (10)
    @CCD(
            label = "How many people does the respondent employ in Great Britain?",
            hint = "It can help the tribunal to have an indication of the employer's size.\r\n\r\nEnter a rough amount in digits or leave blank if you're not sure",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseEmploymentCount")
    private String et3ResponseEmploymentCount;
    @CCD(
            label = "Does the respondent have more than one site in Great Britain?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseMultipleSites")
    private String et3ResponseMultipleSites;
    @CCD(
            label = "How many people are employed at the place where the claimant worked?",
            hint = "Enter a rough amount in digits or leave blank if you're not sure",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseSiteEmploymentCount")
    private String et3ResponseSiteEmploymentCount;
    // ET3 Response - Acas page (11)
    @CCD(
            label = "Do you agree with the details given by the claimant about early conciliation with Acas?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseAcasAgree")
    private String et3ResponseAcasAgree;
    @CCD(
            label = "Why do you disagree with the Acas conciliation details given?",
            hint = "For example, you may consider that the claimant gave an incorrect Acas early conciliation number of that they were wrong to say they were exempt from early conciliation.",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseAcasAgreeReason")
    private String et3ResponseAcasAgreeReason;
    // ET3 Response - Are the employment dates correct page (12)
    @CCD(
            label = "Are the dates of employment given by the claimant correct?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseAreDatesCorrect")
    private String et3ResponseAreDatesCorrect;
    // ET3 Response - Employment dates page (13)
    @CCD(
            label = "Enter the employment start date",
            hint = "For example, 12 11 2020",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseEmploymentStartDate")
    private String et3ResponseEmploymentStartDate;
    @CCD(
            label = "Enter employment end date",
            hint = "For example, 12 11 2020",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseEmploymentEndDate")
    private String et3ResponseEmploymentEndDate;
    @CCD(
            label = "Do you want to provide any further information about the claimant's employment dates?",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseEmploymentInformation")
    private String et3ResponseEmploymentInformation;
    // ET3 Response - Is employment continuing page (14)
    @CCD(
            label = "Is the claimant's employment with the respondent continuing?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseContinuingEmployment")
    private String et3ResponseContinuingEmployment;
    // ET3 Response - Is claimant job title/description correct? (15)
    @CCD(
            label = "Is the claimant's description of their job or job title correct?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("et3ResponseIsJobTitleCorrect")
    private String et3ResponseIsJobTitleCorrect;
    @CCD(
            label = "What is or was the claimant's correct job title?",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseCorrectJobTitle")
    private String et3ResponseCorrectJobTitle;
    // ET3 Response - Claimant total weekly work hours (16)
    @CCD(
            label = "Are the claimant's total weekly work hours correct?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseClaimantWeeklyHours")
    private String et3ResponseClaimantWeeklyHours;
    @CCD(
            label = "What are the claimant's correct total weekly work hours?",
            hint = "Enter their hours work per week",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseClaimantCorrectHours")
    private String et3ResponseClaimantCorrectHours;
    // ET3 Response - Earning details (17)
    @CCD(
            label = "Are the earnings details given by the claimant correct?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseEarningDetailsCorrect")
    private String et3ResponseEarningDetailsCorrect;
    // ET3 Response - Correct pay details (18)
    @CCD(
            label = "How often was the claimant paid?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_et3_pay_frequency",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponsePayFrequency")
    private String et3ResponsePayFrequency;
    @CCD(
            label = "Enter the claimant's pay BEFORE tax",
            hint = "Include overtime, commission and bonuses.",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponsePayBeforeTax")
    private String et3ResponsePayBeforeTax;
    @CCD(
            label = "Enter the claimant's normal take-home pay",
            hint = "Take-home pay is the pay after tax and insurance deductions\r\n\r\nInclude overtime, commission and bonuses.",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponsePayTakehome")
    private String et3ResponsePayTakehome;
    // ET3 Response - Notice given (19)
    @CCD(
            label = "Is the information given by the claimant correct about their notice?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseIsNoticeCorrect")
    private String et3ResponseIsNoticeCorrect;
    @CCD(
            label = "What are the claimant's correct notice details?",
            hint = "For example, you may wish to clarify that the claimant was dismissed without notice or paid them a sum in lieu of notice.",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseCorrectNoticeDetails")
    private String et3ResponseCorrectNoticeDetails;
    // ET3 Response - pension details (20)
    @CCD(
            label = "Are the details about pension and other benefits correct?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_yes_no_not_applicable",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseIsPensionCorrect")
    private String et3ResponseIsPensionCorrect;
    @CCD(
            label = "What are the correct pension and benefit details?",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponsePensionCorrectDetails")
    private String et3ResponsePensionCorrectDetails;
    // ET3 Response - contest claim (21)
    @CCD(
            label = "Does the respondent contest the claim?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_contest_claim",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseRespondentContestClaim")
    private String et3ResponseRespondentContestClaim;
    // ET3 Response - explain contest claim (22)
    @CCD(
            label = "Upload a document to your response",
            searchable = false,
            max = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseContestClaimDocument")
    private List<DocumentTypeItem> et3ResponseContestClaimDocument;
    @CCD(
            label = "Use this text box for any accompanying information",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseContestClaimDetails")
    private String et3ResponseContestClaimDetails;
    // ET3 Response - employer claim (23)
    @CCD(
            label = "Does the respondent wish to make an Employer's Contract Claim?",
            hint = "A respondent can make this claim against the claimant if the claimant breached their employment terms which resulted in financial loss. This typically happens when a claimant has made a notice pay claim.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseEmployerClaim")
    private String et3ResponseEmployerClaim;
    // ET3 Response - explain employer claim (24)
    @CCD(
            label = "Provide the background and details of your Employer's Contract Claim",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseEmployerClaimDetails")
    private String et3ResponseEmployerClaimDetails;
    @CCD(
            label = "Add a document",
            hint = "Files should be a maximum of 100MB in size.",
            categoryID = "C2",
            searchable = false,
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseEmployerClaimDocument")
    private UploadedDocumentType et3ResponseEmployerClaimDocument;
    // ET3 Response - health conditions (25)
    @CCD(
            label = "In the respondent party - are you aware of any physical, mental or learning disability or health conditions which requires support?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_et3_yes_no_not_sure_yet",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseRespondentSupportNeeded")
    private String et3ResponseRespondentSupportNeeded;
    // ET3 Response - Details on health conditions (26)
    @CCD(
            label = "Use this text box or upload the requirements in a document",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseRespondentSupportDetails")
    private String et3ResponseRespondentSupportDetails;
    @CCD(
            label = "Add document",
            categoryID = "C2",
            searchable = false,
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3ResponseRespondentSupportDocument")
    private UploadedDocumentType et3ResponseRespondentSupportDocument;

    // ET3 Notification
    @CCD(
            label = "Upload document PDF",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Et3NotificationDocUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class, CaseworkerEmploymentRAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("et3NotificationDocCollection")
    private List<DocumentTypeItem> et3NotificationDocCollection;
    @CCD(
            label = "Serving document other type name placeholder",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("et3OtherTypeDocumentName")
    private String et3OtherTypeDocumentName;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_ServingDocumentRecipient",
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3NotificationDocRecipient")
    private List<String> et3NotificationDocRecipient;
    @CCD(
            label = "Deprecated",
            searchable = false,
            access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class, CaseworkerEmploymentEnglandwalesRAccess.class, CaseworkerEmploymentEtjudgeEnglandwalesRAccess.class}
    )
    @JsonProperty("et3ClaimantAndRespondentAddresses")
    private String et3ClaimantAndRespondentAddresses;
    @CCD(
            label = "Email link to Acas placeholder",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("et3EmailLinkToAcas")
    private String et3EmailLinkToAcas;

    //Referral
    @CCD(
            label = "Referrals",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "referralDetails",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCaseworkerWaTaskConfigurationRAccess.class}
    )
    @JsonProperty("referralCollection")
    private List<ReferralTypeItem> referralCollection;
    @CCD(
            label = "Referral Hearing Details placeholder",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("referralHearingDetails")
    private String referralHearingDetails;
    @CCD(
            label = "Select a referral",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("selectReferral")
    private DynamicFixedListType selectReferral;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("replyToReferralDcfLink")
    private String replyToReferralDcfLink;
    //Referral Type
    @CCD(
            label = "Who are you referring this case to?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorDPlus1RolesZwtixtAccess.class}
    )
    @JsonProperty("referCaseTo")
    private String referCaseTo;
    @CCD(
            label = "What is their email address?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("referentEmail")
    private String referentEmail;
    @CCD(
            label = "Is this urgent?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorDPlus1RolesZwtixtAccess.class}
    )
    @JsonProperty("isUrgent")
    private String isUrgent;
    @CCD(
            label = "What is the referral subject?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ReferralSubject",
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorDPlus1RolesZwtixtAccess.class}
    )
    @JsonProperty("referralSubject")
    private String referralSubject;
    @CCD(
            label = "Please specify",
            searchable = false,
            max = 30,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("referralSubjectSpecify")
    private String referralSubjectSpecify;
    @CCD(
            label = "Give details of the referral",
            hint = "Explain what you're referring and why, include answers or directions you need.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("referralDetails")
    private String referralDetails;
    @CCD(
            label = "Upload document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentCrudPlus2RolesWfyqbwAccess.class}
    )
    @JsonProperty("referralDocument")
    private List<DocumentTypeItem> referralDocument;
    @CCD(
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("referralInstruction")
    private String referralInstruction;
    @CCD(ignore = true)
    @JsonProperty("referredBy")
    private String referredBy;
    @CCD(ignore = true)
    @JsonProperty("referralDate")
    private String referralDate;

    //Referral Update
    @CCD(ignore = true)
    @JsonProperty("updateReferralNumber")
    private String updateReferralNumber;
    @CCD(
            label = "Who are you referring this case to?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class}
    )
    @JsonProperty("updateReferCaseTo")
    private String updateReferCaseTo;
    @CCD(
            label = "What is their email address?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class}
    )
    @JsonProperty("updateReferentEmail")
    private String updateReferentEmail;
    @CCD(
            label = "Is this urgent?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class}
    )
    @JsonProperty("updateIsUrgent")
    private String updateIsUrgent;
    @CCD(
            label = "What is the referral subject?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ReferralSubject",
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class}
    )
    @JsonProperty("updateReferralSubject")
    private String updateReferralSubject;
    @CCD(
            label = "Please specify",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class}
    )
    @JsonProperty("updateReferralSubjectSpecify")
    private String updateReferralSubjectSpecify;
    @CCD(
            label = "Give details of the referral",
            hint = "Explain what you're referring and why, include answers or directions you need.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class}
    )
    @JsonProperty("updateReferralDetails")
    private String updateReferralDetails;
    @CCD(
            label = "Upload document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class, CaseworkerEmploymentCrudAccess.class}
    )
    @JsonProperty("updateReferralDocument")
    private List<DocumentTypeItem> updateReferralDocument;
    @CCD(
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentCruCaseworkerEmploymentEtjudgeCruEtAcasApiRAccess.class}
    )
    @JsonProperty("updateReferralInstruction")
    private String updateReferralInstruction;

    //Referral Reply
    @CCD(
            label = "Hearing and Referral Details placeholder",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("hearingAndReferralDetails")
    private String hearingAndReferralDetails;
    @CCD(
            label = "Who are you sending these directions to?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("directionTo")
    private String directionTo;
    @CCD(
            label = "What is their email address?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("replyToEmailAddress")
    private String replyToEmailAddress;
    @CCD(
            label = "Is this urgent?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("isUrgentReply")
    private String isUrgentReply;
    @CCD(
            label = "What are your directions?",
            hint = "Give details of your reply to the referral and any issues you've identified",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("directionDetails")
    private String directionDetails;
    @CCD(
            label = "Upload document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class, CaseworkerEmploymentRAccess.class}
    )
    @JsonProperty("replyDocument")
    private List<DocumentTypeItem> replyDocument;
    @CCD(
            label = "General notes",
            hint = "Give details.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("replyGeneralNotes")
    private String replyGeneralNotes;
    @CCD(
            label = "Who are you referring or replying to?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("replyTo")
    private String replyTo;
    @CCD(
            label = "Give details of your reply or referral",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("replyDetails")
    private String replyDetails;
    @CCD(
            label = "For checking if user is a judge",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("isJudge")
    private String isJudge;

    //Close Referral
    @CCD(
            label = "Hearing and Referral Details placeholder",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("closeReferralHearingDetails")
    private String closeReferralHearingDetails;
    @CCD(
            label = "Do you want to close this referral?",
            hint = "All directions must be completed before closing referrals.",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_confirmCloseReferral",
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("confirmCloseReferral")
    private List<String> confirmCloseReferral;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesHozjvjAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class}
    )
    @JsonProperty("closeReferralGeneralNotes")
    private String closeReferralGeneralNotes;

    // Upload Documents Rejection
    @CCD(label = " ", typeOverride = FieldType.YesOrNo)
    @JsonProperty("caseRejectedEmailSent")
    private String caseRejectedEmailSent;

    // Respondent Organisation Policies
    @CCD(
            label = "respondent's legal representative",
            access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentOrganisationPolicy0")
    private OrganisationPolicy respondentOrganisationPolicy0;
    @CCD(
            label = "respondent's legal representative",
            access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentOrganisationPolicy1")
    private OrganisationPolicy respondentOrganisationPolicy1;
    @CCD(
            label = "respondent's legal representative",
            access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentOrganisationPolicy2")
    private OrganisationPolicy respondentOrganisationPolicy2;
    @CCD(
            label = "respondent's legal representative",
            access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentOrganisationPolicy3")
    private OrganisationPolicy respondentOrganisationPolicy3;
    @CCD(
            label = "respondent's legal representative",
            access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentOrganisationPolicy4")
    private OrganisationPolicy respondentOrganisationPolicy4;
    @CCD(
            label = "respondent's legal representative",
            access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentOrganisationPolicy5")
    private OrganisationPolicy respondentOrganisationPolicy5;
    @CCD(
            label = "respondent's legal representative",
            access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentOrganisationPolicy6")
    private OrganisationPolicy respondentOrganisationPolicy6;
    @CCD(
            label = "respondent's legal representative",
            access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentOrganisationPolicy7")
    private OrganisationPolicy respondentOrganisationPolicy7;
    @CCD(
            label = "respondent's legal representative",
            access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentOrganisationPolicy8")
    private OrganisationPolicy respondentOrganisationPolicy8;
    @CCD(
            label = "respondent's legal representative",
            access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentOrganisationPolicy9")
    private OrganisationPolicy respondentOrganisationPolicy9;
    @CCD(
            label = "Suggested hearing venue",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class}
    )
    @JsonProperty("suggestedHearingVenues")
    private DynamicFixedListType suggestedHearingVenues;
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class})
    @JsonProperty("listedDateInPastWarning")
    private String listedDateInPastWarning;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, DEFENDANTCrudAccess.class})
    @JsonProperty("noticeOfChangeAnswers0")
    private NoticeOfChangeAnswers noticeOfChangeAnswers0;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, DEFENDANTCrudAccess.class})
    @JsonProperty("noticeOfChangeAnswers1")
    private NoticeOfChangeAnswers noticeOfChangeAnswers1;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, DEFENDANTCrudAccess.class})
    @JsonProperty("noticeOfChangeAnswers2")
    private NoticeOfChangeAnswers noticeOfChangeAnswers2;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, DEFENDANTCrudAccess.class})
    @JsonProperty("noticeOfChangeAnswers3")
    private NoticeOfChangeAnswers noticeOfChangeAnswers3;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, DEFENDANTCrudAccess.class})
    @JsonProperty("noticeOfChangeAnswers4")
    private NoticeOfChangeAnswers noticeOfChangeAnswers4;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, DEFENDANTCrudAccess.class})
    @JsonProperty("noticeOfChangeAnswers5")
    private NoticeOfChangeAnswers noticeOfChangeAnswers5;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, DEFENDANTCrudAccess.class})
    @JsonProperty("noticeOfChangeAnswers6")
    private NoticeOfChangeAnswers noticeOfChangeAnswers6;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, DEFENDANTCrudAccess.class})
    @JsonProperty("noticeOfChangeAnswers7")
    private NoticeOfChangeAnswers noticeOfChangeAnswers7;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, DEFENDANTCrudAccess.class})
    @JsonProperty("noticeOfChangeAnswers8")
    private NoticeOfChangeAnswers noticeOfChangeAnswers8;
    @CCD(label = " ", access = {CaseworkerApproverCrudPlus4RolesMlxkxlAccess.class, DEFENDANTCrudAccess.class})
    @JsonProperty("noticeOfChangeAnswers9")
    private NoticeOfChangeAnswers noticeOfChangeAnswers9;
    @CCD(
            label = "Change Organisation Request",
            access = {CaseworkerApproverCruAccess.class, CaseworkerCaaCruAccess.class, CaseworkerEmploymentApiCruAccess.class}
    )
    @JsonProperty("changeOrganisationRequestField")
    private ChangeOrganisationRequest changeOrganisationRequestField;

    // Claimant TSE
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentApiCitizenCrudAccess.class})
    @JsonProperty("claimantTse")
    private ClaimantTse claimantTse;

    // Respondent TSE
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentApiCitizenCrudAccess.class})
    private RespondentTse respondentTse;

    //Respondent Tell Something Else
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesSiihenAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("resTseNotAvailableWarning")
    private String resTseNotAvailableWarning;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesSiihenAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("tseRespondNotAvailableWarning")
    private String tseRespondNotAvailableWarning;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesSiihenAccess.class, EtAcasApiRAccess.class}
    )
    @JsonProperty("respondToTribunalNotAvailableWarning")
    private String respondToTribunalNotAvailableWarning;
    @CCD(
            label = "Select an application",
            hint = "For multiple actions you can complete each one separately and explain in the text box that they are linked. Or use Contact the tribunal.",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_resTseSelectApp",
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseSelectApplication")
    private String resTseSelectApplication;
    @CCD(ignore = true)
    @JsonProperty("resTseVariableContent")
    private String resTseVariableContent;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument1")
    private UploadedDocumentType resTseDocument1;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox1")
    private String resTseTextBox1;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument2")
    private UploadedDocumentType resTseDocument2;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox2")
    private String resTseTextBox2;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument3")
    private UploadedDocumentType resTseDocument3;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox3")
    private String resTseTextBox3;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument4")
    private UploadedDocumentType resTseDocument4;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox4")
    private String resTseTextBox4;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument5")
    private UploadedDocumentType resTseDocument5;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox5")
    private String resTseTextBox5;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument6")
    private UploadedDocumentType resTseDocument6;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox6")
    private String resTseTextBox6;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument7")
    private UploadedDocumentType resTseDocument7;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox7")
    private String resTseTextBox7;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument8")
    private UploadedDocumentType resTseDocument8;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox8")
    private String resTseTextBox8;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument9")
    private UploadedDocumentType resTseDocument9;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox9")
    private String resTseTextBox9;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument10")
    private UploadedDocumentType resTseDocument10;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox10")
    private String resTseTextBox10;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument11")
    private UploadedDocumentType resTseDocument11;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox11")
    private String resTseTextBox11;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseDocument12")
    private UploadedDocumentType resTseDocument12;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseTextBox12")
    private String resTseTextBox12;
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class, CaseworkerEmploymentRAccess.class}
    )
    @JsonProperty("resTseCopyToOtherPartyYesOrNo")
    private String resTseCopyToOtherPartyYesOrNo;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("resTseCopyToOtherPartyTextArea")
    private String resTseCopyToOtherPartyTextArea;
    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "genericTseDetails",
            access = {CLAIMANTSOLICITORCudPlus15RolesEslhbeAccess.class}
    )
    @JsonProperty("genericTseApplicationCollection")
    private List<GenericTseApplicationTypeItem> genericTseApplicationCollection;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "genericTseDetails",
            access = {CaseworkerEmploymentApiCrudEtAcasApiRAccess.class, CREATORCrudAccess.class}
    )
    @JsonProperty("tseApplicationStoredCollection")
    private List<GenericTseApplicationTypeItem> tseApplicationStoredCollection;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "genericTseDetails",
            access = {DEFENDANTCrudAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("tseRespondentStoredCollection")
    private List<GenericTseApplicationTypeItem> tseRespondentStoredCollection;

    // Claimant tell something else
    @CCD(
            label = "Select an application",
            hint = "For multiple actions you can complete each one separately and explain in the text box that they are linked. Or use Contact the tribunal.",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_claimantTseSelectApp",
            access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("claimantTseSelectApplication")
    private String claimantTseSelectApplication;
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantTseCopyToOtherPartyYesOrNo",
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseRule92")
    private String claimantTseRule92;
    @CCD(label = " ", searchable = false, access = {CLAIMANTSOLICITORCruAccess.class})
    @JsonProperty("claimantTseRespNotAvailable")
    private String claimantTseRespNotAvailable;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument1")
    private UploadedDocumentType claimantTseDocument1;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox1")
    private String claimantTseTextBox1;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument2")
    private UploadedDocumentType claimantTseDocument2;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox2")
    private String claimantTseTextBox2;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument3")
    private UploadedDocumentType claimantTseDocument3;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox3")
    private String claimantTseTextBox3;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument4")
    private UploadedDocumentType claimantTseDocument4;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox4")
    private String claimantTseTextBox4;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument5")
    private UploadedDocumentType claimantTseDocument5;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox5")
    private String claimantTseTextBox5;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument6")
    private UploadedDocumentType claimantTseDocument6;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox6")
    private String claimantTseTextBox6;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument7")
    private UploadedDocumentType claimantTseDocument7;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox7")
    private String claimantTseTextBox7;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument8")
    private UploadedDocumentType claimantTseDocument8;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox8")
    private String claimantTseTextBox8;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument9")
    private UploadedDocumentType claimantTseDocument9;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox9")
    private String claimantTseTextBox9;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument10")
    private UploadedDocumentType claimantTseDocument10;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox10")
    private String claimantTseTextBox10;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument11")
    private UploadedDocumentType claimantTseDocument11;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox11")
    private String claimantTseTextBox11;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument12")
    private UploadedDocumentType claimantTseDocument12;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox12")
    private String claimantTseTextBox12;
    @CCD(
            label = "Document",
            categoryID = "C4",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseDocument13")
    private UploadedDocumentType claimantTseDocument13;
    @CCD(
            label = "Use this box for any accompanying information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseTextBox13")
    private String claimantTseTextBox13;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCruAccess.class}
    )
    @JsonProperty("claimantTseRule92AnsNoGiveDetails")
    private String claimantTseRule92AnsNoGiveDetails;
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentLegalrepSolicitorRAccess.class})
    @JsonProperty("claimantTseTableMarkUp")
    private String claimantTseTableMarkUp;

    //TSE Admin Record a Decision
    @CCD(
            label = "Select an application",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminSelectApplication")
    private DynamicFixedListType tseAdminSelectApplication;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminTableMarkUp")
    private String tseAdminTableMarkUp;
    @CCD(
            label = "Enter notification title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing.",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminEnterNotificationTitle")
    private String tseAdminEnterNotificationTitle;
    @CCD(
            label = "Decision",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminDecision",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminDecision")
    private String tseAdminDecision;
    @CCD(
            label = "Decision details",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminDecisionDetails")
    private String tseAdminDecisionDetails;
    @CCD(
            label = "Type of decision",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminTypeOfDecision",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminTypeOfDecision")
    private String tseAdminTypeOfDecision;
    @CCD(
            label = "Is a response to the tribunal required?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminIsResponseRequired",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminIsResponseRequired")
    private String tseAdminIsResponseRequired;
    @CCD(
            label = "Select the party or parties who must respond",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantRespondentBothParties",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminSelectPartyRespond")
    private String tseAdminSelectPartyRespond;
    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminAdditionalInformation")
    private String tseAdminAdditionalInformation;
    @CCD(
            label = "Supporting material",
            hint = "Upload a document to the system",
            searchable = false,
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminResponseRequiredYesDoc")
    private List<GenericTypeItem<DocumentType>> tseAdminResponseRequiredYesDoc;
    @CCD(
            label = "Supporting material",
            hint = "Upload a document to the system",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminResponseRequiredNoDoc")
    private List<GenericTypeItem<DocumentType>> tseAdminResponseRequiredNoDoc;
    @CCD(
            label = "Decision was made by",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminDecisionMadeBy",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminDecisionMadeBy")
    private String tseAdminDecisionMadeBy;
    @CCD(
            label = "Full name",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminDecisionMadeByFullName")
    private String tseAdminDecisionMadeByFullName;
    @CCD(
            label = "Select the party or parties to notify",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminSelectPartyNotify",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCruEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminSelectPartyNotify")
    private String tseAdminSelectPartyNotify;

    //TSE Response
    @CCD(
            label = "Select an application",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("tseRespondSelectApplication")
    private DynamicFixedListType tseRespondSelectApplication;
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class})
    @JsonProperty("tseResponseIntro")
    private String tseResponseIntro;
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class})
    @JsonProperty("tseResponseTable")
    private String tseResponseTable;
    @CCD(
            label = "What's your response to the application?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class, CaseworkerEmploymentRAccess.class}
    )
    @JsonProperty("tseResponseText")
    private String tseResponseText;
    @CCD(
            label = "Supporting material",
            hint = "Do you have any supporting material you want to provide?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("tseResponseHasSupportingMaterial")
    private String tseResponseHasSupportingMaterial;
    @CCD(
            label = "Upload a document",
            searchable = false,
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("tseResponseSupportingMaterial")
    private List<GenericTypeItem<DocumentType>> tseResponseSupportingMaterial;
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("tseResponseCopyToOtherParty")
    private String tseResponseCopyToOtherParty;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("tseResponseCopyNoGiveDetails")
    private String tseResponseCopyNoGiveDetails;
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class})
    @JsonProperty("resTseTableMarkUp")
    private String resTseTableMarkUp;
    //  if Respondent is responding to Tribunal
    @CCD(
            label = "Respondent is responding to Tribunal request/order",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class, CaseworkerEmploymentRAccess.class}
    )
    @JsonProperty("tseRespondingToTribunal")
    private String tseRespondingToTribunal;
    @CCD(
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class, CaseworkerEmploymentRAccess.class}
    )
    @JsonProperty("tseRespondingToTribunalText")
    private String tseRespondingToTribunalText;

    // Claimant Representative Response
    @CCD(
            label = "Select an application",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("claimantRepRespondSelectApplication")
    private DynamicFixedListType claimantRepRespondSelectApplication;
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class})
    @JsonProperty("claimantRepResponseIntro")
    private String claimantRepResponseIntro;
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class})
    @JsonProperty("claimantRepResponseTable")
    private String claimantRepResponseTable;
    @CCD(
            label = "What's your response to the application?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("claimantRepResponseText")
    private String claimantRepResponseText;
    @CCD(
            label = "Supporting material",
            hint = "Do you have any supporting material you want to provide?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("claimantRepResponseHasSupportingMaterial")
    private String claimantRepResponseHasSupportingMaterial;
    @CCD(
            label = "Upload a document",
            searchable = false,
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("claimantRepResSupportingMaterial")
    private List<GenericTypeItem<DocumentType>> claimantRepResSupportingMaterial;
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("claimantRepResponseCopyToOtherParty")
    private String claimantRepResponseCopyToOtherParty;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("claimantRepResponseCopyNoGiveDetails")
    private String claimantRepResponseCopyNoGiveDetails;
    @CCD(ignore = true)
    @JsonProperty("resClaimantRepTableMarkUp")
    private String resClaimantRepTableMarkUp;
    // if Claimant Representative is responding to Tribunal
    @CCD(
            label = "Claimant Representative is responding to Tribunal request/order",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("claimantRepResToTribunal")
    private String claimantRepRespondingToTribunal;
    @CCD(
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("claimantRepResToTribunalText")
    private String claimantRepRespondingToTribunalText;

    //TSE Admin Respond to an application
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("tseAdmReplyTableMarkUp")
    private String tseAdmReplyTableMarkUp;
    @CCD(
            label = "Enter response title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing.",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyEnterResponseTitle")
    private String tseAdmReplyEnterResponseTitle;
    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyAdditionalInformation")
    private String tseAdmReplyAdditionalInformation;
    @CCD(
            label = "Add document",
            hint = "Upload a document to the system",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
    )
    @JsonProperty("tseAdmReplyAddDocument")
    private List<GenericTypeItem<DocumentType>> tseAdmReplyAddDocument;
    @CCD(
            label = "Is this a case management order or request?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdmReplyIsCmoOrRequest",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyIsCmoOrRequest")
    private String tseAdmReplyIsCmoOrRequest;
    @CCD(
            label = "Case management order made by",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_tseAdmReplyCmoMadeBy",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyCmoMadeBy")
    private String tseAdmReplyCmoMadeBy;
    @CCD(
            label = "Request made by",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_tseAdmReplyRequestMadeBy",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyRequestMadeBy")
    private String tseAdmReplyRequestMadeBy;
    @CCD(
            label = "Enter their full name",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyCmoEnterFullName")
    private String tseAdmReplyCmoEnterFullName;
    @CCD(
            label = "Is a response to the tribunal required?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "frl_tseAdminIsResponseRequired",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyCmoIsResponseRequired")
    private String tseAdmReplyCmoIsResponseRequired;
    @CCD(
            label = "Enter their full name",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyRequestEnterFullName")
    private String tseAdmReplyRequestEnterFullName;
    @CCD(
            label = "Is a response to the tribunal required?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "frl_tseAdminIsResponseRequired",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyRequestIsResponseRequired")
    private String tseAdmReplyRequestIsResponseRequired;
    @CCD(
            label = "Select the party or parties who must respond",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantRespondentBothParties",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyRequestSelectPartyRespond")
    private String tseAdmReplyRequestSelectPartyRespond;
    @CCD(
            label = "Select the party or parties who must respond",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ClaimantRespondentBothParties",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplyCmoSelectPartyRespond")
    private String tseAdmReplyCmoSelectPartyRespond;
    @CCD(
            label = "Select the party or parties to notify",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseAdminSelectPartyNotify",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdmReplySelectPartyNotify")
    private String tseAdmReplySelectPartyNotify;

    // TSe Admin Close an application
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminCloseApplicationTable")
    private String tseAdminCloseApplicationTable;
    @CCD(
            label = "General notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
    )
    @JsonProperty("tseAdminCloseApplicationText")
    private String tseAdminCloseApplicationText;

    // Tell something else - view an application
    @CCD(
            label = "What application do you wish to view?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_tseApplicationsOpenOrClosed",
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("tseViewApplicationOpenOrClosed")
    private String tseViewApplicationOpenOrClosed;
    @CCD(
            label = "Select Application",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("tseViewApplicationSelect")
    private DynamicFixedListType tseViewApplicationSelect;
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class})
    @JsonProperty("tseApplicationSummaryAndResponsesMarkup")
    private String tseApplicationSummaryAndResponsesMarkup;

    // Provide Something Else to tribunal - Respondent - Respond to an order or request from the tribunal
    @CCD(
            label = "Select an order or request from the tribunal",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("pseRespondentSelectOrderOrRequest")
    private DynamicFixedListType pseRespondentSelectOrderOrRequest;
    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class})
    @JsonProperty("pseRespondentOrdReqTableMarkUp")
    private String pseRespondentOrdReqTableMarkUp;
    @CCD(
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer or you can upload material at the next step.",
            searchable = false,
            max = 2500,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("pseRespondentOrdReqResponseText")
    private String pseRespondentOrdReqResponseText;
    @CCD(
            label = "Supporting material",
            hint = "Do you have any supporting material you want to provide?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("pseRespondentOrdReqHasSupportingMaterial")
    private String pseRespondentOrdReqHasSupportingMaterial;
    @CCD(
            label = "Upload a document",
            searchable = false,
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("pseRespondentOrdReqUploadDocument")
    private List<GenericTypeItem<DocumentType>> pseRespondentOrdReqUploadDocument;
    @CCD(
            label = "Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_CopyToOtherPartyYesOrNo",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("pseRespondentOrdReqCopyToOtherParty")
    private String pseRespondentOrdReqCopyToOtherParty;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("pseRespondentOrdReqCopyNoGiveDetails")
    private String pseRespondentOrdReqCopyNoGiveDetails;

    // Provide Something Else to tribunal - Respondent - View a judgment, order or notification
    @CCD(
            label = "Select a judgment, order or notification",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("pseRespondentSelectJudgmentOrderNotification")
    private DynamicFixedListType pseRespondentSelectJudgmentOrderNotification;

    //sendNotification
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "sendNotificationCollection",
            access = {CaseworkerEmploymentApiCitizenCrudAccess.class, CaseworkerEmploymentEnglandwalesCuPlus1RolesDqbnwvAccess.class}
    )
    @JsonProperty("sendNotificationCollection")
    private List<SendNotificationTypeItem> sendNotificationCollection;
    @CCD(
            label = "Enter notification title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing. Please note the text entered here will be displayed in the notification e-mail sent to the parties.",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationTitle")
    private String sendNotificationTitle;
    @CCD(
            label = "Is there a letter to send out?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationLetter")
    private String sendNotificationLetter;
    @CCD(
            label = "Upload document",
            searchable = false,
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationUploadDocument")
    private List<DocumentTypeItem> sendNotificationUploadDocument;
    @CCD(
            label = "Notification subject",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_sendNotificationSubject",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationSubject")
    private List<String> sendNotificationSubject;
    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationAdditionalInfo")
    private String sendNotificationAdditionalInfo;
    @CCD(
            label = "Select the party or parties to notify",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationNotify",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationNotify")
    private String sendNotificationNotify;
    @CCD(ignore = true)
    @JsonProperty("sendNotificationNotifyLeadCase")
    private String sendNotificationNotifyLeadCase;
    @CCD(
            label = "Select the hearing",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationSelectHearing")
    private DynamicFixedListType sendNotificationSelectHearing;
    @CCD(
            label = "Is this a case management order or request?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationCaseManagement",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationCaseManagement")
    private String sendNotificationCaseManagement;
    @CCD(
            label = "Is a response to the tribunal required?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationResponseTribunal",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationResponseTribunal")
    private String sendNotificationResponseTribunal;
    @CCD(
            label = "Who made the case management order?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationWhoCaseOrder",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationWhoCaseOrder")
    private String sendNotificationWhoCaseOrder;
    @CCD(
            label = "Select the party or parties who must respond",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_sendNotificationParties",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationSelectParties")
    private String sendNotificationSelectParties;
    @CCD(label = "Full name", searchable = false, access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class})
    @JsonProperty("sendNotificationFullName")
    private String sendNotificationFullName;
    @CCD(label = "Full name", searchable = false, access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class})
    @JsonProperty("sendNotificationFullName2")
    private String sendNotificationFullName2;
    @CCD(label = "Details", searchable = false, access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class})
    @JsonProperty("sendNotificationDetails")
    private String sendNotificationDetails;
    @CCD(
            label = "Decision",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationDecision",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationDecision")
    private String sendNotificationDecision;
    @CCD(
            label = "Request was made by",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_sendNotificationRequestMadeBy",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationRequestMadeBy")
    private String sendNotificationRequestMadeBy;
    @CCD(
            label = "What is the ECC notification?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationEccQuestion",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationEccQuestion")
    private String sendNotificationEccQuestion;
    @CCD(
            label = "Who made the judgment?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationWhoMadeJudgement",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("sendNotificationWhoMadeJudgement")
    private String sendNotificationWhoMadeJudgement;
    @CCD(
            label = "Sent from multiple",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentApiCrudCaseworkerEmploymentEnglandwalesCruAccess.class}
    )
    @JsonProperty("notificationSentFrom")
    private String notificationSentFrom;

    @CCD(
            label = "Select a notification",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("selectNotificationDropdown")
    private DynamicFixedListType selectNotificationDropdown;

    @CCD(label = "Placeholder", searchable = false, access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class})
    @JsonProperty("notificationMarkdown")
    private String notificationMarkdown;

    @CCD(
            label = "Placeholder",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("pseViewNotifications")
    private String pseViewNotifications;

    @CCD(
            label = "Select which Respondent this ET3 Form is for",
            hint = "If you wish to select multiple respondents, please click the Add New button",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DynamicListCollection",
            access = {CaseworkerEmploymentApiCrudAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("et3RepresentingRespondent")
    private List<DynamicListTypeItem> et3RepresentingRespondent;

    @CCD(
            label = "Enter response title",
            hint = "Start with a verb if you need the parties to do something. For example: submit hearing agenda, view notice of hearing.",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("respondNotificationTitle")
    private String respondNotificationTitle;
    @CCD(
            label = "Additional information",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("respondNotificationAdditionalInfo")
    private String respondNotificationAdditionalInfo;
    @CCD(
            label = "Add document",
            hint = "Upload a document to the system",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("respondNotificationUploadDocument")
    private List<DocumentTypeItem> respondNotificationUploadDocument;
    @CCD(
            label = "Is this a case management order or request?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationCaseManagement",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("respondNotificationCmoOrRequest")
    private String respondNotificationCmoOrRequest;
    @CCD(
            label = "Is a response to the tribunal required",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_respondNotificationResponseRequired",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("respondNotificationResponseRequired")
    private String respondNotificationResponseRequired;
    @CCD(
            label = "Select the party or parties who must respond",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_sendNotificationParties",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("respondNotificationWhoRespond")
    private String respondNotificationWhoRespond;
    @CCD(
            label = "Case management order made by",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_respondNotificationCmoRequestBy",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("respondNotificationCaseManagementMadeBy")
    private String respondNotificationCaseManagementMadeBy;
    @CCD(
            label = "Request made by",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_respondNotificationRequestBy",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("respondNotificationRequestMadeBy")
    private String respondNotificationRequestMadeBy;
    @CCD(
            label = "Full name",
            hint = " ",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("respondNotificationFullName")
    private String respondNotificationFullName;
    @CCD(
            label = "Select the party or parties to notify",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationParties",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("respondNotificationPartyToNotify")
    private String respondNotificationPartyToNotify;

    // Bundles Respondent
    @CCD(
            label = "Placeholder",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("bundlesRespondentPrepareDocNotesShow")
    private String bundlesRespondentPrepareDocNotesShow;

    @CCD(
            label = "Have you agreed with the other party that this PDF set of documents will be used by both parties at the hearing and that no other documents will be referred to?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundlesRespondentAgreedDocWith",
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("bundlesRespondentAgreedDocWith")
    private String bundlesRespondentAgreedDocWith;
    @CCD(
            label = "Tell us which documents are disputed",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("bundlesRespondentAgreedDocWithBut")
    private String bundlesRespondentAgreedDocWithBut;
    @CCD(
            label = "Tell us why you’ve not been able to agree with the other party",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("bundlesRespondentAgreedDocWithNo")
    private String bundlesRespondentAgreedDocWithNo;

    @CCD(
            label = "Select the hearing these documents are for",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("bundlesRespondentSelectHearing")
    private DynamicFixedListType bundlesRespondentSelectHearing;

    @CCD(
            label = "Select which respondent this ET3 is for",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class, CitizenCrudAccess.class}
    )
    @JsonProperty("submitEt3Respondent")
    private DynamicFixedListType submitEt3Respondent;

    @CCD(
            label = "What are these documents?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundlesWhatDocuments",
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("bundlesRespondentWhatDocuments")
    private String bundlesRespondentWhatDocuments;

    @CCD(
            label = "Whose hearing documents are you uploading?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_bundlesWhoseDocuments",
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("bundlesRespondentWhoseDocuments")
    private String bundlesRespondentWhoseDocuments;

    @CCD(
            label = "Upload document",
            categoryID = "C57",
            searchable = false,
            typeOverride = FieldType.Document,
            typeParameterOverride = "DocumentUpload",
            access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
    )
    @JsonProperty("bundlesRespondentUploadFile")
    private UploadedDocumentType bundlesRespondentUploadFile;
    @CCD(
            label = "Respondent Hearing Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingBundle",
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, SOLICITORACuPlus9RolesXrnczvAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("bundlesRespondentCollection")
    private List<GenericTypeItem<HearingBundleType>> bundlesRespondentCollection;

    // Claimant Bundles
    @CCD(
            label = "Claimant Hearing Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingBundle",
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, SOLICITORACuPlus9RolesXrnczvAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("bundlesClaimantCollection")
    private List<GenericTypeItem<HearingBundleType>> bundlesClaimantCollection;

    // Remove Hearing Bundle
    @CCD(
            label = "Removed Hearing Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RemovedHearingBundle",
            access = {CaseworkerEmploymentEnglandwalesCuPlus1RolesDqbnwvAccess.class, CaseworkerEmploymentApiCuAccess.class}
    )
    @JsonProperty("removedHearingBundlesCollection")
    private List<GenericTypeItem<RemovedHearingBundleItem>> removedHearingBundlesCollection;

    @CCD(
            label = "Please select the hearing bundle to be removed",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class}
    )
    @JsonProperty("removeHearingBundleSelect")
    private DynamicFixedListType removeHearingBundleSelect;

    @CCD(
            label = "Please specify the party whose hearing bundles are to be removed",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_selectHearingBundlesCollection",
            access = {DefaultAccess.class}
    )
    @JsonProperty("removeBundleDropDownSelectedParty")
    private String removeBundleDropDownSelectedParty;

    @CCD(
            label = "Reason for removing the hearing bundle",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class}
    )
    @JsonProperty("hearingBundleRemoveReason")
    private String hearingBundleRemoveReason;

    @CCD(
            label = "Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AdrDocumentUploadDetails",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("adrDocumentCollection")
    private List<DocumentTypeItem> adrDocumentCollection;
    @CCD(
            label = "Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PiiDocumentUploadDetails",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("piiDocumentCollection")
    private List<DocumentTypeItem> piiDocumentCollection;
    @CCD(
            label = "Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "AppealDocumentUploadDetails",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("appealDocumentCollection")
    private List<DocumentTypeItem> appealDocumentCollection;

    // Case Flags
    @CCD(
            label = "Case Flags",
            typeOverride = FieldType.Flags,
            access = {CaseworkerEmploymentApiCrudCaseworkerEmploymentEnglandwalesCruAccess.class}
    )
    private CaseFlagsType caseFlags;
    @CCD(
            label = "Claimant Flags",
            typeOverride = FieldType.Flags,
            access = {CaseworkerEmploymentApiCrudCaseworkerEmploymentEnglandwalesCruAccess.class}
    )
    private CaseFlagsType claimantFlags;
    @CCD(
            label = "Respondent Flags",
            typeOverride = FieldType.Flags,
            access = {CaseworkerEmploymentApiCrudCaseworkerEmploymentEnglandwalesCruAccess.class}
    )
    private CaseFlagsType respondentFlags;

    //et-hearings-api
    @CCD(
            label = "hidden",
            typeOverride = FieldType.YesOrNo,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentRCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("autoListFlag")
    private String autoListFlag;

    @CCD(
            label = "text",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("caseAdditionalSecurityFlag")
    private String caseAdditionalSecurityFlag;
    @CCD(ignore = true)
    @JsonProperty("caseCategories")
    private List<CaseCategory> caseCategories;
    @CCD(label = "hidden", gate = "!CCD_DEF_ENV:prod", access = {CaseworkerEmploymentEnglandwalesCrudAccess.class})
    @JsonProperty("caseDeepLink")
    private String caseDeepLink;

    @CCD(
            label = "text",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("caseInterpreterRequiredFlag")
    private String caseInterpreterRequiredFlag;
    @CCD(
            label = "caseManagementLocationCode",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("caseManagementLocationCode")
    private String caseManagementLocationCode;
    @CCD(ignore = true)
    @JsonProperty("caseSLAStartDate")
    private String caseSLAStartDate;
    @CCD(
            label = "hidden",
            typeOverride = FieldType.YesOrNo,
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentRCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("caseRestrictedFlag")
    private String caseRestrictedFlag;

    @CCD(ignore = true)
    @JsonProperty("duration")
    private Integer duration;

    @CCD(ignore = true)
    @JsonProperty("externalCaseReference")
    private String externalCaseReference;

    @CCD(ignore = true)
    @JsonProperty("facilitiesRequiredList")
    private List<String> facilitiesRequiredList;

    @CCD(ignore = true)
    @JsonProperty("hearingChannels")
    private List<String> hearingChannels;
    @CCD(ignore = true)
    @JsonProperty("hearingInWelshFlag")
    private String hearingInWelshFlag;
    @CCD(label = "hidden", gate = "!CCD_DEF_ENV:prod", access = {CaseworkerEmploymentEnglandwalesCrudAccess.class})
    @JsonProperty("hearingIsLinkedFlag")
    private String hearingIsLinkedFlag;
    @CCD(ignore = true)
    @JsonProperty("hearingLocations")
    private List<HearingLocation> hearingLocations;
    @CCD(ignore = true)
    @JsonProperty("hearingPriorityType")
    private String hearingPriorityType;
    @CCD(ignore = true)
    @JsonProperty("hearingRequester")
    private String hearingRequester;
    @CCD(ignore = true)
    @JsonProperty("hearingType")
    private String hearingType;
    @CCD(ignore = true)
    @JsonProperty("hearingWindow")
    private HearingWindow hearingWindow;
    @CCD(
            label = " ",
            access = {GSProfileRAccess.class, CaseworkerEmploymentApiCudAccess.class, CaseworkerEmploymentEnglandwalesCudAccess.class}
    )
    @JsonProperty("caseNameHmctsInternal")
    private String caseNameHmctsInternal;
    @CCD(
            label = " ",
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentApiCrudCaseworkerWaTaskConfigurationRAccess.class, GSProfileRAccess.class}
    )
    @JsonProperty("caseManagementCategory")
    private DynamicFixedListType caseManagementCategory;
    @CCD(ignore = true)
    @JsonProperty("hmctsServiceID")
    private String hmctsServiceID;
    @CCD(
            label = " ",
            access = {GSProfileRAccess.class, CaseworkerRasValidationRAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    @JsonProperty("hmctsCaseCategory")
    private String hmctsCaseCategory;

    @CCD(ignore = true)
    @JsonProperty("judiciary")
    private Judiciary judiciary;
    @CCD(ignore = true)
    @JsonProperty("leadJudgeContractType")
    private String leadJudgeContractType;

    @CCD(ignore = true)
    @JsonProperty("listingComments")
    private String listingComments;

    @CCD(ignore = true)
    @JsonProperty("numberOfPhysicalAttendees")
    private Integer numberOfPhysicalAttendees;

    @CCD(ignore = true)
    @JsonProperty("panelRequirements")
    private PanelRequirements panelRequirements;

    @CCD(ignore = true)
    @JsonProperty("parties")
    private List<PartyDetails> parties;

    @CCD(
            label = "Judgment/Order to be issued",
            searchable = false,
            access = {CaseworkerEmploymentApiCrudCaseworkerWaTaskConfigurationRAccess.class, CaseworkerEmploymentEnglandwalesRAccess.class, CaseworkerEmploymentEtjudgeEnglandwalesCrudAccess.class}
    )
    @JsonProperty("draftAndSignJudgement")
    private DraftAndSignJudgement draftAndSignJudgement;

    @CCD(
            label = "text",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("privateHearingRequiredFlag")
    private String privateHearingRequiredFlag;

    @CCD(
            label = "Public case name",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("publicCaseName")
    private String publicCaseName;

    @CCD(ignore = true)
    @JsonProperty("screenFlow")
    private List<ScreenNavigation> screenFlow;

    @CCD(ignore = true)
    @JsonProperty("vocabulary")
    private List<Vocabulary> vocabulary;

    @CCD(
            label = " ",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseLink",
            access = {CaseworkerEmploymentEnglandwalesCrudAccess.class}
    )
    @JsonProperty("caseLinks")
    private ListTypeItem<CaseLink> caseLinks;

    @CCD(
            label = "Which parties do you have hearing unavailability dates for?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "frl_partyUnavailability",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("partySelection")
    private List<String> partySelection;

    @CCD(
            label = "Claimant unavailability date ranges",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UnavailabilityDateRange",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("claimantUnavailability")
    private ListTypeItem<UnavailabilityRanges> claimantUnavailability;

    @CCD(
            label = "Respondent unavailability date ranges",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "UnavailabilityDateRange",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
    )
    @JsonProperty("respondentUnavailability")
    private ListTypeItem<UnavailabilityRanges> respondentUnavailability;
    @CCD(
            label = "Please enter an ACAS Certificate number",
            hint = "For example R123456/12/34, MU123456/12/34",
            regex = "[a-zA-Z]{1,2}\\d{6}\\/\\d{2}\\/\\d{2}",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("acasCertificate")
    private String acasCertificate;

    @CCD(label = " ", access = {CaseworkerEmploymentApiCitizenCrudAccess.class, GSProfileRAccess.class})
    @JsonProperty("SearchCriteria")
    private SearchCriteria searchCriteria;

    @CCD(label = "text", typeOverride = FieldType.YesOrNo, access = {CaseworkerEmploymentApiCrudAccess.class})
    private String waRule21ReferralSent;

    @CCD(
            label = "Case stayed",
            gate = "!CCD_DEF_ENV:prod",
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("batchCaseStayed")
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

    @CCD(label = " ", searchable = false, access = {CaseworkerEmploymentApiCaseworkerEmploymentLegalrepSolicitorCrudAccess.class})
    @JsonProperty("et1ReppedTriageAddress")
    private Address et1ReppedTriageAddress;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentApiCaseworkerEmploymentLegalrepSolicitorCrudAccess.class}
    )
    @JsonProperty("et1ReppedTriageYesNo")
    private String et1ReppedTriageYesNo;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentApiCudAccess.class, CaseworkerEmploymentLegalrepSolicitorCudAccess.class}
    )
    @JsonProperty("et1ClaimStatuses")
    private String et1ClaimStatuses;

    @CCD(
            label = "ET1 Repped Section One",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentApiCaseworkerEmploymentLegalrepSolicitorCrudAccess.class}
    )
    @JsonProperty("et1ReppedSectionOne")
    private String et1ReppedSectionOne;
    @CCD(
            label = "ET1 Repped Section Two",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentApiCaseworkerEmploymentLegalrepSolicitorCrudAccess.class}
    )
    @JsonProperty("et1ReppedSectionTwo")
    private String et1ReppedSectionTwo;
    @CCD(
            label = "ET1 Repped Section Three",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentApiCaseworkerEmploymentLegalrepSolicitorCrudAccess.class}
    )
    @JsonProperty("et1ReppedSectionThree")
    private String et1ReppedSectionThree;

    @CCD(
            label = "Claimant's First Name",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantFirstName")
    private String claimantFirstName;
    @CCD(
            label = "Claimant's Last Name",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantLastName")
    private String claimantLastName;
    @CCD(
            label = "Claimant's Date of Birth",
            hint = "For example, 23 04 1981",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantDateOfBirth")
    private String claimantDateOfBirth;
    @CCD(
            label = "Select the claimant's sex",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_Sex",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantSex")
    private List<String> claimantSex;
    @CCD(
            label = "What is the claimant’s preferred title?",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantPreferredTitle")
    private String claimantPreferredTitle;
    @CCD(label = " ", searchable = false, access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class})
    @JsonProperty("claimantContactAddress")
    private Address claimantContactAddress;
    @CCD(
            label = "Which types of hearing can you, as the representative, attend?",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_HearingAttendence",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("representativeAttendHearing")
    private List<String> representativeAttendHearing;
    @CCD(
            label = "Which types of hearing can the claimant attend?",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_HearingAttendence",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantAttendHearing")
    private List<String> claimantAttendHearing;
    @CCD(
            label = " Are there any support requirements?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "frl_et3_yes_no_not_sure_yet",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantSupportQuestion")
    private List<String> claimantSupportQuestion;
    @CCD(
            label = "Give details of the support required",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantSupportQuestionReason")
    private String claimantSupportQuestionReason;
    @CCD(
            label = "How would you prefer to be contacted?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_ContactPreference",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("representativeContactPreference")
    private List<String> representativeContactPreference;
    @CCD(
            label = "Provide a reason why you have selected post",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("contactPreferencePostReason")
    private String contactPreferencePostReason;
    @CCD(
            label = "What is you contact phone number?",
            hint = "Where we can contact you during the day. For international numbers include the country code.",
            searchable = false,
            typeOverride = FieldType.PhoneUK,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("representativePhoneNumber")
    private String representativePhoneNumber;
    @CCD(
            label = "What is your representative reference number?",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("representativeReferenceNumber")
    private String representativeReferenceNumber;
    @CCD(
            label = "Contact address if different from registered address",
            searchable = false,
            access = {CLAIMANTSOLICITORCrudPlus11RolesKpylwqAccess.class}
    )
    @JsonProperty("representativeAddress")
    private Address representativeAddress;
    @CCD(
            label = "Did the claimant work for the respondent?",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("didClaimantWorkForOrg")
    private List<String> didClaimantWorkForOrg;
    @CCD(
            label = "Is the claimant still working for the respondent?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_StillWorking",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantStillWorking")
    private List<String> claimantStillWorking;
    @CCD(
            label = "Enter job title",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantJobTitle")
    private String claimantJobTitle;
    @CCD(
            label = "Enter employment start date",
            hint = "For example, 12 11 2020",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantStartDate")
    private String claimantStartDate;
    @CCD(
            label = "Enter employment end date",
            hint = "For example, 12 11 2020",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantEndDate")
    private String claimantEndDate;
    @CCD(
            label = "Is there a notice period?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_NoticePeriod",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantStillWorkingNoticePeriod")
    private List<String> claimantStillWorkingNoticePeriod;
    @CCD(
            label = "How many months is the notice period?",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantStillWorkingNoticePeriodMonths")
    private String claimantStillWorkingNoticePeriodMonths;
    @CCD(
            label = "How many weeks is the notice period?",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantStillWorkingNoticePeriodWeeks")
    private String claimantStillWorkingNoticePeriodWeeks;
    @CCD(
            label = "Notice period length",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_notice_period_unit",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantWorkingNoticePeriod")
    private List<String> claimantWorkingNoticePeriod;
    @CCD(
            label = "How many months of the notice period is the claimant being paid for?",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantWorkingNoticePeriodMonths")
    private String claimantWorkingNoticePeriodMonths;
    @CCD(
            label = "How many weeks of the notice period is the claimant being paid for?",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantWorkingNoticePeriodWeeks")
    private String claimantWorkingNoticePeriodWeeks;
    @CCD(
            label = "When does the notice period end?",
            hint = "For example, 23 04 1981",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantWorkingNoticePeriodEndDate")
    private String claimantWorkingNoticePeriodEndDate;
    @CCD(
            label = "Work or paid a notice period",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_WorkPayNoticePeriod",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNoLongerWorkingQuestion")
    private List<String> claimantNoLongerWorkingQuestion;
    @CCD(
            label = "Notice period length",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_NoticePeriodLength",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNoLongerWorking")
    private List<String> claimantNoLongerWorking;
    @CCD(
            label = "How many months was the notice period?",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNoLongerWorkingMonths")
    private String claimantNoLongerWorkingMonths;
    @CCD(
            label = "How many weeks was the notice period?",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNoLongerWorkingWeeks")
    private String claimantNoLongerWorkingWeeks;
    @CCD(ignore = true)
    @JsonProperty("claimantNoLongerWorkingPay")
    private String claimantNoLongerWorkingPay;
    @CCD(
            label = "Enter average weekly hours",
            searchable = false,
            min = 0,
            max = 168,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantAverageWeeklyWorkHours")
    private String claimantAverageWeeklyWorkHours;
    @CCD(
            label = "Enter the claimant's pay BEFORE tax and National Insurance",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantPayBeforeTax")
    private String claimantPayBeforeTax;
    @CCD(
            label = "Enter the claimant's pay AFTER tax and National Insurance",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantPayAfterTax")
    private String claimantPayAfterTax;
    @CCD(
            label = "Is this weekly, monthly or annual pay?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_PayFrequency",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantPayType")
    private List<String> claimantPayType;
    @CCD(
            label = "Did the respondent make contributions to the claimant's pension?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_pension_contribution",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantPensionContribution")
    private List<String> claimantPensionContribution;
    @CCD(
            label = "Enter their pension contributions - worked out weekly",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantWeeklyPension")
    private String claimantWeeklyPension;
    @CCD(
            label = "Did the claimant receive any employee benefits?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantEmployeeBenefits")
    private List<String> claimantEmployeeBenefits;
    @CCD(
            label = "Tell us about any benefits",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantBenefits")
    private String claimantBenefits;
    @CCD(
            label = "Has the claimant got a new job?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNewJob")
    private List<String> claimantNewJob;
    @CCD(
            label = "When did the claimant start?",
            hint = "If you do not know the exact date then enter the best estimate",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNewJobStartDate")
    private String claimantNewJobStartDate;
    @CCD(
            label = "Enter pay BEFORE tax and National Insurance",
            searchable = false,
            typeOverride = FieldType.MoneyGBP,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNewJobPayBeforeTax")
    private String claimantNewJobPayBeforeTax;
    @CCD(
            label = "Is this weekly, monthly or annual pay?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_pay_cycle",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNewJobPayPeriod")
    private List<String> claimantNewJobPayPeriod;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_respondentType",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("respondentType")
    private String respondentType;
    @CCD(
            label = "Enter the name of the organisation",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("respondentOrganisationName")
    private String respondentOrganisationName;
    @CCD(
            label = "Enter the first name of the individual",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("respondentFirstName")
    private String respondentFirstName;
    @CCD(
            label = "Enter the last name of the individual",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("respondentLastName")
    private String respondentLastName;
    @CCD(label = " ", searchable = false, access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class})
    @JsonProperty("respondentAddress")
    private Address respondentAddress;
    @CCD(label = " ", searchable = false, access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class})
    @JsonProperty("didClaimantWorkAtSameAddressPreamble")
    private String didClaimantWorkAtSameAddressPreamble;
    @CCD(
            label = "Did the claimant work at this address?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("didClaimantWorkAtSameAddress")
    private String didClaimantWorkAtSameAddress;
    @CCD(ignore = true)
    @JsonProperty("claimantWorkAddressYes")
    private Address claimantWorkAddressYes;
    @CCD(
            label = "Do you have an Acas certificate number for the respondent?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "msl_YesNo",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("respondentAcasYesNo")
    private String respondentAcasYesNo;
    @CCD(
            label = "Enter the Acas number",
            regex = "[a-zA-Z]{1,2}\\d{6}\\/\\d{2}\\/\\d{2}",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("respondentAcasNumber")
    private String respondentAcasNumber;
    @CCD(
            label = "Why is there no certificate number?",
            hint = "Incorrectly claiming an exemption may lead to the claim being rejected. If in doubt, please contact Acas.",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_noAcasReason",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("respondentNoAcasCertificateReason")
    private String respondentNoAcasCertificateReason;
    @CCD(label = " ", searchable = false, access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class})
    @JsonProperty("addAdditionalRespondentPreamble")
    private String addAdditionalRespondentPreamble;
    @CCD(
            label = "Do you want to add another respondent?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("addAdditionalRespondent")
    private String addAdditionalRespondent;
    @CCD(
            label = "Additional respondents",
            searchable = false,
            max = 5,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CreateRespondent",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1ReppedRespondentCollection")
    private List<GenericTypeItem<CreateRespondentType>> et1ReppedRespondentCollection;

    @CCD(
            label = "Enter details of the claim",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1SectionThreeClaimDetails")
    private String et1SectionThreeClaimDetails;
    @CCD(
            label = "Upload the details of the claim",
            searchable = false,
            typeOverride = FieldType.Document,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1SectionThreeDocumentUpload")
    private UploadedDocumentType et1SectionThreeDocumentUpload;
    @CCD(
            label = "What type of claim is this?",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et1TypesOfClaim",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1SectionThreeTypeOfClaim")
    private List<String> et1SectionThreeTypeOfClaim;
    @CCD(
            label = "What type of discrimination are you claiming?",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_et1DiscriminationClaims",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("discriminationTypesOfClaim")
    private List<String> discriminationTypesOfClaim;
    @CCD(
            label = "What type of pay claim are you making?",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_payClaims",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("payTypesOfClaim")
    private List<String> payTypesOfClaim;
    @CCD(
            label = "Do you want us to forward this claim to a relevant regulator or body?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("whistleblowingYesNo")
    private List<String> whistleblowingYesNo;
    @CCD(
            label = "Enter the name of the relevant regulator or body you want us to send this to",
            hint = "If you cannot find one, enter N/A",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("whistleblowingRegulator")
    private String whistleblowingRegulator;
    @CCD(
            label = "Enter the type of claim",
            searchable = false,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("otherTypeOfClaimDetails")
    private String otherTypeOfClaimDetails;
    @CCD(
            label = "What does the claimant want if their claim is successful?",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_claimOutcomes",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimSuccessful")
    private List<String> claimSuccessful;
    @CCD(
            label = "What compensation is the claimant seeking?",
            hint = "Set out all compensation the claimant is claiming for, and provide a total if possible.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("compensationDetails")
    private String compensationDetails;
    @CCD(
            label = "What tribunal recommendation would you like to make?",
            hint = "Tell us what action you’d like the tribunal to recommend the respondent makes to reduce the impact of any discrimination which has occurred.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("tribunalRecommendationDetails")
    private String tribunalRecommendationDetails;
    @CCD(
            label = "Are there any existing cases which may be linked to this new claim",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_YesNo",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("linkedCasesYesNo")
    private List<String> linkedCasesYesNo;
    @CCD(
            label = "Details of linked cases",
            hint = "Enter the case numbers and names of the people in the existing case or cases.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("linkedCasesDetails")
    private String linkedCasesDetails;
    @CCD(
            label = "Date completed",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1SectionOneDateCompleted")
    private String et1SectionOneDateCompleted;
    @CCD(
            label = "Date completed",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1SectionTwoDateCompleted")
    private String et1SectionTwoDateCompleted;
    @CCD(
            label = "Date completed",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("et1SectionThreeDateCompleted")
    private String et1SectionThreeDateCompleted;
    @CCD(
            label = "Claimants Representative",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class, CaseworkerCaaCrudAccess.class, CitizenCrudAccess.class}
    )
    @JsonProperty("claimantRepresentativeOrganisationPolicy")
    private OrganisationPolicy claimantRepresentativeOrganisationPolicy;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("downloadDraftEt1Date")
    private String downloadDraftEt1Date;
    @CCD(
            label = "If a hearing is required, what language do you, as the representative, want to speak at a hearing?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_languages",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("hearingContactLanguage")
    private List<String> hearingContactLanguage;
    @CCD(
            label = "If a hearing is required, what language does the claimant want to speak at a hearing?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_languages",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantHearingContactLanguage")
    private List<String> claimantHearingContactLanguage;
    @CCD(
            label = "What language do you want us to use when we contact you?",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_languages",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("contactLanguageQuestion")
    private List<String> contactLanguageQuestion;
    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("requiresSubmissionDocuments")
    private String requiresSubmissionDocuments;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentRCaseworkerEmploymentApiCrudAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
    )
    @JsonProperty("legalRepDocumentsMarkdown")
    private String legalRepDocumentsMarkdown;

    @CCD(
            label = "Select a judgment, order or notification",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantSelectNotification")
    private DynamicFixedListType claimantSelectNotification;
    @CCD(label = " ", searchable = false, access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class})
    @JsonProperty("claimantNotificationTableMarkdown")
    private String claimantNotificationTableMarkdown;
    @CCD(
            label = "What's your response to the tribunal?",
            hint = "Use this box to provide your answer to the tribunal.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNotificationResponseText")
    private String claimantNotificationResponseText;
    @CCD(
            label = "Do you have any supporting material?",
            hint = "Use this option if you have any documents or other material to support your response.",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNotificationSupportingMaterial")
    private String claimantNotificationSupportingMaterial;
    @CCD(
            label = "Upload a document",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNotificationDocuments")
    private List<GenericTypeItem<DocumentType>> claimantNotificationDocuments;
    @CCD(
            label = "Is the respondent a system user?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("isRespondentSystemUser")
    private String isRespondentSystemUser;
    @CCD(
            label = "Do you want to send a copy of this response to the other party?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantCopyToOtherPartyYesOrNo",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNotificationCopyToOtherParty")
    private String claimantNotificationCopyToOtherParty;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("claimantNotificationsCopyNoDetails")
    private String claimantNotificationsCopyNoDetails;
    @CCD(
            label = "Create, Upload or Remove DCF",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "createUploadOrRemove",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("uploadOrRemoveDcf")
    private String uploadOrRemoveDcf;

    // Migration fields ECM to Reform
    @CCD(label = " ", access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class})
    @JsonProperty("ecmCaseLink")
    private String ecmCaseLink;
    @CCD(label = "ECM Submission Reference", access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class})
    @JsonProperty("ecmFeeGroupReference")
    private String ecmFeeGroupReference;
    @CCD(
            label = "Migrated from ECM",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("migratedFromEcm")
    private String migratedFromEcm;

    // NOC fields - to find if any claimant representative has been removed.
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo, access = {CitizenCrudAccess.class})
    @JsonProperty("claimantRepresentativeRemoved")
    private String claimantRepresentativeRemoved;
    // new fields: et3RepresentativeContactChangeOption and claimantRepresentativeContactChangeOption
    // to determine whether the representative's contact information should be updated using the
    // MyHMCTS address or a manually entered address.
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_representativeContactChangeOptions",
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class, SOLICITORACrudPlus9RolesLhlklnAccess.class}
    )
    @JsonProperty("representativeContactChangeOption")
    private String representativeContactChangeOption;
    // Unable to remove respondent representative from repCollection as a respondent (remove legal representation),
    // Unable to update organisation policies and change role definitions for removed/added representatives
    // To resolve these problems added new field to identify which respondent representatives needs to be
    // removed or added.
    @CCD(
            label = "Respondent representative(s) to remove",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "RespondentRepresentative",
            access = {CaseworkerEmploymentApiCitizenCrudAccess.class}
    )
    @JsonProperty("repCollectionToRemove")
    private List<RepresentedTypeRItem> repCollectionToRemove;
    @JsonProperty("repCollectionToAdd")
    private List<RepresentedTypeRItem> repCollectionToAdd;

    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class, CREATORCrudAccess.class}
    )
    @JsonProperty("acasCertificateRequired")
    private String acasCertificateRequired;

    @CCD(
            label = "Select if the hearing is in the past or in the future",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_futureOrPastHearing",
            access = {DefaultAccess.class}
    )
    @JsonProperty("uploadHearingDocumentsSelectPastOrFutureHearing")
    private String uploadHearingDocumentsSelectPastOrFutureHearing;
    @CCD(
            label = "Select which hearing these documents are for",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class}
    )
    @JsonProperty("uploadHearingDocumentsSelectPastHearing")
    private DynamicFixedListType uploadHearingDocumentsSelectPastHearing;
    @CCD(
            label = "Select which hearing these documents are for",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class}
    )
    @JsonProperty("uploadHearingDocumentsSelectFutureHearing")
    private DynamicFixedListType uploadHearingDocumentsSelectFutureHearing;
    @CCD(
            label = "Upload Hearing Documents",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "HearingDocumentUpload",
            access = {DefaultAccess.class}
    )
    @JsonProperty("uploadHearingDocumentType")
    private List<GenericTypeItem<UploadHearingDocumentType>> uploadHearingDocumentType;
    @CCD(
            label = "Which party has submitted these documents?",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ClaimantOrRespondents",
            access = {DefaultAccess.class}
    )
    @JsonProperty("uploadHearingDocumentsWhoseDocuments")
    private String uploadHearingDocumentsWhoseDocuments;
    @CCD(
            label = "What date were these documents submitted?",
            searchable = false,
            typeOverride = FieldType.Date,
            access = {DefaultAccess.class}
    )
    @JsonProperty("uploadHearingDocumentsDateSubmitted")
    private String uploadHearingDocumentsDateSubmitted;
    @CCD(
            label = "Chosen address",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class, SOLICITORACrudPlus9RolesLhlklnAccess.class}
    )
    @JsonProperty("myHmctsAddressText")
    private String myHmctsAddressText;
  
    @CCD(
            label = "Telephone notes",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "caseNote",
            access = {CaseworkerEmploymentApiCudPlus2RolesPvynvpAccess.class}
    )
    @JsonProperty("caseNotesCollection")
    private List<GenericTypeItem<CaseNote>> caseNotesCollection;
    @CCD(
            label = "Telephone note",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("addCaseNote")
    private CaseNote addCaseNote;
    @CCD(
            label = "Edit or delete telephone note",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_editOrDelete",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("editOrDeleteCaseNote")
    private String editOrDeleteCaseNote;
    @CCD(
            label = "Telephone notes to edit or delete",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class}
    )
    @JsonProperty("caseNoteList")
    private DynamicFixedListType caseNoteList;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Please refer to any instructions or guidance given by the Tribunal as to what documents are needed for the hearing.<br><br>These documents, usually agreed by both parties, will form part of the evidence at the hearing. You should include all the documents that are important to your case, and that both parties want to refer to at the hearing.<br><br>The combined set of documents must be in one PDF file, usually submitted by one party unless instructed otherwise by the tribunal.<br> <br>Generally the documents should be included in the PDF file in date order, and the first page or pages of the pdf numbering should be an index showing the name of the document, its date and the page number.<br><br>The single PDF document should be created offline before uploading through this form.<br><br>You must submit hearing documents according to any time limits set out by the Tribunal.<br><br>Refer to the orders from the tribunal about whether you need to include your witness statements here or separately.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String bundlesRespondentPrepareDocNotes1;
  @CCD(
          label = "Providing a file which contains all relevant documents, and only relevant documents, will help ensure a fair hearing and minimise any delays.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String bundlesRespondentPrepareDocNotes2;
  @CCD(
          label = "Once you’ve read the orders from the tribunal, make sure:<br><ul><li>you have the set of documents in one PDF file</li><li>the documents are in date order or some other logical order and comply with any directions given by the Tribunal</li><li>the index item refers to the correct page in the document</li><li>you only include relevant documents and material for your case</li><li>the PDF document falls within any page limits given by the tribunal</li></ul>If you’re referencing one section of a document like a contract of employment, say which part in the index.<br><br>If you need to refer to previous case documents, go to your <a href=\"/cases/case-details/${[CASE_REFERENCE]}#Documents\" target=\"_blank\">case documents (opens in new tab)</a>.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorRAccess.class}
  )
  private String bundlesRespondentUploadFileLabel;
  @CCD(
          label = "### Hearing Documents",
          typeOverride = FieldType.Label,
          access = {SOLICITORACruPlus13RolesJweikaAccess.class}
  )
  private String bundlesTabTitle;
  @CCD(
          label = "What are these hearing documents?",
          searchable = false,
          typeOverride = FieldType.FixedRadioList,
          typeParameterOverride = "frl_bundleType"
  )
  private String uploadHearingDocumentsWhatAreDocuments;
  @CCD(label = "Please specify", searchable = false)
  private String uploadHearingDocumentsWhatAreDocumentsOther;
  @CCD(
          label = "Component Launcher (for displaying Case View categories)",
          searchable = false,
          typeOverride = FieldType.ComponentLauncher,
          access = {CaseworkerEmploymentApiCudAccess.class, CaseworkerEmploymentEnglandwalesCuAccess.class}
  )
  private String componentLauncher;
  @CCD(
          label = "Flag Launcher",
          typeOverride = FieldType.FlagLauncher,
          access = {CaseworkerEmploymentApiCrudCaseworkerEmploymentEnglandwalesCruAccess.class}
  )
  private String flagLauncher;
  @JsonProperty("LinkedCasesComponentLauncher")
  @CCD(
          label = "Component Launcher (for displaying Linked Cases data)",
          typeOverride = FieldType.ComponentLauncher,
          access = {CaseworkerEmploymentEnglandwalesCruAccess.class}
  )
  private String linkedCasesComponentLauncher;
  @CCD(
          label = "Do you want to automatically generate the DCF?",
          searchable = false,
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo dcfYesNo;
  @CCD(
          label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/createDcf/createDcf1\">Create, Upload or Remove DCF</a>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class}
  )
  private String createDigitalCaseFileLink;
  @CCD(
          label = "If the claimant works or worked at home occasionally or full time, enter the postcode of where they travel into work or where they would have travelled to.\r\n\r\nIf they're claiming against a respondent that they've not worked for - as best as you can, enter the postcode of where the respondent is based\r\n\r\n<a href=\"https://www.claim-employment-tribunals.service.gov.uk\" target=\"_blank\">Complete this ET1 claim form if the respondent's work location is outside the UK.</a>\n\n<a href=\"cases/case-create/EMPLOYMENT/ET_Scotland/et1ReppedCreateCase/et1ReppedCreateCase1\" target=\"_blank\">Complete this ET1 claim form if the the respondent's work location is in Scotland.</a>\n\n </div>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCaseworkerEmploymentLegalrepSolicitorCrudAccess.class}
  )
  private String et1ReppedTriageLabel;
  @CCD(
          label = "The postcode you entered is not included under the early adopter sites yet.\r\n\r\nUntil your submitted location is supported, complete this <a href=\"https://www.claim-employment-tribunals.service.gov.uk\" target=\"_blank\">ET1 claim form</a>.\r\n\r\n<div class=\"govuk-inset-text\">\r\n  This service is only available to solicitors representing single claimants, for a limited number of claims within our early adopter sites who meet the current reform criteria.\r\n</div>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCaseworkerEmploymentLegalrepSolicitorCrudAccess.class}
  )
  private String et1ReppedTriageError;
  @CCD(
          label = "${et1ClaimStatuses}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCudAccess.class, CaseworkerEmploymentLegalrepSolicitorCudAccess.class}
  )
  private String et1ClaimStatusesLabel;
  @CCD(
          label = "In this section, you’ll need to provide:\r\n<ul>\r\n<li>your contact details (as the representative)</li>\r\n<li>the claimant or claimants’ contact details</li>\r\n<li>Acas details</li>\r\n<li>any support requirements for the party</li>\r\n<li>hearing format preferences</li>\r\n</ul>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String et1Section1PreambleLabel;
  @CCD(
          label = "The parties can express their preference of phone, video or in person hearings. Requests have to be agreed by a judge and it can depend on the type of hearing.",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String hearingFormatPreamble;
  @CCD(
          label = "In most circumstances the tribunal will communicate with you (as the representative) by email. If you need communication by post, please tell us why.",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String representativeInformationPreamble;
  @CCD(
          label = "In this section, you’ll need to provide:\r\n<ul>\r\n<li>employment status</li>\r\n<li>respondent details</li>\r\n</ul>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String et1Section2PreambleLabel;
  @CCD(
          label = "If the number of hours the claimant worked changed each week (for example, they had a zero-hours contract), use the final 12 weeks of payslips to work out their weekly average.\r\n\r\nDo not include overtime hours.",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String claimantAverageWeeklyWorkHoursPreamble;
  @CCD(
          label = "For unfair dismissal and some other claims, the tribunal will need the average of the claimant’s last 12 weeks’ pay. Provide your best estimate for the average weekly pay.",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String claimantPayPreamble;
  @CCD(
          label = "Tell us about the claimant's gross pay before tax and other deductions",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String claimantNewJobPayPreamble;
  @CCD(
          label = "Enter the name of the organisation. If there’s no organisation involved - for example, because the claimant was employed by an individual acting as a sole trader - enter the individual’s name. \r\n\r\nThis should be the same as the name of the respondent on the Acas certificate.\r\n\r\nYou will be able to add more respondents later.\r\n",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String respondentNamePreamble;
  @CCD(
          label = "You can add more respondents later if you need to",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String respondentEndLabel;
  @CCD(
          label = "${didClaimantWorkAtSameAddressPreamble}",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String didClaimantWorkAtSameAddressPreambleLabel;
  @CCD(
          label = "${addAdditionalRespondentPreamble}",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String addAdditionalRespondentPreambleLabel;
  @CCD(
          label = "You’ll need to provide details of the claim. This can be uploaded in a document.",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String et1SectionThreePreamble;
  @CCD(
          label = "Upload the claim details in a document or use the text box.\r\nYou can use both if you need to.",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String et1SectionThreeDetailsPreamble;
  @CCD(
          label = "Tell us if there are any existing cases this claim could be linked to.\n\nThis could be: \n\n* a case or cases you have already brought\n* a case or cases brought by other people against the same employer with the same or similar circumstances\n\nThis will help the tribunal consider whether the cases should be linked in any way.",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String linkedCasesPreamble;
  @CCD(
          label = "Do you want to submit this ET1 claim?",
          searchable = false,
          typeOverride = FieldType.MultiSelectList,
          typeParameterOverride = "msl_submitEt1",
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String submitEt1Confirmation;
  @CCD(
          label = "If you need to check your answers you can access individual sections from the claim overview. You can then edit your answers within the section.\n\nYou will not be able to make any further changes to this form once submitted.",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String submitEt1Preamble;
  @CCD(
          label = "Click submit to download a draft copy of the ET1 form completed so far",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String et1DoNotSubmitDraftMessage;
  @CCD(
          label = "${claimantNotificationTableMarkdown}",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String claimantNotificationTableMarkdownLabel;
  @CCD(
          label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantViewAllNotifications/claimantViewAllNotifications1\">All judgments, orders and notifications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantViewNotification/claimantViewNotification1\">View a judgment, order or notification</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantRespondToNotification/claimantRespondToNotification1\">Respond to an order or request from the tribunal</a>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCudAccess.class, CaseworkerEmploymentApiCudAccess.class}
  )
  private String claimantJonsLinks;
  @CCD(
          label = "Consider when providing material that:<br><ul><li>you can upload letters, photos or documents to support your application</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String claimantNotificationSupportingMaterialLabel;
  @CCD(
          label = "<h3>To copy this correspondence to the other party, you must send it to them by post or email. They cannot view it in this service.</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCaseworkerEmploymentApiCrudAccess.class}
  )
  private String claimantRule92TextWhenRespOffline;
  @CCD(
          label = "ECM Case: ${ecmCaseLink}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class}
  )
  private String ecmCaseLinkLabel;
  @CCD(
          label = "# Unavailability Dates",
          typeOverride = FieldType.Label,
          gate = "!CCD_DEF_ENV:prod",
          access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class}
  )
  private String unavailabilityTabTitle;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICAnyOtherDirectionsDividerHrLabel;
  @CCD(
          label = "${etInitialConsiderationHearing}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
  )
  private String etICFurtherInfoHearingLabel;
  @CCD(
          label = "${etInitialConsiderationJurisdictionCodes}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
  )
  private String etICFurtherInfoJurisdictionCodesLabel;
  @CCD(
          label = "${etInitialConsiderationRespondent}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
  )
  private String etICFurtherInfoRespondentLabel;
  @CCD(
          label = "<h3>Any other directions (Optional)</h3><p>Are there any other issues or instructions to consider, or further orders to give?</p><p>This could include:</p><ul><li>Rule 49</li><li>Interpreters</li><li>Adjustments required for hearings</li><li>Further information required</li><li>Employer’s Contract Claim</li><li>Respondent’s identity</li><li>Time limits: claim or response</li></ul>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICFurtherInformationHearingAnyOtherDirectionsLabel;
  @CCD(
          label = "${etInitialConsiderationHearing}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
  )
  private String etICHearingListedHearingLabel;
  @CCD(
          label = "${etInitialConsiderationJurisdictionCodes}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
  )
  private String etICHearingListedJurisdictionCodesLabel;
  @CCD(
          label = "${etInitialConsiderationRespondent}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
  )
  private String etICHearingListedRespondentLabel;
  @CCD(
          label = "<h3>Any other directions (Optional)</h3><p>Are there any other issues or instructions to consider, or further orders to give?</p><p>This could include:</p><ul><li>Rule 49</li><li>Interpreters</li><li>Adjustments required for hearings</li><li>Further information required</li><li>Employer’s Contract Claim</li><li>Respondent’s identity</li><li>Time limits: claim or response</li></ul>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class}
  )
  private String etICHearingNotListedAnyOtherDirectionsLabel;
  @CCD(
          label = "${etInitialConsiderationHearing}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
  )
  private String etICHearingNotListedHearingLabel;
  @CCD(
          label = "${etInitialConsiderationJurisdictionCodes}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
  )
  private String etICHearingNotListedJurCodesLabel;
  @CCD(
          label = "<hr>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICHearingNotListedListUpdatedDividerHrLabel;
  @CCD(
          label = "${etInitialConsiderationRespondent}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class}
  )
  private String etICHearingNotListedRespondentLabel;
  @CCD(
          label = "<br><hr><h1>Issues identified at ET1 vetting and ET3 processing</h1>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICIssuesArisingFromVettingLabel;
  @CCD(
          label = "<hr><h2>ET1 Vetting Issues</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICMinimumInfoFromVettingLabel;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICNavigationButtonsDividerHrLabel;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICUploadDocDividerHrLabel;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICUploadDocDividerHrLabel3;
  @CCD(
          label = "${etIcHearingPanelPreference}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String etIcHearingPanelPreferenceLabel;
  @CCD(
          label = "${etIcPartiesHearingFormat}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String etIcPartiesHearingFormatLabel;
  @CCD(
          label = "${etIcPartiesHearingPanelPreferenceHeader}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String etIcPartiesHearingPanelPreferenceHeaderLabel;
  @CCD(
          label = "${etIcPartiesHearingPanelPreference}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String etIcPartiesHearingPanelPreferenceLabel;
  @CCD(
          label = "${etInitialConsiderationHearing}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String etInitialConsiderationHearingLabel;
  @CCD(
          label = "${etInitialConsiderationJurisdictionCodes}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String etInitialConsiderationJurisdictionCodesLabel;
  @CCD(
          label = "${etInitialConsiderationRespondent}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String etInitialConsiderationRespondentLabel;
  @CCD(
          label = "${icEt1VettingIssuesDetail}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String icEt1VettingIssuesDetailLabel;
  @CCD(
          label = "<hr>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String icEt3ProcessingIssuesBottomDividerHrLabel;
  @CCD(
          label = "<hr><h2>ET3 Processing Issues</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String icEt3ProcessingIssuesDetailDividerHrLabel;
  @CCD(
          label = "${icEt3ProcessingIssuesDetail}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String icEt3ProcessingIssuesDetailLabel;
  @CCD(
          label = "${icRespondentHearingPanelPreference}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String icRespondentHearingPanelPreferenceLabel;
  @CCD(
          label = "${initialConsiderationBeforeYouStart}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String initialConsiderationBeforeYouStartLabel;
  @CCD(
          label = "${etIcPartiesHearingFormat}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String listedHearingPartiesHearingFormatLabel;
  @CCD(
          label = "${etIcPartiesHearingPanelPreference}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String listedHearingPartiesPanelPreferenceLabel;
  @CCD(
          label = "${etIcPartiesHearingFormat}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String notListedHearingPartiesHearingFormatLabel;
  @CCD(
          label = "${etIcPartiesHearingPanelPreference}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class}
  )
  private String notListedHearingPartiesPanelPreferenceLabel;
  @CCD(label = "${initialConsiderationBeforeYouStart}", typeOverride = FieldType.Label, access = {DefaultAccess.class})
  private String initialConsiderationBeforeYouStartLabel2;
  @CCD(label = "${initialConsiderationBeforeYouStart}", typeOverride = FieldType.Label, access = {DefaultAccess.class})
  private String initialConsiderationBeforeYouStartLabel3;
  @CCD(
          label = "${initialConsiderationBeforeYouStart}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class}
  )
  private String initialConsiderationBeforeYouStartLabel4;
  @CCD(
          label = "<h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
          typeOverride = FieldType.Label,
          gate = "!CCD_DEF_ENV:prod",
          access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CitizenCuAccess.class}
  )
  private String multipleReferenceLinkLabel;
  @CCD(
          label = "<h1>LEAD CLAIM</h1><p>This case is assigned as the lead case in a group claim<p><h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
          typeOverride = FieldType.Label,
          gate = "!CCD_DEF_ENV:prod"
  )
  private String multipleLeadClaim;
  @CCD(
          label = "<h1>STAYED CASE</h1><p>This case is stayed and linked to a group claim<p><h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
          typeOverride = FieldType.Label,
          gate = "!CCD_DEF_ENV:prod"
  )
  private String multipleStayedCase;
  @CCD(
          label = "<h1></h1><p>This case is linked to a group claim<p><h3> View the group claim. Multiple reference : ${multipleReferenceLinkMarkUp} </h3>",
          typeOverride = FieldType.Label,
          gate = "!CCD_DEF_ENV:prod"
  )
  private String multipleNotStayedCase;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICAnyOtherDirectionsDividerHrLabel3;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICNavigationButtonsDividerHrLabel3;
  @CCD(label = "${etInitialConsiderationRespondent}", typeOverride = FieldType.Label)
  private String etICRespondentHearingListed;
  @CCD(
          label = "${etInitialConsiderationHearing}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICHearingHearingListed;
  @CCD(
          label = "${etInitialConsiderationJurisdictionCodes}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICJurisdictionCodesHearingListed;
  @CCD(
          label = "<hr><h2>ET1 Vetting Issues</h2>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String icEt1VettingIssuesDividerHrLabel;
  @CCD(
          label = "<hr>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String icEt1VettingIssuesBottomDividerHrLabel;
  @CCD(label = "${et3IsThereAnEt3Response}", searchable = false, typeOverride = FieldType.Label)
  private String icEt3IsThereAnEt3ResponseLabel;
  @CCD(
          label = "${etInitialConsiderationHearing}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICHearingFurtherInfo;
  @CCD(label = "${etInitialConsiderationJurisdictionCodes}", typeOverride = FieldType.Label)
  private String etICJurisdictionCodesFurtherInfo;
  @CCD(
          label = "${etInitialConsiderationRespondent}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class}
  )
  private String etICRespondentFurtherInfo;
  @CCD(
          label = "### Initial Consideration",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class}
  )
  private String initialConsiderationTabTitle;
  @CCD(
          label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/draftAndSignJudgement/draftAndSignJudgement1\">Draft and sign judgment/order</a>",
          typeOverride = FieldType.Label
  )
  private String draftAndSignJudgementLink;
  @CCD(label = "PCQ ID", searchable = false, access = {CaseworkerEtPcqextractorRAccess.class})
  private String pcqId;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorRAccess.class, CaseworkerEmploymentRAccess.class}
  )
  private String horizontalLine;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class, CaseworkerEmploymentLegalrepSolicitorRAccess.class}
  )
  private String horizontalLine2;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class, CaseworkerEmploymentLegalrepSolicitorRAccess.class}
  )
  private String horizontalLine3;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorRAccess.class, CaseworkerEmploymentRAccess.class}
  )
  private String horizontalLine4;
  @CCD(label = "History", searchable = false, access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class})
  private String caseHistory;
  @CCD(
          label = "state",
          searchable = false,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
  )
  private String state;
  @CCD(
          label = "<h2>${flagsImageAltText}</h2>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudAccess.class}
  )
  private String flagsImagePlaceHolder;
  @CCD(
          label = "#### Case Status:  ${[STATE]} ",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CitizenCuAccess.class}
  )
  private String caseStateDesc;
  @CCD(
          label = "Case Transfer: ${linkedCaseCT} ${transferredCaseLink}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CitizenCuAccess.class}
  )
  private String linkedCaseCTLabel;
  @CCD(
          label = "${et1VettingBeforeYouStart}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String et1VettingBeforeYouStartLabel;
  @CCD(
          label = "<hr><h2>Contact Details</h2>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class, CaseworkerEmploymentApiRAccess.class}
  )
  private String et1VettingContactDetailsLabel;
  @CCD(
          label = "${et1VettingClaimantDetailsMarkUp}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String et1VettingClaimantDetailsLabel;
  @CCD(
          label = "${et1VettingRespondentDetailsMarkUp}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String et1VettingRespondentDetailsLabel;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCrudPlus2RolesWfyqbwAccess.class}
  )
  private String et1REJOrVPReferralLine1;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCrudPlus2RolesWfyqbwAccess.class}
  )
  private String et1REJOrVPReferralLine2;
  @CCD(
          label = "Select all that apply.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String selectAllThatApply;
  @CCD(
          label = "${otherTypeDocumentName}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String otherTypeDocumentNameLabel;
  @CCD(
          label = "<hr><h2>Print and send paper documents<h2>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String printAndSendPaperDocuments;
  @CCD(
          label = "Send documents by first class mail to:",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String sendDocByFirstClass;
  @CCD(
          label = "${claimantAndRespondentAddresses}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String claimantAndRespondentAddressesLabel;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String emailDocsToAcasLine;
  @CCD(
          label = "<h2>Email documents to Acas</h2>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String emailDocsToAcasTitle;
  @CCD(
          label = "Attach and send document PDFs to Acas at [et3@acas.org.uk](${emailLinkToAcas})",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String emailDocsToAcasLink;
  @CCD(
          label = "Instructions for the content of the email are on the 'Sending general correspondence to Acas via email' job card.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String emailDocsToAcasInstructions;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCrudAccess.class}
  )
  private String et1JudgeReferralLine1;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCrudPlus2RolesWfyqbwAccess.class}
  )
  private String et1JudgeReferralLine2;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCrudPlus2RolesWfyqbwAccess.class}
  )
  private String et1OtherReferralLine1;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentCrudPlus2RolesWfyqbwAccess.class}
  )
  private String et1OtherReferralLine2;
  @CCD(
          label = "${existingJurisdictionCodes}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String existingJurisdictionCodesLabel;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "fl_jurisdictionCodes",
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et1VettingJurCodeList;
  @CCD(
          label = "${trackAllocation}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String trackAllocationLabel;
  @CCD(
          label = "${tribunalAndOfficeLocation}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String tribunalAndOfficeLocationLabel;
  @CCD(
          label = "${regionalOffice}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String regionalOfficeLabel;
  @CCD(
          label = "${et1AddressDetails}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String et1AddressDetailsLabel;
  @CCD(
          label = "<h3>${et1TribunalRegion} hearing venues</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String et1TribunalRegionLabel;
  @CCD(
          label = "To help you complete this, open the ET1 form and additional documents the claimant or respondent may have uploaded in the <a href=\"/cases/case-details/${[CASE_REFERENCE]}#Documents\" target=\"_blank\"> Documents tab (opens in a new tab)</a>.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class, EtAcasApiRAccess.class}
  )
  private String et3VettingBeforeYouStart;
  @CCD(
          label = "${et3Date}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3DateLabel;
  @CCD(
          label = "${et3Date}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3DateCompanyHousePage;
  @CCD(
          label = "${et3Date}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3ResponseInTimeDateLabel;
  @CCD(
          label = "${et3Date}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3DateIndividualInsolvency;
  @CCD(
          label = "<hr><h2>Contact details</h2><hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3ContactDetailsLabel;
  @CCD(
          label = "${et3NameAddressRespondent}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3NameAddressRespondentLabel;
  @CCD(
          label = "<hr><h2>Contact details</h2><hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3ContactDetailsNameMismatchLabel;
  @CCD(
          label = "${et3NameAddressRespondent}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3NameAddressRespondentNameMismatchLabel;
  @CCD(
          label = "<hr><h2>Contact details</h2><hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3ContactDetailsAddressLabel;
  @CCD(
          label = "${et3NameAddressRespondent}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3NameAddressRespondentAddressLabel;
  @CCD(
          label = "<hr><h2>Contact details</h2><hr>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3ContactDetailsAddressMismatchLabel;
  @CCD(
          label = "${et3NameAddressRespondent}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesYovydhAccess.class, EtAcasApiRAccess.class}
  )
  private String et3NameAddressRespondentAddressMismatchLabel;
  @CCD(
          label = "${et3HearingDetails}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
  )
  private String et3HearingDetailsLabel;
  @CCD(
          label = "${et3TribunalLocation}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
  )
  private String et3TribunalLocationLabel;
  @CCD(
          label = "${et1VettingClaimantDetailsMarkUp}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String et1VettingClaimantDetailsLabel2;
  @CCD(
          label = "${et1VettingRespondentAcasDetails1}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesCuxzijAccess.class}
  )
  private String et1VettingRespondentAcasDetailsLabel1;
  @CCD(
          label = "${et1VettingRespondentAcasDetails2}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesCuxzijAccess.class}
  )
  private String et1VettingRespondentAcasDetailsLabel2;
  @CCD(
          label = "${et1VettingRespondentAcasDetails3}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesCuxzijAccess.class}
  )
  private String et1VettingRespondentAcasDetailsLabel3;
  @CCD(
          label = "${et1VettingRespondentAcasDetails5}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesCuxzijAccess.class}
  )
  private String et1VettingRespondentAcasDetailsLabel5;
  @CCD(
          label = "${et1VettingRespondentAcasDetails6}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRPlus2RolesCuxzijAccess.class}
  )
  private String et1VettingRespondentAcasDetailsLabel6;
  @CCD(
          label = "Select all that apply.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class, EtAcasApiRAccess.class}
  )
  private String et3SelectAllThatApply;
  @CCD(
          label = "${et3OtherTypeDocumentName}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class, EtAcasApiRAccess.class}
  )
  private String et3OtherTypeDocumentNameLabel;
  @CCD(label = "Task list check")
  private TaskListCheck et3ClaimantTaskListChecks;
  @CCD(
          label = "<hr>\r\n<h2>Email documents to Acas</h2>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class, EtAcasApiRAccess.class}
  )
  private String et3EmailDocsToAcasTitle;
  @CCD(
          label = "Attach and send document PDFs to Acas at [ET3@acas.org.uk](${et3EmailLinkToAcas})",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class, EtAcasApiRAccess.class}
  )
  private String et3EmailDocsToAcasLink;
  @CCD(
          label = "Instructions for the content of the email are on the 'Sending general correspondence to Acas via email' job card.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class, EtAcasApiRAccess.class}
  )
  private String et3EmailDocsToAcasInstructions;
  @CCD(
          label = "To help you complete this, open the ET1 form, ACAS certificate and other documents in the <strong>Case Documents</strong> event in the next steps menu.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3StartPagePreamble;
  @CCD(
          label = "This response must be completed and submitted within 28 days of the date of the claim form being sent by the tribunal.\r\n\r\nYou must provide your response to the claim even if you believe that another respondent is liable.\r\n\r\nIf you do not provide a response, a judgment may be issued against you without a hearing. If you consider another person or company maybe liable, you should still submit a response. You will have a chance in this response to explain why you think someone else may be liable.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3StartPageInset;
  @CCD(
          label = "It will help the tribunal and you as the representative if you have: \r\n\r\n* the claimant's employment start and end dates, hours of work and notice period\r\n* details of claimant's pay and benefits, before and after tax\r\n* your response to the claim, which you can upload in document format with a text field for accompanying information\r\n\r\n### How to fill in this form\r\n\r\n* read the questions carefully to make sure you're providing your details (as the representative) or the respondent's details\r\n* optional questions will be marked as such\r\n* you can review and edit your answers before you submit the form",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3StartPageMainBody;
  @CCD(
          label = "${et3ResponseClaimantName}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3ResponseClaimantNameLabel;
  @CCD(
          label = "This will be the respondent recorded in ET1 that you're representing.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3ResponseNamePreamble;
  @CCD(
          label = "If you consider another person or company may be liable, you should still submit a response.\r\n\r\nYou will have a chance in this form to explain why you think someone else may be liable.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3ResponseNameInset;
  @CCD(
          label = "In most circumstances the tribunal will communicate with you (as the representative) by email. If you need communication by post, please tell us why.",
          typeOverride = FieldType.Label
  )
  private String et3RepresentativeInfoFirstWords;
  @CCD(
          label = "The parties can express their preference of in-person, video or phone hearings.\r\n\r\nRequests have to be agreed by a judge and it can depend on the type of hearing. If the case goes to a final hearing, this will normally be in-person but parties will be informed in advance.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3ResponseHearingPreamble;
  @CCD(
          label = "If pay details are provided on the ET1 form, check whether the pay is weekly, monthly or annual. Use the same time period here.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3ResponsePayDetailsPreamble;
  @CCD(
          label = "Consider in your response:\r\n\r\n* setting out the aspects of the claim you agree or disagree with, including if you're contesting only part of the claim\r\n* making sure that any facts or events are in date order\r\n* whether you think another person or company may be liable and why\r\n\r\nYou can upload a statement of case below.\r\n\r\nFile should be a maximum of 100MB in size. If there is a need of uploading several documents, they should be made into one and uploaded as one document.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3ResponseContestClaimPreamble;
  @CCD(
          label = "<h3>Consider in your answer:</h3>\r\n\r\n* the background and details of your claim\r\n* all important dates\r\n*the value of your claim and how you calculated it",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3ResponseEmployerClaimDetailsPreamble;
  @CCD(
          label = "<details class=\"govuk-details\"> <summary class=\"govuk-details__summary\"><span class=\"govuk-details__summary-text\"> What support is available? </span></summary> <div class=\"govuk-details__text\"> We know people with disabilities sometimes need support to access information and use our services. We call this a reasonable adjustment. Some reasonable adjustments need to be agreed by a judge, and you can discuss with the tribunal if your needs change.<br><br>Reasonable adjustments can include:<br><br>\r\n\r\n* documents in alternative formats, colours and fonts\r\n* help with communicating, sight, hearing, speaking and interpretation\r\n* access and mobility support if a hearing takes place in person </div> </details>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3ResponseHealthInsetPreamble;
  @CCD(
          label = "<h3>Consider in your answer:</h3>\r\n\r\nTell us what this disability or condition is and what support that anyone in the respondent party, including representative and witnesses would need as the claim progresses through the system. Consider any hearings that may take place at tribunal buildings.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String et3ResponseHealthDetailsPreamble;
  @CCD(
          label = "Click submit to download a draft copy of the ET3 form completed so far for the respondent selected.\r\n\r\nThis will not submit the ET3 to the tribunal. If you wish to submit the ET3, please use the Submit ET3 event",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCruAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String downloadDraftEt3Label;
  @CCD(
          label = "Do you want to submit this ET3?",
          hint = "If you wish to submit the ET3 to the tribunal, please select the option below and continue. You will not be able to make any further changes once submitted.",
          searchable = false,
          typeOverride = FieldType.MultiSelectList,
          typeParameterOverride = "msl_confirmSubmitEt3",
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String confirmEt3Submit;
  @CCD(label = "**Referrals**", typeOverride = FieldType.Label)
  private String referralsLabel;
  @CCD(
          label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/createReferral/createReferral1\">Send a new referral</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/updateReferral/updateReferral1\">Update a referral</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/replyToReferral/replyToReferral1\">Reply to a referral</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/closeReferral/closeReferral1\">Close a referral</a>",
          typeOverride = FieldType.Label
  )
  private String referralLinks;
  @CCD(
          label = "${referralHearingDetails}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String referralHearingDetailsLabel;
  @CCD(
          label = "Which instructions do you recommend?",
          hint = "Give details of directions, letter or decisions to issue.",
          searchable = false,
          typeOverride = FieldType.TextArea
  )
  private String updateReferralHearingDate;
  @CCD(
          label = "${hearingAndReferralDetails}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus2RolesYovydhAccess.class, CaseworkerEmploymentApiRPlus2RolesXjmnfoAccess.class}
  )
  private String hearingAndReferralDetailsLabel;
  @CCD(
          label = "${closeReferralHearingDetails}",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeCruAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class, CaseworkerEmploymentRAccess.class}
  )
  private String closeReferralHearingDetailsLabel;
  @CCD(
          label = "<h3>One of the listed dates are in the past. If you want to change it please click Previous and enter a date after today otherwise click Continue.</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
  )
  private String listedDateInPastWarningLabel;
  @CCD(
          label = "<hr>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseHorizontalLine;
  @CCD(
          label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exceptional circumstances.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseCopyThisCorrespondenceText;
  @CCD(
          label = "#### Applications",
          typeOverride = FieldType.Label,
          access = {SOLICITORACuPlus9RolesXrnczvAccess.class, CLAIMANTSOLICITORCudAccess.class, CaseworkerEmploymentApiCudAccess.class}
  )
  private String applicationsTab;
  @CCD(
          label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/respondentTseAllApplications/respondentTseAllApplications1\">All applications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/viewRespondentTSEApplications/viewRespondentTSEApplications1\">View an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/respondentTSE/respondentTSE1\">Make an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseRespond/tseRespond1\">Respond to an application</a>",
          typeOverride = FieldType.Label,
          access = {SOLICITORACuPlus9RolesXrnczvAccess.class, CLAIMANTSOLICITORDAccess.class, CaseworkerEmploymentApiCudAccess.class}
  )
  private String applicationsLinks;
  @CCD(
          label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmin/tseAdmin1\">Record a decision</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmReply/tseAdmReply1\">Respond to an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdminCloseAnApplication/tseAdminCloseAnApplication1\">Close application</a>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class}
  )
  private String applicationsAdminLinks;
  @CCD(
          label = " <hr>Use this form to apply to amend the ET3 response.<br><br>The tribunal will decide if you can make the amendment by judging the fairness of the application. This decision may be made at a hearing.<br><br>The tribunal also needs to know if you consider the amendment minor or substantial.<br><br>Providing details of why you want to amend the response and the importance of the amendment will help the tribunal to decide your application more quickly.<h3>Details to include in your application:</h3><ul><li>what you want to amend in your response. Be specific and refer to your ET3 response form if possible</li><li>if you consider it a minor or substantial amendment</li><li>why you want to make this amendment</li><li>why you are asking to make this amendment now</li><li>how this amendment will benefit you and how could it disadvantage you if not granted</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel1;
  @CCD(
          label = "<hr>Use this form to apply to change details like the company address, email or telephone number. <br><br>If you change the postal or email address, we’ll send any letters to the new address.<br><br>If you change the telephone number, we’ll contact you using the new number if we have questions about your response.<h3>Details you can apply to change:</h3><ul><li>name</li><li>sex and preferred title</li><li>address</li><li>telephone number</li><li>email address</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel2;
  @CCD(
          label = "<hr>Use this form to tell us that the claimant has not complied with all or part of an order from the tribunal.<br><br>You should try to resolve your complaint with the claimant. Only use this form if that is not possible.<h3>Details to include in your application:</h3><ul><li>which order has not been complied with</li><li>the date the tribunal issued the order</li><li>what the claimant has not done</li><li>what you want the tribunal to do next</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel3;
  @CCD(
          label = "<hr>Use this form to have a judgment or order made by a legal officer considered afresh by an Employment Judge.<br><br>Considered afresh means that if you disagree with a judgment or order made by a legal officer in this case, you can ask an Employment Judge to look at that decision again.<br><br>The judge will take the decision afresh on the basis of the same information as was before the legal officer, unless either side chooses to supply additional information.<h3>Details to include in your application:</h3><ul><li>the decision you want considered afresh</li><li>the date the tribunal issued the decision</li><li>why you want the decision considered afresh</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel4;
  @CCD(
          label = "<hr>Tell or ask the tribunal about something relevant to this case.<h3>Do not use this form to:</h3><ul><li>seek legal advice from the tribunal. The tribunal is an independent judicial body and cannot give legal advice</li><li>tell us about settlement offers or discussions, which are private and confidential to the parties</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel5;
  @CCD(
          label = "<hr>Use this form to ask the tribunal to order the claimant to do or provide something.<h3>Details to include in your application:</h3><ul><li>what you want the claimant to do</li><li>why it is relevant to your response</li><li>if you have already asked the claimant to do or provide this thing. If you have, tell us when you asked and what their response was</li><li>if you have not asked the claimant to do or provide this thing yet, explain why</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel6;
  @CCD(
          label = "<hr>You can ask the tribunal to order a witness to attend to give evidence.<br><br>The witness must be in Great Britain (England, Scotland and Wales). This does not include Northern Ireland.<br><br>The tribunal can limit the number of witnesses to be called to give evidence on a particular issue, especially if that issue is not central to the case.<br><br>The respondent may also be liable for the costs incurred by the witness’s attendance.<br><br>You should consider whether the evidence of this witness is likely to help your case.<h3>Details to include in your application:</h3><ul><li>the witness’s full name and address</li><li>why the attendance of this witness is necessary for a fair hearing. The tribunal needs to understand why the evidence is relevant, and why there is no alternative way of establishing the same points at the hearing without ordering a witness to attend</li><li>if you have asked this witness to attend voluntarily. If you have, tell us when you asked and what their response was</li><li>if you have not asked the witness to attend yet, explain why</li></ul>If the order is granted, the witness will be ordered to attend on the first day of the hearing. If you want them to attend on another day or days, tell us: <br><br><ul><li>the dates apart from the first day of the hearing that you want the witness to attend</li><li>why their attendance is necessary on those dates</li></ul>If you want the witness to bring documents, tell us: <br/><br/><ul><li>why these documents are relevant to the issues in this case</li><li>why an order to disclose the documents would not be enough</li><li>if you have already asked the witness or anyone else to provide these documents. If you have, tell us when you asked and what their response was</li><li>if you have not asked the witness to provide the documents yet, explain why</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel7;
  @CCD(
          label = "<hr>Use this form to ask the tribunal to postpone a hearing to a later date. <h3>Details to include in your application:</h3><ul><li>which hearing you want to postpone. If the hearing you want to postpone is listed over multiple days, tell us which day or days you are applying to postpone</li><li>the reason you want to postpone. The tribunal will use this to decide whether to grant your application</li><li>any essential documents to support your application</li><li>weekday dates within the next 3 months that you, your witnesses or representatives cannot attend a rescheduled hearing</li><li>the reason you cannot attend on those dates</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel8;
  @CCD(
          label = "<hr>Use this form to have a judgment made by a tribunal reconsidered. This may be a judgment made by a judge alone or by a judge sitting with non-legal members.<br><br>If reconsideration is necessary in the interests of justice you can ask the tribunal to look at the judgment again.<br><br>Only a judgment which finally determines an issue in your case can be reconsidered. This therefore excludes any decision that does not comprise a final determination of the claim, or part of a claim. If you wish to ask for a case management order to be varied or revoked, use the vary or revoke an order application.<br><br>You can only ask the tribunal to reconsider a judgment within 14 days of the date the judgment was sent to you.<br><br>If you want the tribunal to reconsider a judgment that was sent to you over 14 days ago you must explain why your application is late.<br><br>A reconsideration application does not affect the time limit for appealing to the Employment Appeal Tribunal.<h3>Details to include in your application:</h3><ul><li>the judgment you want reconsidered </li><li>the date the tribunal issued the judgment</li><li>your reason for a late application if the judgment was sent over 14 days ago</li><li>why it is in the interests of justice to reconsider this judgment</li><li>if the tribunal should vary or revoke the judgment</li><li>any additional information or material which the tribunal does not already have to support your application</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel9;
  @CCD(
          label = "<hr>Use this form to apply to prevent or restrict publicity in this case.<br><br>It is an important principle that justice should normally be delivered in public.<br><br>However, the tribunal has the power to prevent or restrict the public disclosure of any aspect of this case if necessary in the interests of justice or to protect the Convention rights of any person.<br><br>The tribunal also has certain other powers to sit in private or to restrict publicity in accordance with sections 10A, 10B, 11 and 12 of the Employment Tribunals Act 1996.<br><br>The tribunal may issue an order:<ul><li>that a hearing that would otherwise be in public be conducted, in whole or in part, in private</li><li>that the identities of specified parties, witnesses or other persons referred to in the proceedings should not be disclosed to the public, by the use of anonymisation or otherwise</li><li>for measures preventing witnesses at a public hearing being identifiable by members of the public</li><li>restricting the reporting of the case in the media</li></ul><h3>Details to include in your application:</h3><ul><li>how the tribunal should prevent or restrict publicity in this case</li><li>why the restrictions you want are needed. The tribunal must make the least restrictive order possible. Tell us why fewer restrictions would not be enough</li><li>any media interest in this case. Include names of media organisations or journalists that are interested. The tribunal might seek their comments before making a decision</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel10;
  @CCD(
          label = "<hr>You can request that the tribunal strike out all or parts of the claim.<br><br>If something is ‘struck out’ it is removed from the claim or response and cannot be relied upon.<br><br>The tribunal can strike out all or parts of the claimant’s claim on their own initiative or after a request from the respondent.<br><br>A strike out request must be based on at least one of the grounds of Rule 38 of the Employment Tribunals Rules of Procedure.<h3>Details to include in your application:</h3><ul><li>why you think the claim (or parts of it) should be struck out</li><li>which ground or grounds in Rule 38 you say applies in this case</li><li>if you are referring to numbered points or paragraphs in a claim, include these numbers or other references</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel11;
  @CCD(
          label = "<hr>Use this form to apply to vary or revoke an order the tribunal has issued.<br><br>Tribunal orders will not usually be varied or revoked unless there has been a material change in circumstances since the order was made, which make the variation in the interests of justice.<br><br>The tribunal will consider your reasons for varying or revoking the order and any supporting materials you provide then make a decision.<h3>Details to include in your application:</h3><ul><li>the order you want to vary or revoke </li><li>the date the tribunal issued the order</li><li>explain which part of the order you want to vary or revoke</li><li>how to vary the order</li><li>why it is in the interests of justice to vary or revoke this order. Tell us any relevant changes of circumstance since the order was made and consider the requirements of the ‘overriding objective’ in Rule 3 of the Employment Tribunal Rules of Procedure</li></ul><h3>Consider when providing material that:</h3><ul><li>you can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul><h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseGuidanceLabel12;
  @CCD(
          label = "All applications <hr>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseAllApplicationsLabel;
  @CCD(
          label = "${resTseTableMarkUp}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String resTseTableLabel;
  @CCD(
          label = "${tseAdminTableMarkUp}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiRAccess.class, EtAcasApiRAccess.class}
  )
  private String tseAdminTableLabel;
  @CCD(
          label = "${tseResponseIntro}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String tseResponseIntroLabel;
  @CCD(
          label = "${tseResponseTable}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String tseResponseTableLabel;
  @CCD(
          label = "### Consider in your answer:\r\n + If you agree or disagree with the application \r\n + that if you disagree with the application, you must give reasons \r\n + that you can apply for a hearing instead of responding in writing",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudEtAcasApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorCrudAccess.class}
  )
  private String tseResponseConsider;
  @CCD(
          label = "Consider when providing material that:\r\n + you can upload letters, photos or documents to support your application\r\n + if you are taking a picture of a letter, place it on a flat surface and take the picture from above\r\n + If you are uploading written documents with tracked changes, make sure that tracked changes are turned on\r\n",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String tseResponseConsiderMaterial;
  @CCD(
          label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exception circumstances.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String tseRespondCopyPartyIntro;
  @CCD(
          label = "${tseAdmReplyTableMarkUp}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
  )
  private String tseAdmReplyTableLabel;
  @CCD(
          label = "${tseAdminCloseApplicationTable}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
  )
  private String tseAdminCloseApplicationTableLabel;
  @CCD(
          label = "Do you want to close this application?",
          searchable = false,
          typeOverride = FieldType.MultiSelectList,
          typeParameterOverride = "msl_closeApplicationYes",
          access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudEtAcasApiRAccess.class}
  )
  private String tseAdminCloseApplicationYes;
  @CCD(
          label = "${tseApplicationSummaryAndResponsesMarkup}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCrudPlus2RolesKlompmAccess.class}
  )
  private String tseApplicationSummaryAndResponsesMarkupLabel;
  @CCD(
          label = "Use this service to notify one or both parties about this case. You can do this by uploading standard letter documents.\r\n\r\n You can send multiple letters in one notification\r\n\r\n Do not use this service to notify parties about:\r\n * ET1 serving - use the <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/uploadDocumentForServing/uploadDocumentForServing1 \">ET1 serving service</a>\r\n * ET3 notification - use the <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/et3Notification/et3Notification1 \">ET3 notification service</a>\r\n * Responding to an application - use the <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmin/tseAdmin1\">Record a decision</a> or <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseAdmReply/tseAdmReply1\">Respond to an application service</a>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
  )
  private String sendNotificationInfo;
  @CCD(
          label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exception circumstances.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String pseRespondentOrdReqCopyPartyIntro;
  @CCD(
          label = "Consider when providing material that:<br><ul><li>you can upload letters, photos or documents to support your application</li><li>if you are taking a picture of a letter, place it on a flat surface and take the picture from above</li><li>if you are uploading written documents with tracked changes, make sure that tracked changes are turned on</li></ul>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String pseRespondentOrdReqPage2TableLabel;
  @CCD(
          label = "${pseRespondentOrdReqTableMarkUp}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiRAccess.class, CaseworkerEmploymentLegalrepSolicitorRAccess.class, EtAcasApiRAccess.class}
  )
  private String pseRespondentRequestOrderTableLabel;
  @CCD(
          label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/viewAllNotifications/viewAllNotifications1\">All judgments, orders and notifications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/pseRespondentViewNotification/pseRespondentViewNotification1\">View a judgment, order or notification</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/pseRespondentRespondToTribunal/pseRespondentRespondToTribunal1\">Respond to an order or request from the tribunal</a>",
          typeOverride = FieldType.Label
  )
  private String jonsLinks;
  @CCD(label = "#### Judgments, orders & notifications", typeOverride = FieldType.Label)
  private String jonsTab;
  @CCD(
          label = "${pseViewNotifications}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String pseViewNotificationsLabel;
  @CCD(
          label = "${notificationMarkdown}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class}
  )
  private String notificationMarkdownLabel;
  @CCD(
          label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/sendNotification/sendNotification1\">Send a notification</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/respondNotification/respondNotification1\">Respond to a notification</a><br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/generateNotificationSummary/generateNotificationSummary1\">Generate Notification Summary</a>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class}
  )
  private String notificationsTabLinks;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CitizenCruAccess.class}
  )
  private String respondNotificationDate;
  @CCD(
          label = "<h3>This function is not available for this case, please click cancel to return to the main page, you will need to submit your application outside the portal via email or post.</h3>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesSiihenAccess.class}
  )
  private String resTseNotAvailableWarningLabel;
  @CCD(
          label = "<h3>This function is not available for this case, please click cancel to return to the main page, you will need to submit your application outside the portal via email or post.</h3>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesSiihenAccess.class}
  )
  private String tseRespondNotAvailableWarningLabel;
  @CCD(
          label = "<h3>This function is not available for this case, please click cancel to return to the main page, you will need to submit your application outside the portal via email or post.</h3>",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerEmploymentRPlus2RolesSiihenAccess.class}
  )
  private String respondToTribunalNotAvailableWarningLabel;
  @CCD(
          label = "${replyToReferralDcfLink}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class}
  )
  private String replyToReferralDcfLinkLabel;
  @CCD(
          label = "${legalRepDocumentsMarkdown}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentRCaseworkerEmploymentApiCrudAccess.class, CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String legalRepDocumentsMarkdownLabel;
  @CCD(
          label = "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantTseAllApplications/claimantTseAllApplications1\">All applications</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/viewClaimantTSEApplications/viewClaimantTSEApplications1\">View an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/claimantTSE/claimantTSE1\">Make an application</a> <br> <a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/tseClaimantRepResponse/tseClaimantRepResponse1\"> Respond to an application</a>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCudAccess.class}
  )
  private String applicationsLinksClaimantRep;
  @CCD(
          label = "<p>Use this form to apply to amend the ET1 claim.</p> <p>The tribunal will decide if you can make the amendment by judging the fairness of the application. This decision may be made at a hearing.</p> <p>The tribunal also needs to know if you consider the amendment minor or substantial.</p> <p>Providing details of why you want to amend the claim and the importance of the amendment will help the tribunal to decide your application more quickly.</p> <h3>Details to include in your application:</h3> <ul><li>What you want to amend in the claim</li><li>If you consider it a minor or substantial amendment</li><li>Why you want to make this amendment</li><li>Why you are asking to make this amendment now</li><li>How this amendment will benefit you and how could it disadvantage you if not granted</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel1;
  @CCD(
          label = "<p>Use this form to apply to change the personal details given when making the claim.</p> <p>If you change the postal or email address, we’ll send any letters to the new address.</p> <p>If you change the telephone number, we’ll contact you using the new number if we have questions about the claim.</p> <h3>Details you can apply to change:</h3> <ul><li>Name</li><li>Sex and preferred title</li><li>Contact or home address</li><li>Telephone number</li><li>Email address</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel2;
  @CCD(
          label = "<p>Use this form to have a judgment or order made by a legal officer considered afresh by an Employment Judge.</p> <p>Considered afresh means that if you disagree with a judgment or order made by a legal officer in this case, you can ask an Employment Judge to look at that decision again.</p> <p>The judge will take the decision afresh on the basis of the same information as was before the legal officer, unless either side chooses to supply additional information.</p> <h3>Details to include in your application: </h3> <ul><li>The decision you want considered afresh</li><li>The date the tribunal issued the decision</li><li>Why you want the decision considered afresh</li><li>Any relevant additional information</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel3;
  @CCD(
          label = "<p>Tell or ask the tribunal about something relevant to your case.</p> <h3>Do not use this form to: </h3> <ul><li>Seek legal advice from the tribunal. The tribunal is an independent judicial body and cannot give legal advice</li><li>Tell us about settlement offers or discussions, which are private and confidential to the parties</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel4;
  @CCD(
          label = "<p>You can ask the tribunal to order a witness to attend to give evidence.</p> <p>The witness must be in Great Britain (England, Scotland and Wales). This does not include Northern Ireland.</p> <p>The tribunal can limit the number of witnesses to be called to give evidence on a particular issue, especially if that issue is not central to the case.</p> <p>The claimant may also be liable for the costs incurred by the witness’s attendance.</p> <p>You should consider whether the evidence of this witness is likely to help your case.</p> <h3>Details to include in your application: </h3> <ul><li>The witness’s full name and address </li><li>Why the attendance of this witness is necessary for a fair hearing. The tribunal needs to understand why the evidence is relevant, and why there is no alternative way of establishing the same points at the hearing without ordering a witness to attend</li><li>If you have asked this witness to attend voluntarily. If you have, tell us when you asked and what their response was</li><li>If you have not asked the witness to attend yet, explain why</li></ul> <p>If the order is granted, the witness will be ordered to attend on the first day of the hearing. If you want them to attend on another day or days, tell us:</p> <ul><li>The dates apart from the first day of the hearing that you want the witness to attend</li><li>Why their attendance is necessary on those dates</li></ul> <p>If you want the witness to bring documents, tell us: </p> <ul><li>Why these documents are relevant to the issues in this case</li><li>Why an order to disclose the documents would not be enough</li><li>If you have already asked the witness or anyone else to provide these documents. If you have, tell us when you asked and what their response was</li><li>If you have not asked the witness to provide the documents yet, explain why</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel5;
  @CCD(
          label = "<p>Use this form to ask the tribunal to order the respondent to do or provide something.</p> <h3>Details to include in your application: </h3> <ul><li>What you want the respondent to do</li><li>Why it is relevant to your claim</li><li>If you have already asked the respondent to do or provide this thing. If you have, tell us when you asked and what their response was</li><li>If you have not asked the respondent to do or provide this thing yet, explain why</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel6;
  @CCD(
          label = "<p>Use this form to ask the tribunal to postpone a hearing to a later date.</p> <h3>Details to include in your application: </h3> <ul><li>which hearing you want to postpone. If the hearing you want to postpone is listed over multiple days, tell us which day or days you are applying to postpone</li><li>the reason you want to postpone. The tribunal will use this to decide whether to grant your application</li><li>any essential documents to support your application</li><li>Weekday dates within the next 3 months that you, your witnesses or representatives cannot attend a rescheduled hearing</li><li>The reason you cannot attend on those dates</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String claimantTseGuidanceLabel7;
  @CCD(
          label = "<p>Use this form to have a judgment made by a tribunal reconsidered. This may be a judgment made by a judge alone or by a judge sitting with non-legal members.</p> <p>If reconsideration is necessary in the interests of justice you can ask the tribunal to look at the judgment again.</p> <p>Only a judgment which finally determines an issue in your case can be reconsidered. This therefore excludes any decision that does not comprise a final determination of the claim, or part of a claim. If you wish to ask for a case management order to be varied or revoked, use the vary or revoke an order application.</p> <p>You can only ask the tribunal to reconsider a judgment within 14 days of the date the judgment was sent to you.</p> <p>If you want the tribunal to reconsider a judgment that was sent to you over 14 days ago you must explain why your application is late.</p> <p>A reconsideration application does not affect the time limit for appealing to the Employment Appeal Tribunal.</p> <h3>Details to include in your application: </h3> <ul><li>The judgment you want reconsidered</li><li>The date the tribunal issued the judgment</li><li>Your reason for a late application if the judgment was sent over 14 days ago</li><li>Why it is in the interests of justice to reconsider this judgment</li><li>If the tribunal should vary or revoke the judgment</li><li>Any additional information or material which the tribunal does not already have to support your application</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel8;
  @CCD(
          label = "<p>Use this form to tell us that the respondent has not complied with all or part of an order from the tribunal.</p> <p>You should try to resolve your complaint with the respondent. Only use this form if that is not possible.</p> <h3>Details to include in your application: </h3> <ul><li>Which order has not been complied with</li><li>The date the tribunal issued the order</li><li>What the respondent has not done</li><li>What you want the tribunal to do next</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel9;
  @CCD(
          label = "<p>Use this form to apply to prevent or restrict publicity in this case.</p> <p>It is an important principle that justice should normally be delivered in public. However, the tribunal has the power to prevent or restrict the public disclosure of any aspect of your case if necessary in the interests of justice or to protect the Convention rights of any person.</p> <p>The tribunal also has certain other powers to sit in private or to restrict publicity in accordance with sections 10A, 10B, 11 and 12 of the Employment Tribunals Act 1996.</p> <p>The tribunal may issue an order:</p><ul><li>That a hearing that would otherwise be in public be conducted, in whole or in part, in private</li><li>That the identities of specified parties, witnesses or other persons referred to in the proceedings should not be disclosed to the public, by the use of anonymisation or otherwise</li><li>For measures preventing witnesses at a public hearing being identifiable by members of the public</li><li>Restricting the reporting of the case in the media</li></ul> <h3>Details to include in your application: </h3> <ul><li>How the tribunal should prevent or restrict publicity in this case</li><li>Why the restrictions you want are needed. The tribunal must make the least restrictive order possible. Tell us why fewer restrictions would not be enough</li><li>Any media interest in this case. Include names of media organisations or journalists that are interested. The tribunal might seek their comments before making a decision</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel10;
  @CCD(
          label = "<p>You can request that the tribunal strike out all or parts of the response.</p> <p>If something is ‘struck out’ it is removed from the claim or response and cannot be relied upon.</p> <p>The tribunal can strike out all or parts of the respondent's response on their own initiative or after a request from the claimant.</p> <p>A strike out request must be based on at least one of the grounds of Rule 38 of the Employment Tribunals Rules of Procedure.</p> <h3>Details to include in your application: </h3> <ul><li>Why you think the response (or parts of it) should be struck out</li><li>Which ground or grounds in Rule 38 you say applies in your case</li><li>If you are referring to numbered points or paragraphs in a respondent's response, include these numbers or other references</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel11;
  @CCD(
          label = "<p>Use this form to apply to vary or revoke an order the tribunal has issued.</p> <p>Tribunal orders will not usually be varied or revoked unless there has been a material change in circumstances since the order was made, which make the variation in the interests of justice.</p> <p>The tribunal will consider your reasons for varying or revoking the order and any supporting materials you provide then make a decision.</p><h3>Details to include in your application: </h3> <ul><li>The order you want to vary or revoke</li><li>The date the tribunal issued the order</li><li>Explain which part of the order you want to vary or revoke</li><li>How to vary the order</li><li>Why it is in the interests of justice to vary or revoke this order. Tell us any relevant changes of circumstance since the order was made and consider the requirements of the ‘overriding objective’ in Rule 3 of the Employment Tribunal Rules of Procedure</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel12;
  @CCD(
          label = "<p>Use this form when you’ve reached a settlement or do not want to continue with all or part of the claim.</p> <p>You can withdraw the whole claim or tell us which parts you want to withdraw.</p> <p>This brings the claim or part of it to an end.</p> <p>You can withdraw at any point before or during a hearing.</p> <p>If you’re withdrawing because of a settlement, make sure you have the settlement in writing.</p> <h3>Withdrawal and dismissal</h3> <p>Once you have told the tribunal you want to withdraw the claim, the tribunal will usually issue a judgment dismissing the claim or part of the claim.</p> <p>A dismissal judgment stops you from making that claim against the same respondents in the future.</p> <p>The tribunal will use Rule 51 of the Employment Tribunal Rules of Procedure to decide whether a dismissal judgment is issued following a withdrawal.</p> <p>Withdrawal and dismissal through this service will be final unless:</p> <ul><li>The tribunal contacts you needing more information</li><li>You have given an acceptable reason why dismissal should not happen</li></ul> <h3>Details to include in your application:</h3> <ul><li>Which parts of the claim you want withdrawn and dismissed</li><li>If you do not want the claim (or part of it) dismissed, you must explain why</li></ul> <h3>Consider when providing material that</h3> <ul><li>You can upload letters, photos or documents if they are relevant to what you want to tell or ask the tribunal. Please do not upload any other material at this stage. If the tribunal needs to see documents before making a decision then you may be asked to provide them at a later stage.</li><li>If you are taking a picture of a letter, place it on a flat surface and take the picture from above.</li><li>If you are uploading written documents with tracked changes, make sure that tracked changes are turned on.</li></ul> <h3>Give details of your application in the text box or upload a file</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseGuidanceLabel13;
  @CCD(
          label = "The tribunal must operate in a way fair to both sides. This means that each party must see or hear what the other party tells the tribunal. The rules therefore require all communications with the tribunal to be copied to the other party, apart from in exceptional circumstances.",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseRule92TextArea;
  @CCD(
          label = "<h3>To copy this correspondence to the other party, you must send it to them by post or email. They cannot view it in this service.</h3>",
          typeOverride = FieldType.Label,
          access = {CLAIMANTSOLICITORCruAccess.class}
  )
  private String claimantTseRule92TextWhenRespOffline;
  @CCD(
          label = "All applications <hr>",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentLegalrepSolicitorRAccess.class}
  )
  private String claimantTseAllApplicationsLabel;
  @CCD(
          label = "${claimantTseTableMarkUp}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentLegalrepSolicitorRAccess.class}
  )
  private String claimantTseTableLabel;
  @CCD(label = "${claimantRepResponseIntro}", typeOverride = FieldType.Label)
  private String claimantRepTseRespIntroLabel;
  @CCD(
          label = "${claimantRepResponseTable}",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentLegalrepSolicitorCruAccess.class}
  )
  private String claimantRepResponseTableLabel;
  @CCD(
          label = "# Do you want to delete this draft claim?",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentLegalrepSolicitorRAccess.class}
  )
  private String deleteDraftCaseWarningLabel;
  @CCD(
          label = "You are about the delete the draft claim for ${[CASE_REFERENCE]}.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentLegalrepSolicitorRAccess.class}
  )
  private String deleteDraftCaseWarning1;
  @CCD(
          label = "Once deleted, you will not be able to recover it.",
          typeOverride = FieldType.Label,
          access = {CaseworkerEmploymentLegalrepSolicitorRAccess.class}
  )
  private String deleteDraftCaseWarning2;
  // ==== end synthesised definition-only fields ====
}
