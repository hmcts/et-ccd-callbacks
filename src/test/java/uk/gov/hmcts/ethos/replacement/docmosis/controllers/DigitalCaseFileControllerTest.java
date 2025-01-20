package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.client.BundleApiClient;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelperTest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DigitalCaseFileService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_DATE_TIME_PATTERN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_ATTACHMENT;

@ExtendWith(SpringExtension.class)
@WebMvcTest({DigitalCaseFileController.class, JsonMapper.class})
class DigitalCaseFileControllerTest extends BaseControllerTest {

    private static final String ASYNC_ABOUT_TO_SUBMIT_URL = "/dcf/asyncAboutToSubmit";
    private static final String ASYNC_COMPLETE_ABOUT_TO_SUBMIT_URL = "/dcf/asyncCompleteAboutToSubmit";

    @MockBean
    private BundleApiClient bundleApiClient;
    @MockBean
    private DigitalCaseFileService digitalCaseFileService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    private CCDRequest ccdRequest;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("123456/2021")
                .withClaimantIndType("First", "Last")
                .withDocumentCollection(ET1)
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        CaseData caseData = caseDetails.getCaseData();
        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseData)
                .withState(REJECTED_STATE)
                .withCaseId("1234")
                .build();

        UploadDocumentHelperTest.attachDocumentToCollection(caseData, ET1_ATTACHMENT);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
    }

    @Test
    void uploadOrRemoveDcf_Remove() throws Exception {
        DigitalCaseFileType digitalCaseFileType = new DigitalCaseFileType();
        digitalCaseFileType.setUploadedDocument(
                UploadedDocumentType.builder()
                        .documentBinaryUrl("https://test.com/1234/binary")
                        .documentFilename("1234.pdf")
                        .documentUrl("https://test.com/123")
                .build());
        ccdRequest.getCaseDetails().getCaseData().setDigitalCaseFile(digitalCaseFileType);
        ccdRequest.getCaseDetails().getCaseData().setUploadOrRemoveDcf("Remove");
        doCallRealMethod().when(digitalCaseFileService).createUploadRemoveDcf(anyString(), any());
        mockMvc.perform(post(ASYNC_ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.digitalCaseFile", nullValue()));
    }

    @Test
    void uploadOrRemoveDcf_Upload() throws Exception {
        DigitalCaseFileType digitalCaseFileType = new DigitalCaseFileType();
        digitalCaseFileType.setUploadedDocument(
                UploadedDocumentType.builder()
                        .documentBinaryUrl("https://test.com/1234/binary")
                        .documentFilename("1234.pdf")
                        .documentUrl("https://test.com/123")
                        .build());
        ccdRequest.getCaseDetails().getCaseData().setDigitalCaseFile(digitalCaseFileType);
        ccdRequest.getCaseDetails().getCaseData().setUploadOrRemoveDcf("Upload");
        doCallRealMethod().when(digitalCaseFileService).createUploadRemoveDcf(anyString(), any());
        mockMvc.perform(post(ASYNC_ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.digitalCaseFile.status",
                        is("DCF Uploaded: " + LocalDateTime.now().format(NEW_DATE_TIME_PATTERN))));
    }

    @Test
    void asyncAboutToSubmit() throws Exception {
        when(bundleApiClient.asyncStitchBundle(anyString(), anyString(), any()))
                .thenReturn(ResourceLoader.stitchBundleRequest());
        ccdRequest.getCaseDetails().getCaseData().setUploadOrRemoveDcf("Create");
        mockMvc.perform(post(ASYNC_ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()));
    }

    @Test
    void asyncCompleteAboutToSubmit() throws Exception {
        when(bundleApiClient.asyncStitchBundle(anyString(), anyString(), any()))
                .thenReturn(ResourceLoader.stitchBundleRequest());
        mockMvc.perform(post(ASYNC_COMPLETE_ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }
}