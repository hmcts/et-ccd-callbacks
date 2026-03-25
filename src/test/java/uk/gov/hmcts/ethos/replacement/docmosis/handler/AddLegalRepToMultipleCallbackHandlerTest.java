package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddLegalRepToMultipleCallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private MultipleReferenceService multipleReferenceService;
    @Mock
    private UserIdamService userService;

    private AddLegalRepToMultipleCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AddLegalRepToMultipleCallbackHandler(caseDetailsConverter, multipleReferenceService, userService);
    }

    @Test
    void aboutToSubmitShouldAddLegalRepToMultiple() throws IOException {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("12345/2026");
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails = stubConverter(caseData);

        UserDetails userDetails = new UserDetails();
        userDetails.setUid("user-id-1");
        when(userService.getUserDetails(null)).thenReturn(userDetails);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.aboutToSubmit(callbackCaseDetails());

        verify(multipleReferenceService).addLegalRepToMultiple(ccdCaseDetails, "user-id-1");
        assertThat(response.getData()).isNotNull();
    }

    @Test
    void aboutToSubmitShouldWrapIOExceptionFromService() throws IOException {
        CaseData caseData = new CaseData();
        stubConverter(caseData);

        UserDetails userDetails = new UserDetails();
        userDetails.setUid("user-id-1");
        when(userService.getUserDetails(null)).thenReturn(userDetails);

        doThrow(new IOException("ccd write failed"))
            .when(multipleReferenceService)
            .addLegalRepToMultiple(any(), eq("user-id-1"));

        assertThatThrownBy(() -> handler.aboutToSubmit(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to add legal representative to multiple")
            .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void submittedShouldReturnConfirmationHeader() {
        CaseData caseData = new CaseData();
        caseData.setEthosCaseReference("12345/2026");
        stubConverter(caseData);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(callbackCaseDetails());

        assertThat(response.getConfirmationHeader())
            .contains("You have been added to the Multiple for the case: 12345/2026");
    }

    private uk.gov.hmcts.et.common.model.ccd.CaseDetails stubConverter(CaseData caseData) {
        uk.gov.hmcts.et.common.model.ccd.CaseDetails ccdCaseDetails =
            new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        ccdCaseDetails.setCaseData(caseData);
        ccdCaseDetails.setCaseTypeId("ET_EnglandWales");
        ccdCaseDetails.setCaseId("123");
        when(caseDetailsConverter.convert(any(CaseDetails.class))).thenReturn(ccdCaseDetails);
        return ccdCaseDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .build();
    }
}
