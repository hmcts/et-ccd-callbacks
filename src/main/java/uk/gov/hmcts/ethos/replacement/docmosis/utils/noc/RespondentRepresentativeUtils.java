package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
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
import java.util.stream.Collectors;

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
     *   <li>The representative is marked as a MyHMCTS user</li>
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
                .collect(Collectors.toList());
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
        List<RepresentedTypeRItem> representativesToRemove = new ArrayList<>();
        for (RepresentedTypeRItem oldRepresentative : oldRepresentatives) {
            if (!RespondentRepresentativeUtils.isValidRepresentative(oldRepresentative)) {
                continue;
            }
            // to check if representative exists but its organisation or email is changed or not
            boolean representativeChanged = false;
            // to check if representative exists or not
            boolean representativeFound = false;
            for (RepresentedTypeRItem newRepresentative : newRepresentatives) {
                if (!isValidRepresentative(newRepresentative)
                        || !representsSameRespondent(oldRepresentative, newRepresentative)) {
                    continue;
                }
                representativeFound = true;
                if (isRepresentativeOrganisationChanged(oldRepresentative.getValue(), newRepresentative.getValue())
                        || isRepresentativeEmailChanged(oldRepresentative.getValue(), newRepresentative.getValue())) {
                    // representative already exists but its organisation or email is changed
                    representativeChanged = true;
                }
            }
            if (!representativeFound || representativeChanged) {
                representativesToRemove.add(oldRepresentative);
            }
        }
        return representativesToRemove;
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
}
