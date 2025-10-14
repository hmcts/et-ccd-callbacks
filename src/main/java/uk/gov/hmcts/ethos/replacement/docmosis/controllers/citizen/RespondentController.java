package uk.gov.hmcts.ethos.replacement.docmosis.controllers.citizen;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.RespondentService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.ACCESSED_SUCCESSFULLY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.BAD_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_STATUS_200_OK;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_STATUS_400_BAD_REQUEST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_STATUS_401_UNAUTHORIZED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_STATUS_403_FORBIDDEN;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_STATUS_404_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_STATUS_500_INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_STATUS_503_SERVICE_UNAVAILABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.INTERNAL_SERVER_ERROR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.SERVICE_UNAVAILABLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.UNAUTHORIZED;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/citizen/respondent")
public class RespondentController {

    // Log messages
    public static final String REQUEST_RECEIVED_REMOVE_OWN_REPRESENTATIVE =
            "Received request to remove representative for respondent with index {} in case with ID {}.";

    private final RespondentService respondentService;

    @PostMapping(value = "/removeOwnRepresentative", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Removes respondent representative with given, respondent index,"
            + "and case submission reference.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HTTP_STATUS_200_OK, description = ACCESSED_SUCCESSFULLY,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = HTTP_STATUS_400_BAD_REQUEST, description = BAD_REQUEST),
        @ApiResponse(responseCode = HTTP_STATUS_401_UNAUTHORIZED, description = UNAUTHORIZED),
        @ApiResponse(responseCode = HTTP_STATUS_403_FORBIDDEN, description = FORBIDDEN),
        @ApiResponse(responseCode = HTTP_STATUS_404_NOT_FOUND, description = NOT_FOUND),
        @ApiResponse(responseCode = HTTP_STATUS_500_INTERNAL_SERVER_ERROR, description = INTERNAL_SERVER_ERROR),
        @ApiResponse(responseCode = HTTP_STATUS_503_SERVICE_UNAVAILABLE, description = SERVICE_UNAVAILABLE),
    })
    public ResponseEntity<CaseDetails> removeOwnRepresentative(
            @RequestParam String submissionReference,
            @RequestParam String respondentIndex,
            @RequestHeader(AUTHORIZATION) String userToken) {
        log.info(REQUEST_RECEIVED_REMOVE_OWN_REPRESENTATIVE, respondentIndex, submissionReference);
        CaseDetails caseDetails;
        try {
            caseDetails = respondentService.revokeRespondentSolicitorRole(
                    userToken, submissionReference, respondentIndex);
        } catch (Exception e) {
            throw new CallbacksRuntimeException(e);
        }
        if (ObjectUtils.isEmpty(caseDetails)) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format("No case found for the given submission reference %s.",
                            submissionReference)));
        }
        return ok(caseDetails);
    }

}
