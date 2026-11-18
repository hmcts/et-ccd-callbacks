package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClearHmcCaseFlagsTaskTest {

    @Mock
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Test
    void run_shouldUpdateMatchingCasesWhenDryRunIsFalse() {
        when(jdbcTemplate.queryForObject(
                eq(ClearHmcCaseFlagsTask.COUNT_CASES_WITH_HMC_FLAGS),
                any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenReturn(3L)
                .thenReturn(0L);
        when(jdbcTemplate.update(
                eq(ClearHmcCaseFlagsTask.CLEAR_HMC_FLAGS_SQL),
                any(MapSqlParameterSource.class)))
                .thenReturn(3);

        ClearHmcCaseFlagsTask task = new ClearHmcCaseFlagsTask(jdbcTemplate, false);
        task.run();

        verify(jdbcTemplate, times(1)).update(
                eq(ClearHmcCaseFlagsTask.CLEAR_HMC_FLAGS_SQL),
                any(MapSqlParameterSource.class));
    }

    @Test
    void run_shouldNotUpdateWhenDryRunIsTrue() {
        when(jdbcTemplate.queryForObject(
                eq(ClearHmcCaseFlagsTask.COUNT_CASES_WITH_HMC_FLAGS),
                any(MapSqlParameterSource.class),
                eq(Long.class)))
                .thenReturn(5L)
                .thenReturn(0L);

        ClearHmcCaseFlagsTask task = new ClearHmcCaseFlagsTask(jdbcTemplate, true);
        task.run();

        verify(jdbcTemplate, never()).update(any(String.class), any(MapSqlParameterSource.class));
    }
}
