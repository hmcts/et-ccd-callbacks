package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.BulkData;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.items.ListingTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.ListingType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.SignificantItemType;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_ACCEPTED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_DOC_ETCL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_STAFF;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LETTER_ADDRESS_ALLOCATED_OFFICE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LIST_CASES_CONFIG;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;

public class TornadoServiceTest {
    private TornadoService tornadoService;
    private TornadoConnection tornadoConnection;
    private DocumentManagementService documentManagementService;
    private UserService userService;
    private DefaultValuesReaderService defaultValuesReaderService;
    private VenueAddressReaderService venueAddressReaderService;
    private MockHttpURLConnection mockConnection;
    private final String authToken = "a-test-auth-token";
    private final String documentInfoMarkup = "<a>some test markup</a>";

    @Before
    public void setUp() throws IOException {
        createUserService();
        mockTornadoConnection();
        mockDocumentManagement();
        mockDefaultValuesReaderService();
        mockVenueAddressReaderService();

        tornadoService = new TornadoService(tornadoConnection, documentManagementService,
                userService, defaultValuesReaderService, venueAddressReaderService);
    }

    @Test(expected = IOException.class)
    public void documentGenerationNoTornadoConnectionShouldThrowException() throws IOException {
        CaseData caseData = new CaseData();
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        tornadoService.documentGeneration(authToken, caseData, ENGLANDWALES_CASE_TYPE_ID,
                caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null);
    }

    @Test(expected = IOException.class)
    public void listingGenerationNoTornadoConnectionShouldThrowException() throws IOException {
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        tornadoService.listingGeneration(authToken, createListingData(), ENGLANDWALES_LISTING_CASE_TYPE_ID);
    }

    @Test(expected = IOException.class)
    public void scheduleGenerationNoTornadoConnectionShouldThrowException() throws IOException {
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        tornadoService.scheduleGeneration(authToken, createBulkData(), ENGLANDWALES_LISTING_CASE_TYPE_ID);
    }

    @Test(expected = IOException.class)
    public void shouldThrowExceptionWhenTornadoReturnsErrorResponse() throws IOException {
        mockConnectionError();
        CaseData caseData = new CaseData();

        tornadoService.documentGeneration(authToken, caseData, ENGLANDWALES_CASE_TYPE_ID,
                caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null);
    }

