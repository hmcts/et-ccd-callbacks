package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_CHECK_REPRESENTATIVE_ACCOUNT_BY_EMAIL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL;

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
    public List<String> checkRepresentativeAccountByEmail(String representativeName, String email)
            throws GenericRuntimeException {
        List<String> nocWarnings = new ArrayList<>();
        try {
            ResponseEntity<AccountIdByEmailResponse> userResponse =
                    organisationClient.getAccountIdByEmail(adminUserService.getAdminUserToken(),
                            authTokenGenerator.generate(), email);
            // checking if representative email address exists in organisation users
            if (!OrganisationUtils.hasUserIdentifier(userResponse)) {
                String warningMessage = String.format(WARNING_REPRESENTATIVE_ACCOUNT_NOT_FOUND_BY_EMAIL,
                        representativeName);
                log.warn(warningMessage);
                nocWarnings.add(warningMessage);
            }
        } catch (Exception e) {
            log.error(ERROR_UNABLE_TO_CHECK_REPRESENTATIVE_ACCOUNT_BY_EMAIL, e.getMessage());
            throw new GenericRuntimeException(e);
        }
        return nocWarnings;
    }
}
