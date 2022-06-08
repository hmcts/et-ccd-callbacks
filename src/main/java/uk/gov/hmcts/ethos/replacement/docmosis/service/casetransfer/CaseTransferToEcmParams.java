package uk.gov.hmcts.ethos.replacement.docmosis.service.casetransfer;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class CaseTransferToEcmParams {
    private String userToken;
    private String caseTypeId;
    private String jurisdiction;
    private List<String> ethosCaseReferences;
    private String newCaseTypeId;
    private String positionType;
    private String reason;
    private boolean confirmationRequired;
    private String sourceEthosCaseReference;
}
