package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
