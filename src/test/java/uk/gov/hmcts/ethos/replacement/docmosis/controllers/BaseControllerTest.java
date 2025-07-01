package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.DocmosisApplication;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BulkActionsController.class)
@ContextConfiguration(classes = DocmosisApplication.class)
public class BaseControllerTest {
    public static final String AUTH_TOKEN = "Bearer eyJhbGJbpjciOiJIUzI1NiJ9";

    @MockitoBean
    public VerifyTokenService verifyTokenService;

    @BeforeEach
    protected void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(verifyTokenService.verifyTokenSignature(AUTH_TOKEN)).thenReturn(true);
    }
}