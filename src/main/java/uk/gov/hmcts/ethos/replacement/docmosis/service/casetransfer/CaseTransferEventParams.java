package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class CaseTransferEventParams {
    private String userToken;
    private String caseTypeId;
    private String jurisdiction;
    private List<String> ethosCaseReferences;
    private String newManagingOffice;
    private String positionType;
    private String reason;
    private String multipleReference;
    private boolean confirmationRequired;
    private String multipleReferenceLink;
    private boolean transferSameCountry;
    private String sourceEthosCaseReference;
}
