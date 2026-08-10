package uk.gov.hmcts.et.common.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "etICHearingNotListedUDLHearing", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EtIcudlHearing {
    @CCD(label = "EJ sit alone?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("etICEJSitAlone")
    private String etIcejSitAlone;
    @CCD(ignore = true)
    @JsonProperty("etICIssueStandardDirections")
    private String etICIssueStandardDirections;
    @CCD(ignore = true)
    @JsonProperty("etICUdlGiveReasonsIsd")
    private String etICUdlGiveReasonsIsd;

    /* Candidate for removable - Start */
    /* These fields are not used in the configs
    *  Left in to prevent possible breaking change */
    @CCD(
            label = "Give reasons",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICUDLGiveReasons"
    )
    @JsonProperty("etICUDLGiveReasons")
    private List<String> etIcudlGiveReasons;
    @CCD(
            label = "Likelihood of dispute on facts makes full tribunal desirable",
            showCondition = "etICUDLGiveReasons CONTAINS \"Likelihood of dispute on facts makes full tribunal desirable\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICUDLDisputeOnFacts")
    private String etIcudlDisputeOnFacts;
    @CCD(
            label = "Little or no agreement on facts",
            showCondition = "etICUDLGiveReasons CONTAINS \"Little or no agreement on facts\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICUDLLittleOrNoAgreement")
    private String etIcudlLittleOrNoAgreement;
    @CCD(
            label = "Likelihood of issue of law arising makes EJSA desirable",
            showCondition = "etICUDLGiveReasons CONTAINS \"Likelihood of issue of law arising makes EJSA desirable\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICUDLIssueOfLawArising")
    private String etIcudlIssueOfLawArising;
    @CCD(
            label = "Views of parties",
            showCondition = "etICUDLGiveReasons CONTAINS \"Views of parties\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICUDLViewsOfParties")
    private String etIcudlViewsOfParties;
    @CCD(
            label = "No views expressed by parties",
            showCondition = "etICUDLGiveReasons CONTAINS \"No views expressed by parties\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICUDLNoViewsExpressedByParties")
    private String etIcudlNoViewsExpressedByParties;
    @CCD(
            label = "Concurrent proceedings",
            showCondition = "etICUDLGiveReasons CONTAINS \"Concurrent proceedings\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICUDLConcurrentProceedings")
    private String etIcudlConcurrentProceedings;
    @CCD(
            label = "Other",
            showCondition = "etICUDLGiveReasons CONTAINS \"Other\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("etICUDLOther")
    private String etIcudlOther;
    /* Candidate for removable - end */

    @CCD(
            label = "Hearing format",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_etICUDLHearingFormat"
    )
    @JsonProperty("etICUDLHearFormat")
    private String etIcudlHearFormat;
    @CCD(
            label = "Issue standard Video orders when listed",
            showCondition = "etICUDLHearFormat = \"Video hearing\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICUDLCVPIssue"
    )
    @JsonProperty("etICUDLCVPIssue")
    private List<String> etIcudlCvpIssue;
    @CCD(
            label = "Issue standard orders when listed",
            showCondition = "etICUDLHearFormat = \"Final F2F hearings (not Aberdeen)\"",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_etICUDLFinalF2FIssue"
    )
    @JsonProperty("etICUDLFinalF2FIssue")
    private List<String> etIcudlFinalF2FIssue;
    @CCD(label = "BU to check compliance with orders?", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("etICBUCheckComplianceOrders")
    private String etIcbuCheckComplianceOrders;
}
