package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.Test;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@SuppressWarnings({"PMD.ExcessiveClassLength", "PMD.LawOfDemeter", "PMD.AvoidInstantiatingObjectsInLoops"})
public class FlagsImageHelperTest {

    @Test
    public void testAddsOutstationForScotlandExcludingGlasgow() {
        ArrayList<TribunalOffice> tribunalOffices = new ArrayList<>(TribunalOffice.SCOTLAND_OFFICES);
        tribunalOffices.remove(TribunalOffice.GLASGOW);
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            CaseDetails caseDetails = createCaseDetails(SCOTLAND_CASE_TYPE_ID, caseData);

            FlagsImageHelper.buildFlagsImageFileName(caseDetails);

            assertEquals("<font color='DeepPink' size='5'> WITH OUTSTATION </font>", caseData.getFlagsImageAltText());
            assertEquals("EMP-TRIB-10000000000.jpg", caseData.getFlagsImageFileName());
            //assertEquals("EMP-TRIB-10000000000.jpg", caseData.getFlagsImageFileName());
        }
    }

    @Test
    public void testAddWelshFlag() {
        ArrayList<TribunalOffice> tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            ClaimantHearingPreference hearingPreference = new ClaimantHearingPreference();
            hearingPreference.setContactLanguage("Welsh");
            caseData.setClaimantHearingPreference(hearingPreference);
            CaseDetails caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);
            FlagsImageHelper.buildFlagsImageFileName(caseDetails);
            assertEquals("<font color='Red' size='5'> Cymraeg </font>", caseData.getFlagsImageAltText());
        }
    }

    @Test
    public void testAddWelshFlagHearingLang() {
        ArrayList<TribunalOffice> tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            ClaimantHearingPreference hearingPreference = new ClaimantHearingPreference();
            hearingPreference.setHearingLanguage("Welsh");
            caseData.setClaimantHearingPreference(hearingPreference);
            CaseDetails caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);
            FlagsImageHelper.buildFlagsImageFileName(caseDetails);
            assertEquals("<font color='Red' size='5'> Cymraeg </font>", caseData.getFlagsImageAltText());
        }
    }

    @Test
    public void testAddWelshFlagBothOptions() {
        ArrayList<TribunalOffice> tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            ClaimantHearingPreference hearingPreference = new ClaimantHearingPreference();
            hearingPreference.setHearingLanguage("Welsh");
            hearingPreference.setContactLanguage("Welsh");
            caseData.setClaimantHearingPreference(hearingPreference);
            CaseDetails caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);
            FlagsImageHelper.buildFlagsImageFileName(caseDetails);
            assertEquals("<font color='Red' size='5'> Cymraeg </font>", caseData.getFlagsImageAltText());
        }
    }

    @Test
    public void testAddWelshFlagNoOptions() {
        ArrayList<TribunalOffice> tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            CaseDetails caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);
            FlagsImageHelper.buildFlagsImageFileName(caseDetails);
            assertEquals("", caseData.getFlagsImageAltText());
        }
    }

    @Test
    public void testDoesNotAddOutstationForGlasgow() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        CaseDetails caseDetails = createCaseDetails(SCOTLAND_CASE_TYPE_ID, caseData);

        FlagsImageHelper.buildFlagsImageFileName(caseDetails);

        assertEquals("", caseData.getFlagsImageAltText());
        //assertEquals("EMP-TRIB-00000000000.jpg", caseData.getFlagsImageFileName());
        assertEquals("EMP-TRIB-00000000000.jpg", caseData.getFlagsImageFileName());
    }

    @Test
    public void testDoesNotAddOutstationForEnglandWales() {
        for (TribunalOffice tribunalOffice : TribunalOffice.ENGLANDWALES_OFFICES) {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            CaseDetails caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);

            FlagsImageHelper.buildFlagsImageFileName(caseDetails);

            assertEquals("", caseData.getFlagsImageAltText());

            //assertEquals("EMP-TRIB-00000000000.jpg", caseData.getFlagsImageFileName());
            assertEquals("EMP-TRIB-00000000000.jpg", caseData.getFlagsImageFileName());
        }
    }

    @Test
    public void addReasonableAdjustmentFlagForRespondent() {
        CaseData caseData = CaseDataBuilder.builder()
                .withRespondent("Test", NO, null, false)
                .build();
        caseData.getRespondentCollection().get(0).getValue().setEt3ResponseRespondentSupportNeeded(YES);
        FlagsImageHelper.buildFlagsImageFileName(ENGLANDWALES_CASE_TYPE_ID, caseData);
        assertEquals("EMP-TRIB-00000000010.jpg", caseData.getFlagsImageFileName());
        assertEquals("<font color='DarkSlateBlue' size='5'> REASONABLE ADJUSTMENT </font>",
                caseData.getFlagsImageAltText());

    }

    private CaseDetails createCaseDetails(String caseTypeId, CaseData caseData) {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(caseTypeId)
                .withCaseData(caseData)
                .build();
        return ccdRequest.getCaseDetails();
    }
}
