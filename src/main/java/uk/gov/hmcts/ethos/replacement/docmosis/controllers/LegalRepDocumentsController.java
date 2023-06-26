package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/legalrepDocuments")
@RestController
@RequiredArgsConstructor
public class LegalRepDocumentsController {
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final VerifyTokenService verifyTokenService;

    /**
     * Filters documents viewable to only the legalrep.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> aboutToStart(
        @RequestBody CCDRequest ccdRequest,
        @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DocumentHelper.setLegalRepVisibleDocuments(caseData);

        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }
}