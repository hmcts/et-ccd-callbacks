package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.types.AdditionalCaseInfoType;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.FlagsImageHelper.buildFlagsImageFileName;

@ExtendWith(SpringExtension.class)
class FlagsImageHelperTest {

    @Test
    void testAddsOutstationForScotlandExcludingGlasgow() {
        ArrayList<TribunalOffice> tribunalOffices = new ArrayList<>(TribunalOffice.SCOTLAND_OFFICES);
        tribunalOffices.remove(TribunalOffice.GLASGOW);
        for (TribunalOffice tribunalOffice : tribunalOffices) {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            CaseDetails caseDetails = createCaseDetails(SCOTLAND_CASE_TYPE_ID, caseData);

            buildFlagsImageFileName(caseDetails);

            assertEquals("<font color='DeepPink' size='5'> WITH OUTSTATION </font>", caseData.getFlagsImageAltText());
            assertEquals("EMP-TRIB-01000000000000.jpg", caseData.getFlagsImageFileName());
        }
    }

    @Test
    void testAddWelshFlag() {
        ArrayList<TribunalOffice> tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        tribunalOffices.forEach(tribunalOffice -> {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            ClaimantHearingPreference hearingPreference = new ClaimantHearingPreference();
            hearingPreference.setContactLanguage("Welsh");
            caseData.setClaimantHearingPreference(hearingPreference);
            CaseDetails caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);
            buildFlagsImageFileName(caseDetails);
            assertEquals("<font color='Red' size='5'> Cymraeg </font>", caseData.getFlagsImageAltText());
        });
    }

    @Test
    void testAddWelshFlagHearingLang() {
        ArrayList<TribunalOffice> tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        tribunalOffices.forEach(tribunalOffice -> {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            ClaimantHearingPreference hearingPreference = new ClaimantHearingPreference();
            hearingPreference.setHearingLanguage("Welsh");
            caseData.setClaimantHearingPreference(hearingPreference);
            CaseDetails caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);
            buildFlagsImageFileName(caseDetails);
            assertEquals("<font color='Red' size='5'> Cymraeg </font>", caseData.getFlagsImageAltText());
        });
    }

    @Test
    void testAddWelshFlagBothOptions() {
        ArrayList<TribunalOffice> tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        tribunalOffices.forEach(tribunalOffice -> {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            ClaimantHearingPreference hearingPreference = new ClaimantHearingPreference();
            hearingPreference.setHearingLanguage("Welsh");
            hearingPreference.setContactLanguage("Welsh");
            caseData.setClaimantHearingPreference(hearingPreference);
            CaseDetails caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);
            buildFlagsImageFileName(caseDetails);
            assertEquals("<font color='Red' size='5'> Cymraeg </font>", caseData.getFlagsImageAltText());
        });
    }

    @Test
    void testAddWelshFlagNoOptions() {
        ArrayList<TribunalOffice> tribunalOffices = new ArrayList<>(TribunalOffice.ENGLANDWALES_OFFICES);
        tribunalOffices.forEach(tribunalOffice -> {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            CaseDetails caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);
            buildFlagsImageFileName(caseDetails);
            assertEquals("", caseData.getFlagsImageAltText());
        });
    }

    @Test
    void testDoesNotAddOutstationForGlasgow() {
        CaseData caseData = new CaseData();
        caseData.setManagingOffice(TribunalOffice.GLASGOW.getOfficeName());
        CaseDetails caseDetails = createCaseDetails(SCOTLAND_CASE_TYPE_ID, caseData);

        buildFlagsImageFileName(caseDetails);

        assertEquals("", caseData.getFlagsImageAltText());
        assertEquals("EMP-TRIB-00000000000000.jpg", caseData.getFlagsImageFileName());
    }

    @Test
    void testDoesNotAddOutstationForEnglandWales() {
        TribunalOffice.ENGLANDWALES_OFFICES.forEach(tribunalOffice -> {
            CaseData caseData = new CaseData();
            caseData.setManagingOffice(tribunalOffice.getOfficeName());
            CaseDetails caseDetails = createCaseDetails(ENGLANDWALES_CASE_TYPE_ID, caseData);
            buildFlagsImageFileName(caseDetails);
            assertEquals("", caseData.getFlagsImageAltText());

            assertEquals("EMP-TRIB-00000000000000.jpg", caseData.getFlagsImageFileName());
        });
    }

    @Test
    void addReasonableAdjustmentFlagForRespondent() {
        CaseData caseData = CaseDataBuilder.builder()
                .withRespondent("Test", NO, null, false)
                .build();
        caseData.getRespondentCollection().get(0).getValue().setEt3ResponseRespondentSupportNeeded(YES);
        buildFlagsImageFileName(ENGLANDWALES_CASE_TYPE_ID, caseData);
        assertEquals("EMP-TRIB-00000000001000.jpg", caseData.getFlagsImageFileName());
        assertEquals("<font color='DarkSlateBlue' size='5'> REASONABLE ADJUSTMENT </font>",
                caseData.getFlagsImageAltText());

    }

    @Test
    void ecmMigrationFlag() {
        CaseData caseData = new CaseData();
        caseData.setMigratedFromEcm(YES);
        buildFlagsImageFileName(ENGLANDWALES_CASE_TYPE_ID, caseData);
        assertEquals("EMP-TRIB-10000000000000.jpg", caseData.getFlagsImageFileName());
        assertEquals("<font color='#D6292D' size='5'> MIGRATED FROM ECM </font>",
                caseData.getFlagsImageAltText());

        caseData.setMigratedFromEcm(null);
        buildFlagsImageFileName(ENGLANDWALES_CASE_TYPE_ID, caseData);
        assertEquals("EMP-TRIB-00000000000000.jpg", caseData.getFlagsImageFileName());
    }

    private CaseDetails createCaseDetails(String caseTypeId, CaseData caseData) {
        CCDRequest ccdRequest = CCDRequestBuilder.builder()
                .withCaseTypeId(caseTypeId)
                .withCaseData(caseData)
                .build();
        return ccdRequest.getCaseDetails();
    }

    @Test
    void interventionFlag() {
        AdditionalCaseInfoType additionalCaseInfoType = new AdditionalCaseInfoType();
        additionalCaseInfoType.setInterventionRequired(YES);
        CaseData caseData = new CaseData();
        caseData.setAdditionalCaseInfoType(additionalCaseInfoType);

        buildFlagsImageFileName(ENGLANDWALES_CASE_TYPE_ID, caseData);
        assertEquals("EMP-TRIB-00000000000001.jpg", caseData.getFlagsImageFileName());
        assertTrue(caseData.getFlagsImageAltText().contains("SPEAK TO REJ"));

        buildFlagsImageFileName(SCOTLAND_CASE_TYPE_ID, caseData);
        assertEquals("EMP-TRIB-01000000000010.jpg", caseData.getFlagsImageFileName());
        assertTrue(caseData.getFlagsImageAltText().contains("SPEAK TO VP"));
    }
}
