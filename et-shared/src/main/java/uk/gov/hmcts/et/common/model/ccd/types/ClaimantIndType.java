package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "ClaimantIndividual", generate = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ClaimantIndType {

    @CCD(
            label = "Title",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_PreferredTitle"
    )
    @JsonProperty("claimant_preferred_title")
    private String claimantPreferredTitle;
    @CCD(
            label = "Deprecated",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_Title"
    )
    @JsonProperty("claimant_title1")
    private String claimantTitle;
    @CCD(label = "Other title", showCondition = "claimant_preferred_title=\"Other\"", searchable = false)
    @JsonProperty("claimant_title_other")
    private String claimantTitleOther;
    @CCD(label = "First Name")
    @JsonProperty("claimant_first_names")
    private String claimantFirstNames;
    @CCD(label = "Last Name")
    @JsonProperty("claimant_last_name")
    private String claimantLastName;
    @CCD(label = "Date of birth", searchable = false, typeOverride = FieldType.Date)
    @JsonProperty("claimant_date_of_birth")
    private String claimantDateOfBirth;
    @CCD(label = "Gender", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "fl_Gender")
    @JsonProperty("claimant_gender")
    private String claimantGender;
    @CCD(label = "Sex", searchable = false, typeOverride = FieldType.FixedList, typeParameterOverride = "fl_Sex")
    @JsonProperty("claimant_sex")
    private String claimantSex;
    @CCD(
            label = "Is the claimant's identity and sex registered at birth the same?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "fl_YesOrNoOrPreferNot"
    )
    @JsonProperty("claimant_gender_identity_same")
    private String claimantGenderIdentitySame;
    @CCD(label = "Gender Identity description", searchable = false)
    @JsonProperty("claimant_gender_identity")
    private String claimantGenderIdentity;

    private static final String OTHER = "Other";
    private static final String PREFER_NOT_TO_SAY = "Prefer not to say";

    public String claimantFullNames() {
        String title = getTitle();

        var fullNameList = List.of(title, claimantFirstNames, claimantLastName);
        return String.join(" ", notNullOrEmptyAtt(new ArrayList<>(), fullNameList));
    }

    /**
     * Used to return claimant's full name with his/her title
     * Used while creating hearings, reports and docmosis documents.
     * @return claimant's full name
     */
    public String claimantFullName() {
        String title = getTitle();

        var fullNameList = List.of(title, getInitials(), claimantLastName);
        return String.join(" ", notNullOrEmptyAtt(new ArrayList<>(), fullNameList));
    }

    /**
     * Used to return title according to following conditions:
     * 1. If claimantTitle is not null or empty and not Other, then returns claimant title
     * 2. If claimantTitle is Other and claimantTitleOther is not null then returns claimantTitleOther
     * 3. If claimantTitle is null or empty and claimantPreferredTitle is not null or empty and not Other, then returns
     *    claimantPreferredTitle.
     * 4. If claimantTitle is null or empty and claimantPreferredTitle is Other and claimantTitleOther is not null then
     *    returns claimantTitleOther
     * 5. In any other case returns an empty string as ""
     * @return title according to conditions defined above
     */
    private String getTitle() {
        // When Ecm legacy Title is set rather than the Reform ET Title field
        if (!Strings.isNullOrEmpty(trimStringValue(claimantTitle))) {
            return getCorrectClaimantTitle(claimantTitle);
        }

        // When the Reform ET Title field is set rather than the Ecm legacy Title
        if (!Strings.isNullOrEmpty(trimStringValue(claimantPreferredTitle))) {
            return getCorrectClaimantTitle(claimantPreferredTitle);
        }

        // Fallback default
        return "";
    }

    private String getCorrectClaimantTitle(String currentTitle) {
        //Title is set as expected
        String adjustedClaimantTitle = trimStringValue(currentTitle);
        if (!Strings.isNullOrEmpty(adjustedClaimantTitle)
            && !OTHER.equals(adjustedClaimantTitle)
            && (!PREFER_NOT_TO_SAY.equals(adjustedClaimantTitle))) {
            return adjustedClaimantTitle;
        }

        //If title is Other & custom title is added
        if (OTHER.equals(trimStringValue(currentTitle))
            && (!Strings.isNullOrEmpty(trimStringValue(claimantTitleOther)))) {
            return claimantTitleOther;
        }

        //If title is "Prefer Not to Say" or else as fallback default
        return "";
    }

    /**
     * Implemented to ignore blank title values.
     * @param val string value to be trimmed
     * @return trimmed string value or null
     */
    private String trimStringValue(String val) {
        return val == null ? null : val.trim();
    }

    private String getInitials() {
        if (!isNullOrEmpty(claimantFirstNames)) {
            return Arrays.stream(claimantFirstNames.split(" "))
                    .map(str -> str.substring(0, 1))
                    .collect(Collectors.joining(" "));
        }
        return "";
    }

    private List<String> notNullOrEmptyAtt(List<String> fullClaimantName, List<String> attributes) {
        for (String aux : attributes) {
            if (!isNullOrEmpty(aux)) {
                fullClaimantName.add(aux);
            }
        }
        return fullClaimantName;
    }
}
