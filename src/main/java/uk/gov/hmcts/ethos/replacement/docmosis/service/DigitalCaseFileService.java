package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bundle.Bundle;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.et.common.model.bundle.BundleDetails;
import uk.gov.hmcts.et.common.model.bundle.BundleDocument;
import uk.gov.hmcts.et.common.model.bundle.BundleDocumentDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.client.BundleApiClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DIGITAL_CASE_FILE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.DOC_OPENS_IN_NEW_TAB_MARK_UP;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper.getDocsForDcf;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper.setUpdatingStatus;

@RequiredArgsConstructor
@Service
@Slf4j
public class DigitalCaseFileService {
    private final AuthTokenGenerator authTokenGenerator;
    private final BundleApiClient bundleApiClient;
    private static final String CREATE = "Create";
    private static final String UPLOAD = "Upload";
    private static final String REMOVE = "Remove";

    public void createUploadRemoveDcf(String userToken, CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        switch (caseData.getUploadOrRemoveDcf()) {
            case CREATE -> {
                caseData.setCaseBundles(createBundleData(caseData));
                stitchCaseFileAsync(userToken, caseDetails);
                setUpdatingStatus(caseData);
            }
            case UPLOAD -> {
                DigitalCaseFileType digitalCaseFile = caseData.getDigitalCaseFile();
                if (isNotEmpty(digitalCaseFile)) {
                    digitalCaseFile.setStatus("DCF Uploaded: "
                        + LocalDateTime.now(ZoneId.of("Europe/London")).format(NEW_DATE_TIME_PATTERN));
                    digitalCaseFile.setError(null);

                    // Deprecating old field
                    digitalCaseFile.setDateGenerated(null);
                }
            }
            case REMOVE -> caseData.setDigitalCaseFile(null);
            default -> log.error("Invalid uploadOrRemoveDcf value: {}", caseData.getUploadOrRemoveDcf());
        }
        caseData.setUploadOrRemoveDcf(null);
    }

    private BundleCreateRequest bundleRequestMapper(CaseDetails caseDetails) {
        return BundleCreateRequest.builder()
                .caseDetails(caseDetails)
                .caseTypeId(caseDetails.getCaseTypeId())
                .build();
    }

    public List<Bundle> createBundleData(CaseData caseData) {
        Bundle bundle = Bundle.builder()
                .value(createBundleDetails(caseData))
                .build();
        return List.of(bundle);
    }

    private BundleDetails createBundleDetails(CaseData caseData) {
        List<BundleDocumentDetails> caseDocs = getDocsForDcf(caseData);
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
     * Prepare Markdown to display a link to the Digital Case File (DCF) in the UI.
     *
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
        if (isEmpty(uploadedDocumentType) || isEmpty(uploadedDocumentType.getDocumentBinaryUrl())) {
            return "";
        }
        String documentBinaryUrl = uploadedDocumentType.getDocumentBinaryUrl();
        String link = documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
        return String.format(DOC_OPENS_IN_NEW_TAB_MARK_UP, link, DIGITAL_CASE_FILE);
    }

    public void stitchCaseFileAsync(String userToken, CaseDetails caseDetails) {
        bundleApiClient.asyncStitchBundle(userToken, authTokenGenerator.generate(),
                bundleRequestMapper(caseDetails));
    }
}

