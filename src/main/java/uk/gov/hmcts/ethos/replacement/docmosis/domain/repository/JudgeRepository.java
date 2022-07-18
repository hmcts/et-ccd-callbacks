package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;

import java.util.List;

@Repository
public interface JudgeRepository extends JpaRepository<Judge, Integer> {

    List<Judge> findById(int id);

    List<Judge> findByTribunalOffice(TribunalOffice tribunalOffice);

    List<Judge> findByTribunalOfficeOrderById(TribunalOffice tribunalOffice);

    boolean existsByCodeAndTribunalOffice(String code, TribunalOffice tribunalOffice);

    boolean existsByNameAndTribunalOffice(String name, TribunalOffice tribunalOffice);

    boolean existsByTribunalOfficeAndNameAndIdIsNot(TribunalOffice tribunalOffice, String name, int id);

}
