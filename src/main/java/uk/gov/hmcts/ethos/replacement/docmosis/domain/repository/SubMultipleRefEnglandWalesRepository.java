package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SubMultipleReferenceEnglandWales;

@Repository
@Transactional
public interface SubMultipleRefEnglandWalesRepository extends JpaRepository<SubMultipleReferenceEnglandWales, Integer> {
    @Query(value = "SELECT fn_EnglandWalesEthosSubMultipleCaseRefGen(:multipleRef, :numberCases)", nativeQuery = true)
    String ethosSubMultipleCaseRefGen(@Param("multipleRef") int multipleRef,
                                      @Param("numberCases") int numberCases);
}