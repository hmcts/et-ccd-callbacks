package uk.gov.hmcts.ethos.replacement.docmosis.service.admin.staff;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
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
    public static final String NO_FOUND_ERROR_MESSAGE = "No %s court worker found in the %s office";
    public static final String SAVE_ERROR_MESSAGE = "Update failed";

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

    public List<String> updateCourtWorkerMidEventSelectOffice(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var tribunalOffice = adminData.getUpdateCourtWorkerOffice();
        var courtWorkerType = adminData.getUpdateCourtWorkerType();

        List<CourtWorker> courtWorkerList = courtWorkerRepository.findByTribunalOfficeAndType(
                TribunalOffice.valueOfOfficeName(tribunalOffice),
                CourtWorkerType.valueOf(courtWorkerType));

        if (courtWorkerList.isEmpty()) {
            errors.add(String.format(NO_FOUND_ERROR_MESSAGE, courtWorkerType, tribunalOffice));
            return errors;
        }

        List<DynamicValueType> dynamicCourtWorker = new ArrayList<>();
        for (var courtWorker : courtWorkerList) {
            dynamicCourtWorker.add(DynamicValueType.create(courtWorker.getId().toString(), courtWorker.getName()));
        }

        var courtWorkerDynamicList = new DynamicFixedListType();
        courtWorkerDynamicList.setListItems(dynamicCourtWorker);

        adminData.setUpdateCourtWorkerDynamicList(courtWorkerDynamicList);

        return errors;
    }

    public List<String> updateCourtWorkerMidEventSelectClerk(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var selectedId = Integer.parseInt(adminData.getUpdateCourtWorkerDynamicList().getSelectedCode());

        var findCourtWorker = courtWorkerRepository.findById(selectedId);
        if (findCourtWorker.isPresent()) {
            var selectedCourtWorker = findCourtWorker.get();
            adminData.setUpdateCourtWorkerCode(selectedCourtWorker.getCode());
            adminData.setUpdateCourtWorkerName(selectedCourtWorker.getName());
        } else {
            errors.add(SAVE_ERROR_MESSAGE);
        }

        return errors;
    }

    public List<String> updateCourtWorker(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        var selectedId = Integer.parseInt(adminData.getUpdateCourtWorkerDynamicList().getSelectedCode());

        var findCourtWorker = courtWorkerRepository.findById(selectedId);
        if (findCourtWorker.isPresent()) {
            var thisCourtWorker = findCourtWorker.get();
            thisCourtWorker.setName(adminData.getUpdateCourtWorkerName());
            courtWorkerRepository.save(thisCourtWorker);
        } else {
            errors.add(SAVE_ERROR_MESSAGE);
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