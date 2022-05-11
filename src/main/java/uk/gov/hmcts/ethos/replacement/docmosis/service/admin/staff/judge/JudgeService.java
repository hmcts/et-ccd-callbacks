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

    public JudgeService(JudgeRepository judgeRepository) {
        this.judgeRepository = judgeRepository;
    }

    public boolean saveJudge(AdminData adminData) {
        var judge =  new Judge();
        judge.setCode(adminData.getJudgeCode());
        judge.setName(adminData.getJudgeName());
        judge.setEmploymentStatus(JudgeEmploymentStatus.valueOf(adminData.getEmploymentStatus()));
        judge.setTribunalOffice(TribunalOffice.valueOf(adminData.getTribunalOffice()));

        if (!judgeRepository.existsByCodeOrName(adminData.getJudgeCode(), adminData.getJudgeName())) {
            judgeRepository.save(judge);
        } else {
            log.error("A judge with the same name or code already exists.");
            return false;
        }

        return true;
    }
}
