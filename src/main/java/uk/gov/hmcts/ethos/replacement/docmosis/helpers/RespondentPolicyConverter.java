package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.util.Optional;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
public class RespondentPolicyConverter {
    /**
     * Creates a respondent organisation policy with a given case role for a respondent.
     * @param solicitorRole solicitor role enum containing case role
     * @param respondentRepresentative respondent representative
     * @return organisation policy for a respondent representative
     */
    public OrganisationPolicy generate(SolicitorRole solicitorRole,
                                       Optional<RepresentedTypeRItem> respondentRepresentative) {
        return OrganisationPolicy.builder()
            .organisation(getOrganisation(respondentRepresentative))
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

    private Organisation getOrganisation(Optional<RepresentedTypeRItem> respondentRepresentative) {
        return respondentRepresentative.map(RepresentedTypeRItem::getValue).filter(representedTypeR ->
                isNotEmpty(representedTypeR.getRespondentOrganisation()))
            .map(RepresentedTypeR::getRespondentOrganisation)
            .orElse(Organisation.builder().build());
    }
}
