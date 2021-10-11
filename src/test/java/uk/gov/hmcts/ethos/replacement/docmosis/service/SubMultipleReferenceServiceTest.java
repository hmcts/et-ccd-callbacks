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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_OFFICE_NUMBER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_OFFICE_NUMBER;

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
        var multipleRef = ENGLANDWALES_OFFICE_NUMBER + "1000";
        when(subMultipleRefEnglandWalesRepository.ethosSubMultipleCaseRefGen(Integer.parseInt(multipleRef), 1,
                ENGLANDWALES_CASE_TYPE_ID)).thenReturn(multipleRef + "/1");
        var expectedRef = multipleRef + "/1";

        assertEquals(expectedRef, subMultipleReferenceService.createReference(ENGLANDWALES_BULK_CASE_TYPE_ID,
                multipleRef, 1));
    }

    @Test
    public void createScotlandReference() {
        var multipleRef = SCOTLAND_OFFICE_NUMBER + "1000";
        when(subMultipleRefScotlandRepository.ethosSubMultipleCaseRefGen(Integer.parseInt(multipleRef), 1,
                SCOTLAND_CASE_TYPE_ID)).thenReturn(multipleRef + "/1");
        var expectedRef = multipleRef + "/1";

        assertEquals(expectedRef, subMultipleReferenceService.createReference(SCOTLAND_BULK_CASE_TYPE_ID,
                multipleRef, 1));
    }
}