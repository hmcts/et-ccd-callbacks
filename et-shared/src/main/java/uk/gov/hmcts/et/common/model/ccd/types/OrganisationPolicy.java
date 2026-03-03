package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Jacksonized
@Builder
public class OrganisationPolicy {

    public OrganisationPolicy(String role) {
        this.setOrgPolicyCaseAssignedRole(role);
        this.setOrganisation(Organisation.builder().build());
    }

    @JsonProperty("Organisation")
    private Organisation organisation;

    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;

    @JsonProperty("OrgPolicyCaseAssignedRole")
    private String orgPolicyCaseAssignedRole;

    @JsonProperty("PrepopulateToUsersOrganisation")
    private String prepopulateToUsersOrganisation;
}