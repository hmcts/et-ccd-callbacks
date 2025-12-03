package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_INVALID_REPRESENTATIVE_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXIST;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_REPRESENTATIVE_NOT_FOUND;

public final class RepresentativeUtils {

    private static final String VALIDATE_REPRESENTATIVE_METHOD_NAME = "validateRepresentative";

    private RepresentativeUtils() {
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
    public static void validateRespondentRepresentative(RepresentedTypeRItem representative,
                                                        String caseReferenceNumber)
            throws GenericServiceException {
        if (ObjectUtils.isEmpty(representative)) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_NOT_FOUND, caseReferenceNumber);
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseReferenceNumber, RepresentativeUtils.class.getSimpleName(),
                    VALIDATE_REPRESENTATIVE_METHOD_NAME);
        }
        if (StringUtils.isBlank(representative.getId())) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_ID_NOT_FOUND, caseReferenceNumber);
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseReferenceNumber, RepresentativeUtils.class.getSimpleName(),
                    VALIDATE_REPRESENTATIVE_METHOD_NAME);
        }
        if (ObjectUtils.isEmpty(representative.getValue())) {
            String exceptionMessage = String.format(EXCEPTION_REPRESENTATIVE_DETAILS_NOT_EXIST,
                    representative.getId(), caseReferenceNumber);
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    caseReferenceNumber, RepresentativeUtils.class.getSimpleName(),
                    VALIDATE_REPRESENTATIVE_METHOD_NAME);
        }
    }

    /**
     * Determines whether the given {@link RepresentedTypeRItem} contains valid and usable
     * representative data.
     * <p>
     * A representative is considered <em>valid</em> if all the following conditions are met:
     * <ul>
     *     <li>The {@code representative} object itself is not {@code null}.</li>
     *     <li>The representative has a non-empty identifier ({@code representative.getId()}).</li>
     *     <li>The representative contains a non-null {@code value} object.</li>
     * </ul>
     * <p>
     * This method performs a lightweight structural validation only and does not verify the
     * correctness or completeness of individual fields inside the value object. It is intended
     * for use in pre-validation flows to determine whether a representative record is suitable
     * for further processing.
     *
     * @param representative the representative item to validate
     * @return {@code true} if the representative has all required fields populated;
     *         {@code false} otherwise
     */
    public static boolean isValidRespondentRepresentative(RepresentedTypeRItem representative) {
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
     *         {@link RepresentativeUtils#validateRespondentRepresentative(RepresentedTypeRItem, String)},
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
        RepresentativeUtils.validateRespondentRepresentative(representative, caseReferenceNumber);
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
            if (ObjectUtils.isEmpty(representative)
                    || ObjectUtils.isEmpty(representative.getValue())
                    || ObjectUtils.isEmpty(representative.getValue().getDynamicRespRepName())
                    || ObjectUtils.isEmpty(representative.getValue().getDynamicRespRepName().getValue())
                    || StringUtils.isBlank(representative.getValue().getDynamicRespRepName().getValue().getLabel())) {
                return List.of(ERROR_INVALID_REPRESENTATIVE_EXISTS);
            }
            String respondentName = representative.getValue().getDynamicRespRepName().getValue().getLabel();
            if (!existingRespondentNames.add(respondentName)) {
                return List.of(String.format(ERROR_RESPONDENT_HAS_MULTIPLE_REPRESENTATIVES, respondentName));
            }
        }
        return Collections.emptyList();
    }

}
