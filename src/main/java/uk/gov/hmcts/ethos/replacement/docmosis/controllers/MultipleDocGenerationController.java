package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.LabelsHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleDocGenerationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleLetterService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.excel.MultipleScheduleService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MULTIPLE_CASE_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntityDocInfo;

@Slf4j
@RequiredArgsConstructor
@RestController
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement", "PMD.LawOfDemeter", "PMD.ExcessiveImports"})
public class MultipleDocGenerationController {

    private static final String LOG_MESSAGE = "received notification request for multiple reference : ";
    private static final String GENERATED_DOCUMENT_URL = "Please download the document from : ";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final MultipleScheduleService multipleScheduleService;
    private final MultipleLetterService multipleLetterService;
    private final MultipleDocGenerationService multipleDocGenerationService;
    private final VerifyTokenService verifyTokenService;

    @PostMapping(value = "/printSchedule", consumes = APPLICATION_JSON_VALUE)


    public ResponseEntity<MultipleCallbackResponse> printSchedule(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("PRINT SCHEDULE ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        DocumentInfo documentInfo = multipleScheduleService.bulkScheduleLogic(userToken, multipleDetails, errors);

        return getMultipleCallbackRespEntityDocInfo(errors, multipleDetails, documentInfo);
    }

    @PostMapping(value = "/printLetter", consumes = APPLICATION_JSON_VALUE)


    public ResponseEntity<MultipleCallbackResponse> printLetter(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("PRINT LETTER ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        DocumentInfo documentInfo = multipleLetterService.bulkLetterLogic(userToken, multipleDetails,
                errors, false);

        return getMultipleCallbackRespEntityDocInfo(errors, multipleDetails, documentInfo);
    }

    @PostMapping(value = "/printDocumentConfirmation", consumes = APPLICATION_JSON_VALUE)


    public ResponseEntity<MultipleCallbackResponse> printDocumentConfirmation(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("PRINT DOCUMENT CONFIRMATION ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .data(multipleData)
                .confirmation_header(GENERATED_DOCUMENT_URL + multipleData.getDocMarkUp())
                .build());
    }

    @PostMapping(value = "/midSelectedAddressLabelsMultiple", consumes = APPLICATION_JSON_VALUE)


    public ResponseEntity<MultipleCallbackResponse> midSelectedAddressLabelsMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MID SELECTED ADDRESS LABELS MULTIPLE ---> "
                + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();

        multipleDocGenerationService.midSelectedAddressLabelsMultiple(userToken, multipleDetails, errors);
        LabelsHelper.validateNumberOfSelectedLabels(multipleDetails.getCaseData(), errors);

        return getMultipleCallbackRespEntity(errors, multipleRequest.getCaseDetails());
    }

    @PostMapping(value = "/midValidateAddressLabelsMultiple", consumes = APPLICATION_JSON_VALUE)


    public ResponseEntity<MultipleCallbackResponse> midValidateAddressLabelsMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MID VALIDATE ADDRESS LABELS MULTIPLE ---> "
                + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = LabelsHelper.midValidateAddressLabelsErrors(
                multipleRequest.getCaseDetails().getCaseData().getAddressLabelsAttributesType(), MULTIPLE_CASE_TYPE);

        return getMultipleCallbackRespEntity(errors, multipleRequest.getCaseDetails());
    }

    @PostMapping(value = "/dynamicMultipleLetters", consumes = APPLICATION_JSON_VALUE)


    public ResponseEntity<MultipleCallbackResponse> dynamicMultipleLetters(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("DYNAMIC MULTIPLE LETTERS ---> " + LOG_MESSAGE + multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();
        multipleLetterService.dynamicMultipleLetters(userToken, multipleDetails, errors);
        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

}
