package uk.gov.hmcts.ethos.replacement.docmosis.rdcommondata;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseflags.CaseFlagReferenceDataResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "commondata-organisation-client", url = "${rd_commondata-api-url}")
public interface OrganisationClient {

    @GetMapping(
            value = "/refdata/commondata/caseflags/service-id={serviceId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    CaseFlagReferenceDataResponse getCaseFlags(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
            @PathVariable("serviceId") String serviceId,
            @RequestParam("flag-type") String flagType
    );

}
