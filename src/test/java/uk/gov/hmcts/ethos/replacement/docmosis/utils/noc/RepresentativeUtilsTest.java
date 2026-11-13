package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class RepresentativeUtilsTest {

    private static final String IDAM_ID_1 = "84ac5b15-f28a-40fe-a5f4-c7127366fb41";
    private static final String IDAM_ID_2 = "84ac5b15-f28a-40fe-a5f4-c7127366fb42";

    @Test
    void  theFindFirstByIdamId() {
        // when case user assignments is empty should return null
        List<CaseUserAssignment> caseUserAssignments = new ArrayList<>();
        assertThat(RepresentativeUtils.findCaseUserAssignmentsByIdamId(caseUserAssignments, IDAM_ID_2)).isEmpty();
        // when IDAM id is empty should return null
        caseUserAssignments.add(CaseUserAssignment.builder().userId(IDAM_ID_1).build());
        assertThat(RepresentativeUtils.findCaseUserAssignmentsByIdamId(caseUserAssignments, StringUtils.EMPTY))
                .isEmpty();
        // when IDAM id is not found should return null
        assertThat(RepresentativeUtils.findCaseUserAssignmentsByIdamId(caseUserAssignments, IDAM_ID_2)).isEmpty();
        // when IDAM id is found should return the case user assignment
        assertThat(RepresentativeUtils.findCaseUserAssignmentsByIdamId(caseUserAssignments, IDAM_ID_1))
                .isEqualTo(caseUserAssignments);
    }
}
