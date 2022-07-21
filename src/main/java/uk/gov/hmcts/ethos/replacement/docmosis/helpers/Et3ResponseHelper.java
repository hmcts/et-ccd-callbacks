package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.FILE_EXTENSION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NEW_LINE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OUTPUT_FILE_NAME;

/**
 * ET3 Response Helper provides methods to assist with the ET3 Response Form event.
 */
@Slf4j
public class Et3ResponseHelper {
    private static final String CLAIMANT_NAME_TABLE = "<pre> ET1 claimant name&#09&#09&#09&#09 %s</pre><hr>";
    public static final String START_DATE_MUST_BE_IN_THE_PAST = "Start date must be in the past";
    public static final String END_DATE_MUST_BE_AFTER_THE_START_DATE = "End date must be after the start date";

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

    /**
     * This method converts the casedata into a string builder for the ET3 Form.
     * @param caseData contains the data for the ET3 Form
     * @param accessKey tornado access key
     * @param documentName name of the document for tornado purposes
     * @return a string builder which contains the data for Docmosis to parse
     */
    public static StringBuilder buildEt3FormDocument(CaseData caseData, String accessKey, String documentName) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("\"accessKey\":\"").append(accessKey).append(NEW_LINE);
        sb.append("\"templateName\":\"").append(documentName).append(FILE_EXTENSION).append(NEW_LINE);
        sb.append("\"outputName\":\"").append(OUTPUT_FILE_NAME).append(NEW_LINE);
        sb.append("\"data\":{\n");

