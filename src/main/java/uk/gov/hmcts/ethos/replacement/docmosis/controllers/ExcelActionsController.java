package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.listing.ListingCallbackResponse;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.et.common.model.listing.ListingRequest;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultiplesHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ClerkService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.EventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ScotlandFileLocationSelectionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferOfficeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.FixMultipleCaseApiService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleAmendService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCloseEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCreationMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCreationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleDynamicListFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleHelperService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultiplePreAcceptService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleSingleMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleTransferService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleUpdateService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleUploadService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SubMultipleMidEventValidationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.SubMultipleUpdateService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getListingCallbackRespEntity;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@Slf4j
@RequiredArgsConstructor
@RestController
@SuppressWarnings({"PMD.TooManyFields", "PMD.TooManyMethods", "PMD.ExcessiveImports", "PMD.AvoidDuplicateLiterals",
    "PMD.UnnecessaryAnnotationValueElement", "PMD.LawOfDemeter", "PMD.ConfusingTernary"})
public class ExcelActionsController {

    private static final String LOG_MESSAGE = "received notification request for multiple reference : ";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final MultipleCreationService multipleCreationService;
    private final MultiplePreAcceptService multiplePreAcceptService;
    private final MultipleAmendService multipleAmendService;
    private final MultipleUpdateService multipleUpdateService;
    private final MultipleUploadService multipleUploadService;
    private final MultipleDynamicListFlagsService multipleDynamicListFlagsService;
    private final MultipleMidEventValidationService multipleMidEventValidationService;
    private final MultipleCloseEventValidationService multipleCloseEventValidationService;
    private final SubMultipleUpdateService subMultipleUpdateService;
    private final SubMultipleMidEventValidationService subMultipleMidEventValidationService;
    private final MultipleCreationMidEventValidationService multipleCreationMidEventValidationService;
    private final MultipleSingleMidEventValidationService multipleSingleMidEventValidationService;
    private final EventValidationService eventValidationService;
    private final MultipleHelperService multipleHelperService;
    private final MultipleTransferService multipleTransferService;
    private final FixMultipleCaseApiService fixMultipleCaseApiService;
    private final FileLocationSelectionService fileLocationSelectionService;
    private final ScotlandFileLocationSelectionService scotlandFileLocationSelectionService;
    private final ClerkService clerkService;

