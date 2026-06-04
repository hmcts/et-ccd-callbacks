package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.MultipleCaseState;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

@Component
public class MultipleCaseView implements CaseView<MultipleData, MultipleCaseState> {

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_BULK_CASE_TYPE_ID, SCOTLAND_BULK_CASE_TYPE_ID);
    }

    @Override
    public MultipleData getCase(CaseViewRequest<MultipleCaseState> request, MultipleData blobCase) {
        return blobCase;
    }
}
