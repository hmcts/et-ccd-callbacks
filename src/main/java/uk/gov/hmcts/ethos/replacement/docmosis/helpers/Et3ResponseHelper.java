package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DynamicListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DynamicListType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.ET3DocumentHelper.isET3NotificationDocumentTypeResponseAccepted;

/**
 * ET3 Response Helper provides methods to assist with the ET3 Response Form event.
 */
@Slf4j
public final class Et3ResponseHelper {

    private static final String CLAIMANT_NAME_TABLE = "<pre> ET1 claimant name&#09&#09&#09&#09 %s</pre><hr>";
    private static final String START_DATE_MUST_BE_IN_THE_PAST = "Start date must be in the past";
    private static final String END_DATE_MUST_BE_AFTER_THE_START_DATE = "End date must be after the start date";
    public static final String ET3_RESPONSE = "et3Response";
    public static final String ET3_RESPONSE_EMPLOYMENT_DETAILS = "et3ResponseEmploymentDetails";
    public static final String ET3_RESPONSE_DETAILS = "et3ResponseDetails";
    public static final String ALL_RESPONDENTS_INCOMPLETE_SECTIONS = "There are no respondents that can currently "
            + "submit an ET3 Form. Please make sure all 3 sections have been completed for a respondent";
    public static final String NO_RESPONDENTS_FOUND = "No respondents found";
    private static final String INVALID_EVENT_ID = "Invalid eventId: ";

    private Et3ResponseHelper() {
        // Access through static methods
    }

    /**
     * Formats the name of the claimant for display on the Claimant name correct page.
     *
     * @param caseData data for the current case
     * @return Name ready for presentation on web
     */
    public static String formatClaimantNameForHtml(CaseData caseData) {
        return String.format(CLAIMANT_NAME_TABLE, caseData.getClaimant());
    }

    /**
     * Validates that the employment start date is in the past and not after
     * the employment end date if both dates are provided.
     *
     * @param caseData data for the current case
     * @return List of validation errors encountered
     */
    public static List<String> validateEmploymentDates(CaseData caseData) {
        ArrayList<String> errors = new ArrayList<>();
        String startDateStr = caseData.getEt3ResponseEmploymentStartDate();

        if (isNullOrEmpty(startDateStr)) {
            return errors;
        }

        LocalDate startDate = LocalDate.parse(startDateStr);

        if (startDate.isAfter(LocalDate.now())) {
            errors.add(START_DATE_MUST_BE_IN_THE_PAST);
        }

        String endDateStr = caseData.getEt3ResponseEmploymentEndDate();

        if (isNullOrEmpty(endDateStr)) {
            return errors;
        }

        LocalDate endDate = LocalDate.parse(endDateStr);

        if (startDate.isAfter(endDate)) {
            errors.add(END_DATE_MUST_BE_AFTER_THE_START_DATE);
        }

        return errors;
    }

    /**
     * Create a collection of DynamicLists of respondent names.
     *
     * @param caseData data for the case
     */
    public static List<String> createDynamicListSelection(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return List.of(NO_RESPONDENTS_FOUND);
        }

        List<RespondentSumTypeItem> respondents = caseData.getRespondentCollection().stream()
                .filter(r -> isAllowSubmit(r.getValue()))
                .toList();

        if (CollectionUtils.isEmpty(respondents)) {
            return List.of("There are no respondents that require an ET3");
        }

