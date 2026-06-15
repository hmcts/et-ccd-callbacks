package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

class ETCaseViewTest {

    @Test
    void returnsConfiguredCaseTypesAndCase() {
        ETCaseView caseView = new ETCaseView();
        CaseData caseData = new CaseData();

        assertEquals(Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID), caseView.caseTypeIds());
        assertSame(caseData, caseView.getCase(null, caseData));
    }
}
