package uk.gov.hmcts.ethos.replacement.functional.bulk;

import io.restassured.response.Response;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import uk.gov.hmcts.et.common.model.bulk.BulkRequest;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.ethos.replacement.functional.ComponentTest;
import uk.gov.hmcts.ethos.replacement.functional.util.Constants;
import uk.gov.hmcts.ethos.replacement.functional.util.TestUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@Category(ComponentTest.class)
@RunWith(SerenityRunner.class)
@WithTags({
    @WithTag("ComponentTest"),
    @WithTag("FunctionalTest")
})
public class CreateBulkComponentTest {
    private TestUtil testUtil;
    private final List<String> caseList = new ArrayList<>();
    private static final String ETHOS_CASE_REFERENCE = "#ETHOS-CASE-REFERENCE#";
    private static final String ETHOS_CASE_REFERENCE_ALT = ETHOS_CASE_REFERENCE.substring(0, 21);

    @Before
    public void setUp() {
        testUtil = new TestUtil();
    }

    @Test
    public void createBulkEngIndividualClaimantNotRepresented() throws IOException {
        caseList.clear();
        caseList.add(Constants.TEST_DATA_ENG_BULK1_CASE1);
        caseList.add(Constants.TEST_DATA_ENG_BULK1_CASE2);
        caseList.add(Constants.TEST_DATA_ENG_BULK1_CASE3);

        testUtil.executeCreateBulkTest(false, Constants.TEST_DATA_ENG_BULK1, caseList);
    }

    @Test
    public void createBulkEngIndividualClaimantRepresented() throws IOException {
        caseList.clear();
        caseList.add(Constants.TEST_DATA_ENG_BULK2_CASE1);
        caseList.add(Constants.TEST_DATA_ENG_BULK2_CASE2);
        caseList.add(Constants.TEST_DATA_ENG_BULK2_CASE3);

        testUtil.executeCreateBulkTest(false, Constants.TEST_DATA_ENG_BULK2, caseList);
    }

    @Test
    public void createBulkEngCompanyClaimantNotRepresented() throws IOException {
        caseList.clear();
        caseList.add(Constants.TEST_DATA_ENG_BULK3_CASE1);
        caseList.add(Constants.TEST_DATA_ENG_BULK3_CASE2);
        caseList.add(Constants.TEST_DATA_ENG_BULK3_CASE3);

        testUtil.executeCreateBulkTest(false, Constants.TEST_DATA_ENG_BULK3, caseList);
    }

    @Test
    public void createBulkEngBothIndividualAndCompanyClaimants() throws IOException {
        caseList.clear();
        caseList.add(Constants.TEST_DATA_ENG_BULK1_CASE1);
        caseList.add(Constants.TEST_DATA_ENG_BULK1_CASE2);
        caseList.add(Constants.TEST_DATA_ENG_BULK3_CASE1);
        caseList.add(Constants.TEST_DATA_ENG_BULK3_CASE2);

        testUtil.executeCreateBulkTest(false, Constants.TEST_DATA_ENG_BULK4, caseList);
    }

    @Test
    public void createBulkEngWithSomeNonExistentCases() throws IOException {
        testUtil.loadAuthToken();

        String ethosCaseReference = testUtil.getUniqueCaseReference(10);
        String caseDetails = FileUtils.readFileToString(new File(Constants.TEST_DATA_ENG_BULK1_CASE1),
            StandardCharsets.UTF_8);
        caseDetails = caseDetails.replace(ETHOS_CASE_REFERENCE, ethosCaseReference);

        CCDRequest ccdRequest = testUtil.getCcdRequest("1", "", true, caseDetails);
        Response response = testUtil.getResponse(ccdRequest, Constants.CREATE_CASE_URI);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        String testData = FileUtils.readFileToString(new File(Constants.TEST_DATA_ENG_BULK5), StandardCharsets.UTF_8);
        testData = testData.replace("#ETHOS-CASE-REFERENCE1#", ethosCaseReference);
        ethosCaseReference = testUtil.getUniqueCaseReference(10);
        testData = testData.replace("#ETHOS-CASE-REFERENCE2#", ethosCaseReference);
        ethosCaseReference = testUtil.getUniqueCaseReference(10);
        testData = testData.replace("#ETHOS-CASE-REFERENCE3#", ethosCaseReference);

        BulkRequest bulkRequest = testUtil.getBulkRequest(true, testData);
        testUtil.getBulkResponse(bulkRequest, Constants.CREATE_BULK_URI);
    }

