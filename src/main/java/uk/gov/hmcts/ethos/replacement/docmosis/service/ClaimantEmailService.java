package uk.gov.hmcts.ethos.replacement.docmosis.service;

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

    static final String EMAIL_UNCHANGED_ERROR = "Enter an email address that is different from the current email.";
    static final String IDAM_USER_NOT_FOUND_ERROR =
            "No IdAM account was found for the new email address.";
    static final String IDAM_USER_AMBIGUOUS_ERROR =
            "More than one IdAM account was found for the new email address.";
    static final String ACCESS_LOOKUP_ERROR =
            "The claimant's existing case access could not be checked. Try again later.";
    static final String ACCESS_REVOKE_ERROR =
            "Failed to revoke access linked to the previous claimant email. The claimant email was not changed.";
    static final String ACCESS_GRANT_ERROR =
            "Failed to grant case access using the new claimant email. The claimant email was not changed.";
    static final String EMAIL_UPDATE_AFTER_REASSIGN_ERROR =
            "Case access was updated for the new email, but the claimant email could not be saved. "
                    + "Check case access before retrying.";
    static final String EMAIL_UPDATE_AFTER_GRANT_ERROR =
            "Case access was granted for the new email, but the claimant email could not be saved. "
                    + "Check case access before retrying.";
    static final String EMAIL_UPDATE_ERROR =
            "The claimant email could not be saved. Case access was not changed.";

    private final IdamApi idamApi;
    private final AdminUserService adminUserService;
    private final CcdCaseAssignment ccdCaseAssignment;

    public void initialise(CaseData caseData) {
        caseData.setNewClaimantEmail(null);
        caseData.setCurrentClaimantEmail(caseData.getClaimantType() == null
                ? null
                : caseData.getClaimantType().getClaimantEmailAddress());
    }

    public List<String> validateNewEmail(CaseData caseData) {
        List<String> errors = validateEmailInput(caseData);
        if (errors.isEmpty()) {
            findUserByEmail(caseData.getNewClaimantEmail(), errors);
        }
        return errors;
    }

    public List<String> prepareUpdate(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        List<String> errors = validateEmailInput(caseData);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }

        Optional<UserDetails> newUser = findUserByEmail(caseData.getNewClaimantEmail(), errors);
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
        if (errors.isEmpty() && StringUtils.equalsIgnoreCase(
                caseData.getCurrentClaimantEmail(), caseData.getNewClaimantEmail())) {
            errors.add(EMAIL_UNCHANGED_ERROR);
        }
        return errors;
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
        return Optional.of(exactMatches.getFirst());
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