    @PostMapping(value = "/createMultiple", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> createMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("CREATE MULTIPLE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleCreationService.bulkCreationLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/amendMultiple", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> amendMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("AMEND MULTIPLE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleAmendService.bulkAmendMultipleLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/fixMultipleCaseApi", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> fixMultipleCaseApi(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("AMEND MULTIPLE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();
        multipleAmendService.bulkAmendMultipleLogic(userToken, multipleDetails, errors);
        fixMultipleCaseApiService.fixMultipleCase(userToken, multipleDetails, errors);
        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/amendMultipleAPI", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> amendMultipleAPI(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("AMEND MULTIPLE API ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .data(multipleDetails.getCaseData())
                .build());
    }

    @PostMapping(value = "/importMultiple", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> importMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("IMPORT MULTIPLE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleUploadService.bulkUploadLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/preAcceptMultiple", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> preAcceptMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("PRE ACCEPT MULTIPLE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multiplePreAcceptService.bulkPreAcceptLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/initialiseBatchUpdate", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> initialiseBatchUpdate(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleDynamicListFlagsService.populateDynamicListFlagsLogic(userToken, multipleDetails, errors);
        if (errors.isEmpty()) {
            String caseTypeId = multipleDetails.getCaseTypeId();
            if (ENGLANDWALES_BULK_CASE_TYPE_ID.equals(caseTypeId)) {
                fileLocationSelectionService.initialiseFileLocation(multipleDetails.getCaseData());
            } else if (SCOTLAND_BULK_CASE_TYPE_ID.equals(caseTypeId)) {
                scotlandFileLocationSelectionService.initialiseFileLocation(multipleDetails.getCaseData());
            }
        }

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/batchUpdate", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> batchUpdate(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("BATCH UPDATE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleUpdateService.bulkUpdateLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/dynamicListFlags", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> dynamicListFlags(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("DYNAMIC LIST FLAGS ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleDynamicListFlagsService.populateDynamicListFlagsLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/multipleMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> multipleMidEventValidation(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MULTIPLE MID EVENT VALIDATION ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleMidEventValidationService.multipleValidationLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/subMultipleMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> subMultipleMidEventValidation(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("SUB MULTIPLE MID EVENT VALIDATION ---> "
                + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        subMultipleMidEventValidationService.subMultipleValidationLogic(multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/updateSubMultiple", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> updateSubMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("UPDATE SUB MULTIPLE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        subMultipleUpdateService.subMultipleUpdateLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/multipleCreationMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> multipleCreationMidEventValidation(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MULTIPLE CREATION MID EVENT VALIDATION ---> "
                + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleCreationMidEventValidationService.multipleCreationValidationLogic(
                userToken, multipleDetails, errors, false);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/multipleAmendCaseIdsMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> multipleAmendCaseIdsMidEventValidation(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MULTIPLE AMEND CASE IDS MID EVENT VALIDATION ---> "
                + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleCreationMidEventValidationService.multipleCreationValidationLogic(
                userToken, multipleDetails, errors, true);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/multipleSingleMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> multipleSingleMidEventValidation(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MULTIPLE SINGLE MID EVENT VALIDATION ---> "
                + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleSingleMidEventValidationService.multipleSingleValidationLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/multipleMidBatch1Validation", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> multipleMidBatch1Validation(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MULTIPLE MID BATCH 1 VALIDATION ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();
        List<String> errors = eventValidationService.validateReceiptDateMultiple(multipleDetails.getCaseData());

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/listingsDateRangeMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<ListingCallbackResponse> listingsDateRangeMidEventValidation(
            @RequestBody ListingRequest listingRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("LISTING DATE RANGE VALIDATION ---> " + LOG_MESSAGE + listingRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        ListingData caseData = listingRequest.getCaseDetails().getCaseData();

        List<String> errors = eventValidationService.validateListingDateRange(
                caseData.getListingDateFrom(),
                caseData.getListingDateTo()
        );

        return getListingCallbackRespEntity(errors, caseData);
    }

    @PostMapping(value = "/initialiseCloseMultiple", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> initialiseCloseMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        String caseTypeId = multipleRequest.getCaseDetails().getCaseTypeId();
        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        if (ENGLANDWALES_BULK_CASE_TYPE_ID.equals(caseTypeId)) {
            fileLocationSelectionService.initialiseFileLocation(multipleData);
        } else if (SCOTLAND_BULK_CASE_TYPE_ID.equals(caseTypeId)) {
            scotlandFileLocationSelectionService.initialiseFileLocation(multipleData);
            clerkService.initialiseClerkResponsible(multipleData);
        }

        return getMultipleCallbackRespEntity(Collections.emptyList(), multipleRequest.getCaseDetails());
    }

    @PostMapping(value = "/closeMultiple", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> closeMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("CLOSE MULTIPLE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        List<String> errors = multipleCloseEventValidationService.validateCasesBeforeCloseEvent(userToken,
                multipleDetails);

        if (!errors.isEmpty()) {
            return getMultipleCallbackRespEntity(errors, multipleDetails);
        }

        multipleHelperService.sendCloseToSinglesWithoutConfirmation(userToken, multipleDetails, errors);
        MultiplesHelper.resetMidFields(multipleDetails.getCaseData());

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/updatePayloadMultiple", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> updatePayloadMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("UPDATE PAYLOAD MULTIPLE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        MultiplesHelper.updatePayloadMultiple(multipleDetails.getCaseData());

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .data(multipleDetails.getCaseData())
                .build());
    }

    @PostMapping(value = "/resetMultipleState", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> resetMultipleState(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("RESET MULTIPLE STATE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleHelperService.sendResetMultipleStateWithoutConfirmation(userToken, multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(), multipleDetails.getCaseData(), errors, multipleDetails.getCaseId());

        multipleDetails.getCaseData().setState(OPEN_STATE);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/dynamicListOfficesMultiple", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> dynamicListOfficesMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("DYNAMIC LIST OFFICES MULTIPLE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        CaseTransferOfficeService.populateTransferToEnglandWalesOfficeOptions(multipleData);

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .data(multipleData)
                .build());
    }

    @PostMapping(value = "/multipleTransfer", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> multipleTransfer(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MULTIPLE TRANSFER ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleTransferService.multipleTransferLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }
}
