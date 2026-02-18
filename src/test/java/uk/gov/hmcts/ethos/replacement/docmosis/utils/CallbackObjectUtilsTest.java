package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;

final class CallbackObjectUtilsTest {

    private static final String DUMMY_STRING = "Dummy string";

    @Test
    @SneakyThrows
    void theCloneObject() {
        assertNull(CallbackObjectUtils.cloneObject(null, DocumentTypeItem.class));
        CaseData caseData =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        DocumentTypeItem documentTypeItem = caseData.getDocumentCollection().getFirst();
        assertNull(CallbackObjectUtils.cloneObject(documentTypeItem, null));
        assertEquals(CallbackObjectUtils.cloneObject(documentTypeItem, DocumentTypeItem.class), documentTypeItem);
    }

    @Test
    void theIsEmpty() {
        // when object is instance of string and empty should return empty message
        assertThat(CallbackObjectUtils.isEmpty(StringUtils.EMPTY)).isTrue();
        // when object is instance of string and not empty should return not empty message
        assertThat(CallbackObjectUtils.isEmpty(DUMMY_STRING)).isFalse();
        // when object is a collection and empty should return empty message
        assertThat(CallbackObjectUtils.isEmpty(new ArrayList<>())).isTrue();
        // when object is a collection and not empty should return not empty message
        assertThat(CallbackObjectUtils.isEmpty(List.of(new RespondentSumTypeItem()))).isFalse();
        // when object is empty should return empty message
        assertThat(CallbackObjectUtils.isEmpty(null)).isTrue();
        // when object is not empty should return not empty message
        assertThat(CallbackObjectUtils.isEmpty(new CallbackRequest())).isFalse();
    }

    @Test
    void theIsAnyEmptyTwoObjects() {
        // when both objects are empty should return true
        assertThat(CallbackObjectUtils.isAnyEmpty(null, null)).isTrue();
        // when second object is empty should return true
        assertThat(CallbackObjectUtils.isAnyEmpty(List.of(DUMMY_STRING), null)).isTrue();
        // when first object is empty should return true
        assertThat(CallbackObjectUtils.isAnyEmpty(null, List.of(DUMMY_STRING))).isTrue();
        // when both objects are not empty should return false
        assertThat(CallbackObjectUtils.isAnyEmpty(List.of(DUMMY_STRING), List.of(new CallbackRequest()))).isFalse();
        // when both objects are not empty and different objects should return false
        assertThat(CallbackObjectUtils.isAnyEmpty(List.of(DUMMY_STRING), new CallbackRequest())).isFalse();
    }

    @Test
    void theIsAnyEmptyMoreThanTwoObjects() {
        // when object array is null should return false
        assertThat(CallbackObjectUtils.isAnyEmpty(null)).isFalse();
        // when object array is empty should return false
        assertThat(CallbackObjectUtils.isAnyEmpty(new String[0])).isFalse();
        // when objects are not empty should return false
        String[] stringArray = {DUMMY_STRING, DUMMY_STRING, DUMMY_STRING};
        assertThat(CallbackObjectUtils.isAnyEmpty(stringArray)).isFalse();
        // when second object is empty should return true
        String[] stringArray2 = {StringUtils.EMPTY, DUMMY_STRING, DUMMY_STRING};
        assertThat(CallbackObjectUtils.isAnyEmpty(stringArray2)).isTrue();
    }
}
