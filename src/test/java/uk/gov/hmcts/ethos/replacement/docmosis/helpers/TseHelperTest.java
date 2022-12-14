package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

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
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondentReplyTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondentReplyType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.TseApplicationBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

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
        String expected = "The respondent has applied to <b>Withdraw my claim</b>.</br>You do not need to respond to " +
            "this application.<br></br> If you have any objections or responses to their application you must send " +
            "them to the tribunal as soon as possible and by 20 December 2022 at the latest.</br></br>If you need " +
            "more time to respond, you may request more time from the tribunal. If you do not respond or request more" +
            " time to respond, the tribunal will consider the application without your response.";

        assertThat(caseData.getTseResponseIntro(), is(expected));
    }

    @Test
    public void setDataForRespondingToApplication_withAGroupAApplication_restoresData() {
        caseData.getGenericTseApplicationCollection().get(0).getValue().setType("Postpone a hearing");
        caseData.setTseRespondSelectApplication(TseHelper.populateSelectApplicationDropdown(caseData));
        caseData.getTseRespondSelectApplication().setValue(DynamicValueType.create("1", ""));
        TseHelper.setDataForRespondingToApplication(caseData);
        String expected = "The respondent has applied to <b>Postpone a hearing</b>.</br></br> If you have any " +
            "objections or responses to their application you must send them to the tribunal as soon as possible and " +
            "by 20 December 2022 at the latest.</br></br>If you need more time to respond, you may request more time " +
            "from the tribunal. If you do not respond or request more time to respond, the tribunal will consider the" +
            " application without your response.";

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
        String expected = "| | |\r\n" + "|--|--|\r\n" + "|Application date | 13 December 2022\r\n" + "|Details of " +
            "the application | Text\r\n" + "Application file upload | <a href=\"/documents/1234/binary\" " +
            "target=\"_blank\">image.png</a>";

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

        TseRespondentReplyType replyType =
            caseData.getGenericTseApplicationCollection().get(0).getValue().getRespondentReply().get(0).getValue();

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
    }

    private List<DocumentTypeItem> createSupportingMaterial() {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId(UUID.randomUUID().toString());
        documentTypeItem.setValue(DocumentTypeBuilder.builder().withUploadedDocument("image.png", "1234").build());
        return List.of(documentTypeItem);
    }
}
