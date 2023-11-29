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
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.InitialConsiderationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

/**
 * REST controller for the ET3 Initial Consideration pages. Provides custom formatting for content
 * at the start of the event and after completion.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class InitialConsiderationController {
    private final InitialConsiderationService initialConsiderationService;
    private final DocumentManagementService documentManagementService;
    private final ReportDataService reportDataService;
    private final CaseFlagsService caseFlagsService;
    private final FeatureToggleService featureToggleService;
    private static final String COMPLETE_IC_HDR = "<h1>Initial consideration complete</h1>";

    @PostMapping(value = "/completeInitialConsideration", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "completes the Initial Consideration flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> completeInitialConsideration(@RequestBody CCDRequest ccdRequest,
                                                                            @RequestHeader("Authorization")
                                                                                String userToken) {
        log.info("Initial consideration complete requested for case reference ---> {}",
            ccdRequest.getCaseDetails().getCaseId());

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .confirmation_header(COMPLETE_IC_HDR)
            .build());
    }

    /**
     * About to Submit callback which handle the submission of data from the event into CCD and generates a PDF.
     * @param ccdRequest Holds the request and case data
     * @param userToken Used for authorisation
     * @return caseData in ccdRequest
     */
    @PostMapping(value = "/submitInitialConsideration", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Handles Initial Consideration Submission")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> submitInitialConsideration(@RequestBody CCDRequest ccdRequest,
                                                                          @RequestHeader("Authorization")
                                                                                  String userToken) {
        log.info("INITIAL CONSIDERATION ABOUT TO SUBMIT ---> {}", ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        initialConsiderationService.clearHiddenValue(caseData, ccdRequest.getCaseDetails().getCaseTypeId());
        caseData.setIcCompletedBy(reportDataService.getUserFullName(userToken));
        caseData.setIcDateCompleted(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        DocumentInfo documentInfo = initialConsiderationService.generateDocument(caseData, userToken,
                ccdRequest.getCaseDetails().getCaseTypeId());
        caseData.setEtInitialConsiderationDocument(documentManagementService.addDocumentToDocumentField(documentInfo));

        if (featureToggleService.isHmcEnabled()) {
            caseFlagsService.setPrivateHearingFlag(caseData);
        }

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/startInitialConsideration", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "start the Initial Consideration flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<CCDCallbackResponse> startInitialConsideration(@RequestBody CCDRequest ccdRequest,
                                                                         @RequestHeader("Authorization")
                                                                             String userToken) {
        log.info("START OF INITIAL CONSIDERATION FOR CASE ---> {}", ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        caseData.setEtInitialConsiderationRespondent(
            initialConsiderationService.getRespondentName(caseData.getRespondentCollection()));
        caseData.setEtInitialConsiderationHearing(
            initialConsiderationService.getHearingDetails(caseData.getHearingCollection()));

        String caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();

        caseData.setEtInitialConsiderationJurisdictionCodes(
            initialConsiderationService.generateJurisdictionCodesHtml(caseData.getJurCodesCollection(), caseTypeId));
        initialConsiderationService.setIsHearingAlreadyListed(caseData, caseTypeId);

        return getCallbackRespEntityNoErrors(caseData);
    }
}
