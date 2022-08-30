package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.FileLocation;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.FileLocationRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * FileLocationService is the service layer class of managing File Locations
 * Most of the methods in this class are being used by FileLocationController.
 *
 * @author TEAM: James Turnbull, SinMan Chan(Cindy), Harpreet Jhita, Mehmet Tahir Dede
 */
@Service
@Slf4j
public class FileLocationService {
    private final FileLocationRepository fileLocationRepository;

    public static final String ERROR_ADD_FILE_LOCATION_CODE_AND_OFFICE_CONFLICT =
            "A file location with the same Code (%s) and Tribunal Office (%s) already exists.";

    public static final String ERROR_ADD_UPDATE_FILE_LOCATION_NAME_AND_OFFICE_CONFLICT =
            "A file location with the same Name (%s) and Tribunal Office (%s) already exists.";

    public static final String ERROR_FILE_LOCATION_NOT_FOUND_BY_TRIBUNAL_OFFICE =
            "No file location found in the %s office";

    public static final String ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE =
            "No file location found with the %s location code";

    public FileLocationService(FileLocationRepository fileLocationRepository) {
        this.fileLocationRepository = fileLocationRepository;
    }

    /**
     * This method does not return anything. Gets AdminData as a parameter
     * and creates a FileLocation object to be saved to file_location table in et_cos
     * schema. Throws two errors. One for the same file location code and Tribunal office
     * and one for the same file location name and Tribunal office
     *
     * @param  adminData  AdminData which is a generic data type for most of the
     *                    methods which holds file location code, file location name
     *                    and tribunal office.
     */
    public void saveFileLocation(AdminData adminData) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice());

        FileLocation fileLocation =  new FileLocation();
        fileLocation.setCode(adminData.getFileLocationCode());
        fileLocation.setName(adminData.getFileLocationName());
        fileLocation.setTribunalOffice(tribunalOffice);

        if (fileLocationRepository.existsByCodeAndTribunalOffice(adminData.getFileLocationCode(), tribunalOffice)) {
            throw new SaveFileLocationException(String.format(ERROR_ADD_FILE_LOCATION_CODE_AND_OFFICE_CONFLICT,
                    adminData.getFileLocationCode(), adminData.getTribunalOffice()));
        } else if (fileLocationRepository.existsByNameAndTribunalOffice(adminData.getFileLocationName(),
                tribunalOffice)) {
            throw new SaveFileLocationException(String.format(ERROR_ADD_UPDATE_FILE_LOCATION_NAME_AND_OFFICE_CONFLICT,
                    adminData.getFileLocationName(), adminData.getTribunalOffice()));
        } else {
            fileLocationRepository.save(fileLocation);
        }
    }

    /**
     * This method does not return anything. Initializes AdminData to null values
     * to not show any existing values for both the creation and update of file locations
     *
     * @param  adminData  AdminData which is a generic data type for most of the
     *                    methods which holds file location code, file location name
     *                    and tribunal office and file location list.
     */
    public void initAdminData(AdminData adminData) {
        adminData.setFileLocationCode(null);
        adminData.setFileLocationName(null);
        adminData.setTribunalOffice(null);
        if (adminData.getFileLocationList() != null) {
            adminData.setFileLocationList(null);
        }
    }

    /**
     * This method is used to populate file location list according to selected tribunal office.
     * Returns a list of errors. For this method there may only be one type of error which is
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_TRIBUNAL_OFFICE defined as
     * "There is not any file location found in the %s office"
     * Gets only adminData as parameter.
     *
     * @param  adminData  AdminData which is a generic data type for most of the
     *                    methods which holds file location code, file location name
     *                    and tribunal office and file location list. Used only tribunal office
     *                    value.
     * @return errors     A list of string values that contains error definitions.
     */
    public List<String> midEventSelectTribunalOffice(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        String tribunalOffice = adminData.getTribunalOffice();
        List<FileLocation> fileLocationList = fileLocationRepository.findByTribunalOfficeOrderByNameAsc(
                TribunalOffice.valueOfOfficeName(tribunalOffice));
        if (fileLocationList == null || fileLocationList.isEmpty()) {
            errors.add(String.format(ERROR_FILE_LOCATION_NOT_FOUND_BY_TRIBUNAL_OFFICE, tribunalOffice));
            return errors;
        }
        List<DynamicValueType> fileLocationDynamicList = new ArrayList<>();
        for (FileLocation fileLocation : fileLocationList) {
            fileLocationDynamicList.add(DynamicValueType.create(fileLocation.getCode(), fileLocation.getName()));
        }

        DynamicFixedListType fileLocationDynamicFixedList = new DynamicFixedListType();
        fileLocationDynamicFixedList.setListItems(fileLocationDynamicList);
        adminData.setFileLocationList(fileLocationDynamicFixedList);
        return errors;
    }

    /**
     * This method is used to populate file location name and file location code
     * according to selected tribunal office. Returns a list of errors.
     * For this method there may only be one type of error which is
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE defined as
     * "There is not any file location found with the %s location code"
     * Gets only adminData as parameter.
     *
     * @param  adminData  AdminData which is a generic data type for most of the
     *                    methods which holds file location code, file location name
     *                    and tribunal office and file location list. Used only
     *                    file location list value.
     * @return errors     A list of string values that contains error definitions.
     */
    public List<String> midEventSelectFileLocation(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        String selectedFileLocationCode = adminData.getFileLocationList().getSelectedCode();
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice());

        FileLocation selectedFileLocation = fileLocationRepository
                .findByCodeAndTribunalOffice(selectedFileLocationCode, tribunalOffice);
        if (selectedFileLocation != null) {
            adminData.setFileLocationCode(selectedFileLocation.getCode());
            adminData.setFileLocationName(selectedFileLocation.getName());
        } else {
            errors.add(String.format(ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE, selectedFileLocationCode));
        }

        return errors;
    }

    /**
     * This method is used to update file location name for the selected file location code
     * Returns a list of errors. For this method there may be one of two errors which are
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_TRIBUNAL_OFFICE defined as
     * "There is not any file location found in the %s office"
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE defined as
     * "There is not any file location found with the %s location code"
     * Gets adminData as a parameter.
     *
     * @param  adminData  AdminData which is a generic data type for most of the
     *                    methods which holds file location code, file location name
     *                    and tribunal office and file location list.
     * @return errors     A list of string values that contains error definitions.
     */
    public List<String> updateFileLocation(AdminData adminData) {

        List<String> errors = new ArrayList<>();
        String selectedFileLocationCode = adminData.getFileLocationList().getSelectedCode();
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice());

        if (fileLocationRepository.existsByNameAndTribunalOffice(adminData.getFileLocationName(),
                tribunalOffice)) {
            errors.add(String.format(ERROR_ADD_UPDATE_FILE_LOCATION_NAME_AND_OFFICE_CONFLICT,
                    adminData.getFileLocationName(), adminData.getTribunalOffice()));
            return errors;
        }
        FileLocation selectedFileLocation = fileLocationRepository
                .findByCodeAndTribunalOffice(selectedFileLocationCode, tribunalOffice);
        if (selectedFileLocation != null) {
            selectedFileLocation.setName(adminData.getFileLocationName());
            fileLocationRepository.save(selectedFileLocation);
        } else {
            errors.add(ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE);
        }

        return errors;
    }

    /**
     * This method is used to delete file location name for the selected file location code
     * Returns a list of errors. For this method there may be one of two errors which are
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_TRIBUNAL_OFFICE defined as
     * "There is not any file location found in the %s office"
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE defined as
     * "There is not any file location found with the %s location code"
     * Gets adminData as a parameter.
     *
     * @param  adminData  AdminData which is a generic data type for most of the
     *                    methods which holds file location code, file location name
     *                    and tribunal office and file location list.
     * @return errors     A list of string values that contains error definitions.
     */
    public List<String> deleteFileLocation(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        String selectedFileLocationCode = adminData.getFileLocationList().getSelectedCode();
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice());
        FileLocation selectedFileLocation = fileLocationRepository
                .findByCodeAndTribunalOffice(selectedFileLocationCode, tribunalOffice);
        if (selectedFileLocation != null) {
            fileLocationRepository.delete(selectedFileLocation);
            fileLocationRepository.flush();
        } else {
            errors.add(ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE);
        }
        return errors;
    }
}
