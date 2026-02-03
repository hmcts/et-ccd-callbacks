package uk.gov.hmcts.ethos.replacement.docmosis.service.messagehandler;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.et.common.model.ccd.CaseData;

/**
 * Model for case transfer data.
 * Migrated from et-message-handler.
 */
@Builder
@Getter
public class CaseTransfer {
    private long caseId;
    private boolean transferSameCountry;
    private String caseTypeId;
    private String jurisdiction;
    private CaseData caseData;
    private String officeCT;
    private String positionTypeCT;
    private String reasonCT;
    private String sourceEthosCaseReference;
}
