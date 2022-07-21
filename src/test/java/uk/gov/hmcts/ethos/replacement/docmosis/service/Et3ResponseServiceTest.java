package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.SignificantItemType;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;

public class Et3ResponseServiceTest {
    private TornadoService tornadoService;
    private TornadoConnection tornadoConnection;
    private DocumentManagementService documentManagementService;
    private DefaultValuesReaderService defaultValuesReaderService;
    private VenueAddressReaderService venueAddressReaderService;
    private UserService userService;
    private MockHttpURLConnection mockConnection;
    private final String authToken = "a-test-auth-token";
    private final String documentInfoMarkup = "<a>some test markup</a>";

    @Before
    public void setUp() throws IOException {
        mockTornadoConnection();
        mockDocumentManagement();
        mockDefaultValuesReaderService();
        mockVenueAddressReaderService();

        tornadoService = new TornadoService(tornadoConnection, documentManagementService,
            userService, defaultValuesReaderService, venueAddressReaderService);
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

    private void createUserService() {
        UserDetails userDetails = HelperTest.getUserDetails();
        IdamApi idamApi = authorisation -> userDetails;
        userService = new UserService(idamApi);
    }

    @Test(expected = IOException.class)
    public void createEt3ResponseFormNoTornadoConnectionShouldThrowException() throws IOException {
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);
        CaseData caseData = new CaseData();
        tornadoService.createEt3ResponseForm(authToken, caseData, ENGLANDWALES_LISTING_CASE_TYPE_ID);
    }

    @Test
    public void shouldCreateDocumentInfoForCreateEt3ResponseForm() throws IOException {
        mockConnectionSuccess();
        CaseData caseData = new CaseData();

        DocumentInfo documentInfo = tornadoService.createEt3ResponseForm(
            authToken, caseData, ENGLANDWALES_LISTING_CASE_TYPE_ID);

        verifyDocumentInfo(documentInfo);
    }

    private void verifyDocumentInfo(DocumentInfo documentInfo) {
        assertEquals(documentInfoMarkup, documentInfo.getMarkUp());
        assertEquals(SignificantItemType.DOCUMENT.name(), documentInfo.getType());
    }

}
