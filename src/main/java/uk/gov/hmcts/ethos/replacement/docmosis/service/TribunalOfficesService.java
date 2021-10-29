package uk.gov.hmcts.ethos.replacement.docmosis.service;

import joptsimple.internal.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;
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

    public TribunalOffice getTribunalOffice(String officeName, String managingOffice) {

        if (!officeName.equals(SCOTLAND_CASE_TYPE_ID)) { //temporary fix until scotland ccd fil is not ready.
            return TribunalOffice.valueOfOfficeName(officeName);
        }
        else {
            return getScottishTribunalOffice(managingOffice);
        }
    }

    private TribunalOffice getScottishTribunalOffice(String managingOffice) {
        if (!Strings.isNullOrEmpty(managingOffice)) {
            switch (managingOffice) {
                case EDINBURGH_OFFICE:
                    return TribunalOffice.EDINBURGH;
                case ABERDEEN_OFFICE:
                    return TribunalOffice.ABERDEEN;
                case DUNDEE_OFFICE:
                    return TribunalOffice.DUNDEE;
                default:
                    log.warn(String.format("Unexpected managing office %s therefore defaulting to %s", managingOffice, TribunalOffice.GLASGOW));
                    return TribunalOffice.GLASGOW;
            }
        } else {
            return TribunalOffice.GLASGOW;
        }
    }

    public ContactDetails getTribunalContactDetails(String officeName, String managingOffice) {
        var tribunalName = getTribunalOffice(officeName, managingOffice);
        return config.getContactDetails().get(tribunalName);
    }

}

