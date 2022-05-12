package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.employeemember;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.EmployeeMember;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.employeemember.EmployeeMemberService.CODE_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.employeemember.EmployeeMemberService.NAME_ERROR_MESSAGE;

class EmployeeMemberServiceTest {

    private CourtWorkerRepository courtWorkerRepository;
    private EmployeeMemberService employeeMemberService;
    private AdminData adminData;

    @BeforeEach
    void setUp() {
        courtWorkerRepository = mock(CourtWorkerRepository.class);
        employeeMemberService = new EmployeeMemberService(courtWorkerRepository);

        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(false);
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(false);

        adminData = createAdminData(TribunalOffice.LEEDS.getOfficeName(), "Code1", "Name1");;
    }

    @Test
    void shouldSaveEmployeeMember() {
        List<String> errors = employeeMemberService.addEmployeeMember(adminData);
        assertEquals(0, errors.size());
        verify(courtWorkerRepository, times(1)).save(
                createCourtWorker(TribunalOffice.LEEDS, CourtWorkerType.EMPLOYEE_MEMBER,"Code1", "Name1"));
    }

    @Test
    void shouldGiveCodeError() {
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(true);
        List<String> errors = employeeMemberService.addEmployeeMember(adminData);
        assertEquals(1, errors.size());
        assertEquals(String.format(CODE_ERROR_MESSAGE, "Code1", TribunalOffice.LEEDS.getOfficeName()), errors.get(0));
        verify(courtWorkerRepository, times(0)).save(
                createCourtWorker(TribunalOffice.LEEDS, CourtWorkerType.EMPLOYEE_MEMBER,"Code1", "Name1"));
    }

    @Test
    void shouldGiveNameError() {
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(true);
        List<String> errors = employeeMemberService.addEmployeeMember(adminData);
        assertEquals(1, errors.size());
        assertEquals(String.format(NAME_ERROR_MESSAGE, "Name1", TribunalOffice.LEEDS.getOfficeName()), errors.get(0));
        verify(courtWorkerRepository, times(0)).save(
                createCourtWorker(TribunalOffice.LEEDS, CourtWorkerType.EMPLOYEE_MEMBER,"Code4", "Name1"));
    }


    private AdminData createAdminData(String officeName, String testCode, String testName) {
        var employeeMember = new EmployeeMember();
        employeeMember.setTribunalOffice(officeName);
        employeeMember.setEmployeeMemberName(testName);
        employeeMember.setEmployeeMemberCode(testCode);

        var adminData = new AdminData();
        adminData.setEmployeeMember(employeeMember);

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
