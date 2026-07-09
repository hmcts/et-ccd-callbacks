package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.ColumnTransformer;

import java.time.LocalDateTime;

@Table(schema = "ccd", name = "message_queue_candidates")
@Entity
@Data
public class MessageQueueCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String messageType;
    private LocalDateTime timeStamp;
    private LocalDateTime published;
    @Column(columnDefinition = "jsonb")
    @ColumnTransformer(write = "?::jsonb")
    @Convert(converter = MessageInformationConverter.class)
    private MessageInformation messageInformation;
}
