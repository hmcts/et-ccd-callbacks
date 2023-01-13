package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CcdCaseAssignmentTest {
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @InjectMocks
    private CcdCaseAssignment ccdCaseAssignment;

    private CallbackRequest callbackRequest;

    @BeforeEach
    void setUp() {
        CaseData caseData = new CaseData();
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("600/11");
        callbackRequest = new CallbackRequest();
        callbackRequest.setCaseDetails(caseDetails);
    }

    @Test
    void shouldCallCaseAssignmentNoc() {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());

        when(restTemplate
            .exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(CCDCallbackResponse.class))).thenReturn(ResponseEntity.ok(expected));
        when(serviceAuthTokenGenerator.generate()).thenReturn("token");

        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
    }
}