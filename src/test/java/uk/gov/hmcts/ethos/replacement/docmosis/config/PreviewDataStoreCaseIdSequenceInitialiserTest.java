package uk.gov.hmcts.ethos.replacement.docmosis.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class PreviewDataStoreCaseIdSequenceInitialiserTest {

    @Test
    void shouldAlignSequenceUsingConfiguredBase() throws Exception {
        JdbcTemplate jdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(
            PreviewDataStoreCaseIdSequenceInitialiser.ALIGN_SEQUENCE_SQL,
            Long.class,
            91304100000000000L
        )).thenReturn(91304100000000000L);

        PreviewDataStoreCaseIdSequenceInitialiser initialiser =
            new PreviewDataStoreCaseIdSequenceInitialiser(
                91304100000000000L,
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
            91304100000000000L
        );
    }
}
