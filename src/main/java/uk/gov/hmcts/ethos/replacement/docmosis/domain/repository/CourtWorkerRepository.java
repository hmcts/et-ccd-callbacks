package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;

@Repository
public interface CourtWorkerRepository extends JpaRepository<CourtWorker, Integer> {
    List<CourtWorker> findByTribunalOfficeAndType(TribunalOffice tribunalOffice, CourtWorkerType courtWorkerType);

    List<CourtWorker> findByTribunalOfficeInAndTypeOrderByName(Collection<TribunalOffice> tribunalOffices,
                                                   CourtWorkerType courtWorkerType);

    boolean existsByTribunalOfficeAndTypeAndCode(TribunalOffice tribunalOffice, CourtWorkerType courtWorkerType,
        String code);

    boolean existsByTribunalOfficeAndTypeAndName(TribunalOffice tribunalOffice, CourtWorkerType courtWorkerType,
        String name);

    @Transactional
    @Modifying
    // Query to be made into a function
    @Query("UPDATE CourtWorker c SET c.name = ?1 WHERE c.id = ?2")
    void updateCourtWorkerName(String courtWorkerNameUpdate, Integer id);

}

