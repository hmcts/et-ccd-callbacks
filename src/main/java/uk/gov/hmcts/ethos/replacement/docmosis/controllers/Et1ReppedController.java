package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1ReppedHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService.INVALID_TOKEN;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/et1Repped")
public class Et1ReppedController {
    private final VerifyTokenService verifyTokenService;
    private final Et1ReppedService et1ReppedService;
    private static final String TRIAGE_ERROR_MESSAGE = """
            The postcode you entered is not included under the early adopter sites yet. Please use the ET1 claim form
            linked on this page or copy the following into your browser:
            https://www.claim-employment-tribunals.service.gov.uk/
            """;

    /**
     * Callback to handle postcode validation for the ET1 Repped journey.
     * @param ccdRequest the request
     * @param userToken the user token
     * @return the response entity
     * @throws InvalidPostcodeException if the postcode is invalid
     */
    @PostMapping(value = "/createCase/validatePostcode", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "About to start callback handler for ET1 repped journey")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validatePostcode(@RequestBody
                                                                CCDRequest ccdRequest,
                                                                @RequestHeader("Authorization") String userToken)
            throws InvalidPostcodeException {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setEt1ReppedTriageYesNo(et1ReppedService.validatePostcode(caseData));
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/createCase/officeError", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Error page callback handler for ET1 repped journey")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> officeError(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = List.of(TRIAGE_ERROR_MESSAGE);
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/createCase/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "about to submit callback handler for ET1 repped journey")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et1ReppedHelper.setCreateDraftData(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/sectionOne/validateClaimantSex", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant sex")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantSex(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantSex());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionOne/validateClaimantSupport", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant support question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantSupport(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantSupportQuestion());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionOne/validateRepresentativeInformation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for representative information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateRepresentativeInformation(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getRepresentativeContactPreference());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/aboutToSubmitSection", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for submitting each section")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitSection(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        String eventId = ccdRequest.getEventId();
        Et1ReppedHelper.setEt1SectionStatuses(caseData, eventId);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantWorked", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantWorked(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getDidClaimantWorkForOrg());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantWorking", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantWorking(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantStillWorking());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantWrittenNoticePeriod", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantWrittenNoticePeriod(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantStillWorkingNoticePeriod());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantWorkingNoticePeriod", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantWorkingNoticePeriod(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantWorkingNoticePeriod());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantNoLongerWorking", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantNoLongerWorking(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantNoLongerWorking());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantPay", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantPay(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantPayType());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantPensionBenefits", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantPensionBenefits(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = new ArrayList<>();
        errors.addAll(Et1ReppedHelper.validateSingleOption(caseData.getClaimantPensionContribution()));
        errors.addAll(Et1ReppedHelper.validateSingleOption(caseData.getClaimantEmployeeBenefits()));
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantNewJob", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantNewJob(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantPensionContribution());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantNewJobPay", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateClaimantNewJobPay(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantNewJobPayPeriod());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/generateRespondentPreamble", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> generateRespondentPreamble(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et1ReppedHelper.generateRespondentPreamble(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/sectionTwo/generateWorkAddressLabel", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant worked question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> generateWorkAddressLabel(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et1ReppedHelper.generateWorkAddressLabel(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }
}
