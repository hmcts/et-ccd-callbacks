package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.AmendRepresentativeContactService;
import uk.gov.hmcts.ethos.replacement.docmosis.test.utils.LoggerTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS;

@ExtendWith(SpringExtension.class)
@WebMvcTest({AmendRepresentativeContactController.class, JsonMapper.class})
@ContextConfiguration(classes = DocmosisApplication.class)
class AmendRepresentativeContactControllerTest extends BaseControllerTest {

    private static final String ABOUT_TO_START = "/amendRepresentativeContact/aboutToStart";
    private static final String ABOUT_TO_SUBMIT = "/amendRepresentativeContact/aboutToSubmit";
    private static final String MID_EVENT = "/amendRepresentativeContact/midEvent";

    @Autowired
    private WebApplicationContext applicationContext;
    @MockitoBean
    private AmendRepresentativeContactService amendRepresentativeContactService;
    private MockMvc mvc;
    private CCDRequest ccdRequest;

    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    @Override
    @SneakyThrows
    protected void setUp() {
        super.setUp();
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();

        CaseDetails caseDetails = CaseDataBuilder.builder()
                .withEthosCaseReference("1234567/1234")
                .withRespondent("test", NO, null, false)
                .withSubmitEt3Respondent("test")
                .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);
        caseDetails.getCaseData().setClaimant("Claimant LastName");
        caseDetails.getCaseData().setTribunalCorrespondenceEmail("tribunal@email.com");

        ccdRequest = CCDRequestBuilder.builder()
                .withCaseData(caseDetails.getCaseData())
                .withCaseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .build();
        ccdRequest.getCaseDetails().setCaseId("1683646754393041");

    }

    @Test
    @SneakyThrows
    void theAboutToStart() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doNothing().when(amendRepresentativeContactService).loadStagedContactDetails(AUTH_TOKEN,
                ccdRequest.getCaseDetails().getCaseData(), ccdRequest.getCaseDetails().getCaseId());
        mvc.perform(post(ABOUT_TO_START)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(LoggerTestUtils.INTEGER_ZERO)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void theAboutToStart_WithException() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doThrow(new GenericServiceException(ERROR_CASE_DATA_NOT_FOUND,
                new Exception(ERROR_CASE_DATA_NOT_FOUND),
                ERROR_CASE_DATA_NOT_FOUND,
                StringUtils.EMPTY,
                "amendRepresentativeContactService",
                "loadStagedContactDetails")).when(amendRepresentativeContactService)
                .loadStagedContactDetails(anyString(), any(CaseData.class), anyString());
        mvc.perform(post(ABOUT_TO_START)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(LoggerTestUtils.INTEGER_ONE)))
                .andExpect(jsonPath("$.errors[0]", is(ERROR_CASE_DATA_NOT_FOUND)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void theMidEventAmendRepresentativeContact() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doNothing().when(amendRepresentativeContactService).setStagedMyHmctsContactAddress(AUTH_TOKEN,
                ccdRequest.getCaseDetails().getCaseData());
        ccdRequest.getCaseDetails().getCaseData().setRepresentativeContactChangeOption(
                REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS);
        mvc.perform(post(MID_EVENT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(LoggerTestUtils.INTEGER_ZERO)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        ccdRequest.getCaseDetails().getCaseData().setRepresentativeContactChangeOption("DUMMY");
    }

    @Test
    @SneakyThrows
    void theMidEventAmendRepresentativeContact_skipsMyHmctsWhenAmendOptionSelected() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        ccdRequest.getCaseDetails().getCaseData().setRepresentativeContactChangeOption("Amend contact details");
        mvc.perform(post(MID_EVENT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(LoggerTestUtils.INTEGER_ZERO)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(amendRepresentativeContactService, never())
                .setStagedMyHmctsContactAddress(anyString(), any(CaseData.class));
        ccdRequest.getCaseDetails().getCaseData().setRepresentativeContactChangeOption("DUMMY");
    }

    @Test
    @SneakyThrows
    void theMidEventAmendRepresentativeContact_WithException() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doThrow(new GenericServiceException(ERROR_CASE_DATA_NOT_FOUND,
                new Exception(ERROR_CASE_DATA_NOT_FOUND),
                ERROR_CASE_DATA_NOT_FOUND,
                StringUtils.EMPTY,
                "amendRepresentativeContactService",
                "setStagedMyHmctsContactAddress")).when(amendRepresentativeContactService)
                .setStagedMyHmctsContactAddress(anyString(), any(CaseData.class));
        ccdRequest.getCaseDetails().getCaseData().setRepresentativeContactChangeOption(
                REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS);
        mvc.perform(post(MID_EVENT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(LoggerTestUtils.INTEGER_ONE)))
                .andExpect(jsonPath("$.errors[0]", is(ERROR_CASE_DATA_NOT_FOUND)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        ccdRequest.getCaseDetails().getCaseData().setRepresentativeContactChangeOption("DUMMY");
    }

    @Test
    @SneakyThrows
    void theAboutToSubmitRepresentativeContactDetails() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        Address stagingAddress = new Address();
        stagingAddress.setAddressLine1("221b Baker Street");
        Address et3Address = new Address();
        et3Address.setAddressLine1("10 Downing Street");
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setRespRepAddress(stagingAddress);
        caseData.setRespRepPhoneNumber("07234567890");
        caseData.setMyHmctsAddressText("221b Baker Street, London");
        caseData.setEt3ResponseAddress(et3Address);
        caseData.setEt3ResponsePhone("01234567890");
        doNothing().when(amendRepresentativeContactService).saveStagedContactDetails(AUTH_TOKEN,
                caseData, ccdRequest.getCaseDetails().getCaseId());
        mvc.perform(post(ABOUT_TO_SUBMIT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                // the live ET3 response fields must be left untouched by this event
                .andExpect(jsonPath("$.data.et3ResponseAddress.AddressLine1", is("10 Downing Street")))
                .andExpect(jsonPath("$.data.et3ResponsePhone", is("01234567890")))
                .andExpect(jsonPath("$.errors.size()", is(LoggerTestUtils.INTEGER_ZERO)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }

    @Test
    @SneakyThrows
    void theAboutToSubmitRepresentativeContactDetails_WithException() {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        doThrow(new GenericServiceException(ERROR_CASE_DATA_NOT_FOUND,
                new Exception(ERROR_CASE_DATA_NOT_FOUND),
                ERROR_CASE_DATA_NOT_FOUND,
                StringUtils.EMPTY,
                "amendRepresentativeContactService",
                "saveStagedContactDetails")).when(amendRepresentativeContactService)
                .saveStagedContactDetails(anyString(), any(CaseData.class), anyString());
        mvc.perform(post(ABOUT_TO_SUBMIT)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath("$.errors.size()", is(LoggerTestUtils.INTEGER_ONE)))
                .andExpect(jsonPath("$.errors[0]", is(ERROR_CASE_DATA_NOT_FOUND)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
    }
}
