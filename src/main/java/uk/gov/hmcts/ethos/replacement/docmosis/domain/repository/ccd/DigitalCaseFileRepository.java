package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.ccd;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ccd.DigitalCaseFile;

public interface DigitalCaseFileRepository extends JpaRepository<DigitalCaseFile, Long> {
}
