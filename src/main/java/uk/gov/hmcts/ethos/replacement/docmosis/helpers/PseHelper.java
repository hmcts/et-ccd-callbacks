package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.stream.Collectors;

@Slf4j
public final class PseHelper {
    public static final String CLOSED = "Closed";

    private PseHelper() {
        // Access through static methods
    }

    /**
     * Create fields for application dropdown selector.
     * @param caseData contains all the case data
     */
    public static DynamicFixedListType populateSelectJONDropdown(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getGenericTseApplicationCollection())) {
            return null;
        }

        // TODO: change caseData.getGenericTseApplicationCollection() to get the correct object
        return DynamicFixedListType.from(caseData.getGenericTseApplicationCollection().stream()
            .filter(r -> r.getValue().getRespondCollection() == null
                && r.getValue().getStatus() != null
                && !CLOSED.equals(r.getValue().getStatus())
            ).map(r ->
                DynamicValueType.create(
                    r.getValue().getNumber(),
                    r.getValue().getNumber() + " " + r.getValue().getType()
                )
            ).collect(Collectors.toList()));
    }
}
