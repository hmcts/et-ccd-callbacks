package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.UploadedDocument;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantRequestType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiDocumentAttachment;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ACAS_CERTIFICATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_VETTING;

@Slf4j
@Service
@RequiredArgsConstructor
public class Et1DocumentReaderService {

    private static final int MAX_ATTACHMENT_BYTES = 15 * 1024 * 1024;
    private static final int MAX_TOTAL_ATTACHMENT_BYTES = 25 * 1024 * 1024;
    private static final int MAX_ATTACHMENT_COUNT = 10;
    private static final String APPLICATION_PDF = "application/pdf";
    private static final Set<String> RELEVANT_DOCUMENT_TYPES = Set.of(
            ET1,
            ET1_ATTACHMENT,
            ACAS_CERTIFICATE
    );

    private final DocumentManagementService documentManagementService;

    /**
     * Downloads an uploaded document from DM Store / CDAM as a raw binary attachment for Gemini multimodal input.
     *
     * @param userToken auth token for DM Store (optional, falls back to RequestContextHolder)
     * @param document uploaded document reference
     * @return AiDocumentAttachment with bytes and MIME type, or null if unavailable
     */
    public AiDocumentAttachment downloadDocumentAttachment(String userToken, UploadedDocumentType document) {
        String token = resolveAuthToken(userToken);
        if (StringUtils.isBlank(token) || document == null
                || StringUtils.isBlank(document.getDocumentBinaryUrl())) {
            return null;
        }

        try {
            log.info("Downloading file {} from DM Store for Gemini multimodal vetting",
                    document.getDocumentFilename());
            UploadedDocument uploadedDoc = documentManagementService.downloadFile(
                    token, document.getDocumentBinaryUrl());
            if (uploadedDoc == null || uploadedDoc.getContent() == null) {
                log.warn("Document {} downloaded but content is empty", document.getDocumentFilename());
                return null;
            }

            Resource resource = uploadedDoc.getContent();
            byte[] bytes;
            try (InputStream in = resource.getInputStream()) {
                bytes = in.readAllBytes();
            }

            if (bytes.length == 0 || bytes.length > MAX_ATTACHMENT_BYTES) {
                log.warn("Document {} size ({} bytes) outside acceptable limits",
                        document.getDocumentFilename(), bytes.length);
                return null;
            }

            String mimeType = resolveMimeType(document.getDocumentFilename(), uploadedDoc.getContentType());
            return AiDocumentAttachment.builder()
                    .filename(document.getDocumentFilename())
                    .mimeType(mimeType)
                    .data(bytes)
                    .build();
        } catch (Exception ex) {
            log.warn("Could not download file {} for Gemini multimodal vetting: {}",
                    document.getDocumentFilename(), ex.getMessage());
            return null;
        }
    }

    /**
     * Discovers and downloads all relevant claim documents (Particulars of Claim, ET1 form,
     * ET1 attachments, and ACAS Early Conciliation certificates) for Gemini analysis.
     *
     * @param caseData CCD CaseData
     * @param userToken auth token for DM Store
     * @return List of deduplicated AiDocumentAttachment objects within payload caps
     */
    public List<AiDocumentAttachment> findClaimDocumentAttachments(CaseData caseData, String userToken) {
        if (caseData == null) {
            return List.of();
        }
        List<AiDocumentAttachment> attachments = new ArrayList<>();
        Set<String> seenBinaryUrls = new HashSet<>();
        int totalBytes = 0;

        ClaimantRequestType requests = caseData.getClaimantRequests();
        if (requests != null) {
            totalBytes = tryAddAttachment(
                    requests.getClaimDescriptionDocument(), userToken, attachments, seenBinaryUrls, totalBytes);
        }

        totalBytes = tryAddAttachment(
                caseData.getEt1SectionThreeDocumentUpload(), userToken, attachments, seenBinaryUrls, totalBytes);

        if (CollectionUtils.isNotEmpty(caseData.getDocumentCollection())) {
            for (DocumentTypeItem docItem : caseData.getDocumentCollection()) {
                if (attachments.size() >= MAX_ATTACHMENT_COUNT) {
                    log.info("Reached max AI attachment count ({}); skipping remaining documents",
                            MAX_ATTACHMENT_COUNT);
                    break;
                }
                if (docItem.getValue() != null && docItem.getValue().getUploadedDocument() != null
                        && isRelevantVettingDocument(docItem.getValue())) {
                    totalBytes = tryAddAttachment(docItem.getValue().getUploadedDocument(), userToken,
                            attachments, seenBinaryUrls, totalBytes);
                }
            }
        }

        return attachments;
    }

