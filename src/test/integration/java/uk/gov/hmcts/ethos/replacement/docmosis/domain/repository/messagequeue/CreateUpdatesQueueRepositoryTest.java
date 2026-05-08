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
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.CreateUpdatesQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
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
class CreateUpdatesQueueRepositoryTest {

    private static final PostgreSQLContainer postgreSQLContainer = EtCosPostgresqlContainer.getInstance();

    static {
        postgreSQLContainer.start();
    }

    @Autowired
    CreateUpdatesQueueRepository createUpdatesQueueRepository;

    @Autowired
    EntityManager entityManager;

    @BeforeEach
    void setUp() {
        createUpdatesQueueRepository.deleteAll();
    }

    @Test
    void shouldFindAndLockPendingMessage() {
        CreateUpdatesQueueMessage msg = CreateUpdatesQueueMessage.builder()
            .messageId("msg-" + UUID.randomUUID())
            .messageBody("{\"msg\":\"test\"}")
            .status(QueueMessageStatus.PENDING)
            .createdAt(LocalDateTime.now().minusSeconds(5))
            .retryCount(0)
            .build();

        CreateUpdatesQueueMessage saved = createUpdatesQueueRepository.save(msg);
        assertNotNull(saved.getId());

        List<CreateUpdatesQueueMessage> pending = createUpdatesQueueRepository.findPendingMessages(
            LocalDateTime.now(),
            PageRequest.of(0, 10)
        );
        assertEquals(1, pending.size());

        int locked = createUpdatesQueueRepository.lockMessage(
            saved.getMessageId(),
            "processor-1",
            LocalDateTime.now().plusMinutes(5),
            LocalDateTime.now()
        );
        assertEquals(1, locked);
        flushAndClear();

        CreateUpdatesQueueMessage updated = createUpdatesQueueRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(QueueMessageStatus.PROCESSING, updated.getStatus());
    }

    @Test
    void shouldIncrementRetryWithoutProcessedAtBeforeMaxRetries() {
        CreateUpdatesQueueMessage saved = createUpdatesQueueRepository.save(createProcessingMessage(0));

        createUpdatesQueueRepository.incrementRetryAndMarkFailureIfMax(
            saved.getMessageId(),
            "Temporary failure",
            10,
            null
        );
        flushAndClear();

        CreateUpdatesQueueMessage updated = createUpdatesQueueRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(QueueMessageStatus.PENDING, updated.getStatus());
        assertEquals(Integer.valueOf(1), updated.getRetryCount());
        assertNull(updated.getProcessedAt());
    }

    @Test
    void shouldIncrementRetryAndSetProcessedAtAtMaxRetries() {
        CreateUpdatesQueueMessage saved = createUpdatesQueueRepository.save(createProcessingMessage(9));

        createUpdatesQueueRepository.incrementRetryAndMarkFailureIfMax(
            saved.getMessageId(),
            "Final failure",
            10,
            LocalDateTime.now()
        );
        flushAndClear();

        CreateUpdatesQueueMessage updated = createUpdatesQueueRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(QueueMessageStatus.FAILED, updated.getStatus());
        assertEquals(Integer.valueOf(10), updated.getRetryCount());
        assertNotNull(updated.getProcessedAt());
    }

    private CreateUpdatesQueueMessage createProcessingMessage(int retryCount) {
        return CreateUpdatesQueueMessage.builder()
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
