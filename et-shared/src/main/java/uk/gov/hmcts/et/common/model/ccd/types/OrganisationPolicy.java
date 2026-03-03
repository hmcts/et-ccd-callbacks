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
    @JsonProperty("Organisation")
    private Organisation organisation;

    @JsonProperty("OrgPolicyReference")
    private String orgPolicyReference;

    @JsonProperty("OrgPolicyCaseAssignedRole")
    private String orgPolicyCaseAssignedRole;

    @JsonProperty("PrepopulateToUsersOrganisation")
    private String prepopulateToUsersOrganisation;

    public static OrganisationPolicy createDefaultPolicyByRole(String role) {
        return OrganisationPolicy.builder()
                .orgPolicyCaseAssignedRole(role)
                .organisation(Organisation.builder().build())
                .build();
    }
}