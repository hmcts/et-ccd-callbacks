package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;

public class UploadDocumentHelperTest {
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
            .withState(ACCEPTED_STATE)
            .withCaseId("1234")
            .withCaseData(caseData)
            .build();
    }

    @Test
    public void shouldSendRejectionEmail_givenEmptyDocumentCollection_returnsFalse() {
        assertFalse(UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails()));
    }

    @Test
    public void shouldSendRejectionEmail_noRejectionDocumentPresent_returnsFalse() {
        attachDocumentToCollection(caseData, "Not a Rejection of claim");
        assertFalse(UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails()));
    }

    @Test
    public void shouldSendRejectionEmail_givenRejectionEmailFlagPresent_returnsFalse() {
        attachDocumentToCollection(caseData, "Not a Rejection of claim");
        caseData.setCaseRejectedEmailSent(YES);
        assertFalse(UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails()));
    }

    @Test
    public void shouldSendRejectionEmail_givenCaseIsRejectedWithRejectionDocumentAndEmailFlag_returnsFalse() {
        attachDocumentToCollection(caseData, "Rejection of claim");
        ccdRequest.getCaseDetails().setState("Rejected");
        caseData.setCaseRejectedEmailSent(YES);
        assertFalse(UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails()));
    }

    @Test
    public void shouldSendRejectionEmail_givenCaseIsRejectedWithRejectionDocumentAndNoEmailFlag_returnsTrue() {
        attachDocumentToCollection(caseData, "Rejection of claim");
        ccdRequest.getCaseDetails().setState("Rejected");
        assertTrue(UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails()));
    }

    @Test
    public void buildPersonalisationForCaseRejection_givenNoClaimantTitle_returnsWithInitialAndLastName() {
        var expected = buildPersonalisation("1234", "F", "Last");
        var actual = UploadDocumentHelper.buildPersonalisationForCaseRejection(caseData);

        assertThat(actual, is(expected));
    }

    @Test
    public void buildPersonalisationForCaseRejection_givenClaimantTitle_returnsWithTitleLastName() {
        caseData.getClaimantIndType().setClaimantTitle("Mr");
        var expected = buildPersonalisation("1234", "Mr", "Last");
        var actual = UploadDocumentHelper.buildPersonalisationForCaseRejection(caseData);

        assertThat(actual, is(expected));
    }

    @Test
    public void buildPersonalisationForCaseRejection_givenClaimantPreferredTitle_returnsWithTitleLastName() {
        caseData.getClaimantIndType().setClaimantPreferredTitle("Professor");
        var expected = buildPersonalisation("1234", "Professor", "Last");
        var actual = UploadDocumentHelper.buildPersonalisationForCaseRejection(caseData);

        assertThat(actual, is(expected));
    }

    private Map<String, String> buildPersonalisation(String caseNumber, String initialTitle, String lastName) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put("caseNumber", caseNumber);
        personalisation.put("initialTitle", initialTitle);
        personalisation.put("lastName", lastName);
        return personalisation;
    }

    public static void attachDocumentToCollection(CaseData caseData, String typeOfDocument) {
        DocumentTypeItem docItem = new DocumentTypeItem();
        DocumentType doc = new DocumentType();
        doc.setTypeOfDocument(typeOfDocument);
        docItem.setValue(doc);
        caseData.setDocumentCollection(List.of(docItem));
    }
}
