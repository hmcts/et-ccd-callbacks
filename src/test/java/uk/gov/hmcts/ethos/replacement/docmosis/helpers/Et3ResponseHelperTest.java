package uk.gov.hmcts.ethos.replacement.docmosis.helpers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;

class Et3ResponseHelperTest {

    public static final String START_DATE_MUST_BE_IN_THE_PAST = "Start date must be in the past";
    public static final String END_DATE_MUST_BE_AFTER_THE_START_DATE = "End date must be after the start date";
    private CaseData caseData;
    private UploadedDocumentType document1ToSave;
    private UploadedDocumentType document2ToSave;

    @BeforeEach
    void setUp() {
        CaseDetails caseDetails = CaseDataBuilder.builder()
            .buildAsCaseDetails(ENGLANDWALES_CASE_TYPE_ID);

        caseData = caseDetails.getCaseData();
        document1ToSave = new UploadedDocumentType();
        document1ToSave.setDocumentFilename("Document 1");
        document1ToSave.setDocumentUrl("documentstore.com/document1");
        document1ToSave.setDocumentBinaryUrl("binary.documentstore.com/document1");

        document2ToSave = new UploadedDocumentType();
        document2ToSave.setDocumentFilename("Document 2");
        document2ToSave.setDocumentUrl("documentstore.com/document2");
        document2ToSave.setDocumentBinaryUrl("binary.documentstore.com/document2");
    }

    @Test
    void givenClaimant_shouldFormatToTable() {
        caseData.setClaimant("Test Person");

        String expected = "<pre> ET1 claimant name&#09&#09&#09&#09 Test Person</pre><hr>";
        assertThat(Et3ResponseHelper.formatClaimantNameForHtml(caseData), is(expected));
    }

    @Test
    void givenValidStartDateAndEndDate_shouldReturnNoErrors() {
        caseData.setEt3ResponseEmploymentStartDate("2022-02-02");
        caseData.setEt3ResponseEmploymentEndDate("2022-02-02");

        assertThat(Et3ResponseHelper.validateEmploymentDates(caseData).size(), is(0));
    }

    @Test
    void givenNoStartDateAndEndDate_shouldNotValidateDates() {
        caseData.setEt3ResponseEmploymentEndDate("2022-02-02");

        assertThat(Et3ResponseHelper.validateEmploymentDates(caseData).size(), is(0));
    }

    @Test
    void givenStartDateAndNoEndDate_shouldNotValidateDates() {
        caseData.setEt3ResponseEmploymentStartDate("2022-02-02");

        assertThat(Et3ResponseHelper.validateEmploymentDates(caseData).size(), is(0));
    }

    @Test
    void givenNoStartDateAndNoEndDate_shouldNotValidateDates() {
        assertThat(Et3ResponseHelper.validateEmploymentDates(caseData).size(), is(0));
    }

    @Test
    void givenInvalidStartDateAndEndDate_shouldReturnAnError() {
        caseData.setEt3ResponseEmploymentStartDate("2022-02-03");
        caseData.setEt3ResponseEmploymentEndDate("2022-02-02");

        List<String> errors = Et3ResponseHelper.validateEmploymentDates(caseData);

        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(END_DATE_MUST_BE_AFTER_THE_START_DATE));
    }

    @Test
    void givenStartDateInTheFuture_shouldReturnAnError() {
        caseData.setEt3ResponseEmploymentStartDate("2099-02-02");

        List<String> errors = Et3ResponseHelper.validateEmploymentDates(caseData);

        assertThat(errors.size(), is(1));
        assertThat(errors.get(0), is(START_DATE_MUST_BE_IN_THE_PAST));
    }

    @Test
    void givenStartDateInTheFutureAndEndDateBeforeStartDate_shouldReturnErrors() {
        caseData.setEt3ResponseEmploymentStartDate("2099-02-02");
        caseData.setEt3ResponseEmploymentEndDate("2022-02-02");

        List<String> errors = Et3ResponseHelper.validateEmploymentDates(caseData);

        assertThat(errors.size(), is(2));
        assertThat(errors.get(0), is(START_DATE_MUST_BE_IN_THE_PAST));
        assertThat(errors.get(1), is(END_DATE_MUST_BE_AFTER_THE_START_DATE));
    }

    @Test
    void givenADocument_saveItToTheET3Collection() {
        assertNull(caseData.getEt3ResponseDocumentCollection());

        Et3ResponseHelper.addDocument(caseData, document1ToSave);

        assertThat(caseData.getEt3ResponseDocumentCollection().size(), is(1));
    }

    @Test
    void givenNoDocument_dontSaveItToTheET3Collection() {
        assertNull(caseData.getEt3ResponseDocumentCollection());

        Et3ResponseHelper.addDocument(caseData, null);

        assertNull(caseData.getEt3ResponseDocumentCollection());
    }

    @Test
    void givenCollectionOfDocuments_saveAllToTheET3Collection() {
        assertNull(caseData.getEt3ResponseDocumentCollection());

        DocumentTypeItem documentTypeItem1 = new DocumentTypeItem();
        DocumentType documentType1 = new DocumentType();
        documentType1.setUploadedDocument(document1ToSave);
        documentTypeItem1.setValue(documentType1);

        DocumentTypeItem documentTypeItem2 = new DocumentTypeItem();
        DocumentType documentType2 = new DocumentType();
        documentType2.setUploadedDocument(document2ToSave);
        documentTypeItem2.setValue(documentType2);

        List<DocumentTypeItem> documents = Arrays.asList(documentTypeItem1, documentTypeItem2);

        Et3ResponseHelper.addDocuments(caseData, documents);

        assertThat(caseData.getEt3ResponseDocumentCollection().size(), is(2));
    }

    @Test
    void givenNoCollectionOfDocuments_saveAllToTheET3Collection() {
        assertNull(caseData.getEt3ResponseDocumentCollection());

        Et3ResponseHelper.addDocuments(caseData, null);

        assertNull(caseData.getEt3ResponseDocumentCollection());
    }

    @Test
    void givenADuplicateDocument_saveOnceToTheET3Collection() {
        assertNull(caseData.getEt3ResponseDocumentCollection());

        Et3ResponseHelper.addDocument(caseData, document1ToSave);
        Et3ResponseHelper.addDocument(caseData, document1ToSave);

        assertThat(caseData.getEt3ResponseDocumentCollection().size(), is(1));
    }
}
