package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.PseResponseTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.PseResponseType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationType;
import uk.gov.hmcts.et.common.model.ccd.types.SendNotificationTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_ONLY;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

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
    void initialOrdReqDetailsTableMarkUp_withHearing() {

        PseResponseTypeItem pseResponseTypeItem = PseResponseTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(PseResponseType.builder()
                .from(CLAIMANT_TITLE)
                .date("10 Aug 2022")
                .response("Response text entered")
                .hasSupportingMaterial(YES)
                .supportingMaterial(List.of(createDocumentTypeItem("My claimant hearing agenda.pdf",
                    "ca35bccd-f507-4243-9133-f6081fb0fe5e", null)))
                .copyToOtherParty(YES)
                .build())
            .build();

        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                    .number("1")
                    .date("5 Aug 2022")
                    .sendNotificationTitle("View notice of hearing")
                    .sendNotificationLetter(YES)
                    .sendNotificationUploadDocument(List.of(
                        createDocumentTypeItem("Letter 4.8 - Hearing notice - hearing agenda.pdf",
                            "5fac5af5-b8ac-458c-a329-31cce78da5c2",
                            "Notice of Hearing and Submit Hearing Agenda document")))
                    .sendNotificationSubject(List.of("Hearing", "Case management orders / requests"))
                    .sendNotificationSelectHearing(DynamicFixedListType.of(
                        DynamicValueType.create("3", "3: Hearing - Leeds - 14 Aug 2022")))
                    .sendNotificationCaseManagement("Case management order")
                    .sendNotificationResponseTribunal("Yes - view document for details")
                    .sendNotificationSelectParties(BOTH_PARTIES)
                    .sendNotificationWhoCaseOrder("Legal Officer")
                    .sendNotificationFullName("Mr Lee Gal Officer")
                    .sendNotificationAdditionalInfo("Additional Info")
                    .sendNotificationNotify(BOTH_PARTIES)
                    .respondCollection(List.of(pseResponseTypeItem))
                    .build())
                .build()
        ));

        caseData.setPseRespondentSelectJudgmentOrderNotification(
            DynamicFixedListType.of(DynamicValueType.create("1",
                "1 View notice of hearing")));

        String expected = """
            |View Application||\r
            |--|--|\r
            |Notification|View notice of hearing|\r
            |Hearing|3: Hearing - Leeds - 14 Aug 2022|\r
            |Date sent|5 Aug 2022|\r
            |Sent by|Tribunal|\r
            |Case management order or request?|Case management order|\r
            |Is a response required?|Yes - view document for details|\r
            |Party or parties to respond|Both parties|\r
            |Additional information|Additional Info|\r
            |Document|<a href="/documents/5fac5af5-b8ac-458c-a329-31cce78da5c2/binary" target="_blank">Letter 4.8 - Hearing notice - hearing agenda.pdf</a>|\r
            |Description|Notice of Hearing and Submit Hearing Agenda document|\r
            |Request made by|Legal Officer|\r
            |Name|Mr Lee Gal Officer|\r
            |Sent to|Both parties|\r
            |Response 1 | |\r
            |--|--|\r
            |Response from | Claimant|\r
            |Response date | 10 Aug 2022|\r
            |What's your response to the tribunal? | Response text entered|\r
            |Supporting material | <a href="/documents/ca35bccd-f507-4243-9133-f6081fb0fe5e/binary" target="_blank">My claimant hearing agenda.pdf</a>\r
            |\r
            |Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? | Yes|\r
            \r
            """;

        assertThat(pseRespondentViewService.initialOrdReqDetailsTableMarkUp(caseData),
            is(expected));
    }

    private DocumentTypeItem createDocumentTypeItem(String fileName, String uuid, String shortDescription) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(UUID.randomUUID().toString());
        documentTypeItem.setValue(DocumentTypeBuilder.builder()
            .withUploadedDocument(fileName, uuid)
            .withShortDescription(shortDescription)
            .build());
        return documentTypeItem;
    }

    @Test
    void initialOrdReqDetailsTableMarkUp_NoHearing() {
        caseData.setSendNotificationCollection(List.of(
            SendNotificationTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(SendNotificationType.builder()
                    .number("1")
                    .date("5 Aug 2022")
                    .sendNotificationTitle("View notice of hearing")
                    .sendNotificationLetter(NO)
                    .sendNotificationSubject(List.of("Case management orders / requests"))
                    .sendNotificationCaseManagement("Request")
                    .sendNotificationResponseTribunal("No")
                    .sendNotificationRequestMadeBy("Judge")
                    .sendNotificationFullName("Mr Lee Gal Officer")
                    .sendNotificationNotify(BOTH_PARTIES)
                    .build())
                .build()
        ));

        caseData.setPseRespondentSelectJudgmentOrderNotification(
            DynamicFixedListType.of(DynamicValueType.create("1",
                "1 View notice of hearing")));

        String expected = """
            |View Application||\r
            |--|--|\r
            |Notification|View notice of hearing|\r
            |Hearing||\r
            |Date sent|5 Aug 2022|\r
            |Sent by|Tribunal|\r
            |Case management order or request?|Request|\r
            |Is a response required?|No|\r
            |Request made by|Judge|\r
            |Name|Mr Lee Gal Officer|\r
            |Sent to|Both parties|\r
            """;

        assertThat(pseRespondentViewService.initialOrdReqDetailsTableMarkUp(caseData), is(expected));
    }

    @Test
    void generateViewNotificationsMarkdown_generatesTable() {
        String expected = """
            | No | Subject | To party | Date sent | Notification | Response due | Number of responses |\r
            |:---------|:---------|:---------|:---------|:---------|:---------|:---------|\r
            |1|Other (General correspondence)|Both parties|23 February 2023|View notice of hearing|No|0|\r
            |3|Other (General correspondence)|Respondent only|23 February 2023|Send Notification Title|No|0|\r
            \r
            """;
        String actual = pseRespondentViewService.generateViewNotificationsMarkdown(caseData);

        assertThat(actual, is(expected));
    }
}
