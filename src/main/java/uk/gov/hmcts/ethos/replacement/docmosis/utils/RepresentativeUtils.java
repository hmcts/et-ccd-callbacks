package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

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
}