    @Test
    public void createBulkEngCaseFromAnotherMultiple() throws IOException {
        String testData = FileUtils.readFileToString(new File(Constants.TEST_DATA_ENG_BULK1), StandardCharsets.UTF_8);
        Response response;

        testUtil.loadAuthToken();

        caseList.clear();
        caseList.add(Constants.TEST_DATA_ENG_BULK1_CASE1);
        caseList.add(Constants.TEST_DATA_ENG_BULK1_CASE2);
        caseList.add(Constants.TEST_DATA_ENG_BULK1_CASE3);

        int count = 1;
        List<String> usedCaseReferences = new ArrayList<>();
        for (String caseDataFilePath : caseList) {
            String ethosCaseReference = testUtil.getUniqueCaseReference(10);
            usedCaseReferences.add(ethosCaseReference);

            String caseDetails = FileUtils.readFileToString(new File(caseDataFilePath), StandardCharsets.UTF_8);
            caseDetails = caseDetails.replace(ETHOS_CASE_REFERENCE, ethosCaseReference);

            CCDRequest ccdRequest = testUtil.getCcdRequest("1", "", false, caseDetails);
            response = testUtil.getResponse(ccdRequest, Constants.CREATE_CASE_URI);

            assertEquals(HttpStatus.SC_OK, response.getStatusCode());

            testData = testData.replace(ETHOS_CASE_REFERENCE_ALT + count + "#", ethosCaseReference);
            count++;
        }

        BulkRequest bulkRequest = testUtil.getBulkRequest(false, testData);
        response = testUtil.getBulkResponse(bulkRequest, Constants.CREATE_BULK_URI);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        testData = testData.replace("34534543553", "84325743533");
        bulkRequest = testUtil.getBulkRequest(false, testData);
        testUtil.getBulkResponse(bulkRequest, Constants.CREATE_BULK_URI);
    }

    @Test
    public void createBulkEngCasesWithStatusSubmitted() throws IOException {
        Response response;
        String testData = FileUtils.readFileToString(new File(Constants.TEST_DATA_ENG_BULK1), StandardCharsets.UTF_8);

        testUtil.loadAuthToken();

        caseList.clear();
        caseList.add(Constants.TEST_DATA_ENG_BULK7_CASE1);
        caseList.add(Constants.TEST_DATA_ENG_BULK7_CASE2);
        caseList.add(Constants.TEST_DATA_ENG_BULK7_CASE3);

        int count = 1;
        for (String caseDataFilePath : caseList) {
            String ethosCaseReference = testUtil.getUniqueCaseReference(10);

            String caseDetails = FileUtils.readFileToString(new File(caseDataFilePath), StandardCharsets.UTF_8);
            caseDetails = caseDetails.replace(ETHOS_CASE_REFERENCE, ethosCaseReference);

            CCDRequest ccdRequest = testUtil.getCcdRequest("1", "", false, caseDetails);
            response = testUtil.getResponse(ccdRequest, Constants.CREATE_CASE_URI);

            assertEquals(HttpStatus.SC_OK, response.getStatusCode());

            testData = testData.replace(ETHOS_CASE_REFERENCE_ALT + count + "#", ethosCaseReference);
            count++;
        }

        BulkRequest bulkRequest = testUtil.getBulkRequest(false, testData);
        testUtil.getBulkResponse(bulkRequest, Constants.CREATE_BULK_URI);

    }

    @Test
    @Ignore
    public void createBulkEngAddCaseFromGlasgow() throws IOException {
        Response response;
        String testData = FileUtils.readFileToString(new File(Constants.TEST_DATA_ENG_BULK1), StandardCharsets.UTF_8);

        testUtil.loadAuthToken();

        caseList.clear();
        caseList.add(Constants.TEST_DATA_ENG_BULK1_CASE1);
        caseList.add(Constants.TEST_DATA_ENG_BULK1_CASE2);

        int count = 1;
        for (String caseDataFilePath : caseList) {
            String ethosCaseReference = testUtil.getUniqueCaseReference(10);

            String caseDetails = FileUtils.readFileToString(new File(caseDataFilePath), StandardCharsets.UTF_8);
            caseDetails = caseDetails.replace(ETHOS_CASE_REFERENCE, ethosCaseReference);

            CCDRequest ccdRequest = testUtil.getCcdRequest("1", "", false, caseDetails);
            response = testUtil.getResponse(ccdRequest, Constants.CREATE_CASE_URI);

            assertEquals(HttpStatus.SC_OK, response.getStatusCode());

            testData = testData.replace(ETHOS_CASE_REFERENCE_ALT + count + "#", ethosCaseReference);
            count++;
        }

        caseList.clear();
        caseList.add(Constants.TEST_DATA_SCOT_BULK2_CASE1);
        for (String caseDataFilePath : caseList) {
            String ethosCaseReference = testUtil.getUniqueCaseReference(10);

            String caseDetails = FileUtils.readFileToString(new File(caseDataFilePath), StandardCharsets.UTF_8);
            caseDetails = caseDetails.replace(ETHOS_CASE_REFERENCE, ethosCaseReference);

            CCDRequest ccdRequest = testUtil.getCcdRequest("1", "", true, caseDetails);
            response = testUtil.getResponse(ccdRequest, Constants.CREATE_CASE_URI);

            assertEquals(HttpStatus.SC_OK, response.getStatusCode());

            testData = testData.replace(ETHOS_CASE_REFERENCE_ALT + count + "#", ethosCaseReference);
            count++;
        }

        BulkRequest bulkRequest = testUtil.getBulkRequest(false, testData);
        testUtil.getBulkResponse(bulkRequest, Constants.CREATE_BULK_URI);
    }

