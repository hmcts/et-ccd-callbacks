package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JudgeEmploymentStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JudgeServiceTest {
    private JudgeRepository judgeRepository;
    private final String judgeCode = "testCode";
    private final String judgeName = "testName";
    private final String tribunalOffice = "Aberdeen";
    private Judge judge;
    private AdminData adminData;
    private JudgeService addJudgeService;

    @BeforeEach
    void setup() {
        judgeRepository = mock(JudgeRepository.class);
        adminData = createAdminData(judgeCode, judgeName, tribunalOffice, "SALARIED");
        judge = createJudge(adminData);
        addJudgeService = new JudgeService(judgeRepository);
    }

    @Test
    void shouldSaveJudge() {
        when(judgeRepository.existsByCodeAndTribunalOffice(judgeCode, TribunalOffice.valueOfOfficeName(tribunalOffice)))
                .thenReturn(false);
        when(judgeRepository.existsByNameAndTribunalOffice(judgeName,
                TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(false);

        assertDoesNotThrow(() -> addJudgeService.saveJudge(adminData));
        verify(judgeRepository, times(1)).save(judge);
    }

    @Test
    void shouldReturnErrorIfJudgeWithSameCodeAndOfficeExists() {
        when(judgeRepository.existsByCodeAndTribunalOffice(judgeCode, TribunalOffice.valueOfOfficeName(tribunalOffice)))
                .thenReturn(true);
        assertThrows(SaveJudgeException.class, () -> addJudgeService.saveJudge(adminData));
        verify(judgeRepository, never()).save(judge);
    }

    @Test
    void shouldReturnErrorIfJudgeWithSameNameAndOfficeExists() {
        when(judgeRepository.existsByCodeAndTribunalOffice(judgeCode, TribunalOffice.valueOfOfficeName(tribunalOffice)))
                .thenReturn(false);
        when(judgeRepository.existsByNameAndTribunalOffice(judgeName,
                TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(true);
        assertThrows(SaveJudgeException.class, () -> addJudgeService.saveJudge(adminData));
        verify(judgeRepository, never()).save(judge);
    }

    private AdminData createAdminData(String judgeCode, String judgeName, String tribunalOffice,
                                      String employmentStatus) {
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
        judge.setTribunalOffice(TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice()));
        return judge;
    }
}