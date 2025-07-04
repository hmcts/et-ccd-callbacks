package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.prehearingdeposit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.prehearingdeposit.PreHearingDepositService;

import java.io.IOException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin/preHearingDeposit")
@RequiredArgsConstructor
@Slf4j
public class PreHearingDepositController {
    private final VerifyTokenService verifyTokenService;
    private final PreHearingDepositService preHearingDepositService;

    @PostMapping(value = "/importPHRDeposits", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Import Pre-Hearing deposit Data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = AdminData.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> importPHRDeposits(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        try {
            preHearingDepositService.importPreHearingDepositData(
                    adminData.getPreHearingDepositImportFile(), userToken);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to import pre-hearing deposit data", e);
        }

        return CCDCallbackResponse.getCallbackRespEntityNoErrors(adminData);
    }

    @PostMapping(value = "/createPHRDeposit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Pre-Hearing deposit Data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = AdminData.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<PreHearingDepositCallbackResponse> createPHRDeposit(
            @RequestHeader("Authorization") String userToken,
            @RequestBody PreHearingDepositRequest ccdRequest) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        PreHearingDepositData preHearingDepositData = ccdRequest.getCaseDetails().getCaseData();
        return PreHearingDepositCallbackResponse.getCallbackRespEntityNoErrors(preHearingDepositData);
    }
}
