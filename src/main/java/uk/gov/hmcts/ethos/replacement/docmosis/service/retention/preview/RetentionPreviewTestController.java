package uk.gov.hmcts.ethos.replacement.docmosis.service.retention.preview;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.ethos.replacement.docmosis.service.retention.RetentionTaskResult;

import java.util.List;

@RestController
@RequestMapping("/testing/retention")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "retention.previewTesting", name = "enabled", havingValue = "true")
public class RetentionPreviewTestController {
    private static final String TOKEN_HEADER = "X-Retention-Test-Token";

    private final RetentionPreviewTestService previewTestService;

    @Value("${retention.previewTesting.token:}")
    private String token;

    @PostMapping("/seed")
    public RetentionPreviewTestService.SeedResponse seed(
        @RequestHeader(name = TOKEN_HEADER, required = false) String requestToken,
        @RequestBody(required = false) RetentionPreviewTestService.SeedRequest request
    ) {
        requireToken(requestToken);
        return previewTestService.seed(request);
    }

    @PostMapping("/expire")
    public int expire(
        @RequestHeader(name = TOKEN_HEADER, required = false) String requestToken,
        @RequestBody RetentionPreviewTestService.ExpireRequest request
    ) {
        requireToken(requestToken);
        return previewTestService.expire(request);
    }

    @PostMapping("/run")
    public RetentionTaskResult run(
        @RequestHeader(name = TOKEN_HEADER, required = false) String requestToken,
        @RequestBody RetentionPreviewTestService.RunRequest request
    ) {
        requireToken(requestToken);
        try {
            return previewTestService.run(request);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    @GetMapping("/cases")
    public List<RetentionPreviewTestService.PreviewCase> cases(
        @RequestHeader(name = TOKEN_HEADER, required = false) String requestToken,
        @RequestParam(required = false) String runId
    ) {
        requireToken(requestToken);
        return previewTestService.list(runId);
    }

    @DeleteMapping("/cases")
    public int cleanup(
        @RequestHeader(name = TOKEN_HEADER, required = false) String requestToken,
        @RequestParam(required = false) String runId
    ) {
        requireToken(requestToken);
        return previewTestService.cleanup(runId);
    }

    private void requireToken(String requestToken) {
        if (StringUtils.isNotBlank(token) && !token.equals(requestToken)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
