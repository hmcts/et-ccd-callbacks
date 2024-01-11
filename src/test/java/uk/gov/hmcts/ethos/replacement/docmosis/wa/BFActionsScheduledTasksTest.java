package uk.gov.hmcts.ethos.replacement.docmosis.wa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.client.CcdClient;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.SubmitEvent;
import uk.gov.hmcts.ethos.replacement.docmosis.service.AdminUserService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.org.hamcrest.CoreMatchers.is;
import static org.testcontainers.shaded.org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.SCOTLAND_CASE_TYPE_ID;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class BFActionsScheduledTasksTest {

    private BFActionsScheduledTasks bfActionsScheduledTasks;

    @MockBean
    private AdminUserService adminUserService;

    @MockBean
    private CcdClient ccdClient;

    @BeforeEach
    public void setUp() {
        bfActionsScheduledTasks = new BFActionsScheduledTasks(adminUserService, ccdClient);
    }

    @Test
    void testCreateTasksForBFDates_success() throws IOException, URISyntaxException {
        String resource = ResourceLoader.getResource("bfActionTask_oneExpiredDate.json");
        SubmitEvent submitEvent = new ObjectMapper().readValue(resource, SubmitEvent.class);

        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(ENGLANDWALES_CASE_TYPE_ID), any()))
                .thenReturn(List.of(submitEvent));
        when(ccdClient.buildAndGetElasticSearchRequest(any(), eq(SCOTLAND_CASE_TYPE_ID), any()))
                .thenReturn(new ArrayList<>());

        CaseData caseData = submitEvent.getCaseData();
        bfActionsScheduledTasks.createTasksForBFDates();

        assertThat(caseData.getWaRule21ReferralSent(), is(YES));
    }
}