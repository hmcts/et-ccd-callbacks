package uk.gov.hmcts.ethos.replacement.docmosis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;

import java.util.Map;
import java.util.Optional;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class RespondentRepresentativeService {
    private final NoticeOfChangeFieldPopulator nocFieldPopulator;

    private final UserService userService;

    private final ObjectMapper mapper;

    public Map<String, Object> getCaseDataAsMap(CaseData caseData) {
        return mapper.convertValue(caseData, Map.class);
    }

    public CaseData getCaseDataFromMap(Map<String, Object> caseDataMap) {
        return mapper.convertValue(caseDataMap, CaseData.class);
    }

    public CaseData prepopulateOrgPolicyAndNoc(CaseData caseData) {
        Map<String, Object> caseDataAsMap = getCaseDataAsMap(caseData);
        Map<String, Object> generatedContent =
            nocFieldPopulator.generate(caseData);
        caseDataAsMap.putAll(generatedContent);
        return getCaseDataFromMap(caseDataAsMap);
    }

    public CaseData updateRepresentation(CaseData caseData, String userToken) {
        Map<String, Object> caseDataAsMap = getCaseDataAsMap(caseData);
        Map<String, Object> repCollection = updateRepresentationMap(caseData, userToken);
        caseDataAsMap.putAll(repCollection);
        return getCaseDataFromMap(caseDataAsMap);
    }

    private Map<String, Object> updateRepresentationMap(CaseData caseData, String userToken) {
        UserDetails userDetails = userService.getUserDetails(userToken);

        final ChangeOrganisationRequest change = caseData.getChangeOrganisationRequestField();

        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }

        final SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getSelectedCode()).orElseThrow();

        RepresentedTypeR container = caseData.getRepCollection().get(role.getIndex()).getValue();

        RepresentedTypeR addedSolicitor = RepresentedTypeR.builder()
            .nameOfRepresentative(String.join(" ", userDetails.getFirstName(), userDetails.getLastName()))
            .respondentOrganisation(change.getOrganisationToAdd())
            .respRepName(container.getRespRepName())
            .myHmctsYesNo("Yes")
            .build();

        caseData.getRepCollection().get(role.getIndex()).setValue(addedSolicitor);

        return Map.of(SolicitorRole.CASE_FIELD, caseData.getRepCollection());

    }

    public RespondentSumType getRepresentative(String respName, CaseData caseData) {
        return caseData.getRespondentCollection().stream()
            .filter(respondent -> respondent.getValue().getRespondentName().equals(respName))
            .findFirst().map(RespondentSumTypeItem::getValue)
            .orElse(new RespondentSumType());
    }

    public SolicitorRole getCaseRole(DynamicFixedListType caseRoleId) {
        return Optional.ofNullable(caseRoleId)
            .map(DynamicFixedListType::getSelectedCode)
            .map(SolicitorRole::from)
            .map(Optional::orElseThrow)
            .orElse(null);
    }
}
