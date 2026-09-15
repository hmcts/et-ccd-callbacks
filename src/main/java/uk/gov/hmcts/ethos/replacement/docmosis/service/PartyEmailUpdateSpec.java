package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;

import java.util.Optional;

import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_CREATOR;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;

/**
 * Party-specific configuration for shared email-update case-access mechanics.
 */
public record PartyEmailUpdateSpec(
        String caseRole,
        PartyEmailMessages messages,
        boolean skipLookupWhenPreviousUserIdBlank,
        AssignmentMatcher assignmentMatcher
) {

    @FunctionalInterface
    public interface AssignmentMatcher {
        Optional<CaseUserAssignment> findCurrentAssignment(CaseUserAssignmentData assignments, String previousUserId);
    }

    public static PartyEmailUpdateSpec claimant() {
        return new PartyEmailUpdateSpec(
                CASE_USER_ROLE_CREATOR,
                PartyEmailMessages.claimant(),
                false,
                (assignments, previousUserId) -> findFirstWithRole(assignments, CASE_USER_ROLE_CREATOR)
        );
    }

    public static PartyEmailUpdateSpec respondent() {
        return new PartyEmailUpdateSpec(
                CASE_USER_ROLE_DEFENDANT,
                PartyEmailMessages.respondent(),
                true,
                (assignments, previousUserId) -> {
                    if (StringUtils.isBlank(previousUserId)) {
                        return Optional.empty();
                    }
                    return findWithRoleAndUser(assignments, CASE_USER_ROLE_DEFENDANT, previousUserId);
                }
        );
    }

    private static Optional<CaseUserAssignment> findFirstWithRole(CaseUserAssignmentData assignments, String role) {
        if (assignments == null || assignments.getCaseUserAssignments() == null) {
            return Optional.empty();
        }
        return assignments.getCaseUserAssignments().stream()
                .filter(assignment -> role.equals(assignment.getCaseRole()))
                .findFirst();
    }

    private static Optional<CaseUserAssignment> findWithRoleAndUser(CaseUserAssignmentData assignments,
                                                                    String role,
                                                                    String userId) {
        if (assignments == null || assignments.getCaseUserAssignments() == null) {
            return Optional.empty();
        }
        return assignments.getCaseUserAssignments().stream()
                .filter(assignment -> role.equals(assignment.getCaseRole()))
                .filter(assignment -> userId.equals(assignment.getUserId()))
                .findFirst();
    }
}
