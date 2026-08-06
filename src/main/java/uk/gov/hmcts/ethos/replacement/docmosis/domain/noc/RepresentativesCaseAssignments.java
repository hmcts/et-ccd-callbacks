package uk.gov.hmcts.ethos.replacement.docmosis.domain.noc;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;

import java.util.List;

@Data
@Builder
public class RepresentativesCaseAssignments {
    List<CaseUserAssignment> revokedCaseUserAssignments;
    List<RepresentedTypeRItem> representativesToRemove;
}
