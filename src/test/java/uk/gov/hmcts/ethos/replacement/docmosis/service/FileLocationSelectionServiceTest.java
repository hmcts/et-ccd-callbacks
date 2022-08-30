package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Test;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.SelectionServiceTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.FileLocationService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileLocationSelectionServiceTest {

    private final TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;

    @Test
    public void testInitialiseFileLocationNoFileLocationSelected() {
        var fileLocationService = mockFileLocationService();
        var caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);

        var fileLocationSelectionService = new FileLocationSelectionService(fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getFileLocation(), "fileLocation",
                "File Location ");
    }

    @Test
    public void testInitialiseFileLocationWithFileLocationSelected() {
        var fileLocationService = mockFileLocationService();
        var caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);
        var selectedFileLocation = DynamicValueType.create("fileLocation2", "File Location 2");
        caseData.setFileLocation(DynamicFixedListType.of(selectedFileLocation));

        var fileLocationSelectionService = new FileLocationSelectionService(fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getFileLocation(), "fileLocation",
                "File Location ", selectedFileLocation);
    }

    @Test
    public void testInitialiseFileLocationMultipleDataNoFileLocationSelected() {
        var fileLocationService = mockFileLocationService();
        var multipleData = SelectionServiceTestUtils.createCaseData(tribunalOffice);

        var fileLocationSelectionService = new FileLocationSelectionService(fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(multipleData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(multipleData.getFileLocation(), "fileLocation",
                "File Location ");
    }

    @Test
    public void testInitialiseFileLocationMultipleDataWithFileLocationSelected() {
        var fileLocationService = mockFileLocationService();
        var multipleData = SelectionServiceTestUtils.createMultipleData(tribunalOffice.getOfficeName());
        var selectedFileLocation = DynamicValueType.create("fileLocation2", "File Location 2");
        multipleData.setFileLocation(DynamicFixedListType.of(selectedFileLocation));

        var fileLocationSelectionService = new FileLocationSelectionService(fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(multipleData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(multipleData.getFileLocation(), "fileLocation",
                "File Location ", selectedFileLocation);
    }

    private FileLocationService mockFileLocationService() {
        var fileLocationService = mock(FileLocationService.class);
        var fileLocations = SelectionServiceTestUtils.createListItems("fileLocation", "File Location ");
        when(fileLocationService.getFileLocations(tribunalOffice)).thenReturn(fileLocations);

        return fileLocationService;
    }
}
