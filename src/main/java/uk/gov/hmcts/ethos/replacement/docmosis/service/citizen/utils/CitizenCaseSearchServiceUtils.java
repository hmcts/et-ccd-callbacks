package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.model.ccd.CaseAssignmentUserRole;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.CREATOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.DEFENDANT;

public final class CitizenCaseSearchServiceUtils {

    private CitizenCaseSearchServiceUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Determines the user role associated with a given {@link CaseDetails} and {@link CaseAssignmentUserRole}.
     *
     * <p>
     * This method checks whether both the provided case details and case assignment user role are non-empty,
     * and whether the {@code caseId} from {@link CaseDetails} matches the {@code caseDataId} from
     * {@link CaseAssignmentUserRole}. If the case role is either {@code CREATOR} or {@code DEFENDANT},
     * the corresponding role string is returned. Otherwise, an empty string is returned.
     * </p>
     *
     * <p>
     * This method effectively identifies whether the user’s role in the case is one of the
     * recognized case roles (CREATOR or DEFENDANT) for the provided case.
     * </p>
     *
     * @param caseDetails             the {@link CaseDetails} object containing information about the case;
     *                                must not be {@code null} or empty
     * @param caseAssignmentUserRole  the {@link CaseAssignmentUserRole} object representing the user’s assigned role;
     *                                must not be {@code null} or empty
     * @return the user’s case role if the case IDs match and the role is either CREATOR or DEFENDANT;
     *         otherwise an empty string
     */
    public static String determineCaseUserRole(CaseDetails caseDetails,
                                               CaseAssignmentUserRole caseAssignmentUserRole) {
        return ObjectUtils.isNotEmpty(caseDetails)
                && ObjectUtils.isNotEmpty(caseDetails.getId())
                && ObjectUtils.isNotEmpty(caseAssignmentUserRole)
                && StringUtils.isNotEmpty(caseAssignmentUserRole.getCaseDataId())
                && (caseDetails.getId().toString().equals(caseAssignmentUserRole.getCaseDataId()))
                && (CREATOR.equals(caseAssignmentUserRole.getCaseRole())
                ||  DEFENDANT.equals(caseAssignmentUserRole.getCaseRole()))
                ? caseAssignmentUserRole.getCaseRole() : StringUtils.EMPTY;
    }

    /**
     * Filters a list of {@link CaseDetails} objects to include only those associated
     * with the specified case user role.
     * <p>
     * The method iterates through the provided lists of case details and
     * case assignment user roles, determining the user role for each case via
     * {@code determineCaseUserRole}. If the determined role matches the given
     * {@code caseUserRole}, the case is added to the resulting list.
     * </p>
     *
     * @param caseDetailsList          the list of available case details; may be empty or null
     * @param caseAssignmentUserRoles  the list of case assignment user roles; may be empty or null
     * @param caseUserRole             the user role to filter cases by; must not be blank
     * @return a list of {@link CaseDetails} objects associated with the specified role;
     *         returns an empty list if no matches are found or if any input is invalid
     */
    public static List<CaseDetails> filterCaseDetailsByUserRole(
            List<CaseDetails> caseDetailsList,
            List<CaseAssignmentUserRole> caseAssignmentUserRoles,
            String caseUserRole) {
        List<CaseDetails> caseDetailsListByRole = new ArrayList<>();
        if (CollectionUtils.isEmpty(caseDetailsList)
                || CollectionUtils.isEmpty(caseAssignmentUserRoles)
                || StringUtils.isBlank(caseUserRole)) {
            return caseDetailsListByRole;
        }
        for (CaseAssignmentUserRole caseAssignmentUserRole : caseAssignmentUserRoles) {
            for (CaseDetails caseDetails : caseDetailsList) {
                String tmpCaseUserRole = determineCaseUserRole(caseDetails, caseAssignmentUserRole);
                if (StringUtils.isNotBlank(tmpCaseUserRole) && tmpCaseUserRole.equals(caseUserRole)) {
                    caseDetailsListByRole.add(caseDetails);
                    break;
                }
            }
        }
        return caseDetailsListByRole;
    }

}
