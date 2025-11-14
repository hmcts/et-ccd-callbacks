package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.micrometer.common.util.StringUtils;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.TriageQuestions;

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
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole.CLAIMANTSOLICITOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/claimantRepresentative")
@RestController
@RequiredArgsConstructor
public class ClaimantRepresentativeController {

    private static final String LOG_MESSAGE =
            "received claimant's remove own representative request for case reference : ";

    /**
     * Handles the removal of a claimant's own representative from the case data.
     * <p>
     * This endpoint is typically invoked when a claimant wishes to remove their legal representative
     * information from their case. If the claimant is currently represented (i.e. the
     * {@code claimantRepresentedQuestion} field is set to {@code YES}), this method will:
     * <ul>
     *     <li>Clear the {@code representativeClaimantType} field from the {@link CaseData}.</li>
     *     <li>Reset the {@code claimantRepresentativeOrganisationPolicy} with the appropriate case role
     *         for a claimant solicitor.</li>
     * </ul>
     * Finally, a successful callback response containing the updated {@link CaseData} is returned.
     * </p>
     *
     * @param ccdRequest the incoming CCD callback request containing the case details and data
     * @param userToken  the authorization token of the user performing the action; provided in the
     *                   {@code Authorization} header
     * @return a {@link ResponseEntity} containing a {@link CCDCallbackResponse} with the updated
     *         {@link CaseData} and no errors
     *
     * @apiNote This endpoint is intended to be used by claimants to manage their own representative
     *          information within the CCD case workflow.
     *
     * @see CCDRequest
     * @see CCDCallbackResponse
     * @see CaseData
     * @see CaseDetails
     *
     * @throws org.springframework.web.server.ResponseStatusException if the request is invalid
     */
    @PostMapping(value = "/removeOwnRepresentative", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "remove own representative as claimant.")
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
        log.info("REMOVE OWN REPRESENTATIVE_AS_CLAIMANT ---> {}{}",
                LOG_MESSAGE, ccdRequest.getCaseDetails().getCaseId());
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        if (StringUtils.isNotBlank(caseData.getClaimantRepresentedQuestion())
                && NO.equals(caseData.getClaimantRepresentedQuestion())) {
            caseData.setRepresentativeClaimantType(null);
            if (ObjectUtils.isEmpty(caseData.getTriageQuestions())) {
                caseData.setTriageQuestions(new TriageQuestions());
            }
            caseData.getTriageQuestions().setClaimantRepresentedQuestion(NO);
            caseData.setClaimantRepresentativeOrganisationPolicy(
                    OrganisationPolicy.builder().orgPolicyCaseAssignedRole(CLAIMANTSOLICITOR.getCaseRoleLabel()).build()
            );
        }
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

}
