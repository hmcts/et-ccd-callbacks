package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.AmendRepresentativeContactService;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FIVE_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FIVE_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FIVE_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_ZERO_FOUR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_FOUR_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_CODE_TWO_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_HEADER_AUTHORIZATION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FIVE_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FIVE_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FIVE_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_ZERO_FOUR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_ZERO_ONE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_FOUR_ZERO_THREE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.HttpConstants.HTTP_MESSAGE_TWO_HUNDRED;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Slf4j
@RequestMapping("/amendRepresentativeContact")
@RestController
@RequiredArgsConstructor
public class AmendRepresentativeContactController {

    private final AmendRepresentativeContactService amendRepresentativeContactService;

    @PostMapping(value = "/aboutToStart", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates caseData Et3ResponseAddress and MyHmctsAddressText fields if the user selects the"
            + "option to use MyHmcts details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HTTP_CODE_TWO_HUNDRED, description = HTTP_MESSAGE_TWO_HUNDRED,
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_HUNDRED, description = HTTP_MESSAGE_FOUR_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_ONE, description = HTTP_MESSAGE_FOUR_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_THREE, description = HTTP_MESSAGE_FOUR_ZERO_THREE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_FOUR, description = HTTP_MESSAGE_FOUR_ZERO_FOUR),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_HUNDRED, description = HTTP_MESSAGE_FIVE_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_ONE, description = HTTP_MESSAGE_FIVE_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_THREE, description = HTTP_MESSAGE_FIVE_ZERO_THREE)
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStart(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(HTTP_HEADER_AUTHORIZATION) String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        try {
            amendRepresentativeContactService.setEt3ResponseContactAddress(userToken, caseData,
                    ccdRequest.getCaseDetails().getCaseId());
        } catch (GenericServiceException gse) {
            errors.add(gse.getMessage());
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/midEvent", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates caseData Et3ResponseAddress and MyHmctsAddressText fields if the user selects the"
            + "option to use MyHmcts details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HTTP_CODE_TWO_HUNDRED, description = HTTP_MESSAGE_TWO_HUNDRED,
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_HUNDRED, description = HTTP_MESSAGE_FOUR_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_ONE, description = HTTP_MESSAGE_FOUR_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_THREE, description = HTTP_MESSAGE_FOUR_ZERO_THREE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_FOUR, description = HTTP_MESSAGE_FOUR_ZERO_FOUR),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_HUNDRED, description = HTTP_MESSAGE_FIVE_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_ONE, description = HTTP_MESSAGE_FIVE_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_THREE, description = HTTP_MESSAGE_FIVE_ZERO_THREE)
    })
    public ResponseEntity<CCDCallbackResponse> midEvent(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(HTTP_HEADER_AUTHORIZATION) String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        try {
            if (REPRESENTATIVE_CONTACT_CHANGE_OPTION_MYHMCTS.equals(
                    caseData.getRepresentativeContactChangeOption())) {
                amendRepresentativeContactService.setRepresentativeMyHmctsContactAddress(userToken, caseData);
            }
        } catch (GenericServiceException gse) {
            errors.add(gse.getMessage());
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping(value = "/aboutToSubmit", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates RepresentedTypeR model of the respondent representative with new address and phone"
            + "values")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HTTP_CODE_TWO_HUNDRED, description = HTTP_MESSAGE_TWO_HUNDRED,
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_HUNDRED, description = HTTP_MESSAGE_FOUR_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_ONE, description = HTTP_MESSAGE_FOUR_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_THREE, description = HTTP_MESSAGE_FOUR_ZERO_THREE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_FOUR, description = HTTP_MESSAGE_FOUR_ZERO_FOUR),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_HUNDRED, description = HTTP_MESSAGE_FIVE_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_ONE, description = HTTP_MESSAGE_FIVE_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_THREE, description = HTTP_MESSAGE_FIVE_ZERO_THREE)
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(HTTP_HEADER_AUTHORIZATION) String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        try {
            amendRepresentativeContactService.updateRepresentativeContactDetails(
                    userToken, caseData, ccdRequest.getCaseDetails().getCaseId());
            caseData.setMyHmctsAddressText(null);
            caseData.setEt3ResponseAddress(null);
        } catch (GenericServiceException gse) {
            errors.add(gse.getMessage());
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }
}
