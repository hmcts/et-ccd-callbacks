package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * JPA entity for multiple_errors table.
 * Migrated from et-message-handler.
 */
@Entity
@Data
@Table(name = "multiple_errors")
public class MultipleErrors {

    @Id
    private Long id;
    protected String multipleref;
    protected String ethoscaseref;
    protected String description;

    @Override
    public String toString() {
        return "Ethos Case Reference: '" + this.ethoscaseref
            + "', Description: '" + this.description + "'";
    }
}
