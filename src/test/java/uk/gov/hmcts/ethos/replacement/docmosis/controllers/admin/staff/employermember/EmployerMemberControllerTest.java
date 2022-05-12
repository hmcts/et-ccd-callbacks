package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.staff.employermember;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.employermember.EmployerMemberService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.AdminDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import java.util.ArrayList;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({EmployerMemberController.class, JsonMapper.class})
class EmployerMemberControllerTest {

    private static final String AUTH_TOKEN = "token-eyJhbGJbpjciOiJIUzI1NiJ9";
    private static final String ADD_EMPLOYER_MEMBER_URL = "/admin/staff/employermember/addEmployerMember";
    private CCDRequest ccdRequest;

    @MockBean
    private VerifyTokenService verifyTokenService;
    @MockBean
    private EmployerMemberService employerMemberService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        ccdRequest = AdminDataBuilder
                .builder()
                .withEmployerMember(TribunalOffice.LEEDS.getOfficeName(), "TestCode", "TestName")
                .buildAsCCDRequest();
    }

    @Test
    void addEmployerMember_Success() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        when(employerMemberService.addEmployerMember(any())).thenReturn(new ArrayList<>());

        mockMvc.perform(post(ADD_EMPLOYER_MEMBER_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(0)))
                .andExpect(jsonPath("$.warnings", nullValue()));
        verify(employerMemberService, times(1)).addEmployerMember(ccdRequest.getCaseDetails().getAdminData());

    }

    @Test
    void addEmployerMember_invalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mockMvc.perform(post(ADD_EMPLOYER_MEMBER_URL)
                        .contentType(APPLICATION_JSON)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
                        .content(jsonMapper.toJson(ccdRequest)))
                .andExpect(status().isForbidden());
        verify(employerMemberService, never()).addEmployerMember(ccdRequest.getCaseDetails().getAdminData());
    }

}