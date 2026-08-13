package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
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
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NocRespondentHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.dynamiclists.DynamicRespondentRepresentative;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseFlagsService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.noc.NocRespondentRepresentativeService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.NocUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.noc.RespondentRepresentativeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

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
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_REPRESENTATIVE_ORGANISATION_AND_EMAIL_NOT_MATCHED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_UNABLE_TO_MODIFY_REPRESENTATIVE_ACCESS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrorsAndWarnings;
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
    private final FeatureToggleService featureToggleService;
    private final CaseFlagsService caseFlagsService;

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
        log.info("AMEND RESPONDENT REPRESENTATIVE ABOUT TO START ---> {} {}", LOG_MESSAGE,
                ccdRequest.getCaseDetails().getCaseId());
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        DynamicRespondentRepresentative.dynamicRespondentRepresentativeNames(caseData);
        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/amendRespondentRepresentativeMidEvent", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Checks respondent representative organisation.")
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
    public ResponseEntity<CCDCallbackResponse> amendRespondentRepresentativeMidEvent(
            @RequestBody @NotNull CCDRequest ccdRequest,
            @RequestHeader(AUTHORIZATION) String userToken) {
        CaseDataUtils.validateCCDRequest(ccdRequest);
        log.info("CHECKING RESPONDENT REPRESENTATIVE ORGANISATION ---> {} {}", LOG_MESSAGE,
                ccdRequest.getCaseDetails().getCaseId());
        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        try {
            warnings.addAll(nocRespondentRepresentativeService.validateRepresentativesOrganisationsAndEmails(caseData));
        } catch (GenericRuntimeException | GenericServiceException gse) {
            String errorMessage = String.format(ERROR_REPRESENTATIVE_ORGANISATION_AND_EMAIL_NOT_MATCHED,
                    StringUtils.EMPTY);
            if (EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND.equals(gse.getMessage())) {
                errorMessage = String.format(ERROR_REPRESENTATIVE_ORGANISATION_AND_EMAIL_NOT_MATCHED,
                        EXCEPTION_REPRESENTATIVE_ORGANISATION_NOT_FOUND);
            }
            errors.add(errorMessage);
        }
        return getCallbackRespEntityErrorsAndWarnings(warnings, errors, caseData);
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
            @RequestBody @NotNull CallbackRequest callbackRequest,
            @RequestHeader(AUTHORIZATION) String userToken) {
        CaseDataUtils.validateCaseDetails(callbackRequest.getCaseDetails());
        log.info("AMEND RESPONDENT REPRESENTATIVE ABOUT TO SUBMIT ---> {} {}", LOG_MESSAGE,
                callbackRequest.getCaseDetails().getCaseId());
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        List<String> errors = new ArrayList<>(NocUtils.validateNocCaseData(caseData));
        errors.addAll(nocRespondentRepresentativeService
                .validateRespondentRepresentativesOrganisationMatch(caseDetails));
        if (errors.isEmpty()) {
            try {
                NocUtils.mapRepresentativesToRespondents(caseData, caseDetails.getCaseId());
                nocRespondentHelper.removeUnmatchedRepresentations(caseData);
                nocRespondentRepresentativeService.prepopulateOrgAddress(caseData, userToken);
                NocUtils.assignNonMyHmctsOrganisationIds(caseData.getRepCollection());
                nocRespondentRepresentativeService.removeConflictingClaimantRepresentation(caseDetails);
            } catch (GenericRuntimeException | GenericServiceException gse) {
                errors.addFirst(gse.getMessage());
            }
        }
        if (errors.isEmpty()) {
            setupCaseFlagsIfRespondentRepresentativeChanged(callbackRequest);
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
        log.info("AMEND RESPONDENT REPRESENTATIVE SUBMITTED ---> {}, {}", LOG_MESSAGE,
                callbackRequest.getCaseDetails().getCaseId());
        try {
            NocUtils.validateCallbackRequest(callbackRequest);
            nocRespondentRepresentativeService.updateRepresentativesAccess(callbackRequest, userToken);
        } catch (GenericServiceException | GenericRuntimeException e) {
            log.error(ERROR_UNABLE_TO_MODIFY_REPRESENTATIVE_ACCESS,
                    callbackRequest.getCaseDetails().getCaseId(), e.getMessage());
        }
    }

    @PostMapping(value = "/updateRespOrgPolicyAboutToSubmit", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Updates respondent's organisation policy, representatives' roles and resets change"
            + "organisation request field after representation amended(revoked/assigned)")
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
    public ResponseEntity<CCDCallbackResponse> updateRespOrgPolicyAboutToSubmit(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(AUTHORIZATION) String userToken) {
        log.info("UPDATE RESPONDENT ORGANISATION POLICIES AND ROLES FOR REMOVED REPRESENTATIVES ---> {}{}",
                LOG_MESSAGE, ccdRequest.getCaseDetails().getCaseId());
        CaseDetails caseDetails = ccdRequest.getCaseDetails();
        CaseData caseData = caseDetails.getCaseData();
        if (CollectionUtils.isNotEmpty(caseData.getRepCollectionToRemove())) {
            NocUtils.resetOrganisationPolicies(caseData, caseData.getRepCollectionToRemove());
            RespondentRepresentativeUtils.clearRolesForRepresentatives(caseData, caseData.getRepCollectionToRemove());
            // reset field repCollectionToRemove
            caseData.setRepCollectionToRemove(null);
        }
        if (CollectionUtils.isNotEmpty(caseData.getRepCollectionToAdd())) {
            for (RepresentedTypeRItem representative : caseData.getRepCollectionToAdd()) {
                NocUtils.applyRespondentOrganisationPolicyForRole(caseData, representative);
                for (RepresentedTypeRItem caseRepresentative : caseData.getRepCollection()) {
                    if (caseRepresentative.getId().equals(representative.getId())) {
                        caseRepresentative.getValue().setRole(representative.getValue().getRole());
                        caseRepresentative.getValue().setIdamId(representative.getValue().getIdamId());
                    }
                }
            }
            // reset field repCollectionToAdd
            caseData.setRepCollectionToAdd(null);
        }
        // Clears the changeOrganisationRequestField to prevent errors in the existing representative process
        // and to allow further changes to be made
        caseData.setChangeOrganisationRequestField(null);
        setupCaseFlagsIfRequired(caseData);
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
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
        setupCaseFlagsIfRequired(caseData);
        return getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getCaseData());
    }

    private void setupCaseFlagsIfRequired(CaseData caseData) {
        if (featureToggleService.isCaseFlagsEnabled() && caseFlagsService.caseFlagsSetupRequired(caseData)) {
            caseFlagsService.setupCaseFlags(caseData);
        }
    }

    private void setupCaseFlagsIfRespondentRepresentativeChanged(CallbackRequest callbackRequest) {
        CaseData caseData = callbackRequest.getCaseDetails().getCaseData();
        List<Integer> changedRepresentativeIndexes = changedRespondentRepresentativeIndexes(callbackRequest);
        if (featureToggleService.isCaseFlagsEnabled() && CollectionUtils.isNotEmpty(changedRepresentativeIndexes)) {
            caseFlagsService.clearRespondentRepresentativeFlags(caseData, changedRepresentativeIndexes);
            caseFlagsService.setupCaseFlags(caseData);
        }
    }

    private static List<Integer> changedRespondentRepresentativeIndexes(CallbackRequest callbackRequest) {
        if (callbackRequest.getCaseDetailsBefore() == null
                || callbackRequest.getCaseDetailsBefore().getCaseData() == null
                || CollectionUtils.isEmpty(callbackRequest.getCaseDetailsBefore().getCaseData().getRepCollection())
                || CollectionUtils.isEmpty(callbackRequest.getCaseDetails().getCaseData().getRepCollection())) {
            return List.of();
        }
        List<RepresentedTypeRItem> previousRepresentatives =
                callbackRequest.getCaseDetailsBefore().getCaseData().getRepCollection();
        List<RepresentedTypeRItem> currentRepresentatives = callbackRequest.getCaseDetails().getCaseData()
                .getRepCollection();
        return IntStream.range(0, currentRepresentatives.size())
                .filter(currentIndex -> previousRepresentatives.stream()
                        .filter(previousRepresentative -> sameRepresentative(previousRepresentative,
                                currentRepresentatives.get(currentIndex)))
                        .anyMatch(previousRepresentative -> representativeChanged(previousRepresentative,
                                currentRepresentatives.get(currentIndex))))
                .boxed()
                .distinct()
                .toList();
    }

    private static boolean representativeChanged(
            RepresentedTypeRItem previousRepresentative,
            RepresentedTypeRItem currentRepresentative) {
        return !Objects.equals(representativeName(previousRepresentative), representativeName(currentRepresentative))
                || !Strings.CI.equals(representativeEmail(previousRepresentative),
                representativeEmail(currentRepresentative));
    }

    private static boolean sameRepresentative(
            RepresentedTypeRItem previousRepresentative,
            RepresentedTypeRItem currentRepresentative) {
        if (previousRepresentative == null || currentRepresentative == null) {
            return false;
        }
        if (StringUtils.isNotBlank(representativeId(previousRepresentative))
                && StringUtils.isNotBlank(representativeId(currentRepresentative))) {
            return Objects.equals(representativeId(previousRepresentative), representativeId(currentRepresentative));
        }
        return Objects.equals(representativeRespondentId(previousRepresentative),
                representativeRespondentId(currentRepresentative))
                && Objects.equals(representativeRespondentName(previousRepresentative),
                representativeRespondentName(currentRepresentative));
    }

    private static String representativeName(RepresentedTypeRItem representative) {
        return representative == null || representative.getValue() == null
                ? null
                : representative.getValue().getNameOfRepresentative();
    }

    private static String representativeEmail(RepresentedTypeRItem representative) {
        return representative == null || representative.getValue() == null
                ? null
                : representative.getValue().getRepresentativeEmailAddress();
    }

    private static String representativeId(RepresentedTypeRItem representative) {
        return representative == null ? null : representative.getId();
    }

    private static String representativeRespondentId(RepresentedTypeRItem representative) {
        return representative == null || representative.getValue() == null
                ? null
                : representative.getValue().getRespondentId();
    }

    private static String representativeRespondentName(RepresentedTypeRItem representative) {
        return representative == null || representative.getValue() == null
                ? null
                : representative.getValue().getRespRepName();
    }
}
