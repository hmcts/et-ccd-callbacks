package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.caseview.MultipleCasesRenderer;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

class MultipleCaseViewTest {

    @Test
    void returnsConfiguredCaseTypesAndCase() {
        MultipleCasesRenderer multipleCasesRenderer = mock(MultipleCasesRenderer.class);
        MultipleCaseView caseView = new MultipleCaseView(multipleCasesRenderer);
        MultipleData multipleData = new MultipleData();
        long caseReference = 1234567890123456L;

        when(multipleCasesRenderer.render(caseReference, null)).thenReturn("Rendered cases");

        assertEquals(Set.of(ENGLANDWALES_BULK_CASE_TYPE_ID, SCOTLAND_BULK_CASE_TYPE_ID), caseView.caseTypeIds());
        assertSame(multipleData, caseView.getCase(new CaseViewRequest<>(caseReference, null), multipleData));
        assertEquals("Rendered cases", multipleData.getMultipleCasesMarkdown());
        verify(multipleCasesRenderer).render(caseReference, null);
    }
}
