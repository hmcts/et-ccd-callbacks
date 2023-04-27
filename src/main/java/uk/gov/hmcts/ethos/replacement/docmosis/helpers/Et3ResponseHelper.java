package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.ccd.Address;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DynamicListTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RepresentedTypeRItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DynamicListType;
import uk.gov.hmcts.et.common.model.ccd.types.RepresentedTypeR;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

/**
 * ET3 Response Helper provides methods to assist with the ET3 Response Form event.
 */
@Slf4j
public final class Et3ResponseHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEMPLATE_NAME = "EM-TRB-EGW-ENG-00700.docx";
    private static final String OUTPUT_NAME = "ET3 Response.pdf";
    private static final String CLAIMANT_NAME_TABLE = "<pre> ET1 claimant name&#09&#09&#09&#09 %s</pre><hr>";
    public static final String START_DATE_MUST_BE_IN_THE_PAST = "Start date must be in the past";
    public static final String END_DATE_MUST_BE_AFTER_THE_START_DATE = "End date must be after the start date";
    public static final String CHECKED = "■"; // U+25A0
    public static final String UNCHECKED = "□"; // U+25A1

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
        Et3ResponseData data = Et3ResponseData.builder()
            .ethosCaseReference(caseData.getEthosCaseReference())
            .et3ResponseClaimantName(caseData.getClaimant())
            .et3ResponseRespondentLegalName(caseData.getEt3ResponseRespondentLegalName())
            .et3ResponseRespondentCompanyNumber(caseData.getEt3ResponseRespondentCompanyNumber())
            .et3ResponseRespondentEmployerType(caseData.getEt3ResponseRespondentEmployerType())
            .et3ResponseRespondentContactName(caseData.getEt3ResponseRespondentContactName())
            .respondentAddressLine1(caseData.getEt3RespondentAddress().getAddressLine1())
            .respondentAddressLine2(caseData.getEt3RespondentAddress().getAddressLine2())
            .respondentCity(caseData.getEt3RespondentAddress().getPostTown())
            .respondentPostcode(caseData.getEt3RespondentAddress().getPostCode())
            .et3ResponseDXAddress(caseData.getEt3ResponseDXAddress())
            .et3ResponsePhone(caseData.getEt3ResponsePhone())
            .et3ResponseEmploymentCount(caseData.getEt3ResponseEmploymentCount())
            .et3ResponseSiteEmploymentCount(caseData.getEt3ResponseSiteEmploymentCount())
            .et3ResponseAcasAgreeReason(caseData.getEt3ResponseAcasAgreeReason())
            .et3ResponseEmploymentStartDate(caseData.getEt3ResponseEmploymentStartDate())
            .et3ResponseEmploymentEndDate(caseData.getEt3ResponseEmploymentEndDate())
            .et3ResponseEmploymentInformation(caseData.getEt3ResponseEmploymentInformation())
            .et3ResponseCorrectJobTitle(caseData.getEt3ResponseCorrectJobTitle())
            .et3ResponseClaimantCorrectHours(caseData.getEt3ResponseClaimantCorrectHours())
            .et3ResponsePayBeforeTax(caseData.getEt3ResponsePayBeforeTax())
            .et3ResponsePayTakehome(caseData.getEt3ResponsePayTakehome())
            .et3ResponseCorrectNoticeDetails(caseData.getEt3ResponseCorrectNoticeDetails())
            .et3ResponseContestClaimDetails(caseData.getEt3ResponseContestClaimDetails())
            .et3ResponseEmployerClaimDetails(caseData.getEt3ResponseEmployerClaimDetails())
            .et3ResponseRespondentSupportDetails(caseData.getEt3ResponseRespondentSupportDetails())
            .et3ResponsePensionCorrectDetails(caseData.getEt3ResponsePensionCorrectDetails())
            .hearingPhone(caseData.getEt3ResponseHearingRespondent().contains("Phone hearings") ? CHECKED : UNCHECKED)
            .hearingVideo(caseData.getEt3ResponseHearingRespondent().contains("Video hearings") ? CHECKED : UNCHECKED)
            .repHearingPhone(caseData.getEt3ResponseHearingRepresentative().contains("Phone hearings") ? CHECKED :
                UNCHECKED)
            .repHearingVideo(caseData.getEt3ResponseHearingRepresentative().contains("Video hearings") ? CHECKED :
                UNCHECKED)
            .build();

        addRepDetails(caseData, data);

        setTitle(data, caseData.getEt3ResponseRespondentPreferredTitle());
        setCheck(caseData.getEt3ResponseMultipleSites(), data::setSiteYes, data::setSiteNo, null);
        setCheck(caseData.getEt3ResponseAcasAgree(), data::setAcasYes, data::setAcasNo, null);
        setCheck(caseData.getEt3ResponseAreDatesCorrect(), data::setDatesYes, data::setDatesNo, data::setDatesNA);
        setCheck(caseData.getEt3ResponseIsJobTitleCorrect(), data::setJobYes, data::setJobNo, data::setJobNA);
        setCheck(caseData.getEt3ResponseClaimantWeeklyHours(), data::setHoursYes, data::setHoursNo, data::setHoursNA);
        setCheck(caseData.getEt3ResponseEarningDetailsCorrect(), data::setEarnYes, data::setEarnNo, data::setEarnNA);
        setCheck(caseData.getEt3ResponseIsNoticeCorrect(), data::setNoticeYes, data::setNoticeNo, data::setNoticeNA);
        setCheck(caseData.getEt3ResponseRespondentContestClaim(), data::setContestYes, data::setContestNo, null);
        setCheck(caseData.getEt3ResponseRespondentSupportNeeded(), data::setHelpYes, data::setHelpNo, data::setHelpNA);
        setCheck(caseData.getEt3ResponseEmployerClaim(), data::setEccYes, null, null);
        setCheck(caseData.getEt3ResponseContinuingEmployment(), data::setContinueYes, data::setContinueNo,
            data::setContinueNA);
        setCheck(caseData.getEt3ResponseIsPensionCorrect(), data::setPensionYes, data::setPensionNo,
            data::setPensionNA);
        setCheck("Post".equals(caseData.getEt3ResponseContactPreference()) ? "Yes" : "No", data::setContactPost,
            data::setContactEmail, null);

        data.setRepContactEmail(UNCHECKED);
        data.setRepContactPost(UNCHECKED);
        data.setBeforeWeekly(UNCHECKED);
        data.setTakehomeWeekly(UNCHECKED);
        data.setBeforeMonthly(UNCHECKED);
        data.setTakehomeMonthly(UNCHECKED);
        data.setBeforeAnnually(UNCHECKED);
        data.setTakehomeAnnually(UNCHECKED);

        if (NO.equals(caseData.getEt3ResponseEarningDetailsCorrect())
                && caseData.getEt3ResponsePayFrequency() != null) {
            if ("Weekly".equals(caseData.getEt3ResponsePayFrequency())) {
                data.setBeforeWeekly(CHECKED);
                data.setTakehomeWeekly(CHECKED);
            } else if ("Monthly".equals(caseData.getEt3ResponsePayFrequency())) {
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

    private static void addRepDetails(CaseData caseData, Et3ResponseData data) {
        Optional<RepresentedTypeRItem> representative = caseData.getRepCollection().stream().filter(
            rep -> rep.getValue().getRespRepName().equals(
                caseData.getEt3RepresentingRespondent().get(0).getValue().getDynamicList().getSelectedLabel()
            )
        ).findFirst();

        if (representative.isPresent()) {
            RepresentedTypeR rep = representative.get().getValue();
            data.setRepName(rep.getNameOfRepresentative());
            data.setRepOrgName(rep.getNameOfOrganisation());

            Address address = rep.getRepresentativeAddress();
            data.setRepAddressLine1(address.getAddressLine1());
            data.setRepAddressLine2(address.getAddressLine2());
            data.setRepTown(address.getPostTown());
            data.setRepCounty(address.getCounty());
            data.setRepPostcode(address.getPostCode());

            data.setRepPhoneNumber(rep.getRepresentativePhoneNumber());
            data.setRepEmailAddress(rep.getRepresentativeEmailAddress());
        }
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
    public static void addEt3DataToRespondent(CaseData caseData) {
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
                RespondentSumType respondentSumType = addEt3Data(caseData, respondent.get().getValue());
                for (RespondentSumTypeItem respondentSumTypeItem : caseData.getRespondentCollection()) {
                    if (respondentSelected.equals(respondentSumTypeItem.getValue().getRespondentName())) {
                        respondentSumTypeItem.setValue(respondentSumType);
                    }
                }
            }
        }
    }

    private static RespondentSumType addEt3Data(CaseData caseData, RespondentSumType respondent) {
        respondent.setResponseReceived(YES);
        respondent.setResponseReceivedDate(LocalDate.now().toString());
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
        respondent.setEt3ResponseEmploymentCount(caseData.getEt3ResponseEmploymentCount());
        respondent.setEt3ResponseMultipleSites(caseData.getEt3ResponseMultipleSites());
        respondent.setEt3ResponseSiteEmploymentCount(caseData.getEt3ResponseSiteEmploymentCount());
        respondent.setEt3ResponseAcasAgree(caseData.getEt3ResponseAcasAgree());
        respondent.setEt3ResponseAcasAgreeReason(caseData.getEt3ResponseAcasAgreeReason());
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
        respondent.setEt3ResponseRespondentContestClaim(caseData.getEt3ResponseRespondentContestClaim());
        respondent.setEt3ResponseContestClaimDocument(caseData.getEt3ResponseContestClaimDocument());
        respondent.setEt3ResponseContestClaimDetails(caseData.getEt3ResponseContestClaimDetails());
        respondent.setEt3ResponseEmployerClaim(caseData.getEt3ResponseEmployerClaim());
        respondent.setEt3ResponseEmployerClaimDetails(caseData.getEt3ResponseEmployerClaimDetails());
        respondent.setEt3ResponseEmployerClaimDocument(caseData.getEt3ResponseEmployerClaimDocument());
        respondent.setEt3ResponseRespondentSupportNeeded(caseData.getEt3ResponseRespondentSupportNeeded());
        respondent.setEt3ResponseRespondentSupportDetails(caseData.getEt3ResponseRespondentSupportDetails());
        respondent.setEt3ResponseRespondentSupportDocument(caseData.getEt3ResponseRespondentSupportDocument());

        return respondent;
    }

    /**
     * Reset the fields in the ET3 Response form event.
     * @param caseData data for the case
     */
    public static void resetEt3FormFields(CaseData caseData) {
        caseData.setEt3RepresentingRespondent(null);
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

}
