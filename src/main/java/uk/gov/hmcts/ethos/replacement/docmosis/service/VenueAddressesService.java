package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.config.VenueAddressesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.VenueAddress;

import java.util.List;

@Service
@Slf4j
public class VenueAddressesService {
    private final VenueAddressesConfiguration config;

    public VenueAddressesService(VenueAddressesConfiguration config) {
        this.config = config;
    }

    public TribunalOffice getTribunalOffice(String officeName) {
        return TribunalOffice.valueOfOfficeName(officeName);
    }

    public List<VenueAddress> getTribunalVenueAddresses(String officeName) {
        TribunalOffice tribunalName = getTribunalOffice(officeName);
        return config.getTribunalOffices().get(tribunalName);
    }
}

