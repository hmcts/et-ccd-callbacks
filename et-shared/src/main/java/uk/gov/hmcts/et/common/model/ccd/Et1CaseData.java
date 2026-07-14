package uk.gov.hmcts.et.common.model.ccd;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AdditionalCaseInfoType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantOtherType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantWorkAddressType;
import uk.gov.hmcts.et.common.model.ccd.types.NewEmploymentType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.et.common.model.ccd.types.TTL;
import uk.gov.hmcts.et.common.model.ccd.types.TaskListCheckType;
import uk.gov.hmcts.et.common.model.ccd.types.TriageQuestions;
import uk.gov.hmcts.et.common.model.ccd.types.citizenhub.HubLinksStatuses;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;

import java.util.List;

/**
 * Employment Tribunal claim data that is input on the ET1 form by a claimant.
 * This class should only contain data that is specifically part of the ET1 form.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class Et1CaseData extends BaseCaseData {
    @JsonProperty("typeOfClaim")
    @CCD(
            label = "Deprecated",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Deprecate",
            typeOverride = FieldType.DynamicList,
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> typeOfClaim;
    @JsonProperty("typesOfClaim")
    @CCD(
            label = "Types Of Claim",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_typeOfClaim",
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Types Of Claim",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_typeOfClaim",
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<String> typesOfClaim;
    @JsonProperty("caseType")
    @CCD(
            id = "caseType",
            label = "Single or Multiple",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_CaseType",
            access = SingleAccess.Access137.class,
            includeInProfiles = EnglandWalesSingleCftlibDefinition.class
    )
    @CCD(
            id = "caseType",
            label = "Single or Multiple",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_CaseType",
            access = SingleAccess.Access168.class,
            includeInProfiles = ScotlandSingleCftlibDefinition.class
    )
    @CCD(
            id = "caseType",
            label = "Single or Multiple",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_CaseType",
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleProdDefinition.class
    )
    @CCD(
            id = "caseType",
            label = "Single or Multiple",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_CaseType",
            access = SingleAccess.Access172.class,
            includeInProfiles = ScotlandSingleProdDefinition.class
    )
    private String ecmCaseType;
    @JsonProperty("caseSource")
    @CCD(
            label = "Source",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "caseSourceList",
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Source",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "caseSourceList",
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String caseSource;
    @JsonProperty("claimantRepresentedQuestion")
    @CCD(
            label = "Is the Claimant Represented?",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access050.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is the Claimant Represented?",
            typeOverride = FieldType.YesOrNo,
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantRepresentedQuestion;
    @JsonProperty("claimantWorkAddressQuestion")
    @CCD(
            label = "Is this the same as the claimant's work address?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access050.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Is this the same as the claimant's work address?",
            typeOverride = FieldType.YesOrNo,
            searchable = false,
            access = SingleAccess.Access087.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantWorkAddressQuestion;
    @JsonProperty("ClaimantPcqId")
    @CCD(
            label = "PCQ ID",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access125.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "PCQ ID",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access124.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String claimantPcqId;
    @JsonProperty("jurCodesCollection")
    @CCD(
            label = "Jurisdiction",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Jurisdiction",
            access = SingleAccess.Access143.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Jurisdiction",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Jurisdiction",
            access = SingleAccess.Access174.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<JurCodesTypeItem> jurCodesCollection;
    @JsonProperty("claimantIndType")
    @CCD(
            label = "Claimant Details",
            typeNameOverride = "ClaimantIndividual",
            access = SingleAccess.Access055.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant  Details",
            typeNameOverride = "ClaimantIndividual",
            access = SingleAccess.Access092.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private ClaimantIndType claimantIndType;
    @JsonProperty("claimantType")
    @CCD(
            label = " ",
            typeNameOverride = "ClaimantCorrespondence",
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "ClaimantCorrespondence",
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private ClaimantType claimantType;
    @JsonProperty("representativeClaimantType")
    @CCD(
            label = "Claimant Representative Details",
            typeNameOverride = "ClaimantRepresentative",
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant Representative Details",
            typeNameOverride = "ClaimantRepresentative",
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private RepresentedTypeC representativeClaimantType;
    @JsonProperty("claimantOtherType")
    @CCD(
            label = "Other details",
            typeNameOverride = "EmploymentDetails",
            searchable = false,
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Other details",
            typeNameOverride = "EmploymentDetails",
            searchable = false,
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private ClaimantOtherType claimantOtherType;
    @JsonProperty("respondentCollection")
    @CCD(
            label = "Respondents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Respondent",
            min = 1,
            access = SingleAccess.Access033.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Respondents",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Respondent",
            min = 1,
            access = SingleAccess.Access041.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private List<RespondentSumTypeItem> respondentCollection;
    @JsonProperty("claimantWorkAddress")
    @CCD(
            label = " ",
            hint = "If the claimant has worked at a different address to the Respondent Address, please give the full address",
            typeNameOverride = "WorkAddressDetails",
            searchable = false,
            access = SingleAccess.Access050.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            hint = "If the claimant has worked at a different address to the Respondent Address, please give the full address",
            typeNameOverride = "WorkAddressDetails",
            searchable = false,
            access = SingleAccess.Access087.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private ClaimantWorkAddressType claimantWorkAddress;
    @JsonProperty("caseNotes")
    @CCD(
            label = "Case Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access144.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Case Notes",
            typeOverride = FieldType.TextArea,
            searchable = false,
            access = SingleAccess.Access175.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String caseNotes;
    @JsonProperty("additionalCaseInfo")
    @CCD(
            id = "additionalCaseInfo",
            label = "Additional information",
            hint = "Additional case information",
            typeNameOverride = "AdditionalCaseDetails",
            searchable = false,
            access = SingleAccess.Access050.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "additionalCaseInfo",
            label = "Additional information",
            hint = "Additional case information",
            typeNameOverride = "AdditionalCaseDetails",
            searchable = false,
            access = SingleAccess.Access087.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private AdditionalCaseInfoType additionalCaseInfoType;
    @JsonProperty("managingOffice")
    @CCD(
            label = "Tribunal Office",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice",
            access = SingleAccess.Access154.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Managing Office",
            hint = "Location of the physical case file",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "VenueScotland",
            access = SingleAccess.Access167.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String managingOffice;
    @JsonProperty("newEmploymentType")
    @CCD(
            label = "New employment details",
            typeNameOverride = "NewEmploymentDetails",
            searchable = false,
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "New employment details",
            typeNameOverride = "NewEmploymentDetails",
            searchable = false,
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private NewEmploymentType newEmploymentType;
    @JsonProperty("claimantRequests")
    @CCD(
            label = "Claimant Requests",
            typeNameOverride = "ClaimantRequest",
            searchable = false,
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Claimant Requests",
            typeNameOverride = "ClaimantRequest",
            searchable = false,
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private ClaimantRequestType claimantRequests;
    @JsonProperty("claimantHearingPreference")
    @CCD(
            label = "Additional Claimant Information",
            typeNameOverride = "HearingPreference",
            access = SingleAccess.Access056.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Additional Claimant Information",
            typeNameOverride = "HearingPreference",
            searchable = false,
            access = SingleAccess.Access093.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private ClaimantHearingPreference claimantHearingPreference;
    @JsonProperty("claimantTaskListChecks")
    @CCD(
            label = "Task list check",
            typeNameOverride = "TaskListCheck",
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Task list check",
            typeNameOverride = "TaskListCheck",
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private TaskListCheckType claimantTaskListChecks;
    @JsonProperty("receiptDate")
    @CCD(
            label = "Date of Receipt",
            typeOverride = FieldType.Date,
            access = SingleAccess.Access141.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = "Date of Receipt",
            typeOverride = FieldType.Date,
            access = SingleAccess.Access168.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String receiptDate;
    @JsonProperty("triageQuestions")
    @CCD(
            label = " ",
            typeNameOverride = "TriageQuestions",
            searchable = false,
            access = SingleAccess.Access052.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "TriageQuestions",
            searchable = false,
            access = SingleAccess.Access089.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private TriageQuestions triageQuestions;
    @JsonProperty("et1OnlineSubmission")
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            access = SingleAccess.Access073.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            searchable = false,
            access = SingleAccess.Access099.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private String et1OnlineSubmission;
    // Citizen hub
    @JsonProperty("hubLinksStatuses")
    @CCD(
            label = " ",
            typeNameOverride = "hubLinksStatuses",
            access = SingleAccess.Access073.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            label = " ",
            typeNameOverride = "hubLinksStatuses",
            searchable = false,
            access = SingleAccess.Access099.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private HubLinksStatuses hubLinksStatuses;
    @JsonProperty("TTL")
    @CCD(
            id = "TTL",
            label = "Case Deletion",
            typeOverride = FieldType.TTL,
            includeSearchable = true,
            access = SingleAccess.Access161.class,
            includeInProfiles = EnglandWalesSingleDefinition.class
    )
    @CCD(
            id = "TTL",
            label = "Case Deletion",
            typeOverride = FieldType.TTL,
            includeSearchable = true,
            access = SingleAccess.Access190.class,
            includeInProfiles = ScotlandSingleDefinition.class
    )
    private TTL ttl;
}
