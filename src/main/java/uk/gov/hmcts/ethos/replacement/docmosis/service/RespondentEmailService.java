package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignmentData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.idam.IdamApi;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.CcdCaseAssignment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.reform.et.syaapi.constants.ManageCaseRoleConstants.CASE_USER_ROLE_DEFENDANT;

@Slf4j
@Service
@RequiredArgsConstructor
public class RespondentEmailService {

    static final String NO_UNREPRESENTED_RESPONDENTS_ERROR =
            "There are no unrepresented respondents whose email address can be updated.";
    static final String RESPONDENT_REQUIRED_ERROR = "Select a respondent.";
    static final String EMAIL_UNCHANGED_ERROR = "Enter an email address that is different from the current email.";
    static final String IDAM_USER_NOT_FOUND_ERROR = "No IdAM account was found for the new email address.";
    static final String IDAM_USER_AMBIGUOUS_ERROR =
            "More than one IdAM account was found for the new email address.";
    static final String ACCESS_LOOKUP_ERROR =
            "The respondent's existing case access could not be checked. Try again later.";

    static final String CONFIRMATION_HEADER_SUCCESS = "# Respondent email updated";
    static final String CONFIRMATION_BODY_SUCCESS =
            "The respondent email has been updated.";
    static final String CONFIRMATION_BODY_SUCCESS_WITH_ACCESS =
            "The respondent email has been updated and case access has been transferred to the new email address.";
    static final String CONFIRMATION_HEADER_PARTIAL_FAILURE =
            "# Respondent email updated, but case access was not transferred";
    static final String CONFIRMATION_BODY_PARTIAL_FAILURE =
            "The respondent email was saved, but case access could not be moved to the account for the new email. "
                    + "This case has been marked so access can be retried.";

    static final String UPDATE_RESPONDENT_ACCESS_TRANSFER_STATE = "UPDATE_RESPONDENT_ACCESS_TRANSFER_STATE";

    private final IdamApi idamApi;
    private final AdminUserService adminUserService;
    private final CcdCaseAssignment ccdCaseAssignment;
    private final CcdClient ccdClient;

    public record Confirmation(String header, String body) {
    }

    public List<String> initialise(CaseData caseData) {
        caseData.setCurrentRespondentEmail(null);
        caseData.setNewRespondentEmail(null);

        List<DynamicValueType> respondents = getEligibleRespondents(caseData).stream()
                .map(respondent -> DynamicValueType.create(
                        respondent.getId(), respondent.getValue().getRespondentName()))
                .toList();
        caseData.setRespondentEmailUpdateSelection(DynamicFixedListType.from(respondents));

        return respondents.isEmpty() ? List.of(NO_UNREPRESENTED_RESPONDENTS_ERROR) : List.of();
    }

    public List<String> populateCurrentEmail(CaseData caseData) {
        Optional<RespondentSumTypeItem> selectedRespondent = getSelectedEligibleRespondent(caseData);
        if (selectedRespondent.isEmpty()) {
            caseData.setCurrentRespondentEmail(null);
            return List.of(getSelectionError(caseData));
        }

        RespondentSumType respondent = selectedRespondent.get().getValue();
        caseData.setCurrentRespondentEmail(StringUtils.firstNonBlank(
                respondent.getResponseRespondentEmail(), respondent.getRespondentEmail()));
        caseData.setNewRespondentEmail(null);
        return List.of();
    }

    public List<String> validateNewEmail(CaseData caseData) {
        List<String> errors = validateInput(caseData);
        if (errors.isEmpty()) {
            findUserByEmail(caseData.getNewRespondentEmail(), errors);
        }
        return errors;
    }

