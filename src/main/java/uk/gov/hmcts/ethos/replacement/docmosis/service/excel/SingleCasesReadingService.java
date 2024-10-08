package uk.gov.hmcts.ethos.replacement.docmosis.service.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.ecm.common.model.labels.LabelPayloadEvent;
import uk.gov.hmcts.ecm.common.model.schedule.NotificationSchedulePayloadEvent;
import uk.gov.hmcts.ecm.common.model.schedule.SchedulePayloadEvent;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service("singleCasesReadingService")
public class SingleCasesReadingService {

    private final CcdClient ccdClient;

    @Autowired
    public SingleCasesReadingService(CcdClient ccdClient) {
        this.ccdClient = ccdClient;
    }

    public SubmitEvent retrieveSingleCase(String userToken, String multipleCaseTypeId, String caseId,
                                          String multipleSource) {

        List<SubmitEvent> submitEvents = retrieveSingleCases(userToken,
                multipleCaseTypeId,
                new ArrayList<>(Collections.singletonList(caseId)),
                multipleSource);

        return submitEvents.isEmpty() ? null : submitEvents.get(0);
    }

    public List<SubmitEvent> retrieveSingleCases(String userToken, String multipleCaseTypeId, List<String> caseIds,
                                                 String multipleSource) {

        List<SubmitEvent> submitEvents = new ArrayList<>();

        try {
            submitEvents = ccdClient.retrieveCasesElasticSearchForCreation(userToken,
                    UtilHelper.getCaseTypeId(multipleCaseTypeId),
                    caseIds,
                    multipleSource);

        } catch (Exception ex) {

            log.error("Error retrieving single cases");

            log.error(ex.getMessage());

        }

        return submitEvents;

    }

    public List<LabelPayloadEvent> retrieveLabelCases(String userToken, String multipleCaseTypeId,
                                                      List<String> caseIds) {

        List<LabelPayloadEvent> labelEvents = new ArrayList<>();

        try {
            labelEvents = ccdClient.retrieveCasesElasticSearchLabels(userToken,
                    UtilHelper.getCaseTypeId(multipleCaseTypeId),
                    caseIds);

        } catch (Exception ex) {

            log.error("Error retrieving label cases");

            log.error(ex.getMessage());

        }

        return labelEvents;

    }

    public Set<SchedulePayloadEvent> retrieveScheduleCases(String userToken, String multipleCaseTypeId,
                                                           List<String> caseIds) {

        HashSet<SchedulePayloadEvent> schedulePayloadEvents = new HashSet<>();

        try {
            schedulePayloadEvents = new HashSet<>(ccdClient.retrieveCasesElasticSearchSchedule(userToken,
                    UtilHelper.getCaseTypeId(multipleCaseTypeId),
                    caseIds));

        } catch (Exception ex) {
            log.error("Error retrieving schedule cases: {}", ex.getMessage(), ex);
        }

        return schedulePayloadEvents;

    }

    /**
     * Gets notification schedules with ES Query.
     *
     * @param userToken          user token
     * @param multipleCaseTypeId multiple case type (EW or Scotland)
     * @param caseIds            all single cases on the multiple
     * @return schedulePayloadEvents
     */
    public Set<NotificationSchedulePayloadEvent> retrieveNotificationScheduleCases(String userToken,
                                                                                   String multipleCaseTypeId,
                                                                                   List<String> caseIds) {
        HashSet<NotificationSchedulePayloadEvent> schedulePayloadEvents = new HashSet<>();
        try {
            schedulePayloadEvents = retryableESNotificationsQuery(userToken, multipleCaseTypeId, caseIds);
        } catch (Exception ex) {
            log.error("Error retrieving notification schedule cases: {}", ex.getMessage(), ex);
        }
        return schedulePayloadEvents;
    }

    /**
     * Gets notification schedules with ES Query with default retryable behaviour of 3 attempts with one-second delay.
     *
     * @param userToken          user token
     * @param multipleCaseTypeId multiple case type (EW or Scotland)
     * @param caseIds            all single cases on the multiple
     * @return schedulePayloadEvents
     */
    @Retryable
    private HashSet<NotificationSchedulePayloadEvent> retryableESNotificationsQuery(
            String userToken,
            String multipleCaseTypeId,
            List<String> caseIds) throws IOException {
        return new HashSet<>(ccdClient.retrieveCasesElasticNotificationSearchSchedule(userToken,
                UtilHelper.getCaseTypeId(multipleCaseTypeId),
                caseIds));
    }
}
