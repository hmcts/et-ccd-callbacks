package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

class EmploymentRightsActServiceTest {

    private EmploymentRightsActService employmentRightsActService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        employmentRightsActService = new EmploymentRightsActService();
        caseData = new CaseData();
    }

    @Test
    void shouldShowEraFlags_OnOrAfterOctoberFirst2026_SetsShowEraToYes() {
        caseData.setReceiptDate("2026-10-01");
        employmentRightsActService.shouldShowEraFlags(caseData);
        assertEquals(YES, caseData.getShowEra());
        assertEquals(YES, caseData.getAdditionalCaseInfoType().getShowEra());
    }

    @Test
    void shouldShowEraFlags_AfterOctoberFirst2026_SetsShowEraToYes() {
        caseData.setReceiptDate("2026-10-15");
        employmentRightsActService.shouldShowEraFlags(caseData);
        assertEquals(YES, caseData.getShowEra());
        assertEquals(YES, caseData.getAdditionalCaseInfoType().getShowEra());
    }

    @Test
    void shouldShowEraFlags_BeforeOctoberFirst2026_SetsShowEraToNo() {
        caseData.setReceiptDate("2026-09-30");
        employmentRightsActService.shouldShowEraFlags(caseData);
        assertEquals(NO, caseData.getShowEra());
        assertEquals(NO, caseData.getAdditionalCaseInfoType().getShowEra());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void shouldShowEraFlags_NullOrBlankReceiptDate_DoesNotSetShowEra(String receiptDate) {
        caseData.setReceiptDate(receiptDate);
        employmentRightsActService.shouldShowEraFlags(caseData);
        assertNull(caseData.getShowEra());
    }

    @Test
    void shouldShowEraFlags_NullCaseData_HandlesGracefully() {
        employmentRightsActService.shouldShowEraFlags(null);
    }

    @Test
    void processUnfairDismissalEra_ResponseYes_UdlDoesNotExist_AddsUdlAndSetsEraFlags() {
        caseData.setEtICUnfairDismissalEra(YES);
        employmentRightsActService.processUnfairDismissalEra(ENGLANDWALES_CASE_TYPE_ID, caseData);

        assertEquals(YES, caseData.getShowEra());
        assertEquals(YES, caseData.getAdditionalCaseInfoType().getEra());
        assertEquals(YES, caseData.getAdditionalCaseInfoType().getShowEra());
        assertEquals(1, caseData.getJurCodesCollection().size());
        assertEquals("UDL", caseData.getJurCodesCollection().getFirst().getValue().getJuridictionCodesList());
    }

    @Test
    void processUnfairDismissalEra_ResponseYes_UdlExists_DoesNotAddDuplicateUdl() {
        caseData.setEtICUnfairDismissalEra(YES);
        JurCodesType jurCodesType = new JurCodesType();
        jurCodesType.setJuridictionCodesList("UDL");
        JurCodesTypeItem item = new JurCodesTypeItem();
        item.setValue(jurCodesType);
        caseData.setJurCodesCollection(new ArrayList<>(List.of(item)));

        employmentRightsActService.processUnfairDismissalEra("ET_EnglandWales", caseData);

        assertEquals(1, caseData.getJurCodesCollection().size());
    }

    @Test
    void processUnfairDismissalEra_ResponseNo_NoChangesMade() {
        caseData.setEtICUnfairDismissalEra(NO);
        employmentRightsActService.processUnfairDismissalEra("ET_EnglandWales", caseData);

        assertNull(caseData.getShowEra());
        assertNull(caseData.getJurCodesCollection());
    }

    @Test
    void processUnfairDismissalEra_ResponseNotApplicable_NoChangesMade() {
        caseData.setEtICUnfairDismissalEra("Not applicable");
        employmentRightsActService.processUnfairDismissalEra("ET_EnglandWales", caseData);

        assertNull(caseData.getShowEra());
        assertNull(caseData.getJurCodesCollection());
    }
}
