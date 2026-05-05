package uk.gov.hmcts.ethos.replacement.docmosis.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PreviewDataStoreCaseIdSequenceInitialiserTest {
    private static final long SEQUENCE_BASE = 91_304_100_000_000_000L;

    @Test
    void shouldAlignSequenceUsingConfiguredBase() throws Exception {
        JdbcTemplate applicationJdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        JdbcTemplate dataStoreJdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        when(applicationJdbcTemplate.queryForObject(
            PreviewDataStoreCaseIdSequenceInitialiser.LOCAL_MAX_CASE_DATA_ID_SQL,
            Long.class
        )).thenReturn(0L);
        when(dataStoreJdbcTemplate.queryForObject(
            PreviewDataStoreCaseIdSequenceInitialiser.ALIGN_SEQUENCE_SQL,
            Long.class,
            SEQUENCE_BASE
        )).thenReturn(SEQUENCE_BASE);

        PreviewDataStoreCaseIdSequenceInitialiser initialiser =
            new PreviewDataStoreCaseIdSequenceInitialiser(
                applicationJdbcTemplate,
                SEQUENCE_BASE,
                "host",
                5432,
                "db",
                "user",
                "password",
                "?sslmode=require"
            ) {
                @Override
                JdbcTemplate createDataStoreJdbcTemplate() {
                    return dataStoreJdbcTemplate;
                }
            };

        initialiser.run(null);

        verify(applicationJdbcTemplate).queryForObject(
            PreviewDataStoreCaseIdSequenceInitialiser.LOCAL_MAX_CASE_DATA_ID_SQL,
            Long.class
        );
        verify(dataStoreJdbcTemplate).queryForObject(
            PreviewDataStoreCaseIdSequenceInitialiser.ALIGN_SEQUENCE_SQL,
            Long.class,
            SEQUENCE_BASE
        );
    }

    @Test
    void shouldAlignSequenceUsingLocalCaseDataIdWhenHigherThanConfiguredBase() throws Exception {
        long localMaxCaseDataId = SEQUENCE_BASE + 3;
        JdbcTemplate applicationJdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        JdbcTemplate dataStoreJdbcTemplate = org.mockito.Mockito.mock(JdbcTemplate.class);
        when(applicationJdbcTemplate.queryForObject(
            PreviewDataStoreCaseIdSequenceInitialiser.LOCAL_MAX_CASE_DATA_ID_SQL,
            Long.class
        )).thenReturn(localMaxCaseDataId);
        when(dataStoreJdbcTemplate.queryForObject(
            PreviewDataStoreCaseIdSequenceInitialiser.ALIGN_SEQUENCE_SQL,
            Long.class,
            localMaxCaseDataId
        )).thenReturn(localMaxCaseDataId);

        PreviewDataStoreCaseIdSequenceInitialiser initialiser =
            new PreviewDataStoreCaseIdSequenceInitialiser(
                applicationJdbcTemplate,
                SEQUENCE_BASE,
                "host",
                5432,
                "db",
                "user",
                "password",
                "?sslmode=require"
            ) {
                @Override
                JdbcTemplate createDataStoreJdbcTemplate() {
                    return dataStoreJdbcTemplate;
                }
            };

        initialiser.run(null);

        verify(dataStoreJdbcTemplate).queryForObject(
            PreviewDataStoreCaseIdSequenceInitialiser.ALIGN_SEQUENCE_SQL,
            Long.class,
            localMaxCaseDataId
        );
    }
}
