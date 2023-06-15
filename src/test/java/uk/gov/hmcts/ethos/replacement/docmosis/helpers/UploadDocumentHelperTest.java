package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.NotificationServiceConstants.CASE_NUMBER;

public class UploadDocumentHelperTest {
    CCDRequest ccdRequest;
    CaseDetails caseDetails;
    CaseData caseData;

    @BeforeEach
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

        caseDetails = ccdRequest.getCaseDetails();
    }

    @Test
    void shouldSendRejectionEmail_givenEmptyDocumentCollection_returnsFalse() {
        assertFalse(UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails()));
    }

    @Test
    void shouldSendRejectionEmail_noRejectionDocumentPresent_returnsFalse() {
        attachDocumentToCollection(caseData, "Not a Rejection of claim");
        assertFalse(UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails()));
    }

    @Test
    void shouldSendRejectionEmail_givenRejectionEmailFlagPresent_returnsFalse() {
        attachDocumentToCollection(caseData, "Not a Rejection of claim");
        caseData.setCaseRejectedEmailSent(YES);
        assertFalse(UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails()));
    }

    @Test
    void shouldSendRejectionEmail_givenCaseIsRejectedWithRejectionDocumentAndEmailFlag_returnsFalse() {
        attachDocumentToCollection(caseData, "Rejection of claim");
        ccdRequest.getCaseDetails().setState("Rejected");
        caseData.setCaseRejectedEmailSent(YES);
        assertFalse(UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails()));
    }

    @Test
    void shouldSendRejectionEmail_givenCaseIsRejectedWithRejectionDocumentAndNoEmailFlag_returnsTrue() {
        attachDocumentToCollection(caseData, "Rejection of claim");
        ccdRequest.getCaseDetails().setState("Rejected");
        assertTrue(UploadDocumentHelper.shouldSendRejectionEmail(ccdRequest.getCaseDetails()));
    }

    @Test
    void buildPersonalisationForCaseRejection_givenNoClaimantTitle_returnsWithInitialAndLastName() {
        Map<String, String> expected = buildPersonalisation("F");
        Map<String, String> actual = UploadDocumentHelper.buildPersonalisationForCaseRejection(caseDetails);

        assertThat(actual, is(expected));
    }

    @Test
    void buildPersonalisationForCaseRejection_givenClaimantTitle_returnsWithTitleLastName() {
        caseData.getClaimantIndType().setClaimantTitle("Mr");
        Map<String, String> expected = buildPersonalisation("Mr");
        Map<String, String> actual = UploadDocumentHelper.buildPersonalisationForCaseRejection(caseDetails);

        assertThat(actual, is(expected));
    }

    @Test
    void buildPersonalisationForCaseRejection_givenClaimantPreferredTitle_returnsWithTitleLastName() {
        caseData.getClaimantIndType().setClaimantPreferredTitle("Professor");
        Map<String, String> expected = buildPersonalisation("Professor");
        Map<String, String> actual = UploadDocumentHelper.buildPersonalisationForCaseRejection(caseDetails);

        assertThat(actual, is(expected));
    }

    private Map<String, String> buildPersonalisation(String initialTitle) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, "1234");
        personalisation.put("initialTitle", initialTitle);
        personalisation.put("lastName", "Last");
        personalisation.put("ccdId", "1234");
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
