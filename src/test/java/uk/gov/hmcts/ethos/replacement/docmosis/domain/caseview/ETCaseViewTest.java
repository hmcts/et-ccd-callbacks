package uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.sdk.CaseViewRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;
import uk.gov.hmcts.ethos.replacement.docmosis.service.caseview.JudgeOverviewModelFactory;
import uk.gov.hmcts.ethos.replacement.docmosis.service.caseview.JudgeOverviewRenderer;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

class ETCaseViewTest {

    @Test
    void returnsConfiguredCaseTypesAndCase() {
        ETCaseView caseView = new ETCaseView(
            new JudgeOverviewRenderer(new JudgeOverviewModelFactory()),
            caseReference -> List.of()
        );
        CaseData caseData = new CaseData();
        caseData.setClaimant("Alex Example");
        caseData.setRespondent("Example Ltd");

        assertEquals(Set.of(ENGLANDWALES_CASE_TYPE_ID, SCOTLAND_CASE_TYPE_ID), caseView.caseTypeIds());
        assertSame(
            caseData,
            caseView.getCase(new CaseViewRequest<>(1_234_567_890_123_456L, CaseState.Accepted), caseData)
        );
        assertTrue(caseData.getJudgeOverviewMarkdown().contains("Alex Example v Example Ltd"));
    }

    @Test
    void skipsOverviewForDraftCases() {
        ETCaseView caseView = new ETCaseView(
            new JudgeOverviewRenderer(new JudgeOverviewModelFactory()),
            caseReference -> List.of()
        );
        CaseData caseData = new CaseData();

        caseView.getCase(
            new CaseViewRequest<>(1_234_567_890_123_456L, CaseState.AWAITING_SUBMISSION_TO_HMCTS),
            caseData
        );

        assertNull(caseData.getJudgeOverviewMarkdown());
    }

    @Test
    void rendersOverviewWithoutTimelineWhenEventHistoryCannotBeLoaded() {
        ETCaseView caseView = new ETCaseView(
            new JudgeOverviewRenderer(new JudgeOverviewModelFactory()),
            caseReference -> {
                throw new IllegalStateException("database unavailable");
            }
        );
        CaseData caseData = new CaseData();

        caseView.getCase(new CaseViewRequest<>(1_234_567_890_123_456L, CaseState.Accepted), caseData);

        assertTrue(caseData.getJudgeOverviewMarkdown().contains("No future hearing is listed"));
        assertFalse(caseData.getJudgeOverviewMarkdown().contains("Recent activity"));
    }
}
