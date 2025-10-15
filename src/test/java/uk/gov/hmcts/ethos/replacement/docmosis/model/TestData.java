package uk.gov.hmcts.ethos.replacement.docmosis.model;

import lombok.Data;
import uk.gov.hmcts.et.common.model.ccd.Et1CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.utils.ResourceLoader;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class TestData {
    private final CaseDetails caseDetailsWithData = ResourceLoader.fromString(
            "citizen/caseDetailsWithCaseData.json",
            CaseDetails.class
    );

    private final CaseDetails caseDetails = ResourceLoader.fromString(
            "citizen/caseDetails.json",
            CaseDetails.class
    );

    public Map<String, Object> getCaseRequestCaseDataMap() {
        Et1CaseData et1CaseData = ResourceLoader.fromString("caseData.json", Et1CaseData.class);
        Map<String, Object> requestCaseData = new ConcurrentHashMap<>();
        requestCaseData.put("typesOfClaim", et1CaseData.getTypesOfClaim());
        requestCaseData.put("caseType", et1CaseData.getEcmCaseType());
        requestCaseData.put("caseSource", et1CaseData.getCaseSource());
        requestCaseData.put("claimantRepresentedQuestion", et1CaseData.getClaimantRepresentedQuestion());
        requestCaseData.put("jurCodesCollection", et1CaseData.getJurCodesCollection());
        requestCaseData.put("claimantIndType", et1CaseData.getClaimantIndType());
        requestCaseData.put("claimantType", et1CaseData.getClaimantType());
        requestCaseData.put("representativeClaimantType", et1CaseData.getRepresentativeClaimantType());
        requestCaseData.put("claimantOtherType", et1CaseData.getClaimantOtherType());
        requestCaseData.put("respondentCollection", et1CaseData.getRespondentCollection());
        requestCaseData.put("claimantWorkAddress", et1CaseData.getClaimantWorkAddress());
        requestCaseData.put("caseNotes", et1CaseData.getCaseNotes());
        requestCaseData.put("managingOffice", et1CaseData.getManagingOffice());
        requestCaseData.put("newEmploymentType", et1CaseData.getNewEmploymentType());
        requestCaseData.put("claimantRequests", et1CaseData.getClaimantRequests());
        requestCaseData.put("claimantHearingPreference", et1CaseData.getClaimantHearingPreference());
        requestCaseData.put("claimantTaskListChecks", et1CaseData.getClaimantTaskListChecks());
        return requestCaseData;
    }

}
