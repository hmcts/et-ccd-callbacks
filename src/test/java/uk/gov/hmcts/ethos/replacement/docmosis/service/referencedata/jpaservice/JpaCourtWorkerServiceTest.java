package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JpaCourtWorkerServiceTest {
    @Test
    public void testGetCourtWorkerByTribunalOffice() {
        TribunalOffice tribunalOffice = TribunalOffice.BRISTOL;
        CourtWorkerType courtWorkerType = CourtWorkerType.CLERK;
        List<CourtWorker> courtWorkers = List.of(
                createCourtWorker("worker1", "Worker 1"),
                createCourtWorker("worker2", "Worker 2"),
                createCourtWorker("worker3", "Worker 3"));
        CourtWorkerRepository courtWorkerRepository = mock(CourtWorkerRepository.class);
        when(courtWorkerRepository.findByTribunalOfficeAndType(
                tribunalOffice, courtWorkerType)).thenReturn(courtWorkers);

        JpaCourtWorkerService courtWorkerService = new JpaCourtWorkerService(courtWorkerRepository);
        List<DynamicValueType> values = courtWorkerService.getCourtWorkerByTribunalOffice(
                tribunalOffice, courtWorkerType);

        assertEquals(3, values.size());
        verifyValue(values.get(0), "worker1", "Worker 1");
        verifyValue(values.get(1), "worker2", "Worker 2");
        verifyValue(values.get(2), "worker3", "Worker 3");
    }

    private CourtWorker createCourtWorker(String code, String name) {
        CourtWorker courtWorker = new CourtWorker();
        courtWorker.setCode(code);
        courtWorker.setName(name);
        return courtWorker;
    }

    private void verifyValue(DynamicValueType value, String expectedCode, String expectedLabel) {
        assertEquals(expectedCode, value.getCode());
        assertEquals(expectedLabel, value.getLabel());
    }
}
