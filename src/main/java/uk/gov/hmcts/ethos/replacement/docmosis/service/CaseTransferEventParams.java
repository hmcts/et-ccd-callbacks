package uk.gov.hmcts.ethos.replacement.docmosis.service;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CaseTransferEventParams {
    private String userToken;
    private String caseTypeId;
    private String jurisdiction;
    private String ethosCaseReference;
    private String newManagingOffice;
    private String positionType;
    private String reason;
    private String ecmCaseType;
    private boolean transferSameCountry;
    private String sourceEthosCaseReference;
}
