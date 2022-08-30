package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.refdatafixes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.refdatafixes.RefDataFixesService;

@Slf4j
@RequiredArgsConstructor
@RestController
public class RefDataFixesController {
    private static final String LOG_MESSAGE = "received notification request for case reference :    ";

    private final VerifyTokenService verifyTokenService;
    private final RefDataFixesService refDataFixesService;

    @PostMapping(value = "/updateJudgesItcoReferences", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update the judges' ITCO references")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accessed successfully",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDCallbackResponse> updateJudgesItcoReferences(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("UPDATE JUDGES ITCO REFERENCES ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error("Invalid Token {}", userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        AdminData adminData = refDataFixesService.updateJudgesItcoReferences(ccdRequest.getCaseDetails(), userToken);

        return uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDCallbackResponse.getCallbackRespEntityNoErrors(adminData);
    }


    @PostMapping(value = "/insertClaimServedDate", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Insert the claim served date for existing cases")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accessed successfully",
                    content = {
                            @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
                    }),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDCallbackResponse> insertClaimServedDate(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("INSERT CLAIM SEERVED DATE ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error("Invalid Token {}", userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

       // AdminData adminData = refDataFixesService.insertClaimServedDate(ccdRequest.getCaseDetails());

        return uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDCallbackResponse.getCallbackRespEntityNoErrors(null);
    }
}
