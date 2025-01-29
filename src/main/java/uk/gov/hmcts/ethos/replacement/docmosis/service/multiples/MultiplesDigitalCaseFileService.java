package uk.gov.hmcts.ethos.replacement.docmosis.service.multiples;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.BundleDocument;
import uk.gov.hmcts.et.common.model.bundle.BundleDocumentDetails;
import uk.gov.hmcts.et.common.model.bundle.MultipleBundleCreateRequest;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.client.BundleApiClient;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Slf4j
@RequiredArgsConstructor
@Service("multiplesDigitalCaseFileService")
public class MultiplesDigitalCaseFileService {
    private final AuthTokenGenerator authTokenGenerator;
    private final BundleApiClient bundleApiClient;

    /**
     * Creates a request to create a case file.
     *
     * @param caseData data
     * @return list of bundles
     */
    public List<Bundle> createCaseFileRequest(MultipleData caseData) {
        log.info("setting bundle config");
        setBundleConfig(caseData);
        return createBundleData(caseData);
    }

    /**
     * Creates a case file.
     * @param caseDetails data
     * @param userToken token
     * @return list of bundles
     */
    public List<Bundle> stitchCaseFile(MultipleDetails caseDetails, String userToken) {
        log.info("Stitching case File");
        setBundleConfig(caseDetails.getCaseData());
        if (CollectionUtils.isEmpty(caseDetails.getCaseData().getCaseBundles())) {
            caseDetails.getCaseData().setCaseBundles(createBundleData(caseDetails.getCaseData()));
        }

        BundleCreateResponse bundleCreateResponse = stitchCaseFile(userToken, authTokenGenerator.generate(),
                bundleRequestMapper(caseDetails));
        log.info("Completed stitching case File");
        return bundleCreateResponse.getData().getCaseBundles();
    }

    private BundleCreateResponse stitchCaseFile(String authorization, String serviceAuthorization,
                                                MultipleBundleCreateRequest bundleCreateRequest) {
        log.info("About to call bundle api client");
        return bundleApiClient.stitchMultipleBundle(authorization, serviceAuthorization, bundleCreateRequest);
    }

    private MultipleBundleCreateRequest bundleRequestMapper(MultipleDetails multipleDetails) {
        return MultipleBundleCreateRequest.builder()
                .multipleDetails(multipleDetails)
                .caseTypeId(multipleDetails.getCaseTypeId())
                .build();
    }

    private List<Bundle> createBundleData(MultipleData caseData) {
        Bundle bundle = Bundle.builder()
                .value(createBundleDetails(caseData))
                .build();
        return List.of(bundle);
    }

    private void setBundleConfig(MultipleData caseData) {
        if (isNullOrEmpty(caseData.getBundleConfiguration())) {
            caseData.setBundleConfiguration("et-dcf-2.yaml");
        }
    }

    private BundleDetails createBundleDetails(MultipleData caseData) {
        List<BundleDocumentDetails> caseDocs = DigitalCaseFileHelper.getDocsForDcf(caseData);
        List<BundleDocument> bundleDocuments = caseDocs.stream()
                .map(bundleDocumentDetails -> BundleDocument.builder()
                        .value(bundleDocumentDetails)
                        .build())
                .toList();
        return BundleDetails.builder()
                .id(UUID.randomUUID().toString())
                .title("ET - DCF")
                .eligibleForStitching(YES)
                .eligibleForCloning(NO)
                .fileName(caseData.getMultipleReference() + "-DCF")
                .hasTableOfContents(NO)
                .pageNumberFormat("numberOfPages")
                .documents(bundleDocuments)
                .build();
    }
}
