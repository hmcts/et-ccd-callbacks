package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.staff;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.CourtWorkerService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
@SuppressWarnings({"PMD.LawOfDemeter"})
public class CourtWorkerController {

    private final VerifyTokenService verifyTokenService;
    private final CourtWorkerService courtWorkerService;

    @PostMapping(value = "/initAddCourtWorker", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initial add Court Worker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initAddCourtWorker(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("/initAddCourtWorker");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        courtWorkerService.initAddCourtWorker(adminData);

        return CCDCallbackResponse.getCallbackRespEntityNoErrors(adminData);
    }

    @PostMapping(value = "/addCourtWorker", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Add a court worker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> addCourtWorker(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("/addCourtWorker");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = courtWorkerService.addCourtWorker(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    @PostMapping(value = "/midEventCourtWorkerSelectOffice", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the dynamicList for court worker when office and type selected")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> updateCourtWorkerMidEventSelectOffice(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = courtWorkerService.getCourtWorkerMidEventSelectOffice(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    @PostMapping(value = "/midEventCourtWorkerSelectCourtWorker", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the court worker code and name when dynamicList selected")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> updateCourtWorkerMidEventSelectCourtWorker(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = courtWorkerService.getCourtWorkerMidEventSelectCourtWorker(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    @PostMapping(value = "/updateCourtWorker", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a court worker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> updateCourtWorker(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("/updateCourtWorker");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = courtWorkerService.updateCourtWorker(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    /**
     * This service Gets userToken as a parameter for security validation
     * and ccdRequest data which has adminData as an object.
     * It is used to delete court worker for the selected file location code
     * Returns a list of errors. For this method there may be one of two errors which are
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_TRIBUNAL_OFFICE defined as
     * "There is not any court worker found in the %s office"
     * ERROR_FILE_LOCATION_NOT_FOUND_BY_FILE_LOCATION_CODE defined as
     * "There is not any court worker found with the %s location code"
     *
     * @param  userToken        Used for authorisation
     *
     * @param ccdRequest        AdminData which is a generic data type for most of the
     *                          methods which holds file location code, file location name
     *                          and tribunal office.
     * @return ResponseEntity   It is an HTTPEntity response which has CCDCallbackResponse that
     *                          includes adminData with a list of file locations
     */
    @PostMapping(value = "/deleteCourtWorker", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Delete a court worker")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> deleteCourtWorker(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("/deleteCourtWorker");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = courtWorkerService.deleteCourtWorker(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }
}
