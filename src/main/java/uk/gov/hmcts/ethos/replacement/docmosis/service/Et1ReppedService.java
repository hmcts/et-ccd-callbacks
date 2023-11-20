package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import uk.gov.dwp.regex.InvalidPostcodeException;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.service.PostcodeToOfficeService;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Strings.isNullOrEmpty;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@Service
@Slf4j
@RequiredArgsConstructor
public class Et1ReppedService {
    private final PostcodeToOfficeService postcodeToOfficeService;
    private final List<TribunalOffice> liveTribunalOffices = List.of(TribunalOffice.LEEDS,
            TribunalOffice.MIDLANDS_EAST, TribunalOffice.BRISTOL, TribunalOffice.GLASGOW);

    /**
     * Validates the postcode.
     * @param caseData the case data
     * @return YES if the postcode is valid, NO otherwise
     * @throws InvalidPostcodeException if the postcode is invalid
     */
    public String validatePostcode(CaseData caseData) throws InvalidPostcodeException {
        if (ObjectUtils.isEmpty(caseData.getEt1ReppedTriageAddress())
                || isNullOrEmpty(caseData.getEt1ReppedTriageAddress().getPostCode())) {
            return NO;
        }

        Optional<TribunalOffice> office = postcodeToOfficeService.getTribunalOfficeFromPostcode(
                caseData.getEt1ReppedTriageAddress().getPostCode());
        if (office.isEmpty() || !liveTribunalOffices.contains(office.get())) {
            return NO;
        }

        return YES;
    }
}
