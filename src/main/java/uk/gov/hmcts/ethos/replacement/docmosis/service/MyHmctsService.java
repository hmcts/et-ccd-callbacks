package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationAddress;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Objects;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.ERROR_ORGANISATION_DETAILS_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyHmctsService {

    private final AuthTokenGenerator authTokenGenerator;
    private final OrganisationClient organisationClient;
    private final UserIdamService userIdamService;
    private final AdminUserService adminUserService;

    /**
     * Retrieves the organisation address of the currently authenticated user from the organisation service.
     *
     * <p>This method performs the following steps:
     * <ol>
     *   <li>Retrieves user details from the IDAM service using the provided {@code userToken}.</li>
     *   <li>Calls the organisation API client to retrieve organisation details based on the user's UID.</li>
     *   <li>Validates the response and extracts the first available contact information (organisation address).</li>
     *   <li>If the response or its relevant parts are missing or empty, throws a {@link GenericServiceException}.</li>
     * </ol>
     *
     * @param userToken the authentication token of the user, used to fetch user and organisation details
     * @return the first {@link OrganisationAddress} found in the organisation's contact information
     * @throws GenericServiceException if the organisation details or contact information cannot be retrieved
     */
    public OrganisationAddress getOrganisationAddress(String userToken) throws GenericServiceException {

        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        ResponseEntity<OrganisationsResponse> response =
                organisationClient.retrieveOrganisationDetailsByUserId(adminUserService.getAdminUserToken(),
                        authTokenGenerator.generate(),
                        userDetails.getUid());
        if (ObjectUtils.isEmpty(response)
                || ObjectUtils.isEmpty(response.getBody())
                || CollectionUtils.isEmpty(Objects.requireNonNull(response.getBody()).getContactInformation())
                || ObjectUtils.isEmpty(response.getBody().getContactInformation().getFirst())) {
            throw new GenericServiceException(ERROR_ORGANISATION_DETAILS_NOT_FOUND,
                    new Exception(ERROR_ORGANISATION_DETAILS_NOT_FOUND),
                    ERROR_ORGANISATION_DETAILS_NOT_FOUND,
                    StringUtils.EMPTY,
                    "MyHmctsService",
                    "getOrganisationAddress - organisation details not found");
        }
        return response.getBody().getContactInformation().getFirst();
    }
}