    private int tryAddAttachment(UploadedDocumentType document,
                                 String userToken,
                                 List<AiDocumentAttachment> attachments,
                                 Set<String> seenBinaryUrls,
                                 int currentTotalBytes) {
        if (document == null || StringUtils.isBlank(document.getDocumentBinaryUrl())
                || attachments.size() >= MAX_ATTACHMENT_COUNT) {
            return currentTotalBytes;
        }
        String normalizedUrl = document.getDocumentBinaryUrl().trim();
        if (!seenBinaryUrls.add(normalizedUrl)) {
            return currentTotalBytes;
        }
        AiDocumentAttachment downloaded = downloadDocumentAttachment(userToken, document);
        if (downloaded == null || downloaded.getData() == null) {
            return currentTotalBytes;
        }
        if (currentTotalBytes + downloaded.getData().length > MAX_TOTAL_ATTACHMENT_BYTES) {
            log.warn("Skipping attachment {} because aggregate payload would exceed {} bytes limit",
                    downloaded.getFilename(), MAX_TOTAL_ATTACHMENT_BYTES);
            return currentTotalBytes;
        }
        attachments.add(downloaded);
        return currentTotalBytes + downloaded.getData().length;
    }

    private String resolveMimeType(String filename, String contentType) {
        if (StringUtils.isNotBlank(contentType) && !"application/octet-stream".equalsIgnoreCase(contentType)) {
            return contentType;
        }
        String lower = Objects.toString(filename, "").toLowerCase(Locale.ROOT);
        if (lower.endsWith(".pdf")) {
            return APPLICATION_PDF;
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".csv")) {
            return "text/csv";
        } else {
            return "text/plain";
        }
    }

    private boolean isRelevantVettingDocument(DocumentType docType) {
        String filename = docType.getUploadedDocument() != null
                ? Objects.toString(docType.getUploadedDocument().getDocumentFilename(), "")
                : "";
        if (isVettingDocument(docType, filename)) {
            return false;
        }
        return matchesRelevantDocumentType(docType.getTypeOfDocument())
                || matchesRelevantDocumentType(docType.getStartingClaimDocuments())
                || matchesRelevantDocumentType(docType.getDocumentType())
                || matchesRelevantFilename(filename);
    }

    private boolean isVettingDocument(DocumentType docType, String filename) {
        return ET1_VETTING.equalsIgnoreCase(StringUtils.trimToEmpty(docType.getTypeOfDocument()))
                || ET1_VETTING.equalsIgnoreCase(StringUtils.trimToEmpty(docType.getStartingClaimDocuments()))
                || ET1_VETTING.equalsIgnoreCase(StringUtils.trimToEmpty(docType.getDocumentType()))
                || filename.toLowerCase(Locale.ROOT).contains(ET1_VETTING.toLowerCase(Locale.ROOT));
    }

    private boolean matchesRelevantDocumentType(String value) {
        if (StringUtils.isBlank(value)) {
            return false;
        }
        String trimmed = value.trim();
        return RELEVANT_DOCUMENT_TYPES.stream().anyMatch(trimmed::equalsIgnoreCase);
    }

    private boolean matchesRelevantFilename(String filename) {
        if (StringUtils.isBlank(filename)) {
            return false;
        }
        String lower = filename.toLowerCase(Locale.ROOT);
        return lower.startsWith(ET1.toLowerCase(Locale.ROOT))
                || lower.contains("acas");
    }

    private String resolveAuthToken(String userToken) {
        if (StringUtils.isNotBlank(userToken)) {
            return userToken;
        }
        try {
            RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
            if (attribs instanceof ServletRequestAttributes servletAttribs) {
                return servletAttribs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            }
        } catch (Exception ex) {
            log.debug("Unable to obtain request authorization header from RequestContextHolder: {}", ex.getMessage());
        }
        return null;
    }

}
