package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.entity.MessageQueueCandidate;

import java.util.List;

public interface MessageQueueCandidateRepository extends CrudRepository<MessageQueueCandidate, Long> {
    List<MessageQueueCandidate> findByPublishedIsNullOrderByTimeStampAsc();
}
