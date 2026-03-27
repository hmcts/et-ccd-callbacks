package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.RefreshSharedUsersService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityErrors;
import static uk.gov.hmcts.ethos.replacement.docmosis.helpers.CallbackRespHelper.getCallbackRespEntityNoErrors;

@Component
public class RefreshSharedUsersCallbackHandler extends CallbackHandlerBase {

    private final RefreshSharedUsersService refreshSharedUsersService;

    @Autowired
    public RefreshSharedUsersCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        RefreshSharedUsersService refreshSharedUsersService
    ) {
        super(caseDetailsConverter);
        this.refreshSharedUsersService = refreshSharedUsersService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("refreshSharedUsers");
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
        var ccdCaseDetails = ccdRequest.getCaseDetails();
        var caseData = ccdCaseDetails.getCaseData();

        try {
            refreshSharedUsersService.refreshSharedUsers(ccdCaseDetails);
            return toCallbackResponse(getCallbackRespEntityNoErrors(caseData));
        } catch (Exception exception) {
            return toCallbackResponse(getCallbackRespEntityErrors(List.of(exception.getMessage()), caseData));
        }
    }
}
