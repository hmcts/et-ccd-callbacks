package uk.gov.hmcts.ethos.replacement.docmosis.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.tika.utils.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse.SuperUser;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_CHECK_REPRESENTATIVE_ACCOUNT_BY_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL_LOG;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_UNABLE_TO_FIND_ORGANISATION_BY_USER_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_UNABLE_TO_FIND_ORGANISATION_SUPER_USER;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganisationService {

    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;
    private final OrganisationClient organisationClient;

    /**
     * Checks whether a representative account can be found using the supplied email address.
     *
     * <p>This method calls the organisation service to look up an account by email. If no
     * user identifier is found in the response, a warning message is added to the returned
     * list for the named representative.
     *
     * <p>If the lookup fails for any reason, the same warning message is returned. This
     * means the method treats both "account not found" and lookup errors as the same
     * warning condition.
     *
     * <p><strong>Assumptions:</strong>
     * <ul>
     *   <li>{@code representativeName} is not {@code null} or blank and is suitable for
     *       inclusion in a user-facing warning message.</li>
     *   <li>{@code email} is not {@code null} or blank and represents the email address
     *       to be checked.</li>
     *   <li>{@code adminUserService.getAdminUserToken()} returns a valid admin access token.</li>
     *   <li>{@code authTokenGenerator.generate()} returns a valid service authentication token.</li>
     *   <li>{@code organisationClient.getAccountIdByEmail(...)} may either return a response
     *       without a user identifier or throw an exception when the account cannot be resolved.</li>
     *   <li>{@code OrganisationUtils.hasUserIdentifier(...)} safely handles the returned
     *       {@link ResponseEntity}.</li>
     *   <li>An empty returned list means the representative account was successfully found
     *       by email.</li>
     *   <li>A non-empty returned list contains a warning indicating that the representative
     *       account could not be confirmed by email.</li>
     * </ul>
     *
     * @param representativeName the representative name used in the warning message if no
     *     account can be found
     * @param email the email address used to look up the representative account
     * @return a list of warning messages, or an empty list if the representative account
     *     is found successfully
     */
    public List<String> checkRepresentativeAccountByEmail(String representativeName, String email) {
        List<String> nocWarnings = new ArrayList<>();
        try {
            ResponseEntity<AccountIdByEmailResponse> userResponse =
                    organisationClient.getAccountIdByEmail(adminUserService.getAdminUserToken(),
                            authTokenGenerator.generate(), email);
            // checking if representative email address exists in organisation users
            if (!OrganisationUtils.hasUserIdentifier(userResponse)) {
                String warningMessage = String.format(WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL,
                        representativeName);
                nocWarnings.add(warningMessage);
            }
        } catch (FeignException e) {
            if (e.status() == HttpStatus.NOT_FOUND.value()) {
                String warningMessage = String.format(WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL,
                        representativeName);
                nocWarnings.add(warningMessage);
            } else {
                log.error(ERROR_UNABLE_TO_CHECK_REPRESENTATIVE_ACCOUNT_BY_EMAIL, e.getMessage());
                throw new GenericRuntimeException(e);
            }
        }
        if (CollectionUtils.isNotEmpty(nocWarnings)) {
            log.warn(WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL_LOG);
        }
        return nocWarnings;
    }

    /**
     * Finds and returns organisation details for the given IDAM user ID.
     *
     * <p>This method retrieves organisation details by calling the organisation service using
     * an admin user token and a generated service authentication token. If the response contains
     * a valid organisation identifier, the organisation details are returned. If no valid
     * organisation is found, or if the organisation service call fails with a {@link FeignException},
     * the method logs a warning and returns {@code null}.</p>
     *
     * <p>Assumptions:</p>
     * <ul>
     *     <li>The provided {@code userIdamId} is a valid IDAM user identifier.</li>
     *     <li>The admin user token returned by {@code adminUserService} is valid.</li>
     *     <li>The generated service authentication token is valid for calling the organisation service.</li>
     *     <li>A valid organisation response must contain an organisation identifier.</li>
     *     <li>If the organisation cannot be found or the response is invalid, returning {@code null} is acceptable.
     *     </li>
     * </ul>
     *
     * @param userIdamId the IDAM user ID used to retrieve organisation details
     * @return the organisation details if found and valid; otherwise {@code null}
     */
    public OrganisationsResponse findOrganisationByIdamUserId(String userIdamId) {
        try {
            ResponseEntity<OrganisationsResponse> organisationRepsonse =
                    organisationClient.retrieveOrganisationDetailsByUserId(adminUserService.getAdminUserToken(),
                            authTokenGenerator.generate(), userIdamId);
            if (OrganisationUtils.hasOrganisationIdentifier(organisationRepsonse)) {
                return organisationRepsonse.getBody();
            }
        } catch (FeignException e) {
            log.warn(WARNING_UNABLE_TO_FIND_ORGANISATION_BY_USER_ID, e.getMessage());
        }
        return null;
    }

    /** Finds the superuser for the organisation with the given organisation ID.
     * Finds the superuser for the organisation with the given organisation ID.
     *
     * <p>Returns {@code null} if the organisation ID is blank, the organisation
     * cannot be found, or the organisation response does not contain a superuser
     * email address.</p>
     *
     * <p>If the organisation lookup fails with a {@link FeignException}, the
     * exception is handled and {@code null} is returned. Non-404 errors are logged.</p>
     *
     * @param orgId the ID of the organisation to retrieve the superuser for
     * @return the organisation's {@link SuperUser}, or {@code null} if no superuser
     *         can be found
     */
    public SuperUser findSuperUserByOrganisationId(String orgId) {
        if (StringUtils.isBlank(orgId)) {
            return null;
        }
        ResponseEntity<RetrieveOrgByIdResponse> organisationResponse;
        try {
            organisationResponse = organisationClient.getOrganisationById(adminUserService.getAdminUserToken(),
                    authTokenGenerator.generate(), orgId);
            if (!OrganisationUtils.hasOrganisationSuperuserEmail(organisationResponse)) {
                return null;
            }
        } catch (FeignException e) {
            if (e.status() != HttpStatus.NOT_FOUND.value()) {
                log.error(WARNING_UNABLE_TO_FIND_ORGANISATION_SUPER_USER, e.getMessage());
            }
            return null;
        }
        assert organisationResponse.getBody() != null;
        return organisationResponse.getBody().getSuperUser();
    }
}
