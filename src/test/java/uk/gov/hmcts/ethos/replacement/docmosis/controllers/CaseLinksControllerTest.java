package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLink;
import uk.gov.hmcts.et.common.model.ccd.types.LinkReason;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLinksEmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
@WebMvcTest({CaseLinksController.class, JsonMapper.class})
class CaseLinksControllerTest {

    private static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String CREATE_SUBMITTED_URL = "/caseLinks/create/aboutToSubmit";

    private static final String MAINTAIN_SUBMITTED_URL = "/caseLinks/maintain/aboutToSubmit";

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private CaseLinksEmailService caseLinksEmailService;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;

    @BeforeEach
    void setUp() {
        when(featureToggleService.isHmcEnabled()).thenReturn(true);
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseDetails.getCaseData().setEthosCaseReference("1234");

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseDetails.getCaseData())
                .build();

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
    }

    @ParameterizedTest
    @MethodSource("requests")
    @SneakyThrows
    void submitted_tokenOk(String url) {
        mockMvc.perform(post(url)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
    }

    @ParameterizedTest
    @MethodSource("requests")
    @SneakyThrows
    void aboutToSubmit_badRequest(String url) {
        mockMvc.perform(post(url)
                        .content("garbage content")
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @MethodSource("requests")
    @SneakyThrows
    void testStatusForbidden(String url) {
        CCDRequest localCcdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(url)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(localCcdRequest)))
                .andExpect(status().isForbidden());
    }

    private static Stream<Arguments> requests() {
        return Stream.of(
                Arguments.of(CREATE_SUBMITTED_URL),
                Arguments.of(MAINTAIN_SUBMITTED_URL)
        );
    }

    @Test
    @SneakyThrows
    void testInitTransferToEnglandWalesForbidden() {
        CCDRequest localCcdRequest = CCDRequestBuilder.builder().build();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);

        mockMvc.perform(post(CREATE_SUBMITTED_URL)
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonMapper.toJson(localCcdRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @SneakyThrows
    void testHearingIsLinkedFlagIsYes() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(CREATE_SUBMITTED_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()))
                .andExpect(jsonPath("$.data.hearingIsLinkedFlag").value(YES));
    }

    @Test
    @SneakyThrows
    void testHearingIsLinkedFlagIsFalse() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(MAINTAIN_SUBMITTED_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()))
                .andExpect(jsonPath("$.data.hearingIsLinkedFlag").value(NO));
    }

    @Test
    @SneakyThrows
    void shouldNotSetHearingIsLinkedFlagWhenLinksRemain() {

        CaseLink caseLink1 = getCaseLink();
        CaseLink caseLink2 = getCaseLink();

        ListTypeItem<CaseLink> caseLinks = ListTypeItem.from(caseLink1, caseLink2);

        ccdRequest.getCaseDetails().getCaseData().setCaseLinks(caseLinks);
        ccdRequest.getCaseDetails().getCaseData().setHearingIsLinkedFlag(YES);

        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);

        mockMvc.perform(post(MAINTAIN_SUBMITTED_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()))
                .andExpect(jsonPath("$.data.hearingIsLinkedFlag").value(YES));
    }

    private CaseLink getCaseLink() {
        LinkReason linkReason = new LinkReason();
        linkReason.setReason("CLRC016");
        ListTypeItem<LinkReason> linkReasons = ListTypeItem.from(linkReason, "1");

        return CaseLink.builder().caseReference("1").caseType(ENGLANDWALES_CASE_TYPE_ID)
                .reasonForLink(linkReasons).build();
    }
}
