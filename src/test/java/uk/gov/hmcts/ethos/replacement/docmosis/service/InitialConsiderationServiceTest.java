package uk.gov.hmcts.ethos.replacement.docmosis.service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;

public class InitialConsiderationServiceTest {
    static final String EXPECTED_RESPONDENT_NAME =
        "| Respondent name given | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|In Et1 by claimant | Test Corp|\r\n"
            + "|In Et3 by Respondent | |";

    static final String EXPECTED_RESPONDENT_NAME_BLANK =
        "| Respondent name given | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|In Et1 by claimant | |\r\n"
            + "|In Et3 by Respondent | |";

    static final String EXPECTED_HEARING_STRING =
        "|Hearing Details | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Date | 01 Jul 2022|\r\n"
            + "|Type | Preliminary Hearing(CM)|\r\n"
            + "|Duration | 1.5 Hours|";

    static final String EXPECTED_HEARING_BLANK =
        "|Hearing Details | |\r\n"
            + "|-------------|:------------|\r\n"
            + "|Date | - |\r\n"
            + "|Type | - |\r\n"
            + "|Duration | - |";

    static final String EXPECTED_JURISDICTION_HTML = "<h2>Jurisdiction Codes</h2><a target=\"_blank\" " +
        "href=\"https://intranet.justice.gov.uk/documents/2017/11/jurisdiction-list.pdf\">View all jurisdiction codes and descriptors (opens in new tab)</a><br><br>" +
        "<strong>DAG</strong> - Discrimination, including indirect discrimination, harassment or victimisation or discrimination based on association or perception on " +
        "grounds of age<br><br><strong>SXD</strong> - Discrimination, including indirect discrimination, discrimination based on association or perception, harassment " +
        "or victimisation on grounds of sex, marriage and civil partnership or gender reassignment<br><br><hr>";

    CaseData caseDetailsEmpty;
    CaseData caseDetails;
    InitialConsiderationService initialConsiderationService;


    @BeforeEach
    void setUp() throws Exception {
        caseDetails = generateCaseData("initialConsiderationCase1.json");
        caseDetailsEmpty = generateCaseData("initialConsiderationCase2.json");
        initialConsiderationService = new InitialConsiderationService();
    }

    @Test
    void getEarliestHearingDate() {
        assertEquals(Optional.empty(), initialConsiderationService.getEarliestHearingDate(new ArrayList<>()));
        assertEquals(Optional.of(LocalDate.of(2022, 1, 7)), initialConsiderationService.getEarliestHearingDate(generateHearingDates()));
        assertEquals(Optional.of(LocalDate.of(2022, 1, 7)), initialConsiderationService.getEarliestHearingDate(generateHearingDatesWithEmpty()));
    }

    @Test
    void getHearingDetailsTest() {
        String hearingDetails = initialConsiderationService.getHearingDetails(caseDetails.getHearingCollection());
        assertEquals(EXPECTED_HEARING_STRING, hearingDetails);
    }

    @Test
    void getRespondentNameTest() {
        String respondentName = initialConsiderationService.getRespondentName(caseDetails.getRespondentCollection());
        assertEquals(EXPECTED_RESPONDENT_NAME, respondentName);
    }

    @Test
    void generateJurisdictionCodesHtmlTest() {
        String jurisdictionCodesHtml = initialConsiderationService.generateJurisdictionCodesHtml(generateJurisdictionCodes());
        System.out.println(jurisdictionCodesHtml);
        assertEquals(EXPECTED_JURISDICTION_HTML, jurisdictionCodesHtml);
    }

    @Test
    void missingHearingCollectionTest() {
        String hearingDetails = initialConsiderationService.getHearingDetails(caseDetailsEmpty.getHearingCollection());
        assertEquals(EXPECTED_HEARING_BLANK, hearingDetails);
    }

    @Test
    void missingJurisdictionCollectionTest() {
        String jurisdictionCodesHtml = initialConsiderationService.generateJurisdictionCodesHtml(caseDetailsEmpty.getJurCodesCollection());
        assertEquals("", jurisdictionCodesHtml);
    }

    @Test
    void missingRespondentCollectionTest() {
        String respondentName = initialConsiderationService.getRespondentName(caseDetailsEmpty.getRespondentCollection());
        assertEquals(EXPECTED_RESPONDENT_NAME_BLANK, respondentName);
    }

    private List<JurCodesTypeItem> generateJurisdictionCodes() {
        return List.of(generateJurisdictionCode("DAG"),
            generateJurisdictionCode("SXD"));
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
}
