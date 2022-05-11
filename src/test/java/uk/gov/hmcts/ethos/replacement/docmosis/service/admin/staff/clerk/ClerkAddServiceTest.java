package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.clerk;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.ClerkAdd;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClerkAddServiceTest {

    private CourtWorkerRepository courtWorkerRepository;
    private ClerkAddService clerkAddService;
    private AdminData adminData;

    @BeforeEach
    void setup() {
        courtWorkerRepository = mock(CourtWorkerRepository.class);
        clerkAddService = mock(ClerkAddService.class);

        var tribunalOffice = TribunalOffice.MANCHESTER;
        var clerkCode = "clerkCode";
        var clerkName = "Clerk Name";

        var clerkAdd = new ClerkAdd();
        clerkAdd.setTribunalOffice(tribunalOffice.getOfficeName());
        clerkAdd.setClerkCode(clerkCode);
        clerkAdd.setClerkName(clerkName);

        adminData = new AdminData();
        adminData.setClerkAdd(clerkAdd);
    }

    @Test
    void addClerk_New_Success() {
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString()))
                .thenReturn(false);
        when(courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(any(TribunalOffice.class),
                any(CourtWorkerType.class), anyString()))
                .thenReturn(false);
        clerkAddService.addClerk(adminData);
        verify(courtWorkerRepository, times(1)).save(any(CourtWorker.class));
    }

}