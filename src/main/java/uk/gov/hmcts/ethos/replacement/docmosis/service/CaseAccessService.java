package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseAccessService {

    public static final String CREATOR_ROLE = "[CREATOR]";
    private final CcdCaseAssignment caseAssignment;

    /**
     * Assigns all existing case roles after the case transferred to another jurisdiction.
     * @param caseDetails the case details
     * @return a list of errors
     */
    public List<String> assignExistingCaseRoles(CaseDetails caseDetails) {
        CaseData caseData = caseDetails.getCaseData();

        String caseId;
        try {
            caseId = getOriginalCaseId(caseData);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return List.of("Error getting original case id");
        }
        try {
            List<CaseUserAssignment> caseAssignedUserRolesList =
                    caseAssignment.getCaseUserRoles(caseId).getCaseUserAssignments();

            if (caseAssignedUserRolesList.isEmpty()) {
                return List.of("Case assigned user roles list is empty");
            }
            List<String> errorList = new ArrayList<>();
            for (CaseUserAssignment caseUserAssignment : caseAssignedUserRolesList) {
                if (isNullOrEmpty(caseUserAssignment.getUserId()) || isNullOrEmpty(caseUserAssignment.getCaseRole())) {
                    errorList.add("User ID is null or empty");
                    continue;
                }
                CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
                        .userId(caseUserAssignment.getUserId())
                        .caseDataId(caseDetails.getCaseId())
                        .caseRole(caseUserAssignment.getCaseRole())
                        .build();
                caseAssignment.addCaseUserRole(CaseAssignmentUserRolesRequest.builder()
                        .caseAssignmentUserRoles(List.of(caseAssignmentUserRole))
                        .build());
            }
            if (CollectionUtils.isNotEmpty(errorList)) {
                return errorList;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            return List.of(String.format("Error assigning case access for case %s on behalf of %s",
                    caseId, caseDetails.getCaseId()));
        }
        return new ArrayList<>();
    }

    private String getOriginalCaseId(CaseData caseData) {
        if (isNullOrEmpty(caseData.getLinkedCaseCT())) {
            throw new IllegalArgumentException("Linked case id is null or empty");
        }

        Pattern pattern = Pattern.compile("(\\d{16})");
        Matcher matcher = pattern.matcher(caseData.getLinkedCaseCT());

        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not find 16 digit case id");
        }

        return matcher.group(1);

    }

    public List<String> getClaimantSolicitorUserIds(String caseId) {
        List<String> userIds = new ArrayList<>();
        try {
            List<CaseUserAssignment> assignments = caseAssignment.getCaseUserRoles(caseId).getCaseUserAssignments();
            for (CaseUserAssignment assignment : assignments) {
                if (ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel().equals(assignment.getCaseRole())) {
                    userIds.add(assignment.getUserId());
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving claimant solicitor users for case {}: {}", caseId, e.getMessage());
        }
        return userIds;
    }

    public List<String> getSolicitorUserIds(String caseId) {
        List<String> userIds = new ArrayList<>();
        try {
            List<CaseUserAssignment> assignments = caseAssignment.getCaseUserRoles(caseId).getCaseUserAssignments();
            for (CaseUserAssignment assignment : assignments) {
                if (SolicitorRole.from(assignment.getCaseRole()).isPresent()) {
                    userIds.add(assignment.getUserId());
                }
            }
        } catch (Exception e) {
            log.error("Error retrieving solicitor users for case {}: {}", caseId, e.getMessage());
        }
        return userIds;
    }
}
