package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FIVE_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FIVE_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FIVE_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_ZERO_FOUR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_TWO_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FIVE_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FIVE_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FIVE_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_ZERO_FOUR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_TWO_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/respondentRepresentative")
@RestController
@RequiredArgsConstructor
public class RespondentRepresentativeController {

    private static final String LOG_MESSAGE = "received remove own representative request for case reference : ";

    @PostMapping(value = "/removeOwnRepresentative", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "remove own representative as respondent.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HTTP_CODE_TWO_HUNDRED, description = HTTP_MESSAGE_TWO_HUNDRED,
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_HUNDRED, description = HTTP_MESSAGE_FOUR_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_ONE, description = HTTP_MESSAGE_FOUR_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_THREE, description = HTTP_MESSAGE_FOUR_ZERO_THREE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_FOUR, description = HTTP_MESSAGE_FOUR_ZERO_FOUR),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_HUNDRED, description = HTTP_MESSAGE_FIVE_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_ONE, description = HTTP_MESSAGE_FIVE_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_THREE, description = HTTP_MESSAGE_FIVE_ZERO_THREE)
    })
    public ResponseEntity<CCDCallbackResponse> removeOwnRepresentative(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(AUTHORIZATION) String userToken) {
        log.info("REMOVE OWN REPRESENTATIVE ---> {}{}", LOG_MESSAGE, ccdRequest.getCaseDetails().getCaseId());
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        if (CollectionUtils.isNotEmpty(caseData.getRepCollection())
                && CollectionUtils.isNotEmpty(caseData.getRepCollectionToRemove())) {
            caseData.getRepCollection().removeAll(caseData.getRepCollectionToRemove());
            caseData.setRepCollectionToRemove(null);
        }
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }
}
