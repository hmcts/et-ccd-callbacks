package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseRetentionService {
    private static final Pattern CASE_DETAILS_LINK_PATTERN = Pattern.compile("/case-details/(\\d+)");
    private static final int MAX_LINK_GROUP_ITERATIONS = 50;

    private final RetentionCaseDataRepository repository;
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;
    private final Clock clock;

    public RetentionTaskResult run(Collection<String> deletionCaseTypeIds,
                                   Collection<String> simulationCaseTypeIds,
                                   int batchSize) {
        ModeResult deletionResult = processMode(deletionCaseTypeIds, batchSize, false);
        ModeResult simulationResult = processMode(simulationCaseTypeIds, batchSize, true);
        return new RetentionTaskResult(
            deletionResult.affectedCases(),
            simulationResult.affectedCases(),
            deletionResult.skippedGroups() + simulationResult.skippedGroups()
        );
    }

    private ModeResult processMode(Collection<String> caseTypeIds, int batchSize, boolean simulation) {
        if (caseTypeIds.isEmpty() || batchSize <= 0) {
            log.info("Case retention {} mode has no configured case types", modeName(simulation));
            return new ModeResult(0, 0);
        }

        List<RetentionCaseData> candidates = repository.findExpiredCases(caseTypeIds, batchSize);
        log.info("Case retention {} mode found {} candidate cases", modeName(simulation), candidates.size());

        Set<Long> processed = new LinkedHashSet<>();
        String adminUserToken = adminUserService.getAdminUserToken();
        int affectedCases = 0;
        int skippedGroups = 0;

        for (RetentionCaseData candidate : candidates) {
            if (!processed.add(candidate.reference())) {
                continue;
            }

            Map<Long, RetentionCaseData> linkedGroup = resolveLinkedGroup(candidate.reference());
            processed.addAll(linkedGroup.keySet());

            if (!allCasesEligible(linkedGroup.values(), caseTypeIds)) {
                skippedGroups++;
                log.info("Skipping retention group {} because at least one linked case is not eligible",
                    linkedGroup.keySet());
                continue;
            }

            if (simulation) {
                affectedCases += linkedGroup.size();
                log.info("Simulation mode would delete retention group {}", linkedGroup.keySet());
                continue;
            }

            if (!allCcdPointersRemoved(linkedGroup.values(), adminUserToken)) {
                skippedGroups++;
                log.info("Skipping retention group {} because at least one CCD pointer still exists",
                    linkedGroup.keySet());
                continue;
            }

            int deleted = repository.deleteCases(linkedGroup.keySet());
            affectedCases += deleted;
            log.info("Deleted {} local case_data rows for retention group {}", deleted, linkedGroup.keySet());
        }

        log.info("Case retention {} mode affected {} cases and skipped {} groups",
            modeName(simulation), affectedCases, skippedGroups);
        return new ModeResult(affectedCases, skippedGroups);
    }

    private Map<Long, RetentionCaseData> resolveLinkedGroup(Long startReference) {
        Map<Long, RetentionCaseData> group = new LinkedHashMap<>();
        addCases(group, repository.findByReferences(Set.of(startReference)));

        boolean changed = true;
        int iterations = 0;
        while (changed && iterations < MAX_LINK_GROUP_ITERATIONS) {
            iterations++;
            final int sizeBefore = group.size();
            Set<Long> references = new LinkedHashSet<>(group.keySet());

            Set<Long> linkedReferences = outboundLinkedReferences(group.values());
            addCases(group, repository.findByReferences(linkedReferences));

            addCases(group, repository.findCasesReferencing(references));
            changed = group.size() > sizeBefore;
        }

        if (iterations == MAX_LINK_GROUP_ITERATIONS) {
            log.warn("Stopped resolving linked retention group for case {} after {} iterations",
                startReference, MAX_LINK_GROUP_ITERATIONS);
        }

        return group;
    }

    private void addCases(Map<Long, RetentionCaseData> group, List<RetentionCaseData> cases) {
        for (RetentionCaseData retentionCase : cases) {
            group.putIfAbsent(retentionCase.reference(), retentionCase);
        }
    }

    private boolean allCasesEligible(Collection<RetentionCaseData> cases, Collection<String> caseTypeIds) {
        return cases.stream().allMatch(retentionCase ->
            caseTypeIds.contains(retentionCase.caseTypeId()) && isExpired(retentionCase));
    }

    private boolean isExpired(RetentionCaseData retentionCase) {
        LocalDate resolvedTtl = retentionCase.resolvedTtl();
        return resolvedTtl != null && resolvedTtl.isBefore(LocalDate.now(clock));
    }

    private boolean allCcdPointersRemoved(Collection<RetentionCaseData> cases, String adminUserToken) {
        for (RetentionCaseData retentionCase : cases) {
            if (ccdPointerExists(retentionCase, adminUserToken)) {
                return false;
            }
        }
        return true;
    }

    private boolean ccdPointerExists(RetentionCaseData retentionCase, String adminUserToken) {
        try {
            ccdClient.retrieveCase(
                adminUserToken,
                retentionCase.caseTypeId(),
                retentionCase.jurisdiction(),
                String.valueOf(retentionCase.reference())
            );
            return true;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == NOT_FOUND.value()) {
                return false;
            }
            log.error("CCD existence check failed for case {}", retentionCase.reference(), e);
            return true;
        } catch (IOException e) {
            log.error("CCD existence check failed for case {}", retentionCase.reference(), e);
            return true;
        }
    }

    private Set<Long> outboundLinkedReferences(Collection<RetentionCaseData> cases) {
        Set<Long> references = new LinkedHashSet<>();
        for (RetentionCaseData retentionCase : cases) {
            JsonNode data = retentionCase.data();
            addNumericField(references, data, "transferredCaseLinkSourceCaseId");
            addLinksFromMarkup(references, data, "linkedCaseCT");
            addLinksFromMarkup(references, data, "transferredCaseLink");
            addCaseLinks(references, data.path("caseLinks"));
        }
        return references;
    }

    private void addNumericField(Set<Long> references, JsonNode data, String fieldName) {
        String value = data.path(fieldName).asText(null);
        if (StringUtils.isNumeric(value)) {
            references.add(Long.valueOf(value));
        }
    }

    private void addLinksFromMarkup(Set<Long> references, JsonNode data, String fieldName) {
        String value = data.path(fieldName).asText(null);
        if (StringUtils.isBlank(value)) {
            return;
        }

        Matcher matcher = CASE_DETAILS_LINK_PATTERN.matcher(value);
        while (matcher.find()) {
            references.add(Long.valueOf(matcher.group(1)));
        }
    }

    private void addCaseLinks(Set<Long> references, JsonNode caseLinks) {
        if (!caseLinks.isArray()) {
            return;
        }

        caseLinks.forEach(item -> {
            String reference = item.path("value").path("CaseReference").asText(item.path("CaseReference").asText(null));
            if (StringUtils.isNumeric(reference)) {
                references.add(Long.valueOf(reference));
            }
        });
    }

    private String modeName(boolean simulation) {
        return simulation ? "simulation" : "deletion";
    }

    private record ModeResult(int affectedCases, int skippedGroups) {
    }
}
