package controllers;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DateListedTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.HearingTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DateListedType;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = {DocmosisApplication.class})
public class AllocateHearingControllerTest {

    private static Properties properties;
    private final String cftUser = properties.getProperty("cftUser");
    private final String cftPassword = properties.getProperty("cftPassword");

    private String userToken;
    private static final String testUrl = System.getenv("TEST_URL") != null ? System.getenv("TEST_URL")
            : "http://localhost:8081";
    private HearingType selectedHearing;
    private DateListedType selectedListing;
    private final TribunalOffice tribunalOffice = TribunalOffice.MANCHESTER;

    @BeforeAll
    public static void setup() {
        properties = new Properties();
        try (InputStream inputStream = new FileInputStream("src/test/api/resources/application-test.properties")) {
            properties.load(inputStream);

        } catch (IOException e) {
            throw new RuntimeException("Could not read properties file", e);
        }

    }

    @Test
    public void testInitialiseHearingDynamicList() throws IOException {
        try {
            userToken = getAuthToken();
        } catch (NullPointerException | IOException | JSONException e) {
            userToken = getAuthTokenFromLocal();
        }
        CCDRequest ccdRequest =
                CCDRequestBuilder.builder().withCaseId("123").withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID).build();

        RestAssured.given().log().all()
                .relaxedHTTPSValidation()
                .baseUri(testUrl)
                .basePath("/allocatehearing/initialiseHearings")
                .header("Content-type", ContentType.JSON)
                .header("Authorization", userToken)
                .body(ccdRequest)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    public void testHandleListingSelected() throws IOException {
        try {
            userToken = getAuthToken();
        } catch (NullPointerException | UnknownHostException e) {
            userToken = getAuthTokenFromLocal();
        }

        CaseData caseData = generateCaseData();


        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .withCaseData(caseData)
                .build();

        RestAssured.given().log().all()
                .relaxedHTTPSValidation()
                .baseUri(testUrl)
                .basePath("/allocatehearing/handleListingSelected")
                .header("Content-type", ContentType.JSON)
                .header("Authorization", userToken)
                .body(ccdRequest)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    @Test
    public void testPopulateRoomDynamicList() throws IOException {
        try {
            userToken = getAuthToken();
        } catch (NullPointerException | UnknownHostException e) {
            userToken = getAuthTokenFromLocal();
        }
        CaseData caseData = generateCaseData();

        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withCaseId("123")
                .withCaseTypeId(Constants.ENGLANDWALES_CASE_TYPE_ID)
                .build();

        RestAssured.given().log().all()
                .relaxedHTTPSValidation()
                .baseUri(testUrl)
                .basePath("/allocatehearing/populateRooms")
                .header("Content-type", ContentType.JSON)
                .header("Authorization", userToken)
                .body(ccdRequest)
                .when()
                .post()
                .then()
                .log().all()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    private DateListedTypeItem createListing(String id, String listedDate) {
        DateListedType dateListedType = new DateListedType();
        dateListedType.setListedDate(listedDate);
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setId(id);
        dateListedTypeItem.setValue(dateListedType);
        return dateListedTypeItem;
    }

    private HearingTypeItem createHearing(String id, String hearingNumber, List<DateListedTypeItem> listings) {
        HearingType hearing = new HearingType();
        hearing.setHearingNumber(hearingNumber);
        hearing.setHearingDateCollection(listings);
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setId(id);
        hearingTypeItem.setValue(hearing);
        return hearingTypeItem;
    }

    public String getAuthTokenFromLocal() {
        Response response = RestAssured.given()
                .log().all()
                .contentType(ContentType.URLENC)
                .formParam("client_id", "fake")
                .formParam("client_secret", "fake")
                .formParam("grant_type", "password")
                .formParam("redirect_uri", "example.com")
                .formParam("username", cftUser)
                .formParam("password", cftPassword)
                .formParam("scope", "openid profile roles")
                .post("http://localhost:5000/o/token");
        JSONObject jsonResponse = new JSONObject(response.getBody().asString());
        return "Bearer " + jsonResponse.getString("access_token");
    }

    private CaseData generateCaseData() {
        CaseData caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);
        caseData.setAllocateHearingHearing(new DynamicFixedListType());

        selectedHearing = new HearingType();
        selectedListing = new DateListedType();
        DateListedTypeItem dateListedTypeItem = new DateListedTypeItem();
        dateListedTypeItem.setValue(selectedListing);
        selectedHearing.setHearingDateCollection(List.of(dateListedTypeItem));
        HearingTypeItem hearingTypeItem = new HearingTypeItem();
        hearingTypeItem.setValue(selectedHearing);
        caseData.setHearingCollection(List.of(hearingTypeItem));
        DynamicFixedListType judge = DynamicFixedListType.of(DynamicValueType.create("judge2", "Judge 2"));
        DynamicFixedListType employerMember = DynamicFixedListType.of(DynamicValueType.create("employerMember2",
                "Employer Member 2"));
        DynamicFixedListType employeeMember = DynamicFixedListType.of(DynamicValueType.create("employeeMember2",
                "Employee Member 2"));
        String hearingStatus = Constants.HEARING_STATUS_POSTPONED;
        String postponedBy = "Doris";
        DynamicFixedListType venue = DynamicFixedListType.of(DynamicValueType.create("venue2", "Venue 2"));
        DynamicFixedListType room = DynamicFixedListType.of(DynamicValueType.create("room2", "Room 2"));
        DynamicFixedListType clerk = DynamicFixedListType.of(DynamicValueType.create("clerk2", "Clerk 2"));
        caseData.setAllocateHearingJudge(judge);
        caseData.setAllocateHearingEmployerMember(employerMember);
        caseData.setAllocateHearingEmployeeMember(employeeMember);
        caseData.setAllocateHearingStatus(hearingStatus);
        List<HearingTypeItem> hearings = List.of(
                createHearing("id1", "2", List.of(createListing("id2", "1970-01-03T10:00:00.000"))),
                createHearing("id5", "1", List.of(createListing("id1", "1970-01-01T10:00:00.000"))),
                createHearing("id6", "2", List.of(createListing("id3", "1970-01-03T10:00:00.000")))
        );

        caseData.setHearingCollection(hearings);
        caseData.setAllocateHearingHearing(new DynamicFixedListType("id1"));
        caseData.setAllocateHearingPostponedBy(postponedBy);
        caseData.setAllocateHearingVenue(venue);
        caseData.setAllocateHearingRoom(room);
        caseData.setAllocateHearingClerk(clerk);
        return caseData;
    }

    public String getAuthToken() throws IOException {
        String idamBaseUrl = "https://idam-api.aat.platform.hmcts.net";
        String redirectUri = testUrl + "/oauth2/callback";
        String clientId = "xuiwebapp";
        String clientSecret = System.getenv("IDAM_CLIENT_SECRET");
        String username = System.getenv("ET_CCD_CASEWORKER_USER_NAME");
        String password = System.getenv("ET_CCD_CASEWORKER_PASSWORD");
        String scope = "openid profile roles";
        String idamCodePath =
                "/oauth2/authorize?response_type=code&client_id=" + clientId + "&redirect_uri=" + redirectUri;

        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        Response codeResponse = RestAssured.given()
                .header("Authorization", authHeader)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(idamBaseUrl + idamCodePath);

        String code = codeResponse.jsonPath().getString("code");

        String idamAuthPath =
                "/oauth2/token?grant_type=authorization_code&client_id=" + clientId + "&client_secret=" + clientSecret
                        + "&redirect_uri=" + redirectUri + "&code=" + code + "&scope=" + scope;

        Response authTokenResponse = RestAssured.given()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .post(idamBaseUrl + idamAuthPath);

        String accessToken = authTokenResponse.jsonPath().getString("access_token");
        System.out.println("Access Token: " + accessToken);

        return accessToken;
    }

}
