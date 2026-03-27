package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import io.micrometer.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.et.common.model.ccd.types.OrganisationPolicy;
import uk.gov.hmcts.ethos.replacement.docmosis.domain.ClaimantSolicitorRole;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class RemoveOwnRepAsClaimantCallbackHandler extends CallbackHandlerBase {

    @Autowired
    public RemoveOwnRepAsClaimantCallbackHandler(
        CaseDetailsConverter caseDetailsConverter
    ) {
        super(caseDetailsConverter);
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("REMOVE_OWN_REP_AS_CLAIMANT");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return false;
    }

    @Override
    CallbackResponse<CaseData> aboutToSubmit(CaseDetails caseDetails) {
        var ccdRequest = toCcdRequest(caseDetails);
        var caseData = ccdRequest.getCaseDetails().getCaseData();
        if (StringUtils.isNotBlank(caseData.getClaimantRepresentedQuestion())
                && NO.equals(caseData.getClaimantRepresentedQuestion())) {
            caseData.setRepresentativeClaimantType(null);
            caseData.setClaimantRepresentativeOrganisationPolicy(
                OrganisationPolicy.builder()
                    .orgPolicyCaseAssignedRole(ClaimantSolicitorRole.CLAIMANTSOLICITOR.getCaseRoleLabel())
                    .build()
            );
        }
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }
}
