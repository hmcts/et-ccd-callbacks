package uk.gov.hmcts.ethos.replacement.docmosis.service.caseview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.ccd.sdk.config.DecentralisedDataConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "core_case_data.api.url=localhost:4452")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({DecentralisedDataConfiguration.class, MultipleCasesRenderer.class})
class MultipleCasesRendererIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRES = EtCosPostgresqlContainer.getInstance();

    private static final long MULTIPLE_CASE_REF = 9_999_000_000_000_001L;
    private static final long LEAD_CASE_REF = 9_999_000_000_000_002L;
    private static final long SINGLE_CASE_REF = 9_999_000_000_000_003L;
    private static final long INVALID_REF_CASE_REF = 9_999_000_000_000_004L;
    private static final long BLANK_REF_CASE_REF = 9_999_000_000_000_005L;
    private static final long OTHER_MULTIPLE_CASE_REF = 9_999_000_000_000_006L;
    private static final long SINGLE_TYPE_CASE_REF = 9_999_000_000_000_007L;

    private static final String MULTIPLE_REF = "600001";

    static {
        POSTGRES.start();
    }

    @Autowired
    private MultipleCasesRenderer renderer;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("delete from ccd.case_data where reference >= 9999000000000000 and reference < 9999000000000010");

        insertCase(MULTIPLE_CASE_REF, "ET_EnglandWales_Multiple", "Open",
            String.format("{\"multipleReference\": \"%s\"}", MULTIPLE_REF));

        insertCase(LEAD_CASE_REF, "ET_EnglandWales", "Accepted",
            String.format("""
                {
                    "caseType": "Multiple",
                    "multipleReference": "%s",
                    "ethosCaseReference": "500002/2024",
                    "claimant": "Alice Lead",
                    "respondent": "Respondent Corp",
                    "subMultipleName": "SubGroup 1",
                    "leadClaimant": "Yes"
                }
                """, MULTIPLE_REF));

        insertCase(SINGLE_CASE_REF, "ET_EnglandWales", "Submitted",
            String.format("""
                {
                    "caseType": "Multiple",
                    "multipleReference": "%s",
                    "ethosCaseReference": "500001/2024",
                    "claimant": "Bob Standard",
                    "respondent": "Respondent Corp",
                    "subMultipleName": "SubGroup 1"
                }
                """, MULTIPLE_REF));

        insertCase(INVALID_REF_CASE_REF, "ET_EnglandWales", "Vetted",
            String.format("""
                {
                    "caseType": "Multiple",
                    "multipleReference": "%s",
                    "ethosCaseReference": "NOT_A_NUMERIC_REF",
                    "claimant": "Charlie NonNumeric",
                    "respondent": "Respondent Corp"
                }
                """, MULTIPLE_REF));

        insertCase(BLANK_REF_CASE_REF, "ET_EnglandWales", "Rejected",
            String.format("""
                {
                    "caseType": "Multiple",
                    "multipleReference": "%s",
                    "claimant": "David BlankRef",
                    "respondent": "Respondent Corp"
                }
                """, MULTIPLE_REF));

        insertCase(OTHER_MULTIPLE_CASE_REF, "ET_EnglandWales", "Accepted",
            """
            {
                "caseType": "Multiple",
                "multipleReference": "999999",
                "ethosCaseReference": "500003/2024",
                "claimant": "Eve OtherMultiple",
                "respondent": "Respondent Corp"
            }
            """);

        insertCase(SINGLE_TYPE_CASE_REF, "ET_EnglandWales", "Accepted",
            String.format("""
                {
                    "caseType": "Single",
                    "multipleReference": "%s",
                    "ethosCaseReference": "500004/2024",
                    "claimant": "Frank SingleType",
                    "respondent": "Respondent Corp"
                }
                """, MULTIPLE_REF));
    }

    private void insertCase(long ref, String caseTypeId, String state, String dataJson) {
        jdbc.update("""
            insert into ccd.case_data (
                id,
                reference,
                security_classification,
                jurisdiction,
                case_type_id,
                state,
                data
            ) values (?, ?, 'PUBLIC', 'EMPLOYMENT', ?, ?, ?::jsonb)
            """, ref, ref, caseTypeId, state, dataJson);
    }

    @Test
    void render_returnsOrderedCasesWithNullsLastAndHandlesBlankOrInvalidReferences() {
        String html = renderer.render(MULTIPLE_CASE_REF, MULTIPLE_REF);

        assertThat(html)
            .contains("<table class=\"govuk-table\">")
            .contains("Alice Lead")
            .contains("Bob Standard")
            .contains("Charlie NonNumeric")
            .contains("David BlankRef")
            .doesNotContain("Eve OtherMultiple")
            .doesNotContain("Frank SingleType");

        int leadIndex = html.indexOf("Alice Lead");
        int standardIndex = html.indexOf("Bob Standard");
        int nonNumericIndex = html.indexOf("Charlie NonNumeric");
        int blankRefIndex = html.indexOf("David BlankRef");

        assertThat(leadIndex).isLessThan(standardIndex);
        assertThat(standardIndex).isLessThan(nonNumericIndex);
        assertThat(nonNumericIndex).isLessThan(blankRefIndex);

        assertThat(html).contains(String.format("/cases/case-details/%d\">%d</a>",
            BLANK_REF_CASE_REF, BLANK_REF_CASE_REF));
    }
}
