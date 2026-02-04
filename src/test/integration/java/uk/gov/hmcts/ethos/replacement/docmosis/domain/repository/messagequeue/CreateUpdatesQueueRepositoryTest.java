package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.CreateUpdatesQueueMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.messagequeue.QueueMessageStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.EtCosPostgresqlContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class CreateUpdatesQueueRepositoryTest {

    @Autowired
    CreateUpdatesQueueRepository createUpdatesQueueRepository;

    @ClassRule
    public static final PostgreSQLContainer postgreSQLContainer = EtCosPostgresqlContainer.getInstance();

    @Test
    public void shouldFindAndLockPendingMessage() {
        CreateUpdatesQueueMessage msg = CreateUpdatesQueueMessage.builder()
            .messageId("msg-1")
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

        CreateUpdatesQueueMessage updated = createUpdatesQueueRepository.findById(saved.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(QueueMessageStatus.PROCESSING, updated.getStatus());
    }
}
