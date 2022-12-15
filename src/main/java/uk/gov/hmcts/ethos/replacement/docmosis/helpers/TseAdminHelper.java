package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

public class TseAdminHelper {
    public static final String CLOSED = "Closed";

    /**
     * Create fields for application dropdown selector.
     * @param caseData contains all the case data
     */
    public static DynamicFixedListType populateSelectApplicationAdminDropdown(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
            .filter(r -> r.getValue().getStatus() != null && !CLOSED.equals(r.getValue().getStatus()))
            .map(r -> DynamicValueType.create(r.getValue().getNumber(),
                r.getValue().getNumber() + " " + r.getValue().getType())
            )
            .collect(Collectors.toList()));
    }
}
