package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class ReferralServiceTest {
    private ReferralService referralService;

    @MockBean
    private EmailService emailService;
    @MockBean
    private UserIdamService userIdamService;
    @MockBean
    private TornadoService tornadoService;
    @MockBean
    private CaseLookupService caseLookupService;

    private DocumentInfo documentInfo;

    @BeforeEach
    void setUp() {
        referralService = new ReferralService(emailService, userIdamService, tornadoService, caseLookupService);

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

    @Test
    void populateHearingDetailsFromLeadCase_returnHtml() throws IOException {
        String caseId = "123";
        MultipleData multipleData = MultipleData.builder().leadCaseId(caseId).build();
        when(caseLookupService.getCaseDataAsAdmin(anyString(), anyString())).thenReturn(new CaseData());
        String response = referralService.populateHearingDetailsFromLeadCase(multipleData, ENGLANDWALES_CASE_TYPE_ID);
        
        assertThat(response, is(""));
        verify(caseLookupService, times(1)).getCaseDataAsAdmin(ENGLANDWALES_CASE_TYPE_ID, caseId);
    }

    @Test
    void populateHearingDetailsFromLeadCase_noLeadCaseId() throws IOException {
        MultipleData multipleData = MultipleData.builder().leadCaseId(null).build();
        when(caseLookupService.getCaseDataAsAdmin(anyString(), anyString())).thenReturn(new CaseData());
        
        assertThrows(IllegalArgumentException.class, () -> 
            referralService.populateHearingDetailsFromLeadCase(multipleData, ENGLANDWALES_CASE_TYPE_ID)
        );
    }
}