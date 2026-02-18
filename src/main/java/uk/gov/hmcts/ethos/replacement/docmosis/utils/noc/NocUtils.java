package uk.gov.hmcts.ethos.replacement.docmosis.utils.noc;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CallbackRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationRequest;
import uk.gov.hmcts.et.common.model.ccd.types.NoticeOfChangeAnswers;
import uk.gov.hmcts.et.common.model.ccd.types.Organisation;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.exceptions.GenericServiceException;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CallbackObjectUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CallbacksCollectionUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.LoggingUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.RespondentUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.ccd.types.ChangeOrganisationApprovalStatus.APPROVED;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.ERROR_INVALID_CASE_DATA;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.GenericConstants.EXCEPTION_CALLBACK_REQUEST_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_INVALID_RESPONDENT_EXISTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.ERROR_SELECTED_RESPONDENT_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_NEW_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_NEW_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_NEW_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_NEW_RESPONDENT_COLLECTION_IS_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_OLD_AND_NEW_RESPONDENTS_ARE_DIFFERENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_OLD_AND_NEW_SUBMISSION_REFERENCES_NOT_EQUAL;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_OLD_CASE_DATA_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_OLD_CASE_DETAILS_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_OLD_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.EXCEPTION_OLD_RESPONDENT_COLLECTION_IS_EMPTY;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants.MAX_NOC_ANSWERS;

public final class NocUtils {