    public List<String> prepareUpdate(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();
        List<String> errors = validateInput(caseData);
        if (CollectionUtils.isNotEmpty(errors)) {
            return errors;
        }

        Optional<RespondentSumTypeItem> selectedRespondent = getSelectedEligibleRespondent(caseData);
        if (selectedRespondent.isEmpty()) {
            return List.of(getSelectionError(caseData));
        }

        Optional<UserDetails> newUser = findUserByEmail(caseData.getNewRespondentEmail(), errors);
        if (CollectionUtils.isNotEmpty(errors) || newUser.isEmpty()) {
            return errors;
        }

        RespondentSumType respondent = selectedRespondent.get().getValue();
        String previousIdamId = respondent.getIdamId();
        try {
            if (getDefendantAssignment(caseDetails.getCaseId(), previousIdamId).isPresent()) {
                respondent.setIdamId(newUser.get().getUid());
                markAccessTransferPending(caseData, previousIdamId);
            } else {
                clearAccessTransferTracker(caseData);
            }
        } catch (IOException exception) {
            log.error("Unable to retrieve respondent access for case {}", caseDetails.getCaseId(), exception);
            errors.add(ACCESS_LOOKUP_ERROR);
            return errors;
        }

        respondent.setRespondentEmail(caseData.getNewRespondentEmail());
        respondent.setResponseRespondentEmail(caseData.getNewRespondentEmail());
        caseData.setCurrentRespondentEmail(null);
        caseData.setNewRespondentEmail(null);
        return errors;
    }

    public Confirmation reassignDefendantAccess(CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseDetails caseDetailsBefore = callbackRequest.getCaseDetailsBefore();
        if (caseDetails == null || caseDetailsBefore == null
                || caseDetails.getCaseData() == null || caseDetailsBefore.getCaseData() == null) {
            throw new CcdInputOutputException(
                    "Missing case data required to update respondent access", new IllegalArgumentException());
        }

        String respondentId = getSelectedRespondentId(caseDetails.getCaseData());
        RespondentSumTypeItem respondentBefore = findRespondentById(caseDetailsBefore.getCaseData(), respondentId)
                .orElseThrow(() -> new CcdInputOutputException(
                        "The selected respondent could not be found in the previous case data",
                        new IllegalArgumentException()));
        RespondentSumTypeItem respondentAfter = findRespondentById(caseDetails.getCaseData(), respondentId)
                .orElseThrow(() -> new CcdInputOutputException(
                        "The selected respondent could not be found in the updated case data",
                        new IllegalArgumentException()));

        String oldUserId = respondentBefore.getValue().getIdamId();
        String newUserId = respondentAfter.getValue().getIdamId();
        if (StringUtils.isAnyBlank(oldUserId, newUserId) || StringUtils.equals(oldUserId, newUserId)) {
            clearAccessTransferTracker(caseDetails.getCaseData());
            return new Confirmation(CONFIRMATION_HEADER_SUCCESS, CONFIRMATION_BODY_SUCCESS);
        }

        CaseAssignmentUserRolesRequest oldAccess =
                buildDefendantRequest(caseDetails.getCaseId(), oldUserId);
        CaseAssignmentUserRolesRequest newAccess =
                buildDefendantRequest(caseDetails.getCaseId(), newUserId);
        try {
            ccdCaseAssignment.removeCaseUserRole(oldAccess);
        } catch (Exception revokeException) {
            log.error("Failed to revoke defendant access for case {} after email update",
                    caseDetails.getCaseId(), revokeException);
            return new Confirmation(CONFIRMATION_HEADER_PARTIAL_FAILURE, CONFIRMATION_BODY_PARTIAL_FAILURE);
        }
        try {
            ccdCaseAssignment.addCaseUserRole(newAccess);
        } catch (Exception grantException) {
            try {
                ccdCaseAssignment.addCaseUserRole(oldAccess);
            } catch (Exception restoreException) {
                grantException.addSuppressed(restoreException);
                log.error("Failed to restore defendant access for case {}", caseDetails.getCaseId(), restoreException);
            }
            log.error("Failed to grant defendant access for case {} after email update",
                    caseDetails.getCaseId(), grantException);
            return new Confirmation(CONFIRMATION_HEADER_PARTIAL_FAILURE, CONFIRMATION_BODY_PARTIAL_FAILURE);
        }

        persistClearedAccessTransferTracker(caseDetails);
        return new Confirmation(CONFIRMATION_HEADER_SUCCESS, CONFIRMATION_BODY_SUCCESS_WITH_ACCESS);
    }

    private void markAccessTransferPending(CaseData caseData, String previousIdamId) {
        caseData.setRespondentAccessTransferPending(YES);
        caseData.setRespondentAccessPreviousIdamId(previousIdamId);
    }

