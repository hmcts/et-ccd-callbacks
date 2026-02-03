package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

/**
 * JPA entity for multiple_counter table.
 * Migrated from et-message-handler.
 */
@Entity
@NoArgsConstructor
@Table(name = "multiple_counter")
public class MultipleCounter {

    @Id
    protected String multipleref;
    protected Integer counter;
}
