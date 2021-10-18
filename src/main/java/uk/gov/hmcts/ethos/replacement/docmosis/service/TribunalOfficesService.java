package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.config.TribunalOfficesConfiguration;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.ContactDetails;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

@Service
@Slf4j
public class TribunalOfficesService {
    private final TribunalOfficesConfiguration config;

    public TribunalOfficesService(TribunalOfficesConfiguration config) {
        this.config = config;
    }

    public TribunalOffice getTribunalOffice(String officeName) {
        return TribunalOffice.valueOfOfficeName(officeName);
    }

    public ContactDetails getTribunalContactDetails(String officeName) {
        var tribunalName = getTribunalOffice(officeName);
        return config.getContactDetails().get(tribunalName);
    }

}

