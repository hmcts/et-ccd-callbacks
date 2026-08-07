package uk.gov.hmcts.ethos.replacement.docmosis.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimantEmailService {

    // Shown when the user enters a new email that is the same as the email already on the case.
    public static final String EMAIL_UNCHANGED_ERROR =
            "Enter an email address that is different from the current claimant email address.";
    // Shown when no login account exists for the new email (claimant must already have registered).
    public static final String IDAM_USER_NOT_FOUND_ERROR =
            "No user account was found for the new email address. The claimant must register an "
                    + "account before the email address can be updated.";
    // Shown when more than one login account matches the new email (data issue; cannot safely continue).
    public static final String IDAM_USER_AMBIGUOUS_ERROR =
            "More than one user account was found for the new email address. Check the email address "
                    + "with the claimant before trying again.";
    // Shown when a login account exists for the new email, but it is not a citizen (claimant) account.
    public static final String IDAM_USER_NOT_CITIZEN_ERROR =
            "The new email address is linked to an account that is not a citizen account. "
                    + "Enter a different email address.";
    // Shown when IdAM user search fails (for example 403 when the system token lacks search-user scope).
    public static final String IDAM_USER_LOOKUP_ERROR =
            "The new email address could not be checked against user accounts. Try again later.";
    private static final String CITIZEN_ROLE = "citizen";
    // Shown when the system cannot check who currently has claimant access to the case (temporary system issue).
    public static final String ACCESS_LOOKUP_ERROR =
            "The claimant's current case access could not be checked. Try again later.";
    // Shown when removing access from the old email fails; the case email is left unchanged.
    public static final String ACCESS_REVOKE_ERROR =
            "Case access could not be removed from the previous claimant email address. "
                    + "The claimant email address was not updated. Enter the email address again.";
    // Shown when giving case access to the new email fails; the case email is left unchanged.
    public static final String ACCESS_GRANT_ERROR =
            "Case access could not be given to the new claimant email address. "
                    + "The claimant email address was not updated. Enter the email address again.";
    // Shown when access was moved from the old email to the new one, but saving the new email on the case then failed.
    // Case access already points at the new user — retrying the same email should complete the email save.
    public static final String EMAIL_UPDATE_AFTER_REASSIGN_ERROR =
            "Case access was moved to the new email address, but the claimant email address could not "
                    + "be updated. Enter the email address again.";
    // Shown when access was newly given to the new email (no previous claimant access),
    // but saving the email then failed.
    // Case access already points at the new user — retrying the same email should complete the email save.
    public static final String EMAIL_UPDATE_AFTER_GRANT_ERROR =
            "Case access was given to the new email address, but the claimant email address could not "
                    + "be updated. Enter the email address again.";
    // Shown when saving the new email fails, but case access did not need to change (same user already had access).
    public static final String EMAIL_UPDATE_ERROR =
            "The claimant email address could not be updated. Enter the email address again.";

    private final IdamApi idamApi;
    private final AdminUserService adminUserService;
    private final CcdCaseAssignment ccdCaseAssignment;

    /**
     * Updates claimant contact email and grants or reassigns [CREATOR] case access for both
     * LiP and represented claimants. Solicitor roles are not changed.
     */
    public List<String> initialise(CaseData caseData) {
        caseData.setNewClaimantEmail(null);
        caseData.setCurrentClaimantEmail(caseData.getClaimantType() == null
                ? null
                : caseData.getClaimantType().getClaimantEmailAddress());
        return List.of();
    }

    public List<String> validateNewEmail(CaseData caseData) {
        List<String> errors = validateEmailInput(caseData);
        if (errors.isEmpty()) {
            findCitizenIdamUserByEmail(caseData.getNewClaimantEmail(), errors);
        }
        return errors;
    }

    public List<String> prepareUpdate(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        List<String> errors = validateEmailInput(caseData);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }

        Optional<UserDetails> newUser = findCitizenIdamUserByEmail(caseData.getNewClaimantEmail(), errors);
        if (newUser.isEmpty()) {
            return errors;
        }

        AccessOutcome accessOutcome;
        try {
            accessOutcome = ensureCreatorAccess(caseDetails, newUser.get());
        } catch (CcdInputOutputException exception) {
            log.error("Unable to update creator access for case {}", caseDetails.getCaseId(), exception);
            errors.add(exception.getMessage());
            return errors;
        }

        try {
            applyEmailUpdate(caseData, caseData.getNewClaimantEmail());
        } catch (RuntimeException exception) {
            log.error("Creator access outcome {} but email could not be updated for case {}",
                    accessOutcome, caseDetails.getCaseId(), exception);
            errors.add(emailUpdateFailureMessage(accessOutcome));
        }
        return errors;
    }

    private AccessOutcome ensureCreatorAccess(CaseDetails caseDetails, UserDetails newUser) {
        Optional<CaseUserAssignment> oldCreator;
        try {
            oldCreator = getCreatorAssignment(caseDetails.getCaseId());
        } catch (IOException exception) {
            throw new CcdInputOutputException(ACCESS_LOOKUP_ERROR, exception);
        }

        String newUserId = newUser.getUid();
        if (oldCreator.isEmpty()) {
            grantCreatorAccess(caseDetails.getCaseId(), newUserId);
            caseDetails.getCaseData().setClaimantId(newUserId);
            log.info("Granted creator access to user {} for case {}", newUserId, caseDetails.getCaseId());
            return AccessOutcome.GRANTED;
        }

        String oldUserId = oldCreator.get().getUserId();
        if (StringUtils.equals(oldUserId, newUserId)) {
            caseDetails.getCaseData().setClaimantId(newUserId);
            return AccessOutcome.UNCHANGED;
        }

        reassignCreatorAccess(caseDetails.getCaseId(), oldUserId, newUserId);
        caseDetails.getCaseData().setClaimantId(newUserId);
        return AccessOutcome.REASSIGNED;
    }

    private void reassignCreatorAccess(String caseId, String oldUserId, String newUserId) {
        CaseAssignmentUserRolesRequest oldAccess = buildCreatorRequest(caseId, oldUserId);
        CaseAssignmentUserRolesRequest newAccess = buildCreatorRequest(caseId, newUserId);
        try {
            ccdCaseAssignment.removeCaseUserRole(oldAccess);
        } catch (Exception revokeException) {
            throw new CcdInputOutputException(ACCESS_REVOKE_ERROR, revokeException);
        }
        try {
            ccdCaseAssignment.addCaseUserRole(newAccess);
        } catch (Exception grantException) {
            try {
                ccdCaseAssignment.addCaseUserRole(oldAccess);
            } catch (Exception restoreException) {
                grantException.addSuppressed(restoreException);
                log.error("Failed to restore creator access for case {}", caseId, restoreException);
            }
            throw new CcdInputOutputException(ACCESS_GRANT_ERROR, grantException);
        }
    }

    private void grantCreatorAccess(String caseId, String userId) {
        try {
            ccdCaseAssignment.addCaseUserRole(buildCreatorRequest(caseId, userId));
        } catch (Exception grantException) {
            throw new CcdInputOutputException(ACCESS_GRANT_ERROR, grantException);
        }
    }

    private void applyEmailUpdate(CaseData caseData, String newEmail) {
        if (caseData.getClaimantType() == null) {
            caseData.setClaimantType(new ClaimantType());
        }
        caseData.getClaimantType().setClaimantEmailAddress(newEmail);
        caseData.setCurrentClaimantEmail(null);
        caseData.setNewClaimantEmail(null);
    }

    private static String emailUpdateFailureMessage(AccessOutcome accessOutcome) {
        return switch (accessOutcome) {
            case REASSIGNED -> EMAIL_UPDATE_AFTER_REASSIGN_ERROR;
            case GRANTED -> EMAIL_UPDATE_AFTER_GRANT_ERROR;
            case UNCHANGED -> EMAIL_UPDATE_ERROR;
        };
    }

    private List<String> validateEmailInput(CaseData caseData) {
        List<String> errors = new ArrayList<>(ReferralHelper.validateEmail(caseData.getNewClaimantEmail()));
        if (errors.isEmpty()) {
            String existingEmail = caseData.getClaimantType() == null
                    ? null
                    : caseData.getClaimantType().getClaimantEmailAddress();
            if (StringUtils.equalsIgnoreCase(existingEmail, caseData.getNewClaimantEmail())) {
                errors.add(EMAIL_UNCHANGED_ERROR);
            }
        }
        return errors;
    }

    private Optional<UserDetails> findCitizenIdamUserByEmail(String email, List<String> errors) {
        List<UserDetails> exactMatches;
        try {
            exactMatches = idamApi.searchUsersByQuery(
                            adminUserService.getAdminUserToken(), email, 0, 50)
                    .stream()
                    .filter(user -> StringUtils.equalsIgnoreCase(email, user.getEmail()))
                    .toList();
        } catch (FeignException exception) {
            log.error("Unable to search IdAM users while validating claimant email update", exception);
            errors.add(IDAM_USER_LOOKUP_ERROR);
            return Optional.empty();
        }
        if (exactMatches.isEmpty()) {
            errors.add(IDAM_USER_NOT_FOUND_ERROR);
            return Optional.empty();
        }
        if (exactMatches.size() > 1) {
            errors.add(IDAM_USER_AMBIGUOUS_ERROR);
            return Optional.empty();
        }
        UserDetails user = exactMatches.getFirst();
        if (CollectionUtils.isEmpty(user.getRoles()) || !user.getRoles().contains(CITIZEN_ROLE)) {
            errors.add(IDAM_USER_NOT_CITIZEN_ERROR);
            return Optional.empty();
        }
        return Optional.of(user);
    }

    private Optional<CaseUserAssignment> getCreatorAssignment(String caseId) throws IOException {
        CaseUserAssignmentData assignments = ccdCaseAssignment.getCaseUserRoles(caseId);
        if (assignments == null || assignments.getCaseUserAssignments() == null) {
            return Optional.empty();
        }
        return assignments.getCaseUserAssignments().stream()
                .filter(assignment -> CASE_USER_ROLE_CREATOR.equals(assignment.getCaseRole()))
                .findFirst();
    }

    private CaseAssignmentUserRolesRequest buildCreatorRequest(String caseId, String userId) {
        CaseAssignmentUserRole role = CaseAssignmentUserRole.builder()
                .caseDataId(caseId)
                .userId(userId)
                .caseRole(CASE_USER_ROLE_CREATOR)
                .build();
        return CaseAssignmentUserRolesRequest.builder()
                .caseAssignmentUserRoles(List.of(role))
                .build();
    }

    enum AccessOutcome {
        UNCHANGED,
        REASSIGNED,
        GRANTED
    }
}
