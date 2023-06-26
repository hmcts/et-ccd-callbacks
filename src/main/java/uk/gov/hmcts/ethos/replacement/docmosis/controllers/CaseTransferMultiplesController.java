package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferOfficeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.MultipleTransferDifferentCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.MultipleTransferSameCountryService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@RestController
@RequestMapping("/caseTransferMultiples")
@Slf4j
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class CaseTransferMultiplesController {

    private static final String LOG_MESSAGE = "{} received notification request for case reference : {}";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final MultipleTransferSameCountryService multipleTransferSameCountryService;
    private final MultipleTransferDifferentCountryService multipleTransferDifferentCountryService;

    public CaseTransferMultiplesController(
            VerifyTokenService verifyTokenService,
            MultipleTransferSameCountryService multipleTransferSameCountryService,
            MultipleTransferDifferentCountryService multipleTransferDifferentCountryService) {
        this.verifyTokenService = verifyTokenService;
        this.multipleTransferSameCountryService = multipleTransferSameCountryService;
        this.multipleTransferDifferentCountryService = multipleTransferDifferentCountryService;
    }

    @PostMapping(value = "/initTransferToEnglandWales", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> initTransferToEnglandWales(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
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

    @PostMapping(value = "/initTransferToScotland", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> initTransferToScotland(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleData multipleData = multipleRequest.getCaseDetails().getCaseData();
        CaseTransferOfficeService.populateTransferToScotlandOfficeOptions(multipleData);

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .data(multipleData)
                .build());
    }

    @PostMapping(value = "/transferSameCountry", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> transferSameCountry(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, "MULTIPLE TRANSFER SAME COUNTRY ---> ", multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();
        List<String> errors = multipleTransferSameCountryService.transferMultiple(multipleDetails, userToken);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/transferDifferentCountry", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> transferDifferentCountry(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info(LOG_MESSAGE, "MULTIPLE TRANSFER DIFFERENT COUNTRY ---> ",
                multipleRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();
        List<String> errors = multipleTransferDifferentCountryService.transferMultiple(multipleDetails, userToken);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }
}
