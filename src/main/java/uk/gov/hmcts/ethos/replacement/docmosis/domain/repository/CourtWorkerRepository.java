package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;

import java.util.Collection;
import java.util.List;

@Repository
public interface CourtWorkerRepository extends JpaRepository<CourtWorker, Integer> {
    List<CourtWorker> findById(int id);

    List<CourtWorker> findByTribunalOfficeAndType(TribunalOffice tribunalOffice, CourtWorkerType courtWorkerType);

    List<CourtWorker> findByTribunalOfficeAndTypeOrderByNameAsc(TribunalOffice tribunalOffices,
                                                               CourtWorkerType courtWorkerType);

    List<CourtWorker> findByTribunalOfficeInAndTypeOrderByName(Collection<TribunalOffice> tribunalOffices,
                                                   CourtWorkerType courtWorkerType);

    CourtWorker findByCodeAndTribunalOfficeAndType(String code, TribunalOffice tribunalOffice, CourtWorkerType type);

    boolean existsByTribunalOfficeAndTypeAndCode(TribunalOffice tribunalOffice, CourtWorkerType courtWorkerType,
        String code);

    boolean existsByTribunalOfficeAndTypeAndName(TribunalOffice tribunalOffice, CourtWorkerType courtWorkerType,
        String name);

}

