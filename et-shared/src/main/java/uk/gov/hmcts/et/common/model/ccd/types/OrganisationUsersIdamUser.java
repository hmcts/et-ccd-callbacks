package uk.gov.hmcts.et.common.model.ccd.types;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class OrganisationUsersIdamUser {
    @CCD(label = "User Id", showCondition = "firstName=\"dummy\"")
    private String userIdentifier;
    @CCD(label = "First name")
    private String firstName;
    @CCD(label = "Last name")
    private String lastName;
    @CCD(label = "Email address")
    private String email;
    @CCD(label = "Idam Status", showCondition = "firstName=\"dummy\"")
    private String idamStatus;
}