    @Test
    public void createBulkEngAllCasesInvalid() throws IOException {
        testUtil.loadAuthToken();

        String testData = FileUtils.readFileToString(new File(Constants.TEST_DATA_ENG_BULK5), StandardCharsets.UTF_8);

        String ethosCaseReference = testUtil.getUniqueCaseReference(10);
        testData = testData.replace("#ETHOS-CASE-REFERENCE1#", ethosCaseReference);
        ethosCaseReference = testUtil.getUniqueCaseReference(10);
        testData = testData.replace("#ETHOS-CASE-REFERENCE2#", ethosCaseReference);
        ethosCaseReference = testUtil.getUniqueCaseReference(10);
        testData = testData.replace("#ETHOS-CASE-REFERENCE3#", ethosCaseReference);

        BulkRequest bulkRequest = testUtil.getBulkRequest(true, testData);
        testUtil.getBulkResponse(bulkRequest, Constants.CREATE_BULK_URI, 500);
    }

    @Test
    public void createBulkEngNoCases() throws IOException {
        testUtil.loadAuthToken();

        String testData = FileUtils.readFileToString(new File(Constants.TEST_DATA_ENG_BULK6), StandardCharsets.UTF_8);

        BulkRequest bulkRequest = testUtil.getBulkRequest(true, testData);
        testUtil.getBulkResponse(bulkRequest, Constants.CREATE_BULK_URI, 200);
    }

    @Test
    @Ignore
    public void createBulkScotIndividualClaimantNotRepresented() throws IOException {
        caseList.clear();
        caseList.add(Constants.TEST_DATA_SCOT_BULK1_CASE1);
        caseList.add(Constants.TEST_DATA_SCOT_BULK1_CASE2);
        caseList.add(Constants.TEST_DATA_SCOT_BULK1_CASE3);

        testUtil.executeCreateBulkTest(true, Constants.TEST_DATA_SCOT_BULK1, caseList);
    }

    @Test
    @Ignore
    public void createBulkScotIndividualClaimantRepresented() throws IOException {
        caseList.clear();
        caseList.add(Constants.TEST_DATA_SCOT_BULK2_CASE1);
        caseList.add(Constants.TEST_DATA_SCOT_BULK2_CASE2);
        caseList.add(Constants.TEST_DATA_SCOT_BULK2_CASE3);

        testUtil.executeCreateBulkTest(true, Constants.TEST_DATA_SCOT_BULK2, caseList);
    }

    @Test
    @Ignore
    public void createBulkScotCompanyClaimantNotRepresented() throws IOException {
        caseList.clear();
        caseList.add(Constants.TEST_DATA_SCOT_BULK3_CASE1);
        caseList.add(Constants.TEST_DATA_SCOT_BULK3_CASE2);
        caseList.add(Constants.TEST_DATA_SCOT_BULK3_CASE3);

        testUtil.executeCreateBulkTest(true, Constants.TEST_DATA_SCOT_BULK3, caseList);
    }

    @Test
    @Ignore
    public void createBulkScotBothIndividualAndCompanyClaimants() throws IOException {
        caseList.clear();
        caseList.add(Constants.TEST_DATA_SCOT_BULK1_CASE1);
        caseList.add(Constants.TEST_DATA_SCOT_BULK1_CASE2);
        caseList.add(Constants.TEST_DATA_SCOT_BULK3_CASE1);
        caseList.add(Constants.TEST_DATA_SCOT_BULK3_CASE2);

        testUtil.executeCreateBulkTest(true, Constants.TEST_DATA_SCOT_BULK4, caseList);
    }

    @Test
    public void createBulkWithNoPayload() throws IOException {
        testUtil.loadAuthToken();

        BulkRequest bulkRequest = new BulkRequest();

        testUtil.getBulkResponse(bulkRequest, Constants.CREATE_BULK_URI, 500);

    }

    @Test
    public void createBulkWithInvalidToken() throws IOException {

        testUtil.setAuthToken("authToken");

        String testData = FileUtils.readFileToString(new File(Constants.TEST_DATA_ENG_BULK1), StandardCharsets.UTF_8);
        BulkRequest bulkRequest = testUtil.getBulkRequest(false, testData);

        testUtil.getBulkResponse(bulkRequest, Constants.CREATE_BULK_URI, 500);

        testUtil.setAuthToken(null);
    }

    @After
    public void tearDown() {
        // empty function
    }
}
