package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata;

import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;

import java.util.List;

public interface VenueService {
    List<DynamicValueType> getVenues(TribunalOffice tribunalOffice);
}
