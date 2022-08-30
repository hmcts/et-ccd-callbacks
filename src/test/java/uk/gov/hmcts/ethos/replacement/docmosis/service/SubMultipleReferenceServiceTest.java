package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefScotlandRepository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.et.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.et.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

@RunWith(SpringJUnit4ClassRunner.class)
public class SubMultipleReferenceServiceTest {

    @InjectMocks
    private SubMultipleReferenceService subMultipleReferenceService;
    @Mock
    private SubMultipleRefEnglandWalesRepository subMultipleRefEnglandWalesRepository;
    @Mock
    private SubMultipleRefScotlandRepository subMultipleRefScotlandRepository;

    @Test
    public void createManchesterReference() {
        var multipleRef = "6000001";
        when(subMultipleRefEnglandWalesRepository.ethosSubMultipleCaseRefGen(Integer.parseInt(multipleRef), 1))
                .thenReturn(multipleRef + "/1");
        var expectedRef = multipleRef + "/1";

        assertEquals(expectedRef, subMultipleReferenceService.createReference(ENGLANDWALES_BULK_CASE_TYPE_ID,
                multipleRef, 1));
    }

    @Test
    public void createScotlandReference() {
        var multipleRef = "8000001";
        when(subMultipleRefScotlandRepository.ethosSubMultipleCaseRefGen(Integer.parseInt(multipleRef), 1))
                .thenReturn(multipleRef + "/1");
        var expectedRef = multipleRef + "/1";

        assertEquals(expectedRef, subMultipleReferenceService.createReference(SCOTLAND_BULK_CASE_TYPE_ID,
                multipleRef, 1));
    }
}