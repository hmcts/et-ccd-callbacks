package uk.gov.hmcts.ethos.replacement.docmosis.controllers.admin;

import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.ethos.replacement.docmosis.service.admin.CreateService;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin/create")
@RequiredArgsConstructor
public class CreateController {

    static final String ADMIN_CASE_NAME = "ECM Admin";

    private final VerifyTokenService verifyTokenService;
    private final CreateService createService;

    @PostMapping(value = "/aboutToSubmitEvent", consumes = APPLICATION_JSON_VALUE)
    
    
    public ResponseEntity<CCDCallbackResponse> handleAboutToSubmitEvent(
            @RequestHeader("Authorization") String userToken,
            @RequestBody CCDRequest ccdRequest) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        AdminData adminData = ccdRequest.getCaseDetails().getAdminData();
        adminData.setName(ADMIN_CASE_NAME);
        List<String> errors = createService.initCreateAdmin(userToken);
        return CCDCallbackResponse.getCallbackRespEntityErrors(errors, ccdRequest.getCaseDetails().getAdminData());
    }
}
