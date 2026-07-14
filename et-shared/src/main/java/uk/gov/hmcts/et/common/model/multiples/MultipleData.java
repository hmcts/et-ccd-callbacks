package uk.gov.hmcts.et.common.model.multiples;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.et.common.model.bulk.items.CaseIdTypeItem;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicMultiSelectListType;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesMultipleCftlibDefinition;
import uk.gov.hmcts.et.common.model.ccd.EnglandWalesMultipleProdDefinition;
import uk.gov.hmcts.et.common.model.ccd.MultipleAccess;
import uk.gov.hmcts.et.common.model.ccd.ScotlandMultipleCftlibDefinition;
import uk.gov.hmcts.et.common.model.ccd.ScotlandMultipleProdDefinition;
import uk.gov.hmcts.et.common.model.ccd.items.AddressLabelTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.AddressLabelsAttributesType;
import uk.gov.hmcts.et.common.model.ccd.types.CaseNote;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.et.common.model.ccd.types.DynamicListType;
import uk.gov.hmcts.et.common.model.ccd.types.NotificationsExtract;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SubCaseLegalRepDetails;
import uk.gov.hmcts.et.common.model.generic.BaseCaseData;
import uk.gov.hmcts.et.common.model.multiples.items.CaseMultipleTypeItem;
import uk.gov.hmcts.et.common.model.multiples.items.SubMultipleTypeItem;
import uk.gov.hmcts.et.common.model.multiples.types.MoveCasesType;
import uk.gov.hmcts.et.common.model.multiples.types.SubMultipleActionType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class MultipleData extends BaseCaseData {

    @CCD(
            label = "Case Numbers",
            hint = "The first case will be assigned as the lead for this multiple",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseNumber",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Case Numbers",
            hint = "The first case will be assigned as the lead for this multiple",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseNumber",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("caseIdCollection")
    private List<CaseIdTypeItem> caseIdCollection;

    @CCD(
            label = "Multiple Information",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "multipleMigrationData",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Multiple Information",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "multipleMigrationData",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("caseMultipleCollection")
    private List<CaseMultipleTypeItem> caseMultipleCollection;

    @CCD(
            label = "Multiple Name",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access12.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Multiple Name",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access39.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @CCD(
            label = "Multiple Name",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {EnglandWalesMultipleProdDefinition.class})
    @CCD(
            label = "Multiple Name",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {ScotlandMultipleProdDefinition.class})
    @JsonProperty("multipleName")
    private String multipleName;

    @CCD(
            label = "Multiple reference",
            hint = "Assign a multiple reference",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access11.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Multiple reference",
            hint = "Assign a multiple reference",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access38.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @CCD(
            label = "Multiple reference",
            hint = "Assign a multiple reference",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {EnglandWalesMultipleProdDefinition.class})
    @CCD(
            label = "Multiple reference",
            hint = "Assign a multiple reference",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {ScotlandMultipleProdDefinition.class})
    @JsonProperty("multipleReference")
    private String multipleReference;

    @CCD(
            label = "Selection Criteria",
            typeNameOverride = "importerFileUpload",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Selection Criteria",
            typeNameOverride = "importerFileUpload",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("caseImporterFile")
    private CaseImporterFile caseImporterFile;

    @CCD(
            label = "state is $[STATE]",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "state is $[STATE]",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("state")
    private String state;

    @CCD(
            label = "Source",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "caseSourceList",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Source",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "caseSourceList",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("multipleSource")
    private String multipleSource;

    @CCD(
            label = "Lead Case",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access13.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Lead Case",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access40.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @CCD(
            label = "Lead Case",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {EnglandWalesMultipleProdDefinition.class})
    @CCD(
            label = "Lead Case",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {ScotlandMultipleProdDefinition.class})
    @JsonProperty("leadCase")
    private String leadCase;

    @CCD(
            label = "Lead CaseId",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Lead CaseId",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("leadCaseId")
    private String leadCaseId;

    @JsonProperty("leadEthosCaseRef")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private String leadEthosCaseRef;

    @CCD(
            label = "New lead case",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "New lead case",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("newLeadCase")
    private String amendLeadCase;

    @CCD(
            label = "Case Ref Number Count",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access13.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Case Ref Number Count",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access40.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @CCD(
            label = "Case Ref Number Count",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {EnglandWalesMultipleProdDefinition.class})
    @CCD(
            label = "Case Ref Number Count",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {ScotlandMultipleProdDefinition.class})
    @JsonProperty("caseCounter")
    private String caseCounter;

    @CCD(
            label = "preAccept system field",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "preAccept system field",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("preAcceptDone")
    private String preAcceptDone;

    @CCD(
            label = "Submultiple Name",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Submultiple Name",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("subMultiple")
    private DynamicFixedListType subMultiple;

    @CCD(
            label = "Flag 1",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Flag 1",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("flag1")
    private DynamicFixedListType flag1;

    @CCD(
            label = "Flag 2",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Flag 2",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("flag2")
    private DynamicFixedListType flag2;

    @CCD(
            label = "Flag 3",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Flag 3",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("flag3")
    private DynamicFixedListType flag3;

    @CCD(
            label = "Flag 4",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Flag 4",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("flag4")
    private DynamicFixedListType flag4;

    @CCD(
            label = "Select schedule to print",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_scheduleDoc",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Select schedule to print",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_scheduleDoc",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("scheduleDocName")
    private String scheduleDocName;

    @JsonProperty("docMarkUp")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private String docMarkUp;

    @CCD(
            label = "Batch Update",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_batchUpdate",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Batch Update",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_batchUpdate",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("batchUpdateType")
    private String batchUpdateType;

    @CCD(
            label = "Case Number",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Case Number",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("batchUpdateCase")
    private String batchUpdateCase;

    @CCD(
            label = "Update Live cases only?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_liveCases",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Update Live cases only?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_liveCases",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("liveCases")
    private String liveCases;

    @CCD(
            label = "Claimant Representative Name",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Claimant Representative Name",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("batchUpdateClaimantRep")
    private DynamicFixedListType batchUpdateClaimantRep;

    @CCD(
            label =
                    "Do you wish to remove or add the representative from all the filtered cases ?"
                        + " (Yes = Remove, No = Add)",
            hint =
                    "If the representative is to be removed then it is also removed from the case"
                        + " driving the batch update",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label =
                    "Do you wish to remove or add the representative from all the filtered cases ?"
                        + " (Yes = Remove, No = Add)",
            hint =
                    "If the representative is to be removed then it is also removed from the case"
                        + " driving the batch update",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("batchRemoveClaimantRep")
    private String batchRemoveClaimantRep;

    @CCD(
            label = "Jurisdictions",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DynamicListCollection",
            access = MultipleAccess.Access04.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Jurisdictions",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DynamicListCollection",
            access = MultipleAccess.Access33.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private ListTypeItem<DynamicListType> batchUpdateJurisdictionList;

    @CCD(
            label = "Jurisdiction",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Jurisdiction",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("batchUpdateJurisdiction")
    private DynamicFixedListType batchUpdateJurisdiction;

    @CCD(
            label = "Respondent",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Respondent",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("batchUpdateRespondent")
    private DynamicFixedListType batchUpdateRespondent;

    @CCD(
            label = "Judgment",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Judgment",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("batchUpdateJudgment")
    private DynamicFixedListType batchUpdateJudgment;

    @CCD(
            label = "Respondent Representative",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Respondent Representative",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("batchUpdateRespondentRep")
    private DynamicFixedListType batchUpdateRespondentRep;

    @CCD(
            label =
                    "Do you wish to remove or add the representative from all the filtered cases ?"
                        + " (Yes = Remove, No = Add)",
            hint =
                    "If the representative is to be removed then it is also removed from the case"
                        + " driving the batch update",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label =
                    "Do you wish to remove or add the representative from all the filtered cases ?"
                        + " (Yes = Remove, No = Add)",
            hint =
                    "If the representative is to be removed then it is also removed from the case"
                        + " driving the batch update",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("batchRemoveRespondentRep")
    private String batchRemoveRespondentRep;

    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @JsonProperty("fileLocation")
    private DynamicFixedListType fileLocation;

    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("fileLocationGlasgow")
    private DynamicFixedListType fileLocationGlasgow;

    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("fileLocationAberdeen")
    private DynamicFixedListType fileLocationAberdeen;

    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("fileLocationDundee")
    private DynamicFixedListType fileLocationDundee;

    @CCD(
            label = "Physical Location",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("fileLocationEdinburgh")
    private DynamicFixedListType fileLocationEdinburgh;

    @CCD(
            label = "Current Position",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Position",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Current Position",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Position",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("positionType")
    private String positionType;

    @CCD(
            label = "Clerk Responsible",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Clerk Responsible",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("clerkResponsible")
    private DynamicFixedListType clerkResponsible;

    @CCD(
            label = "Date of Receipt",
            typeOverride = FieldType.Date,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Date of Receipt",
            typeOverride = FieldType.Date,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("receiptDate")
    private String receiptDate;

    @CCD(
            label = "EQP Stage Hearing",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Stage",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "EQP Stage Hearing",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Stage",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("hearingStageEQP")
    private String hearingStage;

    @CCD(
            typeNameOverride = "moveCases",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            typeNameOverride = "moveCases",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("batchMoveCases")
    private MoveCasesType moveCases;

    @CCD(
            typeOverride = FieldType.Collection,
            typeParameterOverride = "subMultipleType",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            typeOverride = FieldType.Collection,
            typeParameterOverride = "subMultipleType",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("subMultipleCollection")
    private List<SubMultipleTypeItem> subMultipleCollection;

    @CCD(
            typeNameOverride = "subMultipleActionType",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            typeNameOverride = "subMultipleActionType",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("subMultipleAction")
    private SubMultipleActionType subMultipleAction;

    @CCD(
            label = "List of correspondence items",
            typeNameOverride = "LettersMultiples",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("correspondenceScotType")
    private CorrespondenceScotType correspondenceScotType;

    @CCD(
            label = "List of correspondence items",
            typeNameOverride = "LettersMultiples",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @JsonProperty("correspondenceType")
    private CorrespondenceType correspondenceType;

    @CCD(
            label = "Select the labels you wish to print",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_SelectLabels",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Select the labels you wish to print",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_SelectLabels",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("addressLabelsSelectionTypeMSL")
    private List<String> addressLabelsSelectionTypeMSL;

    @JsonProperty("addressLabelCollection")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private List<AddressLabelTypeItem> addressLabelCollection;

    @CCD(
            label = "Address labels attributes",
            typeNameOverride = "addressLabelsAttributes",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Address labels attributes",
            typeNameOverride = "addressLabelsAttributes",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("addressLabelsAttributesType")
    private AddressLabelsAttributesType addressLabelsAttributesType;

    @CCD(
            label = "Select the amendments you wish to make:",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "multiplesAmendment",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Select the amendments you wish to make:",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "multiplesAmendment",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("typeOfAmendment")
    private List<String> typeOfAmendmentMSL;

    @CCD(
            label = "Select office to transfer case to",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Select office to transfer case to",
            typeOverride = FieldType.DynamicList,
            typeParameterOverride = "Text",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("officeMultipleCT")
    private DynamicFixedListType officeMultipleCT;

    @CCD(
            label = "Reason for Case Transfer",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Reason for Case Transfer",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("reasonForCT")
    private String reasonForCT;

    @CCD(
            label = "Link to related multiple",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Link to related multiple",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("linkedMultipleCT")
    private String linkedMultipleCT;

    @CCD(
            label = "Current Position",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PositionCT",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Current Position",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PositionCT",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("positionTypeCT")
    private String positionTypeCT;

    @CCD(
            label = "Accept/Reject",
            typeNameOverride = "acceptOrRejectCase",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Accept/Reject",
            typeNameOverride = "acceptOrRejectCase",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("preAcceptMultiple")
    private CasePreAcceptType preAcceptCase;

    @CCD(
            label = "Notes",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Notes",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("notes")
    private String notes;

    @CCD(
            label = "Select import file",
            typeNameOverride = "importerFileUpload",
            access = MultipleAccess.Access55.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("bulkAddSingleCasesImportFile")
    private CaseImporterFile bulkAddSingleCasesImportFile;

    @CCD(
            label = "Fix Case",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access55.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            label = "Fix Case",
            typeOverride = FieldType.Text,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("isFixCase")
    private String isFixCase;

    @CCD(
            label = "Case stayed?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Case stayed?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access31.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("batchCaseStayed")
    private String batchCaseStayed;

    // sendNotification
    @CCD(
            typeOverride = FieldType.Collection,
            typeParameterOverride = "sendNotificationCollection",
            access = MultipleAccess.Access08.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            typeOverride = FieldType.Collection,
            typeParameterOverride = "sendNotificationCollection",
            access = MultipleAccess.Access36.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationCollection")
    private List<SendNotificationTypeItem> sendNotificationCollection;

    @CCD(
            label = "Enter notification title",
            hint =
                    "Start with a verb if you need the parties to do something. For example: submit"
                        + " hearing agenda, view notice of hearing. Please note the text entered"
                        + " here will be displayed in the notification e-mail sent to the parties.",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Enter notification title",
            hint =
                    "Start with a verb if you need the parties to do something. For example: submit"
                        + " hearing agenda, view notice of hearing. Please note the text entered"
                        + " here will be displayed in the notification e-mail sent to the parties.",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationTitle")
    private String sendNotificationTitle;

    @CCD(
            label = "Is there a letter to send out?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Is there a letter to send out?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationLetter")
    private String sendNotificationLetter;

    @CCD(
            label = "Upload document",
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Upload document",
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access31.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationUploadDocument")
    private List<DocumentTypeItem> sendNotificationUploadDocument;

    @CCD(
            label = "Notification subject",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_sendNotificationSubject",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Notification subject",
            hint = "Select all that apply",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_sendNotificationSubject",
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationSubject")
    private List<String> sendNotificationSubject;

    @CCD(
            label = "Additional information",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Additional information",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationAdditionalInfo")
    private String sendNotificationAdditionalInfo;

    @CCD(
            label = "Select the party or parties to notify",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "notifyMultiple",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Select the party or parties to notify",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "notifyMultiple",
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationNotify")
    private String sendNotificationNotify;

    @CCD(
            label = "Party Selection",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationNotifyLead",
            access = MultipleAccess.Access03.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Party Selection",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationNotifyLead",
            access = MultipleAccess.Access34.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationNotifyLeadCase")
    private String sendNotificationNotifyLeadCase;

    @CCD(
            label = "Party Selection",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationNotifyAll",
            access = MultipleAccess.Access03.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Party Selection",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationNotifyAll",
            access = MultipleAccess.Access34.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationNotifyAll")
    private String sendNotificationNotifyAll;

    @CCD(
            label = "Party Selection",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationNotifySelected",
            access = MultipleAccess.Access03.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Party Selection",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationNotifySelected",
            access = MultipleAccess.Access34.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationNotifySelected")
    private String sendNotificationNotifySelected;

    @CCD(
            label = "Select the hearing",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Select the hearing",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationSelectHearing")
    private DynamicFixedListType sendNotificationSelectHearing;

    @CCD(
            label = "Is this a case management order or request?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationCaseManagement",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Is this a case management order or request?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationCaseManagement",
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationCaseManagement")
    private String sendNotificationCaseManagement;

    @CCD(
            label = "Is a response to the tribunal required?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationResponseTribunal",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Is a response to the tribunal required?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationResponseTribunal",
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationResponseTribunal")
    private String sendNotificationResponseTribunal;

    @CCD(
            label = "Who made the case management order?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationWhoCaseOrder",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Who made the case management order?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationWhoCaseOrder",
            access = MultipleAccess.Access31.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationWhoCaseOrder")
    private String sendNotificationWhoCaseOrder;

    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_sendNotificationParties",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Select the party or parties who must respond",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_sendNotificationParties",
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationSelectParties")
    private String sendNotificationSelectParties;

    @CCD(
            label = "Full name",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Full name",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationFullName")
    private String sendNotificationFullName;

    @CCD(
            label = "Full name",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Full name",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationFullName2")
    private String sendNotificationFullName2;

    @CCD(
            label = "Details",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Details",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationDetails")
    private String sendNotificationDetails;

    @CCD(
            label = "Decision",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationDecision",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Decision",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationDecision",
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationDecision")
    private String sendNotificationDecision;

    @CCD(
            label = "Request was made by",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_sendNotificationRequestMadeBy",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Request was made by",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_sendNotificationRequestMadeBy",
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationRequestMadeBy")
    private String sendNotificationRequestMadeBy;

    @CCD(
            label = "What is the ECC notification?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationEccQuestion",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "What is the ECC notification?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "sendNotificationEccQuestion",
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationEccQuestion")
    private String sendNotificationEccQuestion;

    @CCD(
            label = "Who made the judgment?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationWhoMadeJudgement",
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Who made the judgment?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "fl_sendNotificationWhoMadeJudgement",
            access = MultipleAccess.Access31.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationWhoMadeJudgement")
    private String sendNotificationWhoMadeJudgement;

    @CCD(
            label = "Notifications extract",
            typeNameOverride = "notificationsExtract",
            access = MultipleAccess.Access10.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Notifications extract",
            typeNameOverride = "notificationsExtract",
            access = MultipleAccess.Access37.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("notificationsExtract")
    private NotificationsExtract notificationsExtract;

    // Referral
    @CCD(
            label = "Referrals",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "referralDetails",
            access = MultipleAccess.Access07.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Referrals",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "referralDetails",
            access = MultipleAccess.Access35.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referralCollection")
    private List<ReferralTypeItem> referralCollection;

    @CCD(
            label = "Referral Hearing Details placeholder",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access19.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Referral Hearing Details placeholder",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access47.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referralHearingDetails")
    private String referralHearingDetails;

    @CCD(
            label = "Select a referral",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access04.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Select a referral",
            typeOverride = FieldType.DynamicList,
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("selectReferral")
    private DynamicFixedListType selectReferral;

    // Referral Type
    @CCD(
            label = "Who are you referring this case to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = MultipleAccess.Access22.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Who are you referring this case to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = MultipleAccess.Access43.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referCaseTo")
    private String referCaseTo;

    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access22.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access43.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referentEmail")
    private String referentEmail;

    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access22.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access43.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("isUrgent")
    private String isUrgent;

    @CCD(
            label = "What is the referral subject?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ReferralSubject",
            access = MultipleAccess.Access22.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "What is the referral subject?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ReferralSubject",
            access = MultipleAccess.Access43.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referralSubject")
    private String referralSubject;

    @CCD(
            label = "Please specify",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access22.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Please specify",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access43.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referralSubjectSpecify")
    private String referralSubjectSpecify;

    @CCD(
            label = "Give details of the referral",
            hint = "Explain what you're referring and why, include answers or directions you need.",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access22.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Give details of the referral",
            hint = "Explain what you're referring and why, include answers or directions you need.",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access43.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referralDetails")
    private String referralDetails;

    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access26.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access49.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referralDocument")
    private List<DocumentTypeItem> referralDocument;

    @CCD(
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access22.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access43.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referralInstruction")
    private String referralInstruction;

    @JsonProperty("referredBy")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private String referredBy;

    @JsonProperty("referralDate")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private String referralDate;

    // Referral Update
    @JsonProperty("updateReferralNumber")
    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private String updateReferralNumber;

    @CCD(
            label = "Who are you referring this case to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = MultipleAccess.Access19.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Who are you referring this case to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = MultipleAccess.Access46.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("updateReferCaseTo")
    private String updateReferCaseTo;

    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access19.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access46.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("updateReferentEmail")
    private String updateReferentEmail;

    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access19.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access46.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("updateIsUrgent")
    private String updateIsUrgent;

    @CCD(
            label = "What is the referral subject?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ReferralSubject",
            access = MultipleAccess.Access19.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "What is the referral subject?",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_ReferralSubject",
            access = MultipleAccess.Access46.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("updateReferralSubject")
    private String updateReferralSubject;

    @CCD(
            label = "Please specify",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access19.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Please specify",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access46.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("updateReferralSubjectSpecify")
    private String updateReferralSubjectSpecify;

    @CCD(
            label = "Give details of the referral",
            hint = "Explain what you're referring and why, include answers or directions you need.",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access19.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Give details of the referral",
            hint = "Explain what you're referring and why, include answers or directions you need.",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access46.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("updateReferralDetails")
    private String updateReferralDetails;

    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access25.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access50.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("updateReferralDocument")
    private List<DocumentTypeItem> updateReferralDocument;

    @CCD(
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access19.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Which instructions do you recommend?",
            hint = "Give details of directions, letter or decisions to issue.",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access46.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("updateReferralInstruction")
    private String updateReferralInstruction;

    // Referral Reply
    @CCD(
            label = "Hearing and Referral Details placeholder",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access20.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Hearing and Referral Details placeholder",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access47.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("hearingAndReferralDetails")
    private String hearingAndReferralDetails;

    @CCD(
            label = "Who are you sending these directions to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = MultipleAccess.Access20.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Who are you sending these directions to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = MultipleAccess.Access47.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("directionTo")
    private String directionTo;

    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access20.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "What is their email address?",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access47.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("replyToEmailAddress")
    private String replyToEmailAddress;

    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access20.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Is this urgent?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access47.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("isUrgentReply")
    private String isUrgentReply;

    @CCD(
            label = "What are your directions?",
            hint = "Give details of your reply to the referral and any issues you've identified",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access20.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "What are your directions?",
            hint = "Give details of your reply to the referral and any issues you've identified",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access47.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("directionDetails")
    private String directionDetails;

    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access21.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Upload document",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access48.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("replyDocument")
    private List<DocumentTypeItem> replyDocument;

    @CCD(
            label = "General notes",
            hint = "Give details.",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access20.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "General notes",
            hint = "Give details.",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access47.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("replyGeneralNotes")
    private String replyGeneralNotes;

    @CCD(
            label = "Who are you referring or replying to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = MultipleAccess.Access20.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Who are you referring or replying to?",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "frl_ReferCaseTo",
            access = MultipleAccess.Access47.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("replyTo")
    private String replyTo;

    @CCD(
            label = "Give details of your reply or referral",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access20.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Give details of your reply or referral",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access47.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("replyDetails")
    private String replyDetails;

    @CCD(
            label = "For checking if user is a judge",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access20.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "For checking if user is a judge",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access44.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("isJudge")
    private String isJudge;

    // Close Referral
    @CCD(
            label = "Hearing and Referral Details placeholder",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access23.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Hearing and Referral Details placeholder",
            typeOverride = FieldType.Text,
            access = MultipleAccess.Access44.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("closeReferralHearingDetails")
    private String closeReferralHearingDetails;

    @CCD(
            label = "Do you want to close this referral?",
            hint = "All directions must be completed before closing referrals.",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_confirmCloseReferral",
            access = MultipleAccess.Access23.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Do you want to close this referral?",
            hint = "All directions must be completed before closing referrals.",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_confirmCloseReferral",
            access = MultipleAccess.Access44.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("confirmCloseReferral")
    private List<String> confirmCloseReferral;

    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access23.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "General notes",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access44.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("closeReferralGeneralNotes")
    private String closeReferralGeneralNotes;

    // Document collections
    @CCD(
            label = "Case documentation",
            hint = "Upload documentation for the case",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access27.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Case documentation",
            hint = "Upload documentation for the case",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access53.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    private List<DocumentTypeItem> documentCollection;

    @CCD(
            label = "Documents available to Claimants",
            hint = "These are documents that claimants have access to",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access17.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Documents available to Claimants",
            hint = "These are documents that claimants have access to",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access52.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    private List<DocumentTypeItem> claimantDocumentCollection;

    @CCD(
            label = "Documents available to legal reps",
            hint = "These are documents that legal reps have access to",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access16.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Documents available to legal reps",
            hint = "These are documents that legal reps have access to",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "DocumentUpload",
            access = MultipleAccess.Access51.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    private List<DocumentTypeItem> legalrepDocumentCollection;

    // Collection of Legal Reps with access to the case
    @CCD(
            typeOverride = FieldType.Collection,
            typeParameterOverride = "SubCaseLegalRepDetails",
            access = MultipleAccess.Access15.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            typeOverride = FieldType.Collection,
            typeParameterOverride = "SubCaseLegalRepDetails",
            access = MultipleAccess.Access42.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("legalRepCollection")
    private ListTypeItem<SubCaseLegalRepDetails> legalRepCollection;

    @CCD(
            label = "Multiple notes",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "caseNote",
            access = MultipleAccess.Access10.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Multiple notes",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "caseNote",
            access = MultipleAccess.Access37.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    private List<GenericTypeItem<CaseNote>> multipleCaseNotesCollection;

    @CCD(
            label = "Note",
            typeNameOverride = "caseNote",
            access = MultipleAccess.Access10.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Note",
            typeNameOverride = "caseNote",
            access = MultipleAccess.Access37.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    private CaseNote caseNote;

    // multiplesDocumentsTabTitles
    @CCD(
            label = "### Documents",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access09.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "### Documents",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access28.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    private List<DocumentTypeItem> multiplesDocumentsTabTitle;

    @CCD(
            label = "### Claimant Documents",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access08.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "### Claimant Documents",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access36.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    private List<DocumentTypeItem> multiplesClaimantDocumentsTabTitle;

    @CCD(
            excludeFromProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    private List<DocumentTypeItem> multiplesRespondentsDocumentsTabTitle;

    // documentSelect
    @CCD(
            label = "Select a document",
            hint = "Select a document or documents",
            typeOverride = FieldType.DynamicMultiSelectList,
            access = MultipleAccess.Access17.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Select a document",
            hint = "Select a document or documents",
            typeOverride = FieldType.DynamicMultiSelectList,
            access = MultipleAccess.Access52.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    private DynamicMultiSelectListType documentSelect;

    // documentAccess
    @CCD(
            label = "Select the roles",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "DocumentAccess",
            access = MultipleAccess.Access17.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Select the roles",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "DocumentAccess",
            access = MultipleAccess.Access52.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    private String documentAccess;

    @CCD(
            label = "Multiple Note",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access10.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Multiple Note",
            typeOverride = FieldType.TextArea,
            access = MultipleAccess.Access37.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("multipleNote")
    private String multipleNote;

    @CCD(
            label = "Case Numbers",
            hint = "The lead case cannot be removed",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseNumber",
            access = MultipleAccess.Access15.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            label = "Case Numbers",
            hint = "The lead case cannot be removed",
            typeOverride = FieldType.Collection,
            typeParameterOverride = "CaseNumber",
            access = MultipleAccess.Access42.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("altCaseIdCollection")
    private List<CaseIdTypeItem> altCaseIdCollection;

    // Definition fields that were previously accepted through JsonIgnoreProperties only.
    @CCD(
            id = "  leadEthosCaseRef",
            label = "Lead ethos case reference",
            typeOverride = FieldType.Text,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class
            })
    @JsonProperty("  leadEthosCaseRef")
    private Object migrationDefinitionField01;

    @CCD(
            id = "addCasesLabel",
            label = "**Press Continue to add cases to this Multiple on the next page**",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            id = "addCasesLabel",
            label = "**Press Continue to add cases to this Multiple on the next page**",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access60.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("addCasesLabel")
    private Object migrationDefinitionField02;

    @CCD(
            id = "applicationsLinks",
            label =
                    "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/respondentTseAllApplications/respondentTseAllApplications1\">All"
                        + " applications</a> <br> <a"
                        + " href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/viewRespondentTSEApplications/viewRespondentTSEApplications1\">View"
                        + " an application</a>",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access61.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class
            })
    @JsonProperty("applicationsLinks")
    private Object migrationDefinitionField03;

    @CCD(
            id = "applicationsTab",
            label = "### Applications",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access61.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class
            })
    @JsonProperty("applicationsTab")
    private Object migrationDefinitionField04;

    @CCD(
            id = "caseCounterLabel",
            label = "Number of cases: ${caseCounter}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access13.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "caseCounterLabel",
            label = "Number of cases: ${caseCounter}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access40.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @CCD(
            id = "caseCounterLabel",
            label = "Number of cases: ${caseCounter}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {EnglandWalesMultipleProdDefinition.class})
    @CCD(
            id = "caseCounterLabel",
            label = "Number of cases: ${caseCounter}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {ScotlandMultipleProdDefinition.class})
    @JsonProperty("caseCounterLabel")
    private Object migrationDefinitionField05;

    @CCD(
            id = "closeReferralHearingDetailsLabel",
            label = "${closeReferralHearingDetails}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access24.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "closeReferralHearingDetailsLabel",
            label = "${closeReferralHearingDetails}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access45.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("closeReferralHearingDetailsLabel")
    private Object migrationDefinitionField06;

    @CCD(
            id = "componentLauncher",
            label = "Component Launcher (for displaying Case View categories)",
            typeOverride = FieldType.ComponentLauncher,
            access = MultipleAccess.Access18.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "componentLauncher",
            label = "Component Launcher (for displaying Case View categories)",
            typeOverride = FieldType.ComponentLauncher,
            access = MultipleAccess.Access53.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("componentLauncher")
    private Object migrationDefinitionField07;

    @CCD(
            id = "createDigitalCaseFileLink",
            label =
                    "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/createDcf/createDcf1\">Create"
                        + " DCF</a>",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access10.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "createDigitalCaseFileLink",
            label =
                    "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/createDcf/createDcf1\">Create"
                        + " DCF</a>",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access37.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("createDigitalCaseFileLink")
    private Object migrationDefinitionField08;

    @CCD(
            id = "customHistoryViewer",
            label = "My history",
            typeNameOverride = "CaseHistoryViewer",
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            id = "customHistoryViewer",
            label = "My history",
            typeNameOverride = "CaseHistoryViewer",
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("customHistoryViewer")
    private Object migrationDefinitionField09;

    @CCD(
            id = "dcfYesNo",
            label = "Do you want to automatically generate the DCF?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access10.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "dcfYesNo",
            label = "Do you want to automatically generate the DCF?",
            typeOverride = FieldType.YesOrNo,
            access = MultipleAccess.Access37.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("dcfYesNo")
    private Object migrationDefinitionField10;

    @CCD(
            id = "docMarkUp ",
            label = "Doc MarkUp",
            typeOverride = FieldType.Text,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                ScotlandMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("docMarkUp ")
    private Object migrationDefinitionField11;

    @CCD(
            id = "extractEventText",
            label =
                    "Submit the notification extract request below. <br><br> Once completed, the"
                        + " generated extract will be available to download on the notifications"
                        + " tab of the multiple.",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access10.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "extractEventText",
            label =
                    "Submit the notification extract request below. <br><br> Once completed, the"
                        + " generated extract will be available to download on the notifications"
                        + " tab of the multiple.",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access37.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("extractEventText")
    private Object migrationDefinitionField12;

    @CCD(
            id = "hearingAndReferralDetailsLabel",
            label = "${hearingAndReferralDetails}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access56.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "hearingAndReferralDetailsLabel",
            label = "${hearingAndReferralDetails}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access58.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("hearingAndReferralDetailsLabel")
    private Object migrationDefinitionField13;

    @CCD(
            id = "leadCaseLabel",
            label = "Lead Case: ${leadCase}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access13.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "leadCaseLabel",
            label = "Lead Case: ${leadCase}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access40.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @CCD(
            id = "leadCaseLabel",
            label = "Lead Case: ${leadCase}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {EnglandWalesMultipleProdDefinition.class})
    @CCD(
            id = "leadCaseLabel",
            label = "Lead Case: ${leadCase}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {ScotlandMultipleProdDefinition.class})
    @JsonProperty("leadCaseLabel")
    private Object migrationDefinitionField14;

    @CCD(
            id = "linkedMultipleCTLabel",
            label = "Multiple Transfer: ${linkedMultipleCT}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            id = "linkedMultipleCTLabel",
            label = "Multiple Transfer: ${linkedMultipleCT}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("linkedMultipleCTLabel")
    private Object migrationDefinitionField15;

    @CCD(
            id = "multiplesRespondentDocumentsTabTitle",
            label = "### Respondent Documents",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access06.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "multiplesRespondentDocumentsTabTitle",
            label = "### Respondent Documents",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access30.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("multiplesRespondentDocumentsTabTitle")
    private Object migrationDefinitionField16;

    @CCD(
            id = "notificationsTabLinks",
            label =
                    "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/sendNotification/sendNotification1\">Send"
                        + " a notification</a> <br> <a"
                        + " href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/extractNotifications/extractNotifications1\">Extract"
                        + " notifications</a>",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access01.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "notificationsTabLinks",
            label =
                    "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/sendNotification/sendNotification1\">Send"
                        + " a notification</a> <br> <a"
                        + " href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/extractNotifications/extractNotifications1\">Extract"
                        + " notifications</a>",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access28.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("notificationsTabLinks")
    private Object migrationDefinitionField17;

    @CCD(
            id = "positionLabel",
            label = "The position will be set to: **Case closed**",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            id = "positionLabel",
            label = "The position will be set to: **Case closed**",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access60.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("positionLabel")
    private Object migrationDefinitionField18;

    @CCD(
            id = "referralHearingDetailsLabel",
            label = "${referralHearingDetails}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access56.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "referralHearingDetailsLabel",
            label = "${referralHearingDetails}",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access58.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referralHearingDetailsLabel")
    private Object migrationDefinitionField19;

    @CCD(
            id = "referralLinks",
            label =
                    "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/createReferral/createReferral1\">Send"
                        + " a new referral</a> <br> <a"
                        + " href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/updateReferral/updateReferral1\">Update"
                        + " a referral</a> <br> <a"
                        + " href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/replyToReferral/replyToReferral1\">Reply"
                        + " to a referral</a> <br> <a"
                        + " href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/closeReferral/closeReferral1\">Close"
                        + " a referral</a>",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access57.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "referralLinks",
            label =
                    "<a href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/createReferral/createReferral1\">Send"
                        + " a new referral</a> <br> <a"
                        + " href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/updateReferral/updateReferral1\">Update"
                        + " a referral</a> <br> <a"
                        + " href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/replyToReferral/replyToReferral1\">Reply"
                        + " to a referral</a> <br> <a"
                        + " href=\"/cases/case-details/${[CASE_REFERENCE]}/trigger/closeReferral/closeReferral1\">Close"
                        + " a referral</a>",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access59.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referralLinks")
    private Object migrationDefinitionField20;

    @CCD(
            id = "referralsLabel",
            label = "**Referrals**",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access57.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "referralsLabel",
            label = "**Referrals**",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access59.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("referralsLabel")
    private Object migrationDefinitionField21;

    @CCD(
            id = "removeCasesLabel",
            label = "**Press Continue to remove cases from this Multiple**",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "removeCasesLabel",
            label = "**Press Continue to remove cases from this Multiple**",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("removeCasesLabel")
    private Object migrationDefinitionField22;

    @CCD(
            id = "sendNotificationInfo",
            label =
                    "Use this service to send a notification to parties within this multiple. You"
                        + " can do this by uploading standard letter documents.\n\n"
                        + " You can send multiple letters in one notification\n\n",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access02.class,
            includeInProfiles = {EnglandWalesMultipleCftlibDefinition.class})
    @CCD(
            id = "sendNotificationInfo",
            label =
                    "Use this service to send a notification to parties within this multiple. You"
                        + " can do this by uploading standard letter documents.\n\n"
                        + " You can send multiple letters in one notification\n\n",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access33.class,
            includeInProfiles = {ScotlandMultipleCftlibDefinition.class})
    @JsonProperty("sendNotificationInfo")
    private Object migrationDefinitionField23;

    @CCD(
            id = "stateLabel",
            label = "#### Case Status:  ${[STATE]} ",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access14.class,
            includeInProfiles = {
                EnglandWalesMultipleCftlibDefinition.class,
                EnglandWalesMultipleProdDefinition.class
            })
    @CCD(
            id = "stateLabel",
            label = "#### Case Status:  ${[STATE]} ",
            typeOverride = FieldType.Label,
            access = MultipleAccess.Access41.class,
            includeInProfiles = {
                ScotlandMultipleCftlibDefinition.class,
                ScotlandMultipleProdDefinition.class
            })
    @JsonProperty("stateLabel")
    private Object migrationDefinitionField24;
}
