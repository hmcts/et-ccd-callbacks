package uk.gov.hmcts.ethos.replacement.apitest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.*;
import uk.gov.hmcts.et.common.model.ccd.items.ReferralTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.http.client.methods.RequestBuilder.get;
import static org.apache.http.client.methods.RequestBuilder.post;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;

@Slf4j
public class UpdateReferralMultiplesControllerFunctionalTest extends BaseFunctionalTest {
    private static final String ABOUT_TO_START_URL = "multiples/updateReferral/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "multiples/updateReferral/aboutToSubmit";
    private static final String MID_DETAILS_URL = "multiples/updateReferral/initHearingAndReferralDetails";

    private static final String EXUI_CREATE_CASE = "/data/case-types/ET_EnglandWales/cases";
    private static final String EXUI_TRIGGER_CASE = "/data/internal/case-types/ET_EnglandWales/event-triggers/initiateCase";

    private MultipleRequest request;

    @Autowired
    private ResourceLoader resourceLoader;

    @BeforeAll
    public void setUpCaseData() throws IOException {
        MultipleData multipleData = MultipleData.builder().build();
        request = new MultipleRequest();
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID);
        request.setCaseDetails(multipleDetails);

        multipleData.setReferralCollection(List.of(createReferralTypeItem()));

        DynamicFixedListType selectReferralList =
                ReferralHelper.populateSelectReferralDropdown(multipleData.getReferralCollection());
        selectReferralList.setValue(new DynamicValueType());
        selectReferralList.getValue().setCode("1");
        selectReferralList.getValue().setLabel("idklol");
        multipleData.setSelectReferral(selectReferralList);
        multipleData.setLeadCase("6000001/2024");

        JSONObject aCase = createCase();
        log.info(aCase.getString("id"));
        log.info("idklol");
    }

    JSONObject createCase() throws IOException {
        List<String> cookies = idamTestApiRequests.idamAuth();

        HttpGet triggerCreateGet = new HttpGet("http://localhost:3000/data/internal/case-types/ET_Scotland/event-triggers/initiateCase?ignore-warning=false");
        triggerCreateGet.setHeader("Cookie", String.join("; ", cookies));

        setAuthorisedHeaders(triggerCreateGet, xsrfToken);

        HttpClient instance = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpResponse triggerCreateResponse = instance.execute(
                buildAuthorisedRequest(
                        get("http://localhost:3000/data/internal/case-types/ET_Scotland/event-triggers/initiateCase?ignore-warning=false"),

                ));


        String string = EntityUtils.toString(triggerCreateResponse.getEntity());
        JSONObject jsonObject = new JSONObject(string);
        String eventToken = jsonObject.get("event_token").toString();

        String newScotlandCase = readJsonResource(resourceLoader.getResource("classpath:/caseDetails2024Flex.json"));

        StringEntity entity1 = new StringEntity(newScotlandCase.replace("\"event_token\": \"\"", "\"event_token\": \"" + eventToken + "\""));

        HttpResponse createResponse = instance.execute(
                buildAuthorisedRequest(
                    post(exuiUrl + EXUI_CREATE_CASE)
                            .setHeader("Content-Type", "application/json"), cookies, entity1
                )
        );

        String string2 = EntityUtils.toString(createResponse.getEntity());
        JSONObject jsonObject2 = new JSONObject(string2);

        return jsonObject2;
    }

    public HttpUriRequest buildAuthorisedRequest(RequestBuilder request, List<String> cookies, HttpEntity entity) {
        String xsrfToken = cookies.stream().filter(o -> o.startsWith("XSRF-TOKEN")).findFirst().get().replace("XSRF-TOKEN=", "");

        return request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:102.0) Gecko/20100101 Firefox/102.0")
                .setHeader("Accept", "*/*")
                .setHeader("Accept-Language", "en-GB,en;q=0.5")
                .setHeader("Accept-Encoding", "gzip, deflate, br")
                .setHeader("Referer", exuiUrl)
                .setHeader("Cookie", String.join("; ", cookies))
                .setHeader("Connection", "keep-alive")
                .setHeader("Upgrade-Insecure-Requests", "1")
                .setHeader("Sec-Fetch-Dest", "empty")
                .setHeader("Sec-Fetch-Mode", "cors")
                .setHeader("Sec-Fetch-Site", "same-origin")
                .setHeader("Pragma", "no-cache")
                .setHeader("Cache-Control", "no-cache")
                .setHeader("X-XSRF-TOKEN", xsrfToken)
                .setHeader("experimental", "true")
                .setEntity(entity)
                .build();
    }

    public void setAuthorisedHeaders(HttpRequestBase httpMethod, String xsrfToken) {
        httpMethod.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.15; rv:102.0) Gecko/20100101 Firefox/102.0");
        httpMethod.setHeader("Accept", "*/*");
        httpMethod.setHeader("Accept-Language", "en-GB,en;q=0.5");
        httpMethod.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpMethod.setHeader("Referer", exuiUrl);
        httpMethod.setHeader("Connection", "keep-alive");
        httpMethod.setHeader("Upgrade-Insecure-Requests", "1");
        httpMethod.setHeader("Sec-Fetch-Dest", "empty");
        httpMethod.setHeader("Sec-Fetch-Mode", "cors");
        httpMethod.setHeader("Sec-Fetch-Site", "same-origin");
        httpMethod.setHeader("Pragma", "no-cache");
        httpMethod.setHeader("Cache-Control", "no-cache");
        httpMethod.setHeader("X-XSRF-TOKEN", xsrfToken);
        httpMethod.setHeader("experimental", "true");
    }

    public String readJsonResource(Resource jsonResource) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(jsonResource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    @Test
    void aboutToStartUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(ABOUT_TO_START_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    @Test
    void midReferralDetailsUrl() {
        RestAssured.given()
                .spec(spec)
                .contentType(ContentType.JSON)
                .header(new Header(AUTHORIZATION, userToken))
                .body(request)
                .post(MID_DETAILS_URL)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .log()
                .all(true);
    }

    private ReferralTypeItem createReferralTypeItem() {
        ReferralTypeItem referralTypeItem = new ReferralTypeItem();
        ReferralType referralType = new ReferralType();
        referralType.setReferralNumber("1");
        referralType.setReferralSubject("Other");
        referralTypeItem.setValue(referralType);
        referralType.setReferralStatus("referralStatus");
        return referralTypeItem;
    }
}
