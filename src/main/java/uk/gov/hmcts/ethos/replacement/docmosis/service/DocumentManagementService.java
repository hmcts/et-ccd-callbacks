package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.UploadedDocument;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.DocumentDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Classification;
import uk.gov.hmcts.reform.document.DocumentDownloadClientApi;
import uk.gov.hmcts.reform.document.DocumentUploadClientApi;
import uk.gov.hmcts.reform.document.domain.Document;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.document.utils.InMemoryMultipartFile;

import java.net.URI;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OUTPUT_FILE_NAME;

@Service
@Slf4j
@ComponentScan("uk.gov.hmcts.reform.ccd.document.am.feign")
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.ExcessiveImports"})
public class DocumentManagementService {

    private static final String FILES_NAME = "files";
    private static final String BINARY = "/binary";
    public static final String APPLICATION_DOCX_VALUE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String FILE_DISPLAY = "<a href=\"/documents/%s\" target=\"_blank\">%s (%s, %s)</a>";

    private final DocumentUploadClientApi documentUploadClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final DocumentDownloadClientApi documentDownloadClientApi;
    private final UserService userService;
    private final CaseDocumentClient caseDocumentClient;
    private final RestTemplate restTemplate;

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;
    @Value("${document_management.ccdCaseDocument.url}")
    private String ccdDMStoreBaseUrl;
    @Value("${case_document_am.url}")
    private String caseDocumentAmUrl;
    @Value("${feature.secure-doc-store.enabled}")
    private boolean secureDocStoreEnabled;

    @Autowired
    public DocumentManagementService(DocumentUploadClientApi documentUploadClient,
                                     AuthTokenGenerator authTokenGenerator, UserService userService,
                                     DocumentDownloadClientApi documentDownloadClientApi,
                                     CaseDocumentClient caseDocumentClient,
                                     RestTemplate restTemplate) {
        this.documentUploadClient = documentUploadClient;
        this.authTokenGenerator = authTokenGenerator;
        this.userService = userService;
        this.documentDownloadClientApi = documentDownloadClientApi;
        this.caseDocumentClient = caseDocumentClient;
        this.restTemplate = restTemplate;
    }

