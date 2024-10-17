package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.BundleDocument;
import uk.gov.hmcts.et.common.model.bundle.BundleDocumentDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.client.BundleApiClient;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DIGITAL_CASE_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOC_OPENS_IN_NEW_TAB_MARK_UP;

@RequiredArgsConstructor
@Service
public class DigitalCaseFileService {
    private final AuthTokenGenerator authTokenGenerator;
    private final BundleApiClient bundleApiClient;

    @Value("${em-ccd-orchestrator.config.default}")
    private String defaultBundle;

    /**
     * Creates a request to create a case file.
     * @param caseData data
     * @return list of bundles
     */
    public List<Bundle> createCaseFileRequest(CaseData caseData) {
        setBundleConfig(caseData);
        return createBundleData(caseData);
    }

    /**
     * Creates a case file.
     * @param caseDetails data
     * @param userToken token
     * @return list of bundles
     */
    public List<Bundle> stitchCaseFile(CaseDetails caseDetails, String userToken) {
        setBundleConfig(caseDetails.getCaseData());
        if (CollectionUtils.isEmpty(caseDetails.getCaseData().getCaseBundles())) {
            caseDetails.getCaseData().setCaseBundles(createBundleData(caseDetails.getCaseData()));
        }
        BundleCreateResponse bundleCreateResponse = stitchCaseFile(userToken, authTokenGenerator.generate(),
                bundleRequestMapper(caseDetails));
        return bundleCreateResponse.getData().getCaseBundles();
    }

    private BundleCreateResponse stitchCaseFile(String authorization, String serviceAuthorization,
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

    private List<Bundle> createBundleData(CaseData caseData) {
        Bundle bundle = Bundle.builder()
                .value(createBundleDetails(caseData))
                .build();
        return List.of(bundle);
    }

    private BundleDetails createBundleDetails(CaseData caseData) {
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
                .fileName(caseData.getEthosCaseReference().replace("/", "-") + "-DCF")
                .hasTableOfContents(NO)
                .pageNumberFormat("numberOfPages")
                .documents(bundleDocuments)
                .build();
    }

    /**
     * Prepare wordings to display digitalCaseFile link.
     * @param caseData Get caseData
     * @return Link with Markup
     */
    public String getReplyToReferralDCFLink(CaseData caseData) {
        if (caseData.getDigitalCaseFile() != null) {
            return formatReplyToReferralDCFLink(caseData.getDigitalCaseFile().getUploadedDocument());
        }

        return emptyIfNull(caseData.getDocumentCollection())
            .stream()
            .filter(d -> defaultIfEmpty(d.getValue().getTypeOfDocument(), "").equals(TRIBUNAL_CASE_FILE)
                || defaultIfEmpty(d.getValue().getMiscDocuments(), "").equals(TRIBUNAL_CASE_FILE))
            .map(d -> formatReplyToReferralDCFLink(d.getValue().getUploadedDocument()))
            .collect(Collectors.joining());
    }

    private String formatReplyToReferralDCFLink(UploadedDocumentType uploadedDocumentType) {
        String documentBinaryUrl = uploadedDocumentType.getDocumentBinaryUrl();
        String link = documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
        return String.format(DOC_OPENS_IN_NEW_TAB_MARK_UP, link, DIGITAL_CASE_FILE);
    }
}

