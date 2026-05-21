package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.UpdateCaseQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest(properties = "core_case_data.api.url=localhost:4452")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class UpdateCaseQueueRepositoryTest {

    private static final PostgreSQLContainer postgreSQLContainer = EtCosPostgresqlContainer.getInstance();

    static {
        postgreSQLContainer.start();
    }

    @Autowired
    UpdateCaseQueueRepository updateCaseQueueRepository;

    @Autowired
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        updateCaseQueueRepository.deleteAll();
    }

    @Test
    void shouldFindAndLockPendingMessage() {
        UpdateCaseQueueMessage msg = UpdateCaseQueueMessage.builder()
            .messageId("msg-" + UUID.randomUUID())
            .messageBody("{\"msg\":\"test\"}")
            .status(QueueMessageStatus.PENDING)
            .createdAt(LocalDateTime.now().minusSeconds(5))
            .retryCount(0)
            .build();

        UpdateCaseQueueMessage saved = updateCaseQueueRepository.save(msg);
        assertNotNull(saved.getId());

        List<UpdateCaseQueueMessage> pending = updateCaseQueueRepository.findPendingMessages(
            LocalDateTime.now(),
            PageRequest.of(0, 10)
        );
        assertEquals(1, pending.size());

        int locked = updateCaseQueueRepository.lockMessage(
            saved.getMessageId(),
            "processor-1",
            LocalDateTime.now().plusMinutes(5),
            LocalDateTime.now()
        );
        assertEquals(1, locked);
        flushAndClear();

        UpdateCaseQueueMessage updated = updateCaseQueueRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(QueueMessageStatus.PROCESSING, updated.getStatus());
    }

    @Test
    void shouldMarkRetriedMessageAsPendingWithoutProcessedAt() {
        UpdateCaseQueueMessage saved = updateCaseQueueRepository.save(createProcessingMessage(0));

        updateCaseQueueRepository.markAsFailed(
            saved.getMessageId(),
            "Temporary failure",
            1,
            QueueMessageStatus.PENDING,
            null
        );
        flushAndClear();

        UpdateCaseQueueMessage updated = updateCaseQueueRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(QueueMessageStatus.PENDING, updated.getStatus());
        assertEquals(Integer.valueOf(1), updated.getRetryCount());
        assertNull(updated.getProcessedAt());
    }

    @Test
    void shouldMarkFailedMessageWithProcessedAt() {
        UpdateCaseQueueMessage saved = updateCaseQueueRepository.save(createProcessingMessage(9));
        LocalDateTime processedAt = LocalDateTime.now();

        updateCaseQueueRepository.markAsFailed(
            saved.getMessageId(),
            "Final failure",
            10,
            QueueMessageStatus.FAILED,
            processedAt
        );
        flushAndClear();

        UpdateCaseQueueMessage updated = updateCaseQueueRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(QueueMessageStatus.FAILED, updated.getStatus());
        assertEquals(Integer.valueOf(10), updated.getRetryCount());
        assertNotNull(updated.getProcessedAt());
    }

    private UpdateCaseQueueMessage createProcessingMessage(int retryCount) {
        return UpdateCaseQueueMessage.builder()
            .messageId("msg-" + UUID.randomUUID())
            .messageBody("{\"msg\":\"test\"}")
            .status(QueueMessageStatus.PROCESSING)
            .createdAt(LocalDateTime.now().minusSeconds(5))
            .retryCount(retryCount)
            .lockedBy("processor-1")
            .lockedUntil(LocalDateTime.now().plusMinutes(5))
            .build();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
