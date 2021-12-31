package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CaseTransferOfficeServiceTest {

    @Test
    public void testPopulateCaseTransferOfficesScotland() {
        var caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        var tribunalOffices = List.of(TribunalOffice.LEEDS, TribunalOffice.MANCHESTER);
        var caseTransferOffices = createOfficeList(tribunalOffices);
        caseData.setOfficeCT(caseTransferOffices);

        CaseTransferOfficeService.populateOfficeOptions(caseData);

        assertTrue(caseData.getOfficeCT().getListItems().isEmpty());
        assertNull(caseData.getOfficeCT().getValue());
    }

    @Test
    public void testPopulateCaseTransferOfficesEnglandWales() {
        var caseData = new CaseData();
        var tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        tribunalOffices.remove(TribunalOffice.MANCHESTER);
        var caseTransferOffices = createOfficeList(tribunalOffices);
        caseData.setManagingOffice(managingOffice);
        caseData.setOfficeCT(caseTransferOffices);

        CaseTransferOfficeService.populateOfficeOptions(caseData);

        verifyTribunalOffices(tribunalOffices, caseData.getOfficeCT().getListItems());
    }

    @Test
    public void testPopulateMultipleCaseTransferOfficesIgnoresMissingManagingOffice() {
        var multipleData = new MultipleData();
        var tribunalOffices = List.of(TribunalOffice.LEEDS, TribunalOffice.MANCHESTER);
        var caseTransferOffices = createOfficeList(tribunalOffices);
        multipleData.setOfficeMultipleCT(caseTransferOffices);
        var missingManagingOfficeValues = new String[] { null, "", " "};
        for (String managingOffice : missingManagingOfficeValues) {
            multipleData.setManagingOffice(managingOffice);

            CaseTransferOfficeService.populateOfficeOptions(multipleData);

            verifyTribunalOffices(tribunalOffices, multipleData.getOfficeMultipleCT().getListItems());
        }
    }

    @Test
    public void testPopulateMultipleCaseTransferOfficesScotland() {
        var multipleData = new MultipleData();
        multipleData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        var tribunalOffices = List.of(TribunalOffice.LEEDS, TribunalOffice.MANCHESTER);
        var caseTransferOffices = createOfficeList(tribunalOffices);
        multipleData.setOfficeMultipleCT(caseTransferOffices);

        CaseTransferOfficeService.populateOfficeOptions(multipleData);

        assertTrue(multipleData.getOfficeMultipleCT().getListItems().isEmpty());
        assertNull(multipleData.getOfficeMultipleCT().getValue());
    }

    @Test
    public void testPopulateMultipleCaseTransferOfficesEnglandWales() {
        var multipleData = new MultipleData();
        var tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        var managingOffice = TribunalOffice.MANCHESTER.getOfficeName();
        tribunalOffices.remove(TribunalOffice.MANCHESTER);
        var caseTransferOffices = createOfficeList(tribunalOffices);
        multipleData.setManagingOffice(managingOffice);
        multipleData.setOfficeMultipleCT(caseTransferOffices);

        CaseTransferOfficeService.populateOfficeOptions(multipleData);

        verifyTribunalOffices(tribunalOffices, multipleData.getOfficeMultipleCT().getListItems());
    }

    private DynamicFixedListType createOfficeList(List<TribunalOffice> tribunalOffices) {
        var listItems = new ArrayList<DynamicValueType>();
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            listItems.add(DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()));
        }

        return DynamicFixedListType.from(listItems);
    }

    private void verifyTribunalOffices(List<TribunalOffice> expected, List<DynamicValueType> listItems) {
        assertEquals(expected.size(), listItems.size());
        Iterator<TribunalOffice> expectedItr = expected.listIterator();
        Iterator<DynamicValueType> listItemsItr = listItems.listIterator();
        while (expectedItr.hasNext() && listItemsItr.hasNext()) {
            var tribunalOffice = expectedItr.next();
            var dynamicValueType = listItemsItr.next();
            assertEquals(tribunalOffice.getOfficeName(), dynamicValueType.getCode());
            assertEquals(tribunalOffice.getOfficeName(), dynamicValueType.getLabel());
        }
    }
}
