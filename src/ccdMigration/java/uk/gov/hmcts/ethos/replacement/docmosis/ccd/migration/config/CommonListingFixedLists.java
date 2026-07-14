package uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CCDConfig;
import uk.gov.hmcts.ccd.sdk.api.ConfigBuilder;
import uk.gov.hmcts.et.common.model.ccd.ListingRole;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.ListingCaseState;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.BROUGHT_FORWARD_REPORT;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.CASES_AWAITING_JUDGMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.CASES_COMPLETED;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.CASE_SOURCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.CLAIMS_ACCEPTED;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.CLAIMS_BY_HEARING_VENUE;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.ECC;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.ETCL;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.ETRP;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.HEARINGS_BY_HEARING_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.HEARINGS_TO_JUDGMENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.IT56;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.LIVE_CASELOAD;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.MEMBER_DAYS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.NO_CHANGE_IN_CURRENT_POSITION;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.PRESS_LIST;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.PUBLIC;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.RANGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.RESPONDENTS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.SERVING_CLAIMS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.SESSION_DAYS;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.SINGLE;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.STAFF;
import static uk.gov.hmcts.ethos.replacement.docmosis.ccd.migration.config.ListingFixedListValue.TIME_TO_FIRST_HEARING;

@Component
public class CommonListingFixedLists implements
    CCDConfig<ListingData, ListingCaseState, ListingRole> {

    @Override
    public List<String> groupingKeys() {
        return List.of(
            EnglandWalesListingDefinition.CASE_TYPE,
            ScotlandListingDefinition.CASE_TYPE
        );
    }

    @Override
    public void configure(ConfigBuilder<ListingData, ListingCaseState, ListingRole> builder) {
        builder.registerFixedList("fl_HearingDateType", SINGLE, RANGE);
        builder.registerFixedList("fl_HearingDocETCL", PUBLIC, STAFF, PRESS_LIST);
        builder.registerFixedList("fl_HearingDocType", IT56, ETCL, ETRP);
        builder.registerFixedList("fl_reportCaseType", BROUGHT_FORWARD_REPORT, MEMBER_DAYS,
            CASES_COMPLETED, CLAIMS_BY_HEARING_VENUE, HEARINGS_BY_HEARING_TYPE,
            TIME_TO_FIRST_HEARING, HEARINGS_TO_JUDGMENTS, LIVE_CASELOAD,
            NO_CHANGE_IN_CURRENT_POSITION, CLAIMS_ACCEPTED, SERVING_CLAIMS,
            CASES_AWAITING_JUDGMENT, CASE_SOURCE, RESPONDENTS, SESSION_DAYS, ECC);
    }
}
