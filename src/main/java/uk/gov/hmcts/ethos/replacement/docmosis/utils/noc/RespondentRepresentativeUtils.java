package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.CaseUserAssignment;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_INVALID_REPRESENTATIVE_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXIST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_NOT_FOUND;

public final class RespondentRepresentativeUtils {

    private RespondentRepresentativeUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Validates that the provided {@link RepresentedTypeRItem} representative contains
     * all mandatory information required for Notice of Change (NoC) processing.
     *
     * <p>The method performs the following validation checks:</p>
     * <ul>
     *     <li>The representative object is not null or empty</li>
     *     <li>The representative has a non-blank identifier</li>
     *     <li>The representative contains a populated value object</li>
     * </ul>
     *
     * <p>If any of the above validations fail, a {@link GenericServiceException} is
     * thrown. The exception message includes the case reference number and details
     * describing the specific validation failure. Additional metadata—such as the
     * originating class name and method name—is also included to support clearer
     * diagnostic logging.</p>
     *
     * @param representative        the representative to validate
     * @param caseReferenceNumber   the case reference number used to enrich error messages
     *
     * @throws GenericServiceException if:
     *     <ul>
     *      <li>the representative object is null or empty</li>
     *      <li>the representative ID is blank or missing</li>
     *      <li>the representative details (value object) are missing</li>
     *     </ul>
     *     The exception includes a descriptive error message and relevant
     *     context for troubleshooting.
     */
    public static void validateRepresentative(RepresentedTypeRItem representative,
                                              String caseReferenceNumber) throws GenericServiceException {
        if (ObjectUtils.isEmpty(representative)) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_NOT_FOUND, caseReferenceNumber);
            throw new GenericServiceException(exceptionMessage);
        }
        if (StringUtils.isBlank(representative.getId())) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND, caseReferenceNumber);
            throw new GenericServiceException(exceptionMessage);
        }
        if (ObjectUtils.isEmpty(representative.getValue())) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXIST,
                    representative.getId(), caseReferenceNumber);
            throw new GenericServiceException(exceptionMessage);
        }
    }

    /**
     * Checks whether a representative item is valid.
     * <p>
     * A representative is considered valid if:
     * <ul>
     *   <li>The representative item itself is not {@code null}</li>
     *   <li>The representative has a non-blank ID</li>
     *   <li>The representative has a non-null value object</li>
     * </ul>
     *
     * @param representative the representative item to validate
     * @return {@code true} if the representative is non-null and contains a valid ID and value;
     *         {@code false} otherwise
     */
    public static boolean isValidRepresentative(RepresentedTypeRItem representative) {
        return ObjectUtils.isNotEmpty(representative)
                && StringUtils.isNotBlank(representative.getId())
                && ObjectUtils.isNotEmpty(representative.getValue());
    }

    /**
     * Validates that both the specified respondent and representative contain the
     * minimum required data necessary to establish or process a representation
     * relationship within a case.
     * <p>
     * This method performs two separate validation steps:
     * <ul>
     *     <li>Validates the respondent using
     *         {@link RespondentUtils#validateRespondent(RespondentSumTypeItem, String)}, ensuring that:
     *         <ul>
     *             <li>the respondent object is not {@code null},</li>
     *             <li>the respondent has a non-blank identifier,</li>
     *             <li>the respondent has a non-null value object, and</li>
     *             <li>the respondent's name is present.</li>
     *         </ul>
     *     </li>
     *     <li>Validates the representative using
     *         {@link RespondentRepresentativeUtils#validateRepresentative(RepresentedTypeRItem, String)},
     *         ensuring that the representative record contains the required structural data.</li>
     * </ul>
     * <p>
     * If either validation fails, a {@link GenericServiceException} is thrown containing
     * a descriptive message and contextual information related to the provided
     * {@code caseReferenceNumber}. No further processing occurs after a validation failure.
     *
     * @param respondent          the respondent whose details should be validated
     * @param representative      the representative to validate against the respondent
     * @param caseReferenceNumber the CCD case reference used for context in error reporting
     *
     * @throws GenericServiceException if the respondent or representative fails validation
     */
    public static void validateRepresentation(RespondentSumTypeItem respondent,
                                              RepresentedTypeRItem representative,
                                              String caseReferenceNumber) throws GenericServiceException {
        RespondentUtils.validateRespondent(respondent, caseReferenceNumber);
        validateRepresentative(representative, caseReferenceNumber);
    }

    /**
     * Validates that no two representatives are assigned to the same respondent name.
     * <p>
     * This method iterates through the list of representatives and extracts each representative's
     * associated respondent name. If an invalid representative entry is encountered (e.g., missing
     * value fields or an empty respondent name), a single-element list containing the appropriate
     * validation error message is returned.
     * </p>
     *
     * <p>
     * A {@link java.util.Set} is used to track previously encountered respondent names. If a
     * duplicate respondent name is found, the method returns an error list indicating that a
     * respondent has been assigned more than one representative.
     * </p>
     *
     * <p>
     * It is assumed that the {@code representatives} list is not empty and contains at least one
     * representative. The caller is responsible for ensuring that the list is populated before
     * invoking this method.
     * </p>
     *
     * @param representatives the list of representatives to validate; expected to be non-empty
     * @return a list containing a validation error message if a duplicate respondent name or an
     *         invalid representative is detected; otherwise, an empty list if validation succeeds
     */
    public static List<String> hasDuplicateRespondentNames(List<RepresentedTypeRItem> representatives) {
        if (CollectionUtils.isEmpty(representatives)) {
            return Collections.emptyList();
        }
        Set<String> existingRespondentNames = new HashSet<>();
        for (RepresentedTypeRItem representative : representatives) {
            if (!NocUtils.isValidNocRepresentative(representative)) {
                return List.of(ERROR_INVALID_REPRESENTATIVE_EXISTS);
            }
            String respondentName = representative.getValue().getDynamicRespRepName().getValue().getLabel();
            if (!existingRespondentNames.add(respondentName)) {
                return List.of(String.format(ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES, respondentName));
            }
        }
        return Collections.emptyList();
    }

    /**
     * Determines whether the given {@link CaseData} instance contains one or more
     * representatives in its representative collection.
     * <p>
     * The method returns {@code true} only if:
     * </p>
     * <ul>
     *     <li>The {@code caseData} object is not null or empty, and</li>
     *     <li>The representative collection ({@code repCollection}) is not null and not empty.</li>
     * </ul>
     *
     * @param caseData the case data object to evaluate
     * @return {@code true} if the case contains at least one representative; {@code false} otherwise
     */
    public static boolean hasRespondentRepresentative(CaseData caseData) {
        return ObjectUtils.isNotEmpty(caseData)
                && CollectionUtils.isNotEmpty(caseData.getRepCollection());
    }

    /**
     * Determines whether a representative can be removed from assignments.
     * <p>
     * A representative is eligible for removal if:
     * <ul>
     *   <li>The representative is valid</li>
     *   <li>The representative is marked as my HMCTS user</li>
     *   <li>A respondent organisation is present</li>
     *   <li>The respondent organisation has a non-empty organisation ID</li>
     *   <li>The representative has a non-empty email address</li>
     * </ul>
     *
     * @param representative the representative to be evaluated
     * @return {@code true} if the representative meets all criteria required for removal;
     *         {@code false} otherwise
     */
    public static boolean canModifyAccess(RepresentedTypeRItem representative) {
        return isValidRepresentative(representative)
                && YES.equals(representative.getValue().getMyHmctsYesNo())
                && ObjectUtils.isNotEmpty(representative.getValue().getRespondentOrganisation())
                && StringUtils.isNotEmpty(representative.getValue().getRespondentOrganisation()
                .getOrganisationID())
                && StringUtils.isNotEmpty(representative.getValue().getRepresentativeEmailAddress());
    }

    /**
     * Filters the given list of representatives and returns only those
     * for which access modification is permitted.
     *
     * <p>The original list is not modified. If the input list is {@code null}
     * or empty, an empty list is returned.</p>
     *
     * @param representatives the list of representatives to evaluate
     * @return a list containing only representatives that can have their access modified
     */
    public static List<RepresentedTypeRItem> filterModifiableRepresentatives(
            List<RepresentedTypeRItem> representatives) {
        if (CollectionUtils.isEmpty(representatives)) {
            return Collections.emptyList();
        }
        return representatives.stream()
                .filter(RespondentRepresentativeUtils::canModifyAccess)
                .toList();
    }

    /**
     * Determines whether two representatives refer to the same respondent.
     * <p>
     * The representatives are considered to refer to the same respondent if the
     * respondent ID of the old representative is present and matches the respondent
     * ID of the new representative.
     *
     * <p><strong>Assumptions:</strong>
     * <ul>
     *   <li>Both {@code oldRepresentative} and {@code newRepresentative} are non-null</li>
     *   <li>Both representatives have a non-null value object</li>
     *   <li>The new representative may or may not have a respondent ID</li>
     * </ul>
     *
     * @param oldRepresentative the existing representative containing the respondent ID
     * @param newRepresentative the updated representative to compare against
     * @return {@code true} if both representatives refer to the same respondent;
     *         {@code false} otherwise
     */
    public static boolean representsSameRespondent(RepresentedTypeRItem oldRepresentative,
                                                   RepresentedTypeRItem newRepresentative) {
        return StringUtils.isNotBlank(oldRepresentative.getValue().getRespondentId())
                && oldRepresentative.getValue().getRespondentId()
                .equals(newRepresentative.getValue().getRespondentId())
                || StringUtils.isNotBlank(oldRepresentative.getValue().getRespRepName())
                && oldRepresentative.getValue().getRespRepName().equals(newRepresentative.getValue().getRespRepName());
    }

    /**
     * Determines whether a representative’s respondent organisation has changed.
     * <p>
     * A change is considered to have occurred if there is any difference between the
     * old and new representatives in terms of the presence or value of the respondent
     * organisation or its organisation ID.
     * <p>
     * Specifically, this method returns {@code true} if:
     * <ul>
     *   <li>One representative has a respondent organisation and the other does not</li>
     *   <li>One representative has a blank organisation ID and the other has a non-blank organisation ID</li>
     *   <li>Both representatives have a non-blank organisation ID, but the values differ</li>
     * </ul>
     *
     * <p>If both representatives have no respondent organisation, or both have the same
     * non-blank organisation ID, the organisation is considered unchanged.
     *
     * <p><strong>Assumptions:</strong>
     * <ul>
     *   <li>Both {@code oldRepresentative} and {@code newRepresentative} are non-null</li>
     * </ul>
     *
     * @param oldRepresentative the existing representative to compare from
     * @param newRepresentative the updated representative to compare against
     * @return {@code true} if the respondent organisation has changed;
     *         {@code false} otherwise
     */
    public static boolean isRepresentativeOrganisationChanged(RepresentedTypeR oldRepresentative,
                                                              RepresentedTypeR newRepresentative) {
        if (ObjectUtils.isEmpty(oldRepresentative.getRespondentOrganisation())
                && ObjectUtils.isEmpty(newRepresentative.getRespondentOrganisation())) {
            return false;
        }
        if (isOrganisationObjectChanged(oldRepresentative, newRepresentative)) {
            return true;
        }
        return isOrganisationIdChanged(oldRepresentative, newRepresentative);
    }

    private static boolean isOrganisationObjectChanged(RepresentedTypeR oldRepresentative,
                                                        RepresentedTypeR newRepresentative) {
        return ObjectUtils.isEmpty(oldRepresentative.getRespondentOrganisation())
                && ObjectUtils.isNotEmpty(newRepresentative.getRespondentOrganisation())
                || ObjectUtils.isNotEmpty(oldRepresentative.getRespondentOrganisation())
                && ObjectUtils.isEmpty(newRepresentative.getRespondentOrganisation())
                || StringUtils.isNotBlank(oldRepresentative.getRespondentOrganisation().getOrganisationID())
                && StringUtils.isBlank(newRepresentative.getRespondentOrganisation().getOrganisationID())
                || StringUtils.isBlank(oldRepresentative.getRespondentOrganisation().getOrganisationID())
                && StringUtils.isNotBlank(newRepresentative.getRespondentOrganisation().getOrganisationID());
    }

    private static boolean isOrganisationIdChanged(RepresentedTypeR oldRepresentative,
                                                   RepresentedTypeR newRepresentative) {
        return ObjectUtils.isNotEmpty(oldRepresentative.getRespondentOrganisation())
                && ObjectUtils.isNotEmpty(newRepresentative.getRespondentOrganisation())
                && StringUtils.isNotBlank(oldRepresentative.getRespondentOrganisation().getOrganisationID())
                && StringUtils.isNotBlank(newRepresentative.getRespondentOrganisation().getOrganisationID())
                && !oldRepresentative.getRespondentOrganisation().getOrganisationID().equals(
                newRepresentative.getRespondentOrganisation().getOrganisationID());
    }

    public static boolean isRepresentativeEmailChanged(RepresentedTypeR oldRepresentative,
                                                        RepresentedTypeR newRepresentative) {
        return !Strings.CI.equals(oldRepresentative.getRepresentativeEmailAddress(), newRepresentative
                .getRepresentativeEmailAddress());
    }

    /**
     * Identifies representatives from the existing list that should be treated as changed
     * for the same respondent when compared against a new list of representatives.
     * <p>
     * A representative from {@code oldRepresentatives} is included in the returned list if:
     * <ul>
     *     <li>it is a valid representative, and</li>
     *     <li>no matching representative exists in {@code newRepresentatives} for the same respondent, or</li>
     *     <li>a matching representative exists but the organisation or email address has changed</li>
     * </ul>
     * <p>
     * Only valid representatives are considered during the comparison. If
     * {@code oldRepresentatives} is {@code null} or empty, an empty list is returned.
     *
     * @param oldRepresentatives the existing representatives to compare against
     * @param newRepresentatives the updated representatives to compare with
     * @return a list of representatives from {@code oldRepresentatives} that are either
     *         no longer present or have updated organisation or email details
     */
    public static List<RepresentedTypeRItem> findRepresentativesToRemove(
            List<RepresentedTypeRItem> oldRepresentatives, List<RepresentedTypeRItem> newRepresentatives) {
        if (CollectionUtils.isEmpty(oldRepresentatives)) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(newRepresentatives)) {
            return oldRepresentatives;
        }
        List<RepresentedTypeRItem> representativesToRemove = new ArrayList<>();
        for (RepresentedTypeRItem oldRepresentative : oldRepresentatives) {
            if (!RespondentRepresentativeUtils.isValidRepresentative(oldRepresentative)) {
                continue;
            }
            // to check if representative exists but its organisation or email is changed or not
            boolean hasRepresentativeContactDetailsChanged = false;
            // to check if representative exists or not
            boolean isMatchingValidRepresentative = false;
            for (RepresentedTypeRItem newRepresentative : newRepresentatives) {
                if (isMatchingValidRepresentative(oldRepresentative, newRepresentative)) {
                    isMatchingValidRepresentative = true;
                    // representative already exists but its organisation or email is changed
                    hasRepresentativeContactDetailsChanged = hasRepresentativeContactDetailsChanged(oldRepresentative,
                                newRepresentative);
                }
            }
            if (canRemoveRepresentative(isMatchingValidRepresentative, hasRepresentativeContactDetailsChanged)) {
                representativesToRemove.add(oldRepresentative);
            }
        }
        return representativesToRemove;
    }

    private static boolean isMatchingValidRepresentative(RepresentedTypeRItem oldRepresentative,
                                                         RepresentedTypeRItem newRepresentative) {
        return isValidRepresentative(newRepresentative)
                && representsSameRespondent(oldRepresentative, newRepresentative);
    }

    private static boolean hasRepresentativeContactDetailsChanged(
            RepresentedTypeRItem oldRepresentative, RepresentedTypeRItem newRepresentative) {
        return isRepresentativeOrganisationChanged(oldRepresentative.getValue(), newRepresentative.getValue())
                || isRepresentativeEmailChanged(oldRepresentative.getValue(), newRepresentative.getValue());
    }

    private static boolean canRemoveRepresentative(boolean  isMatchingValidRepresentative,
                                                   boolean hasRepresentativeContactDetailsChanged) {
        return !isMatchingValidRepresentative || hasRepresentativeContactDetailsChanged;
    }

    /**
     * Identifies representatives that are new or have been updated between two versions
     * of a case.
     * <p>
     * A representative is considered <strong>new or updated</strong> if, when compared
     * to the previous list:
     * <ul>
     *     <li>They represent a respondent that did not previously have a representative, or</li>
     *     <li>They represent the same respondent but either the representative's
     *     <strong>organisation</strong> or <strong>email address</strong> has changed</li>
     * </ul>
     * </p>
     * <p>
     * Representatives are considered unchanged only when they represent the same respondent
     * and both the organisation and email address remain the same.
     * </p>
     * <p>
     * Any invalid representatives are ignored during the comparison.
     * </p>
     *
     * @param newRepresentatives the list of representatives from the updated case data
     * @param oldRepresentatives the list of representatives from the previous case data
     * @return a list of representatives that are either new or have been updated due to
     *         a change in organisation or email address
     */
    public static List<RepresentedTypeRItem> findNewOrUpdatedRepresentatives(
            List<RepresentedTypeRItem> newRepresentatives, List<RepresentedTypeRItem> oldRepresentatives) {
        if (CollectionUtils.isEmpty(newRepresentatives)) {
            return new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(oldRepresentatives)) {
            return newRepresentatives;
        }
        List<RepresentedTypeRItem> newRepresentativesToReturn = new ArrayList<>();
        for (RepresentedTypeRItem newRepresentative : newRepresentatives) {
            if (!isValidRepresentative(newRepresentative)) {
                continue;
            }
            boolean representsSameRespondent = false;
            for (RepresentedTypeRItem oldRepresentative : oldRepresentatives) {
                if (isValidRepresentative(oldRepresentative)
                        && representsSameRespondent(oldRepresentative, newRepresentative)
                        && !isRepresentativeOrganisationChanged(oldRepresentative.getValue(),
                        newRepresentative.getValue())
                        && !isRepresentativeEmailChanged(oldRepresentative.getValue(), newRepresentative.getValue())) {
                    representsSameRespondent = true;
                    break;
                }
            }
            if (!representsSameRespondent) {
                newRepresentativesToReturn.add(newRepresentative);
            }
        }
        return newRepresentativesToReturn;
    }

    /**
     * Resolves the respondent name associated with the given representative.
     * <p>
     * If the representative already contains a respondent name, that value is returned.
     * Otherwise, the method attempts to derive the respondent name using the respondent
     * ID from the provided {@link CaseData}. If the respondent cannot be resolved, or if
     * the required data is missing or invalid, {@code UNKNOWN} is returned.
     *
     * @param caseData the case data containing the respondent collection
     * @param representative the representative whose associated respondent name is required
     * @return the resolved respondent name, or {@code UNKNOWN} if it cannot be determined
     */
    public static RespondentSumTypeItem findRespondentByRepresentative(CaseData caseData,
                                                                       RepresentedTypeRItem representative) {
        if (ObjectUtils.isEmpty(caseData)
                || CollectionUtils.isEmpty(caseData.getRespondentCollection())
                || ObjectUtils.isEmpty(representative)
                || ObjectUtils.isEmpty(representative.getValue())
                || StringUtils.isBlank(representative.getValue().getRespondentId())
                && StringUtils.isBlank(representative.getValue().getRespRepName())
                && StringUtils.isBlank(representative.getId())) {
            return null;
        }
        RespondentSumTypeItem respondent = RespondentUtils.findRespondentById(caseData.getRespondentCollection(),
                representative.getValue().getRespondentId());
        if (ObjectUtils.isEmpty(respondent)) {
            respondent = RespondentUtils.findRespondentByName(caseData.getRespondentCollection(),
                    representative.getValue().getRespRepName());
        }
        if (ObjectUtils.isEmpty(respondent)) {
            respondent = RespondentUtils.findRespondentByRepresentativeId(caseData.getRespondentCollection(),
                    representative.getId());
        }
        return respondent;
    }

    /**
     * Finds a valid {@link RepresentedTypeRItem} in the case data by its identifier.
     * <p>
     * This method searches the representative collection on the given {@link CaseData}
     * and returns the first representative whose identifier matches the supplied
     * {@code representativeId} and which passes validity checks.
     * </p>
     * <p>
     * If the case data is null or empty, the representative collection is empty,
     * the identifier is blank, or no matching valid representative is found,
     * the method returns {@code null}.
     * </p>
     *
     * @param caseData         the case data containing the representative collection
     * @param representativeId the identifier of the representative to find
     * @return the matching valid {@link RepresentedTypeRItem}, or {@code null} if none is found
     */
    public static RepresentedTypeRItem findRepresentativeById(CaseData caseData,
                                                              String representativeId) {
        if (ObjectUtils.isEmpty(caseData)
                || CollectionUtils.isEmpty(caseData.getRepCollection())
                || StringUtils.isBlank(representativeId)) {
            return null;
        }

        return caseData.getRepCollection().stream()
                .filter(RespondentRepresentativeUtils::isValidRepresentative)
                .filter(rep -> representativeId.equals(rep.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Extracts a list of valid respondent representative organisation IDs
     * from the given {@link CaseData}.
     *
     * <p>This method iterates through the respondent representative collection
     * contained in {@code caseData} and returns the organisation IDs for
     * representatives that meet all of the following conditions:
     * <ul>
     *     <li>The representative passes {@code isValidRepresentative(...)} validation.</li>
     *     <li>The representative has a non-null respondent organisation.</li>
     *     <li>The respondent organisation has a non-blank organisation ID.</li>
     * </ul>
     *
     * <p>Representatives that do not satisfy these conditions are ignored.
     *
     * <h3>Assumptions</h3>
     * <ul>
     *     <li>{@code caseData} is not {@code null}.</li>
     *     <li>{@code caseData.getRepCollection()} is not {@code null}
     *         (an empty collection is permitted).</li>
     *     <li>Each {@link RepresentedTypeRItem} in the collection is not {@code null}.</li>
     *     <li>{@code representative.getValue()} is not {@code null}.</li>
     * </ul>
     *
     * <p>If any of the above assumptions are violated, a {@link NullPointerException}
     * may be thrown.
     *
     * @param caseData the case data containing the respondent representative collection;
     *                 must not be {@code null}
     * @return a list of non-blank organisation IDs for valid respondent representatives;
     *         never {@code null}, but may be empty if no valid representatives are found
     */
    public static List<String> extractValidRespondentRepresentativeOrganisationIds(CaseData caseData) {
        List<String> respondentRepresentativeOrganisations = new ArrayList<>();
        for (RepresentedTypeRItem representative : caseData.getRepCollection()) {
            if (ObjectUtils.isEmpty(representative)
                    || ObjectUtils.isEmpty(representative.getValue())
                    || ObjectUtils.isEmpty(representative.getValue().getRespondentOrganisation())
                    || StringUtils.isBlank(representative.getValue().getRespondentOrganisation().getOrganisationID())) {
                continue;
            }
            respondentRepresentativeOrganisations.add(representative.getValue().getRespondentOrganisation()
                    .getOrganisationID());
        }
        return respondentRepresentativeOrganisations;
    }

    /**
     * Clears (sets to {@code null}) the role of the specified respondent representatives
     * within the given {@link CaseData}.
     *
     * <p>This method performs the following steps:
     * <ul>
     *     <li>Validates that the provided {@code caseData}, its representative collection,
     *     and the input {@code representatives} list are not empty.</li>
     *     <li>Iterates over the supplied representatives and validates each using
     *     {@code RespondentRepresentativeUtils.isValidRepresentative(...)}.</li>
     *     <li>For each valid representative, attempts to locate the corresponding
     *     representative in the case data by ID.</li>
     *     <li>If found, sets the representative's {@code role} field to {@code null}.</li>
     * </ul>
     *
     * <p>If any of the required inputs are missing or empty, the method exits
     * without making changes. If a representative cannot be found in the case data,
     * it is skipped. If an invalid representative is encountered in the input list,
     * processing stops immediately.
     *
     * @param caseData the case data containing the full representative collection
     * @param representatives the list of representatives whose roles should be cleared
     */
    public static void clearRolesForRepresentatives(CaseData caseData, List<RepresentedTypeRItem> representatives) {
        if (ObjectUtils.isEmpty(caseData)
                || CollectionUtils.isEmpty(caseData.getRepCollection())
                || CollectionUtils.isEmpty(representatives)) {
            return;
        }
        for (RepresentedTypeRItem representative : representatives) {
            if (!RespondentRepresentativeUtils.isValidRepresentative(representative)) {
                return;
            }
            RepresentedTypeRItem tmpRepresentative = findRepresentativeById(caseData, representative.getId());
            if (ObjectUtils.isEmpty(tmpRepresentative)) {
                continue;
            }
            tmpRepresentative.getValue().setRole(null);
        }
    }

    /**
     * Determines whether the provided {@link CaseData} contains
     * a non-empty collection of representatives.
     *
     * <p>This method returns {@code true} only if:
     * <ul>
     *     <li>The {@code caseData} object is not {@code null} or empty, and</li>
     *     <li>The representative collection within {@code caseData} is not {@code null} or empty.</li>
     * </ul>
     *
     * @param caseData the case data to evaluate; may be {@code null}
     * @return {@code true} if the case data contains one or more representatives,
     *         otherwise {@code false}
     */
    public static boolean hasRepresentatives(CaseData caseData) {
        return ObjectUtils.isNotEmpty(caseData) && CollectionUtils.isNotEmpty(caseData.getRepCollection());
    }

    /**
     * Determines whether the given {@link RepresentedTypeR} is associated
     * with a valid organisation.
     *
     * <p>This method returns {@code true} only if:
     * <ul>
     *     <li>The {@code representative} is not {@code null} or empty,</li>
     *     <li>The associated respondent organisation is not {@code null} or empty, and</li>
     *     <li>The organisation has a non-blank organisation ID.</li>
     * </ul>
     *
     * @param representative the representative to evaluate; may be {@code null}
     * @return {@code true} if the representative has an associated organisation
     *         with a non-blank organisation ID, otherwise {@code false}
     */
    public static boolean hasOrganisation(RepresentedTypeR representative) {
        return ObjectUtils.isNotEmpty(representative)
                && ObjectUtils.isNotEmpty(representative.getRespondentOrganisation())
                && StringUtils.isNotBlank(representative.getRespondentOrganisation().getOrganisationID());
    }

    /**
     * Determines whether a representative is eligible for access revocation.
     *
     * <p>A representative is considered eligible if:
     * <ul>
     *     <li>The representative is allowed to modify access (via {@link
     *     RespondentRepresentativeUtils#canModifyAccess(RepresentedTypeRItem)}), and</li>
     *     <li>Either:
     *         <ul>
     *             <li>The provided respondent name is not blank and matches the representative's name, or</li>
     *             <li>The {@link CaseUserAssignment} is not null, its case role is not blank,
     *                 and it matches the representative's assigned role.</li>
     *         </ul>
     *     </li>
     * </ul>
     *
     * @param representative     the representative whose access eligibility is being evaluated
     * @param caseUserAssignment the case user assignment containing the case role (maybe null)
     * @param respondentName     the respondent name to match against the representative (maybe blank)
     * @return {@code true} if the representative meets the criteria for access revocation;
     *         {@code false} otherwise
     */
    public static boolean isEligibleForAccessRevocation(RepresentedTypeRItem representative,
                                                        CaseUserAssignment caseUserAssignment,
                                                        String respondentName) {
        return RespondentRepresentativeUtils.canModifyAccess(representative)
                && (StringUtils.isNotBlank(respondentName)
                && respondentName.equals(representative.getValue().getRespRepName())
                || ObjectUtils.isNotEmpty(caseUserAssignment)
                && StringUtils.isNotBlank(caseUserAssignment.getCaseRole())
                && caseUserAssignment.getCaseRole().equals(representative.getValue().getRole()));
    }

    /**
     * Determines whether the assignment context is valid.
     *
     * <p>The context is considered valid if:
     * <ul>
     *     <li>The list of representatives is not null and not empty,</li>
     *     <li>The {@link CaseDetails} object is not null or empty, and</li>
     *     <li>The case ID within the {@link CaseDetails} is not null, empty, or blank.</li>
     * </ul>
     *
     * <p>This method is intended to be used as a guard check before performing
     * representative assignment logic.</p>
     *
     * @param representatives the list of representatives eligible for assignment
     * @param caseDetails     the case details containing the case identifier
     * @return {@code true} if all required assignment context data is present and valid;
     *         {@code false} otherwise
     */
    public static boolean hasValidAssignmentContext(List<RepresentedTypeRItem> representatives,
                                                    CaseDetails caseDetails) {
        return CollectionUtils.isNotEmpty(representatives)
                && ObjectUtils.isNotEmpty(caseDetails)
                && StringUtils.isNotBlank(caseDetails.getCaseId());
    }
}
