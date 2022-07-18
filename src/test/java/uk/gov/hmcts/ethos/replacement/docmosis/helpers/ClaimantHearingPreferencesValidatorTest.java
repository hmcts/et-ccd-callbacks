package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantHearingPreferencesValidator.INVALID_HEARING_PREFERENCE_OPTIONS_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantHearingPreferencesValidator.NEITHER_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantHearingPreferencesValidator.PHONE_PREFERENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantHearingPreferencesValidator.VIDEO_PREFERENCE;

class ClaimantHearingPreferencesValidatorTest {

    @Test
    void nullHearingPreferencesProducesNoValidationErrors() {
        Et1CaseData caseData = new Et1CaseData();

        assertThat(ClaimantHearingPreferencesValidator.validateHearingPreferenceOptions(caseData))
                .isEmpty();
    }

    @Test
    void nullHearingPreferencesOptionsProducesNoValidationErrors() {
        Et1CaseData caseData = new Et1CaseData();
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());

        assertThat(ClaimantHearingPreferencesValidator.validateHearingPreferenceOptions(caseData))
                .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideValidHearingPreferenceOptions")
    void validOptionsProducesNoValidationErrors(List<String> options) {
        Et1CaseData caseData = new Et1CaseData();
        ClaimantHearingPreference preferences = new ClaimantHearingPreference();
        preferences.setHearingPreferences(options);
        caseData.setClaimantHearingPreference(preferences);

        assertThat(ClaimantHearingPreferencesValidator.validateHearingPreferenceOptions(caseData))
                .isEmpty();
    }

    private static Stream<Arguments> provideValidHearingPreferenceOptions() {
        return Stream.of(
                Arguments.of(Collections.emptyList()),
                Arguments.of(List.of(NEITHER_PREFERENCE)),
                Arguments.of(List.of(VIDEO_PREFERENCE)),
                Arguments.of(List.of(PHONE_PREFERENCE)),
                Arguments.of(List.of(VIDEO_PREFERENCE, PHONE_PREFERENCE))
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidHearingPreferenceOptions")
    void invalidOptionsProducesValidationError(List<String> options) {
        Et1CaseData caseData = new Et1CaseData();
        ClaimantHearingPreference preferences = new ClaimantHearingPreference();
        preferences.setHearingPreferences(options);
        caseData.setClaimantHearingPreference(preferences);

        assertThat(ClaimantHearingPreferencesValidator.validateHearingPreferenceOptions(caseData))
                .isEqualTo(List.of(INVALID_HEARING_PREFERENCE_OPTIONS_MESSAGE));
    }

    private static Stream<Arguments> provideInvalidHearingPreferenceOptions() {
        return Stream.of(
                Arguments.of(List.of(NEITHER_PREFERENCE, VIDEO_PREFERENCE)),
                Arguments.of(List.of(NEITHER_PREFERENCE, PHONE_PREFERENCE)),
                Arguments.of(List.of(NEITHER_PREFERENCE, VIDEO_PREFERENCE, PHONE_PREFERENCE))
        );
    }
}
