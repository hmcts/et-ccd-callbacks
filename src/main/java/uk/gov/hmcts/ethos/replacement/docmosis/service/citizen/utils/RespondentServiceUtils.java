package uk.gov.hmcts.ethos.replacement.docmosis.service.citizen.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.enums.RespondentSolicitorType;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.CallbacksRuntimeException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.MapperUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.NoticeOfChangeUtils;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericServiceConstants.EXCEPTION_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericServiceConstants.EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.CLAIMANT_SOLICITOR;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.EXCEPTION_CASE_USER_ROLE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.RoleConstants.EXCEPTION_INVALID_CASE_USER_ROLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_EMPTY_RESPONDENT_COLLECTION;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_INVALID_RESPONDENT_INDEX;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_NO_RESPONDENT_DEFINED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_RESPONDENT_NOT_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_RESPONDENT_NOT_FOUND_WITH_INDEX;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.citizen.RespondentServiceConstants.EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND;

public final class RespondentServiceUtils {

    private RespondentServiceUtils() {
        // Utility classes should not have a public or default constructor.
    }

    /**
     * Retrieves the {@link RespondentSolicitorType} for a specified respondent within a given case.
     *
     * <p>This method validates the provided {@link CaseDetails} and respondent index before attempting
     * to extract the respondent’s solicitor information. It performs several checks to ensure data
     * integrity, such as verifying the existence of case data, respondent collection, and the validity
     * of the respondent index. If any validation fails, a {@link CallbacksRuntimeException} is thrown
     * with an appropriate message.</p>
     *
     * <p><b>Validation steps include:</b></p>
     * <ul>
     *   <li>Ensures {@code caseDetails} is not null or empty.</li>
     *   <li>Checks that {@code caseDetails.getData()} contains valid case data.</li>
     *   <li>Validates that {@code respondentIndex} is not blank, numeric, and non-negative.</li>
     *   <li>Ensures the respondent collection in {@code caseData} is not empty.</li>
     *   <li>Checks that {@code respondentIndex} is within the bounds of the respondent collection.</li>
     *   <li>Validates that the respondent exists and has a non-empty name.</li>
     *   <li>Finds the corresponding Notice of Change (NoC) answer index for the respondent.</li>
     *   <li>Retrieves and validates the {@link RespondentSolicitorType} using the NoC answer index.</li>
     * </ul>
     *
     * <p>If all validations pass, the method returns the {@link RespondentSolicitorType} associated
     * with the respondent at the specified index.</p>
     *
     * @param caseDetails     the {@link CaseDetails} object containing the case data and respondent information
     * @param respondentIndex the index of the respondent whose solicitor type is to be retrieved;
     *                        must be a non-negative integer within the bounds of the respondent collection
     * @return the {@link RespondentSolicitorType} for the respondent at the specified index
     *
     * @throws CallbacksRuntimeException if:
     *      <ul>
         *      <li>{@code caseDetails} or its data is null or empty;</li>
         *      <li>{@code respondentIndex} is blank, non-numeric, or out of range;</li>
         *      <li>the respondent collection is empty or the respondent at the given index does not exist;</li>
         *      <li>the respondent has no associated Notice of Change answer;</li>
         *      <li>the solicitor type could not be found for the given respondent.</li>
     *      </ul>
     *
     * @see MapperUtils#convertCaseDataMapToCaseDataObject(Map)
     * @see NoticeOfChangeUtils#findNoticeOfChangeAnswerIndex(CaseData, String)
     * @see NoticeOfChangeUtils#findRespondentSolicitorTypeByIndex(int)
     */
    public static RespondentSolicitorType getRespondentSolicitorType(CaseDetails caseDetails, String respondentIndex) {
        // Check if caseDetails is null or empty
        if (ObjectUtils.isEmpty(caseDetails)) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_CASE_DETAILS_NOT_FOUND, StringUtils.EMPTY)));
        }
        String caseId = ObjectUtils.isNotEmpty(
                caseDetails.getId()) ? caseDetails.getId().toString() : StringUtils.EMPTY;

        // Check if caseDetails has no case data
        if (MapUtils.isEmpty(caseDetails.getData())) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA, caseId)));
        }

        // Check if respondentIndex is blank or not a valid number
        if (StringUtils.isBlank(respondentIndex)
                || !NumberUtils.isCreatable(respondentIndex)
                || NumberUtils.createInteger(respondentIndex) < 0) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX, respondentIndex, caseId)));
        }
        CaseData caseData = MapperUtils.convertCaseDataMapToCaseDataObject(caseDetails.getData());
        // Check if caseData is null or empty
        if (ObjectUtils.isEmpty(caseData)) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA, caseId)));
        }

        // Check if respondentCollection is null or empty
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_EMPTY_RESPONDENT_COLLECTION, caseId)));
        }

        // Check if respondentIndex is within bounds of respondentCollection
        if (NumberUtils.createInteger(respondentIndex) >= caseData.getRespondentCollection().size()) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX, respondentIndex, caseId)));
        }

        // Check if caseData has respondent at the given index
        RespondentSumTypeItem respondentSumTypeItem = caseData.getRespondentCollection()
                .get(NumberUtils.createInteger(respondentIndex));
        if (ObjectUtils.isEmpty(respondentSumTypeItem)
                || ObjectUtils.isEmpty(respondentSumTypeItem.getValue())
                || StringUtils.isBlank(respondentSumTypeItem.getValue().getRespondentName())) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_RESPONDENT_NOT_EXISTS, caseId)));
        }

        int noticeOfChangeAnswerIndex = NoticeOfChangeUtils
                .findNoticeOfChangeAnswerIndex(caseData, respondentSumTypeItem.getValue().getRespondentName());
        if (noticeOfChangeAnswerIndex == -1) {
            throw new CallbacksRuntimeException(new Exception(
                    String.format(EXCEPTION_NOTICE_OF_CHANGE_ANSWER_NOT_FOUND,
                            respondentSumTypeItem.getValue().getRespondentName(),
                            caseId)));
        }
        RespondentSolicitorType respondentSolicitorType = NoticeOfChangeUtils
                .findRespondentSolicitorTypeByIndex(noticeOfChangeAnswerIndex);
        if (ObjectUtils.isEmpty(respondentSolicitorType)) {
            throw new CallbacksRuntimeException(new Exception(String.format(
                    EXCEPTION_RESPONDENT_SOLICITOR_TYPE_NOT_FOUND, caseId, noticeOfChangeAnswerIndex)));
        }
        return respondentSolicitorType;
    }

    /**
     * Retrieves a {@link RespondentSumTypeItem} from the provided list based on the given respondent index.
     * <p>
     * This method attempts to parse the provided {@code respondentIndex} string into an integer and uses it
     * to fetch the corresponding {@link RespondentSumTypeItem} from the {@code respondentSumTypeItems} list.
     * It performs validation to ensure that:
     * <ul>
     *   <li>The list of respondents is not empty or null.</li>
     *   <li>The index is within the valid bounds of the list.</li>
     *   <li>The respondent item and its value are not null or empty.</li>
     * </ul>
     * <p>
     * If any of these validations fail, a {@link CallbacksRuntimeException} is thrown with an appropriate
     * error message, including the {@code caseId} for traceability.
     *
     * @param respondentSumTypeItems the list of {@link RespondentSumTypeItem} objects to search within; must not be
     *                               null or empty
     * @param respondentIndex the index of the respondent as a string; must represent a valid integer within the
     *                        list's bounds
     * @param caseId the unique identifier of the case; used for error message context
     * @return the {@link RespondentSumTypeItem} located at the specified index in the list
     * @throws CallbacksRuntimeException if the respondent list is empty, the index is invalid, or the item/value at
     *                                   the index is null
     * @throws NumberFormatException if {@code respondentIndex} cannot be parsed into an integer
     * @throws IndexOutOfBoundsException if the parsed index is outside the valid range of the list
     */
    public static RespondentSumTypeItem findRespondentSumTypeItemByIndex(
            List<RespondentSumTypeItem> respondentSumTypeItems, String respondentIndex, String caseId) {
        try {
            if (org.apache.commons.collections.CollectionUtils.isEmpty(respondentSumTypeItems)) {
                throw new CallbacksRuntimeException(new Exception(
                        String.format(EXCEPTION_NO_RESPONDENT_DEFINED, caseId)));
            }
            int index = Integer.parseInt(respondentIndex);
            RespondentSumTypeItem respondentSumTypeItem = respondentSumTypeItems.get(index);
            if (ObjectUtils.isEmpty(respondentSumTypeItem)
                    || ObjectUtils.isEmpty(respondentSumTypeItem.getValue())) {
                throw new CallbacksRuntimeException(new Exception(
                        String.format(EXCEPTION_RESPONDENT_NOT_FOUND_WITH_INDEX, respondentIndex)));
            }
            return respondentSumTypeItem;
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            throw new CallbacksRuntimeException(
                    new Exception(String.format(EXCEPTION_INVALID_RESPONDENT_INDEX, respondentIndex, caseId), e));
        }
    }

    /**
     * Resets the {@link OrganisationPolicy} field within the provided {@link CaseData} based on the given case user
     * role.
     * <p>
     * This method is responsible for reinitializing (or clearing) the relevant organisation policy depending on
     * whether the user role corresponds to the claimant solicitor or one of the respondent solicitors (A–J).
     * <p>
     * Behaviour:
     * <ul>
     *   <li>If {@code caseData} is {@code null} or empty, a {@link CallbacksRuntimeException} is thrown.</li>
     *   <li>If {@code caseUserRole} is blank or {@code null}, a {@link CallbacksRuntimeException} is thrown.</li>
     *   <li>If the role matches {@code CLAIMANT_SOLICITOR}, the claimant’s organisation policy is reset.</li>
     *   <li>If the role matches a valid {@link RespondentSolicitorType}, the corresponding respondent’s organisation
     *   policy is reset.</li>
     *   <li>If the role is unrecognised, a {@link CallbacksRuntimeException} is thrown.</li>
     * </ul>
     * <p>
     * This ensures that the case’s organisation policy structure remains valid and consistent across all solicitor
     * roles.
     *
     * @param caseData the {@link CaseData} object representing the case; must not be null
     * @param caseUserRole the case user role, typically one of {@code CLAIMANT_SOLICITOR} or respondent solicitor
     *                     labels (A–J)
     * @param caseId the unique case identifier, used in exception messages for traceability
     *
     * @throws CallbacksRuntimeException if {@code caseData} is null, {@code caseUserRole} is blank,
     *                                   or {@code caseUserRole} does not correspond to a valid solicitor role
     */
    public static void resetOrganizationPolicy(CaseData caseData, String caseUserRole, String caseId) {
        if (ObjectUtils.isEmpty(caseData)) {
            throw new CallbacksRuntimeException(new Exception(String.format(EXCEPTION_CASE_DETAILS_NOT_HAVE_CASE_DATA,
                    caseId)));
        }

        if (StringUtils.isBlank(caseUserRole)) {
            throw new CallbacksRuntimeException(new Exception(
                    String.format(EXCEPTION_CASE_USER_ROLE_NOT_FOUND, caseId)));
        }

        if (CLAIMANT_SOLICITOR.equals(caseUserRole)) {
            OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
                    .orgPolicyCaseAssignedRole(CLAIMANT_SOLICITOR).build();
            caseData.setClaimantRepresentativeOrganisationPolicy(organisationPolicy);
            return;
        }
        try {
            RespondentSolicitorType caseUserRoleEnum = RespondentSolicitorType.fromLabel(caseUserRole);
            switch (caseUserRoleEnum) {
                case SOLICITORA ->
                        caseData.setRespondentOrganisationPolicy0(createEmptyOrganisationPolicyByRole(caseUserRole));
                case SOLICITORB ->
                        caseData.setRespondentOrganisationPolicy1(createEmptyOrganisationPolicyByRole(caseUserRole));
                case SOLICITORC ->
                        caseData.setRespondentOrganisationPolicy2(createEmptyOrganisationPolicyByRole(caseUserRole));
                case SOLICITORD ->
                        caseData.setRespondentOrganisationPolicy3(createEmptyOrganisationPolicyByRole(caseUserRole));
                case SOLICITORE ->
                        caseData.setRespondentOrganisationPolicy4(createEmptyOrganisationPolicyByRole(caseUserRole));
                case SOLICITORF ->
                        caseData.setRespondentOrganisationPolicy5(createEmptyOrganisationPolicyByRole(caseUserRole));
                case SOLICITORG ->
                        caseData.setRespondentOrganisationPolicy6(createEmptyOrganisationPolicyByRole(caseUserRole));
                case SOLICITORH ->
                        caseData.setRespondentOrganisationPolicy7(createEmptyOrganisationPolicyByRole(caseUserRole));
                case SOLICITORI ->
                        caseData.setRespondentOrganisationPolicy8(createEmptyOrganisationPolicyByRole(caseUserRole));
                case SOLICITORJ ->
                        caseData.setRespondentOrganisationPolicy9(createEmptyOrganisationPolicyByRole(caseUserRole));
                default -> throw new CallbacksRuntimeException(new Exception(String.format(
                        EXCEPTION_INVALID_CASE_USER_ROLE,
                        caseUserRole
                )));
            }
        } catch (IllegalArgumentException e) {
            throw new CallbacksRuntimeException(new Exception(
                    String.format(EXCEPTION_INVALID_CASE_USER_ROLE, caseUserRole), e));
        }
    }

    private static OrganisationPolicy createEmptyOrganisationPolicyByRole(String caseUserRole) {
        return OrganisationPolicy.builder().orgPolicyCaseAssignedRole(caseUserRole).build();
    }

    /**
     * Finds the {@link RepresentedTypeRItem} (respondent representative) associated with the given
     * {@link RespondentSumTypeItem} within the provided representative collection.
     * <p>
     * The method iterates through the list of respondent representatives and identifies the one
     * whose {@code respondentId} matches the {@code id} of the supplied respondent. It validates
     * input parameters and ensures both the respondent and representative collection are present
     * and properly populated before performing the lookup.
     * <p>
     * Behaviour:
     * <ul>
     *   <li>If {@code respondentSumTypeItem} is {@code null} or empty, a {@link CallbacksRuntimeException} is
     *   thrown.</li>
     *   <li>If {@code representativeCollection} is {@code null} or empty, a {@link CallbacksRuntimeException} is
     *   thrown.</li>
     *   <li>If no representative is found for the respondent, a {@link CallbacksRuntimeException} is thrown.</li>
     * </ul>
     * <p>
     * This method is used to ensure that each respondent in a case has a corresponding representative
     * entry, enabling correct mapping between respondents and their legal representatives.
     *
     * @param respondentSumTypeItem the {@link RespondentSumTypeItem} representing the respondent whose representative
     *                              is to be found
     * @param representativeCollection the list of {@link RepresentedTypeRItem} objects to search through; must not be
     *                                 null or empty
     * @param caseId the unique identifier of the case, used in exception messages for traceability
     * @return the {@link RepresentedTypeRItem} corresponding to the given respondent
     *
     * @throws CallbacksRuntimeException if the respondent or representative collection is missing,
     *                                   or if no representative matches the given respondent
     */
    public static RepresentedTypeRItem findRespondentRepresentative(RespondentSumTypeItem respondentSumTypeItem,
                                                                    List<RepresentedTypeRItem> representativeCollection,
                                                                    String caseId) {
        if (ObjectUtils.isEmpty(respondentSumTypeItem)) {
            throw new CallbacksRuntimeException(new Exception(
                    String.format(EXCEPTION_RESPONDENT_NOT_EXISTS, caseId)));
        }
        if (CollectionUtils.isEmpty(representativeCollection)) {
            throw new CallbacksRuntimeException(new Exception(
                    String.format(EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND, caseId)));
        }
        for (RepresentedTypeRItem representativeType : representativeCollection) {
            if (ObjectUtils.isNotEmpty(representativeType)
                    && ObjectUtils.isNotEmpty(representativeType.getValue())
                    && StringUtils.isNotBlank(representativeType.getValue().getRespondentId())
                    && representativeType.getValue().getRespondentId()
                    .equals(respondentSumTypeItem.getId())) {
                return representativeType;
            }
        }
        throw new CallbacksRuntimeException(new Exception(
                String.format(EXCEPTION_RESPONDENT_REPRESENTATIVE_NOT_FOUND, caseId)));
    }
}
