package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SingleReferenceEnglandWales;

@Repository
@Transactional
public interface SingleRefEnglandWalesRepository extends JpaRepository<SingleReferenceEnglandWales, Integer> {
    @Query(value = "SELECT fn_EnglandWalesEthosCaseRefGen(:currentYear)", nativeQuery = true)
    String ethosCaseRefGen(@Param("currentYear") int currentYear);
}
