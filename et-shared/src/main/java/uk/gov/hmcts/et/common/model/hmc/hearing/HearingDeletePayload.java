package uk.gov.hmcts.et.common.model.hmc.hearing;

import jakarta.validation.constraints.Size;

import java.util.List;

public class HearingDeletePayload {
    private List<@Size(max = 100) String> cancellationReasonCodes;
}
