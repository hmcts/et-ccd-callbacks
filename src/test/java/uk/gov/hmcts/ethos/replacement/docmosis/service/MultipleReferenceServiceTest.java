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
import static uk.gov.hmcts.et.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.et.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

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
        var expectedReference = "6000001";
        when(multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen()).thenReturn(expectedReference);

        assertEquals(expectedReference, multipleReferenceService.createReference(ENGLANDWALES_BULK_CASE_TYPE_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createEnglandWalesReferenceCaseTypeNotFound() {
        multipleReferenceService.createReference("invalid-case-type-id");

        fail("Should throw IllegalArgumentException");
    }

    @Test
    public void createScotlandReference() {
        var expectedReference = "8000001";
        when(multipleRefScotlandRepository.ethosMultipleCaseRefGen()).thenReturn(expectedReference);

        assertEquals(expectedReference, multipleReferenceService.createReference(SCOTLAND_BULK_CASE_TYPE_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createScotlandReferenceCaseTypeNotFound() {
        multipleReferenceService.createReference("invalid-case-type-id");

        fail("Should throw IllegalArgumentException");
    }
}