package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingDetails;
import uk.gov.hmcts.et.common.model.listing.ListingRequest;
import uk.gov.hmcts.et.common.model.listing.items.ListingTypeItem;
import uk.gov.hmcts.et.common.model.listing.types.ListingType;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.CasesAwaitingJudgmentReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.casesawaitingjudgment.ReportSummary;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.hearingstojudgments.HearingsToJudgmentsReportSummary;
import uk.gov.hmcts.ethos.replacement.docmosis.reports.memberdays.MemberDaysReportData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.GenerateReportService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ListingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PrintHearingListService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MEMBER_DAYS_REPORT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_LISTING_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.CASES_AWAITING_JUDGMENT_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.reports.Constants.HEARINGS_TO_JUDGEMENTS_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(ListingGenerationController.class)
@ContextConfiguration(classes = DocmosisApplication.class)
class ListingGenerationControllerTest extends BaseControllerTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String LISTING_CASE_CREATION_URL = "/listingCaseCreation";
    private static final String LISTING_HEARINGS_URL = "/listingHearings";
    private static final String GENERATE_HEARING_DOCUMENT_URL = "/generateHearingDocument";
    private static final String GENERATE_HEARING_DOCUMENT_CONFIRMATION_URL = "/generateHearingDocumentConfirmation";
    private static final String LISTING_SINGLE_CASES_URL = "/listingSingleCases";
    private static final String GENERATE_LISTINGS_DOC_SINGLE_CASES_URL = "/generateListingsDocSingleCases";
    private static final String GENERATE_LISTINGS_DOC_SINGLE_CASES_CONFIRMATION_URL =
            "/generateListingsDocSingleCasesConfirmation";
    private static final String GENERATE_REPORT_URL = "/generateReport";
    private static final String INIT_PRINT_HEARING_LISTS_URL = "/initPrintHearingLists";
    private static final String INIT_GENERATE_REPORT_URL = "/initGenerateReport";
    private static final String DYNAMIC_LISTING_VENUE = "/dynamicListingVenue";
    private static final String AUTHORIZATION = "Authorization";
    
    @Autowired
    private WebApplicationContext applicationContext;

    @MockBean
    private ListingService listingService;

    @MockBean
    private ReportDataService reportDataService;

    @MockBean
    private DefaultValuesReaderService defaultValuesReaderService;

    @MockBean
    private PrintHearingListService printHearingListService;

    @MockBean
    private GenerateReportService generateReportService;

    private MockMvc mvc;
    private JsonNode requestContent;
    private JsonNode requestContent1;
    private JsonNode requestContentSingleCase;
    private JsonNode requestContentSingleCase1;
    private ListingDetails listingDetails;
    private CaseData caseData;
    private DocumentInfo documentInfo;
    private DefaultValues defaultValues;
    private ListingRequest singleListingRequest;
    private ListingRequest listingRequest;

    private void doRequestSetUp() throws IOException, URISyntaxException {
        ObjectMapper objectMapper = new ObjectMapper();
        requestContent = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleListingV1.json")).toURI()));
        requestContent1 = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleListingV2.json")).toURI()));
        requestContentSingleCase = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleListingSingleV1.json")).toURI()));
        requestContentSingleCase1 = objectMapper.readTree(new File(Objects.requireNonNull(getClass()
                .getResource("/exampleListingSingleV2.json")).toURI()));

        listingRequest = generateListingDetails("exampleListingV3.json");
        singleListingRequest = generateListingDetails("exampleListingV2.json");
    }

    private ListingRequest generateListingDetails(String jsonFileName) throws IOException, URISyntaxException {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(
                Thread.currentThread().getContextClassLoader().getResource(jsonFileName)).toURI())));

        return new ObjectMapper().readValue(json, ListingRequest.class);
    }

    private ListingRequest getListingData() {

        ListingData listingData = new ListingData();
        listingData.setDocMarkUp("Test doc markup");
        listingData.setDocumentName("test listing doc name");
        listingData.setListingDate("2021-10-20");
        listingData.setListingDateFrom("2020-11-12");
        listingData.setHearingDateType("Range");
        listingData.setListingDateTo("2021-10-18");
        listingData.setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());

        ListingTypeItem listingTypeItem1 = new ListingTypeItem();
        listingTypeItem1.setId("97087d19-795a-4886-8cdb-06489b8b2ef5");

        ListingType listingTypeValues = new ListingType();
        listingTypeValues.setCauseListTime("12 October 2020");
        listingTypeValues.setCauseListTime("00:00");
        listingTypeValues.setCauseListVenue("Manchester");
        listingTypeValues.setElmoCaseReference("1112");
        listingTypeValues.setJurisdictionCodesList("ADG, COM");
        listingTypeValues.setHearingType("Hearing");
        listingTypeValues.setPositionType("Manually Created");
        listingTypeItem1.setValue(listingTypeValues);

        ListingTypeItem listingTypeItem2 = new ListingTypeItem();
        listingTypeItem2.setId("97087d19-795a-4886-8cdb-46089b8b27ef");

        ListingType listingTypeValues2 = new ListingType();
        listingTypeValues2.setCauseListTime("12 October 2020");
        listingTypeValues2.setCauseListTime("00:00");
        listingTypeValues2.setCauseListVenue("Manchester");
        listingTypeValues2.setElmoCaseReference("1135");
        listingTypeValues2.setJurisdictionCodesList("ADG, COM");
        listingTypeValues2.setHearingType("Preliminary Hearing (CM)");
        listingTypeValues2.setPositionType("Manually Created");
        listingTypeItem1.setValue(listingTypeValues2);

        List<ListingTypeItem> listingCollection = new ArrayList<>();
        listingCollection.add(listingTypeItem1);
        listingCollection.add(listingTypeItem2);

        listingData.setListingCollection(listingCollection);

        listingData = new ListingData();
        listingData.setTribunalCorrespondenceDX("DX");
        listingData.setTribunalCorrespondenceEmail("m@m.com");
        listingData.setTribunalCorrespondenceFax("100300200");
        listingData.setTribunalCorrespondenceTelephone("077123123");

        Address address = new Address();
        address.setAddressLine1("AddressLine1");
        address.setAddressLine2("AddressLine2");
        address.setAddressLine3("AddressLine3");
        address.setPostTown("Manchester");
        address.setCountry("UK");
        address.setPostCode("L1 122");
        listingData.setTribunalCorrespondenceAddress(address);

        List<ListingTypeItem> listingTypeItems = new ArrayList<>();
        listingTypeItems.add(new ListingTypeItem());
        ListingDetails listingDetails = new ListingDetails();
        listingData.setListingCollection(listingTypeItems);
        listingDetails.setCaseData(listingData);
        singleListingRequest = new ListingRequest();
        singleListingRequest.setCaseDetails(listingDetails);

        return singleListingRequest;
    }

    @BeforeEach
    @Override
    @SneakyThrows
    public void setUp() {
        super.setUp();
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        doRequestSetUp();
        listingDetails = new ListingDetails();
        listingDetails.setCaseTypeId(SCOTLAND_LISTING_CASE_TYPE_ID);
        listingDetails.setCaseData(getListingData().getCaseDetails().getCaseData());

        documentInfo = new DocumentInfo();
        documentInfo.setMarkUp("Test doc markup");
        caseData = new CaseData();
        caseData.setPrintHearingDetails(getListingData().getCaseDetails().getCaseData());

        defaultValues = DefaultValues.builder()
                .positionType("Awaiting ET3")
                .claimantTypeOfClaimant("Individual")
                .managingOffice("Glasgow")
                .caseType(SINGLE_CASE_TYPE)
                .tribunalCorrespondenceAddressLine1("")
                .tribunalCorrespondenceAddressLine2("")
                .tribunalCorrespondenceAddressLine3("")
                .tribunalCorrespondenceTown("")
                .tribunalCorrespondencePostCode("")
                .tribunalCorrespondenceTelephone("3577131270")
                .tribunalCorrespondenceFax("7577126570")
                .tribunalCorrespondenceDX("123456")
                .tribunalCorrespondenceEmail("manchester@gmail.com")
                .build();
    }

    @Test
    @SneakyThrows
    void listingCaseCreation() {
        when(listingService.listingCaseCreation(isA(ListingDetails.class)))
                .thenReturn(singleListingRequest.getCaseDetails().getCaseData());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(LISTING_CASE_CREATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void listingHearings() {
        when(listingService.processListingHearingsRequest(isA(ListingDetails.class), eq(AUTH_TOKEN)))
                .thenReturn(listingDetails.getCaseData());
        when(defaultValuesReaderService.getListingDefaultValues(isA(ListingDetails.class)))
                .thenReturn(defaultValues);
        when(defaultValuesReaderService.getListingData(isA(ListingData.class), isA(DefaultValues.class)))
                .thenReturn(singleListingRequest.getCaseDetails().getCaseData());
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(LISTING_HEARINGS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateHearingDocument() {
        when(listingService.processHearingDocument(isA(ListingData.class), isA(String.class), eq(AUTH_TOKEN)))
                .thenReturn(documentInfo);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_HEARING_DOCUMENT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void dynamicListingVenue() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(DYNAMIC_LISTING_VENUE)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateHearingDocumentConfirmation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_HEARING_DOCUMENT_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateHearingDocumentWithErrors() {
        when(listingService.processHearingDocument(isA(ListingData.class), isA(String.class), eq(AUTH_TOKEN)))
                .thenReturn(documentInfo);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_HEARING_DOCUMENT_URL)
                .content(requestContent1.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void listingSingleCases() {
        when(listingService.processListingSingleCasesRequest(isA(CaseDetails.class))).thenReturn(caseData);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(LISTING_SINGLE_CASES_URL)
                .content(requestContentSingleCase.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateListingsDocSingleCases() {
        when(listingService.setManagingOfficeAndCourtAddressFromCaseData(isA(CaseData.class)))
                .thenReturn(singleListingRequest.getCaseDetails().getCaseData());
        when(listingService.processHearingDocument(isA(ListingData.class), isA(String.class), eq(AUTH_TOKEN)))
                .thenReturn(documentInfo);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_LISTINGS_DOC_SINGLE_CASES_URL)
                .content(requestContentSingleCase.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateListingsDocSingleCasesConfirmation() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_LISTINGS_DOC_SINGLE_CASES_CONFIRMATION_URL)
                .content(requestContentSingleCase.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateListingsDocSingleCasesWithErrors() {
        when(listingService.processHearingDocument(isA(ListingData.class), isA(String.class), eq(AUTH_TOKEN)))
                .thenReturn(documentInfo);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_LISTINGS_DOC_SINGLE_CASES_URL)
                .content(requestContentSingleCase1.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateReportOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(reportDataService.generateReportData(isA(ListingDetails.class), eq(AUTH_TOKEN)))
                .thenReturn(listingRequest.getCaseDetails().getCaseData());
        when(listingService.processHearingDocument(isA(ListingData.class),
                isA(String.class), eq(AUTH_TOKEN)))
                .thenReturn(documentInfo);
        mvc.perform(post(GENERATE_REPORT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateHearingsToJudgmentsReportOk() {
        HearingsToJudgmentsReportSummary reportSummary = new HearingsToJudgmentsReportSummary(
            TribunalOffice.LEEDS.getOfficeName());
        HearingsToJudgmentsReportData reportData = new HearingsToJudgmentsReportData(reportSummary);
        reportData.setReportType(HEARINGS_TO_JUDGEMENTS_REPORT);

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(reportDataService.generateReportData(isA(ListingDetails.class), eq(AUTH_TOKEN)))
                .thenReturn(reportData);
        when(listingService.processHearingDocument(isA(ListingData.class),
                isA(String.class), eq(AUTH_TOKEN)))
                .thenReturn(documentInfo);
        mvc.perform(post(GENERATE_REPORT_URL)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateCasesAwaitingJudgmentReportOk() {
        ReportSummary reportSummary = new ReportSummary(ENGLANDWALES_CASE_TYPE_ID);
        CasesAwaitingJudgmentReportData reportData = new CasesAwaitingJudgmentReportData(reportSummary);
        reportData.setReportType(CASES_AWAITING_JUDGMENT_REPORT);

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(reportDataService.generateReportData(isA(ListingDetails.class), eq(AUTH_TOKEN)))
                .thenReturn(reportData);
        when(listingService.processHearingDocument(isA(ListingData.class),
                isA(String.class), eq(AUTH_TOKEN)))
                .thenReturn(documentInfo);
        mvc.perform(post(GENERATE_REPORT_URL)
                        .content(requestContent.toString())
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void generateCMemberDaysReportOk() {
        MemberDaysReportData reportData = new MemberDaysReportData();
        reportData.setReportType(MEMBER_DAYS_REPORT);

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN))
            .thenReturn(true);
        when(reportDataService.generateReportData(isA(ListingDetails.class), eq(AUTH_TOKEN)))
            .thenReturn(reportData);
        when(listingService.processHearingDocument(isA(ListingData.class),
            isA(String.class), eq(AUTH_TOKEN)))
            .thenReturn(documentInfo);

        mvc.perform(post(GENERATE_REPORT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void initGenerateReportOk() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mvc.perform(post(INIT_GENERATE_REPORT_URL)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestContent.toString()))
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(generateReportService, times(1)).initGenerateReport(any(ListingDetails.class));
    }

    @Test
    @SneakyThrows
    void generateReportError400() {
        mvc.perform(post(GENERATE_REPORT_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void generateReportError500() {
        when(reportDataService.generateReportData(isA(ListingDetails.class), eq(AUTH_TOKEN)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_REPORT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void generateReportForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(GENERATE_REPORT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void dynamicListingVenueError400() {
        mvc.perform(post(DYNAMIC_LISTING_VENUE)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void generateHearingDocumentError400() {
        mvc.perform(post(GENERATE_HEARING_DOCUMENT_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void listingCaseCreationError400() {
        mvc.perform(post(LISTING_CASE_CREATION_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void listingHearingsError400() {
        mvc.perform(post(LISTING_HEARINGS_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void listingSingleCasesError400() {
        mvc.perform(post(LISTING_SINGLE_CASES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void generateListingsDocSingleCasesError400() {
        mvc.perform(post(GENERATE_LISTINGS_DOC_SINGLE_CASES_URL)
                .content("error")
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @SneakyThrows
    void initGenerateReportError400() {
        mvc.perform(post(INIT_GENERATE_REPORT_URL)
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"error\""))
                .andExpect(status().isBadRequest());

        verify(generateReportService, never()).initGenerateReport(any(ListingDetails.class));
    }

    @Test
    @SneakyThrows
    void listingCaseCreationError500() {
        when(listingService.listingCaseCreation(isA(ListingDetails.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(LISTING_CASE_CREATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void listingSingleCasesError500() {
        when(listingService.processListingSingleCasesRequest(isA(CaseDetails.class)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(LISTING_SINGLE_CASES_URL)
                .content(requestContentSingleCase.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void generateListingsDocSingleCasesError500() {
        when(listingService.processHearingDocument(isA(ListingData.class), isA(String.class), eq(AUTH_TOKEN)))
                .thenThrow(new InternalException(ERROR_MESSAGE));

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(listingService.setManagingOfficeAndCourtAddressFromCaseData(isA(CaseData.class)))
                .thenReturn(singleListingRequest.getCaseDetails().getCaseData());

        mvc.perform(post(GENERATE_LISTINGS_DOC_SINGLE_CASES_URL)
                .content(requestContentSingleCase.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void generateHearingDocumentError500() {
        when(listingService.processHearingDocument(isA(ListingData.class), isA(String.class), eq(AUTH_TOKEN)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(GENERATE_HEARING_DOCUMENT_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void listingHearingsError500() {
        when(listingService.processListingHearingsRequest(isA(ListingDetails.class), eq(AUTH_TOKEN)))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(LISTING_HEARINGS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @SneakyThrows
    void initGenerateReportError500() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doThrow(new InternalException(ERROR_MESSAGE)).when(generateReportService).initGenerateReport(
                isA(ListingDetails.class));

        mvc.perform(post(INIT_GENERATE_REPORT_URL)
                        .header(AUTHORIZATION, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent.toString()))
                .andExpect(status().isInternalServerError());

        verify(generateReportService, times(1)).initGenerateReport(any(ListingDetails.class));
    }

    @Test
    @SneakyThrows
    void listingCaseCreationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(LISTING_CASE_CREATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void listingHearingsForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(LISTING_HEARINGS_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void generateHearingDocumentConfirmationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(GENERATE_HEARING_DOCUMENT_CONFIRMATION_URL)
                .content(requestContent.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void generateHearingDocumentWithErrorsForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(GENERATE_HEARING_DOCUMENT_URL)
                .content(requestContent1.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void listingSingleCasesForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(LISTING_SINGLE_CASES_URL)
                .content(requestContentSingleCase.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void generateListingsDocSingleCasesForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(GENERATE_LISTINGS_DOC_SINGLE_CASES_URL)
                .content(requestContentSingleCase.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void generateListingsDocSingleCasesConfirmationForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(GENERATE_LISTINGS_DOC_SINGLE_CASES_CONFIRMATION_URL)
                .content(requestContentSingleCase.toString())
                .header(AUTHORIZATION, AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void initPrintHearingLists() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mvc.perform(post(INIT_PRINT_HEARING_LISTS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(requestContentSingleCase.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(printHearingListService, times(1)).initPrintHearingLists(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void initPrintHearingListsForbidden() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mvc.perform(post(INIT_PRINT_HEARING_LISTS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content(requestContentSingleCase.toString()))
                .andExpect(status().isForbidden());

        verify(printHearingListService, never()).initPrintHearingLists(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void initPrintHearingListsError400() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mvc.perform(post(INIT_PRINT_HEARING_LISTS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, AUTH_TOKEN)
                .content("\"bad-content\""))
                .andExpect(status().isBadRequest());

        verify(printHearingListService, never()).initPrintHearingLists(any(CaseData.class));
    }

    @Test
    @SneakyThrows
    void initGenerateReportForbidden() {
        mvc.perform(post(INIT_GENERATE_REPORT_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestContent.toString()))
                .andExpect(status().isForbidden());

        verify(generateReportService, never()).initGenerateReport(any(ListingDetails.class));
    }
}
