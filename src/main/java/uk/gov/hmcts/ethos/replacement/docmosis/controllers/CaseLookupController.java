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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleCallbackResponse;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.ReferralHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getMultipleCallbackRespEntity;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CaseLookupController {
    private static final String INVALID_TOKEN = "Invalid Token {}";
    private final VerifyTokenService verifyTokenService;
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;

    @PostMapping(value = "/leadCaseHearingDetails", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Retrieve ACAS Certificate from ACAS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<MultipleCallbackResponse> leadCaseHearingDetails(
            @RequestBody MultipleRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) throws Exception {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        MultipleDetails caseDetails = ccdRequest.getCaseDetails();
        MultipleData caseData = caseDetails.getCaseData();

        String adminToken = adminUserService.getAdminUserToken();

        Pattern pattern = Pattern.compile("(\\d{16})");
        Matcher matcher = pattern.matcher(caseData.getLeadCase());

        if (!matcher.find()) {
            throw new Exception("Could not find 16 digit case id for lead case");
        }

        String number = matcher.group(1);
        SubmitEvent submitEvent = ccdClient.retrieveCase(adminToken, caseDetails.getCaseTypeId()
                .replace("_Multiple", ""), caseDetails.getJurisdiction(), number);

        CaseData caseData1 = submitEvent.getCaseData();

        log.error("Got details for " + number);

        caseData.setExampleLeadCaseHearingDetails(ReferralHelper.populateHearingDetails(caseData1));

        return getMultipleCallbackRespEntity(List.of(), caseDetails);
    }
}
