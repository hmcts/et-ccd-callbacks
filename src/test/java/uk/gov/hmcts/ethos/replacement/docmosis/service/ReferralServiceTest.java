package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.exceptions.DocumentManagementException;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.TornadoDocument;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class ReferralServiceTest {
    private ReferralService referralService;

    @MockitoBean
    private TornadoService tornadoService;
    @MockitoBean
    private CaseLookupService caseLookupService;
    @MockitoBean
    private EmailService emailService;

    private DocumentInfo documentInfo;

    @BeforeEach
    void setUp() {
        referralService = new ReferralService(tornadoService, caseLookupService, emailService);

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

    @Test
    void generateDocument_exception() throws IOException {
        MultipleData multipleData = MultipleData.builder().leadCaseId(null).build();
        when(tornadoService.generateDocument(anyString(), any(), anyString(), anyString()))
            .thenThrow(IOException.class);

        assertThrows(Exception.class, () ->
                referralService.generateDocument(multipleData, new CaseData(), "", ENGLANDWALES_BULK_CASE_TYPE_ID)
        );
    }

    @Test
    void generateDocument_success() throws IOException {
        MultipleData multipleData = MultipleData.builder().leadCaseId(null).build();
        when(tornadoService.generateDocument(anyString(), any(), anyString(), anyString()))
                .thenReturn(DocumentInfo.builder().build());

        ArgumentCaptor<TornadoDocument> doc = ArgumentCaptor.forClass(TornadoDocument.class);
        referralService.generateDocument(multipleData, new CaseData(), "", ENGLANDWALES_BULK_CASE_TYPE_ID);

        verify(tornadoService).generateDocument(any(), doc.capture(), any(), any());
        assertEquals("Referral Summary.pdf", doc.getValue().getOutputName());
    }

    @Test
    void sendEmail_NoEmail() {
        MultipleDetails multipleDetails = new MultipleDetails();
        MultipleData multipleData = MultipleData.builder().leadCaseId(null).build();
        multipleDetails.setCaseData(multipleData);

        referralService.sendEmail(multipleDetails, new CaseData(), "", true, ENGLANDWALES_BULK_CASE_TYPE_ID);
        verifyNoInteractions(emailService);
    }

    @Test
    void sendEmail_withEmail() {
        MultipleDetails multipleDetails = new MultipleDetails();
        MultipleData multipleData = MultipleData.builder().leadCaseId(null).build();
        multipleDetails.setCaseData(multipleData);
        multipleData.setReferentEmail("test@email.com");
        multipleData.setReferralSubject("ET1");
        multipleData.setMultipleReference("123");

        CaseData leadCase = CaseDataBuilder.builder().withRespondent("respondent", "", "", false).build();
        leadCase.setClaimant("claimant");

        when(emailService.getExuiCaseLink(any())).thenReturn("exui");

        referralService.sendEmail(multipleDetails, leadCase, "1", true, "Test Person");

        Map<String, String> expected = new ConcurrentHashMap<>();

        expected.put("date", "Not set");
        expected.put("respondents", "respondent");
        expected.put("linkToExUI", "exui");
        expected.put("emailFlag", "");
        expected.put("refNumber", "1");
        expected.put("caseNumber", "123");
        expected.put("subject", "ET1");
        expected.put("body", "You have a new referral on this case.");
        expected.put("claimant", "claimant");
        expected.put("replyReferral", "Referred by");
        expected.put("username", "Test Person");

        verify(emailService).sendEmail(any(), eq(multipleData.getReferentEmail()), eq(expected));
    }
}