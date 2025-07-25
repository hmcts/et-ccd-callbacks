package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.CasePreAcceptType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

class PreAcceptanceCaseServiceTest {

    private CaseData caseData;

    private final PreAcceptanceCaseService preAcceptanceCaseService = new PreAcceptanceCaseService();

    @BeforeEach
    void setUp() {
        caseData = new CaseData();
        caseData.setReceiptDate("2024-01-10");
    }

    @Test
    void shouldReturnError_whenDateAcceptedBeforeReceiptDate() {
        CasePreAcceptType preAcceptType = new CasePreAcceptType();
        preAcceptType.setCaseAccepted(YES);
        preAcceptType.setDateAccepted("2024-01-05");
        caseData.setPreAcceptCase(preAcceptType);

        List<String> errors = preAcceptanceCaseService.validateAcceptanceDate(caseData);

        assertThat(errors).containsExactly("Accepted date should not be earlier than the case received date");
    }

    @Test
    void shouldNotReturnError_whenDateAcceptedAfterReceiptDate() {
        CasePreAcceptType preAcceptType = new CasePreAcceptType();
        preAcceptType.setCaseAccepted(YES);
        preAcceptType.setDateAccepted("2024-01-15");
        caseData.setPreAcceptCase(preAcceptType);

        List<String> errors = preAcceptanceCaseService.validateAcceptanceDate(caseData);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldReturnError_whenDateRejectedBeforeReceiptDate() {
        CasePreAcceptType preAcceptType = new CasePreAcceptType();
        preAcceptType.setCaseAccepted(NO);
        preAcceptType.setDateRejected("2024-01-05");
        caseData.setPreAcceptCase(preAcceptType);

        List<String> errors = preAcceptanceCaseService.validateAcceptanceDate(caseData);

        assertThat(errors).containsExactly("Rejected date should not be earlier than the case received date");
    }

    @Test
    void shouldNotReturnError_whenDateRejectedAfterReceiptDate() {
        CasePreAcceptType preAcceptType = new CasePreAcceptType();
        preAcceptType.setCaseAccepted(NO);
        preAcceptType.setDateRejected("2024-01-15");
        caseData.setPreAcceptCase(preAcceptType);

        List<String> errors = preAcceptanceCaseService.validateAcceptanceDate(caseData);

        assertThat(errors).isEmpty();
    }
}