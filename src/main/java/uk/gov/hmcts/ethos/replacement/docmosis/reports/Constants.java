package uk.gov.hmcts.ethos.replacement.docmosis.reports;

import java.time.DayOfWeek;
import java.util.EnumSet;
import java.util.Set;

public final class Constants {
    public static final String NO_CHANGE_IN_CURRENT_POSITION_REPORT = "No Change In Current Position";
    public static final String HEARINGS_TO_JUDGEMENTS_REPORT = "Hearings To Judgments";
    public static final String CASES_AWAITING_JUDGMENT_REPORT = "Cases Awaiting Judgment";
    public static final String RESPONDENTS_REPORT = "Respondents";
    public static final String ECC_REPORT = "ECC";

    public static final String REPORT_OFFICE = "\"Report_Office\":\"";
    public static final String TOTAL_CASES = "\"Total_Cases\":\"";
    public static final String REPORT_DETAILS = "reportDetails";

    public static final String ELASTICSEARCH_FIELD_MANAGING_OFFICE_KEYWORD = "data.managingOffice.keyword";
    public static final String ELASTICSEARCH_FIELD_STATE_KEYWORD = "state.keyword";
    public static final String ELASTICSEARCH_FIELD_HEARING_COLLECTION = "data.hearingCollection";
    public static final String ELASTICSEARCH_FIELD_JUDGMENT_COLLECTION = "data.judgementCollection";
    public static final String ELASTICSEARCH_FIELD_HEARING_LISTED_DATE =
            "data.hearingCollection.value.hearingDateCollection.value.listedDate";

    public static final Set<DayOfWeek> WEEKEND_DAYS_LIST = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

    private Constants() {
    }
}
