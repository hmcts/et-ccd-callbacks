package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DigitalCaseFileService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DigitalCaseFileHelper.NO_DOCS_FOR_DCF;

@ExtendWith(SpringExtension.class)
@WebMvcTest({DigitalCaseFileController.class, JsonMapper.class})
class DigitalCaseFileControllerTest extends BaseControllerTest {

    private static final String ABOUT_TO_START_URL = "/dcf/aboutToStart";

    @MockitoBean
    private DigitalCaseFileService digitalCaseFileService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    @Override
    protected void setUp() throws IOException, URISyntaxException {
        super.setUp();
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
    }

    @Test
    void aboutToStart_noDocuments_returnsError() throws Exception {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("765432/2025")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        CCDRequest req = CCDRequestBuilder.builder()
                .withCaseData(caseDetails.getCaseData())
                .withCaseId("2222")
                .build();
        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(req))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is(NO_DOCS_FOR_DCF)));
    }

    @Test
    void aboutToStart_allDocumentsExcluded_returnsError() throws Exception {
        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("123000/2025")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        // Add one excluded document to the documentCollection
        UploadedDocumentType uploaded = UploadedDocumentType.builder()
                .documentFilename("x.pdf")
                .documentUrl("http://dm/doc/xyz")
                .documentBinaryUrl("http://dm/doc/xyz/binary")
                .build();
        DocumentType excluded = DocumentType.builder()
                .docNumber("1")
                .uploadedDocument(uploaded)
                .excludeFromDcf(List.of("Yes"))
                .build();
        DocumentTypeItem item = new DocumentTypeItem();
        item.setId("1");
        item.setValue(excluded);
        caseDetails.getCaseData().setDocumentCollection(List.of(item));

        CCDRequest req = CCDRequestBuilder.builder()
                .withCaseData(caseDetails.getCaseData())
                .withCaseId("3333")
                .build();

        mockMvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(req))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath("$.errors[0]", is(NO_DOCS_FOR_DCF)));
    }
}
