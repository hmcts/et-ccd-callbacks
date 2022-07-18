package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SubMultipleReferenceEnglandWales;

@Repository
@Transactional
public interface SubMultipleRefEnglandWalesRepository extends JpaRepository<SubMultipleReferenceEnglandWales, Integer> {
    @Procedure("fn_EnglandWalesEthosSubMultipleCaseRefGen")
    String ethosSubMultipleCaseRefGen(int multipleRef, int numberCases);
}