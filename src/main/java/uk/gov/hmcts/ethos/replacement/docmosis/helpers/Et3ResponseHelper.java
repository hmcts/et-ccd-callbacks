package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DynamicListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DynamicListType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.Et3ResponseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.Et3ResponseDocument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * ET3 Response Helper provides methods to assist with the ET3 Response Form event.
 */
@Slf4j
@SuppressWarnings({"PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal", "PMD.LinguisticNaming",
    "PMD.ExcessiveMethodLength", "PMD.ClassNamingConventions", "PMD.PrematureDeclaration", "PMD.GodClass",
    "PMD.CyclomaticComplexity"})
public class Et3ResponseHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEMPLATE_NAME = "EM-TRB-EGW-ENG-00700.docx";
    private static final String OUTPUT_NAME = "ET3 Response.pdf";
    private static final String CLAIMANT_NAME_TABLE = "<pre> ET1 claimant name&#09&#09&#09&#09 %s</pre><hr>";
    public static final String START_DATE_MUST_BE_IN_THE_PAST = "Start date must be in the past";
    public static final String END_DATE_MUST_BE_AFTER_THE_START_DATE = "End date must be after the start date";
    public static final String CHECKED = "■"; // U+25A0
    public static final String UNCHECKED = "□"; // U+25A1
    private static final String ET3_RESPONSE = "et3Response";

    private static final String ET3_RESPONSE_EMPLOYMENT_DETAILS = "et3ResponseEmploymentDetails";
    private static final String ET3_RESPONSE_CLAIM_DETAILS = "et3ResponseClaimDetails";

    private Et3ResponseHelper() {
        // Access through static methods
    }

    /**
     * Formats the name of the claimant for display on the Claimant name correct page.
     * @param caseData data for the current case
     * @return Name ready for presentation on web
     */
    public static String formatClaimantNameForHtml(CaseData caseData) {
        return String.format(CLAIMANT_NAME_TABLE, caseData.getClaimant());
    }

    /**
     * Validates that the employment start date is in the past and not after 
     * the employment end date if both dates are provided.
     * @param caseData data for the current case
     * @return List of validation errors encountered
     */
    public static List<String> validateEmploymentDates(CaseData caseData) {
        ArrayList<String> errors = new ArrayList<>();
        String startDateStr = caseData.getEt3ResponseEmploymentStartDate();
        String endDateStr = caseData.getEt3ResponseEmploymentEndDate();

        if (isNullOrEmpty(startDateStr)) {
            return errors;
        }

        LocalDate startDate = LocalDate.parse(startDateStr);

        if (startDate.isAfter(LocalDate.now())) {
            errors.add(START_DATE_MUST_BE_IN_THE_PAST);
        }

        if (isNullOrEmpty(endDateStr)) {
            return errors;
        }

        LocalDate endDate = LocalDate.parse(endDateStr);

        if (startDate.isAfter(endDate)) {
            errors.add(END_DATE_MUST_BE_AFTER_THE_START_DATE);
        }

        return errors;
    }

    private static void setValue(String value, Consumer<String> fn) {
        if (fn == null) {
            return;
        }
        fn.accept(value);
    }

    private static void setCheck(String option, Consumer<String> yes, Consumer<String> no, Consumer<String> other) {
        setValue(UNCHECKED, yes);
        setValue(UNCHECKED, no);
        setValue(UNCHECKED, other);

        if ("Yes".equals(option)) {
            setValue(CHECKED, yes);
        } else if ("No".equals(option)) {
            setValue(CHECKED, no);
        } else {
            setValue(CHECKED, other);
        }
    }

    private static void setTitle(Et3ResponseData data, String title) {
        data.setTitleMr(UNCHECKED);
        data.setTitleMrs(UNCHECKED);
        data.setTitleMiss(UNCHECKED);
        data.setTitleMs(UNCHECKED);
        data.setTitleOther(UNCHECKED);

        if (isNullOrEmpty(title)) {
            return;
        }

        switch  (title) {
            case "Mr":
                data.setTitleMr(CHECKED);
                break;
            case "Mrs":
                data.setTitleMrs(CHECKED);
                break;
            case "Miss":
                data.setTitleMiss(CHECKED);
                break;
            case "Ms":
                data.setTitleMs(CHECKED);
                break;
            default:
                data.setTitleOther(CHECKED);
                data.setTitleText(title);
                break;
        }
    }

    /**
     * Formats data needed for the ET3 Response form document.
     * @param caseData data for the case
     * @param accessKey required to use docmosis
     * @return document request for docmosis to create the final ET3 Response form
     */
    public static String getDocumentRequest(CaseData caseData, String accessKey)  throws JsonProcessingException {
        if (caseData.getSubmitEt3Respondent() == null || CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            throw new RuntimeException();
        }
        String submitRespondent = caseData.getSubmitEt3Respondent().getSelectedLabel();
        RespondentSumType respondentSumType = caseData.getRespondentCollection().stream()
                .filter(r -> submitRespondent.equals(r.getValue().getRespondentName()))
                .collect(Collectors.toList()).get(0).getValue();

        Et3ResponseData data = Et3ResponseData.builder()
            .ethosCaseReference(caseData.getEthosCaseReference())
            .et3ResponseClaimantName(isNullOrEmpty(respondentSumType.getEt3ResponseClaimantNameCorrection())
                    ? caseData.getClaimant()
                    : respondentSumType.getEt3ResponseClaimantNameCorrection())
            .et3ResponseRespondentLegalName(defaultIfEmpty(respondentSumType.getResponseRespondentName(), null))
            .et3ResponseRespondentCompanyNumber(
                    defaultIfEmpty(respondentSumType.getEt3ResponseRespondentCompanyNumber(), null))
            .et3ResponseRespondentEmployerType(
                    defaultIfEmpty(respondentSumType.getEt3ResponseRespondentEmployerType(), null))
            .et3ResponseRespondentContactName(
                    defaultIfEmpty(respondentSumType.getEt3ResponseRespondentContactName(), null))
            .respondentAddressLine1(
                    defaultIfEmpty(respondentSumType.getResponseRespondentAddress().getAddressLine1(), null))
            .respondentAddressLine2(
                    defaultIfEmpty(respondentSumType.getResponseRespondentAddress().getAddressLine2(), null))
            .respondentCity(defaultIfEmpty(respondentSumType.getResponseRespondentAddress().getPostTown(), null))
            .respondentPostcode(defaultIfEmpty(respondentSumType.getResponseRespondentAddress().getPostCode(), null))
            .et3ResponseDXAddress(defaultIfEmpty(respondentSumType.getEt3ResponseDXAddress(), null))
            .et3ResponsePhone(defaultIfEmpty(respondentSumType.getResponseRespondentPhone1(), null))
            .et3ResponseEmploymentCount(defaultIfEmpty(respondentSumType.getEt3ResponseEmploymentCount(), null))
            .et3ResponseSiteEmploymentCount(defaultIfEmpty(respondentSumType.getEt3ResponseSiteEmploymentCount(), null))
            .et3ResponseAcasAgreeReason(defaultIfEmpty(respondentSumType.getEt3ResponseAcasAgreeReason(), null))
            .et3ResponseEmploymentStartDate(defaultIfEmpty(respondentSumType.getEt3ResponseEmploymentStartDate(), null))
            .et3ResponseEmploymentEndDate(defaultIfEmpty(respondentSumType.getEt3ResponseEmploymentEndDate(), null))
            .et3ResponseEmploymentInformation(
                    defaultIfEmpty(respondentSumType.getEt3ResponseEmploymentInformation(), null))
            .et3ResponseCorrectJobTitle(defaultIfEmpty(respondentSumType.getEt3ResponseCorrectJobTitle(), null))
            .et3ResponseClaimantCorrectHours(
                    defaultIfEmpty(respondentSumType.getEt3ResponseClaimantCorrectHours(), null))
            .et3ResponsePayBeforeTax(defaultIfEmpty(respondentSumType.getEt3ResponsePayBeforeTax(), null))
            .et3ResponsePayTakehome(defaultIfEmpty(respondentSumType.getEt3ResponsePayTakehome(), null))
            .et3ResponseCorrectNoticeDetails(
                    defaultIfEmpty(respondentSumType.getEt3ResponseCorrectNoticeDetails(), null))
            .et3ResponseContestClaimDetails(defaultIfEmpty(respondentSumType.getEt3ResponseContestClaimDetails(), null))
            .et3ResponseEmployerClaimDetails(
                    defaultIfEmpty(respondentSumType.getEt3ResponseEmployerClaimDetails(), null))
            .et3ResponseRespondentSupportDetails(
                    defaultIfEmpty(respondentSumType.getEt3ResponseRespondentSupportDetails(), null))
            .et3ResponsePensionCorrectDetails(
                    defaultIfEmpty(respondentSumType.getEt3ResponsePensionCorrectDetails(), null))
            .hearingPhone(respondentSumType.getEt3ResponseHearingRespondent().contains("Phone hearings")
                    ? CHECKED
                    : UNCHECKED)
            .hearingVideo(respondentSumType.getEt3ResponseHearingRespondent().contains("Video hearings")
                    ? CHECKED
                    : UNCHECKED)
            .repHearingPhone(respondentSumType.getEt3ResponseHearingRepresentative().contains("Phone hearings")
                    ? CHECKED
                    : UNCHECKED)
            .repHearingVideo(respondentSumType.getEt3ResponseHearingRepresentative().contains("Video hearings")
                    ? CHECKED
                    : UNCHECKED)
            .build();

        setTitle(data, respondentSumType.getEt3ResponseRespondentPreferredTitle());
        setCheck(respondentSumType.getEt3ResponseMultipleSites(), data::setSiteYes, data::setSiteNo, null);
        setCheck(respondentSumType.getEt3ResponseAcasAgree(), data::setAcasYes, data::setAcasNo, null);
        setCheck(respondentSumType.getEt3ResponseAreDatesCorrect(),
                data::setDatesYes, data::setDatesNo, data::setDatesNA);
        setCheck(respondentSumType.getEt3ResponseIsJobTitleCorrect(),
                data::setJobYes, data::setJobNo, data::setJobNA);
        setCheck(respondentSumType.getEt3ResponseClaimantWeeklyHours(),
                data::setHoursYes, data::setHoursNo, data::setHoursNA);
        setCheck(respondentSumType.getEt3ResponseEarningDetailsCorrect(),
                data::setEarnYes, data::setEarnNo, data::setEarnNA);
        setCheck(respondentSumType.getEt3ResponseIsNoticeCorrect(),
                data::setNoticeYes, data::setNoticeNo, data::setNoticeNA);
        setCheck(respondentSumType.getEt3ResponseRespondentContestClaim(),
                data::setContestYes, data::setContestNo, null);
        setCheck(respondentSumType.getEt3ResponseRespondentSupportNeeded(),
                data::setHelpYes, data::setHelpNo, data::setHelpNA);
        setCheck(respondentSumType.getEt3ResponseEmployerClaim(), data::setEccYes, null, null);
        setCheck(respondentSumType.getEt3ResponseContinuingEmployment(), data::setContinueYes, data::setContinueNo,
            data::setContinueNA);
        setCheck(respondentSumType.getEt3ResponseIsPensionCorrect(), data::setPensionYes, data::setPensionNo,
            data::setPensionNA);
        setCheck("Post".equals(respondentSumType.getResponseRespondentContactPreference()) ? "Yes" : "No",
                data::setContactPost, data::setContactEmail, null);

        data.setRepContactEmail(UNCHECKED);
        data.setRepContactPost(UNCHECKED);
        data.setBeforeWeekly(UNCHECKED);
        data.setTakehomeWeekly(UNCHECKED);
        data.setBeforeMonthly(UNCHECKED);
        data.setTakehomeMonthly(UNCHECKED);
        data.setBeforeAnnually(UNCHECKED);
        data.setTakehomeAnnually(UNCHECKED);

        if (NO.equals(respondentSumType.getEt3ResponseEarningDetailsCorrect())
                && respondentSumType.getEt3ResponsePayFrequency() != null) {
            if ("Weekly".equals(respondentSumType.getEt3ResponsePayFrequency())) {
                data.setBeforeWeekly(CHECKED);
                data.setTakehomeWeekly(CHECKED);
            } else if ("Monthly".equals(respondentSumType.getEt3ResponsePayFrequency())) {
                data.setBeforeMonthly(CHECKED);
                data.setTakehomeMonthly(CHECKED);
            } else {
                data.setBeforeAnnually(CHECKED);
                data.setTakehomeAnnually(CHECKED);
            }
        }

        Et3ResponseDocument et3ResponseDocument = Et3ResponseDocument.builder()
            .accessKey(accessKey)
            .outputName(OUTPUT_NAME)
            .templateName(TEMPLATE_NAME)
            .et3ResponseData(data)
            .build();

        return OBJECT_MAPPER.writeValueAsString(et3ResponseDocument);
    }

    /**
     * Create a collection of DynamicLists of respondent names.
     * @param caseData data for the case
     */
    public static List<String> createDynamicListSelection(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return List.of("No respondents found");
        }

        DynamicFixedListType dynamicList = DynamicFixedListType.from(DynamicListHelper.createDynamicRespondentName(
                caseData.getRespondentCollection().stream()
                        .filter(r -> NO.equals(r.getValue().getResponseReceived()))
                        .collect(Collectors.toList())));
        DynamicListType dynamicListType = new DynamicListType();
        dynamicListType.setDynamicList(dynamicList);
        DynamicListTypeItem dynamicListTypeItem = new DynamicListTypeItem();
        dynamicListTypeItem.setValue(dynamicListType);
        caseData.setEt3RepresentingRespondent(List.of(dynamicListTypeItem));
        return new ArrayList<>();
    }

    /**
     * Validate the selection of respondents.
     * @param caseData data for the case
     * @return an error is the user has selected the same respondent multiple times
     */
    public static List<String> validateRespondents(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getEt3RepresentingRespondent())) {
            return List.of("No respondents found");
        }

        Set<String> respondentSet = new HashSet<>();
        for (DynamicListTypeItem dynamicListTypeItem : caseData.getEt3RepresentingRespondent()) {
            respondentSet.add(String.valueOf(DynamicFixedListType.getSelectedLabel(
                    dynamicListTypeItem.getValue().getDynamicList())));
        }

        if (respondentSet.size() != caseData.getEt3RepresentingRespondent().size()) {
            return List.of("Please do not choose the same respondent multiple times");
        }
        return new ArrayList<>();
    }

    /**
     * Saves the data from the ET3 Form event to the respondent.
     * @param caseData data for the case
     */
    public static void addEt3DataToRespondent(CaseData caseData, String eventId) {
        if (CollectionUtils.isEmpty(caseData.getEt3RepresentingRespondent())
                || CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return;
        }

        Set<String> respondentSet = new HashSet<>();
        for (DynamicListTypeItem dynamicListTypeItem : caseData.getEt3RepresentingRespondent()) {
            respondentSet.add(dynamicListTypeItem.getValue().getDynamicList().getSelectedLabel());
        }

        for (String respondentSelected : respondentSet) {
            Optional<RespondentSumTypeItem> respondent = caseData.getRespondentCollection().stream()
                    .filter(r -> respondentSelected.equals(r.getValue().getRespondentName()))
                    .findFirst();
            if (respondent.isPresent()) {
                RespondentSumType respondentSumType;
                switch (eventId) {
                    case ET3_RESPONSE :
                        respondentSumType = addPersonalDetailsToRespondent(caseData, respondent.get().getValue());
                        break;
                    case ET3_RESPONSE_EMPLOYMENT_DETAILS:
                        respondentSumType = addEmploymentDetailsToRespondent(caseData, respondent.get().getValue());
                        break;
                    case ET3_RESPONSE_CLAIM_DETAILS:
                        respondentSumType = addClaimDetailsToRespondent(caseData, respondent.get().getValue());
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid eventId");

                }
                for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
                    if (respondentSelected.equals(respondentSumTypeItem.getValue().getRespondentName())) {
                        respondentSumTypeItem.setValue(respondentSumType);
                    }
                }
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

    private static RespondentSumType addPersonalDetailsToRespondent(CaseData caseData, RespondentSumType respondent) {
        respondent.setEt3ResponseIsClaimantNameCorrect(caseData.getEt3ResponseIsClaimantNameCorrect());
        respondent.setEt3ResponseClaimantNameCorrection(caseData.getEt3ResponseClaimantNameCorrection());
        respondent.setResponseRespondentName(caseData.getEt3ResponseRespondentLegalName());
        respondent.setResponseRespondentAddress(caseData.getEt3RespondentAddress());
        respondent.setResponseRespondentPhone1(caseData.getEt3ResponsePhone());
        respondent.setResponseRespondentContactPreference(caseData.getEt3ResponseContactPreference());
        respondent.setEt3ResponseContactReason(caseData.getEt3ResponseContactReason());
        respondent.setEt3ResponseRespondentCompanyNumber(caseData.getEt3ResponseRespondentCompanyNumber());
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
        respondent.setEt3ResponseEmploymentCount(caseData.getEt3ResponseEmploymentCount());
        respondent.setEt3ResponseMultipleSites(caseData.getEt3ResponseMultipleSites());
        respondent.setEt3ResponseSiteEmploymentCount(caseData.getEt3ResponseSiteEmploymentCount());
        respondent.setEt3ResponseAreDatesCorrect(caseData.getEt3ResponseAreDatesCorrect());
        respondent.setEt3ResponseEmploymentStartDate(caseData.getEt3ResponseEmploymentStartDate());
        respondent.setEt3ResponseEmploymentEndDate(caseData.getEt3ResponseEmploymentEndDate());
        respondent.setEt3ResponseEmploymentInformation(caseData.getEt3ResponseEmploymentInformation());
        respondent.setEt3ResponseContinuingEmployment(caseData.getEt3ResponseContinuingEmployment());
        respondent.setEt3ResponseIsJobTitleCorrect(caseData.getEt3ResponseIsJobTitleCorrect());
        respondent.setEt3ResponseCorrectJobTitle(caseData.getEt3ResponseCorrectJobTitle());
        respondent.setEt3ResponseClaimantWeeklyHours(caseData.getEt3ResponseClaimantWeeklyHours());
        respondent.setEt3ResponseClaimantCorrectHours(caseData.getEt3ResponseClaimantCorrectHours());
        respondent.setEt3ResponseEarningDetailsCorrect(caseData.getEt3ResponseEarningDetailsCorrect());
        respondent.setEt3ResponsePayFrequency(caseData.getEt3ResponsePayFrequency());
        respondent.setEt3ResponsePayBeforeTax(caseData.getEt3ResponsePayBeforeTax());
        respondent.setEt3ResponsePayTakehome(caseData.getEt3ResponsePayTakehome());
        respondent.setEt3ResponseIsNoticeCorrect(caseData.getEt3ResponseIsNoticeCorrect());
        respondent.setEt3ResponseCorrectNoticeDetails(caseData.getEt3ResponseCorrectNoticeDetails());
        respondent.setEt3ResponseIsPensionCorrect(caseData.getEt3ResponseIsPensionCorrect());
        respondent.setEt3ResponsePensionCorrectDetails(caseData.getEt3ResponsePensionCorrectDetails());
        respondent.setEmploymentDetailsSection(YES);
        return respondent;
    }

    /**
     * Loads the existing ET3 data onto the form if a resubmission is required.
     * @param caseData data for the case
     */
    public static void reloadDataOntoEt3(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getEt3RepresentingRespondent())
                || CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return;
        }

        String selectedRespondent = caseData.getEt3RepresentingRespondent().get(0).getValue()
                .getDynamicList().getSelectedLabel();
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
    }

    /**
     * Reset the fields in the ET3 Response form event.
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
    }

    public static List<String> et3SubmitRespondents(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            return List.of("No respondents found");
        }

        var validRespondents = caseData.getRespondentCollection().stream()
                .filter(Et3ResponseHelper::checkRespondentSections)
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(validRespondents)) {
            return List.of("There are no respondents that can currently submit an ET3 Form. Please make sure all 3 sections have been completed for a respondent");
        }
        DynamicFixedListType dynamicList = DynamicFixedListType.from(
                DynamicListHelper.createDynamicRespondentName(validRespondents));
        DynamicListType dynamicListType = new DynamicListType();
        dynamicListType.setDynamicList(dynamicList);
        caseData.setSubmitEt3Respondent(dynamicList);
        return new ArrayList<>();
    }

    public static boolean checkRespondentSection(CaseData caseData) {
        if (CollectionUtils.isNotEmpty(caseData.getRespondentCollection())) {
            for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
                if (checkRespondentSections(respondentSumTypeItem)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean checkRespondentSections(RespondentSumTypeItem respondentSumTypeItem) {
        RespondentSumType respondent = respondentSumTypeItem.getValue();
        return YES.equals(respondent.getPersonalDetailsSection())
                && YES.equals(respondent.getEmploymentDetailsSection())
                && YES.equals(respondent.getClaimDetailsSection())
                && NO.equals(respondent.getResponseReceived());
    }

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

}
