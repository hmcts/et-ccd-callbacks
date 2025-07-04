package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Table(name = "single_reference_scotland")
public class SingleReferenceScotland extends SingleReference {
}
