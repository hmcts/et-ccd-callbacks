package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_HEARD;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_STATUS_POSTPONED;

class CreateReferralHelperTest {

    private CreateReferralHelper createReferralHelper;
    private CaseData caseData;

    private final String expectedSingleHearingDetails = "<hr>To help you complete this form, open the <a href=\"url\">"
        + "referral guidance documents</a><hr><h3>Hearing details </h3><pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; 25 "
        + "December 2021 00:00<br><br>Hearing &#09&#09&#09&#09&nbsp; test<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09"
        + " null</pre><hr>";

    private final String expectedMultipleHearingDetails = "<hr>To help you complete this form, open the <a href="
        + "\"url\">referral guidance documents</a><hr><h3>Hearing details 1</h3><pre>Date &nbsp;&#09&#09&#09&#09&#0"
        + "9&nbsp; 25 December 2021 00:00<br><br>Hearing &#09&#09&#09&#09&nbsp; test<br><br>Type &nbsp;&nbsp;&#09&#0"
        + "9&#09&#09&#09 null</pre><hr><h3>Hearing details 2</h3><pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; 26 December"
        + " 2021 00:00<br><br>Hearing &#09&#09&#09&#09&nbsp; test<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09 null<"
        + "/pre><hr>";

    @BeforeEach
    void setUp() {
        createReferralHelper = new CreateReferralHelper();
        caseData = CaseDataBuilder.builder()
            .withHearing("1", "test", "Judy", null)
            .withHearingSession(0, "1", "2021-12-25T00:00:00.000",
                HEARING_STATUS_POSTPONED, false)
            .build();
    }

    @Test
    void populateSingleHearingDetails() {
        createReferralHelper.populateHearingDetails(caseData);
        assertThat(caseData.getReferralHearingDetails())
            .isEqualTo(expectedSingleHearingDetails);
    }

    @Test
    void populateMultipleHearingDetails() {
        caseData = CaseDataBuilder.builder()
            .withHearing("1", "test", "Judy")
            .withHearingSession(0, "1", "2021-12-25T00:00:00.000",
                HEARING_STATUS_POSTPONED, false)
            .withHearing("2", "test", "Judy")
            .withHearingSession(1, "1", "2021-12-26T00:00:00.000",
                HEARING_STATUS_HEARD, false)
            .build();

        createReferralHelper.populateHearingDetails(caseData);
        assertThat(caseData.getReferralHearingDetails())
            .isEqualTo(expectedMultipleHearingDetails);
    }
}