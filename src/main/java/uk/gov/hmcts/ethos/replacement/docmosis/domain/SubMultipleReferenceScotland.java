package uk.gov.hmcts.ethos.replacement.docmosis.domain;

import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@NoArgsConstructor
@Table(name = "submultiple_reference_scotland")
public class SubMultipleReferenceScotland extends SubMultipleReference {
}
