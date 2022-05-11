package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.clerk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ClerkAdd;
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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.clerk.ClerkAddService.EXIST_CLERK_CODE_ERROR_MESSAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.clerk.ClerkAddService.EXIST_CLERK_NAME_ERROR_MESSAGE;

class ClerkAddServiceTest {

    private CourtWorkerRepository courtWorkerRepository;
    private ClerkAdd clerkAdd;
    private AdminData adminData;

    @BeforeEach
    void setup() {
        courtWorkerRepository = mock(CourtWorkerRepository.class);

        var tribunalOffice = TribunalOffice.MANCHESTER;
        var clerkCode = "clerkCode";
        var clerkName = "Clerk Name";

        clerkAdd = new ClerkAdd();
        clerkAdd.setTribunalOffice(tribunalOffice.getOfficeName());
        clerkAdd.setClerkCode(clerkCode);
        clerkAdd.setClerkName(clerkName);

        adminData = new AdminData();
        adminData.setClerkAdd(clerkAdd);
    }

    @Test
    void addClerk_NewCode_NewName_Save() {
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString()))
                .thenReturn(false);
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString()))
                .thenReturn(false);
        var clerkAddService = new ClerkAddService(courtWorkerRepository);
        List<String> errors = clerkAddService.addClerk(adminData);
        assertEquals(0, errors.size());
        verify(courtWorkerRepository, times(1)).save(any(CourtWorker.class));
    }

    @Test
    void addClerk_OldCode_NewName_NoSave() {
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString()))
                .thenReturn(true);
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString()))
                .thenReturn(false);
        var clerkAddService = new ClerkAddService(courtWorkerRepository);
        List<String> errors = clerkAddService.addClerk(adminData);
        assertEquals(1, errors.size());
        assertEquals(EXIST_CLERK_CODE_ERROR_MESSAGE, errors.get(0));
        verify(courtWorkerRepository, times(0)).save(any(CourtWorker.class));
    }

    @Test
    void addClerk_NewCode_OldName_NoSave() {
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString()))
                .thenReturn(false);
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString()))
                .thenReturn(true);
        var clerkAddService = new ClerkAddService(courtWorkerRepository);
        List<String> errors = clerkAddService.addClerk(adminData);
        assertEquals(1, errors.size());
        assertEquals(EXIST_CLERK_NAME_ERROR_MESSAGE, errors.get(0));
        verify(courtWorkerRepository, times(0)).save(any(CourtWorker.class));
    }

    @Test
    void addClerk_OldCode_OldName_NoSave() {
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString()))
                .thenReturn(true);
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString()))
                .thenReturn(true);
        var clerkAddService = new ClerkAddService(courtWorkerRepository);
        List<String> errors = clerkAddService.addClerk(adminData);
        assertEquals(2, errors.size());
        assertEquals(EXIST_CLERK_CODE_ERROR_MESSAGE, errors.get(0));
        assertEquals(EXIST_CLERK_NAME_ERROR_MESSAGE, errors.get(1));
        verify(courtWorkerRepository, times(0)).save(any(CourtWorker.class));
    }

}