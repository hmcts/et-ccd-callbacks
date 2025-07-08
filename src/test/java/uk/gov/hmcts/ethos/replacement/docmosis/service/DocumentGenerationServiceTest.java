package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.bulk.BulkData;
import uk.gov.hmcts.et.common.model.bulk.BulkDetails;
import uk.gov.hmcts.et.common.model.bulk.BulkDocumentInfo;
import uk.gov.hmcts.et.common.model.bulk.BulkRequest;
import uk.gov.hmcts.et.common.model.bulk.items.SearchTypeItem;
import uk.gov.hmcts.et.common.model.bulk.types.SearchType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class DocumentGenerationServiceTest {

    @InjectMocks
    private DocumentGenerationService documentGenerationService;
    @Mock
    private TornadoService tornadoService;
    private CaseDetails caseDetailsScot1;
    private CaseDetails caseDetails9;
    private CaseDetails caseDetails10;
    private CaseDetails caseDetails11;
    private CaseDetails caseDetails12;
    private CaseDetails caseDetails13;
    private CaseDetails caseDetails14;
    private CaseDetails caseDetails15;
    private CCDRequest ccdRequest;
    private BulkRequest bulkRequest;
    private DocumentInfo documentInfo;
    private BulkDocumentInfo bulkDocumentInfo;
    @Mock
    private CcdClient ccdClient;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        caseDetailsScot1 = generateCaseDetails("caseDetailsScotTest1.json");
        caseDetails9 = generateCaseDetails("caseDetailsTest9.json");
        caseDetails10 = generateCaseDetails("caseDetailsTest10.json");
        caseDetails11 = generateCaseDetails("caseDetailsTest11.json");
        caseDetails12 = generateCaseDetails("caseDetailsTest12.json");
        caseDetails13 = generateCaseDetails("caseDetailsTest13.json");
        caseDetails14 = generateCaseDetails("caseDetailsTest14.json");
        caseDetails15 = generateCaseDetails("caseDetailsTest15.json");

        ccdRequest = new CCDRequest();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        ccdRequest.setCaseDetails(caseDetails);
        bulkRequest = new BulkRequest();
        BulkData bulkData = new BulkData();
        SearchType searchType = new SearchType();
        searchType.setCaseIDS("1");
        SearchTypeItem searchTypeItem = new SearchTypeItem();
        searchTypeItem.setValue(searchType);
        bulkData.setSearchCollection(new ArrayList<>(Collections.singletonList(searchTypeItem)));
        BulkDetails bulkDetails = new BulkDetails();
        bulkDetails.setCaseData(bulkData);
        bulkDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        bulkRequest.setCaseDetails(bulkDetails);
        documentGenerationService = new DocumentGenerationService(tornadoService, ccdClient);
        documentInfo = DocumentInfo.builder().description("resources/exampleV1.json").build();
        documentInfo.setMarkUp("Markup");
        documentInfo.setType("Document");
        documentInfo.setUrl("http://google.com");
        bulkDocumentInfo = new BulkDocumentInfo();
        bulkDocumentInfo.setMarkUps(documentInfo.getMarkUp());
        bulkDocumentInfo.setErrors(new ArrayList<>());
    }

    @Test
    void midAddressLabelsInvalidTemplateName() {
        CaseData caseData = caseDetails9.getCaseData();
        documentGenerationService.midAddressLabels(caseData);
        assertNull(caseData.getAddressLabelCollection());
    }

    @Test
    void midAddressLabelsCustomiseSelectedAddresses() {
        CaseData caseData = caseDetails10.getCaseData();
        documentGenerationService.midAddressLabels(caseData);
        assertEquals(5, caseData.getAddressLabelCollection().size());
    }

    @Test
    void midAddressLabelsCustomiseSelectedAddressesNoAddressLabelsSelectionFields() {
        CaseData caseData = caseDetails11.getCaseData();
        documentGenerationService.midAddressLabels(caseData);
        assertEquals(new ArrayList<>(), caseData.getAddressLabelCollection());
    }

    @Test
    void midAddressLabelsAllAvailableAddresses() {
        CaseData caseData = caseDetails12.getCaseData();
        documentGenerationService.midAddressLabels(caseData);
        assertEquals("Individual", caseData.getClaimantTypeOfClaimant());
        assertEquals("CLAIMANT : Mr A J Rodriguez",
                caseData.getAddressLabelCollection().getFirst().getValue().getFullName());
        assertEquals(6, caseData.getAddressLabelCollection().size());
    }

    @Test
    void midAddressLabelsAllAvailableAddressesMissingEntities() {
        CaseData caseData = caseDetails13.getCaseData();
        documentGenerationService.midAddressLabels(caseData);
        assertEquals("Company", caseData.getClaimantTypeOfClaimant());
        assertEquals("CLAIMANT : Orlando LTD",
                caseData.getAddressLabelCollection().getFirst().getValue().getFullName());
        assertEquals(1, caseData.getAddressLabelCollection().size());
    }

    @Test
    void setBfActionsEnglandOrWales() {
        documentInfo.setDescription("TemplateName_2.6");
        assertNull(caseDetails13.getCaseData().getBfActions());
        documentGenerationService.updateBfActions(documentInfo, caseDetails13.getCaseData());
        assertEquals(1, caseDetails13.getCaseData().getBfActions().size());
        assertEquals(YES, caseDetails13.getCaseData().getBfActions().getFirst().getValue().getLetters());
        assertEquals(LocalDate.now().toString(),
                caseDetails13.getCaseData().getBfActions().getFirst().getValue().getDateEntered());
        assertEquals(LocalDate.now().plusDays(29).toString(),
                caseDetails13.getCaseData().getBfActions().getFirst().getValue().getBfDate());
        assertEquals("Claim served",
                caseDetails13.getCaseData().getBfActions().getFirst().getValue().getAllActions());
        assertEquals("Other action",
                caseDetails13.getCaseData().getBfActions().getFirst().getValue().getCwActions());

    }

    @Test
    void setBfActionsScotland() {
        documentInfo.setDescription("TemplateName_72");
        CorrespondenceScotType correspondenceScotType = new CorrespondenceScotType();
        correspondenceScotType.setClaimantOrRespondent(CLAIMANT);
        correspondenceScotType.setHearingNumber("1");
        caseDetails13.getCaseData().setCorrespondenceType(null);
        caseDetails13.getCaseData().setCorrespondenceScotType(correspondenceScotType);
        caseDetails13.getCaseData().setCorrespondenceScotType(new CorrespondenceScotType());
        assertNull(caseDetails13.getCaseData().getBfActions());
        documentGenerationService.updateBfActions(documentInfo, caseDetails13.getCaseData());
        assertEquals(1, caseDetails13.getCaseData().getBfActions().size());
        assertEquals(YES, caseDetails13.getCaseData().getBfActions().getFirst().getValue().getLetters());
        assertEquals(LocalDate.now().toString(),
                caseDetails13.getCaseData().getBfActions().getFirst().getValue().getDateEntered());
        assertEquals(LocalDate.now().plusDays(29).toString(),
                caseDetails13.getCaseData().getBfActions().getFirst().getValue().getBfDate());
        assertEquals("Other action",
                caseDetails13.getCaseData().getBfActions().getFirst().getValue().getCwActions());
        assertEquals("Claim served",
                caseDetails13.getCaseData().getBfActions().getFirst().getValue().getAllActions());
        caseDetails13.getCaseData().setCorrespondenceScotType(null);
        CorrespondenceType backUp = caseDetails13.getCaseData().getCorrespondenceType();
        caseDetails13.getCaseData().setCorrespondenceType(backUp);
    }

    @Test
    void midAddressLabelsAllAvailableAddressesMissingClaimantType() {
        CaseData caseData = caseDetails14.getCaseData();
        documentGenerationService.midAddressLabels(caseData);
        assertEquals("Individual", caseData.getClaimantTypeOfClaimant());
        assertEquals("CLAIMANT : Mr A J Rodriguez",
                caseData.getAddressLabelCollection().getFirst().getValue().getFullName());
        assertEquals(6, caseData.getAddressLabelCollection().size());
    }

    @Test
    void midAddressLabelsAllAvailableAddressesMissingClaimantRepFields() {
        CaseData caseData = caseDetails15.getCaseData();
        documentGenerationService.midAddressLabels(caseData);
        assertEquals("Individual", caseData.getClaimantTypeOfClaimant());
        assertEquals("CLAIMANT : Mr A J Rodriguez",
                caseData.getAddressLabelCollection().getFirst().getValue().getFullName());
        assertEquals(6, caseData.getAddressLabelCollection().size());
    }

    static Stream<Arguments> addressLabelParams() {
        return Stream.of(
            Arguments.of("0.3", 1),
            Arguments.of("0.4", 1),
            Arguments.of("0.5", 2),
            Arguments.of("0.6", 3),
            Arguments.of("0.7", 1),
            Arguments.of("0.8", 4),
            Arguments.of("0.9", 4),
            Arguments.of("0.10", 2),
            Arguments.of("0.11", 2),
            Arguments.of("0.12", 4)
        );
    }

    @ParameterizedTest
    @MethodSource("addressLabelParams")
    void midAddressLabelsVariousAddressSelections(String part0Documents, int expectedSize) {
        CaseData caseData = caseDetails12.getCaseData();
        caseData.getCorrespondenceType().setPart0Documents(part0Documents);
        documentGenerationService.midAddressLabels(caseData);
        caseData.getCorrespondenceType().setPart0Documents("0.2");
        assertEquals(expectedSize, caseData.getAddressLabelCollection().size());
    }

    @Test
    void midSelectedAddressLabelsNullCollection() {
        CaseData caseData = caseDetails11.getCaseData();
        documentGenerationService.midSelectedAddressLabels(caseData);
        assertNull(caseData.getAddressLabelCollection());
    }

    @Test
    void midSelectedAddressLabelsFullCollection() {
        CaseData caseData = caseDetails12.getCaseData();
        documentGenerationService.midAddressLabels(caseData);
        documentGenerationService.midSelectedAddressLabels(caseData);
        assertEquals(6, caseData.getAddressLabelCollection().size());
    }

    @Test
    void midSelectedAddressLabelsEmptyCollection() {
        CaseData caseData = caseDetails13.getCaseData();
        caseData.getCorrespondenceType().setPart0Documents("0.4");
        documentGenerationService.midAddressLabels(caseData);
        documentGenerationService.midSelectedAddressLabels(caseData);
        assertEquals(0, caseData.getAddressLabelCollection().size());
    }

    @Test
    void midValidateAddressLabelsNoErrors() {
        CaseData caseData = caseDetails10.getCaseData();
        List<String> errors = documentGenerationService.midValidateAddressLabels(caseData);
        assertEquals(0, errors.size());
    }

    @Test
    void clearUserChoicesScotland() {
        CaseDetails caseDetails = caseDetailsScot1;
        caseDetails.setCaseTypeId(SCOTLAND_CASE_TYPE_ID);
        documentGenerationService.clearUserChoices(caseDetails);
        assertNull(caseDetails.getCaseData().getCorrespondenceScotType());
        assertNull(caseDetails.getCaseData().getAddressLabelsSelectionType());
        assertNull(caseDetails.getCaseData().getAddressLabelCollection());
        assertNull(caseDetails.getCaseData().getAddressLabelsAttributesType());
    }

    @Test
    void clearUserChoicesEngland() {
        CaseDetails caseDetails = caseDetails9;
        caseDetails.setCaseTypeId(ENGLANDWALES_CASE_TYPE_ID);
        documentGenerationService.clearUserChoices(caseDetails);
        assertNull(caseDetails.getCaseData().getCorrespondenceType());
        assertNull(caseDetails.getCaseData().getAddressLabelsSelectionType());
        assertNull(caseDetails.getCaseData().getAddressLabelCollection());
        assertNull(caseDetails.getCaseData().getAddressLabelsAttributesType());
    }

    @Test
    void clearUserChoicesForMultiplesScotland() {
        BulkDetails bulkDetails = bulkRequest.getCaseDetails();
        bulkDetails.setCaseTypeId(SCOTLAND_BULK_CASE_TYPE_ID);
        documentGenerationService.clearUserChoicesForMultiples(bulkDetails);
        assertNull(bulkDetails.getCaseData().getCorrespondenceScotType());
    }

    @Test
    void clearUserChoicesForMultiplesEngland() {
        BulkDetails bulkDetails = bulkRequest.getCaseDetails();
        bulkDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        documentGenerationService.clearUserChoicesForMultiples(bulkDetails);
        assertNull(bulkDetails.getCaseData().getCorrespondenceType());
    }

    @Test
    @SneakyThrows
    void processDocumentRequest() {
        when(tornadoService.documentGeneration(
                anyString(), any(), anyString(), any(), any(), any())).thenReturn(documentInfo);
        DocumentInfo documentInfo1 = documentGenerationService.processDocumentRequest(
                ccdRequest, "authToken");
        assertEquals(documentInfo, documentInfo1);
    }

    @Test
    void processDocumentRequestException() {
        assertThrows(Exception.class, () -> {
            when(tornadoService.documentGeneration(
                    anyString(), any(), anyString(), any(), any(), any()))
                    .thenThrow(new InternalException(ERROR_MESSAGE));
            documentGenerationService.processDocumentRequest(ccdRequest, "authToken");

        });
    }

    @Test
    @SneakyThrows
    void processBulkDocumentRequest() {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(1);
        submitEvent.setCaseData(new CaseData());
        List<SubmitEvent> submitEvents = Collections.singletonList(submitEvent);
        when(tornadoService.documentGeneration(
                anyString(), any(), anyString(), any(), any(), any())).thenReturn(documentInfo);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), any(), any())).thenReturn(submitEvents);
        BulkDocumentInfo bulkDocumentInfo1 = documentGenerationService.processBulkDocumentRequest(
                bulkRequest, "authToken");
        assertEquals(bulkDocumentInfo.toString(), bulkDocumentInfo1.toString());
    }

    @Test
    @SneakyThrows
    void processBulkDocumentRequestWithErrors() {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(1);
        submitEvent.setCaseData(new CaseData());
        bulkRequest.getCaseDetails().getCaseData().setSearchCollection(null);
        List<SubmitEvent> submitEvents = Collections.singletonList(submitEvent);
        when(tornadoService.documentGeneration(
                anyString(), any(), anyString(), any(), any(), any())).thenReturn(documentInfo);
        when(ccdClient.retrieveCasesElasticSearch(anyString(), any(), any())).thenReturn(submitEvents);

        BulkDocumentInfo bulkDocumentInfo1 = documentGenerationService.processBulkDocumentRequest(
                bulkRequest, "authToken");
        assertEquals("BulkDocumentInfo(markUps=, errors=[There are not cases searched to "
                + "generate letters], documentInfo=null)", bulkDocumentInfo1.toString());
    }

    @Test
    @SneakyThrows
    void processBulkDocumentRequestException() {
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(1);
        submitEvent.setCaseData(new CaseData());
        List<SubmitEvent> submitEvents = Collections.singletonList(submitEvent);
        when(tornadoService.documentGeneration(
                anyString(), any(), anyString(), any(), any(), any()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        when(ccdClient.retrieveCasesElasticSearch(anyString(), any(), any())).thenReturn(submitEvents);

        assertThrows(Exception.class, () ->
                documentGenerationService.processBulkDocumentRequest(bulkRequest, "authToken")
        );
    }

    @Test
    @SneakyThrows
    void processBulkScheduleRequest() {
        when(tornadoService.scheduleGeneration(anyString(), any(), anyString())).thenReturn(documentInfo);
        BulkDocumentInfo bulkDocumentInfo1 = documentGenerationService.processBulkScheduleRequest(
                bulkRequest, "authToken");
        assertEquals("BulkDocumentInfo(markUps=Markup, errors=[], documentInfo=DocumentInfo(type=Document, "
                        + "description=resources/exampleV1.json, url=http://google.com, markUp=Markup))",
                bulkDocumentInfo1.toString());
    }

    @Test
    @SneakyThrows
    void processBulkScheduleRequestWithErrors() {
        bulkRequest.getCaseDetails().getCaseData().setSearchCollection(null);
        when(tornadoService.scheduleGeneration(anyString(), any(), anyString())).thenReturn(documentInfo);
        BulkDocumentInfo bulkDocumentInfo1 = documentGenerationService.processBulkScheduleRequest(
                bulkRequest, "authToken");
        assertEquals("BulkDocumentInfo(markUps= , errors=[There are not cases searched "
                        + "to generate schedules], "
                        + "documentInfo=DocumentInfo(type=null, description=null, url=null, markUp=null))",
                bulkDocumentInfo1.toString());
    }

    @Test
    @SneakyThrows
    void processBulkScheduleRequestException() {
        when(tornadoService.scheduleGeneration(anyString(), any(), anyString()))
                .thenThrow(new InternalException(ERROR_MESSAGE));

        assertThrows(Exception.class, () ->
                documentGenerationService.processBulkScheduleRequest(bulkRequest, "authToken")
        );
    }

    @SneakyThrows
    private CaseDetails generateCaseDetails(String jsonFileName) {
        String json = new String(Files.readAllBytes(Paths.get(Objects
                .requireNonNull(Thread.currentThread().getContextClassLoader()
                        .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }
}
