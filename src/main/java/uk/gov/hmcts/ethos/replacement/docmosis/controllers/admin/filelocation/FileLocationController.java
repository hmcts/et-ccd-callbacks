package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.filelocation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.FileLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.filelocation.SaveFileLocationException;

import java.util.Arrays;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin/filelocation")
@RequiredArgsConstructor
public class FileLocationController {

    private final VerifyTokenService verifyTokenService;
    private final FileLocationService fileLocationService;

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

    @PostMapping(value = "/initAdminData", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initialise file location data to null values")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accessed successfully",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
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

}
