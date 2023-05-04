package controllers;


import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.AllocateHearingController;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.AllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.ScotlandAllocateHearingService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;


@WebMvcTest(AllocateHearingController.class)
@ContextConfiguration(classes = {DocmosisApplication.class, AllocateHearingController.class})
@ActiveProfiles("test")
public class AllocateHearingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    private uk.gov.hmcts.et.common.model.ccd.CCDRequest ccdRequest;

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private AllocateHearingService allocateHearingService;

    @MockBean
    private ScotlandAllocateHearingService scotlandAllocateHearingService;

    @BeforeEach
    public void setup() {
        RestAssuredMockMvc.mockMvc(mockMvc);
        Mockito.when(verifyTokenService.verifyTokenSignature(ArgumentMatchers.any())).thenReturn(true);
    }

    @Test
    public void testInitialiseHearingDynamicList() {

        CCDRequest ccdRequest = generateCCDRequest();
        String userToken = "validToken";

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header("Authorization", userToken)
                .body(ccdRequest)
                .post("/allocatehearing/initialiseHearings")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    // Add similar tests for other endpoints: handleListingSelected, handleManagingOfficeSelected, populateRooms, aboutToSubmit

    private CCDRequest generateCCDRequest() {
        // Implement this method to return a valid CCDRequest object
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(generateCaseData())
                .withCaseId("123")
                .build();
        return ccdRequest;
    }

    private CaseData generateCaseData() {
        uk.gov.hmcts.et.common.model.ccd.CaseData caseData = CaseDataBuilder.builder()
                .withHearingScotland("hearingNumber", Constants.HEARING_TYPE_JUDICIAL_HEARING, "Judge",
                        TribunalOffice.ABERDEEN, "venue")
                .withHearingSession(
                        0,
                        "hearingNumber",
                        "2019-11-25T12:11:00.000",
                        Constants.HEARING_STATUS_HEARD,
                        true)
                .build();
        return caseData;
    }

}
