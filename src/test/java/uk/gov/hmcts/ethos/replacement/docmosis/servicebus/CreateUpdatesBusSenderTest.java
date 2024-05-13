package uk.gov.hmcts.ethos.replacement.docmosis.servicebus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.postgresql.util.PGobject;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesDto;
import uk.gov.hmcts.ecm.common.model.servicebus.CreateUpdatesMsg;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.CreationDataModel;
import uk.gov.hmcts.ecm.common.model.servicebus.datamodel.DataModelParent;
import uk.gov.hmcts.ecm.common.servicebus.ServiceBusSender;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.EMPLOYMENT;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_BULK_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class CreateUpdatesBusSenderTest {

    @InjectMocks
    private CreateUpdatesBusSender createUpdatesBusSender;
    @Mock
    private ServiceBusSender serviceBusSender;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private DataSource datasource;

    private CreateUpdatesDto createUpdatesDto;

    private CreationDataModel creationDataModel;

    private List<String> ethosCaseRefCollection;

    @BeforeEach
    public void setUp() {
        createUpdatesBusSender = new CreateUpdatesBusSender(serviceBusSender, featureToggleService, datasource);
        when(featureToggleService.isMultiplesDBEnabled()).thenReturn(false);
        ethosCaseRefCollection = Arrays.asList("4150001/2020", "4150002/2020",
                "4150003/2020", "4150004/2020");
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

    @Test
    void testSendUpdatesToQueue() throws SQLException, JsonProcessingException {
        when(featureToggleService.isMultiplesDBEnabled()).thenReturn(true);
        Connection mockConn = Mockito.mock(Connection.class);
        when(datasource.getConnection()).thenReturn(mockConn);
        CallableStatement mockAddWork = Mockito.mock(CallableStatement.class);
        
        Mockito.when(mockConn.prepareCall(anyString())).thenReturn(mockAddWork);

        CreateUpdatesDto createUpdatesDto = CreateUpdatesDto.builder()
                .caseTypeId(ENGLANDWALES_BULK_CASE_TYPE_ID)
                .jurisdiction(EMPLOYMENT)
                .multipleRef("600001")
                .ethosCaseRefCollection(ethosCaseRefCollection)
                .username("idklol")
                .build();

        DataModelParent dataModelParent = new DataModelParent();

        createUpdatesBusSender.sendUpdatesToQueue(createUpdatesDto, dataModelParent, Collections.emptyList(), "1");

        verify(mockConn).prepareCall(anyString());
        verify(mockAddWork).setString(1, "600001");
        ArgumentCaptor<PGobject> captor = ArgumentCaptor.forClass(PGobject.class);
        verify(mockAddWork).setObject(eq(2), captor.capture());
        CreateUpdatesMsg actual = new ObjectMapper().readValue(captor.getValue().getValue(), CreateUpdatesMsg.class);

        CreateUpdatesMsg expected = CreateUpdatesMsg.builder()
                .msgId(UUID.randomUUID().toString())
                .jurisdiction(createUpdatesDto.getJurisdiction())
                .caseTypeId(createUpdatesDto.getCaseTypeId())
                .multipleRef(createUpdatesDto.getMultipleRef())
                .ethosCaseRefCollection(List.of("4150001/2020", "4150002/2020", "4150003/2020", "4150004/2020"))
                .totalCases("1")
                .username(createUpdatesDto.getUsername())
                .confirmation(createUpdatesDto.getConfirmation())
                .dataModelParent(dataModelParent)
                .multipleReferenceLinkMarkUp(createUpdatesDto.getMultipleReferenceLinkMarkUp())
                .build();

        actual.setMsgId(expected.getMsgId());
        assertEquals(actual, expected);
        verify(mockAddWork).execute();
    }
}
