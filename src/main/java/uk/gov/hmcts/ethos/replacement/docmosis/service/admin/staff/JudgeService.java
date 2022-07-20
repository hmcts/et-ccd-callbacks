package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JudgeEmploymentStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class JudgeService {

    private final JudgeRepository judgeRepository;

    public static final String ADD_JUDGE_CODE_AND_OFFICE_CONFLICT_ERROR =
            "A judge with the same Code (%s) and Tribunal Office (%s) already exists.";
    public static final String ADD_JUDGE_NAME_AND_OFFICE_CONFLICT_ERROR =
            "A judge with the same Name (%s) and Tribunal Office (%s) already exists.";
    public static final String NO_JUDGE_FOUND_ERROR_MESSAGE = "No judge found in the %s office";
    public static final String NO_JUDGE_FOUND_WITH_NAME_SPECIFIED_ERROR_MESSAGE = "No judge found with name %s";
    public static final String SAVE_ERROR_MESSAGE = "Update failed";

    public JudgeService(JudgeRepository judgeRepository) {
        this.judgeRepository = judgeRepository;
    }

    public void initAddJudge(AdminData adminData) {
        adminData.setTribunalOffice(null);
        adminData.setJudgeCode(null);
        adminData.setJudgeName(null);
        adminData.setEmploymentStatus(null);
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

    public List<String> updateJudgeMidEventSelectOffice(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var tribunalOffice = adminData.getTribunalOffice();
        List<Judge> judgeList = getJudgesListByOffice(tribunalOffice);

        if (judgeList.isEmpty()) {
            errors.add(String.format(NO_JUDGE_FOUND_ERROR_MESSAGE, tribunalOffice));
            return errors;
        }

        List<DynamicValueType> dynamicJudge = new ArrayList<>();
        for (var judge : judgeList) {
            dynamicJudge.add(DynamicValueType.create(judge.getId().toString(), judge.getName()));
        }

        var judgeDynamicList = new DynamicFixedListType();
        judgeDynamicList.setListItems(dynamicJudge);

        adminData.setJudgeSelectList(judgeDynamicList);
        return errors;
    }

    public List<String> updateJudgeMidEventSelectJudge(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var selectedId = Integer.parseInt(adminData.getJudgeSelectList().getSelectedCode());
        var findJudge = judgeRepository.findById(selectedId);
        if (findJudge.isEmpty()) {
            errors.add(SAVE_ERROR_MESSAGE);
            return errors;
        }
        setSelectedJudgeDetails(findJudge.get(0), adminData);
        return errors;
    }

    public List<String> updateJudge(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var selectedId = Integer.parseInt(adminData.getJudgeSelectList().getSelectedCode());

        var findJudge = judgeRepository.findById(selectedId);
        if (!findJudge.isEmpty()) {
            var thisJudge = findJudge.get(0);
            thisJudge.setName(adminData.getJudgeName());
            thisJudge.setEmploymentStatus(JudgeEmploymentStatus.valueOf(adminData.getEmploymentStatus()));
            if (judgeRepository.existsByTribunalOfficeAndNameAndIdIsNot(
                    thisJudge.getTribunalOffice(), thisJudge.getName(), selectedId)) {
                errors.add(String.format(ADD_JUDGE_NAME_AND_OFFICE_CONFLICT_ERROR, thisJudge.getName(),
                        thisJudge.getTribunalOffice()));
            } else {
                judgeRepository.save(thisJudge);
            }
        } else {
            errors.add(SAVE_ERROR_MESSAGE);
        }

        return errors;
    }

    public List<String> deleteJudge(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var selectedId = Integer.parseInt(adminData.getJudgeSelectList().getSelectedCode());
        var matchingJudgesList = judgeRepository.findById(selectedId);
        if (matchingJudgesList.isEmpty()) {
            errors.add(String.format(NO_JUDGE_FOUND_WITH_NAME_SPECIFIED_ERROR_MESSAGE, adminData.getJudgeName()));
            return errors;
        }
        judgeRepository.deleteAll(matchingJudgesList);
        judgeRepository.flush();
        return errors;
    }

    public List<String> deleteJudgeMidEventSelectOffice(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var tribunalOffice = adminData.getTribunalOffice();
        List<Judge> judgeList = getJudgesListByOffice(adminData.getTribunalOffice());
        if (judgeList.isEmpty()) {
            errors.add(String.format(NO_JUDGE_FOUND_ERROR_MESSAGE, tribunalOffice));
            return errors;
        }
        adminData.setJudgeSelectList(getJudgesDynamicList(judgeList));
        return errors;
    }

    public List<String> deleteJudgeMidEventSelectJudge(AdminData adminData) {
        var errors = new ArrayList<String>();
        var selectedId = Integer.parseInt(adminData.getJudgeSelectList().getSelectedCode());
        var findJudge = judgeRepository.findById(selectedId);
        if (findJudge.isEmpty()) {
            errors.add(String.format(NO_JUDGE_FOUND_WITH_NAME_SPECIFIED_ERROR_MESSAGE, adminData.getJudgeName()));
            return errors;
        }
        setSelectedJudgeDetails(findJudge.get(0), adminData);
        return errors;
    }

    private List<Judge> getJudgesListByOffice(String tribunalOffice) {
        return judgeRepository.findByTribunalOfficeOrderById(TribunalOffice.valueOfOfficeName(tribunalOffice));
    }

    private DynamicFixedListType getJudgesDynamicList(List<Judge> judgeList) {
        List<DynamicValueType> dynamicJudge = new ArrayList<>();
        judgeList.forEach(judge -> dynamicJudge.add(DynamicValueType.create(judge.getId().toString(),
            judge.getName())));
        var judgeDynamicList = new DynamicFixedListType();
        judgeDynamicList.setListItems(dynamicJudge);
        return judgeDynamicList;
    }

    private void setSelectedJudgeDetails(Judge selectedJudge, AdminData adminData) {
        adminData.setJudgeCode(selectedJudge.getCode());
        adminData.setJudgeName(selectedJudge.getName());
        adminData.setEmploymentStatus(selectedJudge.getEmploymentStatus().toString());
    }
}
