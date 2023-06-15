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
                createJudge("judge1", "Judge 1"),
                createJudge("judge2", "Judge 2"),
                createJudge("judge3", "Judge 3"));
        JudgeRepository judgeRepository = mock(JudgeRepository.class);
        when(judgeRepository.findByTribunalOffice(tribunalOffice)).thenReturn(judges);

        JpaJudgeService judgeService = new JpaJudgeService(judgeRepository);
        List<DynamicValueType> values = judgeService.getJudgesDynamicList(tribunalOffice);

        assertEquals(3, values.size());
        verifyValue(values.get(0), "judge1", "Judge 1");
        verifyValue(values.get(1), "judge2", "Judge 2");
        verifyValue(values.get(2), "judge3", "Judge 3");
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
