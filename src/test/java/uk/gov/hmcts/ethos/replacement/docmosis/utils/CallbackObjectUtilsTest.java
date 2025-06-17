package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;

final class CallbackObjectUtilsTest {
    @Test
    @SneakyThrows
    void testCloneObject() {
        assertNull(CallbackObjectUtils.cloneObject(null, DocumentTypeItem.class));

        CaseData caseData =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        DocumentTypeItem documentTypeItem = caseData.getDocumentCollection().get(0);

        assertNull(CallbackObjectUtils.cloneObject(documentTypeItem, null));

        assertEquals(CallbackObjectUtils.cloneObject(documentTypeItem, DocumentTypeItem.class), documentTypeItem);
    }
}
