package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefBristolRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefLeedsRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefLondonCentralRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefLondonEastRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefLondonSouthRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefManchesterRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefMidlandsEastRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefMidlandsWestRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefNewcastleRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefScotlandRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefWatfordRepository;
import java.time.LocalDate;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_DEV_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_USERS_CASE_TYPE_ID;





import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_DEV_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_USERS_CASE_TYPE_ID;

@Slf4j
@RequiredArgsConstructor
@Service("singleReferenceService")
public class SingleReferenceService {

    private final SingleRefManchesterRepository singleRefManchesterRepository;
    private final SingleRefScotlandRepository singleRefScotlandRepository;
    private final SingleRefLeedsRepository singleRefLeedsRepository;
    private final SingleRefMidlandsWestRepository singleRefMidlandsWestRepository;
    private final SingleRefMidlandsEastRepository singleRefMidlandsEastRepository;
    private final SingleRefBristolRepository singleRefBristolRepository;
    private final SingleRefWalesRepository singleRefWalesRepository;
    private final SingleRefNewcastleRepository singleRefNewcastleRepository;
    private final SingleRefWatfordRepository singleRefWatfordRepository;
    private final SingleRefLondonCentralRepository singleRefLondonCentralRepository;
    private final SingleRefLondonSouthRepository singleRefLondonSouthRepository;
    private final SingleRefLondonEastRepository singleRefLondonEastRepository;

    public synchronized String createReference(String caseTypeId, int numberCases) {
//        var currentYear = String.valueOf(LocalDate.now().getYear());
//        switch (caseTypeId) {
//            case ENGLANDWALES_DEV_CASE_TYPE_ID:
//            case ENGLANDWALES_USERS_CASE_TYPE_ID:
//            case ENGLANDWALES_CASE_TYPE_ID:
//                return generateOfficeReference(singleRefManchesterRepository, currentYear, numberCases,
//                        ENGLANDWALES_OFFICE_NUMBER, ENGLANDWALES_CASE_TYPE_ID);
//            case SCOTLAND_DEV_CASE_TYPE_ID:
//            case SCOTLAND_USERS_CASE_TYPE_ID:
//            case SCOTLAND_CASE_TYPE_ID:
//                return generateOfficeReference(singleRefScotlandRepository, currentYear, numberCases,
//                        GLASGOW_OFFICE_NUMBER, SCOTLAND_CASE_TYPE_ID);
//
//
//            default:
//                return generateOfficeReference(singleRefLeedsRepository, currentYear, numberCases,
//                        LEEDS_OFFICE_NUMBER, ENGLANDWALES_CASE_TYPE_ID);
//        }
       throw new UnsupportedOperationException();
    }

    private String generateOfficeReference(SingleRefRepository referenceRepository, String currentYear,
                                           int numberCases, String officeNumber, String officeName) {
        return officeNumber + referenceRepository.ethosCaseRefGen(numberCases, Integer.parseInt(currentYear),
                officeName);
    }

}
