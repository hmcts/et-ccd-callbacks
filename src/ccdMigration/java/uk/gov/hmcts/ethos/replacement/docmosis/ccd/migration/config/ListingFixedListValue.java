package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

enum ListingFixedListValue implements HasLabel {
    SINGLE("Single"),
    RANGE("Range"),
    PUBLIC("Public"),
    STAFF("Staff"),
    PRESS_LIST("Press List"),
    IT56("IT56 - List of Exhibits"),
    ETCL("ETCL - Cause List"),
    ETRP("ETRP - ET recording of proceeding"),
    BRISTOL("Bristol"),
    LEEDS("Leeds"),
    LONDON_CENTRAL("London Central"),
    LONDON_EAST("London East"),
    LONDON_SOUTH("London South"),
    MANCHESTER("Manchester"),
    MIDLANDS_EAST("Midlands East"),
    MIDLANDS_WEST("Midlands West"),
    NEWCASTLE("Newcastle"),
    WALES("Wales"),
    WATFORD("Watford"),
    UNASSIGNED("Unassigned"),
    GLASGOW("Glasgow"),
    ABERDEEN("Aberdeen"),
    DUNDEE("Dundee"),
    EDINBURGH("Edinburgh"),
    ALL("All"),
    BROUGHT_FORWARD_REPORT("Brought Forward Report"),
    MEMBER_DAYS("Member Days"),
    CASES_COMPLETED("Cases Completed"),
    CLAIMS_BY_HEARING_VENUE("Claims By Hearing Venue"),
    HEARINGS_BY_HEARING_TYPE("Hearings By Hearing Type"),
    TIME_TO_FIRST_HEARING("Time To First Hearing"),
    HEARINGS_TO_JUDGMENTS("Hearings To Judgments"),
    LIVE_CASELOAD("Live Caseload"),
    NO_CHANGE_IN_CURRENT_POSITION("No Change In Current Position"),
    CLAIMS_ACCEPTED("Claims Accepted"),
    SERVING_CLAIMS("Serving Claims"),
    CASES_AWAITING_JUDGMENT("Cases Awaiting Judgment"),
    CASE_SOURCE("Case Source"),
    RESPONDENTS("Respondents"),
    SESSION_DAYS("Session Days"),
    ECC("ECC");

    @JsonValue
    private final String value;

    ListingFixedListValue(String value) {
        this.value = value;
    }

    @Override
    public String getLabel() {
        return value;
    }
}
