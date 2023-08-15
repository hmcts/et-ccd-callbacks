package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLink;
import uk.gov.hmcts.et.common.model.ccd.types.LinkReason;

import java.util.List;
import java.util.Objects;

@Slf4j
public final class CaseLinksHelper {

    private CaseLinksHelper() {
    }

    private static final String LINKED_FOR_HEARING = "CLRC017";

    public static boolean isLinkedForHearing(List<GenericTypeItem<CaseLink>> caseLinks) {
        if (caseLinks == null || caseLinks.isEmpty()) {
            return false;
        }
        for (GenericTypeItem<CaseLink> link : caseLinks) {
            for (GenericTypeItem<LinkReason> linkReason : link.getValue().getReasonForLink()) {
                if (Objects.equals(linkReason.getValue().getReason(), LINKED_FOR_HEARING)) {
                    return true;
                }
            }
        }
        return false;
    }
}
