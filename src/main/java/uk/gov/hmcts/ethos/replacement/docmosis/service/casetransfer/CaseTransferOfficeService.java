package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.multiples.MultipleData;

import java.util.stream.Collectors;

public class CaseTransferOfficeService {

    private CaseTransferOfficeService() {
        // All access through static methods
    }

    public static void populateTransferToEnglandWalesOfficeOptions(CaseData caseData) {
        var managingOffice = caseData.getManagingOffice();
        if (StringUtils.isBlank(managingOffice)) {
            return;
        }

        var offices = getOffices(managingOffice);
        caseData.setOfficeCT(offices);
    }

    public static void populateTransferToEnglandWalesOfficeOptions(MultipleData multipleData) {
        var managingOffice = multipleData.getManagingOffice();
        if (StringUtils.isBlank(managingOffice)) {
            return;
        }

        var offices = getOffices(managingOffice);
        multipleData.setOfficeMultipleCT(offices);
    }

    public static void populateTransferToScotlandOfficeOptions(CaseData caseData) {
        var tribunalOffices = TribunalOffice.SCOTLAND_OFFICES.stream()
                .map(tribunalOffice ->
                        DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
                .collect(Collectors.toList());

        var officeCT = DynamicFixedListType.from(tribunalOffices);
        var defaultSelectedOffice = TribunalOffice.GLASGOW.getOfficeName();
        officeCT.setValue(DynamicValueType.create(defaultSelectedOffice, defaultSelectedOffice));
        caseData.setOfficeCT(officeCT);
    }

    public static void populateTransferToScotlandOfficeOptions(MultipleData multipleData) {
        var tribunalOffices = TribunalOffice.SCOTLAND_OFFICES.stream()
                .map(tribunalOffice ->
                        DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
                .collect(Collectors.toList());

        var officeCT = DynamicFixedListType.from(tribunalOffices);
        var defaultSelectedOffice = TribunalOffice.GLASGOW.getOfficeName();
        officeCT.setValue(DynamicValueType.create(defaultSelectedOffice, defaultSelectedOffice));
        multipleData.setOfficeMultipleCT(officeCT);
    }

    private static DynamicFixedListType getOffices(String managingOffice) {
        var tribunalOffices = TribunalOffice.ENGLANDWALES_OFFICES.stream()
                .filter(tribunalOffice -> !tribunalOffice.getOfficeName().equals(managingOffice))
                .map(tribunalOffice ->
                        DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
                .collect(Collectors.toList());
        return DynamicFixedListType.from(tribunalOffices);
    }
}
