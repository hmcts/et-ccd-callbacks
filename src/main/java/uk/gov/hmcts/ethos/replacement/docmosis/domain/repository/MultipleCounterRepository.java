package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.MultipleCounter;

import java.util.List;

/**
 * Repository for MultipleCounter entity.
 * Migrated from et-message-handler.
 */
@Repository
@Transactional
public interface MultipleCounterRepository extends JpaRepository<MultipleCounter, Integer> {

    @Query(value = "SELECT fn_persistentq_getnextmultiplecountval(:multipleRef)", nativeQuery = true)
    int persistentQGetNextMultipleCountVal(@Param("multipleRef") String multipleRef);

    List<MultipleCounter> findByMultipleref(String multipleRef);
}
