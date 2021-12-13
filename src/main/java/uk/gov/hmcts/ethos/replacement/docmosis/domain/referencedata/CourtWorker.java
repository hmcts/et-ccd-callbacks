package uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;


@Entity()
@Table(name = "court_worker")
@Data
public class CourtWorker {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Enumerated(EnumType.STRING)
    private TribunalOffice tribunalOffice;
    @Enumerated(EnumType.STRING)
    private CourtWorkerType type;
    private String code;
    private String name;
}

