package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.helper.Constants;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.HEARING_TYPE_JUDICIAL_HEARING;

class CreateReferralHelperTest {

    private CreateReferralHelper createReferralHelper;
    private CaseData caseData;

    private static final String HEARING_DETAILS = "<hr>To help you complete this form, open the "
        + "<a href=\"url\">referral guidance documents</a>"
        + "<hr><h3>Hearing details</h3>"
        + "<pre>Date &nbsp;&#09&#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Hearing &#09&#09&#09&#09&nbsp; %s"
        + "<br><br>Type &nbsp;&nbsp;&#09&#09&#09&#09&#09 %s</pre><hr>";

    @BeforeEach
    void setUp() {
        createReferralHelper = new CreateReferralHelper();
        caseData = CaseDataBuilder.builder()
            .withHearingScotland("hearingNumber", HEARING_TYPE_JUDICIAL_HEARING, "Judge",
                TribunalOffice.ABERDEEN, "venue")
            .withHearingSession(
                0,
                "hearingNumber",
                "2019-11-25T12:11:00.000",
                Constants.HEARING_STATUS_HEARD,
                true)
            .build();
    }

    @Test
    void populateHearingDetails() {
        createReferralHelper.populateHearingDetails(caseData);
        HearingType hearing = caseData.getHearingCollection().get(0).getValue();
        String hearingDate = UtilHelper.formatLocalDateTime(hearing.getHearingDateCollection().get(0).getValue()
            .getListedDate());
        assertThat(caseData.getReferralHearingDetails())
            .isEqualTo(String.format(HEARING_DETAILS, hearingDate, hearing.getHearingType(), caseData.getTrackType()));
    }
}