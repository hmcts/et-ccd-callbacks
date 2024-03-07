package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.BulkData;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.items.ListingTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.ListingType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.config.OAuth2Configuration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1VettingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3ResponseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et3VettingHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentTellSomethingElseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.SignificantItemType;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMS_ACCEPTED_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_DOC_ETCL;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_ETCL_STAFF;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LETTER_ADDRESS_ALLOCATED_OFFICE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LIST_CASES_CONFIG;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;

@ExtendWith(SpringExtension.class)
class TornadoServiceTest {
    @MockBean
    private TseService tseService;
    private TornadoService tornadoService;
    private TornadoConnection tornadoConnection;
    private DocumentManagementService documentManagementService;
    private UserIdamService userIdamService;
    private DefaultValuesReaderService defaultValuesReaderService;
    private VenueAddressReaderService venueAddressReaderService;
    private MockHttpURLConnection mockConnection;
    private OAuth2Configuration oauth2Configuration;

    private static final String AUTH_TOKEN = "a-test-auth-token";
    private static final String DOCUMENT_INFO_MARKUP = "<a>some test markup</a>";
    private static final String DUMMY_PDF = "dummy.pdf";
    private static final String ET1_VETTING_PDF = "ET1 Vetting.pdf";
    private static final String TSE_ADMIN_REPLY_PDF = "TSE Admin Reply.pdf";
    private static final String ET3_PROCESSING_PDF = "ET3 Processing.pdf";
    private static final String INITIAL_CONSIDERATION_PDF = "Initial Consideration.pdf";

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        createUserService();
        mockTornadoConnection();
        mockDocumentManagement();
        mockDefaultValuesReaderService();
        mockVenueAddressReaderService();

