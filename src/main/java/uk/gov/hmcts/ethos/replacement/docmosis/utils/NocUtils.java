package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_INVALID_CASE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_INVALID_RESPONDENT_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_SELECTED_RESPONDENT_NOT_FOUND;

public final class NocUtils {

    private NocUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Performs Notice of Change (NoC) validation against the provided {@link CaseData} instance.
     * This method applies all NoC-related validation rules in sequence and returns the first
     * encountered error. If all validations pass, an empty list is returned.
     *
     * <p>The validation consists of the following steps:</p>
     *
     * <ol>
     *     <li><b>Respondent presence validation:</b><br>
     *         Ensures that the case contains at least one respondent using
     *         {@link RespondentUtils#hasRespondents(CaseData)}.
     *         If no respondents are found, {@code ERROR_INVALID_CASE_DATA} is returned.</li>
     *
     *     <li><b>Duplicate respondent name validation:</b><br>
     *         Uses {@link RepresentativeUtils#hasDuplicateRespondentNames(List)} to check that
     *         no respondent name appears more than once across representatives.
     *         If duplicates are detected, the corresponding error list is returned.</li>
     *
     *     <li><b>Representative-to-respondent mapping validation:</b><br>
     *         Invokes {@link #validateRepresentativeRespondentMapping(List, List)} to ensure that
     *         every representative references a valid respondent. If any mapping is invalid,
     *         the matching error is returned.</li>
     * </ol>
     *
     * <p>
     * The method uses a fail-fast strategy: as soon as a validation rule fails, the corresponding
     * error list is returned. No further validation is performed after the first failure.
     * </p>
     *
     * <p>
     * This method assumes that if the representative list is non-empty, all representative entries
     * are structurally valid (i.e., contain properly populated {@code dynamicRespRepName} fields).
     * Only logical consistency is evaluated here.
     * </p>
     *
     * @param caseData the case data to validate for Notice of Change rules
     * @return a list containing a single validation error message if validation fails, or an empty
     *         list if all NoC validation rules pass
     */
    public static List<String> validateNocCaseData(CaseData caseData) {
        if (!RespondentUtils.hasRespondents(caseData)) {
            return List.of(ERROR_INVALID_CASE_DATA);
        }
        List<String> duplicateRespondentErrors =
                RepresentativeUtils.hasDuplicateRespondentNames(caseData.getRepCollection());
        if (!duplicateRespondentErrors.isEmpty()) {
            return duplicateRespondentErrors;
        }
        List<String> mappingErrors = validateRepresentativeRespondentMapping(
                caseData.getRepCollection(), caseData.getRespondentCollection());
        if (!mappingErrors.isEmpty()) {
            return mappingErrors;
        }
        return Collections.emptyList();
    }

    /**
     * Validates that each representative in the provided list is mapped to a valid respondent
     * from the supplied respondent collection. This ensures that every representative references
     * an existing respondent by name.
     *
     * <p>The validation process consists of two phases:</p>
     *
     * <ol>
     *     <li><b>Respondent validation and extraction:</b>
     *         <ul>
     *             <li>Each respondent is checked for structural validity.</li>
     *             <li>A respondent is considered invalid if the respondent item, its value,
     *                 or its respondent name is missing or blank.</li>
     *             <li>If any respondent is invalid, the method returns a list containing
     *                 {@code ERROR_INVALID_RESPONDENT_EXISTS}.</li>
     *             <li>All valid respondent names are collected into a set for fast lookup.</li>
     *         </ul>
     *     </li>
     *
     *     <li><b>Representative-to-respondent mapping validation:</b>
     *         <ul>
     *             <li>Each representative's selected respondent name (from
     *                 {@code dynamicRespRepName}) is checked against the set of valid respondent names.</li>
     *             <li>If a representative refers to a respondent name that does not exist in the
     *                 respondent list, a validation error is returned containing
     *                 {@code ERROR_SELECTED_RESPONDENT_NOT_FOUND}, formatted with the missing name.</li>
     *         </ul>
     *     </li>
     * </ol>
     *
     * <p>
     * If the representative list is empty, the method returns an empty list because there is
     * no mapping to validate. If all representatives reference valid respondents, the method
     * returns an empty list indicating successful validation.
     * </p>
     *
     * <p>
     * <b>Assumptions:</b>
     * <ul>
     *     <li>The respondent list contains at least one respondent.</li>
     *     <li>If the representatives list contains entries, each representative is assumed to be
     *         structurally valid and contains a populated {@code dynamicRespRepName} hierarchy.</li>
     * </ul>
     * </p>
     *
     * @param representatives the list of representatives to validate; may be empty
     * @param respondents     the list of respondents against which representative mappings are validated;
     *                        expected to contain at least one valid respondent
     * @return a list containing a single validation error message if an invalid respondent or
     *         invalid mapping is detected; otherwise an empty list if all mappings are valid
     */
    public static List<String> validateRepresentativeRespondentMapping(
            List<RepresentedTypeRItem> representatives,
            List<RespondentSumTypeItem> respondents) {
        if (CollectionUtils.isEmpty(representatives)) {
            return Collections.emptyList();
        }
        Set<String> respondentNames = new HashSet<>();
        for (RespondentSumTypeItem respondent : respondents) {
            if (ObjectUtils.isEmpty(respondent) || ObjectUtils.isEmpty(respondent.getValue())
                    || StringUtils.isBlank(respondent.getValue().getRespondentName())) {
                return List.of(ERROR_INVALID_RESPONDENT_EXISTS);
            }
            String respondentName = respondent.getValue().getRespondentName();
            respondentNames.add(respondentName);
        }
        for (RepresentedTypeRItem representative : representatives) {
            String selectedRespondentName = representative.getValue().getDynamicRespRepName().getValue().getLabel();
            if (!respondentNames.contains(selectedRespondentName)) {
                return List.of(String.format(ERROR_SELECTED_RESPONDENT_NOT_FOUND, selectedRespondentName));
            }
        }
        return Collections.emptyList();
    }

    /**
     * Validates whether the given {@link RepresentedTypeRItem} contains the minimum
     * required data to be considered a valid Notice of Change (NOC) representative.
     * <p>
     * A representative is considered valid if all the following are present and non-empty:
     * </p>
     * <ul>
     *     <li>The {@code representative} object itself</li>
     *     <li>A non-blank {@code id}</li>
     *     <li>A non-null {@code value} object</li>
     *     <li>A non-null {@code dynamicRespRepName} object within the value</li>
     *     <li>A non-null {@code value} object inside {@code dynamicRespRepName}</li>
     *     <li>A non-empty {@code label} inside the nested value object</li>
     * </ul>
     *
     * @param representative the representative object to validate
     * @return {@code true} if all required fields are present and non-empty; {@code false} otherwise
     */
    public static boolean isValidNocRepresentative(RepresentedTypeRItem representative) {
        return ObjectUtils.isNotEmpty(representative)
                && StringUtils.isNotBlank(representative.getId())
                && ObjectUtils.isNotEmpty(representative.getValue())
                && StringUtils.isNotBlank(representative.getValue().getNameOfRepresentative())
                && (ObjectUtils.isNotEmpty(representative.getValue().getDynamicRespRepName())
                && ObjectUtils.isNotEmpty(representative.getValue().getDynamicRespRepName().getValue())
                && StringUtils.isNotEmpty(representative.getValue().getDynamicRespRepName().getValue().getLabel()));
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
    public static void mapRepresentativesToRespondents(CaseData caseData, String submissionReference)
            throws GenericServiceException {
        if (!RespondentUtils.hasRespondents(caseData) || !RepresentativeUtils.hasRepresentatives(caseData)) {
            return;
        }
        Map<String, RespondentSumTypeItem> respondentsById = new HashMap<>();
        Map<String, RespondentSumTypeItem> respondentsByName = new HashMap<>();
        for (RespondentSumTypeItem respondent : caseData.getRespondentCollection()) {
            if (!RespondentUtils.isValidRespondent(respondent)) {
                continue;
            }
            respondentsByName.put(respondent.getValue().getRespondentName(), respondent);
            respondentsById.put(respondent.getId(), respondent);
        }
        for (RepresentedTypeRItem representative : caseData.getRepCollection()) {
            if (!isValidNocRepresentative(representative)) {
                continue;
            }
            String repRespondentId = representative.getValue().getRespondentId();
            String repRespondentName = representative.getValue().getDynamicRespRepName().getValue().getLabel();
            if  (StringUtils.isNotBlank(repRespondentId) || StringUtils.isNotBlank(repRespondentName)) {
                setMatchingRespondent(respondentsById, respondentsByName, repRespondentId,
                        repRespondentName, representative, submissionReference);
            }
        }
    }

    private static void setMatchingRespondent(Map<String, RespondentSumTypeItem> respondentsById,
                                              Map<String, RespondentSumTypeItem> respondentsByName,
                                              String repRespondentId,
                                              String repRespondentName,
                                              RepresentedTypeRItem representative,
                                              String submissionReference)
            throws GenericServiceException {
        RespondentSumTypeItem matchedRespondent = null;
        if (StringUtils.isNotBlank(repRespondentId)) {
            matchedRespondent = respondentsById.get(repRespondentId);
        }
        if (ObjectUtils.isEmpty(matchedRespondent) && StringUtils.isNotBlank(repRespondentName)) {
            matchedRespondent = respondentsByName.get(repRespondentName);
        }
        if (ObjectUtils.isNotEmpty(matchedRespondent)) {
            assignRepresentative(matchedRespondent, representative, submissionReference);
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
     * @param submissionReference the CCD case reference number associated with this representation
     * @throws GenericServiceException if validation fails or if representation cannot be established
     */
    public static void assignRepresentative(RespondentSumTypeItem respondent,
                                            RepresentedTypeRItem representative,
                                            String submissionReference) throws GenericServiceException {
        RepresentativeUtils.validateRepresentation(respondent, representative, submissionReference);
        representative.getValue().setRespondentId(respondent.getId());
        representative.getValue().setRespRepName(respondent.getValue().getRespondentName());
        respondent.getValue().setRepresentativeRemoved(NO);
        respondent.getValue().setRepresented(YES);
        respondent.getValue().setRepresentativeId(representative.getId());
    }

    /**
     * Updates the given collection of representatives to ensure that all non-MyHMCTS
     * representatives have a valid non-MyHMCTS organisation identifier.
     *
     * <p>This method performs the following logic for each representative in the list:</p>
     * <ul>
     *     <li>If the representative or its value object is {@code null}, it is skipped.</li>
     *     <li>If the representative is marked as a MyHMCTS user ({@code myHmctsYesNo = YES}),
     *         the method stops processing and returns immediately.</li>
     *     <li>Otherwise, the representative is explicitly marked as non-MyHMCTS
     *         ({@code myHmctsYesNo = NO}).</li>
     *     <li>If the representative does not already have a
     *         {@code nonMyHmctsOrganisationId}, a new UUID is generated and assigned.</li>
     * </ul>
     *
     * <p>
     * If the input list is {@code null} or empty, the method performs no action.
     * </p>
     *
     * @param representatives the list of representatives to update;
     *                                 may be {@code null} or empty
     */
    public static void assignNonMyHmctsOrganisationIds(List<RepresentedTypeRItem> representatives) {
        if (CollectionUtils.isEmpty(representatives)) {
            return;
        }
        for (RepresentedTypeRItem representative : representatives) {
            if (representative == null
                    || representative.getValue() == null
                    || YES.equals(representative.getValue().getMyHmctsYesNo())) {
                continue;
            }
            representative.getValue().setMyHmctsYesNo(NO);
            if (StringUtils.isBlank(representative.getValue().getNonMyHmctsOrganisationId())) {
                representative.getValue().setNonMyHmctsOrganisationId(UUID.randomUUID().toString());
            }
        }
    }
}
