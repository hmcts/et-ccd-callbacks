package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.io.IOException;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class GlobalSearchDataMigrationController {

    private static final String LOG_MESSAGE = "received notification request for case reference :    ";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final EventValidationService eventValidationService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @PostMapping(value = "/global-search-migration/about-to-submit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update the old cases with some default values of Global search fields.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> addGlobalSearchFieldsInCaseData(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("Migrating existing case Id for global search ---> "
                + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.setCaseNameHmctsInternal(caseData);
        caseManagementForCaseWorkerService.setCaseManagementCategory(caseData);
        caseManagementForCaseWorkerService.setCaseManagementLocation(caseData);

        log.info("Migrating existing case: {} for caseManagementCategory: {},  caseNameHmctsInternal: {},"
                        + "  caseManagementLocation: {}",
                ccdRequest.getCaseDetails().getCaseTypeId(),
                caseData.getCaseManagementCategory(),
                caseData.getCaseNameHmctsInternal(),
                caseData.getCaseManagementLocation());

        return getCallbackRespEntityErrors(List.of(), caseData);
    }

    @PostMapping(value = "/global-search-migration/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Add HMCTSServiceId to supplementary_data on exiting case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> addServiceIdForGlobalSearchInCaseData(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) throws IOException {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(ccdRequest.getCaseDetails(), userToken);
        return getCallbackRespEntityNoErrors(caseData);
    }
}
