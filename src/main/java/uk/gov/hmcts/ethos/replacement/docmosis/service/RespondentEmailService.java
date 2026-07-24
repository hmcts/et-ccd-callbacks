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

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentEmailService {

    static final String NO_UNREPRESENTED_RESPONDENTS_ERROR =
            "There are no unrepresented respondents whose email address can be updated.";
    static final String RESPONDENT_REQUIRED_ERROR = RespondentEmailUpdateHelper.RESPONDENT_REQUIRED_ERROR;
    static final String EMAIL_UNCHANGED_ERROR = RespondentEmailUpdateHelper.EMAIL_UNCHANGED_ERROR;
    static final String IDAM_USER_NOT_FOUND_ERROR = "No IdAM account was found for the new email address.";
    static final String IDAM_USER_AMBIGUOUS_ERROR =
            "More than one IdAM account was found for the new email address.";
    static final String ACCESS_LOOKUP_ERROR =
            "The respondent's existing case access could not be checked. Try again later.";
    static final String ACCESS_REVOKE_ERROR =
            "Failed to revoke access linked to the previous respondent email. The respondent email was not changed.";
    static final String ACCESS_GRANT_ERROR =
            "Failed to grant case access using the new respondent email. The respondent email was not changed.";
    static final String EMAIL_UPDATE_AFTER_REASSIGN_ERROR =
            "Case access was updated for the new email, but the respondent email could not be saved. "
                    + "Check case access before retrying.";
    static final String EMAIL_UPDATE_AFTER_GRANT_ERROR =
            "Case access was granted for the new email, but the respondent email could not be saved. "
                    + "Check case access before retrying.";
    static final String EMAIL_UPDATE_ERROR =
            "The respondent email could not be saved. Case access was not changed.";

    private static final boolean UNREPRESENTED = false;

    private final IdamApi idamApi;
    private final AdminUserService adminUserService;
    private final CcdCaseAssignment ccdCaseAssignment;

    public List<String> initialise(CaseData caseData) {
        return RespondentEmailUpdateHelper.initialise(caseData, UNREPRESENTED, NO_UNREPRESENTED_RESPONDENTS_ERROR);
    }

    public List<String> populateCurrentEmail(CaseData caseData) {
        return RespondentEmailUpdateHelper.populateCurrentEmail(
                caseData, UNREPRESENTED, NO_UNREPRESENTED_RESPONDENTS_ERROR);
    }

    public List<String> validateNewEmail(CaseData caseData) {
        List<String> errors = RespondentEmailUpdateHelper.validateInput(
                caseData, UNREPRESENTED, NO_UNREPRESENTED_RESPONDENTS_ERROR);
        if (errors.isEmpty()) {
            findUserByEmail(caseData.getNewRespondentEmail(), errors);
        }
        return errors;
    }

    public List<String> prepareUpdate(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        List<String> errors = RespondentEmailUpdateHelper.validateInput(
                caseData, UNREPRESENTED, NO_UNREPRESENTED_RESPONDENTS_ERROR);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }

        Optional<RespondentSumTypeItem> selectedRespondent =
                RespondentEmailUpdateHelper.getSelectedEligibleRespondent(caseData, UNREPRESENTED);
        if (selectedRespondent.isEmpty()) {
            return List.of(RespondentEmailUpdateHelper.getSelectionError(
                    caseData, UNREPRESENTED, NO_UNREPRESENTED_RESPONDENTS_ERROR));
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
            caseData.setRespondentAccessTransferPending(null);
            caseData.setRespondentAccessPreviousIdamId(null);
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
        return Optional.of(exactMatches.getFirst());
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
