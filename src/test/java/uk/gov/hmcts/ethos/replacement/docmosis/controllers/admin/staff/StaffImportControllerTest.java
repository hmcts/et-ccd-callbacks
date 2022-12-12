package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.staff;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.StaffImportService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.io.IOException;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({StaffImportController.class, JsonMapper.class})
class StaffImportControllerTest {

    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private StaffImportService staffImportService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void testImportSuccess() throws Exception {
        String documentBinaryUrl = "http://dm-store:8888/documents/12131212";
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withStaffImportFile(documentBinaryUrl)
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/admin/staff/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(staffImportService, times(1)).importStaff(ccdRequest.getCaseDetails().getAdminData(), token);
    }

    @Test
    void testImportForbidden() throws Exception {
        String documentBinaryUrl = "http://dm-store:8888/documents/12131212";
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withStaffImportFile(documentBinaryUrl)
                .buildAsCCDRequest();

        String token = "invalid-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/staff/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(staffImportService, never()).importStaff(ccdRequest.getCaseDetails().getAdminData(), token);
    }

    @Test
    void testImportBadRequest() throws Exception {
        mockMvc.perform(post("/admin/staff/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "some-token")
                        .content("error"))
                .andExpect(status().isBadRequest());
        verify(staffImportService, never()).importStaff(isA(AdminData.class), isA(String.class));
    }

    @Test
    void testImportServerError() throws Exception {
        String documentBinaryUrl = "http://dm-store:8888/documents/12131212";
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withStaffImportFile(documentBinaryUrl)
                .buildAsCCDRequest();

        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);
        doThrow(new IOException()).when(staffImportService).importStaff(ccdRequest.getCaseDetails().getAdminData(),
                token);
        mockMvc.perform(post("/admin/staff/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isInternalServerError());
        verify(staffImportService, times(1)).importStaff(ccdRequest.getCaseDetails().getAdminData(), token);
    }
}
