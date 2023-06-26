package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles.BulkAddSinglesService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.multiples.bulkaddsingles.BulkAddSinglesValidator;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@RestController
@Slf4j
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.UnnecessaryAnnotationValueElement"})
public class BulkAddSinglesController {
    private final BulkAddSinglesValidator bulkAddSinglesValidator;
    private final BulkAddSinglesService bulkAddSinglesService;
    private final VerifyTokenService verifyTokenService;
    private static final String INVALID_TOKEN = "Invalid Token {}";

    public BulkAddSinglesController(BulkAddSinglesValidator bulkAddSinglesValidator,
                                    BulkAddSinglesService bulkAddSinglesService,
                                    VerifyTokenService verifyTokenService) {
        this.bulkAddSinglesValidator = bulkAddSinglesValidator;
        this.bulkAddSinglesService = bulkAddSinglesService;
        this.verifyTokenService = verifyTokenService;
    }

    @PostMapping(value = "/bulkAddSingleCasesImportFileMidEventValidation", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> bulkAddSingleCasesImportFileMidEventValidation(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();
        List<String> errors = bulkAddSinglesValidator.validate(multipleDetails, userToken);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }

    @PostMapping(value = "/bulkAddSingleCasesToMultiple", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<MultipleCallbackResponse> bulkAddSingleCasesToMultiple(
            @RequestBody MultipleRequest multipleRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails multipleDetails = multipleRequest.getCaseDetails();
        List<String> errors = bulkAddSinglesService.execute(multipleDetails, userToken);

        return getMultipleCallbackRespEntity(errors, multipleDetails);
    }
}
