package uk.gov.hmcts.ethos.replacement.docmosis.tasks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ethos.replacement.docmosis.service.retention.CaseRetentionService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.retention.RetentionTaskResult;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CaseRetentionTask implements Runnable {
    private final CaseRetentionService caseRetentionService;

    @Value("${retention.disposal.enabled:true}")
    private boolean enabled;

    @Value("${retention.disposal.caseTypeIds:}")
    private String caseTypeIds;

    @Value("${retention.disposal.simulationCaseTypeIds:}")
    private String simulationCaseTypeIds;

    @Value("${retention.disposal.batchSize:100}")
    private int batchSize;

    @Override
    public void run() {
        if (!enabled) {
            log.info("Case retention task is disabled");
            return;
        }

        RetentionTaskResult result = caseRetentionService.run(
            parseCaseTypes(caseTypeIds),
            parseCaseTypes(simulationCaseTypeIds),
            batchSize
        );
        log.info("Case retention task complete: deleted={}, simulated={}, skipped={}",
            result.deletedCases(), result.simulatedCases(), result.skippedCases());
    }

    private Set<String> parseCaseTypes(String configuredCaseTypes) {
        if (StringUtils.isBlank(configuredCaseTypes)) {
            return Set.of();
        }

        return Arrays.stream(configuredCaseTypes.split(","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
    }
}
