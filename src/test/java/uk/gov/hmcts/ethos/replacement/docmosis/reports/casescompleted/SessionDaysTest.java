package uk.gov.hmcts.ethos.replacement.docmosis.reports.casescompleted;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.listing.ListingData;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_WITHDRAWN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_COSTS_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_MEDIATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_MEDIATION_TCC;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_RECONSIDERATION;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_REMEDY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_PERLIMINARY_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_HEARING_DATE_TYPE;

@ExtendWith(SpringExtension.class)
class SessionDaysTest {

    @Test
    void shouldGetSessionDaysForSingleHearingSingleSession() {
        CaseDataBuilder caseDataBuilder = new CaseDataBuilder();
        CaseData caseData = caseDataBuilder
                .withHearing("1", HEARING_TYPE_JUDICIAL_HEARING, "Judge Dave", null, null, null, null)
                .withHearingSession(0, "1", "2021-07-02T10:00:00", HEARING_STATUS_HEARD, true)
                .build();
        ListingData listingData = createListingData("2021-07-02");

        SessionDays sessionDays = new SessionDays(listingData, caseData);

        verifySessionDays(sessionDays, HEARING_TYPE_JUDICIAL_HEARING, 1, "2021-07-02T10:00:00");
    }

    @Test
    void shouldGetSessionDaysForSingleHearingMultipleSession() {
        CaseDataBuilder caseDataBuilder = new CaseDataBuilder();
        CaseData caseData = caseDataBuilder
                .withHearing("1", HEARING_TYPE_JUDICIAL_HEARING, "Judge Dave", null, null, null, null)
                .withHearingSession(0, "1", "2021-07-01T09:00:00", HEARING_STATUS_HEARD, false)
                .withHearingSession(0, "1", "2021-07-02T10:00:00", HEARING_STATUS_HEARD, true)
                .build();
        ListingData listingData = createListingData("2021-07-02");

        SessionDays sessionDays = new SessionDays(listingData, caseData);

        verifySessionDays(sessionDays, HEARING_TYPE_JUDICIAL_HEARING, 2, "2021-07-02T10:00:00");
    }

    @Test
    void shouldGetSessionDaysForMultipleHearings() {
        CaseDataBuilder caseDataBuilder = new CaseDataBuilder();
        CaseData caseData = caseDataBuilder
                .withHearing("1", HEARING_TYPE_JUDICIAL_HEARING, "Judge Dave", null, null, null, null)
                .withHearing("2", HEARING_TYPE_JUDICIAL_HEARING, "Judge Brenda", null, null, null, null)
                .withHearingSession(0, "1", "2021-07-01T09:00:00", HEARING_STATUS_POSTPONED, false)
                .withHearingSession(1, "1", "2021-07-03T09:00:00", HEARING_STATUS_HEARD, false)
                .withHearingSession(1, "2", "2021-07-04T10:00:00", HEARING_STATUS_HEARD, true)
                .build();
        ListingData listingData = createListingData("2021-07-04");

        SessionDays sessionDays = new SessionDays(listingData, caseData);

        verifySessionDays(sessionDays, HEARING_TYPE_JUDICIAL_HEARING, 2, "2021-07-04T10:00:00");
    }

    @Test
    void shouldGetSessionDaysForMultipleHearingsScenario2() {
        CaseDataBuilder caseDataBuilder = new CaseDataBuilder();
        CaseData caseData = caseDataBuilder
                .withHearing("1", HEARING_TYPE_PERLIMINARY_HEARING, "Judge Dave", null, null, null, null)
                .withHearing("2", HEARING_TYPE_JUDICIAL_HEARING, "Judge Brenda", null, null, null, null)
                .withHearingSession(0, "1", "2021-07-01T09:00:00", HEARING_STATUS_POSTPONED, false)
                .withHearingSession(0, "2", "2021-07-02T09:00:00", HEARING_STATUS_HEARD, true)
                .withHearingSession(1, "1", "2021-07-03T10:00:00", HEARING_STATUS_HEARD, true)
                .withHearingSession(1, "2", "2021-07-04T10:00:00", HEARING_STATUS_WITHDRAWN, false)
                .build();
        ListingData listingData = createListingData("2021-07-03");

        SessionDays sessionDays = new SessionDays(listingData, caseData);

        verifySessionDays(sessionDays, HEARING_TYPE_JUDICIAL_HEARING, 2, "2021-07-03T10:00:00");
    }

    @Test
    void shouldGetSessionDaysIgnoreInvalidHearingType() {
        CaseDataBuilder caseDataBuilder = new CaseDataBuilder();
        CaseData caseData = caseDataBuilder
                .withHearing("1", HEARING_TYPE_PERLIMINARY_HEARING, "Judge Dave", null, null, null, null)
                .withHearing("2", HEARING_TYPE_JUDICIAL_COSTS_HEARING, "Judge Brenda", null, null, null, null)
                .withHearingSession(0, "1", "2021-07-01T09:00:00", HEARING_STATUS_POSTPONED, false)
                .withHearingSession(0, "2", "2021-07-02T09:00:00", HEARING_STATUS_HEARD, true)
                .withHearingSession(1, "1", "2021-07-02T10:00:00", HEARING_STATUS_HEARD, true)
                .build();
        ListingData listingData = createListingData("2021-07-02");

        SessionDays sessionDays = new SessionDays(listingData, caseData);

        verifySessionDays(sessionDays, HEARING_TYPE_PERLIMINARY_HEARING, 1, "2021-07-02T09:00:00");
    }

