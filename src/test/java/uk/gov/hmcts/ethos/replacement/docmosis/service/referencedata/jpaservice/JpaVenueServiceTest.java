package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Venue;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.VenueRepository;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JpaVenueServiceTest {
    @Test
    public void testGetVenues() {
        TribunalOffice tribunalOffice = TribunalOffice.BRISTOL;
        List<Venue> venues = List.of(
                createVenue("venue1", "Venue 1"),
                createVenue("venue2", "Venue 2"),
                createVenue("venue3", "Venue 3"));
        VenueRepository venueRepository = mock(VenueRepository.class);
        when(venueRepository.findByTribunalOffice(tribunalOffice)).thenReturn(venues);

        JpaVenueService venueService = new JpaVenueService(venueRepository);
        List<DynamicValueType> values = venueService.getVenues(tribunalOffice);

        assertEquals(3, values.size());
        verifyValue(values.get(0), "venue1", "Venue 1");
        verifyValue(values.get(1), "venue2", "Venue 2");
        verifyValue(values.get(2), "venue3", "Venue 3");
    }

    private Venue createVenue(String code, String name) {
        Venue venue = new Venue();
        venue.setCode(code);
        venue.setName(name);
        return venue;
    }

    private void verifyValue(DynamicValueType value, String expectedCode, String expectedLabel) {
        assertEquals(expectedCode, value.getCode());
        assertEquals(expectedLabel, value.getLabel());
    }
}
