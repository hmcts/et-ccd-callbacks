package uk.gov.hmcts.ethos.replacement.docmosis.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.ccd.sdk.CallbackRequestContext;
import uk.gov.hmcts.ccd.sdk.CallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.et.common.model.ccd.CaseData;
import uk.gov.hmcts.ethos.replacement.docmosis.helpers.CaseDetailsConverter;
import uk.gov.hmcts.ethos.replacement.docmosis.service.CaseLinksEmailService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.FeatureToggleService;
import uk.gov.hmcts.ethos.replacement.docmosis.service.VerifyTokenService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static uk.gov.hmcts.ecm.common.model.helper.Constants.NO;

@Component
public class MaintainCaseLinkCallbackHandler extends CallbackHandlerBase {

    private final VerifyTokenService verifyTokenService;
    private final CaseLinksEmailService caseLinksEmailService;
    private final FeatureToggleService featureToggleService;

    @Autowired
    public MaintainCaseLinkCallbackHandler(
        CaseDetailsConverter caseDetailsConverter,
        VerifyTokenService verifyTokenService,
        CaseLinksEmailService caseLinksEmailService,
        FeatureToggleService featureToggleService
    ) {
        super(caseDetailsConverter);
        this.verifyTokenService = verifyTokenService;
        this.caseLinksEmailService = caseLinksEmailService;
        this.featureToggleService = featureToggleService;
    }

    @Override
    public List<String> getHandledCaseTypeIds() {
        return List.of("ET_Scotland", "ET_EnglandWales");
    }

    @Override
    public List<String> getHandledEventIds() {
        return List.of("maintainCaseLink");
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
        String authorizationToken = CallbackRequestContext.getAuthorizationToken().orElse(null);
        return toCallbackResponse(maintainCaseLinkAboutToSubmit(
                    toCcdRequest(caseDetails),
                    authorizationToken
                ));
    }

    private ResponseEntity<CCDCallbackResponse> maintainCaseLinkAboutToSubmit(
        uk.gov.hmcts.et.common.model.ccd.CCDRequest request,
        String userToken
    ) {
        if (!verifyTokenService.verifyTokenSignature(userToken)) {
            return ResponseEntity.status(FORBIDDEN.value()).build();
        }

        caseLinksEmailService.sendMailWhenCaseLinkForHearing(request, userToken, false);
        CaseData caseData = request.getCaseDetails().getCaseData();
        if (featureToggleService.isHmcEnabled() && CollectionUtils.isEmpty(caseData.getCaseLinks())) {
            caseData.setHearingIsLinkedFlag(NO);
        }

        return ResponseEntity.ok(CCDCallbackResponse.builder()
            .data(request.getCaseDetails().getCaseData())
            .build());
    }
}
