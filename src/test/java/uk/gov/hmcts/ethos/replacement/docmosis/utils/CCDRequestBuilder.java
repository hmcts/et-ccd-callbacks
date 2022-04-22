package uk.gov.hmcts.ethos.replacement.docmosis.utils;

import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

public class CCDRequestBuilder {

    private CaseData caseData = new CaseData();
    private String state;
    private String caseTypeId;

    public static CCDRequestBuilder builder() {
        return new CCDRequestBuilder();
    }

    public CCDRequestBuilder withCaseData(CaseData caseData) {
        this.caseData = caseData;
        return this;
    }

    public CCDRequestBuilder withState(String state) {
        this.state = state;
        return this;
    }

    public CCDRequestBuilder withCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
        return this;
    }

    public CCDRequest build() {
        var caseDetails = new CaseDetails();
        caseDetails.setState(state);
        caseDetails.setCaseTypeId(caseTypeId);
        caseDetails.setCaseData(caseData);
        var ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }
}
