package uk.gov.hmcts.ethos.replacement.docmosis.controllers.applications.claimant;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementForCaseWorkerService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.applications.TseService;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.DocumentHelper.setDocumentNumbers;

@RestController
@RequestMapping("/tseClaimant")
@RequiredArgsConstructor
public class TseClaimantController {

    private final TseService tseService;
    private final CaseManagementForCaseWorkerService caseManagementForCaseWorkerService;

    @PostMapping(value = "/aboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> aboutToSubmitRespondentTSE(
        @RequestBody CCDRequest ccdRequest) {
        CaseDetails caseDetails = ccdRequest.getCaseDetails();

        if (caseDetails.getCaseData().getClaimantTse() != null) {
            tseService.createApplication(caseDetails.getCaseData(), CLAIMANT_TITLE);
            tseService.removeStoredApplication(caseDetails.getCaseData());
            tseService.clearApplicationData(caseDetails.getCaseData());
        }
        setDocumentNumbers(caseDetails.getCaseData());
        caseManagementForCaseWorkerService.setNextListedDate(caseDetails.getCaseData());
        return getCallbackRespEntityNoErrors(caseDetails.getCaseData());
    }
}
