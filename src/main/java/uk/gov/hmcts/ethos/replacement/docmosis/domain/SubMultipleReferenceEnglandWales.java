package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@NoArgsConstructor
@Table(name = "sub_multiple_reference_englandwales")
public class SubMultipleReferenceEnglandWales extends SubMultipleReference {
}
