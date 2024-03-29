package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;

import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;

public final class TseApplicationUtil {
    private TseApplicationUtil() {
    }

    @NotNull
    public static GenericTseApplicationTypeItem getGenericTseApplicationTypeItem(String respondentResponseRequired) {
        GenericTseApplicationType build = GenericTseApplicationType.builder()
            .applicant(CLAIMANT_TITLE)
            .date("13 December 2022").dueDate("20 December 2022").type("Withdraw my claim")
            .details("Text").number("1")
            .responsesCount("0").status(OPEN_STATE)
            .respondentResponseRequired(respondentResponseRequired).build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        return genericTseApplicationTypeItem;
    }
}
