package uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Records that the claimant has viewed a notification, tribunal response or application response.
 * Kept out of the case data blob so marking items as viewed cannot conflict with other case updates.
 */
@Entity
@Table(name = "notification_view")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationView {

    @EmbeddedId
    private Key id;

    @Column(name = "viewed_at", insertable = false, updatable = false)
    private LocalDateTime viewedAt;

    @Embeddable
    @Getter
    @EqualsAndHashCode
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Key implements Serializable {

        private static final long serialVersionUID = 1L;

        @Column(name = "case_reference")
        private Long caseReference;

        @Column(name = "item_id")
        private String itemId;
    }
}
