package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SubMultipleRefScotlandRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.tribunaloffice.TribunalOffice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_DEV_BULK_CASE_TYPE_ID;

@RunWith(SpringJUnit4ClassRunner.class)
public class SubMultipleReferenceServiceTest {

    @InjectMocks
    private SubMultipleReferenceService subMultipleReferenceService;
    @Mock
    private SubMultipleRefScotlandRepository subMultipleRefScotlandRepository;

    private String manchesterMultipleReference;
    private String scotlandMultipleReference;
    private String multipleRef;

    @Before
    public void setUp() {
        multipleRef = "10000";
        manchesterMultipleReference = TribunalOffice.MANCHESTER.getOfficeNumber() + multipleRef;
        scotlandMultipleReference = TribunalOffice.GLASGOW.getOfficeNumber() + multipleRef;
    }

    @Test
    public void createManchesterReference() {
        fail("Implement EnglandWales test");
    }

    @Test
    public void createScotlandReference() {
        when(subMultipleRefScotlandRepository.ethosSubMultipleCaseRefGen(Integer.parseInt(scotlandMultipleReference), 1,
                SCOTLAND_CASE_TYPE_ID)).thenReturn(scotlandMultipleReference + "/1");
        String scotlandRef = TribunalOffice.GLASGOW.getOfficeNumber() + multipleRef + "/1";
        assertEquals(subMultipleReferenceService.createReference(SCOTLAND_DEV_BULK_CASE_TYPE_ID, scotlandMultipleReference, 1), scotlandRef);
    }



}