package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.model.labels.LabelPayloadEvent;
import uk.gov.hmcts.ecm.common.model.schedule.SchedulePayloadEvent;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.et.common.model.multiples.MultipleDetails;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.MultipleUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class SingleCasesReadingServiceTest {

    @Mock
    private CcdClient ccdClient;
    @InjectMocks
    private SingleCasesReadingService singleCasesReadingService;

    private MultipleDetails multipleDetails;
    private String userToken;
    private List<SubmitEvent> submitEventList;
    private HashSet<SchedulePayloadEvent> schedulePayloadEvents;
    private List<LabelPayloadEvent> labelPayloadEvents;

    @BeforeEach
    public void setUp() {
        multipleDetails = new MultipleDetails();
        multipleDetails.setCaseData(MultipleUtil.getMultipleData());
        multipleDetails.setCaseTypeId("Manchester_Multiple");
        submitEventList = MultipleUtil.getSubmitEvents();
        schedulePayloadEvents = MultipleUtil.getSchedulePayloadEvents();
        labelPayloadEvents = MultipleUtil.getLabelPayloadEvents();
        userToken = "authString";
    }

    @Test
    void retrieveSingleCase() throws IOException {
        when(ccdClient.retrieveCasesElasticSearchForCreation(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(Collections.singletonList("240001/2020")),
                multipleDetails.getCaseData().getMultipleSource()))
                .thenReturn(submitEventList);
        singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                "240001/2020",
                multipleDetails.getCaseData().getMultipleSource());
        verify(ccdClient, times(1)).retrieveCasesElasticSearchForCreation(userToken,
                "Manchester",
                new ArrayList<>(Collections.singletonList("240001/2020")),
                multipleDetails.getCaseData().getMultipleSource());
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    void retrieveSingleCaseException() throws IOException {
        when(ccdClient.retrieveCasesElasticSearchForCreation(anyString(),
                anyString(),
                anyList(),
                anyString()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        SubmitEvent submitEvent = singleCasesReadingService.retrieveSingleCase(userToken,
                multipleDetails.getCaseTypeId(),
                "240001/2020",
                multipleDetails.getCaseData().getMultipleSource());
        assertNull(submitEvent);
    }

    @Test
    void retrieveScheduleCases() throws IOException {
        when(ccdClient.retrieveCasesElasticSearchSchedule(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(Collections.singletonList("240001/2020"))))
                .thenReturn(new ArrayList<>(schedulePayloadEvents));
        singleCasesReadingService.retrieveScheduleCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(Collections.singletonList("240001/2020")));
        verify(ccdClient, times(1)).retrieveCasesElasticSearchSchedule(userToken,
                "Manchester",
                new ArrayList<>(Collections.singletonList("240001/2020")));
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    void retrieveScheduleCasesException() throws IOException {
        when(ccdClient.retrieveCasesElasticSearchSchedule(anyString(),
                anyString(),
                anyList()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        Set<SchedulePayloadEvent> schedulePayloadEventList = singleCasesReadingService
                .retrieveScheduleCases(userToken,
                    multipleDetails.getCaseTypeId(),
                    new ArrayList<>(Collections.singletonList("240001/2020")));
        assertEquals(schedulePayloadEventList, new HashSet<>());
    }

    @Test
    void retrieveLabelCases() throws IOException {
        when(ccdClient.retrieveCasesElasticSearchLabels(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(Collections.singletonList("240001/2020"))))
                .thenReturn(labelPayloadEvents);
        singleCasesReadingService.retrieveLabelCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(Collections.singletonList("240001/2020")));
        verify(ccdClient, times(1)).retrieveCasesElasticSearchLabels(userToken,
                "Manchester",
                new ArrayList<>(Collections.singletonList("240001/2020")));
        verifyNoMoreInteractions(ccdClient);
    }

    @Test
    void retrieveLabelCasesException() throws IOException {
        when(ccdClient.retrieveCasesElasticSearchLabels(anyString(),
                anyString(),
                anyList()))
                .thenThrow(new InternalException(ERROR_MESSAGE));
        List<LabelPayloadEvent> labelPayloadEvents = singleCasesReadingService.retrieveLabelCases(userToken,
                multipleDetails.getCaseTypeId(),
                new ArrayList<>(Collections.singletonList("240001/2020")));
        assertEquals(labelPayloadEvents, new ArrayList<>());
    }

}