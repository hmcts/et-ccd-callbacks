package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class InitialConsiderationServiceTest {
    static final String EXPECTED_RESPONDENT_NAME =
        "| Respondent name given | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|In ET1 by claimant | Test Corp|\r\n"
            + "|In ET3 by respondent | |";

    static final String EXPECTED_RESPONDENT_NAME_BLANK =
        "| Respondent name given | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|In ET1 by claimant | |\r\n"
            + "|In ET3 by respondent | |";

    static final String EXPECTED_HEARING_STRING =
        "|Hearing details | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Date | 01 Jul 2022|\r\n"
            + "|Type | Preliminary Hearing(CM)|\r\n"
            + "|Duration | 1.5 Hours|";

    static final String EXPECTED_HEARING_BLANK =
        "|Hearing details | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Date | -|\r\n"
            + "|Type | -|\r\n"
            + "|Duration | -|";

    static final String EXPECTED_JURISDICTION_HTML = "<h2>Jurisdiction codes</h2><a target=\"_blank\" "
        + "href=\"https://intranet.justice.gov.uk/documents/2017/11/jurisdiction-list.pdf\">View all "
        + "jurisdiction codes and descriptors (opens in new tab)</a><br><br><strong>DAG</strong> - "
        + "Discrimination, including indirect discrimination, harassment or victimisation or discrimination "
        + "based on association or perception on grounds of age<br><br><strong>SXD</strong> - Discrimination, "
        + "including indirect discrimination, discrimination based on association or perception, harassment "
        + "or victimisation on grounds of sex, marriage and civil partnership or gender reassignment<br><br><hr>";

    private static final String IC_SUMMARY_FILENAME = "InitialConsideration.pdf";

    CaseData caseDetailsEmpty;
    CaseData caseDetails;

    @Mock
    TornadoService tornadoService;

    InitialConsiderationService initialConsiderationService;

    @BeforeEach
    void setUp() throws Exception {
        caseDetails = generateCaseData("initialConsiderationCase1.json");
        caseDetailsEmpty = generateCaseData("initialConsiderationCase2.json");
        initialConsiderationService = new InitialConsiderationService(tornadoService);
    }

    @Test
    void getEarliestHearingDate() {
        assertThat(initialConsiderationService.getEarliestHearingDate(new ArrayList<>()))
            .isEmpty();
        assertThat(initialConsiderationService.getEarliestHearingDate(generateHearingDates()))
            .isEqualTo(Optional.of(LocalDate.of(2022, 1, 7)));
        assertThat(initialConsiderationService.getEarliestHearingDate(generateHearingDatesWithEmpty()))
            .isEqualTo(Optional.of(LocalDate.of(2022, 1, 7)));
    }

    @Test
    void getHearingDetailsTest() {
        String hearingDetails = initialConsiderationService.getHearingDetails(caseDetails.getHearingCollection());
        assertThat(hearingDetails)
            .isEqualTo(EXPECTED_HEARING_STRING);
    }

    @Test
    void getRespondentNameTest() {
        String respondentName = initialConsiderationService.getRespondentName(caseDetails.getRespondentCollection());
        assertThat(respondentName)
            .isEqualTo(EXPECTED_RESPONDENT_NAME);
    }

    @Test
    void generateJurisdictionCodesHtmlTest() {
        String jurisdictionCodesHtml =
            initialConsiderationService.generateJurisdictionCodesHtml(generateJurisdictionCodes());
        assertThat(jurisdictionCodesHtml)
            .isEqualTo(EXPECTED_JURISDICTION_HTML);
    }

    @Test
    void missingHearingCollectionTest() {
        String hearingDetails = initialConsiderationService.getHearingDetails(caseDetailsEmpty.getHearingCollection());
        assertThat(hearingDetails)
            .isEqualTo(EXPECTED_HEARING_BLANK);
    }

    @Test
    void missingJurisdictionCollectionTest() {
        String jurisdictionCodesHtml =
            initialConsiderationService.generateJurisdictionCodesHtml(caseDetailsEmpty.getJurCodesCollection());
        assertThat(jurisdictionCodesHtml)
            .isEmpty();
    }

    @Test
    void invalidJurisdictionCollectionTest() {
        String jurisdictionCodesHtml =
            initialConsiderationService.generateJurisdictionCodesHtml(generateInvalidJurisdictionCodes());
        assertThat(jurisdictionCodesHtml)
            .isEmpty();
    }

    @Test
    void invalidAndValidJurisdictionCollectionTest() {
        String jurisdictionCodesHtml =
            initialConsiderationService.generateJurisdictionCodesHtml(generateValidInvalidJurisdictionCodes());
        assertThat(jurisdictionCodesHtml)
            .isEqualTo(EXPECTED_JURISDICTION_HTML);
    }

    @Test
    void missingRespondentCollectionTest() {
        String respondentName =
            initialConsiderationService.getRespondentName(caseDetailsEmpty.getRespondentCollection());
        assertThat(respondentName)
            .isEqualTo(EXPECTED_RESPONDENT_NAME_BLANK);
    }

    private List<JurCodesTypeItem> generateJurisdictionCodes() {
        return List.of(generateJurisdictionCode("DAG"),
            generateJurisdictionCode("SXD"));
    }

    private List<JurCodesTypeItem> generateInvalidJurisdictionCodes() {
        return List.of(generateJurisdictionCode("PGA"),
            generateJurisdictionCode("CGA"));
    }

    private List<JurCodesTypeItem> generateValidInvalidJurisdictionCodes() {
        return List.of(generateJurisdictionCode("DAG"),
            generateJurisdictionCode("SXD"),
            generateJurisdictionCode("PGA"),
            generateJurisdictionCode("CGA"));
    }

    private JurCodesTypeItem generateJurisdictionCode(String codeString) {
        JurCodesTypeItem jurCodesTypeItem = new JurCodesTypeItem();
        JurCodesType code = new JurCodesType();
        code.setJuridictionCodesList(codeString);
        jurCodesTypeItem.setValue(code);

        return jurCodesTypeItem;
    }

    private CaseData generateCaseData(String fileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
            .getResource(fileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseData.class);
    }

    private List<DateListedTypeItem> generateHearingDates() {
        return List.of(createDate("2022-07-15T10:00:00.000"),
            createDate("2022-07-15T10:00:00.000"),
            createDate("2022-05-20T10:00:00.000"),
            createDate("2022-03-22T10:00:00.000"),
            createDate("2022-01-07T10:00:00.000"));
    }

    private List<DateListedTypeItem> generateHearingDatesWithEmpty() {
        return List.of(createDate("2022-07-15T10:00:00.000"),
            createDate("2022-07-15T10:00:00.000"),
            new DateListedTypeItem(),
            createDate("2022-03-22T10:00:00.000"),
            createDate("2022-01-07T10:00:00.000"));
    }

    private DateListedTypeItem createDate(String dateString) {
        DateListedTypeItem hearingDate = new DateListedTypeItem();
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(dateString);
        hearingDate.setValue(dateListedType);

        return hearingDate;
    }

    @Test
    void processSummaryDocument_Normal() throws IOException {
        DocumentInfo documentInfo = new DocumentInfo();
        when(tornadoService.summaryGeneration(anyString(), any(), anyString()))
                .thenReturn(documentInfo);
        CaseData caseData = new CaseData();
        assertThat(initialConsiderationService.processSummaryDocument(caseData, "caseTypeId", "authToken"))
                .isEqualTo(documentInfo);
    }

    @Test
    void processSummaryDocument_Exception() throws IOException {
        when(tornadoService.summaryGeneration(anyString(), any(), anyString()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        CaseData caseData = new CaseData();
        assertThrows(DocumentManagementException.class, () ->
                initialConsiderationService.processSummaryDocument(caseData, "caseTypeId", "authToken"));
    }

    @Test
    void addIcEwDocumentLink_NoDocumentCollection() {
        CaseData caseData = new CaseData();
        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo.setUrl("http://dm-store:8080/documents/9c6cc92e-1eea-430b-8583-a1e71508b2a1/binary");

        List<DocumentTypeItem> expectDocumentCollection = new ArrayList<>();
        expectDocumentCollection.add(createDocumentTypeItem("9c6cc92e-1eea-430b-8583-a1e71508b2a1"));

        initialConsiderationService.addIcEwDocumentLink(caseData, documentInfo);
        assertThat(caseData.getDocumentCollection())
                .isEqualTo(expectDocumentCollection);
    }

    @Test
    void addIcEwDocumentLink_HaveDocumentCollection() {
        List<DocumentTypeItem> documentCollection = new ArrayList<>();
        documentCollection.add(createDocumentTypeItem("9c6cc92e-1eea-430b-8583-a1e71508b2a1"));
        CaseData caseData = new CaseData();
        caseData.setDocumentCollection(documentCollection);
        DocumentInfo documentInfo = new DocumentInfo();
        documentInfo.setUrl("http://dm-store:8080/documents/1c67f047-72ee-48a5-a39c-441b5080b264/binary");

        List<DocumentTypeItem> expectDocumentCollection = new ArrayList<>();
        expectDocumentCollection.add(createDocumentTypeItem("9c6cc92e-1eea-430b-8583-a1e71508b2a1"));
        expectDocumentCollection.add(createDocumentTypeItem("1c67f047-72ee-48a5-a39c-441b5080b264"));

        initialConsiderationService.addIcEwDocumentLink(caseData, documentInfo);
        assertThat(caseData.getDocumentCollection())
                .isEqualTo(expectDocumentCollection);
    }

    private DocumentTypeItem createDocumentTypeItem(String docUrl) {
        UploadedDocumentType uploadedDocumentType = new UploadedDocumentType();
        uploadedDocumentType.setDocumentBinaryUrl("null/documents/" + docUrl + "/binary");
        uploadedDocumentType.setDocumentFilename(IC_SUMMARY_FILENAME);
        uploadedDocumentType.setDocumentUrl("null/documents/" + docUrl);

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(uploadedDocumentType);

        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(docUrl);
        documentTypeItem.setValue(documentType);

        return documentTypeItem;
    }
}
