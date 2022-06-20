package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

@Service
public class Et1VettingService {

    @Value("${ccd_gateway_base_url}")
    private String ccdGatewayBaseUrl;

    static final String BEFORE_LINK_LABEL = "Open these documents to help you complete this form: "
            + "<br/><a target=\"_blank\" href=\"%s/cases/case-details/%s#Documents\">ET1 form (opens in new tab)</a>"
            + "<br/><a target=\"_blank\" href=\"%s/cases/case-details/%s#Documents\">Acas certificate (opens in new tab)</a>"
            + "<br/>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";

    /**
     * Update et1VettingBeforeYouStart.
     * @param caseDetails Get caseId and Update caseData
     */
    public void initialBeforeYouStart(CaseDetails caseDetails) {
        caseDetails.getCaseData().setEt1VettingBeforeYouStart(String.format(BEFORE_LINK_LABEL,
                ccdGatewayBaseUrl, caseDetails.getCaseId(), ccdGatewayBaseUrl, caseDetails.getCaseId()));
    }

}
