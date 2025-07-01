package uk.gov.hmcts.ethos.replacement.apitest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.hmcts.ethos.replacement.apitest.model.CreateUser;
import uk.gov.hmcts.ethos.replacement.apitest.model.Role;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.http.client.methods.RequestBuilder.post;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
public class IdamTestApiRequests {
    private final HttpClient client;
    private final String baseIdamApiUrl;
    private static final String USER_PASSWORD = "Apassword123";
    private CreateUser user;

    public IdamTestApiRequests(HttpClient client, String baseIdamApiUrl) {
        this.client = client;
        this.baseIdamApiUrl = baseIdamApiUrl;
    }

    public CreateUser createUser(String email) throws IOException {
        CreateUser createUser = new CreateUser(
            email,
            "ATestForename",
            "ATestSurname",
            USER_PASSWORD,
            List.of(new Role("citizen"), new Role("caseworker-employment-api"))
        );

        String body = new ObjectMapper().writeValueAsString(createUser);
        String resJson = makePostRequest(baseIdamApiUrl + "/testing-support/accounts", body);
        createUser.setId(getIdFromIdamResponse(new JSONObject(resJson)));
        log.info("BaseFunctionalTest user created.");
        user = createUser;
        return createUser;
    }

    private String getIdFromIdamResponse(JSONObject idamResponse) {
        try {
            return idamResponse.getString("uuid");
        } catch (Exception e) {
            return idamResponse.getString("id");
        }
    }

    private String makePostRequest(String uri, String body) throws IOException {
        HttpResponse createUserResponse = client.execute(post(uri)
                                                             .setEntity(new StringEntity(body, APPLICATION_JSON))
                                                             .build());

        int statusCode = createUserResponse.getStatusLine().getStatusCode();

        assertTrue(statusCode == CREATED.value() || statusCode == OK.value());
        log.info("BaseFunctionalTest user created.");
        return EntityUtils.toString(createUserResponse.getEntity());
    }

    /**
     * Get Access Token when testing locally - uses standard login journey which requires xui app to be running.
     */
    public String getLocalAccessToken() throws IOException {
        List<String> cookies = idamAuth();

        String auth = cookies.stream().filter(o -> o.startsWith("__auth__")).findFirst().get();
        return "Bearer " + auth.substring(9, auth.indexOf(";"));
    }

    /**
     * Authorize with Idam and return cookies.
     */
    public List<String> idamAuth() throws IOException {
        HttpClient instance = HttpClientBuilder.create().disableRedirectHandling().build();
        HttpResponse response = instance.execute(new HttpGet("http://localhost:3000/auth/login"));

        List<String> cookies =
                Arrays.stream(response.getHeaders("Set-Cookie")).map(o -> o.getValue().substring(0,
                        o.getValue().indexOf(";"))).collect(Collectors.toList());

        cookies.add("seen_cookie_message=yes");
        cookies.add("cookies_policy={ \"essential\": true, \"analytics\": false, \"apm\": false }");
        cookies.add("cookies_preferences_set=false");

        List<NameValuePair> formparams = new ArrayList<>();
        formparams.add(new BasicNameValuePair("username", user.getEmail()));
        formparams.add(new BasicNameValuePair("password", user.getPassword()));
        formparams.add(new BasicNameValuePair("save", "Sign in"));
        formparams.add(new BasicNameValuePair("selfRegistrationEnabled", "true"));
        formparams.add(new BasicNameValuePair("azureLoginEnabled", "true"));
        formparams.add(new BasicNameValuePair("mojLoginEnabled", "true"));
        formparams.add(new BasicNameValuePair("_csrf", "idklol"));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);

        HttpResponse loginResponse = instance.execute(post(response.getHeaders("Location")[0].getValue())
                .setHeader("Content-type", APPLICATION_FORM_URLENCODED_VALUE)
                .setHeader("Cookie", String.join("; ", cookies))
                .setEntity(entity)
                .build());

        Header[] locations = loginResponse.getHeaders("Location");

        Arrays.stream(loginResponse.getHeaders("Set-Cookie")).forEach(o -> cookies.add(o.getValue()));
        String cookieStr = String.join("; ", cookies);
        HttpGet httpGet = new HttpGet(locations[0].getValue());
        httpGet.setHeader("Cookie", cookieStr);

        HttpResponse execute = instance.execute(httpGet);
        Arrays.stream(execute.getHeaders("Set-Cookie")).forEach(o -> cookies.add(o.getValue()));

        return cookies;
    }

    public String getAccessToken(String email) throws IOException {
        List<NameValuePair> formparams = new ArrayList<>();
        formparams.add(new BasicNameValuePair("username", email));
        formparams.add(new BasicNameValuePair("password", USER_PASSWORD));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
        HttpResponse loginResponse = client.execute(post(baseIdamApiUrl + "/loginUser")
                                                        .setHeader("Content-type", APPLICATION_FORM_URLENCODED_VALUE)
                                                        .setEntity(entity)
                                                        .build());
        assertEquals(OK.value(), loginResponse.getStatusLine().getStatusCode());

        String tokens = EntityUtils.toString(loginResponse.getEntity());
        JSONObject jsonObject;
        String accessToken = null;
        try {
            jsonObject = new JSONObject(tokens);
            accessToken = jsonObject.get("access_token").toString();
        } catch (JSONException e) {
            log.error("Failed to get access token from loginResponse, error: ", e);
        }
        assertNotNull(accessToken);
        return "Bearer " + accessToken;
    }
}