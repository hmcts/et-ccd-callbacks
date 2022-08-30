package uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata;

import lombok.Data;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "file_location")
@Data
public class FileLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "serial")
    private Integer id;
    @Enumerated(EnumType.STRING)
    private TribunalOffice tribunalOffice;
    private String code;
    private String name;
}
