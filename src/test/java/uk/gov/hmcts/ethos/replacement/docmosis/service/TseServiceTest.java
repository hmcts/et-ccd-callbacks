package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseAdminRecordDecisionTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseAdminRecordDecisionType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentFixtures;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;

@ExtendWith(SpringExtension.class)
class TseServiceTest {
    private TseService tseService;
    @MockBean
    private DocumentManagementService documentManagementService;
    private static final String AUTH_TOKEN = "Bearer authToken";

    @BeforeEach
    void setUp() {
        tseService = new TseService(documentManagementService);
        when(documentManagementService.displayDocNameTypeSizeLink(any(), any())).thenReturn("Document (txt, 1MB)");
    }

    @Test
    void addDocumentRow_returnsTwoStringArrays() {
        DocumentType document = DocumentType.from(DocumentFixtures.getUploadedDocumentType());
        document.setShortDescription("Description");
        List<String[]> actual = tseService.addDocumentRow(document, AUTH_TOKEN);

        assertThat(actual).hasSize(2);
        assertThat(actual.get(0)).isEqualTo(new String[] {"Document", "Document (txt, 1MB)"});
        assertThat(actual.get(1)).isEqualTo(new String[] {"Description", "Description"});
    }

    @Test
    void addDocumentRows_withNoDocuments_returnsEmptyList() {
        List<String[]> actual = tseService.addDocumentRows(null, AUTH_TOKEN);

        assertThat(actual).isEmpty();
    }

