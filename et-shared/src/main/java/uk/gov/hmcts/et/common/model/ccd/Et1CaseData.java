package uk.gov.hmcts.et.common.model.ccd;

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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRPlus5RolesBbxtjpAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudCitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorDAccess;
import uk.gov.hmcts.et.common.ccd.access.CitizenCuAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorCudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorCrudCitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess;
import uk.gov.hmcts.et.common.ccd.access.CitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudCitizenCudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CitizenCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.DefaultAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEtjudgeRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorRCitizenCruAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCrudCitizenCuAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentLegalrepSolicitorRAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentApiCaseworkerEmploymentLegalrepSolicitorCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.TTLProfileCrudAccess;
import uk.gov.hmcts.et.common.ccd.access.CaseworkerEmploymentEnglandwalesCrudAccess;

/**
 * Employment Tribunal claim data that is input on the ET1 form by a claimant.
 * This class should only contain data that is specifically part of the ET1 form.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@EqualsAndHashCode(callSuper = false)
public class Et1CaseData extends BaseCaseData {
    @CCD(
            label = "Deprecated",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("typeOfClaim")
    private List<String> typeOfClaim;
    @CCD(
            label = "Types Of Claim",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_typeOfClaim",
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("typesOfClaim")
    private List<String> typesOfClaim;
    @CCD(
            label = "Single or Multiple",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_CaseType",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("caseType")
    private String ecmCaseType;
    @CCD(
            label = "Source",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "caseSourceList",
            access = {CaseworkerEmploymentApiCudPlus2RolesJgoofxAccess.class, CaseworkerEmploymentLegalrepSolicitorDAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("caseSource")
    private String caseSource;
    @CCD(
            label = "Is the Claimant Represented?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess.class, CaseworkerEmploymentLegalrepSolicitorCudAccess.class}
    )
    @JsonProperty("claimantRepresentedQuestion")
    private String claimantRepresentedQuestion;
    @CCD(
            label = "Is this the same as the claimant's work address?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCrudCitizenCruAccess.class}
    )
    @JsonProperty("claimantWorkAddressQuestion")
    private String claimantWorkAddressQuestion;
    @CCD(
            label = "PCQ ID",
            searchable = false,
            access = {CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("ClaimantPcqId")
    private String claimantPcqId;
    @CCD(
            label = "Jurisdiction",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Jurisdiction",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCudAccess.class}
    )
    @JsonProperty("jurCodesCollection")
    private List<JurCodesTypeItem> jurCodesCollection;
    @CCD(
            label = "Claimant Details",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    @JsonProperty("claimantIndType")
    private ClaimantIndType claimantIndType;
    @CCD(
            label = " ",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("claimantType")
    private ClaimantType claimantType;
    @CCD(
            label = "Claimant Representative Details",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("representativeClaimantType")
    private RepresentedTypeC representativeClaimantType;
    @CCD(
            label = "Other details",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("claimantOtherType")
    private ClaimantOtherType claimantOtherType;
    @CCD(
            label = "Respondents",
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Respondent",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCaseworkerWaTaskConfigurationRAccess.class, CaseworkerEmploymentLegalrepSolicitorCudAccess.class, CaseworkerEmploymentCrudAccess.class, CitizenCrudAccess.class}
    )
    @JsonProperty("respondentCollection")
    private List<RespondentSumTypeItem> respondentCollection;
    @CCD(
            label = " ",
            hint = "If the claimant has worked at a different address to the Respondent Address, please give the full address",
            searchable = false,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentRCaseworkerEmploymentApiCrudCitizenCruAccess.class, CaseworkerEmploymentLegalrepSolicitorCudAccess.class}
    )
    @JsonProperty("claimantWorkAddress")
    private ClaimantWorkAddressType claimantWorkAddress;
    @CCD(
            label = "Case Notes",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudAccess.class}
    )
    @JsonProperty("caseNotes")
    private String caseNotes;
    @CCD(
            label = "Additional information",
            hint = "Additional case information",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorCrudCitizenCruAccess.class}
    )
    @JsonProperty("additionalCaseInfo")
    private AdditionalCaseInfoType additionalCaseInfoType;
    @CCD(
            label = "Tribunal Office",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TribunalOffice",
            access = {DefaultAccess.class, CaseworkerEmploymentEtjudgeRAccess.class, CaseworkerWaTaskConfigurationRAccess.class, CitizenCrudAccess.class}
    )
    @JsonProperty("managingOffice")
    private String managingOffice;
    @CCD(
            label = "New employment details",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorRCitizenCruAccess.class}
    )
    @JsonProperty("newEmploymentType")
    private NewEmploymentType newEmploymentType;
    @CCD(
            label = "Claimant Requests",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorRCitizenCruAccess.class}
    )
    @JsonProperty("claimantRequests")
    private ClaimantRequestType claimantRequests;
    @CCD(
            label = "Additional Claimant Information",
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCuAccess.class, CaseworkerEmploymentRAccess.class}
    )
    @JsonProperty("claimantHearingPreference")
    private ClaimantHearingPreference claimantHearingPreference;
    @CCD(
            label = "Task list check",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("claimantTaskListChecks")
    private TaskListCheckType claimantTaskListChecks;
    @CCD(
            label = "Date of Receipt",
            typeOverride = FieldType.Date,
            access = {CaseworkerEmploymentEnglandwalesCruPlus2RolesHenbjpAccess.class, CaseworkerEmploymentApiCrudCitizenCruAccess.class}
    )
    @JsonProperty("receiptDate")
    private String receiptDate;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerEmploymentRPlus5RolesBbxtjpAccess.class, CaseworkerEmploymentLegalrepSolicitorDCitizenCruAccess.class}
    )
    @JsonProperty("triageQuestions")
    private TriageQuestions triageQuestions;
    @CCD(
            label = " ",
            access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class, CaseworkerEmploymentLegalrepSolicitorRAccess.class, CitizenCrudAccess.class}
    )
    @JsonProperty("et1OnlineSubmission")
    private String et1OnlineSubmission;
    // Citizen hub
    @CCD(
            label = " ",
            access = {DefaultAccess.class, CaseworkerEmploymentCaseworkerEmploymentEtjudgeRAccess.class, CaseworkerEmploymentLegalrepSolicitorRAccess.class, CitizenCrudAccess.class}
    )
    @JsonProperty("hubLinksStatuses")
    private HubLinksStatuses hubLinksStatuses;
    @CCD(
            label = "Case Deletion",
            access = {CaseworkerEmploymentApiCaseworkerEmploymentLegalrepSolicitorCrudAccess.class, TTLProfileCrudAccess.class, CaseworkerEmploymentEnglandwalesCrudAccess.class, CitizenCrudAccess.class}
    )
    @JsonProperty("TTL")
    private TTL ttl;
}
