package uk.gov.hmcts.ethos.replacement.docmosis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Runs against a booted CFTLIB stack to verify CCD-triggered CDAM upload and case attachment.
 */
@Slf4j
public final class CftlibDocumentUploadAttachCheck {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
    private static final String SERVICE_URL = env("ET_COS_URL", "http://localhost:8081");
    private static final String IDAM_URL = env("IDAM_API_BASEURL", "http://localhost:5062");
    private static final String S2S_URL = env("SERVICE_AUTH_PROVIDER_URL", "http://localhost:8489");
    private static final String CCD_URL = env("CCD_DATA_STORE_API_URL", "http://localhost:4452");
    private static final String CDAM_URL = env("CASE_DOCUMENT_AM_URL", "http://localhost:4455");
    private static final String CASE_TYPE = "ET_EnglandWales";
    private static final String JURISDICTION = "EMPLOYMENT";
    private static final String USER = env("ET_COS_SYSTEM_USER", "admin@hmcts.net");
    private static final Path INITIATE_CASE_PAYLOAD = Path.of(env("CFTLIB_INITIATE_CASE_PAYLOAD",
            "src/test/resources/eventInitiateCase.json"));
    private static final Path ET1_VETTING_PAYLOAD = Path.of(env("CFTLIB_ET1_VETTING_PAYLOAD",
            "src/test/resources/eventET1Vetting.json"));
    private static final Path PRE_ACCEPTANCE_PAYLOAD = Path.of(env("CFTLIB_PRE_ACCEPTANCE_PAYLOAD",
            "src/test/resources/eventPreAcceptanceCase.json"));
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern DOCUMENT_ID_PATTERN = Pattern.compile("/documents/([0-9a-fA-F-]{36})");

    private CftlibDocumentUploadAttachCheck() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        waitFor(SERVICE_URL + "/health");
        waitFor(CCD_URL + "/health");
        waitFor(CDAM_URL + "/health");

        String userToken = getUserToken();
        String serviceToken = getServiceToken("ccd_data");
        String userId = getUserId(userToken);

        long caseReference = createCase(userToken, serviceToken, userId);
        submitCcdEvent(userToken, serviceToken, userId, caseReference, "et1Vetting", ET1_VETTING_PAYLOAD);
        submitCcdEvent(userToken, serviceToken, userId, caseReference, "preAcceptanceCase", PRE_ACCEPTANCE_PAYLOAD);
        JsonNode initialConsiderationResponse = submitInitialConsideration(
                userToken, serviceToken, userId, caseReference);

        String documentId = findInitialConsiderationDocumentId(initialConsiderationResponse);
        JsonNode metadata = getDocumentMetadata(userToken, serviceToken, documentId);
        assertAttachedToCase(metadata, documentId, caseReference);

