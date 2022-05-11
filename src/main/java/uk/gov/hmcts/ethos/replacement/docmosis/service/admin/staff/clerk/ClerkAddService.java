package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.clerk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ClerkAddService {

    private final CourtWorkerRepository courtWorkerRepository;

    public void addClerk(AdminData adminData) {

        var tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getClerkAdd().getTribunalOffice());
        var clerkCode = adminData.getClerkAdd().getClerkCode();
        var clerkName = adminData.getClerkAdd().getClerkName();
        var courtWorker = toSetCourtWorker(tribunalOffice, clerkCode, clerkName);
        List<CourtWorker> listExistCourtWorker =
                courtWorkerRepository.findByTribunalOfficeAndType(tribunalOffice, CourtWorkerType.CLERK);
        if (existClerkCode(listExistCourtWorker, clerkCode)) {
            log.info("Clerk Code should not already exist for this tribunal office.");
        } else if (existClerkName(listExistCourtWorker, clerkName)) {
            log.info("Clerk Name should not already exist for this tribunal office.");
        } else {
            courtWorkerRepository.save(courtWorker);
        }
    }

    private boolean existClerkCode(final List<CourtWorker> list, final String clerkCode) {
        return list.stream().anyMatch(o -> o.getCode().equals(clerkCode));
    }

    private boolean existClerkName(final List<CourtWorker> list, final String clerkName) {
        return list.stream().anyMatch(o -> o.getName().equals(clerkName));
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
