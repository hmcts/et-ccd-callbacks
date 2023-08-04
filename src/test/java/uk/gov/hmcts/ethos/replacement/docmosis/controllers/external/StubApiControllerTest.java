package uk.gov.hmcts.ethos.replacement.docmosis.controllers.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.JsonMapper;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest({StubApiController.class, JsonMapper.class})
@ContextConfiguration(classes = DocmosisApplication.class)
class StubApiControllerTest {

    private static final String AUTH_TOKEN = "some-token";
    private static final String STUB_API = "/stubApi";
    @Autowired
    private WebApplicationContext applicationContext;
    @MockBean
    private VerifyTokenService verifyTokenService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
    }

    @Test
    void stubApiSuccess() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
        mvc.perform(get(STUB_API)
                    .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("Oh wow, it actually works!"));
    }

    @Test
    void stubApiInvalidToken() throws Exception {
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(false);
        mvc.perform(get(STUB_API)
                        .header(HttpHeaders.AUTHORIZATION, AUTH_TOKEN))
                .andExpect(status().isOk())
                .andExpect(content().string("Invalid token"));
    }
}
