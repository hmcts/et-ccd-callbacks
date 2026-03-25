package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateReferralMultiplesCreateReferralCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private ReferralService referralService;
    @Mock
    private UserIdamService userIdamService;
    @Mock
    private DocumentManagementService documentManagementService;
    @Mock
    private CaseLookupService caseLookupService;

    private CreateReferralMultiplesCreateReferralCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateReferralMultiplesCreateReferralCallbackHandler(
            caseDetailsConverter,
            referralService,
            userIdamService,
            documentManagementService,
            caseLookupService
        );
        lenient().when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
    }

    @Test
    void aboutToSubmitShouldCreateReferralForMultipleAndSendEmail() throws IOException {
        MultipleData multipleData = new MultipleData();
        multipleData.setReferralSubject("Case management");
        final MultipleDetails multipleDetails = stubMultipleConverter(multipleData, "999");

        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("Admin");
        userDetails.setLastName("User");
        userDetails.setName("Admin User");
        when(userIdamService.getUserDetails(null)).thenReturn(userDetails);

        CaseData leadCase = new CaseData();
        leadCase.setEthosCaseReference("11111/2026");
        when(caseLookupService.getLeadCaseFromMultipleAsAdmin(multipleDetails)).thenReturn(leadCase);
        when(referralService.generateDocument(multipleData, leadCase, null, "ET_EnglandWales_Multiple"))
            .thenReturn(DocumentInfo.builder().build());
        when(documentManagementService.addDocumentToDocumentField(any()))
            .thenReturn(new UploadedDocumentType());

        handler.aboutToSubmit(callbackCaseDetails());

        assertThat(multipleData.getReferralCollection()).hasSize(1);
        verify(referralService).sendEmail(multipleDetails, leadCase, "1", true, "Admin User");
    }

    @Test
    void aboutToSubmitShouldWrapIOException() throws IOException {
        MultipleData multipleData = new MultipleData();
        final MultipleDetails multipleDetails = stubMultipleConverter(multipleData, "999");

        UserDetails userDetails = new UserDetails();
        userDetails.setFirstName("Admin");
        userDetails.setLastName("User");
        when(userIdamService.getUserDetails(null)).thenReturn(userDetails);

        when(caseLookupService.getLeadCaseFromMultipleAsAdmin(multipleDetails))
            .thenThrow(new IOException("lookup failed"));

        assertThatThrownBy(() -> handler.aboutToSubmit(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to create referral for multiple")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void submittedShouldReturnReferralConfirmationBody() {
        MultipleData multipleData = new MultipleData();
        stubMultipleConverter(multipleData, "999");

        uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse response =
            (uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse) handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationBody()).contains("/cases/case-details/999#Referrals");
    }

    private MultipleDetails stubMultipleConverter(MultipleData multipleData, String caseId) {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId("ET_EnglandWales_Multiple");
        multipleDetails.setCaseId(caseId);
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class))).thenReturn(
            multipleDetails);
        return multipleDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(999L)
            .caseTypeId("ET_EnglandWales_Multiple")
            .state("Open")
            .build();
    }
}
