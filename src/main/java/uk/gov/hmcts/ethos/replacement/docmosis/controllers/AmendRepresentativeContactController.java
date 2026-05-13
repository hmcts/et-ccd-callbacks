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

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.ET3ResponseConstants.REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;

@Slf4j
@RequestMapping("/amendRepresentativeContact")
@RestController
@RequiredArgsConstructor
public class AmendRepresentativeContactController {

    private final AmendRepresentativeContactService amendRepresentativeContactService;

    @PostMapping(value = "/midEvent", consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates caseData Et3ResponseAddress and MyHmctsAddressText fields if the user selects the"
            + "option to use MyHmcts details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> midEvent(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        try {
            if (REPRESENTATIVE_CONTACT_CHANGE_OPTION_USE_MYHMCTS_DETAILS.equals(
                    caseData.getRepresentativeContactChangeOption())) {
                amendRepresentativeContactService.setRepresentativeMyHmctsContactAddress(userToken,
                        caseData, ccdRequest.getCaseDetails().getCaseId());
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
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        try {
            amendRepresentativeContactService.setRespondentRepresentsContactDetails(
                    userToken, caseData, ccdRequest.getCaseDetails().getCaseId());
            caseData.setMyHmctsAddressText(null);
        } catch (GenericServiceException gse) {
            errors.add(gse.getMessage());
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }
}
