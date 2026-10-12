package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.DigitalCaseFileType;
import uk.gov.hmcts.et.common.model.ccd.types.DocumentType;
import uk.gov.hmcts.et.common.model.ccd.types.UploadedDocumentType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ecm.common.model.helper.DocumentConstants.TRIBUNAL_CASE_FILE;

class DigitalCaseFileServiceTest {

    private static final String EXPECTED_LINK = "<a target=\"_blank\" href=\"/documents/file/binary\">"
        + "Digital case file (opens in new tab)</a><br>";
    private final DigitalCaseFileService service = new DigitalCaseFileService(null, null, null);

    @Test
    void returnsDigitalCaseFileLink() {
        CaseData caseData = new CaseData();
        DigitalCaseFileType dcf = new DigitalCaseFileType();
        dcf.setUploadedDocument(uploadedDocument());
        caseData.setDigitalCaseFile(dcf);

        assertThat(service.getReplyToReferralDCFLink(caseData)).isEqualTo(EXPECTED_LINK);
    }

    @Test
    void fallsBackToLegacyTribunalCaseFile() {
        assertThat(service.getReplyToReferralDCFLink(legacyCaseData())).isEqualTo(EXPECTED_LINK);
    }

    @Test
    void returnsEmptyWithoutARelevantDocument() {
        assertThat(service.getReplyToReferralDCFLink(new CaseData())).isEmpty();

        CaseData caseData = legacyCaseData();
        caseData.getDocumentCollection().getFirst().getValue().setTypeOfDocument("Other");
        caseData.getDocumentCollection().getFirst().getValue().setMiscDocuments("Other");
        assertThat(service.getReplyToReferralDCFLink(caseData)).isEmpty();
    }

    @Test
    void returnsEmptyWithoutAnUploadedDocumentOrBinaryUrl() {
        CaseData caseData = legacyCaseData();
        caseData.getDocumentCollection().getFirst().getValue().setUploadedDocument(null);
        assertThat(service.getReplyToReferralDCFLink(caseData)).isEmpty();

        caseData = legacyCaseData();
        caseData.getDocumentCollection().getFirst().getValue().getUploadedDocument().setDocumentBinaryUrl(null);
        assertThat(service.getReplyToReferralDCFLink(caseData)).isEmpty();
    }

    private CaseData legacyCaseData() {
        CaseData caseData = new CaseData();
        DocumentTypeItem document = new DocumentTypeItem();
        document.setValue(DocumentType.builder()
            .uploadedDocument(uploadedDocument())
            .typeOfDocument(TRIBUNAL_CASE_FILE)
            .miscDocuments(TRIBUNAL_CASE_FILE)
            .build());
        caseData.setDocumentCollection(List.of(document));
        return caseData;
    }

    private UploadedDocumentType uploadedDocument() {
        return UploadedDocumentType.builder()
            .documentBinaryUrl("http://dm-store:8080/documents/file/binary")
            .build();
    }
}
