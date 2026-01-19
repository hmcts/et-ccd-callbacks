package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationsResponse;
import uk.gov.hmcts.et.common.model.ccd.types.UpdateRespondentRepresentativeRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EXCEPTION_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EXCEPTION_CASE_REFERENCE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.Constants.EXCEPTION_UPDATE_RESPONDENT_REPRESENTATIVE_REQUEST_EMPTY;

public final class OrganisationUtils {

    private static final String CLASS_NAME_ORGANISATION_UTILS = "OrganisationUtils";
    private static final String EXCEPTION_RESPONDENT_NAME_EMPTY = "Respondent name is empty";

    private static final String METHOD_NAME_UPDATE_ORGANISATION_POLICIES = "updateOrganisationPolicies";

    private OrganisationUtils() {
        // Utility classes should not have a public or default constructor.
    }

    public static void removeRespondentOrganisationPolicyByRespondentName(
            CaseData caseData, UpdateRespondentRepresentativeRequest updateRespondentRepresentativeRequest)
            throws GenericServiceException {
        if (ObjectUtils.isEmpty(caseData)) {
            throw new GenericServiceException(EXCEPTION_CASE_DATA_NOT_FOUND,
                    new Exception(EXCEPTION_CASE_DATA_NOT_FOUND),
                    EXCEPTION_CASE_DATA_NOT_FOUND,
                    EXCEPTION_CASE_REFERENCE_NOT_FOUND,
                    CLASS_NAME_ORGANISATION_UTILS,
                    METHOD_NAME_UPDATE_ORGANISATION_POLICIES);
        }
        if (ObjectUtils.isEmpty(updateRespondentRepresentativeRequest)) {
            throw new GenericServiceException(EXCEPTION_UPDATE_RESPONDENT_REPRESENTATIVE_REQUEST_EMPTY,
                    new Exception(EXCEPTION_UPDATE_RESPONDENT_REPRESENTATIVE_REQUEST_EMPTY),
                    EXCEPTION_UPDATE_RESPONDENT_REPRESENTATIVE_REQUEST_EMPTY,
                    caseData.getCcdID(),
                    CLASS_NAME_ORGANISATION_UTILS,
                    METHOD_NAME_UPDATE_ORGANISATION_POLICIES);
        }
        if (ObjectUtils.isEmpty(updateRespondentRepresentativeRequest.getRespondentName())) {
            throw new GenericServiceException(EXCEPTION_RESPONDENT_NAME_EMPTY,
                    new Exception(EXCEPTION_RESPONDENT_NAME_EMPTY),
                    EXCEPTION_RESPONDENT_NAME_EMPTY,
                    caseData.getCcdID(),
                    CLASS_NAME_ORGANISATION_UTILS,
                    METHOD_NAME_UPDATE_ORGANISATION_POLICIES);
        }

        if (ObjectUtils.isEmpty(updateRespondentRepresentativeRequest.getChangeOrganisationRequest())
                || ObjectUtils.isEmpty(updateRespondentRepresentativeRequest.getChangeOrganisationRequest()
                .getOrganisationToRemove())
                || StringUtils.isBlank(updateRespondentRepresentativeRequest.getChangeOrganisationRequest()
                .getOrganisationToRemove().getOrganisationID())) {
            // No organisation to remove specified; do nothing
            return;
        }

        int noticeOfChangeAnswerIndex = findNoticeOfChangeAnswerIndexByRespondentName(
                caseData, updateRespondentRepresentativeRequest.getRespondentName());
        if (noticeOfChangeAnswerIndex == NumberUtils.INTEGER_MINUS_ONE) {
            // No matching respondent found; do nothing
            return;
        }
        List<Integer> respondentOrganisationPolicyIndex = findRespondentOrganisationPolicyIndicesByOrganisationId(
                caseData,
                updateRespondentRepresentativeRequest.getChangeOrganisationRequest().getOrganisationToRemove()
                        .getOrganisationID());
        // If respondent organisation policy index list is empty, no matching organisation to remove was found;
        // do nothing
        // If respondent organisation policy index list does not contain the notice of change index,
        // the organisation to remove is not associated with the respondent; do nothing
        if (CollectionUtils.isEmpty(respondentOrganisationPolicyIndex)
                || !respondentOrganisationPolicyIndex.contains(noticeOfChangeAnswerIndex)) {
            return;
        }
        removeOrganisationPolicyByIndex(caseData, noticeOfChangeAnswerIndex);
    }

