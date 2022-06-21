package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ACAS_DOC_TYPE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.BEFORE_LINK_LABEL;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ET1_DOC_TYPE;

class Et1VettingServiceTest {

    private Et1VettingService et1VettingService;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        et1VettingService = new Et1VettingService();
        caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("1655312312192821");
    }

    @Test
    void initialBeforeLinkLabel_Exist_shouldReturnBinaryUrl() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE,
                "http://dm-store:8080/documents/et10dcae-4efd-8886-0dca-1e3876c3178c/binary"));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE,
                "http://dm-store:8080/documents/acas76ce-4ef8ca1e3-8c60-d3d78808dca1/binary"));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialBeforeYouStart(caseDetails);
        assertEquals(String.format(BEFORE_LINK_LABEL,
                        "/documents/et10dcae-4efd-8886-0dca-1e3876c3178c/binary",
                        "/documents/acas76ce-4ef8ca1e3-8c60-d3d78808dca1/binary"),
                caseDetails.getCaseData().getEt1VettingBeforeYouStart());
    }

    @Test
    void initialBeforeLinkLabel_NotExist_shouldReturnDefaultUrl() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialBeforeYouStart(caseDetails);
        assertEquals(String.format(BEFORE_LINK_LABEL,
                        "/cases/case-details/1655312312192821#Documents",
                        "/cases/case-details/1655312312192821#Documents"),
                caseDetails.getCaseData().getEt1VettingBeforeYouStart());
    }

    @Test
    void initialBeforeYouStart_NoDocumentCollection_shouldReturnDefaultUrl() {
        et1VettingService.initialBeforeYouStart(caseDetails);
        assertEquals(String.format(BEFORE_LINK_LABEL,
                        "/cases/case-details/1655312312192821#Documents",
                        "/cases/case-details/1655312312192821#Documents"),
                caseDetails.getCaseData().getEt1VettingBeforeYouStart());
    }

    private DocumentTypeItem createDocumentTypeItem(String typeOfDocument, String binaryLink) {
        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(typeOfDocument);
        documentType.setUploadedDocument(new UploadedDocumentType());
        documentType.getUploadedDocument().setDocumentBinaryUrl(binaryLink);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(documentType);
        return documentTypeItem;
    }

}