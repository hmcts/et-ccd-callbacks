package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    @Query(value = "SELECT fn_persistentq_logmultipleerror(:multipleRef, :ethosCaseRef, :description)",
        nativeQuery = true)
    String persistentQLogMultipleError(String multipleRef, String ethosCaseRef, String description);

    List<MultipleErrors> findByMultipleref(String multipleRef);
}
