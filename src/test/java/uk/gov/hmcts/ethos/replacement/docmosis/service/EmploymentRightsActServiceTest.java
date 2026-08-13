package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.JurCodesTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.JurCodesType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(MockitoExtension.class)
class EmploymentRightsActServiceTest {

    private EmploymentRightsActService employmentRightsActService;
    private CaseData caseData;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        employmentRightsActService = new EmploymentRightsActService(featureToggleService);
        caseData = new CaseData();
        when(featureToggleService.isEraOctober2026Enabled()).thenReturn(true);
    }

    @Test
    void setEraFlagByReceiptDate_BeforeOctoberFirst2026_SetsEraToNo() {
        caseData.setReceiptDate("2026-09-30");
        employmentRightsActService.setEraFlagByReceiptDate(caseData);
        assertEquals(NO, caseData.getAdditionalCaseInfoType().getEra());
    }

    @Test
    void setEraFlagByReceiptDate_OnOrAfterOctoberFirst2026_DoesNotSetEraToNo() {
        caseData.setReceiptDate("2026-10-01");
        employmentRightsActService.setEraFlagByReceiptDate(caseData);
        assertNull(caseData.getAdditionalCaseInfoType());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    void setEraFlagByReceiptDate_NullOrBlankReceiptDate_DoesNothing(String receiptDate) {
        caseData.setReceiptDate(receiptDate);
        employmentRightsActService.setEraFlagByReceiptDate(caseData);
        assertNull(caseData.getAdditionalCaseInfoType());
    }

    @Test
    void setEraFlagByReceiptDate_NullCaseData_HandlesGracefully() {
        employmentRightsActService.setEraFlagByReceiptDate(null);
    }

    @Test
    void setUnfairDismissalEraByReceiptDate_BeforeOctoberFirst2026_SetsIcUnfairDismissalToNotApplicable() {
        caseData.setReceiptDate("2026-09-30");
        employmentRightsActService.setUnfairDismissalEraByReceiptDate(caseData);
        assertEquals("Not applicable", caseData.getEtICUnfairDismissalEra());
    }

    @Test
    void setUnfairDismissalEraByReceiptDate_OnOrAfterOctoberFirst2026_DoesNotSetIcUnfairDismissal() {
        caseData.setReceiptDate("2026-10-01");
        employmentRightsActService.setUnfairDismissalEraByReceiptDate(caseData);
        assertNull(caseData.getEtICUnfairDismissalEra());
    }

    @Test
    void processUnfairDismissalEra_ResponseYes_UdlDoesNotExist_AddsUdlAndSetsEraFlags() {
        caseData.setEtICUnfairDismissalEra(YES);
        employmentRightsActService.processUnfairDismissalEra(ENGLANDWALES_CASE_TYPE_ID, caseData);

        assertEquals(YES, caseData.getAdditionalCaseInfoType().getEra());
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

        assertNull(caseData.getJurCodesCollection());
    }

    @Test
    void processUnfairDismissalEra_ResponseNotApplicable_NoChangesMade() {
        caseData.setEtICUnfairDismissalEra("Not applicable");
        employmentRightsActService.processUnfairDismissalEra("ET_EnglandWales", caseData);

        assertNull(caseData.getJurCodesCollection());
    }

    @Test
    void setEraFlagByReceiptDate_EraFeatureDisabled_DoesNotSetEraToNo() {
        when(featureToggleService.isEraOctober2026Enabled()).thenReturn(false);
        caseData.setReceiptDate("2026-09-30");

        employmentRightsActService.setEraFlagByReceiptDate(caseData);

        assertNull(caseData.getAdditionalCaseInfoType());
    }

    @Test
    void setUnfairDismissalEraByReceiptDate_EraFeatureDisabled_DoesNotSetIcUnfairDismissal() {
        when(featureToggleService.isEraOctober2026Enabled()).thenReturn(false);
        caseData.setReceiptDate("2026-09-30");

        employmentRightsActService.setUnfairDismissalEraByReceiptDate(caseData);

        assertNull(caseData.getEtICUnfairDismissalEra());
    }

    @Test
    void processUnfairDismissalEra_EraFeatureDisabled_DoesNotSetEraOrUdl() {
        when(featureToggleService.isEraOctober2026Enabled()).thenReturn(false);
        caseData.setEtICUnfairDismissalEra(YES);

        employmentRightsActService.processUnfairDismissalEra(ENGLANDWALES_CASE_TYPE_ID, caseData);

        assertNull(caseData.getAdditionalCaseInfoType());
        assertNull(caseData.getJurCodesCollection());
    }
}
