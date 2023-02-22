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
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;

class PseRespondentViewServiceTest {

    private PseRespondentViewService pseRespondentViewService;
    private CaseData caseData;

    @BeforeEach
    void setUp() {
        pseRespondentViewService = new PseRespondentViewService();
        caseData = CaseDataBuilder.builder().build();
    }

    @Test
    void populateSelectDropdownView_checkSendNotificationNotify_returnList() {
        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                    .number("1")
                    .sendNotificationTitle("View notice of hearing")
                    .sendNotificationNotify(BOTH_PARTIES)
                    .build())
                .build(),
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                    .number("2")
                    .sendNotificationTitle("Submit hearing agenda")
                    .sendNotificationNotify(CLAIMANT_ONLY)
                    .build())
                .build(),
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                    .number("3")
                    .sendNotificationTitle("Send Notification Title")
                    .sendNotificationNotify(RESPONDENT_ONLY)
                    .build())
                .build()
        ));

        DynamicFixedListType expected = DynamicFixedListType.from(List.of(
            DynamicValueType.create("1", "1 View notice of hearing"),
            DynamicValueType.create("3", "3 Send Notification Title")
        ));

        assertThat(pseRespondentViewService.populateSelectDropdownView(caseData),
            is(expected));
    }

}
