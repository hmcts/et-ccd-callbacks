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
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3NotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ServingService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@WebMvcTest({Et3NotificationController.class, JsonMapper.class})
class Et3NotificationControllerTest extends BaseControllerTest {

    private static final String MID_UPLOAD_DOCUMENTS_URL = "/et3Notification/midUploadDocuments";
    private static final String SUBMITTED_URL = "/et3Notification/submitted";
    private CCDRequest ccdRequestWithET3NotificationDocument;
    private CCDRequest ccdRequestWithoutET3NotificationDocument;

    @MockBean
    private ServingService servingService;
    @MockBean
    private Et3NotificationService et3NotificationService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .withEthosCaseReference("12345/6789")
            .withClaimantType("claimant@unrepresented.com")
            .withRepresentativeClaimantType("Claimant Rep", "claimant@represented.com")
            .withClaimantIndType("Claimant", "LastName", "Mr", "Mr")
            .withRespondentWithAddress("Respondent Unrepresented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null, "respondent@unrepresented.com")
            .withRespondentWithAddress("Respondent Represented",
                "32 Sweet Street", "14 House", null,
                "Manchester", "M11 4ED", "United Kingdom",
                null)
            .withRespondentRepresentative("Respondent Represented", "Rep LastName", "res@rep.com")
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        CaseData caseDataWithNotificationDocument = caseDetails.getCaseData();
        caseDataWithNotificationDocument.setClaimant("Claimant LastName");
        caseDataWithNotificationDocument.setEt3NotificationDocCollection(List.of(DocumentTypeItem.builder()
                .value(DocumentType.builder().typeOfDocument("2.11").build()).build()));

        ccdRequestWithET3NotificationDocument = CCDRequestBuilder.builder()
                .withCaseData(caseDataWithNotificationDocument).build();
        CaseData caseDataWithoutET3NotificationDocument = new CaseData();
        caseDataWithoutET3NotificationDocument.setEt3NotificationDocCollection(List.of());
        ccdRequestWithoutET3NotificationDocument = CCDRequestBuilder.builder()
                .withCaseData(caseDataWithoutET3NotificationDocument).build();

    }

    @Test
    void midUploadDocuments_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(servingService.generateOtherTypeDocumentLink(anyList())).thenReturn("expectedDocumentName");
        when(servingService.generateEmailLinkToAcas(any(), anyBoolean())).thenReturn("expectedLink");
        mvc.perform(post(MID_UPLOAD_DOCUMENTS_URL)
                .content(jsonMapper.toJson(ccdRequestWithET3NotificationDocument))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.et3OtherTypeDocumentName", notNullValue()))
            .andExpect(jsonPath("$.data.et3EmailLinkToAcas", notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(servingService, times(1)).generateOtherTypeDocumentLink(anyList());
        verify(servingService, times(1)).generateEmailLinkToAcas(any(), anyBoolean());
    }

    @Test
    void midUploadDocuments_invalidET3NotificationDocumentList() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_UPLOAD_DOCUMENTS_URL)
                        .content(jsonMapper.toJson(ccdRequestWithoutET3NotificationDocument))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()));
    }

    @Test
    void midUploadDocuments_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(MID_UPLOAD_DOCUMENTS_URL)
                .content(jsonMapper.toJson(ccdRequestWithET3NotificationDocument))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void midUploadDocuments_badRequest() throws Exception {
        mvc.perform(post(SUBMITTED_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void submitted_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(SUBMITTED_URL)
                .content(jsonMapper.toJson(ccdRequestWithET3NotificationDocument))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.confirmation_header", notNullValue()))
            .andExpect(jsonPath("$.confirmation_body", notNullValue()))
            .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
            .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
            .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void submitted_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(SUBMITTED_URL)
                .content(jsonMapper.toJson(ccdRequestWithET3NotificationDocument))
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    void submitted_badRequest() throws Exception {
        mvc.perform(post(SUBMITTED_URL)
                .content("garbage content")
                .header("Authorization", AUTH_TOKEN)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }
}
