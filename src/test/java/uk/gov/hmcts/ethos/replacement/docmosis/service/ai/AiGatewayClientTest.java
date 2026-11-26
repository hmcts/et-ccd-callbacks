package uk.gov.hmcts.ethos.replacement.docmosis.service.ai;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;
import uk.gov.hmcts.ethos.replacement.docmosis.config.ai.AiGatewayProperties;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiDocumentAttachment;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiGatewayClientTest {

    private static final String BASE_URL = "https://ai-gateway.staging.platform.hmcts.net";
    private static final String API_VERSION = "2024-10-21";
    private static final String EXPECTED_URL = BASE_URL
            + "/ai/platform/v1/chat/completions?api-version=" + API_VERSION;
    private static final String SUBSCRIPTION_KEY = "test-subscription-key";
    private static final String CLIENT_ID = "97f4f377-1eda-417b-8367-17c2c64cecd8";
    private static final String BEARER_TOKEN = "entra-access-token";

    @Mock
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper;
    private AiGatewayProperties properties;
    private TokenCredential tokenCredential;
    private AiGatewayClient aiGatewayClient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        properties = new AiGatewayProperties();
        properties.setBaseUrl(BASE_URL);
        properties.setApiVersion(API_VERSION);
        properties.setModel("gpt-4.1-mini");
        properties.setGatewayClientId(CLIENT_ID);
        properties.setSubscriptionKey(SUBSCRIPTION_KEY);
        properties.setMaxRetries(1);

        tokenCredential = request -> Mono.just(
                new AccessToken(BEARER_TOKEN, OffsetDateTime.now().plusHours(1)));
        aiGatewayClient = new AiGatewayClient(properties, restTemplate, objectMapper, tokenCredential);
    }

    @Test
    void generateContent_sendsHeadersAndMessagesAndStripsJsonFences() throws Exception {
        String responseBody = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "```json\\n{\\"confidence\\": \\"HIGH\\"}\\n```"
                      }
                    }
                  ]
                }
                """;
        when(restTemplate.exchange(
                eq(EXPECTED_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        TokenRequestContext[] capturedContext = new TokenRequestContext[1];
        TokenCredential spyCredential = request -> {
            capturedContext[0] = request;
            return Mono.just(new AccessToken(BEARER_TOKEN, OffsetDateTime.now().plusHours(1)));
        };
        AiGatewayClient client = new AiGatewayClient(properties, restTemplate, objectMapper, spyCredential);

        String result = client.generateContent("System rules", "User prompt", List.of());

        assertThat(result).isEqualTo("{\"confidence\": \"HIGH\"}");
        assertThat(capturedContext[0].getScopes())
                .containsExactly("api://" + CLIENT_ID + "/.default");

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(EXPECTED_URL),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class));

        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertThat(headers.getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer " + BEARER_TOKEN);
        assertThat(headers.getFirst("Ocp-Apim-Subscription-Key")).isEqualTo(SUBSCRIPTION_KEY);

        JsonNode payload = objectMapper.valueToTree(entityCaptor.getValue().getBody());
        assertThat(payload.path("model").asText()).isEqualTo("gpt-4.1-mini");
        assertThat(payload.path("response_format").path("type").asText()).isEqualTo("json_object");
        assertThat(payload.path("messages")).hasSize(2);
        assertThat(payload.path("messages").get(0).path("role").asText()).isEqualTo("system");
        assertThat(payload.path("messages").get(0).path("content").asText()).isEqualTo("System rules");
        assertThat(payload.path("messages").get(1).path("role").asText()).isEqualTo("user");
        assertThat(payload.path("messages").get(1).path("content").get(0).path("text").asText())
                .isEqualTo("User prompt");
    }

    @Test
    void generateContent_extractsPdfDocxTextAndIncludesPngImageAttachment() throws Exception {
        String responseBody = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"status\\": \\"ok\\"}"
                      }
                    }
                  ]
                }
                """;
        when(restTemplate.exchange(
                eq(EXPECTED_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        List<AiDocumentAttachment> attachments = List.of(
                AiDocumentAttachment.builder()
                        .filename("et1.pdf")
                        .mimeType("application/pdf")
                        .data(createPdfBytes("Unfair dismissal claim details"))
                        .build(),
                AiDocumentAttachment.builder()
                        .filename("particulars.docx")
                        .mimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        .data(createDocxBytes("Claimant worked from 2022 to 2026."))
                        .build(),
                AiDocumentAttachment.builder()
                        .filename("scan.png")
                        .mimeType("image/png")
                        .data(new byte[]{1, 2, 3})
                        .build()
        );

        String result = aiGatewayClient.generateContent("System", "Case summary", attachments);

        assertThat(result).isEqualTo("{\"status\": \"ok\"}");

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(EXPECTED_URL),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class));

        JsonNode userContent = objectMapper.valueToTree(entityCaptor.getValue().getBody())
                .path("messages").get(1).path("content");
        assertThat(userContent).hasSize(5);
        assertThat(userContent.get(1).path("text").asText())
                .contains("et1.pdf")
                .contains("Unfair dismissal claim details");
        assertThat(userContent.get(2).path("type").asText()).isEqualTo("image_url");
        assertThat(userContent.get(2).path("image_url").path("url").asText())
                .startsWith("data:image/jpeg;base64,");
        assertThat(userContent.get(3).path("text").asText())
                .contains("particulars.docx")
                .contains("Claimant worked from 2022 to 2026.");
        assertThat(userContent.get(4).path("type").asText()).isEqualTo("image_url");
        assertThat(userContent.get(4).path("image_url").path("url").asText())
                .isEqualTo("data:image/png;base64,AQID");
    }

    @Test
    void generateContent_extractsXlsxPptxRtfDocAndConvertsBmpToPng() throws Exception {
        String responseBody = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"status\\": \\"ok\\"}"
                      }
                    }
                  ]
                }
                """;
        when(restTemplate.exchange(
                eq(EXPECTED_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        List<AiDocumentAttachment> attachments = List.of(
                AiDocumentAttachment.builder()
                        .filename("schedule.xlsx")
                        .mimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        .data(createXlsxBytes("Unpaid holiday", "1500"))
                        .build(),
                AiDocumentAttachment.builder()
                        .filename("timeline.pptx")
                        .mimeType("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                        .data(createPptxBytes("Dismissal meeting on 01-05-2026"))
                        .build(),
                AiDocumentAttachment.builder()
                        .filename("statement.rtf")
                        .mimeType("application/rtf")
                        .data("{\\rtf1\\ansi Witness statement text}".getBytes(StandardCharsets.UTF_8))
                        .build(),
                AiDocumentAttachment.builder()
                        .filename("legacy.doc")
                        .mimeType("application/msword")
                        .data("Legacy binary doc claim text".getBytes(StandardCharsets.UTF_16LE))
                        .build(),
                AiDocumentAttachment.builder()
                        .filename("evidence.bmp")
                        .mimeType("image/bmp")
                        .data(createBmpBytes())
                        .build()
        );

        String result = aiGatewayClient.generateContent("System", "Case summary", attachments);

        assertThat(result).isEqualTo("{\"status\": \"ok\"}");

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(EXPECTED_URL),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class));

        JsonNode userContent = objectMapper.valueToTree(entityCaptor.getValue().getBody())
                .path("messages").get(1).path("content");
        assertThat(userContent).hasSize(6);
        assertThat(userContent.get(1).path("text").asText())
                .contains("schedule.xlsx")
                .contains("Unpaid holiday")
                .contains("1500");
        assertThat(userContent.get(2).path("text").asText())
                .contains("timeline.pptx")
                .contains("Dismissal meeting on 01-05-2026");
        assertThat(userContent.get(3).path("text").asText())
                .contains("statement.rtf")
                .contains("Witness statement text");
        assertThat(userContent.get(4).path("text").asText())
                .contains("legacy.doc")
                .contains("Legacy binary doc claim text");
        assertThat(userContent.get(5).path("type").asText()).isEqualTo("image_url");
        assertThat(userContent.get(5).path("image_url").path("url").asText())
                .startsWith("data:image/png;base64,");
    }

    @Test
    void generateContent_retriesOn429AndReturnsResult() {
        String responseBody = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"retried\\": true}"
                      }
                    }
                  ]
                }
                """;
        when(restTemplate.exchange(
                eq(EXPECTED_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.TOO_MANY_REQUESTS, "Too Many Requests", HttpHeaders.EMPTY, new byte[0], null))
                .thenReturn(ResponseEntity.ok(responseBody));

        String result = aiGatewayClient.generateContent("System", "Prompt", List.of());

        assertThat(result).isEqualTo("{\"retried\": true}");
        verify(restTemplate, times(2)).exchange(
                eq(EXPECTED_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    void generateContent_omitsTemperatureWhenUnset() throws Exception {
        properties.setModel("gpt-6.2-sol");
        properties.setTemperature(null);
        String responseBody = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"status\\": \\"ok\\"}"
                      }
                    }
                  ]
                }
                """;
        when(restTemplate.exchange(
                eq(EXPECTED_URL),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(ResponseEntity.ok(responseBody));

        String result = aiGatewayClient.generateContent("System", "Prompt", List.of());

        assertThat(result).isEqualTo("{\"status\": \"ok\"}");

        ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(
                eq(EXPECTED_URL),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(String.class));

        JsonNode payload = objectMapper.valueToTree(entityCaptor.getValue().getBody());
        assertThat(payload.has("temperature")).isFalse();
    }

    @Test
    void generateContent_returnsNullWhenSubscriptionKeyBlank() {
        properties.setSubscriptionKey("");

        String result = aiGatewayClient.generateContent("System", "Prompt", List.of());

        assertThat(result).isNull();
        verify(restTemplate, never()).exchange(anyString(), any(), any(), eq(String.class));
    }

    private byte[] createPdfBytes(String text) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(text);
                contentStream.endText();
            }
            document.save(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocxBytes(String text) throws IOException {
        String xml = "<w:document><w:body><w:p><w:r><w:t>" + text + "</w:t></w:r></w:p></w:body></w:document>";
        return createZipBytes(Map.of("word/document.xml", xml));
    }

    private byte[] createXlsxBytes(String sharedText, String numericVal) throws IOException {
        String shared = "<sst><si><t>" + sharedText + "</t></si></sst>";
        String sheet = "<worksheet><sheetData><row><c t=\"s\"><v>0</v></c>"
                + "<c><v>" + numericVal + "</v></c></row></sheetData></worksheet>";
        return createZipBytes(Map.of(
                "xl/sharedStrings.xml", shared,
                "xl/worksheets/sheet1.xml", sheet
        ));
    }

    private byte[] createPptxBytes(String slideText) throws IOException {
        String slide = "<p:sld><p:cSld><p:spTree><p:sp><p:txBody><a:p><a:r><a:t>"
                + slideText + "</a:t></a:r></a:p></p:txBody></p:sp></p:spTree></p:cSld></p:sld>";
        return createZipBytes(Map.of("ppt/slides/slide1.xml", slide));
    }

    private byte[] createZipBytes(Map<String, String> entries) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(out)) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zipOut.putNextEntry(new ZipEntry(entry.getKey()));
                zipOut.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zipOut.closeEntry();
            }
            zipOut.finish();
            return out.toByteArray();
        }
    }

    private byte[] createBmpBytes() throws IOException {
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, "bmp", out);
            return out.toByteArray();
        }
    }
}
