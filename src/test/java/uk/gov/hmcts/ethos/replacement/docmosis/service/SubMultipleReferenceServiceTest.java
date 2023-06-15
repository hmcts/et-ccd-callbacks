package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefScotlandRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class SubMultipleReferenceServiceTest {

    @InjectMocks
    private SubMultipleReferenceService subMultipleReferenceService;
    @Mock
    private SubMultipleRefEnglandWalesRepository subMultipleRefEnglandWalesRepository;
    @Mock
    private SubMultipleRefScotlandRepository subMultipleRefScotlandRepository;

    @Test
    void createManchesterReference() {
        String multipleRef = "6000001";
        when(subMultipleRefEnglandWalesRepository.ethosSubMultipleCaseRefGen(Integer.parseInt(multipleRef), 1))
                .thenReturn(multipleRef + "/1");
        String expectedRef = multipleRef + "/1";

        assertEquals(expectedRef, subMultipleReferenceService.createReference(ENGLANDWALES_BULK_CASE_TYPE_ID,
                multipleRef, 1));
    }

    @Test
    void createScotlandReference() {
        String multipleRef = "8000001";
        when(subMultipleRefScotlandRepository.ethosSubMultipleCaseRefGen(Integer.parseInt(multipleRef), 1))
                .thenReturn(multipleRef + "/1");
        String expectedRef = multipleRef + "/1";

        assertEquals(expectedRef, subMultipleReferenceService.createReference(SCOTLAND_BULK_CASE_TYPE_ID,
                multipleRef, 1));
    }
}
