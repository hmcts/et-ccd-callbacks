package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseStatusTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantHearingPreference;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.TseStatusType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.RetentionPeriodDuration;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_POSTPONE_A_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.UPDATED;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.et.common.model.ccd.types.citizenhub.ClaimantTse.CY_RESPONDING_TO_APP_TYPE_MAP;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.ENGLISH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.LINK_TO_DOCUMENT;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.WELSH_LANGUAGE_PARAM;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getClaimantRepSelectedApplicationType;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.TseHelper.getRespondentSelectedApplicationType;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.TseApplicationUtil.getGenericTseApplicationTypeItem;

@ExtendWith(SpringExtension.class)
class TseHelperTest {
    private static final DynamicValueType SELECT_APPLICATION = DynamicValueType.create("1", "");

    private CaseData caseData;
    private GenericTseApplicationTypeItem genericTseApplicationTypeItem;
    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    public void setUp() {
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("First", "Last")
            .withEthosCaseReference("1234")
            .withClaimant("First Last")
            .withRespondent("Respondent Name", YES, "13 December 2022", false)
            .build();

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(RESPONDENT_TITLE)
                .withDate("13 December 2022").withDue("20 December 2022").withType("Withdraw my claim")
                .withCopyToOtherPartyYesOrNo(YES).withDetails("Text").withNumber("1")
                .withResponsesCount("0").withStatus(OPEN_STATE).build();

        genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));
    }

    @Test
    void populateSelectApplicationDropdown_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        DynamicFixedListType actual = TseHelper.populateRespondentSelectApplication(caseData);
        assertNull(actual);
    }

    @Test
    void populateSelectApplicationDropdown_withAnApplication_returnsDynamicList() {
        DynamicFixedListType actual = TseHelper.populateRespondentSelectApplication(caseData);
        assert actual != null;
        assertThat(actual.getListItems().size(), is(1));
    }

    @Test
    void populateClaimantRepSelectApplicationDropdown_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        DynamicFixedListType actual = TseHelper.populateClaimantRepSelectApplication(caseData);
        assertNull(actual);
    }

    @Test
    void populateClaimantRepSelectApplicationDropdown_withAnApplication_returnsDynamicList() {
        DynamicFixedListType actual = TseHelper.populateClaimantRepSelectApplication(caseData);
        assert actual != null;
        assertThat(actual.getListItems().size(), is(1));
    }

    @ParameterizedTest
    @MethodSource("populateSelectApplicationDropdownHasTribunalResponse")
    void populateSelectApplicationDropdownHasTribunalResponse(String respondentResponseRequired,
                                                              int numberOfApplication) {
        genericTseApplicationTypeItem = getGenericTseApplicationTypeItem(
            respondentResponseRequired);
        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));

        DynamicFixedListType actual = TseHelper.populateRespondentSelectApplication(caseData);
        assert actual != null;
        assertThat(actual.getListItems().size(), is(numberOfApplication));
    }

    private static Stream<Arguments> populateSelectApplicationDropdownHasTribunalResponse() {
        return Stream.of(
            Arguments.of(NO, 0),
            Arguments.of(null, 0),
            Arguments.of(YES, 1)
        );
    }

    @Test
    void populateSelectApplicationDropdown_withAnApplication_returnsEmpty() {
        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(RESPONDENT_TITLE)
            .withDate("13 December 2022").withDue("20 December 2022").withType("Order a witness to attend")
            .withDetails("Text").withNumber("1")
            .withResponsesCount("0").withStatus(OPEN_STATE).build();

        genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        CaseData caseData1 = CaseDataBuilder.builder()
                .withClaimantIndType("First", "Last")
                .withEthosCaseReference("1234")
                .withClaimant("First Last")
                .withRespondent("Respondent Name", YES, "13 December 2022", false)
                .build();
        caseData1.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));

        DynamicFixedListType actual = TseHelper.populateRespondentSelectApplication(caseData1);
        assert actual != null;
        assertThat(actual.getListItems().size(), is(0));
    }

    @Test
    void populateSelectApplicationDropdown_withRespondentReply_returnsNothing() {
        caseData.getGenericTseApplicationCollection().get(0).getValue()
            .setRespondCollection(List.of(TseRespondTypeItem.builder()
                .id(UUID.randomUUID().toString())
                .value(TseRespondType.builder()
                    .from(RESPONDENT_TITLE)
                    .build())
                .build()));
        DynamicFixedListType actual = TseHelper.populateRespondentSelectApplication(caseData);
        assert actual != null;
        assertThat(actual.getListItems().size(), is(0));
    }

    @Test
    void setDataForRespondingToApplication_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        TseHelper.setDataForRespondingToApplication(caseData, false);
        assertNull(caseData.getTseResponseIntro());
    }

    @Test
    void setDataForRespondingToApplication_withAGroupBApplication_restoresData() {
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(SELECT_APPLICATION);
        TseHelper.setDataForRespondingToApplication(caseData, false);
        String expected = """
            <p>The respondent has applied to <strong>Withdraw my claim</strong>.</p>
            <p>You do not need to respond to this application.</p>
            <p>If you have any objections or responses to their application you must send them to the tribunal as soon
            as possible and by <strong>20 December 2022</strong> at the latest.
            
            If you need more time to respond, you may request more time from the tribunal. If you do not respond or
            request more time to respond, the tribunal will consider the application without your response.</p>
            """;

        assertThat(caseData.getTseResponseIntro(), is(expected));
    }

    @Test
    void setDataForRespondingToApplication_withAGroupAApplication_restoresData() {
        caseData.getGenericTseApplicationCollection().get(0).getValue().setType(TSE_APP_POSTPONE_A_HEARING);
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(SELECT_APPLICATION);
        TseHelper.setDataForRespondingToApplication(caseData, false);
        String expected = """
            <p>The respondent has applied to <strong>Postpone a hearing</strong>.</p>
            
            <p>If you have any objections or responses to their application you must send them to the tribunal as soon
            as possible and by <strong>20 December 2022</strong> at the latest.
            
            If you need more time to respond, you may request more time from the tribunal. If you do not respond or
            request more time to respond, the tribunal will consider the application without your response.</p>
            """;

        assertThat(caseData.getTseResponseIntro(), is(expected));
    }

    @Test
    void setDataForRespondingToApplication_withApplicationWithDocument_restoresData() {
        UploadedDocumentType documentType =
            UploadedDocumentBuilder.builder().withFilename("image.png").withUuid("1234").build();
        caseData.getGenericTseApplicationCollection().get(0).getValue().setDocumentUpload(documentType);
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(SELECT_APPLICATION);
        TseHelper.setDataForRespondingToApplication(caseData, false);
        String expected = """
            | | |\r
            |--|--|\r
            |Application date | 13 December 2022\r
            |Details of the application | Text\r
            Application file upload | <a href="/documents/1234/binary" target="_blank">image.png</a>""";

        assertThat(caseData.getTseResponseTable(), is(expected));
    }

    @Test
    void getReplyDocumentRequest_generatesData() throws JsonProcessingException {
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(SELECT_APPLICATION);

        UploadedDocumentType docType = new UploadedDocumentType();
        docType.setDocumentBinaryUrl("http://dm-store:8080/documents/1234/binary");
        docType.setDocumentFilename("image.png");
        docType.setDocumentUrl("http://dm-store:8080/documents/1234");

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(docType);

        GenericTypeItem<DocumentType> item = new GenericTypeItem<>();
        item.setValue(documentType);
        item.setId("78910");

        caseData.setTseResponseSupportingMaterial(List.of(item));
        String expectedDate = UtilHelper.formatCurrentDate(LocalDate.now());
        String replyDocumentRequest = TseHelper.getReplyDocumentRequest(caseData, "");
        String expected = "{\"accessKey\":\"\",\"templateName\":\"EM-TRB-EGW-ENG-01212.docx\","
            + "\"outputName\":\"Withdraw my claim Reply.pdf\",\"data\":{\"caseNumber\":\"1234\","
            + "\"type\":\"Withdraw my claim\",\"responseDate\":\"" + expectedDate + "\",\"supportingYesNo\":\"Yes\","
            + "\"documentCollection\":[{\"id\":\"78910\","
            + "\"value\":{\"typeOfDocument\":null,"
            + "\"uploadedDocument\":{\"document_binary_url\":\"http://dm-store:8080/documents/1234/binary"
            + "\",\"document_filename\":\"image.png\","
            + "\"document_url\":\"http://dm-store:8080/documents/1234\",\"category_id\":null,\"upload_timestamp\""
            + ":null},\"ownerDocument\":null,"
            + "\"creationDate\":null,\"shortDescription\":null,\"topLevelDocuments\":null,\"startingClaimDocuments\":"
            + "null,\"responseClaimDocuments\":null,\"initialConsiderationDocuments\":null,\"caseManagementDocuments\""
            + ":null,\"withdrawalSettledDocuments\":null,\"hearingsDocuments\":null,\"judgmentAndReasonsDocuments\":"
            + "null,\"reconsiderationDocuments\":null,\"miscDocuments\":null,\"documentType\":null,\""
            + "dateOfCorrespondence\":null,\"docNumber\":null,\"tornadoEmbeddedPdfUrl\":null,"
            + "\"excludeFromDcf\":null,\"documentIndex\":null}}],"
            + "\"copy\":\"Yes\","
            + "\"response\":\"Text\",\"respondentParty\":\"Respondent\"}}";

        assertThat(replyDocumentRequest, is(expected));
    }

    @Test
    void getClaimantReplyDocumentRequest_generatesData() throws JsonProcessingException {
        caseData.setClaimantRepRespondSelectApplication(TseHelper.populateClaimantRepSelectApplication(caseData));
        caseData.getClaimantRepRespondSelectApplication().setValue(SELECT_APPLICATION);

        UploadedDocumentType docType = new UploadedDocumentType();
        docType.setDocumentBinaryUrl("http://dm-store:8080/documents/1234/binary");
        docType.setDocumentFilename("image.png");
        docType.setDocumentUrl("http://dm-store:8080/documents/1234");

        DocumentType documentType = new DocumentType();
        documentType.setUploadedDocument(docType);

        GenericTypeItem<DocumentType> item = new GenericTypeItem<>();
        item.setValue(documentType);
        item.setId("78910");

        caseData.setTseResponseSupportingMaterial(List.of(item));
        String expectedDate = UtilHelper.formatCurrentDate(LocalDate.now());
        String replyDocumentRequest = TseHelper.getClaimantReplyDocumentRequest(caseData, "");
        String expected = "{\"accessKey\":\"\",\"templateName\":\"EM-TRB-EGW-ENG-01212.docx\","
                + "\"outputName\":\"Withdraw my claim Reply.pdf\",\"data\":{\"caseNumber\":\"1234\","
                + "\"type\":\"Withdraw my claim\",\"responseDate\":\""
                + expectedDate + "\",\"supportingYesNo\":\"Yes\","
                + "\"documentCollection\":[{\"id\":\"78910\","
                + "\"value\":{\"typeOfDocument\":null,"
                + "\"uploadedDocument\":{\"document_binary_url\":\"http://dm-store:8080/documents/1234/binary"
                + "\",\"document_filename\":\"image.png\","
                + "\"document_url\":\"http://dm-store:8080/documents/1234\",\"category_id\":null,\"upload_timestamp\""
                + ":null},\"ownerDocument\":null,"
                + "\"creationDate\":null,"
                + "\"shortDescription\":null,\"topLevelDocuments\":null,\"startingClaimDocuments\":"
                + "null,\"responseClaimDocuments\":null,"
                + "\"initialConsiderationDocuments\":null,\"caseManagementDocuments\""
                + ":null,\"withdrawalSettledDocuments\":null,"
                + "\"hearingsDocuments\":null,\"judgmentAndReasonsDocuments\":"
                + "null,\"reconsiderationDocuments\":null,\"miscDocuments\":null,\"documentType\":null,\""
                + "dateOfCorrespondence\":null,\"docNumber\":null,\"tornadoEmbeddedPdfUrl\":null,"
                + "\"excludeFromDcf\":null,\"documentIndex\":null}}],"
                + "\"copy\":\"Yes\","
                + "\"response\":\"Text\",\"respondentParty\":\"Respondent\"}}";

        assertThat(replyDocumentRequest, is(expected));
    }

    @Test
    void getPersonalisationForResponse_withResponse() throws NotificationClientException {
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(SELECT_APPLICATION);
        caseData.setTseResponseText("TseResponseText");
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(ENGLISH_LANGUAGE);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("CaseId");
        caseDetails.setCaseData(caseData);
        byte[] document = {};
        Map<String, Object> actual = TseHelper.getPersonalisationForResponse(
                caseDetails, document, "citizenUrlCaseId", false);

        Map<String, Object> expected = Map.of(
            "linkToCitizenHub", "citizenUrlCaseId",
            "caseNumber", "1234",
            "applicationType", "Withdraw my claim",
            "response", "TseResponseText",
            "claimant", "First Last",
            "respondents", "Respondent Name",
            "linkToDocument", NotificationClient.prepareUpload(document, true,
                        new RetentionPeriodDuration(52, ChronoUnit.WEEKS))
        );

        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            if (LINK_TO_DOCUMENT.equals(entry.getKey())) {
                continue;
            }
            assertEquals(entry.getValue(), actual.get(entry.getKey()));
        }
    }

    @ParameterizedTest
    @MethodSource("applicationTypes")
    void getPersonalisationForResponse_withResponse_Welsh(
            String applicationTypeKey) throws NotificationClientException {
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getGenericTseApplicationCollection().get(0).getValue().setType(applicationTypeKey);
        caseData.getTseRespondSelectApplication().setValue(SELECT_APPLICATION);
        caseData.setTseResponseText("TseResponseText");
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(WELSH_LANGUAGE);
        when(featureToggleService.isWelshEnabled()).thenReturn(true);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("CaseId");
        caseDetails.setCaseData(caseData);
        byte[] document = {};
        Map<String, Object> actual = TseHelper.getPersonalisationForResponse(
                caseDetails, document, "citizenUrlCaseId", true);

        Map<String, Object> expected = Map.of(
                "linkToCitizenHub", "citizenUrlCaseId" + WELSH_LANGUAGE_PARAM,
                "caseNumber", "1234",
                "applicationType", CY_RESPONDING_TO_APP_TYPE_MAP.get(applicationTypeKey),
                "response", "TseResponseText",
                "claimant", "First Last",
                "respondents", "Respondent Name",
                LINK_TO_DOCUMENT, NotificationClient.prepareUpload(document, true,
                        new RetentionPeriodDuration(52, ChronoUnit.WEEKS))
        );
        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            if (LINK_TO_DOCUMENT.equals(entry.getKey())) {
                continue;
            }
            assertEquals(entry.getValue(), actual.get(entry.getKey()));
        }
    }

    static Stream<String> applicationTypes() {
        return CY_RESPONDING_TO_APP_TYPE_MAP.keySet().stream();
    }

    @Test
    void getPersonalisationForResponse_withoutResponse() throws NotificationClientException {
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(SELECT_APPLICATION);
        caseData.setClaimantHearingPreference(new ClaimantHearingPreference());
        caseData.getClaimantHearingPreference().setContactLanguage(ENGLISH_LANGUAGE);

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("CaseId");
        caseDetails.setCaseData(caseData);
        byte[] document = {};
        Map<String, Object> actual = TseHelper.getPersonalisationForResponse(
                caseDetails, document, "citizenUrlCaseId", false);

        Map<String, Object> expected = Map.of(
            "linkToCitizenHub", "citizenUrlCaseId",
            "caseNumber", "1234",
            "applicationType", "Withdraw my claim",
            "response", "",
            "claimant", "First Last",
            "respondents", "Respondent Name",
                LINK_TO_DOCUMENT, NotificationClient.prepareUpload(document, true,
                        new RetentionPeriodDuration(52, ChronoUnit.WEEKS))
        );

        for (Map.Entry<String, Object> entry : expected.entrySet()) {
            if (LINK_TO_DOCUMENT.equals(entry.getKey())) {
                continue;
            }
            assertEquals(entry.getValue(), actual.get(entry.getKey()));
        }
    }

    @Nested
    class GetRespondentSelectedApplicationTypeItem {
        @Test
        void findExistingApplication() {
            caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
            caseData.getTseRespondSelectApplication().setValue(SELECT_APPLICATION);

            GenericTseApplicationType actualApplication = getRespondentSelectedApplicationType(caseData);

            assertEquals(genericTseApplicationTypeItem.getValue(), actualApplication);
        }

        @Test
        void nullWhenApplicationDoesNotExist() {
            caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
            caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("3", ""));

            assertNull(getRespondentSelectedApplicationType(caseData));
        }
    }

    @Nested
    class GetClaimantRepSelectedApplicationTypItem {
        @Test
        void findExistingApplication() {
            caseData.setClaimantRepRespondSelectApplication(TseHelper.populateClaimantRepSelectApplication(caseData));
            caseData.getClaimantRepRespondSelectApplication().setValue(SELECT_APPLICATION);

            GenericTseApplicationType actualApplication = getClaimantRepSelectedApplicationType(caseData);

            assertEquals(genericTseApplicationTypeItem.getValue(), actualApplication);
        }

        @Test
        void nullWhenApplicationDoesNotExist() {
            caseData.setClaimantRepRespondSelectApplication(TseHelper.populateClaimantRepSelectApplication(caseData));
            caseData.getClaimantRepRespondSelectApplication().setValue(DynamicValueType.create("3", ""));

            assertNull(getClaimantRepSelectedApplicationType(caseData));
        }
    }

    @Nested
    class SetRespondentApplicationState {
        @Test
        void shouldUpdateApplicationStateForValidRespondentStates() {
            TseStatusTypeItem item1 = new TseStatusTypeItem();
            TseStatusTypeItem item2 = new TseStatusTypeItem();

            TseStatusType status1 = new TseStatusType();
            TseStatusType status2 = new TseStatusType();

            item1.setValue(status1);
            item2.setValue(status2);

            List<TseStatusTypeItem> respondentStateList = List.of(item1, item2);

            GenericTseApplicationType applicationType = new GenericTseApplicationType();
            applicationType.setRespondentState(respondentStateList);

            TseHelper.setRespondentApplicationState(applicationType, UPDATED);

            assertEquals(UPDATED, status1.getApplicationState());
            assertEquals(UPDATED, status2.getApplicationState());
        }

        @Test
        void shouldDoNothingWhenRespondentStateListIsNull() {
            GenericTseApplicationType applicationType = new GenericTseApplicationType();
            TseHelper.setRespondentApplicationState(applicationType, UPDATED);
            assertNull(applicationType.getRespondentState());
        }
    }
}
