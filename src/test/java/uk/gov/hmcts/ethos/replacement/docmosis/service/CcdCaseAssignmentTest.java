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
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesReadingService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

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
    private CallbackRequest callbackRequest;
    @Mock
    private AdminUserService adminUserService;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private CcdClient ccdClient;
    @Mock
    private NocCcdService nocCcdService;
    @Mock
    private MultipleCasesReadingService multipleCasesReadingService;
    @InjectMocks
    private CcdCaseAssignment ccdCaseAssignment;

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
    void shouldCallCaseAssignmentNoc() throws IOException {
        CCDCallbackResponse expected = new CCDCallbackResponse(callbackRequest.getCaseDetails().getCaseData());

        when(restTemplate
                .exchange(
                        anyString(),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(CCDCallbackResponse.class))).thenReturn(ResponseEntity.ok(expected));
        when(serviceAuthTokenGenerator.generate()).thenReturn("token");
        when(featureToggleService.isMultiplesEnabled()).thenReturn(true);
        when(adminUserService.getAdminUserToken()).thenReturn("adminToken");
        when(nocCcdService.getLatestAuditEventByName(any(), any(), any())).thenReturn(Optional.of(getAuditEvent()));
        CCDCallbackResponse actual = ccdCaseAssignment.applyNoc(callbackRequest, "token");

        assertThat(expected).isEqualTo(actual);
    }

    private AuditEvent getAuditEvent() {
        return AuditEvent.builder()
                .userId("128")
                .userFirstName("John")
                .userLastName("Smith")
                .id("nocRequest")
                .createdDate(LocalDateTime.of(2022, 9, 10, 8, 0, 0))
                .build();
    }
}
