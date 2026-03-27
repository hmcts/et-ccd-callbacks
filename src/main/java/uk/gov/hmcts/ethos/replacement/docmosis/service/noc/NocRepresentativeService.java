package uk.gov.hmcts.ethos.replacement.docmosis.service.noc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.OrganisationUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES;

@Service
@RequiredArgsConstructor
@Slf4j
public class NocRepresentativeService {

    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;
    private final NocClaimantRepresentativeService nocClaimantRepresentativeService;
    private final NocService nocService;
    private final AdminUserService adminUserService;

    public CaseData updateRepresentation(CaseDetails caseDetails, String userToken) throws IOException {
        CaseData caseData = caseDetails.getCaseData();
        ChangeOrganisationRequest change = validateChangeRequest(caseData);
        DynamicFixedListType caseRoleId = change.getCaseRoleId();

        if (caseRoleId.getValue().getCode().equals(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())) {
            caseData = nocClaimantRepresentativeService.updateClaimantRepresentation(caseDetails, userToken);
            caseDetails.setCaseData(caseData);
            nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(
                    caseDetails);
        } else {
            caseData = nocRespondentRepresentativeService.updateRespondentRepresentation(caseDetails);
            caseData = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, userToken);
            caseDetails.setCaseData(caseData);
            caseData = nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails);
        }
        return caseData;
    }

    private ChangeOrganisationRequest validateChangeRequest(CaseData caseData) {
        ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();
        if (Objects.isNull(change)
                || Objects.isNull(change.getCaseRoleId())
                || Objects.isNull(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }
        return change;
    }

    /**
     * Validates that each eligible representative's selected respondent organisation matches
     * the organisation associated with that representative's user account.
     *
     * <p><strong>Assumptions:</strong>
     * <ul>
     *   <li>{@code caseDetails}, {@code caseDetails.getCaseData()}, and the representative entries
     *       in {@code repCollection} are non-null.</li>
     *   <li>{@code RespondentRepresentativeUtils.isValidRepresentative(...)} safely determines
     *       whether a representative has the minimum required data for validation.</li>
     *   <li>A representative should only be organisation-validated when they have a non-blank
     *       email address, {@code myHmctsYesNo} is {@code YES}, and a respondent organisation
     *       has been selected.</li>
     *   <li>If a user cannot be found in IDAM or their organisation cannot be retrieved,
     *       the method treats that representative as not valid for organisation comparison
     *       and does not add a validation error.</li>
     *   <li>{@code OrganisationUtils.hasMatchingOrganisationId(...)} performs the authoritative
     *       comparison between the selected organisation and the organisation returned for the user.</li>
     * </ul>
     *
     * <p>The validation is performed only for representatives that:
     * <ul>
     *   <li>are considered valid representatives,</li>
     *   <li>have a non-blank representative email address,</li>
     *   <li>have MyHMCTS enabled, and</li>
     *   <li>have a respondent organisation present.</li>
     * </ul>
     *
     * <p>For each such representative, the method looks up the user by email address and then
     * retrieves the organisation linked to that user. If the selected respondent organisation
     * does not match the organisation returned for the user, a validation error is added.
     *
     * <p>If the representative cannot be found or their organisation cannot be retrieved
     * (for example, due to a {@code GenericServiceException}), organisation matching is skipped
     * for that representative and no error is added.
     *
     * @param caseDetails the case details containing representative information to validate
     * @return a list of validation error messages; empty if there are no representatives to
     *     validate or no organisation mismatches are found
     */
    public List<String> validateRepresentativesOrganisation(CaseDetails caseDetails) {
        List<String> errors = new ArrayList<>();
        if (CollectionUtils.isEmpty(caseDetails.getCaseData().getRepCollection())) {
            return errors;
        }
        for (RepresentedTypeRItem representative : caseDetails.getCaseData().getRepCollection()) {
            if (RespondentRepresentativeUtils.isValidRepresentative(representative)
                    && StringUtils.isNotBlank(representative.getValue().getRepresentativeEmailAddress())
                    && YES.equals(representative.getValue().getMyHmctsYesNo())
                    && ObjectUtils.isNotEmpty(representative.getValue().getRespondentOrganisation())) {
                AccountIdByEmailResponse userResponse;
                OrganisationsResponse organisationsResponse = null;
                boolean isValidUserAndOrganisation = true;
                try {
                    String accessToken = adminUserService.getAdminUserToken();
                    userResponse = nocService.findUserByEmail(accessToken,
                            representative.getValue().getRepresentativeEmailAddress(), caseDetails.getCaseId());
                    organisationsResponse = nocService.findOrganisationByUserId(accessToken,
                            userResponse.getUserIdentifier(), caseDetails.getCaseId());
                } catch (GenericServiceException e) {
                    // if user is not defined on idam should not check for organisation.
                    isValidUserAndOrganisation = false;
                }
                if (isValidUserAndOrganisation
                        && !OrganisationUtils.hasMatchingOrganisationId(
                                representative.getValue().getRespondentOrganisation(), organisationsResponse)) {
                    errors.add(String.format(ERROR_SELECTED_ORGANISATION_REPRESENTATIVE_ORGANISATION_NOT_MATCHES,
                            representative.getValue().getNameOfRepresentative(),
                            representative.getValue().getRespondentOrganisation().getOrganisationID()));
                }
            }
        }
        return errors;
    }
}
