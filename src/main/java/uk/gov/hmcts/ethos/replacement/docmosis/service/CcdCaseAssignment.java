package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesResponse;
import uk.gov.hmcts.et.common.model.ccd.AuditEvent;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.SubCaseLegalRepDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleCaseSearchResult;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.SubmitMultipleEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCasesSendingService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MAX_ES_SIZE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService.NOC_REQUEST;

@Slf4j
@Service
public class CcdCaseAssignment {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String ADD_USER_ERROR = "Call to add legal rep to Multiple Case failed for %s";
    private static final String SEARCH_CASES_FORMAT = "%s/searchCases?ctid=%s";
    private static final String MULTIPLE_CASE_REFERENCE_KEYWORD = "data.multipleReference.keyword";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final AdminUserService adminUserService;
    private final FeatureToggleService featureToggleService;
    private final CcdClient ccdClient;
    private final NocCcdService nocCcdService;

    private final String aacUrl;
    private final String applyNocAssignmentsApiPath;
    private final String ccdDataStoreUrl;
    private final MultipleCasesSendingService multipleCasesSendingService;

    @SuppressWarnings({"PMD.ExcessiveParameterList"})
    public CcdCaseAssignment(RestTemplate restTemplate,
                             AuthTokenGenerator serviceAuthTokenGenerator,
                             AdminUserService adminUserService,
                             FeatureToggleService featureToggleService,
                             CcdClient ccdClient,
                             NocCcdService nocCcdService,
                             MultipleCasesSendingService multipleCasesSendingService,
                             @Value("${assign_case_access_api_url}") String aacUrl,
                             @Value("${apply_noc_access_api_assignments_path}") String applyNocAssignmentsApiPath,
                             @Value("${ccd.data-store-api-url}") String ccdDataStoreUrl) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.adminUserService = adminUserService;
        this.featureToggleService = featureToggleService;
        this.ccdClient = ccdClient;
        this.nocCcdService = nocCcdService;
        this.multipleCasesSendingService = multipleCasesSendingService;
        this.aacUrl = aacUrl;
        this.applyNocAssignmentsApiPath = applyNocAssignmentsApiPath;
        this.ccdDataStoreUrl = ccdDataStoreUrl;
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
            log.info("Error from ccd - {}", exception.getMessage());
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
        if (!YES.equals(caseDetails.getCaseData().getMultipleFlag())) {
            return;
        }

        String adminUserToken = adminUserService.getAdminUserToken();
        String caseId = caseDetails.getCaseId();
        String userToAddId = getEventTriggerUserId(adminUserToken, caseId);

        if (userToAddId.isEmpty()) {
            log.info("Add Respondent Representative to Multiple failed. Legal Rep Id not found for case {}", caseId);
            return;
        }

        String caseType = MultiplesHelper.appendMultipleSuffix(caseDetails.getCaseTypeId());
        String multipleRef = caseDetails.getCaseData().getMultipleReference();
        SubmitMultipleEvent multiShell = getMultipleByReference(adminUserToken, caseType, multipleRef);

        if (String.valueOf(multiShell.getCaseId()).isBlank()) {
            log.info("Add Respondent Representative to Multiple failed. "
                    + "Multiple Id not found for case {}, with MultipleReference {}", caseId, multipleRef);
            return;
        }

        String multipleId = String.valueOf(multiShell.getCaseId());
        String jurisdiction = caseDetails.getJurisdiction();

        addUserToCase(adminUserToken, jurisdiction, caseType, multipleId, userToAddId);

