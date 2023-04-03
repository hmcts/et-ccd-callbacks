package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicFixedListType;
import uk.gov.hmcts.et.common.model.bulk.types.DynamicValueType;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationType;
import uk.gov.hmcts.et.common.model.ccd.items.GenericTseApplicationTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.TseRespondTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.TseRespondType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentTypeBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.UploadedDocumentBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.utils.TseApplicationBuilder;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.CLAIMANT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.OPEN_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.RESPONDENT_TITLE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_AMEND_RESPONSE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.TSE_APP_POSTPONE_A_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

public class TseViewApplicationHelperTest {
    private CaseData caseData;

    @Before
    public void setUp() {
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("First", "Last")
            .withEthosCaseReference("1234")
            .withClaimant("First Last")
            .withRespondent("Respondent Name", YES, "13 December 2022", false)
            .build();

        GenericTseApplicationType build = TseApplicationBuilder.builder().withApplicant(CLAIMANT_TITLE)
            .withDate("13 December 2022").withDue("20 December 2022").withType("Withdraw my claim")
            .withDetails("Text").withNumber("1").withResponsesCount("0").withStatus(OPEN_STATE).build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = new GenericTseApplicationTypeItem();
        genericTseApplicationTypeItem.setId(UUID.randomUUID().toString());
        genericTseApplicationTypeItem.setValue(build);
        caseData.setGenericTseApplicationCollection(List.of(genericTseApplicationTypeItem));
    }

