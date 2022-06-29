package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InitialConsiderationHelperTest {

    private CaseData caseDetails;

    private static final String EXPECTED_RESPONDENT_NAME =
            "| Respondent name given | |\r\n"
                    + "|-------------|:------------|\r\n"
                    + "|In Et1 by claimant | Test Corp|\r\n"
                    + "|In Et3 by Respondent | |";

    private static final String EXPECTED_HEARING_STRING =
            "|Hearing Details | |\r\n"
                    + "|-------------|:------------|\r\n"
                    + "|Date | 01 Jul 2022|\r\n"
                    + "|Type | Preliminary Hearing(CM)|\r\n"
                    + "|Duration | 1.5 Hours|";

    private static final String EXPECTED_JURISDICTION_HTML = "<h2>Jurisdiction Codes</h2><a target=\"_blank\" " +
            "href=\"https://intranet.justice.gov.uk/documents/2017/11/jurisdiction-list.pdf\">View all jurisdiction codes and descriptors (opens in new tab)</a><br><br>" +
            "<strong>DAG</strong> - Discrimination, including indirect discrimination, harassment or victimisation or discrimination based on association or perception on " +
            "grounds of age<br><br><strong>SXD</strong> - Discrimination, including indirect discrimination, discrimination based on association or perception, harassment " +
            "or victimisation on grounds of sex, marriage and civil partnership or gender reassignment<br><br><hr>";

    @BeforeEach
    public void setUp() throws Exception {
        caseDetails = generateCaseData();
    }

    @Test
    public void getEarliestHearingDate() {
        assertEquals(Optional.empty(), InitialConsiderationHelper.getEarliestHearingDate(new ArrayList<>()));
        assertEquals(Optional.of(LocalDate.of(2022, 1, 7)), InitialConsiderationHelper.getEarliestHearingDate(generateHearingDates()));
        assertEquals(Optional.of(LocalDate.of(2022, 1, 7)), InitialConsiderationHelper.getEarliestHearingDate(generateHearingDatesWithEmpty()));
    }

    @Test
    public void getHearingDetailsTest() {
        String hearingDetails = InitialConsiderationHelper.getHearingDetails(caseDetails.getHearingCollection());
        assertEquals(EXPECTED_HEARING_STRING, hearingDetails);
    }

    @Test
    public void getRespondentNameTest() {
        String respondentName = InitialConsiderationHelper.getRespondentName(caseDetails.getRespondentCollection());
        assertEquals(EXPECTED_RESPONDENT_NAME, respondentName);
    }

    @Test
    public void generateJurisdictionCodesHtmlTest() {
        String jurisdictionCodesHtml = InitialConsiderationHelper.generateJurisdictionCodesHtml(generateJurisdictionCodes());
        System.out.println(jurisdictionCodesHtml);
        assertEquals(EXPECTED_JURISDICTION_HTML, jurisdictionCodesHtml);
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


    private CaseData generateCaseData() throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource("initialConsiderationCase1.json")).toURI())));
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
