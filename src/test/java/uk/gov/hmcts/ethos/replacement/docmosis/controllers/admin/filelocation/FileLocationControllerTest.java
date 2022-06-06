package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.filelocation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.FileLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.SaveFileLocationException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.FileLocationService.ADD_FILE_LOCATION_CODE_AND_OFFICE_CONFLICT_ERROR;

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
        var ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "ABERDEEN")
                .buildAsCCDRequest();

        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/admin/filelocation/addFileLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(fileLocationService, times(1)).saveFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testAddFileLocationError() throws Exception {
        var ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "Aberdeen")
                .buildAsCCDRequest();

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        String error = String.format(ADD_FILE_LOCATION_CODE_AND_OFFICE_CONFLICT_ERROR,
                adminData.getFileLocationCode(), TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice()));

        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        doThrow(new SaveFileLocationException(error)).when(fileLocationService).saveFileLocation(adminData);

        mockMvc.perform(post("/admin/filelocation/addFileLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(jsonPath("$.errors[0]", is(error)));
        verify(fileLocationService, times(1)).saveFileLocation(adminData);
    }

    @Test
    void testAddFileLocationInvalidToken() throws Exception {
        var ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "ABERDEEN")
                .buildAsCCDRequest();

        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/filelocation/addFileLocation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(fileLocationService, never()).saveFileLocation(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testInitAdminDataSuccess() throws Exception {

        var ccdRequest = AdminDataBuilder
                .builder()
                .withFileLocationData("testCode", "testName", "ABERDEEN")
                .buildAsCCDRequest();
        var token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/admin/filelocation/initAdminData")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));

        verify(fileLocationService, times(1)).initAdminData(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testInitAdminDataBadRequest() throws Exception {
        mockMvc.perform(post("/admin/filelocation/initAdminData")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "user-token")
                        .content("bad-request"))
                .andExpect(status().isBadRequest());

        verify(verifyTokenService, never()).verifyTokenSignature(anyString());
        verify(fileLocationService, never()).initAdminData(any(AdminData.class));
    }
}
