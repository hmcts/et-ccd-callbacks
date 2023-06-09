package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import lombok.Data;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;

/**
 Listed Hearing Data object for sorting purposes
 */
@Data
public class ListedHearingData {

    private String hearingType;
    private DynamicFixedListType hearingVenue;
    private String hearingVenueScotland;
    private String listedId;
    private String listedDate;

    public ListedHearingData(String hearingType, DynamicFixedListType hearingVenue, String hearingVenueScotland, String listedId, String listedDate) {
        this.hearingType = hearingType;
        this.hearingVenue = hearingVenue;
        this.hearingVenueScotland = hearingVenueScotland;
        this.listedId = listedId;
        this.listedDate = listedDate;
    }
}
