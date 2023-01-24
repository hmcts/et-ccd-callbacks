package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.venues;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.excelimport.fixedlistsheetreader.FixedListSheetReaderException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.venues.VenueImportService;

import java.io.IOException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin/venue")
@RequiredArgsConstructor
@Slf4j
public class VenueImportController {

    private final VerifyTokenService verifyTokenService;
    private final VenueImportService venueImportService;

    @PostMapping(value = "/initImport", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initialise import venue data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initImport(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        venueImportService.initImport(adminData);

        return CCDCallbackResponse.getCallbackRespEntityNoErrors(adminData);
    }

    @PostMapping(value = "/import", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Import venue data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> importFile(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) throws FixedListSheetReaderException, IOException {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        venueImportService.importVenues(adminData, userToken);

        return CCDCallbackResponse.getCallbackRespEntityNoErrors(adminData);
    }
}
