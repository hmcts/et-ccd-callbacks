package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;

public class FlagsImageHelperTest {

    @Test
    public void testAddsOutstationForScotlandExcludingGlasgow() {
        var tribunalOffices = new ArrayList<>(TribunalOffice.SCOTLAND_OFFICES);
        tribunalOffices.remove(TribunalOffice.GLASGOW);
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            var caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            var caseDetails = createCaseDetails(SCOTLAND_CASE_TYPE_ID, caseData);

            FlagsImageHelper.buildFlagsImageFileName(caseDetails);

            assertEquals("<font color='DeepPink' size='5'> WITH OUTSTATION </font>", caseData.getFlagsImageAltText());
            assertEquals("EMP-TRIB-1000000000.jpg", caseData.getFlagsImageFileName());
        }
    }

    @Test
    public void testDoesNotAddOutstationForGlasgow() {
        var caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        var caseDetails = createCaseDetails(SCOTLAND_CASE_TYPE_ID, caseData);

        FlagsImageHelper.buildFlagsImageFileName(caseDetails);

        assertEquals("", caseData.getFlagsImageAltText());
        assertEquals("EMP-TRIB-0000000000.jpg", caseData.getFlagsImageFileName());
    }

    @Test
    public void testDoesNotAddOutstationForEnglandWales() {
        for (TribunalOffice tribunalOffice : TribunalOffice.ENGLANDWALES_OFFICES) {
            var caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            var caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);

            FlagsImageHelper.buildFlagsImageFileName(caseDetails);

            assertEquals("", caseData.getFlagsImageAltText());
            assertEquals("EMP-TRIB-0000000000.jpg", caseData.getFlagsImageFileName());
        }
    }

    private CaseDetails createCaseDetails(String caseTypeId, CaseData caseData) {
        var ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(caseTypeId)
                .withCaseData(caseData)
                .build();
        return ccdRequest.getCaseDetails();
    }
}
