package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.judge;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JudgeEmploymentStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;

@Service
@Slf4j
public class JudgeService {

    private final JudgeRepository judgeRepository;
    public static final String ADD_JUDGE_CONFLICT_ERROR =
            "A judge already exists with the same Code (%s) and Tribunal Office (%s)";

    public JudgeService(JudgeRepository judgeRepository) {
        this.judgeRepository = judgeRepository;
    }

    public void saveJudge(AdminData adminData) {
        var judge =  new Judge();
        judge.setCode(adminData.getJudgeCode());
        judge.setName(adminData.getJudgeName());
        judge.setEmploymentStatus(JudgeEmploymentStatus.valueOf(adminData.getEmploymentStatus()));
        judge.setTribunalOffice(TribunalOffice.valueOf(adminData.getTribunalOffice()));

        if (judgeRepository.existsByCodeAndTribunalOffice(adminData.getJudgeCode(),
                TribunalOffice.valueOf(adminData.getTribunalOffice()))) {
            throw new SaveJudgeException(String.format(ADD_JUDGE_CONFLICT_ERROR,
                    adminData.getJudgeCode(), adminData.getTribunalOffice()));
        } else {
            judgeRepository.save(judge);
        }
    }
}
