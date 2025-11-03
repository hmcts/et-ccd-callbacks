package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SubMultipleReferenceScotland;

@Repository
@Transactional
public interface SubMultipleRefScotlandRepository extends JpaRepository<SubMultipleReferenceScotland, Integer> {
    @Query(value = "SELECT fn_ScotlandEthosSubMultipleCaseRefGen(:multipleRef, :numberCases)", nativeQuery = true)
    String ethosSubMultipleCaseRefGen(@Param("multipleRef") int multipleRef,
                                      @Param("numberCases") int numberCases);

}