package uk.gov.hmcts.ethos.replacement.docmosis.rdprofessional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.et.common.model.ccd.RetrieveOrgByIdResponse;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationUsersResponse;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.AccountIdByEmailResponse;

import java.util.List;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi.SERVICE_AUTHORIZATION;

@FeignClient(name = "organisation-client", url = "${rd_professional-api-url}")
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

    @GetMapping(
            value = "/refdata/internal/v1/organisations?id={orgId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<RetrieveOrgByIdResponse> getOrganisationById(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
            @PathVariable("orgId") String orgId
    );

    @GetMapping(
            value = "/refdata/internal/v1/organisations/orgDetails/{userId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<OrganisationsResponse> retrieveOrganisationDetailsByUserId(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
            @PathVariable("userId") String userId
    );

    @GetMapping(
        value = "/refdata/internal/v1/organisations/{orgId}/users?returnRoles=false",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<OrganisationUsersResponse> getOrganisationUsers(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
        @PathVariable("orgId") String orgId
    );

    @GetMapping(
            value = "/refdata/external/v1/organisations/users/accountId",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<AccountIdByEmailResponse> getAccountIdByEmail(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestHeader(SERVICE_AUTHORIZATION) String s2sToken,
            @RequestParam("email") String email
    );

}
