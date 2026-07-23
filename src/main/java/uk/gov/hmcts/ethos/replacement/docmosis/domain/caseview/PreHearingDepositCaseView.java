package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.PreHearingDepositCaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.prehearingdeposit.PreHearingDepositData;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.PRE_HEARING_DEPOSIT_CASE_TYPE_ID;

@Component
public class PreHearingDepositCaseView implements CaseView<PreHearingDepositData, PreHearingDepositCaseState> {

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(PRE_HEARING_DEPOSIT_CASE_TYPE_ID);
    }

    @Override
    public PreHearingDepositData getCase(
        CaseViewRequest<PreHearingDepositCaseState> request,
        PreHearingDepositData blobCase
    ) {
        return blobCase;
    }
}
