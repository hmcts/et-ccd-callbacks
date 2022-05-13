package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.courtworker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.AdminCourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.courtworker.CourtWorkerService.CODE_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.courtworker.CourtWorkerService.NAME_ERROR_MESSAGE;

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

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void shouldSaveCourtWorker(CourtWorkerType courtWorkerType) {
        adminData = createAdminData(TribunalOffice.LEEDS.getOfficeName(), courtWorkerType.name(), "Code1", "Name1");
        List<String> errors = courtWorkerService.addCourtWorker(adminData);
        assertEquals(0, errors.size());
        verify(courtWorkerRepository, times(1)).save(
                createCourtWorker(TribunalOffice.LEEDS, courtWorkerType, "Code1", "Name1"));
    }

    @ParameterizedTest
    @EnumSource(CourtWorkerType.class)
    void shouldGiveCodeError(CourtWorkerType courtWorkerType) {
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
    void shouldGiveNameError(CourtWorkerType courtWorkerType) {
        adminData = createAdminData(TribunalOffice.LEEDS.getOfficeName(), courtWorkerType.name(), "Code1", "Name1");
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(true);
        List<String> errors = courtWorkerService.addCourtWorker(adminData);
        assertEquals(1, errors.size());
        assertEquals(String.format(NAME_ERROR_MESSAGE, "Name1", TribunalOffice.LEEDS.getOfficeName()), errors.get(0));
        verify(courtWorkerRepository, times(0)).save(
                createCourtWorker(TribunalOffice.LEEDS, courtWorkerType, "Code4", "Name1"));
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

    private CourtWorker createCourtWorker(TribunalOffice tribunalOffice, CourtWorkerType courtWorkerType,
                                          String code, String name) {
        var courtWorker = new CourtWorker();
        courtWorker.setTribunalOffice(tribunalOffice);
        courtWorker.setType(courtWorkerType);
        courtWorker.setCode(code);
        courtWorker.setName(name);
        return courtWorker;
    }
}
