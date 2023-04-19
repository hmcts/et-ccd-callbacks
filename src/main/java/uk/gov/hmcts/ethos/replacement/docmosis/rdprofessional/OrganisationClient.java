package uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@FeignClient(name = "organisation-client", url = "${rd_professional.api.url}")
public interface OrganisationClient {

    @GetMapping(
        value = "/refdata/internal/v1/organisations",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    OrganisationsResponse getUserOrganisation(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader("s2s") String s2sToken //todo remove
    );
}