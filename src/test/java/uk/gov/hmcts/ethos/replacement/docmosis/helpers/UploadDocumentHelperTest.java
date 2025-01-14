package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.utils.CCDRequestBuilder;
import uk.gov.hmcts.ethos.utils.CaseDataBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ACCEPTED_STATE;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.YES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ACAS_CERTIFICATE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CLAIM_ACCEPTED;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.CLAIM_REJECTED;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET1_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.ET3_ATTACHMENT;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.HEARINGS;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.INITIAL_CONSIDERATION;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.LEGACY_DOCUMENT_NAMES;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.MISC;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.NOTICE_OF_A_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.NOTICE_OF_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.NOTICE_OF_HEARING;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.OTHER;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.REJECTION_OF_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RESPONSE_TO_A_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RULE_27_NOTICE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.RULE_28_NOTICE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.STARTING_A_CLAIM;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CORRESPONDENCE;
import static uk.gov.hmcts.ethos.replacement.docmosis.constants.InitialConsiderationConstants.RULE_29_NOTICE;
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
        Map<String, String> actual = UploadDocumentHelper.buildPersonalisationForCaseRejection(
                caseDetails.getCaseData(),
                "link"
        );

        assertThat(actual, is(expected));
    }

    @Test
    void buildPersonalisationForCaseRejection_givenClaimantTitle_returnsWithTitleLastName() {
        caseData.getClaimantIndType().setClaimantTitle("Mr");
        Map<String, String> expected = buildPersonalisation("Mr");
        Map<String, String> actual = UploadDocumentHelper.buildPersonalisationForCaseRejection(
                caseDetails.getCaseData(),
                "link"
        );

        assertThat(actual, is(expected));
    }

    @Test
    void buildPersonalisationForCaseRejection_givenClaimantPreferredTitle_returnsWithTitleLastName() {
        caseData.getClaimantIndType().setClaimantPreferredTitle("Professor");
        Map<String, String> expected = buildPersonalisation("Professor");
        Map<String, String> actual = UploadDocumentHelper.buildPersonalisationForCaseRejection(
                caseDetails.getCaseData(),
                "link"
        );

        assertThat(actual, is(expected));
    }

    @ParameterizedTest
    @MethodSource("convertLegacyDocsToNewDocNaming")
    void convertLegacyDocsToNewDocNaming(String docType, String topLevel) {
        caseData = new CaseDataBuilder()
                .withDocumentCollection(docType)
                .build();
        UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseData);
        assertNotNull(caseData.getDocumentCollection());
        assertEquals(topLevel, caseData.getDocumentCollection().get(0).getValue().getTopLevelDocuments());
    }

    private static Stream<Arguments> convertLegacyDocsToNewDocNaming() {
        return Stream.of(
                Arguments.of(ET1, STARTING_A_CLAIM),
                Arguments.of(ET1_ATTACHMENT, STARTING_A_CLAIM),
                Arguments.of(ACAS_CERTIFICATE, STARTING_A_CLAIM),
                Arguments.of(NOTICE_OF_A_CLAIM, STARTING_A_CLAIM),
                Arguments.of(TRIBUNAL_CORRESPONDENCE, STARTING_A_CLAIM),
                Arguments.of(REJECTION_OF_CLAIM, STARTING_A_CLAIM),
                Arguments.of(ET3, RESPONSE_TO_A_CLAIM),
                Arguments.of(ET3_ATTACHMENT, RESPONSE_TO_A_CLAIM),
                Arguments.of(NOTICE_OF_HEARING, HEARINGS),
                Arguments.of(OTHER, LEGACY_DOCUMENT_NAMES),
                Arguments.of(TRIBUNAL_CASE_FILE, MISC)
        );
    }

    @ParameterizedTest
    @MethodSource("setDocumentTypeForDocumentCollection")
    void setDocumentTypeForDocumentCollection(String typeOfDocument, String documentType) {
        caseData = new CaseDataBuilder()
                .withDocumentCollection(typeOfDocument)
                .build();
        UploadDocumentHelper.convertLegacyDocsToNewDocNaming(caseData);
        UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseData);
        assertNotNull(caseData.getDocumentCollection());
        assertEquals(documentType, caseData.getDocumentCollection().get(0).getValue().getDocumentType());
        assertEquals("1", caseData.getDocumentCollection().get(0).getValue().getDocNumber());
    }

    private static Stream<Arguments> setDocumentTypeForDocumentCollection() {
        return Stream.of(
                Arguments.of(ET1, ET1),
                Arguments.of(ET1_ATTACHMENT, ET1_ATTACHMENT),
                Arguments.of(ACAS_CERTIFICATE, ACAS_CERTIFICATE),
                Arguments.of(NOTICE_OF_A_CLAIM, NOTICE_OF_CLAIM),
                Arguments.of(TRIBUNAL_CORRESPONDENCE, CLAIM_ACCEPTED),
                Arguments.of(REJECTION_OF_CLAIM, CLAIM_REJECTED),
                Arguments.of(ET3, ET3),
                Arguments.of(ET3_ATTACHMENT, ET3_ATTACHMENT),
                Arguments.of(NOTICE_OF_HEARING, NOTICE_OF_HEARING),
                Arguments.of(OTHER, OTHER)

        );
    }

    @Test
    void initialConsiderationRuleChangeDocType() {
        caseData = new CaseDataBuilder()
                .withDocumentCollection("Doc 1")
                .withDocumentCollection("Doc 2")
                .build();
        caseData.getDocumentCollection().get(0).getValue().setTopLevelDocuments(INITIAL_CONSIDERATION);
        caseData.getDocumentCollection().get(0).getValue().setInitialConsiderationDocuments(RULE_27_NOTICE);
        caseData.getDocumentCollection().get(1).getValue().setTopLevelDocuments(INITIAL_CONSIDERATION);
        caseData.getDocumentCollection().get(1).getValue().setInitialConsiderationDocuments(RULE_28_NOTICE);

        UploadDocumentHelper.setDocumentTypeForDocumentCollection(caseData);
        assertEquals(RULE_28_NOTICE, caseData.getDocumentCollection().get(0).getValue().getDocumentType());
        assertEquals(RULE_29_NOTICE, caseData.getDocumentCollection().get(1).getValue().getDocumentType());
    }

    private Map<String, String> buildPersonalisation(String initialTitle) {
        Map<String, String> personalisation = new ConcurrentHashMap<>();
        personalisation.put(CASE_NUMBER, "1234");
        personalisation.put("initialTitle", initialTitle);
        personalisation.put("lastName", "Last");
        personalisation.put("linkToCitizenHub", "link");
        return personalisation;
    }

    public static void attachDocumentToCollection(CaseData caseData, String typeOfDocument) {
        DocumentTypeItem docItem = new DocumentTypeItem();
        DocumentType doc = new DocumentType();
        doc.setUploadedDocument(UploadedDocumentType.builder()
                .documentUrl("http://localhost:8080/documents/1234")
                .documentBinaryUrl("http://localhost:8080/documents/1234/binary")
                .documentFilename("test.pdf")
                .build());
        doc.setTypeOfDocument(typeOfDocument);
        docItem.setValue(doc);
        caseData.setDocumentCollection(List.of(docItem));
    }
}
