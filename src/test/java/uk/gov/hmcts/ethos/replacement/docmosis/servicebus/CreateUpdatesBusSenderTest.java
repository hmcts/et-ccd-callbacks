package uk.gov.hmcts.ethos.replacement.docmosis.servicebus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesDto;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationDataModel;
import uk.gov.hmcts.ecm.common.servicebus.ServiceBusSender;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class CreateUpdatesBusSenderTest {

    @InjectMocks
    private CreateUpdatesBusSender createUpdatesBusSender;
    @Mock
    private ServiceBusSender serviceBusSender;

    private CreateUpdatesDto createUpdatesDto;

    private CreationDataModel creationDataModel;

    private List<String> ethosCaseRefCollection;

    @BeforeEach
    public void setUp() {
        createUpdatesBusSender = new CreateUpdatesBusSender(serviceBusSender);
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
                .when(serviceBusSender).sendMessage(any());
        createUpdatesBusSender.sendUpdatesToQueue(createUpdatesDto, creationDataModel,
                new ArrayList<>(), String.valueOf(ethosCaseRefCollection.size()));
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
                .lead(ethosCaseRefCollection.get(0))
                .multipleRef("4150001")
                .build();
    }

}
