package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.caseview.state.CaseState;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JudgeOverviewRenderer {

    private static final String TEMPLATE_PATH = "templates/caseview/judge-overview.mustache";
    private static final String FAILURE_MARKUP = "<div class=\"govuk-warning-text\">"
        + "<strong class=\"govuk-warning-text__text\">"
        + "Warning: This overview could not be generated. The other case tabs are still available."
        + "</strong></div>";

    private final JudgeOverviewModelFactory modelFactory;
    private final Template template;

    @Autowired
    public JudgeOverviewRenderer(JudgeOverviewModelFactory modelFactory) {
        this(modelFactory, compileTemplate());
    }

    JudgeOverviewRenderer(JudgeOverviewModelFactory modelFactory, Template template) {
        this.modelFactory = modelFactory;
        this.template = template;
    }

    public String render(CaseData caseData, long caseReference, CaseState state) {
        return render(caseData, caseReference, state, List.of());
    }

    public String render(CaseData caseData, long caseReference, CaseState state,
                         List<CaseTimelineEvent> timeline) {
        try {
            return template.execute(modelFactory.create(caseData, caseReference, state, timeline));
        } catch (RuntimeException exception) {
            log.warn("Unable to render judge overview for case {}", caseReference, exception);
            return FAILURE_MARKUP;
        }
    }

    private static Template compileTemplate() {
        ClassPathResource resource = new ClassPathResource(TEMPLATE_PATH);
        try {
            String templateSource = resource.getContentAsString(StandardCharsets.UTF_8);
            String compactTemplate = templateSource.replaceAll("\\R\\s*", "");
            return Mustache.compiler().compile(compactTemplate);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + TEMPLATE_PATH, exception);
        }
    }
}
