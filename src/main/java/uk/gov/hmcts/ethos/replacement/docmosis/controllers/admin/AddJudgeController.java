package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.AddJudgeService;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AddJudgeController {

    private final VerifyTokenService verifyTokenService;
    private final AddJudgeService addJudgeService;

    @PostMapping(value = "/addJudge", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Add Judge")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accessed successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> addJudge(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {

        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        if (!addJudgeService.saveJudge(adminData)) {
            return ResponseEntity.status(HttpStatus.CONFLICT.value()).build();
        }

        return CCDCallbackResponse.getCallbackRespEntityNoErrors(adminData);
    }
}
