package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.hmcts.ccd.sdk.api.HasLabel;

enum AdminFixedListValue implements HasLabel {
    BRISTOL("Bristol", "Bristol"),
    LEEDS("Leeds", "Leeds"),
    LONDON_CENTRAL("London Central", "London Central"),
    LONDON_EAST("London East", "London East"),
    LONDON_SOUTH("London South", "London South"),
    MANCHESTER("Manchester", "Manchester"),
    MIDLANDS_EAST("Midlands East", "Midlands East"),
    MIDLANDS_WEST("Midlands West", "Midlands West"),
    NEWCASTLE("Newcastle", "Newcastle"),
    SCOTLAND("Scotland", "Scotland"),
    WALES("Wales", "Wales"),
    WATFORD("Watford", "Watford"),
    FEE_PAID("FEE_PAID", "Fee Paid"),
    SALARIED("SALARIED", "Salaried"),
    UNKNOWN("UNKNOWN", "Unknown"),
    CLERK("CLERK", "Clerk"),
    EMPLOYEE_MEMBER("EMPLOYEE_MEMBER", "Employee Member"),
    EMPLOYER_MEMBER("EMPLOYER_MEMBER", "Employer Member");

    @JsonValue
    private final String code;
    private final String label;

    AdminFixedListValue(String code, String label) {
        this.code = code;
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }
}