    @Test
    void shouldGetSessionDaysForValidHearingType() {
        for (String hearingType : CasesCompletedReport.VALID_HEARING_TYPES) {
            CaseDataBuilder caseDataBuilder = new CaseDataBuilder();
            CaseData caseData = caseDataBuilder
                    .withHearing("1", hearingType, "Judge Dave", null, null, null, null)
                    .withHearingSession(0, "1", "2021-07-01T09:00:00", HEARING_STATUS_HEARD, false)
                    .withHearingSession(0, "1", "2021-07-02T10:00:00", HEARING_STATUS_HEARD, true)
                    .build();
            ListingData listingData = createListingData("2021-07-02");

            SessionDays sessionDays = new SessionDays(listingData, caseData);

            verifySessionDays(sessionDays, hearingType, 2, "2021-07-02T10:00:00");
        }
    }

    @Test
    void shouldGetNoSessionDaysIfCaseHasNoHearings() {
        SessionDays sessionDays = new SessionDays(new ListingData(), new CaseData());
        assertNull(sessionDays.getLatestDisposedHearingSession());
    }

    @Test
    void shouldGetNoSessionDaysIfCaseHasEmptyHearingsCollection() {
        CaseData caseData = new CaseData();
        caseData.setHearingCollection(new ArrayList<>());
        SessionDays sessionDays = new SessionDays(new ListingData(), caseData);
        assertNull(sessionDays.getLatestDisposedHearingSession());
    }

    @Test
    void shouldGetNoSessionDaysIfCaseHasNoValidHearingType() {
        List<String> invalidHearingTypes = List.of(
                HEARING_TYPE_JUDICIAL_COSTS_HEARING,
                HEARING_TYPE_JUDICIAL_MEDIATION,
                HEARING_TYPE_JUDICIAL_MEDIATION_TCC,
                HEARING_TYPE_JUDICIAL_RECONSIDERATION,
                HEARING_TYPE_JUDICIAL_REMEDY);

        for (String invalidHearingType : invalidHearingTypes) {
            CaseDataBuilder caseDataBuilder = new CaseDataBuilder();
            CaseData caseData = caseDataBuilder
                    .withHearing("1", invalidHearingType, "Judge Doris", null, null, null, null)
                    .withHearingSession(0, "1", "2021-07-02T10:00:00", HEARING_STATUS_HEARD, true)
                    .build();

            ListingData listingData = new ListingData();
            listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
            listingData.setListingDate("2021-07-02");
            SessionDays sessionDays = new SessionDays(listingData, caseData);
            assertNull(sessionDays.getLatestDisposedHearingSession());
        }
    }

    @Test
    void shouldGetNoSessionDaysIfNotEqualsListedDate() {
        CaseDataBuilder caseDataBuilder = new CaseDataBuilder();
        CaseData caseData = caseDataBuilder
                .withHearing("1", HEARING_TYPE_JUDICIAL_HEARING, "Judge Dave", null, null, null, null)
                .withHearingSession(0, "1", "2021-07-01T10:00:00", HEARING_STATUS_HEARD, true)
                .build();

        ListingData listingData = new ListingData();
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setListingDate("2021-07-02");
        SessionDays sessionDays = new SessionDays(listingData, caseData);
        assertNull(sessionDays.getLatestDisposedHearingSession());
    }

    @Test
    void shouldGetNoSessionDaysIfHearingNotDisposed() {
        CaseDataBuilder caseDataBuilder = new CaseDataBuilder();
        CaseData caseData = caseDataBuilder
                .withHearing("1", HEARING_TYPE_JUDICIAL_HEARING, "Judge Dave", null, null, null, null)
                .withHearingSession(0, "1", "2021-07-01T10:00:00", HEARING_STATUS_HEARD, false)
                .build();

        ListingData listingData = new ListingData();
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setListingDate("2021-07-01");
        SessionDays sessionDays = new SessionDays(listingData, caseData);
        assertNull(sessionDays.getLatestDisposedHearingSession());
    }

    private ListingData createListingData(String listingDate) {
        ListingData listingData = new ListingData();
        listingData.setHearingDateType(SINGLE_HEARING_DATE_TYPE);
        listingData.setListingDate(listingDate);
        return listingData;
    }

    private void verifySessionDays(SessionDays sessionDays, String expectedHearingType, int expectedSessionDaysCount,
                                   String expectedListedDate) {
        HearingSession latestDisposedHearingSession = sessionDays.getLatestDisposedHearingSession();
        assertEquals(expectedHearingType, latestDisposedHearingSession.getHearingType().getHearingType());
        assertEquals(expectedSessionDaysCount, latestDisposedHearingSession.getSessionDays());
        assertEquals(expectedListedDate, latestDisposedHearingSession.getDateListedType().getListedDate());
    }

}
