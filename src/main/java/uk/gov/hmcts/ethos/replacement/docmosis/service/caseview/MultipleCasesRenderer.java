package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MultipleCasesRenderer {

    private static final String TEMPLATE_PATH = "templates/caseview/multiple-cases.mustache";

    private static final String MULTIPLE_CASES_SQL = """
        SELECT cd.reference,
               cd.data ->> 'ethosCaseReference' AS ethos_case_reference,
               cd.data ->> 'claimant' AS claimant,
               cd.data ->> 'respondent' AS respondent,
               cd.data ->> 'subMultipleName' AS sub_multiple,
               cd.state,
               cd.data->> 'leadClaimant' AS lead_claimant
        FROM ccd.case_data cd
        WHERE cd.data ->> 'multipleReference' = :multipleReference
          AND cd.case_type_id = REPLACE(
                (SELECT current_case.case_type_id
                 FROM ccd.case_data current_case
                 WHERE current_case.reference = :multipleCaseReference),
                '_Multiple',
                ''
              )
        ORDER BY cd.data->> 'leadClaimant' DESC, 
            (string_to_array(cd.data ->> 'ethosCaseReference', '/'))[2]::INT,
            (string_to_array(cd.data ->> 'ethosCaseReference', '/'))[1]::BIGINT
        """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Template template = compileTemplate();

    public String render(long multipleCaseReference, String multipleReference) {
        if (multipleReference == null || multipleReference.isBlank()) {
            return template.execute(new MultipleCasesModel(multipleCaseReference, List.of()));
        }

        List<LinkedCase> linkedCases = jdbcTemplate.query(
            MULTIPLE_CASES_SQL,
            new MapSqlParameterSource()
                .addValue("multipleCaseReference", multipleCaseReference)
                .addValue("multipleReference", multipleReference),
            (resultSet, rowNumber) -> new LinkedCase(
                resultSet.getLong("reference"),
                resultSet.getString("ethos_case_reference"),
                resultSet.getString("claimant"),
                resultSet.getString("respondent"),
                resultSet.getString("sub_multiple"),
                resultSet.getString("state"),
                resultSet.getString("lead_claimant")
            )
        );

        return template.execute(new MultipleCasesModel(multipleCaseReference, linkedCases));
    }

    private static Template compileTemplate() {
        try {
            String source = new ClassPathResource(TEMPLATE_PATH).getContentAsString(StandardCharsets.UTF_8);
            return Mustache.compiler().compile(source.replaceAll("\\R\\s*", ""));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load " + TEMPLATE_PATH, exception);
        }
    }

    @lombok.Value
    private static class MultipleCasesModel {
        long caseReference;
        List<LinkedCase> cases;
        boolean hasCases;

        MultipleCasesModel(long caseReference, List<LinkedCase> linkedCases) {
            this.caseReference = caseReference;
            this.cases = linkedCases;
            this.hasCases = !linkedCases.isEmpty();
        }
    }

    private record LinkedCase(long reference, String ethosCaseReference, String claimant, String respondent,
                              String subMultiple, String state, String leadClaimant) {

        public String displayReference() {
            return ethosCaseReference == null || ethosCaseReference.isBlank()
                ? Long.toString(reference) : ethosCaseReference;
        }

        public String displaySubMultiple() {
            return subMultiple == null || subMultiple.isBlank() ? "-" : subMultiple;
        }
    }
}
