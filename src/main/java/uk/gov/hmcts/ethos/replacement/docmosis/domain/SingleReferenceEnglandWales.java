package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@NoArgsConstructor
@Table(name = "single_reference_englandwales")
public class SingleReferenceEnglandWales extends SingleReference {
}
