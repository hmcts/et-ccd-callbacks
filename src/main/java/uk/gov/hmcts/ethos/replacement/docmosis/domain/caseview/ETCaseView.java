package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
public class ETCaseView implements CaseView<CaseData, State> {

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public CaseData getCase(CaseViewRequest<State> request, CaseData blobCase) {
        return blobCase;
    }
}
