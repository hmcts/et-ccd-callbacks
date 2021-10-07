package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleRefScotlandRepository;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@RunWith(SpringJUnit4ClassRunner.class)
public class MultipleReferenceServiceTest {

    @Mock
    private MultipleRefScotlandRepository multipleRefScotlandRepository;

    @Test
    public void createScotlandReference() {
        when(multipleRefScotlandRepository.ethosMultipleCaseRefGen(1, SCOTLAND_CASE_TYPE_ID)).thenReturn("00015");
        //String scotlandRef = GLASGOW_OFFICE_NUMBER + "00015";
        //assertEquals(multipleReferenceService.createReference(SCOTLAND_DEV_BULK_CASE_TYPE_ID,1), scotlandRef);
        fail("Unit test needs to be rewritten for new repositories");
    }

}