    @Retryable(value = {DocumentManagementException.class}, backoff = @Backoff(delay = 200))
    public URI uploadDocument(String authToken, byte[] byteArray, String outputFileName, String type,
                              String caseTypeID) {
        try {
            MultipartFile file = new InMemoryMultipartFile(FILES_NAME, outputFileName, type, byteArray);
            if (secureDocStoreEnabled) {
                log.info("Using Case Document Client");
                uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse response = caseDocumentClient.uploadDocuments(
                        authToken,
                        authTokenGenerator.generate(),
                        caseTypeID,
                        JURISDICTION,
                        singletonList(file),
                        Classification.PUBLIC
                );

                uk.gov.hmcts.reform.ccd.document.am.model.Document document = response.getDocuments().stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new DocumentManagementException("Document management failed uploading file"
                                        + OUTPUT_FILE_NAME));
                log.info("Uploaded document successful");
                return URI.create(document.links.self.href);
            } else {
                log.info("Using Document Upload Client");
                UserDetails user = userService.getUserDetails(authToken);
                UploadResponse response = documentUploadClient.upload(
                       authToken,
                       authTokenGenerator.generate(),
                        user.getUid(),
                        new ArrayList<>(singletonList("caseworker-employment")),
                        uk.gov.hmcts.reform.document.domain.Classification.PUBLIC,
                        singletonList(file)
                );
                Document document = response.getEmbedded().getDocuments().stream()
                    .findFirst()
                    .orElseThrow(() ->
                            new DocumentManagementException("Document management failed uploading file"
                                    + OUTPUT_FILE_NAME));
                log.info("Uploaded document successful");
                return URI.create(document.links.self.href);
            }
        } catch (Exception ex) {
            log.info("Exception: " + ex.getMessage());
            throw new DocumentManagementException(String.format("Unable to upload document %s to document management",
                    outputFileName), ex);
        }
    }

    public String generateDownloadableURL(URI documentSelf) {
        return ccdGatewayBaseUrl + documentSelf.getRawPath() + BINARY;
    }

    public String generateMarkupDocument(String documentDownloadableURL) {
        return "<a target=\"_blank\" href=\"" + documentDownloadableURL + "\">Document</a>";
    }

    public UploadedDocument downloadFile(String authToken, String urlString) {
        UserDetails user = userService.getUserDetails(authToken);
        ResponseEntity<Resource> response;
        if (secureDocStoreEnabled) {
            response = caseDocumentClient.getDocumentBinary(
                    authToken,
                    authTokenGenerator.generate(),
                    getDocumentUUID(urlString)
            );

        } else {
            response = documentDownloadClientApi.downloadBinary(
                    authToken,
                    authTokenGenerator.generate(),
                    String.join(",", user.getRoles()),
                    user.getUid(),
                    getDownloadUrl(urlString)
            );
        }
        if (HttpStatus.OK.equals(response.getStatusCode())) {
            return UploadedDocument.builder()
                    .content(response.getBody())
                    .name(Objects.requireNonNull(response.getHeaders().get("originalfilename")).get(0))
                    .contentType(Objects.requireNonNull(response.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0))
                    .build();
        } else {
            throw new IllegalStateException("Cannot download document that is stored in CCD got "
                    + "[" + response.getStatusCode() + "] " + response.getBody());
        }
    }

    private String getDownloadUrl(String urlString) {
        String path = urlString.replace(ccdDMStoreBaseUrl, "");
        if (path.startsWith("/")) {
            return path;
        }

        return "/" + path;
    }

    public String getDocumentUUID(String urlString) {
        String documentUUID = urlString.replace(ccdDMStoreBaseUrl + "/documents/", "");
        documentUUID = documentUUID.replace(BINARY, "");
        return documentUUID;
    }

    /**
     * This method converts the DocumentInfo generated by Tornado into the Document type which is used by some CCD
     * fields.
     * @param documentInfo contains the data generated by Tornado which includes links and descriptions of the document
     * @return Document object which contains the links and name of the document generated by Tornado
     */
    public UploadedDocumentType addDocumentToDocumentField(DocumentInfo documentInfo) {
        UploadedDocumentType document = new UploadedDocumentType();
        String documentId = documentInfo.getUrl().substring(documentInfo.getUrl().indexOf("/documents/"));
        document.setDocumentBinaryUrl(ccdDMStoreBaseUrl + documentId);
        document.setDocumentUrl(document.getDocumentBinaryUrl().replace(BINARY, ""));
        document.setDocumentFilename(documentInfo.getDescription());
        return document;
    }

    /**
     * Return document info in format [File Name] (File Type, File Size).
     * @param document file in UploadedDocumentType
     * @param authToken the caller's bearer token used to verify the caller
     * @return String which contains the document name, type, size and link
     */
    public String displayDocNameTypeSizeLink(UploadedDocumentType document, String authToken) {
        if (document == null) {
            return "";
        }

        Pattern pattern = Pattern.compile("^.+?/documents/");
        Matcher matcher = pattern.matcher(document.getDocumentBinaryUrl());
        String documentLink = matcher.replaceFirst("");

        String documentName = document.getDocumentFilename();
        String documentType = document.getDocumentFilename();
        int lastIndexDot = document.getDocumentFilename().lastIndexOf('.');
        if (lastIndexDot > 0) {
            documentName = document.getDocumentFilename().substring(0, lastIndexDot);
            documentType = document.getDocumentFilename().substring(lastIndexDot + 1).toUpperCase(Locale.ENGLISH);
        }

        ResponseEntity<DocumentDetails> documentDetails =
                getDocumentDetails(authToken, UUID.fromString(getDocumentUUID(document.getDocumentUrl())));

        DocumentDetails docDetails = documentDetails.getBody();
        String fileSize = (docDetails == null) 
            ? "" : FileUtils.byteCountToDisplaySize(Long.parseLong(docDetails.getSize()));

        return String.format(FILE_DISPLAY, documentLink, documentName, documentType, fileSize);
    }

    private ResponseEntity<DocumentDetails> getDocumentDetails(String authToken, UUID documentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, authToken);
        headers.add(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<DocumentDetails> response = restTemplate.exchange(
                    caseDocumentAmUrl + "/cases/documents/" + documentId,
                    HttpMethod.GET,
                    request,
                    DocumentDetails.class
            );
            return new ResponseEntity<>(response.getBody(), getResponseHeaders(), HttpStatus.OK);
        } catch (Exception ex) {
            throw new DocumentManagementException(
                    String.format("Unable to get document details %s from document management", documentId), ex);
        }
    }

    private HttpHeaders getResponseHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("Connection", "keep-alive");
        responseHeaders.add("Content-Type", "application/json");
        responseHeaders.add("X-Frame-Options", "DENY");
        responseHeaders.add("X-XSS-Protection", "1; mode=block");
        responseHeaders.add("X-Content-Type-Options", "nosniff");
        return responseHeaders;
    }

}
