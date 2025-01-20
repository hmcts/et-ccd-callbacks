package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

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
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.types.ReferralType;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLookupService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentManagementService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.ReferralService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.UserIdamService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralDataFromCaseData;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.getNearestHearingToReferral;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.setReferralSubject;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.updateReferral;

/**
 * REST controller for the Update Referral event pages, formats data appropriately for rendering on the front end.
 */
@Slf4j
@RequestMapping("/multiples/updateReferral")
@RestController
@RequiredArgsConstructor
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement", "PMD.ExcessiveImports"})
public class UpdateReferralMultiplesController {
    private final UserIdamService userIdamService;
    private final ReferralService referralService;
    private final DocumentManagementService documentManagementService;
    private final CaseLookupService caseLookupService;
    private static final String LOG_MESSAGE = "received update multiples referral request for case reference : ";

    /**
     * Called for the first page of the Update Referral event.
     * Populates the Referral hearing detail's section on the page.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for referral update")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> updateReferralAboutToStart(
        @RequestBody MultipleRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) throws IOException {
        log.info("ABOUT TO START UPDATE REFERRAL ---> " + LOG_MESSAGE + "{}", ccdRequest.getCaseDetails().getCaseId());

        MultipleData caseData = ccdRequest.getCaseDetails().getCaseData();
        clearReferralDataFromCaseData(caseData);
        CaseData leadCase = caseLookupService.getLeadCaseFromMultipleAsAdmin(ccdRequest.getCaseDetails());
        caseData.setReferralHearingDetails(ReferralHelper.populateHearingDetails(leadCase));
        caseData.setSelectReferral(ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection()));
        return multipleResponse(caseData, null);
    }

    /**
     * Called for the second page of the Reply Referral event.
     * Populates the Referral hearing and reply detail's section on the page.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/initHearingAndReferralDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "initialize data for reply to referral event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
                    content = {
                        @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MultipleCallbackResponse.class))
                    }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> initHearingDetailsForUpdateReferral(
            @RequestBody MultipleRequest ccdRequest,
            @RequestHeader(value = "Authorization") String userToken) throws IOException {
        log.info("INIT HEARING AND UPDATE REFERRAL DETAILS ---> " + LOG_MESSAGE + "{}",
                ccdRequest.getCaseDetails().getCaseId());

        List<String> errors = new ArrayList<>();
        MultipleData caseData = ccdRequest.getCaseDetails().getCaseData();

        if (ReferralHelper.isValidReferralStatus(caseData)) {
            ReferralHelper.populateUpdateReferralDetails(caseData);
            CaseData leadCase = caseLookupService.getLeadCaseFromMultipleAsAdmin(ccdRequest.getCaseDetails());
            caseData.setHearingAndReferralDetails(ReferralHelper.populateHearingReferralDetails(caseData, leadCase));
        } else {
            errors.add("Only referrals with status awaiting instructions can be updated.");
        }
        
        return multipleResponse(caseData, errors);
    }

    /**
     * Called at the end of updating a referral, takes the information saved in case data and stores it in the
     * update referral collection.
     * @param ccdRequest holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with case data and errors attached.
     */
    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MultipleCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmitUpdateReferralDetails(
        @RequestBody MultipleRequest ccdRequest,
        @RequestHeader(value = "Authorization") String userToken) throws IOException {
        log.info("ABOUT TO SUBMIT UPDATE REFERRAL ---> " + LOG_MESSAGE + "{}", ccdRequest.getCaseDetails().getCaseId());

        MultipleData caseData = ccdRequest.getCaseDetails().getCaseData();
        caseData.setReferralSubject(setReferralSubject(caseData.getReferralSubject()));
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        CaseData leadCase = caseLookupService.getLeadCaseFromMultipleAsAdmin(ccdRequest.getCaseDetails());
        String nextHearingDate = getNearestHearingToReferral(leadCase, "None");
        String name = String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName());
        updateReferral(caseData, name, nextHearingDate);
        ReferralType referral = caseData.getReferralCollection()
                .get(Integer.parseInt(caseData.getSelectReferral().getValue().getCode()) - 1).getValue();

        String caseTypeId = ccdRequest.getCaseDetails().getCaseTypeId();
        DocumentInfo documentInfo = referralService.generateDocument(caseData, leadCase, userToken, caseTypeId);

        referral.setReferralSummaryPdf(documentManagementService.addDocumentToDocumentField(documentInfo));

        clearReferralDataFromCaseData(caseData);
        return multipleResponse(caseData, null);
    }

}
