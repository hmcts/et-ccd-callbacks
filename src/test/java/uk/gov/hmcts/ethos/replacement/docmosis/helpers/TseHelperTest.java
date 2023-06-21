package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_POSTPONE_A_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@ExtendWith(SpringExtension.class)
class TseHelperTest {
    private CaseData caseData;

    @BeforeEach
    public void setUp() {
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("First", "Last")
            .withEthosCaseReference("1234")
            .withClaimant("First Last")
            .withRespondent("Respondent Name", YES, "13 December 2022", false)
            .build();

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_TITLE)
            .withDate("13 December 2022").withDue("20 December 2022").withType("Withdraw my claim")
            .withCopyToOtherPartyYesOrNo(YES).withDetails("Text").withNumber("1")
            .withResponsesCount("0").withStatus(OPEN_STATE).build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
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
        assertThat(actual.getListItems().size(), is(1));
    }

    @ParameterizedTest
    @MethodSource
    void populateSelectApplicationDropdown_hasTribunalResponse(String respondentRespondRequired,
                                                               int numberOfApplication) {
        GenericTseApplicationTypeItem genericTseApplicationTypeItem = getGenericTseApplicationTypeItem(
                respondentRespondRequired);
        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));

        DynamicFixedListType actual = TseHelper.populateRespondentSelectApplication(caseData);
        assertThat(actual.getListItems().size(), is(numberOfApplication));
    }

    private static Stream<Arguments> populateSelectApplicationDropdown_hasTribunalResponse() {
        return Stream.of(
                Arguments.of(YES, 0),
                Arguments.of(null, 0),
                Arguments.of(NO, 1)
        );
    }

    @Test
    void populateSelectApplicationDropdown_withAnApplication_returnsEmpty() {
        CaseData caseData1 = CaseDataBuilder.builder()
                .withClaimantIndType("First", "Last")
                .withEthosCaseReference("1234")
                .withClaimant("First Last")
                .withRespondent("Respondent Name", YES, "13 December 2022", false)
                .build();

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_TITLE)
                .withDate("13 December 2022").withDue("20 December 2022").withType("Order a witness to attend")
                .withDetails("Text").withNumber("1")
                .withResponsesCount("0").withStatus(OPEN_STATE).build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        caseData1.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));

        DynamicFixedListType actual = TseHelper.populateRespondentSelectApplication(caseData1);
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
        assertThat(actual.getListItems().size(), is(0));
    }

    @Test
    void setDataForRespondingToApplication_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        TseHelper.setDataForRespondingToApplication(caseData);
        assertNull(caseData.getTseResponseIntro());
    }

    @Test
    void setDataForRespondingToApplication_withAGroupBApplication_restoresData() {
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));
        TseHelper.setDataForRespondingToApplication(caseData);
        String expected = "The respondent has applied to <b>Withdraw my claim</b>.</br>You do not need to respond to "
            + "this application.<br></br> If you have any objections or responses to their application you must send "
            + "them to the tribunal as soon as possible and by 20 December 2022 at the latest.</br></br>If you need "
            + "more time to respond, you may request more time from the tribunal. If you do not respond or request more"
            + " time to respond, the tribunal will consider the application without your response.";

        assertThat(caseData.getTseResponseIntro(), is(expected));
    }

    @Test
    void setDataForRespondingToApplication_withAGroupAApplication_restoresData() {
        caseData.getGenericTseApplicationCollection().get(0).getValue().setType(TSE_APP_POSTPONE_A_HEARING);
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));
        TseHelper.setDataForRespondingToApplication(caseData);
        String expected = "The respondent has applied to <b>Postpone a hearing</b>.</br></br> If you have any "
            + "objections or responses to their application you must send them to the tribunal as soon as possible and "
            + "by 20 December 2022 at the latest.</br></br>If you need more time to respond, you may request more time "
            + "from the tribunal. If you do not respond or request more time to respond, the tribunal will consider the"
            + " application without your response.";

        assertThat(caseData.getTseResponseIntro(), is(expected));
    }

    @Test
    void setDataForRespondingToApplication_withApplicationWithDocument_restoresData() {
        UploadedDocumentType documentType =
            UploadedDocumentBuilder.builder().withFilename("image.png").withUuid("1234").build();
        caseData.getGenericTseApplicationCollection().get(0).getValue().setDocumentUpload(documentType);
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));
        TseHelper.setDataForRespondingToApplication(caseData);
        String expected = "| | |\r\n" + "|--|--|\r\n" + "|Application date | 13 December 2022\r\n" + "|Details of "
            + "the application | Text\r\n" + "Application file upload | <a href=\"/documents/1234/binary\" "
            + "target=\"_blank\">image.png</a>";

        assertThat(caseData.getTseResponseTable(), is(expected));
    }

    @Test
    void saveReplyToApplication_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        TseHelper.saveReplyToApplication(caseData, false);
        assertNull(caseData.getGenericTseApplicationCollection());
    }

    @Test
    void saveReplyToApplication_withApplication_savesReply() {
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));

        caseData.setTseResponseText("ResponseText");
        caseData.setTseResponseSupportingMaterial(createSupportingMaterial());

        caseData.setTseResponseHasSupportingMaterial(YES);
        caseData.setTseResponseCopyToOtherParty(NO);
        caseData.setTseResponseCopyNoGiveDetails("It's a secret");

        TseHelper.saveReplyToApplication(caseData, false);

        TseRespondType replyType =
            caseData.getGenericTseApplicationCollection().get(0).getValue().getRespondCollection().get(0).getValue();

        String dateNow = UtilHelper.formatCurrentDate(LocalDate.now());

        assertThat(replyType.getDate(), is(dateNow));
        assertThat(replyType.getResponse(), is("ResponseText"));
        assertThat(replyType.getCopyNoGiveDetails(), is("It's a secret"));
        assertThat(replyType.getHasSupportingMaterial(), is(YES));
        assertThat(replyType.getCopyToOtherParty(), is(NO));
        assertThat(replyType.getFrom(), is(RESPONDENT_TITLE));
        assertThat(replyType.getSupportingMaterial().get(0).getValue().getUploadedDocument().getDocumentFilename(),
            is("image.png"));
    }

    @Test
    void saveReplyToApplication_withTribunalResponse_setRespondentResponded() {

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = getGenericTseApplicationTypeItem(NO);
        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));

        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));

        caseData.setTseResponseText("ResponseText");
        caseData.setTseResponseSupportingMaterial(createSupportingMaterial());

        caseData.setTseResponseHasSupportingMaterial(YES);
        caseData.setTseResponseCopyToOtherParty(NO);
        caseData.setTseResponseCopyNoGiveDetails("It's a secret");

        TseHelper.saveReplyToApplication(caseData, true);

        String respondentRespondRequired = caseData.getGenericTseApplicationCollection().get(0)
                .getValue().getRespondentResponseRequired();

        assertThat(respondentRespondRequired, is(YES));
    }

    @Test
    void resetReplyToApplicationPage_resetsData() {
        caseData.setTseResponseCopyToOtherParty(YES);
        caseData.setTseResponseCopyNoGiveDetails(YES);
        caseData.setTseResponseText(YES);
        caseData.setTseResponseIntro(YES);
        caseData.setTseResponseTable(YES);
        caseData.setTseResponseHasSupportingMaterial(YES);
        caseData.setTseResponseSupportingMaterial(createSupportingMaterial());
        TseHelper.resetReplyToApplicationPage(caseData);

        assertNull(caseData.getTseResponseText());
        assertNull(caseData.getTseResponseIntro());
        assertNull(caseData.getTseResponseTable());
        assertNull(caseData.getTseResponseHasSupportingMaterial());
        assertNull(caseData.getTseResponseSupportingMaterial());
        assertNull(caseData.getTseResponseCopyToOtherParty());
        assertNull(caseData.getTseResponseCopyNoGiveDetails());
    }

    @Test
    void getReplyDocumentRequest_generatesData() throws JsonProcessingException {
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));

        caseData.getGenericTseApplicationCollection().get(0).getValue()
            .setRespondCollection(List.of(
                TseRespondTypeItem.builder()
                    .id("c0bae193-ded6-4db8-a64d-b260847bcc9b")
                    .value(
                        TseRespondType.builder()
                            .from(CLAIMANT_TITLE)
                            .date("16-May-1996")
                            .response("response")
                            .hasSupportingMaterial(YES)
                            .supportingMaterial(createSupportingMaterial())
                            .copyToOtherParty(YES)
                            .build()
                    ).build()));

        String replyDocumentRequest = TseHelper.getReplyDocumentRequest(caseData, "");
        String expected = "{\"accessKey\":\"\",\"templateName\":\"EM-TRB-EGW-ENG-01212.docx\","
            + "\"outputName\":\"Withdraw my claim Reply.pdf\",\"data\":{\"caseNumber\":\"1234\","
            + "\"type\":\"Withdraw my claim\",\"supportingYesNo\":\"Yes\","
            + "\"documentCollection\":[{\"id\":\"1234\","
            + "\"value\":{\"typeOfDocument\":null,"
            + "\"uploadedDocument\":{\"document_binary_url\":\"http://dm-store:8080/documents/1234/binary"
            + "\",\"document_filename\":\"image.png\","
            + "\"document_url\":\"http://dm-store:8080/documents/1234\"},\"ownerDocument\":null,"
            + "\"creationDate\":null,\"shortDescription\":null}}],\"copy\":\"Yes\","
            + "\"response\":\"response\"}}";

        assertThat(replyDocumentRequest, is(expected));
    }

    private List<GenericTypeItem<DocumentType>> createSupportingMaterial() {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId("1234");
        documentTypeItem.setValue(DocumentTypeBuilder.builder().withUploadedDocument("image.png", "1234").build());
        return List.of(documentTypeItem);
    }

    @Test
    void getPersonalisationForResponse_withResponse() throws NotificationClientException {
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));
        caseData.setTseResponseText("TseResponseText");

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("CaseId");
        caseDetails.setCaseData(caseData);
        byte[] document = {};
        Map<String, Object> actual = TseHelper.getPersonalisationForResponse(caseDetails, document, "citizenUrl");

        Map<String, Object> expected = Map.of(
                "linkToCitizenHub", "citizenUrlCaseId",
            "caseNumber", "1234",
            "applicationType", "Withdraw my claim",
            "response", "TseResponseText",
            "claimant", "First Last",
            "respondents", "Respondent Name",
            "linkToDocument", NotificationClient.prepareUpload(document, false, true, "52 weeks")
        );

        assertThat(actual.toString(), is(expected.toString()));
    }

    @Test
    void getPersonalisationForResponse_withoutResponse() throws NotificationClientException {
        caseData.setTseRespondSelectApplication(TseHelper.populateRespondentSelectApplication(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));

        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setCaseId("CaseId");
        caseDetails.setCaseData(caseData);
        byte[] document = {};
        Map<String, Object> actual = TseHelper.getPersonalisationForResponse(caseDetails, document, "citizenUrl");

        Map<String, Object> expected = Map.of(
                "linkToCitizenHub", "citizenUrlCaseId",
            "caseNumber", "1234",
            "applicationType", "Withdraw my claim",
            "response", "",
            "claimant", "First Last",
            "respondents", "Respondent Name",
            "linkToDocument", NotificationClient.prepareUpload(document, false, true, "52 weeks")
        );

        assertThat(actual.toString(), is(expected.toString()));
    }

    @NotNull
    private static GenericTseApplicationTypeItem getGenericTseApplicationTypeItem(String respondentRespondRequired) {
        GenericTseApplicationType build = GenericTseApplicationType.builder()
                .applicant(CLAIMANT_TITLE)
                .date("13 December 2022").dueDate("20 December 2022").type("Withdraw my claim")
                .details("Text").number("1")
                .responsesCount("0").status(OPEN_STATE)
                .respondentResponseRequired(respondentRespondRequired).build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        return genericTseApplicationTypeItem;
    }

}
