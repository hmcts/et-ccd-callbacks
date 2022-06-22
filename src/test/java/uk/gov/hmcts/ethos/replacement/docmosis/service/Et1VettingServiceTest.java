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
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.BEFORE_LABEL_ACAS;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.BEFORE_LABEL_ACAS_OPEN_TAB;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.BEFORE_LABEL_ET1;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.BEFORE_LABEL_TEMPLATE;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.ET1_DOC_TYPE;

class Et1VettingServiceTest {

    private Et1VettingService et1VettingService;
    private CaseDetails caseDetails;
    private final String et1BinaryUrl1 = "/documents/et1o0c3e-4efd-8886-0dca-1e3876c3178c/binary";
    private final String acasBinaryUrl1 = "/documents/acas1111-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private final String acasBinaryUrl2 = "/documents/acas2222-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private final String acasBinaryUrl3 = "/documents/acas3333-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private final String acasBinaryUrl4 = "/documents/acas4444-4ef8ca1e3-8c60-d3d78808dca1/binary";
    private final String acasBinaryUrl5 = "/documents/acas5555-4ef8ca1e3-8c60-d3d78808dca1/binary";

    @BeforeEach
    void setUp() {
        et1VettingService = new Et1VettingService();
        caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId("1655312312192821");
    }

    @Test
    void initialBeforeLinkLabel_ZeroAcas_shouldReturnEt1Only() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE, et1BinaryUrl1));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertEquals(String.format(BEFORE_LABEL_TEMPLATE, String.format(BEFORE_LABEL_ET1, et1BinaryUrl1), ""),
                caseDetails.getCaseData().getEt1VettingBeforeYouStart());
    }

    @Test
    void initialBeforeLinkLabel_FiveAcas_shouldReturnFiveAcas() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE, et1BinaryUrl1));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl1));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl2));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl3));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl4));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl5));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertEquals(String.format(BEFORE_LABEL_TEMPLATE,
                        String.format(BEFORE_LABEL_ET1, et1BinaryUrl1),
                        String.format(BEFORE_LABEL_ACAS, acasBinaryUrl1, "1")
                                + String.format(BEFORE_LABEL_ACAS, acasBinaryUrl2, "2")
                                + String.format(BEFORE_LABEL_ACAS, acasBinaryUrl3, "3")
                                + String.format(BEFORE_LABEL_ACAS, acasBinaryUrl4, "4")
                                + String.format(BEFORE_LABEL_ACAS, acasBinaryUrl5, "5")),
                caseDetails.getCaseData().getEt1VettingBeforeYouStart());
    }

    @Test
    void initialBeforeLinkLabel_SixAcas_shouldReturnDocTab() {
        List<DocumentTypeItem> documentTypeItemList = new ArrayList<>();
        documentTypeItemList.add(createDocumentTypeItem(ET1_DOC_TYPE, et1BinaryUrl1));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl1));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl2));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl3));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl4));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE, acasBinaryUrl5));
        documentTypeItemList.add(createDocumentTypeItem(ACAS_DOC_TYPE,
                "/documents/acas6666-4ef8ca1e3-8c60-d3d78808dca1/binary"));
        caseDetails.getCaseData().setDocumentCollection(documentTypeItemList);

        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertEquals(String.format(BEFORE_LABEL_TEMPLATE,
                        String.format(BEFORE_LABEL_ET1, et1BinaryUrl1),
                        String.format(BEFORE_LABEL_ACAS_OPEN_TAB, "/cases/case-details/1655312312192821#Documents")),
                caseDetails.getCaseData().getEt1VettingBeforeYouStart());
    }

    @Test
    void initialBeforeYouStart_NoDocumentCollection_shouldReturnWithoutUrl() {
        et1VettingService.initialiseEt1Vetting(caseDetails);
        assertEquals(String.format(BEFORE_LABEL_TEMPLATE, "", ""),
                caseDetails.getCaseData().getEt1VettingBeforeYouStart());
    }

    private DocumentTypeItem createDocumentTypeItem(String typeOfDocument, String binaryLink) {
        DocumentType documentType = new DocumentType();
        documentType.setTypeOfDocument(typeOfDocument);
        documentType.setUploadedDocument(new UploadedDocumentType());
        documentType.getUploadedDocument().setDocumentBinaryUrl("http://dm-store:8080" + binaryLink);
        DocumentTypeItem documentTypeItem = new DocumentTypeItem();
        documentTypeItem.setValue(documentType);
        return documentTypeItem;
    }

}