package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
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
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationApprovalStatus.APPROVED;

@Component
@Slf4j
@SuppressWarnings({"PMD.ConfusingTernary"})
public class NocRespondentHelper {
    public Map<String, Organisation> getRespondentOrganisations(CaseData caseData) {
        List<RepresentedTypeRItem> repCollection = caseData.getRepCollection();
        List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();

        return respondentCollection.stream().collect(
                HashMap::new,
                (container, respondent) ->
                        container.put(respondent.getId(), getOrgFromRep(respondent, repCollection)),
                HashMap::putAll
        );
    }

    public Organisation getOrgFromRep(RespondentSumTypeItem respondent, List<RepresentedTypeRItem> repCollection) {
        return emptyIfNull(repCollection).stream()
                .filter(rep -> validateRespondent(respondent, rep.getValue()))
                .filter(rep -> rep.getValue().getRespondentOrganisation() != null)
                .filter(rep -> isNotEmpty(rep.getValue().getRespondentOrganisation().getOrganisationID()))
                .map(rep -> rep.getValue().getRespondentOrganisation())
                .findFirst()
                .orElse(null);
    }
    
    public static boolean validateRespondent(RespondentSumTypeItem respondentSumTypeItem,
                                             RepresentedTypeR representedTypeR) {
        if (representedTypeR.getRespondentId() != null) {
            return respondentSumTypeItem.getId().equals(representedTypeR.getRespondentId());
        } else {
            return respondentSumTypeItem.getValue().getRespondentName().equals(representedTypeR.getRespRepName());
        }
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

    /**
     * Removes representation from respondents who no longer have a matching representative
     * in the provided {@link CaseData}.
     * <p>
     * This method iterates through each respondent and determines whether a representative
     * exists that matches the respondent either by:
     * <ul>
     *     <li>respondent ID, or</li>
     *     <li>respondent name</li>
     * </ul>
     * using the details present in the representative records.
     * <p>
     * A respondent is considered <em>unmatched</em> if no representative in the case has:
     * <ul>
     *     <li>a {@code respondentId} equal to the respondent's ID, nor</li>
     *     <li>a {@code respRepName} equal to the respondent's name</li>
     * </ul>
     * When an unmatched respondent is found, their representation is cleared by invoking
     * {@link #resetRepresentation(RespondentSumTypeItem)}.
     * <p>
     * Invalid or incomplete respondent and representative entries are safely skipped.
     * No action is taken if the case contains no respondents or no representatives.
     *
     * @param caseData the case data containing respondents and representatives from which
     *                 unmatched representation entries should be removed
     * @throws GenericServiceException if resetting a respondent's representation fails
     */
    public void removeUnmatchedRepresentations(CaseData caseData) throws GenericServiceException {
        if (!RespondentUtils.hasRespondents(caseData)) {
            return;
        }

        for (RespondentSumTypeItem respondent : caseData.getRespondentCollection()) {
            if (!RespondentUtils.isValidRespondent(respondent)) {
                continue;
            }

            String respondentId = respondent.getId();
            String respondentName = respondent.getValue().getRespondentName();

            boolean hasMatchingRepresentative = emptyIfNull(caseData.getRepCollection()).stream()
                    .filter(Objects::nonNull)
                    .filter(rep -> rep.getValue() != null)
                    .anyMatch(rep ->
                            StringUtils.isNotBlank(rep.getValue().getRespondentId())
                                    && rep.getValue().getRespondentId().equals(respondentId)
                                    || StringUtils.isNotBlank(rep.getValue().getRespRepName())
                                    && rep.getValue().getRespRepName().equals(respondentName)
                    );

            if (!hasMatchingRepresentative) {
                resetRepresentation(respondent);
            }
        }
    }

    /**
     * Resets the representation status of the given respondent.
     *
     * <p>This method marks the representative as removed, sets the respondent as no longer represented,
     * and clears any associated representative ID.</p>
     *
     * <p>It is assumed that the {@code respondent} is not {@code null} and that
     * {@code respondent.getValue()} is not {@code null}.</p>
     *
     * @param respondent the {@link RespondentSumTypeItem} whose representation details are to be reset
     */
    public static void resetRepresentation(RespondentSumTypeItem respondent) {
        respondent.getValue().setRepresentativeRemoved(YES);
        respondent.getValue().setRepresented(NO);
        respondent.getValue().setRepresentativeId(null);
    }

    public void amendRespondentNameRepresentativeNames(CaseData caseData) {
        List<RepresentedTypeRItem> repCollection = new ArrayList<>();
        for (RepresentedTypeRItem respondentRep : emptyIfNull(caseData.getRepCollection())) {
            final List<RespondentSumTypeItem> respondentCollection = caseData.getRespondentCollection();
            Optional<RespondentSumTypeItem> matchedRespondent = respondentCollection.stream()
                    .filter(resp ->
                            resp.getId().equals(respondentRep.getValue().getRespondentId())).findFirst();

            matchedRespondent.ifPresent(respondent ->
                    updateRepWithRespondentDetails(respondent, respondentRep, respondentCollection));

            repCollection.add(respondentRep);
        }

        caseData.setRepCollection(repCollection);
    }

    public void updateRepWithRespondentDetails(RespondentSumTypeItem respondent, RepresentedTypeRItem respondentRep,
                                               List<RespondentSumTypeItem> respondents) {
        List<DynamicValueType> respondentNameList = DynamicListHelper.createDynamicRespondentName(
                respondents);

        DynamicFixedListType dynamicFixedListType = new DynamicFixedListType();
        dynamicFixedListType.setListItems(respondentNameList);
        DynamicValueType dynamicValueType = new DynamicValueType();
        dynamicValueType.setCode("R: " + respondent.getValue().getRespondentName());
        dynamicValueType.setLabel(respondent.getValue().getRespondentName());
        dynamicFixedListType.setValue(dynamicValueType);

        respondentRep.getValue().setDynamicRespRepName(dynamicFixedListType);
        respondentRep.getValue().setRespondentId(respondent.getId());
        respondentRep.getValue().setRespRepName(respondent.getValue().getRespondentName());
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
