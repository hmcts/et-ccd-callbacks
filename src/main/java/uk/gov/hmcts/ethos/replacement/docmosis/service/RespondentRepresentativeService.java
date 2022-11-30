package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.NoticeOfChangeFieldPopulator;

import java.util.Map;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class RespondentRepresentativeService {
    private final NoticeOfChangeFieldPopulator noticeOfChangeFieldPopulator;

    private final UserService userService;

    private final CaseConverter caseConverter;

    public CaseData prepopulateOrgPolicyAndNoc(CaseData caseData) {
        Map<String, Object> caseDataAsMap = caseConverter.toMap(caseData);
        Map<String, Object> generatedContent =
            noticeOfChangeFieldPopulator.generate(caseData);
        caseDataAsMap.putAll(generatedContent);
        return  caseConverter.convert(caseDataAsMap, CaseData.class);
    }

    public CaseData updateRepresentation(CaseData caseData, String userToken) {
        Map<String, Object> caseDataAsMap = caseConverter.toMap(caseData);
        Map<String, Object> repCollection = updateRepresentationMap(caseData, userToken);
        caseDataAsMap.putAll(repCollection);
        return  caseConverter.convert(caseDataAsMap, CaseData.class);
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
            .representativeEmailAddress(userDetails.getEmail())
            .respondentOrganisation(change.getOrganisationToAdd())
            .respRepName(container.getRespRepName())
            .myHmctsYesNo("Yes")
            .build();

        caseData.getRepCollection().get(role.getIndex()).setValue(addedSolicitor);

        return Map.of(SolicitorRole.CASE_FIELD, caseData.getRepCollection());

    }

    public RespondentSumType getRespondent(String respName, CaseData caseData) {
        return caseData.getRespondentCollection().stream()
            .filter(respondent -> respondent.getValue().getRespondentName().equals(respName))
            .findFirst().map(RespondentSumTypeItem::getValue)
            .orElse(new RespondentSumType());
    }
}
