package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.BFHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.HearingsHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.UploadDocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicDepositOrder;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicJudgements;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicRespondentRepresentative;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicRestrictedReporting;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.letters.InvalidCharacterCheck;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AddSingleCaseToMultipleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseCloseValidator;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseCreationForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseRetrievalForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseUpdateForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ClerkService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ConciliationTrackService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DepositOrderValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1SubmissionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FixCaseApiService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.JudgmentValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ScotlandFileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SingleCaseMultipleMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SingleReferenceService;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ABOUT_TO_SUBMIT_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLOSED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MID_EVENT_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REJECTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SUBMITTED_CALLBACK;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntity;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EMPTY_STRING;

@Slf4j
@RequiredArgsConstructor
@RestController
public class CaseActionsForCaseWorkerController {
    private static final String LOG_MESSAGE = "received notification request for case reference :    ";
    private static final String EVENT_FIELDS_VALIDATION = "Event fields validation: ";
    private static final String TWO_HUNDRED = "200";
    private static final String FOUR_HUNDRED = "400";
    private static final String FIVE_HUNDRED = "500";
    private static final String SUBMIT_CASE_DRAFT = "SUBMIT_CASE_DRAFT";
    public static final String ACCESSED_SUCCESSFULLY = "Accessed successfully";
    public static final String BAD_REQUEST = "Bad Request";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    private static final List<String> SUBMISSION_EVENTS = List.of(SUBMIT_CASE_DRAFT, "initiateCase", "submitEt1Draft");

    private final CaseCloseValidator caseCloseValidator;
    private final CaseCreationForCaseWorkerService caseCreationForCaseWorkerService;
    private final CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;
    private final CaseUpdateForCaseWorkerService caseUpdateForCaseWorkerService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;
    private final ConciliationTrackService conciliationTrackService;
    private final DefaultValuesReaderService defaultValuesReaderService;
    private final FixCaseApiService fixCaseApiService;
    private final SingleReferenceService singleReferenceService;
    private final EventValidationService eventValidationService;
    private final SingleCaseMultipleMidEventValidationService singleCaseMultipleMidEventValidationService;
    private final AddSingleCaseToMultipleService addSingleCaseToMultipleService;
    private final ClerkService clerkService;
    private final FileLocationSelectionService fileLocationSelectionService;
    private final ScotlandFileLocationSelectionService scotlandFileLocationSelectionService;
    private final DepositOrderValidationService depositOrderValidationService;
    private final JudgmentValidationService judgmentValidationService;
    private final Et1VettingService et1VettingService;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;
    private final FeatureToggleService featureToggleService;
    private final CaseFlagsService caseFlagsService;
    private final CaseManagementLocationService caseManagementLocationService;
    private final Et1SubmissionService et1SubmissionService;
    private final NocRespondentHelper nocRespondentHelper;

