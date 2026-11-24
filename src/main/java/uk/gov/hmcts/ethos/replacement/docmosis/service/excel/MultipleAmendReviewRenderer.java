package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MultipleAmendReviewRenderer {

    private static final String TEMPLATE_PATH = "templates/multiples/amend-review.mustache";
    private final Template template = compileTemplate();

    public String render(List<CaseData> cases, boolean removing) {
        return template.execute(Map.of(
                "action", removing ? "removing" : "adding",
                "verb", removing ? "remove" : "add",
                "preposition", removing ? "from" : "to",
                "count", cases.size(),
                "caseNoun", cases.size() == 1 ? "case" : "cases",
                "cases", cases.stream().map(this::caseRow).toList()));
    }

    private Map<String, Object> caseRow(CaseData data) {
        return Map.of(
                "caseNumber", display(data.getEthosCaseReference()),
                "claimant", claimant(data),
                "respondents", respondents(data));
    }

    private String claimant(CaseData data) {
        if (data.getClaimantCompany() != null && !data.getClaimantCompany().isBlank()) {
            return data.getClaimantCompany();
        }
        if (data.getClaimantIndType() != null) {
            String name = fullName(data.getClaimantIndType().getClaimantFirstNames(),
                    data.getClaimantIndType().getClaimantLastName());
            if (!name.isBlank()) {
                return name;
            }
        }
        return display(data.getClaimant());
    }

    private List<String> respondents(CaseData data) {
        List<String> names = data.getRespondentCollection() == null ? List.of()
                : data.getRespondentCollection().stream()
                .filter(Objects::nonNull)
                .map(RespondentSumTypeItem::getValue)
                .filter(Objects::nonNull)
                .map(this::respondentName)
                .filter(name -> !name.isBlank())
                .toList();
        return names.isEmpty() ? List.of(display(data.getRespondent())) : names;
    }

    private String respondentName(RespondentSumType respondent) {
        if (respondent.getRespondentName() != null && !respondent.getRespondentName().isBlank()) {
            return respondent.getRespondentName();
        }
        if (respondent.getRespondentOrganisation() != null && !respondent.getRespondentOrganisation().isBlank()) {
            return respondent.getRespondentOrganisation();
        }
        return fullName(respondent.getRespondentFirstName(), respondent.getRespondentLastName());
    }

    private String fullName(String firstName, String lastName) {
        return Stream.of(firstName, lastName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .collect(Collectors.joining(" "));
    }

    private String display(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }

    private static Template compileTemplate() {
        try {
            String source = new ClassPathResource(TEMPLATE_PATH).getContentAsString(StandardCharsets.UTF_8);
            return Mustache.compiler().compile(source.replaceAll("\\R\\s*", ""));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + TEMPLATE_PATH, exception);
        }
    }
}