        sb.append("\"et3ResponseIsClaimantNameCorrect\":\"")
            .append(caseData.getEt3ResponseIsClaimantNameCorrect()).append(NEW_LINE);
        sb.append("\"et3ResponseClaimantNameCorrection\":\"")
            .append(caseData.getEt3ResponseClaimantNameCorrection()).append(NEW_LINE);
        sb.append("\"et3ResponseRespondentLegalName\":\"")
            .append(caseData.getEt3ResponseRespondentLegalName()).append(NEW_LINE);
        sb.append("\"et3ResponseRespondentCompanyNumber\":\"")
            .append(caseData.getEt3ResponseRespondentCompanyNumber()).append(NEW_LINE);
        sb.append("\"et3ResponseRespondentEmployerType\":\"")
            .append(caseData.getEt3ResponseRespondentEmployerType()).append(NEW_LINE);
        sb.append("\"et3ResponseRespondentPreferredTitle\":\"")
            .append(caseData.getEt3ResponseRespondentPreferredTitle()).append(NEW_LINE);
        sb.append("\"et3ResponseRespondentContactName\":\"")
            .append(caseData.getEt3ResponseRespondentContactName()).append(NEW_LINE);
        sb.append("\"et3RespondentAddress\":\"").append(caseData.getEt3RespondentAddress()).append(NEW_LINE);
        sb.append("\"et3ResponseDXAddress\":\"").append(caseData.getEt3ResponseDXAddress()).append(NEW_LINE);
        sb.append("\"et3ResponsePhone\":\"").append(caseData.getEt3ResponsePhone()).append(NEW_LINE);
        sb.append("\"et3ResponseContactPreference\":\"")
            .append(caseData.getEt3ResponseContactPreference()).append(NEW_LINE);
        sb.append("\"et3ResponseContactReason\":\"").append(caseData.getEt3ResponseContactReason()).append(NEW_LINE);
        sb.append("\"et3ResponseHearingRepresentative\":\"")
            .append(caseData.getEt3ResponseHearingRepresentative()).append(NEW_LINE);
        sb.append("\"et3ResponseHearingRespondent\":\"")
            .append(caseData.getEt3ResponseHearingRespondent()).append(NEW_LINE);
        sb.append("\"et3ResponseEmploymentCount\":\"")
            .append(caseData.getEt3ResponseEmploymentCount()).append(NEW_LINE);
        sb.append("\"et3ResponseMultipleSites\":\"").append(caseData.getEt3ResponseMultipleSites()).append(NEW_LINE);
        sb.append("\"et3ResponseSiteEmploymentCount\":\"")
            .append(caseData.getEt3ResponseSiteEmploymentCount()).append(NEW_LINE);
        sb.append("\"et3ResponseAcasAgree\":\"").append(caseData.getEt3ResponseAcasAgree()).append(NEW_LINE);
        sb.append("\"et3ResponseAcasAgreeReason\":\"")
            .append(caseData.getEt3ResponseAcasAgreeReason()).append(NEW_LINE);
        sb.append("\"et3ResponseAreDatesCorrect\":\"")
            .append(caseData.getEt3ResponseAreDatesCorrect()).append(NEW_LINE);
        sb.append("\"et3ResponseEmploymentStartDate\":\"")
            .append(caseData.getEt3ResponseEmploymentStartDate()).append(NEW_LINE);
        sb.append("\"et3ResponseEmploymentEndDate\":\"")
            .append(caseData.getEt3ResponseEmploymentEndDate()).append(NEW_LINE);
        sb.append("\"et3ResponseEmploymentInformation\":\"")
            .append(caseData.getEt3ResponseEmploymentInformation()).append(NEW_LINE);
        sb.append("\"et3ResponseContinuingEmployment\":\"")
            .append(caseData.getEt3ResponseContinuingEmployment()).append(NEW_LINE);
        sb.append("\"et3ResponseIsJobTitleCorrect\":\"")
            .append(caseData.getEt3ResponseIsJobTitleCorrect()).append(NEW_LINE);
        sb.append("\"et3ResponseCorrectJobTitle\":\"")
            .append(caseData.getEt3ResponseCorrectJobTitle()).append(NEW_LINE);
        sb.append("\"et3ResponseClaimantWeeklyHours\":\"")
            .append(caseData.getEt3ResponseClaimantWeeklyHours()).append(NEW_LINE);
        sb.append("\"et3ResponseClaimantCorrectHours\":\"")
            .append(caseData.getEt3ResponseClaimantCorrectHours()).append(NEW_LINE);
        sb.append("\"et3ResponseEarningDetailsCorrect\":\"")
            .append(caseData.getEt3ResponseEarningDetailsCorrect()).append(NEW_LINE);
        sb.append("\"et3ResponsePayFrequency\":\"")
            .append(caseData.getEt3ResponsePayFrequency()).append(NEW_LINE);
        sb.append("\"et3ResponsePayBeforeTax\":\"").append(caseData.getEt3ResponsePayBeforeTax()).append(NEW_LINE);
        sb.append("\"et3ResponsePayTakehome\":\"").append(caseData.getEt3ResponsePayTakehome()).append(NEW_LINE);
        sb.append("\"et3ResponseIsNoticeCorrect\":\"")
            .append(caseData.getEt3ResponseIsNoticeCorrect()).append(NEW_LINE);
        sb.append("\"et3ResponseCorrectNoticeDetails\":\"")
            .append(caseData.getEt3ResponseCorrectNoticeDetails()).append(NEW_LINE);
        sb.append("\"et3ResponseIsPensionCorrect\":\"")
            .append(caseData.getEt3ResponseIsPensionCorrect()).append(NEW_LINE);
        sb.append("\"et3ResponsePensionCorrectDetails\":\"")
            .append(caseData.getEt3ResponsePensionCorrectDetails()).append(NEW_LINE);
        sb.append("\"et3ResponseRespondentContestClaim\":\"")
            .append(caseData.getEt3ResponseRespondentContestClaim()).append(NEW_LINE);
        sb.append("\"et3ResponseContestClaimDetails\":\"")
            .append(caseData.getEt3ResponseContestClaimDetails()).append(NEW_LINE);
        sb.append("\"et3ResponseEmployerClaim\":\"").append(caseData.getEt3ResponseEmployerClaim()).append(NEW_LINE);
        sb.append("\"et3ResponseEmployerClaimDetails\":\"")
            .append(caseData.getEt3ResponseEmployerClaimDetails()).append(NEW_LINE);
        sb.append("\"et3ResponseRespondentSupportNeeded\":\"")
            .append(caseData.getEt3ResponseRespondentSupportNeeded()).append(NEW_LINE);
        sb.append("\"et3ResponseRespondentSupportDetails\":\"")
            .append(caseData.getEt3ResponseRespondentSupportDetails()).append("\"\n");
        sb.append("}\n");
        sb.append("}\n");

        return sb;
    }
}
