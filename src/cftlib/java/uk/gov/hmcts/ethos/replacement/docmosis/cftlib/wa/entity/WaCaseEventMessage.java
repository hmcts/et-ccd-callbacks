package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "wa_case_event_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaCaseEventMessage {

    @Id
    @Column(nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sequence;

    @Column(name = "message_id", nullable = false)
    private String messageId;

    @Column(name = "case_id", nullable = false, columnDefinition = "text")
    private String caseId;

    @Column(name = "event_timestamp")
    private LocalDateTime eventTimestamp;

    @Column(name = "from_dlq", nullable = false)
    private boolean fromDlq;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private WaCaseEventMessageState state;

    @Column(name = "message_properties", columnDefinition = "jsonb")
    @Convert(disableConversion = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode messageProperties;

    @Column(name = "message_content", columnDefinition = "text")
    private String messageContent;

    @Column(name = "received", nullable = false)
    private LocalDateTime received;

    @Column(name = "delivery_count", nullable = false)
    private Integer deliveryCount;

    @Column(name = "hold_until")
    private LocalDateTime holdUntil;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;
}
