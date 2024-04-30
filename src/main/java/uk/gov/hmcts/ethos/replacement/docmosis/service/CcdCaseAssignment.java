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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService.NOC_REQUEST;

@Slf4j
@Service
public class CcdCaseAssignment {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String UPDATE_USER_ERROR = "Call to Supplementary Data API failed for %s";
    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final String aacUrl;
    private final String applyNocAssignmentsApiPath;
    private final AdminUserService adminUserService;
    private final FeatureToggleService featureToggleService;
    private final UserIdamService userIdamService;
    private final CcdClient ccdClient;
    private final NocCcdService nocCcdService;

    public CcdCaseAssignment(RestTemplate restTemplate,
                             AuthTokenGenerator serviceAuthTokenGenerator,
                             AdminUserService adminUserService,
                             FeatureToggleService featureToggleService,
                             UserIdamService userIdamService,
                             CcdClient ccdClient,
                             NocCcdService nocCcdService,
                             @Value("${assign_case_access_api_url}") String aacUrl,
                             @Value("${apply_noc_access_api_assignments_path}") String applyNocAssignmentsApiPath
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.adminUserService = adminUserService;
        this.featureToggleService = featureToggleService;
        this.userIdamService = userIdamService;
        this.ccdClient = ccdClient;
        this.aacUrl = aacUrl;
        this.applyNocAssignmentsApiPath = applyNocAssignmentsApiPath;
        this.nocCcdService = nocCcdService;
    }

    public CCDCallbackResponse applyNoc(
        final CallbackRequest callback, String userToken) throws IOException {
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
//            if (featureToggleService.isMultiplesEnabled() && caseData.getMultipleFlag().equals(YES)) {
            addRespondentsToMultiple(userToken, callback.getCaseDetails());
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

    public void addUserToCase(
            String userToken, String jurisdiction, String caseType, String caseId) throws IOException {

        String uid = null;
        String accessToken = adminUserService.getAdminUserToken();
        Optional<AuditEvent> auditEvent =
                nocCcdService.getLatestAuditEventByName(accessToken, caseId, NOC_REQUEST);
//        String uid = userIdamService.getUserDetails(userToken).getUid();
        if (auditEvent.isPresent()) {
            uid = auditEvent.get().getUserId();}

        Map<String, String> payload = Maps.newHashMap();
        payload.put("id", uid);
        String errorMessage = String.format(UPDATE_USER_ERROR, caseId);

        try {
            ResponseEntity<Object> response =
                    ccdClient.addUserToMultiple(
                            accessToken,
                            payload,
                            caseType,
                            jurisdiction,
                            caseId);

            if (response == null) {
                throw new CaseCreationException(errorMessage);
            }
            log.info("Http status received from CCD supplementary update API; {}", response.getStatusCodeValue());
        } catch (RestClientResponseException e) {
            throw new CaseCreationException(String.format("%s with %s", errorMessage, e.getMessage()));
        }
    }

    public void addRespondentsToMultiple(String userToken, CaseDetails caseDetails) throws IOException {
        String jurisdiction = caseDetails.getJurisdiction();
        String caseId = caseDetails.getCaseId();
        String caseType = caseDetails.getCaseTypeId();
//      String multipleId = multipleDetails.getCaseId();
        addUserToCase(userToken, jurisdiction, caseType, caseId);

    }
}
