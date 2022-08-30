package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Venue;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Integer> {
    List<Venue> findByTribunalOffice(TribunalOffice tribunalOffice);
}
