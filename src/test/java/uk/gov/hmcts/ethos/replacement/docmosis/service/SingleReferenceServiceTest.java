package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.joda.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefEnglandWalesRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.SingleRefScotlandRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

@ExtendWith(SpringExtension.class)
class SingleReferenceServiceTest {

    @InjectMocks
    private SingleReferenceService singleReferenceService;
    @Mock
    private SingleRefScotlandRepository singleRefScotlandRepository;
    @Mock
    private SingleRefEnglandWalesRepository singleRefEnglandWalesRepository;

    @Test
    void createEnglandWalesReference() {
        String expectedRef = "6000001/" + LocalDateTime.now().getYear();
        when(singleRefEnglandWalesRepository.ethosCaseRefGen(LocalDateTime.now().getYear())).thenReturn(expectedRef);
        assertEquals(expectedRef, singleReferenceService.createReference(ENGLANDWALES_CASE_TYPE_ID));
    }

    @Test
    void createScotlandReference() {
        String expectedRef = "8000001/" + LocalDateTime.now().getYear();
        when(singleRefScotlandRepository.ethosCaseRefGen(LocalDateTime.now().getYear())).thenReturn(expectedRef);
        assertEquals(expectedRef, singleReferenceService.createReference(SCOTLAND_CASE_TYPE_ID));
    }
}
