package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.helper.TribunalOffice;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.et.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer.CaseTransferToEcmService.NO_CASES_FOUND;

@ExtendWith(SpringExtension.class)
class CaseTransferToEcmServiceTest {

    private static final String CASE_TRANSFER_TO_ECM = " Transferring case to ECM";
    private static final String AUTH_TOKEN = "Bearer some-random-token";

    @InjectMocks
    private CaseTransferToEcmService caseTransferToEcmService;

    @Mock
    private CaseTransferUtils caseTransferUtils;

    @Mock
    private CaseTransferEventService caseTransferEventService;

    @Test
    void transferToEcm() {
        var ecmOfficeCT = TribunalOffice.BRISTOL.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.LEEDS.getOfficeName(), ecmOfficeCT);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, AUTH_TOKEN))
                .thenReturn(List.of(caseDetails.getCaseData()));

        var errors = caseTransferToEcmService.createCaseTransferToEcm(caseDetails, AUTH_TOKEN);
        assertTrue(errors.isEmpty());
        verify(caseTransferEventService, times(1)).transferToEcm(isA(CaseTransferToEcmParams.class));
    }

    @Test
    void noCasesFound() {
        var ecmOfficeCT = TribunalOffice.BRISTOL.getOfficeName();
        var caseDetails = createCaseDetails(TribunalOffice.LEEDS.getOfficeName(), ecmOfficeCT);
        when(caseTransferUtils.getAllCasesToBeTransferred(caseDetails, AUTH_TOKEN))
                .thenReturn(Collections.emptyList());
        var errors = caseTransferToEcmService.createCaseTransferToEcm(caseDetails, AUTH_TOKEN);
        assertEquals(1, errors.size());
        assertEquals(String.format(NO_CASES_FOUND, "60000001/2022"), errors.get(0));
    }

    private CaseDetails createCaseDetails(String managingOffice, String ecmOfficeCT) {
        CaseDataBuilder builder = CaseDataBuilder.builder()
                .withEthosCaseReference("60000001/2022")
                .withManagingOffice(managingOffice)
                .withEcmOfficeCT(ecmOfficeCT, CASE_TRANSFER_TO_ECM);
        return builder.buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID, "EMPLOYMENT");
    }
}
