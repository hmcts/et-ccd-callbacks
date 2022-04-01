package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDCallbackResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.CCDRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin/create")
@RequiredArgsConstructor
public class CreateController {

    static final String ADMIN_CASE_NAME = "ECM Admin";

    private final VerifyTokenService verifyTokenService;

    @PostMapping(value = "/aboutToSubmitEvent", consumes = APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Admin Case: About to Submit Event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Accessed successfully"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    public ResponseEntity<CCDCallbackResponse> handleAboutToSubmitEvent(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        var adminData = ccdRequest.getCaseDetails().getAdminData();
        adminData.setName(ADMIN_CASE_NAME);

        return CCDCallbackResponse.getCallbackRespEntityNoErrors(ccdRequest.getCaseDetails().getAdminData());
    }
}
