package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;

@Service
public class Et1VettingService {

    static final String ET1_DOC_TYPE = "ET1";
    static final String ACAS_DOC_TYPE = "ACAS Certificate";
    static final String DOC_LINK_DEFAULT = "/cases/case-details/%s#Documents";
    static final String BEFORE_LINK_LABEL = "Open these documents to help you complete this form: "
            + "<br/><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>"
            + "<br/><a target=\"_blank\" href=\"%s\">Acas certificate (opens in new tab)</a>"
            + "<br/>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";

    /**
     * Update et1VettingBeforeYouStart.
     * @param caseDetails Get caseId and Update caseData
     */
    public void initialBeforeYouStart(CaseDetails caseDetails) {

        var et1BinaryUrl = createDocLinkDefault(caseDetails.getCaseId());
        var acasBinaryUrl = createDocLinkDefault(caseDetails.getCaseId());

        var documentCollection = caseDetails.getCaseData().getDocumentCollection();
        for (DocumentTypeItem d : documentCollection) {
            if (d.getValue().getTypeOfDocument().equals(ET1_DOC_TYPE)) {
                et1BinaryUrl = createDocLinkBinary(d);
            }
            if (d.getValue().getTypeOfDocument().equals(ACAS_DOC_TYPE)) {
                acasBinaryUrl = createDocLinkBinary(d);
            }
        }
        caseDetails.getCaseData().setEt1VettingBeforeYouStart(
                String.format(BEFORE_LINK_LABEL, et1BinaryUrl, acasBinaryUrl));
    }

    private String createDocLinkDefault(String caseId) {
        return String.format(DOC_LINK_DEFAULT, caseId);
    }

    private String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        var documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
    }

}
