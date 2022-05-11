package uk.gov.hmcts.ethos.replacement.docmosis.service.admin;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JudgeEmploymentStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;

import static org.mockito.Mockito.*;

class AddJudgeServiceTest {
    private JudgeRepository judgeRepository;
    private final String judgeCode = "testCode";
    private final String judgeName = "testName";

    @BeforeEach
    void setup() {
        judgeRepository = mock(JudgeRepository.class);
    }

    @Test
    void shouldSaveJudge() {
        var adminData = createAdminData(judgeCode, judgeName, "ABERDEEN", "SALARIED");
        var addJudgeService = new AddJudgeService(judgeRepository);
        when(judgeRepository.existsByCodeOrName(judgeCode, judgeName)).thenReturn(false);
        Assert.assertEquals(addJudgeService.saveJudge(adminData), true);
        verify(judgeRepository, times(1)).save(createJudge(adminData));
    }

    @Test
    void shouldReturnFalseIfJudgeExists() {
        var adminData = createAdminData(judgeCode, judgeName, "ABERDEEN", "SALARIED");
        var addJudgeService = new AddJudgeService(judgeRepository);
        when(judgeRepository.existsByCodeOrName(judgeCode, judgeName)).thenReturn(true);
        Assert.assertEquals(addJudgeService.saveJudge(adminData), false);
        verify(judgeRepository, never()).save(createJudge(adminData));
    }

    private AdminData createAdminData(String judgeCode, String judgeName, String tribunalOffice, String employmentStatus) {
        AdminData adminData = new AdminData();
        adminData.setJudgeCode(judgeCode);
        adminData.setJudgeName(judgeName);
        adminData.setTribunalOffice(tribunalOffice);
        adminData.setEmploymentStatus(employmentStatus);
        return adminData;
    }

    private Judge createJudge(AdminData adminData) {
        var judge =  new Judge();
        judge.setCode(adminData.getJudgeCode());
        judge.setName(adminData.getJudgeName());
        judge.setEmploymentStatus(JudgeEmploymentStatus.valueOf(adminData.getEmploymentStatus()));
        judge.setTribunalOffice(TribunalOffice.valueOf(adminData.getTribunalOffice()));
        return judge;
    }
}