package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.launchdarkly.FeatureToggleApi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeatureToggleServiceTest {

    @Mock
    private FeatureToggleApi featureToggleApi;

    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        featureToggleService = new FeatureToggleService(featureToggleApi);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenGlobalSearchIsEnabled(Boolean toggleStat) {
        var caseFileKey = "global_search_enabled";
        givenToggle(caseFileKey, toggleStat);

        assertThat(featureToggleService.isGlobalSearchEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenAnyFeatureIsEnabled(Boolean toggleStat) {
        var caseFileKey = "any-feature";
        givenToggle(caseFileKey, toggleStat);

        assertThat(featureToggleService.isFeatureEnabled(caseFileKey)).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenCaseFlagsLinkingIsEnabled(Boolean toggleStat) {
        givenToggle("case-flags-linking-enabled", toggleStat);

        assertThat(featureToggleService.isCaseFlagsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenBundlesIsEnabled(Boolean toggleStat) {
        givenToggle("bundles", toggleStat);

        assertThat(featureToggleService.isBundlesEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenHmcIsEnabled(Boolean toggleStat) {
        givenToggle("hmc", toggleStat);

        assertThat(featureToggleService.isHmcEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenWorkAllocationIsEnabled(Boolean toggleStat) {
        givenToggle("work-allocation", toggleStat);

        assertThat(featureToggleService.isWorkAllocationEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenWelshIsEnabled(Boolean toggleStat) {
        givenToggle("welsh-language", toggleStat);

        assertThat(featureToggleService.isWelshEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenEccIsEnabled(Boolean toggleStat) {
        givenToggle("ecc", toggleStat);

        assertThat(featureToggleService.isEccEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenMultiplesIsEnabled(Boolean toggleStat) {
        givenToggle("multiples", toggleStat);

        assertThat(featureToggleService.isMultiplesEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenEt1DocGenIsEnabled(Boolean toggleStat) {
        givenToggle("et1-doc-gen", toggleStat);
        assertThat(featureToggleService.isEt1DocGenEnabled()).isEqualTo(toggleStat);
    }
  
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenMultiples2IsEnabled(Boolean toggleStat) {
        givenToggle("MUL2", toggleStat);

        assertThat(featureToggleService.isMul2Enabled()).isEqualTo(toggleStat);

    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenNoticeOfChangeFieldsIsEnabled(Boolean toggleStat) {
        givenToggle("noticeOfChangeFields", toggleStat);
        assertThat(featureToggleService.isNoticeOfChangeFieldsEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenPartySpacingCronIsEnabled(Boolean toggleStat) {
        givenToggle("party-spacing-cron", toggleStat);
        assertThat(featureToggleService.isPartySpacingCronEnabled()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnCorrectValue_whenAcasCertificateCronIsEnabled(Boolean toggleStat) {
        givenToggle("acasCertificatePostSubmission", toggleStat);
        assertThat(featureToggleService.isAcasCertificatePostSubmissionEnabled()).isEqualTo(toggleStat);
    }

    @ValueSource(booleans = {true, false})
    void returnsCorrectValue_whenCitizenEt1GenerationIsEnabled(Boolean toggleStat) {
        givenToggle("citizen-et1-generation", toggleStat);
        assertThat(featureToggleService.citizenEt1Generation()).isEqualTo(toggleStat);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void returnsCorrectValue_whenWaTaskForExpiredBfActionsIsEnabled(Boolean toggleStat) {
        givenToggle("waTaskExpiredBfActions", toggleStat);
        assertThat(featureToggleService.isWaTaskForExpiredBfActionsEnabled()).isEqualTo(toggleStat);
    }

    private void givenToggle(String feature, boolean state) {
        when(featureToggleApi.isFeatureEnabled(feature)).thenReturn(state);
    }
}
