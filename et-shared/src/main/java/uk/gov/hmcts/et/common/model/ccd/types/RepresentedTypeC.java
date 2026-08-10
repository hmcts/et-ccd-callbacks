package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ClaimantRepresentative", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepresentedTypeC {
    @CCD(label = " ", showCondition = "representative_occupation=\"dummy\"")
    @JsonProperty("representative_id")
    private String representativeId;
    @CCD(label = "Name of Representative")
    @JsonProperty("name_of_representative")
    private String nameOfRepresentative;
    @CCD(label = "Name of Organisation")
    @JsonProperty("name_of_organisation")
    private String nameOfOrganisation;
    @CCD(label = "Reference", searchable = false)
    @JsonProperty("representative_reference")
    private String representativeReference;
    @CCD(
            label = "Occupation",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_RepresentativeOccupation"
    )
    @JsonProperty("representative_occupation")
    private String representativeOccupation;
    @CCD(
            label = "What is the Representative's occupation?",
            showCondition = "representative_occupation=\"Other\"",
            searchable = false
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
    /** UUID for identifying legal rep's firm. */
    @CCD(label = " ", showCondition = "representative_id=\"dummy\"")
    @JsonProperty("organisationId")
    private String organisationId;
    @CCD(label = "MyHMCTS Organisation")
    @JsonProperty("myHmctsOrganisation")
    private Organisation myHmctsOrganisation;
    @CCD(
            label = "Hearing Language",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_languages"
    )
    @JsonProperty("hearingContactLanguage")
    private List<String> hearingContactLanguage;
    @CCD(
            label = "Contact Language",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "fl_languages"
    )
    @JsonProperty("contactLanguageQuestion")
    private List<String> contactLanguageQuestion;
    @CCD(
            label = "Hearing Attendence",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "msl_HearingAttendence"
    )
    @JsonProperty("representativeAttendHearing")
    private List<String> representativeAttendHearing;
    @CCD(label = "Organisation users")
    @JsonProperty("organisationUsers")
    private List<GenericTypeItem<OrganisationUsersIdamUser>> organisationUsers;
}
