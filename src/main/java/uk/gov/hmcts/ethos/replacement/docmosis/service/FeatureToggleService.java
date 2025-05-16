package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.launchdarkly.FeatureToggleApi;

@Service
public class FeatureToggleService {

    private final FeatureToggleApi featureToggleApi;

    @Autowired
    public FeatureToggleService(FeatureToggleApi featureToggleApi) {
        this.featureToggleApi = featureToggleApi;
    }

    public boolean isFeatureEnabled(String feature) {
        return this.featureToggleApi.isFeatureEnabled(feature);
    }

    public boolean isGlobalSearchEnabled() {
        return this.featureToggleApi.isFeatureEnabled("global_search_enabled");
    }

    public boolean isCaseFlagsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("case-flags-linking-enabled");
    }

    public boolean isHmcEnabled() {
        return this.featureToggleApi.isFeatureEnabled("hmc");
    }

    public boolean isBundlesEnabled() {
        return this.featureToggleApi.isFeatureEnabled("bundles");
    }

    public boolean isWorkAllocationEnabled() {
        return this.featureToggleApi.isFeatureEnabled("work-allocation");
    }

    public boolean isWelshEnabled() {
        return this.featureToggleApi.isFeatureEnabled("welsh-language");
    }

    public boolean isEccEnabled() {
        return this.featureToggleApi.isFeatureEnabled("ecc");
    }

    public boolean isMultiplesEnabled() {
        return this.featureToggleApi.isFeatureEnabled("multiples");
    }

    public boolean isEt1DocGenEnabled() {
        return this.featureToggleApi.isFeatureEnabled("et1-doc-gen");
    }

    public boolean isMul2Enabled() {
        return this.featureToggleApi.isFeatureEnabled("MUL2");
    }

    public boolean citizenEt1Generation() {
        return this.featureToggleApi.isFeatureEnabled("citizen-et1-generation");
    }

    public boolean isAcasCertificatePostSubmissionEnabled() {
        return this.featureToggleApi.isFeatureEnabled("acasCertificatePostSubmission");
    }

    public boolean isNoticeOfChangeFieldsEnabled() {
        return this.featureToggleApi.isFeatureEnabled("noticeOfChangeFields");
    }

    public boolean isPartySpacingCronEnabled() {
        return this.featureToggleApi.isFeatureEnabled("party-spacing-cron");
    }
}
