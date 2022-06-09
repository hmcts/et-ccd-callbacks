package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.helper.TribunalOffice;
import uk.gov.hmcts.ethos.replacement.docmosis.service.PersistentQHelperService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SINGLE_CASE_TYPE;


@ExtendWith(SpringExtension.class)
class CaseTransferEventServiceTest {

    @InjectMocks
    CaseTransferEventService caseTransferEventService;

    @Mock
    PersistentQHelperService persistentQHelperService;

    @Test
    void testTransfer() {
        var params = CaseTransferEventParams.builder()
                .userToken("test-token")
                .caseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .jurisdiction("EMPLOYMENT")
                .ethosCaseReferences(List.of("120001/2021"))
                .newManagingOffice(TribunalOffice.NEWCASTLE.getOfficeName())
                .positionType("Test position type")
                .reason("Test reason")
                .multipleReference(SINGLE_CASE_TYPE)
                .confirmationRequired(false)
                .transferSameCountry(true)
                .sourceEthosCaseReference("120002/2021")
                .build();

        var errors = caseTransferEventService.transfer(params);

        Assertions.assertTrue(errors.isEmpty());
        verify(persistentQHelperService, times(1)).sendCreationEventToSingles(
                "test-token", ENGLANDWALES_CASE_TYPE_ID, "EMPLOYMENT", new ArrayList<>(),
                List.of("120001/2021"), TribunalOffice.NEWCASTLE.getOfficeName(), "Test position type",
                null, "Test reason", SINGLE_CASE_TYPE, NO, null, true, "120002/2021");
    }

    @Test
    void testEcmTransfer() {
        var params = CaseTransferToEcmParams.builder()
                .userToken("test-token")
                .caseTypeId(ENGLANDWALES_CASE_TYPE_ID)
                .jurisdiction("EMPLOYMENT")
                .ethosCaseReferences(List.of("120001/2021"))
                .newCaseTypeId(TribunalOffice.NEWCASTLE.getOfficeName())
                .positionType("Test position type")
                .reason("Test reason")
                .confirmationRequired(false)
                .sourceEthosCaseReference("120002/2021")
                .build();

        var errors = caseTransferEventService.transferToEcm(params);

        Assertions.assertTrue(errors.isEmpty());
        verify(persistentQHelperService, times(1)).sendTransferToEcmEvent(
                "test-token", ENGLANDWALES_CASE_TYPE_ID, "EMPLOYMENT", new ArrayList<>(),
                List.of("120001/2021"), TribunalOffice.NEWCASTLE.getOfficeName(), "Test position type",
                null, "Test reason", NO, "120002/2021");

    }
}
