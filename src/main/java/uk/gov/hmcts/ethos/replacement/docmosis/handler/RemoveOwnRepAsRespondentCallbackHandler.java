package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class RemoveOwnRepAsRespondentCallbackHandler extends CallbackHandlerBase {

    @Autowired
    public RemoveOwnRepAsRespondentCallbackHandler(
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
        return List.of("REMOVE_OWN_REP_AS_RESPONDENT");
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
        if (CollectionUtils.isNotEmpty(caseData.getRepCollection())
                && CollectionUtils.isNotEmpty(caseData.getRepCollectionToRemove())) {
            caseData.getRepCollection().removeAll(caseData.getRepCollectionToRemove());
            caseData.setRepCollectionToRemove(null);
        }
        return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
    }
}
