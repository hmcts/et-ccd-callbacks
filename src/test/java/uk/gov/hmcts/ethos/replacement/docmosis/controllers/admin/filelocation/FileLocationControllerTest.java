package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.filelocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.FileLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.SaveFileLocationException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.FileLocationService.ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.FileLocationService.ERROR_FILE_LOCATION_NOT_FOUND_BY_TRIBUNAL_OFFICE;

@ExtendWith(SpringExtension.class)
@WebMvcTest({FileLocationController.class, JsonMapper.class})
class FileLocationControllerTest {

    @MockBean
    private VerifyTokenService verifyTokenService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockBean
    private FileLocationService fileLocationService;

    @Test
    void testAddFileLocationSuccess() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "ABERDEEN")
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/admin/filelocation/addFileLocation")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationService, times(1)).saveFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testAddFileLocationError() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "Aberdeen")
                .buildAsCCDRequest();

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        String error = String.format(ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE,
                adminData.getFileLocationCode());

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        doThrow(new SaveFileLocationException(error)).when(fileLocationService).saveFileLocation(adminData);

        mockMvc.perform(post("/admin/filelocation/addFileLocation")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(jsonPath("$.errors[0]", is(error)));
        verify(fileLocationService, times(1)).saveFileLocation(adminData);
    }

    @Test
    void testAddFileLocationInvalidToken() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "ABERDEEN")
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/filelocation/addFileLocation")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(fileLocationService, never()).saveFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testInitAdminDataSuccess() throws Exception {

        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "ABERDEEN")
                .buildAsCCDRequest();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/admin/filelocation/initAdminData")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, nullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));

        verify(fileLocationService, times(1)).initAdminData(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testInitAdminDataBadRequest() throws Exception {
        mockMvc.perform(post("/admin/filelocation/initAdminData")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", "user-token")
                        .content("bad-request"))
                .andExpect(status().isBadRequest());

        verify(verifyTokenService, never()).verifyTokenSignature(anyString());
        verify(fileLocationService, never()).initAdminData(any(AdminData.class));
    }
  
    @Test
    void testUpdateFileLocationSuccess() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "ABERDEEN")
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/admin/filelocation/updateFileLocation")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationService, times(1)).updateFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testUpdateFileLocationInvalidToken() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "ABERDEEN")
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/filelocation/updateFileLocation")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(fileLocationService, never()).updateFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testUpdateFileLocationError() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "Aberdeen")
                .buildAsCCDRequest();

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        String error = String.format(ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE,
                adminData.getFileLocationCode());

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        when(fileLocationService.updateFileLocation(adminData)).thenReturn(Arrays.asList(error));

        mockMvc.perform(post("/admin/filelocation/updateFileLocation")
                        .contentType(APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath("$.errors[0]", notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationService, times(1)).updateFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testDeleteFileLocationSuccess() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "ABERDEEN")
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/admin/filelocation/deleteFileLocation")
                .contentType(APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationService, times(1)).deleteFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testDeleteFileLocationInvalidToken() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "ABERDEEN")
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/filelocation/deleteFileLocation")
                .contentType(APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(fileLocationService, never()).deleteFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testDeleteFileLocationError() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "Aberdeen")
                .buildAsCCDRequest();

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        String error = String.format(ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE,
                adminData.getFileLocationCode());

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        when(fileLocationService.deleteFileLocation(adminData)).thenReturn(Collections.singletonList(error));

        mockMvc.perform(post("/admin/filelocation/deleteFileLocation")
                .contentType(APPLICATION_JSON)
                .header("Authorization", token)
                .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath("$.errors[0]", notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationService, times(1)).deleteFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void midEventSelectFileLocationSuccess() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "Aberdeen")
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        when(fileLocationService.midEventSelectFileLocation(any())).thenReturn(new ArrayList<>());
        mockMvc.perform(post("/admin/filelocation/midEventSelectFileLocation")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationService, times(1))
                .midEventSelectFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void midEventSelectFileLocationInvalidToken() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "Aberdeen")
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/filelocation/midEventSelectFileLocation")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(fileLocationService, never())
                .midEventSelectFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void midEventSelectFileLocationError() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "Aberdeen")
                .buildAsCCDRequest();

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        String error = String.format(ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE,
                adminData.getFileLocationCode());

        when(fileLocationService.midEventSelectFileLocation(any())).thenReturn(Arrays.asList(error));
        mockMvc.perform(post("/admin/filelocation/midEventSelectFileLocation")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath("$.errors[0]", notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationService, times(1))
                .midEventSelectFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void midEventSelectTribunalOfficeSuccess() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "Aberdeen")
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        when(fileLocationService.midEventSelectTribunalOffice(any())).thenReturn(new ArrayList<>());
        mockMvc.perform(post("/admin/filelocation/midEventSelectTribunalOffice")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(0)))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationService, times(1))
                .midEventSelectTribunalOffice(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void midEventSelectTribunalOfficeInvalidToken() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "Aberdeen")
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/filelocation/midEventSelectTribunalOffice")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(fileLocationService, never())
                .midEventSelectTribunalOffice(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void midEventSelectTribunalOfficeError() throws Exception {
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "Aberdeen")
                .buildAsCCDRequest();

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        String error = String.format(ERROR_FILE_LOCATION_NOT_FOUND_BY_TRIBUNAL_OFFICE,
                adminData.getFileLocationCode());

        when(fileLocationService.midEventSelectTribunalOffice(any())).thenReturn(Arrays.asList(error));
        mockMvc.perform(post("/admin/filelocation/midEventSelectTribunalOffice")
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(JsonMapper.DATA, notNullValue()))
                .andExpect(jsonPath(JsonMapper.ERRORS, hasSize(1)))
                .andExpect(jsonPath("$.errors[0]", notNullValue()))
                .andExpect(jsonPath(JsonMapper.WARNINGS, nullValue()));
        verify(fileLocationService, times(1))
                .midEventSelectTribunalOffice(ccdRequest.getCaseDetails().getAdminData());
    }

}
