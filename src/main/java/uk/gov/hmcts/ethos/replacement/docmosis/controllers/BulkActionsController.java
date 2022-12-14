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
import uk.gov.hmcts.ecm.common.model.helper.BulkCasesPayload;
import uk.gov.hmcts.ecm.common.model.helper.BulkRequestPayload;
import uk.gov.hmcts.et.common.model.bulk.BulkCallbackResponse;
import uk.gov.hmcts.et.common.model.bulk.BulkDocumentInfo;
import uk.gov.hmcts.et.common.model.bulk.BulkRequest;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.Helper;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BulkCreationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BulkSearchService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.BulkUpdateService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.DocumentGenerationService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.SubMultipleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ET1_ONLINE_CASE_SOURCE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.MANUALLY_CREATED_POSITION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SELECT_ALL_VALUE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SELECT_NONE_VALUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.BulkCreationService.BULK_CREATION_STEP;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.BulkCreationService.UPDATE_SINGLES_STEP;

@Slf4j
@RequiredArgsConstructor
@RestController
@SuppressWarnings({"PMD.TooManyMethods", "PMD.AvoidDuplicateLiterals", "PMD.UnnecessaryAnnotationValueElement",
    "PMD.LawOfDemeter", "PMD.ConfusingTernary", "PMD.ExcessiveImports"})
public class BulkActionsController {

    private static final String LOG_MESSAGE = "received notification request for bulk reference :    ";
    private static final String GENERATED_DOCUMENTS_URL = "Please download the documents from : ";
    private static final String INVALID_TOKEN = "Invalid Token {}";

    private final BulkCreationService bulkCreationService;
    private final BulkUpdateService bulkUpdateService;
    private final BulkSearchService bulkSearchService;
    private final DocumentGenerationService documentGenerationService;
    private final SubMultipleService subMultipleService;
    private final VerifyTokenService verifyTokenService;

