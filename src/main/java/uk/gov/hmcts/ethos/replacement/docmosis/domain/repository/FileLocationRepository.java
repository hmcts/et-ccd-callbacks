package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.FileLocation;

import java.util.List;

public interface FileLocationRepository extends JpaRepository<FileLocation, Integer> {
    List<FileLocation> findByTribunalOffice(TribunalOffice tribunalOffice);
}
