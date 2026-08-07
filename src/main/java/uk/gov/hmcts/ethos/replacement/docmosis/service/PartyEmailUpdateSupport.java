package uk.gov.hmcts.ethos.replacement.docmosis.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Shared IdAM lookup and case-access grant/reassign mechanics for party email updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PartyEmailUpdateSupport {

    private static final String CITIZEN_ROLE = "citizen";

    private final IdamApi idamApi;
    private final AdminUserService adminUserService;
    private final CcdCaseAssignment ccdCaseAssignment;

    public Optional<UserDetails> findCitizenUserByEmail(String email,
                                                        PartyEmailMessages messages,
                                                        List<String> errors) {
        List<UserDetails> exactMatches;
        try {
            exactMatches = idamApi.searchUsersByQuery(
                            adminUserService.getAdminUserToken(), email, 0, 50)
                    .stream()
                    .filter(user -> StringUtils.equalsIgnoreCase(email, user.getEmail()))
                    .toList();
        } catch (FeignException exception) {
            log.error("Unable to search IdAM users while validating party email update", exception);
            errors.add(messages.idamUserLookupError());
            return Optional.empty();
        }
        if (exactMatches.isEmpty()) {
            errors.add(messages.idamUserNotFoundError());
            return Optional.empty();
        }
        if (exactMatches.size() > 1) {
            errors.add(messages.idamUserAmbiguousError());
            return Optional.empty();
        }
        UserDetails user = exactMatches.getFirst();
        if (CollectionUtils.isEmpty(user.getRoles()) || !user.getRoles().contains(CITIZEN_ROLE)) {
            errors.add(messages.idamUserNotCitizenError());
            return Optional.empty();
        }
        return Optional.of(user);
    }

    public AccessOutcome ensureCaseAccess(String caseId,
                                          String previousUserId,
                                          String newUserId,
                                          PartyEmailUpdateSpec spec) {
        Optional<CaseUserAssignment> oldAssignment = findCurrentAssignment(caseId, previousUserId, spec);
        if (oldAssignment.isEmpty()) {
            grantAccess(caseId, newUserId, spec);
            log.info("Granted {} access to user {} for case {}", spec.caseRole(), newUserId, caseId);
            return AccessOutcome.GRANTED;
        }

        String oldUserId = oldAssignment.get().getUserId();
        if (StringUtils.equals(oldUserId, newUserId)) {
            return AccessOutcome.UNCHANGED;
        }

        reassignAccess(caseId, oldUserId, newUserId, spec);
        return AccessOutcome.REASSIGNED;
    }

    public String emailUpdateFailureMessage(AccessOutcome accessOutcome, PartyEmailMessages messages) {
        return switch (accessOutcome) {
            case REASSIGNED -> messages.emailUpdateAfterReassignError();
            case GRANTED -> messages.emailUpdateAfterGrantError();
            case UNCHANGED -> messages.emailUpdateError();
        };
    }

    private Optional<CaseUserAssignment> findCurrentAssignment(String caseId,
                                                               String previousUserId,
                                                               PartyEmailUpdateSpec spec) {
        if (spec.skipLookupWhenPreviousUserIdBlank() && StringUtils.isBlank(previousUserId)) {
            return Optional.empty();
        }
        try {
            CaseUserAssignmentData assignments = ccdCaseAssignment.getCaseUserRoles(caseId);
            return spec.assignmentMatcher().findCurrentAssignment(assignments, previousUserId);
        } catch (IOException exception) {
            throw new CcdInputOutputException(spec.messages().accessLookupError(), exception);
        }
    }

    private void reassignAccess(String caseId, String oldUserId, String newUserId, PartyEmailUpdateSpec spec) {
        CaseAssignmentUserRolesRequest oldAccess = buildRoleRequest(caseId, oldUserId, spec.caseRole());
        CaseAssignmentUserRolesRequest newAccess = buildRoleRequest(caseId, newUserId, spec.caseRole());
        try {
            ccdCaseAssignment.removeCaseUserRole(oldAccess);
        } catch (Exception revokeException) {
            throw new CcdInputOutputException(spec.messages().accessRevokeError(), revokeException);
        }
        try {
            ccdCaseAssignment.addCaseUserRole(newAccess);
        } catch (Exception grantException) {
            try {
                ccdCaseAssignment.addCaseUserRole(oldAccess);
            } catch (Exception restoreException) {
                grantException.addSuppressed(restoreException);
                log.error("Failed to restore {} access for case {}", spec.caseRole(), caseId, restoreException);
            }
            throw new CcdInputOutputException(spec.messages().accessGrantError(), grantException);
        }
    }

    private void grantAccess(String caseId, String userId, PartyEmailUpdateSpec spec) {
        try {
            ccdCaseAssignment.addCaseUserRole(buildRoleRequest(caseId, userId, spec.caseRole()));
        } catch (Exception grantException) {
            throw new CcdInputOutputException(spec.messages().accessGrantError(), grantException);
        }
    }

    private CaseAssignmentUserRolesRequest buildRoleRequest(String caseId, String userId, String caseRole) {
        CaseAssignmentUserRole role = CaseAssignmentUserRole.builder()
                .caseDataId(caseId)
                .userId(userId)
                .caseRole(caseRole)
                .build();
        return CaseAssignmentUserRolesRequest.builder()
                .caseAssignmentUserRoles(List.of(role))
                .build();
    }
}
