package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefScotlandRepository;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@RunWith(SpringJUnit4ClassRunner.class)
public class SingleReferenceServiceTest {

    @InjectMocks
    private SingleReferenceService singleReferenceService;
    @Mock
    private SingleRefScotlandRepository singleRefScotlandRepository;
    @Mock
    private SingleRefEnglandWalesRepository singleRefEnglandWalesRepository;

    @Test
    public void createEnglandWalesReference() {
        String expectedRef = "6000001/" + LocalDateTime.now().getYear();
        when(singleRefEnglandWalesRepository.ethosCaseRefGen(LocalDateTime.now().getYear())).thenReturn(expectedRef);
        assertEquals(expectedRef, singleReferenceService.createReference(ENGLANDWALES_CASE_TYPE_ID));
    }

    @Test
    public void createScotlandReference() {
        String expectedRef = "8000001/" + LocalDateTime.now().getYear();
        when(singleRefScotlandRepository.ethosCaseRefGen(LocalDateTime.now().getYear())).thenReturn(expectedRef);
        assertEquals(expectedRef, singleReferenceService.createReference(SCOTLAND_CASE_TYPE_ID));
    }
}
