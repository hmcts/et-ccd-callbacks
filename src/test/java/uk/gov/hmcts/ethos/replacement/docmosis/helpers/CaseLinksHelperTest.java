package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLink;
import uk.gov.hmcts.et.common.model.ccd.types.LinkReason;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class CaseLinksHelperTest {

    @Test
    void shouldReturnFalseIfEmpty() {
        List<GenericTypeItem<CaseLink>> caseLinks = new ArrayList<>();
        boolean result = CaseLinksHelper.isLinkedForHearing(caseLinks);
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseIfNull() {
        boolean result = CaseLinksHelper.isLinkedForHearing(null);
        assertFalse(result);
    }

    @Test
    void shouldReturnFalseIfNotForHearing() {
        LinkReason linkReason = new LinkReason();
        linkReason.setReason("CLRC016");
        ListTypeItem<LinkReason> linkReasons = ListTypeItem.from(linkReason, "1");
        var caseLink = CaseLink.builder().caseReference("1").caseType(ENGLANDWALES_CASE_TYPE_ID)
                .reasonForLink(linkReasons).build();

        List<GenericTypeItem<CaseLink>> caseLinks = new ArrayList<>();
        GenericTypeItem<CaseLink> caseLinksBeforeSubmit = GenericTypeItem.from(caseLink);
        caseLinks.add(caseLinksBeforeSubmit);

        boolean result = CaseLinksHelper.isLinkedForHearing(caseLinks);

        assertFalse(result);
    }

    @Test
    void shouldReturnTrueIfForHearing() {
        LinkReason linkReason = new LinkReason();
        linkReason.setReason("CLRC017");
        ListTypeItem<LinkReason> linkReasons = ListTypeItem.from(linkReason, "1");
        var caseLink = CaseLink.builder().caseReference("1").caseType(ENGLANDWALES_CASE_TYPE_ID)
                .reasonForLink(linkReasons).build();
        List<GenericTypeItem<CaseLink>> caseLinks = new ArrayList<>();
        GenericTypeItem<CaseLink> caseLinksBeforeSubmit = GenericTypeItem.from(caseLink);
        caseLinks.add(caseLinksBeforeSubmit);

        boolean result = CaseLinksHelper.isLinkedForHearing(caseLinks);

        assertTrue(result);
    }
}