    @Test
    public void shouldCreateDocumentInfoForDocumentGeneration() throws IOException {
        mockConnectionSuccess();
        CaseData caseData = new CaseData();

        DocumentInfo documentInfo = tornadoService.documentGeneration(authToken, caseData, ENGLANDWALES_CASE_TYPE_ID,
                caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    public void shouldCreateDocumentInfoForDocumentGenerationAllocatedOffice() throws IOException {
        mockConnectionSuccess();
        var defaultValues = mock(DefaultValues.class);
        when(defaultValuesReaderService.getDefaultValues(TribunalOffice.GLASGOW.getOfficeName()))
                .thenReturn(defaultValues);
        CaseData caseData = new CaseData();
        caseData.setAllocatedOffice(TribunalOffice.GLASGOW.getOfficeName());
        CorrespondenceScotType correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setTopLevelScotDocuments("test-template");
        correspondenceScotType.setLetterAddress(LETTER_ADDRESS_ALLOCATED_OFFICE);
        caseData.setCorrespondenceScotType(correspondenceScotType);

        DocumentInfo documentInfo = tornadoService.documentGeneration(authToken, caseData, SCOTLAND_CASE_TYPE_ID,
                caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    public void shouldCreateDocumentInfoForDocumentGenerationAllocatedOfficeMultiples() throws IOException {
        mockConnectionSuccess();
        DefaultValues defaultValues = mock(DefaultValues.class);
        when(defaultValuesReaderService.getDefaultValues(TribunalOffice.GLASGOW.getOfficeName()))
                .thenReturn(defaultValues);
        CaseData caseData = new CaseData();
        caseData.setAllocatedOffice(TribunalOffice.GLASGOW.getOfficeName());
        CorrespondenceScotType correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setTopLevelScotDocuments("test-template");
        correspondenceScotType.setLetterAddress(LETTER_ADDRESS_ALLOCATED_OFFICE);
        caseData.setCorrespondenceScotType(correspondenceScotType);

        DocumentInfo documentInfo = tornadoService.documentGeneration(authToken, caseData, SCOTLAND_CASE_TYPE_ID,
                caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), new MultipleData());

        verifyDocumentInfo(documentInfo);
    }

    @Test
    public void shouldCreateDocumentInfoForListingGeneration() throws IOException {
        mockConnectionSuccess();
        ListingData listingData = createListingData();

        DocumentInfo documentInfo = tornadoService.listingGeneration(
                authToken, listingData, ENGLANDWALES_LISTING_CASE_TYPE_ID);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    public void shouldCreateDocumentInforForScheduleGeneration() throws IOException {
        mockConnectionSuccess();
        var bulkData = new BulkData();
        bulkData.setScheduleDocName(LIST_CASES_CONFIG);
        bulkData.setSearchCollection(new ArrayList<>());

        DocumentInfo documentInfo = tornadoService.scheduleGeneration(
                authToken, bulkData, ENGLANDWALES_LISTING_CASE_TYPE_ID);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    public void shouldCreateDocumentInfoForReportGeneration() throws IOException {
        mockConnectionSuccess();
        ListingData listingData = createListingData();
        listingData.setReportType(CLAIMS_ACCEPTED_REPORT);

        DocumentInfo documentInfo = tornadoService.listingGeneration(authToken, listingData,
                ENGLANDWALES_LISTING_CASE_TYPE_ID);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    public void generateEt1VettingDocument() throws IOException {
        mockConnectionSuccess();
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                new CaseData(), authToken, ENGLANDWALES_CASE_TYPE_ID, "ET1 Vetting.pdf");
        verifyDocumentInfo(documentInfo);
    }

    @Test
    public void generateEt3VettingDocument() throws IOException {
        mockConnectionSuccess();
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                new CaseData(), authToken, ENGLANDWALES_CASE_TYPE_ID, "ET3 Processing.pdf");
        verifyDocumentInfo(documentInfo);
    }

    @Test(expected = IOException.class)
    public void generateDocument_exception() throws IOException {
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);
        tornadoService.generateEventDocument(new CaseData(), authToken, ENGLANDWALES_CASE_TYPE_ID,
                "random-string");
    }

    private void createUserService() {
        UserDetails userDetails = HelperTest.getUserDetails();
        IdamApi idamApi = authorisation -> userDetails;
        userService = new UserService(idamApi);
    }

    private void mockTornadoConnection() throws IOException {
        mockConnection = new MockHttpURLConnection(new URL("http://testdocmosis"));
        tornadoConnection = mock(TornadoConnection.class);
        when(tornadoConnection.createConnection()).thenReturn(mockConnection);
    }

    private void mockDocumentManagement() {
        documentManagementService = mock(DocumentManagementService.class);
        String documentUrl = "http://testdocumentserver/testdocument";
        URI uri = URI.create(documentUrl);
        when(documentManagementService.uploadDocument(anyString(), any(byte[].class),
                anyString(), anyString(), anyString())).thenReturn(uri);
        when(documentManagementService.generateDownloadableURL(uri)).thenReturn(documentUrl);
        when(documentManagementService.generateMarkupDocument(anyString())).thenReturn(documentInfoMarkup);
    }

    private void mockDefaultValuesReaderService() {
        defaultValuesReaderService = mock(DefaultValuesReaderService.class);
    }

    private void mockVenueAddressReaderService() {
        venueAddressReaderService = mock(VenueAddressReaderService.class);
    }

    private void mockConnectionSuccess() throws IOException {
        InputStream inputStream = mock(InputStream.class);
        OutputStream outputStream = mock(OutputStream.class);
        when(inputStream.read(any(byte[].class))).thenReturn(-1);
        mockConnection.setInputStream(inputStream);
        mockConnection.setOutputStream(outputStream);
        mockConnection.setResponseCode(HTTP_OK);
    }

    private void mockConnectionError() throws IOException {
        InputStream mockInputStream = mock(InputStream.class);
        when(mockInputStream.read(any(byte[].class))).thenReturn(-1);
        when(mockInputStream.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        OutputStream mockOutputStream = mock(OutputStream.class);
        mockConnection.setErrorStream(mockInputStream);
        mockConnection.setOutputStream(mockOutputStream);
        mockConnection.setResponseCode(HTTP_INTERNAL_ERROR);
    }

    private ListingData createListingData() {
        ListingTypeItem listingTypeItem = new ListingTypeItem();
        ListingType listingType = new ListingType();
        listingType.setCauseListDate("2019-12-12");
        listingTypeItem.setId("1111");
        listingTypeItem.setValue(listingType);
        ListingData listingData = new ListingData();
        listingData.setHearingDocType(HEARING_DOC_ETCL);
        listingData.setHearingDocETCL(HEARING_ETCL_STAFF);
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setListingVenue(new DynamicFixedListType("Glasgow"));
        listingData.setListingCollection(new ArrayList<>(Collections.singleton(listingTypeItem)));

        return listingData;
    }

    private BulkData createBulkData() {
        BulkData bulkData = new BulkData();
        bulkData.setScheduleDocName(LIST_CASES_CONFIG);
        bulkData.setSearchCollection(new ArrayList<>());
        return bulkData;
    }

    private void verifyDocumentInfo(DocumentInfo documentInfo) {
        assertEquals(documentInfoMarkup, documentInfo.getMarkUp());
        assertEquals(SignificantItemType.DOCUMENT.name(), documentInfo.getType());
    }
}