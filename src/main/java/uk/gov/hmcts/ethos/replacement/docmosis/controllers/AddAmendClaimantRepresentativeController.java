package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocClaimantRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.ClaimantRepresentativeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_REPRESENTATIVE_ORGANISATION_AND_EMAIL_NOT_MATCHED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrorsAndWarnings;

/**
 * REST controller for the addAmendClaimantRepresentative event.
 */
@Slf4j
@RequestMapping("/addAmendClaimantRepresentative")
@RestController
@RequiredArgsConstructor
public class AddAmendClaimantRepresentativeController {
    private static final String LOG_MESSAGE = "received notification request for case reference : ";
    private final NocClaimantRepresentativeService nocClaimantRepresentativeService;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;

    @PostMapping(value = "/amendClaimantRepresentativeMidEvent", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "checks claimant representative's organisation and email address")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> amendClaimantRepresentativeMidEvent(
            @RequestBody CCDRequest ccdRequest) {
        CaseDataUtils.validateCCDRequest(ccdRequest);
        log.info("CHECKING CLAIMANT REPRESENTATIVE ORGANISATION ---> " + LOG_MESSAGE + "{}",
                ccdRequest.getCaseDetails().getCaseId());
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        try {
            warnings.addAll(nocClaimantRepresentativeService.validateRepresentativeOrganisationAndEmail(caseData));
        } catch (GenericRuntimeException gse) {
            String errorMessage = String.format(ERROR_REPRESENTATIVE_ORGANISATION_AND_EMAIL_NOT_MATCHED,
                    StringUtils.EMPTY);
            if (EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND.equals(gse.getMessage())) {
                errorMessage = String.format(ERROR_REPRESENTATIVE_ORGANISATION_AND_EMAIL_NOT_MATCHED,
                        EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND);
            }
            errors.add(errorMessage);
        }
        return getCallbackRespEntityErrorsAndWarnings(warnings, errors, caseData);
    }


    /**
     * AboutToSubmit for addAmendClaimantRepresentative. Sets the claimant rep's id.
     *
     * @param ccdRequest holds the request and case data
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "gives the claimant representative an id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest ccdRequest) {
        CaseDataUtils.validateCCDRequest(ccdRequest);
        List<String> errors = new ArrayList<>();
        String error = nocClaimantRepresentativeService.validateClaimantRepresentativeOrganisationMatch(
                ccdRequest.getCaseDetails());
        if (StringUtils.isNotBlank(error)) {
            errors.add(error);
        }
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        if (errors.isEmpty()) {
            ClaimantRepresentativeUtils.addAmendClaimantRepresentative(caseData);
            nocRespondentRepresentativeService.revokeRespondentRepresentativesWithSameOrganisationAsClaimant(
                    ccdRequest.getCaseDetails());
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping("/amendClaimantRepSubmitted")
    @Operation(summary = "processes notice of change update after amending claimant representatives")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public void amendClaimantRepSubmitted(
            @RequestBody CallbackRequest callbackRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken) {

        log.info("AMEND CLAIMANT REPRESENTATIVE SUBMITTED ---> " + LOG_MESSAGE + "{}",
                callbackRequest.getCaseDetails().getCaseId());
        try {
            nocClaimantRepresentativeService.updateClaimantRepAccess(callbackRequest);
        } catch (IOException e) {
            throw new CcdInputOutputException("Failed to update claimant representatives access", e);
        }
    }
}
