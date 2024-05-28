package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.MultipleReferenceService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService.NOC_REQUEST;

@Slf4j
@Service
public class CcdCaseAssignment {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String LEGAL_REP_ID_NOT_FOUND_ERROR =
            "Add Respondent Representative to Multiple failed. Legal Rep Id not found for case {}";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final FeatureToggleService featureToggleService;
    private final AdminUserService adminUserService;
    private final NocCcdService nocCcdService;
    private final MultipleReferenceService multipleReferenceService;

    private final String aacUrl;
    private final String applyNocAssignmentsApiPath;

    public CcdCaseAssignment(RestTemplate restTemplate,
                             AuthTokenGenerator serviceAuthTokenGenerator,
                             FeatureToggleService featureToggleService,
                             AdminUserService adminUserService,
                             NocCcdService nocCcdService,
                             MultipleReferenceService multipleReferenceService,
                             @Value("${assign_case_access_api_url}") String aacUrl,
                             @Value("${apply_noc_access_api_assignments_path}") String applyNocAssignmentsApiPath
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.featureToggleService = featureToggleService;
        this.adminUserService = adminUserService;
        this.nocCcdService = nocCcdService;
        this.multipleReferenceService = multipleReferenceService;
        this.aacUrl = aacUrl;
        this.applyNocAssignmentsApiPath = applyNocAssignmentsApiPath;
    }

    public CCDCallbackResponse applyNoc(final CallbackRequest callback, String userToken) throws IOException {
        requireNonNull(callback, "callback must not be null");

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();

        HttpEntity<CallbackRequest> requestEntity =
                new HttpEntity<>(
                        callback,
                        createHeaders(serviceAuthorizationToken, userToken)
                );

        ResponseEntity<CCDCallbackResponse> response;
        try {
            response = restTemplate
                    .exchange(
                            aacUrl + applyNocAssignmentsApiPath,
                            HttpMethod.POST,
                            requestEntity,
                            CCDCallbackResponse.class
                    );

        } catch (RestClientResponseException exception) {
            log.info("Error form ccd - {}", exception.getMessage());
            throw exception;
        }

        log.info("Apply NoC. Http status received from AAC API; {} for case {}",
                response.getStatusCodeValue(), callback.getCaseDetails().getCaseId());

        if (featureToggleService.isMul2Enabled()) {
            addRespondentRepresentativeToMultiple(callback.getCaseDetails());
        }
        return response.getBody();
    }

    public CCDCallbackResponse applyNocAsAdmin(CallbackRequest callbackRequest) throws IOException {
        return this.applyNoc(callbackRequest, adminUserService.getAdminUserToken());
    }

    private HttpHeaders createHeaders(String serviceAuthorizationToken, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);
        return headers;
    }

    private void addRespondentRepresentativeToMultiple(CaseDetails caseDetails) throws IOException {
        if (!YES.equals(caseDetails.getCaseData().getMultipleFlag())) {
            return;
        }

        String adminUserToken = adminUserService.getAdminUserToken();
        String caseId = caseDetails.getCaseId();
        String userToAddId = getEventTriggerUserId(adminUserToken, caseId);

        if (userToAddId.isEmpty()) {
            log.info(LEGAL_REP_ID_NOT_FOUND_ERROR, caseId);
            return;
        }

        multipleReferenceService.addLegalRepToMultiple(caseDetails, userToAddId);
    }

    private String getEventTriggerUserId(String adminUserToken, String caseId) throws IOException {
        Optional<AuditEvent> auditEvent = nocCcdService.getLatestAuditEventByName(adminUserToken, caseId, NOC_REQUEST);

        if (auditEvent.isPresent()) {
            return auditEvent.get().getUserId();
        }
        return "";
    }
}
