package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersIdamUser;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersResponse;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.NocUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.Collections;
import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_ORGANISATION_USERS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_INVALID_REPRESENTATIVE_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_REPRESENTATIVE_EMAIL_DOES_NOT_MATCH_ORGANISATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_REPRESENTATIVE_MISSING_EMAIL_ADDRESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_REPRESENTATIVE_ORGANISATION_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class RepresentativeService {

    private final OrganisationClient organisationClient;
    private final AdminUserService adminUserService;
    private final AuthTokenGenerator authTokenGenerator;

    /**
     * Validates that each NOC representative in the given {@link CaseData} has a valid email address
     * and that the email belongs to a user within the representative's associated organisation.
     *
     * <p>The method performs the following checks for each representative:
     * <ul>
     *     <li>Representative data is valid according to {@code NocUtils#isValidNocRepresentative}.</li>
     *     <li>If the representative is marked as a MyHMCTS user, an email address must be present.</li>
     *     <li>The representative must have a valid respondent organisation with a non-blank organisation ID.</li>
     *     <li>The organisation must contain at least one user.</li>
     *     <li>The representative's email must match one of the organisation's registered users.</li>
     * </ul>
     *
     * <p>If any validation fails, the method returns a list containing a single error message
     * describing the issue. If all representatives are valid, an empty list is returned.
     *
     * @param caseData the case data containing representatives to validate
     * @return a list of validation error messages, or an empty list if all representatives are valid
     */
    public List<String> validateRepresentativeEmailMatchesOrganisationUsers(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData) || CollectionUtils.isEmpty(caseData.getRepCollection())) {
            return Collections.emptyList();
        }
        for (RepresentedTypeRItem representativeItem :  caseData.getRepCollection()) {
            if (!NocUtils.isValidNocRepresentative(representativeItem)) {
                return List.of(ERROR_INVALID_REPRESENTATIVE_EXISTS);
            }
            RepresentedTypeR representative = representativeItem.getValue();
            if (!YES.equals(representative.getMyHmctsYesNo())) {
                continue;
            }
            final String representativeEmail = representative.getRepresentativeEmailAddress();
            if (StringUtils.isBlank(representativeEmail)) {
                return List.of(String.format(ERROR_REPRESENTATIVE_MISSING_EMAIL_ADDRESS, representativeItem.getId()));
            }
            if (ObjectUtils.isEmpty(representative.getRespondentOrganisation())
                    || StringUtils.isBlank(representative.getRespondentOrganisation().getOrganisationID())) {
                return List.of(String.format(ERROR_REPRESENTATIVE_ORGANISATION_NOT_FOUND,  representativeItem.getId()));
            }
            final String organisationId = representative.getRespondentOrganisation().getOrganisationID();
            final ResponseEntity<OrganisationUsersResponse> organisationUsersResponse =
                    organisationClient.getOrganisationUsers(adminUserService.getAdminUserToken(),
                            authTokenGenerator.generate(), organisationId);

            if (ObjectUtils.isEmpty(organisationUsersResponse)
                    || ObjectUtils.isEmpty(organisationUsersResponse.getBody())
                    || CollectionUtils.isEmpty(organisationUsersResponse.getBody().getUsers())) {
                return List.of(String.format(ERROR_ORGANISATION_USERS_NOT_FOUND, organisationId));
            }
            final List<OrganisationUsersIdamUser> organisationUsers = organisationUsersResponse.getBody().getUsers();
            boolean representativeExistsInOrganisation = organisationUsers.stream()
                    .anyMatch(user -> representativeEmail.equals(user.getEmail()));
            if (!representativeExistsInOrganisation) {
                return List.of(String.format(
                        ERROR_REPRESENTATIVE_EMAIL_DOES_NOT_MATCH_ORGANISATION, representativeEmail));
            }
        }
        return Collections.emptyList();
    }
}
