package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "BFActions", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class BFActionType {

    @CCD(ignore = true)
    @JsonProperty("action")
    private DynamicFixedListType action;
    @CCD(
            label = "Description",
            showCondition = "imported != \"Yes\" OR letters != \"Yes\"",
            retainHiddenValue = true,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_BFActionsCW"
    )
    @JsonProperty("cwActions")
    private String cwActions;
    @CCD(label = "Date/Time")
    @JsonProperty("dateEntered")
    private String dateEntered;
    @CCD(label = "Migrated from Ethos", typeOverride = FieldType.YesOrNo)
    @JsonProperty("imported")
    private String imported;
    @CCD(label = "From Letter generation", typeOverride = FieldType.YesOrNo)
    @JsonProperty("letters")
    private String letters;
    @CCD(
            label = "Action",
            showCondition = "(imported = \"Yes\" OR letters = \"Yes\") OR allActions = \"ECC served\"",
            retainHiddenValue = true
    )
    @JsonProperty("allActions")
    private String allActions;
    @CCD(label = "B/F Date", typeOverride = FieldType.Date)
    @JsonProperty("bfDate")
    private String bfDate;
    @CCD(label = "Date Cleared", typeOverride = FieldType.Date)
    @JsonProperty("cleared")
    private String cleared;
    @CCD(label = "Comments", typeOverride = FieldType.TextArea)
    @JsonProperty("notes")
    private String notes;
    //flag to track if a wa task is already created for an expired bf due date
    @CCD(label = "Is wa task created?", typeOverride = FieldType.YesOrNo)
    @JsonProperty("isWaTaskCreated")
    private String isWaTaskCreated;
}
