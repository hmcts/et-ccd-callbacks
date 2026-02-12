package uk.gov.hmcts.ecm.common.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.servicebus.IQueueClient;
import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.exceptions.InvalidMessageException;
import uk.gov.hmcts.ecm.common.exceptions.ServiceBusConnectionTimeoutException;
import uk.gov.hmcts.ecm.common.helpers.ServiceBusHelper;
import uk.gov.hmcts.ecm.common.model.servicebus.UpdateCaseMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationDataModel;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ServiceBusSenderTest {

    @InjectMocks
    private ServiceBusSender serviceBusSender;
    @Mock
    private IQueueClient sendClient;
    @Mock
    private ObjectMapper objectMapper;

    private UpdateCaseMsg updateCaseMsg;

    @BeforeEach
    public void setUp() throws Exception {
        serviceBusSender = new ServiceBusSender(sendClient, objectMapper);
        CreationDataModel creationDataModel = ServiceBusHelper.getCreationDataModel("4150002/2020");
        updateCaseMsg = ServiceBusHelper.generateUpdateCaseMsg(creationDataModel);
        lenient().when(objectMapper.writeValueAsBytes(any())).thenReturn("{}".getBytes());
    }

    @Test
   void sendMessageAsync() {
        when(sendClient.sendAsync(any())).thenReturn(CompletableFuture.completedFuture(null));
        assertDoesNotThrow(() -> serviceBusSender.sendMessageAsync(updateCaseMsg));
        verify(sendClient).sendAsync(any());
    }

    @Test
    void sendMessage() throws Exception {
        assertDoesNotThrow(() -> serviceBusSender.sendMessage(updateCaseMsg));
        verify(sendClient).send(any());
    }

    @Test
    void sendMessageNull() {
        assertThrows(InvalidMessageException.class, () -> {
            serviceBusSender.sendMessage(null);
        });
    }

    @Test()
    void sendMessageNullId() {
        updateCaseMsg.setMsgId(null);
        assertThrows(InvalidMessageException.class, () -> {
            serviceBusSender.sendMessageAsync(updateCaseMsg);
        });

    }

    @Test()
    void sendMessageTimeoutException() throws ServiceBusException, InterruptedException {
        doThrow(ServiceBusConnectionTimeoutException.class).when(sendClient).send(any());
        assertThrows(ServiceBusConnectionTimeoutException.class, () -> {
            serviceBusSender.sendMessage(updateCaseMsg);
        });

    }

    @Test()
    void sendMessageInterruptedException() throws ServiceBusException, InterruptedException {
        doThrow(new InterruptedException()).when(sendClient).send(any());
        assertThrows(InvalidMessageException.class, () -> {
            serviceBusSender.sendMessage(updateCaseMsg);
        });
    }

    @Test()
    void sendMessageServiceBusException() throws ServiceBusException, InterruptedException {
        doThrow(new ServiceBusException(true)).when(sendClient).send(any());
        assertThrows(InvalidMessageException.class, () -> {
            serviceBusSender.sendMessage(updateCaseMsg);
        });
    }

}
