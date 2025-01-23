package uk.gov.hmcts.ethos.replacement.docmosis.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateRequest;
import uk.gov.hmcts.et.common.model.bundle.BundleCreateResponse;
import uk.gov.hmcts.et.common.model.bundle.MultipleBundleCreateRequest;

@FeignClient(name = "bundle", url = "${em-ccd-orchestrator.api.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface BundleApiClient {
    // TODO - When Multiple is being worked on, switch this to api/async-stitch-ccd-bundles
    @PostMapping(value = "api/stitch-ccd-bundles", consumes = "application/json")
    BundleCreateResponse stitchMultipleBundle(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody MultipleBundleCreateRequest bundleCreateRequest
    );

    @PostMapping(value = "api/async-stitch-ccd-bundles", consumes = "application/json")
    BundleCreateResponse asyncStitchBundle(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody BundleCreateRequest bundleCreateRequest
    );
}
