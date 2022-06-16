package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

@Service
public class CaseVettingService {

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    public void initialBeforeLinkLabel(CaseDetails caseDetails) {
        caseDetails.getCaseData().setVettingBeforeLinkLabel(generateBeforeLinkLabel(caseDetails.getCaseId()));
    }

    private String generateBeforeLinkLabel(String caseId) {
        return "Open these documents to help you complete this form: "
                + generateDocLink(caseId, "ET1 form (opens in new tab)")
                + generateDocLink(caseId, "Acas certificate (opens in new tab)")
                + "<br/>"
                + "Check the Documents tab for additional ET1 documents the claimant may have uploaded.";
    }

    private String generateDocLink(String caseId, String displayWords) {
        return "<br/>"
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
