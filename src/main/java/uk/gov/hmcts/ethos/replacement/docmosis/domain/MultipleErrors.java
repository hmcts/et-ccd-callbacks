package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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
