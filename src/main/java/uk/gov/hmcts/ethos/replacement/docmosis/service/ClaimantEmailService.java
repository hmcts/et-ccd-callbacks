package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        if (!errors.isEmpty()) {
            return errors;
        }

        Optional<UserDetails> newUser = findUserByEmail(caseData.getNewClaimantEmail(), errors);
        if (!errors.isEmpty() || newUser.isEmpty()) {
            return errors;
        }

        try {
            if (getCreatorAssignment(caseDetails.getCaseId()).isPresent()) {
                caseData.setClaimantId(newUser.get().getUid());
            }
        } catch (IOException exception) {
            log.error("Unable to retrieve creator access for case {}", caseDetails.getCaseId(), exception);
            errors.add(ACCESS_LOOKUP_ERROR);
            return errors;
        }

        if (caseData.getClaimantType() == null) {
            caseData.setClaimantType(new ClaimantType());
        }
        caseData.getClaimantType().setClaimantEmailAddress(caseData.getNewClaimantEmail());
        caseData.setCurrentClaimantEmail(null);
        caseData.setNewClaimantEmail(null);
        return errors;
    }

    public void reassignCreatorAccess(CaseDetails caseDetails) {
        Optional<CaseUserAssignment> oldCreator;
        try {
            oldCreator = getCreatorAssignment(caseDetails.getCaseId());
        } catch (IOException exception) {
            throw new CcdInputOutputException("Failed to retrieve claimant case access", exception);
        }

        if (oldCreator.isEmpty()) {
            log.info("No creator access exists for case {}; email updated without changing access",
                    caseDetails.getCaseId());
            return;
        }

        String newUserId = caseDetails.getCaseData().getClaimantId();
        String oldUserId = oldCreator.get().getUserId();
        if (StringUtils.equals(oldUserId, newUserId)) {
            return;
        }

        CaseAssignmentUserRolesRequest oldAccess = buildCreatorRequest(caseDetails.getCaseId(), oldUserId);
        CaseAssignmentUserRolesRequest newAccess = buildCreatorRequest(caseDetails.getCaseId(), newUserId);
        try {
            ccdCaseAssignment.removeCaseUserRole(oldAccess);
        } catch (Exception revokeException) {
            throw new CcdInputOutputException("Failed to revoke access linked to the previous claimant email",
                    revokeException);
        }
        try {
            ccdCaseAssignment.addCaseUserRole(newAccess);
        } catch (Exception grantException) {
            try {
                ccdCaseAssignment.addCaseUserRole(oldAccess);
            } catch (Exception restoreException) {
                grantException.addSuppressed(restoreException);
                log.error("Failed to restore creator access for case {}", caseDetails.getCaseId(), restoreException);
            }
            throw new CcdInputOutputException("Failed to grant case access using the new claimant email",
                    grantException);
        }
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
}
