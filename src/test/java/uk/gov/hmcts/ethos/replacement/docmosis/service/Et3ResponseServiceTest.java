package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.DocumentInfo;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.CaseDataBuilder;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.DocumentFixtures;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.ENGLANDWALES_CASE_TYPE_ID;
import static uk.gov.hmcts.ethos.replacement.docmosis.utils.InternalException.ERROR_MESSAGE;

@ExtendWith(SpringExtension.class)
class Et3ResponseServiceTest {
    private Et3ResponseService et3ResponseService;
    @MockBean
    private DocumentManagementService documentManagementService;
    @MockBean
    private TornadoService tornadoService;
    private CaseData caseData;
    private DocumentInfo documentInfo;

    @BeforeEach
    void setUp() {
        et3ResponseService = new Et3ResponseService(documentManagementService, tornadoService);
        caseData = CaseDataBuilder.builder()
            .withClaimantIndType("Doris", "Johnson")
            .withClaimantType("232 Petticoat Square", "3 House", null,
                "London", "W10 4AG", "United Kingdom")
            .withRespondentWithAddress("Antonio Vazquez",
                "11 Small Street", "22 House", null,
                "Manchester", "M12 42R", "United Kingdom",
                "1234/5678/90")
            .withEt3RepresentingRespondent("Antonio Vazquez")
            .withSubmitEt3Respondent("Antonio Vazquez")
            .build();
        caseData.setEt3NoEt3Response("Test data");
        documentInfo = DocumentInfo.builder()
            .description("test-description")
            .url("https://test.com/documents/random-uuid")
            .build();
        doCallRealMethod().when(documentManagementService).addDocumentToDocumentField(documentInfo);
    }

    @Test
    void generateEt3ProcessingDocument() throws IOException {
        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
            anyString(), anyString())).thenReturn(documentInfo);
        DocumentInfo documentInfo1 = et3ResponseService.generateEt3ResponseDocument(new CaseData(), "userToken",
            ENGLANDWALES_CASE_TYPE_ID);
        assertThat(documentInfo1, is(documentInfo));
    }

    @Test
    void generateEt3ProcessingDocumentExceptions() throws IOException {
        when(tornadoService.generateEventDocument(any(CaseData.class), anyString(),
            anyString(), anyString())).thenThrow(new InternalException(ERROR_MESSAGE));
        assertThrows(Exception.class, () -> et3ResponseService.generateEt3ResponseDocument(new CaseData(), "userToken",
            ENGLANDWALES_CASE_TYPE_ID));
    }

    @Test
    void assertThatEt3DocumentIsSaved() {
        et3ResponseService.saveEt3ResponseDocument(caseData, documentInfo);
        assertThat(caseData.getDocumentCollection().size(), is(1));
        assertNotNull(caseData.getRespondentCollection().get(0).getValue().getEt3Form());
    }

    @Test
    void saveRelatedDocumentsToDocumentCollection_savesAllDocuments() {
        caseData.setEt3ResponseEmployerClaimDocument(
                DocumentFixtures.getUploadedDocumentType("ecc.docx")
        );
        caseData.setEt3ResponseRespondentSupportDocument(
                DocumentFixtures.getUploadedDocumentType("support.docx")
        );
        caseData.setEt3ResponseContestClaimDocument(
                List.of(DocumentTypeItem.fromUploadedDocument(DocumentFixtures.getUploadedDocumentType("claim.docx")))
        );
        et3ResponseService.saveRelatedDocumentsToDocumentCollection(caseData);
        assertThat(caseData.getDocumentCollection().size(), is(3));
    }

    @Test
    void saveRelatedDocumentsToDocumentCollection_doesNotSaveNullDocuments() {
        caseData.setEt3ResponseContestClaimDocument(
                List.of(DocumentTypeItem.fromUploadedDocument(DocumentFixtures.getUploadedDocumentType("claim.docx")))
        );
        et3ResponseService.saveRelatedDocumentsToDocumentCollection(caseData);
        assertThat(caseData.getDocumentCollection().size(), is(1));
    }

    @Test
    void saveRelatedDocumentsToDocumentCollection_doesNotSaveSameDocumentTwice() {
        caseData.setEt3ResponseContestClaimDocument(
                List.of(DocumentTypeItem.fromUploadedDocument(DocumentFixtures.getUploadedDocumentType("claim.docx")))
        );
        et3ResponseService.saveRelatedDocumentsToDocumentCollection(caseData);
        et3ResponseService.saveRelatedDocumentsToDocumentCollection(caseData);
        assertThat(caseData.getDocumentCollection().size(), is(1));
    }
}