        String caseRef = caseDetails.getCaseData().getEthosCaseReference();
        MultipleData multipleShell = multiShell.getCaseData();
        if (multipleShell == null) {
            log.info("MultipleData is null for case {}", caseId);
            return;
        }
        updateMultipleLegalRepCollection(
                adminUserToken, caseType, jurisdiction, multipleShell, multipleId, caseRef, userToAddId);
    }

    private void updateMultipleLegalRepCollection(
            String userToken,
            String caseTypeId,
            String jurisdiction,
            MultipleData multiDataToUpdate,
            String multipleId,
            String caseRef,
            String legalRepId) {

        ListTypeItem<SubCaseLegalRepDetails> legalRepCollection = multiDataToUpdate.getLegalRepCollection();
        if (legalRepCollection == null) {
            legalRepCollection = new ListTypeItem<>();
            multiDataToUpdate.setLegalRepCollection(legalRepCollection);
        }

        boolean caseExists = false;
        for (GenericTypeItem<SubCaseLegalRepDetails> details : legalRepCollection) {
            if (details.getValue().getCaseReference().equals(caseRef)) {
                caseExists = true;
                if (details.getValue().getLegalRepIds().stream().noneMatch(
                        item -> legalRepId.equals(item.getValue()))) {
                    GenericTypeItem<String> legalRepList = GenericTypeItem.from(legalRepId);
                    details.getValue().getLegalRepIds().add(legalRepList);
                }
                break;
            }
        }

        if (!caseExists) {
            ListTypeItem<String> newLegalRepList = ListTypeItem.from(legalRepId);
            GenericTypeItem<SubCaseLegalRepDetails> newDetails =
                    GenericTypeItem.from(new SubCaseLegalRepDetails(caseRef, newLegalRepList));
            legalRepCollection.add(newDetails);
            log.warn("legalRepCollection: {}", legalRepCollection);
        }

        multiDataToUpdate.setLegalRepCollection(legalRepCollection);
        multipleCasesSendingService.sendUpdateToMultiple(userToken, caseTypeId, jurisdiction,
                multiDataToUpdate, multipleId);
    }

    private String getEventTriggerUserId(String adminUserToken, String caseId) throws IOException {
        Optional<AuditEvent> auditEvent = nocCcdService.getLatestAuditEventByName(adminUserToken, caseId, NOC_REQUEST);

        if (auditEvent.isPresent()) {
            return auditEvent.get().getUserId();
        }
        return "";
    }

    public SubmitMultipleEvent getMultipleByReference(String adminUserToken,
                                                        String caseType,
                                                        String multipleReference) {
        String getUrl = String.format(SEARCH_CASES_FORMAT, ccdDataStoreUrl, caseType);
        String requestBody = buildQueryForGetMultipleByReference(multipleReference);

        HttpEntity<String> request =
                new HttpEntity<>(
                        requestBody,
                        createHeaders(serviceAuthTokenGenerator.generate(), adminUserToken)
                );

        ResponseEntity<MultipleCaseSearchResult> response;

        try {
            response = restTemplate
                    .exchange(
                            getUrl,
                            HttpMethod.POST,
                            request,
                            MultipleCaseSearchResult.class
                    );
        } catch (RestClientResponseException exception) {
            log.error("Error from ccd - {}", exception.getMessage());
            throw exception;
        }

        MultipleCaseSearchResult resultBody = response.getBody();

        if (resultBody != null && CollectionUtils.isNotEmpty(resultBody.getCases())) {
            return resultBody.getCases().get(0);
        }

        return new SubmitMultipleEvent();
    }

    private String buildQueryForGetMultipleByReference(String multipleReference) {
        TermsQueryBuilder termsQueryBuilder = termsQuery(MULTIPLE_CASE_REFERENCE_KEYWORD, multipleReference);

        return new SearchSourceBuilder()
                .size(MAX_ES_SIZE)
                .query(termsQueryBuilder)
                .toString();
    }

    private void addUserToCase(String adminUserToken,
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
                            adminUserToken,
                            jurisdiction,
                            caseType,
                            multipleId,
                            payload);

            if (response == null) {
                throw new CaseCreationException(errorMessage);
            }

            log.info("Http status received from CCD addUserToMultiple API; {}", response.getStatusCodeValue());
        } catch (RestClientResponseException e) {
            throw (CaseCreationException)
                    new CaseCreationException(String.format("%s with %s", errorMessage, e.getMessage())).initCause(e);
        }
    }

    public void removeCaseUserRoles(CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest) {
        String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        String userToken = adminUserService.getAdminUserToken();
        HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserRolesRequest, createHeaders(serviceAuthorizationToken, userToken));
        ResponseEntity<CaseAssignmentUserRolesResponse> response;
        try {
            response = restTemplate.exchange(
                ccdDataStoreUrl + "/case-users",
                HttpMethod.DELETE,
                requestEntity,
                CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException exception) {
            log.info("Error from CCD - {}", exception.getMessage());
            throw exception;
        }

        log.info("Remove case user roles. Http status received from CCD API; {}",
            response.getStatusCodeValue());
    }

    public void addCaseUserRoles(CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest) {
        String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        String userToken = adminUserService.getAdminUserToken();
        HttpEntity<CaseAssignmentUserRolesRequest> requestEntity =
                new HttpEntity<>(caseAssignmentUserRolesRequest, createHeaders(serviceAuthorizationToken, userToken));
        ResponseEntity<CaseAssignmentUserRolesResponse> response;
        try {
            response = restTemplate.exchange(
                ccdDataStoreUrl + "/case-users",
                HttpMethod.POST,
                requestEntity,
                    CaseAssignmentUserRolesResponse.class);
        } catch (RestClientResponseException exception) {
            log.info("Error from CCD - {}", exception.getMessage());
            throw exception;
        }

        log.info("Add case user roles. Http status received from CCD API; {}",
            response.getStatusCodeValue());
    }

    public CaseAssignmentUserRolesRequest getCaseAssignmentRequest(Long caseId, String userId, String orgId,
                                                                   String role) {
        return CaseAssignmentUserRolesRequest.builder()
                .caseAssignmentUserRolesWithOrganisation(
                        List.of(getCaseAssignmentUserRole(caseId, orgId, role, userId))
                ).build();
    }

    private CaseAssignmentUserRoleWithOrganisation getCaseAssignmentUserRole(Long caseId, String orgId,
                                                                             String role, String userId) {
        return CaseAssignmentUserRoleWithOrganisation.builder()
                .organisationId(orgId)
                .caseDataId(String.valueOf(caseId))
                .caseRole(role)
                .userId(userId)
                .build();
    }
}