    @Test
    public void populateOpenOrClosedApplications_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        caseData.setTseViewApplicationOpenOrClosed("Open");
        DynamicFixedListType actual = TseViewApplicationHelper.populateOpenOrClosedApplications(caseData);
        assertNull(actual);
    }

    @Test
    public void populateOpenApplications_withAnOpenApplication_returnsDynamicList() {
        caseData.setTseViewApplicationOpenOrClosed("Open");
        DynamicFixedListType actual = TseViewApplicationHelper.populateOpenOrClosedApplications(caseData);
        assertThat(actual.getListItems().size(), is(1));
    }

    @Test
    public void populateClosedApplications_withNoClosedApplications_returnEmptyList() {
        caseData.setTseViewApplicationOpenOrClosed("Closed");
        DynamicFixedListType actual = TseViewApplicationHelper.populateOpenOrClosedApplications(caseData);
        assertThat(actual.getListItems().size(), is(0));
    }

    @Test
    public void setDataForTseApplicationSummaryAndResponses_withEmptyList_doesNothing() {
        caseData.setGenericTseApplicationCollection(null);
        TseViewApplicationHelper.setDataForTseApplicationSummaryAndResponses(caseData);
        assertNull(caseData.getTseApplicationSummaryAndResponsesMarkup());
    }

    @Test
    public void setDataForTseApplicationSummaryAndResponses_withAnApplication_setsApplicationSummary() {

        caseData.setTseViewApplicationOpenOrClosed(OPEN_STATE);
        caseData.setTseViewApplicationSelect(TseViewApplicationHelper.populateOpenOrClosedApplications(caseData));
        caseData.getTseViewApplicationSelect().setValue(DynamicValueType.create("1", ""));
        TseViewApplicationHelper.setDataForTseApplicationSummaryAndResponses(caseData);
        String expected = "|Application | |\r\n"
                + "|--|--|\r\n"
                + "|Applicant | Claimant|\r\n"
                + "|Type of application | Withdraw my claim|\r\n"
                + "|Application date | 13 December 2022|\r\n"
                + "|What do you want to tell or ask the tribunal? | Text|\r\n"
                + "|Supporting material | N/A|\r\n"
                + "|Do you want to copy this correspondence to the other party"
                + " to satisfy the Rules of Procedure? | N/A |\r\n\r\n";
        assertThat(caseData.getTseApplicationSummaryAndResponsesMarkup(), is(expected));
    }

    @Test
    public void setDataForTseApplicationSummaryAndResponses_withAnApplicationToPostpone_setsApplicationSummary() {
        caseData.getGenericTseApplicationCollection().get(0).getValue().setType(TSE_APP_POSTPONE_A_HEARING);
        caseData.setTseViewApplicationOpenOrClosed(OPEN_STATE);
        caseData.setTseViewApplicationSelect(TseViewApplicationHelper.populateOpenOrClosedApplications(caseData));
        caseData.getTseViewApplicationSelect().setValue(DynamicValueType.create("1", ""));
        TseViewApplicationHelper.setDataForTseApplicationSummaryAndResponses(caseData);
        String expected = "|Application | |\r\n"
                + "|--|--|\r\n|Applicant | Claimant|\r\n"
                + "|Type of application | Postpone a hearing|\r\n"
                + "|Application date | 13 December 2022|\r\n"
                + "|What do you want to tell or ask the tribunal? | Text|\r\n"
                + "|Supporting material | N/A|\r\n"
                + "|Do you want to copy this correspondence to the other party"
                + " to satisfy the Rules of Procedure? | N/A |\r\n\r\n";

        assertThat(caseData.getTseApplicationSummaryAndResponsesMarkup(), is(expected));
    }

    @Test
    public void setDataForTseApplicationSummaryAndResponses_withApplicationWithDocument_setsApplicationSummary() {
        UploadedDocumentType documentType =
                UploadedDocumentBuilder.builder().withFilename("image.png").withUuid("1234").build();
        caseData.getGenericTseApplicationCollection().get(0).getValue().setDocumentUpload(documentType);

        caseData.setTseViewApplicationSelect(TseViewApplicationHelper.populateOpenOrClosedApplications(caseData));
        caseData.getTseViewApplicationSelect().setValue(DynamicValueType.create("1", ""));
        TseViewApplicationHelper.setDataForTseApplicationSummaryAndResponses(caseData);
        String expected = "|Application | |\r\n|--|--|\r\n|Applicant | Claimant|\r\n|"
                + "Type of application | Withdraw my claim|\r\n|Application date | 13 December 2022|\r\n"
                + "|What do you want to tell or ask the tribunal? | Text|\r\n"
                + "|Supporting material | <a href=\"/documents/1234/binary\" target=\"_blank\">image.png</a>|\r\n"
                + "|Do you want to copy this correspondence to the other party "
                + "to satisfy the Rules of Procedure? | N/A |\r\n\r\n";

        assertThat(caseData.getTseApplicationSummaryAndResponsesMarkup(), is(expected));
    }

    @Test
    public void setData_withAmendResponseApplication_withAReponse_setsMarkup() {
        TseRespondType tseRespondType = TseRespondType.builder()
            .from(CLAIMANT_TITLE)
            .date("23 December 2022")
            .response("Response Details")
            .hasSupportingMaterial(YES)
            .supportingMaterial(List.of(
                createDocumentTypeItem("image.png"),
                createDocumentTypeItem("Form.pdf")))
            .copyToOtherParty(YES)
            .build();

        TseRespondTypeItem tseRespondTypeItem = TseRespondTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(tseRespondType)
            .build();

        GenericTseApplicationType genericTseApplicationType = TseApplicationBuilder.builder()
            .withNumber("1")
            .withType(TSE_APP_AMEND_RESPONSE)
            .withApplicant(RESPONDENT_TITLE)
            .withDate("13 December 2022")
            .withDocumentUpload(createUploadedDocumentType("document.txt"))
            .withDetails("Details Text")
            .withCopyToOtherPartyYesOrNo(YES)
            .withStatus(OPEN_STATE)
            .withRespondCollection(List.of(tseRespondTypeItem))
            .build();

        GenericTseApplicationTypeItem genericTseApplicationTypeItem = GenericTseApplicationTypeItem.builder()
            .id(UUID.randomUUID().toString())
            .value(genericTseApplicationType)
            .build();

        caseData.setGenericTseApplicationCollection(
            List.of(genericTseApplicationTypeItem)
        );

        caseData.setTseViewApplicationSelect(
            DynamicFixedListType.of(DynamicValueType.create("1", "1 - Amend response")));

        TseViewApplicationHelper.setDataForTseApplicationSummaryAndResponses(caseData);

        String fileDisplay1 = "<a href=\"/documents/1234/binary\" target=\"_blank\">document.txt</a>";
        String fileDisplay2 = "<a href=\"/documents/1234/binary\" target=\"_blank\">image.png</a>";
        String fileDisplay3 = "<a href=\"/documents/1234/binary\" target=\"_blank\">Form.pdf</a>";

        String expected = "|Application | |\r\n"
            + "|--|--|\r\n"
            + "|Applicant | Respondent|\r\n"
            + "|Type of application | Amend response|\r\n"
            + "|Application date | 13 December 2022|\r\n"
            + "|What do you want to tell or ask the tribunal? | Details Text|\r\n"
            + "|Supporting material | " + fileDisplay1 + "|\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? "
            + "| Yes |\r\n\r\n"
            + "|Responses | |\r\n|--|--|\r\n\r\n"
            + "|Response 1 | |\r\n"
            + "|--|--|\r\n"
            + "|Response from | Claimant|\r\n"
            + "|Response date | 23 December 2022|\r\n"
            + "|What’s your response to the respondent’s application? | Response Details|\r\n"
            + "|Supporting material | " + fileDisplay2 + "<br>" + fileDisplay3 + "<br>" + "|\r\n"
            + "|Do you want to copy this correspondence to the other party to satisfy the Rules of Procedure? "
            + "| Yes|\r\n"
            + "\r\n";
        
        assertThat(caseData.getTseApplicationSummaryAndResponsesMarkup(), is(expected));

    }

    private UploadedDocumentType createUploadedDocumentType(String fileName) {
        return UploadedDocumentBuilder.builder()
                .withFilename(fileName)
                .withUuid("1234")
                .build();
    }

    private DocumentTypeItem createDocumentTypeItem(String fileName) {
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setId("1234");
        documentTypeItem.setValue(DocumentTypeBuilder.builder().withUploadedDocument(fileName, "1234").build());
        return documentTypeItem;
    }
}
