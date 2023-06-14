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

public class FileLocationSelectionServiceTest {

    private final TribunalOffice tribunalOffice = TribunalOffice.ABERDEEN;

    @Test
    public void testInitialiseFileLocationNoFileLocationSelected() {
        FileLocationService fileLocationService = mockFileLocationService();
        CaseData caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);

        FileLocationSelectionService fileLocationSelectionService = new FileLocationSelectionService(
            fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(caseData.getFileLocation(), "fileLocation",
                "File Location ");
    }

    @Test
    public void testInitialiseFileLocationWithFileLocationSelected() {
        FileLocationService fileLocationService = mockFileLocationService();
        CaseData caseData = SelectionServiceTestUtils.createCaseData(tribunalOffice);
        DynamicValueType selectedFileLocation = DynamicValueType.create("fileLocation2", "File Location 2");
        caseData.setFileLocation(DynamicFixedListType.of(selectedFileLocation));

        FileLocationSelectionService fileLocationSelectionService = new FileLocationSelectionService(
            fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(caseData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(caseData.getFileLocation(), "fileLocation",
                "File Location ", selectedFileLocation);
    }

    @Test
    public void testInitialiseFileLocationMultipleDataNoFileLocationSelected() {
        FileLocationService fileLocationService = mockFileLocationService();
        CaseData multipleData = SelectionServiceTestUtils.createCaseData(tribunalOffice);

        FileLocationSelectionService fileLocationSelectionService = new FileLocationSelectionService(
            fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(multipleData);

        SelectionServiceTestUtils.verifyDynamicFixedListNoneSelected(multipleData.getFileLocation(),
            "fileLocation", "File Location ");
    }

    @Test
    public void testInitialiseFileLocationMultipleDataWithFileLocationSelected() {
        FileLocationService fileLocationService = mockFileLocationService();
        MultipleData multipleData = SelectionServiceTestUtils.createMultipleData(tribunalOffice.getOfficeName());
        DynamicValueType selectedFileLocation = DynamicValueType.create("fileLocation2", "File Location 2");
        multipleData.setFileLocation(DynamicFixedListType.of(selectedFileLocation));

        FileLocationSelectionService fileLocationSelectionService = new FileLocationSelectionService(
            fileLocationService);
        fileLocationSelectionService.initialiseFileLocation(multipleData);

        SelectionServiceTestUtils.verifyDynamicFixedListSelected(multipleData.getFileLocation(), "fileLocation",
                "File Location ", selectedFileLocation);
    }

    private FileLocationService mockFileLocationService() {
        FileLocationService fileLocationService = mock(FileLocationService.class);
        List<DynamicValueType> fileLocations = SelectionServiceTestUtils.createListItems("fileLocation",
            "File Location ");
        when(fileLocationService.getFileLocations(tribunalOffice)).thenReturn(fileLocations);

        return fileLocationService;
    }
}
