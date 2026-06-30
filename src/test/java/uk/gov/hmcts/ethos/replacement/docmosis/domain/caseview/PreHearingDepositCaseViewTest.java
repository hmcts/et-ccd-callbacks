package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.PRE_HEARING_DEPOSIT_CASE_TYPE_ID;

class PreHearingDepositCaseViewTest {

    @Test
    void returnsConfiguredCaseTypeAndCase() {
        PreHearingDepositCaseView caseView = new PreHearingDepositCaseView();
        PreHearingDepositData preHearingDepositData = new PreHearingDepositData();

        assertEquals(Set.of(PRE_HEARING_DEPOSIT_CASE_TYPE_ID), caseView.caseTypeIds());
        assertSame(preHearingDepositData, caseView.getCase(null, preHearingDepositData));
    }
}
