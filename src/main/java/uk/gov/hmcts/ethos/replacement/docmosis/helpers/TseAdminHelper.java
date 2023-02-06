package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.stream.Collectors;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;

public final class TseAdminHelper {

    private TseAdminHelper() {
        // Sonar Lint: Utility classes should not have public constructors
    }

    /**
     * Create fields for application dropdown selector.
     * @param caseData contains all the case data
     */
    public static DynamicFixedListType populateSelectApplicationAdminDropdown(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
            .filter(r -> r.getValue().getStatus() != null && !CLOSED_STATE.equals(r.getValue().getStatus()))
            .map(r -> DynamicValueType.create(r.getValue().getNumber(),
                r.getValue().getNumber() + " " + r.getValue().getType())
            )
            .collect(Collectors.toList()));
    }
}
