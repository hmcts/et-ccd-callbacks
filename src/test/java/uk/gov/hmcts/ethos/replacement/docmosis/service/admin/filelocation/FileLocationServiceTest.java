package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.FileLocation;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({"PMD.TooManyMethods"})
class FileLocationServiceTest {

    private FileLocationRepository fileLocationRepository;
    private static final String FILE_LOCATION_CODE = "testCode";
    private static final String FILE_LOCATION_NAME = "testName";
    private static final String TRIBUNAL_OFFICE = "Aberdeen";
    private FileLocation fileLocation;
    private AdminData adminData;
    private FileLocationService fileLocationService;

    @BeforeEach
    void setup() {
        fileLocationRepository = mock(FileLocationRepository.class);
        adminData = createAdminData();
        fileLocation = createFileLocation(adminData);
        fileLocationService = new FileLocationService(fileLocationRepository);
    }

    @Test
    void shouldSaveFileLocation() {
        when(fileLocationRepository.existsByCodeAndTribunalOffice(FILE_LOCATION_CODE,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(false);
        when(fileLocationRepository.existsByNameAndTribunalOffice(FILE_LOCATION_NAME,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(false);

        assertDoesNotThrow(() -> fileLocationService.saveFileLocation(adminData));
        verify(fileLocationRepository, times(1)).save(fileLocation);
    }

    @Test
    void shouldReturnErrorIfFileLocationWithSameCodeAndOfficeExists() {
        when(fileLocationRepository.existsByCodeAndTribunalOffice(FILE_LOCATION_CODE,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(true);
        assertThrows(SaveFileLocationException.class, () -> fileLocationService.saveFileLocation(adminData));
        verify(fileLocationRepository, never()).save(fileLocation);
    }

    @Test
    void shouldReturnErrorIfFileLocationWithSameNameAndOfficeExists() {
        when(fileLocationRepository.existsByCodeAndTribunalOffice(FILE_LOCATION_CODE,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(false);
        when(fileLocationRepository.existsByNameAndTribunalOffice(FILE_LOCATION_NAME,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(true);
        assertThrows(SaveFileLocationException.class, () -> fileLocationService.saveFileLocation(adminData));
        verify(fileLocationRepository, never()).save(fileLocation);
    }

    @Test
    void shouldUpdateFileLocation() {
        when(fileLocationRepository.existsByCodeAndTribunalOffice(FILE_LOCATION_CODE,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(false);
        when(fileLocationRepository.existsByNameAndTribunalOffice(FILE_LOCATION_NAME,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(false);
        when(fileLocationRepository.findByCodeAndTribunalOffice(FILE_LOCATION_CODE,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(fileLocation);

        List<String> errors = fileLocationService.updateFileLocation(adminData);
        verify(fileLocationRepository, times(1)).save(fileLocation);
        assertEquals(0, errors.size());
    }

    @Test
    void shouldDeleteFileLocation() {
        when(fileLocationRepository.existsByCodeAndTribunalOffice(FILE_LOCATION_CODE,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(false);
        when(fileLocationRepository.existsByNameAndTribunalOffice(FILE_LOCATION_NAME,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(false);
        when(fileLocationRepository.findByCodeAndTribunalOffice(FILE_LOCATION_CODE,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(fileLocation);

        List<String> errors = fileLocationService.deleteFileLocation(adminData);
        verify(fileLocationRepository, times(1)).delete(fileLocation);
        assertEquals(0, errors.size());
    }

    @Test
    void shouldUpdateFileLocation_ReturnFileLocationNameAndOfficeConflictError() {
        when(fileLocationRepository.existsByNameAndTribunalOffice(FILE_LOCATION_NAME,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(true);
        when(fileLocationRepository.findByCodeAndTribunalOffice(FILE_LOCATION_CODE,
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE))).thenReturn(fileLocation);

        List<String> errors = fileLocationService.updateFileLocation(adminData);
        verify(fileLocationRepository, times(0)).save(fileLocation);
        assertEquals(1, errors.size());
        assertEquals("A file location with the same Name (testName) and Tribunal Office (Aberdeen) already exists.",
                errors.get(0));
    }

    @Test
    void midEventSelectOffice_shouldReturnDynamicList() {
        when(fileLocationRepository.findByTribunalOfficeOrderByNameAsc(
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE)))
                .thenReturn(List.of(fileLocation));

        List<String> errors = fileLocationService.midEventSelectTribunalOffice(adminData);
        assertEquals(0, errors.size());
        assertEquals(1, adminData.getFileLocationList().getListItems().size());
    }

    @Test
    void midEventSelectOffice_shouldGiveFileLocationNotFoundByTribunalOffice() {

        when(fileLocationRepository.findByTribunalOfficeOrderByNameAsc(
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE)))
                .thenReturn(null);

        List<String> errors = fileLocationService.midEventSelectTribunalOffice(adminData);
        assertEquals(1, errors.size());
        assertEquals("No file location found in the Aberdeen office", errors.get(0));
    }

    @Test
    void midEventSelectFileLocation_shouldReturnFileLocation() {
        when(fileLocationRepository.findByCodeAndTribunalOffice(
                adminData.getFileLocationCode(),
                TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice())))
                .thenReturn(fileLocation);

        List<String> errors = fileLocationService.midEventSelectFileLocation(adminData);

        assertEquals(0, errors.size());
        assertEquals("testCode", adminData.getFileLocationCode());
        assertEquals("testName", adminData.getFileLocationName());
    }

    @Test
    void midEventSelectFileLocation_shouldGiveFileLocationNotFoundByFileLocationCode() {
        when(fileLocationRepository.findByTribunalOfficeOrderByNameAsc(
                TribunalOffice.valueOfOfficeName(TRIBUNAL_OFFICE)))
                .thenReturn(null);

        List<String> errors = fileLocationService.midEventSelectFileLocation(adminData);

        assertEquals(1, errors.size());
        assertEquals("No file location found with the testCode location code", errors.get(0));
    }

    private AdminData createAdminData() {
        AdminData adminData = new AdminData();
        adminData.setFileLocationCode(FILE_LOCATION_CODE);
        adminData.setFileLocationName(FILE_LOCATION_NAME);
        adminData.setTribunalOffice(TRIBUNAL_OFFICE);
        List<DynamicValueType> fileLocationDynamicList = new ArrayList<>();
        DynamicValueType dynamicValueType = DynamicValueType.create(FILE_LOCATION_CODE, FILE_LOCATION_NAME);
        fileLocationDynamicList.add(dynamicValueType);
        DynamicFixedListType fileLocationDynamicFixedList = new DynamicFixedListType();
        fileLocationDynamicFixedList.setListItems(fileLocationDynamicList);
        adminData.setFileLocationList(fileLocationDynamicFixedList);
        adminData.getFileLocationList().setValue(dynamicValueType);
        return adminData;
    }

    private FileLocation createFileLocation(AdminData adminData) {
        var fileLocation =  new FileLocation();
        fileLocation.setCode(adminData.getFileLocationCode());
        fileLocation.setName(adminData.getFileLocationName());
        fileLocation.setTribunalOffice(TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice()));
        return fileLocation;
    }

    @Test
    void testInitImport() {
        var adminData = createAdminData();
        fileLocationService.initAdminData(adminData);

        assertNull(adminData.getFileLocationCode());
        assertNull(adminData.getFileLocationName());
        assertNull(adminData.getTribunalOffice());
    }

}
