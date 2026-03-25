package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AmendMultipleAPICallbackHandlerTest {

    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @Mock
    private VerifyTokenService verifyTokenService;

    private AmendMultipleAPICallbackHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AmendMultipleAPICallbackHandler(caseDetailsConverter, verifyTokenService);
    }

    @Test
    void aboutToSubmitShouldReturnEmptyResponseWhenTokenInvalid() {
        stubMultipleConverter(multipleDetails(new MultipleData()));
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(false);

        var response = handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getData()).isNull();
    }

    @Test
    void aboutToSubmitShouldReturnCaseDataWhenTokenValid() {
        MultipleData multipleData = new MultipleData();
        stubMultipleConverter(multipleDetails(multipleData));
        when(caseDetailsConverter.getObjectMapper()).thenReturn(new ObjectMapper());
        when(verifyTokenService.verifyTokenSignature(null)).thenReturn(true);

        var response = handler.aboutToSubmit(callbackCaseDetails());

        assertThat(response.getData()).isNotNull();
        verify(caseDetailsConverter).getObjectMapper();
    }

    @Test
    void submittedShouldThrowWhenCallbackTypeUnsupported() {
        assertThatThrownBy(() -> handler.submitted(callbackCaseDetails()))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("does not support submitted callbacks");
    }

    private void stubMultipleConverter(MultipleDetails multipleDetails) {
        when(caseDetailsConverter.convert(any(CaseDetails.class), eq(MultipleDetails.class)))
            .thenReturn(multipleDetails);
    }

    private MultipleDetails multipleDetails(MultipleData multipleData) {
        MultipleDetails multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(multipleData);
        multipleDetails.setCaseTypeId("ET_EnglandWales_Multiple");
        multipleDetails.setCaseId("123");
        multipleDetails.setState("Open");
        return multipleDetails;
    }

    private CaseDetails callbackCaseDetails() {
        return CaseDetails.builder()
            .id(123L)
            .caseTypeId("ET_EnglandWales_Multiple")
            .state("Open")
            .build();
    }
}
