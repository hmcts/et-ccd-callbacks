package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.lang3.Strings;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class RepresentativeUtils {

    private RepresentativeUtils() {
        // Utility class, no instances allowed
    }

    public static List<CaseUserAssignment> findCaseUserAssignmentsByIdamId(
            List<CaseUserAssignment> caseUserAssignments, String idamId) {

        if (caseUserAssignments == null || idamId == null) {
            return new ArrayList<>();
        }

        return caseUserAssignments.stream()
                .filter(assignment -> Strings.CI.equals(idamId, assignment.getUserId()))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
