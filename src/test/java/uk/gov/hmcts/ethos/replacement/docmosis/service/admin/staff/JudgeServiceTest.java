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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.JudgeService.NO_JUDGE_FOUND_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.JudgeService.NO_JUDGE_FOUND_WITH_NAME_SPECIFIED_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.JudgeService.SAVE_ERROR_MESSAGE;

class JudgeServiceTest {
    private JudgeRepository judgeRepository;
    private static final String TEST_CODE = "testCode";
    private static final String TEST_NAME = "testName";
    private static final String SALARIED_EMPLOYMENT_STATUS = "SALARIED";
    private final String tribunalOffice = TribunalOffice.LEEDS.getOfficeName();
    private Judge judge;
    private AdminData adminData;
    private JudgeService judgeService;

    @BeforeEach
    void setup() {
        judgeRepository = mock(JudgeRepository.class);
        adminData = createAdminData(TEST_CODE, TEST_NAME, tribunalOffice, "SALARIED");
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
        when(judgeRepository.existsByCodeAndTribunalOffice(TEST_CODE, TribunalOffice.valueOfOfficeName(tribunalOffice)))
                .thenReturn(false);
        when(judgeRepository.existsByNameAndTribunalOffice(TEST_NAME,
                TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(false);

        assertDoesNotThrow(() -> judgeService.saveJudge(adminData));
        verify(judgeRepository, times(1)).save(judge);
    }

    @Test
    void saveJudge_shouldReturnErrorIfJudgeWithSameCodeAndOfficeExists() {
        when(judgeRepository.existsByCodeAndTribunalOffice(TEST_CODE, TribunalOffice.valueOfOfficeName(tribunalOffice)))
                .thenReturn(true);
        assertThrows(SaveJudgeException.class, () -> judgeService.saveJudge(adminData));
        verify(judgeRepository, never()).save(judge);
    }

    @Test
    void saveJudge_shouldReturnErrorIfJudgeWithSameNameAndOfficeExists() {
        when(judgeRepository.existsByCodeAndTribunalOffice(TEST_CODE, TribunalOffice.valueOfOfficeName(tribunalOffice)))
                .thenReturn(false);
        when(judgeRepository.existsByNameAndTribunalOffice(TEST_NAME,
                TribunalOffice.valueOfOfficeName(tribunalOffice))).thenReturn(true);
        assertThrows(SaveJudgeException.class, () -> judgeService.saveJudge(adminData));
        verify(judgeRepository, never()).save(judge);
    }

    @Test
    void updateJudgeMidEventSelectOffice_shouldReturnDynamicList() {
        adminData.setTribunalOffice(tribunalOffice);

        List<Judge> listJudge = createListJudge(1, TribunalOffice.LEEDS, TEST_CODE, TEST_NAME,
            SALARIED_EMPLOYMENT_STATUS);
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
        String expectedErrorMsg = String.format(NO_JUDGE_FOUND_ERROR_MESSAGE, tribunalOffice);
        String actualErrorMsg = errors.get(0);
        assertThat(expectedErrorMsg, is(equalTo(actualErrorMsg)));
    }

    @Test
    void updateJudgeMidEventSelectJudge_shouldReturnJudge() {
        adminData = createAdminDataWithDynamicList(
                "1", TribunalOffice.LEEDS.getOfficeName(), TEST_CODE, TEST_NAME, SALARIED_EMPLOYMENT_STATUS);

        List<Judge> listJudge = createListJudge(1, TribunalOffice.LEEDS, TEST_CODE, TEST_NAME,
            SALARIED_EMPLOYMENT_STATUS);
        when(judgeRepository.findById(anyInt())).thenReturn(listJudge);

        List<String> errors = judgeService.updateJudgeMidEventSelectJudge(adminData);
        assertEquals(0, errors.size());
        assertEquals(TEST_CODE, adminData.getJudgeCode());
        assertEquals(TEST_NAME, adminData.getJudgeName());
    }

    @Test
    void updateJudgeMidEventSelectJudge_shouldGiveError() {
        adminData = createAdminDataWithDynamicList(
                "1", TribunalOffice.LEEDS.getOfficeName(), TEST_CODE, TEST_NAME, SALARIED_EMPLOYMENT_STATUS);

        List<Judge> listJudge = new ArrayList<>();
        when(judgeRepository.findById(anyInt())).thenReturn(listJudge);

        List<String> errors = judgeService.updateJudgeMidEventSelectJudge(adminData);
        verify(judgeRepository, never()).save(judge);
        assertEquals(1, errors.size());
        assertThat(SAVE_ERROR_MESSAGE, is(equalTo(errors.get(0))));
    }

    @Test
    void updateJudge_shouldSaveJudge() {
        String newJudgeName = "Name2";
        adminData = createAdminDataWithDynamicList(
                "1", TribunalOffice.LEEDS.getOfficeName(), TEST_CODE, TEST_NAME, SALARIED_EMPLOYMENT_STATUS);
        adminData.setJudgeName(newJudgeName);

        List<Judge> listJudge = createListJudge(1, TribunalOffice.LEEDS, TEST_CODE, TEST_NAME,
            SALARIED_EMPLOYMENT_STATUS);
        when(judgeRepository.findById(anyInt())).thenReturn(listJudge);

        List<String> errors = judgeService.updateJudge(adminData);
        assertEquals(0, errors.size());
        verify(judgeRepository, times(1)).save(
                createJudgeWithId(1, TribunalOffice.LEEDS, TEST_CODE, newJudgeName, SALARIED_EMPLOYMENT_STATUS));
    }

    @Test
    void updateJudge_shouldReturnError() {
        adminData = createAdminDataWithDynamicList(
                "1", TribunalOffice.LEEDS.getOfficeName(), TEST_CODE, TEST_NAME, SALARIED_EMPLOYMENT_STATUS);
        adminData.setJudgeName("Name2");

        List<Judge> listJudge = new ArrayList<>();
        when(judgeRepository.findById(anyInt())).thenReturn(listJudge);

        List<String> errors = judgeService.updateJudge(adminData);
        assertEquals(1, errors.size());
        assertEquals(SAVE_ERROR_MESSAGE, errors.get(0));
    }

    @Test
    void deleteJudge_shouldDeleteJudge() {
        adminData = createAdminDataWithDynamicList("1", TribunalOffice.LEEDS.getOfficeName(),
            TEST_CODE, TEST_NAME, SALARIED_EMPLOYMENT_STATUS);
        List<Judge> listJudge = createListJudge(1, TribunalOffice.LEEDS, TEST_CODE, TEST_NAME,
            SALARIED_EMPLOYMENT_STATUS);
        when(judgeRepository.findById(anyInt())).thenReturn(listJudge);
        List<String> errors = judgeService.deleteJudge(adminData);

        assertEquals(0, errors.size());
        verify(judgeRepository, times(1)).deleteAll(listJudge);
        verify(judgeRepository, times(1)).flush();
    }

    @Test
    void deleteJudge_shouldReturnNoJudgeFoundError() {
        adminData = createAdminDataWithDynamicList("1", TribunalOffice.LEEDS.getOfficeName(), TEST_CODE, TEST_NAME,
            SALARIED_EMPLOYMENT_STATUS);
        List<Judge> emptyJudgeList = new ArrayList<>();
        when(judgeRepository.findById(anyInt())).thenReturn(emptyJudgeList);
        String expectedErrorMsg = String.format(NO_JUDGE_FOUND_WITH_NAME_SPECIFIED_ERROR_MESSAGE,
            adminData.getJudgeName());
        List<String> errors = judgeService.deleteJudge(adminData);

        assertEquals(1, errors.size());
        assertThat(expectedErrorMsg, is(equalTo(errors.get(0))));
    }

    @Test
    void deleteJudgeMidEventSelectOffice_shouldReturnJudgesListBySelectedOffice() {
        adminData = createAdminDataWithDynamicList("1", TribunalOffice.LEEDS.getOfficeName(), TEST_CODE, TEST_NAME,
            SALARIED_EMPLOYMENT_STATUS);
        List<Judge> judges = createListJudge(1, TribunalOffice.LEEDS, TEST_CODE, TEST_NAME, SALARIED_EMPLOYMENT_STATUS);
        when(judgeRepository.findByTribunalOfficeOrderById(TribunalOffice.valueOfOfficeName(tribunalOffice)))
            .thenReturn(judges);
        List<String> errors = judgeService.deleteJudgeMidEventSelectOffice(adminData);

        assertEquals(0, errors.size());
        verify(judgeRepository, times(1)).findByTribunalOfficeOrderById(any());
    }

    @Test
    void deleteJudgeMidEventSelectOffice_shouldReturnNoJudgeFoundError() {
        adminData = createAdminDataWithDynamicList("1", TribunalOffice.LEEDS.getOfficeName(), TEST_CODE, TEST_NAME,
            SALARIED_EMPLOYMENT_STATUS);
        List<Judge> emptyJudgeList = new ArrayList<>();
        when(judgeRepository.findByTribunalOfficeOrderById(TribunalOffice.valueOfOfficeName(tribunalOffice)))
            .thenReturn(emptyJudgeList);
        String expectedErrorMsg = String.format(NO_JUDGE_FOUND_ERROR_MESSAGE, adminData.getTribunalOffice());
        List<String> errors = judgeService.deleteJudgeMidEventSelectOffice(adminData);

        assertEquals(1, errors.size());
        assertThat(expectedErrorMsg, is(equalTo(errors.get(0))));
    }

    @Test
    void deleteJudgeMidEventSelectJudge_shouldSetJudgeForSelectedCode() {
        adminData = createAdminDataWithDynamicList("22", TribunalOffice.LEEDS.getOfficeName(), TEST_CODE, TEST_NAME,
            SALARIED_EMPLOYMENT_STATUS);
        List<Judge> judges = createListJudge(22, TribunalOffice.LEEDS, TEST_CODE, TEST_NAME,
            SALARIED_EMPLOYMENT_STATUS);
        when(judgeRepository.findById(anyInt())).thenReturn(judges);
        List<String> errors = judgeService.deleteJudgeMidEventSelectJudge(adminData);

        assertEquals(0, errors.size());
        verify(judgeRepository, times(1)).findById(anyInt());
    }

    @Test
    void deleteJudgeMidEventSelectJudge_shouldReturnNoJudgeFoundError() {
        adminData = createAdminDataWithDynamicList("22", TribunalOffice.LEEDS.getOfficeName(), TEST_CODE, TEST_NAME,
            SALARIED_EMPLOYMENT_STATUS);
        List<Judge> emptyJudgeList = new ArrayList<>();
        when(judgeRepository.findById(anyInt())).thenReturn(emptyJudgeList);
        String expectedErrorMsg = String.format(NO_JUDGE_FOUND_WITH_NAME_SPECIFIED_ERROR_MESSAGE,
            adminData.getJudgeName());
        List<String> errors = judgeService.deleteJudgeMidEventSelectJudge(adminData);

        assertEquals(1, errors.size());
        assertThat(expectedErrorMsg, is(equalTo(errors.get(0))));
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
        AdminData adminData = createAdminData(code, name, tribunalOffice, employmentStatus);
        adminData.setJudgeSelectList(DynamicFixedListType.of(DynamicValueType.create(code, name)));
        adminData.getJudgeSelectList().setValue(DynamicValueType.create(id, name));
        return adminData;
    }

    private Judge createJudge(AdminData adminData) {
        Judge judge =  new Judge();
        judge.setCode(adminData.getJudgeCode());
        judge.setName(adminData.getJudgeName());
        judge.setEmploymentStatus(JudgeEmploymentStatus.valueOf(adminData.getEmploymentStatus()));
        judge.setTribunalOffice(TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice()));
        return judge;
    }

    private Judge createJudge(TribunalOffice tribunalOffice, String code, String name, String employmentStatus) {
        Judge judge =  new Judge();
        judge.setCode(code);
        judge.setName(name);
        judge.setEmploymentStatus(JudgeEmploymentStatus.valueOf(employmentStatus));
        judge.setTribunalOffice(tribunalOffice);
        return judge;
    }

    private Judge createJudgeWithId(int id, TribunalOffice tribunalOffice, String code, String name,
                                    String employmentStatus) {
        Judge judge = createJudge(tribunalOffice, code, name, employmentStatus);
        judge.setId(id);
        return judge;
    }

    private List<Judge> createListJudge(int id, TribunalOffice tribunalOffice, String code, String name,
                                        String employmentStatus) {
        Judge judge = createJudgeWithId(id, tribunalOffice, code, name, employmentStatus);
        List<Judge> listJudge = new ArrayList<>();
        listJudge.add(judge);
        return listJudge;
    }
}