    @Test
    void addDocumentRows_withDocuments_returnsListOfStringArrays() {
        DocumentType document = DocumentType.from(DocumentFixtures.getUploadedDocumentType("File1"));
        document.setShortDescription("Description1");

        DocumentType documentTwo = DocumentType.from(DocumentFixtures.getUploadedDocumentType("File2"));
        documentTwo.setShortDescription("Description2");

        when(documentManagementService.displayDocNameTypeSizeLink(document.getUploadedDocument(), AUTH_TOKEN))
                .thenReturn("File1 (txt, 1MB)");
        when(documentManagementService.displayDocNameTypeSizeLink(documentTwo.getUploadedDocument(), AUTH_TOKEN))
                .thenReturn("File2 (txt, 1MB)");

        var documents = List.of(GenericTypeItem.from(document), GenericTypeItem.from(documentTwo));
        List<String[]> actual = tseService.addDocumentRows(documents, AUTH_TOKEN);

        assertThat(actual).hasSize(4);
        assertThat(actual.get(0)).isEqualTo(new String[] {"Document", "File1 (txt, 1MB)"});
        assertThat(actual.get(1)).isEqualTo(new String[] {"Description", "Description1"});
        assertThat(actual.get(2)).isEqualTo(new String[] {"Document", "File2 (txt, 1MB)"});
        assertThat(actual.get(3)).isEqualTo(new String[] {"Description", "Description2"});
    }

//    @Test todo
//    void formatAdminReply_withAllData() {
//        TseRespondType reply = setupAdminTseRespondType();
//        List<String[]> actual = tseService.formatAdminReply(reply, 1, AUTH_TOKEN);
//
//        List<String[]> expected = List.of(
//            new String[] {"|Response 1", "|"},
//                new String[] {"--", "--"},
//                new String[] {"Response", "Title"},
//                new String[] {"Date", "2000-01-01"},
//                new String[] {"Sent by", "Tribunal"},
//                new String[] {"Case management order or request?", "Request"},
//                new String[] {"Is a response required?", "No"},
//                new String[] {"Party or parties to respond", "Both parties"},
//                new String[] {"Additional information", "More data"},
//                new String[] {"Document", "Document (txt, 1MB)"},
//                new String[] {"Description", "Description1"},
//                new String[] {"Document", "Document (txt, 1MB)"},
//                new String[] {"Description", "Description2"},
//                new String[] {"Case management order made by", "Legal officer"},
//                new String[] {"Request made by", "Caseworker"},
//                new String[] {"Full name", "Mr Lee Gal Officer"},
//                new String[] {"Sent to", "Respondent"});
//
//        assertThat(actual).isEqualTo(expected);
//    }

//    @Test
//    void formatAdminReply_withMissingData() {
//        TseRespondType reply = setupAdminTseRespondType();
//        reply.setAddDocument(null);
//        reply.setRequestMadeBy(null);
//        String actual = tseService.formatAdminReply(reply, 1, AUTH_TOKEN);
//
//        String expected = "|Response 1||\r\n"
//                + "|--|--|\r\n"
//                + "|Response|Title|\r\n"
//                + "|Date|2000-01-01|\r\n"
//                + "|Sent by|Tribunal|\r\n"
//                + "|Case management order or request?|Request|\r\n"
//                + "|Is a response required?|No|\r\n"
//                + "|Party or parties to respond|Both parties|\r\n"
//                + "|Additional information|More data|\r\n"
//                + "|Case management order made by|Legal officer|\r\n"
//                + "|Full name|Mr Lee Gal Officer|\r\n"
//                + "|Sent to|Respondent|\r\n"
//                + "\r\n";
//
//        assertThat(actual).isEqualTo(expected);
//    }

//    @Test
//    void formatApplicationResponses_withFullData() {
//        GenericTseApplicationType application = GenericTseApplicationType.builder()
//                .applicant(RESPONDENT_TITLE)
//                .respondCollection(List.of(
//                        TseRespondTypeItem.builder()
//                                .id(UUID.randomUUID().toString())
//                                .value(setupAdminTseRespondType())
//                                .build(),
//                        TseRespondTypeItem.builder()
//                                .id(UUID.randomUUID().toString())
//                                .value(setupNonAdminTseRespondType())
//                                .build())
//                ).build();
//
//        String actual = tseService.formatApplicationResponses(application, AUTH_TOKEN, false);
//
//        String expected = "|Response 1||\r\n"
//                + "|--|--|\r\n"
//                + "|Response|Title|\r\n"
//                + "|Date|2000-01-01|\r\n"
//                + "|Sent by|Tribunal|\r\n"
//                + "|Case management order or request?|Request|\r\n"
//                + "|Is a response required?|No|\r\n"
//                + "|Party or parties to respond|Both parties|\r\n"
//                + "|Additional information|More data|\r\n"
//                + "|Document|Document (txt, 1MB)|\r\n"
//                + "|Description|Description1|\r\n"
//                + "|Document|Document (txt, 1MB)|\r\n"
//                + "|Description|Description2|\r\n"
//                + "|Case management order made by|Legal officer|\r\n"
//                + "|Request made by|Caseworker|\r\n"
//                + "|Full name|Mr Lee Gal Officer|\r\n"
//                + "|Sent to|Respondent|\r\n"
//                + "\r\n"
//                + "|Response 2||\r\n"
//                + "|--|--|\r\n"
//                + "|Response from|Claimant|\r\n"
//                + "|Response date|2000-01-01|\r\n"
//                + "|What's your response to the respondent's application|I disagree|\r\n"
//                + "|Document|Document (txt, 1MB)|\r\n"
//                + "|Description|Description1|\r\n"
//                + "|Document|Document (txt, 1MB)|\r\n"
//                + "|Description|Description2|\r\n"
//                + "|Do you want to copy this correspondence to the other party to satisfy the"
//                + " Rules of Procedure?|No|\r\n"
//                + "|Details of why you do not want to inform the other party|Details|\r\n\r\n";
//
//        assertThat(actual).isEqualTo(expected);
//    }
//
//    @Test
//    void formatApplicationResponses_respondentView() {
//        GenericTseApplicationType application = GenericTseApplicationType.builder()
//                .applicant(RESPONDENT_TITLE)
//                .respondCollection(List.of(
//                        TseRespondTypeItem.builder()
//                                .id(UUID.randomUUID().toString())
//                                .value(setupAdminTseRespondType())
//                                .build(),
//                        TseRespondTypeItem.builder()
//                                .id(UUID.randomUUID().toString())
//                                .value(setupNonAdminTseRespondType())
//                                .build())
//                ).build();
//
//        String actual = tseService.formatApplicationResponses(application, AUTH_TOKEN, true);
//
//        String expected = "|Response 1||\r\n"
//                + "|--|--|\r\n"
//                + "|Response|Title|\r\n"
//                + "|Date|2000-01-01|\r\n"
//                + "|Sent by|Tribunal|\r\n"
//                + "|Case management order or request?|Request|\r\n"
//                + "|Is a response required?|No|\r\n"
//                + "|Party or parties to respond|Both parties|\r\n"
//                + "|Additional information|More data|\r\n"
//                + "|Document|Document (txt, 1MB)|\r\n"
//                + "|Description|Description1|\r\n"
//                + "|Document|Document (txt, 1MB)|\r\n"
//                + "|Description|Description2|\r\n"
//                + "|Case management order made by|Legal officer|\r\n"
//                + "|Request made by|Caseworker|\r\n"
//                + "|Full name|Mr Lee Gal Officer|\r\n"
//                + "|Sent to|Respondent|\r\n\r\n";
//        assertThat(actual).isEqualTo(expected);
//    }

