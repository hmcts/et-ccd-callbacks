package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;

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
     * Give a claimant access to a case when the case has been transferred to another jurisdiction.
     * @param caseDetails the case details
     * @return a list of errors
     */
    public List<String> assignClaimantCaseAccess(CaseDetails caseDetails) {
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

            String userId = caseAssignedUserRolesList.stream()
                    .filter(caseAssignedUserRole -> CREATOR_ROLE.equals(caseAssignedUserRole.getCaseRole()))
                    .findFirst()
                    .map(CaseUserAssignment::getUserId)
                    .orElse("");

            if (isNullOrEmpty(userId)) {
                return List.of("User ID is null or empty");
            }

            CaseAssignmentUserRole caseAssignmentUserRole = CaseAssignmentUserRole.builder()
                    .userId(userId)
                    .caseDataId(caseDetails.getCaseId())
                    .caseRole(CREATOR_ROLE)
                    .build();
            caseAssignment.addCaseUserRole(CaseAssignmentUserRolesRequest.builder()
                    .caseAssignmentUserRoles(List.of(caseAssignmentUserRole))
                    .build());

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

}
