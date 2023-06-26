package uk.gov.hmcts.ethos.replacement.docmosis.controllers.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;

/**
 * REST Controller for Stub API to test API calls from an external network/service.
 */
@RequiredArgsConstructor
@RestController
@Slf4j
@SuppressWarnings({"PMD.UnnecessaryAnnotationValueElement"})
public class StubApiController {
    private final VerifyTokenService verifyTokenService;

    /**
     * Small API to test externally.
     * @param userToken authentication token used for IDAM
     * @return basic string
     */
    @GetMapping(value = "/stubApi")
    
    
    public String stubApi(@RequestHeader(value = HttpHeaders.AUTHORIZATION) String userToken) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return "Invalid token";
        }
        return "Oh wow, it actually works!";
    }

}
