package uk.gov.hmcts.ethos.replacement.docmosis.service.retention;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaseRetentionService {
    private final RetentionCaseDataRepository repository;
    private final AdminUserService adminUserService;
    private final CcdClient ccdClient;

    public RetentionTaskResult run(Collection<String> deletionCaseTypeIds,
                                   Collection<String> simulationCaseTypeIds,
                                   int batchSize) {
        ModeResult deletionResult = processDeletion(deletionCaseTypeIds, batchSize);
        ModeResult simulationResult = processSimulation(simulationCaseTypeIds, batchSize);
        return new RetentionTaskResult(
            deletionResult.affectedCases(),
            simulationResult.affectedCases(),
            deletionResult.skippedCases()
        );
    }

    private ModeResult processDeletion(Collection<String> caseTypeIds, int batchSize) {
        if (caseTypeIds.isEmpty() || batchSize <= 0) {
            log.info("Case retention deletion mode has no configured case types");
            return new ModeResult(0, 0);
        }

        List<RetentionCaseData> candidates = repository.findExpiredDraftCases(caseTypeIds, batchSize);
        log.info("Case retention deletion mode found {} candidate cases", candidates.size());
        if (candidates.isEmpty()) {
            return new ModeResult(0, 0);
        }

        String adminUserToken = adminUserService.getAdminUserToken();
        Set<Long> referencesToDelete = new LinkedHashSet<>();
        int skippedCases = 0;

        for (RetentionCaseData candidate : candidates) {
            if (isCcdPointerConfirmedDeleted(candidate, adminUserToken)) {
                referencesToDelete.add(candidate.reference());
            } else {
                skippedCases++;
                log.info("Skipping case {} because the CCD pointer is not confirmed deleted", candidate.reference());
            }
        }

        int deletedCases = referencesToDelete.isEmpty() ? 0 : repository.deleteCases(referencesToDelete);
        log.info("Case retention deletion mode deleted {} cases and skipped {} cases", deletedCases, skippedCases);
        return new ModeResult(deletedCases, skippedCases);
    }

    private ModeResult processSimulation(Collection<String> caseTypeIds, int batchSize) {
        if (caseTypeIds.isEmpty() || batchSize <= 0) {
            log.info("Case retention simulation mode has no configured case types");
            return new ModeResult(0, 0);
        }

        List<RetentionCaseData> candidates = repository.findExpiredDraftCases(caseTypeIds, batchSize);
        log.info("Case retention simulation mode would delete {} cases", candidates.size());
        return new ModeResult(candidates.size(), 0);
    }

    private boolean isCcdPointerConfirmedDeleted(RetentionCaseData retentionCase, String adminUserToken) {
        try {
            ccdClient.retrieveCase(
                adminUserToken,
                retentionCase.caseTypeId(),
                retentionCase.jurisdiction(),
                String.valueOf(retentionCase.reference())
            );
            return false;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == NOT_FOUND.value()) {
                return true;
            }
            log.error("CCD existence check failed for case {}", retentionCase.reference(), e);
            return false;
        } catch (IOException e) {
            log.error("CCD existence check failed for case {}", retentionCase.reference(), e);
            return false;
        }
    }

    private record ModeResult(int affectedCases, int skippedCases) {
    }
}
