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
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeC;
import uk.gov.hmcts.ethos.replacement.docmosis.constants.ET1ReppedConstants;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Et1ReppedHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1ReppedService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/et1Repped")
public class Et1ReppedController {
    private static final String GENERATED_DOCUMENT_URL = "Please download the draft ET1 from : ";
    private final CaseActionsForCaseWorkerController caseActionsForCaseWorkerController;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final Et1ReppedService et1ReppedService;
    private final FeatureToggleService featureToggleService;
    private final Et1SubmissionService et1SubmissionService;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;

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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = et1ReppedService.validatePostcode(caseData,
                ccdRequest.getCaseDetails().getCaseTypeId());
        return getCallbackRespEntityErrors(errors, caseData);
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = List.of(ET1ReppedConstants.TRIAGE_ERROR_MESSAGE);
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et1ReppedHelper.setCreateDraftData(caseData, ccdRequest.getCaseDetails().getCaseId());
        caseData.setSearchCriteria(null);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/createCase/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "create case submitted callback handler for ET1 repped journey")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> createCaseSubmitted(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken) throws IOException {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        et1ReppedService.assignCaseAccess(ccdRequest.getCaseDetails(), userToken);
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantSex());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionOne/validateHearingPreferences", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for hearing preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateHearingPreferences(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        errors.addAll(Et1ReppedHelper.validateSingleOption(caseData.getHearingContactLanguage()));
        errors.addAll(Et1ReppedHelper.validateSingleOption(caseData.getClaimantHearingContactLanguage()));
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        errors.addAll(Et1ReppedHelper.validateSingleOption(caseData.getRepresentativeContactPreference()));
        errors.addAll(Et1ReppedHelper.validateSingleOption(caseData.getContactLanguageQuestion()));
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

        Et1ReppedHelper.setEt1SectionStatuses(ccdRequest);
        et1ReppedService.addClaimantRepresentativeDetails(ccdRequest.getCaseDetails().getCaseData(), userToken);
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getDidClaimantWorkForOrg());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantWorking", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant working question")
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantStillWorking());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantWrittenNoticePeriod", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant written notice period question")
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantStillWorkingNoticePeriod());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantWorkingNoticePeriod", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant working notice period question")
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantWorkingNoticePeriod());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantNoLongerWorking", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant no longer working question")
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantNoLongerWorking());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantPay", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant pay question")
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantPayType());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantPensionBenefits", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant pension benefits question")
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = new ArrayList<>();
        errors.addAll(Et1ReppedHelper.validateSingleOption(caseData.getClaimantPensionContribution()));
        errors.addAll(Et1ReppedHelper.validateSingleOption(caseData.getClaimantEmployeeBenefits()));
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantNewJob", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant new job question")
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantNewJob());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/validateClaimantNewJobPay", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for claimant new job pay question")
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getClaimantNewJobPayPeriod());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionTwo/generateRespondentPreamble", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for generating respondent preamble label")
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et1ReppedHelper.generateRespondentPreamble(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/sectionTwo/generateWorkAddressLabel", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for generating work address label")
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

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Et1ReppedHelper.generateWorkAddressLabel(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/sectionCompleted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for section completed")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> sectionCompleted(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(caseData)
                .confirmation_body(
                        Et1ReppedHelper.getSectionCompleted(caseData, ccdRequest.getCaseDetails().getCaseId()))
                .build());
    }

    @PostMapping(value = "/sectionThree/validateWhistleblowing", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for validating whistleblowing question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateWhistleblowing(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getWhistleblowingYesNo());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/sectionThree/validateLinkedCases", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for validating LinkedCases question")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateLinkedCases(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors  = Et1ReppedHelper.validateSingleOption(caseData.getLinkedCasesYesNo());
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/submitClaim", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for ET1 Submission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submitClaim(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        Et1ReppedHelper.setEt1SubmitData(caseData);
        et1ReppedService.addDefaultData(caseDetails.getCaseTypeId(), caseData);
        et1ReppedService.addClaimantRepresentativeDetails(caseData, userToken);
        caseActionsForCaseWorkerController.postDefaultValues(ccdRequest, userToken);
        if (featureToggleService.isEt1DocGenEnabled()) {
            caseData.setRequiresSubmissionDocuments(YES);
        } else {
            et1SubmissionService.createAndUploadEt1Docs(caseDetails, userToken);
        }
        et1SubmissionService.sendEt1ConfirmationMyHmcts(caseDetails, userToken);
        Et1ReppedHelper.clearEt1ReppedCreationFields(caseData);
        caseData = nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/submitClaim/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for ET1 Submission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> submitClaimAboutToStart(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        if (!Et1ReppedHelper.allSectionsCompleted(caseData)) {
            errors.add("Please complete all sections before submitting the claim");
        }

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for ET1 Submitted page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> et1ReppedSubmitted(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) throws IOException {

        caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(ccdRequest.getCaseDetails());

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_header("<h1>You have submitted the ET1 claim</h1>")
                .confirmation_body("""
                                   <h3>What happens next</h3>
                                   
                                   The tribunal will send you updates as the claim progresses.
                                   """)
                .build());
    }

    @PostMapping(value = "/createDraftEt1", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for creating the draft ET1")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> createDraftEt1(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        Et1ReppedHelper.setEt1SubmitData(caseDetails.getCaseData());
        et1ReppedService.addDefaultData(caseDetails.getCaseTypeId(), caseDetails.getCaseData());
        et1ReppedService.addClaimantRepresentativeDetails(caseDetails.getCaseData(), userToken);
        et1ReppedService.createDraftEt1(ccdRequest.getCaseDetails(), userToken);
        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    @PostMapping(value = "/createDraftEt1Submitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Show Draft ET1 download link.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> createDraftEt1Submitted(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .confirmation_body(GENERATED_DOCUMENT_URL + ccdRequest.getCaseDetails().getCaseData().getDocMarkUp()
                       + "\n\n"
                       + Et1ReppedHelper.getSectionCompleted(ccdRequest.getCaseDetails().getCaseData(),
                        ccdRequest.getCaseDetails().getCaseId()))
                .build());
    }

    @PostMapping(value = "/validateGrounds", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for validating grounds for a claim")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> validateGrounds(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = Et1ReppedHelper.validateGrounds(caseData);
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/generateDocuments", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "callback handler for generating docs on ET1 submission")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> generateDocuments(
            @RequestBody CCDRequest ccdRequest, @RequestHeader("Authorization") String userToken) {

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        if (featureToggleService.isEt1DocGenEnabled()) {
            et1SubmissionService.createAndUploadEt1Docs(caseDetails, userToken);
            caseDetails.getCaseData().setRequiresSubmissionDocuments(null);
        }

        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    /**
     * Sets existing values from claimant representative to case data model {@link CaseData}.
     *
     * @param ccdRequest generic request from CCD
     * @param userToken  authentication token to verify the user
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToStartAmendClaimantRepresentativeContact",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @Operation(summary = "Loads RepresentedTypeC model values to case data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStartAmendClaimantRepresentativeContact(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        try {
            Et1ReppedHelper.loadClaimantRepresentativeValues(caseData);
        } catch (GenericServiceException gse) {
            errors.add(gse.getMessage());
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }

    /**
     * Sets new values to respondent representative model {@link RepresentedTypeC}.
     *
     * @param ccdRequest generic request from CCD
     * @param userToken  authentication token to verify the user
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/midEventAmendClaimantRepresentativeContact", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates RepresentedTypeC model of the claimant representative with new values")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midEventAmendClaimantRepresentativeContact(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        try {
            if (REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS
                    .equals(caseData.getRepresentativeContactChangeOption())) {
                et1ReppedService.setMyHmctsOrganisationAddress(userToken, caseData);
            }
        } catch (GenericServiceException gse) {
            errors.add(gse.getMessage());
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }

    /**
     * Sets new values to respondent representative model {@link RepresentedTypeC}.
     *
     * @param ccdRequest generic request from CCD
     * @param userToken  authentication token to verify the user
     * @return Callback response entity with case data attached.
     */
    @PostMapping(value = "/aboutToSubmitAmendClaimantRepresentativeContact",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates RepresentedTypeC model of the claimant representative with new values")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitAmendClaimantRepresentativeContact(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        try {
            et1ReppedService.setClaimantRepresentativeValues(userToken, caseData);
            caseData.setMyHmctsAddressText(null);
        } catch (GenericServiceException gse) {
            errors.add(gse.getMessage());
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }
}
