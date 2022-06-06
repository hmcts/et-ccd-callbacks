package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.FileLocation;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileLocationServiceTest {

    private FileLocationRepository fileLocationRepository;
    private final String fileLocationCode = "testCode";
    private final String fileLocationName = "testName";
    private final String tribunalOffice = "Aberdeen";
    private FileLocation fileLocation;
    private AdminData adminData;
    private FileLocationService fileLocationService;

    @BeforeEach
    void setup() {
        fileLocationRepository = mock(FileLocationRepository.class);
        adminData = createAdminData(fileLocationCode, fileLocationName, tribunalOffice);
        fileLocation = createFileLocation(adminData);
        fileLocationService = new FileLocationService(fileLocationRepository);
    }

    @Test
    void shouldSaveFileLocation() {
        when(fileLocationRepository.existsByCodeAndTribunalOffice(fileLocationCode, TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(false);
        when(fileLocationRepository.existsByNameAndTribunalOffice(fileLocationName,
                TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(false);

        assertDoesNotThrow(() -> fileLocationService.saveFileLocation(adminData));
        verify(fileLocationRepository, times(1)).save(fileLocation);
    }

    @Test
    void shouldReturnErrorIfFileLocationWithSameCodeAndOfficeExists() {
        when(fileLocationRepository.existsByCodeAndTribunalOffice(fileLocationCode,TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(true);
        assertThrows(SaveFileLocationException.class, () -> fileLocationService.saveFileLocation(adminData));
        verify(fileLocationRepository, never()).save(fileLocation);
    }

    @Test
    void shouldReturnErrorIfFileLocationWithSameNameAndOfficeExists() {
        when(fileLocationRepository.existsByCodeAndTribunalOffice(fileLocationCode,TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(false);
        when(fileLocationRepository.existsByNameAndTribunalOffice(fileLocationName,
                TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(true);
        assertThrows(SaveFileLocationException.class, () -> fileLocationService.saveFileLocation(adminData));
        verify(fileLocationRepository, never()).save(fileLocation);
    }

    private AdminData createAdminData(String fileLocationCode, String fileLocationName, String tribunalOffice) {
        AdminData adminData = new AdminData();
        adminData.setFileLocationCode(fileLocationCode);
        adminData.setFileLocationName(fileLocationName);
        adminData.setTribunalOffice(tribunalOffice);

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
        var adminData = createAdminData(fileLocationCode,fileLocationName, tribunalOffice);
        fileLocationService.initAdminData(adminData);

        assertNull(adminData.getFileLocationCode());
        assertNull(adminData.getFileLocationName());
        assertNull(adminData.getTribunalOffice());
    }

}
