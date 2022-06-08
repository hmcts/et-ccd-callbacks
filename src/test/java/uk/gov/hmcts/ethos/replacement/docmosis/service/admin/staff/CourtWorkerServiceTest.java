package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.AdminCourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.CourtWorkerService.CODE_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.CourtWorkerService.NAME_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.CourtWorkerService.NO_FOUND_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.CourtWorkerService.SAVE_ERROR_MESSAGE;

class CourtWorkerServiceTest {

    private CourtWorkerRepository courtWorkerRepository;
    private CourtWorkerService courtWorkerService;
    private AdminData adminData;

    @BeforeEach
    void setUp() {
        courtWorkerRepository = mock(CourtWorkerRepository.class);
        courtWorkerService = new CourtWorkerService(courtWorkerRepository);

        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(false);
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(false);
    }

    @Test
    void initAddCourtWorker_shouldClearAdminData() {
        var adminData = new AdminData();
        courtWorkerService.initAddCourtWorker(adminData);
        assertNull(adminData.getAdminCourtWorker());
    }

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void addCourtWorker_shouldSaveCourtWorker(CourtWorkerType courtWorkerType) {
        adminData = createAdminData(TribunalOffice.LEEDS.getOfficeName(), courtWorkerType.name(), "Code1", "Name1");
        List<String> errors = courtWorkerService.addCourtWorker(adminData);
        assertEquals(0, errors.size());
        verify(courtWorkerRepository, times(1)).save(
                createCourtWorker(TribunalOffice.LEEDS, courtWorkerType, "Code1", "Name1"));
    }

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void addCourtWorker_shouldGiveCodeError(CourtWorkerType courtWorkerType) {
        adminData = createAdminData(TribunalOffice.LEEDS.getOfficeName(), courtWorkerType.name(), "Code1", "Name1");
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(true);
        List<String> errors = courtWorkerService.addCourtWorker(adminData);
        assertEquals(1, errors.size());
        assertEquals(String.format(CODE_ERROR_MESSAGE, "Code1", TribunalOffice.LEEDS.getOfficeName()), errors.get(0));
        verify(courtWorkerRepository, times(0)).save(
                createCourtWorker(TribunalOffice.LEEDS, courtWorkerType, "Code1", "Name1"));
    }

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void addCourtWorker_shouldGiveNameError(CourtWorkerType courtWorkerType) {
        adminData = createAdminData(TribunalOffice.LEEDS.getOfficeName(), courtWorkerType.name(), "Code1", "Name1");
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(true);
        List<String> errors = courtWorkerService.addCourtWorker(adminData);
        assertEquals(1, errors.size());
        assertEquals(String.format(NAME_ERROR_MESSAGE, "Name1", TribunalOffice.LEEDS.getOfficeName()), errors.get(0));
        verify(courtWorkerRepository, times(0)).save(
                createCourtWorker(TribunalOffice.LEEDS, courtWorkerType, "Code4", "Name1"));
    }

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void updateCourtWorkerMidEventSelectOffice_shouldReturnDynamicList(CourtWorkerType courtWorkerType) {
        var tribunalOffice = TribunalOffice.LEEDS;
        var code = "Code1";
        var name = "Name1";

        adminData = createAdminData(tribunalOffice.getOfficeName(), courtWorkerType.name(), code, name);
        adminData.setUpdateCourtWorkerOffice(tribunalOffice.getOfficeName());
        adminData.setUpdateCourtWorkerType(courtWorkerType.name());

        var listCourtWorker = createListCourtWorker(1, TribunalOffice.LEEDS, courtWorkerType, "Code1", "Name1");
        when(courtWorkerRepository.findByTribunalOfficeAndTypeOrderByNameAsc(any(TribunalOffice.class),
                any(CourtWorkerType.class))).thenReturn(listCourtWorker);

        List<String> errors = courtWorkerService.updateCourtWorkerMidEventSelectOffice(adminData);
        assertEquals(0, errors.size());
        assertEquals(1, adminData.getUpdateCourtWorkerSelectList().getListItems().size());
    }

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void updateCourtWorkerMidEventSelectOffice_shouldGiveNotFoundError(CourtWorkerType courtWorkerType) {
        var tribunalOffice = TribunalOffice.LEEDS;
        var code = "Code1";
        var name = "Name1";

        adminData = createAdminData(tribunalOffice.getOfficeName(), courtWorkerType.name(), code, name);
        adminData.setUpdateCourtWorkerOffice(tribunalOffice.getOfficeName());
        adminData.setUpdateCourtWorkerType(courtWorkerType.name());

        List<CourtWorker> listCourtWorker = new ArrayList<>();
        when(courtWorkerRepository.findByTribunalOfficeAndTypeOrderByNameAsc(any(TribunalOffice.class),
                any(CourtWorkerType.class))).thenReturn(listCourtWorker);

        List<String> errors = courtWorkerService.updateCourtWorkerMidEventSelectOffice(adminData);
        assertEquals(1, errors.size());
        assertEquals(String.format(NO_FOUND_ERROR_MESSAGE, courtWorkerType.name(), tribunalOffice.getOfficeName()),
                errors.get(0));
    }

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void updateCourtWorkerMidEventSelectCourtWorker_shouldReturnClerk(CourtWorkerType courtWorkerType) {
        adminData = createAdminDataWithDynamicList("1", TribunalOffice.LEEDS.getOfficeName(), courtWorkerType.name(),
                "Code1", "Name1");

        var listCourtWorker = createListCourtWorker(1, TribunalOffice.LEEDS, courtWorkerType, "Code1", "Name1");
        when(courtWorkerRepository.findById(anyInt())).thenReturn(listCourtWorker);

        List<String> errors = courtWorkerService.updateCourtWorkerMidEventSelectCourtWorker(adminData);
        assertEquals(0, errors.size());
        assertEquals("Code1", adminData.getUpdateCourtWorkerCode());
        assertEquals("Name1", adminData.getUpdateCourtWorkerName());
    }

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void updateCourtWorkerMidEventSelectCourtWorker_shouldGiveError(CourtWorkerType courtWorkerType) {
        adminData = createAdminDataWithDynamicList("1", TribunalOffice.LEEDS.getOfficeName(), courtWorkerType.name(),
                "Code1", "Name1");

        List<CourtWorker> listCourtWorker = new ArrayList<>();
        when(courtWorkerRepository.findById(anyInt())).thenReturn(listCourtWorker);

        List<String> errors = courtWorkerService.updateCourtWorkerMidEventSelectCourtWorker(adminData);
        assertEquals(1, errors.size());
        assertEquals(SAVE_ERROR_MESSAGE, errors.get(0));
    }

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void updateCourtWorker_shouldSaveClerk(CourtWorkerType courtWorkerType) {
        adminData = createAdminDataWithDynamicList("1", TribunalOffice.LEEDS.getOfficeName(), courtWorkerType.name(),
                "Code1", "Name1");
        adminData.setUpdateCourtWorkerName("Name2");

        var listCourtWorker = createListCourtWorker(1, TribunalOffice.LEEDS, courtWorkerType, "Code1", "Name1");
        when(courtWorkerRepository.findById(anyInt())).thenReturn(listCourtWorker);

        List<String> errors = courtWorkerService.updateCourtWorker(adminData);
        assertEquals(0, errors.size());
        verify(courtWorkerRepository, times(1)).save(
                createCourtWorkerWithId(1, TribunalOffice.LEEDS, courtWorkerType, "Code1", "Name2"));
    }

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void updateCourtWorker_shouldReturnError(CourtWorkerType courtWorkerType) {
        adminData = createAdminDataWithDynamicList("1", TribunalOffice.LEEDS.getOfficeName(), courtWorkerType.name(),
                "Code1", "Name1");
        adminData.setUpdateCourtWorkerName("Name2");

        List<CourtWorker> listCourtWorker = new ArrayList<>();
        when(courtWorkerRepository.findById(anyInt())).thenReturn(listCourtWorker);

        List<String> errors = courtWorkerService.updateCourtWorker(adminData);
        assertEquals(1, errors.size());
        assertEquals(SAVE_ERROR_MESSAGE, errors.get(0));
    }

