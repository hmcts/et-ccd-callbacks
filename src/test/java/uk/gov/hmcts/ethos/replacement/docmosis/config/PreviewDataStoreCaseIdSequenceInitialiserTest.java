package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PreviewDataStoreCaseIdSequenceInitialiserTest {
    private static final long SEQUENCE_BASE = 91_304_100_000_000_000L;

    @Test
    void shouldAlignSequenceUsingConfiguredBase() throws Exception {
        JdbcTemplate jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(
            PreviewDataStoreCaseIdSequenceInitialiser.ALIGN_SEQUENCE_SQL,
            Long.class,
            SEQUENCE_BASE
        )).thenReturn(SEQUENCE_BASE);

        PreviewDataStoreCaseIdSequenceInitialiser initialiser =
            new PreviewDataStoreCaseIdSequenceInitialiser(
                SEQUENCE_BASE,
                "host",
                5432,
                "db",
                "user",
                "password",
                "?sslmode=require"
            ) {
                @Override
                JdbcTemplate createJdbcTemplate() {
                    return jdbcTemplate;
                }
            };

        initialiser.run(null);

        verify(jdbcTemplate).queryForObject(
            PreviewDataStoreCaseIdSequenceInitialiser.ALIGN_SEQUENCE_SQL,
            Long.class,
            SEQUENCE_BASE
        );
    }
}
