package uk.gov.hmcts.ethos.replacement.docmosis.service;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.VenueAddress;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.VenueAddressReaderException;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ABERDEEN_OFFICE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.DUNDEE_OFFICE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EDINBURGH_OFFICE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.GLASGOW_OFFICE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Slf4j
@Service
public class VenueAddressReaderService {
    private final VenueAddressesService venueAddressesService;

    public VenueAddressReaderService(VenueAddressesService venueAddressesService) {
        this.venueAddressesService = venueAddressesService;
    }

    public String getVenueAddress(HearingType hearingType, String caseTypeId, String managingOffice) {
        String hearingVenue = getHearingVenue(hearingType, caseTypeId);
        List<VenueAddress> venueAddressList  = venueAddressesService.getTribunalVenueAddresses(managingOffice);

        if (CollectionUtils.isEmpty(venueAddressList)) {
            throw new VenueAddressReaderException("Venue address list not found for " + managingOffice);
        }

         VenueAddress venueAddressItem = venueAddressList.stream()
                .filter(v -> v.getVenue().equals(hearingVenue))
                .findAny()
                .orElse(null);

        return venueAddressItem != null
                ? StringUtils.defaultIfBlank(venueAddressItem.getAddress(), hearingVenue)
                : hearingVenue;
    }

    private String getHearingVenue(HearingType hearingType, String caseTypeId) {
        if (ENGLANDWALES_CASE_TYPE_ID.equals(caseTypeId)) {
            return hearingType.getHearingVenue().getValue().getLabel();
        } else if (SCOTLAND_CASE_TYPE_ID.equals(caseTypeId)) {
            return getHearingVenueScotland(hearingType);
        } else {
            throw new IllegalArgumentException("Unexpected case type id " + caseTypeId);
        }
    }

    private String getHearingVenueScotland(HearingType hearingType) {
        var venue = hearingType.getHearingVenueScotland();
        if (StringUtils.isBlank(venue)) {
            log.error("No office found for hearing {}", hearingType.getHearingNumber());
            return null;
        }

        switch (venue) {
            case GLASGOW_OFFICE:
                return hearingType.getHearingGlasgow().getSelectedLabel();
            case ABERDEEN_OFFICE:
                return hearingType.getHearingAberdeen().getSelectedLabel();
            case DUNDEE_OFFICE:
                return hearingType.getHearingDundee().getSelectedLabel();
            case EDINBURGH_OFFICE:
                return hearingType.getHearingEdinburgh().getSelectedLabel();
            default:
                log.error("No {} venue found", venue);
                return null;
        }
    }
}
