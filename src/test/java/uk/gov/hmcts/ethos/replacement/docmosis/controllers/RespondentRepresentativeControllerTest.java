package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({RespondentRepresentativeController.class, JsonMapper.class})
class RespondentRepresentativeControllerTest {

    private static final String DUMMY_TOKEN = "dummy-token";
    private static final String DUMMY_SUBMISSION_REFERENCE = "1234567890123456";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String ID_RESPONDENT_1 = "12345acc-be68-4614-b6ad-0ca1cfa9e1d5";
    private static final String ID_REPRESENTATIVE_1 = "71240acc-be68-4614-b6ad-0ca1cfa9e1d5";
    private static final String ID_REPRESENTATIVE_2 = "3130e246-5fee-4b35-b805-41d58099625c";
    private static final String RESPONDENT_NAME_1 = "Respondent Name 1";
    private static final String RESPONDENT_NAME_2 = "Respondent Name 2";
    private static final String DUMMY_EXCEPTION_MESSAGE = "dummy exception message";
    private static final String REPRESENTATIVE_NAME = "Representative Name";

    private static final String URL_REMOVE_OWN_REPRESENTATIVE = "/respondentRepresentative/removeOwnRepresentative";
    private static final String URL_AMEND_RESPONDENT_REPRESENTATIVE_ABOUT_TO_START =
            "/respondentRepresentative/amendRespondentRepresentativeAboutToStart";
    private static final String URL_AMEND_RESPONDENT_REPRESENTATIVE_MID_EVENT =
            "/respondentRepresentative/amendRespondentRepresentativeMidEvent";
    private static final String URL_AMEND_RESPONDENT_REPRESENTATIVE_ABOUT_TO_SUBMIT =
            "/respondentRepresentative/amendRespondentRepresentativeAboutToSubmit";
    private static final String URL_AMEND_RESPONDENT_REPRESENTATIVE_SUBMITTED =
            "/respondentRepresentative/amendRespondentRepresentativeSubmitted";

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private NocRespondentHelper nocRespondentHelper;
    @MockBean
    private NocRespondentRepresentativeService nocRespondentRepresentativeService;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        when(verifyTokenService.verifyTokenSignature(DUMMY_TOKEN)).thenReturn(true);
    }

    @Test
    void testRemoveOwnRepresentative_withoutRepCollection() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        mockMvc.perform(post(URL_REMOVE_OWN_REPRESENTATIVE)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void testRemoveOwnRepresentative_withoutRepCollectionToRemove() throws Exception {
        CCDRequest ccdRequest = CCDRequestBuilder.builder().build();
        CaseData caseData = CaseDataBuilder.builder().build();
        RepresentedTypeRItem representedTypeRItem = RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_1).build();
        caseData.setRepCollection(List.of(representedTypeRItem));
        mockMvc.perform(post(URL_REMOVE_OWN_REPRESENTATIVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void testRemoveOwnRepresentative_RepCollectionToRemoveNotExistsInRepCollection() throws Exception {
        CaseData caseData = CaseDataBuilder.builder().build();
        RepresentedTypeRItem representedTypeRItem = RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_1).build();
        caseData.setRepCollection(List.of(representedTypeRItem));
        RepresentedTypeRItem representedTypeRItemToRemove = RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_2)
                .build();
        caseData.setRepCollectionToRemove(List.of(representedTypeRItemToRemove));
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        mockMvc.perform(post(URL_REMOVE_OWN_REPRESENTATIVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    void testRemoveOwnRepresentative_withRepCollection() throws Exception {
        CaseData caseData = CaseDataBuilder.builder().build();
        RepresentedTypeRItem representedTypeRItem = RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_1).build();
        caseData.setRepCollection(List.of(representedTypeRItem));
        caseData.setRepCollectionToRemove(List.of(representedTypeRItem));
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        mockMvc.perform(post(URL_REMOVE_OWN_REPRESENTATIVE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentativeAboutToStart() {
        CaseData caseData = new CaseData();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        caseData.setRespondentCollection(List.of(respondent));
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        ccdRequest.getCaseDetails().setCaseId(DUMMY_SUBMISSION_REFERENCE);
        mockMvc.perform(post(URL_AMEND_RESPONDENT_REPRESENTATIVE_ABOUT_TO_START)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentativeAboutToSubmit() {
        CaseData caseData = new CaseData();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        respondent.setId(ID_RESPONDENT_1);
        caseData.setRespondentCollection(List.of(respondent));
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(RESPONDENT_NAME_1);
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_1).value(
                RepresentedTypeR.builder().dynamicRespRepName(dynamicFixedListType)
                        .nameOfRepresentative(REPRESENTATIVE_NAME).build()).build()));
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        ccdRequest.getCaseDetails().setCaseId(DUMMY_SUBMISSION_REFERENCE);
        doNothing().when(nocRespondentRepresentativeService).validateRepresentativesOrganisationsAndEmails(
                any(CaseData.class));
        mockMvc.perform(post(URL_AMEND_RESPONDENT_REPRESENTATIVE_ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, empty()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentativeAboutToSubmitWithError() {
        CaseData caseData = new CaseData();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        respondent.setId(ID_RESPONDENT_1);
        caseData.setRespondentCollection(List.of(respondent));
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(RESPONDENT_NAME_2);
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_1).value(
                RepresentedTypeR.builder().dynamicRespRepName(dynamicFixedListType)
                        .nameOfRepresentative(REPRESENTATIVE_NAME).build()).build()));
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        ccdRequest.getCaseDetails().setCaseId(DUMMY_SUBMISSION_REFERENCE);
        doNothing().when(nocRespondentRepresentativeService).validateRepresentativesOrganisationsAndEmails(
                any(CaseData.class));
        mockMvc.perform(post(URL_AMEND_RESPONDENT_REPRESENTATIVE_ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentativeAboutToSubmitWithGenericServiceException() {
        CaseData caseData = new CaseData();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        respondent.setId(ID_RESPONDENT_1);
        caseData.setRespondentCollection(List.of(respondent));
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(RESPONDENT_NAME_1);
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_1).value(
                RepresentedTypeR.builder().dynamicRespRepName(dynamicFixedListType).build()).build()));
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        ccdRequest.getCaseDetails().setCaseId(DUMMY_SUBMISSION_REFERENCE);
        doNothing().when(nocRespondentRepresentativeService).validateRepresentativesOrganisationsAndEmails(
                any(CaseData.class));
        mockMvc.perform(post(URL_AMEND_RESPONDENT_REPRESENTATIVE_ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentativeAboutToSubmitWithRuntimeException() {
        CaseData caseData = new CaseData();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        respondent.setId(ID_RESPONDENT_1);
        caseData.setRespondentCollection(List.of(respondent));
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(RESPONDENT_NAME_1);
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_1).value(
                RepresentedTypeR.builder().dynamicRespRepName(dynamicFixedListType)
                        .nameOfRepresentative(REPRESENTATIVE_NAME).build()).build()));
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        ccdRequest.getCaseDetails().setCaseId(DUMMY_SUBMISSION_REFERENCE);
        doNothing().when(nocRespondentRepresentativeService).validateRepresentativesOrganisationsAndEmails(
                any(CaseData.class));
        when(nocRespondentRepresentativeService.prepopulateOrgAddress(any(CaseData.class), anyString())).thenThrow(
                new GenericRuntimeException(new GenericServiceException(DUMMY_EXCEPTION_MESSAGE,
                        new Exception(DUMMY_EXCEPTION_MESSAGE), DUMMY_EXCEPTION_MESSAGE, DUMMY_SUBMISSION_REFERENCE,
                        RespondentRepresentativeController.class.getSimpleName(),
                        "amendRespondentRepresentativeAboutToSubmit")));
        mockMvc.perform(post(URL_AMEND_RESPONDENT_REPRESENTATIVE_ABOUT_TO_SUBMIT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentativeMidEvent() {
        CaseData caseData = new CaseData();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        respondent.setId(ID_RESPONDENT_1);
        caseData.setRespondentCollection(List.of(respondent));
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(RESPONDENT_NAME_1);
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_1).value(
                RepresentedTypeR.builder().dynamicRespRepName(dynamicFixedListType).build()).build()));
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        ccdRequest.getCaseDetails().setCaseId(DUMMY_SUBMISSION_REFERENCE);
        doNothing().when(nocRespondentRepresentativeService).validateRepresentativesOrganisationsAndEmails(
                any(CaseData.class));
        mockMvc.perform(post(URL_AMEND_RESPONDENT_REPRESENTATIVE_MID_EVENT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentativeMidEventThrowsException() {
        CaseData caseData = new CaseData();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        respondent.setId(ID_RESPONDENT_1);
        caseData.setRespondentCollection(List.of(respondent));
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(RESPONDENT_NAME_1);
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_1).value(
                RepresentedTypeR.builder().dynamicRespRepName(dynamicFixedListType).build()).build()));
        CCDRequest ccdRequest = CCDRequestBuilder.builder().withCaseData(caseData).build();
        ccdRequest.getCaseDetails().setCaseId(DUMMY_SUBMISSION_REFERENCE);
        doThrow(GenericServiceException.class).when(nocRespondentRepresentativeService)
                .validateRepresentativesOrganisationsAndEmails(any(CaseData.class));
        mockMvc.perform(post(URL_AMEND_RESPONDENT_REPRESENTATIVE_MID_EVENT)
                        .content(jsonMapper.toJson(ccdRequest))
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void amendRespondentRepresentativeSubmitted() {
        CaseData caseData = new CaseData();
        RespondentSumTypeItem respondent = new RespondentSumTypeItem();
        respondent.setValue(RespondentSumType.builder().respondentName(RESPONDENT_NAME_1).build());
        respondent.setId(ID_RESPONDENT_1);
        caseData.setRespondentCollection(List.of(respondent));
        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setLabel(RESPONDENT_NAME_1);
        dynamicFixedListType.setValue(dynamicValueType);
        caseData.setRepCollection(List.of(RepresentedTypeRItem.builder().id(ID_REPRESENTATIVE_1).value(
                RepresentedTypeR.builder().dynamicRespRepName(dynamicFixedListType).build()).build()));
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails)
                .caseDetailsBefore(caseDetails).build();
        callbackRequest.getCaseDetails().setCaseId(DUMMY_SUBMISSION_REFERENCE);
        callbackRequest.getCaseDetailsBefore().setCaseId(DUMMY_SUBMISSION_REFERENCE);
        mockMvc.perform(post(URL_AMEND_RESPONDENT_REPRESENTATIVE_SUBMITTED)
                        .content(jsonMapper.toJson(callbackRequest))
                        .header(HEADER_AUTHORIZATION, DUMMY_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

}
