package uk.gov.hmcts.ethos.replacement.docmosis.service.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.admin.CreateService.CREATE_EXIST_ERROR_MESSAGE;

class CreateServiceTest {

    private CcdClient ccdClient;
    private CreateService createService;
    private String userToken;

    @BeforeEach
    void setUp() {
        ccdClient = mock(CcdClient.class);
        createService = new CreateService(ccdClient);
        userToken = "userToken";
    }

    @Test
    void initCreateAdmin_NotExist_shouldReturnNoError() throws IOException {
        when(ccdClient.executeElasticSearch(anyString(), anyString(), anyString())).thenReturn(new ArrayList<>());
        List<String> errors = createService.initCreateAdmin(userToken);
        assertEquals(0, errors.size());
    }

    @Test
    @Disabled
    void initCreateAdmin_Exist_shouldReturnError() throws IOException {
        SubmitEvent submitEvent = new SubmitEvent();
        when(ccdClient.executeElasticSearch(anyString(), anyString(), anyString())).thenReturn(List.of(submitEvent));
        List<String> errors = createService.initCreateAdmin(userToken);
        assertEquals(1, errors.size());
        assertEquals(CREATE_EXIST_ERROR_MESSAGE, errors.get(0));
    }
}
