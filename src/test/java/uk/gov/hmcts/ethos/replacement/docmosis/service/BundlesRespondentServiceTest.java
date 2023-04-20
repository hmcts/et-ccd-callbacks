package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class BundlesRespondentServiceTest {

    private BundlesRespondentService bundlesRespondentService;
    private CaseData scotlandCaseData;
    private CaseData englandCaseData;

    @BeforeEach
    void setUp() {
        bundlesRespondentService = new BundlesRespondentService();
        englandCaseData = CaseDataBuilder.builder()
                .withHearing("1", "Hearing", "Judge", "Bodmin", List.of("In person"), "60", "Days", "Sit Alone")
                .withHearingSession(0, "1", "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(0, "2", "2022-05-16T01:00:00.000", "Listed", false)
                .withHearing("2", "Costs Hearing", "Judge", "ROIT", List.of("Video"), "60", "Days", "Sit Alone")
                .withHearingSession(1, "1", "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(1, "2", "2070-05-16T01:00:00.000", "Listed", false)
                .build();

        scotlandCaseData = CaseDataBuilder.builder()
                .withHearingScotland("1", "Hearing", "Judge", TribunalOffice.EDINBURGH, "Venue")
                .withHearingSession(0, "1", "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(0, "2", "2022-05-16T01:00:00.000", "Listed", false)
                .withHearingScotland("2", "Costs Hearing", "Judge", TribunalOffice.ABERDEEN, "Venue")
                .withHearingSession(1, "1", "2069-05-16T01:00:00.000", "Listed", false)
                .withHearingSession(1, "2", "2070-05-16T01:00:00.000", "Listed", false)
                .build();
    }

    @Test
    void clearInputData() {
        englandCaseData.setBundlesRespondentPrepareDocNotesShow(YES);
        englandCaseData.setBundlesRespondentAgreedDocWith(NO);
        englandCaseData.setBundlesRespondentAgreedDocWithBut("Some input");
        englandCaseData.setBundlesRespondentAgreedDocWithNo("Some input");

        bundlesRespondentService.clearInputData(englandCaseData);

        assertNull(englandCaseData.getBundlesRespondentPrepareDocNotesShow());
        assertNull(englandCaseData.getBundlesRespondentAgreedDocWith());
        assertNull(englandCaseData.getBundlesRespondentAgreedDocWithBut());
        assertNull(englandCaseData.getBundlesRespondentAgreedDocWithNo());
    }

    @Test
    void populateSelectHearings_englandWales_twoOptionsWithCorrectDates() {
        bundlesRespondentService.populateSelectHearings(englandCaseData);
        var actual = englandCaseData.getBundlesRespondentSelectHearing().getListItems();

        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getLabel(), is("1 Hearing - Bodmin - 16 May 2069"));
        assertThat(actual.get(1).getLabel(), is("2 Costs Hearing - ROIT - 16 May 2069"));
    }

    @Test
    void populateSelectHearings_scotland_twoOptionsWithCorrectDates() {
        bundlesRespondentService.populateSelectHearings(scotlandCaseData);
        var actual = scotlandCaseData.getBundlesRespondentSelectHearing().getListItems();

        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getLabel(), is("1 Hearing - Edinburgh - 16 May 2069"));
        assertThat(actual.get(1).getLabel(), is("2 Costs Hearing - Aberdeen - 16 May 2069"));
    }
}
