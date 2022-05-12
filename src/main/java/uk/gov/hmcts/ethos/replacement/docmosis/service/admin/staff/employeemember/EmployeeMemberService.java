package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.employeemember;

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
public class EmployeeMemberService {

    public static final String CODE_ERROR_MESSAGE = "The code %s already exists for the %s office";
    public static final String NAME_ERROR_MESSAGE = "The name %s already exists for the %s office";

    private final CourtWorkerRepository courtWorkerRepository;

    public List<String> addEmployeeMember(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var tribunalOffice = TribunalOffice.valueOfOfficeName(
                adminData.getEmployeeMember().getTribunalOffice());
        var employeeMemberCode = adminData.getEmployeeMember().getEmployeeMemberCode();
        var employeeMemberName = adminData.getEmployeeMember().getEmployeeMemberName();

        var courtWorker = setCourtWorker(tribunalOffice, employeeMemberCode, employeeMemberName);

        if (checkIfEmployeeMemberExists(courtWorker, tribunalOffice, errors)) {
            courtWorkerRepository.save(courtWorker);
        }

        return errors;
    }

    private boolean checkIfEmployeeMemberExists(CourtWorker courtWorker, TribunalOffice tribunalOffice,
                                                List<String> errors) {
        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(tribunalOffice, CourtWorkerType.EMPLOYEE_MEMBER,
                courtWorker.getCode())) {
            errors.add(String.format(CODE_ERROR_MESSAGE, courtWorker.getCode(), tribunalOffice.getOfficeName()));
        }

        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(tribunalOffice, CourtWorkerType.EMPLOYEE_MEMBER,
                courtWorker.getName())) {
            errors.add(String.format(NAME_ERROR_MESSAGE, courtWorker.getName(), tribunalOffice.getOfficeName()));
        }

        return errors.isEmpty();
    }

    private CourtWorker setCourtWorker(TribunalOffice tribunalOffice, String employeeMemberCode,
                                       String employeeMemberName) {
        var courtWorker = new CourtWorker();
        courtWorker.setCode(employeeMemberCode);
        courtWorker.setName(employeeMemberName);
        courtWorker.setType(CourtWorkerType.EMPLOYEE_MEMBER);
        courtWorker.setTribunalOffice(tribunalOffice);

        return courtWorker;
    }
}
