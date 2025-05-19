package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.shaded.org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentUtil;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.*;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.pdf.et3.ET3FormTestConstants.TEST_ET3_FORM_CASE_DATA_FILE;

public class ET3DocumentHelperTest {

    private static final String RESPONSE_STATUS_ACCEPTED = "Accepted";
    private static final String RESPONSE_STATUS_REJECTED = "Rejected";
    private static final String ENGLISH_ET3_FORM_NAME = "Test Company - ET3 Response.pdf";
    private static final String WELSH_ET3_FORM_NAME = "Test Company - ET3 Response - Welsh.pdf";

    @Test
    void theHasET3Document() {
        assertThat(ET3DocumentHelper.hasET3Document(null)).isFalse();

        RespondentSumTypeItem respondentSumTypeItemNullValue = new RespondentSumTypeItem();
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItemNullValue)).isFalse();

        RespondentSumTypeItem respondentSumTypeItem = ResourceLoader.fromString(
                TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class).getRespondentCollection().get(0);
        // All ET3 Documents Exists
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // Only ET3 English Form is Removed
        respondentSumTypeItem.getValue().setEt3Form(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // ET3 English and Welsh Forms are Removed
        respondentSumTypeItem.getValue().setEt3FormWelsh(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // ET3 English and Welsh Forms and Contest Claim Documents are Removed
        respondentSumTypeItem.getValue().setEt3ResponseContestClaimDocument(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // ET3 English and Welsh Forms, Contest Claim and Response Respondent Support Documents are Removed
        respondentSumTypeItem.getValue().setEt3ResponseRespondentSupportDocument(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // ET3 English and Welsh Forms, Contest Claim and Response Respondent Support Documents are Removed
        respondentSumTypeItem.getValue().setEt3ResponseRespondentSupportDocument(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isTrue();

        // ET3 English and Welsh Forms, Contest Claim, Response Respondent Support and Response Employer Claim
        // Documents are Removed
        respondentSumTypeItem.getValue().setEt3ResponseEmployerClaimDocument(null);
        assertThat(ET3DocumentHelper.hasET3Document(respondentSumTypeItem)).isFalse();
    }

    @Test
    void theRemoveET3DocumentsFromDocumentCollection() {
        assertDoesNotThrow(() -> ET3DocumentHelper.removeET3DocumentsFromDocumentCollection(null));

        RespondentSumTypeItem respondentSumTypeItem = ResourceLoader.fromString(
                TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class).getRespondentCollection().get(0);
        List<DocumentTypeItem> documentTypeItems = new ArrayList<>();
        documentTypeItems.add(respondentSumTypeItem.getValue().getEt3ResponseContestClaimDocument().get(0));
        assertThat(documentTypeItems.size()).isEqualTo(1);
        ET3DocumentHelper.removeET3DocumentsFromDocumentCollection(documentTypeItems);
        assertThat(documentTypeItems.size()).isEqualTo(1);

        documentTypeItems.remove(0);
        documentTypeItems.add(DocumentUtil.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
                respondentSumTypeItem.getValue().getEt3Form(), RESPONSE_TO_A_CLAIM, ET3));
        documentTypeItems.add(DocumentUtil.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
                respondentSumTypeItem.getValue().getEt3FormWelsh(), RESPONSE_TO_A_CLAIM, ET3));
        documentTypeItems.add(DocumentUtil.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
                respondentSumTypeItem.getValue().getEt3ResponseEmployerClaimDocument(),
                RESPONSE_TO_A_CLAIM,
                ET3_ATTACHMENT));
        documentTypeItems.add(DocumentUtil.convertUploadedDocumentTypeToDocumentTypeItemWithLevels(
                respondentSumTypeItem.getValue().getEt3ResponseRespondentSupportDocument(),
                RESPONSE_TO_A_CLAIM,
                ET3_ATTACHMENT));
        DocumentUtil.setDocumentTypeItemLevels(
                respondentSumTypeItem.getValue().getEt3ResponseContestClaimDocument().get(0), RESPONSE_TO_A_CLAIM, ET3_ATTACHMENT);
        documentTypeItems.add(respondentSumTypeItem.getValue().getEt3ResponseContestClaimDocument().get(0));
        assertThat(documentTypeItems).hasSize(5);
        ET3DocumentHelper.removeET3DocumentsFromDocumentCollection(documentTypeItems);
        assertThat(documentTypeItems).hasSize(0);
    }

    @Test
    void theFindAllET3DocumentsOfRespondent() {
        assertThat(ET3DocumentHelper.findAllET3DocumentsOfRespondent(null)).isEmpty();

        RespondentSumTypeItem respondentSumTypeItem = ResourceLoader.fromString(
                TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class).getRespondentCollection().get(0);
        assertThat(ET3DocumentHelper.findAllET3DocumentsOfRespondent(respondentSumTypeItem)).hasSize(5);

        respondentSumTypeItem.getValue().setEt3ResponseContestClaimDocument(null);
        assertThat(ET3DocumentHelper.findAllET3DocumentsOfRespondent(respondentSumTypeItem)).hasSize(4);
    }

    @ParameterizedTest
    @MethodSource("provideDataForModifyDocumentCollectionForET3FormsTest")
    void testAddOrRemoveET3Documents(CaseData caseData) {
        ET3DocumentHelper.addOrRemoveET3Documents(caseData);
        if (CollectionUtils.isEmpty(caseData.getRespondentCollection())) {
            assertThat(caseData.getDocumentCollection()).hasSize(7);
        } else if (ObjectUtils.isEmpty(caseData.getRespondentCollection().get(0).getValue())) {
            assertThat(caseData.getDocumentCollection().get(5).getValue().getUploadedDocument().getDocumentFilename())
                    .isEqualTo(ENGLISH_ET3_FORM_NAME);
            assertThat(caseData.getDocumentCollection().get(6).getValue().getUploadedDocument().getDocumentFilename())
                    .isEqualTo(WELSH_ET3_FORM_NAME);
        } else if (RESPONSE_STATUS_REJECTED.equals(
                caseData.getRespondentCollection().get(0).getValue().getResponseStatus())) {
            assertThat(caseData.getDocumentCollection()).hasSize(2);
        } else if (RESPONSE_STATUS_ACCEPTED.equals(
                caseData.getRespondentCollection().get(0).getValue().getResponseStatus())) {
            assertThat(caseData.getDocumentCollection().get(0).getValue().getUploadedDocument().getDocumentFilename())
                    .isEqualTo(ENGLISH_ET3_FORM_NAME);
            assertThat(caseData.getDocumentCollection().get(1).getValue().getUploadedDocument().getDocumentFilename())
                    .isEqualTo(WELSH_ET3_FORM_NAME);
        }
    }

    private static Stream<Arguments> provideDataForModifyDocumentCollectionForET3FormsTest() {
        CaseData caseDataWithoutRespondentCollection =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataWithoutRespondentCollection.setRespondentCollection(null);

        CaseData caseDataRespondentValueNotExists =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataRespondentValueNotExists.getRespondentCollection().get(0).setValue(null);

        CaseData caseDataResponseNotAccepted =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataResponseNotAccepted.getRespondentCollection()
                .get(0).getValue().setResponseStatus(RESPONSE_STATUS_REJECTED);

        CaseData caseDataWithoutDocumentCollectionAndResponseAccepted =
                ResourceLoader.fromString(TEST_ET3_FORM_CASE_DATA_FILE, CaseData.class);
        caseDataWithoutDocumentCollectionAndResponseAccepted.setDocumentCollection(null);
        caseDataWithoutDocumentCollectionAndResponseAccepted.getRespondentCollection()
                .get(0).getValue().setResponseStatus(RESPONSE_STATUS_ACCEPTED);

        return Stream.of(Arguments.of(caseDataWithoutRespondentCollection),
                Arguments.of(caseDataRespondentValueNotExists),
                Arguments.of(caseDataResponseNotAccepted),
                Arguments.of(caseDataWithoutDocumentCollectionAndResponseAccepted));
    }
}
