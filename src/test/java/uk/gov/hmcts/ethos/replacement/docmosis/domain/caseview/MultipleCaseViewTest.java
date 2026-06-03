package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

class MultipleCaseViewTest {

    @Test
    void returnsConfiguredCaseTypesAndCase() {
        MultipleCaseView caseView = new MultipleCaseView();
        MultipleData multipleData = new MultipleData();

        assertEquals(Set.of(ENGLANDWALES_BULK_CASE_TYPE_ID, SCOTLAND_BULK_CASE_TYPE_ID), caseView.caseTypeIds());
        assertSame(multipleData, caseView.getCase(null, multipleData));
    }
}
