package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;

class PseRespondentViewServiceTest {

    private PseRespondentViewService pseRespondentViewService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        pseRespondentViewService = new PseRespondentViewService();
        caseData = CaseDataBuilder.builder().build();

        caseData.setSendNotificationCollection(List.of(
                SendNotificationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(SendNotificationType.builder()
                                .number("1")
                                .sendNotificationTitle("View notice of hearing")
                                .sendNotificationSubject(List.of("Other (General correspondence)"))
                                .sendNotificationResponseTribunal(NO)
                                .date("23 February 2023")
                                .sendNotificationNotify(BOTH_PARTIES)
                                .build())
                        .build(),
                SendNotificationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(SendNotificationType.builder()
                                .number("2")
                                .sendNotificationTitle("Submit hearing agenda")
                                .sendNotificationSubject(List.of("Other (General correspondence)"))
                                .sendNotificationResponseTribunal(NO)
                                .date("23 February 2023")
                                .sendNotificationNotify(CLAIMANT_ONLY)
                                .build())
                        .build(),
                SendNotificationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(SendNotificationType.builder()
                                .number("3")
                                .sendNotificationTitle("Send Notification Title")
                                .sendNotificationSubject(List.of("Other (General correspondence)"))
                                .sendNotificationResponseTribunal(NO)
                                .date("23 February 2023")
                                .sendNotificationNotify(RESPONDENT_ONLY)
                                .build())
                        .build()
        ));
    }

    @Test
    void populateSelectDropdownView_checkSendNotificationNotify_returnList() {
        DynamicFixedListType expected = DynamicFixedListType.from(List.of(
            DynamicValueType.create("1", "1 View notice of hearing"),
            DynamicValueType.create("3", "3 Send Notification Title")
        ));

        assertThat(pseRespondentViewService.populateSelectDropdownView(caseData),
            is(expected));
    }

    @Test
    void generateViewNotificationsMarkdown_generatesTable() {
        String expected = "| No | Subject | To party | Date sent | Notification | Response due | Number of "
                + "responses |\r\n|:---------|:---------|:---------|:---------|:---------|:---------|:---------|\r\n"
                + "|1|Other (General correspondence)|Both parties|23 February 2023|View notice of hearing|No|0|\r\n"
                + "|3|Other (General correspondence)|Respondent only|23 February 2023|Send Notification Title|No|0|\r\n"
                + "\r\n";
        String actual = pseRespondentViewService.generateViewNotificationsMarkdown(caseData);

        assertThat(actual, is(expected));
    }
}
