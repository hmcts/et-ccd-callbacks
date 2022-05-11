package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.clerk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClerkAddService {

    private final CourtWorkerRepository courtWorkerRepository;

    public void addClerk(AdminData adminData) {
        var tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getClerkAdd().getTribunalOffice());
        var clerkCode = adminData.getClerkAdd().getClerkCode();
        var clerkName = adminData.getClerkAdd().getClerkName();

        if (courtWorkerRepository.existsByTribunalOfficeAndAndTypeAndCode(
                tribunalOffice, CourtWorkerType.CLERK, clerkCode)) {
            log.info("Clerk Code should not already exist for this tribunal office.");
        } else if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(
                tribunalOffice, CourtWorkerType.CLERK, clerkName)) {
            log.info("Clerk Name should not already exist for this tribunal office.");
        } else {
            var courtWorker = toSetCourtWorker(tribunalOffice, clerkCode, clerkName);
            courtWorkerRepository.save(courtWorker);
        }
    }

    private CourtWorker toSetCourtWorker(TribunalOffice tribunalOffice, String clerkCode, String clerkName) {
        var courtWorker = new CourtWorker();
        courtWorker.setType(CourtWorkerType.CLERK);
        courtWorker.setCode(clerkCode);
        courtWorker.setName(clerkName);
        courtWorker.setTribunalOffice(tribunalOffice);
        return courtWorker;
    }

}
