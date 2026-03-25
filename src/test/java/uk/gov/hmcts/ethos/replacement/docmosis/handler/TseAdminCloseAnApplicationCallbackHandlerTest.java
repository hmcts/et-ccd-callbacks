package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.admin.TseAdmCloseService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TseAdminCloseAnApplicationCallbackHandlerTest {

    @Mock
    private VerifyTokenService verifyTokenService;
    @Mock
    private TseAdmCloseService tseAdmCloseService;

    private TseAdminCloseAnApplicationCallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new TseAdminCloseAnApplicationCallbackHandler(
            new CaseDetailsConverter(new ObjectMapper()),
            verifyTokenService,
            tseAdmCloseService
        );
    }

    @Test
    void aboutToSubmitShouldReturnForbiddenWhenTokenInvalid() {
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.aboutToSubmit(caseDetails());

        verifyNoInteractions(tseAdmCloseService);
    }

    @Test
    void aboutToSubmitShouldDelegateWhenTokenValid() {
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        handler.aboutToSubmit(caseDetails());

        verify(tseAdmCloseService).aboutToSubmitCloseApplication(any());
    }

    @Test
    void submittedShouldReturnForbiddenWhenTokenInvalid() {
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        handler.submitted(caseDetails());

        verifyNoInteractions(tseAdmCloseService);
    }

    @Test
    void submittedShouldBuildConfirmationBodyWhenTokenValid() {
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        CCDCallbackResponse response = (CCDCallbackResponse) handler.submitted(caseDetails());

        assertThat(response.getConfirmationBody()).contains("123");
    }

    private CaseDetails caseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales")
            .state("Open")
            .data(new HashMap<>())
            .build();
    }
}

