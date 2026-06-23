package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ethos.replacement.docmosis.service.retention.CaseRetentionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.retention.RetentionTaskResult;

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseRetentionTaskTest {
    @Mock
    private CaseRetentionService caseRetentionService;

    private CaseRetentionTask task;

    @BeforeEach
    void setUp() {
        task = new CaseRetentionTask(caseRetentionService);
        ReflectionTestUtils.setField(task, "caseTypeIds", "ET_EnglandWales, ET_Scotland");
        ReflectionTestUtils.setField(task, "simulationCaseTypeIds", "ET_EnglandWales_Multiple");
        ReflectionTestUtils.setField(task, "batchSize", 25);
    }

    @Test
    void runPassesConfiguredCaseTypesToService() {
        when(caseRetentionService.run(Set.of("ET_EnglandWales", "ET_Scotland"),
            Set.of("ET_EnglandWales_Multiple"), 25))
            .thenReturn(new RetentionTaskResult(1, 1, 0));

        task.run();

        verify(caseRetentionService).run(Set.of("ET_EnglandWales", "ET_Scotland"),
            Set.of("ET_EnglandWales_Multiple"), 25);
    }
}
