package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
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
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RepresentativeUtils;
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
     * Maps representatives to corresponding respondents within the given {@link CaseData}
     * and establishes their representation relationship.
     * <p>
     * This method performs the following steps:
     * <ul>
     *     <li>Validates that the case contains both representatives and respondents.</li>
     *     <li>Builds lookup maps for respondent IDs and respondent names to allow fast matching.</li>
     *     <li>Iterates through each representative and attempts to locate the matching respondent
     *         by respondent ID or respondent name.</li>
     *     <li>If a matching respondent is found, the representative–respondent relationship is
     *         established using {@code assignRepresentative}.</li>
     * </ul>
     * <p>
     * Invalid or incomplete representative/respondent entries are safely skipped.
     * No exceptions are thrown for unmatched representatives.
     *
     * @param caseData the case data containing respondent and representative collections
     * @throws GenericServiceException if representation assignment fails during processing
     */
    public void mapRepresentativesToRespondents(CaseData caseData) throws GenericServiceException {
        if (!RespondentUtils.hasRespondents(caseData)) {
            return;
        }
        Map<String, RespondentSumTypeItem> respondentsById = new HashMap<>();
        Map<String, RespondentSumTypeItem> respondentsByName = new HashMap<>();
        for (RespondentSumTypeItem respondent : caseData.getRespondentCollection()) {
            if (!RespondentUtils.isValidRespondent(respondent)) {
                continue;
            }
            String respondentId = respondent.getId();
            String respondentName = respondent.getValue().getRespondentName();
            if (StringUtils.isNotBlank(respondentId)) {
                respondentsById.put(respondentId, respondent);
            }
            if (StringUtils.isNotBlank(respondentName)) {
                respondentsByName.put(respondentName, respondent);
            }
        }
        for (RepresentedTypeRItem representative : caseData.getRepCollection()) {
            if (!RepresentativeUtils.isValidRespondentRepresentative(representative)) {
                continue;
            }
            String repRespondentId = representative.getValue().getRespondentId();
            String repRespondentName = representative.getValue().getRespRepName();
            if  (StringUtils.isNotBlank(repRespondentId) || StringUtils.isNotBlank(repRespondentName)) {
                setMatchingRespondent(caseData, respondentsById, respondentsByName, repRespondentId, repRespondentName,
                        representative);
            }
        }
    }

    private static void setMatchingRespondent(CaseData caseData,
                                              Map<String, RespondentSumTypeItem> respondentsById,
                                              Map<String, RespondentSumTypeItem> respondentsByName,
                                              String repRespondentId,
                                              String repRespondentName,
                                              RepresentedTypeRItem representative)
            throws GenericServiceException {
        RespondentSumTypeItem matchedRespondent = null;
        if (StringUtils.isNotBlank(repRespondentId)) {
            matchedRespondent = respondentsById.get(repRespondentId);
        }
        if (ObjectUtils.isEmpty(matchedRespondent) && StringUtils.isNotBlank(repRespondentName)) {
            matchedRespondent = respondentsByName.get(repRespondentName);
        }
        if (ObjectUtils.isNotEmpty(matchedRespondent)) {
            assignRepresentative(matchedRespondent, representative, caseData.getCcdID());
        }
    }

    /**
     * Establishes a representation relationship between a respondent and a representative
     * within a case.
     * <p>
     * This method performs validation on the provided respondent, representative, and
     * case reference number. If validation succeeds, it links the representative to the
     * respondent and updates the respondent's representation status to indicate that a
     * representative is now assigned.
     * <p>
     * Specifically, this method:
     * <ul>
     *     <li>Validates the input data using {@code validateRepresentation}.</li>
     *     <li>Sets the respondent's ID on the representative.</li>
     *     <li>Marks the respondent as currently represented.</li>
     *     <li>Associates the representative's ID with the respondent.</li>
     *     <li>Ensures the respondent is not marked as having their representative removed.</li>
     * </ul>
     *
     * @param respondent          the respondent who is being represented
     * @param representative      the representative assigned to the respondent
     * @param caseReferenceNumber the CCD case reference number associated with this representation
     * @throws GenericServiceException if validation fails or if representation cannot be established
     */
    public static void assignRepresentative(RespondentSumTypeItem respondent,
                                            RepresentedTypeRItem representative,
                                            String caseReferenceNumber) throws GenericServiceException {
        RepresentativeUtils.validateRepresentation(respondent, representative, caseReferenceNumber);
        representative.getValue().setRespondentId(respondent.getId());
        representative.getValue().setRespRepName(respondent.getValue().getRespondentName());
        respondent.getValue().setRepresentativeRemoved(NO);
        respondent.getValue().setRepresented(YES);
        respondent.getValue().setRepresentativeId(representative.getId());
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
     * {@link #resetRepresentation(RespondentSumTypeItem, String)}.
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

            boolean hasMatchingRepresentative = caseData.getRepCollection().stream()
                    .filter(Objects::nonNull)
                    .filter(rep -> rep.getValue() != null)
                    .anyMatch(rep ->
                            StringUtils.isNotBlank(rep.getValue().getRespondentId())
                                    && rep.getValue().getRespondentId().equals(respondentId)
                                    || StringUtils.isNotBlank(rep.getValue().getRespRepName())
                                    && rep.getValue().getRespRepName().equals(respondentName)
                    );

            if (!hasMatchingRepresentative) {
                resetRepresentation(respondent, caseData.getCcdID());
            }
        }
    }

    /**
     * Resets the representation details for the specified respondent within a case by
     * clearing all representation-related fields and marking the respondent as no longer
     * represented.
     * <p>
     * Before applying the reset, this method performs strict validation on the provided
     * respondent using {@link RespondentUtils#validateRespondent(RespondentSumTypeItem, String)}.
     * The validation ensures that:
     * <ul>
     *     <li>The respondent object itself is not null.</li>
     *     <li>The respondent has a non-blank identifier.</li>
     *     <li>The respondent contains a non-null value object.</li>
     *     <li>The respondent has a non-null and non-empty respondent name.</li>
     * </ul>
     * If any of these conditions fail, a {@link GenericServiceException} is thrown with
     * a descriptive, case-specific error message.
     * <p>
     * Once validation succeeds, this method:
     * <ul>
     *     <li>Marks the respondent as having a representative removed ({@code YES}).</li>
     *     <li>Marks the respondent as not represented ({@code NO}).</li>
     *     <li>Clears the associated representative identifier by setting it to {@code null}.</li>
     * </ul>
     * This method is typically invoked when a respondent’s representation is withdrawn,
     * terminated, or otherwise needs to be fully removed from the case.
     *
     * @param respondent          the respondent whose representation information is being reset
     * @param caseReferenceNumber the CCD case reference number associated with the operation
     * @throws GenericServiceException if the respondent fails validation or if the
     *                                 representation cannot be reset
     */
    public static void resetRepresentation(RespondentSumTypeItem respondent,
                                         String caseReferenceNumber) throws GenericServiceException {
        RespondentUtils.validateRespondent(respondent, caseReferenceNumber);
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
