package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.employermember;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.EmployerMember;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.employermember.EmployerMemberService.CODE_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.employermember.EmployerMemberService.NAME_ERROR_MESSAGE;

class EmployerMemberServiceTest {

    private CourtWorkerRepository courtWorkerRepository;
    private EmployerMemberService employerMemberService;
    private CourtWorker courtWorker;
    private AdminData adminData;

    @BeforeEach
    void setUp() {
        courtWorkerRepository = mock(CourtWorkerRepository.class);
        employerMemberService = new EmployerMemberService(courtWorkerRepository);

        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(false);
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(false);

        courtWorker = new CourtWorker();
        courtWorker.setTribunalOffice(TribunalOffice.LEEDS);
        courtWorker.setType(CourtWorkerType.EMPLOYER_MEMBER);
        courtWorker.setCode("Code1");
        courtWorker.setName("Name1");

        var employerMember = new EmployerMember();
        employerMember.setTribunalOffice(courtWorker.getTribunalOffice().getOfficeName());
        employerMember.setEmployerMemberCode(courtWorker.getCode());
        employerMember.setEmployerMemberName(courtWorker.getName());
        adminData = new AdminData();
        adminData.setEmployerMember(employerMember);

    }

    @Test
    void shouldSaveEmployerMember() {
        List<String> errors = employerMemberService.addEmployerMember(adminData);
        assertEquals(0, errors.size());
        verify(courtWorkerRepository, times(1)).save(courtWorker);
    }

    @Test
    void shouldGiveCodeError() {
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(true);
        List<String> errors = employerMemberService.addEmployerMember(adminData);
        assertEquals(1, errors.size());
        assertEquals(String.format(CODE_ERROR_MESSAGE, courtWorker.getCode(),
                courtWorker.getTribunalOffice().getOfficeName()), errors.get(0));
        verify(courtWorkerRepository, times(0)).save(any(CourtWorker.class));
    }

    @Test
    void shouldGiveNameError() {
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString())).thenReturn(true);
        List<String> errors = employerMemberService.addEmployerMember(adminData);
        assertEquals(1, errors.size());
        assertEquals(String.format(NAME_ERROR_MESSAGE, courtWorker.getName(),
                courtWorker.getTribunalOffice().getOfficeName()), errors.get(0));
        verify(courtWorkerRepository, times(0)).save(any(CourtWorker.class));
    }

}