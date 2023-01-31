package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.idam.models.UserDetails;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationApprovalStatus.APPROVED;

@Service
public class RespondentService {
    public Map<String, Organisation> getRespondentOrganisations(CaseData caseData) {
        List<RepresentedTypeRItem> repCollection = caseData.getRepCollection();
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();

        return respondentCollection.stream().collect(
            HashMap::new,
            (container, respondent) ->
                container.put(respondent.getId(), getOrgFromRep(respondent.getId(), repCollection)),
            HashMap::putAll
        );
    }

    public Organisation getOrgFromRep(String respId, List<RepresentedTypeRItem> repCollection) {
        return emptyIfNull(repCollection).stream()
            .filter(rep -> rep.getValue().getRespondentId().equals(respId))
            .filter(rep -> rep.getValue().getRespondentOrganisation() != null)
            .filter(rep -> isNotEmpty(rep.getValue().getRespondentOrganisation().getOrganisationID()))
            .map(rep -> rep.getValue().getRespondentOrganisation())
            .findFirst()
            .orElse(null);
    }

    public int getIndexOfRep(RespondentSumTypeItem respondent,
                              List<RepresentedTypeRItem> repCollection) {
        return IntStream.range(0, repCollection.size())
            .filter(index -> respondent.getId().equals(repCollection.get(index).getValue().getRespondentId()))
            .findFirst().orElse(-1);
    }

    public Optional<RepresentedTypeRItem> getRespondentRep(RespondentSumTypeItem respondent,
                                                                   List<RepresentedTypeRItem> repCollection) {
        return repCollection.stream()
            .filter(rep -> rep.getValue().getRespondentId().equals(respondent.getId())).findFirst();

    }

    public ChangeOrganisationRequest createChangeRequest(Organisation newOrganisation,
                                                                Organisation oldOrganisation,
                                                                SolicitorRole solicitorRole) {
        DynamicFixedListType roleItem = new DynamicFixedListType();
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode(solicitorRole.getCaseRoleLabel());
        dynamicValueType.setLabel(solicitorRole.getCaseRoleLabel());
        roleItem.setValue(dynamicValueType);

        return ChangeOrganisationRequest.builder()
            .approvalStatus(APPROVED)
            .requestTimestamp(LocalDateTime.now())
            .caseRoleId(roleItem)
            .organisationToRemove(oldOrganisation)
            .organisationToAdd(newOrganisation)
            .build();
    }

    public RespondentSumType getRespondent(String respName, CaseData caseData) {
        return caseData.getRespondentCollection().stream()
            .filter(respondent -> respondent.getValue().getRespondentName().equals(respName))
            .findFirst().map(RespondentSumTypeItem::getValue)
            .orElse(new RespondentSumType());
    }

    public void amendRespondentNameRepresentativeNames(CaseData caseData) {
        List<DynamicValueType> listItems = DynamicListHelper.createDynamicRespondentName(
            caseData.getRespondentCollection());

        List<RepresentedTypeRItem> repCollection = emptyIfNull(caseData.getRepCollection()).stream()
            .peek(respondentRep -> {
                final List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();
                Optional<RespondentSumTypeItem> matchedRespondent = respondentCollection.stream()
                    .filter(resp ->
                        resp.getId().equals(respondentRep.getValue().getRespondentId())).findFirst();

                matchedRespondent.ifPresent(respondent -> {
                    DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
                    dynamicFixedListType.setListItems(listItems);

                    DynamicValueType dynamicValueType = new DynamicValueType();
                    dynamicValueType.setCode("R: " + respondent.getValue().getRespondentName());
                    dynamicValueType.setLabel(respondent.getValue().getRespondentName());
                    dynamicFixedListType.setValue(dynamicValueType);

                    respondentRep.getValue().setDynamicRespRepName(dynamicFixedListType);
                    respondentRep.getValue().setRespondentId(respondent.getId());
                    respondentRep.getValue().setRespRepName(respondent.getValue().getRespondentName());
                });

            }).collect(toList());

        caseData.setRepCollection(repCollection);
    }

    public RepresentedTypeR generateNewRepDetails(ChangeOrganisationRequest change,
                                                     Optional<UserDetails> userDetails,
                                                  RespondentSumTypeItem respondent) {
        return RepresentedTypeR.builder()
            .nameOfRepresentative(userDetails
                .map(user -> String.join(" ", user.getFirstName(), user.getLastName()))
                .orElse(null))
            .representativeEmailAddress(userDetails.map(UserDetails::getEmail).orElse(null))
            .respondentOrganisation(change.getOrganisationToAdd())
            .respRepName(respondent.getValue().getRespondentName())
            .respondentId(respondent.getId())
            .myHmctsYesNo("Yes")
            .build();
    }
}
