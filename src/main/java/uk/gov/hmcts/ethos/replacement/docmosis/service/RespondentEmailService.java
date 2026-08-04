package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentEmailUpdateHelper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;

/**
 * Updates respondent contact email and grants or reassigns [DEFENDANT] case access for both
 * LiP and represented respondents. Solicitor roles are not changed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentEmailService {

    // Shown when the case has no eligible respondents for this event.
    public static final String NO_RESPONDENTS_ERROR = RespondentEmailUpdateHelper.NO_RESPONDENTS_ERROR;
    // Shown when the user continues without selecting a respondent.
    public static final String RESPONDENT_REQUIRED_ERROR = RespondentEmailUpdateHelper.RESPONDENT_REQUIRED_ERROR;
    // Shown when the user enters a new email that is the same as the email already on the case.
    public static final String EMAIL_UNCHANGED_ERROR = RespondentEmailUpdateHelper.EMAIL_UNCHANGED_ERROR;
    // Shown when no login account exists for the new email (respondent must already have registered).
    public static final String IDAM_USER_NOT_FOUND_ERROR =
            "No user account was found for the new email address. The respondent must register an "
                    + "account before the email address can be updated.";
    // Shown when more than one login account matches the new email (data issue; cannot safely continue).
    public static final String IDAM_USER_AMBIGUOUS_ERROR =
            "More than one user account was found for the new email address. Check the email address "
                    + "with the respondent before trying again.";
    // Shown when a login account exists for the new email, but it is not a citizen (respondent portal) account.
    public static final String IDAM_USER_NOT_CITIZEN_ERROR =
            "The new email address is linked to an account that is not a citizen account. "
                    + "Enter a different email address.";
    private static final String CITIZEN_ROLE = "citizen";
    // Shown when the system cannot check who currently has respondent access to the case.
    public static final String ACCESS_LOOKUP_ERROR =
            "The respondent's current case access could not be checked. Try again later.";
    // Shown when removing access from the old email fails; the case email is left unchanged.
    public static final String ACCESS_REVOKE_ERROR =
            "Case access could not be removed from the previous respondent email address. "
                    + "The respondent email address was not updated. Enter the email address again.";
    // Shown when giving case access to the new email fails; the case email is left unchanged.
    public static final String ACCESS_GRANT_ERROR =
            "Case access could not be given to the new respondent email address. "
                    + "The respondent email address was not updated. Enter the email address again.";
    // Shown when access was moved from the old email to the new one, but saving the new email then failed.
    // Case access already points at the new user — retrying the same email should complete the email save.
    public static final String EMAIL_UPDATE_AFTER_REASSIGN_ERROR =
            "Case access was moved to the new email address, but the respondent email address could not "
                    + "be updated. Enter the email address again.";
    // Shown when access was newly given to the new email (no previous respondent access),
    // but saving the email then failed.
    // Case access already points at the new user — retrying the same email should complete the email save.
    public static final String EMAIL_UPDATE_AFTER_GRANT_ERROR =
            "Case access was given to the new email address, but the respondent email address could not "
                    + "be updated. Enter the email address again.";
    // Shown when saving the new email fails, but case access did not need to change (same user already had access).
    public static final String EMAIL_UPDATE_ERROR =
            "The respondent email address could not be updated. Enter the email address again.";

    private final IdamApi idamApi;
    private final AdminUserService adminUserService;
    private final CcdCaseAssignment ccdCaseAssignment;

    public List<String> initialise(CaseData caseData) {
        return RespondentEmailUpdateHelper.initialise(caseData);
    }

    public List<String> populateCurrentEmail(CaseData caseData) {
        return RespondentEmailUpdateHelper.populateCurrentEmail(caseData);
    }

    public List<String> validateNewEmail(CaseData caseData) {
        List<String> errors = RespondentEmailUpdateHelper.validateInput(caseData);
        if (errors.isEmpty()) {
            findUserByEmail(caseData.getNewRespondentEmail(), errors);
        }
        return errors;
    }

    public List<String> prepareUpdate(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        List<String> errors = RespondentEmailUpdateHelper.validateInput(caseData);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }

        Optional<RespondentSumTypeItem> selectedRespondent =
                RespondentEmailUpdateHelper.getSelectedEligibleRespondent(caseData);
        if (selectedRespondent.isEmpty()) {
            return List.of(RespondentEmailUpdateHelper.getSelectionError(caseData));
        }

        Optional<UserDetails> newUser = findUserByEmail(caseData.getNewRespondentEmail(), errors);
        if (CollectionUtils.isNotEmpty(errors) || newUser.isEmpty()) {
            return errors;
        }

        RespondentSumTypeItem respondentItem = selectedRespondent.get();
        AccessOutcome accessOutcome;
        try {
            accessOutcome = ensureDefendantAccess(caseDetails.getCaseId(), respondentItem, newUser.get());
        } catch (CcdInputOutputException exception) {
            log.error("Unable to update defendant access for case {}", caseDetails.getCaseId(), exception);
            errors.add(exception.getMessage());
            return errors;
        }

        try {
            RespondentEmailUpdateHelper.applyEmailUpdate(
                    caseData, respondentItem.getValue(), caseData.getNewRespondentEmail());
        } catch (RuntimeException exception) {
            log.error("Defendant access outcome {} but email could not be updated for case {}",
                    accessOutcome, caseDetails.getCaseId(), exception);
            errors.add(emailUpdateFailureMessage(accessOutcome));
        }
        return errors;
    }

    private AccessOutcome ensureDefendantAccess(String caseId,
                                                RespondentSumTypeItem respondentItem,
                                                UserDetails newUser) {
        RespondentSumType respondent = respondentItem.getValue();
        String previousIdamId = respondent.getIdamId();
        String newUserId = newUser.getUid();

        Optional<CaseUserAssignment> oldDefendant;
        try {
            oldDefendant = getDefendantAssignment(caseId, previousIdamId);
        } catch (IOException exception) {
            throw new CcdInputOutputException(ACCESS_LOOKUP_ERROR, exception);
        }

        if (oldDefendant.isEmpty()) {
            grantDefendantAccess(caseId, newUserId);
            respondent.setIdamId(newUserId);
            log.info("Granted defendant access to user {} for respondent {} on case {}",
                    newUserId, respondentItem.getId(), caseId);
            return AccessOutcome.GRANTED;
        }

        String oldUserId = oldDefendant.get().getUserId();
        if (StringUtils.equals(oldUserId, newUserId)) {
            respondent.setIdamId(newUserId);
            return AccessOutcome.UNCHANGED;
        }

        reassignDefendantAccess(caseId, oldUserId, newUserId);
        respondent.setIdamId(newUserId);
        return AccessOutcome.REASSIGNED;
    }

    private void reassignDefendantAccess(String caseId, String oldUserId, String newUserId) {
        CaseAssignmentUserRolesRequest oldAccess = buildDefendantRequest(caseId, oldUserId);
        CaseAssignmentUserRolesRequest newAccess = buildDefendantRequest(caseId, newUserId);
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
                log.error("Failed to restore defendant access for case {}", caseId, restoreException);
            }
            throw new CcdInputOutputException(ACCESS_GRANT_ERROR, grantException);
        }
    }

    private void grantDefendantAccess(String caseId, String userId) {
        try {
            ccdCaseAssignment.addCaseUserRole(buildDefendantRequest(caseId, userId));
        } catch (Exception grantException) {
            throw new CcdInputOutputException(ACCESS_GRANT_ERROR, grantException);
        }
    }

    private static String emailUpdateFailureMessage(AccessOutcome accessOutcome) {
        return switch (accessOutcome) {
            case REASSIGNED -> EMAIL_UPDATE_AFTER_REASSIGN_ERROR;
            case GRANTED -> EMAIL_UPDATE_AFTER_GRANT_ERROR;
            case UNCHANGED -> EMAIL_UPDATE_ERROR;
        };
    }

    private Optional<UserDetails> findUserByEmail(String email, List<String> errors) {
        List<UserDetails> exactMatches = idamApi.searchUsersByQuery(
                        adminUserService.getAdminUserToken(), email, 0, 50)
                .stream()
                .filter(user -> StringUtils.equalsIgnoreCase(email, user.getEmail()))
                .toList();
        if (exactMatches.isEmpty()) {
            errors.add(IDAM_USER_NOT_FOUND_ERROR);
            return Optional.empty();
        }
        if (exactMatches.size() > 1) {
            errors.add(IDAM_USER_AMBIGUOUS_ERROR);
            return Optional.empty();
        }
        return requireCitizenAccount(exactMatches.getFirst(), errors);
    }

    private Optional<UserDetails> requireCitizenAccount(UserDetails user, List<String> errors) {
        if (CollectionUtils.isEmpty(user.getRoles()) || !user.getRoles().contains(CITIZEN_ROLE)) {
            errors.add(IDAM_USER_NOT_CITIZEN_ERROR);
            return Optional.empty();
        }
        return Optional.of(user);
    }

    private Optional<CaseUserAssignment> getDefendantAssignment(String caseId, String userId) throws IOException {
        if (StringUtils.isBlank(userId)) {
            return Optional.empty();
        }
        CaseUserAssignmentData assignments = ccdCaseAssignment.getCaseUserRoles(caseId);
        if (assignments == null || assignments.getCaseUserAssignments() == null) {
            return Optional.empty();
        }
        return assignments.getCaseUserAssignments().stream()
                .filter(assignment -> CASE_USER_ROLE_DEFENDANT.equals(assignment.getCaseRole()))
                .filter(assignment -> userId.equals(assignment.getUserId()))
                .findFirst();
    }

    private CaseAssignmentUserRolesRequest buildDefendantRequest(String caseId, String userId) {
        CaseAssignmentUserRole role = CaseAssignmentUserRole.builder()
                .caseDataId(caseId)
                .userId(userId)
                .caseRole(CASE_USER_ROLE_DEFENDANT)
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