    @Test
    void formatApplicationDetails_noRule92() {
        GenericTseApplicationType application = setupTestApplication();
        String actual = tseService.formatApplicationDetails(application, AUTH_TOKEN, false);

        String expected = "|Application||\r\n"
                + "|--|--|\r\n"
                + "|Applicant|Respondent|\r\n"
                + "|Type of application|Amend response|\r\n"
                + "|Application date|2000-01-01|\r\n"
                + "|What do you want to tell or ask the tribunal?|Details|\r\n"
                + "|Supporting material|Document (txt, 1MB)|\r\n";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatApplicationDetails_yesRule92() {
        GenericTseApplicationType application = setupTestApplication();
        String actual = tseService.formatApplicationDetails(application, AUTH_TOKEN, true);

        String expected = "|Application||\r\n"
                + "|--|--|\r\n"
                + "|Applicant|Respondent|\r\n"
                + "|Type of application|Amend response|\r\n"
                + "|Application date|2000-01-01|\r\n"
                + "|What do you want to tell or ask the tribunal?|Details|\r\n"
                + "|Supporting material|Document (txt, 1MB)|\r\n"
                + "|Do you want to copy this correspondence to the other party to satisfy"
                + " the Rules of Procedure?|No|\r\n"
                + "|Details of why you do not want to inform the other party|Details|\r\n";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatViewApplication_withNoApplications() {
        CaseData caseData = new CaseData();
        assertThatThrownBy(() -> tseService.formatViewApplication(caseData, AUTH_TOKEN))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void formatViewApplication_withNoSelectedApplication() {
        CaseData caseData = setupCaseDataWithAnApplication();
        assertThatThrownBy(() -> tseService.formatViewApplication(caseData, AUTH_TOKEN))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void formatViewApplication_withAllResponse() {
        CaseData caseData = setupCaseDataWithAnApplication();

        DynamicFixedListType listType = DynamicFixedListType.from(List.of(DynamicValueType.create("1", "")));
        listType.setValue(listType.getListItems().get(0));
        caseData.setTseViewApplicationSelect(listType);

        String expected = "|Application||\r\n"
                + "|--|--|\r\n"
                + "|Applicant|Respondent|\r\n"
                + "|Type of application|Amend response|\r\n"
                + "|Application date|2000-01-01|\r\n"
                + "|What do you want to tell or ask the tribunal?|Details|\r\n"
                + "|Supporting material|Document (txt, 1MB)|\r\n"
                + "|Do you want to copy this correspondence to the other party to satisfy"
                + " the Rules of Procedure?|No|\r\n"
                + "|Details of why you do not want to inform the other party|Details|\r\n"
                + "\r\n"
                + "|Response 1||\r\n"
                + "|--|--|\r\n"
                + "|Response|Title|\r\n"
                + "|Date|2000-01-01|\r\n"
                + "|Sent by|Tribunal|\r\n"
                + "|Case management order or request?|Request|\r\n"
                + "|Is a response required?|No|\r\n"
                + "|Party or parties to respond|Both parties|\r\n"
                + "|Additional information|More data|\r\n"
                + "|Document|Document (txt, 1MB)|\r\n"
                + "|Description|Description1|\r\n"
                + "|Document|Document (txt, 1MB)|\r\n"
                + "|Description|Description2|\r\n"
                + "|Case management order made by|Legal officer|\r\n"
                + "|Request made by|Caseworker|\r\n"
                + "|Full name|Mr Lee Gal Officer|\r\n"
                + "|Sent to|Respondent|\r\n\r\n\r\n";

        assertThat(tseService.formatViewApplication(caseData, AUTH_TOKEN)).isEqualTo(expected);
    }

    @Test
    void formatViewApplication_withResponseAndDecisions() {
        TseAdminRecordDecisionType tseAdminRecordDecisionType1 = new TseAdminRecordDecisionType();
        tseAdminRecordDecisionType1.setDecision("Granted");
        tseAdminRecordDecisionType1.setDate("2023-01-01");
        tseAdminRecordDecisionType1.setTypeOfDecision("Judgment");
        tseAdminRecordDecisionType1.setAdditionalInformation("MORE INFO");
        tseAdminRecordDecisionType1.setEnterNotificationTitle("title");
        tseAdminRecordDecisionType1.setDecisionMadeBy("Judge");
        tseAdminRecordDecisionType1.setDecisionMadeByFullName("John Doe");
        tseAdminRecordDecisionType1.setSelectPartyNotify("Respondent");
        TseAdminRecordDecisionTypeItem decisionType1 = new TseAdminRecordDecisionTypeItem();
        decisionType1.setId("1");
        decisionType1.setValue(tseAdminRecordDecisionType1);

        TseAdminRecordDecisionType tseAdminRecordDecisionType2 = new TseAdminRecordDecisionType();
        tseAdminRecordDecisionType2.setDecision("Granted");
        tseAdminRecordDecisionType2.setDate("2023-01-02");
        tseAdminRecordDecisionType2.setTypeOfDecision("Judgment");
        tseAdminRecordDecisionType2.setAdditionalInformation("MORE INFO");
        tseAdminRecordDecisionType2.setEnterNotificationTitle("title2");
        tseAdminRecordDecisionType2.setDecisionMadeBy("Judge");
        tseAdminRecordDecisionType2.setDecisionMadeByFullName("John Doe");
        tseAdminRecordDecisionType2.setSelectPartyNotify("Respondent");
        TseAdminRecordDecisionTypeItem decisionType2 = new TseAdminRecordDecisionTypeItem();
        decisionType2.setId("2");
        decisionType2.setValue(tseAdminRecordDecisionType2);

        TseAdminRecordDecisionType tseAdminRecordDecisionType3 = new TseAdminRecordDecisionType();
        tseAdminRecordDecisionType3.setDecision("Granted");
        tseAdminRecordDecisionType3.setDate("2023-01-03");
        tseAdminRecordDecisionType3.setTypeOfDecision("Judgment");
        tseAdminRecordDecisionType3.setAdditionalInformation("MORE INFO");
        tseAdminRecordDecisionType3.setEnterNotificationTitle("title3");
        tseAdminRecordDecisionType3.setDecisionMadeBy("Judge");
        tseAdminRecordDecisionType3.setDecisionMadeByFullName("John Doe");
        tseAdminRecordDecisionType3.setSelectPartyNotify("Respondent");
        TseAdminRecordDecisionTypeItem decisionType3 = new TseAdminRecordDecisionTypeItem();
        decisionType3.setId("3");
        decisionType3.setValue(tseAdminRecordDecisionType3);

        CaseData caseData = setupCaseDataWithAnApplication();
        caseData.getGenericTseApplicationCollection().get(0).getValue().setAdminDecision(List.of(decisionType1,
            decisionType2, decisionType3));
        DynamicFixedListType listType = DynamicFixedListType.from(List.of(DynamicValueType.create("1", "")));
        listType.setValue(listType.getListItems().get(0));
        caseData.setTseViewApplicationSelect(listType);

        String expected = "|Application||\r\n"
            + "|--|--|\r\n"
            + "|Applicant|Respondent|\r\n"
            + "|Type of application|Amend response|\r\n"
            + "|Application date|2000-01-01|\r\n"
            + "|What do you want to tell or ask the tribunal?|Details|\r\n"
            + "|Supporting material|Document (txt, 1MB)|\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure?|No|\r\n"
            + "|Details of why you do not want to inform the other party|Details|\r\n"
            + "\r\n"
            + "|Response 1||\r\n"
            + "|--|--|\r\n"
            + "|Response|Title|\r\n"
            + "|Date|2000-01-01|\r\n"
            + "|Sent by|Tribunal|\r\n"
            + "|Case management order or request?|Request|\r\n"
            + "|Is a response required?|No|\r\n"
            + "|Party or parties to respond|Both parties|\r\n"
            + "|Additional information|More data|\r\n"
            + "|Document|Document (txt, 1MB)|\r\n"
            + "|Description|Description1|\r\n"
            + "|Document|Document (txt, 1MB)|\r\n"
            + "|Description|Description2|\r\n"
            + "|Case management order made by|Legal officer|\r\n"
            + "|Request made by|Caseworker|\r\n"
            + "|Full name|Mr Lee Gal Officer|\r\n"
            + "|Sent to|Respondent|\r\n"
            + "\r\n\r\n"
            + "|Decision||\r\n"
            + "|--|--|\r\n"
            + "|Notification|title3|\r\n"
            + "|Decision|Granted|\r\n"
            + "|Date|2023-01-03|\r\n"
            + "|Sent by|Tribunal|\r\n"
            + "|Type of decision|Judgment|\r\n"
            + "|Additional information|MORE INFO|\r\n"
            + "|Decision made by|Judge|\r\n"
            + "|Name|John Doe|\r\n"
            + "|Sent to|Respondent|\r\n\r\n"
            + "|Decision||\r\n"
            + "|--|--|\r\n"
            + "|Notification|title2|\r\n"
            + "|Decision|Granted|\r\n"
            + "|Date|2023-01-02|\r\n"
            + "|Sent by|Tribunal|\r\n"
            + "|Type of decision|Judgment|\r\n"
            + "|Additional information|MORE INFO|\r\n"
            + "|Decision made by|Judge|\r\n"
            + "|Name|John Doe|\r\n"
            + "|Sent to|Respondent|\r\n";

        assertThat(tseService.formatViewApplication(caseData, AUTH_TOKEN)).isEqualTo(expected);
    }

    private CaseData setupCaseDataWithAnApplication() {
        CaseData caseData = new CaseData();

        caseData.setGenericTseApplicationCollection(List.of(
                GenericTseApplicationTypeItem.builder()
                        .id(UUID.randomUUID().toString())
                        .value(setupTestApplication())
                        .build())
        );

        return caseData;
    }

    private GenericTseApplicationType setupTestApplication() {
        return GenericTseApplicationType.builder()
                .applicant(RESPONDENT_TITLE)
                .type(TSE_APP_AMEND_RESPONSE)
                .number("1")
                .date("2000-01-01")
                .details("Details")
                .copyToOtherPartyYesOrNo(NO)
                .copyToOtherPartyText("Details")
                .documentUpload(DocumentFixtures.getUploadedDocumentType("application.docx"))
                .respondCollection(List.of(
                        TseRespondTypeItem.builder()
                                .id(UUID.randomUUID().toString())
                                .value(setupAdminTseRespondType())
                                .build())
                ).build();
    }

    private List<GenericTypeItem<DocumentType>> createDocumentList() {
        DocumentType document = DocumentType.from(DocumentFixtures.getUploadedDocumentType("File1"));
        document.setShortDescription("Description1");

        DocumentType documentTwo = DocumentType.from(DocumentFixtures.getUploadedDocumentType("File2"));
        documentTwo.setShortDescription("Description2");

        return List.of(GenericTypeItem.from(document), GenericTypeItem.from(documentTwo));
    }

    private TseRespondType setupAdminTseRespondType() {
        return TseRespondType.builder()
                .enterResponseTitle("Title")
                .date("2000-01-01")
                .isCmoOrRequest("Request")
                .isResponseRequired("No")
                .selectPartyRespond(BOTH_PARTIES)
                .additionalInformation("More data")
                .cmoMadeBy("Legal officer")
                .requestMadeBy("Caseworker")
                .madeByFullName("Mr Lee Gal Officer")
                .selectPartyNotify(RESPONDENT_TITLE)
                .addDocument(createDocumentList())
                .from(ADMIN)
                .response("I disagree")
                .copyToOtherParty(NO)
                .copyNoGiveDetails("Details")
                .build();
    }

    private TseRespondType setupNonAdminTseRespondType() {
        return TseRespondType.builder()
                .date("2000-01-01")
                .from(CLAIMANT_TITLE)
                .response("I disagree")
                .copyToOtherParty(NO)
                .copyNoGiveDetails("Details")
                .supportingMaterial(createDocumentList())
                .build();
    }
}
