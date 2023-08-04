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
    public static final String NO_WORKER_CODE_FOUND_ERROR_MESSAGE =
            "No court worker found with the worker code %s";
    public static final String SAVE_ERROR_MESSAGE = "Update failed";

    private final CourtWorkerRepository courtWorkerRepository;

    public void initAddCourtWorker(AdminData adminData) {
        adminData.setAdminCourtWorker(null);
    }

    public List<String> addCourtWorker(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        CourtWorker courtWorker = setCourtWorker(adminData);

        checkIfCourtWorkerCodeExists(courtWorker, errors);
        checkIfCourtWorkerNameExists(courtWorker, errors);
        if (errors.isEmpty()) {
            courtWorkerRepository.save(courtWorker);
        }

        return errors;
    }

    public List<String> getCourtWorkerMidEventSelectOffice(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        String tribunalOffice = adminData.getCourtWorkerOffice();
        String courtWorkerType = adminData.getCourtWorkerType();

        List<CourtWorker> courtWorkerList = courtWorkerRepository.findByTribunalOfficeAndTypeOrderByNameAsc(
                TribunalOffice.valueOfOfficeName(tribunalOffice), CourtWorkerType.valueOf(courtWorkerType));

        if (courtWorkerList.isEmpty()) {
            errors.add(String.format(NO_FOUND_ERROR_MESSAGE, courtWorkerType, tribunalOffice));
            return errors;
        }

        List<DynamicValueType> dynamicCourtWorker = new ArrayList<>();
        for (CourtWorker courtWorker : courtWorkerList) {
            dynamicCourtWorker.add(DynamicValueType.create(courtWorker.getCode(), courtWorker.getName()));
        }

        DynamicFixedListType courtWorkerDynamicList = new DynamicFixedListType();
        courtWorkerDynamicList.setListItems(dynamicCourtWorker);

        adminData.setCourtWorkerSelectList(courtWorkerDynamicList);

        return errors;
    }

    public List<String> getCourtWorkerMidEventSelectCourtWorker(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        String selectedCode = adminData.getCourtWorkerSelectList().getSelectedCode();

        CourtWorker selectedCourtWorker = courtWorkerRepository.findByCodeAndTribunalOfficeAndType(selectedCode,
                TribunalOffice.valueOfOfficeName(adminData.getCourtWorkerOffice()),
                CourtWorkerType.valueOf(adminData.getCourtWorkerType()));
        if (selectedCourtWorker != null) {

            adminData.setCourtWorkerCode(selectedCourtWorker.getCode());
            adminData.setCourtWorkerName(selectedCourtWorker.getName());
        } else {
            errors.add(SAVE_ERROR_MESSAGE);
        }

        return errors;
    }

    public List<String> updateCourtWorker(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        String selectedCode = adminData.getCourtWorkerSelectList().getSelectedCode();

        CourtWorker selectedCourtWorker = courtWorkerRepository.findByCodeAndTribunalOfficeAndType(selectedCode,
                TribunalOffice.valueOfOfficeName(adminData.getCourtWorkerOffice()),
                CourtWorkerType.valueOf(adminData.getCourtWorkerType()));

        if (selectedCourtWorker != null) {
            selectedCourtWorker.setName(adminData.getCourtWorkerName());
            checkIfCourtWorkerNameExists(selectedCourtWorker, errors);
            if (errors.isEmpty()) {
                courtWorkerRepository.save(selectedCourtWorker);
            }
        } else {
            errors.add(SAVE_ERROR_MESSAGE);
        }

        return errors;
    }

    public List<String> deleteCourtWorker(AdminData adminData) {
        List<String> errors = new ArrayList<>();
        String selectedCode = adminData.getAdminCourtWorker().getCourtWorkerCode();

        CourtWorker selectedCourtWorker = courtWorkerRepository.findByCodeAndTribunalOfficeAndType(selectedCode,
                TribunalOffice.valueOfOfficeName(adminData.getAdminCourtWorker().getTribunalOffice()),
                CourtWorkerType.valueOf(adminData.getAdminCourtWorker().getCourtWorkerType()));

        if (selectedCourtWorker != null) {
            courtWorkerRepository.delete(selectedCourtWorker);
            courtWorkerRepository.flush();
        } else {
            errors.add(String.format(NO_WORKER_CODE_FOUND_ERROR_MESSAGE, selectedCode));
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
        TribunalOffice tribunalOffice = TribunalOffice.valueOfOfficeName(
                adminData.getAdminCourtWorker().getTribunalOffice());
        CourtWorkerType courtWorkerType = CourtWorkerType.valueOf(
                adminData.getAdminCourtWorker().getCourtWorkerType());
        String courtWorkerCode = adminData.getAdminCourtWorker().getCourtWorkerCode();
        String courtWorkerName = adminData.getAdminCourtWorker().getCourtWorkerName();

        CourtWorker courtWorker = new CourtWorker();
        courtWorker.setTribunalOffice(tribunalOffice);
        courtWorker.setType(courtWorkerType);
        courtWorker.setCode(courtWorkerCode);
        courtWorker.setName(courtWorkerName);

        return courtWorker;
    }
}