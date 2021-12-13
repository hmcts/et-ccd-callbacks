package uk.gov.hmcts.ethos.replacement.docmosis.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Venue;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import java.util.List;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Integer> {
    List<Venue> findByTribunalOffice(TribunalOffice tribunalOffice);
}
