package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.et.common.model.ccd.CCDRequest;
import uk.gov.hmcts.et.common.model.multiples.MultipleRequest;
import uk.gov.hmcts.ethos.replacement.docmosis.controllers.multiples.CloseReferralMultiplesController;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import java.util.List;

@Component
public class CloseReferralMultiplesCloseReferralCallbackHandler extends MultipleCallbackHandlerBase {

    private final CloseReferralMultiplesController closeReferralMultiplesController;

    @Autowired
    public CloseReferralMultiplesCloseReferralCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        CloseReferralMultiplesController closeReferralMultiplesController
    ) {
        super(caseDetailsConverter);
        this.closeReferralMultiplesController = closeReferralMultiplesController;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland_Multiple", "ET_EnglandWales_Multiple");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("closeReferral");
    }

    @Override
    public boolean acceptsAboutToSubmit() {
        return true;
    }

    @Override
    public boolean acceptsSubmitted() {
        return true;
    }

    @Override
    Object aboutToSubmit(MultipleRequest multipleRequest) {
        return closeReferralMultiplesController.aboutToSubmitCloseReferral(multipleRequest);
    }

    @Override
    Object submitted(MultipleRequest multipleRequest) {
        return closeReferralMultiplesController.completeInitialConsideration(toCcdRequestFromMultiple(multipleRequest));
    }

    private CCDRequest toCcdRequestFromMultiple(MultipleRequest multipleRequest) {
        var caseDetails = new uk.gov.hmcts.et.common.model.ccd.CaseDetails();
        caseDetails.setCaseId(multipleRequest.getCaseDetails().getCaseId());
        CCDRequest ccdRequest = new CCDRequest();
        ccdRequest.setCaseDetails(caseDetails);
        return ccdRequest;
    }
}
