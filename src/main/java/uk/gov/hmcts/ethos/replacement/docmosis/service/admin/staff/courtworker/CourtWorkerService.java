package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff.courtworker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.AdminData;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.admin.types.AdminCourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorker;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.referencedata.CourtWorkerType;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.CourtWorkerRepository;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;

@Slf4j
@RequiredArgsConstructor
@Service
public class CourtWorkerService {

    public static final String CODE_ERROR_MESSAGE = "The code %s already exists for the %s office";
    public static final String NAME_ERROR_MESSAGE = "The name %s already exists for the %s office";
    private static final String NO_COURT_WORKERS_FOUND = "No %s was found for the %s office";

    private final CourtWorkerRepository courtWorkerRepository;

    public List<String> addCourtWorker(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var tribunalOffice = TribunalOffice.valueOfOfficeName(
                adminData.getAdminCourtWorker().getTribunalOffice());
        var courtWorkerRole = CourtWorkerType.valueOf(adminData.getAdminCourtWorker().getCourtWorkerType());
        var courtWorkerCode = adminData.getAdminCourtWorker().getCourtWorkerCode();
        var courtWorkerName = adminData.getAdminCourtWorker().getCourtWorkerName();

        var courtWorker = setCourtWorker(tribunalOffice, courtWorkerRole, courtWorkerCode, courtWorkerName);

        if (checkIfEmployeeMemberExists(courtWorker, errors)) {
            courtWorkerRepository.save(courtWorker);
        }

        return errors;
    }

    private boolean checkIfEmployeeMemberExists(CourtWorker courtWorker, List<String> errors) {
        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndCode(courtWorker.getTribunalOffice(),
                courtWorker.getType(), courtWorker.getCode())) {
            errors.add(String.format(CODE_ERROR_MESSAGE, courtWorker.getCode(),
                    courtWorker.getTribunalOffice().getOfficeName()));
        }

        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(courtWorker.getTribunalOffice(),
                courtWorker.getType(), courtWorker.getName())) {
            errors.add(String.format(NAME_ERROR_MESSAGE, courtWorker.getName(),
                    courtWorker.getTribunalOffice().getOfficeName()));
        }

        return errors.isEmpty();
    }

    private CourtWorker setCourtWorker(TribunalOffice tribunalOffice, CourtWorkerType courtWorkerType,
                                       String employeeMemberCode, String employeeMemberName) {
        var courtWorker = new CourtWorker();
        courtWorker.setCode(employeeMemberCode);
        courtWorker.setName(employeeMemberName);
        courtWorker.setType(courtWorkerType);
        courtWorker.setTribunalOffice(tribunalOffice);

        return courtWorker;
    }

    public List<String> updateCourtWorkerMidEvent(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        List<CourtWorker> courtWorkerList = courtWorkerRepository.findByTribunalOfficeAndType(
                TribunalOffice.valueOfOfficeName(adminData.getTribunalOffice()),
                CourtWorkerType.valueOf(adminData.getCourtWorkerType()));
        if (courtWorkerList.isEmpty()) {
            errors.add(String.format(NO_COURT_WORKERS_FOUND, adminData.getCourtWorkerType(),
                    adminData.getTribunalOffice()));
            return errors;
        }

        List<DynamicValueType> dynamicCourtWorker = new ArrayList<>();
        for (var courtWorker : courtWorkerList) {
            dynamicCourtWorker.add(DynamicValueType.create(courtWorker.getId().toString(), courtWorker.getName()));
        }

        var courtWorkerDynamicList = new DynamicFixedListType();
        courtWorkerDynamicList.setListItems(dynamicCourtWorker);

        var adminCourtWorker   = new AdminCourtWorker();
        adminCourtWorker.setDynamicCourtWorkerList(courtWorkerDynamicList);
        adminCourtWorker.setTribunalOffice(adminData.getTribunalOffice());
        adminCourtWorker.setCourtWorkerType(adminData.getCourtWorkerType());
        adminData.setAdminCourtWorker(adminCourtWorker);

        return errors;
    }

    public List<String> updateCourtWorker(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var adminCourtWorker = adminData.getAdminCourtWorker();

        if (isNullOrEmpty(adminCourtWorker.getCourtWorkerName())) {
            errors.add("Court worker name cannot be blank");
            return errors;
        }

        if (courtWorkerRepository.existsByTribunalOfficeAndTypeAndName(
                TribunalOffice.valueOfOfficeName(adminCourtWorker.getTribunalOffice()),
                CourtWorkerType.valueOf(adminCourtWorker.getCourtWorkerType()),
                adminCourtWorker.getCourtWorkerName())) {
            errors.add("The name " + adminCourtWorker.getCourtWorkerName() + " already exists");
        } else {
            courtWorkerRepository.updateCourtWorkerName(adminCourtWorker.getCourtWorkerName(),
                    Integer.parseInt(adminCourtWorker.getDynamicCourtWorkerList().getSelectedCode()));
        }

        return errors;
    }
}
