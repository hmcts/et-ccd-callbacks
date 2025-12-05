package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CcdInputOutputException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicRespondentRepresentative;
import uk.gov.hmcts.ethos.replacement.docmosis.service.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.NocUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Slf4j
@RequestMapping("/respondentRepresentative")
@RestController
@RequiredArgsConstructor
public class RespondentRepresentativeController {

    private static final String LOG_MESSAGE =
            "received respondent's remove own representative request for case reference : ";

    private final NocRespondentHelper nocRespondentHelper;
    private final NocRespondentRepresentativeService nocRespondentRepresentativeService;

    @PostMapping(value = "/amendRespondentRepresentativeAboutToStart", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the respondents names into a dynamic list")
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
    public ResponseEntity<CCDCallbackResponse> amendRespondentRepresentativeAboutToStart(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(AUTHORIZATION) String userToken) {
        CaseDataUtils.validateCCDRequest(ccdRequest);
        log.info("AMEND RESPONDENT REPRESENTATIVE ABOUT TO START ---> " + LOG_MESSAGE + "{}",
                ccdRequest.getCaseDetails().getCaseId());
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DynamicRespondentRepresentative.dynamicRespondentRepresentativeNames(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/amendRespondentRepresentativeAboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Amends respondent representative for a single case.")
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
    public ResponseEntity<CCDCallbackResponse> amendRespondentRepresentativeAboutToSubmit(
            @RequestBody @NotNull CCDRequest ccdRequest,
            @RequestHeader(AUTHORIZATION) String userToken) {
        CaseDataUtils.validateCCDRequest(ccdRequest);
        log.info("AMEND RESPONDENT REPRESENTATIVE ABOUT TO SUBMIT ---> " + LOG_MESSAGE + "{}",
                ccdRequest.getCaseDetails().getCaseId());
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>(NocUtils.validateNocCaseData(caseData));
        if (errors.isEmpty()) {
            try {
                NocUtils.mapRepresentativesToRespondents(caseData, ccdRequest.getCaseDetails().getCaseId());
                nocRespondentHelper.removeUnmatchedRepresentations(caseData);
                nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, userToken);
                NocUtils.assignNonMyHmctsOrganisationIds(caseData.getRepCollection());
            } catch (GenericRuntimeException | GenericServiceException gse) {
                errors.addFirst(gse.getMessage());
            }
        } else {
            log.info(errors.toString());
        }
        return getCallbackRespEntityErrors(errors, caseData);
    }

    @PostMapping("/amendRespondentRepresentativeSubmitted")
    @Operation(summary = "processes notice of change update after amending respondent representatives")
    @ApiResponses(value = {
        @ApiResponse(responseCode = HTTP_CODE_TWO_HUNDRED, description = HTTP_MESSAGE_TWO_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_HUNDRED, description = HTTP_MESSAGE_FOUR_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_ONE, description = HTTP_MESSAGE_FOUR_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_THREE, description = HTTP_MESSAGE_FOUR_ZERO_THREE),
        @ApiResponse(responseCode = HTTP_CODE_FOUR_ZERO_FOUR, description = HTTP_MESSAGE_FOUR_ZERO_FOUR),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_HUNDRED, description = HTTP_MESSAGE_FIVE_HUNDRED),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_ONE, description = HTTP_MESSAGE_FIVE_ZERO_ONE),
        @ApiResponse(responseCode = HTTP_CODE_FIVE_ZERO_THREE, description = HTTP_MESSAGE_FIVE_ZERO_THREE)
    })
    public void amendRespondentRepresentativeSubmitted(
            @RequestBody CallbackRequest callbackRequest,
            @RequestHeader(AUTHORIZATION) String userToken) {
        log.info("AMEND RESPONDENT REPRESENTATIVE SUBMITTED ---> " + LOG_MESSAGE + "{}",
                callbackRequest.getCaseDetails().getCaseId());
        try {
            nocRespondentRepresentativeService.updateRespondentRepresentativesAccess(callbackRequest);
        } catch (IOException e) {
            throw new CcdInputOutputException("Failed to update respondent representatives accesses", e);
        } catch (GenericServiceException e) {
            throw new GenericRuntimeException(e);
        }
    }

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
        log.info("REMOVE OWN REPRESENTATIVE_AS_RESPONDENT ---> {}{}",
                LOG_MESSAGE, ccdRequest.getCaseDetails().getCaseId());
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
