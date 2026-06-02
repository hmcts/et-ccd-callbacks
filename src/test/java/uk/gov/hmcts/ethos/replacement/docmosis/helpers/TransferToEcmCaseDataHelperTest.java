package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.ccd.CaseData;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.TribunalOfficesService.UNASSIGNED_OFFICE;

@ExtendWith(SpringExtension.class)
class TransferToEcmCaseDataHelperTest {

    private static final String CASE_ID = "1234567890";
    private static final String GATEWAY_URL = "http://ccd-gateway";

    @Test
    void copyCaseData_scotlandUnassignedManagingOffice_mapsToGlasgow() {
        var oldCaseData = new uk.gov.hmcts.et.common.model.ccd.CaseData();
        oldCaseData.setManagingOffice(UNASSIGNED_OFFICE);
        oldCaseData.setAllocatedOffice(TribunalOffice.EDINBURGH.getOfficeName());

        CaseData result = TransferToEcmCaseDataHelper.copyCaseData(
            oldCaseData, new CaseData(), CASE_ID, GATEWAY_URL, null, SCOTLAND_CASE_TYPE_ID);

        assertEquals(TribunalOffice.GLASGOW.getOfficeName(), result.getManagingOffice());
        assertEquals(TribunalOffice.EDINBURGH.getOfficeName(), result.getAllocatedOffice());
    }

    @Test
    void copyCaseData_scotlandUnassignedAllocatedOffice_mapsToGlasgow() {
        var oldCaseData = new uk.gov.hmcts.et.common.model.ccd.CaseData();
        oldCaseData.setManagingOffice(TribunalOffice.ABERDEEN.getOfficeName());
        oldCaseData.setAllocatedOffice(UNASSIGNED_OFFICE);

        CaseData result = TransferToEcmCaseDataHelper.copyCaseData(
            oldCaseData, new CaseData(), CASE_ID, GATEWAY_URL, null, SCOTLAND_CASE_TYPE_ID);

        assertEquals(TribunalOffice.ABERDEEN.getOfficeName(), result.getManagingOffice());
        assertEquals(TribunalOffice.GLASGOW.getOfficeName(), result.getAllocatedOffice());
    }

    @Test
    void copyCaseData_scotlandNullManagingOffice_mapsToGlasgow() {
        var oldCaseData = new uk.gov.hmcts.et.common.model.ccd.CaseData();
        oldCaseData.setManagingOffice(null);
        oldCaseData.setAllocatedOffice(TribunalOffice.DUNDEE.getOfficeName());

        CaseData result = TransferToEcmCaseDataHelper.copyCaseData(
            oldCaseData, new CaseData(), CASE_ID, GATEWAY_URL, null, SCOTLAND_CASE_TYPE_ID);

        assertEquals(TribunalOffice.GLASGOW.getOfficeName(), result.getManagingOffice());
        assertEquals(TribunalOffice.DUNDEE.getOfficeName(), result.getAllocatedOffice());
    }

    @Test
    void copyCaseData_scotlandNullAllocatedOffice_mapsToGlasgow() {
        var oldCaseData = new uk.gov.hmcts.et.common.model.ccd.CaseData();
        oldCaseData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        oldCaseData.setAllocatedOffice(null);

        CaseData result = TransferToEcmCaseDataHelper.copyCaseData(
            oldCaseData, new CaseData(), CASE_ID, GATEWAY_URL, null, SCOTLAND_CASE_TYPE_ID);

        assertEquals(TribunalOffice.GLASGOW.getOfficeName(), result.getManagingOffice());
        assertEquals(TribunalOffice.GLASGOW.getOfficeName(), result.getAllocatedOffice());
    }

    @Test
    void copyCaseData_scotlandBothNullOffices_mapsBothToGlasgow() {
        var oldCaseData = new uk.gov.hmcts.et.common.model.ccd.CaseData();

        CaseData result = TransferToEcmCaseDataHelper.copyCaseData(
            oldCaseData, new CaseData(), CASE_ID, GATEWAY_URL, null, SCOTLAND_CASE_TYPE_ID);

        assertEquals(TribunalOffice.GLASGOW.getOfficeName(), result.getManagingOffice());
        assertEquals(TribunalOffice.GLASGOW.getOfficeName(), result.getAllocatedOffice());
    }

    @Test
    void copyCaseData_scotlandAssignedOffices_preservesValues() {
        var oldCaseData = new uk.gov.hmcts.et.common.model.ccd.CaseData();
        oldCaseData.setManagingOffice(TribunalOffice.EDINBURGH.getOfficeName());
        oldCaseData.setAllocatedOffice(TribunalOffice.ABERDEEN.getOfficeName());

        CaseData result = TransferToEcmCaseDataHelper.copyCaseData(
            oldCaseData, new CaseData(), CASE_ID, GATEWAY_URL, null, SCOTLAND_CASE_TYPE_ID);

        assertEquals(TribunalOffice.EDINBURGH.getOfficeName(), result.getManagingOffice());
        assertEquals(TribunalOffice.ABERDEEN.getOfficeName(), result.getAllocatedOffice());
    }
}
