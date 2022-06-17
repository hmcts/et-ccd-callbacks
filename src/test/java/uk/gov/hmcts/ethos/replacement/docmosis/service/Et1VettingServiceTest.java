package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.LABEL_ACAS_CERT;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.LABEL_FT1_FROM;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.LABEL_LINE_BR;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.LABEL_LINE_END;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.LABEL_LINE_START;

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
        return LABEL_LINE_START
                + LABEL_LINE_BR + "<a target=\"_blank\" href=\"" + ccdGatewayBaseUrl + "/cases/case-details/" + caseId
                + "#Documents\">" + LABEL_FT1_FROM + "</a>"
                + LABEL_LINE_BR + "<a target=\"_blank\" href=\"" + ccdGatewayBaseUrl + "/cases/case-details/" + caseId
                + "#Documents\">" + LABEL_ACAS_CERT + "</a>"
                + LABEL_LINE_BR + LABEL_LINE_END;
    }

}