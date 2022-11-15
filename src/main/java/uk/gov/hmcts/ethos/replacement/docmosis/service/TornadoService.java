package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.bulk.BulkData;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BulkHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1VettingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.InitialConsiderationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ListingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReportDocHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.SignificantItemType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TornadoDocumentFilter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LETTER_ADDRESS_ALLOCATED_OFFICE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OUTPUT_FILE_NAME;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService.APPLICATION_DOCX_VALUE;

@Slf4j
@RequiredArgsConstructor
@Service("tornadoService")
@SuppressWarnings({"PMD.TooManyMethods", "PMD.LawOfDemeter", "PMD.AvoidThrowingNullPointerException",
    "PMD.ExcessiveImports"})
public class TornadoService {
    private static final String UNABLE_TO_CONNECT_TO_DOCMOSIS = "Unable to connect to Docmosis: ";
    private static final String OUTPUT_FILE_NAME_PDF = "document.pdf";

    private final TornadoConnection tornadoConnection;
    private final DocumentManagementService documentManagementService;
    private final UserService userService;
    private final DefaultValuesReaderService defaultValuesReaderService;
    private final VenueAddressReaderService venueAddressReaderService;

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    private String dmStoreDocumentName = OUTPUT_FILE_NAME_PDF;

    public DocumentInfo documentGeneration(String authToken, CaseData caseData, String caseTypeId,
                                           CorrespondenceType correspondenceType,
                                           CorrespondenceScotType correspondenceScotType,
                                           MultipleData multipleData) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = createConnection();

