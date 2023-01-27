package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.bundle.Bundle;
import uk.gov.hmcts.et.common.model.ccd.bundle.BundleCreateRequest;
import uk.gov.hmcts.et.common.model.ccd.bundle.BundleCreateResponse;
import uk.gov.hmcts.et.common.model.ccd.bundle.BundleDocument;
import uk.gov.hmcts.et.common.model.ccd.bundle.BundleDocumentDetails;
import uk.gov.hmcts.et.common.model.ccd.bundle.BundleFolder;
import uk.gov.hmcts.ethos.replacement.docmosis.clients.BundleApiClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

@Service
@Slf4j
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

        for (Bundle bundle : bundleCreateResponse.getData().getCaseBundles()) {
            for (BundleFolder bundleFolder : bundle.getValue().getFolders()) {
                int count = 0;
                for (BundleDocument bundleDocument : bundleFolder.getValue().getDocuments()) {
                    BundleDocumentDetails bundleDocumentDetails = BundleDocumentDetails.builder()
                            .name(bundleDocument.getValue().getName().substring(0,
                                    bundleDocument.getValue().getName().lastIndexOf('.')))
                            .sourceDocument(bundleDocument.getValue().getSourceDocument())
                            .sortIndex(count)
                            .build();
                    count++;
                    bundleDocument.toBuilder().value(bundleDocumentDetails).build();
                    log.info(bundle.toString());
                }
            }
        }

        return bundleCreateResponse.getData().getCaseBundles();
    }

    private BundleCreateResponse createBundle(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) {
        BundleCreateResponse bundleServiceRequest =
                bundleApiClient.createBundleServiceRequest(authorization, serviceAuthorization, bundleCreateRequest);
        log.info(String.valueOf(bundleServiceRequest));
        return bundleServiceRequest;
    }

    public List<Bundle> stitchBundle(CaseDetails caseDetails, String userToken) {
        setBundleConfig(caseDetails.getCaseData());
        BundleCreateResponse bundleCreateResponse = stitchBundle(userToken, authTokenGenerator.generate(),
                bundleRequestMapper(caseDetails));
        return bundleCreateResponse.getData().getCaseBundles();
    }

    private BundleCreateResponse stitchBundle(String authorization, String serviceAuthorization,
                                              BundleCreateRequest bundleCreateRequest) {
        BundleCreateResponse bundleServiceRequest =
                bundleApiClient.stitchBundle(authorization, serviceAuthorization, bundleCreateRequest);
        log.info(String.valueOf(bundleServiceRequest));
        return bundleServiceRequest;
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
