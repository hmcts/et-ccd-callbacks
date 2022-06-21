package uk.gov.hmcts.ethos.replacement.docmosis.helpers;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;

class Et3VettingHelperTest {

    private CaseDetails caseDetails1;

    @BeforeEach
    void setUp() throws Exception {
        caseDetails1 = generateCaseDetails("caseDetailsTest1.json");
    }

    @Test
    void givenET3Received_datesShouldShow() {
        caseDetails1.getCaseData().setClaimServedDate("2022-01-01");
        String actualResult = Et3VettingHelper.getEt3Dates(caseDetails1.getCaseData());
        String expectedResult = generateEt3Dates("1 January 2022", "30 January 2022", "1 March 2022");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenET3NotReceived_et3ShouldNotShow() {
        caseDetails1.getCaseData().setClaimServedDate("2022-01-01");
        caseDetails1.getCaseData().getRespondentCollection().get(0).getValue().setResponseReceived(NO);
        String actualResult = Et3VettingHelper.getEt3Dates(caseDetails1.getCaseData());
        String expectedResult = generateEt3Dates("1 January 2022", "30 January 2022", NO);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenET1isNull_et1ShouldShowError() {
        caseDetails1.getCaseData().setClaimServedDate(null);
        String actualResult = Et3VettingHelper.getEt3Dates(caseDetails1.getCaseData());
        String expectedResult = generateEt3Dates("Cannot find ET1 Served Date", "Cannot find ET3 Due Date", "1 March 2022");
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void givenRespondentCollectionEmpty() {
        caseDetails1.getCaseData().setRespondentCollection(null);
        String actualResult = Et3VettingHelper.getEt3Dates(caseDetails1.getCaseData());
        String expectedResult = generateEt3Dates("Cannot find ET1 Served Date", "Cannot find ET3 Due Date", "No");
        assertEquals(expectedResult, actualResult);
    }

    private String generateEt3Dates(String et1ServedDate, String et3DueDate, String et3ReceivedDate) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("| Dates| |\r\n");
        stringBuilder.append("|--|--|\r\n");
        stringBuilder.append(String.format("|ET1 Served| %s|\r\n", et1ServedDate));
        stringBuilder.append(String.format("|ET3 due| %s|\r\n", et3DueDate));
        stringBuilder.append("|Extension| None|\r\n");
        stringBuilder.append(String.format("|ET3 received| %s|", et3ReceivedDate));

        return stringBuilder.toString();
    }

    private CaseDetails generateCaseDetails(String jsonFileName) throws Exception {
        String json = new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(getClass().getClassLoader()
                .getResource(jsonFileName)).toURI())));
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, CaseDetails.class);
    }


}
