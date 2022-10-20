package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.RespondentPolicyConverter;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RespondentRepresentativeService {

    private final RespondentPolicyConverter respondentPolicyConverter;

    public Map<String, OrganisationPolicy> getOrganisationPolicies(CaseData caseData) {
        Map<String, OrganisationPolicy> data = new HashMap<>();

        for (int i = 0; i < caseData.getRepCollection().size(); i++) {
            RepresentedTypeR rep = caseData.getRepCollection().get(i).getValue();
            OrganisationPolicy organisationPolicy =
                respondentPolicyConverter.getOrganisationPolicy(rep);
            data.put(String.format("respondentOrganisationPolicy%d", i), organisationPolicy);
        }

        return data;
    }

    public void updateCaseWithOrganisationPolicyDetails(CaseData caseData,
                                                        Map.Entry<String, OrganisationPolicy> policy) {
        switch (policy.getKey()) {
            case "respondentOrganisationPolicy0":
                caseData.setRespondentOrganisationPolicy0(policy.getValue());
                break;
            case "respondentOrganisationPolicy1":
                caseData.setRespondentOrganisationPolicy1(policy.getValue());
                break;
            case "respondentOrganisationPolicy2":
                caseData.setRespondentOrganisationPolicy2(policy.getValue());
                break;
            case "respondentOrganisationPolicy3":
                caseData.setRespondentOrganisationPolicy3(policy.getValue());
                break;
            case "respondentOrganisationPolicy4":
                caseData.setRespondentOrganisationPolicy4(policy.getValue());
                break;
            case "respondentOrganisationPolicy5":
                caseData.setRespondentOrganisationPolicy5(policy.getValue());
                break;
            case "respondentOrganisationPolicy6":
                caseData.setRespondentOrganisationPolicy6(policy.getValue());
                break;
            case "respondentOrganisationPolicy7":
                caseData.setRespondentOrganisationPolicy7(policy.getValue());
                break;
            case "respondentOrganisationPolicy8":
                caseData.setRespondentOrganisationPolicy8(policy.getValue());
                break;
            case "respondentOrganisationPolicy9":
                caseData.setRespondentOrganisationPolicy9(policy.getValue());
                break;
            default:
                break;
        }
    }
}
