package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static java.util.Objects.requireNonNull;

@Slf4j
@Service
public class CcdCaseAssignment {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;

    @Qualifier("xui")
    private final AuthTokenGenerator serviceAuthTokenGenerator;

    private final String ccdUrl;
    private final String aacUrl;
    private final String ccdAssignmentsApiPath;
    private final String aacAssignmentsApiPath;
    private final String applyNocAssignmentsApiPath;

    public CcdCaseAssignment(RestTemplate restTemplate,
                             AuthTokenGenerator serviceAuthTokenGenerator,
                             @Value("${core_case_data_api_assignments_url}") String ccdUrl,
                             @Value("${assign_case_access_api_url}") String aacUrl,
                             @Value("${core_case_data_api_assignments_path}") String ccdAssignmentsApiPath,
                             @Value("${assign_case_access_api_assignments_path}") String aacAssignmentsApiPath,
                             @Value("${apply_noc_access_api_assignments_path}") String applyNocAssignmentsApiPath
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.ccdUrl = ccdUrl;
        this.aacUrl = aacUrl;
        this.ccdAssignmentsApiPath = ccdAssignmentsApiPath;
        this.aacAssignmentsApiPath = aacAssignmentsApiPath;
        this.applyNocAssignmentsApiPath = applyNocAssignmentsApiPath;
    }

    public CCDCallbackResponse applyNoc(
        final CallbackRequest callback, String userToken
    ) throws CaseAssignmentException {
        requireNonNull(callback, "callback must not be null");

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();

        HttpEntity<CallbackRequest> requestEntity =
            new HttpEntity<>(
                callback,
                setHeaders(serviceAuthorizationToken, userToken)
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

        } catch (RestClientResponseException e) {
            log.info("Error form ccd - {}", e.getMessage());
            throw new  CaseAssignmentException("CCD error");
        }

        log.info("Apply NoC. Http status received from AAC API; {} for case {}",
            response.getStatusCodeValue(), callback.getCaseDetails().getCaseId());

        return response.getBody();
    }

    private HttpHeaders setHeaders(String serviceAuthorizationToken, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);
        return headers;
    }
}