package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CaseTransferOfficeServiceTest {

    @Test
    void populateTransferToEnglandWalesOfficeOptionsForCaseInEnglandWales() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        ArrayList<TribunalOffice> expectedTribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);

        CaseTransferOfficeService.populateTransferToEnglandWalesOfficeOptions(caseData);

        verifyOfficeCTDynamicList(expectedTribunalOffices, null, caseData.getOfficeCT());
    }

    @Test
    void populateTransferToEnglandWalesOfficeOptionsForCaseInScotland() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        ArrayList<TribunalOffice> expectedTribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);

        CaseTransferOfficeService.populateTransferToEnglandWalesOfficeOptions(caseData);

        verifyOfficeCTDynamicList(expectedTribunalOffices, null, caseData.getOfficeCT());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " "})
    void populateTransferToEnglandWalesOfficeOptionsHandlesMissingManagingOffice(String managingOffice) {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(managingOffice);
        String existingOfficeCT = TribunalOffice.MANCHESTER.getOfficeName();
        caseData.setOfficeCT(DynamicFixedListType.of(DynamicValueType.create(existingOfficeCT, existingOfficeCT)));

        CaseTransferOfficeService.populateTransferToEnglandWalesOfficeOptions(caseData);

        assertNull(caseData.getOfficeCT().getListItems());
        assertEquals(existingOfficeCT, caseData.getOfficeCT().getSelectedCode());
        assertEquals(existingOfficeCT, caseData.getOfficeCT().getSelectedLabel());
    }

    @Test
    void populateTransferToScotlandOfficeOptionsForCaseInEnglandWales() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        ArrayList<TribunalOffice> expectedTribunalOffices = new ArrayList<>(TribunalOffice.SCOTLAND_OFFICES);

        CaseTransferOfficeService.populateTransferToScotlandOfficeOptions(caseData);

        verifyOfficeCTDynamicList(expectedTribunalOffices, TribunalOffice.GLASGOW.getOfficeName(),
                caseData.getOfficeCT());
    }

    @Test
    void populateTransferToEnglandWalesOfficeOptionsForMultipleCaseInEnglandWales() {
        MultipleData multipleData = new MultipleData();
        multipleData.setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        ArrayList<TribunalOffice> expectedTribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);

        CaseTransferOfficeService.populateTransferToEnglandWalesOfficeOptions(multipleData);

        verifyOfficeCTDynamicList(expectedTribunalOffices, null, multipleData.getOfficeMultipleCT());
    }

    @Test
    void populateTransferToEnglandWalesOfficeOptionsForMultipleCaseInScotland() {
        MultipleData multipleData = new MultipleData();
        multipleData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        ArrayList<TribunalOffice> expectedTribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);

        CaseTransferOfficeService.populateTransferToEnglandWalesOfficeOptions(multipleData);

        verifyOfficeCTDynamicList(expectedTribunalOffices, null, multipleData.getOfficeMultipleCT());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", " "})
    void populateTransferToEnglandWalesOfficeOptionsHandlesMissingManagingOfficeMultiple(String managingOffice) {
        MultipleData multipleData = new MultipleData();
        multipleData.setManagingOffice(managingOffice);
        String existingOfficeCT = TribunalOffice.MANCHESTER.getOfficeName();
        multipleData.setOfficeMultipleCT(DynamicFixedListType.of(DynamicValueType.create(existingOfficeCT,
                existingOfficeCT)));

        CaseTransferOfficeService.populateTransferToEnglandWalesOfficeOptions(multipleData);

        assertNull(multipleData.getOfficeMultipleCT().getListItems());
        assertEquals(existingOfficeCT, multipleData.getOfficeMultipleCT().getSelectedCode());
        assertEquals(existingOfficeCT, multipleData.getOfficeMultipleCT().getSelectedLabel());
    }

    @Test
    void populateTransferToScotlandOfficeOptionsForMultipleCaseInEnglandWales() {
        MultipleData multipleData = new MultipleData();
        multipleData.setManagingOffice(TribunalOffice.MANCHESTER.getOfficeName());
        ArrayList<TribunalOffice> expectedTribunalOffices = new ArrayList<>(TribunalOffice.SCOTLAND_OFFICES);

        CaseTransferOfficeService.populateTransferToScotlandOfficeOptions(multipleData);

        verifyOfficeCTDynamicList(expectedTribunalOffices, TribunalOffice.GLASGOW.getOfficeName(),
                multipleData.getOfficeMultipleCT());
    }

    private void verifyOfficeCTDynamicList(List<TribunalOffice> expectedOffices, String expectedValue,
                                           DynamicFixedListType actual) {
        assertEquals(expectedOffices.size(), actual.getListItems().size());
        Iterator<TribunalOffice> expectedItr = expectedOffices.listIterator();
        Iterator<DynamicValueType> listItemsItr = actual.getListItems().listIterator();
        while (expectedItr.hasNext() && listItemsItr.hasNext()) {
            TribunalOffice tribunalOffice = expectedItr.next();
            DynamicValueType dynamicValueType = listItemsItr.next();
            assertEquals(tribunalOffice.getOfficeName(), dynamicValueType.getCode());
            assertEquals(tribunalOffice.getOfficeName(), dynamicValueType.getLabel());
        }

        if (expectedValue == null) {
            assertNull(actual.getValue());
        } else {
            assertEquals(expectedValue, actual.getValue().getCode());
            assertEquals(expectedValue, actual.getValue().getLabel());
        }

    }
}
