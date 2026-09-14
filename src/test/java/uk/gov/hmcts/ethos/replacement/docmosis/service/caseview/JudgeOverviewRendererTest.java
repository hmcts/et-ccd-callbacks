package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import com.samskivert.mustache.Mustache;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JudgeOverviewRendererTest {

    private static final long CASE_REFERENCE = 1_701_234_567_890_123L;
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-14T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void rendersEscapedSanitiserSafeMarkup() {
        CaseData caseData = new CaseData();
        caseData.setClaimant("<script>alert('claimant')</script>");
        caseData.setRespondent("Example & Sons");
        JudgeOverviewRenderer renderer = new JudgeOverviewRenderer(new JudgeOverviewModelFactory(CLOCK));

        String markup = renderer.render(caseData, CASE_REFERENCE, CaseState.Accepted);

        assertThat(markup)
            .contains("&lt;script&gt;alert(&#39;claimant&#39;)&lt;/script&gt; v Example &amp; Sons")
            .contains("/cases/case-details/1701234567890123/trigger/createReferral/createReferral1")
            .contains("<h3 class=\"govuk-heading-m\">Next hearing</h3>")
            .doesNotContain("\n")
            .doesNotContain("<script", "style=", "id=", "data-", "javascript:", "onclick=");
    }

    @Test
    void templateUsesEscapedVariablesOnly() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/caseview/judge-overview.mustache");
        String template = resource.getContentAsString(StandardCharsets.UTF_8);

        assertThat(template).doesNotContain("{{{", "{{&");
    }

    @Test
    void returnsWarningWhenModelCreationFails() {
        JudgeOverviewModelFactory failingFactory = new JudgeOverviewModelFactory(CLOCK) {
            @Override
            public JudgeOverviewModel create(CaseData caseData, long caseReference, CaseState state,
                                             List<CaseTimelineEvent> timeline) {
                throw new IllegalStateException("boom");
            }
        };
        JudgeOverviewRenderer renderer = new JudgeOverviewRenderer(
            failingFactory,
            Mustache.compiler().compile("unused")
        );

        String markup = renderer.render(new CaseData(), CASE_REFERENCE, CaseState.Accepted);

        assertThat(markup)
            .contains("This overview could not be generated")
            .doesNotContain("boom");
    }
}
