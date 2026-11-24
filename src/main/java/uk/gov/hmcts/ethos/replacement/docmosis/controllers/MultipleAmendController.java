package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleAmendReviewService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleAmendService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleCreationMidEventValidationService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADD_CASES_TO_MULTIPLE_AMENDMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.LEAD_CASE_AMENDMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.REMOVE_CASES_FROM_MULTIPLE_AMENDMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntityErrorsAndWarnings;

@RequiredArgsConstructor
@RestController
public class MultipleAmendController {

    private final VerifyTokenService verifyTokenService;
    private final MultipleAmendService multipleAmendService;
    private final MultipleAmendReviewService multipleAmendReviewService;
    private final MultipleCreationMidEventValidationService validationService;

    @PostMapping(value = "/multiples/amendDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates the multiple name and note without changing its cases")
    public ResponseEntity<MultipleCallbackResponse> amendDetails(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN).build();
        }

        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();
        multipleDetails.getCaseData().setTypeOfAmendmentMSL(null);
        return getMultipleCallbackRespEntity(List.of(), multipleDetails);
    }

    @PostMapping(value = "/multiples/addCases", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Adds cases to a multiple and regenerates its Selection Criteria spreadsheet")
    public ResponseEntity<MultipleCallbackResponse> addCases(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {
        return amendMultiple(multipleRequest, userToken, ADD_CASES_TO_MULTIPLE_AMENDMENT);
    }

    @PostMapping(value = "/multiples/removeCases", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Removes cases from a multiple and regenerates its Selection Criteria spreadsheet")
    public ResponseEntity<MultipleCallbackResponse> removeCases(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {
        return amendMultiple(multipleRequest, userToken, REMOVE_CASES_FROM_MULTIPLE_AMENDMENT);
    }

    @PostMapping(value = "/multiples/updateLeadCase", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates the lead case of a multiple")
    public ResponseEntity<MultipleCallbackResponse> updateLeadCase(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {
        return amendMultiple(multipleRequest, userToken, LEAD_CASE_AMENDMENT);
    }

    @PostMapping(value = "/multiples/addCases/review", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Validates cases to add and prepares their read-only review table")
    public ResponseEntity<MultipleCallbackResponse> reviewAddCases(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {
        return reviewCases(multipleRequest, userToken, false);
    }

    @PostMapping(value = "/multiples/removeCases/review", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Validates cases to remove and prepares their read-only review table")
    public ResponseEntity<MultipleCallbackResponse> reviewRemoveCases(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader("Authorization") String userToken) {
        return reviewCases(multipleRequest, userToken, true);
    }

    private ResponseEntity<MultipleCallbackResponse> reviewCases(
            MultipleRequest multipleRequest, String userToken, boolean removing) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN).build();
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        MultipleDetails details = multipleRequest.getCaseDetails();
        multipleAmendReviewService.prepareReview(userToken, details, removing, errors, warnings);
        return getMultipleCallbackRespEntityErrorsAndWarnings(warnings, errors, details.getCaseData());
    }

    private ResponseEntity<MultipleCallbackResponse> amendMultiple(
            MultipleRequest multipleRequest, String userToken, String amendment) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN).build();
        }

        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();
        List<String> errors = new ArrayList<>();
        if (ADD_CASES_TO_MULTIPLE_AMENDMENT.equals(amendment)) {
            validationService.multipleCreationValidationLogic(
                    userToken, multipleDetails, errors, new ArrayList<>(), true);
            if (!errors.isEmpty()) {
                return getMultipleCallbackRespEntity(errors, multipleDetails);
            }
        }
        multipleDetails.getCaseData().setTypeOfAmendmentMSL(List.of(amendment));
        multipleAmendService.bulkAmendMultipleLogic(userToken, multipleDetails, errors);
        if (errors.isEmpty()) {
            multipleDetails.getCaseData().setMultipleAmendReview(null);
        }
        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }
}
