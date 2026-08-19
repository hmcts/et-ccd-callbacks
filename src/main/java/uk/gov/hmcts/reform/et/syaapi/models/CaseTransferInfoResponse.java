package uk.gov.hmcts.reform.et.syaapi.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Transfer metadata for a case that is no longer accessible in the citizen hub.
 */
@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseTransferInfoResponse {

    private boolean transferred;

    private CaseTransferType transferType;

    private String caseState;

    private String originalCaseId;

    private String originalEthosCaseReference;

    private String newEthosCaseReference;

    private String newCaseId;

    private String destinationOffice;

    private String reasonForCT;

    private boolean transferComplete;
}
