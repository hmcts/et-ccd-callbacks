package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.*;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefBristolRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefLeedsRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefLondonCentralRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefLondonEastRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefLondonSouthRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefMidlandsEastRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefMidlandsWestRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefNewcastleRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefScotlandRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefWatfordRepository;

@Slf4j
@RequiredArgsConstructor
@Service("multipleReferenceService")
public class MultipleReferenceService {

    private final MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;
    private final MultipleRefScotlandRepository multipleRefScotlandRepository;
    private final MultipleRefLeedsRepository multipleRefLeedsRepository;
    private final MultipleRefMidlandsWestRepository multipleRefMidlandsWestRepository;
    private final MultipleRefMidlandsEastRepository multipleRefMidlandsEastRepository;
    private final MultipleRefBristolRepository multipleRefBristolRepository;
    private final MultipleRefWalesRepository multipleRefWalesRepository;
    private final MultipleRefNewcastleRepository multipleRefNewcastleRepository;
    private final MultipleRefWatfordRepository multipleRefWatfordRepository;
    private final MultipleRefLondonCentralRepository multipleRefLondonCentralRepository;
    private final MultipleRefLondonSouthRepository multipleRefLondonSouthRepository;
    private final MultipleRefLondonEastRepository multipleRefLondonEastRepository;

    public synchronized String createReference(String caseTypeId, int numberCases) {
//        switch (caseTypeId) {
//            case ENGLANDWALES_DEV_BULK_CASE_TYPE_ID:
//            case ENGLANDWALES_USERS_BULK_CASE_TYPE_ID:
//            case ENGLANDWALES_BULK_CASE_TYPE_ID:
//                return generateOfficeReference(multipleRefEnglandWalesRepository, numberCases,
//                        ENGLANDWALES_OFFICE_NUMBER, ENGLANDWALES_CASE_TYPE_ID);
//            case SCOTLAND_DEV_BULK_CASE_TYPE_ID:
//            case SCOTLAND_USERS_BULK_CASE_TYPE_ID:
//            case SCOTLAND_BULK_CASE_TYPE_ID:
//                return generateOfficeReference(multipleRefScotlandRepository, numberCases,
//                        GLASGOW_OFFICE_NUMBER, SCOTLAND_CASE_TYPE_ID);
//            default:
//                return generateOfficeReference(multipleRefLeedsRepository, numberCases,
//                        ENGLANDWALES_OFFICE_NUMBER, ENGLANDWALES_CASE_TYPE_ID);
//        }
        throw new UnsupportedOperationException();
    }

    private String generateOfficeReference(MultipleRefRepository referenceRepository, int numberCases,
                                           String officeNumber, String officeName) {
        return officeNumber + referenceRepository.ethosMultipleCaseRefGen(numberCases, officeName);
    }

}
