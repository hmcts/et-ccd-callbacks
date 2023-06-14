package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.FileLocationService;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScotlandFileLocationSelectionServiceTest {

    @Test
    public void testInitialiseFileLocationNoFileLocationSelected() {
        FileLocationService fileLocationService = mockFileLocationService();
        CaseData caseData = new CaseData();

        ScotlandFileLocationSelectionService fileLocationSelectionService =
            new ScotlandFileLocationSelectionService(fileLocationService);
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
        FileLocationService fileLocationService = mockFileLocationService();
        CaseData caseData = new CaseData();
        DynamicValueType selectedAberdeen = createSelectedListItemAtIndex(TribunalOffice.ABERDEEN, 1);
        caseData.setFileLocationAberdeen(DynamicFixedListType.of(selectedAberdeen));
        DynamicValueType selectedDundee = createSelectedListItemAtIndex(TribunalOffice.DUNDEE, 2);
        caseData.setFileLocationDundee(DynamicFixedListType.of(selectedDundee));
        DynamicValueType selectedEdinburgh = createSelectedListItemAtIndex(TribunalOffice.EDINBURGH, 3);
        caseData.setFileLocationEdinburgh(DynamicFixedListType.of(selectedEdinburgh));
        DynamicValueType selectedGlasgow = createSelectedListItemAtIndex(TribunalOffice.GLASGOW, 3);
        caseData.setFileLocationGlasgow(DynamicFixedListType.of(selectedGlasgow));

        ScotlandFileLocationSelectionService fileLocationSelectionService =
            new ScotlandFileLocationSelectionService(fileLocationService);
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
        FileLocationService fileLocationService = mockFileLocationService();
        MultipleData caseData = new MultipleData();

        ScotlandFileLocationSelectionService fileLocationSelectionService =
            new ScotlandFileLocationSelectionService(fileLocationService);
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
        FileLocationService fileLocationService = mockFileLocationService();
        MultipleData caseData = new MultipleData();
        DynamicValueType selectedAberdeen = createSelectedListItemAtIndex(TribunalOffice.ABERDEEN, 1);
        caseData.setFileLocationAberdeen(DynamicFixedListType.of(selectedAberdeen));
        DynamicValueType selectedDundee = createSelectedListItemAtIndex(TribunalOffice.DUNDEE, 2);
        caseData.setFileLocationDundee(DynamicFixedListType.of(selectedDundee));
        DynamicValueType selectedEdinburgh = createSelectedListItemAtIndex(TribunalOffice.EDINBURGH, 3);
        caseData.setFileLocationEdinburgh(DynamicFixedListType.of(selectedEdinburgh));
        DynamicValueType selectedGlasgow = createSelectedListItemAtIndex(TribunalOffice.GLASGOW, 3);
        caseData.setFileLocationGlasgow(DynamicFixedListType.of(selectedGlasgow));

        ScotlandFileLocationSelectionService fileLocationSelectionService =
            new ScotlandFileLocationSelectionService(fileLocationService);
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
        FileLocationService fileLocationService = mock(FileLocationService.class);

        for (TribunalOffice tribunalOffice : TribunalOffice.SCOTLAND_OFFICES) {
            List<DynamicValueType> fileLocations = SelectionServiceTestUtils.createListItems(
                tribunalOffice.getOfficeNumber(), tribunalOffice.getOfficeName());
            when(fileLocationService.getFileLocations(tribunalOffice)).thenReturn(fileLocations);
        }

        return fileLocationService;
    }

    private DynamicValueType createSelectedListItemAtIndex(TribunalOffice tribunalOffice, int index) {
        return DynamicValueType.create(tribunalOffice.getOfficeNumber() + index,
                tribunalOffice.getOfficeName() + index);
    }
}
