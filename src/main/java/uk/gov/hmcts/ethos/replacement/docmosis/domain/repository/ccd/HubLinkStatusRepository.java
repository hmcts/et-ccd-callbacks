package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.HubLinkStatus;

public interface HubLinkStatusRepository extends JpaRepository<HubLinkStatus, Long> {
}
