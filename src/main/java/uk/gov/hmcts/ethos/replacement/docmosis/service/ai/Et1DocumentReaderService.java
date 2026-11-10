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
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class Et1DocumentReaderService {

    private static final int MAX_ATTACHMENT_BYTES = 15 * 1024 * 1024;
    private static final String APPLICATION_PDF = "application/pdf";

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
     * Discovers and downloads relevant claim particulars documents for Gemini analysis.
     *
     * @param caseData CCD CaseData
     * @param userToken auth token for DM Store
     * @return List of AiDocumentAttachment objects
     */
    public List<AiDocumentAttachment> findClaimDocumentAttachments(CaseData caseData, String userToken) {
        List<AiDocumentAttachment> attachments = new ArrayList<>();
        ClaimantRequestType requests = caseData.getClaimantRequests();

        if (requests != null && requests.getClaimDescriptionDocument() != null) {
            AiDocumentAttachment claimDoc = downloadDocumentAttachment(
                    userToken, requests.getClaimDescriptionDocument());
            if (claimDoc != null) {
                attachments.add(claimDoc);
            }
        }

        boolean hasNarrative = requests != null && StringUtils.isNotBlank(requests.getClaimDescription());
        if (!hasNarrative && attachments.isEmpty() && CollectionUtils.isNotEmpty(caseData.getDocumentCollection())) {
            for (DocumentTypeItem docItem : caseData.getDocumentCollection()) {
                if (docItem.getValue() != null && docItem.getValue().getUploadedDocument() != null
                        && isEt1ClaimDocument(docItem.getValue())) {
                    AiDocumentAttachment et1Doc = downloadDocumentAttachment(
                            userToken, docItem.getValue().getUploadedDocument());
                    if (et1Doc != null) {
                        attachments.add(et1Doc);
                        break;
                    }
                }
            }
        }

        return attachments;
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

    private boolean isEt1ClaimDocument(DocumentType docType) {
        String type = Objects.toString(docType.getTypeOfDocument(), "").toLowerCase(Locale.ROOT);
        String starting = Objects.toString(docType.getStartingClaimDocuments(), "").toLowerCase(Locale.ROOT);
        String name = docType.getUploadedDocument() != null
                ? Objects.toString(docType.getUploadedDocument().getDocumentFilename(), "")
                .toLowerCase(Locale.ROOT) : "";
        return type.contains("et1") || starting.contains("et1") || name.startsWith("et1");
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
