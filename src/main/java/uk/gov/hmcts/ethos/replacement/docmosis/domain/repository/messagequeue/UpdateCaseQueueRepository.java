package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.UpdateCaseQueueMessage;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UpdateCaseQueueRepository extends JpaRepository<UpdateCaseQueueMessage, Long> {

    /**
     * Find messages that are ready to be processed (PENDING status and not locked).
     *
     * @param now current timestamp
     * @param pageable maximum number of messages to fetch
     * @return list of messages ready for processing
     */
    @Query("SELECT m FROM UpdateCaseQueueMessage m "
           + "WHERE m.status = uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus.PENDING "
           + "AND (m.lockedUntil IS NULL OR m.lockedUntil < :now) "
           + "ORDER BY m.createdAt ASC")
    List<UpdateCaseQueueMessage> findPendingMessages(@Param("now") LocalDateTime now,
                                                      org.springframework.data.domain.Pageable pageable);

    /**
     * Lock a message for processing.
     *
     * @param messageId message to lock
     * @param lockedBy identifier of processor
     * @param lockedUntil timestamp until which to lock
     * @param now current timestamp
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE UpdateCaseQueueMessage m "
           + "SET m.status = uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus.PROCESSING,"
           + "m.lockedBy = :lockedBy, "
           + "m.lockedUntil = :lockedUntil "
           + "WHERE m.messageId = :messageId "
           + "AND m.status = uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus.PENDING "
           + "AND (m.lockedUntil IS NULL OR m.lockedUntil < :now)")
    int lockMessage(@Param("messageId") String messageId,
                    @Param("lockedBy") String lockedBy,
                    @Param("lockedUntil") LocalDateTime lockedUntil,
                    @Param("now") LocalDateTime now);

    /**
     * Mark message as completed.
     *
     * @param messageId message to complete
     * @param processedAt timestamp of completion
     */
    @Modifying
    @Query("UPDATE UpdateCaseQueueMessage m "
           + "SET m.status = uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus.COMPLETED, "
           + "m.processedAt = :processedAt, "
           + "m.lockedBy = NULL, "
           + "m.lockedUntil = NULL "
           + "WHERE m.messageId = :messageId")
    void markAsCompleted(@Param("messageId") String messageId,
                        @Param("processedAt") LocalDateTime processedAt);

    /**
     * Mark message as failed and increment retry count.
     *
     * @param messageId message that failed
     * @param errorMessage error description
     * @param retryCount new retry count
     * @param status new status (PENDING for retry, FAILED for permanent failure)
     */
    @Modifying
    @Query("UPDATE UpdateCaseQueueMessage m "
           + "SET m.status = :status, "
           + "m.errorMessage = :errorMessage, "
           + "m.retryCount = :retryCount, "
           + "m.lockedBy = NULL, "
           + "m.lockedUntil = NULL, "
           + "m.processedAt = CASE "
           + "WHEN :status = uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus.FAILED T"
           + "HEN :processedAt ELSE NULL END "
           + "WHERE m.messageId = :messageId")
    void markAsFailed(@Param("messageId") String messageId,
                     @Param("errorMessage") String errorMessage,
                     @Param("retryCount") int retryCount,
                     @Param("status") QueueMessageStatus status,
                     @Param("processedAt") LocalDateTime processedAt);
}
