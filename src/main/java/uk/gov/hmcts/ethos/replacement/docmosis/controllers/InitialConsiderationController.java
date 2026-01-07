package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.InitialConsiderationHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.InitialConsiderationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.MONTH_STRING_DATE_FORMAT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.setDocumentNumbers;


/**
 * REST controller for the ET3 Initial Consideration pages. Provides custom formatting for content
 * at the start of the event and after completion.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
public class InitialConsiderationController {

    private final VerifyTokenService verifyTokenService;
    private final InitialConsiderationService initialConsiderationService;
    private final DocumentManagementService documentManagementService;
    private final ReportDataService reportDataService;
    private final CaseFlagsService caseFlagsService;
    private final FeatureToggleService featureToggleService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private static final String INVALID_TOKEN = "Invalid Token {}";
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

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

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

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        initialConsiderationService.processIcDocumentCollections(caseData);

        caseData.setIcCompletedBy(reportDataService.getUserFullName(userToken));
        caseData.setIcDateCompleted(LocalDate.now().format(DateTimeFormatter.ofPattern(MONTH_STRING_DATE_FORMAT)));
        DocumentInfo documentInfo = initialConsiderationService.generateDocument(caseData, userToken,
                ccdRequest.getCaseDetails().getCaseTypeId());
        caseData.setEtInitialConsiderationDocument(documentManagementService.addDocumentToDocumentField(documentInfo));
        InitialConsiderationHelper.addToDocumentCollection(caseData);

        if (featureToggleService.isHmcEnabled()) {
            caseFlagsService.setPrivateHearingFlag(caseData);
        }

        setDocumentNumbers(caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);

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

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setEtICHearingListedAnswers(null);
        initialConsiderationService.clearOldEtICHearingListedAnswersValues(caseData);
        initialConsiderationService.clearIcHearingNotListedOldValues(caseData);
        initialConsiderationService.clearHiddenValue(caseData);

        // Sets the respondent details(respondent ET1 and ET3 names, hearing panel preference, and
        // availability for video hearing) of all respondents in a concatenated string format
        caseData.setEtInitialConsiderationRespondent(initialConsiderationService.setRespondentDetails(caseData));

        //hearing details
        HearingsHelper.setEtInitialConsiderationListedHearingType(caseData);
        caseData.setEtInitialConsiderationHearing(initialConsiderationService.getHearingDetails(
                    caseData.getHearingCollection(), ccdRequest.getCaseDetails().getCaseTypeId()));

        //Parties' panel preference in a table
        caseData.setEtIcPartiesHearingPanelPreference(
                initialConsiderationService.setPartiesHearingPanelPreferenceDetails(caseData));

        //Parties' Hearing Format in a table
        caseData.setEtIcPartiesHearingFormat(
                initialConsiderationService.setPartiesHearingFormatDetails(caseData));

        //JurCodes
        String caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();
        caseData.setEtInitialConsiderationJurisdictionCodes(initialConsiderationService.generateJurisdictionCodesHtml(
                        caseData.getJurCodesCollection(), caseTypeId));

        initialConsiderationService.initialiseInitialConsideration(ccdRequest.getCaseDetails());

        if (CollectionUtils.isNotEmpty(caseData.getEtICHearingNotListedList())) {
            initialConsiderationService.mapOldIcHearingNotListedOptionsToNew(caseData, caseTypeId);
        }

        // ET1 Vetting Issues
        caseData.setIcEt1VettingIssuesDetail(initialConsiderationService.setIcEt1VettingIssuesDetails(caseData));

        // ET3 Vetting Issues
        caseData.setIcEt3ProcessingIssuesDetail(
                initialConsiderationService.setIcEt3VettingIssuesDetailsForEachRespondent(caseData));

        caseData.setRegionalOffice(caseData.getRegionalOfficeList() != null
                ? caseData.getRegionalOfficeList().getSelectedLabel() : null);
        caseData.setEt1TribunalRegion(caseData.getEt1HearingVenues() != null
                ? caseData.getEt1HearingVenues().getSelectedLabel() : null);

        return getCallbackRespEntityNoErrors(caseData);
    }
}
