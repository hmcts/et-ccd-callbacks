package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.employermember;

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
public class EmployerMemberService {

    private final CourtWorkerRepository courtWorkerRepository;

    static final String CODE_ERROR_MESSAGE = "The code %s already exists for the %s office";
    static final String NAME_ERROR_MESSAGE = "The name %s already exists for the %s office";

    public List<String> addEmployerMember(AdminData adminData) {
        List<String> errors = new ArrayList<>();

        var tribunalOffice = TribunalOffice.valueOfOfficeName(adminData.getEmployerMember().getTribunalOffice());
        var courtWorkerType = CourtWorkerType.EMPLOYER_MEMBER;
        var employerMemberCode = adminData.getEmployerMember().getEmployerMemberCode();
        var employerMemberName = adminData.getEmployerMember().getEmployerMemberName();
        var courtWorker = setCourtWorker(tribunalOffice, courtWorkerType, employerMemberCode, employerMemberName);

        if (checkIfEmployerMemberExists(courtWorker, errors)) {
            courtWorkerRepository.save(courtWorker);
        }

        return errors;
    }

    private boolean checkIfEmployerMemberExists(CourtWorker courtWorker, List<String> errors) {
        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(
                courtWorker.getTribunalOffice(), courtWorker.getType(), courtWorker.getCode())) {
            errors.add(String.format(CODE_ERROR_MESSAGE, courtWorker.getCode(),
                    courtWorker.getTribunalOffice().getOfficeName()));
        }

        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(
                courtWorker.getTribunalOffice(), courtWorker.getType(), courtWorker.getName())) {
            errors.add(String.format(NAME_ERROR_MESSAGE, courtWorker.getName(),
                    courtWorker.getTribunalOffice().getOfficeName()));
        }

        return errors.isEmpty();
    }

    private CourtWorker setCourtWorker(TribunalOffice tribunalOffice, CourtWorkerType courtWorkerType, String code,
                                       String name) {
        var courtWorker = new CourtWorker();
        courtWorker.setTribunalOffice(tribunalOffice);
        courtWorker.setType(courtWorkerType);
        courtWorker.setCode(code);
        courtWorker.setName(name);

        return courtWorker;
    }

}