    @PostMapping(value = "/createCase", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "create a case for a caseWorker.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> createCase(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("CREATE CASE ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        SubmitEvent submitEvent = caseCreationForCaseWorkerService.caseCreationRequest(ccdRequest, userToken);
        log.info("Case created correctly with case Id: " + submitEvent.getCaseId());

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/retrieveCase", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "retrieve a case for a caseWorker.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    @Deprecated
    public ResponseEntity<CCDCallbackResponse> retrieveCase(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("RETRIEVE CASE ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        SubmitEvent submitEvent = caseRetrievalForCaseWorkerService.caseRetrievalRequest(userToken,
                ccdRequest.getCaseDetails().getCaseTypeId(),
                ccdRequest.getCaseDetails().getJurisdiction(), "1550576532211563");
        log.info("Case received correctly with id: " + submitEvent.getCaseId());

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/retrieveCases", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "retrieve cases for a caseWorker.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> retrieveCases(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("RETRIEVE CASES ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        List<SubmitEvent> submitEvents = caseRetrievalForCaseWorkerService.casesRetrievalRequest(ccdRequest, userToken);
        log.info("Cases received: " + submitEvents.size());
        submitEvents.forEach(submitEvent -> log.info(String.valueOf(submitEvent.getCaseId())));

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/updateCase", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update a case for a caseWorker.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> updateCase(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("UPDATE CASE ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        SubmitEvent submitEvent = caseUpdateForCaseWorkerService.caseUpdateRequest(ccdRequest, userToken);
        log.info("Case updated correctly with id: " + submitEvent.getCaseId());

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/preDefaultValues", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update pre default values in a case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> preDefaultValues(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("PRE DEFAULT VALUES ---> " + LOG_MESSAGE);

        ccdRequest.getCaseDetails().getCaseData().setClaimantTypeOfClaimant(
                defaultValuesReaderService.getClaimantTypeOfClaimant());

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/postDefaultValues", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update the case with some default values after submitted.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> postDefaultValues(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("POST DEFAULT VALUES ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();

        List<String> errors = eventValidationService.validateReceiptDate(caseDetails);

        if (errors.isEmpty()) {
            defaultValuesReaderService.setSubmissionReference(caseDetails);
            DefaultValues defaultValues = getPostDefaultValues(caseDetails);
            defaultValuesReaderService.setCaseData(caseData, defaultValues);
            caseManagementForCaseWorkerService.caseDataDefaults(caseData);
            generateEthosCaseReference(caseData, ccdRequest);
            FlagsImageHelper.buildFlagsImageFileName(ccdRequest.getCaseDetails());
            caseData.setMultipleFlag(caseData.getEcmCaseType() != null
                    && caseData.getEcmCaseType().equals(MULTIPLE_CASE_TYPE) ? YES : NO);
            UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseData);
            UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseData);
            DocumentHelper.setDocumentNumbers(caseData);
            Helper.removeSpacesFromPartyNames(caseData);
            //create NOC answers section only on case submission events
            if (SUBMISSION_EVENTS.contains(defaultIfEmpty(ccdRequest.getEventId(), EMPTY_STRING))) {
                caseData = nocRespondentRepresentativeService.prepopulateOrgPolicyAndNoc(caseData);
            }
            defaultValuesReaderService.setPositionAndOffice(caseDetails.getCaseTypeId(), caseData);

            boolean caseFlagsToggle = featureToggleService.isCaseFlagsEnabled();
            log.info("Caseflags feature flag is {}", caseFlagsToggle);
            if (caseFlagsToggle && caseFlagsService.caseFlagsSetupRequired(caseData)) {
                caseFlagsService.setupCaseFlags(caseData);
            }

            boolean hmcToggle = featureToggleService.isHmcEnabled();
            log.info("HMC feature flag is {}", hmcToggle);
            if (hmcToggle) {
                caseManagementForCaseWorkerService.setPublicCaseName(caseData);
                caseManagementLocationService.setCaseManagementLocationCode(caseData);
            }

            if (featureToggleService.citizenEt1Generation() && SUBMIT_CASE_DRAFT.equals(ccdRequest.getEventId())) {
                caseDetails.setCaseData(caseData);
                et1SubmissionService.createAndUploadEt1Docs(caseDetails, userToken);
                et1SubmissionService.sendEt1ConfirmationClaimant(caseDetails, userToken);
            }
        }

        log.info("PostDefaultValues for case: {} {}", ccdRequest.getCaseDetails().getCaseTypeId(),
                caseData.getEthosCaseReference());

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/addServiceId", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Add HMCTSServiceId to supplementary_data on a case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> addServiceId(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) throws IOException {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        caseManagementForCaseWorkerService.setHmctsServiceIdSupplementary(ccdRequest.getCaseDetails());
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/initialiseAmendCaseDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initialise case data for amendCaseDetails and amendCaseDetailsClosed events")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> initialiseAmendCaseDetails(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        clerkService.initialiseClerkResponsible(caseData);

        if (ENGLANDWALES_CASE_TYPE_ID.equals(ccdRequest.getCaseDetails().getCaseTypeId())) {
            fileLocationSelectionService.initialiseFileLocation(caseData);
        } else if (SCOTLAND_CASE_TYPE_ID.equals(ccdRequest.getCaseDetails().getCaseTypeId())) {
            scotlandFileLocationSelectionService.initialiseFileLocation(caseData);
        }

        et1VettingService.populateHearingVenue(caseData);
        et1VettingService.populateSuggestedHearingVenues(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/amendCaseDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "amend the case details for a single case and validates receipt date.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> amendCaseDetails(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("AMEND CASE DETAILS ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        List<String> errors = eventValidationService.validateReceiptDate(caseDetails);

        if (!eventValidationService.validateCaseState(caseDetails)) {
            errors.add(caseData.getEthosCaseReference() + " Case has not been Accepted.");
        }

        if (!eventValidationService.validateCurrentPosition(caseDetails)) {
            errors.add("To set the current position to 'Case closed' "
                    + "and to close the case, please take the Close Case action.");
        }

        if (errors.isEmpty()) {
            DefaultValues defaultValues = getPostDefaultValues(caseDetails);
            log.info("Post Default values loaded: " + defaultValues);
            defaultValuesReaderService.setCaseData(caseData, defaultValues);
            caseManagementForCaseWorkerService.dateToCurrentPosition(caseData);
            caseManagementForCaseWorkerService.setEt3ResponseDueDate(caseData);
            caseManagementForCaseWorkerService.setNextListedDate(caseData);
            FlagsImageHelper.buildFlagsImageFileName(ccdRequest.getCaseDetails());
            UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseData);
            UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseData);
            String caseTypeId = caseDetails.getCaseTypeId();
            addSingleCaseToMultipleService.addSingleCaseToMultipleLogic(
                    userToken, caseData, caseTypeId,
                    caseDetails.getJurisdiction(),
                    caseDetails.getCaseId(), errors);

            if (featureToggleService.isWorkAllocationEnabled() && caseTypeId.equals(SCOTLAND_CASE_TYPE_ID)) {
                caseManagementLocationService.setCaseManagementLocation(caseData);
            }
            Helper.removeSpacesFromPartyNames(caseData);
        }

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/migrateCaseLinkDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "amends the case link details of a transferred single case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
                content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> migrateCaseLinkDetails(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("MIGRATE CASE LINK DETAILS ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        caseManagementForCaseWorkerService.setMigratedCaseLinkDetails(userToken,
                ccdRequest.getCaseDetails());
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/migrateCaseTTLDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "amends the case TTL details of a single case with/in draft state.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
                content = { @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
                }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> migrateCaseTTLDetails(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) throws IOException {
        log.info("MIGRATE CASE TTL DETAILS ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        caseManagementForCaseWorkerService.setMigratedCaseTtlDetails(userToken, ccdRequest);
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/amendClaimantDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "amend the case claimant details for a single case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> amendClaimantDetails(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("AMEND CLAIMANT DETAILS ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        FlagsImageHelper.buildFlagsImageFileName(ccdRequest.getCaseDetails());
        if (featureToggleService.isGlobalSearchEnabled()) {
            caseManagementForCaseWorkerService.setCaseNameHmctsInternal(caseData);
        }
        caseManagementForCaseWorkerService.claimantDefaults(caseData);

        if (featureToggleService.isHmcEnabled()) {
            caseManagementForCaseWorkerService.setPublicCaseName(caseData);
        }

        caseFlagsService.setupCaseFlags(caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        Helper.removeSpacesFromPartyNames(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/amendRespondentDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "amend respondent details for a single case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> amendRespondentDetails(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("AMEND RESPONDENT DETAILS ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = eventValidationService.validateActiveRespondents(caseData);
        if (errors.isEmpty()) {
            errors = eventValidationService.validateET3ResponseFields(caseData);
            if (errors.isEmpty()) {
                errors = InvalidCharacterCheck.checkNamesForInvalidCharacters(caseData, "respondent");
            }
            if (errors.isEmpty()) {
                caseManagementForCaseWorkerService.continuingRespondent(ccdRequest);
                caseManagementForCaseWorkerService.struckOutRespondents(ccdRequest);
            }
        }

        eventValidationService.validateMaximumSize(caseData).ifPresent(errors::add);

        if (errors.isEmpty() && !isEmpty(caseData.getRepCollection())) {
            //Needed to keep the respondent names in the rep collection sync
            nocRespondentHelper.amendRespondentNameRepresentativeNames(caseData);
        }

        if (featureToggleService.isGlobalSearchEnabled()) {
            caseManagementForCaseWorkerService.setCaseNameHmctsInternal(caseData);
        }

        if (featureToggleService.isHmcEnabled()) {
            caseManagementForCaseWorkerService.setPublicCaseName(caseData);
        }

        caseFlagsService.setupCaseFlags(caseData);

        caseManagementForCaseWorkerService.updateWorkAllocationField(errors, caseData);
        Helper.removeSpacesFromPartyNames(caseData);

        log.info(EVENT_FIELDS_VALIDATION + errors);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/amendRespondentRepresentative", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "amend respondent representative for a single case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> amendRespondentRepresentative(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("AMEND RESPONDENT REPRESENTATIVE ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        List<String> errors = eventValidationService.validateRespRepNames(caseData);

        if (errors.isEmpty()) {
            // add org policy and NOC elements
            nocRespondentHelper.updateWithRespondentIds(caseData);
            caseData = nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, userToken);

            if (featureToggleService.isHmcEnabled()) {
                nocRespondentRepresentativeService.updateNonMyHmctsOrgIds(caseData.getRepCollection());
            }
        }

        log.info(EVENT_FIELDS_VALIDATION + errors);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping("/amendRespondentRepSubmitted")
    @Operation(summary = "processes notice of change update after amending respondent representatives")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public void amendRespondentRepSubmitted(@RequestBody CallbackRequest callbackRequest) {
        log.info("AMEND RESPONDENT REPRESENTATIVE SUBMITTED ---> "
            + LOG_MESSAGE + callbackRequest.getCaseDetails().getCaseId());
        try {
            nocRespondentRepresentativeService.updateRepresentativesAccess(callbackRequest);
        } catch (IOException e) {
            throw new CcdInputOutputException("Failed to update respondent representatives accesses", e);
        }
    }

    @PostMapping(value = "/dynamicRespondentRepresentativeNames", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populates the respondents names into a dynamic list")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> dynamicRespondentRepresentativeNames(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("DYNAMIC RESPONDENT REPRESENTATIVE NAMES ---> " + LOG_MESSAGE
                + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DynamicRespondentRepresentative.dynamicRespondentRepresentativeNames(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/updateHearing", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update hearing details for a single case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> updateHearing(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("UPDATE HEARING ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        FlagsImageHelper.buildFlagsImageFileName(caseDetails);
        caseManagementForCaseWorkerService.setNextListedDate(caseDetails.getCaseData());
        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }

    @PostMapping(value = "/allocateHearing", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update postponed date when allocating a hearing.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> allocateHearing(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("ALLOCATE HEARING ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        Helper.updatePostponedDate(caseData);
        caseManagementForCaseWorkerService.setNextListedDate(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/restrictedCases", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "change restricted reporting for a single case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> restrictedCases(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("RESTRICTED CASES ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        FlagsImageHelper.buildFlagsImageFileName(ccdRequest.getCaseDetails());
        eventValidationService.validateRestrictedReportingNames(caseData);

        if (featureToggleService.isHmcEnabled()) {
            caseManagementForCaseWorkerService.setPublicCaseName(caseData);
            caseFlagsService.setPrivateHearingFlag(caseData);
        }

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/dynamicRestrictedReporting", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populates a dynamic list for restricted reporting")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> dynamicRestrictedReporting(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("DYNAMIC RESTRICTED REPORTING ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DynamicRestrictedReporting.dynamicRestrictedReporting(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/amendHearing", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "amend hearing details for a single case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> amendHearing(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) throws IOException {
        log.info("AMEND HEARING ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseManagementForCaseWorkerService.amendHearing(caseData, ccdRequest.getCaseDetails().getCaseTypeId());
        caseManagementForCaseWorkerService.setNextListedDate(caseData);

        if (featureToggleService.isMul2Enabled()) {
            caseManagementForCaseWorkerService.setNextListedDateOnMultiple(ccdRequest.getCaseDetails());
        }
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/midEventAmendHearing", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "mid event amend hearing details for a single case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> midEventAmendHearing(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("MID EVENT AMEND HEARING ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        caseManagementForCaseWorkerService.midEventAmendHearing(
                caseData, errors);
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/amendCaseState", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "amend the case state for a single case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> amendCaseState(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("AMEND CASE STATE ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        List<String> errors = new ArrayList<>();
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        if (ccdRequest.getCaseDetails().getState().equals(CLOSED_STATE)) {
            eventValidationService.validateJurisdictionOutcome(caseData,
                    ccdRequest.getCaseDetails().getState().equals(REJECTED_STATE), false, errors);
            log.info(EVENT_FIELDS_VALIDATION + errors);
        }

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/midRespondentAddress", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populates the mid dynamic fixed list with the respondent addresses.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> midRespondentAddress(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("MID RESPONDENT ADDRESS ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = Helper.midRespondentAddress(ccdRequest.getCaseDetails().getCaseData());

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/jurisdictionValidation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "validates jurisdiction entries to prevent duplicates.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> jurisdictionValidation(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("JURISDICTION VALIDATION ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        List<String> errors = new ArrayList<>();
        CaseData caseData =  ccdRequest.getCaseDetails().getCaseData();
        eventValidationService.validateJurisdiction(caseData, errors);
        log.info(EVENT_FIELDS_VALIDATION + errors);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/addAmendJurisdiction", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "amend jurisdiction details for a single case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> addAmendJurisdiction(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("AMEND JURISDICTION ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        conciliationTrackService.populateConciliationTrackForJurisdiction(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/midRespondentECC", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populates the mid dynamic list with the respondent names.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> midRespondentECC(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("MID RESPONDENT ECC ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        List<String> errors = new ArrayList<>();
        CaseData caseData = caseManagementForCaseWorkerService.createECC(ccdRequest.getCaseDetails(),
                userToken, errors, MID_EVENT_CALLBACK);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/createECC", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "create a new Employer Contract Claim.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> createECC(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("CREATE ECC ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        List<String> errors = new ArrayList<>();
        CaseData caseData = caseManagementForCaseWorkerService.createECC(
                ccdRequest.getCaseDetails(), userToken, errors, ABOUT_TO_SUBMIT_EVENT_CALLBACK);
        generateEthosCaseReference(caseData, ccdRequest);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/linkOriginalCaseECC", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "send an update to the original case with the new ECC reference created to link it.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> linkOriginalCaseECC(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("LINK ORIGINAL CASE ECC ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        List<String> errors = new ArrayList<>();
        CaseData caseData = caseManagementForCaseWorkerService.createECC(ccdRequest.getCaseDetails(),
                userToken, errors, SUBMITTED_CALLBACK);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/singleCaseMultipleMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "validates the multiple and sub multiple in the single case when moving to a multiple.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> singleCaseMultipleMidEventValidation(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("SINGLE CASE MULTIPLE MID EVENT VALIDATION ---> " + LOG_MESSAGE
                + ccdRequest.getCaseDetails().getCaseId());

        List<String> errors = new ArrayList<>();
        CaseDetails caseDetails = ccdRequest.getCaseDetails();

        et1VettingService.populateSuggestedHearingVenues(caseDetails.getCaseData());

        singleCaseMultipleMidEventValidationService.singleCaseMultipleValidationLogic(
                userToken, caseDetails, errors);

        return getCallbackRespEntity(errors, caseDetails);
    }

    @PostMapping(value = "/hearingMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "validates the hearing number and the hearing days to prevent their creation.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> hearingMidEventValidation(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("HEARING MID EVENT VALIDATION ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        List<String> errors = HearingsHelper.hearingMidEventValidation(caseDetails.getCaseData());
        return getCallbackRespEntity(errors, caseDetails);
    }

    @PostMapping(value = "/dynamicListBfActions", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populate bf actions in dynamic lists.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> dynamicListBfActions(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("DYNAMIC LIST BF ACTIONS ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        BFHelper.populateDynamicListBfActions(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/bfActions", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "updates the dateEntered by the user with the current date.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> bfActions(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("BF ACTIONS ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        BFHelper.updateBfActionItems(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/dynamicJudgments", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populates the dynamic lists for judgements")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> dynamicJudgementList(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("DYNAMIC JUDGEMENT LIST ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DynamicJudgements.dynamicJudgements(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/judgementSubmitted", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populates the dynamic lists for judgements")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> judgementSubmitted(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) throws ParseException {
        log.info("JUDGEMENT SUBMITTED ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        judgmentValidationService.validateJudgments(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/judgmentValidation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "validates jurisdiction codes within judgement collection.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> judgmentValidation(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("JUDGEMENT VALIDATION ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData =  ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = eventValidationService.validateJurisdictionCodesWithinJudgement(caseData);
        errors.addAll(eventValidationService.validateJudgementDates(caseData));
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/depositValidation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "validates deposit amount and deposit refunded.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> depositValidation(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("DEPOSIT VALIDATION ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData =  ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = depositOrderValidationService.validateDepositOrder(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/dynamicDepositOrder", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populates the respondents names into a dynamic list")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> dynamicDepositOrder(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("DYNAMIC DEPOSIT ORDER ---> " + LOG_MESSAGE
                + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DynamicDepositOrder.dynamicDepositOrder(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/aboutToStartDisposal", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update the position type to case closed.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStartDisposal(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("ABOUT TO START DISPOSAL ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        List<String> errors = new ArrayList<>();
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();

        errors = eventValidationService.validateCaseBeforeCloseEvent(caseData,
                ccdRequest.getCaseDetails().getState().equals(REJECTED_STATE), false, errors);

        if (errors.isEmpty()) {
            String caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();
            if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
                fileLocationSelectionService.initialiseFileLocation(caseData);
            } else if (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
                scotlandFileLocationSelectionService.initialiseFileLocation(caseData);
            }

            clerkService.initialiseClerkResponsible(caseData);
            Helper.updatePositionTypeToClosed(caseData);
            return getCallbackRespEntityNoErrors(caseData);
        }

        log.info(EVENT_FIELDS_VALIDATION + errors);
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/amendFixCaseAPI", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "amend case details in Fix Case API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> amendFixCaseAPI(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("FIX CASE API VALUE ---> " + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        fixCaseApiService.checkUpdateMultipleReference(ccdRequest.getCaseDetails(), userToken);

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/reinstateClosedCaseMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "validates position type when reinstate closed case.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = TWO_HUNDRED, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = FOUR_HUNDRED, description = BAD_REQUEST),
        @ApiResponse(responseCode = FIVE_HUNDRED, description = INTERNAL_SERVER_ERROR)
    })
    public ResponseEntity<CCDCallbackResponse> reinstateClosedCaseMidEventValidation(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info("REINSTATE CLOSED CASE MID EVENT VALIDATION ---> "
                + LOG_MESSAGE + ccdRequest.getCaseDetails().getCaseId());

        CaseData caseData =  ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = caseCloseValidator.validateReinstateClosedCaseMidEvent(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
    }

    private DefaultValues getPostDefaultValues(CaseDetails caseDetails) {
        return defaultValuesReaderService.getDefaultValues(caseDetails.getCaseData().getManagingOffice());
    }

    private void generateEthosCaseReference(CaseData caseData, CCDRequest ccdRequest) {
        if (StringUtils.isBlank(caseData.getEthosCaseReference())) {
            String reference = singleReferenceService.createReference(ccdRequest.getCaseDetails().getCaseTypeId());
            log.info(String.format("Created reference %s for CCD case %s", reference,
                    ccdRequest.getCaseDetails().getCaseId()));
            caseData.setEthosCaseReference(reference);
        }
    }
}
