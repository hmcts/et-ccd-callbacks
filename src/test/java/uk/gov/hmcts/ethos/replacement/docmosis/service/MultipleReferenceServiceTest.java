package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefScotlandRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
public class MultipleReferenceServiceTest {

    @InjectMocks
    private MultipleReferenceService multipleReferenceService;

    @Mock
    private MultipleRefEnglandWalesRepository multipleRefEnglandWalesRepository;

    @Mock
    private MultipleRefScotlandRepository multipleRefScotlandRepository;

    @Test
    public void createEnglandWalesReference() {
        String expectedReference = "6000001";
        when(multipleRefEnglandWalesRepository.ethosMultipleCaseRefGen()).thenReturn(expectedReference);

        assertEquals(expectedReference, multipleReferenceService.createReference(ENGLANDWALES_BULK_CASE_TYPE_ID));
    }

    @Test
    public void createEnglandWalesReferenceCaseTypeNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                multipleReferenceService.createReference("invalid-case-type-id")
        );
    }

    @Test
    public void createScotlandReference() {
        String expectedReference = "8000001";
        when(multipleRefScotlandRepository.ethosMultipleCaseRefGen()).thenReturn(expectedReference);

        assertEquals(expectedReference, multipleReferenceService.createReference(SCOTLAND_BULK_CASE_TYPE_ID));
    }

    @Test
    public void createScotlandReferenceCaseTypeNotFound() {
        assertThrows(IllegalArgumentException.class, () ->
                multipleReferenceService.createReference("invalid-case-type-id")
        );
    }
}
