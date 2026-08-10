package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Letters", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CorrespondenceType {

    @CCD(
            label = "Top Level",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_TopLevel"
    )
    @JsonProperty("topLevel_Documents")
    private String topLevelDocuments;
    @CCD(
            label = "Part 0",
            showCondition = "topLevel_Documents=\"EM-TRB-LET-ENG-00544\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_0"
    )
    @JsonProperty("part_0_Documents")
    private String part0Documents;
    @CCD(
            label = "Part 1",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00026\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_1"
    )
    @JsonProperty("part_1_Documents")
    private String part1Documents;
    @CCD(
            label = "Part 2",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00027\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_2"
    )
    @JsonProperty("part_2_Documents")
    private String part2Documents;
    @CCD(
            label = "Part 3",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00028\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_3"
    )
    @JsonProperty("part_3_Documents")
    private String part3Documents;
    @CCD(
            label = "Part 4",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00029\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_4"
    )
    @JsonProperty("part_4_Documents")
    private String part4Documents;
    @CCD(
            label = "Part 5",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00030\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_5"
    )
    @JsonProperty("part_5_Documents")
    private String part5Documents;
    @CCD(
            label = "Part 6",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00031\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_6"
    )
    @JsonProperty("Part_6_Documents")
    private String part6Documents;
    @CCD(
            label = "Part 7",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00032\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_7"
    )
    @JsonProperty("Part_7_Documents")
    private String part7Documents;
    @CCD(
            label = "Part 8",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00065\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_8"
    )
    @JsonProperty("Part_8_Documents")
    private String part8Documents;
    @CCD(
            label = "Part 9",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00033\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_9"
    )
    @JsonProperty("Part_9_Documents")
    private String part9Documents;
    @CCD(
            label = "Part 10",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00034\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_10"
    )
    @JsonProperty("Part_10_Documents")
    private String part10Documents;
    @CCD(
            label = "Part 11",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00035\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_11"
    )
    @JsonProperty("Part_11_Documents")
    private String part11Documents;
    @CCD(
            label = "Part 12",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00036\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_12"
    )
    @JsonProperty("Part_12_Documents")
    private String part12Documents;
    @CCD(
            label = "Part 13",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00037\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_13"
    )
    @JsonProperty("Part_13_Documents")
    private String part13Documents;
    @CCD(
            label = "Part 14",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00038\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_14"
    )
    @JsonProperty("Part_14_Documents")
    private String part14Documents;
    @CCD(
            label = "Part 15",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00039\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_15"
    )
    @JsonProperty("Part_15_Documents")
    private String part15Documents;
    @CCD(
            label = "Part 16",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00040\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_16"
    )
    @JsonProperty("Part_16_Documents")
    private String part16Documents;
    @CCD(
            label = "Part 17",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00041\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_17"
    )
    @JsonProperty("Part_17_Documents")
    private String part17Documents;
    @CCD(
            label = "Part 18",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00066\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_18"
    )
    @JsonProperty("Part_18_Documents")
    private String part18Documents;
    @CCD(
            label = "Part 20",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00043\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Part_20"
    )
    @JsonProperty("Part_20_Documents")
    private String part20Documents;
    @CCD(
            label = "Hearing Number",
            showCondition = "topLevel_Documents=\"dummy\"",
            searchable = false,
            typeOverride = FieldType.Number
    )
    @JsonProperty("hearingNumber")
    private String hearingNumber;
    @CCD(
            label = "Hearing Number",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00032\" OR topLevel_Documents=\"EM-TRB-EGW-ENG-00065\" OR part_2_Documents = \"2.5\" OR part_2_Documents = \"2.7\" OR part_2_Documents = \"2.7 Reform\" OR part_2_Documents = \"2.7A\" OR part_2_Documents = \"2.7A Reform\" OR part_2_Documents = \"2.8\" OR part_2_Documents = \"2.8 Reform\" OR part_2_Documents = \"2.8A\" OR part_2_Documents = \"2.8A Reform\" OR part_4_Documents = \"4.5\" OR part_4_Documents = \"4.8A\"  OR part_5_Documents=\"5.18\" OR part_5_Documents=\"5.18A\" OR Part_7_Documents=\"7.8A\" OR Part_7_Documents=\"7.9A\" OR Part_8_Documents=\"8.1A\" OR Part_11_Documents = \"11.8\" OR Part_11_Documents = \"11.9\" OR Part_15_Documents = \"15.2\" OR Part_15_Documents = \"15.1\" OR Part_15_Documents = \"15.3\" OR Part_15_Documents = \"15.4\" OR Part_16_Documents = \"16.1\" OR Part_16_Documents = \"16.2\" OR Part_17_Documents = \"17.3\"",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    @JsonProperty("dynamicHearingNumber")
    private DynamicFixedListType dynamicHearingNumber;
    @CCD(
            label = "Respondents with ECC",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00028\" AND part_3_Documents != \"3.1\" AND part_3_Documents != \"3.2\" AND part_3_Documents != \"3.3\" AND part_3_Documents != \"3.4\" AND part_3_Documents != \"3.5\" AND part_3_Documents != \"3.6\" AND part_3_Documents != \"3.7\" AND part_3_Documents != \"3.22\" AND part_3_Documents != \"3.23\"",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    @JsonProperty("dynamicRespondentsWithEcc")
    private DynamicFixedListType dynamicRespondentsWithEcc;
    @CCD(
            label = "Address ECC letter to Claimant?",
            showCondition = "topLevel_Documents=\"EM-TRB-EGW-ENG-00028\" AND dynamicRespondentsWithEcc != \"\" AND dynamicRespondentsWithEcc != \"No respondents with ECC\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("flipRespondentAndClaimantValues")
    private String flipRespondentAndClaimantValues;
}
