package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.Et3ResponseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.documents.Et3ResponseDocument;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * ET3 Response Helper provides methods to assist with the ET3 Response Form event.
 */
@Slf4j
@SuppressWarnings({"PMD.ClassWithOnlyPrivateConstructorsShouldBeFinal", "PMD.LinguisticNaming",
    "PMD.ExcessiveMethodLength", "PMD.ClassNamingConventions", "PMD.PrematureDeclaration"})
public class Et3ResponseHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TEMPLATE_NAME = "ET3_0922 (1).docx";
    private static final String OUTPUT_NAME = "ET3 Response.pdf";
    private static final String CLAIMANT_NAME_TABLE = "<pre> ET1 claimant name&#09&#09&#09&#09 %s</pre><hr>";
    public static final String START_DATE_MUST_BE_IN_THE_PAST = "Start date must be in the past";
    public static final String END_DATE_MUST_BE_AFTER_THE_START_DATE = "End date must be after the start date";
    public static final String CHECKED = "■";
    public static final String UNCHECKED = "□";

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
            .hearingPhone(caseData.getEt3ResponseHearingRespondent().contains("Phone hearings") ? "■" : "□")
            .hearingVideo(caseData.getEt3ResponseHearingRespondent().contains("Video hearings") ? "■" : "□")
            .repHearingPhone(caseData.getEt3ResponseHearingRepresentative().contains("Phone hearings") ? "■" : "□")
            .repHearingVideo(caseData.getEt3ResponseHearingRepresentative().contains("Video hearings") ? "■" : "□")
            .build();

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

        data.setRepContactEmail("□");
        data.setRepContactPost("□");
        data.setBeforeWeekly("□");
        data.setTakehomeWeekly("□");
        data.setBeforeMonthly("□");
        data.setTakehomeMonthly("□");
        data.setBeforeAnnually("□");
        data.setTakehomeAnnually("□");

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

        Et3ResponseDocument et3ResponseDocument = Et3ResponseDocument.builder()
            .accessKey(accessKey)
            .outputName(OUTPUT_NAME)
            .templateName(TEMPLATE_NAME)
            .et3ResponseData(data)
            .build();

        return OBJECT_MAPPER.writeValueAsString(et3ResponseDocument);
    }
}
