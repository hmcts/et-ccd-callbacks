package uk.gov.hmcts.ethos.replacement.docmosis.reports;

public final class Constants {
    public static final String NO_CHANGE_IN_CURRENT_POSITION_REPORT = "No Change In Current Position";
    public static final String HEARINGS_TO_JUDGEMENTS_REPORT = "Hearings To Judgments";
    public static final String CASES_AWAITING_JUDGMENT_REPORT = "Cases Awaiting Judgment";

    public static final String REPORT_OFFICE = "\"Report_Office\":\"";
    public static final String TOTAL_CASES = "\"Total_Cases\":\"";
    public static final String REPORT_DATE = "\"Report_Date\":\"";
    public static final String TOTAL_SINGLE = "\"Total_Single\":\"";
    public static final String TOTAL_MULTIPLE = "\"Total_Multiple\":\"";
    public static final String TOTAL_WITHIN_4WEEKS = "\"Total_Within_4Weeks\":\"";
    public static final String TOTAL_PERCENT_WITHIN_4WEEKS = "\"Total_Percent_Within_4Weeks\":\"";
    public static final String TOTAL_NOT_WITHIN_4WEEKS = "\"Total_Not_Within_4Weeks\":\"";
    public static final String TOTAL_PERCENT_NOT_WITHIN_4WEEKS = "\"Total_Percent_Not_Within_4Weeks\":\"";
    public static final String REPORT_DETAILS = "reportDetails";
    public static final String REPORT_DETAILS_SINGLE = "reportDetailsSingle";
    public static final String REPORT_DETAILS_MULTIPLE = "reportDetailsMultiple";

    public static final String ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD = "data.managingOffice.keyword";
    public static final String ELASTICSEARCH_FIELD_STATE_KEYWORD = "state.keyword";
    public static final String ELASTICSEARCH_FIELD_HEARING_COLLECTION = "data.hearingCollection";
    public static final String ELASTICSEARCH_FIELD_JUDGMENT_COLLECTION = "data.judgementCollection";
    public static final String ELASTICSEARCH_FIELD_HEARING_LISTED_DATE =
            "data.hearingCollection.value.hearingDateCollection.value.listedDate";

    private Constants() {
    }
}