    private void clearAccessTransferTracker(CaseData caseData) {
        caseData.setRespondentAccessTransferPending(null);
        caseData.setRespondentAccessPreviousIdamId(null);
    }

    private void persistClearedAccessTransferTracker(CaseDetails caseDetails) {
        clearAccessTransferTracker(caseDetails.getCaseData());
        try {
            String adminToken = adminUserService.getAdminUserToken();
            CCDRequest ccdRequest = ccdClient.startEventForCase(
                    adminToken,
                    caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(),
                    caseDetails.getCaseId(),
                    UPDATE_RESPONDENT_ACCESS_TRANSFER_STATE);
            CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
            clearAccessTransferTracker(caseData);
            ccdClient.submitEventForCase(
                    adminToken,
                    caseData,
                    caseDetails.getCaseTypeId(),
                    caseDetails.getJurisdiction(),
                    ccdRequest,
                    caseDetails.getCaseId());
        } catch (Exception exception) {
            log.error("Failed to clear respondent access transfer tracker for case {}",
                    caseDetails.getCaseId(), exception);
        }
    }

    private List<String> validateInput(CaseData caseData) {
        List<String> errors = new ArrayList<>();
        Optional<RespondentSumTypeItem> selectedRespondent = getSelectedEligibleRespondent(caseData);
        if (selectedRespondent.isEmpty()) {
            errors.add(getSelectionError(caseData));
            return errors;
        }

        errors.addAll(ReferralHelper.validateEmail(caseData.getNewRespondentEmail()));
        if (errors.isEmpty() && StringUtils.equalsIgnoreCase(
                caseData.getCurrentRespondentEmail(), caseData.getNewRespondentEmail())) {
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

    private List<RespondentSumTypeItem> getEligibleRespondents(CaseData caseData) {
        return emptyIfNull(caseData.getRespondentCollection()).stream()
                .filter(this::isValidRespondent)
                .filter(respondent -> !isRepresented(caseData, respondent))
                .toList();
    }

    private boolean isValidRespondent(RespondentSumTypeItem respondent) {
        return respondent != null && StringUtils.isNotBlank(respondent.getId())
                && respondent.getValue() != null
                && StringUtils.isNotBlank(respondent.getValue().getRespondentName());
    }

    private boolean isRepresented(CaseData caseData, RespondentSumTypeItem respondent) {
        RespondentSumType respondentValue = respondent.getValue();
        if (YES.equalsIgnoreCase(respondentValue.getRepresentativeRemoved())) {
            return false;
        }
        if (YES.equalsIgnoreCase(respondentValue.getRepresented())) {
            return true;
        }
        return emptyIfNull(caseData.getRepCollection()).stream()
                .filter(rep -> rep != null && rep.getValue() != null)
                .map(RepresentedTypeRItem::getValue)
                .anyMatch(rep -> respondent.getId().equals(rep.getRespondentId())
                        || respondentValue.getRespondentName().equals(rep.getRespRepName()));
    }

    private Optional<RespondentSumTypeItem> getSelectedEligibleRespondent(CaseData caseData) {
        String selectedId = getSelectedRespondentId(caseData);
        if (StringUtils.isBlank(selectedId)) {
            return Optional.empty();
        }
        return getEligibleRespondents(caseData).stream()
                .filter(respondent -> selectedId.equals(respondent.getId()))
                .findFirst();
    }

    private Optional<RespondentSumTypeItem> findRespondentById(CaseData caseData, String respondentId) {
        return emptyIfNull(caseData.getRespondentCollection()).stream()
                .filter(this::isValidRespondent)
                .filter(respondent -> respondent.getId().equals(respondentId))
                .findFirst();
    }

    private String getSelectedRespondentId(CaseData caseData) {
        DynamicFixedListType selection = caseData.getRespondentEmailUpdateSelection();
        return selection == null ? null : selection.getSelectedCode();
    }

    private String getSelectionError(CaseData caseData) {
        return getEligibleRespondents(caseData).isEmpty()
                ? NO_UNREPRESENTED_RESPONDENTS_ERROR
                : RESPONDENT_REQUIRED_ERROR;
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
}
