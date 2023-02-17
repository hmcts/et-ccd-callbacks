package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReportDataService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import java.time.LocalDate;
import java.util.List;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntity;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RestController
@RequiredArgsConstructor
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement", "PMD.LawOfDemeter", "PDM.FieldNamingConventions"})
public class Et1VettingController {
    public static final String PROCESSING_COMPLETE_TEXT = "<hr><h2>Do this next</h2>"
        + "<p>You must <a href=\"/cases/case-details/%s/trigger/preAcceptanceCase/preAcceptanceCase1\">"
        + "accept or reject the case</a> or refer the case.</p>";

    private final VerifyTokenService verifyTokenService;
    private final Et1VettingService et1VettingService;
    private final DocumentManagementService documentManagementService;
    private final ReportDataService reportDataService;

    /**
     * Initialise ET1 case vetting.
     * Gets userToken as a parameter for security validation and ccdRequest data which has caseData as an object.
     * @param userToken Used for authorisation
     * @param ccdRequest CaseData which is a generic data type for most of the methods which holds ET1 case data
     * @return caseData in ccdRequest
     */
    @PostMapping(value = "/initialiseEt1Vetting", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initialise case vetting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initialiseEt1Vetting(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("CASE VETTING ABOUT TO START ---> " + ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        et1VettingService.initialiseEt1Vetting(caseDetails);

        CaseData caseData = caseDetails.getCaseData();
        List<JurCodesTypeItem> jurCodesCollection = caseData.getJurCodesCollection();
        if (jurCodesCollection != null) {
            caseData.setExistingJurisdictionCodes(
                et1VettingService.generateJurisdictionCodesHtml(jurCodesCollection));
        }

        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Mid callback event in the Jurisdiction code page to set the track allocation upon the case as long based
     * upon jurisdiction codes, also validates the jurisdiction code that caseworker has added against the existing
     * codes to prevent duplicate entries and populates the tribunal/office location fields based on managing
     * office location.
     * @param userToken Used for authorisation
     * @param ccdRequest CaseData which is a generic data type for most of the methods which holds ET1 case data
     * @return errors from the jurisdiction code validation (if there is any) and caseData in ccdRequest
     */
    @PostMapping(value = "/jurisdictionCodes", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Validate Jurisdiction Codes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> jurisdictionCodes(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
            @RequestBody CCDRequest ccdRequest) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = et1VettingService.validateJurisdictionCodes(caseData);
        if (errors.isEmpty() && caseData.getJurCodesCollection() != null) {
            caseData.setTrackAllocation(et1VettingService.populateEt1TrackAllocationHtml(caseData));
        }

        et1VettingService.populateTribunalOfficeFields(caseData);

        return getCallbackRespEntity(errors, ccdRequest.getCaseDetails());
    }

    /**
     * Mid callback event in the hearing venue page to set the information of the claimant and respondents, the location
     * of the tribunal, and popular the dynamic list of hearing venues for that tribunal location.
     * @param userToken Used for authorisation
     * @param ccdRequest CaseData which is a generic data type for most of the methods which holds ET1 case data
     * @return caseData in ccdRequest
     */
    @PostMapping(value = "/et1HearingVenue", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Return listing details and hearing venues.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> et1HearingVenue(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
        @RequestBody CCDRequest ccdRequest) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        caseData.setEt1AddressDetails(et1VettingService.getAddressesHtml(caseData));
        et1VettingService.populateHearingVenue(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * About to Submit callback which handle the submission of data from the event into CCD and also generates a PDF
     * of the ET1 Vetting data.
     * @param userToken Used for authorisation
     * @param ccdRequest CaseData which is a generic data type for most of the methods which holds ET1 case data
     * @return caseData in ccdRequest
     */
    @PostMapping(value = "/et1VettingAboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Generates the PDF for ET1 Vetting.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> et1VettingAboutToSubmit(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken,
            @RequestBody CCDRequest ccdRequest) {
        log.info("ET1 CASE VETTING ABOUT TO SUBMIT ---> {}", ccdRequest.getCaseDetails().getCaseId());
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setEt1VettingCompletedBy(reportDataService.getUserFullName(userToken));
        caseData.setEt1DateCompleted(LocalDate.now().toString());
        DocumentInfo documentInfo = et1VettingService.generateEt1VettingDocument(caseData, userToken,
                ccdRequest.getCaseDetails().getCaseTypeId());
        caseData.setEt1VettingDocument(documentManagementService.addDocumentToDocumentField(documentInfo));
        caseData.setSuggestedHearingVenues(caseData.getEt1HearingVenues());
        return getCallbackRespEntityNoErrors(caseData);
    }

    /**
     * Return the next-steps that the user must carry out after the ET1 vetting process.
     * @param ccdRequest CaseData which is a generic data type for most of the methods which holds ET1 case data
     * @param userToken Used for authorisation
     * @return this will return and display a message to the user regarding the next steps.
     */
    @PostMapping(value = "/finishEt1Vetting", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "display the next steps after ET1 Vetting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> processingComplete(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        String caseNumber = ccdRequest.getCaseDetails().getCaseId();

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(ccdRequest.getCaseDetails().getCaseData())
            .confirmation_body(String.format(PROCESSING_COMPLETE_TEXT, caseNumber))
            .build());
    }
}