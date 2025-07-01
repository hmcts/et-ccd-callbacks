package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.MultipleReferenceScotland;

@Repository
@Transactional
public interface MultipleRefScotlandRepository extends JpaRepository<MultipleReferenceScotland, Integer> {
    @Query(value = "SELECT fn_ScotlandEthosMultipleCaseRefGen()", nativeQuery = true)
    String ethosMultipleCaseRefGen();
}
