package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.ecm.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ecm.common.model.multiples.MultipleData;

import java.util.Collections;
import java.util.stream.Collectors;

public class CaseTransferOfficeService {

    private CaseTransferOfficeService() {
        // All access through static methods
    }

    public static void populateOfficeOptions(CaseData caseData) {
        var managingOffice = caseData.getManagingOffice();
        if (StringUtils.isBlank(managingOffice)) {
            return;
        }

        var offices = getOffices(managingOffice);
        caseData.setOfficeCT(offices);
    }

    public static void populateOfficeOptions(MultipleData multipleData) {
        var managingOffice = multipleData.getManagingOffice();
        if (StringUtils.isBlank(managingOffice)) {
            return;
        }

        var offices = getOffices(managingOffice);
        multipleData.setOfficeMultipleCT(offices);
    }

    private static DynamicFixedListType getOffices(String managingOffice) {
        var officeCT = new DynamicFixedListType();
        if (TribunalOffice.isEnglandWalesOffice(managingOffice)) {
            var tribunalOffices = TribunalOffice.ENGLANDWALES_OFFICES.stream()
                    .filter(tribunalOffice -> !tribunalOffice.getOfficeName().equals(managingOffice))
                    .map(tribunalOffice ->
                            DynamicValueType.create(tribunalOffice.getOfficeName(), tribunalOffice.getOfficeName()))
                    .collect(Collectors.toList());
            officeCT.setListItems(tribunalOffices);
        } else if (TribunalOffice.isScotlandOffice(managingOffice)) {
            officeCT.setListItems(Collections.emptyList());
        }

        return officeCT;
    }
}
