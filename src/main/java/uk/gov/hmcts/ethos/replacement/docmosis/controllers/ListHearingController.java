package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.ScotlandVenueSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.hearings.allocatehearing.VenueSelectionService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@RestController
@Slf4j
public class ListHearingController {

    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final VenueSelectionService venueSelectionService;
    private final ScotlandVenueSelectionService scotlandVenueSelectionService;

    public ListHearingController(VerifyTokenService verifyTokenService, VenueSelectionService venueSelectionService,
                                 ScotlandVenueSelectionService scotlandVenueSelectionService) {
        this.verifyTokenService = verifyTokenService;
        this.venueSelectionService = venueSelectionService;
        this.scotlandVenueSelectionService = scotlandVenueSelectionService;
    }

    @PostMapping("/initialiseHearings")
    @Operation(summary = "Initialise data for Listing Hearings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initialiseHearings(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        log.info("/initialiseHearings");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        try {
            log.info("/initialiseHearings case type id " + caseTypeId);
            if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
                venueSelectionService.initHearingCollection(caseData);
            } else if (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
                scotlandVenueSelectionService.initHearingCollection(caseData);
            } else {
                throw new IllegalArgumentException("Unexpected case type id " + caseTypeId);
            }
        } catch (Exception e) {
            log.error("/initialiseHearings error", e);
            throw e;
        }

        return getCallbackRespEntityNoErrors(caseData);
    }
}
