package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.FileLocationService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.LawOfDemeter"})
public class ScotlandFileLocationSelectionServiceTest {

    @Test
    public void testInitialiseFileLocationNoFileLocationSelected() {
        var fileLocationService = mockFileLocationService();
        var caseData = new CaseData();

        var fileLocationSelectionService = new ScotlandFileLocationSelectionService(fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(caseData);
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getFileLocationAberdeen(),
                TribunalOffice.ABERDEEN.getOfficeNumber(), TribunalOffice.ABERDEEN.getOfficeName());
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getFileLocationDundee(),
                TribunalOffice.DUNDEE.getOfficeNumber(), TribunalOffice.DUNDEE.getOfficeName());
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getFileLocationEdinburgh(),
                TribunalOffice.EDINBURGH.getOfficeNumber(), TribunalOffice.EDINBURGH.getOfficeName());
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getFileLocationGlasgow(),
                TribunalOffice.GLASGOW.getOfficeNumber(), TribunalOffice.GLASGOW.getOfficeName());
    }

    @Test
    public void testInitialiseFileLocationsWithFileLocationSelected() {
        var fileLocationService = mockFileLocationService();
        var caseData = new CaseData();
        var selectedAberdeen = createSelectedListItemAtIndex(TribunalOffice.ABERDEEN, 1);
        caseData.setFileLocationAberdeen(DynamicFixedListType.of(selectedAberdeen));
        var selectedDundee = createSelectedListItemAtIndex(TribunalOffice.DUNDEE, 2);
        caseData.setFileLocationDundee(DynamicFixedListType.of(selectedDundee));
        var selectedEdinburgh = createSelectedListItemAtIndex(TribunalOffice.EDINBURGH, 3);
        caseData.setFileLocationEdinburgh(DynamicFixedListType.of(selectedEdinburgh));
        var selectedGlasgow = createSelectedListItemAtIndex(TribunalOffice.GLASGOW, 3);
        caseData.setFileLocationGlasgow(DynamicFixedListType.of(selectedGlasgow));

        var fileLocationSelectionService = new ScotlandFileLocationSelectionService(fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getFileLocationAberdeen(),
                TribunalOffice.ABERDEEN.getOfficeNumber(), TribunalOffice.ABERDEEN.getOfficeName(), selectedAberdeen);
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getFileLocationDundee(),
                TribunalOffice.DUNDEE.getOfficeNumber(), TribunalOffice.DUNDEE.getOfficeName(), selectedDundee);
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getFileLocationEdinburgh(),
                TribunalOffice.EDINBURGH.getOfficeNumber(), TribunalOffice.EDINBURGH.getOfficeName(),
                selectedEdinburgh);
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getFileLocationGlasgow(),
                TribunalOffice.GLASGOW.getOfficeNumber(), TribunalOffice.GLASGOW.getOfficeName(), selectedGlasgow);
    }

    @Test
    public void testInitialiseFileLocationMultipleDataNoFileLocationSelected() {
        var fileLocationService = mockFileLocationService();
        var caseData = new MultipleData();

        var fileLocationSelectionService = new ScotlandFileLocationSelectionService(fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(caseData);
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getFileLocationAberdeen(),
                TribunalOffice.ABERDEEN.getOfficeNumber(), TribunalOffice.ABERDEEN.getOfficeName());
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getFileLocationDundee(),
                TribunalOffice.DUNDEE.getOfficeNumber(), TribunalOffice.DUNDEE.getOfficeName());
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getFileLocationEdinburgh(),
                TribunalOffice.EDINBURGH.getOfficeNumber(), TribunalOffice.EDINBURGH.getOfficeName());
        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getFileLocationGlasgow(),
                TribunalOffice.GLASGOW.getOfficeNumber(), TribunalOffice.GLASGOW.getOfficeName());
    }

    @Test
    public void testInitialiseFileLocationsMultipleDataWithFileLocationSelected() {
        var fileLocationService = mockFileLocationService();
        var caseData = new MultipleData();
        var selectedAberdeen = createSelectedListItemAtIndex(TribunalOffice.ABERDEEN, 1);
        caseData.setFileLocationAberdeen(DynamicFixedListType.of(selectedAberdeen));
        var selectedDundee = createSelectedListItemAtIndex(TribunalOffice.DUNDEE, 2);
        caseData.setFileLocationDundee(DynamicFixedListType.of(selectedDundee));
        var selectedEdinburgh = createSelectedListItemAtIndex(TribunalOffice.EDINBURGH, 3);
        caseData.setFileLocationEdinburgh(DynamicFixedListType.of(selectedEdinburgh));
        var selectedGlasgow = createSelectedListItemAtIndex(TribunalOffice.GLASGOW, 3);
        caseData.setFileLocationGlasgow(DynamicFixedListType.of(selectedGlasgow));

        var fileLocationSelectionService = new ScotlandFileLocationSelectionService(fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getFileLocationAberdeen(),
                TribunalOffice.ABERDEEN.getOfficeNumber(), TribunalOffice.ABERDEEN.getOfficeName(), selectedAberdeen);
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getFileLocationDundee(),
                TribunalOffice.DUNDEE.getOfficeNumber(), TribunalOffice.DUNDEE.getOfficeName(), selectedDundee);
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getFileLocationEdinburgh(),
                TribunalOffice.EDINBURGH.getOfficeNumber(), TribunalOffice.EDINBURGH.getOfficeName(),
                selectedEdinburgh);
        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getFileLocationGlasgow(),
                TribunalOffice.GLASGOW.getOfficeNumber(), TribunalOffice.GLASGOW.getOfficeName(), selectedGlasgow);
    }

    private FileLocationService mockFileLocationService() {
        var fileLocationService = mock(FileLocationService.class);

        for (TribunalOffice tribunalOffice : TribunalOffice.SCOTLAND_OFFICES) {
            var fileLocations = SelectionServiceTestUtils.createListItems(tribunalOffice.getOfficeNumber(),
                    tribunalOffice.getOfficeName());
            when(fileLocationService.getFileLocations(tribunalOffice)).thenReturn(fileLocations);
        }

        return fileLocationService;
    }

    private DynamicValueType createSelectedListItemAtIndex(TribunalOffice tribunalOffice, int index) {
        return DynamicValueType.create(tribunalOffice.getOfficeNumber() + index,
                tribunalOffice.getOfficeName() + index);
    }
}
