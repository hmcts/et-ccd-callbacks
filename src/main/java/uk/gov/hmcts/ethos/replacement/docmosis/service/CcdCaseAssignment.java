package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.collect.Maps;
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
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.exceptions.CaseCreationException;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.*;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesReadingService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService.NOC_REQUEST;

@Slf4j
@Service
public class CcdCaseAssignment {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String ADD_USER_ERROR = "Call to add legal rep to Multiple Case failed for %s";
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final String aacUrl;
    private final String applyNocAssignmentsApiPath;
    private final AdminUserService adminUserService;
    private final FeatureToggleService featureToggleService;
    private final CcdClient ccdClient;
    private final NocCcdService nocCcdService;
    private final MultipleCasesReadingService multipleCasesReadingService;

    public CcdCaseAssignment(RestTemplate restTemplate,
                             AuthTokenGenerator serviceAuthTokenGenerator,
                             AdminUserService adminUserService,
                             FeatureToggleService featureToggleService,
                             CcdClient ccdClient,
                             NocCcdService nocCcdService,
                             MultipleCasesReadingService multipleCasesReadingService,
                             @Value("${assign_case_access_api_url}") String aacUrl,
                             @Value("${apply_noc_access_api_assignments_path}") String applyNocAssignmentsApiPath
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.adminUserService = adminUserService;
        this.featureToggleService = featureToggleService;
        this.ccdClient = ccdClient;
        this.aacUrl = aacUrl;
        this.applyNocAssignmentsApiPath = applyNocAssignmentsApiPath;
        this.nocCcdService = nocCcdService;
        this.multipleCasesReadingService = multipleCasesReadingService;
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

        if (featureToggleService.isMultiplesEnabled()) {
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
        String accessToken = adminUserService.getAdminUserToken();
        String jurisdiction = caseDetails.getJurisdiction();
        String caseType = caseDetails.getCaseTypeId();
        String caseId = caseDetails.getCaseId();
        String userToAddId = getEventTriggerUserId(accessToken, caseId);

        if (!userToAddId.isEmpty() && YES.equals(caseDetails.getCaseData().getMultipleFlag())) {
            List<SubmitMultipleEvent> submitMultipleEvents = multipleCasesReadingService.retrieveMultipleCases(
                    accessToken,
                    caseType,
                    caseDetails.getCaseData().getMultipleReference());
               if (!submitMultipleEvents.isEmpty()) {
                   String multipleId = String.valueOf(submitMultipleEvents.get(0).getCaseId());
                   addUserToCase(accessToken, jurisdiction, caseType, multipleId, userToAddId);
            }

        }
    }

    private void addUserToCase(String accessToken,
                               String jurisdiction,
                               String caseType,
                               String multipleId,
                               String userToAddId) throws IOException {
        Map<String, String> payload = Maps.newHashMap();
        payload.put("id", userToAddId);
        String errorMessage = String.format(ADD_USER_ERROR, multipleId);

        try {
            ResponseEntity<Object> response =
                    ccdClient.addUserToMultiple(
                            accessToken,
                            jurisdiction,
                            caseType,
                            multipleId,
                            payload);

            if (response == null) {
                throw new CaseCreationException(errorMessage);
            }
            log.info("Http status received from CCD addUserToMultiple API; {}", response.getStatusCodeValue());
        } catch (RestClientResponseException e) {
            throw new CaseCreationException(String.format("%s with %s", errorMessage, e.getMessage()));
        }
    }

    private String getEventTriggerUserId(String accessToken, String caseId) throws IOException {
        Optional<AuditEvent> auditEvent = nocCcdService.getLatestAuditEventByName(accessToken, caseId, NOC_REQUEST);
        if (auditEvent.isPresent()) {
            return auditEvent.get().getUserId();
        }
        return "";
    }
}
