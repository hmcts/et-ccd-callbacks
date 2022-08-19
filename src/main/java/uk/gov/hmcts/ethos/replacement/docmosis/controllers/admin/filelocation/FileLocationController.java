package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.filelocation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.FileLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.SaveFileLocationException;
import java.util.Arrays;
import java.util.List;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * FileLocationController is the controller layer for managing File Locations
 * Most of the methods in this class are being used by frontend which is called
 * EX-UI.
 *
 * @author  TEAM: James Turnbull, Cindy Chan, Harpreet Jhita, Mehmet Tahir Dede
 */
@RestController
@RequestMapping("/admin/filelocation")
@RequiredArgsConstructor
@SuppressWarnings({"PMD.LawOfDemeter"})
public class FileLocationController {

    /**
     * Verify Token Service injection.
     */
    private final VerifyTokenService verifyTokenService;
    /**
     * File Location Service injection.
     */
    private final FileLocationService fileLocationService;


    /**
     * This service Gets userToken as a parameter for security validation
     * and ccdRequest data which has adminData as an object.
     * Initializes AdminData to null values to not show any existing values for
     * both the creation and update of file locations.
     *
     * @param  userToken        Used for authorization
     *
     * @param ccdRequest        AdminData which is a generic data type for most of the
     *                          methods which holds file location code, file location name
     *                          and tribunal office.
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes adminData
     */
    @PostMapping(value = "/initAdminData", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initialise file location data to null values")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initAdminData(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        fileLocationService.initAdminData(adminData);

        return CCDCallbackResponse.getCallbackRespEntityNoErrors(adminData);
    }

    /**
     * This service Gets userToken as a parameter for security validation
     * and ccdRequest data which has adminData as an object.
     * Creates a FileLocation object to be saved to file_location table in et_cos
     * schema. In case throws one of two errors.
     * One for the same file location code and Tribunal office
     * and one for the same file location name and Tribunal office
     *
     * @param  userToken        Used for authorisation
     *
     * @param ccdRequest        AdminData which is a generic data type for most of the
     *                          methods which holds file location code, file location name
     *                          and tribunal office.
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes adminData
     */
    @PostMapping(value = "/addFileLocation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Add File Location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
             content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
             }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> addFileLocation(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        try {
            fileLocationService.saveFileLocation(adminData);
        } catch (SaveFileLocationException e) {
            return CCDCallbackResponse.getCallbackRespEntityErrors(Arrays.asList(e.getMessage()), adminData);
        }

        return CCDCallbackResponse.getCallbackRespEntityNoErrors(adminData);
    }

    /**
     * This service Gets userToken as a parameter for security validation
     * and ccdRequest data which has adminData as an object.
     * It is used to populate file location list according to selected tribunal office.
     * Returns a list of errors and file locations.
     * There may only be one type of error in the returned errors list which is
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_TRIBUNAL_OFFICE defined as
     * "There is not any file location found in the %s office"
     *
     * @param  userToken        Used for authorisation
     *
     * @param ccdRequest        AdminData which is a generic data type for most of the
     *                          methods which holds file location code, file location name
     *                          and tribunal office.
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes adminData with a list of file locations
     */
    @PostMapping(value = "/midEventSelectTribunalOffice", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the dynamicList for file location when tribunal office selected")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midEventSelectTribunalOffice(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = fileLocationService.midEventSelectTribunalOffice(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    /**
     * This service Gets userToken as a parameter for security validation
     * and ccdRequest data which has adminData as an object.
     * It is used to populate file location name and file location code
     * according to selected tribunal office. Returns a list of errors.
     * For this method there may only be one type of error which is
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE defined as
     * "There is not any file location found with the %s location code"
     *
     * @param  userToken        Used for authorisation
     *
     * @param ccdRequest        AdminData which is a generic data type for most of the
     *                          methods which holds file location code, file location name
     *                          and tribunal office.
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes adminData with a list of file locations
     */
    @PostMapping(value = "/midEventSelectFileLocation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates File Location Code and File Location Name according to selected Tribunal Office")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midEventSelectFileLocation(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = fileLocationService.midEventSelectFileLocation(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    /**
     * This service Gets userToken as a parameter for security validation
     * and ccdRequest data which has adminData as an object.
     * It is used to update file location name for the selected file location code
     * Returns a list of errors. For this method there may be one of two errors which are
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_TRIBUNAL_OFFICE defined as
     * "There is not any file location found in the %s office"
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE defined as
     * "There is not any file location found with the %s location code"
     *
     * @param  userToken        Used for authorisation
     *
     * @param ccdRequest        AdminData which is a generic data type for most of the
     *                          methods which holds file location code, file location name
     *                          and tribunal office.
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes adminData with a list of file locations
     */
    @PostMapping(value = "/updateFileLocation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update file location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> updateFileLocation(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = fileLocationService.updateFileLocation(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

}
