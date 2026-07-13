package uk.gov.hmcts.ethos.replacement.docmosis.servicebus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ecm.common.helpers.CreateUpdatesHelper;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesDto;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationDataModel;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.repository.messagequeue.CreateUpdatesQueueRepository;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(MockitoExtension.class)
class CreateUpdatesBusSenderTest {

    private CreateUpdatesBusSender createUpdatesBusSender;
    @Mock
    private CreateUpdatesQueueRepository createUpdatesQueueRepository;
    @Mock
    private ObjectMapper objectMapper;

    private CreateUpdatesDto createUpdatesDto;

    private CreationDataModel creationDataModel;

    private List<String> ethosCaseRefCollection;

    @BeforeEach
    void setUp() {
        createUpdatesBusSender = new CreateUpdatesBusSender(createUpdatesQueueRepository, objectMapper);
        ethosCaseRefCollection = Arrays.asList("4150001/2020", "4150002/2020",
                "4150003/2020", "4150004/2020", "4150005/2020");
        createUpdatesDto = getCreateUpdatesDto(ethosCaseRefCollection);
        creationDataModel = getCreationDataModel(ethosCaseRefCollection);
    }

    @Test
    void runMainMethodTest() {
        createUpdatesBusSender.sendUpdatesToQueue(createUpdatesDto, creationDataModel,
                new ArrayList<>(), String.valueOf(ethosCaseRefCollection.size()));
    }

    @Test
    void runMainMethodTestException() {
        doThrow(new InternalException(ERROR_MESSAGE))
            .when(createUpdatesQueueRepository).save(any());
        createUpdatesBusSender.sendUpdatesToQueue(createUpdatesDto, creationDataModel,
                new ArrayList<>(), String.valueOf(ethosCaseRefCollection.size()));
    }

    @Test
    void shouldSaveQueueMessageWithPendingStatusWhenSerializationSucceeds() throws Exception {
        CreateUpdatesMsg createUpdatesMsg = new CreateUpdatesMsg();
        createUpdatesMsg.setMsgId("msg-id");
        List<String> errors = new ArrayList<>();

        try (MockedStatic<CreateUpdatesHelper> createUpdatesHelper = org.mockito.Mockito
            .mockStatic(CreateUpdatesHelper.class)) {
            createUpdatesHelper.when(() -> CreateUpdatesHelper.getCreateUpdatesMessagesCollection(
                createUpdatesDto,
                creationDataModel,
                500,
                String.valueOf(ethosCaseRefCollection.size())
            )).thenReturn(List.of(createUpdatesMsg));
            when(objectMapper.writeValueAsString(createUpdatesMsg)).thenReturn("{\"msgId\":\"msg-id\"}");

            createUpdatesBusSender.sendUpdatesToQueue(
                createUpdatesDto,
                creationDataModel,
                errors,
                String.valueOf(ethosCaseRefCollection.size())
            );
        }

        verify(createUpdatesQueueRepository).save(any());
        assertEquals(0, errors.size());
    }

    @Test
    void shouldAppendErrorAndSkipSaveWhenSerializationFails() throws Exception {
        CreateUpdatesMsg createUpdatesMsg = new CreateUpdatesMsg();
        createUpdatesMsg.setMsgId("msg-id");
        List<String> errors = new ArrayList<>();

        try (MockedStatic<CreateUpdatesHelper> createUpdatesHelper = org.mockito.Mockito
            .mockStatic(CreateUpdatesHelper.class)) {
            createUpdatesHelper.when(() -> CreateUpdatesHelper.getCreateUpdatesMessagesCollection(
                createUpdatesDto,
                creationDataModel,
                500,
                String.valueOf(ethosCaseRefCollection.size())
            )).thenReturn(List.of(createUpdatesMsg));
            when(objectMapper.writeValueAsString(createUpdatesMsg)).thenThrow(new RuntimeException("boom"));

            createUpdatesBusSender.sendUpdatesToQueue(
                createUpdatesDto,
                creationDataModel,
                errors,
                String.valueOf(ethosCaseRefCollection.size())
            );
        }

        verify(createUpdatesQueueRepository, never()).save(any());
        assertEquals(List.of("Failed to send the message to the queue"), errors);
    }

    private CreateUpdatesDto getCreateUpdatesDto(List<String> ethosCaseRefCollection) {
        return CreateUpdatesDto.builder()
                .caseTypeId(SCOTLAND_BULK_CASE_TYPE_ID)
                .jurisdiction("EMPLOYMENT")
                .multipleRef("4150001")
                .username("testEmail@hotmail.com")
                .ethosCaseRefCollection(ethosCaseRefCollection)
                .build();
    }

    private CreationDataModel getCreationDataModel(List<String> ethosCaseRefCollection) {
        return CreationDataModel.builder()
                .lead(ethosCaseRefCollection.getFirst())
                .multipleRef("4150001")
                .build();
    }

}
