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
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CaseFlagsDataMigrationController {
    private static final String LOG_MESSAGE = "received notification request for case reference :    ";
    private final CaseFlagsService caseFlagsService;

    @PostMapping(value = "/case-flags-migration/about-to-submit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update existing cases with default values for case flags.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> addCaseFlagsData(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("Migrating existing case Id for case flags ---> "
                + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseFlagsService.setupCaseFlags(caseData);
        log.info("Migrating existing case: {} for claimant: {},  respondent: {},",
                ccdRequest.getCaseDetails().getCaseTypeId(),
                caseData.getClaimantFlags().getPartyName(),
                caseData.getRespondentFlags().getPartyName());

        return getCallbackRespEntityErrors(List.of(), caseData);
    }

    @PostMapping(value = "/case-flags-rollback/about-to-submit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "rollback default values for case flags.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                content = {@Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> rollbackCaseFlagsData(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("Rolling back existing case Id for case flags ---> "
                + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseFlagsService.rollbackCaseFlags(caseData);

        return getCallbackRespEntityErrors(List.of(), caseData);
    }

}
