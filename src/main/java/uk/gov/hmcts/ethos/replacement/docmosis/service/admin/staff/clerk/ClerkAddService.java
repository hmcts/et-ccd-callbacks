package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.clerk;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ClerkAddService {

    private final CourtWorkerRepository courtWorkerRepository;

    static final String EXIST_CLERK_CODE_ERROR_MESSAGE = "Clerk Code exists for this tribunal office.";
    static final String EXIST_CLERK_NAME_ERROR_MESSAGE = "Clerk Name exists for this tribunal office.";

    public List<String>  addClerk(AdminData adminData) {
        List<String> errors = new ArrayList<>();

        var tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getClerkAdd().getTribunalOffice());
        var clerkCode = adminData.getClerkAdd().getClerkCode();
        var clerkName = adminData.getClerkAdd().getClerkName();

        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(
                tribunalOffice, CourtWorkerType.CLERK, clerkCode)) {
            errors.add(EXIST_CLERK_CODE_ERROR_MESSAGE);
        }

        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(
                tribunalOffice, CourtWorkerType.CLERK, clerkName)) {
            errors.add(EXIST_CLERK_NAME_ERROR_MESSAGE);
        }

        if (errors.isEmpty()) {
            var courtWorker = createCourtWorker(tribunalOffice, clerkCode, clerkName);
            courtWorkerRepository.save(courtWorker);
        }

        return errors;
    }

    private CourtWorker createCourtWorker(TribunalOffice tribunalOffice, String clerkCode, String clerkName) {
        var courtWorker = new CourtWorker();
        courtWorker.setType(CourtWorkerType.CLERK);
        courtWorker.setCode(clerkCode);
        courtWorker.setName(clerkName);
        courtWorker.setTribunalOffice(tribunalOffice);
        return courtWorker;
    }

}
