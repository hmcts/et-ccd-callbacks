package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.Before;
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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_OFFICE_NUMBER;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_OFFICE_NUMBER;

@RunWith(SpringJUnit4ClassRunner.class)
public class SingleReferenceServiceTest {

    @InjectMocks
    private SingleReferenceService singleReferenceService;
    @Mock
    private SingleRefScotlandRepository singleRefScotlandRepository;
    @Mock
    private SingleRefEnglandWalesRepository singleRefEnglandWalesRepository;

    private String currentYear;

    @Before
    public void setUp() {
        currentYear = String.valueOf(LocalDate.now().getYear());
    }

    @Test
    public void createEnglandWalesReference() {
        when(singleRefEnglandWalesRepository.ethosCaseRefGen(1, Integer.parseInt(currentYear),
                ENGLANDWALES_CASE_TYPE_ID)).thenReturn("00012/" + currentYear);
        String englandWalesRef = ENGLANDWALES_OFFICE_NUMBER + "00012/" + currentYear;
        assertEquals(singleReferenceService.createReference(ENGLANDWALES_CASE_TYPE_ID, 1), englandWalesRef);
    }

    @Test
    public void createEnglandWalesReferenceMultipleCases() {
        when(singleRefEnglandWalesRepository.ethosCaseRefGen(2, Integer.parseInt(currentYear),
                ENGLANDWALES_CASE_TYPE_ID)).thenReturn("00012/" + currentYear);
        String manchesterRef = ENGLANDWALES_OFFICE_NUMBER + "00012/" + currentYear;
        assertEquals(singleReferenceService.createReference(ENGLANDWALES_CASE_TYPE_ID, 2), manchesterRef);
    }

    @Test
    public void createScotlandReference() {
        when(singleRefScotlandRepository.ethosCaseRefGen(1, Integer.parseInt(currentYear),
                SCOTLAND_CASE_TYPE_ID)).thenReturn("00012/" + currentYear);
        String scotlandRef = SCOTLAND_OFFICE_NUMBER + "00012/" + currentYear;
        assertEquals(singleReferenceService.createReference(SCOTLAND_CASE_TYPE_ID, 1), scotlandRef);
    }

}