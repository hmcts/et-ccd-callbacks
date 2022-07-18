package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.Judge;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.JudgeEmploymentStatus;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.JudgeRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.JudgeService.NO_FOUND_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.JudgeService.SAVE_ERROR_MESSAGE;

class JudgeServiceTest {
    private JudgeRepository judgeRepository;
    private final String judgeCode = "testCode";
    private final String judgeName = "testName";
    private final String tribunalOffice = TribunalOffice.LEEDS.getOfficeName();
    private final String employmentStatus = "SALARIED";
    private Judge judge;
    private AdminData adminData;
    private JudgeService judgeService;

    @BeforeEach
    void setup() {
        judgeRepository = mock(JudgeRepository.class);
        adminData = createAdminData(judgeCode, judgeName, tribunalOffice, "SALARIED");
        judge = createJudge(adminData);
        judgeService = new JudgeService(judgeRepository);
    }

    @Test
    void initAddJudge_shouldClearAdminData() {
        judgeService.initAddJudge(adminData);
        assertNull(adminData.getTribunalOffice());
        assertNull(adminData.getJudgeCode());
        assertNull(adminData.getJudgeName());
        assertNull(adminData.getEmploymentStatus());
    }

    @Test
    void saveJudge_shouldSaveJudge() {
        when(judgeRepository.existsByCodeAndTribunalOffice(judgeCode, TribunalOffice.valueOfOfficeName(tribunalOffice)))
                .thenReturn(false);
        when(judgeRepository.existsByNameAndTribunalOffice(judgeName,
                TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(false);

        assertDoesNotThrow(() -> judgeService.saveJudge(adminData));
        verify(judgeRepository, times(1)).save(judge);
    }

    @Test
    void saveJudge_shouldReturnErrorIfJudgeWithSameCodeAndOfficeExists() {
        when(judgeRepository.existsByCodeAndTribunalOffice(judgeCode, TribunalOffice.valueOfOfficeName(tribunalOffice)))
                .thenReturn(true);
        assertThrows(SaveJudgeException.class, () -> judgeService.saveJudge(adminData));
        verify(judgeRepository, never()).save(judge);
    }

    @Test
    void saveJudge_shouldReturnErrorIfJudgeWithSameNameAndOfficeExists() {
        when(judgeRepository.existsByCodeAndTribunalOffice(judgeCode, TribunalOffice.valueOfOfficeName(tribunalOffice)))
                .thenReturn(false);
        when(judgeRepository.existsByNameAndTribunalOffice(judgeName,
                TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(true);
        assertThrows(SaveJudgeException.class, () -> judgeService.saveJudge(adminData));
        verify(judgeRepository, never()).save(judge);
    }

    @Test
    void updateJudgeMidEventSelectOffice_shouldReturnDynamicList() {
        adminData.setTribunalOffice(tribunalOffice);

        var listJudge = createListJudge(1, TribunalOffice.LEEDS, judgeCode, judgeName, employmentStatus);
        when(judgeRepository.findByTribunalOfficeOrderById(any(TribunalOffice.class))).thenReturn(listJudge);

        List<String> errors = judgeService.updateJudgeMidEventSelectOffice(adminData);
        assertEquals(0, errors.size());
        assertEquals(1, adminData.getJudgeSelectList().getListItems().size());
    }

    @Test
    void updateJudgeMidEventSelectOffice_shouldGiveError() {
        adminData.setTribunalOffice(tribunalOffice);

        List<Judge> listJudge = new ArrayList<>();
        when(judgeRepository.findByTribunalOfficeOrderById(any(TribunalOffice.class))).thenReturn(listJudge);

        List<String> errors = judgeService.updateJudgeMidEventSelectOffice(adminData);
        assertEquals(1, errors.size());
        assertEquals(String.format(NO_FOUND_ERROR_MESSAGE, tribunalOffice), errors.get(0));
    }

    @Test
    void updateJudgeMidEventSelectJudge_shouldReturnJudge() {
        adminData = createAdminDataWithDynamicList(
                "1", TribunalOffice.LEEDS.getOfficeName(), judgeCode, judgeName, employmentStatus);

        var listJudge = createListJudge(1, TribunalOffice.LEEDS, judgeCode, judgeName, employmentStatus);
        when(judgeRepository.findById(anyInt())).thenReturn(listJudge);

        List<String> errors = judgeService.updateJudgeMidEventSelectJudge(adminData);
        assertEquals(0, errors.size());
        assertEquals(judgeCode, adminData.getJudgeCode());
        assertEquals(judgeName, adminData.getJudgeName());
    }

    @Test
    void updateJudgeMidEventSelectJudge_shouldGiveError() {
        adminData = createAdminDataWithDynamicList(
                "1", TribunalOffice.LEEDS.getOfficeName(), judgeCode, judgeName, employmentStatus);

        List<Judge> listJudge = new ArrayList<>();
        when(judgeRepository.findById(anyInt())).thenReturn(listJudge);

        List<String> errors = judgeService.updateJudgeMidEventSelectJudge(adminData);
        verify(judgeRepository, never()).save(judge);
        assertEquals(1, errors.size());
        assertEquals(SAVE_ERROR_MESSAGE, errors.get(0));
    }

    @Test
    void updateJudge_shouldSaveJudge() {
        var newJudgeName = "Name2";
        adminData = createAdminDataWithDynamicList(
                "1", TribunalOffice.LEEDS.getOfficeName(), judgeCode, judgeName, employmentStatus);
        adminData.setJudgeName(newJudgeName);

        var listJudge = createListJudge(1, TribunalOffice.LEEDS, judgeCode, judgeName, employmentStatus);
        when(judgeRepository.findById(anyInt())).thenReturn(listJudge);

        List<String> errors = judgeService.updateJudge(adminData);
        assertEquals(0, errors.size());
        verify(judgeRepository, times(1)).save(
                createJudgeWithId(1, TribunalOffice.LEEDS, judgeCode, newJudgeName, employmentStatus));
    }

    @Test
    void updateJudge_shouldReturnError() {
        adminData = createAdminDataWithDynamicList(
                "1", TribunalOffice.LEEDS.getOfficeName(), judgeCode, judgeName, employmentStatus);
        adminData.setJudgeName("Name2");

        List<Judge> listJudge = new ArrayList<>();
        when(judgeRepository.findById(anyInt())).thenReturn(listJudge);

        List<String> errors = judgeService.updateJudge(adminData);
        assertEquals(1, errors.size());
        assertEquals(SAVE_ERROR_MESSAGE, errors.get(0));
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

    private AdminData createAdminDataWithDynamicList(String id, String tribunalOffice, String code, String name,
                                                     String employmentStatus) {
        var adminData = createAdminData(code, name, tribunalOffice, employmentStatus);
        adminData.setTribunalOffice(tribunalOffice);

        List<DynamicValueType> dynamicJudge = new ArrayList<>();
        dynamicJudge.add(DynamicValueType.create(id, name));

        var judgeDynamicList = new DynamicFixedListType();
        judgeDynamicList.setListItems(dynamicJudge);
        adminData.setJudgeSelectList(judgeDynamicList);

        var dynamicValueType = DynamicValueType.create(id, name);
        adminData.getJudgeSelectList().setValue(dynamicValueType);
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

    private Judge createJudge(TribunalOffice tribunalOffice, String code, String name, String employmentStatus) {
        var judge =  new Judge();
        judge.setCode(code);
        judge.setName(name);
        judge.setEmploymentStatus(JudgeEmploymentStatus.valueOf(employmentStatus));
        judge.setTribunalOffice(tribunalOffice);
        return judge;
    }

    private Judge createJudgeWithId(int id, TribunalOffice tribunalOffice, String code, String name,
                                    String employmentStatus) {
        var judge = createJudge(tribunalOffice, code, name, employmentStatus);
        judge.setId(id);
        return judge;
    }

    private List<Judge> createListJudge(int id, TribunalOffice tribunalOffice, String code, String name,
                                        String employmentStatus) {
        var judge = createJudgeWithId(id, tribunalOffice, code, name, employmentStatus);
        List<Judge> listJudge = new ArrayList<>();
        listJudge.add(judge);
        return listJudge;
    }

}