        DynamicFixedListType dynamicList = DynamicFixedListType.from(DynamicListHelper.createDynamicRespondentName(
                caseData.getRespondentCollection().stream()
                        .filter(r -> isAllowSubmit(r.getValue()))
                        .toList()));
        DynamicListType dynamicListType = new DynamicListType();
        dynamicListType.setDynamicList(dynamicList);
        DynamicListTypeItem dynamicListTypeItem = new DynamicListTypeItem();
        dynamicListTypeItem.setValue(dynamicListType);
        caseData.setEt3RepresentingRespondent(List.of(dynamicListTypeItem));
        caseData.setSubmitEt3Respondent(dynamicList);
        return new ArrayList<>();
    }

    private static boolean isAllowSubmit(RespondentSumType respondent) {
        if (NO.equals(respondent.getResponseReceived())) {
            return true;
        }
        if (respondent.getExtensionDate() != null) {
            LocalDate extensionDate = LocalDate.parse(respondent.getExtensionDate());
            return YES.equals(respondent.getExtensionRequested())
                && YES.equals(respondent.getExtensionGranted())
                && extensionDate.isAfter(LocalDate.now())
                && !YES.equals(respondent.getExtensionResubmitted());
        }
        return false;
    }

    /**
     * Validate the selection of respondents.
     *
     * @param caseData data for the case
     * @return an error is the user has selected the same respondent multiple times
     */
    public static List<String> validateRespondents(CaseData caseData, String eventId) {
        switch (eventId) {
            case ET3_RESPONSE -> {
                if (caseData.getSubmitEt3Respondent() == null) {
                    return List.of(NO_RESPONDENTS_FOUND);
                }
            }
            case ET3_RESPONSE_DETAILS, ET3_RESPONSE_EMPLOYMENT_DETAILS -> {
                if (CollectionUtils.isEmpty(caseData.getEt3RepresentingRespondent())) {
                    return List.of(NO_RESPONDENTS_FOUND);
                }

                Set<String> respondentSet = new HashSet<>();
                for (DynamicListTypeItem dynamicListTypeItem : caseData.getEt3RepresentingRespondent()) {
                    respondentSet.add(String.valueOf(DynamicFixedListType.getSelectedLabel(
                            dynamicListTypeItem.getValue().getDynamicList())));
                }

                if (respondentSet.size() != caseData.getEt3RepresentingRespondent().size()) {
                    return List.of("Please do not choose the same respondent multiple times");
                }
            }
            default -> throw new IllegalArgumentException(INVALID_EVENT_ID + eventId);

        }

        return new ArrayList<>();
    }

    /**
     * Saves the data from the ET3 Form event to the respondent.
     *
     * @param caseData data for the case
     */
    public static void addEt3DataToRespondent(CaseData caseData, String eventId) {
        Set<String> respondentSet = new HashSet<>();
        switch (eventId) {
            case ET3_RESPONSE -> respondentSet.add(caseData.getSubmitEt3Respondent().getSelectedLabel());
            case ET3_RESPONSE_DETAILS, ET3_RESPONSE_EMPLOYMENT_DETAILS -> {
                for (DynamicListTypeItem dynamicListTypeItem : caseData.getEt3RepresentingRespondent()) {
                    respondentSet.add(dynamicListTypeItem.getValue().getDynamicList().getSelectedLabel());
                }
            }
            default -> throw new IllegalArgumentException(INVALID_EVENT_ID + eventId);
        }

        for (String respondentSelected : respondentSet) {
            Optional<RespondentSumTypeItem> respondent = caseData.getRespondentCollection().stream()
                    .filter(r -> respondentSelected.equals(r.getValue().getRespondentName()))
                    .findFirst();
            respondent.ifPresent(respondentSumTypeItem -> addEt3Data(caseData, eventId, respondentSelected,
                    respondentSumTypeItem));
        }
    }

    private static void addEt3Data(CaseData caseData, String eventId, String respondentSelected,
                                   RespondentSumTypeItem respondent) {
        RespondentSumType respondentSumType = switch (eventId) {
            case ET3_RESPONSE -> addPersonalDetailsToRespondentOrRepresentative(caseData, respondent.getValue());
            case ET3_RESPONSE_EMPLOYMENT_DETAILS -> addEmploymentDetailsToRespondent(caseData, respondent.getValue());
            case ET3_RESPONSE_DETAILS -> addClaimDetailsToRespondent(caseData, respondent.getValue());
            default -> throw new IllegalArgumentException(INVALID_EVENT_ID + eventId);
        };
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            if (respondentSelected.equals(respondentSumTypeItem.getValue().getRespondentName())) {
                respondentSumTypeItem.setValue(respondentSumType);
            }
        }
    }

    private static RespondentSumType addClaimDetailsToRespondent(CaseData caseData, RespondentSumType respondent) {
        respondent.setEt3ResponseAcasAgree(caseData.getEt3ResponseAcasAgree());
        respondent.setEt3ResponseAcasAgreeReason(caseData.getEt3ResponseAcasAgreeReason());
        respondent.setEt3ResponseRespondentContestClaim(caseData.getEt3ResponseRespondentContestClaim());
        respondent.setEt3ResponseContestClaimDocument(caseData.getEt3ResponseContestClaimDocument());
        respondent.setEt3ResponseContestClaimDetails(caseData.getEt3ResponseContestClaimDetails());
        respondent.setEt3ResponseEmployerClaim(caseData.getEt3ResponseEmployerClaim());
        respondent.setEt3ResponseEmployerClaimDetails(caseData.getEt3ResponseEmployerClaimDetails());
        respondent.setEt3ResponseEmployerClaimDocument(caseData.getEt3ResponseEmployerClaimDocument());
        respondent.setClaimDetailsSection(YES);
        return respondent;
    }

    private static RespondentSumType addPersonalDetailsToRespondentOrRepresentative(CaseData caseData,
                                                                                    RespondentSumType respondent) {
        respondent.setEt3ResponseIsClaimantNameCorrect(caseData.getEt3ResponseIsClaimantNameCorrect());
        respondent.setEt3ResponseClaimantNameCorrection(caseData.getEt3ResponseClaimantNameCorrection());
        respondent.setResponseRespondentName(caseData.getEt3ResponseRespondentLegalName());
        respondent.setResponseRespondentAddress(caseData.getEt3RespondentAddress());
        RepresentedTypeR representative = findRepresentativeFromCaseData(caseData);
        if (representative != null) {
            // This should be mapped to Representative
            // mentioned in the ticket https://tools.hmcts.net/jira/browse/RET-5054
            // existing statement respondent.setResponseRespondentPhone1(caseData.getEt3ResponsePhone())
            // replaced with representative.setRepresentativePhoneNumber(caseData.getEt3ResponsePhone())
            representative.setRepresentativePhoneNumber(caseData.getEt3ResponsePhone());
            // This should be mapped to Representative
            // mentioned in the ticket https://tools.hmcts.net/jira/browse/RET-5054
            // existing statement respondent.setResponseRespondentContactPreference(
            // caseData.getEt3ResponseContactPreference())
            // replaced with representative.setRepresentativePreference(caseData.getEt3ResponseContactPreference())
            representative.setRepresentativePreference(caseData.getEt3ResponseContactPreference());
            // There weren't any mapping of reference for correspondence - representative.
            // mentioned in the ticket https://tools.hmcts.net/jira/browse/RET-5054
            // added this field to representative
            representative.setRepresentativeReference(caseData.getEt3ResponseReference());
            representative.setRepresentativePreferenceReason(caseData.getEt3ResponseContactReason());
            representative.setRepresentativeContactLanguage(caseData.getEt3ResponseContactLanguage());
            representative.setRepresentativeAddress(caseData.getEt3ResponseAddress());
        }
        respondent.setResponseReference(caseData.getEt3ResponseReference());
        respondent.setEt3ResponseRespondentCompanyNumber(caseData.getEt3ResponseRespondentCompanyNumber());
        respondent.setEt3ResponseContactReason(caseData.getEt3ResponseContactReason());
        respondent.setEt3ResponseRespondentEmployerType(caseData.getEt3ResponseRespondentEmployerType());
        respondent.setEt3ResponseRespondentPreferredTitle(caseData.getEt3ResponseRespondentPreferredTitle());
        respondent.setEt3ResponseRespondentContactName(caseData.getEt3ResponseRespondentContactName());
        respondent.setEt3ResponseDXAddress(caseData.getEt3ResponseDXAddress());
        respondent.setEt3ResponseHearingRepresentative(caseData.getEt3ResponseHearingRepresentative());
        respondent.setEt3ResponseHearingRespondent(caseData.getEt3ResponseHearingRespondent());
        respondent.setEt3ResponseRespondentSupportNeeded(caseData.getEt3ResponseRespondentSupportNeeded());
        respondent.setEt3ResponseRespondentSupportDetails(caseData.getEt3ResponseRespondentSupportDetails());
        respondent.setEt3ResponseRespondentSupportDocument(caseData.getEt3ResponseRespondentSupportDocument());
        respondent.setPersonalDetailsSection(YES);
        return respondent;
    }

    private static RespondentSumType addEmploymentDetailsToRespondent(CaseData caseData, RespondentSumType respondent) {
        RepresentedTypeR representative = findRepresentativeFromCaseData(caseData);
        if (representative != null) {
            caseData.setEt3ResponsePhone(representative.getRepresentativePhoneNumber());
            caseData.setEt3ResponseContactPreference(representative.getRepresentativePreference());
            caseData.setEt3ResponseContactReason(representative.getRepresentativePreferenceReason());
            caseData.setEt3ResponseContactLanguage(representative.getRepresentativeContactLanguage());
            caseData.setEt3ResponseAddress(representative.getRepresentativeAddress());
            caseData.setEt3ResponseReference(representative.getRepresentativeReference());
        }
        respondent.setEt3ResponseEmploymentCount(caseData.getEt3ResponseEmploymentCount());
        respondent.setEt3ResponseMultipleSites(caseData.getEt3ResponseMultipleSites());
        respondent.setEt3ResponseAreDatesCorrect(caseData.getEt3ResponseAreDatesCorrect());
        respondent.setEt3ResponseClaimantWeeklyHours(caseData.getEt3ResponseClaimantWeeklyHours());
        respondent.setEt3ResponseSiteEmploymentCount(caseData.getEt3ResponseSiteEmploymentCount());
        respondent.setEt3ResponseEarningDetailsCorrect(caseData.getEt3ResponseEarningDetailsCorrect());
        respondent.setEt3ResponsePayFrequency(caseData.getEt3ResponsePayFrequency());
        respondent.setEt3ResponseClaimantCorrectHours(caseData.getEt3ResponseClaimantCorrectHours());
        respondent.setEt3ResponsePayBeforeTax(caseData.getEt3ResponsePayBeforeTax());
        respondent.setEt3ResponsePayTakehome(caseData.getEt3ResponsePayTakehome());
        respondent.setEt3ResponseIsNoticeCorrect(caseData.getEt3ResponseIsNoticeCorrect());
        respondent.setEt3ResponseCorrectNoticeDetails(caseData.getEt3ResponseCorrectNoticeDetails());
        respondent.setEt3ResponseIsPensionCorrect(caseData.getEt3ResponseIsPensionCorrect());
        respondent.setEt3ResponsePensionCorrectDetails(caseData.getEt3ResponsePensionCorrectDetails());
        respondent.setEmploymentDetailsSection(YES);
        respondent.setEt3ResponseEmploymentEndDate(caseData.getEt3ResponseEmploymentEndDate());
        respondent.setEt3ResponseEmploymentInformation(caseData.getEt3ResponseEmploymentInformation());
        respondent.setEt3ResponseContinuingEmployment(caseData.getEt3ResponseContinuingEmployment());
        respondent.setEt3ResponseIsJobTitleCorrect(caseData.getEt3ResponseIsJobTitleCorrect());
        respondent.setEt3ResponseEmploymentStartDate(caseData.getEt3ResponseEmploymentStartDate());
        respondent.setEt3ResponseCorrectJobTitle(caseData.getEt3ResponseCorrectJobTitle());

        return respondent;
    }

    /**
     * Loads the existing ET3 data onto the form if a resubmission is required.
     *
     * @param caseData data for the case
     */
    public static void reloadDataOntoEt3(CaseData caseData, String eventId) {
        String selectedRespondent = switch (eventId) {
            case ET3_RESPONSE -> caseData.getSubmitEt3Respondent().getSelectedLabel();
            case ET3_RESPONSE_DETAILS, ET3_RESPONSE_EMPLOYMENT_DETAILS -> caseData.getEt3RepresentingRespondent()
                    .getFirst().getValue().getDynamicList().getSelectedLabel();

            default -> throw new IllegalArgumentException(INVALID_EVENT_ID + eventId);
        };
        Optional<RespondentSumTypeItem> respondent = caseData.getRespondentCollection().stream()
                .filter(r -> selectedRespondent.equals(r.getValue().getRespondentName()))
                .findFirst();
        respondent.ifPresent(respondentSumTypeItem -> reloadRespondentToCaseData(caseData,
                respondentSumTypeItem.getValue()));

    }

    private static void reloadRespondentToCaseData(CaseData caseData, RespondentSumType value) {
        caseData.setEt3ResponseIsClaimantNameCorrect(value.getEt3ResponseIsClaimantNameCorrect());
        caseData.setEt3ResponseClaimantNameCorrection(value.getEt3ResponseClaimantNameCorrection());
        caseData.setEt3ResponseRespondentLegalName(value.getResponseRespondentName());
        caseData.setEt3RespondentAddress(value.getResponseRespondentAddress());
        caseData.setEt3ResponsePhone(value.getResponseRespondentPhone1());
        caseData.setEt3ResponseContactPreference(value.getResponseRespondentContactPreference());
        caseData.setEt3ResponseContactReason(value.getEt3ResponseContactReason());
        caseData.setEt3ResponseClaimantNameCorrection(value.getEt3ResponseClaimantNameCorrection());
        caseData.setEt3ResponseIsClaimantNameCorrect(value.getEt3ResponseIsClaimantNameCorrect());
        caseData.setEt3ResponseRespondentCompanyNumber(value.getEt3ResponseRespondentCompanyNumber());
        caseData.setEt3ResponseRespondentEmployerType(value.getEt3ResponseRespondentEmployerType());
        caseData.setEt3ResponseRespondentPreferredTitle(value.getEt3ResponseRespondentPreferredTitle());
        caseData.setEt3ResponseRespondentContactName(value.getEt3ResponseRespondentContactName());
        caseData.setEt3ResponseDXAddress(value.getEt3ResponseDXAddress());
        caseData.setEt3ResponseHearingRepresentative(value.getEt3ResponseHearingRepresentative());
        caseData.setEt3ResponseHearingRespondent(value.getEt3ResponseHearingRespondent());
        caseData.setEt3ResponseEmploymentCount(value.getEt3ResponseEmploymentCount());
        caseData.setEt3ResponseMultipleSites(value.getEt3ResponseMultipleSites());
        caseData.setEt3ResponseSiteEmploymentCount(value.getEt3ResponseSiteEmploymentCount());
        caseData.setEt3ResponseAcasAgree(value.getEt3ResponseAcasAgree());
        caseData.setEt3ResponseAcasAgreeReason(value.getEt3ResponseAcasAgreeReason());
        caseData.setEt3ResponseAreDatesCorrect(value.getEt3ResponseAreDatesCorrect());
        caseData.setEt3ResponseEmploymentStartDate(value.getEt3ResponseEmploymentStartDate());
        caseData.setEt3ResponseEmploymentEndDate(value.getEt3ResponseEmploymentEndDate());
        caseData.setEt3ResponseEmploymentInformation(value.getEt3ResponseEmploymentInformation());
        caseData.setEt3ResponseContinuingEmployment(value.getEt3ResponseContinuingEmployment());
        caseData.setEt3ResponseIsJobTitleCorrect(value.getEt3ResponseIsJobTitleCorrect());
        caseData.setEt3ResponseCorrectJobTitle(value.getEt3ResponseCorrectJobTitle());
        caseData.setEt3ResponseClaimantWeeklyHours(value.getEt3ResponseClaimantWeeklyHours());
        caseData.setEt3ResponseClaimantCorrectHours(value.getEt3ResponseClaimantCorrectHours());
        caseData.setEt3ResponseEarningDetailsCorrect(value.getEt3ResponseEarningDetailsCorrect());
        caseData.setEt3ResponsePayFrequency(value.getEt3ResponsePayFrequency());
        caseData.setEt3ResponsePayBeforeTax(value.getEt3ResponsePayBeforeTax());
        caseData.setEt3ResponsePayTakehome(value.getEt3ResponsePayTakehome());
        caseData.setEt3ResponseIsNoticeCorrect(value.getEt3ResponseIsNoticeCorrect());
        caseData.setEt3ResponseCorrectNoticeDetails(value.getEt3ResponseCorrectNoticeDetails());
        caseData.setEt3ResponseIsPensionCorrect(value.getEt3ResponseIsPensionCorrect());
        caseData.setEt3ResponsePensionCorrectDetails(value.getEt3ResponsePensionCorrectDetails());
        caseData.setEt3ResponseRespondentContestClaim(value.getEt3ResponseRespondentContestClaim());
        caseData.setEt3ResponseContestClaimDocument(value.getEt3ResponseContestClaimDocument());
        caseData.setEt3ResponseContestClaimDetails(value.getEt3ResponseContestClaimDetails());
        caseData.setEt3ResponseEmployerClaim(value.getEt3ResponseEmployerClaim());
        caseData.setEt3ResponseEmployerClaimDetails(value.getEt3ResponseEmployerClaimDetails());
        caseData.setEt3ResponseEmployerClaimDocument(value.getEt3ResponseEmployerClaimDocument());
        caseData.setEt3ResponseRespondentSupportNeeded(value.getEt3ResponseRespondentSupportNeeded());
        caseData.setEt3ResponseRespondentSupportDetails(value.getEt3ResponseRespondentSupportDetails());
        caseData.setEt3ResponseRespondentSupportDocument(value.getEt3ResponseRespondentSupportDocument());
        RepresentedTypeR representative = findRepresentativeFromCaseData(caseData);
        if (ObjectUtils.isNotEmpty(representative)) {
            caseData.setEt3ResponsePhone(representative.getRepresentativePhoneNumber());
            caseData.setEt3ResponseContactPreference(representative.getRepresentativePreference());
            caseData.setEt3ResponseContactReason(representative.getRepresentativePreferenceReason());
            caseData.setEt3ResponseContactLanguage(representative.getRepresentativeContactLanguage());
            caseData.setEt3ResponseAddress(representative.getRepresentativeAddress());
            caseData.setEt3ResponseReference(representative.getRepresentativeReference());
        }
    }

    /**
     * Reset the fields in the ET3 Response form event.
     *
     * @param caseData data for the case
     */
    public static void resetEt3FormFields(CaseData caseData) {
        caseData.setEt3RepresentingRespondent(null);
        caseData.setSubmitEt3Respondent(null);
        caseData.setEt3ResponseShowInset(null);
        caseData.setEt3ResponseClaimantName(null);
        caseData.setEt3ResponseIsClaimantNameCorrect(null);
        caseData.setEt3ResponseClaimantNameCorrection(null);
        caseData.setEt3ResponseNameShowInset(null);
        caseData.setEt3ResponseRespondentLegalName(null);
        caseData.setEt3ResponseRespondentCompanyNumber(null);
        caseData.setEt3ResponseRespondentEmployerType(null);
        caseData.setEt3ResponseRespondentPreferredTitle(null);
        caseData.setEt3ResponseRespondentContactName(null);
        caseData.setEt3RespondentAddress(null);
        caseData.setEt3ResponseDXAddress(null);
        caseData.setEt3ResponsePhone(null);
        caseData.setEt3ResponseReference(null);
        caseData.setEt3ResponseContactPreference(null);
        caseData.setEt3ResponseContactReason(null);
        caseData.setEt3ResponseHearingRepresentative(null);
        caseData.setEt3ResponseHearingRespondent(null);
        caseData.setEt3ResponseEmploymentCount(null);
        caseData.setEt3ResponseMultipleSites(null);
        caseData.setEt3ResponseSiteEmploymentCount(null);
        caseData.setEt3ResponseAcasAgree(null);
        caseData.setEt3ResponseAcasAgreeReason(null);
        caseData.setEt3ResponseAreDatesCorrect(null);
        caseData.setEt3ResponseEmploymentStartDate(null);
        caseData.setEt3ResponseEmploymentEndDate(null);
        caseData.setEt3ResponseEmploymentInformation(null);
        caseData.setEt3ResponseContinuingEmployment(null);
        caseData.setEt3ResponseIsJobTitleCorrect(null);
        caseData.setEt3ResponseCorrectJobTitle(null);
        caseData.setEt3ResponseClaimantWeeklyHours(null);
        caseData.setEt3ResponseClaimantCorrectHours(null);
        caseData.setEt3ResponseEarningDetailsCorrect(null);
        caseData.setEt3ResponsePayFrequency(null);
        caseData.setEt3ResponsePayBeforeTax(null);
        caseData.setEt3ResponsePayTakehome(null);
        caseData.setEt3ResponseIsNoticeCorrect(null);
        caseData.setEt3ResponseCorrectNoticeDetails(null);
        caseData.setEt3ResponseIsPensionCorrect(null);
        caseData.setEt3ResponsePensionCorrectDetails(null);
        caseData.setEt3ResponseRespondentContestClaim(null);
        caseData.setEt3ResponseContestClaimDocument(null);
        caseData.setEt3ResponseContestClaimDetails(null);
        caseData.setEt3ResponseEmployerClaim(null);
        caseData.setEt3ResponseEmployerClaimDetails(null);
        caseData.setEt3ResponseEmployerClaimDocument(null);
        caseData.setEt3ResponseRespondentSupportNeeded(null);
        caseData.setEt3ResponseRespondentSupportDetails(null);
        caseData.setEt3ResponseRespondentSupportDocument(null);
        caseData.setEt3ResponseContactLanguage(null);
        caseData.setEt3ResponseAddress(null);
    }

    /**
     * Create a list of respondents for ET3 submission.
     *
     * @param caseData caseData
     * @return a list of errors if any
     */
    public static List<String> et3SubmitRespondents(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return List.of(NO_RESPONDENTS_FOUND);
        }

        List<RespondentSumTypeItem> validRespondents = caseData.getRespondentCollection().stream()
                .filter(Et3ResponseHelper::checkRespondentSections)
                .toList();

        if (CollectionUtils.isEmpty(validRespondents)) {
            return List.of(ALL_RESPONDENTS_INCOMPLETE_SECTIONS);
        }
        DynamicFixedListType dynamicList = DynamicFixedListType.from(
                DynamicListHelper.createDynamicRespondentName(validRespondents));
        caseData.setSubmitEt3Respondent(dynamicList);
        return new ArrayList<>();
    }

    private static boolean checkRespondentSections(RespondentSumTypeItem respondentSumTypeItem) {
        RespondentSumType respondent = respondentSumTypeItem.getValue();
        return YES.equals(respondent.getPersonalDetailsSection())
                && YES.equals(respondent.getEmploymentDetailsSection())
                && YES.equals(respondent.getClaimDetailsSection())
                && isAllowSubmit(respondent);
    }

    /**
     * Reloads the data from the respondent to the toplevel caseData to generate the ET3 for submission and attach
     * documents to document collection.
     *
     * @param caseData caseData
     */
    public static void reloadSubmitOntoEt3(CaseData caseData) {
        if (caseData.getSubmitEt3Respondent() == null
                || CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return;
        }

        String selectedRespondent = caseData.getSubmitEt3Respondent().getSelectedLabel();
        Optional<RespondentSumTypeItem> respondent = caseData.getRespondentCollection().stream()
                .filter(r -> selectedRespondent.equals(r.getValue().getRespondentName()))
                .findFirst();
        respondent.ifPresent(respondentSumTypeItem -> reloadRespondentToCaseData(caseData,
                respondentSumTypeItem.getValue()));

    }

    public static RepresentedTypeR findRepresentativeFromCaseData(CaseData caseData) {
        if (isCaseDataRespondentEmpty(caseData)) {
            return null;
        }
        Stream<RepresentedTypeRItem> selectedRepresentativeStream = caseData.getRepCollection().stream().filter(
                rep -> isNotEmpty(rep.getValue()) && rep.getValue().getRespRepName().equals(
                        caseData.getSubmitEt3Respondent().getSelectedLabel()
                )
        );
        if (isEmpty(selectedRepresentativeStream)) {
            return null;
        }
        Optional<RepresentedTypeRItem> selectedRepresentative = selectedRepresentativeStream.findFirst();
        if (selectedRepresentative.isEmpty()
                || isEmpty(selectedRepresentative.get())
                || isEmpty(selectedRepresentative.get().getValue())) {
            return null;
        }
        return selectedRepresentative.get().getValue();
    }

    private static boolean isCaseDataRespondentEmpty(CaseData caseData) {
        return isEmpty(caseData) || CollectionUtils.isEmpty(caseData.getRepCollection())
                || isEmpty(caseData.getSubmitEt3Respondent())
                || isBlank(caseData.getSubmitEt3Respondent().getSelectedLabel());
    }

    public static void setEt3NotificationAcceptedDates(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return;
        }
        for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
            if (isNotEmpty(respondentSumTypeItem.getValue())) {
                if (isET3NotificationDocumentTypeResponseAccepted(caseData.getEt3NotificationDocCollection())
                        && ACCEPTED_STATE.equals(respondentSumTypeItem.getValue().getResponseStatus())) {
                    respondentSumTypeItem.getValue().setEt3NotificationAcceptedDate(LocalDate.now().toString());
                } else {
                    respondentSumTypeItem.getValue().setEt3NotificationAcceptedDate(null);
                }
            }
        }
    }
}
