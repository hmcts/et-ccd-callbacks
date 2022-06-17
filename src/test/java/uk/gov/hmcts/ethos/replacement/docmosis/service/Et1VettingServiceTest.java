package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Et1VettingServiceTest {

    private Et1VettingService et1VettingService;
    private CaseDetails caseDetails;

    @BeforeEach
    void setUp() {
        et1VettingService = new Et1VettingService();
        caseDetails = new CaseDetails();
        CaseData caseData = new CaseData();
        caseDetails.setCaseData(caseData);
    }

    @Test
    void initialBeforeLinkLabel_Normal_shouldReturnLinkLabel() {
        String caseId = "1655312312192821";
        caseDetails.setCaseId(caseId);
        et1VettingService.initialBeforeLinkLabel(caseDetails);
        assertEquals(createBeforeLink(caseId), caseDetails.getCaseData().getVettingBeforeLink());
    }

    private String createBeforeLink(String caseId) {
        String ccdGatewayBaseUrl = "null";
        return "Open these documents to help you complete this form: "
                + "<br/><a target=\"_blank\" href=\"" + ccdGatewayBaseUrl + "/cases/case-details/"
                + caseId + "#Documents\">ET1 form (opens in new tab)</a>"
                + "<br/><a target=\"_blank\" href=\"" + ccdGatewayBaseUrl + "/cases/case-details/"
                + caseId + "#Documents\">Acas certificate (opens in new tab)</a>"
                + "<br/>Check the Documents tab for additional ET1 documents the claimant may have uploaded.";
    }

}