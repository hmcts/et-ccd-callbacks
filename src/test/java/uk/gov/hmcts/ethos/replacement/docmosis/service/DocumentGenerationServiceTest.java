package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceScotType;
import uk.gov.hmcts.et.common.model.ccd.types.CorrespondenceType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
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
    private DocumentInfo documentInfo;

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
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
        documentGenerationService = new DocumentGenerationService(tornadoService);
        documentInfo = DocumentInfo.builder().description("resources/exampleV1.json").build();
        documentInfo.setMarkUp("Markup");
        documentInfo.setType("Document");
        documentInfo.setUrl("https://google.com");
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
    void processDocumentRequest() throws IOException {
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

    private CaseDetails generateCaseDetails(String jsonFileName) throws URISyntaxException, IOException {
        String json = new String(Files.readAllBytes(Paths.get(Objects
                .requireNonNull(Thread.currentThread().getContextClassLoader()
                        .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }
}
