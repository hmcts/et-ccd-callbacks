package uk.gov.hmcts.ethos.replacement.docmosis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ethos.replacement.docmosis.service.Et1VettingService.BEFORE_LINK_LABEL;

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
        assertEquals(String.format(BEFORE_LINK_LABEL, "null", caseId, "null", caseId),
                caseDetails.getCaseData().getVettingBeforeLink());
    }

}