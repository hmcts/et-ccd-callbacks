package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
import uk.gov.hmcts.ethos.replacement.docmosis.config.OAuth2Configuration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenResponse;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

@ExtendWith(SpringExtension.class)
class TornadoServiceTest {
    private TornadoService tornadoService;
    private TornadoConnection tornadoConnection;
    private DocumentManagementService documentManagementService;
    private UserService userService;
    private DefaultValuesReaderService defaultValuesReaderService;
    private VenueAddressReaderService venueAddressReaderService;
    private MockHttpURLConnection mockConnection;
    private static final String AUTH_TOKEN = "a-test-auth-token";
    private static final String DOCUMENT_INFO_MARKUP = "<a>some test markup</a>";
    private OAuth2Configuration oauth2Configuration;

    @BeforeEach
    public void setUp() throws IOException {
        createUserService();
        mockTornadoConnection();
        mockDocumentManagement();
        mockDefaultValuesReaderService();
        mockVenueAddressReaderService();

        tornadoService = new TornadoService(tornadoConnection, documentManagementService,
                userService, defaultValuesReaderService, venueAddressReaderService);
    }

    @Test
    void documentGenerationNoTornadoConnectionShouldThrowException() throws IOException {
        CaseData caseData = new CaseData();
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        assertThrows(IOException.class, () ->
                tornadoService.documentGeneration(AUTH_TOKEN, caseData, ENGLANDWALES_CASE_TYPE_ID,
                        caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null)
        );
    }

    @Test
    void listingGenerationNoTornadoConnectionShouldThrowException() throws IOException {
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        assertThrows(IOException.class, () ->
                tornadoService.listingGeneration(AUTH_TOKEN, createListingData(), ENGLANDWALES_LISTING_CASE_TYPE_ID)
        );
    }

    @Test
    void scheduleGenerationNoTornadoConnectionShouldThrowException() throws IOException {
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        assertThrows(IOException.class, () ->
                tornadoService.scheduleGeneration(AUTH_TOKEN, createBulkData(), ENGLANDWALES_LISTING_CASE_TYPE_ID)
        );
    }

    @Test
    void shouldThrowExceptionWhenTornadoReturnsErrorResponse() throws IOException {
        mockConnectionError();
        CaseData caseData = new CaseData();

        assertThrows(IOException.class, () ->
                tornadoService.documentGeneration(AUTH_TOKEN, caseData, ENGLANDWALES_CASE_TYPE_ID,
                        caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null)
        );
    }

    @Test
    void shouldCreateDocumentInfoForDocumentGeneration() throws IOException {
        mockConnectionSuccess();
        CaseData caseData = new CaseData();

        DocumentInfo documentInfo = tornadoService.documentGeneration(AUTH_TOKEN, caseData, ENGLANDWALES_CASE_TYPE_ID,
                caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    void shouldCreateDocumentInfoForDocumentGenerationAllocatedOffice() throws IOException {
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

        DocumentInfo documentInfo = tornadoService.documentGeneration(AUTH_TOKEN, caseData, SCOTLAND_CASE_TYPE_ID,
                caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    void shouldCreateDocumentInfoForDocumentGenerationAllocatedOfficeMultiples() throws IOException {
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

        DocumentInfo documentInfo = tornadoService.documentGeneration(AUTH_TOKEN, caseData, SCOTLAND_CASE_TYPE_ID,
                caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), new MultipleData());

        verifyDocumentInfo(documentInfo);
    }

    @Test
    void shouldCreateDocumentInfoForListingGeneration() throws IOException {
        mockConnectionSuccess();
        ListingData listingData = createListingData();

        DocumentInfo documentInfo = tornadoService.listingGeneration(
            AUTH_TOKEN, listingData, ENGLANDWALES_LISTING_CASE_TYPE_ID);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    void shouldCreateDocumentInforForScheduleGeneration() throws IOException {
        mockConnectionSuccess();
        BulkData bulkData = new BulkData();
        bulkData.setScheduleDocName(LIST_CASES_CONFIG);
        bulkData.setSearchCollection(new ArrayList<>());

        DocumentInfo documentInfo = tornadoService.scheduleGeneration(
            AUTH_TOKEN, bulkData, ENGLANDWALES_LISTING_CASE_TYPE_ID);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    void shouldCreateDocumentInfoForReportGeneration() throws IOException {
        mockConnectionSuccess();
        ListingData listingData = createListingData();
        listingData.setReportType(CLAIMS_ACCEPTED_REPORT);

        DocumentInfo documentInfo = tornadoService.listingGeneration(AUTH_TOKEN, listingData,
                ENGLANDWALES_LISTING_CASE_TYPE_ID);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    void generateEt1VettingDocument() throws IOException {
        mockConnectionSuccess();
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                new CaseData(), AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, "ET1 Vetting.pdf");
        verifyDocumentInfo(documentInfo);
    }

    @Test
    void generateEt3VettingDocument() throws IOException {
        mockConnectionSuccess();
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                new CaseData(), AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, "ET3 Processing.pdf");
        verifyDocumentInfo(documentInfo);
    }

    @Test
    void generateInConEWDocument() throws IOException {
        mockConnectionSuccess();
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                new CaseData(), AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, "Initial Consideration.pdf");
        verifyDocumentInfo(documentInfo);
    }

    @Test
    void generateInConSCDocument() throws IOException {
        mockConnectionSuccess();
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                new CaseData(), AUTH_TOKEN, SCOTLAND_CASE_TYPE_ID, "Initial Consideration.pdf");
        verifyDocumentInfo(documentInfo);
    }

    @Test
    void generateDocument_exception() throws IOException {
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        assertThrows(IOException.class, () ->
                tornadoService.generateEventDocument(new CaseData(), AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID,
                        "random-string")
        );
    }

    @Test
    void generateDocument_noDocumentName() throws IOException {
        mockConnectionSuccess();

        assertThrows(IllegalArgumentException.class, () ->
                tornadoService.generateEventDocument(new CaseData(), AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, null)
        );
    }

    @Test
    void generateDocumentAsBytes() throws IOException {
        mockConnectionSuccess();
        byte[] bytes = tornadoService.generateEventDocumentBytes(new CaseData(), ENGLANDWALES_CASE_TYPE_ID, "Initial "
            + "Consideration.pdf");
        assertThat(bytes.length, is(0));
    }

    private void createUserService() {
        IdamApi idamApi = new IdamApi() {
            @Override
            public UserDetails retrieveUserDetails(String authorisation) {
                return HelperTest.getUserDetails();
            }

            @Override
            public UserDetails getUserByUserId(String authorisation, String userId) {
                return HelperTest.getUserDetails();
            }

            @Override
            public TokenResponse generateOpenIdToken(TokenRequest tokenRequest) {
                return null;
            }
        };

        mockOauth2Configuration();
        userService = new UserService(idamApi, oauth2Configuration);
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
        when(documentManagementService.generateMarkupDocument(anyString())).thenReturn(DOCUMENT_INFO_MARKUP);
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
        assertEquals(DOCUMENT_INFO_MARKUP, documentInfo.getMarkUp());
        assertEquals(SignificantItemType.DOCUMENT.name(), documentInfo.getType());
    }

    private void mockOauth2Configuration() {
        oauth2Configuration = mock(OAuth2Configuration.class);
        when(oauth2Configuration.getClientId()).thenReturn("111");
        when(oauth2Configuration.getClientSecret()).thenReturn("AAAAA");
        when(oauth2Configuration.getRedirectUri()).thenReturn("http://localhost:8080/test");
        when(oauth2Configuration.getClientScope()).thenReturn("roles");
    }
}