        log.info("Verified CCD event uploaded document {} and attached it to case {} in CDAM.",
                documentId, caseReference);
    }

    private static void waitFor(String url) throws IOException, InterruptedException {
        Instant deadline = Instant.now().plus(Duration.ofMinutes(2));
        while (Instant.now().isBefore(deadline)) {
            try {
                HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(url))
                                .timeout(Duration.ofSeconds(5))
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 500) {
                    return;
                }
            } catch (IOException ignored) {
                // Service is still starting.
            }
            Thread.sleep(1000);
        }
        throw new IllegalStateException("Timed out waiting for " + url);
    }

    private static long createCase(String userToken, String serviceToken, String userId)
            throws IOException, InterruptedException {
        String eventToken = getEventToken(userToken, serviceToken, userId, null, "initiateCase");
        ObjectNode payload = payloadFromFile(INITIATE_CASE_PAYLOAD, eventToken);
        ObjectNode data = (ObjectNode) payload.path("data");
        data.put("caseType", "Single");
        data.put("managingOffice", "Leeds");
        data.put("regionalOffice", "Leeds");
        data.put("et1TribunalRegion", "Leeds");

        JsonNode response = sendCcdEvent(
                userToken,
                serviceToken,
                "/caseworkers/" + userId + "/jurisdictions/" + JURISDICTION + "/case-types/" + CASE_TYPE + "/cases",
                payload);

        long caseReference = response.path("id").asLong();
        if (caseReference == 0) {
            throw new IllegalStateException("CCD create case response did not include a case id: " + response);
        }
        return caseReference;
    }

    private static JsonNode submitCcdEvent(String userToken, String serviceToken, String userId, long caseReference,
                                           String eventId, Path payloadFile)
            throws IOException, InterruptedException {
        String eventToken = getEventToken(userToken, serviceToken, userId, caseReference, eventId);
        ObjectNode payload = payloadFromFile(payloadFile, eventToken);
        return sendCcdEvent(userToken, serviceToken, ccdCaseEventPath(userId, caseReference), payload);
    }

    private static JsonNode submitInitialConsideration(String userToken, String serviceToken, String userId,
                                                       long caseReference)
            throws IOException, InterruptedException {
        String eventToken = getEventToken(userToken, serviceToken, userId, caseReference, "initialConsideration");
        ObjectNode payload = MAPPER.createObjectNode();
        payload.set("data", MAPPER.createObjectNode());
        payload.set("event", MAPPER.createObjectNode()
                .put("id", "initialConsideration")
                .put("summary", "CFTLIB CDAM attach check")
                .put("description", "Verify CCD-triggered document upload attach"));
        payload.put("event_token", eventToken);
        payload.put("ignore_warning", false);
        return sendCcdEvent(userToken, serviceToken, ccdCaseEventPath(userId, caseReference), payload);
    }

    private static String ccdCaseEventPath(String userId, long caseReference) {
        return "/caseworkers/" + userId + "/jurisdictions/" + JURISDICTION
                + "/case-types/" + CASE_TYPE + "/cases/" + caseReference + "/events";
    }

    private static ObjectNode payloadFromFile(Path path, String eventToken) throws IOException {
        ObjectNode payload = (ObjectNode) MAPPER.readTree(Files.readString(path));
        payload.put("event_token", eventToken);
        return payload;
    }

    private static String getEventToken(String userToken, String serviceToken, String userId, Long caseReference,
                                        String eventId) throws IOException, InterruptedException {
        String path = "/caseworkers/" + userId + "/jurisdictions/" + JURISDICTION
                + "/case-types/" + CASE_TYPE
                + (caseReference == null ? "" : "/cases/" + caseReference)
                + "/event-triggers/" + eventId + "/token";
        HttpResponse<String> response = sendGet(CCD_URL + path, userToken, serviceToken);
        if (response.statusCode() != 200) {
            throw new IllegalStateException("CCD event token call failed with status "
                    + response.statusCode() + ": " + response.body());
        }
        Matcher matcher = TOKEN_PATTERN.matcher(response.body());
        if (!matcher.find()) {
            throw new IllegalStateException("CCD event token response did not include a token: " + response.body());
        }
        return matcher.group(1);
    }

    private static JsonNode sendCcdEvent(String userToken, String serviceToken, String path, JsonNode payload)
            throws IOException, InterruptedException {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(CCD_URL + path))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", userToken)
                .header("ServiceAuthorization", serviceToken)
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(payload)))
                .build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            throw new IllegalStateException("CCD event call failed with status "
                    + response.statusCode() + ": " + response.body());
        }
        JsonNode body = MAPPER.readTree(response.body());
        JsonNode callbackErrors = body.path("callback_errors");
        if (callbackErrors.isArray() && !callbackErrors.isEmpty()) {
            throw new IllegalStateException("CCD event returned callback errors: " + callbackErrors);
        }
        return body;
    }

    private static String findInitialConsiderationDocumentId(JsonNode payload) {
        String documentUrl = payload.at("/case_data/etInitialConsiderationDocument/document_url").asText();
        Matcher matcher = DOCUMENT_ID_PATTERN.matcher(documentUrl);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("Could not find the Initial Consideration document id in response: " + payload);
    }

    private static JsonNode getDocumentMetadata(String userToken, String serviceToken, String documentId)
            throws IOException, InterruptedException {
        HttpResponse<String> response = sendGet(CDAM_URL + "/cases/documents/" + documentId, userToken, serviceToken);
        if (response.statusCode() != 200) {
            throw new IllegalStateException("CDAM metadata call failed with status "
                    + response.statusCode() + ": " + response.body());
        }
        return MAPPER.readTree(response.body());
    }

    private static void assertAttachedToCase(JsonNode metadata, String documentId, long caseReference) {
        JsonNode metadataNode = metadata.path("metadata");
        if (!Long.toString(caseReference).equals(metadataNode.path("case_id").asText())
                || !CASE_TYPE.equals(metadataNode.path("case_type_id").asText())
                || !JURISDICTION.equals(metadataNode.path("jurisdiction").asText())) {
            throw new IllegalStateException("Document " + documentId
                    + " metadata was not attached to case " + caseReference + ": " + metadata);
        }
    }

    private static String getUserToken() throws IOException, InterruptedException {
        List<String> passwords = List.of(
                env("ET_COS_SYSTEM_USER_PASSWORD", "Password"),
                "password",
                "");
        List<Client> clients = List.of(
                new Client(env("IDAM_CLIENT_ID", "et-cos"), env("IDAM_CLIENT_SECRET", "AAAAAAAAAAAAAAAA")),
                new Client("divorce", "123456"));

        for (Client client : clients) {
            for (String password : passwords) {
                HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(IDAM_URL + "/o/token"))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(form(Map.of(
                                "grant_type", "password",
                                "client_id", client.id(),
                                "client_secret", client.secret(),
                                "username", USER,
                                "password", password,
                                "scope", "openid profile roles"))))
                        .build(), HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String accessToken = MAPPER.readTree(response.body()).path("access_token").asText();
                    if (!accessToken.isBlank()) {
                        return "Bearer " + accessToken;
                    }
                }
            }
        }
        throw new IllegalStateException("Could not obtain IDAM token for " + USER);
    }

    private static String getUserId(String userToken) throws IOException, InterruptedException {
        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(IDAM_URL + "/details"))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", userToken)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Could not obtain IDAM user details: "
                    + response.statusCode() + " " + response.body());
        }
        String userId = MAPPER.readTree(response.body()).path("id").asText();
        if (userId.isBlank()) {
            throw new IllegalStateException("IDAM user details did not include id: " + response.body());
        }
        return userId;
    }

    private static String getServiceToken(String microservice) throws IOException, InterruptedException {
        ObjectNode request = MAPPER.createObjectNode();
        request.put("microservice", microservice);
        request.put("oneTimePassword", oneTimePassword(env("ET_COS_S2S_SECRET", "AAAAAAAAAAAAAAAA")));

        HttpResponse<String> response = HTTP.send(HttpRequest.newBuilder(URI.create(S2S_URL + "/lease"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MAPPER.writeValueAsString(request)))
                .build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Could not obtain S2S token: "
                    + response.statusCode() + " " + response.body());
        }
        return response.body().replace("\"", "");
    }

    private static HttpResponse<String> sendGet(String url, String userToken, String serviceToken)
            throws IOException, InterruptedException {
        return HTTP.send(HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Authorization", userToken)
                .header("ServiceAuthorization", serviceToken)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String oneTimePassword(String secret) {
        try {
            byte[] key = base32Decode(secret.toUpperCase(Locale.ROOT));
            byte[] time = ByteBuffer.allocate(8)
                    .putLong(Instant.now().getEpochSecond() / 30)
                    .array();
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            byte[] hash = mac.doFinal(time);
            int offset = hash[hash.length - 1] & 0xf;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);
            return String.format("%06d", binary % 1_000_000);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to generate S2S one-time password", e);
        }
    }

    private static byte[] base32Decode(String value) {
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        int buffer = 0;
        int bitsLeft = 0;
        byte[] output = new byte[value.length() * 5 / 8];
        int index = 0;
        for (char c : value.toCharArray()) {
            if (c == '=') {
                break;
            }
            int val = alphabet.indexOf(c);
            if (val < 0) {
                throw new IllegalArgumentException("Invalid base32 character: " + c);
            }
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                output[index++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xff);
                bitsLeft -= 8;
            }
        }
        return HexFormat.of().parseHex(HexFormat.of().formatHex(output, 0, index));
    }

    private static String form(Map<String, String> values) {
        StringBuilder builder = new StringBuilder();
        Iterator<Map.Entry<String, String>> iterator = values.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            builder.append('=');
            builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            if (iterator.hasNext()) {
                builder.append('&');
            }
        }
        return builder.toString();
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private record Client(String id, String secret) {
    }
}