        tornadoService = new TornadoService(tornadoConnection, documentManagementService,
                userIdamService, defaultValuesReaderService, venueAddressReaderService, tseService);
    }

    @Test
    @SneakyThrows
    void documentGenerationNoTornadoConnectionShouldThrowException() {
        CaseData caseData = new CaseData();
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        assertThrows(IOException.class, () ->
                tornadoService.documentGeneration(AUTH_TOKEN, caseData, ENGLANDWALES_CASE_TYPE_ID,
                        caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null)
        );
    }

    @Test
    @SneakyThrows
    void listingGenerationNoTornadoConnectionShouldThrowException() {
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        assertThrows(IOException.class, () ->
                tornadoService.listingGeneration(AUTH_TOKEN, createListingData(), ENGLANDWALES_LISTING_CASE_TYPE_ID)
        );
    }

    @Test
    @SneakyThrows
    void scheduleGenerationNoTornadoConnectionShouldThrowException() {
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        assertThrows(IOException.class, () ->
                tornadoService.scheduleGeneration(AUTH_TOKEN, createBulkData(), ENGLANDWALES_LISTING_CASE_TYPE_ID)
        );
    }

    @Test
    @SneakyThrows
    void shouldThrowExceptionWhenTornadoReturnsErrorResponse() {
        mockConnectionError();
        CaseData caseData = new CaseData();

        assertThrows(IOException.class, () ->
                tornadoService.documentGeneration(AUTH_TOKEN, caseData, ENGLANDWALES_CASE_TYPE_ID,
                        caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null)
        );
    }

    @Test
    @SneakyThrows
    void shouldCreateDocumentInfoForDocumentGeneration() {
        mockConnectionSuccess();
        CaseData caseData = new CaseData();

        DocumentInfo documentInfo = tornadoService.documentGeneration(AUTH_TOKEN, caseData, ENGLANDWALES_CASE_TYPE_ID,
                caseData.getCorrespondenceType(), caseData.getCorrespondenceScotType(), null);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    @SneakyThrows
    void shouldCreateDocumentInfoForDocumentGenerationAllocatedOffice() {
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
    @SneakyThrows
    void shouldCreateDocumentInfoForDocumentGenerationAllocatedOfficeMultiples() {
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
    @SneakyThrows
    void shouldCreateDocumentInfoForListingGeneration() {
        mockConnectionSuccess();
        ListingData listingData = createListingData();

        DocumentInfo documentInfo = tornadoService.listingGeneration(
            AUTH_TOKEN, listingData, ENGLANDWALES_LISTING_CASE_TYPE_ID);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    @SneakyThrows
    void shouldCreateDocumentInforForScheduleGeneration() {
        mockConnectionSuccess();
        BulkData bulkData = new BulkData();
        bulkData.setScheduleDocName(LIST_CASES_CONFIG);
        bulkData.setSearchCollection(new ArrayList<>());

        DocumentInfo documentInfo = tornadoService.scheduleGeneration(
            AUTH_TOKEN, bulkData, ENGLANDWALES_LISTING_CASE_TYPE_ID);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    @SneakyThrows
    void shouldCreateDocumentInfoForReportGeneration() {
        mockConnectionSuccess();
        ListingData listingData = createListingData();
        listingData.setReportType(CLAIMS_ACCEPTED_REPORT);

        DocumentInfo documentInfo = tornadoService.listingGeneration(AUTH_TOKEN, listingData,
                ENGLANDWALES_LISTING_CASE_TYPE_ID);

        verifyDocumentInfo(documentInfo);
    }

    @Test
    @SneakyThrows
    void generateEt1VettingDocument() {
        mockConnectionSuccess();
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                new CaseData(), AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, ET1_VETTING_PDF);
        verifyDocumentInfo(documentInfo);
    }

    @Test
    @SneakyThrows
    void generateTseAdminReplyDocument() {
        mockConnectionSuccess();
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                getCaseData(), AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, TSE_ADMIN_REPLY_PDF);
        verifyDocumentInfo(documentInfo);
    }

    @Test
    @SneakyThrows
    void generateEt3VettingDocument() {
        mockConnectionSuccess();
        CaseData caseData = new CaseData();
        caseData.setEt3ChooseRespondent(DynamicFixedListType.from("Test Code", "Test Label", true));
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                caseData, AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, ET3_PROCESSING_PDF);
        verifyDocumentInfo(documentInfo);
    }

    @Test
    @SneakyThrows
    void generateInConEWDocument() {
        mockConnectionSuccess();
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                new CaseData(), AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID, INITIAL_CONSIDERATION_PDF);
        verifyDocumentInfo(documentInfo);
    }

    @Test
    @SneakyThrows
    void generateInConSCDocument() {
        mockConnectionSuccess();
        DocumentInfo documentInfo = tornadoService.generateEventDocument(
                new CaseData(), AUTH_TOKEN, SCOTLAND_CASE_TYPE_ID, INITIAL_CONSIDERATION_PDF);
        verifyDocumentInfo(documentInfo);
    }

    @Test
    @SneakyThrows
    void generateDocument_exception() {
        when(tornadoConnection.createConnection()).thenThrow(IOException.class);

        assertThrows(IOException.class, () ->
                tornadoService.generateEventDocument(new CaseData(), AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID,
                        "random-string")
        );
    }

    @Test
    @SneakyThrows
    void generateDocument_noDocumentName() {
        mockConnectionSuccess();
        CaseData caseData = new CaseData();
        assertThrows(IllegalArgumentException.class, () ->
                tornadoService.generateEventDocument(caseData, AUTH_TOKEN, ENGLANDWALES_CASE_TYPE_ID,
                        null)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"ET1 Vetting.pdf", "ET3 Processing.pdf", "ET3 Response.pdf", "Initial Consideration.pdf",
                            "Contact the tribunal.pdf", "Referral Summary.pdf", "TSE Reply.pdf", "decision.pdf",
                            "dummy.pdf"})
    @SneakyThrows
    void generateDocumentAsBytesForTSE(String fileName) {
        try (MockedStatic<TseHelper> tseHelperMockedStatic = mockStatic(TseHelper.class);
             MockedStatic<Et1VettingHelper> et1VettingHelperMockedStatic = mockStatic(Et1VettingHelper.class);
             MockedStatic<Et3VettingHelper> et3VettingHelperMockedStatic = mockStatic(Et3VettingHelper.class);
             MockedStatic<Et3ResponseHelper> et3ResponseHelperMockedStatic = mockStatic(Et3ResponseHelper.class);
             MockedStatic<ReferralHelper> referralHelperMockedStatic = mockStatic(ReferralHelper.class);
             MockedStatic<RespondentTellSomethingElseHelper> respondentTellSomethingElseHelperMockedStatic =
                     mockStatic(RespondentTellSomethingElseHelper.class)) {
            mockConnectionSuccess();
            CaseData caseData = new CaseData();
            DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
            DynamicValueType dynamicValueType = new DynamicValueType();
            dynamicValueType.setLabel("Test label");
            dynamicFixedListType.setValue(dynamicValueType);
            caseData.setSubmitEt3Respondent(dynamicFixedListType);
            caseData.setEt3ChooseRespondent(dynamicFixedListType);
            tseHelperMockedStatic.when(() -> TseHelper.getDecisionDocument(caseData, tornadoConnection.getAccessKey(),
                            null))
                    .thenReturn("");
            tseHelperMockedStatic.when(() -> TseHelper.getReplyDocumentRequest(caseData,
                    tornadoConnection.getAccessKey(), null)).thenReturn("");
            et1VettingHelperMockedStatic.when(() -> Et1VettingHelper.getDocumentRequest(caseData,
                            tornadoConnection.getAccessKey())).thenReturn("");
            et3VettingHelperMockedStatic.when(() -> Et3VettingHelper.getDocumentRequest(caseData,
                    tornadoConnection.getAccessKey())).thenReturn("");
            et3ResponseHelperMockedStatic.when(() -> Et3ResponseHelper.getDocumentRequest(caseData,
                            tornadoConnection.getAccessKey())).thenReturn("");
            referralHelperMockedStatic.when(() -> ReferralHelper.getDocumentRequest(caseData,
                    tornadoConnection.getAccessKey())).thenReturn("");
            respondentTellSomethingElseHelperMockedStatic.when(() ->
                    RespondentTellSomethingElseHelper.getDocumentRequest(caseData,
                            tornadoConnection.getAccessKey())).thenReturn("");
            if (DUMMY_PDF.equals(fileName)) {
                assertThrows(IllegalArgumentException.class, () ->
                        tornadoService.generateEventDocumentBytes(caseData, ENGLANDWALES_CASE_TYPE_ID, fileName));
            } else {
                byte[] bytes = tornadoService.generateEventDocumentBytes(caseData, ENGLANDWALES_CASE_TYPE_ID, fileName);
                assertThat(bytes.length, is(0));
            }

        }
    }

    @Test
    @SneakyThrows
    void generateDocumentAsBytesForTSEReply() {
        try (MockedStatic<TseHelper> tseHelperMockedStatic = mockStatic(TseHelper.class)) {
            mockConnectionSuccess();
            CaseData caseData = new CaseData();
            DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
            DynamicValueType dynamicValueType = new DynamicValueType();
            dynamicValueType.setCode("testCode");
            dynamicFixedListType.setValue(dynamicValueType);
            caseData.setTseAdminSelectApplication(dynamicFixedListType);
            tseHelperMockedStatic.when(() -> TseHelper.getDecisionDocument(caseData, tornadoConnection.getAccessKey(),
                            null))
                    .thenReturn("");
            byte[] bytes = tornadoService.generateEventDocumentBytes(
                    new CaseData(),
                    ENGLANDWALES_CASE_TYPE_ID,
                    INITIAL_CONSIDERATION_PDF);
            assertThat(bytes.length, is(0));
        }
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
        userIdamService = new UserIdamService(idamApi, oauth2Configuration);
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
        inputStream.close();
        outputStream.close();
    }

    private void mockConnectionError() throws IOException {
        InputStream mockInputStream = mock(InputStream.class);
        when(mockInputStream.read(any(byte[].class))).thenReturn(-1);
        when(mockInputStream.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        OutputStream mockOutputStream = mock(OutputStream.class);
        mockConnection.setErrorStream(mockInputStream);
        mockConnection.setOutputStream(mockOutputStream);
        mockConnection.setResponseCode(HTTP_INTERNAL_ERROR);
        mockInputStream.close();
        mockOutputStream.close();
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

    private CaseData getCaseData() {
        CaseData caseData = new CaseData();
        GenericTseApplicationType build = TseApplicationBuilder.builder()
                .withApplicant(CLAIMANT_TITLE)
                .withDate("13 December 2022")
                .withDue("20 December 2022")
                .withType("Withdraw my claim")
                .withDetails("Text")
                .withNumber("1")
                .withResponsesCount("0")
                .withStatus(OPEN_STATE)
                .build();
        GenericTseApplicationTypeItem item = new GenericTseApplicationTypeItem();
        item.setValue(build);
        caseData.setGenericTseApplicationCollection(List.of(item));
        DynamicFixedListType flt = new DynamicFixedListType();
        DynamicValueType valueType = new DynamicValueType();
        valueType.setCode("1");
        valueType.setLabel("test lbl");
        flt.setValue(valueType);
        caseData.setTseAdminSelectApplication(flt);
        return caseData;
    }
}
