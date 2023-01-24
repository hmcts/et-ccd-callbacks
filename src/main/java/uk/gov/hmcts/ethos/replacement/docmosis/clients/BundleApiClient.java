package uk.gov.hmcts.ethos.replacement.docmosis.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.et.common.model.ccd.bundle.BundleCreateRequest;
import uk.gov.hmcts.et.common.model.ccd.bundle.BundleCreateResponse;

@FeignClient(name = "bundle", url = "${bundle.api.url}",
        configuration = FeignClientProperties.FeignClientConfiguration.class)
public interface BundleApiClient {
    @PostMapping(value = "api/new-bundle", consumes = "application/json")
    BundleCreateResponse createBundleServiceRequest(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody BundleCreateRequest bundleCreateRequest
            );

    @PostMapping(value = "api/stitch-ccd-bundles", consumes = "application/json")
    BundleCreateResponse stitchBundle(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestHeader("ServiceAuthorization") String serviceAuthorization,
            @RequestBody BundleCreateRequest bundleCreateRequest
    );
}
