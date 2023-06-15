package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class MultipleCasesSendingServiceTest {

    @Mock
    private CcdClient ccdClient;
    @InjectMocks
    private MultipleCasesSendingService multipleCasesSendingService;

    private MultipleDetails multipleDetails;
    private String userToken;
    private CCDRequest ccdRequest;

    @BeforeEach
    public void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        multipleDetails.setCaseTypeId("Manchester_Multiple");
        userToken = "authString";
        ccdRequest = new CCDRequest();
    }

    @Test
    void sendUpdateToMultiple() throws IOException {
        when(ccdClient.startBulkAmendEventForCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(),
                multipleDetails.getCaseId()))
                .thenReturn(ccdRequest);
        multipleCasesSendingService.sendUpdateToMultiple(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(),
                multipleDetails.getCaseData(),
                multipleDetails.getCaseId());
        verify(ccdClient, times(1)).startBulkAmendEventForCase(userToken,
                multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(),
                multipleDetails.getCaseId());
        verify(ccdClient, times(1)).submitMultipleEventForCase(userToken,
                multipleDetails.getCaseData(),
                multipleDetails.getCaseTypeId(),
                multipleDetails.getJurisdiction(),
                ccdRequest,
                multipleDetails.getCaseId());
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    void sendUpdateToMultipleException() throws IOException {
        assertThrows(Exception.class, () -> {
            when(ccdClient.startBulkAmendEventForCase(userToken,
                    multipleDetails.getCaseTypeId(),
                    multipleDetails.getJurisdiction(),
                    multipleDetails.getCaseId()))
                    .thenThrow(new InternalException(ERROR_MESSAGE));
            multipleCasesSendingService.sendUpdateToMultiple(userToken,
                    multipleDetails.getCaseTypeId(),
                    multipleDetails.getJurisdiction(),
                    multipleDetails.getCaseData(),
                    multipleDetails.getCaseId());
            verify(ccdClient, times(1)).startBulkAmendEventForCase(userToken,
                    multipleDetails.getCaseTypeId(),
                    multipleDetails.getJurisdiction(),
                    multipleDetails.getCaseId());
            verifyNoMoreInteractions(ccdClient);

        });
    }

}