    private AdminData createAdminData(String officeName, String courtWorkerType, String testCode, String testName) {
        var adminCourtWorker = new AdminCourtWorker();
        adminCourtWorker.setTribunalOffice(officeName);
        adminCourtWorker.setCourtWorkerName(testName);
        adminCourtWorker.setCourtWorkerCode(testCode);
        adminCourtWorker.setCourtWorkerType(courtWorkerType);

        var adminData = new AdminData();
        adminData.setAdminCourtWorker(adminCourtWorker);

        return adminData;
    }

    private AdminData createAdminDataWithDynamicList(String id, String tribunalOffice, String courtWorkerType,
                                                     String code, String name) {
        var adminData = createAdminData(tribunalOffice, courtWorkerType, code, name);
        adminData.setUpdateCourtWorkerOffice(tribunalOffice);
        adminData.setUpdateCourtWorkerType(courtWorkerType);

        List<DynamicValueType> listDynamicValueType = new ArrayList<>();
        listDynamicValueType.add(DynamicValueType.create(id, name));

        var courtWorkerDynamicList = new DynamicFixedListType();
        courtWorkerDynamicList.setListItems(listDynamicValueType);
        adminData.setUpdateCourtWorkerSelectList(courtWorkerDynamicList);

        var dynamicValueType = DynamicValueType.create(id, name);
        adminData.getUpdateCourtWorkerSelectList().setValue(dynamicValueType);

        return adminData;
    }

    private CourtWorker createCourtWorker(TribunalOffice tribunalOffice, CourtWorkerType courtWorkerType,
                                          String code, String name) {
        var courtWorker = new CourtWorker();
        courtWorker.setTribunalOffice(tribunalOffice);
        courtWorker.setType(courtWorkerType);
        courtWorker.setCode(code);
        courtWorker.setName(name);
        return courtWorker;
    }

    private CourtWorker createCourtWorkerWithId(int id, TribunalOffice tribunalOffice,
                                                CourtWorkerType courtWorkerType, String code, String name) {
        var courtWorker = createCourtWorker(tribunalOffice, courtWorkerType, code, name);
        courtWorker.setId(id);
        return courtWorker;
    }

    private List<CourtWorker> createListCourtWorker(int id, TribunalOffice tribunalOffice,
                                                    CourtWorkerType courtWorkerType, String code, String name) {
        var courtWorker = createCourtWorkerWithId(id, tribunalOffice, courtWorkerType, code, name);
        List<CourtWorker> listCourtWorker = new ArrayList<>();
        listCourtWorker.add(courtWorker);
        return listCourtWorker;
    }
}