    private static final String CLASS_NAME = NocUtils.class.getSimpleName();
    private static final Map<String, BiConsumer<CaseData, OrganisationPolicy>> ROLE_TO_POLICY_SETTER = Map.of(
            SolicitorRole.SOLICITORA.getCaseRoleLabel().toLowerCase(Locale.UK),
            CaseData::setRespondentOrganisationPolicy0,
            SolicitorRole.SOLICITORB.getCaseRoleLabel().toLowerCase(Locale.UK),
            CaseData::setRespondentOrganisationPolicy1,
            SolicitorRole.SOLICITORC.getCaseRoleLabel().toLowerCase(Locale.UK),
            CaseData::setRespondentOrganisationPolicy2,
            SolicitorRole.SOLICITORD.getCaseRoleLabel().toLowerCase(Locale.UK),
            CaseData::setRespondentOrganisationPolicy3,
            SolicitorRole.SOLICITORE.getCaseRoleLabel().toLowerCase(Locale.UK),
            CaseData::setRespondentOrganisationPolicy4,
            SolicitorRole.SOLICITORF.getCaseRoleLabel().toLowerCase(Locale.UK),
            CaseData::setRespondentOrganisationPolicy5,
            SolicitorRole.SOLICITORG.getCaseRoleLabel().toLowerCase(Locale.UK),
            CaseData::setRespondentOrganisationPolicy6,
            SolicitorRole.SOLICITORH.getCaseRoleLabel().toLowerCase(Locale.UK),
            CaseData::setRespondentOrganisationPolicy7,
            SolicitorRole.SOLICITORI.getCaseRoleLabel().toLowerCase(Locale.UK),
            CaseData::setRespondentOrganisationPolicy8,
            SolicitorRole.SOLICITORJ.getCaseRoleLabel().toLowerCase(Locale.UK),
            CaseData::setRespondentOrganisationPolicy9
    );

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
     *         Uses {@link RespondentRepresentativeUtils#hasDuplicateRespondentNames(List)} to check that
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
                RespondentRepresentativeUtils.hasDuplicateRespondentNames(caseData.getRepCollection());
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
        if (!RespondentUtils.hasRespondents(caseData)
                || !RespondentRepresentativeUtils.hasRespondentRepresentative(caseData)) {
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
        RespondentRepresentativeUtils.validateRepresentation(respondent, representative, submissionReference);
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

    /**
     * Validates the integrity and consistency of a {@link CallbackRequest} used to identify
     * representation changes.
     *
     * <p>This method performs a series of defensive checks to ensure that the callback request
     * and its nested data are present, consistent, and suitable for further processing.
     * Validation includes:</p>
     * <ul>
     *   <li>Presence of the callback request</li>
     *   <li>Presence of both current and previous case details</li>
     *   <li>Presence and equality of case identifiers in current and previous case details</li>
     *   <li>Presence of current and previous {@link CaseData}</li>
     *   <li>Presence of respondent representative collections in both case states</li>
     *   <li>Consistency of respondent identities between old and new case data</li>
     * </ul>
     *
     * <p>If any validation step fails, a {@link GenericServiceException} is thrown with a
     * descriptive error message indicating the cause of the failure.</p>
     *
     * @param callbackRequest the callback request to validate
     * @throws GenericServiceException if the callback request is {@code null}, incomplete,
     *                                  inconsistent, or contains invalid case data
     */
    public static void validateCallbackRequest(CallbackRequest callbackRequest) throws GenericServiceException {
        String methodName = "validateCallbackRequest";
        if (ObjectUtils.isEmpty(callbackRequest)) {
            throw new GenericServiceException(EXCEPTION_CALLBACK_REQUEST_NOT_FOUND,
                    new Exception(EXCEPTION_CALLBACK_REQUEST_NOT_FOUND), EXCEPTION_CALLBACK_REQUEST_NOT_FOUND,
                    StringUtils.EMPTY, CLASS_NAME, methodName);
        }
        if (CallbackObjectUtils.isAnyEmpty(callbackRequest.getCaseDetails(), callbackRequest.getCaseDetailsBefore())) {
            String exceptionMessage = LoggingUtils.resolveMessageByPresence(callbackRequest.getCaseDetails(),
                    EXCEPTION_NEW_CASE_DETAILS_NOT_FOUND, EXCEPTION_OLD_CASE_DETAILS_NOT_FOUND);
            throw new GenericServiceException(exceptionMessage,
                    new Exception(exceptionMessage), exceptionMessage, StringUtils.EMPTY, CLASS_NAME, methodName);
        }
        if (CallbackObjectUtils.isAnyEmpty(callbackRequest.getCaseDetails().getCaseId(),
                callbackRequest.getCaseDetailsBefore().getCaseId())) {
            String exceptionMessage = LoggingUtils.resolveMessageByPresence(
                    callbackRequest.getCaseDetails().getCaseId(),
                    EXCEPTION_NEW_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND,
                    EXCEPTION_OLD_CASE_DETAILS_SUBMISSION_REFERENCE_NOT_FOUND);
            throw new GenericServiceException(exceptionMessage,
                    new Exception(exceptionMessage), exceptionMessage, StringUtils.EMPTY, CLASS_NAME, methodName);
        }
        if (!callbackRequest.getCaseDetails().getCaseId().equals(callbackRequest.getCaseDetailsBefore().getCaseId())) {
            String exceptionMessage = String.format(EXCEPTION_OLD_AND_NEW_SUBMISSION_REFERENCES_NOT_EQUAL,
                    callbackRequest.getCaseDetailsBefore().getCaseId(), callbackRequest.getCaseDetails().getCaseId());
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    StringUtils.EMPTY, CLASS_NAME, methodName);
        }
        CaseData newCaseData = callbackRequest.getCaseDetails().getCaseData();
        CaseData oldCaseData = callbackRequest.getCaseDetailsBefore().getCaseData();
        if (CallbackObjectUtils.isAnyEmpty(newCaseData, oldCaseData)) {
            String exceptionMessage = LoggingUtils.resolveMessageByPresence(newCaseData,
                    String.format(EXCEPTION_NEW_CASE_DATA_NOT_FOUND,
                            callbackRequest.getCaseDetailsBefore().getCaseId()),
                    String.format(EXCEPTION_OLD_CASE_DATA_NOT_FOUND,
                            callbackRequest.getCaseDetailsBefore().getCaseId()));
            throw new GenericServiceException(exceptionMessage,
                    new Exception(exceptionMessage), exceptionMessage, StringUtils.EMPTY, CLASS_NAME, methodName);
        }
        if (CallbackObjectUtils.isAnyEmpty(newCaseData.getRespondentCollection(),
                oldCaseData.getRespondentCollection())) {
            String exceptionMessage = LoggingUtils.resolveMessageByPresence(newCaseData.getRespondentCollection(),
                    String.format(EXCEPTION_NEW_RESPONDENT_COLLECTION_IS_EMPTY,
                    callbackRequest.getCaseDetailsBefore().getCaseId()),
                    String.format(EXCEPTION_OLD_RESPONDENT_COLLECTION_IS_EMPTY,
                    callbackRequest.getCaseDetailsBefore().getCaseId()));
            throw new GenericServiceException(exceptionMessage,
                    new Exception(exceptionMessage), exceptionMessage, StringUtils.EMPTY, CLASS_NAME, methodName);
        }
        if (!CallbacksCollectionUtils.sameByKey(oldCaseData.getRespondentCollection(),
                newCaseData.getRespondentCollection(), RespondentSumTypeItem::getId)) {
            String exceptionMessage = String.format(EXCEPTION_OLD_AND_NEW_RESPONDENTS_ARE_DIFFERENT,
                    callbackRequest.getCaseDetails().getCaseId());
            throw new GenericServiceException(exceptionMessage, new Exception(exceptionMessage), exceptionMessage,
                    StringUtils.EMPTY, CLASS_NAME, methodName);
        }
    }

    /**
     * Resets respondent organisation policies for all provided representatives.
     * <p>
     * For each representative in the list, this method resolves the associated solicitor
     * role within the given {@link CaseData} and resets the corresponding respondent
     * organisation policy by delegating to esetOrganisationPolicyByRepresentative(CaseData, RepresentedTypeRItem).
     * <p>
     * If {@code caseData} is {@code null} or empty, or if the list of representatives is
     * {@code null} or empty, the method performs no action.
     *
     * @param caseData        the case data containing respondent organisation policies to reset
     * @param representatives the list of representatives whose associated organisation
     *                        policies should be reset
     */
    public static void resetOrganisationPolicies(CaseData caseData,
                                                 List<RepresentedTypeRItem> representatives) {
        if (ObjectUtils.isEmpty(caseData) || CollectionUtils.isEmpty(representatives)) {
            return;
        }
        for (RepresentedTypeRItem representative : representatives) {
            RoleUtils.resetOrganisationPolicyByRepresentative(caseData, representative);

        }
    }

    /**
     * Builds an approved {@link ChangeOrganisationRequest} for changing an organisation
     * associated with a specific case role.
     * <p>
     * The request is populated with:
     * <ul>
     *     <li>{@code APPROVED} approval status</li>
     *     <li>The current timestamp</li>
     *     <li>The provided case role</li>
     *     <li>The organisation to be added and/or removed</li>
     * </ul>
     *
     * <p>
     * If both organisations are {@code null} or empty, the role is blank, or the role
     * is not valid, an empty {@link ChangeOrganisationRequest} is returned.
     *
     * @param newOrganisation the organisation to be added; may be {@code null}
     * @param oldOrganisation the organisation to be removed; may be {@code null}
     * @param role the case role identifier; must be non-blank and valid
     * @return a fully populated approved {@link ChangeOrganisationRequest}, or an empty
     *         request if the input parameters are invalid
     */
    public static ChangeOrganisationRequest buildApprovedChangeOrganisationRequest(Organisation newOrganisation,
                                                                                   Organisation oldOrganisation,
                                                                                   String role) {
        if (ObjectUtils.isEmpty(newOrganisation) && ObjectUtils.isEmpty(oldOrganisation) || StringUtils.isBlank(role)) {
            return ChangeOrganisationRequest.builder().build();
        }
        if (!RoleUtils.isValidRole(role)) {
            return ChangeOrganisationRequest.builder().build();
        }
        DynamicFixedListType roleItem = new DynamicFixedListType(role);

        return ChangeOrganisationRequest.builder()
                .approvalStatus(APPROVED)
                .requestTimestamp(LocalDateTime.now())
                .caseRoleId(roleItem)
                .organisationToRemove(oldOrganisation)
                .organisationToAdd(newOrganisation)
                .build();
    }

    /**
     * Populates {@link NoticeOfChangeAnswers} on the given {@link CaseData}
     * based on the respondents in the respondent collection.
     * <p>
     * For each respondent, this method checks whether a valid Notice of Change
     * answers entry already exists at the corresponding index. If no valid entry
     * is present and the respondent data is valid, a new
     * {@link NoticeOfChangeAnswers} instance is created using the respondent name
     * and set at that index.
     * </p>
     * <p>
     * Existing valid Notice of Change answers are not overwritten.
     * </p>
     * <p>
     * If the {@code caseData} is null or empty, or if the respondent collection
     * is empty, the method performs no action.
     * </p>
     *
     * @param caseData the case data to update with populated Notice of Change answers
     */
    public static void populateNoticeOfChangeAnswers(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData) || CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return;
        }
        for (int i = 0; i < caseData.getRespondentCollection().size(); i++) {
            NoticeOfChangeAnswers answers = RoleUtils.getNoticeOfChangeAnswersAtIndex(caseData, i);
            if (RoleUtils.isValidNoticeOfChangeAnswers(answers)) {
                continue;
            }
            RespondentSumTypeItem respondent = RespondentUtils.getRespondentAtIndex(caseData, i);
            if (!RespondentUtils.isValidRespondent(respondent)) {
                continue;
            }
            assert respondent != null;
            setNoticeOfChangeAnswerAtIndex(caseData, respondent.getValue().getRespondentName(), i);
        }
    }

    /**
     * Sets a {@link NoticeOfChangeAnswers} entry on the given {@link CaseData}
     * at the specified index, using the provided respondent name.
     * <p>
     * The method creates a new {@link NoticeOfChangeAnswers} instance and assigns
     * it to the corresponding Notice of Change field based on the index.
     * </p>
     * <p>
     * If the {@code caseData} is null or empty, the {@code respondentName} is blank,
     * or the {@code index} is out of bounds (less than 0 or greater than or equal to
     * {@link uk.gov.hmcts.ethos.replacement.docmosis.constants.NOCConstants#MAX_NOC_ANSWERS}),
     * the method performs no action.
     * </p>
     *
     * @param caseData        the case data to update
     * @param respondentName the name of the respondent to set on the Notice of Change answers
     * @param index           the zero-based index identifying which Notice of Change answers field to update
     */
    public static void setNoticeOfChangeAnswerAtIndex(CaseData caseData, String respondentName, int index) {
        if (ObjectUtils.isEmpty(caseData)
                || StringUtils.isBlank(respondentName)
                || index < 0
                || index >= MAX_NOC_ANSWERS) {
            return;
        }

        NoticeOfChangeAnswers answer = NoticeOfChangeAnswers.builder()
                .respondentName(respondentName)
                .build();

        switch (index) {
            case 0 -> caseData.setNoticeOfChangeAnswers0(answer);
            case 1 -> caseData.setNoticeOfChangeAnswers1(answer);
            case 2 -> caseData.setNoticeOfChangeAnswers2(answer);
            case 3 -> caseData.setNoticeOfChangeAnswers3(answer);
            case 4 -> caseData.setNoticeOfChangeAnswers4(answer);
            case 5 -> caseData.setNoticeOfChangeAnswers5(answer);
            case 6 -> caseData.setNoticeOfChangeAnswers6(answer);
            case 7 -> caseData.setNoticeOfChangeAnswers7(answer);
            case 8 -> caseData.setNoticeOfChangeAnswers8(answer);
            case 9 -> caseData.setNoticeOfChangeAnswers9(answer);
            default -> { /* no-op */ }
        }
    }

    /**
     * Applies a respondent {@link OrganisationPolicy} to the given {@link CaseData}
     * based on the representative’s case role.
     * <p>
     * If the representative has a valid respondent representative role, this method
     * creates an {@link OrganisationPolicy} using the representative’s organisation
     * and assigns it to the corresponding respondent organisation policy field on
     * the case data.
     * </p>
     * <p>
     * The method performs no action if the case data or representative is null or empty,
     * if the representative organisation is missing, if the role is blank or not a
     * respondent representative role, or if no organisation policy field is mapped
     * to the given role.
     * </p>
     *
     * @param caseData       the case data to update with the respondent organisation policy
     * @param representative the representative whose role and organisation are used
     *                       to determine and apply the organisation policy
     */
    public static void applyRespondentOrganisationPolicyForRole(CaseData caseData,
                                                                RepresentedTypeRItem representative) {
        if (ObjectUtils.isEmpty(caseData)
                || ObjectUtils.isEmpty(representative)
                || ObjectUtils.isEmpty(representative.getValue())
                || ObjectUtils.isEmpty(representative.getValue().getRespondentOrganisation())) {
            return;
        }
        String role = representative.getValue().getRole();
        if (StringUtils.isBlank(role) || !RoleUtils.isRespondentRepresentativeRole(role)) {
            return;
        }
        BiConsumer<CaseData, OrganisationPolicy> setter = ROLE_TO_POLICY_SETTER.get(role.toLowerCase(Locale.UK));
        if (setter == null) {
            return;
        }
        OrganisationPolicy policy = OrganisationPolicy.builder()
                .organisation(representative.getValue().getRespondentOrganisation())
                .orgPolicyCaseAssignedRole(role)
                .build();
        setter.accept(caseData, policy);
    }

    /**
     * Clears the Notice of Change (NoC) warning from the given {@link CaseData},
     * if one is present.
     *
     * <p>If the {@code caseData} is {@code null} or empty, or if no NoC warning
     * is currently set, this method performs no action.</p>
     *
     * @param caseData the case data from which the NoC warning should be cleared;
     *                 may be {@code null}
     */
    public static void clearNocWarningIfPresent(CaseData caseData) {
        if (ObjectUtils.isEmpty(caseData) || StringUtils.isBlank(caseData.getNocWarning())) {
            return;
        }
        caseData.setNocWarning(null);
    }

    /**
     * Determines whether sufficient data is available to proceed with
     * revoking representative access.
     *
     * <p>This method returns {@code true} only if:
     * <ul>
     *     <li>The {@code callbackRequest} is not {@code null} or empty,</li>
     *     <li>The {@code userToken} is not blank,</li>
     *     <li>The {@code caseDetailsBefore} section of the callback request is present,</li>
     *     <li>The case ID within {@code caseDetailsBefore} is not blank,</li>
     *     <li>The case data within {@code caseDetailsBefore} is present, and</li>
     *     <li>The list of representatives to remove is not {@code null} or empty.</li>
     * </ul>
     *
     * <p>No business validation is performed beyond checking for the presence
     * of required values.
     *
     * @param callbackRequest the callback request containing case details; may be {@code null}
     * @param userToken the authorisation token of the requesting user; may be {@code null} or blank
     * @param representativesToRemove the list of representatives whose access is to be revoked;
     *                                may be {@code null} or empty
     * @return {@code true} if all required parameters are present and non-empty,
     *         otherwise {@code false}
     */
    public static boolean canRevokeRepresentativeAccess(CallbackRequest callbackRequest,
                                                        String userToken,
                                                        List<RepresentedTypeRItem> representativesToRemove) {
        return ObjectUtils.isNotEmpty(callbackRequest)
                && ObjectUtils.isNotEmpty(callbackRequest.getCaseDetailsBefore())
                && StringUtils.isNotBlank(callbackRequest.getCaseDetailsBefore().getCaseId())
                && ObjectUtils.isNotEmpty(callbackRequest.getCaseDetailsBefore().getCaseData())
                && StringUtils.isNotBlank(userToken)
                && CollectionUtils.isNotEmpty(representativesToRemove);
    }
}
