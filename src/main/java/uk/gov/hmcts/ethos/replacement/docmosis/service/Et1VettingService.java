package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;
import uk.gov.hmcts.et.common.model.ccd.items.DocumentTypeItem;
import uk.gov.hmcts.et.common.model.ccd.items.RespondentSumTypeItem;
import uk.gov.hmcts.et.common.model.ccd.types.ClaimantIndType;
import uk.gov.hmcts.et.common.model.ccd.types.RespondentSumType;

import java.util.List;

@Service
public class Et1VettingService {

    static final String ET1_DOC_TYPE = "ET1";
    static final String ACAS_DOC_TYPE = "ACAS Certificate";
    static final String BEFORE_LABEL_TEMPLATE = "Open these documents to help you complete this form: %s%s"
            + "<br/>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";
    static final String BEFORE_LABEL_ET1 =
            "<br/><a target=\"_blank\" href=\"%s\">ET1 form (opens in new tab)</a>";
    static final String BEFORE_LABEL_ACAS =
            "<br/><a target=\"_blank\" href=\"%s\">Acas certificate %s (opens in new tab)</a>";
    static final String BEFORE_LABEL_ACAS_OPEN_TAB = "<br/><a target=\"_blank\" href=\"/cases/case-details/%s#Documents\">"
            + "Open the Documents tab to view/open Acas certificates (opens in new tab)</a>";
    static final String CLAIMANT_DETAILS = "| Claimant | |\n"
            + "| --- | --- |\n"
            + "| First name | %s |\n"
            + "| Last name | %s |\n"
            + "| Contact address | %s |\n";
    static final String RESPONDENT_DETAILS = "| Respondent | |\n"
            + "| --- | --- |\n"
            + "| Name | %s |\n"
            + "| Contact address | %s |";

    /**
     * Update et1VettingBeforeYouStart.
     * @param caseDetails Get caseId and Update caseData
     */
    public void initialiseEt1Vetting(CaseDetails caseDetails) {
        caseDetails.getCaseData().setEt1VettingBeforeYouStart(initialBeforeYouStart(caseDetails));
        caseDetails.getCaseData().setEt1VettingClaimantDetailsMarkUp(
                initialClaimantDetailsMarkUp(caseDetails.getCaseData()));
        caseDetails.getCaseData().setEt1VettingRespondentDetailsMarkUp(
                initialRespondentDetailsMarkUp(caseDetails.getCaseData()));
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

        String et1Display = "";
        StringBuilder acasDisplayStringBuilder = new StringBuilder();
        int acasCount = 0;

        List<DocumentTypeItem> documentCollection = caseDetails.getCaseData().getDocumentCollection();
        if (CollectionUtils.isNotEmpty(documentCollection)) {
            for (DocumentTypeItem d : documentCollection) {
                if (ACAS_DOC_TYPE.equals(d.getValue().getTypeOfDocument())) {
                    acasCount++;
                    acasDisplayStringBuilder
                            .append(String.format(BEFORE_LABEL_ACAS, createDocLinkBinary(d), acasCount));
                } else if (ET1_DOC_TYPE.equals(d.getValue().getTypeOfDocument())) {
                    et1Display = String.format(BEFORE_LABEL_ET1, createDocLinkBinary(d));
                }
            }
        }

        String acasDisplay = acasCount > 5
                ? String.format(BEFORE_LABEL_ACAS_OPEN_TAB, caseDetails.getCaseId())
                : acasDisplayStringBuilder.toString();

        return String.format(BEFORE_LABEL_TEMPLATE, et1Display, acasDisplay);
    }

    private String initialClaimantDetailsMarkUp(CaseData caseData) {
        return String.format(CLAIMANT_DETAILS,
                caseData.getClaimantIndType().getClaimantFirstNames(),
                caseData.getClaimantIndType().getClaimantLastName(),
                caseData.getClaimantType().getClaimantAddressUK().toAddressHtml());
    }

    private String initialRespondentDetailsMarkUp(CaseData caseData) {
        RespondentSumType respondent = caseData.getRespondentCollection().get(0).getValue();
        return String.format(RESPONDENT_DETAILS,
                respondent.getRespondentName(),
                respondent.getRespondentAddress().toAddressHtml());
    }

    private String createDocLinkBinary(DocumentTypeItem documentTypeItem) {
        String documentBinaryUrl = documentTypeItem.getValue().getUploadedDocument().getDocumentBinaryUrl();
        return documentBinaryUrl.substring(documentBinaryUrl.indexOf("/documents/"));
    }

}
