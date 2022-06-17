package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

@Service
public class Et1VettingService {

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    static final String LABEL_LINE_START = "Open these documents to help you complete this form: ";
    static final String LABEL_FT1_FROM = "ET1 form (opens in new tab)";
    static final String LABEL_ACAS_CERT = "Acas certificate (opens in new tab)";
    static final String LABEL_LINE_END =
            "Check the Documents tab for additional ET1 documents the claimant may have uploaded.";
    static final String LABEL_LINE_BR = "<br/>";

    /**
     * Update vettingBeforeLink.
     * @param caseDetails Get caseId and Update caseData
     */
    public void initialBeforeLinkLabel(CaseDetails caseDetails) {
        caseDetails.getCaseData().setVettingBeforeLink(generateBeforeLinkLabel(caseDetails.getCaseId()));
    }

    private String generateBeforeLinkLabel(String caseId) {
        return LABEL_LINE_START
                + generateDocLink(caseId, LABEL_FT1_FROM)
                + generateDocLink(caseId, LABEL_ACAS_CERT)
                + LABEL_LINE_BR + LABEL_LINE_END;
    }

    private String generateDocLink(String caseId, String displayWords) {
        return LABEL_LINE_BR
                + "<a target=\"_blank\" href=\""
                + ccdGatewayBaseUrl
                + "/cases/case-details/"
                + caseId
                + "#Documents"
                + "\">"
                + displayWords
                + "</a>";
    }

}
