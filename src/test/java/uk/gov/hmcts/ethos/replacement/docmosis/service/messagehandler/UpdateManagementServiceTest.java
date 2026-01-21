package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CloseDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.LegalRepDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.ResetStateDataModel;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.MultipleErrors;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleCounterRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.MultipleErrorsRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import javax.naming.NameNotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class UpdateManagementServiceTest {

    @InjectMocks
    private transient UpdateManagementService updateManagementService;
    @Mock
    private transient MultipleCounterRepository multipleCounterRepository;
    @Mock
    private transient MultipleErrorsRepository multipleErrorsRepository;
    @Mock
    private transient MultipleUpdateService multipleUpdateService;
    @Mock
    private transient SingleReadingService singleReadingService;
    @Mock
    private LegalRepAccessService legalRepAccessService;

    private transient UpdateCaseMsg updateCaseMsg;

    @BeforeEach
    public void setUp() {
        updateCaseMsg = generateUpdateCaseMsg();
    }

    @Test
    void updateLogic() throws IOException, InterruptedException, NameNotFoundException {
        when(multipleCounterRepository.persistentQGetNextMultipleCountVal(
            updateCaseMsg.getMultipleRef())).thenReturn(1);
        when(multipleErrorsRepository.findByMultipleref(updateCaseMsg.getMultipleRef())).thenReturn(new ArrayList<>());

        updateManagementService.updateLogic(updateCaseMsg);

        verify(singleReadingService).sendUpdateToSingleLogic(updateCaseMsg);
        verifyNoMoreInteractions(singleReadingService);
        verify(multipleUpdateService).sendUpdateToMultipleLogic(eq(updateCaseMsg), any());
        verifyNoMoreInteractions(multipleUpdateService);
        verify(multipleCounterRepository).persistentQGetNextMultipleCountVal(updateCaseMsg.getMultipleRef());
        verify(multipleCounterRepository, times(1))
            .findByMultipleref(updateCaseMsg.getMultipleRef());
        verify(multipleCounterRepository).deleteAllInBatch(new ArrayList<>());
        verify(multipleErrorsRepository, times(2))
            .findByMultipleref(updateCaseMsg.getMultipleRef());
        verify(multipleErrorsRepository).deleteAllInBatch(new ArrayList<>());
        verifyNoMoreInteractions(multipleErrorsRepository);
        verifyNoMoreInteractions(multipleCounterRepository);
    }

    @Test
    void updateLogicWithErrorsDefaultConstructor() throws IOException, InterruptedException,
        NameNotFoundException {
        MultipleErrors multipleErrors = new MultipleErrors();
        when(multipleCounterRepository.persistentQGetNextMultipleCountVal(
            updateCaseMsg.getMultipleRef())).thenReturn(1);
        when(multipleErrorsRepository.findByMultipleref(updateCaseMsg.getMultipleRef())).thenReturn(new ArrayList<>(
            Collections.singletonList(multipleErrors)));

        updateManagementService.updateLogic(updateCaseMsg);

        verify(singleReadingService).sendUpdateToSingleLogic(updateCaseMsg);
        verifyNoMoreInteractions(singleReadingService);
        verify(multipleUpdateService).sendUpdateToMultipleLogic(eq(updateCaseMsg), any());
        verifyNoMoreInteractions(multipleUpdateService);
        verify(multipleCounterRepository).persistentQGetNextMultipleCountVal(updateCaseMsg.getMultipleRef());
        verify(multipleCounterRepository, times(1))
            .findByMultipleref(updateCaseMsg.getMultipleRef());
        verify(multipleCounterRepository).deleteAllInBatch(new ArrayList<>());
        verify(multipleErrorsRepository, times(2))
            .findByMultipleref(updateCaseMsg.getMultipleRef());
        verify(multipleErrorsRepository)
            .deleteAllInBatch(new ArrayList<>(Collections.singletonList(new MultipleErrors())));
        verifyNoMoreInteractions(multipleErrorsRepository);
        verifyNoMoreInteractions(multipleCounterRepository);
    }

    @Test
    void addUnrecoverableErrorToDatabase() {
        updateManagementService.addUnrecoverableErrorToDatabase(updateCaseMsg);

        verify(multipleErrorsRepository).persistentQLogMultipleError(
            eq(updateCaseMsg.getMultipleRef()),
            eq(updateCaseMsg.getEthosCaseReference()),
            eq("Unprocessable message"));
        verifyNoMoreInteractions(multipleErrorsRepository);
    }

    @Test
    void updateLogicResetState() throws IOException, InterruptedException, NameNotFoundException {
        ResetStateDataModel resetStateDataModel = ResetStateDataModel.builder().build();
        updateCaseMsg.setDataModelParent(resetStateDataModel);
        updateManagementService.updateLogic(updateCaseMsg);

        verify(multipleCounterRepository).findByMultipleref(eq(updateCaseMsg.getMultipleRef()));
        verify(multipleCounterRepository).deleteAllInBatch(new ArrayList<>());
        verify(multipleErrorsRepository).findByMultipleref(eq(updateCaseMsg.getMultipleRef()));
        verify(multipleErrorsRepository).deleteAllInBatch(new ArrayList<>());
        verifyNoMoreInteractions(multipleErrorsRepository);
        verifyNoMoreInteractions(multipleCounterRepository);
    }

    @Test
    void legalRepAccessModel() throws NameNotFoundException, IOException, InterruptedException {
        LegalRepDataModel legalRepDataModel = LegalRepDataModel.builder().build();
        updateCaseMsg.setDataModelParent(legalRepDataModel);
        updateManagementService.updateLogic(updateCaseMsg);
        verify(legalRepAccessService).run(any());
    }

    private UpdateCaseMsg generateUpdateCaseMsg() {
        UpdateCaseMsg msg = new UpdateCaseMsg();
        msg.setMsgId(UUID.randomUUID().toString());
        msg.setJurisdiction("EMPLOYMENT");
        msg.setCaseTypeId("ET_EnglandWales");
        msg.setMultipleRef("6000001");
        msg.setEthosCaseReference("240001/2024");
        msg.setTotalCases("1");
        msg.setUsername("test@test.com");
        msg.setDataModelParent(new CloseDataModel());
        msg.setConfirmation(YES);
        return msg;
    }
}
