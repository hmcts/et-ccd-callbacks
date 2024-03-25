package uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.multipleResponse;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearReferralDataFromCaseData;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper.clearUpdateReferralDataFromCaseData;

/**
 * REST controller for the Update Multiples Referral event pages,
 * formats data appropriately for rendering on the front end.
 */
@Slf4j
@RequestMapping("/multiples/updateReferral")
@RestController
@RequiredArgsConstructor
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement", "PMD.ExcessiveImports"})
public class UpdateReferralMultiplesController {
    private final VerifyTokenService verifyTokenService;
    private final UserIdamService userIdamService;
    private final ReferralService referralService;
    private final DocumentManagementService documentManagementService;
    private final CaseLookupService caseLookupService;

    private static final String INVALID_TOKEN = "Invalid Token {}";
    private static final String LOG_MESSAGE = "received notification request for case reference :    ";

    /**
     * Called for the first page of the Update Multiples Referral event.
     * Populates the Referral hearing detail's section on the page.
     * @param request holds the request and case data
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
        @RequestBody MultipleRequest request,
        @RequestHeader(value = "Authorization") String userToken) throws IOException {
        log.info("ABOUT TO START UPDATE MULTIPLES REFERRAL ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleData caseData = request.getCaseDetails().getCaseData();
        caseData.setSelectReferral(ReferralHelper.populateSelectReferralDropdown(caseData.getReferralCollection()));
        return multipleResponse(caseData, null);
    }

    /**
     * Called for the second page of the Update Multiples Referral event.
     * Populates the Referral hearing and reply detail's section on the page.
     * @param request holds the request and case data
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
            @RequestBody MultipleRequest request,
            @RequestHeader(value = "Authorization") String userToken) throws IOException {
        log.info("INIT HEARING AND UPDATE MULTIPLE REFERRAL DETAILS ---> "
                + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = new ArrayList<>();
        MultipleData multipleData = request.getCaseDetails().getCaseData();
        CaseData caseData = new ObjectMapper().convertValue(request.getCaseDetails().getCaseData(), CaseData.class);
        if (ReferralHelper.isValidReferralStatus(caseData)) {
            ReferralHelper.populateUpdateMultiplesReferralDetails(multipleData);
            multipleData.setReferralHearingDetails(
                    ReferralHelper.populateHearingReferralDetails(multipleData, caseData));
        } else {
            errors.add("Only referrals with status awaiting instructions can be updated.");
        }
        return multipleResponse(multipleData, errors);
    }

    /**
     * Called at the end of updating a referral, takes the information saved in case data and stores it in the
     * update referral collection.
     * @param request holds the request and case data
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
    public ResponseEntity<MultipleCallbackResponse> aboutToSubmitReferralDetails(
            @RequestBody MultipleRequest request,
            @RequestHeader(value = "Authorization") String userToken) throws IOException {
        log.info(
                "ABOUT TO SUBMIT UPDATE MULTIPLES REFERRAL ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }
        CaseData caseData = new ObjectMapper().convertValue(request.getCaseDetails().getCaseData(), CaseData.class);
        MultipleData multipleData = request.getCaseDetails().getCaseData();
        multipleData.setReferralCollection(caseData.getReferralCollection());
        UserDetails userDetails = userIdamService.getUserDetails(userToken);
        ReferralHelper.updateMultiplesReferral(
                multipleData,
                caseData,
                String.format("%s %s", userDetails.getFirstName(), userDetails.getLastName()));
        ReferralType referral = multipleData.getReferralCollection()
                .get(Integer.parseInt(multipleData.getSelectReferral().getValue().getCode()) - 1).getValue();

        DocumentInfo documentInfo = referralService.generateDocument(multipleData, caseData,
                userToken, request.getCaseDetails().getCaseTypeId());

        referral.setReferralSummaryPdf(this.documentManagementService.addDocumentToDocumentField(documentInfo));
        clearUpdateReferralDataFromCaseData(caseData);
        clearReferralDataFromCaseData(multipleData);
        return multipleResponse(multipleData, null);
    }

    /**
     * Called after submitting a update referral event.
     *
     * @param request holds the request and case data
     * @param userToken  used for authorization
     * @return Callback response entity with confirmation header and body
     */
    @PostMapping(value = "/completeUpdateReferral", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "completes the reply to referral event flow")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accessed successfully", content = {
        @Content(
                    mediaType = "application/json", schema = @Schema(
                            implementation = MultipleCallbackResponse.class))}),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<MultipleCallbackResponse> completeUpdateReferral(
            @RequestBody MultipleRequest request,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("COMPLETE UPDATE MULTIPLES REFERRAL ---> " + LOG_MESSAGE + request.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(MultipleCallbackResponse.builder()
                .build());
    }
}
