package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides methods to validate claimant hearing preferences.
 */
public class ClaimantHearingPreferencesValidator {

    static final String VIDEO_PREFERENCE = "Video";
    static final String PHONE_PREFERENCE = "Phone";
    static final String NEITHER_PREFERENCE = "Neither";
    static final String INVALID_HEARING_PREFERENCE_OPTIONS_MESSAGE =
            "Hearing preferences cannot contain Neither and another option";

    private ClaimantHearingPreferencesValidator() {
        // All access through static methods
    }

    /**
     * Validates that hearing preferences do not contain an incorrect combination of options.
     * If hearing preferences contain a Neither option then they should not contain any other option.
     *
     * @param  caseData the case data containing the hearing preferences to be validated
     * @return List of validation errors or an empty list if no validation errors exist
     */
    public static List<String> validateHearingPreferenceOptions(Et1CaseData caseData) {
        List<String> validationErrors = new ArrayList<>();

        List<String> options = getHearingPreferenceOptions(caseData.getClaimantHearingPreference());
        if (options.contains(NEITHER_PREFERENCE) && options.size() > 1) {
            validationErrors.add(INVALID_HEARING_PREFERENCE_OPTIONS_MESSAGE);
        }

        return validationErrors;
    }

    private static List<String> getHearingPreferenceOptions(ClaimantHearingPreference claimantHearingPreference) {
        if (claimantHearingPreference != null
                && CollectionUtils.isNotEmpty(claimantHearingPreference.getHearingPreferences())) {
            return claimantHearingPreference.getHearingPreferences();
        } else {
            return Collections.emptyList();
        }
    }
}
