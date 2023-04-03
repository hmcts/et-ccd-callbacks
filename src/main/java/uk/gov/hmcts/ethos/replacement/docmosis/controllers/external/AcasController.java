package uk.gov.hmcts.ethos.replacement.docmosis.controllers.external;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for ACAS to communicate with CCD through ET using Azure API Management.
 */
@RequiredArgsConstructor
@RestController
@Slf4j
public class AcasController {
    private final VerifyTokenService verifyTokenService;

    /**
     * Given a datetime, this method will return a list of caseIds which have been modified since the datetime
     * provided.
     * @param userToken used for IDAM Authentication
     * @param dateTime used for querying when a case was last updated
     * @return a list of case ids
     */
    @GetMapping(value = "/getLastModifiedCaseList")
    @Operation(summary = "Return a list of CCD case IDs from a provided date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Object> getLastModifiedCaseList(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String userToken,
            @RequestParam(name = "datetime")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        // TODO Refactor this code for the actual ES Query
        List<String> dummyResult = List.of("1111111001111111", "1111110000111111", "1111100000011111",
                "1111000000001111", "1110000000000111", "1100000000000011", "1000000000000001", "0000000000000000");
        LocalDateTime localDateTime = LocalDateTime.of(2022, 1, 1, 0, 0, 0);
        if (dateTime.isAfter(localDateTime)) {
            return new ResponseEntity<>(dummyResult.stream(), HttpStatus.OK);
        }

        return new ResponseEntity<>("No cases found", HttpStatus.OK);
    }

    /**
     * Given a list of case IDs, this API will return the case data for the ID specified.
     * @param userToken used for IDAM Authentication
     * @param caseIds list of case IDs to find
     * @return a list of cases in JSON format
     */
    @GetMapping(value = "/getCaseData")
    @Operation(summary = "Return case data from a list of provided IDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<Object> getCaseData(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION) String userToken,
            @RequestParam(name = "caseIds") List<String> caseIds) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (CollectionUtils.isEmpty(caseIds)) {
            return new ResponseEntity<>("No case ids entered", HttpStatus.OK);
        }
        // TODO Refactor this code for the actual ES Query
        List<SubmitEvent> submitEventList = getTestData();
        return new ResponseEntity<>(submitEventList, HttpStatus.OK);
    }

    // TODO Remove method once ES query is in place
    /**
     * Method to create some static data which is how we expect the ES query to return data once implemented.
     * @return a list of case data
     */
    private List<SubmitEvent> getTestData() {
        CaseData caseData = new CaseData();
        caseData.setClaimant("Harpreet Jhita");
        caseData.setRespondent("Nick West");
        caseData.setCaseNotes("Nick fired Harpreet for using var and nested for loops");

        SubmitEvent submitEvent = new SubmitEvent();
        submitEvent.setCaseData(caseData);
        submitEvent.setCaseId(1_234_567_891_234_567L);

        CaseData caseData2 = new CaseData();
        caseData2.setClaimant("Jack Reeve");
        caseData2.setRespondent("Nick West");
        caseData2.setCaseNotes("Nick got really angry at Jack for not doing JavaDocs");

        SubmitEvent submitEvent2 = new SubmitEvent();
        submitEvent2.setCaseData(caseData2);
        submitEvent2.setCaseId(1_234_567_891_234_567L);

        return List.of(submitEvent, submitEvent2);

    }

}