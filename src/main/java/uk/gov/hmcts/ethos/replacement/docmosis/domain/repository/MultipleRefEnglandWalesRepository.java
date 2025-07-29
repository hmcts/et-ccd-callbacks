package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.MultipleReferenceEnglandWales;

@Repository
@Transactional
public interface MultipleRefEnglandWalesRepository extends JpaRepository<MultipleReferenceEnglandWales, Integer> {
    @Query(value = "SELECT fn_EnglandWalesEthosMultipleCaseRefGen()", nativeQuery = true)
    String ethosMultipleCaseRefGen();
}