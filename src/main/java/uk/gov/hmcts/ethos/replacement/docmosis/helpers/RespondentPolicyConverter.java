package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
public class RespondentPolicyConverter {
    public OrganisationPolicy generate(SolicitorRole solicitorRole,
                                       Optional<RepresentedTypeRItem> optionalRespondentElement) {
        return OrganisationPolicy.builder()
            .organisation(getOrganisation(optionalRespondentElement))
            .orgPolicyCaseAssignedRole(solicitorRole.getCaseRoleLabel())
            .build();
    }

    private Organisation getOrganisation(Optional<RepresentedTypeRItem> optionalRespondentRepItem) {
        return optionalRespondentRepItem.map(RepresentedTypeRItem::getValue).filter(representedTypeR ->
                isNotEmpty(representedTypeR.getRespondentOrganisation()))
            .map(RepresentedTypeR::getRespondentOrganisation)
            .orElse(Organisation.builder().build());
    }
}
