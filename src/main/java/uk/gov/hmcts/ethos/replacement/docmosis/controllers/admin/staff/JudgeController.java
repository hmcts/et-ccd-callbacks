package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.staff;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.JudgeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.SaveJudgeException;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
public class JudgeController {

    private final VerifyTokenService verifyTokenService;
    private final JudgeService judgeService;

    @PostMapping(value = "/initAddJudge", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Initial add Judge")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> initAddJudge(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("/initAddJudge");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        judgeService.initAddJudge(adminData);

        return CCDCallbackResponse.getCallbackRespEntityNoErrors(adminData);
    }

    @PostMapping(value = "/addJudge", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Add Judge")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> addJudge(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("/addJudge");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        try {
            judgeService.saveJudge(adminData);
        } catch (SaveJudgeException e) {
            return CCDCallbackResponse.getCallbackRespEntityErrors(Arrays.asList(e.getMessage()), adminData);
        }

        return CCDCallbackResponse.getCallbackRespEntityNoErrors(adminData);
    }

    @PostMapping(value = "/updateJudgeMidEventSelectOffice", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the dynamicList for judge when office and type selected")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> updateJudgeMidEventSelectOffice(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("/updateJudgeMidEventSelectOffice");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = judgeService.updateJudgeMidEventSelectOffice(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    @PostMapping(value = "/updateJudgeMidEventSelectJudge", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the judge code and name when dynamicList selected")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> updateJudgeMidEventSelectJudge(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("/updateJudgeMidEventSelectJudge");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = judgeService.updateJudgeMidEventSelectJudge(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    @PostMapping(value = "/updateJudge", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a judge")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> updateJudge(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        log.info("/updateJudge");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = judgeService.updateJudge(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    @PostMapping(value = "/deleteJudgeMidEventSelectOffice", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the dynamicList for judges when an office is selected")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> deleteJudgeMidEventSelectOffice(
        @RequestHeader("Authorization") String userToken,
        @RequestBody CCDRequest ccdRequest) {

        log.info("/deleteJudgeMidEventSelectOffice");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = judgeService.deleteJudgeMidEventSelectOffice(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    @PostMapping(value = "/deleteJudgeMidEventSelectJudge", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Populates the judge name when an item is selected from the judges dynamicList")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> deleteJudgeMidEventSelectJudge(
        @RequestHeader("Authorization") String userToken,
        @RequestBody CCDRequest ccdRequest) {

        log.info("/deleteJudgeMidEventSelectJudge");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = judgeService.deleteJudgeMidEventSelectJudge(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }

    @PostMapping(value = "/deleteJudge", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Deletes a judge")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> deleteJudge(
        @RequestHeader("Authorization") String userToken,
        @RequestBody CCDRequest ccdRequest) {

        log.info("/deleteJudge");

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        List<String> errors = judgeService.deleteJudge(adminData);

        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, adminData);
    }
}
