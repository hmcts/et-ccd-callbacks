package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CaseView;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.service.caseview.JudgeOverviewRenderer;

import java.util.Set;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Component
@RequiredArgsConstructor
public class ETCaseView implements CaseView<CaseData, CaseState> {

    private final JudgeOverviewRenderer judgeOverviewRenderer;

    @Override
    public Set<String> caseTypeIds() {
        return Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID);
    }

    @Override
    public CaseData getCase(CaseViewRequest<CaseState> request, CaseData blobCase) {
        if (request.state() != CaseState.AWAITING_SUBMISSION_TO_HMCTS) {
            blobCase.setJudgeOverviewMarkdown(
                judgeOverviewRenderer.render(blobCase, request.caseRef(), request.state())
            );
        }
        return blobCase;
    }
}
