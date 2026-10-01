package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRemoveRepNotificationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRemoveRepresentationService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntity;

@Slf4j
@RequestMapping("/nocRemoveRepresentation")
@RestController
@RequiredArgsConstructor
public class NocRemoveRepresentationController {

    private final UserService userService;
    private final NocRemoveRepresentationService nocRemoveRepresentationService;
    private final NocRemoveRepNotificationService nocRemoveRepNotificationService;

    @PostMapping(value = "/claimant/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "nocRemoveRep claimant about to submit page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToStartClaimant(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        List<String> errors = new ArrayList<>();
        try {
            UserDetails userDetails = userService.getValidatedUserDetails(userToken, caseDetails.getCaseId());
            nocRemoveRepresentationService.setNocRemoveOption(userDetails, caseDetails);
        } catch (GenericServiceException gse) {
            errors.add(gse.getMessage());
        }
        return getCallbackRespEntity(errors, caseDetails);
    }

    @PostMapping(value = "/claimant/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "nocRemoveRep claimant about to submit page")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitClaimant(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        List<String> errors = new ArrayList<>();
        try {
            UserDetails userDetails = userService.getValidatedUserDetails(userToken, caseDetails.getCaseId());
            nocRemoveRepresentationService.setNocRemoveOption(userDetails, caseDetails);
            nocRemoveRepresentationService.revokeClaimantLegalRep(caseDetails);
            nocRemoveRepNotificationService.sendClaimantRepresentativeRemovalNotifications(userDetails, caseDetails);
            caseDetails.getCaseData().setNocRemoveOption(null);
        } catch (GenericServiceException gse) {
            errors.add(gse.getMessage());
        }
        return getCallbackRespEntity(errors, caseDetails);
    }
}