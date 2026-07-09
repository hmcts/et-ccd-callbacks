package uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.cftlib.wa.entity.WaCaseEventMessage;

public interface WaCaseEventMessageRepository extends CrudRepository<WaCaseEventMessage, Long> {
}
