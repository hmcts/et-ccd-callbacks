package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.SingleReferenceEnglandWales;

@Repository
@Transactional
public interface SingleRefEnglandWalesRepository extends JpaRepository<SingleReferenceEnglandWales, Integer> {
    @Procedure("fn_EnglandWalesEthosCaseRefGen")
    String ethosCaseRefGen(int currentYear);
}