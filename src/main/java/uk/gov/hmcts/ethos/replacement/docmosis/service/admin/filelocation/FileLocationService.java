package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.FileLocation;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;

@Service
@Slf4j
public class FileLocationService {
    private final FileLocationRepository fileLocationRepository;

    public static final String ADD_FILE_LOCATION_CODE_AND_OFFICE_CONFLICT_ERROR =
            "A file location with the same Code (%s) and Tribunal Office (%s) already exists.";

    public static final String ADD_FILE_LOCATION_NAME_AND_OFFICE_CONFLICT_ERROR =
            "A file location with the same Name (%s) and Tribunal Office (%s) already exists.";

    public FileLocationService(FileLocationRepository fileLocationRepository) {
        this.fileLocationRepository = fileLocationRepository;
    }

    public void saveFileLocation(AdminData adminData) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice());

        var fileLocation =  new FileLocation();
        fileLocation.setCode(adminData.getFileLocationCode());
        fileLocation.setName(adminData.getFileLocationName());
        fileLocation.setTribunalOffice(tribunalOffice);

        if (fileLocationRepository.existsByCodeAndTribunalOffice(adminData.getFileLocationCode(), tribunalOffice)) {
            throw new SaveFileLocationException(String.format(ADD_FILE_LOCATION_CODE_AND_OFFICE_CONFLICT_ERROR,
                    adminData.getFileLocationCode(), adminData.getTribunalOffice()));
        } else if (fileLocationRepository.existsByNameAndTribunalOffice(adminData.getFileLocationName(), tribunalOffice)) {
            throw new SaveFileLocationException(String.format(ADD_FILE_LOCATION_NAME_AND_OFFICE_CONFLICT_ERROR,
                    adminData.getFileLocationName(), adminData.getTribunalOffice()));
        } else {
            fileLocationRepository.save(fileLocation);
        }
    }

    public void initAdminData(AdminData adminData) {
        adminData.setFileLocationCode(null);
        adminData.setFileLocationName(null);
        adminData.setTribunalOffice(null);
    }
}
