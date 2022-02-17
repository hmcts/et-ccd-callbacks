package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefScotlandRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_OFFICE_NUMBER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_OFFICE_NUMBER;

@RunWith(SpringJUnit4ClassRunner.class)
public class MultipleReferenceServiceTest {

    @InjectMocks
    private MultipleReferenceService multipleReferenceService;

    @Mock
    private MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;

    @Mock
    private MultipleRefScotlandRepository multipleRefScotlandRepository;

    @Test
    public void createEnglandWalesReference() {
        when(multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen(1, ENGLANDWALES_CASE_TYPE_ID))
                .thenReturn("00015");
        var expectedReference = ENGLANDWALES_OFFICE_NUMBER + "00015";

        assertEquals(expectedReference, multipleReferenceService.createReference(ENGLANDWALES_BULK_CASE_TYPE_ID, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEnglandWalesReferenceCaseTypeNotFound() {
        when(multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen(1, ENGLANDWALES_CASE_TYPE_ID))
                .thenReturn("00015");

        multipleReferenceService.createReference("invalid-case-type-id", 1);

        fail("Should throw IllegalArgumentException");
    }

    @Test
    public void createScotlandReference() {
        when(multipleRefScotlandRepository.ethosMultipleCaseRefGen(1, SCOTLAND_CASE_TYPE_ID)).thenReturn("00015");
        var expectedReference = SCOTLAND_OFFICE_NUMBER + "00015";

        assertEquals(expectedReference, multipleReferenceService.createReference(SCOTLAND_BULK_CASE_TYPE_ID, 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createScotlandReferenceCaseTypeNotFound() {
        when(multipleRefScotlandRepository.ethosMultipleCaseRefGen(1, SCOTLAND_CASE_TYPE_ID))
                .thenReturn("00015");

        multipleReferenceService.createReference("invalid-case-type-id", 1);

        fail("Should throw IllegalArgumentException");
    }
}