package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class ReferralServiceTest {
    private ReferralService referralService;

    @MockBean
    private EmailService emailService;
    @MockBean
    private UserService userService;
    @MockBean
    private TornadoService tornadoService;

    private DocumentInfo documentInfo;

    @BeforeEach
    void setUp() {
        referralService = new ReferralService(emailService, userService, tornadoService);

        documentInfo = DocumentInfo.builder()
            .description("Referral Summary.pdf")
            .url("https://test.com/documents/random-uuid")
            .build();
    }

    @Test
    void whenCreatingReferral_ReturnDocument() throws IOException {
        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
            anyString(), anyString())).thenReturn(documentInfo);

        DocumentInfo responseDoc = referralService.generateCRDocument(new CaseData(), "", "");
        assertThat(responseDoc, is(documentInfo));
    }

    @Test
    void whenCreatingReferralFails_ThrowException() throws IOException {
        Throwable ioException = new IOException("test");

        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
            anyString(), anyString())).thenThrow(ioException);
        assertThrows(DocumentManagementException.class,
            () -> referralService.generateCRDocument(new CaseData(), "",
            ""));
    }
}