    /**
     * Finds the indexes of {@link OrganisationPolicy} entries in the given {@link CaseData}
     * that are associated with the specified organisation ID.
     *
     * <p>The method performs the following steps:</p>
     * <ul>
     *   <li>If the provided {@code organisationId} is blank, an empty list is returned.</li>
     *   <li>Retrieves the list of respondent organisation policies from {@link CaseData}.</li>
     *   <li>Iterates over each policy, checking whether it is not null, has a non-empty
     *       {@link uk.gov.hmcts.et.common.model.ccd.types.Organisation}, and that organisation has a
     *       non-blank organisation ID matching the provided {@code organisationId}.</li>
     *   <li>Collects the zero-based indexes of all matching policies into a result list.</li>
     * </ul>
     *
     * @param caseData       the {@link CaseData} containing up to 10 respondent organisation policies
     * @param organisationId the organisation ID to search for; must not be blank
     * @return a list of zero-based indexes representing the positions of organisation policies
     *         matching the given {@code organisationId}, or an empty list if no matches are found
     */
    public static List<Integer> findRespondentOrganisationPolicyIndicesByOrganisationId(CaseData caseData,
                                                                                        String organisationId) {
        List<Integer> indexes = new ArrayList<>();
        if (ObjectUtils.isEmpty(caseData) || StringUtils.isBlank(organisationId)) {
            return indexes;
        }
        List<OrganisationPolicy> organisationPolicies = getRespondentOrganisationPolicies(caseData);
        if (CollectionUtils.isEmpty(organisationPolicies)) {
            return indexes;
        }
        for (int i = 0; i < organisationPolicies.size(); i++) {
            OrganisationPolicy organisationPolicy = organisationPolicies.get(i);
            if (ObjectUtils.isNotEmpty(organisationPolicy)
                    && ObjectUtils.isNotEmpty(organisationPolicy.getOrganisation())
                    && StringUtils.isNotBlank(organisationPolicy.getOrganisation().getOrganisationID())
                    && organisationId.equals(organisationPolicy.getOrganisation().getOrganisationID())) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    /**
     * Retrieves all non-null respondent {@link OrganisationPolicy} objects from the given {@link CaseData}.
     *
     * <p>This method inspects the respondent organisation policy fields
     * {@code respondentOrganisationPolicy0} through {@code respondentOrganisationPolicy9}
     * of the provided {@link CaseData} and collects the non-null entries into a {@link List}.
     * </p>
     *
     * <p>Key points:</p>
     * <ul>
     *   <li>If {@code caseData} is {@code null}, an empty list is returned.</li>
     *   <li>Only non-null {@link OrganisationPolicy} objects are included in the result.</li>
     *   <li>The order of policies in the list matches their order in {@link CaseData}
     *       (i.e. index 0 through 9).</li>
     *   <li>The returned list may be empty but is never {@code null}.</li>
     * </ul>
     *
     * @param caseData the {@link CaseData} containing up to 10 respondent organisation policies
     * @return a {@link List} of non-null respondent {@link OrganisationPolicy} objects in order,
     *         or an empty list if {@code caseData} is {@code null} or contains no policies
     */
    public static List<OrganisationPolicy> getRespondentOrganisationPolicies(CaseData caseData) {
        if (caseData == null) {
            return Collections.emptyList();
        }
        return Stream.of(
                caseData.getRespondentOrganisationPolicy0(),
                caseData.getRespondentOrganisationPolicy1(),
                caseData.getRespondentOrganisationPolicy2(),
                caseData.getRespondentOrganisationPolicy3(),
                caseData.getRespondentOrganisationPolicy4(),
                caseData.getRespondentOrganisationPolicy5(),
                caseData.getRespondentOrganisationPolicy6(),
                caseData.getRespondentOrganisationPolicy7(),
                caseData.getRespondentOrganisationPolicy8(),
                caseData.getRespondentOrganisationPolicy9()
        ).filter(Objects::nonNull).toList();
    }

    /**
     * Finds the index of a {@link NoticeOfChangeAnswers} entry within the list of Notice of Change answers
     * associated with the given {@link CaseData}, by matching the provided respondent name.
     *
     * <p>The method will:
     * <ul>
     *   <li>Return {@link NumberUtils#INTEGER_MINUS_ONE} if the {@code respondentName} is blank.</li>
     *   <li>Return {@link NumberUtils#INTEGER_MINUS_ONE} if the Notice of Change answers list is empty or null.</li>
     *   <li>Iterate through the list of Notice of Change answers, comparing the {@code respondentName} with
     *       each answer's respondent name.</li>
     *   <li>Return the zero-based index of the first matching {@link NoticeOfChangeAnswers} where the
     *       respondent name matches exactly.</li>
     *   <li>Return {@link NumberUtils#INTEGER_MINUS_ONE} if no match is found.</li>
     * </ul>
     *
     * @param caseData       the {@link CaseData} object containing Notice of Change answers
     * @param respondentName the name of the respondent to search for; must not be blank
     * @return the index of the matching {@link NoticeOfChangeAnswers} in the list, or
     *         {@link NumberUtils#INTEGER_MINUS_ONE} if no match is found, the input is blank,
     *         or the list of answers is empty
     */
    public static int findNoticeOfChangeAnswerIndexByRespondentName(CaseData caseData, String respondentName) {
        if (StringUtils.isBlank(respondentName)) {
            return NumberUtils.INTEGER_MINUS_ONE;
        }
        List<NoticeOfChangeAnswers> answers = getNoticeOfChangeAnswersList(caseData);
        if (CollectionUtils.isEmpty(answers)) {
            return NumberUtils.INTEGER_MINUS_ONE;
        }
        for (int i = 0; i < answers.size(); i++) {
            NoticeOfChangeAnswers answer = answers.get(i);
            if (ObjectUtils.isNotEmpty(answer) && StringUtils.isNotBlank(answer.getRespondentName())
                    && respondentName.equals(answer.getRespondentName())) {
                return i;
            }
        }
        return NumberUtils.INTEGER_MINUS_ONE;
    }

    /**
     * Retrieves all non-null {@link NoticeOfChangeAnswers} objects from the given {@link CaseData}.
     *
     * <p>This method inspects the Notice of Change answer fields
     * {@code noticeOfChangeAnswers0} through {@code noticeOfChangeAnswers9} in the provided
     * {@link CaseData} and collects only the non-null entries into a {@link List}.
     * </p>
     *
     * <p>Key points:</p>
     * <ul>
     *   <li>If {@code caseData} is {@code null}, an empty list is returned.</li>
     *   <li>Only non-null {@link NoticeOfChangeAnswers} are included in the result list.</li>
     *   <li>The order of answers in the list matches their order in {@link CaseData}
     *       (i.e., index 0 through 9).</li>
     *   <li>The returned list may be empty but is never {@code null}.</li>
     * </ul>
     *
     * @param caseData the {@link CaseData} containing up to 10 Notice of Change answers
     * @return a {@link List} of non-null {@link NoticeOfChangeAnswers} objects in order,
     *         or an empty list if {@code caseData} is {@code null} or contains no answers
     */
    public static List<NoticeOfChangeAnswers> getNoticeOfChangeAnswersList(CaseData caseData) {
        if (caseData == null) {
            return Collections.emptyList();
        }
        return Stream.of(
                    caseData.getNoticeOfChangeAnswers0(),
                    caseData.getNoticeOfChangeAnswers1(),
                    caseData.getNoticeOfChangeAnswers2(),
                    caseData.getNoticeOfChangeAnswers3(),
                    caseData.getNoticeOfChangeAnswers4(),
                    caseData.getNoticeOfChangeAnswers5(),
                    caseData.getNoticeOfChangeAnswers6(),
                    caseData.getNoticeOfChangeAnswers7(),
                    caseData.getNoticeOfChangeAnswers8(),
                    caseData.getNoticeOfChangeAnswers9()
        ).filter(Objects::nonNull).toList();
    }

    /**
     * Removes (sets to {@code null}) the respondent organisation policy at the specified index
     * in the given {@link CaseData}.
     *
     * <p>The method targets one of the indexed organisation policy fields
     * ({@code respondentOrganisationPolicy0} through {@code respondentOrganisationPolicy9})
     * based on the provided {@code index}.</p>
     *
     * <p>Key points:</p>
     * <ul>
     *   <li>If {@code caseData} is {@code null} or empty, the method does nothing.</li>
     *   <li>If the {@code index} is outside the valid range (0–9), the method does nothing.</li>
     *   <li>When a valid index is provided, the corresponding organisation policy field in
     *       {@link CaseData} is set to {@code null}.</li>
     * </ul>
     *
     * @param caseData the {@link CaseData} object containing respondent organisation policies
     * @param index    the zero-based index (0–9) of the organisation policy field to remove
     */
    public static void removeOrganisationPolicyByIndex(CaseData caseData, int index) {
        if (ObjectUtils.isEmpty(caseData) || index < 0 || index > 9) {
            return;
        }
        switch (index) {
            case 0 -> caseData.getRespondentOrganisationPolicy0().setOrganisation(null);
            case 1 -> caseData.getRespondentOrganisationPolicy1().setOrganisation(null);
            case 2 -> caseData.getRespondentOrganisationPolicy2().setOrganisation(null);
            case 3 -> caseData.getRespondentOrganisationPolicy3().setOrganisation(null);
            case 4 -> caseData.getRespondentOrganisationPolicy4().setOrganisation(null);
            case 5 -> caseData.getRespondentOrganisationPolicy5().setOrganisation(null);
            case 6 -> caseData.getRespondentOrganisationPolicy6().setOrganisation(null);
            case 7 -> caseData.getRespondentOrganisationPolicy7().setOrganisation(null);
            case 8 -> caseData.getRespondentOrganisationPolicy8().setOrganisation(null);
            case 9 -> caseData.getRespondentOrganisationPolicy9().setOrganisation(null);
            default -> {
                // This case should never be reached due to the earlier index check.
                // added to not have any PMD or Checkstyle warnings
            }
        }
    }

    @NotNull
    public static Address getOrganisationAddress(OrganisationsResponse organisationDetails) {
        Address organisationAddress = new Address();
        if (CollectionUtils.isEmpty(organisationDetails.getContactInformation())) {
            return organisationAddress;
        }
        organisationAddress.setAddressLine1(organisationDetails.getContactInformation().getFirst().getAddressLine1());
        organisationAddress.setAddressLine2(organisationDetails.getContactInformation().getFirst().getAddressLine2());
        organisationAddress.setAddressLine3(organisationDetails.getContactInformation().getFirst().getAddressLine3());
        organisationAddress.setPostCode(organisationDetails.getContactInformation().getFirst().getPostCode());
        organisationAddress.setPostTown(organisationDetails.getContactInformation().getFirst().getTownCity());
        organisationAddress.setCounty(organisationDetails.getContactInformation().getFirst().getCounty());
        organisationAddress.setCountry(organisationDetails.getContactInformation().getFirst().getCountry());
        return organisationAddress;
    }

}