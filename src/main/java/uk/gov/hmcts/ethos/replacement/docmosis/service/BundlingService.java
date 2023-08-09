package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.client.BundleApiClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@RequiredArgsConstructor
@Service
public class BundlingService {

    private BundleApiClient bundleApiClient;

    private AuthTokenGenerator authTokenGenerator;

    @Value("${bundle.config.default}")
    private String defaultBundle;

    public BundlingService(BundleApiClient bundleApiClient, AuthTokenGenerator authTokenGenerator) {
        this.bundleApiClient = bundleApiClient;
        this.authTokenGenerator = authTokenGenerator;
    }

    public List<Bundle> createBundleRequest(CaseDetails caseDetails, String userToken) {
        setBundleConfig(caseDetails.getCaseData());
        BundleCreateResponse bundleCreateResponse = createBundle(userToken, authTokenGenerator.generate(),
                bundleRequestMapper(caseDetails));
        setCustomBundleValues(caseDetails, bundleCreateResponse);
        return bundleCreateResponse.getData().getCaseBundles();
    }

    private static void setCustomBundleValues(CaseDetails caseDetails, BundleCreateResponse bundleCreateResponse) {
        for (Bundle bundle : bundleCreateResponse.getData().getCaseBundles()) {
            bundle.getValue().setEligibleForStitching(YES);
            bundle.getValue().setFileName(
                    caseDetails.getCaseData().getEthosCaseReference().replace("/", "-") + "-DCF");
        }
    }

    private BundleCreateResponse createBundle(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) {
        return bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
    }

    public List<Bundle> stitchBundle(CaseDetails caseDetails, String userToken) {
        setBundleConfig(caseDetails.getCaseData());
        BundleCreateResponse bundleCreateResponse = stitchBundle(userToken, authTokenGenerator.generate(),
                bundleRequestMapper(caseDetails));
        return bundleCreateResponse.getData().getCaseBundles();
    }

    private BundleCreateResponse stitchBundle(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) {
        return bundleApiClient.stitchBundle(authorization, serviceAuthorization, bundleCreateRequest);
    }

    private BundleCreateRequest bundleRequestMapper(CaseDetails caseDetails) {
        return BundleCreateRequest.builder()
                .caseDetails(caseDetails)
                .caseTypeId(caseDetails.getCaseTypeId())
                .build();
    }

    /**
     * Sets the default bundle config is none is present.
     * @param caseData data
     */
    public void setBundleConfig(CaseData caseData) {
        if (isNullOrEmpty(caseData.getBundleConfiguration())) {
            caseData.setBundleConfiguration(defaultBundle);
        }
    }
}

