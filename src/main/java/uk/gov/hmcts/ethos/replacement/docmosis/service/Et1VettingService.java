package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.IntWrapper;

import java.util.List;
import java.util.Optional;

@Service
public class Et1VettingService {

    private static final String ET1_DOC_TYPE = "ET1";
    private static final String ACAS_DOC_TYPE = "ACAS Certificate";
    private static final String BEFORE_LABEL_TEMPLATE = "Open these documents to help you complete this form: %s%s"
            + "<br/>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";
    private static final String BEFORE_LABEL_ET1 =
            "<br/><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS =
            "<br/><a target=\"_blank\" href=\"%s\">Acas certificate %s (opens in new tab)</a>";
    private static final String BEFORE_LABEL_ACAS_OPEN_TAB =
            "<br/><a target=\"_blank\" href=\"/cases/case-details/%s#Documents\">"
                    + "Open the Documents tab to view/open Acas certificates (opens in new tab)</a>";

    /**
     * Update et1VettingBeforeYouStart.
     * @param caseDetails Get caseId and Update caseData
     */
    public void initialiseEt1Vetting(CaseDetails caseDetails) {
        caseDetails.getCaseData().setEt1VettingBeforeYouStart(initialBeforeYouStart(caseDetails));
    }

    /**
     * Prepare wordings to be displayed in et1VettingBeforeYouStart.
     * Check uploaded document in documentCollection
     *  For ET1 form
     *  - get and display ET1 form
     *  For Acas cert
     *  - get and count number of Acas cert
     *  - if 0 Acas cert, hide the Acas link
     *  - if 1-5 Acas cert(s), display one or multi Acas link(s)
     *  - if 6 or more Acas certs, display a link to case doc tab
     * @param caseDetails Get caseId and documentCollection
     * @return et1VettingBeforeYouStart
     */
    private String initialBeforeYouStart(CaseDetails caseDetails) {

        Optional<String> et1Display = Optional.empty();
        StringBuilder acasDisplayStringBuilder = new StringBuilder();
        IntWrapper acasCount = new IntWrapper(0);

        List<DocumentTypeItem> documentCollection = caseDetails.getCaseData().getDocumentCollection();
        if (documentCollection != null) {
            et1Display = documentCollection
                    .stream()
                    .map(d -> getInfoWithDocumentCollection(d, acasDisplayStringBuilder, acasCount))
                    .findFirst();
        }

        String acasDisplay = acasCount.getValue() > 5
                ? String.format(BEFORE_LABEL_ACAS_OPEN_TAB, caseDetails.getCaseId())
                : acasDisplayStringBuilder.toString();

        return String.format(BEFORE_LABEL_TEMPLATE, et1Display.orElse(""), acasDisplay);
    }

    private String getInfoWithDocumentCollection(DocumentTypeItem d, StringBuilder acasDisplayStringBuilder,
                                                 IntWrapper acasCount) {
        if (ACAS_DOC_TYPE.equals(d.getValue().getTypeOfDocument())) {
            acasCount.incrementValue();
            acasDisplayStringBuilder
                    .append(String.format(BEFORE_LABEL_ACAS, createDocLinkBinary(d), acasCount));
        } else if (ET1_DOC_TYPE.equals(d.getValue().getTypeOfDocument())) {
            return String.format(BEFORE_LABEL_ET1, createDocLinkBinary(d));
        }
        return null;
    }

    private String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
    }

}
