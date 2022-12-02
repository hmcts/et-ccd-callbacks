package uk.gov.hmcts.ethos.replacement.apitest.utils;

import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.CaseDetails;

public class CCDRequestBuilder {

    private CaseData caseData = new CaseData();
    private String state;
    private String caseTypeId;
    private String caseId;

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

    public CCDRequestBuilder withCaseId(String id) {
        this.caseId = id;
        return this;
    }

    public CCDRequestBuilder withCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
        return this;
    }

    public CCDRequest build() {
        CaseDetails caseDetails = new CaseDetails();
        caseDetails.setState(state);
        caseDetails.setCaseTypeId(caseTypeId);
        caseDetails.setCaseData(caseData);
        caseDetails.setCaseId(caseId);
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }
}
