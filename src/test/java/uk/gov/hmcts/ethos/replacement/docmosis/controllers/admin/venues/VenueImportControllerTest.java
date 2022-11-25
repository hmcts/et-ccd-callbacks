package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.venues;

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
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.venues.VenueImportService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({VenueImportController.class, JsonMapper.class})
class VenueImportControllerTest {
    @MockBean
    private VerifyTokenService verifyTokenService;

    @MockBean
    private VenueImportService venueImportService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void testInitImportSuccess() throws Exception {
        String documentBinaryUrl = "http://dm-store:8888/documents/12131212";
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withVenueImport(TribunalOffice.LEEDS, documentBinaryUrl)
                .buildAsCCDRequest();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/admin/venue/initImport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));

        verify(venueImportService, times(1)).initImport(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testInitImportForbidden() throws Exception {
        String documentBinaryUrl = "http://dm-store:8888/documents/12131212";
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withVenueImport(TribunalOffice.LEEDS, documentBinaryUrl)
                .buildAsCCDRequest();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/venue/initImport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(venueImportService, never()).initImport(ccdRequest.getCaseDetails().getAdminData());
    }

    @Test
    void testInitImportBadRequest() throws Exception {
        mockMvc.perform(post("/admin/venue/initImport")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "user-token")
                        .content("bad-request"))
                .andExpect(status().isBadRequest());

        verify(verifyTokenService, never()).verifyTokenSignature(anyString());
        verify(venueImportService, never()).initImport(any(AdminData.class));
    }

    @Test
    void testImportSuccess() throws Exception {
        String documentBinaryUrl = "http://dm-store:8888/documents/12131212";
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withVenueImport(TribunalOffice.LEEDS, documentBinaryUrl)
                .buildAsCCDRequest();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(true);

        mockMvc.perform(post("/admin/venue/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", nullValue()))
                .andExpect(jsonPath("$.warnings", nullValue()));

        verify(venueImportService, times(1)).importVenues(ccdRequest.getCaseDetails().getAdminData(), token);
    }

    @Test
    void testImportForbidden() throws Exception {
        String documentBinaryUrl = "http://dm-store:8888/documents/12131212";
        CCDRequest ccdRequest = AdminDataBuilder
                .builder()
                .withVenueImport(TribunalOffice.LEEDS, documentBinaryUrl)
                .buildAsCCDRequest();
        String token = "some-token";
        when(verifyTokenService.verifyTokenSignature(token)).thenReturn(false);

        mockMvc.perform(post("/admin/venue/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", token)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());

        verify(venueImportService, never()).importVenues(ccdRequest.getCaseDetails().getAdminData(), token);
    }

    @Test
    void testImportBadRequest() throws Exception {
        mockMvc.perform(post("/admin/venue/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "user-token")
                        .content("bad-request"))
                .andExpect(status().isBadRequest());

        verify(verifyTokenService, never()).verifyTokenSignature(anyString());
        verify(venueImportService, never()).importVenues(any(AdminData.class), anyString());
    }
}
