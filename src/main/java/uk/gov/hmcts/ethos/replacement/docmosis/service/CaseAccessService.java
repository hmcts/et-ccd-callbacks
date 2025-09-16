package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional.OrganisationClient;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import static com.google.common.base.Strings.isNullOrEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class CaseAccessService {

    public static final String CREATOR_ROLE = "[CREATOR]";
    private final CcdCaseAssignment caseAssignment;
    private final OrganisationClient organisationClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final AdminUserService adminUserService;

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

    public List<CaseUserAssignment> getCaseUserAssignmentsById(String caseId) {
        try {
            return caseAssignment.getCaseUserRoles(caseId).getCaseUserAssignments();
        } catch (Exception e) {
            log.error("Error retrieving user assignments for case {}: {}", caseId, e.getMessage());
            throw new NoSuchElementException(
                    String.format("No user assignments found for case %s", caseId), e);
        }
    }

    /**
     * Filters the list of CaseUserAssignment to include only those that match the specified organisation ID.
     *
     * @param caseUserAssignments the list of CaseUserAssignment to filter
     * @param userDetails current user details
     * @return a set of CaseUserAssignment that belong to the specified organisation
     */
    public Set<CaseUserAssignment> filterCaseAssignmentsByOrgId(List<CaseUserAssignment> caseUserAssignments,
                                                                UserDetails userDetails) {
        OrganisationsResponse organisationsResponse = organisationClient.retrieveOrganisationDetailsByUserId(
                adminUserService.getAdminUserToken(), authTokenGenerator.generate(), userDetails.getUid()).getBody();
        if (organisationsResponse != null && organisationsResponse.getOrganisationIdentifier() != null) {
            return caseUserAssignments.stream()
                    .filter(assignment ->
                            organisationsResponse.getOrganisationIdentifier().equals(assignment.getOrganisationId()))
                    .collect(Collectors.toSet());
        } else {
            return new HashSet<>();
        }
    }
}
