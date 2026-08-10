package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "RespondentRepresentative", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepresentedTypeR {
    @CCD(ignore = true)
    @JsonProperty("id")
    private String id;
    @CCD(label = "Respondent Id", showCondition = "resp_rep_name=\"dummy\"")
    @JsonProperty("respondentId")
    private String respondentId;
    @CCD(label = "Respondent who is being represented", typeOverride = FieldType.DynamicList)
    @JsonProperty("dynamic_resp_rep_name")
    private DynamicFixedListType dynamicRespRepName;
    @CCD(
            label = "Respondent who is being represented",
            showCondition = "representative_occupation=\"dummy\" OR dynamic_resp_rep_name!=\"*\"",
            retainHiddenValue = true
    )
    @JsonProperty("resp_rep_name") // Respondent who is being represented
    private String respRepName;
    @CCD(label = "Name of Representative")
    @JsonProperty("name_of_representative")
    private String nameOfRepresentative;
    @CCD(label = "Name of Organisation")
    @JsonProperty("name_of_organisation")
    private String nameOfOrganisation;
    @CCD(label = "Reference", showCondition = "myHmctsYesNo=\"No\"", searchable = false)
    @JsonProperty("representative_reference")
    private String representativeReference;
    @CCD(
            label = "Occupation",
            showCondition = "myHmctsYesNo=\"No\"",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_RepresentativeOccupation"
    )
    @JsonProperty("representative_occupation")
    private String representativeOccupation;
    @CCD(
            label = "What is the Representative's occupation?",
            showCondition = "representative_occupation=\"Other\"",
            searchable = false,
            retainHiddenValue = true
    )
    @JsonProperty("representative_occupation_other")
    private String representativeOccupationOther;
    @CCD(label = "Address", searchable = false, typeOverride = FieldType.AddressUK)
    @JsonProperty("representative_address")
    private Address representativeAddress;
    @CCD(label = "Phone number", searchable = false, typeOverride = FieldType.PhoneUK)
    @JsonProperty("representative_phone_number")
    private String representativePhoneNumber;
    @CCD(label = "Alternative number", searchable = false, typeOverride = FieldType.PhoneUK)
    @JsonProperty("representative_mobile_number")
    private String representativeMobileNumber;
    @CCD(label = "Email address", searchable = false, typeOverride = FieldType.Email)
    @JsonProperty("representative_email_address")
    private String representativeEmailAddress;
    @CCD(
            label = "Contact preference",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_RepresentativeContact"
    )
    @JsonProperty("representative_preference")
    private String representativePreference;
    @CCD(label = "Representative contact preference reason", searchable = false)
    @JsonProperty("representative_preference_reason")
    private String representativePreferenceReason;
    @CCD(label = "MyHMCTS Organisation", showCondition = "myHmctsYesNo=\"Yes\"")
    @JsonProperty("respondentOrganisation")
    private Organisation respondentOrganisation;
    @CCD(
            label = "Does the representative have a MyHMCTS account?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("myHmctsYesNo")
    private String myHmctsYesNo;
    /** UUID for identifying the non system user legal rep's organisation for HMC. */
    @CCD(label = " ", showCondition = "myHmctsYesNo=\"dummy\"")
    @JsonProperty("nonMyHmctsOrganisationId")
    private String nonMyHmctsOrganisationId;
    @CCD(
            label = "Representative contact language",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_languages"
    )
    @JsonProperty("representativeContactLanguage")
    private String representativeContactLanguage;
    @CCD(label = "Organisation users")
    @JsonProperty("organisationUsers")
    private List<GenericTypeItem<OrganisationUsersIdamUser>> organisationUsers;
    @CCD(label = "Respondent representative role", searchable = false)
    @JsonProperty("role")
    private String role;
    @CCD(label = "Respondent representative idam id", showCondition = "role=\"dummy\"", searchable = false)
    @JsonProperty("idamId")
    private String idamId;
}
