package uk.gov.hmcts.ethos.replacement.docmosis.idam;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.TokenResponse;

import java.util.List;

@FeignClient(name = "idam-api", url = "${idam.api.url}")
public interface IdamApi {
    @GetMapping("/o/userinfo")
    UserDetails retrieveUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);

    @GetMapping("/api/v1/users/{userId}")
    UserDetails getUserByUserId(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @PathVariable("userId") String userId
    );

    @PostMapping(
        value = "/o/token",
        consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    TokenResponse generateOpenIdToken(@RequestBody TokenRequest tokenRequest);


    @GetMapping("/api/v1/users")
    List<UserDetails> searchUsersByQuery(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
            @RequestParam("query") String query,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size
    );
}
