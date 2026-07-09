package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.entity.MessageQueueCandidate;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.entity.WaCaseEventMessage;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.entity.WaCaseEventMessageState;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.repository.MessageQueueCandidateRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.repository.WaCaseEventMessageRepository;

import java.time.LocalDateTime;

@Service
@Transactional
@ConditionalOnProperty(name = "et.work-allocation.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
class MessageQueueCandidateHandler {

    private final MessageQueueCandidateRepository messageQueueCandidateRepository;
    private final WaCaseEventMessageRepository waCaseEventMessageRepository;
    private final ObjectMapper objectMapper;

    public void handle(MessageQueueCandidate candidate) {
        log.info("Processing message queue candidate with ID: {}", candidate.getId());
        try {
            WaCaseEventMessage message = buildWaCaseEventMessage(candidate);
            waCaseEventMessageRepository.save(message);
            candidate.setPublished(LocalDateTime.now());
            messageQueueCandidateRepository.save(candidate);
            log.info("Processed and published message queue candidate with ID: {}", candidate.getId());
        } catch (JsonProcessingException e) {
            log.error("Error while processing the message queue candidate with ID: {}", candidate.getId(), e);
        }
    }

    private WaCaseEventMessage buildWaCaseEventMessage(MessageQueueCandidate candidate)
            throws JsonProcessingException {
        String messageContent = objectMapper.writeValueAsString(candidate.getMessageInformation());
        return WaCaseEventMessage.builder()
                .messageId(String.valueOf(candidate.getId()))
                .caseId(candidate.getMessageInformation().getCaseId())
                .eventTimestamp(candidate.getTimeStamp())
                .fromDlq(false)
                .state(WaCaseEventMessageState.READY)
                .messageContent(messageContent)
                .received(LocalDateTime.now())
                .deliveryCount(0)
                .retryCount(0)
                .build();
    }
}
