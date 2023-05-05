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
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentFixtures;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ADMIN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.BOTH_PARTIES;
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

        assertThat(actual.size()).isEqualTo(2);
        assertThat(actual.get(0)).isEqualTo(new String[] {"Document", "Document (txt, 1MB)"});
        assertThat(actual.get(1)).isEqualTo(new String[] {"Description", "Description"});
    }

    @Test
    void addDocumentRows_withNoDocuments_returnsEmptyList() {
        List<String[]> actual = tseService.addDocumentRows(null, AUTH_TOKEN);

        assertThat(actual.size()).isEqualTo(0);
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

        assertThat(actual.size()).isEqualTo(4);
        assertThat(actual.get(0)).isEqualTo(new String[] {"Document", "File1 (txt, 1MB)"});
        assertThat(actual.get(1)).isEqualTo(new String[] {"Description", "Description1"});
        assertThat(actual.get(2)).isEqualTo(new String[] {"Document", "File2 (txt, 1MB)"});
        assertThat(actual.get(3)).isEqualTo(new String[] {"Description", "Description2"});
    }

    @Test
    void formatAdminReply_withAllData() {
        TseRespondType reply = setupAdminTseRespondType();
        String actual = tseService.formatAdminReply(reply, 1, AUTH_TOKEN);

        String expected = "|Response 1||\r\n"
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
                + "\r\n";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatAdminReply_withMissingData() {
        TseRespondType reply = setupAdminTseRespondType();
        reply.setAddDocument(null);
        reply.setRequestMadeBy(null);
        String actual = tseService.formatAdminReply(reply, 1, AUTH_TOKEN);

        String expected = "|Response 1||\r\n"
                + "|--|--|\r\n"
                + "|Response|Title|\r\n"
                + "|Date|2000-01-01|\r\n"
                + "|Sent by|Tribunal|\r\n"
                + "|Case management order or request?|Request|\r\n"
                + "|Is a response required?|No|\r\n"
                + "|Party or parties to respond|Both parties|\r\n"
                + "|Additional information|More data|\r\n"
                + "|Case management order made by|Legal officer|\r\n"
                + "|Full name|Mr Lee Gal Officer|\r\n"
                + "|Sent to|Respondent|\r\n"
                + "\r\n";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void formatApplicationResponses_withFullData() {
        GenericTseApplicationType application = GenericTseApplicationType.builder()
                .applicant(RESPONDENT_TITLE)
                .respondCollection(List.of(
                        TseRespondTypeItem.builder()
                                .id(UUID.randomUUID().toString())
                                .value(setupAdminTseRespondType())
                                .build(),
                        TseRespondTypeItem.builder()
                                .id(UUID.randomUUID().toString())
                                .value(setupNonAdminTseRespondType())
                                .build())
                ).build();

        String actual = tseService.formatApplicationResponses(application, AUTH_TOKEN);

        String expected = "|Response 1||\r\n"
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
                + "\r\n"
                + "|Response 2||\r\n"
                + "|--|--|\r\n"
                + "|Response from|Respondent|\r\n"
                + "|Response date|2000-01-01|\r\n"
                + "|What's your response to the respondent's application|I disagree|\r\n"
                + "|Document|Document (txt, 1MB)|\r\n"
                + "|Description|Description1|\r\n"
                + "|Document|Document (txt, 1MB)|\r\n"
                + "|Description|Description2|\r\n"
                + "|Do you want to copy this correspondence to the other party to satisfy the"
                + " Rules of Procedure?|No|\r\n"
                + "|Details of why you do not want to inform the other party|Details|\r\n\r\n";

        assertThat(actual).isEqualTo(expected);
    }

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
        assertThat(tseService.formatViewApplication(new CaseData(), AUTH_TOKEN)).isEqualTo("");
    }

    @Test
    void formatViewApplication_withNoSelectedApplication() {
        CaseData caseData = setupCaseDataWithAnApplication();
        assertThat(tseService.formatViewApplication(caseData, AUTH_TOKEN)).isEqualTo("");
    }

    @Test
    void formatViewApplication_withAllData() {
        CaseData caseData = setupCaseDataWithAnApplication();

        DynamicFixedListType listType = DynamicFixedListType.from(List.of(DynamicValueType.create("1", "")));
        listType.setValue(listType.getListItems().get(0));
        caseData.setTseRespondSelectApplication(listType);

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
                + "|Sent to|Respondent|\r\n\r\n";

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
                .from(RESPONDENT_TITLE)
                .response("I disagree")
                .copyToOtherParty(NO)
                .copyNoGiveDetails("Details")
                .supportingMaterial(createDocumentList())
                .build();
    }
}
