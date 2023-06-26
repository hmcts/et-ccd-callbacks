package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ClaimantHearingPreferencesValidator;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

/**
 * REST Controller that handles CCD callbacks for hearing preferences.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class ClaimantHearingPreferencesController {
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final VerifyTokenService verifyTokenService;

    /**
     * Mid-event callback for validating hearing preferences options are a valid combination.
     * @param userToken        Authorisation token
     * @param ccdRequest       CCD request containing the case data
     * @return ResponseEntity  CCD callback response
     */
    @PostMapping(value = "/midEventHearingPreferences", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> validateHearingPreferences(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = ClaimantHearingPreferencesValidator.validateHearingPreferenceOptions(caseData);

        return getCallbackRespEntityErrors(errors, caseData);
    }
}
