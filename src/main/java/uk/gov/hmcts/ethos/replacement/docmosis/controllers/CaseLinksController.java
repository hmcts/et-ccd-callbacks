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
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.ListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.CaseLink;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseLinksHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLinksEmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseRetrievalForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/caseLinks")
@Slf4j
@RequiredArgsConstructor
public class CaseLinksController {

    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final CaseLinksEmailService caseLinksEmailService;
    private final CaseRetrievalForCaseWorkerService caseRetrievalForCaseWorkerService;


    /**
     * Sends email confirmation of case linking.
     */
    @PostMapping(value = "create/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Sends email confirmation.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sent successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> createCaseLinkAbout(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        SubmitEvent currentCase = caseRetrievalForCaseWorkerService.caseRetrievalRequest(userToken,
                ccdRequest.getCaseDetails().getCaseTypeId(), ccdRequest.getCaseDetails().getJurisdiction(),
                ccdRequest.getCaseDetails().getCaseId());
        ListTypeItem<CaseLink> caseLinks = currentCase.getCaseData().getCaseLinks();
        ListTypeItem<CaseLink> newCaseLinks = ccdRequest.getCaseDetails().getCaseData().getCaseLinks();
        boolean isLinkedForHearing;
        if (caseLinks == null  || caseLinks.isEmpty()) {
            isLinkedForHearing = CaseLinksHelper.isLinkedForHearing(newCaseLinks);
        } else {
            List<GenericTypeItem<CaseLink>> addedCaseLinks = new ArrayList<>(newCaseLinks);
            addedCaseLinks.remove(caseLinks);
            isLinkedForHearing = CaseLinksHelper.isLinkedForHearing(addedCaseLinks);
        }
        if (isLinkedForHearing) {
            caseLinksEmailService.sendCaseLinkingEmails(ccdRequest.getCaseDetails(), true);
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .build());
    }

    /**
     * Sends email confirmation of case unlinking.
     */
    @PostMapping(value = "maintain/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Sends email confirmation.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sent successfully",
            content = {
                @Content(mediaType = "application/json",
                        schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> unLinkCaseSubmitted(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        SubmitEvent currentCase = caseRetrievalForCaseWorkerService.caseRetrievalRequest(userToken,
                ccdRequest.getCaseDetails().getCaseTypeId(), ccdRequest.getCaseDetails().getJurisdiction(),
                ccdRequest.getCaseDetails().getCaseId());
        ListTypeItem<CaseLink> caseLinks = currentCase.getCaseData().getCaseLinks();
        ListTypeItem<CaseLink> newCaseLinks = ccdRequest.getCaseDetails().getCaseData().getCaseLinks();
        boolean isLinkedForHearing;
        if (newCaseLinks == null || newCaseLinks.isEmpty()) {
            isLinkedForHearing = CaseLinksHelper.isLinkedForHearing(caseLinks);
        } else {
            List<GenericTypeItem<CaseLink>> removedCaseLinks = new ArrayList<>(caseLinks);
            removedCaseLinks.remove(newCaseLinks);
            isLinkedForHearing = CaseLinksHelper.isLinkedForHearing(removedCaseLinks);
        }
        if (isLinkedForHearing) {
            caseLinksEmailService.sendCaseLinkingEmails(ccdRequest.getCaseDetails(), false);
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder()
                .data(ccdRequest.getCaseDetails().getCaseData())
                .build());
    }
}
