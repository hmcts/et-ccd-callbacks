package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.List;

public final class CaseTransferOfficeService {

    private CaseTransferOfficeService() {
        // All access through static methods
    }

    public static void populateTransferToEnglandWalesOfficeOptions(CaseData caseData) {
        String managingOffice = caseData.getManagingOffice();
        if (StringUtils.isBlank(managingOffice)) {
            return;
        }

        DynamicFixedListType offices = getOffices();
        caseData.setOfficeCT(offices);
        caseData.setAssignOffice(offices);
    }

    public static void populateTransferToEnglandWalesOfficeOptions(MultipleData multipleData) {
        String managingOffice = multipleData.getManagingOffice();
        if (StringUtils.isBlank(managingOffice)) {
            return;
        }

        DynamicFixedListType offices = getOffices();
        multipleData.setOfficeMultipleCT(offices);
    }

    public static void populateTransferToScotlandOfficeOptions(CaseData caseData) {
        List<DynamicValueType> tribunalOffices = TribunalOffice.SCOTLAND_OFFICES.stream()
                .map(tribunalOffice ->
                        DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
                .toList();

        DynamicFixedListType officeCT = DynamicFixedListType.from(tribunalOffices);
        String defaultSelectedOffice = TribunalOffice.GLASGOW.getOfficeName();
        officeCT.setValue(DynamicValueType.create(defaultSelectedOffice, defaultSelectedOffice));
        caseData.setOfficeCT(officeCT);
    }

    public static void populateTransferToScotlandOfficeOptions(MultipleData multipleData) {
        List<DynamicValueType> tribunalOffices = TribunalOffice.SCOTLAND_OFFICES.stream()
                .map(tribunalOffice ->
                        DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
                .toList();

        DynamicFixedListType officeCT = DynamicFixedListType.from(tribunalOffices);
        String defaultSelectedOffice = TribunalOffice.GLASGOW.getOfficeName();
        officeCT.setValue(DynamicValueType.create(defaultSelectedOffice, defaultSelectedOffice));
        multipleData.setOfficeMultipleCT(officeCT);
    }

    private static DynamicFixedListType getOffices() {
        List<DynamicValueType> tribunalOffices = TribunalOffice.ENGLANDWALES_OFFICES.stream()
                .map(tribunalOffice ->
                        DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
                .toList();
        return DynamicFixedListType.from(tribunalOffices);
    }
}