    /**
     * This service Gets userToken as a parameter for security validation and bulkRequest data for retrieving
     * cases by ethos case reference and bulk case creation.
     * It is used to retrieve submit events by ethos case reference and build bulk case
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/createBulk", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "creates a bulk case. Retrieves cases by ethos case reference.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @Deprecated public ResponseEntity<BulkCallbackResponse> createBulk(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("CREATE BULK ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkCasesPayload bulkCasesPayload = bulkSearchService.bulkCasesRetrievalRequest(
                bulkRequest.getCaseDetails(), userToken, true);

        BulkRequestPayload bulkRequestPayload = bulkCreationService.bulkCreationLogic(
                bulkRequest.getCaseDetails(), bulkCasesPayload, userToken, BULK_CREATION_STEP);

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * This service Gets userToken as a parameter for security validation and bulkRequest data for retrieving
     * cases by ethos case reference(using Elastic search client) and bulk case creation.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/createBulkES", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "creates a bulk case. Retrieves cases by ethos case reference.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> createBulkES(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("CREATE BULKES ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        if (bulkRequest.getCaseDetails().getCaseData().getMultipleSource() == null
                || bulkRequest.getCaseDetails().getCaseData().getMultipleSource().trim().equals("")) {
            bulkRequest.getCaseDetails().getCaseData().setMultipleSource(MANUALLY_CREATED_POSITION);
        }

        BulkCasesPayload bulkCasesPayload = bulkSearchService.bulkCasesRetrievalRequestElasticSearch(
                bulkRequest.getCaseDetails(), userToken, true, true);

        BulkRequestPayload bulkRequestPayload = bulkCreationService.bulkCreationLogic(
                bulkRequest.getCaseDetails(), bulkCasesPayload, userToken, BULK_CREATION_STEP);

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * This service Gets userToken as a parameter for security validation and bulkRequest data for retrieving
     * cases by ethos case reference(using Elastic search client). Displays retrieved bulk info data.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/afterSubmittedBulk", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "display the bulk info.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> afterSubmittedBulk(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("AFTER SUBMITTED BULK ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        if (bulkRequest.getCaseDetails().getCaseData().getMultipleSource() != null
                && !bulkRequest.getCaseDetails().getCaseData().getMultipleSource().equals(ET1_ONLINE_CASE_SOURCE)) {
            BulkCasesPayload bulkCasesPayload = bulkSearchService.bulkCasesRetrievalRequestElasticSearch(
                    bulkRequest.getCaseDetails(), userToken, true, false);
            bulkCreationService.bulkCreationLogic(bulkRequest.getCaseDetails(), bulkCasesPayload, userToken,
                    UPDATE_SINGLES_STEP);
        }

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .data(bulkRequest.getCaseDetails().getCaseData())
                .confirmation_header("Updates are being processed...")
                .build());
    }

    /**
     * This service Gets userToken as a parameter for security validation and bulkRequest data for updating cases in
     * a bulk case, also updates cases in searchCollection.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/updateBulk", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "updates cases in a bulk case. Update cases in searchCollection by given fields.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> updateBulk(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("UPDATE BULK ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = bulkUpdateService.bulkUpdateLogic(bulkRequest.getCaseDetails(),
                userToken);

        bulkRequestPayload = bulkUpdateService.clearUpFields(bulkRequestPayload);

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * This service Gets userToken as a parameter for security validation and bulkRequest data for updating cases in
     * a bulk case, also updates multiple collection.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/updateBulkCase", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update a bulk case. Update the multiple collection.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> updateBulkCase(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("UPDATE BULK CASE IDS ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = bulkCreationService.bulkUpdateCaseIdsLogic(bulkRequest, userToken,
                false);

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * This service Gets userToken as a parameter for security validation and bulkRequest data for generating a bulk
     * of letters.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/generateBulkLetter", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "generate a bulk of letters.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> generateBulkLetter(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("GENERATE BULK LETTER ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkDocumentInfo bulkDocumentInfo = documentGenerationService.processBulkDocumentRequest(bulkRequest,
                userToken);
        bulkRequest.getCaseDetails().getCaseData().setDocMarkUp(bulkDocumentInfo.getMarkUps());
        documentGenerationService.clearUserChoicesForMultiples(bulkRequest.getCaseDetails());

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkDocumentInfo.getErrors())
                .data(bulkRequest.getCaseDetails().getCaseData())
                .build());
    }

    /**
     * This service Gets userToken as a parameter for security validation and bulkRequest data for generating a bulk
     * letter confirmation.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/generateBulkLetterConfirmation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "generate a bulk letter confirmation.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> generateBulkLetterConfirmation(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("GENERATE BULK LETTER CONFIRMATION ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .data(bulkRequest.getCaseDetails().getCaseData())
                .confirmation_header(GENERATED_DOCUMENTS_URL
                        + bulkRequest.getCaseDetails().getCaseData().getDocMarkUp())
                .build());
    }

    /**
     * This service Gets userToken as a parameter for security validation and bulkRequest data for searching cases
     * in a bulk case, also searches for cases in multipleCollection by provided parameters/fields.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/midSearchBulk", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "searches cases in a bulk case. Look for cases in multipleCollection by fields. "
            + "Mid event callback.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> midSearchBulk(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MID SEARCH BULK ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = bulkSearchService.bulkMidSearchLogic(
                bulkRequest.getCaseDetails(), false);

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * This service Gets userToken as a parameter for security validation and bulkRequest data for searching cases
     * in a bulk case, also searches for cases in multipleCollection by provided parameters/fields.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/searchBulk", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "searches cases in a bulk case. Look for cases in multipleCollection by fields.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> searchBulk(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("SEARCH BULK ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = bulkSearchService.bulkSearchLogic(bulkRequest.getCaseDetails());

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * This service Gets userToken as a parameter for security validation and bulkRequest data for sub multiple
     * collection filter creation to search for cases in multipleTypeItems list(mid event).
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/midCreateSubMultiple", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "create a sub multiple looking for cases in multipleCollection. Mid event callback.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> midCreateSubMultiple(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MID CREATE SUB MULTIPLE ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = bulkSearchService.bulkMidSearchLogic(
                bulkRequest.getCaseDetails(), true);

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * Creates a sub multiple filter for case search in multipleColletion.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/createSubMultiple", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "create a sub multiple looking for cases in multipleCollection.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> createSubMultiple(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("CREATE SUB MULTIPLE ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = subMultipleService.createSubMultipleLogic(
                bulkRequest.getCaseDetails());

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * Populates a dynamic list with all sub multiple names.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/subMultipleDynamicList", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populate a dynamic list with all sub multiple names.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> subMultipleDynamicList(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("SUB MULTIPLE DYNAMIC LIST ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = subMultipleService.populateSubMultipleDynamicListLogic(
                bulkRequest.getCaseDetails());

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * Populates a dynamic list with all sub multiple names and "Select all" as a default value.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/filterDefaultedAllDynamicList", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populate a dynamic list with all sub multiple names and Select All as default.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> filterDefaultedAllDynamicList(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("FILTER DEFAULTED ALL DYNAMIC LIST ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = subMultipleService.populateFilterDefaultedDynamicListLogic(
                bulkRequest.getCaseDetails(), SELECT_ALL_VALUE);

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * Populates a dynamic list with all sub multiple names, jurcodes and "None" as a default value.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/filterDefaultedNoneDynamicList", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populate a dynamic list with all sub multiple names and jurCodes and None as default.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> filterDefaultedNoneDynamicList(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("FILTER DEFAULTED NONE DYNAMIC LIST ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = subMultipleService.populateFilterDefaultedDynamicListLogic(
                bulkRequest.getCaseDetails(), SELECT_NONE_VALUE);

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * Populates sub multiple name and search collection filter.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/midUpdateSubMultiple", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "populates Sub multiple name and search collection for edit.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> midUpdateSubMultiple(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("MID UPDATE SUB MULTIPLE ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = subMultipleService.bulkMidUpdateLogic(bulkRequest.getCaseDetails());

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * Updates sub multiple collection name and the list of cases.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/updateSubMultiple1", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "update the name and the list of cases of a sub multiple.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> updateSubMultiple1(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("UPDATE SUB MULTIPLE ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = subMultipleService.updateSubMultipleLogic(bulkRequest.getCaseDetails());

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * Removes sub multiple references from multiple collection.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/deleteSubMultiple", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "delete a Sub multiple from a dynamic list.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> deleteSubMultiple(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("DELETE SUB MULTIPLE ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkRequestPayload bulkRequestPayload = subMultipleService.deleteSubMultipleLogic(bulkRequest.getCaseDetails());

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

    /**
     * Generates a multiple schedule from bulkRequest(using "Tornado" service).
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/generateBulkSchedule", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "generate a multiple schedule.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> generateBulkSchedule(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("GENERATE BULK SCHEDULE ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        BulkDocumentInfo bulkDocumentInfo = documentGenerationService.processBulkScheduleRequest(
                bulkRequest, userToken);

        if (bulkDocumentInfo.getErrors().isEmpty()) {
            bulkRequest.getCaseDetails().getCaseData().setDocMarkUp(bulkDocumentInfo.getMarkUps());
            return ResponseEntity.ok(BulkCallbackResponse.builder()
                    .data(bulkRequest.getCaseDetails().getCaseData())
                    .significant_item(Helper.generateSignificantItem(bulkDocumentInfo.getDocumentInfo() != null
                            ? bulkDocumentInfo.getDocumentInfo() : new DocumentInfo(), new ArrayList<>()))
                    .build());
        } else {
            return ResponseEntity.ok(BulkCallbackResponse.builder()
                    .errors(bulkDocumentInfo.getErrors())
                    .data(bulkRequest.getCaseDetails().getCaseData())
                    .build());
        }
    }

    /**
     * Generates a multiple schedule confirmation from bulkRequest.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/generateBulkScheduleConfirmation", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "generate a multiple schedule confirmation.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> generateBulkScheduleConfirmation(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("GENERATE BULK SCHEDULE CONFIRMATION ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .data(bulkRequest.getCaseDetails().getCaseData())
                .confirmation_header(
                        GENERATED_DOCUMENTS_URL + bulkRequest.getCaseDetails().getCaseData().getDocMarkUp())
                .build());
    }

    /**
     * Retrieves bulk cases(for preAcceptRequest), updates multiple type collection with "Accepted" state,
     * moving cases to the "Accepted" state.
     *
     * @param  userToken        Used for authorisation
     * @param  bulkRequest      Holds the bulkRequest and bulk caseData
     * @return ResponseEntity   It is an HTTPEntity response which has BulkCallbackResponse
     */
    @PostMapping(value = "/preAcceptBulk", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "accept a bulk of cases.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully",
            content = {
                @Content(mediaType = "application/json", schema = @Schema(implementation = CCDCallbackResponse.class))
            }),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<BulkCallbackResponse> preAcceptBulk(
            @RequestBody BulkRequest bulkRequest,
            @RequestHeader(value = "Authorization") String userToken) {
        log.info("PRE ACCEPT BULK ---> " + LOG_MESSAGE + bulkRequest.getCaseDetails().getCaseId());

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            log.error(INVALID_TOKEN, userToken);
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        List<SubmitEvent> submitEvents =
                bulkSearchService.retrievalCasesForPreAcceptRequest(bulkRequest.getCaseDetails(), userToken);

        BulkRequestPayload bulkRequestPayload = bulkUpdateService.bulkPreAcceptLogic(bulkRequest.getCaseDetails(),
                submitEvents, userToken, false);

        return ResponseEntity.ok(BulkCallbackResponse.builder()
                .errors(bulkRequestPayload.getErrors())
                .data(bulkRequestPayload.getBulkDetails().getCaseData())
                .build());
    }

}

