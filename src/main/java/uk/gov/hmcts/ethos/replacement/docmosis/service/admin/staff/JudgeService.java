package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

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

    public static final String ADD_JUDGE_CODE_AND_OFFICE_CONFLICT_ERROR =
            "A judge with the same Code (%s) and Tribunal Office (%s) already exists.";

    public static final String ADD_JUDGE_NAME_AND_OFFICE_CONFLICT_ERROR =
            "A judge with the same Name (%s) and Tribunal Office (%s) already exists.";

    public JudgeService(JudgeRepository judgeRepository) {
        this.judgeRepository = judgeRepository;
    }

    public void saveJudge(AdminData adminData) {
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice());

        var judge =  new Judge();
        judge.setCode(adminData.getJudgeCode());
        judge.setName(adminData.getJudgeName());
        judge.setEmploymentStatus(JudgeEmploymentStatus.valueOf(adminData.getEmploymentStatus()));
        judge.setTribunalOffice(tribunalOffice);

        if (judgeRepository.existsByCodeAndTribunalOffice(adminData.getJudgeCode(), tribunalOffice)) {
            throw new SaveJudgeException(String.format(ADD_JUDGE_CODE_AND_OFFICE_CONFLICT_ERROR,
                    adminData.getJudgeCode(), adminData.getTribunalOffice()));
        } else if (judgeRepository.existsByNameAndTribunalOffice(adminData.getJudgeName(), tribunalOffice)) {
            throw new SaveJudgeException(String.format(ADD_JUDGE_NAME_AND_OFFICE_CONFLICT_ERROR,
                    adminData.getJudgeName(), adminData.getTribunalOffice()));
        } else {
            judgeRepository.save(judge);
        }
    }
}
