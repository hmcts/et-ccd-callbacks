package uk.gov.hmcts.ethos.replacement.docmosis.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ecm.common.model.helper.DefaultValues;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseManagementLocationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DefaultValuesReaderService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferDifferentCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferOfficeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferSameCountryService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferToEcmService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferUtils;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@RestController
@RequestMapping("/caseTransfer")
@Slf4j
public class CaseTransferController {

    private static final String LOG_MESSAGE = "{} received notification request for case reference : {}";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final VerifyTokenService verifyTokenService;
    private final CaseTransferSameCountryService caseTransferSameCountryService;
    private final CaseTransferDifferentCountryService caseTransferDifferentCountryService;
    private final CaseTransferToEcmService caseTransferToEcmService;
    private final DefaultValuesReaderService defaultValuesReaderService;
    private final CaseManagementLocationService caseManagementLocationService;
    private final FeatureToggleService featureToggleService;

    public CaseTransferController(VerifyTokenService verifyTokenService,
                                  CaseTransferSameCountryService caseTransferSameCountryService,
                                  CaseTransferDifferentCountryService caseTransferDifferentCountryService,
                                  CaseTransferToEcmService caseTransferToEcmService,
                                  DefaultValuesReaderService defaultValuesReaderService,
                                  CaseManagementLocationService caseManagementLocationService,
                                  FeatureToggleService featureToggleService) {
        this.verifyTokenService = verifyTokenService;
        this.caseTransferSameCountryService = caseTransferSameCountryService;
        this.caseTransferDifferentCountryService = caseTransferDifferentCountryService;
        this.caseTransferToEcmService = caseTransferToEcmService;
        this.defaultValuesReaderService = defaultValuesReaderService;
        this.caseManagementLocationService = caseManagementLocationService;
        this.featureToggleService = featureToggleService;
    }

    @PostMapping(value = "/initTransferToScotland", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initialise case for transfer to Scotland")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initTransferToScotland(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info(LOG_MESSAGE, "CASE TRANSFER INIT TRANSFER TO SCOTLAND ---> ", ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        CaseTransferOfficeService.populateTransferToScotlandOfficeOptions(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/initTransferToEnglandWales", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initialise case for transfer to England/Wales")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initTransferToEnglandWales(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info(LOG_MESSAGE, "CASE TRANSFER INIT TRANSFER TO ENGLAND/WALES---> ",
                ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData = ccdRequest.getCaseDetails().getCaseData();
        CaseTransferOfficeService.populateTransferToEnglandWalesOfficeOptions(caseData);

        return getCallbackRespEntityNoErrors(caseData);
    }

    @PostMapping(value = "/transferSameCountry", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Transfer a case to another office within the same country")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> transferSameCountry(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info(LOG_MESSAGE, "CASE TRANSFER SAME COUNTRY ---> ", ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = caseTransferSameCountryService.transferCase(ccdRequest.getCaseDetails(), userToken);
        ccdRequest.getCaseDetails().getCaseData().setSuggestedHearingVenues(null);

        if (featureToggleService.isHmcEnabled() || featureToggleService.isWorkAllocationEnabled()) {
            caseManagementLocationService.setCaseManagementLocationCode(ccdRequest.getCaseDetails().getCaseData());
            caseManagementLocationService.setCaseManagementLocation(ccdRequest.getCaseDetails().getCaseData());
        }

        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/transferSameCountryEccLinkedCase", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Transfer a ECC linked case to another office within the same country")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> transferSameCountryEccLinkedCase(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info(LOG_MESSAGE, "CASE TRANSFER SAME COUNTRY ECC LINKED CASE ---> ",
                ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = caseTransferSameCountryService.updateEccLinkedCase(ccdRequest.getCaseDetails(),
            userToken);
        ccdRequest.getCaseDetails().getCaseData().setSuggestedHearingVenues(null);

        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/transferDifferentCountry", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Transfer a case to another office in a different country")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> transferDifferentCountry(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {
        log.info(LOG_MESSAGE, "CASE TRANSFER DIFFERENT COUNTRY ---> ", ccdRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = caseTransferDifferentCountryService.transferCase(ccdRequest.getCaseDetails(), userToken);
        ccdRequest.getCaseDetails().getCaseData().setSuggestedHearingVenues(null);

        if (featureToggleService.isHmcEnabled()) {
            caseManagementLocationService.setCaseManagementLocationCode(ccdRequest.getCaseDetails().getCaseData());
        }

        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/transferToEcm", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Transfer a case to ECM")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<CCDCallbackResponse> transferToEcm(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader(HttpHeaders.AUTHORIZATION) String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<String> errors = caseTransferToEcmService.createCaseTransferToEcm(ccdRequest.getCaseDetails(), userToken);

        return getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getCaseData());
    }

    @PostMapping(value = "/assignCase", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "assigns a case to a tribunal office")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public ResponseEntity<CCDCallbackResponse> assignCase(
            @RequestBody CCDRequest ccdRequest,
            @RequestHeader("Authorization") String userToken) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        CaseData caseData =  ccdRequest.getCaseDetails().getCaseData();
        CaseTransferUtils.setCaseManagingOffice(caseData, ccdRequest.getCaseDetails().getCaseTypeId());

        if (featureToggleService.isHmcEnabled() || featureToggleService.isWorkAllocationEnabled()) {
            caseManagementLocationService.setCaseManagementLocationCode(caseData);
            caseManagementLocationService.setCaseManagementLocation(caseData);
        }

        DefaultValues defaultValues = defaultValuesReaderService.getDefaultValues(caseData.getManagingOffice());
        defaultValuesReaderService.setCaseData(caseData, defaultValues);
        FlagsImageHelper.buildFlagsImageFileName(ccdRequest.getCaseDetails());

        if (featureToggleService.isHmcEnabled()) {
            caseManagementLocationService.setCaseManagementLocationCode(caseData);
        }

        return getCallbackRespEntityNoErrors(caseData);
    }
}
