package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin.staff;

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
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.JudgeService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.SaveJudgeException;

import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin/staff")
@RequiredArgsConstructor
public class JudgeController {

    private final VerifyTokenService verifyTokenService;
    private final JudgeService judgeService;

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
}
