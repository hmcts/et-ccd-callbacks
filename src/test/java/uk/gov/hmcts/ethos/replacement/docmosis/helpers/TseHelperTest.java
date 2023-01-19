package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ecm.common.helpers.UtilHelper;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TseApplicationBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

@SuppressWarnings({"PMD.LinguisticNaming"})
public class TseHelperTest {
    CCDRequest ccdRequest;
    CaseData caseData;

    @Before
    public void setUp() {
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("First", "Last")
            .withEthosCaseReference("1234")
            .build();

        caseData.setClaimant("First Last");

        ccdRequest = CCDRequestBuilder.builder()
            .withState("Accepted")
            .withCaseId("1234")
            .withCaseData(caseData)
            .build();

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant("Claimant")
            .withDate("13 December 2022").withDue("20 December 2022").withType("Withdraw my claim")
            .withDetails("Text").withNumber("1").withResponsesCount("0").withStatus("Open").build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));
    }

    @Test
    public void populateSelectApplicationDropdown_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        DynamicFixedListType actual = TseHelper.populateSelectApplicationDropdown(caseData);
        assertNull(actual);
    }

    @Test
    public void populateSelectApplicationDropdown_withAnApplication_returnsDynamicList() {
        DynamicFixedListType actual = TseHelper.populateSelectApplicationDropdown(caseData);
        assertThat(actual.getListItems().size(), is(1));
    }

    @Test
    public void setDataForRespondingToApplication_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        TseHelper.setDataForRespondingToApplication(caseData);
        assertNull(caseData.getTseResponseIntro());
    }

    @Test
    public void setDataForRespondingToApplication_withAGroupBApplication_restoresData() {
        caseData.setTseRespondSelectApplication(TseHelper.populateSelectApplicationDropdown(caseData));
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
    public void setDataForRespondingToApplication_withAGroupAApplication_restoresData() {
        caseData.getGenericTseApplicationCollection().get(0).getValue().setType("Postpone a hearing");
        caseData.setTseRespondSelectApplication(TseHelper.populateSelectApplicationDropdown(caseData));
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
    public void setDataForRespondingToApplication_withApplicationWithDocument_restoresData() {
        UploadedDocumentType documentType =
            UploadedDocumentBuilder.builder().withFilename("image.png").withUuid("1234").build();
        caseData.getGenericTseApplicationCollection().get(0).getValue().setDocumentUpload(documentType);
        caseData.setTseRespondSelectApplication(TseHelper.populateSelectApplicationDropdown(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));
        TseHelper.setDataForRespondingToApplication(caseData);
        String expected = "| | |\r\n" + "|--|--|\r\n" + "|Application date | 13 December 2022\r\n" + "|Details of "
            + "the application | Text\r\n" + "Application file upload | <a href=\"/documents/1234/binary\" "
            + "target=\"_blank\">image.png</a>";

        assertThat(caseData.getTseResponseTable(), is(expected));
    }

    @Test
    public void saveReplyToApplication_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        TseHelper.saveReplyToApplication(caseData);
        assertNull(caseData.getGenericTseApplicationCollection());
    }

    @Test
    public void saveReplyToApplication_withApplication_savesReply() {
        caseData.setTseRespondSelectApplication(TseHelper.populateSelectApplicationDropdown(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));

        caseData.setTseResponseText("ResponseText");
        caseData.setTseResponseSupportingMaterial(createSupportingMaterial());

        caseData.setTseResponseHasSupportingMaterial(YES);
        caseData.setTseResponseCopyToOtherParty(NO);
        caseData.setTseResponseCopyNoGiveDetails("It's a secret");

        TseHelper.saveReplyToApplication(caseData);

        TseRespondType replyType =
            caseData.getGenericTseApplicationCollection().get(0).getValue().getRespondCollection().get(0).getValue();

        String dateNow = UtilHelper.formatCurrentDate(LocalDate.now());

        assertThat(replyType.getDate(), is(dateNow));
        assertThat(replyType.getResponse(), is("ResponseText"));
        assertThat(replyType.getCopyNoGiveDetails(), is("It's a secret"));
        assertThat(replyType.getHasSupportingMaterial(), is(YES));
        assertThat(replyType.getCopyToOtherParty(), is(NO));
        assertThat(replyType.getFrom(), is("Respondent"));
        assertThat(replyType.getSupportingMaterial().get(0).getValue().getUploadedDocument().getDocumentFilename(),
            is("image.png"));
    }

    @Test
    public void resetReplyToApplicationPage_resetsData() {
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
    public void getReplyDocumentRequest_generatesData() throws JsonProcessingException {
        caseData.setTseRespondSelectApplication(TseHelper.populateSelectApplicationDropdown(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));

        caseData.getGenericTseApplicationCollection().get(0).getValue()
            .setRespondCollection(List.of(
                TseRespondTypeItem.builder()
                    .id("c0bae193-ded6-4db8-a64d-b260847bcc9b")
                    .value(
                        TseRespondType.builder()
                            .from("Claimant")
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

    private List<DocumentTypeItem> createSupportingMaterial() {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId("1234");
        documentTypeItem.setValue(DocumentTypeBuilder.builder().withUploadedDocument("image.png", "1234").build());
        return List.of(documentTypeItem);
    }
}
