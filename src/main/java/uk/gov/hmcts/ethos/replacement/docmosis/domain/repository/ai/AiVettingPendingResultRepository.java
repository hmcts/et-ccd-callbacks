package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ai.AiVettingPendingResult;

import java.time.Instant;

public interface AiVettingPendingResultRepository extends JpaRepository<AiVettingPendingResult, Long> {

    @Transactional
    void deleteByExpiresAtBefore(Instant cutoff);
}
