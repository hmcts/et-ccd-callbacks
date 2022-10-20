package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
public class RespondentPolicyConverter {

    public static final String RESPONDENTSOLICITOR = "[RESPONDENTSOLICITOR]";

    public OrganisationPolicy getOrganisationPolicy(RepresentedTypeR representative) {

        OrganisationPolicy.OrganisationPolicyBuilder organisationPolicyBuilder = OrganisationPolicy.builder();
        Organisation respondentOrganisation = representative.getRespondentOrganisation();

        if (hasOrganisation(representative)) {
            organisationPolicyBuilder.organisation(respondentOrganisation);
        }

        organisationPolicyBuilder.orgPolicyCaseAssignedRole(RESPONDENTSOLICITOR);

        return organisationPolicyBuilder.build();
    }

    private boolean hasOrganisation(RepresentedTypeR representative) {
        return isNotEmpty(representative) && isNotEmpty(representative.getRespondentOrganisation());
    }
}
