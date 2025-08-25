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
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseUpdateForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et3NotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ServingService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
@WebMvcTest({Et3NotificationController.class, JsonMapper.class})
class Et3NotificationControllerTest extends BaseControllerTest {

    private static final String MID_UPLOAD_DOCUMENTS_URL = "/et3Notification/midUploadDocuments";
    private static final String ABOUT_TO_START_URL = "/et3Notification/aboutToStart";
    private static final String ABOUT_TO_SUBMIT_URL = "/et3Notification/aboutToSubmit";
    private static final String SUBMITTED_URL = "/et3Notification/submitted";
    private static final String ET3_NOTIFICATION_ACCEPTED_DOCUMENT_TYPE = "2.11";
    private static final String ET3_NOTIFICATION_REJECTED_DOCUMENT_TYPE = "2.12";
    private CCDRequest ccdRequestWithoutRespondentResponse;
    private CCDRequest ccdRequestWithET3NotificationDocument;
    private CCDRequest ccdRequestInvalidET3NotificationDocumentCollection;
    private CCDRequest ccdRequestWithoutRespondentResponseAcceptedState;

    @MockBean
    private ServingService servingService;
    @MockBean
    private Et3NotificationService et3NotificationService;
    @MockBean
    private CaseUpdateForCaseWorkerService caseUpdateForCaseWorkerService;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    @Override
    protected void setUp() throws IOException, URISyntaxException {
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
        caseDataWithNotificationDocument.getRespondentCollection().get(0)
                .getValue().setResponseStatus(ACCEPTED_STATE);
        caseDataWithNotificationDocument.setEt3NotificationDocCollection(List.of(DocumentTypeItem.builder()
                .value(DocumentType.builder().typeOfDocument(
                        ET3_NOTIFICATION_ACCEPTED_DOCUMENT_TYPE).build()).build()));
        ccdRequestWithET3NotificationDocument = CCDRequestBuilder.builder()
                .withCaseData(caseDataWithNotificationDocument).build();

        CaseData caseDataRejectedET3NotificationDocument = new CaseData();
        caseDataRejectedET3NotificationDocument.setEt3NotificationDocCollection(List.of(
                DocumentTypeItem.builder().value(DocumentType.builder()
                        .typeOfDocument(ET3_NOTIFICATION_REJECTED_DOCUMENT_TYPE).build()).build(),
                DocumentTypeItem.builder().value(DocumentType.builder()
                        .typeOfDocument(ET3_NOTIFICATION_ACCEPTED_DOCUMENT_TYPE).build()).build()
        ));
        ccdRequestInvalidET3NotificationDocumentCollection = CCDRequestBuilder.builder()
                .withCaseData(caseDataRejectedET3NotificationDocument).build();

        CaseData caseDataWithoutRespondentResponseAcceptedState = new CaseData();
        caseDataRejectedET3NotificationDocument.setEt3NotificationDocCollection(List.of(
                DocumentTypeItem.builder().value(DocumentType.builder()
                        .typeOfDocument(ET3_NOTIFICATION_ACCEPTED_DOCUMENT_TYPE).build()).build()
        ));
        ccdRequestWithoutRespondentResponseAcceptedState = CCDRequestBuilder.builder()
                .withCaseData(caseDataWithoutRespondentResponseAcceptedState).build();

        CaseData caseDataWithoutRespondentResponse = new CaseData();
        RespondentSumTypeItem respondentSumTypeItem = new RespondentSumTypeItem();
        respondentSumTypeItem.setValue(RespondentSumType.builder().build());
        caseDataWithoutRespondentResponse.setRespondentCollection(List.of(respondentSumTypeItem));
        ccdRequestWithoutRespondentResponse = CCDRequestBuilder.builder()
                .withCaseData(caseDataWithoutRespondentResponse).build();

    }

    @Test
    void aboutToStart_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequestWithET3NotificationDocument))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.ERRORS, emptyCollectionOf(String.class)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void aboutToStart_noRespondentResponseStatus() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(servingService.generateOtherTypeDocumentLink(anyList())).thenReturn("expectedDocumentName");
        when(servingService.generateEmailLinkToAcas(any(), anyBoolean())).thenReturn("expectedLink");
        mvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequestWithoutRespondentResponse))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue(String.class)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void aboutToStart_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ABOUT_TO_START_URL)
                        .content(jsonMapper.toJson(ccdRequestWithET3NotificationDocument))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
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
                .andExpect(jsonPath(JsonMapper.ERRORS, emptyCollectionOf(String.class)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(servingService, times(1)).generateOtherTypeDocumentLink(anyList());
        verify(servingService, times(1)).generateEmailLinkToAcas(any(), anyBoolean());
    }

    @Test
    void midUploadDocuments_invalidET3NotificationDocumentList() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_UPLOAD_DOCUMENTS_URL)
                        .content(jsonMapper.toJson(ccdRequestInvalidET3NotificationDocumentCollection))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()));
    }

    @Test
    void midUploadDocuments_invalidRespondentCollection() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(MID_UPLOAD_DOCUMENTS_URL)
                        .content(jsonMapper.toJson(ccdRequestWithoutRespondentResponseAcceptedState))
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
    void aboutToSubmit_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequestWithET3NotificationDocument))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.respondentCollection", notNullValue()))
                .andExpect(jsonPath("$.data.respondentCollection[0].value.et3NotificationAcceptedDate",
                        notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, emptyCollectionOf(String.class)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void aboutToSubmit_tokenFail() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(post(ABOUT_TO_SUBMIT_URL)
                        .content(jsonMapper.toJson(ccdRequestWithET3NotificationDocument))
                        .header("Authorization", AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitted_tokenOk() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseId(1L);
        when(caseUpdateForCaseWorkerService.caseUpdateRequest(any(), anyString()))
                .thenReturn(submitEvent);
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
