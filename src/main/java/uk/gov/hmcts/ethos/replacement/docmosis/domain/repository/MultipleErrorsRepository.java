package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.MultipleErrors;

import java.util.List;

/**
 * Repository for MultipleErrors entity.
 * Migrated from et-message-handler.
 */
@Repository
@Transactional
public interface MultipleErrorsRepository extends JpaRepository<MultipleErrors, Integer> {

    @Procedure("fn_persistentq_logmultipleerror")
    String persistentQLogMultipleError(String multipleRef, String ethosCaseRef, String description);

    List<MultipleErrors> findByMultipleref(String multipleRef);
}
