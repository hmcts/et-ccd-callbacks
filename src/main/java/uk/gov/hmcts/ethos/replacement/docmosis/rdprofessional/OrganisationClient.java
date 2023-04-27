package uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;

import java.util.List;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "organisation-client", url = "${rd_professional.api.url}")
public interface OrganisationClient {

    @GetMapping(
        value = "/refdata/external/v1/organisations/status/ACTIVE?address=true",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<OrganisationsResponse> getOrganisations(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken
    );

}
