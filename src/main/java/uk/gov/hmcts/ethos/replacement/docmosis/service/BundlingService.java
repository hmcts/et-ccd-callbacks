package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.clients.BundleApiClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

@Service
public class BundlingService {

    @Autowired
    private BundleApiClient bundleApiClient;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Value("${bundle.config.default}")
    private String defaultBundle;

    public List<Bundle> createBundleRequest(CaseDetails caseDetails, String userToken) {
        setBundleConfig(caseDetails.getCaseData());
        BundleCreateResponse bundleCreateResponse = createBundle(userToken, authTokenGenerator.generate(),
                bundleRequestMapper(caseDetails));
        return bundleCreateResponse.getData().getCaseBundles();
    }

    private BundleCreateResponse createBundle(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) {
        return bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
    }

    private BundleCreateRequest bundleRequestMapper(CaseDetails caseDetails) {
        return BundleCreateRequest.builder()
                .caseDetails(caseDetails)
                .caseTypeId(caseDetails.getCaseTypeId())
                .build();
    }

    private void setBundleConfig(CaseData caseData) {
        if (isNullOrEmpty(caseData.getBundleConfiguration())) {
            caseData.setBundleConfiguration(defaultBundle);
        }
    }
}
