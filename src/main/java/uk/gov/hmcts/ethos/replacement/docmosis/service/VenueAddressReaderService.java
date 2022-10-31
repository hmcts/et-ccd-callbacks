package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.types.HearingType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.VenueAddress;
import uk.gov.hmcts.ethos.replacement.docmosis.service.exceptions.VenueAddressReaderException;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@Slf4j
@Service
@SuppressWarnings({"PMD.ConfusingTernary", "PMD.PrematureDeclaration", "PMD.GodClass", "PMD.CognitiveComplexity",
    "PMD.TooManyMethods"})
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
            throw new VenueAddressReaderException("No office found for hearing " + hearingType.getHearingNumber());
        }

        final TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(venue);
        switch (tribunalOffice) {
            case GLASGOW:
                return hearingType.getHearingGlasgow().getSelectedLabel();
            case ABERDEEN:
                return hearingType.getHearingAberdeen().getSelectedLabel();
            case DUNDEE:
                return hearingType.getHearingDundee().getSelectedLabel();
            case EDINBURGH:
                return hearingType.getHearingEdinburgh().getSelectedLabel();
            default:
                // Should never be thrown since TribunalOffice will catch the exception first
                throw new VenueAddressReaderException("No venue found for " + venue);
        }
    }
}
