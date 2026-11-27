package uk.gov.hmcts.ethos.replacement.docmosis.domain.ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Transient DB-backed cache entry for pre-computed AI ET1 vetting results across AKS replicas.
 */
@Entity
@Table(name = "ai_vetting_pending_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiVettingPendingResult {

    @Id
    @Column(name = "case_reference")
    private Long caseReference;

    @Column(name = "result_json", nullable = false, columnDefinition = "TEXT")
    private String resultJson;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public static AiVettingPendingResult create(long caseReference, String resultJson, Instant expiresAt) {
        return new AiVettingPendingResult(caseReference, resultJson, expiresAt);
    }
}
