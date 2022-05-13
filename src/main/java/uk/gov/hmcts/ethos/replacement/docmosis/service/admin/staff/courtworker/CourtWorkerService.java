package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.courtworker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CourtWorkerService {

    public static final String CODE_ERROR_MESSAGE = "The code %s already exists for the %s office";
    public static final String NAME_ERROR_MESSAGE = "The name %s already exists for the %s office";

    private final CourtWorkerRepository courtWorkerRepository;

    public List<String> addCourtWorker(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var courtWorker = setCourtWorker(adminData);

        checkIfCourtWorkerCodeExists(courtWorker, errors);
        checkIfCourtWorkerNameExists(courtWorker, errors);
        if (errors.isEmpty()) {
            courtWorkerRepository.save(courtWorker);
        }

        return errors;
    }

    public List<String> updateCourtWorker(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var courtWorker = setCourtWorker(adminData);

        checkIfCourtWorkerNameExists(courtWorker, errors);
        if (errors.isEmpty()) {
            courtWorkerRepository.save(courtWorker);
        }

        return errors;
    }

    private void checkIfCourtWorkerCodeExists(CourtWorker courtWorker, List<String> errors) {
        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(courtWorker.getTribunalOffice(),
                courtWorker.getType(), courtWorker.getCode())) {
            errors.add(String.format(CODE_ERROR_MESSAGE, courtWorker.getCode(),
                    courtWorker.getTribunalOffice().getOfficeName()));
        }
    }

    private void checkIfCourtWorkerNameExists(CourtWorker courtWorker, List<String> errors) {
        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(courtWorker.getTribunalOffice(),
                courtWorker.getType(), courtWorker.getName())) {
            errors.add(String.format(NAME_ERROR_MESSAGE, courtWorker.getName(),
                    courtWorker.getTribunalOffice().getOfficeName()));
        }
    }

    private CourtWorker setCourtWorker(AdminData adminData) {
        var tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getAdminCourtWorker().getTribunalOffice());
        var courtWorkerType = CourtWorkerType.valueOf(adminData.getAdminCourtWorker().getCourtWorkerType());
        var courtWorkerCode = adminData.getAdminCourtWorker().getCourtWorkerCode();
        var courtWorkerName = adminData.getAdminCourtWorker().getCourtWorkerName();

        var courtWorker = new CourtWorker();
        courtWorker.setTribunalOffice(tribunalOffice);
        courtWorker.setType(courtWorkerType);
        courtWorker.setCode(courtWorkerCode);
        courtWorker.setName(courtWorkerName);

        return courtWorker;
    }
}
