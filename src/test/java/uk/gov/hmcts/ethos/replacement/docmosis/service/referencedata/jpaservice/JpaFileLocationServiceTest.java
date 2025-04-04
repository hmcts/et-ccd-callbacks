package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.FileLocation;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class JpaFileLocationServiceTest {

    @Test
    void testGetFileLocations() {
        TribunalOffice tribunalOffice = TribunalOffice.BRISTOL;
        List<FileLocation> fileLocations = List.of(
                createFileLocation("old location", "z location"),
                createFileLocation("fileLocation1", "FileLocation 1"),
                createFileLocation("fileLocation2", "FileLocation 2"),
                createFileLocation("fileLocation3", "FileLocation 3"),
                createFileLocation("A Location", "A Location"));
        FileLocationRepository fileLocationRepository = mock(FileLocationRepository.class);
        when(fileLocationRepository.findByTribunalOffice(tribunalOffice)).thenReturn(fileLocations);

        JpaFileLocationService fileLocationService = new JpaFileLocationService(fileLocationRepository);
        List<DynamicValueType> values = fileLocationService.getFileLocations(tribunalOffice);

        assertEquals(5, values.size());
        // Results should be sorted in alphabetical order by label
        verifyValue(values.get(0), "A Location", "A Location");
        verifyValue(values.get(1), "fileLocation1", "FileLocation 1");
        verifyValue(values.get(2), "fileLocation2", "FileLocation 2");
        verifyValue(values.get(3), "fileLocation3", "FileLocation 3");
        verifyValue(values.get(4), "old location", "z location");
    }

    private FileLocation createFileLocation(String code, String name) {
        FileLocation fileLocation = new FileLocation();
        fileLocation.setCode(code);
        fileLocation.setName(name);
        return fileLocation;
    }

    private void verifyValue(DynamicValueType value, String expectedCode, String expectedLabel) {
        assertEquals(expectedCode, value.getCode());
        assertEquals(expectedLabel, value.getLabel());
    }
}
