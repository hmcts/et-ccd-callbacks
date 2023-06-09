package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;

/**
 Listed Hearing Data object for sorting purposes.
 */
@AllArgsConstructor
@Data
public class ListedHearingData {

    private String hearingType;
    private DynamicFixedListType hearingVenue;
    private String hearingVenueScotland;
    private String listedId;
    private String listedDate;
}
