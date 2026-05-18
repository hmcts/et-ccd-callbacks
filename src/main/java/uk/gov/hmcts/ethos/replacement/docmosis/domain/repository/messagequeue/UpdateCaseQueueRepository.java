package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.UpdateCaseQueueMessage;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UpdateCaseQueueRepository extends JpaRepository<UpdateCaseQueueMessage, Long> {

    @Query("SELECT m FROM UpdateCaseQueueMessage m "
           + "WHERE m.status = "
           + "uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus.PENDING "
           + "AND (m.lockedUntil IS NULL OR m.lockedUntil < :now) "
           + "ORDER BY m.createdAt ASC")
    List<UpdateCaseQueueMessage> findPendingMessages(@Param("now") LocalDateTime now,
                                                     Pageable pageable);

    @Modifying
    @Query("UPDATE UpdateCaseQueueMessage m "
           + "SET m.status = "
           + "uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus.PROCESSING, "
           + "m.lockedBy = :lockedBy, "
           + "m.lockedUntil = :lockedUntil "
           + "WHERE m.messageId = :messageId "
           + "AND m.status = "
           + "uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus.PENDING "
           + "AND (m.lockedUntil IS NULL OR m.lockedUntil < :now)")
    int lockMessage(@Param("messageId") String messageId,
                    @Param("lockedBy") String lockedBy,
                    @Param("lockedUntil") LocalDateTime lockedUntil,
                    @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE UpdateCaseQueueMessage m "
           + "SET m.status = uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus.COMPLETED, "
           + "m.processedAt = :processedAt, "
           + "m.lockedBy = NULL, "
           + "m.lockedUntil = NULL "
           + "WHERE m.messageId = :messageId")
    void markAsCompleted(@Param("messageId") String messageId,
                         @Param("processedAt") LocalDateTime processedAt);

    @Modifying
    @Query(value = """
    UPDATE update_case_queue_message
        SET status = :status,
            error_message = :errorMessage,
            retry_count = :retryCount,
            locked_by = NULL,
            locked_until = NULL,
            processed_at = CASE
                WHEN :status = 'FAILED'
                THEN :processedAt::timestamp
                ELSE processed_at
            END
        WHERE message_id = :messageId
        """, nativeQuery = true)
    void markAsFailed(@Param("messageId") String messageId,
                      @Param("errorMessage") String errorMessage,
                      @Param("retryCount") int retryCount,
                      @Param("status") String status, // Pass as String for native SQL
                      @Param("processedAt") LocalDateTime processedAt);
}