            buildInstruction(conn, caseData, authToken, caseTypeId,
                    correspondenceType, correspondenceScotType, multipleData);
            String documentName = Helper.getDocumentName(correspondenceType, correspondenceScotType);
            return checkResponseStatus(authToken, conn, documentName, caseTypeId);
        } catch (IOException e) {
            log.error(UNABLE_TO_CONNECT_TO_DOCMOSIS, e);
            throw e;
        } finally {
            closeConnection(conn);
        }
    }

    private void buildInstruction(HttpURLConnection conn, CaseData caseData, String authToken,
                                  String caseTypeId, CorrespondenceType correspondenceType,
                                  CorrespondenceScotType correspondenceScotType,
                                  MultipleData multipleData) throws IOException {
        try (OutputStreamWriter os = new OutputStreamWriter(conn.getOutputStream(), StandardCharsets.UTF_8)) {
            DefaultValues allocatedCourtAddress = getAllocatedCourtAddress(caseData, caseTypeId, multipleData);
            UserDetails userDetails = userService.getUserDetails(authToken);

            StringBuilder documentContent = DocumentHelper.buildDocumentContent(caseData,
                    tornadoConnection.getAccessKey(),
                    userDetails, caseTypeId, correspondenceType,
                    correspondenceScotType, multipleData, allocatedCourtAddress, venueAddressReaderService);

            writeOutputStream(os, documentContent);
        }
    }

    private DefaultValues getAllocatedCourtAddress(CaseData caseData, String caseTypeId, MultipleData multipleData) {
        if (multipleData != null && isAllocatedOffice(caseTypeId, multipleData.getCorrespondenceScotType())
                || isAllocatedOffice(caseTypeId, caseData.getCorrespondenceScotType())) {
            return defaultValuesReaderService.getDefaultValues(caseData.getAllocatedOffice());
        }
        return null;
    }

    private boolean isAllocatedOffice(String caseTypeId, CorrespondenceScotType correspondenceScotType) {
        return caseTypeId.equals(SCOTLAND_CASE_TYPE_ID)
                && correspondenceScotType != null
                && correspondenceScotType.getLetterAddress().equals(LETTER_ADDRESS_ALLOCATED_OFFICE);
    }

    DocumentInfo listingGeneration(String authToken, ListingData listingData, String caseType) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = createConnection();

            String documentName = ListingHelper.getListingDocName(listingData);
            buildListingInstruction(conn, listingData, documentName, authToken, caseType);
            return checkResponseStatus(authToken, conn, documentName, caseType);
        } catch (IOException e) {
            log.error(UNABLE_TO_CONNECT_TO_DOCMOSIS, e);
            throw e;
        } finally {
            closeConnection(conn);
        }
    }

    private void buildListingInstruction(HttpURLConnection conn, ListingData listingData,
                                         String documentName, String authToken, String caseType) throws IOException {
        UserDetails userDetails = userService.getUserDetails(authToken);
        StringBuilder sb;

        if (ListingHelper.isReportType(listingData.getReportType())) {
            sb = ReportDocHelper.buildReportDocumentContent(listingData, tornadoConnection.getAccessKey(),
                    documentName, userDetails);
        } else {
            sb = ListingHelper.buildListingDocumentContent(listingData, tornadoConnection.getAccessKey(),
                    documentName, userDetails, caseType);
        }
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
            conn.getOutputStream(), StandardCharsets.UTF_8)) {
            writeOutputStream(outputStreamWriter, sb);
        }
    }

    DocumentInfo scheduleGeneration(String authToken, BulkData bulkData, String caseTypeId) throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = createConnection();

            String documentName = BulkHelper.getScheduleDocName(bulkData.getScheduleDocName());
            buildScheduleInstruction(conn, bulkData);
            return checkResponseStatus(authToken, conn, documentName, caseTypeId);
        } catch (IOException e) {
            log.error(UNABLE_TO_CONNECT_TO_DOCMOSIS, e);
            throw e;
        } finally {
            closeConnection(conn);
        }
    }

    private void buildScheduleInstruction(HttpURLConnection conn, BulkData bulkData) throws IOException {
        StringBuilder sb = BulkHelper.buildScheduleDocumentContent(bulkData, tornadoConnection.getAccessKey());

        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
            conn.getOutputStream(), StandardCharsets.UTF_8)) {
            writeOutputStream(outputStreamWriter, sb);
        }
    }

    private HttpURLConnection createConnection() throws IOException {
        return tornadoConnection.createConnection();
    }

    private void closeConnection(HttpURLConnection conn) {
        if (conn != null) {
            conn.disconnect();
        }
    }

    private DocumentInfo checkResponseStatus(String authToken, HttpURLConnection conn, String documentName,
                                             String caseTypeId)
            throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            int responseCode = conn.getResponseCode();
            if (responseCode == HTTP_OK) {
                return createDocument(authToken, conn, documentName, os, caseTypeId);
            } else {
                throw new IOException(String.format("Invalid response code %d received from Tornado: %s", responseCode,
                        conn.getResponseMessage()));
            }
        }
    }

    private DocumentInfo createDocument(String authToken, HttpURLConnection conn, String documentName,
                                        ByteArrayOutputStream os, String caseTypeId) throws IOException {

        byte[] bytes;
        try (InputStream is = conn.getInputStream()) {
            bytes = getBytesFromInputStream(os, is);
        }
        URI documentSelfPath = uploadDocument(documentName, authToken, bytes, caseTypeId);
        log.info("URI documentSelfPath uploaded and created: " + documentSelfPath.toString());
        String downloadUrl = documentManagementService.generateDownloadableURL(documentSelfPath);
        String markup = documentManagementService.generateMarkupDocument(downloadUrl);
        return generateDocumentInfo(documentName, documentSelfPath, markup);
    }

    private URI uploadDocument(String documentName, String authToken, byte[] bytes, String caseTypeId) {
        if (documentName.endsWith(".pdf")) {
            return documentManagementService.uploadDocument(authToken, bytes, OUTPUT_FILE_NAME_PDF,
                    APPLICATION_PDF_VALUE, caseTypeId);
        } else {
            return documentManagementService.uploadDocument(authToken, bytes, OUTPUT_FILE_NAME,
                    APPLICATION_DOCX_VALUE, caseTypeId);
        }
    }

    private byte[] getBytesFromInputStream(ByteArrayOutputStream os, InputStream is) throws IOException {
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }

    private DocumentInfo generateDocumentInfo(String documentName, URI documentSelfPath, String markupURL) {
        return DocumentInfo.builder()
                .type(SignificantItemType.DOCUMENT.name())
                .description(documentName)
                .markUp(markupURL)
                .url(ccdGatewayBaseUrl + documentSelfPath.getRawPath() + "/binary")
                .build();
    }

    private void writeOutputStream(OutputStreamWriter outputStreamWriter, StringBuilder sb) throws IOException {
        String reportJson = TornadoDocumentFilter.filterJson(sb.toString());
        outputStreamWriter.write(reportJson);
        outputStreamWriter.flush();
    }

    /**
     * This method calls the helper method to create the data to be passed through to Tornado and then checks whether
     * it can reach the service.
     * @param caseData contains the data needed to generate the PDF
     * @param userToken contains the user authentication token
     * @param caseTypeId reference for which casetype the document is being uploaded to
     * @param documentName name of the document
     * @return DocumentInfo which contains the URL and markup of the uploaded document
     * @throws IOException if the call to Tornado has failed, an exception will be thrown. This could be due to
     timeout or maybe a bad gateway.
     */
    public DocumentInfo generateEventDocument(CaseData caseData, String userToken, String caseTypeId,
                                              String documentName)
        throws IOException {
        HttpURLConnection connection = null;
        try {
            dmStoreDocumentName = documentName;
            connection = createConnection();
            buildDocumentInstruction(connection, caseData, documentName, caseTypeId);
            return checkResponseStatus(userToken, connection, dmStoreDocumentName, caseTypeId);
        } catch (IOException exception) {
            log.error(UNABLE_TO_CONNECT_TO_DOCMOSIS, exception);
            throw exception;
        } finally {
            closeConnection(connection);
        }
    }

    private void buildDocumentInstruction(HttpURLConnection connection, CaseData caseData, String documentName,
                                          String caseTypeId)
            throws IOException {
        String documentContent = getDocumentContent(caseData, documentName, caseTypeId);

        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(connection.getOutputStream(),
                StandardCharsets.UTF_8)) {
            outputStreamWriter.write(documentContent);
            outputStreamWriter.flush();
        }
    }

    private String getDocumentContent(CaseData caseData, String documentName, String caseTypeId)
            throws JsonProcessingException {
        if (isNullOrEmpty(documentName)) {
            throw new NullPointerException("Document name cannot be null or empty");
        }
        switch (documentName) {
            case "ET1 Vetting.pdf":
                return Et1VettingHelper.getDocumentRequest(caseData, tornadoConnection.getAccessKey());
            case "ET3 Processing.pdf":
                return Et3VettingHelper.getDocumentRequest(caseData, tornadoConnection.getAccessKey());
            case "ET3 Response.pdf":
                dmStoreDocumentName = String.format("%s - ET3 Response.pdf",
                    caseData.getEt3ResponseRespondentLegalName());
                return Et3ResponseHelper.getDocumentRequest(caseData, tornadoConnection.getAccessKey());
            case "Initial Consideration.pdf" :
                return InitialConsiderationHelper.getDocumentRequest(
                        caseData, tornadoConnection.getAccessKey(), caseTypeId);
            default:
                throw new IllegalArgumentException("Unexpected document name " + documentName);
        }
    }
}
