package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SubMultipleReferenceScotland;

@Repository
@Transactional
public interface SubMultipleRefScotlandRepository extends JpaRepository<SubMultipleReferenceScotland, Integer> {
    @Procedure("fn_ScotlandEthosSubMultipleCaseRefGen")
    String ethosSubMultipleCaseRefGen(int multipleRef, int numberCases);
}