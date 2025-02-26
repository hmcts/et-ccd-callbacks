package uk.gov.hmcts.ethos.replacement.docmosis.service.referencedata.jpaservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class JpaJudgeServiceTest {
    @Test
    void testGetJudges() {
        TribunalOffice tribunalOffice = TribunalOffice.BRISTOL;
        List<Judge> judges = List.of(
                createJudge("judge1", "z A Test"),
                createJudge("judge2", "A Judge"),
                createJudge("judge3", "A John"),
                createJudge("judge4", "B John"),
                createJudge("judge4", "z A John"),
                createJudge("judge5", "z B John"),
                createJudge("judge6", "z C Judge"),
                createJudge("judge7", "C Judge"));
        JudgeRepository judgeRepository = mock(JudgeRepository.class);
        when(judgeRepository.findByTribunalOffice(tribunalOffice)).thenReturn(judges);

        JpaJudgeService judgeService = new JpaJudgeService(judgeRepository);
        List<DynamicValueType> values = judgeService.getJudgesDynamicList(tribunalOffice);

        assertEquals(8, values.size());
        // Results should be sorted in alphabetical order by z values and surname
        verifyValue(values.get(0), "judge3", "A John");
        verifyValue(values.get(1), "judge4", "B John");
        verifyValue(values.get(2), "judge2", "A Judge");
        verifyValue(values.get(3), "judge7", "C Judge");
        verifyValue(values.get(4), "judge4", "z A John");
        verifyValue(values.get(5), "judge5", "z B John");
        verifyValue(values.get(6), "judge6", "z C Judge");
        verifyValue(values.get(7), "judge1", "z A Test");
    }

    private Judge createJudge(String code, String name) {
        Judge judge = new Judge();
        judge.setCode(code);
        judge.setName(name);
        return judge;
    }

    private void verifyValue(DynamicValueType value, String expectedCode, String expectedLabel) {
        assertEquals(expectedCode, value.getCode());
        assertEquals(expectedLabel, value.getLabel());
    }
}
